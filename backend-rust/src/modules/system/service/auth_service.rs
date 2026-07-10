use crate::common::{AppError, I64String};
use crate::config::TokenSettings;
use crate::modules::system::dto::auth_request::{
    LoginRequest, PasswordRequest, ProfileUpdateRequest,
};
use crate::modules::system::service::session_service::SessionData;
use crate::modules::system::service::{login_security_service, session_service};
use crate::modules::system::vo::auth_vo::{AuthLoginVo, AuthMeVo};
use crate::modules::system::vo::menu_tree_vo::MenuTreeVo;
use chrono::{Duration, Utc};
use deadpool_redis::Pool;
use uuid::Uuid;

pub async fn login(
    request: LoginRequest,
    token_settings: &TokenSettings,
    redis_pool: Option<&Pool>,
) -> Result<AuthLoginVo, AppError> {
    let redis_pool = redis_pool.ok_or_else(|| AppError::system("Redis pool is not configured"))?;
    if request.username.trim().is_empty() || request.password.trim().is_empty() {
        let _ = login_security_service::record_failure(redis_pool, &request.username).await;
        return Err(AppError::unauthorized("用户名或密码错误"));
    }
    login_security_service::assert_not_locked(redis_pool, &request.username).await?;
    login_security_service::clear_failures(redis_pool, &request.username).await?;
    let now = Utc::now();
    let expires_at = now + Duration::seconds(token_settings.active_timeout_seconds);
    let token = Uuid::new_v4().to_string();
    let device_type = request.device_type.unwrap_or_else(|| "web".to_string());
    let session = SessionData {
        user_id: I64String(0),
        username: request.username,
        real_name: String::new(),
        token: token.clone(),
        device_type: device_type.clone(),
        login_at: now.to_rfc3339_opts(chrono::SecondsFormat::Millis, true),
        last_active_at: now.to_rfc3339_opts(chrono::SecondsFormat::Millis, true),
    };
    session_service::register_session(
        redis_pool,
        &session,
        token_settings.timeout_seconds,
        token_settings.active_timeout_seconds,
    )
    .await?;
    Ok(AuthLoginVo {
        token,
        expires_at: expires_at.to_rfc3339_opts(chrono::SecondsFormat::Millis, true),
        active_timeout: token_settings.active_timeout_seconds,
        timeout: token_settings.timeout_seconds,
        device_type,
    })
}

pub async fn logout(redis_pool: Option<&Pool>, token: &str) -> Result<(), AppError> {
    let redis_pool = redis_pool.ok_or_else(|| AppError::system("Redis pool is not configured"))?;
    let session = session_service::load_session(redis_pool, token, 1).await?;
    session_service::remove_session(redis_pool, token, session.user_id.value()).await?;
    Ok(())
}

pub async fn me() -> Result<AuthMeVo, AppError> {
    Ok(AuthMeVo {
        id: I64String(0),
        username: String::new(),
        real_name: String::new(),
        phone: None,
        email: None,
        avatar: None,
        dept_id: None,
        role_codes: Vec::new(),
        permission_codes: Vec::new(),
        menus: Vec::<MenuTreeVo>::new(),
    })
}

pub async fn change_password(_request: PasswordRequest) -> Result<(), AppError> {
    Err(AppError::not_implemented())
}

pub async fn update_profile(_request: ProfileUpdateRequest) -> Result<(), AppError> {
    Err(AppError::not_implemented())
}
