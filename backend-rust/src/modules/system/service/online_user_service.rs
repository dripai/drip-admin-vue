use crate::common::{AppError, PageParams, PageResult};
use serde_json::Value;

pub async fn list(params: PageParams) -> Result<PageResult<Value>, AppError> {
    Ok(PageResult::empty(params))
}

pub async fn detail() -> Result<Value, AppError> {
    Err(AppError::not_found("资源不存在"))
}

pub async fn kickout() -> Result<(), AppError> {
    Err(AppError::not_implemented())
}
