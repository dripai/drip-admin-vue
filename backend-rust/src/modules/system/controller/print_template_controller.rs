use super::{ok, ok_null};
use crate::common::{ApiResponse, AppError, PageQuery, PageResult};
use crate::modules::system::service::print_template_service;
use axum::Json;
use axum::extract::{Path, Query};
use serde_json::Value;

pub async fn list(
    Query(query): Query<PageQuery>,
) -> Result<Json<ApiResponse<PageResult<Value>>>, AppError> {
    Ok(ok(print_template_service::list(query.normalize()?).await?))
}

pub async fn detail(Path(_id): Path<i64>) -> Result<Json<ApiResponse<Value>>, AppError> {
    Ok(ok(print_template_service::detail().await?))
}
pub async fn create() -> Result<Json<ApiResponse<()>>, AppError> {
    print_template_service::mutate().await?;
    Ok(ok_null())
}
pub async fn copy(Path(_id): Path<i64>) -> Result<Json<ApiResponse<()>>, AppError> {
    print_template_service::mutate().await?;
    Ok(ok_null())
}
pub async fn update(Path(_id): Path<i64>) -> Result<Json<ApiResponse<()>>, AppError> {
    print_template_service::mutate().await?;
    Ok(ok_null())
}
pub async fn delete(Path(_id): Path<i64>) -> Result<Json<ApiResponse<()>>, AppError> {
    print_template_service::mutate().await?;
    Ok(ok_null())
}
pub async fn status(Path(_id): Path<i64>) -> Result<Json<ApiResponse<()>>, AppError> {
    print_template_service::mutate().await?;
    Ok(ok_null())
}
