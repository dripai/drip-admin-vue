use serde::Deserialize;

use crate::common::{AppError, PageParams};

#[derive(Debug, Clone, Deserialize, utoipa::ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct ConfigQuery {
    pub page: Option<i32>,
    pub page_size: Option<i32>,
    pub config_name: Option<String>,
    pub config_key: Option<String>,
    pub status: Option<i32>,
}

impl ConfigQuery {
    pub fn normalize_page(&self) -> Result<PageParams, AppError> {
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

#[derive(Debug, Clone, Deserialize, utoipa::ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct ConfigSaveRequest {
    pub config_name: String,
    pub config_key: String,
    pub config_value: Option<String>,
    pub value_type: Option<String>,
    pub status: Option<i32>,
    pub remark: Option<String>,
}
