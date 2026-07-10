use serde::Deserialize;

#[derive(Debug, Clone, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct RoleSaveRequest {
    pub role_name: String,
    pub role_code: String,
    pub status: Option<i32>,
    pub remark: Option<String>,
}

#[derive(Debug, Clone, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct RolePermissionAssignRequest {
    pub menu_ids: Vec<i64>,
}
