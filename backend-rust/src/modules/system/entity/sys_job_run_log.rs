use crate::common::I64String;
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct SysJobRunLog {
    pub id: I64String,
    pub job_id: I64String,
    pub job_name: String,
    pub status: String,
    pub started_at: chrono::NaiveDateTime,
    pub finished_at: Option<chrono::NaiveDateTime>,
    pub cost_ms: i64,
    pub error_message: Option<String>,
}
