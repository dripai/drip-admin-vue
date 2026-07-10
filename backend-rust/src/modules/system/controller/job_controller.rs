use super::{ok, ok_null};
use crate::common::{ApiResponse, AppError, PageQuery, PageResult};
use crate::modules::system::service::job_service;
use axum::Json;
use axum::extract::{Path, Query};
use serde_json::Value;

pub async fn list(
    Query(query): Query<PageQuery>,
) -> Result<Json<ApiResponse<PageResult<Value>>>, AppError> {
    Ok(ok(job_service::list(query.normalize()?).await?))
}

pub async fn scripts() -> Result<Json<ApiResponse<Vec<String>>>, AppError> {
    Ok(ok(Vec::new()))
}
pub async fn detail(Path(_id): Path<i64>) -> Result<Json<ApiResponse<Value>>, AppError> {
    Ok(ok(job_service::detail().await?))
}
pub async fn create() -> Result<Json<ApiResponse<()>>, AppError> {
    job_service::mutate().await?;
    Ok(ok_null())
}
pub async fn update(Path(_id): Path<i64>) -> Result<Json<ApiResponse<()>>, AppError> {
    job_service::mutate().await?;
    Ok(ok_null())
}
pub async fn delete(Path(_id): Path<i64>) -> Result<Json<ApiResponse<()>>, AppError> {
    job_service::mutate().await?;
    Ok(ok_null())
}
pub async fn status(Path(_id): Path<i64>) -> Result<Json<ApiResponse<()>>, AppError> {
    job_service::mutate().await?;
    Ok(ok_null())
}
pub async fn run(Path(_id): Path<i64>) -> Result<Json<ApiResponse<()>>, AppError> {
    job_service::mutate().await?;
    Ok(ok_null())
}
pub async fn run_logs(
    Query(query): Query<PageQuery>,
    Path(_id): Path<i64>,
) -> Result<Json<ApiResponse<PageResult<Value>>>, AppError> {
    Ok(ok(job_service::run_logs(query.normalize()?).await?))
}
pub async fn all_run_logs(
    Query(query): Query<PageQuery>,
) -> Result<Json<ApiResponse<PageResult<Value>>>, AppError> {
    Ok(ok(job_service::run_logs(query.normalize()?).await?))
}
