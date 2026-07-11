use crate::common::I64String;
use crate::modules::system::vo::menu_tree_vo::MenuTreeVo;
use serde::Serialize;

#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct AuthLoginVo {
    pub token: String,
    pub expires_at: String,
    pub active_timeout_seconds: i64,
    pub token_timeout_seconds: i64,
    pub device_type: String,
}

#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct AuthMeVo {
    pub id: I64String,
    pub username: String,
    pub real_name: String,
    pub phone: Option<String>,
    pub email: Option<String>,
    pub avatar: Option<String>,
    pub dept_id: Option<I64String>,
    pub roles: Vec<String>,
    pub permissions: Vec<String>,
    pub menus: Vec<MenuTreeVo>,
}
