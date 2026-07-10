use super::{ok, ok_null};
use crate::common::{ApiResponse, AppError, PageQuery, PageResult};
use crate::modules::system::service::role_service;
use axum::Json;
use axum::extract::{Path, Query};
use serde_json::Value;

pub async fn list(
    Query(query): Query<PageQuery>,
) -> Result<Json<ApiResponse<PageResult<Value>>>, AppError> {
    Ok(ok(role_service::list(query.normalize()?).await?))
}

pub async fn options() -> Result<Json<ApiResponse<Vec<Value>>>, AppError> {
    Ok(ok(role_service::options().await?))
}

pub async fn detail(Path(_id): Path<i64>) -> Result<Json<ApiResponse<Value>>, AppError> {
    Ok(ok(role_service::detail().await?))
}

pub async fn users(
    Query(query): Query<PageQuery>,
    Path(_id): Path<i64>,
) -> Result<Json<ApiResponse<PageResult<Value>>>, AppError> {
    Ok(ok(role_service::list(query.normalize()?).await?))
}

pub async fn permissions(Path(_id): Path<i64>) -> Result<Json<ApiResponse<Value>>, AppError> {
    Ok(ok(role_service::permissions().await?))
}

pub async fn create() -> Result<Json<ApiResponse<()>>, AppError> {
    role_service::mutate().await?;
    Ok(ok_null())
}
pub async fn update(Path(_id): Path<i64>) -> Result<Json<ApiResponse<()>>, AppError> {
    role_service::mutate().await?;
    Ok(ok_null())
}
pub async fn delete(Path(_id): Path<i64>) -> Result<Json<ApiResponse<()>>, AppError> {
    role_service::mutate().await?;
    Ok(ok_null())
}
pub async fn status(Path(_id): Path<i64>) -> Result<Json<ApiResponse<()>>, AppError> {
    role_service::mutate().await?;
    Ok(ok_null())
}
pub async fn assign_permissions(Path(_id): Path<i64>) -> Result<Json<ApiResponse<()>>, AppError> {
    role_service::mutate().await?;
    Ok(ok_null())
}
