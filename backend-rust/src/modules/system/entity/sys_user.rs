use crate::common::I64String;
use chrono::NaiveDateTime;
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct SysUser {
    pub id: I64String,
    pub username: String,
    pub password_hash: String,
    pub password_salt: String,
    pub real_name: String,
    pub phone: Option<String>,
    pub email: Option<String>,
    pub avatar: Option<String>,
    pub status: i32,
    pub dept_id: Option<I64String>,
    pub remark: Option<String>,
    pub last_login_at: Option<NaiveDateTime>,
    pub deleted: i32,
    pub created_at: Option<NaiveDateTime>,
    pub updated_at: Option<NaiveDateTime>,
}
