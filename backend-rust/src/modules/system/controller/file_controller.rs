use super::ok;
use crate::common::{ApiResponse, AppError};
use crate::modules::system::AppState;
use crate::modules::system::service::file_service;
use crate::modules::system::vo::file_upload_vo::FileUploadVo;
use axum::extract::{Multipart, State};
use axum::Json;
pub async fn upload(State(state): State<AppState>, mut multipart: Multipart) -> Result<Json<ApiResponse<FileUploadVo>>, AppError> { let field = multipart.next_field().await.map_err(|_| AppError::bad_request("file must not be empty"))?.ok_or_else(|| AppError::bad_request("file must not be empty"))?; if field.name() != Some("file") { return Err(AppError::bad_request("file must not be empty")); } let name = field.file_name().unwrap_or_default().to_string(); let bytes = field.bytes().await.map_err(|_| AppError::bad_request("file must not be empty"))?; Ok(ok(file_service::upload(state.database.as_ref(), name, bytes.len() as i64).await?)) }
