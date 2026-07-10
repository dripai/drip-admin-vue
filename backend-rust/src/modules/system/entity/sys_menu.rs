use crate::common::I64String;
use chrono::NaiveDateTime;
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
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
    pub created_at: Option<NaiveDateTime>,
    pub updated_at: Option<NaiveDateTime>,
}
