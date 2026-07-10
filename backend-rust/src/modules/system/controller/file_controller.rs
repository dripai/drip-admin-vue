use super::ok;
use crate::common::{ApiResponse, AppError};
use crate::modules::system::service::file_service;
use crate::modules::system::vo::file_upload_vo::FileUploadVo;
use axum::Json;

pub async fn upload() -> Result<Json<ApiResponse<FileUploadVo>>, AppError> {
    Ok(ok(file_service::upload().await?))
}
