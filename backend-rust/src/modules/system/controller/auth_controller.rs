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
    Ok(ok(auth_service::login(
        request,
        &state.settings.token,
        state.redis_pool.as_ref(),
    )
    .await?))
}

pub async fn logout(
    State(state): State<AppState>,
    headers: HeaderMap,
) -> Result<Json<ApiResponse<()>>, AppError> {
    let token = headers
        .get(&state.settings.token.name)
        .and_then(|value| value.to_str().ok())
        .ok_or_else(|| AppError::unauthorized("未登录或 token 失效"))?;
    auth_service::logout(state.redis_pool.as_ref(), token).await?;
    Ok(ok_null())
}

pub async fn me() -> Result<Json<ApiResponse<AuthMeVo>>, AppError> {
    Ok(ok(auth_service::me().await?))
}

pub async fn password(
    Json(request): Json<PasswordRequest>,
) -> Result<Json<ApiResponse<()>>, AppError> {
    auth_service::change_password(request).await?;
    Ok(ok_null())
}

pub async fn profile(
    Json(request): Json<ProfileUpdateRequest>,
) -> Result<Json<ApiResponse<()>>, AppError> {
    auth_service::update_profile(request).await?;
    Ok(ok_null())
}
