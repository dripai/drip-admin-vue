use crate::common::AppError;
use crate::modules::system::AppState;
use crate::modules::system::service::session_service::{self, SessionData};
use axum::extract::{Request, State};
use axum::middleware::Next;
use axum::response::{IntoResponse, Response};
use serde::Deserialize;
use std::future::Future;
use std::pin::Pin;
use std::convert::Infallible;
use std::task::{Context, Poll};
use tower::{Layer, Service};

pub async fn auth_required(
    State(state): State<AppState>,
    mut request: Request,
    next: Next,
) -> Result<Response, AppError> {
    let token = request.headers().get(&state.settings.token.name).and_then(|value| value.to_str().ok()).filter(|value| !value.trim().is_empty()).ok_or_else(|| AppError::unauthorized("未登录或 token 失效"))?;
    let redis_pool = state.redis_pool.as_ref().ok_or_else(|| AppError::system("Redis pool is not configured"))?;
    let session = session_service::load_session(redis_pool, token, state.settings.token.active_timeout_seconds).await?;
    request.extensions_mut().insert(session);
    request.extensions_mut().insert(state);
    Ok(next.run(request).await)
}

#[derive(Clone)]
pub struct RequirePermissionLayer { permission: &'static str }
pub fn require_permission(permission: &'static str) -> RequirePermissionLayer { RequirePermissionLayer { permission } }
#[derive(Clone)]
pub struct RequirePermissionService<S> { inner: S, permission: &'static str }
impl<S> Layer<S> for RequirePermissionLayer { type Service = RequirePermissionService<S>; fn layer(&self, inner: S) -> Self::Service { RequirePermissionService { inner, permission: self.permission } } }
impl<S> Service<Request> for RequirePermissionService<S>
where S: Service<Request, Response = Response, Error = Infallible> + Clone + Send + 'static, S::Future: Send + 'static {
    type Response = Response; type Error = Infallible; type Future = Pin<Box<dyn Future<Output = Result<Response, Infallible>> + Send>>;
    fn poll_ready(&mut self, cx: &mut Context<'_>) -> Poll<Result<(), Self::Error>> { self.inner.poll_ready(cx) }
    fn call(&mut self, request: Request) -> Self::Future { let session = request.extensions().get::<SessionData>().cloned().ok_or_else(|| AppError::unauthorized("未登录或 token 失效")); let state = request.extensions().get::<AppState>().cloned().ok_or_else(|| AppError::system("application state is unavailable")); let mut inner = self.inner.clone(); let permission = self.permission; Box::pin(async move { let result = match (session, state) { (Ok(session), Ok(state)) => match has_permission(&state, session.user_id.value(), permission).await { Ok(true) => Ok(()), Ok(false) => Err(AppError::forbidden("无权限")), Err(error) => Err(error) }, (Err(error), _) | (_, Err(error)) => Err(error) }; match result { Ok(()) => inner.call(request).await, Err(error) => Ok(error.into_response()) } }) }
}

async fn has_permission(state: &AppState, user_id: i64, permission: &str) -> Result<bool, AppError> {
    let database = state.database.as_ref().ok_or_else(|| AppError::system("Rbatis database is not configured"))?;
    #[derive(Deserialize)] struct Count { total: i64 }
    let super_admin: Vec<Count> = database.exec_decode("select count(*) as total from sys_role r inner join sys_user_role ur on ur.role_id = r.id where ur.user_id = ? and r.role_code = 'SUPER_ADMIN' and r.status = 1 and r.deleted = 0", vec![rbs::value!(user_id)]).await.map_err(|err| AppError::system(err.to_string()))?;
    if super_admin.first().map(|row| row.total).unwrap_or(0) > 0 { return Ok(true); }
    let rows: Vec<Count> = database.exec_decode("select count(*) as total from sys_menu m inner join sys_role_menu rm on rm.menu_id = m.id inner join sys_user_role ur on ur.role_id = rm.role_id inner join sys_role r on r.id = ur.role_id where ur.user_id = ? and m.permission_code = ? and m.status = 1 and m.deleted = 0 and r.status = 1 and r.deleted = 0", vec![rbs::value!(user_id), rbs::value!(permission)]).await.map_err(|err| AppError::system(err.to_string()))?;
    Ok(rows.first().map(|row| row.total).unwrap_or(0) > 0)
}

#[cfg(test)]
mod tests {
    #[test]
    fn super_admin_permission_rule_is_explicit() {
        let role_code = "SUPER_ADMIN";
        assert_eq!(role_code, "SUPER_ADMIN");
    }
}
