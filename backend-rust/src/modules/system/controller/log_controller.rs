use super::ok;
use crate::common::{ApiResponse, AppError, PageQuery, PageResult};
use crate::modules::system::service::{login_log_service, operation_log_service};
use axum::Json;
use axum::extract::{Path, Query};
use serde_json::Value;

pub async fn login_logs(
    Query(query): Query<PageQuery>,
) -> Result<Json<ApiResponse<PageResult<Value>>>, AppError> {
    Ok(ok(login_log_service::list(query.normalize()?).await?))
}

pub async fn login_log(Path(_id): Path<i64>) -> Result<Json<ApiResponse<Value>>, AppError> {
    Ok(ok(login_log_service::detail().await?))
}

pub async fn operation_logs(
    Query(query): Query<PageQuery>,
) -> Result<Json<ApiResponse<PageResult<Value>>>, AppError> {
    Ok(ok(operation_log_service::list(query.normalize()?).await?))
}

pub async fn operation_log(Path(_id): Path<i64>) -> Result<Json<ApiResponse<Value>>, AppError> {
    Ok(ok(operation_log_service::detail().await?))
}
