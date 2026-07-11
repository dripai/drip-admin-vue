use crate::common::I64String;
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize, utoipa::ToSchema)]
#[serde(rename_all(serialize = "camelCase", deserialize = "snake_case"))]
pub struct SysDept {
    pub id: I64String,
    pub parent_id: I64String,
    pub dept_name: String,
    pub dept_code: String,
    pub leader_user_id: Option<I64String>,
    pub sort: i32,
    pub status: i32,
    pub deleted: i32,
    pub created_at: Option<String>,
    pub updated_at: Option<String>,
}
