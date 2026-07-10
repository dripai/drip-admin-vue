use crate::common::{AppError, PageParams, PageResult};
use serde_json::Value;

pub async fn type_list(params: PageParams) -> Result<PageResult<Value>, AppError> {
    Ok(PageResult::empty(params))
}

pub async fn item_list() -> Result<Vec<Value>, AppError> {
    Ok(Vec::new())
}

pub async fn mutate() -> Result<(), AppError> {
    Err(AppError::not_implemented())
}

pub async fn refresh_cache() -> Result<(), AppError> {
    Ok(())
}
