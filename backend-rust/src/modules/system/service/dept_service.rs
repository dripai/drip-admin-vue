use crate::common::AppError;
use serde_json::Value;

pub async fn list() -> Result<Vec<Value>, AppError> {
    Ok(Vec::new())
}

pub async fn detail() -> Result<Value, AppError> {
    Err(AppError::not_found("资源不存在"))
}

pub async fn mutate() -> Result<(), AppError> {
    Err(AppError::not_implemented())
}
