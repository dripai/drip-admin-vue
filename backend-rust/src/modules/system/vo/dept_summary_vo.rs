use crate::common::I64String;
use serde::Serialize;

#[derive(Debug, Clone, Serialize, utoipa::ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct DeptSummaryVo {
    pub id: I64String,
    pub dept_name: String,
}
