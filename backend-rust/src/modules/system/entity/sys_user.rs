use crate::common::I64String;
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize, utoipa::ToSchema)]
#[serde(rename_all(serialize = "camelCase", deserialize = "snake_case"))]
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
    pub last_login_at: Option<String>,
    pub deleted: i32,
    pub created_at: Option<String>,
    pub updated_at: Option<String>,
}

#[cfg(test)]
mod tests {
    use super::SysUser;

    #[test]
    fn accepts_database_column_names() {
        let user: SysUser = serde_json::from_str(
            r#"{"id":"1","username":"admin","password_hash":"hash","password_salt":"salt","real_name":"管理员","phone":null,"email":null,"avatar":null,"status":1,"dept_id":null,"remark":null,"last_login_at":null,"deleted":0,"created_at":null,"updated_at":null}"#,
        )
        .unwrap();
        assert_eq!(user.password_hash, "hash");
        assert_eq!(serde_json::to_value(user).unwrap()["passwordHash"], "hash");
    }

    #[test]
    fn accepts_mysql_datetime_strings() {
        let user: SysUser = serde_json::from_str(
            r#"{"id":"1","username":"admin","password_hash":"hash","password_salt":"salt","real_name":"管理员","phone":null,"email":null,"avatar":null,"status":1,"dept_id":null,"remark":null,"last_login_at":"2026-07-11 09:22:05","deleted":0,"created_at":"2026-07-01 00:00:00","updated_at":"2026-07-11 09:22:05"}"#,
        )
        .unwrap();
        assert_eq!(user.last_login_at.as_deref(), Some("2026-07-11 09:22:05"));
    }
}
