use crate::common::I64String;
use serde::Serialize;

#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct RolePermissionVo {
    pub role_id: I64String,
    pub menu_ids: Vec<I64String>,
}
