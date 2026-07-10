use crate::common::I64String;
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct SysDictItem {
    pub id: I64String,
    pub dict_type_id: I64String,
    pub label: String,
    pub value: String,
    pub status: i32,
    pub sort: i32,
}
