use crate::common::{AppError, PageParams};
use serde::Deserialize;

#[derive(Debug, Clone, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct RoleQuery {
    pub page: Option<i32>,
    pub page_size: Option<i32>,
    pub role_name: Option<String>,
    pub role_code: Option<String>,
    pub status: Option<i32>,
    pub created_at: Option<String>,
}

impl RoleQuery {
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

#[derive(Debug, Clone, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct RoleSaveRequest {
    pub role_name: String,
    pub role_code: String,
    pub status: Option<i32>,
    pub remark: Option<String>,
}

#[derive(Debug, Clone, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct RolePermissionAssignRequest {
    pub menu_ids: Vec<i64>,
}
