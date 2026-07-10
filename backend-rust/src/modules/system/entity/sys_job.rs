use crate::common::I64String;
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct SysJob {
    pub id: I64String,
    pub job_name: String,
    pub cron_expression: String,
    pub script_type: String,
    pub script_path: String,
    pub status: i32,
    pub deleted: i32,
}
