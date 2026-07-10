use crate::common::I64String;
use chrono::NaiveDateTime;
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct SysRole {
    pub id: I64String,
    pub role_name: String,
    pub role_code: String,
    pub builtin: i32,
    pub status: i32,
    pub remark: Option<String>,
    pub deleted: i32,
    pub created_at: Option<NaiveDateTime>,
    pub updated_at: Option<NaiveDateTime>,
}
