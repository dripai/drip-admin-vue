use serde::Deserialize;

#[derive(Debug, Clone, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct DeptSaveRequest {
    pub parent_id: Option<i64>,
    pub dept_name: String,
    pub dept_code: String,
    pub leader_user_id: Option<i64>,
    pub sort: Option<i32>,
    pub status: Option<i32>,
}
