use super::ok;
use crate::common::ApiResponse;
use crate::modules::system::vo::health_vo::HealthVo;
use axum::Json;
use axum::http::StatusCode;
use axum::response::{IntoResponse, Redirect, Response};
use chrono::Utc;
use serde_json::{Value, json};

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

pub async fn openapi_docs() -> Json<Value> {
    Json(json!({
        "openapi": "3.0.3",
        "info": {"title": "Drip Admin Rust API", "version": "0.1.0"},
        "paths": {
            "/system/publicConfig": {"get": {}},
            "/system/login": {"post": {}},
            "/system/logout": {"post": {}},
            "/system/me": {"get": {}},
            "/system/password": {"put": {}},
            "/system/profile": {"put": {}},
            "/system/user": {"get": {}, "post": {}},
            "/system/user/{id}": {"get": {}, "put": {}, "delete": {}},
            "/system/user/{id}/status": {"put": {}},
            "/system/user/{id}/unlock": {"post": {}},
            "/system/user/{id}/role": {"put": {}},
            "/system/user/{id}/resetPassword": {"post": {}},
            "/system/role": {"get": {}, "post": {}},
            "/system/role/option": {"get": {}},
            "/system/role/{id}": {"get": {}, "put": {}, "delete": {}},
            "/system/role/{id}/user": {"get": {}},
            "/system/role/{id}/permission": {"get": {}, "put": {}},
            "/system/role/{id}/status": {"put": {}},
            "/system/menu": {"get": {}, "post": {}},
            "/system/menu/{id}": {"put": {}, "delete": {}},
            "/system/menu/{id}/status": {"put": {}},
            "/system/dept": {"get": {}, "post": {}},
            "/system/dept/{id}": {"get": {}, "put": {}, "delete": {}},
            "/system/dept/{id}/status": {"put": {}},
            "/system/config": {"get": {}, "post": {}},
            "/system/config/{id}": {"put": {}, "delete": {}},
            "/system/config/{id}/status": {"put": {}},
            "/system/dict/type": {"get": {}, "post": {}},
            "/system/dict/type/{id}": {"put": {}, "delete": {}},
            "/system/dict/type/{id}/item": {"get": {}},
            "/system/dict/item": {"post": {}},
            "/system/dict/item/{id}": {"put": {}, "delete": {}},
            "/system/dict/item/{id}/status": {"put": {}},
            "/system/dict/cache/refresh": {"post": {}},
            "/system/onlineUser": {"get": {}},
            "/system/onlineUser/{tokenId}": {"get": {}},
            "/system/onlineUser/{tokenId}/kickout": {"post": {}},
            "/system/loginLog": {"get": {}},
            "/system/loginLog/{id}": {"get": {}},
            "/system/operationLog": {"get": {}},
            "/system/operationLog/{id}": {"get": {}},
            "/system/job": {"get": {}, "post": {}},
            "/system/job/scripts": {"get": {}},
            "/system/job/{id}": {"get": {}, "put": {}, "delete": {}},
            "/system/job/{id}/status": {"put": {}},
            "/system/job/{id}/run": {"post": {}},
            "/system/job/{id}/runLog": {"get": {}},
            "/system/jobRunLog": {"get": {}},
            "/system/files": {"post": {}},
            "/system/print-template": {"get": {}, "post": {}},
            "/system/print-template/{id}": {"get": {}, "put": {}, "delete": {}},
            "/system/print-template/{id}/copy": {"post": {}},
            "/system/print-template/{id}/status": {"put": {}}
        }
    }))
}

pub async fn swagger_ui() -> Redirect {
    Redirect::temporary("swagger-ui/index.html")
}

pub async fn swagger_ui_asset() -> Response {
    StatusCode::OK.into_response()
}
