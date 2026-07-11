use crate::common::I64String;
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize, utoipa::ToSchema)]
#[serde(rename_all(serialize = "camelCase", deserialize = "snake_case"))]
pub struct SysMenu {
    pub id: I64String,
    pub parent_id: I64String,
    pub name: String,
    pub r#type: String,
    pub path: Option<String>,
    pub component: Option<String>,
    pub permission_code: Option<String>,
    pub icon: Option<String>,
    pub sort: i32,
    pub visible: i32,
    pub status: i32,
    pub deleted: i32,
    pub created_at: Option<String>,
    pub updated_at: Option<String>,
}
