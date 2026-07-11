use crate::common::{AppError, PageParams};
use serde::Deserialize;

#[derive(Debug, Clone, Deserialize, utoipa::ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct UserSaveRequest {
    pub username: String,
    pub real_name: String,
    pub phone: Option<String>,
    pub email: Option<String>,
    pub dept_id: Option<i64>,
    pub status: Option<i32>,
    pub role_ids: Option<Vec<i64>>,
    pub remark: Option<String>,
    pub password: Option<String>,
}

#[derive(Debug, Clone, Default, Deserialize, utoipa::ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct UserQuery {
    pub page: Option<i32>,
    pub page_size: Option<i32>,
    pub username: Option<String>,
    pub real_name: Option<String>,
    pub phone: Option<String>,
    pub status: Option<i32>,
    pub role_id: Option<i64>,
    pub dept_id: Option<i64>,
    pub created_from: Option<String>,
    pub created_to: Option<String>,
}

impl UserQuery {
    pub fn normalize_page(&self) -> Result<PageParams, AppError> {
        let page = self.page.unwrap_or(1);
        let page_size = self.page_size.unwrap_or(10);
        if page < 1 {
            return Err(AppError::bad_request("page must be >= 1"));
        }
        if !(1..=100).contains(&page_size) {
            return Err(AppError::bad_request("pageSize must be 1 to 100"));
        }
        Ok(PageParams { page, page_size })
    }
}

#[derive(Debug, Clone, Deserialize, utoipa::ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct UserRoleAssignRequest {
    pub role_ids: Vec<i64>,
}

#[derive(Debug, Clone, Deserialize, utoipa::ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct PasswordResetRequest {
    pub password: String,
}
