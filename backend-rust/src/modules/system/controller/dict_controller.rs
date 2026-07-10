use super::{ok, ok_null};
use crate::common::{ApiResponse, AppError, I64String};
use crate::modules::system::AppState;
use crate::modules::system::dto::dict_request::{DictItemSaveRequest, DictTypeSaveRequest};
use crate::modules::system::dto::status_update_request::StatusUpdateRequest;
use crate::modules::system::entity::sys_dict_item::SysDictItem;
use crate::modules::system::entity::sys_dict_type::SysDictType;
use crate::modules::system::service::dict_service;
use axum::Json;
use axum::extract::{Path, State};

pub async fn type_list(
    State(state): State<AppState>,
) -> Result<Json<ApiResponse<Vec<SysDictType>>>, AppError> {
    Ok(ok(dict_service::type_list(state.database.as_ref()).await?))
}

pub async fn items(
    State(state): State<AppState>,
    Path(id): Path<i64>,
) -> Result<Json<ApiResponse<Vec<SysDictItem>>>, AppError> {
    Ok(ok(
        dict_service::item_list(state.database.as_ref(), id).await?
    ))
}

pub async fn create_type(
    State(state): State<AppState>,
    Json(request): Json<DictTypeSaveRequest>,
) -> Result<Json<ApiResponse<I64String>>, AppError> {
    Ok(ok(dict_service::create_type(
        state.database.as_ref(),
        request,
    )
    .await?))
}

pub async fn update_type(
    State(state): State<AppState>,
    Path(id): Path<i64>,
    Json(request): Json<DictTypeSaveRequest>,
) -> Result<Json<ApiResponse<()>>, AppError> {
    dict_service::update_type(state.database.as_ref(), id, request).await?;
    Ok(ok_null())
}

pub async fn delete_type(
    State(state): State<AppState>,
    Path(id): Path<i64>,
) -> Result<Json<ApiResponse<()>>, AppError> {
    dict_service::delete_type(state.database.as_ref(), id).await?;
    Ok(ok_null())
}

pub async fn create_item(
    State(state): State<AppState>,
    Json(request): Json<DictItemSaveRequest>,
) -> Result<Json<ApiResponse<I64String>>, AppError> {
    Ok(ok(dict_service::create_item(
        state.database.as_ref(),
        request,
    )
    .await?))
}

pub async fn update_item(
    State(state): State<AppState>,
    Path(id): Path<i64>,
    Json(request): Json<DictItemSaveRequest>,
) -> Result<Json<ApiResponse<()>>, AppError> {
    dict_service::update_item(state.database.as_ref(), id, request).await?;
    Ok(ok_null())
}

pub async fn delete_item(
    State(state): State<AppState>,
    Path(id): Path<i64>,
) -> Result<Json<ApiResponse<()>>, AppError> {
    dict_service::delete_item(state.database.as_ref(), id).await?;
    Ok(ok_null())
}

pub async fn item_status(
    State(state): State<AppState>,
    Path(id): Path<i64>,
    Json(request): Json<StatusUpdateRequest>,
) -> Result<Json<ApiResponse<()>>, AppError> {
    dict_service::update_item_status(state.database.as_ref(), id, request.status).await?;
    Ok(ok_null())
}

pub async fn refresh_cache(
    State(state): State<AppState>,
) -> Result<Json<ApiResponse<()>>, AppError> {
    dict_service::refresh_cache(state.database.as_ref()).await?;
    Ok(ok_null())
}
