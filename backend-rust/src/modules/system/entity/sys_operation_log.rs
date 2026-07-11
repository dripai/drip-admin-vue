use crate::common::I64String;
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize, utoipa::ToSchema)]
#[serde(rename_all(serialize = "camelCase", deserialize = "snake_case"))]
pub struct SysOperationLog {
    pub id: I64String,
    pub user_id: Option<I64String>,
    pub username: Option<String>,
    pub module_name: String,
    pub action_name: String,
    pub method: String,
    pub path: String,
    pub status: String,
}
