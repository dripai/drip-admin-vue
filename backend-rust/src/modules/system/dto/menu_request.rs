use serde::Deserialize;

#[derive(Debug, Clone, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct MenuSaveRequest {
    pub parent_id: Option<i64>,
    pub name: String,
    pub menu_type: String,
    pub path: Option<String>,
    pub component: Option<String>,
    pub permission_code: Option<String>,
    pub icon: Option<String>,
    pub sort: Option<i32>,
    pub visible: Option<i32>,
    pub status: Option<i32>,
}
