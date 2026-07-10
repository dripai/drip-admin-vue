use crate::common::I64String;
use chrono::NaiveDateTime;
use serde::Serialize;

#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct DeptTreeVo {
    pub id: I64String,
    pub parent_id: I64String,
    pub dept_name: String,
    pub dept_code: String,
    pub leader_user_id: Option<I64String>,
    pub sort: i32,
    pub status: i32,
    pub created_at: Option<NaiveDateTime>,
    pub updated_at: Option<NaiveDateTime>,
    pub children: Vec<DeptTreeVo>,
}
