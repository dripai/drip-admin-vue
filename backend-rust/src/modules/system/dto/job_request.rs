use serde::Deserialize;

#[derive(Debug, Clone, Deserialize, utoipa::ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct JobSaveRequest {
    pub job_name: String,
    pub cron_expression: String,
    pub executor_type: String,
    pub script_file: String,
    pub script_args: Option<String>,
    pub status: Option<i32>,
    pub remark: Option<String>,
}
