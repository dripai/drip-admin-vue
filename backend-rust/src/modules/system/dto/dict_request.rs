use serde::Deserialize;

#[derive(Debug, Clone, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct DictTypeSaveRequest {
    pub dict_name: String,
    pub dict_code: String,
    pub status: Option<i32>,
    pub builtin: Option<i32>,
    pub remark: Option<String>,
}

#[derive(Debug, Clone, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct DictItemSaveRequest {
    pub dict_type_id: i64,
    pub label: String,
    pub value: String,
    pub is_default: Option<i32>,
    pub status: Option<i32>,
    pub sort: Option<i32>,
    pub builtin: Option<i32>,
}
