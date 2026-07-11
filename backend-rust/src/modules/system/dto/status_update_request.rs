use serde::Deserialize;

#[derive(Debug, Clone, Deserialize, utoipa::ToSchema)]
pub struct StatusUpdateRequest {
    pub status: i32,
}
