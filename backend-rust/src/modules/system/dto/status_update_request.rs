use serde::Deserialize;

#[derive(Debug, Clone, Deserialize)]
pub struct StatusUpdateRequest {
    pub status: i32,
}
