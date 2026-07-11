use serde::Serialize;

#[derive(Debug, Clone, Serialize, utoipa::ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct FileUploadVo {
    pub url: String,
    pub filename: String,
    pub original_name: String,
    pub size: i64,
}
