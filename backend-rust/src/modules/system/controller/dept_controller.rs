use super::{ok, ok_null};
use crate::common::{ApiResponse, AppError, I64String};
use crate::modules::system::AppState;
use crate::modules::system::dto::dept_request::DeptSaveRequest;
use crate::modules::system::dto::status_update_request::StatusUpdateRequest;
use crate::modules::system::entity::sys_dept::SysDept;
use crate::modules::system::service::dept_service;
use crate::modules::system::vo::dept_tree_vo::DeptTreeVo;
use axum::Json;
use axum::extract::{Path, State};

pub async fn list(
    State(state): State<AppState>,
) -> Result<Json<ApiResponse<Vec<DeptTreeVo>>>, AppError> {
    Ok(ok(dept_service::list(state.database.as_ref()).await?))
}

pub async fn detail(
    State(state): State<AppState>,
    Path(id): Path<i64>,
) -> Result<Json<ApiResponse<SysDept>>, AppError> {
    Ok(ok(dept_service::detail(state.database.as_ref(), id).await?))
}

pub async fn create(
    State(state): State<AppState>,
    Json(request): Json<DeptSaveRequest>,
) -> Result<Json<ApiResponse<I64String>>, AppError> {
    Ok(ok(
        dept_service::create(state.database.as_ref(), request).await?
    ))
}

pub async fn update(
    State(state): State<AppState>,
    Path(id): Path<i64>,
    Json(request): Json<DeptSaveRequest>,
) -> Result<Json<ApiResponse<()>>, AppError> {
    dept_service::update(state.database.as_ref(), id, request).await?;
    Ok(ok_null())
}

pub async fn delete(
    State(state): State<AppState>,
    Path(id): Path<i64>,
) -> Result<Json<ApiResponse<()>>, AppError> {
    dept_service::delete(state.database.as_ref(), id).await?;
    Ok(ok_null())
}

pub async fn status(
    State(state): State<AppState>,
    Path(id): Path<i64>,
    Json(request): Json<StatusUpdateRequest>,
) -> Result<Json<ApiResponse<()>>, AppError> {
    dept_service::update_status(state.database.as_ref(), id, request.status).await?;
    Ok(ok_null())
}
