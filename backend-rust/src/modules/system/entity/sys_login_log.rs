use crate::common::I64String;
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize, utoipa::ToSchema)]
#[serde(rename_all(serialize = "camelCase", deserialize = "snake_case"))]
pub struct SysLoginLog {
    pub id: I64String,
    pub user_id: Option<I64String>,
    pub username: String,
    pub real_name: Option<String>,
    pub action: String,
    pub status: String,
}
