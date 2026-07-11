use serde::Serialize;

#[derive(Debug, Clone, Serialize, utoipa::ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct HealthVo {
    pub status: String,
    pub service: String,
    pub timestamp: String,
}
