use super::{ok, ok_null};
use crate::common::{ApiResponse, AppError, I64String, PageResult};
use crate::modules::system::AppState;
use crate::modules::system::dto::status_update_request::StatusUpdateRequest;
use crate::modules::system::dto::user_request::{PasswordResetRequest, UserQuery, UserRoleAssignRequest, UserSaveRequest};
use crate::modules::system::entity::sys_user::SysUser;
use crate::modules::system::service::{auth_service, login_security_service, user_service};
use crate::modules::system::vo::user_list_vo::UserListVo;
use axum::extract::{Path, Query, State};
use axum::http::HeaderMap;
use axum::Json;

pub async fn list(State(state): State<AppState>, Query(query): Query<UserQuery>) -> Result<Json<ApiResponse<PageResult<UserListVo>>>, AppError> { Ok(ok(user_service::list(state.database.as_ref(), &query).await?)) }
pub async fn detail(State(state): State<AppState>, Path(id): Path<i64>) -> Result<Json<ApiResponse<SysUser>>, AppError> { Ok(ok(user_service::detail(state.database.as_ref(), id).await?)) }
pub async fn create(State(state): State<AppState>, Json(request): Json<UserSaveRequest>) -> Result<Json<ApiResponse<I64String>>, AppError> { Ok(ok(user_service::create(state.database.as_ref(), request).await?)) }
pub async fn update(State(state): State<AppState>, Path(id): Path<i64>, Json(request): Json<UserSaveRequest>) -> Result<Json<ApiResponse<()>>, AppError> { user_service::update(state.database.as_ref(), id, request).await?; Ok(ok_null()) }
pub async fn delete(State(state): State<AppState>, headers: HeaderMap, Path(id): Path<i64>) -> Result<Json<ApiResponse<()>>, AppError> { let user_id = auth_service::current_user_id(&state, &headers).await?; user_service::delete(state.database.as_ref(), user_id, id).await?; Ok(ok_null()) }
pub async fn status(State(state): State<AppState>, headers: HeaderMap, Path(id): Path<i64>, Json(request): Json<StatusUpdateRequest>) -> Result<Json<ApiResponse<()>>, AppError> { let user_id = auth_service::current_user_id(&state, &headers).await?; user_service::update_status(state.database.as_ref(), user_id, id, request.status).await?; Ok(ok_null()) }
pub async fn unlock(State(state): State<AppState>, Path(id): Path<i64>) -> Result<Json<ApiResponse<()>>, AppError> { let user = user_service::detail(state.database.as_ref(), id).await?; let redis = state.redis_pool.as_ref().ok_or_else(|| AppError::system("Redis pool is not configured"))?; login_security_service::clear_failures(redis, &user.username).await?; Ok(ok_null()) }
pub async fn assign_roles(State(state): State<AppState>, headers: HeaderMap, Path(id): Path<i64>, Json(request): Json<UserRoleAssignRequest>) -> Result<Json<ApiResponse<()>>, AppError> { let user_id = auth_service::current_user_id(&state, &headers).await?; user_service::assign_roles(state.database.as_ref(), user_id, id, request.role_ids).await?; Ok(ok_null()) }
pub async fn reset_password(State(state): State<AppState>, headers: HeaderMap, Path(id): Path<i64>, Json(request): Json<PasswordResetRequest>) -> Result<Json<ApiResponse<()>>, AppError> { let user_id = auth_service::current_user_id(&state, &headers).await?; user_service::reset_password(state.database.as_ref(), user_id, id, request.password).await?; Ok(ok_null()) }
