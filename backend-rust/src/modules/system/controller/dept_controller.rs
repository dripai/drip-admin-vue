use super::{ok, ok_null};
use crate::common::{ApiResponse, AppError};
use crate::modules::system::service::dept_service;
use axum::Json;
use axum::extract::Path;
use serde_json::Value;

pub async fn list() -> Result<Json<ApiResponse<Vec<Value>>>, AppError> {
    Ok(ok(dept_service::list().await?))
}
pub async fn detail(Path(_id): Path<i64>) -> Result<Json<ApiResponse<Value>>, AppError> {
    Ok(ok(dept_service::detail().await?))
}
pub async fn create() -> Result<Json<ApiResponse<()>>, AppError> {
    dept_service::mutate().await?;
    Ok(ok_null())
}
pub async fn update(Path(_id): Path<i64>) -> Result<Json<ApiResponse<()>>, AppError> {
    dept_service::mutate().await?;
    Ok(ok_null())
}
pub async fn delete(Path(_id): Path<i64>) -> Result<Json<ApiResponse<()>>, AppError> {
    dept_service::mutate().await?;
    Ok(ok_null())
}
pub async fn status(Path(_id): Path<i64>) -> Result<Json<ApiResponse<()>>, AppError> {
    dept_service::mutate().await?;
    Ok(ok_null())
}
