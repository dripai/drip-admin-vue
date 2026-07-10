use serde::Serialize;

#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct HealthVo {
    pub status: String,
    pub service: String,
    pub timestamp: String,
}
