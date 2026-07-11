use crate::common::{AppError, I64String, PageResult, hash_password, next_id};
use crate::modules::system::dto::user_request::{UserQuery, UserSaveRequest};
use crate::modules::system::entity::sys_user::SysUser;
use crate::modules::system::vo::dept_summary_vo::DeptSummaryVo;
use crate::modules::system::vo::role_summary_vo::RoleSummaryVo;
use crate::modules::system::vo::user_list_vo::UserListVo;
use rbatis::RBatis;
use serde::Deserialize;
use std::collections::BTreeSet;
use std::sync::Arc;
use uuid::Uuid;

#[derive(Debug, Deserialize)]
struct CountRow { total: i64 }

pub async fn list(database: Option<&Arc<RBatis>>, query: &UserQuery) -> Result<PageResult<UserListVo>, AppError> {
    let database = require_database(database)?;
    let params = query.normalize_page()?;
    let (where_sql, args) = user_where(query);
    let mut page_args = args.clone();
    page_args.push(rbs::value!(params.page_size));
    page_args.push(rbs::value!(((params.page - 1) * params.page_size) as i64));
    let users: Vec<SysUser> = database.exec_decode(
        &format!("select id, username, password_hash, password_salt, real_name, phone, email, avatar, status, dept_id, remark, last_login_at, deleted, created_at, updated_at from sys_user {where_sql} order by created_at desc limit ? offset ?"), page_args
    ).await.map_err(map_database_error)?;
    let total = count(database, &format!("select count(*) as total from sys_user {where_sql}"), args).await?;
    let mut list = Vec::with_capacity(users.len());
    for user in users { list.push(to_list_vo(database, user).await?); }
    Ok(PageResult { list, total: I64String(total), page: params.page, page_size: params.page_size })
}

pub async fn detail(database: Option<&Arc<RBatis>>, id: i64) -> Result<SysUser, AppError> {
    find_user(require_database(database)?, id).await
}

pub async fn create(database: Option<&Arc<RBatis>>, request: UserSaveRequest) -> Result<I64String, AppError> {
    let database = require_database(database)?;
    validate_save(database, &request).await?;
    if count(database, "select count(*) as total from sys_user where username = ? and deleted = 0", vec![rbs::value!(request.username.trim())]).await? > 0 { return Err(AppError::conflict("username already exists")); }
    let password = request.password.as_deref().map(str::trim).filter(|v| !v.is_empty()).unwrap_or("Admin@123456");
    validate_password(password)?;
    let id = next_id(); let salt = format!("salt{}", Uuid::new_v4().simple());
    database.exec("insert into sys_user (id, username, password_hash, password_salt, real_name, phone, email, status, dept_id, remark, deleted) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)", vec![
        rbs::value!(id), rbs::value!(request.username.trim()), rbs::value!(hash_password(password, &salt)), rbs::value!(salt), rbs::value!(request.real_name.trim()), rbs::value!(empty_to_none(request.phone)), rbs::value!(empty_to_none(request.email)), rbs::value!(request.status.unwrap_or(1)), rbs::value!(request.dept_id), rbs::value!(empty_to_none(request.remark)),
    ]).await.map_err(map_database_error)?;
    Ok(I64String(id))
}

pub async fn update(database: Option<&Arc<RBatis>>, id: i64, request: UserSaveRequest) -> Result<(), AppError> {
    let database = require_database(database)?;
    find_user(database, id).await?;
    validate_save(database, &request).await?;
    if count(database, "select count(*) as total from sys_user where username = ? and id <> ? and deleted = 0", vec![rbs::value!(request.username.trim()), rbs::value!(id)]).await? > 0 { return Err(AppError::conflict("username already exists")); }
    database.exec("update sys_user set username = ?, real_name = ?, phone = ?, email = ?, status = ?, dept_id = ?, remark = ? where id = ? and deleted = 0", vec![rbs::value!(request.username.trim()), rbs::value!(request.real_name.trim()), rbs::value!(empty_to_none(request.phone)), rbs::value!(empty_to_none(request.email)), rbs::value!(request.status.unwrap_or(1)), rbs::value!(request.dept_id), rbs::value!(empty_to_none(request.remark)), rbs::value!(id)]).await.map_err(map_database_error)?;
    Ok(())
}

pub async fn delete(database: Option<&Arc<RBatis>>, current_user_id: i64, id: i64) -> Result<(), AppError> {
    if current_user_id == id { return Err(AppError::bad_request("operation failed")); }
    let database = require_database(database)?; assert_not_super_admin_target(database, current_user_id, id).await?; find_user(database, id).await?;
    database.exec("update sys_user set deleted = 1 where id = ? and deleted = 0", vec![rbs::value!(id)]).await.map_err(map_database_error)?; Ok(())
}

pub async fn update_status(database: Option<&Arc<RBatis>>, current_user_id: i64, id: i64, status: i32) -> Result<(), AppError> {
    if !matches!(status, 0 | 1) { return Err(AppError::bad_request("status is invalid")); }
    if current_user_id == id && status != 1 { return Err(AppError::bad_request("operation failed")); }
    let database = require_database(database)?; assert_not_super_admin_target(database, current_user_id, id).await?; find_user(database, id).await?;
    database.exec("update sys_user set status = ? where id = ? and deleted = 0", vec![rbs::value!(status), rbs::value!(id)]).await.map_err(map_database_error)?; Ok(())
}

pub async fn assign_roles(database: Option<&Arc<RBatis>>, current_user_id: i64, user_id: i64, role_ids: Vec<i64>) -> Result<(), AppError> {
    let database = require_database(database)?; assert_not_super_admin_target(database, current_user_id, user_id).await?; find_user(database, user_id).await?;
    let unique: BTreeSet<i64> = role_ids.iter().copied().collect(); if unique.len() != role_ids.len() { return Err(AppError::bad_request("operation failed")); }
    if !unique.is_empty() && count(database, &format!("select count(*) as total from sys_role where deleted = 0 and id in ({})", placeholders(unique.len())), unique.iter().map(|id| rbs::value!(*id)).collect()).await? != unique.len() as i64 { return Err(AppError::bad_request("operation failed")); }
    database.exec("delete from sys_user_role where user_id = ?", vec![rbs::value!(user_id)]).await.map_err(map_database_error)?;
    for role_id in unique { database.exec("insert into sys_user_role (id, user_id, role_id) values (?, ?, ?)", vec![rbs::value!(next_id()), rbs::value!(user_id), rbs::value!(role_id)]).await.map_err(map_database_error)?; }
    Ok(())
}

pub async fn reset_password(database: Option<&Arc<RBatis>>, current_user_id: i64, id: i64, password: String) -> Result<(), AppError> {
    let database = require_database(database)?; assert_not_super_admin_target(database, current_user_id, id).await?; find_user(database, id).await?;
    let password = if password.trim().is_empty() { "Admin@123456" } else { password.trim() }; validate_password(password)?;
    let salt = format!("salt{}", Uuid::new_v4().simple()); database.exec("update sys_user set password_salt = ?, password_hash = ? where id = ? and deleted = 0", vec![rbs::value!(&salt), rbs::value!(hash_password(password, &salt)), rbs::value!(id)]).await.map_err(map_database_error)?; Ok(())
}

async fn find_user(database: &RBatis, id: i64) -> Result<SysUser, AppError> { let rows: Vec<SysUser> = database.exec_decode("select id, username, password_hash, password_salt, real_name, phone, email, avatar, status, dept_id, remark, last_login_at, deleted, created_at, updated_at from sys_user where id = ? and deleted = 0", vec![rbs::value!(id)]).await.map_err(map_database_error)?; rows.into_iter().next().ok_or_else(|| AppError::not_found("资源不存在")) }
async fn assert_not_super_admin_target(database: &RBatis, current_user_id: i64, target_user_id: i64) -> Result<(), AppError> { if role_codes(database, current_user_id).await?.iter().any(|code| code == "SUPER_ADMIN") { return Ok(()); } if role_codes(database, target_user_id).await?.iter().any(|code| code == "SUPER_ADMIN") { return Err(AppError::forbidden("不能操作超级管理员")); } Ok(()) }
pub async fn role_codes(database: &RBatis, user_id: i64) -> Result<Vec<String>, AppError> { #[derive(Deserialize)] struct Row { role_code: String } database.exec_decode("select r.role_code from sys_role r inner join sys_user_role ur on ur.role_id = r.id where ur.user_id = ? and r.deleted = 0 and r.status = 1", vec![rbs::value!(user_id)]).await.map_err(map_database_error).map(|rows: Vec<Row>| rows.into_iter().map(|row| row.role_code).collect()) }
async fn to_list_vo(database: &RBatis, user: SysUser) -> Result<UserListVo, AppError> { #[derive(Deserialize)] struct Dept { id: I64String, dept_name: String } #[derive(Deserialize)] struct Role { id: I64String, role_name: String, role_code: String }
    let dept: Option<Dept> = if let Some(id) = user.dept_id.as_ref() { let rows: Vec<Dept> = database.exec_decode("select id, dept_name from sys_dept where id = ? and deleted = 0", vec![rbs::value!(id.value())]).await.map_err(map_database_error)?; rows.into_iter().next() } else { None };
    let roles: Vec<Role> = database.exec_decode("select r.id, r.role_name, r.role_code from sys_role r inner join sys_user_role ur on ur.role_id = r.id where ur.user_id = ? and r.deleted = 0", vec![rbs::value!(user.id.value())]).await.map_err(map_database_error)?;
    Ok(UserListVo { id: user.id, username: user.username, real_name: user.real_name, phone: user.phone, email: user.email, status: user.status, dept: dept.map(|d| DeptSummaryVo { id: d.id, dept_name: d.dept_name }), roles: roles.into_iter().map(|r| RoleSummaryVo { id: r.id, role_name: r.role_name, role_code: r.role_code }).collect(), created_at: user.created_at, last_login_at: user.last_login_at }) }
async fn validate_save(database: &RBatis, request: &UserSaveRequest) -> Result<(), AppError> { if request.username.trim().is_empty() { return Err(AppError::bad_request("username is required")); } if request.real_name.trim().is_empty() { return Err(AppError::bad_request("realName is required")); } if request.username.chars().count() > 64 || request.real_name.chars().count() > 64 { return Err(AppError::bad_request("username and realName length must be <= 64")); } if !matches!(request.status.unwrap_or(1), 0 | 1) { return Err(AppError::bad_request("status is invalid")); } if let Some(dept_id) = request.dept_id { if count(database, "select count(*) as total from sys_dept where id = ? and deleted = 0", vec![rbs::value!(dept_id)]).await? == 0 { return Err(AppError::bad_request("部门不存在")); } } Ok(()) }
fn validate_password(value: &str) -> Result<(), AppError> { if !(8..=64).contains(&value.chars().count()) { return Err(AppError::bad_request("password length must be 8 to 64")); } Ok(()) }
fn user_where(query: &UserQuery) -> (String, Vec<rbs::Value>) { let mut sql = String::from("where deleted = 0"); let mut args = Vec::new(); for (column, value) in [("username", query.username.as_deref()), ("real_name", query.real_name.as_deref()), ("phone", query.phone.as_deref())] { if let Some(value) = value.map(str::trim).filter(|v| !v.is_empty()) { sql.push_str(&format!(" and {column} like ?")); args.push(rbs::value!(format!("%{value}%"))); } } if let Some(status) = query.status { sql.push_str(" and status = ?"); args.push(rbs::value!(status)); } if let Some(dept_id) = query.dept_id { sql.push_str(" and dept_id = ?"); args.push(rbs::value!(dept_id)); } if let Some(role_id) = query.role_id { sql.push_str(" and id in (select user_id from sys_user_role where role_id = ?)"); args.push(rbs::value!(role_id)); } for (operator, value) in [(">=", query.created_from.as_deref()), ("<=", query.created_to.as_deref())] { if let Some(value) = value.map(str::trim).filter(|v| !v.is_empty()) { sql.push_str(&format!(" and created_at {operator} ?")); args.push(rbs::value!(value)); } } (sql, args) }
async fn count(database: &RBatis, sql: &str, args: Vec<rbs::Value>) -> Result<i64, AppError> { let rows: Vec<CountRow> = database.exec_decode(sql, args).await.map_err(map_database_error)?; Ok(rows.first().map(|row| row.total).unwrap_or(0)) }
fn require_database(database: Option<&Arc<RBatis>>) -> Result<&RBatis, AppError> { database.map(AsRef::as_ref).ok_or_else(|| AppError::system("Rbatis database is not configured")) }
fn placeholders(len: usize) -> String { vec!["?"; len].join(",") }
fn empty_to_none(value: Option<String>) -> Option<String> { value.and_then(|v| { let t = v.trim(); (!t.is_empty()).then(|| t.to_string()) }) }
fn map_database_error(err: rbatis::Error) -> AppError { if err.to_string().to_ascii_lowercase().contains("duplicate") { AppError::conflict("数据冲突") } else { AppError::system(err.to_string()) } }
