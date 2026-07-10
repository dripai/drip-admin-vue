use serde::Deserialize;
use serde_json::Value;

#[derive(Debug, Clone, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct PrintTemplateSaveRequest {
    pub template_code: String,
    pub template_name: String,
    pub paper_type: Option<String>,
    pub content: Value,
    pub status: Option<i32>,
    pub remark: Option<String>,
}

#[derive(Debug, Clone, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct PrintTemplateCopyRequest {
    pub template_code: String,
    pub template_name: String,
}
