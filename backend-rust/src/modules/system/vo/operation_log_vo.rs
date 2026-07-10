use crate::common::I64String;
use serde::Serialize;

#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct OperationLogVo {
    pub id: I64String,
    pub module_name: String,
    pub action_name: String,
    pub status: String,
}
