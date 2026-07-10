use super::{ok, ok_null};
use crate::common::{ApiResponse, AppError, I64String, PageResult};
use crate::modules::system::AppState;
use crate::modules::system::dto::config_request::{ConfigQuery, ConfigSaveRequest};
use crate::modules::system::dto::status_update_request::StatusUpdateRequest;
use crate::modules::system::entity::sys_config::SysConfig;
use crate::modules::system::service::config_service;
use axum::Json;
use axum::extract::{Path, Query, State};
use serde_json::Value;

pub async fn public_config(
    State(state): State<AppState>,
) -> Result<Json<ApiResponse<Value>>, AppError> {
    Ok(ok(
        config_service::public_config(state.database.as_ref()).await?
    ))
}

pub async fn list(
    State(state): State<AppState>,
    Query(query): Query<ConfigQuery>,
) -> Result<Json<ApiResponse<PageResult<SysConfig>>>, AppError> {
    Ok(ok(
        config_service::list(state.database.as_ref(), &query).await?
    ))
}

pub async fn detail(
    State(state): State<AppState>,
    Path(id): Path<i64>,
) -> Result<Json<ApiResponse<SysConfig>>, AppError> {
    Ok(ok(
        config_service::detail(state.database.as_ref(), id).await?
    ))
}

pub async fn create(
    State(state): State<AppState>,
    Json(request): Json<ConfigSaveRequest>,
) -> Result<Json<ApiResponse<I64String>>, AppError> {
    Ok(ok(
        config_service::create(state.database.as_ref(), request).await?
    ))
}

pub async fn update(
    State(state): State<AppState>,
    Path(id): Path<i64>,
    Json(request): Json<ConfigSaveRequest>,
) -> Result<Json<ApiResponse<()>>, AppError> {
    config_service::update(state.database.as_ref(), id, request).await?;
    Ok(ok_null())
}

pub async fn delete(
    State(state): State<AppState>,
    Path(id): Path<i64>,
) -> Result<Json<ApiResponse<()>>, AppError> {
    config_service::delete(state.database.as_ref(), id).await?;
    Ok(ok_null())
}

pub async fn status(
    State(state): State<AppState>,
    Path(id): Path<i64>,
    Json(request): Json<StatusUpdateRequest>,
) -> Result<Json<ApiResponse<()>>, AppError> {
    config_service::update_status(state.database.as_ref(), id, request.status).await?;
    Ok(ok_null())
}
