use crate::common::I64String;
use serde::Serialize;

#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct DeptTreeVo {
    pub id: I64String,
    pub parent_id: I64String,
    pub dept_name: String,
    pub dept_code: String,
    pub status: i32,
    pub children: Vec<DeptTreeVo>,
}
