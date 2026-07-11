use crate::common::{AppError, I64String, PageResult};
use crate::modules::system::dto::log_request::LogQuery;
use rbatis::RBatis;
use serde::{Deserialize, Serialize};
use std::sync::Arc;

#[derive(Debug, Deserialize, Serialize, utoipa::ToSchema)]
#[serde(rename_all(serialize = "camelCase", deserialize = "snake_case"))]
pub struct LoginLog {
    pub id: I64String,
    pub user_id: Option<I64String>,
    pub username: String,
    pub real_name: Option<String>,
    pub login_type: String,
    pub status: String,
    pub failure_reason: Option<String>,
    pub ip: Option<String>,
    pub user_agent: Option<String>,
    pub device_type: Option<String>,
    pub login_at: Option<String>,
}

#[derive(Deserialize)]
struct Count {
    total: i64,
}

pub async fn list(
    db: Option<&Arc<RBatis>>,
    query: &LogQuery,
) -> Result<PageResult<LoginLog>, AppError> {
    let db = db
        .map(AsRef::as_ref)
        .ok_or_else(|| AppError::system("Rbatis database is not configured"))?;
    let page = query.page()?;
    let rows: Vec<LoginLog> = db
        .exec_decode(
            "select id, user_id, username, real_name, login_type, status, failure_reason, ip, user_agent, device_type, login_at from sys_login_log order by login_at desc limit ? offset ?",
            vec![
                rbs::value!(page.page_size),
                rbs::value!(((page.page - 1) * page.page_size) as i64),
            ],
        )
        .await
        .map_err(|error| AppError::system(error.to_string()))?;
    let totals: Vec<Count> = db
        .exec_decode("select count(*) total from sys_login_log", vec![])
        .await
        .map_err(|error| AppError::system(error.to_string()))?;

    Ok(PageResult {
        list: rows,
        total: I64String(totals.first().map(|row| row.total).unwrap_or(0)),
        page: page.page,
        page_size: page.page_size,
    })
}

pub async fn detail(db: Option<&Arc<RBatis>>, id: i64) -> Result<LoginLog, AppError> {
    let db = db
        .map(AsRef::as_ref)
        .ok_or_else(|| AppError::system("Rbatis database is not configured"))?;
    let rows: Vec<LoginLog> = db
        .exec_decode(
            "select id, user_id, username, real_name, login_type, status, failure_reason, ip, user_agent, device_type, login_at from sys_login_log where id = ?",
            vec![rbs::value!(id)],
        )
        .await
        .map_err(|error| AppError::system(error.to_string()))?;
    rows.into_iter()
        .next()
        .ok_or_else(|| AppError::not_found("资源不存在"))
}

#[allow(clippy::too_many_arguments)]
pub async fn write(
    db: Option<&Arc<RBatis>>,
    user_id: Option<i64>,
    username: &str,
    real_name: Option<&str>,
    login_type: &str,
    status: &str,
    reason: Option<&str>,
    device_type: &str,
    ip: Option<&str>,
    user_agent: Option<&str>,
) {
    let Some(db) = db.map(AsRef::as_ref) else {
        return;
    };
    let _ = db
        .exec(
            "insert into sys_login_log(id, user_id, username, real_name, login_type, status, failure_reason, ip, user_agent, device_type) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            vec![
                rbs::value!(crate::common::next_id()),
                rbs::value!(user_id),
                rbs::value!(username),
                rbs::value!(real_name),
                rbs::value!(login_type),
                rbs::value!(status),
                rbs::value!(reason),
                rbs::value!(ip),
                rbs::value!(user_agent),
                rbs::value!(device_type),
            ],
        )
        .await;
}
