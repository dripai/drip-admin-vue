use crate::common::I64String;
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct SysDept {
    pub id: I64String,
    pub parent_id: I64String,
    pub dept_name: String,
    pub dept_code: String,
    pub sort: i32,
    pub status: i32,
    pub deleted: i32,
}
