use axum::body::Body;
use axum::http::{Request, StatusCode};
use drip_admin_rust::common::{ApiResponse, I64String, PageParams, PageQuery, PageResult};
use drip_admin_rust::config::Settings;
use drip_admin_rust::modules::system::dto::menu_request::MenuSaveRequest;
use drip_admin_rust::modules::system::{AppState, router};
use http_body_util::BodyExt;
use serde_json::{Value, json};
use std::fs;
use std::path::{Path, PathBuf};
use std::sync::Arc;
use tower::ServiceExt;

fn app() -> axum::Router {
    router::create_router(AppState::new(Arc::new(Settings::default()), None, None))
}

async fn json_response(response: axum::response::Response) -> Value {
    let bytes = response.into_body().collect().await.unwrap().to_bytes();
    serde_json::from_slice(&bytes).unwrap()
}

#[tokio::test]
async fn route_and_public_response_contract() {
    let response = app()
        .oneshot(
            Request::builder()
                .uri("/api/health")
                .body(Body::empty())
                .unwrap(),
        )
        .await
        .unwrap();
    assert_eq!(response.status(), StatusCode::OK);
    let payload = json_response(response).await;
    assert_eq!(payload["code"], 0);
    assert_eq!(payload["message"], "success");
    assert_eq!(payload["data"]["status"], "UP");
    assert_eq!(payload["data"]["service"], "drip-admin-backend");
    assert!(
        payload["data"]["timestamp"]
            .as_str()
            .unwrap()
            .ends_with('Z')
    );

    let response = app()
        .oneshot(Request::builder().uri("/api/").body(Body::empty()).unwrap())
        .await
        .unwrap();
    assert_eq!(response.status(), StatusCode::TEMPORARY_REDIRECT);

    let response = app()
        .oneshot(
            Request::builder()
                .uri("/api/favicon.ico")
                .body(Body::empty())
                .unwrap(),
        )
        .await
        .unwrap();
    assert_eq!(response.status(), StatusCode::NO_CONTENT);
}

#[tokio::test]
async fn openapi_contains_expected_route_contract() {
    let response = app()
        .oneshot(
            Request::builder()
                .uri("/api/v3/api-docs")
                .body(Body::empty())
                .unwrap(),
        )
        .await
        .unwrap();
    assert_eq!(response.status(), StatusCode::OK);
    let payload = json_response(response).await;
    let paths = payload["paths"].as_object().unwrap();
    for path in expected_paths() {
        assert!(paths.contains_key(path), "{path}");
    }
}

#[tokio::test]
async fn response_pagination_long_and_error_contract() {
    let payload = serde_json::to_value(ApiResponse::success(PageResult::<Value> {
        list: vec![json!({"id": I64String(2070959624609583106), "parentId": I64String(0)})],
        total: I64String(2070959624609583106),
        page: 1,
        page_size: 10,
    }))
    .unwrap();
    assert_eq!(
        payload,
        json!({
            "code":0,
            "message":"success",
            "data":{
                "list":[{"id":"2070959624609583106","parentId":"0"}],
                "total":"2070959624609583106",
                "page":1,
                "pageSize":10
            }
        })
    );

    let failure = serde_json::to_value(ApiResponse::failure(400000, "错误信息")).unwrap();
    assert_eq!(
        failure,
        json!({"code":400000,"message":"错误信息","data":null})
    );

    assert_eq!(
        PageQuery::default().normalize().unwrap(),
        PageParams {
            page: 1,
            page_size: 10
        }
    );
    assert_eq!(
        PageQuery {
            page: Some(0),
            page_size: Some(10)
        }
        .normalize()
        .unwrap_err()
        .code,
        400000
    );
}

#[test]
fn dict_response_contract() {
    let payload = json!({
        "code": 0,
        "message": "success",
        "data": [
            {
                "id": I64String(1),
                "dictName": "状态",
                "dictCode": "common_status",
                "status": 1,
                "builtin": 1,
                "remark": "通用启停状态",
                "createdAt": null,
                "updatedAt": null
            }
        ]
    });
    assert!(payload["data"].is_array());
    assert!(payload["data"]["list"].is_null());
    assert_eq!(payload["data"][0]["id"], "1");
}

#[test]
fn menu_request_contract_uses_java_type_field() {
    let request: MenuSaveRequest = serde_json::from_value(json!({
        "parentId": 0,
        "name": "菜单管理",
        "type": "MENU",
        "path": "/system/menu",
        "component": "system/menu/index",
        "permissionCode": "system:menu:list",
        "icon": "menu",
        "sort": 30,
        "visible": 1,
        "status": 1
    }))
    .unwrap();
    assert_eq!(request.r#type, "MENU");
}

#[tokio::test]
async fn auth_error_contract() {
    let response = app()
        .oneshot(
            Request::builder()
                .method("POST")
                .uri("/api/system/login")
                .header("content-type", "application/json")
                .body(Body::from("{"))
                .unwrap(),
        )
        .await
        .unwrap();
    assert_eq!(response.status(), StatusCode::BAD_REQUEST);
    let payload = json_response(response).await;
    assert_eq!(payload["code"], 400000);
    assert_eq!(payload["data"], Value::Null);

    let response = app()
        .oneshot(
            Request::builder()
                .uri("/api/system/me")
                .body(Body::empty())
                .unwrap(),
        )
        .await
        .unwrap();
    assert_eq!(response.status(), StatusCode::UNAUTHORIZED);
    let payload = json_response(response).await;
    assert_eq!(payload["code"], 401000);
    assert_eq!(payload["data"], Value::Null);
}

#[test]
fn permission_contract() {
    let source = fs::read_to_string(project_root().join("src/modules/system/router.rs")).unwrap();
    for permission in expected_permissions() {
        assert!(
            source.contains(&format!("require_permission(\"{permission}\")")),
            "{permission}"
        );
    }
}

#[test]
fn layered_structure_contract() {
    let system = project_root().join("src/modules/system");
    for dir in ["controller", "dto", "entity", "service", "vo"] {
        assert!(system.join(dir).is_dir(), "{dir}");
    }
    for forbidden in [
        "models.rs",
        "entity.rs",
        "handlers.rs",
        "controllers.rs",
        "services.rs",
    ] {
        assert!(!system.join(forbidden).exists(), "{forbidden}");
    }
    for file in [
        "controller/auth_controller.rs",
        "controller/config_controller.rs",
        "controller/dept_controller.rs",
        "controller/dict_controller.rs",
        "controller/file_controller.rs",
        "controller/job_controller.rs",
        "controller/log_controller.rs",
        "controller/menu_controller.rs",
        "controller/online_user_controller.rs",
        "controller/print_template_controller.rs",
        "controller/role_controller.rs",
        "controller/user_controller.rs",
        "dto/user_request.rs",
        "dto/role_request.rs",
        "dto/menu_request.rs",
        "dto/dept_request.rs",
        "dto/config_request.rs",
        "dto/dict_request.rs",
        "dto/job_request.rs",
        "dto/print_template_request.rs",
        "entity/sys_user.rs",
        "entity/sys_role.rs",
        "entity/sys_menu.rs",
        "entity/sys_dept.rs",
        "service/user_service.rs",
        "service/role_service.rs",
        "service/menu_service.rs",
        "service/dept_service.rs",
        "service/config_service.rs",
        "service/dict_service.rs",
        "service/online_user_service.rs",
        "service/login_log_service.rs",
        "service/login_security_service.rs",
        "service/log_service.rs",
        "service/job_service.rs",
        "service/file_service.rs",
        "service/operation_log_service.rs",
        "service/permission_service.rs",
        "service/print_template_service.rs",
        "vo/user_list_vo.rs",
        "vo/role_permission_vo.rs",
        "vo/menu_tree_vo.rs",
        "vo/dept_tree_vo.rs",
        "vo/operation_log_vo.rs",
    ] {
        assert!(system.join(file).is_file(), "{file}");
    }
}

#[test]
fn database_contract_has_no_mutation_or_migrations() {
    let root = project_root();
    let mut source = String::new();
    collect_rs_source(&root.join("src"), &mut source);
    assert!(!source.contains("sea-orm-migration"));
    assert!(!source.contains("AutoMigrate"));
    assert!(!source.contains("create_all"));
    assert!(!source.to_uppercase().contains("CREATE TABLE"));
    assert!(!source.to_uppercase().contains("ALTER TABLE"));
    assert!(!root.join("migrations").exists());
}

fn collect_rs_source(path: &Path, out: &mut String) {
    for entry in fs::read_dir(path).unwrap() {
        let path = entry.unwrap().path();
        if path.is_dir() {
            collect_rs_source(&path, out);
        } else if path.extension().and_then(|value| value.to_str()) == Some("rs") {
            out.push_str(&fs::read_to_string(path).unwrap());
        }
    }
}

fn project_root() -> PathBuf {
    PathBuf::from(env!("CARGO_MANIFEST_DIR"))
}

fn expected_paths() -> Vec<&'static str> {
    vec![
        "/system/publicConfig",
        "/system/login",
        "/system/logout",
        "/system/me",
        "/system/password",
        "/system/profile",
        "/system/user",
        "/system/user/{id}",
        "/system/user/{id}/status",
        "/system/user/{id}/unlock",
        "/system/user/{id}/role",
        "/system/user/{id}/resetPassword",
        "/system/role",
        "/system/role/option",
        "/system/role/{id}",
        "/system/role/{id}/user",
        "/system/role/{id}/permission",
        "/system/role/{id}/status",
        "/system/menu",
        "/system/menu/{id}",
        "/system/menu/{id}/status",
        "/system/dept",
        "/system/dept/{id}",
        "/system/dept/{id}/status",
        "/system/config",
        "/system/config/{id}",
        "/system/config/{id}/status",
        "/system/dict/type",
        "/system/dict/type/{id}",
        "/system/dict/type/{id}/item",
        "/system/dict/item",
        "/system/dict/item/{id}",
        "/system/dict/item/{id}/status",
        "/system/dict/cache/refresh",
        "/system/onlineUser",
        "/system/onlineUser/{tokenId}",
        "/system/onlineUser/{tokenId}/kickout",
        "/system/loginLog",
        "/system/loginLog/{id}",
        "/system/operationLog",
        "/system/operationLog/{id}",
        "/system/job",
        "/system/job/scripts",
        "/system/job/{id}",
        "/system/job/{id}/status",
        "/system/job/{id}/run",
        "/system/job/{id}/runLog",
        "/system/jobRunLog",
        "/system/files",
        "/system/print-template",
        "/system/print-template/{id}",
        "/system/print-template/{id}/copy",
        "/system/print-template/{id}/status",
    ]
}

fn expected_permissions() -> Vec<&'static str> {
    vec![
        "system:user:list",
        "system:user:detail",
        "system:user:create",
        "system:user:update",
        "system:user:delete",
        "system:user:disable",
        "system:user:unlock",
        "system:user:assignRole",
        "system:user:resetPassword",
        "system:role:list",
        "system:role:permission",
        "system:role:create",
        "system:role:update",
        "system:role:delete",
        "system:menu:list",
        "system:menu:create",
        "system:menu:update",
        "system:menu:delete",
        "system:menu:status",
        "system:dept:list",
        "system:dept:create",
        "system:dept:update",
        "system:dept:delete",
        "system:config:list",
        "system:config:create",
        "system:config:update",
        "system:config:delete",
        "system:dict:list",
        "system:dict:create",
        "system:dict:update",
        "system:dict:delete",
        "system:online:list",
        "system:online:kickout",
        "system:loginLog:list",
        "system:operationLog:list",
        "system:job:list",
        "system:job:create",
        "system:job:update",
        "system:job:delete",
        "system:job:run",
        "system:job:history",
        "system:file:upload",
        "system:printTemplate:list",
        "system:printTemplate:create",
        "system:printTemplate:update",
        "system:printTemplate:delete",
    ]
}
