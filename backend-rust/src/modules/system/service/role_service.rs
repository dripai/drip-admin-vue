use crate::common::{AppError, I64String, PageResult, next_id};
use crate::modules::system::dto::role_request::{RoleQuery, RoleSaveRequest};
use crate::modules::system::entity::sys_role::SysRole;
use crate::modules::system::entity::sys_user::SysUser;
use crate::modules::system::vo::role_permission_vo::RolePermissionVo;
use rbatis::RBatis;
use serde::Deserialize;
use std::collections::BTreeSet;
use std::sync::Arc;

#[derive(Debug, Deserialize)]
struct CountRow {
    total: i64,
}

#[derive(Debug, Deserialize)]
struct IdRow {
    id: I64String,
}

pub async fn list(
    database: Option<&Arc<RBatis>>,
    query: &RoleQuery,
) -> Result<PageResult<SysRole>, AppError> {
    let database = require_database(database)?;
    let params = query.normalize_page()?;
    let offset = ((params.page - 1) * params.page_size) as i64;
    let (where_sql, args) = role_where(query);

    let mut page_args = args.clone();
    page_args.push(rbs::value!(params.page_size));
    page_args.push(rbs::value!(offset));
    let rows: Vec<SysRole> = database
        .exec_decode(
            &format!(
                "select id, role_name, role_code, builtin, status, remark, deleted, created_at, updated_at from sys_role {where_sql} order by created_at desc limit ? offset ?"
            ),
            page_args,
        )
        .await
        .map_err(map_database_error)?;
    let total = count(
        database,
        &format!("select count(*) as total from sys_role {where_sql}"),
        args,
    )
    .await?;
    Ok(PageResult {
        list: rows,
        total: I64String(total),
        page: params.page,
        page_size: params.page_size,
    })
}

pub async fn options(database: Option<&Arc<RBatis>>) -> Result<Vec<SysRole>, AppError> {
    let database = require_database(database)?;
    database
        .exec_decode(
            "select id, role_name, role_code, builtin, status, remark, deleted, created_at, updated_at from sys_role where deleted = 0 order by created_at desc",
            vec![],
        )
        .await
        .map_err(map_database_error)
}

pub async fn detail(database: Option<&Arc<RBatis>>, id: i64) -> Result<SysRole, AppError> {
    let database = require_database(database)?;
    detail_with_database(database, id).await
}

pub async fn users(
    database: Option<&Arc<RBatis>>,
    role_id: i64,
    query: &RoleQuery,
) -> Result<PageResult<SysUser>, AppError> {
    let database = require_database(database)?;
    detail_with_database(database, role_id).await?;
    let params = query.normalize_page()?;
    let user_ids = role_user_ids(database, role_id).await?;
    if user_ids.is_empty() {
        return Ok(PageResult::empty(params));
    }

    let offset = ((params.page - 1) * params.page_size) as i64;
    let placeholders = placeholders(user_ids.len());
    let mut args: Vec<rbs::Value> = user_ids.iter().map(|id| rbs::value!(id.value())).collect();
    args.push(rbs::value!(params.page_size));
    args.push(rbs::value!(offset));
    let rows: Vec<SysUser> = database
        .exec_decode(
            &format!(
                "select id, username, password_hash, password_salt, real_name, phone, email, avatar, status, dept_id, deleted, created_at, updated_at from sys_user where deleted = 0 and id in ({placeholders}) order by created_at desc limit ? offset ?"
            ),
            args,
        )
        .await
        .map_err(map_database_error)?;
    let total = count(
        database,
        &format!(
            "select count(*) as total from sys_user where deleted = 0 and id in ({placeholders})"
        ),
        user_ids.iter().map(|id| rbs::value!(id.value())).collect(),
    )
    .await?;
    Ok(PageResult {
        list: rows,
        total: I64String(total),
        page: params.page,
        page_size: params.page_size,
    })
}

pub async fn permissions(
    database: Option<&Arc<RBatis>>,
    role_id: i64,
) -> Result<RolePermissionVo, AppError> {
    let database = require_database(database)?;
    detail_with_database(database, role_id).await?;
    let rows: Vec<IdRow> = database
        .exec_decode(
            "select menu_id as id from sys_role_menu where role_id = ? order by menu_id asc",
            vec![rbs::value!(role_id)],
        )
        .await
        .map_err(map_database_error)?;
    Ok(RolePermissionVo {
        menu_ids: rows.into_iter().map(|row| row.id).collect(),
        permission_codes: Vec::new(),
    })
}

pub async fn create(
    database: Option<&Arc<RBatis>>,
    request: RoleSaveRequest,
) -> Result<I64String, AppError> {
    let database = require_database(database)?;
    validate_request(&request)?;
    let id = next_id();
    database
        .exec(
            "insert into sys_role (id, role_name, role_code, builtin, status, remark, deleted) values (?, ?, ?, 0, ?, ?, 0)",
            vec![
                rbs::value!(id),
                rbs::value!(request.role_name),
                rbs::value!(request.role_code),
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
    request: RoleSaveRequest,
) -> Result<(), AppError> {
    let database = require_database(database)?;
    detail_with_database(database, id).await?;
    validate_request(&request)?;
    database
        .exec(
            "update sys_role set role_name = ?, role_code = ?, status = ?, remark = ? where id = ? and deleted = 0",
            vec![
                rbs::value!(request.role_name),
                rbs::value!(request.role_code),
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
    let role = detail_with_database(database, id).await?;
    if role.builtin == 1 {
        return Err(AppError::bad_request("operation failed"));
    }
    let user_count = count(
        database,
        "select count(*) as total from sys_user_role where role_id = ?",
        vec![rbs::value!(id)],
    )
    .await?;
    if user_count > 0 {
        return Err(AppError::conflict("operation failed"));
    }
    database
        .exec(
            "update sys_role set deleted = 1 where id = ? and deleted = 0",
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
    detail_with_database(database, id).await?;
    database
        .exec(
            "update sys_role set status = ? where id = ? and deleted = 0",
            vec![rbs::value!(status), rbs::value!(id)],
        )
        .await
        .map_err(map_database_error)?;
    Ok(())
}

pub async fn assign_permissions(
    database: Option<&Arc<RBatis>>,
    role_id: i64,
    menu_ids: Vec<i64>,
) -> Result<(), AppError> {
    let database = require_database(database)?;
    detail_with_database(database, role_id).await?;
    assert_existing_menus(database, &menu_ids).await?;
    database
        .exec(
            "delete from sys_role_menu where role_id = ?",
            vec![rbs::value!(role_id)],
        )
        .await
        .map_err(map_database_error)?;
    for menu_id in menu_ids {
        database
            .exec(
                "insert into sys_role_menu (id, role_id, menu_id) values (?, ?, ?)",
                vec![
                    rbs::value!(next_id()),
                    rbs::value!(role_id),
                    rbs::value!(menu_id),
                ],
            )
            .await
            .map_err(map_database_error)?;
    }
    Ok(())
}

async fn detail_with_database(database: &RBatis, id: i64) -> Result<SysRole, AppError> {
    let rows: Vec<SysRole> = database
        .exec_decode(
            "select id, role_name, role_code, builtin, status, remark, deleted, created_at, updated_at from sys_role where id = ? and deleted = 0",
            vec![rbs::value!(id)],
        )
        .await
        .map_err(map_database_error)?;
    rows.into_iter()
        .next()
        .ok_or_else(|| AppError::not_found("资源不存在"))
}

async fn role_user_ids(database: &RBatis, role_id: i64) -> Result<Vec<I64String>, AppError> {
    let rows: Vec<IdRow> = database
        .exec_decode(
            "select user_id as id from sys_user_role where role_id = ? order by user_id asc",
            vec![rbs::value!(role_id)],
        )
        .await
        .map_err(map_database_error)?;
    Ok(rows.into_iter().map(|row| row.id).collect())
}

async fn assert_existing_menus(database: &RBatis, menu_ids: &[i64]) -> Result<(), AppError> {
    if menu_ids.is_empty() {
        return Ok(());
    }
    let unique_ids: BTreeSet<i64> = menu_ids.iter().copied().collect();
    if unique_ids.len() != menu_ids.len() {
        return Err(AppError::bad_request("operation failed"));
    }
    let total = count(
        database,
        &format!(
            "select count(*) as total from sys_menu where deleted = 0 and id in ({})",
            placeholders(unique_ids.len())
        ),
        unique_ids.iter().map(|id| rbs::value!(*id)).collect(),
    )
    .await?;
    if total != unique_ids.len() as i64 {
        return Err(AppError::bad_request("operation failed"));
    }
    Ok(())
}

fn role_where(query: &RoleQuery) -> (String, Vec<rbs::Value>) {
    let mut where_sql = String::from("where deleted = 0");
    let mut args = Vec::new();
    if let Some(role_name) = non_blank(query.role_name.as_deref()) {
        where_sql.push_str(" and role_name like ?");
        args.push(rbs::value!(format!("%{role_name}%")));
    }
    if let Some(role_code) = non_blank(query.role_code.as_deref()) {
        where_sql.push_str(" and role_code like ?");
        args.push(rbs::value!(format!("%{role_code}%")));
    }
    if let Some(status) = query.status {
        where_sql.push_str(" and status = ?");
        args.push(rbs::value!(status));
    }
    if let Some(created_at) = non_blank(query.created_at.as_deref()) {
        where_sql.push_str(" and created_at like ?");
        args.push(rbs::value!(format!("%{created_at}%")));
    }
    (where_sql, args)
}

async fn count(database: &RBatis, sql: &str, args: Vec<rbs::Value>) -> Result<i64, AppError> {
    let counts: Vec<CountRow> = database
        .exec_decode(sql, args)
        .await
        .map_err(map_database_error)?;
    Ok(counts.first().map(|row| row.total).unwrap_or(0))
}

fn require_database(database: Option<&Arc<RBatis>>) -> Result<&RBatis, AppError> {
    database
        .map(|database| database.as_ref())
        .ok_or_else(|| AppError::system("Rbatis database is not configured"))
}

fn validate_request(request: &RoleSaveRequest) -> Result<(), AppError> {
    require_text(&request.role_name, "roleName")?;
    require_text(&request.role_code, "roleCode")?;
    validate_length(&request.role_name, "roleName", 64)?;
    if !valid_role_code(&request.role_code) {
        return Err(AppError::bad_request("roleCode format is invalid"));
    }
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

fn valid_role_code(value: &str) -> bool {
    let mut chars = value.chars();
    matches!(chars.next(), Some(first) if first.is_ascii_uppercase())
        && value.chars().count() <= 64
        && value.chars().count() >= 2
        && chars.all(|ch| ch.is_ascii_uppercase() || ch.is_ascii_digit() || ch == '_')
}

fn placeholders(len: usize) -> String {
    vec!["?"; len].join(",")
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
