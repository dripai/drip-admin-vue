use crate::common::I64String;
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize, utoipa::ToSchema)]
#[serde(rename_all(serialize = "camelCase", deserialize = "snake_case"))]
pub struct SysJobRunLog {
    pub id: I64String,
    pub job_id: I64String,
    pub job_name: String,
    pub status: String,
    pub started_at: String,
    pub finished_at: Option<String>,
    pub cost_ms: i64,
    pub error_message: Option<String>,
}
