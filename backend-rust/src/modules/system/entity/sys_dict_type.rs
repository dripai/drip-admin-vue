use crate::common::I64String;
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct SysDictType {
    pub id: I64String,
    pub dict_name: String,
    pub dict_code: String,
    pub status: i32,
}
