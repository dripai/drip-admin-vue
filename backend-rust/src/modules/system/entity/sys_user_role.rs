use crate::common::I64String;
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct SysUserRole {
    pub id: I64String,
    pub user_id: I64String,
    pub role_id: I64String,
}
