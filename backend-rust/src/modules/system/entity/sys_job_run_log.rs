use crate::common::I64String;
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct SysJobRunLog {
    pub id: I64String,
    pub job_id: I64String,
    pub job_name: String,
    pub status: String,
    pub duration_ms: i64,
}
