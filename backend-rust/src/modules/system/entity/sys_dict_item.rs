use crate::common::I64String;
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize, utoipa::ToSchema)]
#[serde(rename_all(serialize = "camelCase", deserialize = "snake_case"))]
pub struct SysDictItem {
    pub id: I64String,
    pub dict_type_id: I64String,
    pub label: String,
    pub value: String,
    pub is_default: i32,
    pub status: i32,
    pub sort: i32,
    pub builtin: i32,
    pub created_at: Option<String>,
    pub updated_at: Option<String>,
}
