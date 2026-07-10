use crate::common::I64String;
use chrono::NaiveDateTime;
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct SysDictItem {
    pub id: I64String,
    pub dict_type_id: I64String,
    pub label: String,
    pub value: String,
    pub is_default: i32,
    pub status: i32,
    pub sort: i32,
    pub builtin: i32,
    pub created_at: Option<NaiveDateTime>,
    pub updated_at: Option<NaiveDateTime>,
}
