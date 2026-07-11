use super::ok;
use crate::common::ApiResponse;
use crate::modules::system::vo::health_vo::HealthVo;
use axum::Json;
use axum::http::StatusCode;
use axum::response::Redirect;
use chrono::Utc;

pub async fn root() -> Redirect {
    Redirect::temporary("swagger-ui/index.html")
}

pub async fn favicon() -> StatusCode {
    StatusCode::NO_CONTENT
}

pub async fn health() -> Json<ApiResponse<HealthVo>> {
    ok(HealthVo {
        status: "UP".to_string(),
        service: "drip-admin-backend".to_string(),
        timestamp: Utc::now().to_rfc3339_opts(chrono::SecondsFormat::Millis, true),
    })
}
