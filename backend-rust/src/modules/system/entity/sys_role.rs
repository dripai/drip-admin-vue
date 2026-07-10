use crate::common::I64String;
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct SysRole {
    pub id: I64String,
    pub role_name: String,
    pub role_code: String,
    pub super_admin: i32,
    pub status: i32,
    pub deleted: i32,
}
