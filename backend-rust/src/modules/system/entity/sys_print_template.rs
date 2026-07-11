use crate::common::I64String;
use serde::{Deserialize, Serialize};
use serde_json::Value;

#[derive(Debug, Clone, Serialize, Deserialize, utoipa::ToSchema)]
#[serde(rename_all(serialize = "camelCase", deserialize = "snake_case"))]
pub struct SysPrintTemplate {
    pub id: I64String,
    pub template_code: String,
    pub template_name: String,
    pub paper_type: String,
    pub content: Value,
    pub status: i32,
    pub deleted: i32,
}
