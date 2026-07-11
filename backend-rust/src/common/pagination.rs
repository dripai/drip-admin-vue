use crate::common::{AppError, I64String};
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Deserialize, utoipa::ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct PageQuery {
    pub page: Option<i32>,
    pub page_size: Option<i32>,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub struct PageParams {
    pub page: i32,
    pub page_size: i32,
}

impl PageQuery {
    pub fn normalize(&self) -> Result<PageParams, AppError> {
        let page = self.page.unwrap_or(1);
        let page_size = self.page_size.unwrap_or(10);
        if page < 1 {
            return Err(AppError::bad_request("page must be >= 1"));
        }
        if page_size < 1 {
            return Err(AppError::bad_request("pageSize must be >= 1"));
        }
        if page_size > 100 {
            return Err(AppError::bad_request("pageSize must be <= 100"));
        }
        Ok(PageParams { page, page_size })
    }
}

impl Default for PageQuery {
    fn default() -> Self {
        Self {
            page: None,
            page_size: None,
        }
    }
}

#[derive(Debug, Clone, Serialize, utoipa::ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct PageResult<T>
where
    T: Serialize,
{
    pub list: Vec<T>,
    pub total: I64String,
    pub page: i32,
    pub page_size: i32,
}

impl<T> PageResult<T>
where
    T: Serialize,
{
    pub fn empty(params: PageParams) -> Self {
        Self {
            list: Vec::new(),
            total: I64String(0),
            page: params.page,
            page_size: params.page_size,
        }
    }
}
