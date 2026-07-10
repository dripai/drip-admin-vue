use super::{ok, ok_null};
use crate::common::{ApiResponse, AppError, I64String};
use crate::modules::system::AppState;
use crate::modules::system::dto::menu_request::MenuSaveRequest;
use crate::modules::system::dto::status_update_request::StatusUpdateRequest;
use crate::modules::system::service::menu_service;
use crate::modules::system::vo::menu_tree_vo::MenuTreeVo;
use axum::Json;
use axum::extract::{Path, State};

pub async fn list(
    State(state): State<AppState>,
) -> Result<Json<ApiResponse<Vec<MenuTreeVo>>>, AppError> {
    Ok(ok(menu_service::list(state.database.as_ref()).await?))
}

pub async fn create(
    State(state): State<AppState>,
    Json(request): Json<MenuSaveRequest>,
) -> Result<Json<ApiResponse<I64String>>, AppError> {
    Ok(ok(
        menu_service::create(state.database.as_ref(), request).await?
    ))
}

pub async fn update(
    State(state): State<AppState>,
    Path(id): Path<i64>,
    Json(request): Json<MenuSaveRequest>,
) -> Result<Json<ApiResponse<()>>, AppError> {
    menu_service::update(state.database.as_ref(), id, request).await?;
    Ok(ok_null())
}

pub async fn delete(
    State(state): State<AppState>,
    Path(id): Path<i64>,
) -> Result<Json<ApiResponse<()>>, AppError> {
    menu_service::delete(state.database.as_ref(), id).await?;
    Ok(ok_null())
}

pub async fn status(
    State(state): State<AppState>,
    Path(id): Path<i64>,
    Json(request): Json<StatusUpdateRequest>,
) -> Result<Json<ApiResponse<()>>, AppError> {
    menu_service::update_status(state.database.as_ref(), id, request.status).await?;
    Ok(ok_null())
}
