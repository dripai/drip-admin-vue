use super::{ok, ok_null};
use crate::common::{ApiResponse, AppError, I64String, PageResult};
use crate::modules::system::AppState;
use crate::modules::system::dto::role_request::{
    RolePermissionAssignRequest, RoleQuery, RoleSaveRequest,
};
use crate::modules::system::dto::status_update_request::StatusUpdateRequest;
use crate::modules::system::entity::sys_role::SysRole;
use crate::modules::system::entity::sys_user::SysUser;
use crate::modules::system::service::role_service;
use crate::modules::system::vo::role_permission_vo::RolePermissionVo;
use axum::Json;
use axum::extract::{Path, Query, State};

pub async fn list(
    State(state): State<AppState>,
    Query(query): Query<RoleQuery>,
) -> Result<Json<ApiResponse<PageResult<SysRole>>>, AppError> {
    Ok(ok(
        role_service::list(state.database.as_ref(), &query).await?
    ))
}

pub async fn options(
    State(state): State<AppState>,
) -> Result<Json<ApiResponse<Vec<SysRole>>>, AppError> {
    Ok(ok(role_service::options(state.database.as_ref()).await?))
}

pub async fn detail(
    State(state): State<AppState>,
    Path(id): Path<i64>,
) -> Result<Json<ApiResponse<SysRole>>, AppError> {
    Ok(ok(role_service::detail(state.database.as_ref(), id).await?))
}

pub async fn users(
    State(state): State<AppState>,
    Path(id): Path<i64>,
    Query(query): Query<RoleQuery>,
) -> Result<Json<ApiResponse<PageResult<SysUser>>>, AppError> {
    Ok(ok(
        role_service::users(state.database.as_ref(), id, &query).await?
    ))
}

pub async fn permissions(
    State(state): State<AppState>,
    Path(id): Path<i64>,
) -> Result<Json<ApiResponse<RolePermissionVo>>, AppError> {
    Ok(ok(
        role_service::permissions(state.database.as_ref(), id).await?
    ))
}

pub async fn create(
    State(state): State<AppState>,
    Json(request): Json<RoleSaveRequest>,
) -> Result<Json<ApiResponse<I64String>>, AppError> {
    Ok(ok(
        role_service::create(state.database.as_ref(), request).await?
    ))
}

pub async fn update(
    State(state): State<AppState>,
    Path(id): Path<i64>,
    Json(request): Json<RoleSaveRequest>,
) -> Result<Json<ApiResponse<()>>, AppError> {
    role_service::update(state.database.as_ref(), id, request).await?;
    Ok(ok_null())
}

pub async fn delete(
    State(state): State<AppState>,
    Path(id): Path<i64>,
) -> Result<Json<ApiResponse<()>>, AppError> {
    role_service::delete(state.database.as_ref(), id).await?;
    Ok(ok_null())
}

pub async fn status(
    State(state): State<AppState>,
    Path(id): Path<i64>,
    Json(request): Json<StatusUpdateRequest>,
) -> Result<Json<ApiResponse<()>>, AppError> {
    role_service::update_status(state.database.as_ref(), id, request.status).await?;
    Ok(ok_null())
}

pub async fn assign_permissions(
    State(state): State<AppState>,
    Path(id): Path<i64>,
    Json(request): Json<RolePermissionAssignRequest>,
) -> Result<Json<ApiResponse<()>>, AppError> {
    role_service::assign_permissions(state.database.as_ref(), id, request.menu_ids).await?;
    Ok(ok_null())
}
