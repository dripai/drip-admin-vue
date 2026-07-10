use crate::common::I64String;
use serde::Serialize;

#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct OnlineUserVo {
    pub user_id: I64String,
    pub token_id: String,
    pub username: String,
    pub real_name: String,
    pub device_type: String,
}
