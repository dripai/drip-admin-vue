use crate::common::{AppError, I64String, next_id};
use crate::modules::system::dto::dict_request::{DictItemSaveRequest, DictTypeSaveRequest};
use crate::modules::system::entity::sys_dict_item::SysDictItem;
use crate::modules::system::entity::sys_dict_type::SysDictType;
use rbatis::RBatis;
use serde::Deserialize;
use std::sync::Arc;

#[derive(Debug, Deserialize)]
struct CountRow {
    total: i64,
}

pub async fn type_list(database: Option<&Arc<RBatis>>) -> Result<Vec<SysDictType>, AppError> {
    let database = require_database(database)?;
    database
        .exec_decode(
            "select id, dict_name, dict_code, status, builtin, remark, created_at, updated_at from sys_dict_type order by created_at desc",
            vec![],
        )
        .await
        .map_err(map_database_error)
}

pub async fn item_list(
    database: Option<&Arc<RBatis>>,
    dict_type_id: i64,
) -> Result<Vec<SysDictItem>, AppError> {
    let database = require_database(database)?;
    type_detail_with_database(database, dict_type_id).await?;
    database
        .exec_decode(
            "select id, dict_type_id, label, value, is_default, status, sort, builtin, created_at, updated_at from sys_dict_item where dict_type_id = ? order by sort asc, id asc",
            vec![rbs::value!(dict_type_id)],
        )
        .await
        .map_err(map_database_error)
}

pub async fn create_type(
    database: Option<&Arc<RBatis>>,
    request: DictTypeSaveRequest,
) -> Result<I64String, AppError> {
    let database = require_database(database)?;
    validate_type_request(&request)?;
    let id = next_id();
    database
        .exec(
            "insert into sys_dict_type (id, dict_name, dict_code, status, builtin, remark) values (?, ?, ?, ?, ?, ?)",
            vec![
                rbs::value!(id),
                rbs::value!(request.dict_name),
                rbs::value!(request.dict_code),
                rbs::value!(request.status.unwrap_or(1)),
                rbs::value!(request.builtin.unwrap_or(0)),
                rbs::value!(request.remark),
            ],
        )
        .await
        .map_err(map_database_error)?;
    Ok(I64String(id))
}

pub async fn update_type(
    database: Option<&Arc<RBatis>>,
    id: i64,
    request: DictTypeSaveRequest,
) -> Result<(), AppError> {
    let database = require_database(database)?;
    validate_type_request(&request)?;
    let current = type_detail_with_database(database, id).await?;
    database
        .exec(
            "update sys_dict_type set dict_name = ?, dict_code = ?, status = ?, builtin = ?, remark = ? where id = ?",
            vec![
                rbs::value!(request.dict_name),
                rbs::value!(request.dict_code),
                rbs::value!(request.status.unwrap_or(current.status)),
                rbs::value!(request.builtin.unwrap_or(current.builtin)),
                rbs::value!(request.remark),
                rbs::value!(id),
            ],
        )
        .await
        .map_err(map_database_error)?;
    Ok(())
}

pub async fn delete_type(database: Option<&Arc<RBatis>>, id: i64) -> Result<(), AppError> {
    let database = require_database(database)?;
    let current = type_detail_with_database(database, id).await?;
    if current.builtin == 1 {
        return Err(AppError::bad_request("内置字典类型不能删除"));
    }
    let counts: Vec<CountRow> = database
        .exec_decode(
            "select count(*) as total from sys_dict_item where dict_type_id = ?",
            vec![rbs::value!(id)],
        )
        .await
        .map_err(map_database_error)?;
    if counts.first().map(|row| row.total).unwrap_or(0) > 0 {
        return Err(AppError::bad_request("字典类型下存在字典项，不能删除"));
    }
    database
        .exec(
            "delete from sys_dict_type where id = ?",
            vec![rbs::value!(id)],
        )
        .await
        .map_err(map_database_error)?;
    Ok(())
}

pub async fn create_item(
    database: Option<&Arc<RBatis>>,
    request: DictItemSaveRequest,
) -> Result<I64String, AppError> {
    let database = require_database(database)?;
    validate_item_request(&request)?;
    type_detail_with_database(database, request.dict_type_id).await?;
    if request.is_default == Some(1) {
        clear_default_item(database, request.dict_type_id, None).await?;
    }
    let id = next_id();
    database
        .exec(
            "insert into sys_dict_item (id, dict_type_id, label, value, is_default, sort, status, builtin) values (?, ?, ?, ?, ?, ?, ?, ?)",
            vec![
                rbs::value!(id),
                rbs::value!(request.dict_type_id),
                rbs::value!(request.label),
                rbs::value!(request.value),
                rbs::value!(request.is_default.unwrap_or(0)),
                rbs::value!(request.sort.unwrap_or(0)),
                rbs::value!(request.status.unwrap_or(1)),
                rbs::value!(request.builtin.unwrap_or(0)),
            ],
        )
        .await
        .map_err(map_database_error)?;
    Ok(I64String(id))
}

pub async fn update_item(
    database: Option<&Arc<RBatis>>,
    id: i64,
    request: DictItemSaveRequest,
) -> Result<(), AppError> {
    let database = require_database(database)?;
    validate_item_request(&request)?;
    let current = item_detail_with_database(database, id).await?;
    type_detail_with_database(database, request.dict_type_id).await?;
    if request.is_default == Some(1) {
        clear_default_item(database, request.dict_type_id, Some(id)).await?;
    }
    database
        .exec(
            "update sys_dict_item set dict_type_id = ?, label = ?, value = ?, is_default = ?, sort = ?, status = ?, builtin = ? where id = ?",
            vec![
                rbs::value!(request.dict_type_id),
                rbs::value!(request.label),
                rbs::value!(request.value),
                rbs::value!(request.is_default.unwrap_or(current.is_default)),
                rbs::value!(request.sort.unwrap_or(current.sort)),
                rbs::value!(request.status.unwrap_or(current.status)),
                rbs::value!(request.builtin.unwrap_or(current.builtin)),
                rbs::value!(id),
            ],
        )
        .await
        .map_err(map_database_error)?;
    Ok(())
}

pub async fn delete_item(database: Option<&Arc<RBatis>>, id: i64) -> Result<(), AppError> {
    let database = require_database(database)?;
    let current = item_detail_with_database(database, id).await?;
    if current.builtin == 1 {
        return Err(AppError::bad_request("内置字典项不能删除"));
    }
    database
        .exec(
            "delete from sys_dict_item where id = ?",
            vec![rbs::value!(id)],
        )
        .await
        .map_err(map_database_error)?;
    Ok(())
}

pub async fn update_item_status(
    database: Option<&Arc<RBatis>>,
    id: i64,
    status: i32,
) -> Result<(), AppError> {
    let database = require_database(database)?;
    item_detail_with_database(database, id).await?;
    database
        .exec(
            "update sys_dict_item set status = ? where id = ?",
            vec![rbs::value!(status), rbs::value!(id)],
        )
        .await
        .map_err(map_database_error)?;
    Ok(())
}

pub async fn refresh_cache(database: Option<&Arc<RBatis>>) -> Result<(), AppError> {
    let database = require_database(database)?;
    let _: Vec<CountRow> = database
        .exec_decode("select count(*) as total from sys_dict_type", vec![])
        .await
        .map_err(map_database_error)?;
    Ok(())
}

async fn type_detail_with_database(database: &RBatis, id: i64) -> Result<SysDictType, AppError> {
    let rows: Vec<SysDictType> = database
        .exec_decode(
            "select id, dict_name, dict_code, status, builtin, remark, created_at, updated_at from sys_dict_type where id = ?",
            vec![rbs::value!(id)],
        )
        .await
        .map_err(map_database_error)?;
    rows.into_iter()
        .next()
        .ok_or_else(|| AppError::not_found("资源不存在"))
}

async fn item_detail_with_database(database: &RBatis, id: i64) -> Result<SysDictItem, AppError> {
    let rows: Vec<SysDictItem> = database
        .exec_decode(
            "select id, dict_type_id, label, value, is_default, status, sort, builtin, created_at, updated_at from sys_dict_item where id = ?",
            vec![rbs::value!(id)],
        )
        .await
        .map_err(map_database_error)?;
    rows.into_iter()
        .next()
        .ok_or_else(|| AppError::not_found("资源不存在"))
}

async fn clear_default_item(
    database: &RBatis,
    dict_type_id: i64,
    exclude_id: Option<i64>,
) -> Result<(), AppError> {
    match exclude_id {
        Some(id) => {
            database
                .exec(
                    "update sys_dict_item set is_default = 0 where dict_type_id = ? and is_default = 1 and id <> ?",
                    vec![rbs::value!(dict_type_id), rbs::value!(id)],
                )
                .await
                .map_err(map_database_error)?;
        }
        None => {
            database
                .exec(
                    "update sys_dict_item set is_default = 0 where dict_type_id = ? and is_default = 1",
                    vec![rbs::value!(dict_type_id)],
                )
                .await
                .map_err(map_database_error)?;
        }
    }
    Ok(())
}

fn require_database(database: Option<&Arc<RBatis>>) -> Result<&RBatis, AppError> {
    database
        .map(|database| database.as_ref())
        .ok_or_else(|| AppError::system("Rbatis database is not configured"))
}

fn validate_type_request(request: &DictTypeSaveRequest) -> Result<(), AppError> {
    require_text(&request.dict_name, "dictName")?;
    require_text(&request.dict_code, "dictCode")?;
    validate_length(&request.dict_name, "dictName", 64)?;
    validate_length(&request.dict_code, "dictCode", 64)?;
    Ok(())
}

fn validate_item_request(request: &DictItemSaveRequest) -> Result<(), AppError> {
    if request.dict_type_id <= 0 {
        return Err(AppError::bad_request("dictTypeId is required"));
    }
    require_text(&request.label, "label")?;
    require_text(&request.value, "value")?;
    validate_length(&request.label, "label", 64)?;
    validate_length(&request.value, "value", 64)?;
    Ok(())
}

fn require_text(value: &str, field: &str) -> Result<(), AppError> {
    if value.trim().is_empty() {
        return Err(AppError::bad_request(format!("{field} is required")));
    }
    Ok(())
}

fn validate_length(value: &str, field: &str, max: usize) -> Result<(), AppError> {
    if value.chars().count() > max {
        return Err(AppError::bad_request(format!(
            "{field} length must be <= {max}"
        )));
    }
    Ok(())
}

fn map_database_error(err: rbatis::Error) -> AppError {
    let message = err.to_string();
    if message.to_ascii_lowercase().contains("duplicate") {
        AppError::conflict("数据冲突")
    } else {
        AppError::system(message)
    }
}
