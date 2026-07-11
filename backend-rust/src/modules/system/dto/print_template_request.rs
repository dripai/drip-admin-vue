use serde::Deserialize;

#[derive(Debug, Clone, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct PrintTemplateSaveRequest {
    pub code: String,
    pub name: String,
    pub paper_type: Option<String>,
    pub template_json: String,
    pub status: i32,
}

#[derive(Debug, Clone, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct PrintTemplateCopyRequest {
    pub code: String,
    pub name: String,
    pub status: i32,
}
