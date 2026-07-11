use super::{ok, ok_null};
use crate::common::{ApiResponse, AppError};
use crate::modules::system::AppState;
use crate::modules::system::dto::auth_request::{
    LoginRequest, PasswordRequest, ProfileUpdateRequest,
};
use crate::modules::system::service::auth_service;
use crate::modules::system::vo::auth_vo::{AuthLoginVo, AuthMeVo};
use axum::Json;
use axum::extract::State;
use axum::extract::rejection::JsonRejection;
use axum::http::HeaderMap;

pub async fn login(
    State(state): State<AppState>,
    payload: Result<Json<LoginRequest>, JsonRejection>,
) -> Result<Json<ApiResponse<AuthLoginVo>>, AppError> {
    let Json(request) = payload.map_err(|_| AppError::bad_request("请求体 JSON 格式错误"))?;
    let _ = &state.settings.token;
    Ok(ok(auth_service::login(&state, request, &HeaderMap::new()).await?))
}

pub async fn logout(
    State(state): State<AppState>,
    headers: HeaderMap,
) -> Result<Json<ApiResponse<()>>, AppError> {
    auth_service::logout(&state, &headers).await?;
    Ok(ok_null())
}

pub async fn me(
    State(state): State<AppState>,
    headers: HeaderMap,
) -> Result<Json<ApiResponse<AuthMeVo>>, AppError> {
    Ok(ok(auth_service::me(&state, &headers).await?))
}

pub async fn password(
    State(state): State<AppState>,
    headers: HeaderMap,
    Json(request): Json<PasswordRequest>,
) -> Result<Json<ApiResponse<()>>, AppError> {
    auth_service::change_password(&state, &headers, request).await?;
    Ok(ok_null())
}

pub async fn profile(
    State(state): State<AppState>,
    headers: HeaderMap,
    Json(request): Json<ProfileUpdateRequest>,
) -> Result<Json<ApiResponse<()>>, AppError> {
    auth_service::update_profile(&state, &headers, request).await?;
    Ok(ok_null())
}
