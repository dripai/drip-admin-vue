use crate::common::ApiResponse;
use axum::Json;
use serde::Serialize;

pub mod auth_controller;
pub mod common_controller;
pub mod config_controller;
pub mod dept_controller;
pub mod dict_controller;
pub mod file_controller;
pub mod job_controller;
pub mod log_controller;
pub mod menu_controller;
pub mod online_user_controller;
pub mod print_template_controller;
pub mod role_controller;
pub mod user_controller;

pub fn ok<T>(data: T) -> Json<ApiResponse<T>>
where
    T: Serialize,
{
    Json(ApiResponse::success(data))
}

pub fn ok_null() -> Json<ApiResponse<()>> {
    Json(ApiResponse::success_null())
}
