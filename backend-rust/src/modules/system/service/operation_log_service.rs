use crate::common::{AppError, I64String, PageResult};
use crate::modules::system::dto::log_request::LogQuery;
use crate::modules::system::vo::operation_log_vo::OperationLogVo;
use rbatis::RBatis;
use serde::Deserialize;
use std::sync::Arc;

#[derive(Deserialize)]
struct Count {
    total: i64,
}

pub async fn list(
    db: Option<&Arc<RBatis>>,
    query: &LogQuery,
) -> Result<PageResult<OperationLogVo>, AppError> {
    let db = database(db)?;
    let page = query.page()?;
    let (where_sql, args) = where_clause(query);
    let mut page_args = args.clone();
    page_args.push(rbs::value!(page.page_size));
    page_args.push(rbs::value!(((page.page - 1) * page.page_size) as i64));
    let rows: Vec<OperationLogVo> = db
        .exec_decode(
            &format!(
                "select id, operator_id, operator_name as operator, module, action, method, path, request_params, response_status as status, error_message, cost_ms as duration, created_at from sys_operation_log {where_sql} order by created_at desc, id desc limit ? offset ?"
            ),
            page_args,
        )
        .await
        .map_err(map_database_error)?;
    let totals: Vec<Count> = db
        .exec_decode(
            &format!("select count(*) as total from sys_operation_log {where_sql}"),
            args,
        )
        .await
        .map_err(map_database_error)?;

    Ok(PageResult {
        list: rows,
        total: I64String(totals.first().map(|row| row.total).unwrap_or(0)),
        page: page.page,
        page_size: page.page_size,
    })
}

pub async fn detail(db: Option<&Arc<RBatis>>, id: i64) -> Result<OperationLogVo, AppError> {
    let rows: Vec<OperationLogVo> = database(db)?
        .exec_decode(
            "select id, operator_id, operator_name as operator, module, action, method, path, request_params, response_status as status, error_message, cost_ms as duration, created_at from sys_operation_log where id = ?",
            vec![rbs::value!(id)],
        )
        .await
        .map_err(map_database_error)?;
    rows.into_iter()
        .next()
        .ok_or_else(|| AppError::not_found("资源不存在"))
}

fn where_clause(query: &LogQuery) -> (String, Vec<rbs::Value>) {
    let mut sql = String::new();
    let mut args = Vec::new();
    for (column, value) in [
        ("operator_name", query.operator.as_deref()),
        ("module", query.module.as_deref()),
        ("action", query.action.as_deref()),
        ("response_status", query.status.as_deref()),
        ("path", query.path.as_deref()),
    ] {
        if let Some(value) = value.map(str::trim).filter(|value| !value.is_empty()) {
            sql.push_str(if args.is_empty() { " where " } else { " and " });
            sql.push_str(column);
            sql.push_str(" like ?");
            args.push(rbs::value!(format!("%{value}%")));
        }
    }
    for (operator, value) in [
        (">=", query.created_from.as_deref()),
        ("<=", query.created_to.as_deref()),
    ] {
        if let Some(value) = value.map(str::trim).filter(|value| !value.is_empty()) {
            sql.push_str(if args.is_empty() { " where " } else { " and " });
            sql.push_str("created_at ");
            sql.push_str(operator);
            sql.push_str(" ?");
            args.push(rbs::value!(value));
        }
    }
    (sql, args)
}

fn database(db: Option<&Arc<RBatis>>) -> Result<&RBatis, AppError> {
    db.map(AsRef::as_ref)
        .ok_or_else(|| AppError::system("Rbatis database is not configured"))
}

fn map_database_error(error: rbatis::Error) -> AppError {
    AppError::system(error.to_string())
}
