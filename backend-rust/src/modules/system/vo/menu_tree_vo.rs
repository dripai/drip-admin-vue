use crate::common::I64String;
use serde::Serialize;

#[derive(Debug, Clone, Serialize, utoipa::ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct MenuTreeVo {
    pub id: I64String,
    pub parent_id: I64String,
    pub name: String,
    pub r#type: String,
    pub path: Option<String>,
    pub component: Option<String>,
    pub permission_code: Option<String>,
    pub icon: Option<String>,
    pub sort: i32,
    pub visible: i32,
    pub status: i32,
    #[schema(no_recursion)]
    pub children: Vec<MenuTreeVo>,
}
