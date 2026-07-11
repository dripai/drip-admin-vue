use crate::common::I64String;
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize, utoipa::ToSchema)]
#[serde(rename_all(serialize = "camelCase", deserialize = "snake_case"))]
pub struct SysRoleMenu {
    pub id: I64String,
    pub role_id: I64String,
    pub menu_id: I64String,
    pub created_at: Option<String>,
}
