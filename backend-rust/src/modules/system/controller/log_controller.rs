use super::ok;
use crate::common::{ApiResponse, AppError, PageResult};
use crate::modules::system::AppState;
use crate::modules::system::dto::log_request::LogQuery;
use crate::modules::system::service::login_log_service::LoginLog;
use crate::modules::system::service::{login_log_service, operation_log_service};
use crate::modules::system::vo::operation_log_vo::OperationLogVo;
use axum::Json;
use axum::extract::{Path, Query, State};

pub async fn login_logs(
    State(state): State<AppState>, Query(query): Query<LogQuery>,
) -> Result<Json<ApiResponse<PageResult<LoginLog>>>, AppError> {
    Ok(ok(login_log_service::list(state.database.as_ref(), &query).await?))
}

pub async fn login_log(State(state): State<AppState>, Path(id): Path<i64>) -> Result<Json<ApiResponse<LoginLog>>, AppError> {
    Ok(ok(login_log_service::detail(state.database.as_ref(), id).await?))
}

pub async fn operation_logs(
    State(state): State<AppState>, Query(query): Query<LogQuery>,
) -> Result<Json<ApiResponse<PageResult<OperationLogVo>>>, AppError> {
    Ok(ok(operation_log_service::list(state.database.as_ref(), &query).await?))
}

pub async fn operation_log(State(state): State<AppState>, Path(id): Path<i64>) -> Result<Json<ApiResponse<OperationLogVo>>, AppError> {
    Ok(ok(operation_log_service::detail(state.database.as_ref(), id).await?))
}
