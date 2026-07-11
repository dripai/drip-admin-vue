use super::{ok, ok_null};
use crate::common::{ApiResponse, AppError, I64String, PageQuery, PageResult};
use crate::modules::system::dto::job_request::JobSaveRequest;
use crate::modules::system::dto::status_update_request::StatusUpdateRequest;
use crate::modules::system::entity::sys_job::SysJob;
use crate::modules::system::entity::sys_job_run_log::SysJobRunLog;
use crate::modules::system::service::job_service;
use crate::modules::system::AppState;
use axum::{
    extract::{Path, Query, State},
    Json,
};

pub async fn list(
    State(state): State<AppState>,
    Query(query): Query<PageQuery>,
) -> Result<Json<ApiResponse<PageResult<SysJob>>>, AppError> {
    Ok(ok(job_service::list(state.database.as_ref(), query.normalize()?).await?))
}

pub async fn scripts(State(state): State<AppState>) -> Result<Json<ApiResponse<Vec<String>>>, AppError> {
    Ok(ok(job_service::scripts(&state.settings).await?))
}

pub async fn detail(
    State(state): State<AppState>,
    Path(id): Path<i64>,
) -> Result<Json<ApiResponse<SysJob>>, AppError> {
    Ok(ok(job_service::detail(state.database.as_ref(), id).await?))
}

pub async fn create(
    State(state): State<AppState>,
    Json(request): Json<JobSaveRequest>,
) -> Result<Json<ApiResponse<I64String>>, AppError> {
    Ok(ok(job_service::create(state.database.as_ref(), request).await?))
}

pub async fn update(
    State(state): State<AppState>,
    Path(id): Path<i64>,
    Json(request): Json<JobSaveRequest>,
) -> Result<Json<ApiResponse<()>>, AppError> {
    job_service::update(state.database.as_ref(), id, request).await?;
    Ok(ok_null())
}

pub async fn delete(
    State(state): State<AppState>,
    Path(id): Path<i64>,
) -> Result<Json<ApiResponse<()>>, AppError> {
    job_service::delete(state.database.as_ref(), id).await?;
    Ok(ok_null())
}

pub async fn status(
    State(state): State<AppState>,
    Path(id): Path<i64>,
    Json(request): Json<StatusUpdateRequest>,
) -> Result<Json<ApiResponse<()>>, AppError> {
    job_service::status(state.database.as_ref(), id, request.status).await?;
    Ok(ok_null())
}

pub async fn run(
    State(state): State<AppState>,
    Path(id): Path<i64>,
) -> Result<Json<ApiResponse<()>>, AppError> {
    let database = state
        .database
        .clone()
        .ok_or_else(|| AppError::system("Rbatis database is not configured"))?;
    job_service::run(database, state.settings.clone(), id).await?;
    Ok(ok_null())
}

pub async fn run_logs(
    State(state): State<AppState>,
    Query(query): Query<PageQuery>,
    Path(id): Path<i64>,
) -> Result<Json<ApiResponse<PageResult<SysJobRunLog>>>, AppError> {
    Ok(ok(
        job_service::run_logs(state.database.as_ref(), query.normalize()?, Some(id)).await?,
    ))
}

pub async fn all_run_logs(
    State(state): State<AppState>,
    Query(query): Query<PageQuery>,
) -> Result<Json<ApiResponse<PageResult<SysJobRunLog>>>, AppError> {
    Ok(ok(
        job_service::run_logs(state.database.as_ref(), query.normalize()?, None).await?,
    ))
}
