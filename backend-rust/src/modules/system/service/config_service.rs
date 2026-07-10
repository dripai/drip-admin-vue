use crate::common::{AppError, I64String, PageResult, next_id};
use crate::modules::system::dto::config_request::{ConfigQuery, ConfigSaveRequest};
use crate::modules::system::entity::sys_config::SysConfig;
use rbatis::RBatis;
use serde::Deserialize;
use serde_json::{Value, json};
use std::collections::BTreeMap;
use std::sync::Arc;

#[derive(Debug, Deserialize)]
struct CountRow {
    total: i64,
}

pub async fn public_config(database: Option<&Arc<RBatis>>) -> Result<Value, AppError> {
    let database = require_database(database)?;
    let rows: Vec<SysConfig> = database
        .exec_decode(
            "select id, config_name, config_key, config_value, value_type, builtin, status, remark, deleted from sys_config where deleted = 0 and config_key in (?, ?, ?, ?, ?) and (builtin = 1 or status = 1)",
            vec![
                rbs::value!("system.name"),
                rbs::value!("system.company.fullName"),
                rbs::value!("system.logo"),
                rbs::value!("system.watermark.enabled"),
                rbs::value!("print.silent.enabled"),
            ],
        )
        .await
        .map_err(map_database_error)?;
    let values: BTreeMap<String, String> = rows
        .into_iter()
        .map(|row| (row.config_key, row.config_value))
        .collect();
    Ok(json!({
        "systemName": required_value(&values, "system.name")?,
        "companyFullName": values.get("system.company.fullName").cloned().unwrap_or_default(),
        "logoUrl": values.get("system.logo").cloned().unwrap_or_default(),
        "watermarkEnabled": values.get("system.watermark.enabled").cloned().unwrap_or_else(|| "false".to_string()),
        "silentPrintEnabled": values.get("print.silent.enabled").cloned().unwrap_or_else(|| "false".to_string()),
    }))
}

pub async fn list(
    database: Option<&Arc<RBatis>>,
    query: &ConfigQuery,
) -> Result<PageResult<SysConfig>, AppError> {
    let database = require_database(database)?;
    let params = query.normalize_page()?;
    let offset = ((params.page - 1) * params.page_size) as i64;
    let mut where_sql = String::from(" where deleted = 0");
    let mut args = Vec::new();
    if let Some(config_name) = non_blank(query.config_name.as_deref()) {
        where_sql.push_str(" and config_name like ?");
        args.push(rbs::value!(format!("%{config_name}%")));
    }
    if let Some(config_key) = non_blank(query.config_key.as_deref()) {
        where_sql.push_str(" and config_key like ?");
        args.push(rbs::value!(format!("%{config_key}%")));
    }
    if let Some(status) = query.status {
        where_sql.push_str(" and status = ?");
        args.push(rbs::value!(status));
    }

    let mut page_args = args.clone();
    page_args.push(rbs::value!(params.page_size));
    page_args.push(rbs::value!(offset));
    let rows: Vec<SysConfig> = database
        .exec_decode(
            &format!(
                "select id, config_name, config_key, config_value, value_type, builtin, status, remark, deleted from sys_config {where_sql} order by created_at desc limit ? offset ?"
            ),
            page_args,
        )
        .await
        .map_err(map_database_error)?;
    let counts: Vec<CountRow> = database
        .exec_decode(
            &format!("select count(*) as total from sys_config {where_sql}"),
            args,
        )
        .await
        .map_err(map_database_error)?;
    Ok(PageResult {
        list: rows,
        total: I64String(counts.first().map(|row| row.total).unwrap_or(0)),
        page: params.page,
        page_size: params.page_size,
    })
}

pub async fn detail(database: Option<&Arc<RBatis>>, id: i64) -> Result<SysConfig, AppError> {
    let database = require_database(database)?;
    detail_with_database(database, id).await
}

pub async fn create(
    database: Option<&Arc<RBatis>>,
    request: ConfigSaveRequest,
) -> Result<I64String, AppError> {
    let database = require_database(database)?;
    validate_request(&request, true)?;
    let id = next_id();
    let value_type = type_or_default(request.value_type.as_deref()).to_string();
    database
        .exec(
            "insert into sys_config (id, config_name, config_key, config_value, value_type, builtin, status, remark, deleted) values (?, ?, ?, ?, ?, 0, ?, ?, 0)",
            vec![
                rbs::value!(id),
                rbs::value!(request.config_name),
                rbs::value!(request.config_key),
                rbs::value!(request.config_value.unwrap_or_default()),
                rbs::value!(value_type),
                rbs::value!(request.status.unwrap_or(1)),
                rbs::value!(request.remark),
            ],
        )
        .await
        .map_err(map_database_error)?;
    Ok(I64String(id))
}

pub async fn update(
    database: Option<&Arc<RBatis>>,
    id: i64,
    request: ConfigSaveRequest,
) -> Result<(), AppError> {
    let database = require_database(database)?;
    detail_with_database(database, id).await?;
    validate_request(&request, false)?;
    let value_type = type_or_default(request.value_type.as_deref()).to_string();
    database
        .exec(
            "update sys_config set config_name = ?, config_key = ?, config_value = ?, value_type = ?, status = ?, remark = ? where id = ? and deleted = 0",
            vec![
                rbs::value!(request.config_name),
                rbs::value!(request.config_key),
                rbs::value!(request.config_value.unwrap_or_default()),
                rbs::value!(value_type),
                rbs::value!(request.status.unwrap_or(1)),
                rbs::value!(request.remark),
                rbs::value!(id),
            ],
        )
        .await
        .map_err(map_database_error)?;
    Ok(())
}

pub async fn delete(database: Option<&Arc<RBatis>>, id: i64) -> Result<(), AppError> {
    let database = require_database(database)?;
    let current = detail_with_database(database, id).await?;
    if current.builtin == 1 {
        return Err(AppError::bad_request("operation failed"));
    }
    database
        .exec(
            "update sys_config set deleted = 1 where id = ? and deleted = 0",
            vec![rbs::value!(id)],
        )
        .await
        .map_err(map_database_error)?;
    Ok(())
}

pub async fn update_status(
    database: Option<&Arc<RBatis>>,
    id: i64,
    status: i32,
) -> Result<(), AppError> {
    let database = require_database(database)?;
    let current = detail_with_database(database, id).await?;
    if current.builtin == 1 {
        return Err(AppError::bad_request("operation failed"));
    }
    database
        .exec(
            "update sys_config set status = ? where id = ? and deleted = 0",
            vec![rbs::value!(status), rbs::value!(id)],
        )
        .await
        .map_err(map_database_error)?;
    Ok(())
}

async fn detail_with_database(database: &RBatis, id: i64) -> Result<SysConfig, AppError> {
    let rows: Vec<SysConfig> = database
        .exec_decode(
            "select id, config_name, config_key, config_value, value_type, builtin, status, remark, deleted from sys_config where id = ? and deleted = 0",
            vec![rbs::value!(id)],
        )
        .await
        .map_err(map_database_error)?;
    rows.into_iter()
        .next()
        .ok_or_else(|| AppError::not_found("资源不存在"))
}

fn require_database(database: Option<&Arc<RBatis>>) -> Result<&RBatis, AppError> {
    database
        .map(|database| database.as_ref())
        .ok_or_else(|| AppError::system("Rbatis database is not configured"))
}

fn required_value(values: &BTreeMap<String, String>, key: &str) -> Result<String, AppError> {
    let value = values.get(key).cloned().unwrap_or_default();
    if value.trim().is_empty() {
        return Err(AppError::system(format!("system config missing: {key}")));
    }
    Ok(value)
}

fn validate_request(request: &ConfigSaveRequest, require_value: bool) -> Result<(), AppError> {
    require_text(&request.config_name, "configName")?;
    require_text(&request.config_key, "configKey")?;
    if require_value && request.config_value.is_none() {
        return Err(AppError::bad_request("configValue is required"));
    }
    let value_type = type_or_default(request.value_type.as_deref());
    if let Some(value) = request.config_value.as_deref() {
        validate_value(value_type, value)?;
    }
    Ok(())
}

fn require_text(value: &str, field: &str) -> Result<(), AppError> {
    if value.trim().is_empty() {
        return Err(AppError::bad_request(format!("{field} is required")));
    }
    Ok(())
}

fn type_or_default(value_type: Option<&str>) -> &str {
    match value_type {
        Some(value) if !value.trim().is_empty() => value,
        _ => "string",
    }
}

fn validate_value(value_type: &str, value: &str) -> Result<(), AppError> {
    match value_type {
        "string" => Ok(()),
        "boolean" if value.eq_ignore_ascii_case("true") || value.eq_ignore_ascii_case("false") => {
            Ok(())
        }
        "boolean" => Err(AppError::bad_request("configValue must be true or false")),
        "number" => value
            .parse::<f64>()
            .map(|_| ())
            .map_err(|_| AppError::bad_request("configValue must be number")),
        _ => Err(AppError::bad_request(
            "valueType must be string, boolean or number",
        )),
    }
}

fn non_blank(value: Option<&str>) -> Option<&str> {
    value.map(str::trim).filter(|value| !value.is_empty())
}

fn map_database_error(err: rbatis::Error) -> AppError {
    let message = err.to_string();
    if message.to_ascii_lowercase().contains("duplicate") {
        AppError::conflict("数据冲突")
    } else {
        AppError::system(message)
    }
}
