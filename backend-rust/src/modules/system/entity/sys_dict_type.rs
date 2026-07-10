use crate::common::I64String;
use chrono::NaiveDateTime;
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct SysDictType {
    pub id: I64String,
    pub dict_name: String,
    pub dict_code: String,
    pub status: i32,
    pub builtin: i32,
    pub remark: Option<String>,
    pub created_at: Option<NaiveDateTime>,
    pub updated_at: Option<NaiveDateTime>,
}
