use crate::common::I64String;
use serde::Serialize;

#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct DeptSummaryVo {
    pub id: I64String,
    pub dept_name: String,
    pub dept_code: String,
}
