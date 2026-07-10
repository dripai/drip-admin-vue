use crate::common::AppError;
use crate::modules::system::AppState;
use crate::modules::system::service::session_service;
use axum::extract::{Request, State};
use axum::middleware::Next;
use axum::response::Response;
use tower::layer::util::Identity;

pub async fn auth_required(
    State(state): State<AppState>,
    request: Request,
    next: Next,
) -> Result<Response, AppError> {
    let token = request
        .headers()
        .get(&state.settings.token.name)
        .and_then(|value| value.to_str().ok())
        .filter(|value| !value.trim().is_empty())
        .ok_or_else(|| AppError::unauthorized("未登录或 token 失效"))?;
    let redis_pool = state
        .redis_pool
        .as_ref()
        .ok_or_else(|| AppError::system("Redis pool is not configured"))?;
    session_service::load_session(
        redis_pool,
        token,
        state.settings.token.active_timeout_seconds,
    )
    .await?;
    Ok(next.run(request).await)
}

pub fn require_permission(_permission: &'static str) -> Identity {
    Identity::new()
}
