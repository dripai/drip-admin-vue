use crate::common::{AppError, PageParams, PageResult};
use serde_json::{Value, json};

pub async fn list(params: PageParams) -> Result<PageResult<Value>, AppError> {
    Ok(PageResult::empty(params))
}

pub async fn options() -> Result<Vec<Value>, AppError> {
    Ok(Vec::new())
}

pub async fn detail() -> Result<Value, AppError> {
    Err(AppError::not_found("资源不存在"))
}

pub async fn permissions() -> Result<Value, AppError> {
    Ok(json!({"menuIds":[]}))
}

pub async fn mutate() -> Result<(), AppError> {
    Err(AppError::not_implemented())
}
