use crate::common::I64String;
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct SysJob {
    pub id: I64String,
    pub job_name: String,
    pub cron_expression: String,
    pub executor_type: String,
    pub script_file: Option<String>,
    pub script_args: Option<String>,
    pub remark: Option<String>,
    pub status: i32,
    pub deleted: i32,
}
