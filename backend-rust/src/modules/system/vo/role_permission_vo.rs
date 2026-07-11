use crate::common::I64String;
use serde::Serialize;

#[derive(Debug, Clone, Serialize, utoipa::ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct RolePermissionVo {
    pub menu_ids: Vec<I64String>,
    pub permission_codes: Vec<String>,
}
