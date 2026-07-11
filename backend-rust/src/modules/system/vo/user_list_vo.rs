use crate::common::I64String;
use crate::modules::system::vo::dept_summary_vo::DeptSummaryVo;
use crate::modules::system::vo::role_summary_vo::RoleSummaryVo;
use chrono::NaiveDateTime;
use serde::Serialize;

#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct UserListVo {
    pub id: I64String,
    pub username: String,
    pub real_name: String,
    pub phone: Option<String>,
    pub email: Option<String>,
    pub status: i32,
    pub dept: Option<DeptSummaryVo>,
    pub roles: Vec<RoleSummaryVo>,
    pub created_at: Option<NaiveDateTime>,
    pub last_login_at: Option<NaiveDateTime>,
}
