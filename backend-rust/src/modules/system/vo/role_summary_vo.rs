use crate::common::I64String;
use serde::Serialize;

#[derive(Debug, Clone, Serialize, utoipa::ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct RoleSummaryVo {
    pub id: I64String,
    pub role_name: String,
    pub role_code: String,
}
