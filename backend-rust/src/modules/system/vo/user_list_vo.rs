use crate::common::I64String;
use serde::Serialize;

#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct UserListVo {
    pub id: I64String,
    pub username: String,
    pub real_name: String,
    pub status: i32,
}
