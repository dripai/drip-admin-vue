use crate::common::I64String;
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize, utoipa::ToSchema)]
#[serde(rename_all(serialize = "camelCase", deserialize = "snake_case"))]
pub struct SysDbBackup {
    pub id: I64String,
    pub filename: String,
    pub file_path: String,
    pub file_size: i64,
    pub status: String,
}
