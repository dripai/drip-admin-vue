use crate::common::I64String;
use serde::Deserialize;

#[derive(Debug, Clone, Deserialize, utoipa::ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct MenuSaveRequest {
    pub parent_id: Option<I64String>,
    pub name: String,
    pub r#type: String,
    pub path: Option<String>,
    pub component: Option<String>,
    pub permission_code: Option<String>,
    pub icon: Option<String>,
    pub sort: Option<i32>,
    pub visible: Option<i32>,
    pub status: Option<i32>,
}
