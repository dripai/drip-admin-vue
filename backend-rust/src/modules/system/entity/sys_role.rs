use crate::common::I64String;
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize, utoipa::ToSchema)]
#[serde(rename_all(serialize = "camelCase", deserialize = "snake_case"))]
pub struct SysRole {
    pub id: I64String,
    pub role_name: String,
    pub role_code: String,
    pub builtin: i32,
    pub status: i32,
    pub remark: Option<String>,
    pub deleted: i32,
    pub created_at: Option<String>,
    pub updated_at: Option<String>,
}
