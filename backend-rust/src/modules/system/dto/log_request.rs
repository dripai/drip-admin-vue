use crate::common::{AppError, PageParams};
use serde::Deserialize;

#[derive(Debug, Clone, Default, Deserialize, utoipa::ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct LogQuery {
    pub page: Option<i32>,
    pub page_size: Option<i32>,
    pub username: Option<String>,
    pub status: Option<String>,
    pub login_type: Option<String>,
    pub device_type: Option<String>,
    pub ip: Option<String>,
    pub operator: Option<String>,
    pub module: Option<String>,
    pub action: Option<String>,
    pub path: Option<String>,
    pub login_from: Option<String>,
    pub login_to: Option<String>,
    pub created_from: Option<String>,
    pub created_to: Option<String>,
}

impl LogQuery {
    pub fn page(&self) -> Result<PageParams, AppError> {
        let page = self.page.unwrap_or(1);
        let page_size = self.page_size.unwrap_or(10);
        if page < 1 || !(1..=100).contains(&page_size) {
            return Err(AppError::bad_request("page or pageSize is invalid"));
        }
        Ok(PageParams { page, page_size })
    }
}
