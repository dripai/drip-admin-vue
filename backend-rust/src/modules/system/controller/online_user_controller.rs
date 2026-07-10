use super::{ok, ok_null};
use crate::common::{ApiResponse, AppError, PageQuery, PageResult};
use crate::modules::system::service::online_user_service;
use axum::Json;
use axum::extract::{Path, Query};
use serde_json::Value;

pub async fn list(
    Query(query): Query<PageQuery>,
) -> Result<Json<ApiResponse<PageResult<Value>>>, AppError> {
    Ok(ok(online_user_service::list(query.normalize()?).await?))
}

pub async fn detail(Path(_token_id): Path<String>) -> Result<Json<ApiResponse<Value>>, AppError> {
    Ok(ok(online_user_service::detail().await?))
}

pub async fn kickout(Path(_token_id): Path<String>) -> Result<Json<ApiResponse<()>>, AppError> {
    online_user_service::kickout().await?;
    Ok(ok_null())
}
