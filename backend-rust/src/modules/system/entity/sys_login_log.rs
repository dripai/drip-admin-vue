use crate::common::I64String;
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct SysLoginLog {
    pub id: I64String,
    pub user_id: Option<I64String>,
    pub username: String,
    pub real_name: Option<String>,
    pub action: String,
    pub status: String,
}
