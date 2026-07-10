use crate::common::AppError;
use crate::modules::system::vo::file_upload_vo::FileUploadVo;

pub async fn upload() -> Result<FileUploadVo, AppError> {
    Err(AppError::not_implemented())
}
