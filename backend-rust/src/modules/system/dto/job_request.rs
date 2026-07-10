use serde::Deserialize;

#[derive(Debug, Clone, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct JobSaveRequest {
    pub job_name: String,
    pub cron_expression: String,
    pub script_type: String,
    pub script_path: String,
    pub status: Option<i32>,
    pub remark: Option<String>,
}
