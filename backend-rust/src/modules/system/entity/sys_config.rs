use crate::common::I64String;
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct SysConfig {
    pub id: I64String,
    pub config_name: String,
    pub config_key: String,
    pub config_value: String,
    pub value_type: String,
    pub builtin: i32,
    pub status: i32,
    pub remark: Option<String>,
    pub deleted: i32,
}
