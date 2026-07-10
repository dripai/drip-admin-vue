use super::{ok, ok_null};
use crate::common::{ApiResponse, AppError, PageQuery, PageResult};
use crate::modules::system::service::dict_service;
use axum::Json;
use axum::extract::{Path, Query};
use serde_json::Value;

pub async fn type_list(
    Query(query): Query<PageQuery>,
) -> Result<Json<ApiResponse<PageResult<Value>>>, AppError> {
    Ok(ok(dict_service::type_list(query.normalize()?).await?))
}

pub async fn items(Path(_id): Path<i64>) -> Result<Json<ApiResponse<Vec<Value>>>, AppError> {
    Ok(ok(dict_service::item_list().await?))
}

pub async fn create_type() -> Result<Json<ApiResponse<()>>, AppError> {
    dict_service::mutate().await?;
    Ok(ok_null())
}
pub async fn update_type(Path(_id): Path<i64>) -> Result<Json<ApiResponse<()>>, AppError> {
    dict_service::mutate().await?;
    Ok(ok_null())
}
pub async fn delete_type(Path(_id): Path<i64>) -> Result<Json<ApiResponse<()>>, AppError> {
    dict_service::mutate().await?;
    Ok(ok_null())
}
pub async fn create_item() -> Result<Json<ApiResponse<()>>, AppError> {
    dict_service::mutate().await?;
    Ok(ok_null())
}
pub async fn update_item(Path(_id): Path<i64>) -> Result<Json<ApiResponse<()>>, AppError> {
    dict_service::mutate().await?;
    Ok(ok_null())
}
pub async fn delete_item(Path(_id): Path<i64>) -> Result<Json<ApiResponse<()>>, AppError> {
    dict_service::mutate().await?;
    Ok(ok_null())
}
pub async fn item_status(Path(_id): Path<i64>) -> Result<Json<ApiResponse<()>>, AppError> {
    dict_service::mutate().await?;
    Ok(ok_null())
}
pub async fn refresh_cache() -> Result<Json<ApiResponse<()>>, AppError> {
    dict_service::refresh_cache().await?;
    Ok(ok_null())
}
