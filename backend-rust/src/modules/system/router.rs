use super::AppState;
use super::controller::{
    auth_controller, common_controller, config_controller, dept_controller, dict_controller,
    file_controller, job_controller, log_controller, menu_controller, online_user_controller,
    print_template_controller, role_controller, user_controller,
};
use super::service::permission_service::{auth_required, require_permission};
use super::service::request_log_service::record_request;
use axum::Router;
use axum::middleware;
use axum::routing::{delete, get, post, put};
use tower_http::cors::{Any, CorsLayer};
use tower_http::trace::TraceLayer;
use utoipa_swagger_ui::SwaggerUi;

pub fn create_router(state: AppState) -> Router {
    let cors = CorsLayer::new()
        .allow_origin(Any)
        .allow_methods(Any)
        .allow_headers(Any);

    let public = Router::new()
        .route("/", get(common_controller::root))
        .route("/favicon.ico", get(common_controller::favicon))
        .route("/health", get(common_controller::health))
        .route(
            "/system/publicConfig",
            get(config_controller::public_config),
        )
        .route("/system/login", post(auth_controller::login))
        .merge(SwaggerUi::new("/swagger-ui").url("/v3/api-docs", super::api_doc::openapi()));

    let protected = Router::new()
        .route("/system/logout", post(auth_controller::logout))
        .route("/system/me", get(auth_controller::me))
        .route("/system/password", put(auth_controller::password))
        .route("/system/profile", put(auth_controller::profile))
        .route(
            "/system/user",
            get(user_controller::list).route_layer(require_permission("system:user:list")),
        )
        .route(
            "/system/user/{id}",
            get(user_controller::detail).route_layer(require_permission("system:user:detail")),
        )
        .route(
            "/system/user",
            post(user_controller::create).route_layer(require_permission("system:user:create")),
        )
        .route(
            "/system/user/{id}",
            put(user_controller::update).route_layer(require_permission("system:user:update")),
        )
        .route(
            "/system/user/{id}",
            delete(user_controller::delete).route_layer(require_permission("system:user:delete")),
        )
        .route(
            "/system/user/{id}/status",
            put(user_controller::status).route_layer(require_permission("system:user:disable")),
        )
        .route(
            "/system/user/{id}/unlock",
            post(user_controller::unlock).route_layer(require_permission("system:user:unlock")),
        )
        .route(
            "/system/user/{id}/role",
            put(user_controller::assign_roles)
                .route_layer(require_permission("system:user:assignRole")),
        )
        .route(
            "/system/user/{id}/resetPassword",
            post(user_controller::reset_password)
                .route_layer(require_permission("system:user:resetPassword")),
        )
        .merge(role_routes())
        .merge(menu_routes())
        .merge(dept_routes())
        .merge(config_routes())
        .merge(dict_routes())
        .merge(online_user_routes())
        .merge(log_routes())
        .merge(job_routes())
        .merge(file_routes())
        .merge(print_template_routes())
        .route_layer(middleware::from_fn_with_state(state.clone(), auth_required));

    Router::new()
        .route("/api/", get(common_controller::root))
        .nest("/api", public.merge(protected))
        .layer(middleware::from_fn_with_state(state.clone(), record_request))
        .layer(cors)
        .layer(TraceLayer::new_for_http())
        .with_state(state)
}

fn role_routes() -> Router<AppState> {
    Router::new()
        .route(
            "/system/role",
            get(role_controller::list).route_layer(require_permission("system:role:list")),
        )
        .route(
            "/system/role/option",
            get(role_controller::options).route_layer(require_permission("system:role:list")),
        )
        .route(
            "/system/role/{id}",
            get(role_controller::detail).route_layer(require_permission("system:role:list")),
        )
        .route(
            "/system/role/{id}/user",
            get(role_controller::users).route_layer(require_permission("system:role:list")),
        )
        .route(
            "/system/role/{id}/permission",
            get(role_controller::permissions)
                .route_layer(require_permission("system:role:permission")),
        )
        .route(
            "/system/role",
            post(role_controller::create).route_layer(require_permission("system:role:create")),
        )
        .route(
            "/system/role/{id}",
            put(role_controller::update).route_layer(require_permission("system:role:update")),
        )
        .route(
            "/system/role/{id}",
            delete(role_controller::delete).route_layer(require_permission("system:role:delete")),
        )
        .route(
            "/system/role/{id}/status",
            put(role_controller::status).route_layer(require_permission("system:role:update")),
        )
        .route(
            "/system/role/{id}/permission",
            put(role_controller::assign_permissions)
                .route_layer(require_permission("system:role:permission")),
        )
}

fn menu_routes() -> Router<AppState> {
    Router::new()
        .route(
            "/system/menu",
            get(menu_controller::list).route_layer(require_permission("system:menu:list")),
        )
        .route(
            "/system/menu",
            post(menu_controller::create).route_layer(require_permission("system:menu:create")),
        )
        .route(
            "/system/menu/{id}",
            put(menu_controller::update).route_layer(require_permission("system:menu:update")),
        )
        .route(
            "/system/menu/{id}",
            delete(menu_controller::delete).route_layer(require_permission("system:menu:delete")),
        )
        .route(
            "/system/menu/{id}/status",
            put(menu_controller::status).route_layer(require_permission("system:menu:status")),
        )
}

fn dept_routes() -> Router<AppState> {
    Router::new()
        .route(
            "/system/dept",
            get(dept_controller::list).route_layer(require_permission("system:dept:list")),
        )
        .route(
            "/system/dept/{id}",
            get(dept_controller::detail).route_layer(require_permission("system:dept:list")),
        )
        .route(
            "/system/dept",
            post(dept_controller::create).route_layer(require_permission("system:dept:create")),
        )
        .route(
            "/system/dept/{id}",
            put(dept_controller::update).route_layer(require_permission("system:dept:update")),
        )
        .route(
            "/system/dept/{id}",
            delete(dept_controller::delete).route_layer(require_permission("system:dept:delete")),
        )
        .route(
            "/system/dept/{id}/status",
            put(dept_controller::status).route_layer(require_permission("system:dept:update")),
        )
}

fn config_routes() -> Router<AppState> {
    Router::new()
        .route(
            "/system/config",
            get(config_controller::list).route_layer(require_permission("system:config:list")),
        )
        .route(
            "/system/config",
            post(config_controller::create).route_layer(require_permission("system:config:create")),
        )
        .route(
            "/system/config/{id}",
            get(config_controller::detail).route_layer(require_permission("system:config:list")),
        )
        .route(
            "/system/config/{id}",
            put(config_controller::update).route_layer(require_permission("system:config:update")),
        )
        .route(
            "/system/config/{id}",
            delete(config_controller::delete)
                .route_layer(require_permission("system:config:delete")),
        )
        .route(
            "/system/config/{id}/status",
            put(config_controller::status).route_layer(require_permission("system:config:update")),
        )
}

fn dict_routes() -> Router<AppState> {
    Router::new()
        .route(
            "/system/dict/type",
            get(dict_controller::type_list).route_layer(require_permission("system:dict:list")),
        )
        .route(
            "/system/dict/type",
            post(dict_controller::create_type)
                .route_layer(require_permission("system:dict:create")),
        )
        .route(
            "/system/dict/type/{id}",
            put(dict_controller::update_type).route_layer(require_permission("system:dict:update")),
        )
        .route(
            "/system/dict/type/{id}",
            delete(dict_controller::delete_type)
                .route_layer(require_permission("system:dict:delete")),
        )
        .route(
            "/system/dict/type/{id}/item",
            get(dict_controller::items).route_layer(require_permission("system:dict:list")),
        )
        .route(
            "/system/dict/item",
            post(dict_controller::create_item)
                .route_layer(require_permission("system:dict:create")),
        )
        .route(
            "/system/dict/item/{id}",
            put(dict_controller::update_item).route_layer(require_permission("system:dict:update")),
        )
        .route(
            "/system/dict/item/{id}",
            delete(dict_controller::delete_item)
                .route_layer(require_permission("system:dict:delete")),
        )
        .route(
            "/system/dict/item/{id}/status",
            put(dict_controller::item_status).route_layer(require_permission("system:dict:update")),
        )
        .route(
            "/system/dict/cache/refresh",
            post(dict_controller::refresh_cache)
                .route_layer(require_permission("system:dict:update")),
        )
}

fn online_user_routes() -> Router<AppState> {
    Router::new()
        .route(
            "/system/onlineUser",
            get(online_user_controller::list).route_layer(require_permission("system:online:list")),
        )
        .route(
            "/system/onlineUser/{tokenId}",
            get(online_user_controller::detail)
                .route_layer(require_permission("system:online:list")),
        )
        .route(
            "/system/onlineUser/{tokenId}/kickout",
            post(online_user_controller::kickout)
                .route_layer(require_permission("system:online:kickout")),
        )
}

fn log_routes() -> Router<AppState> {
    Router::new()
        .route(
            "/system/loginLog",
            get(log_controller::login_logs).route_layer(require_permission("system:loginLog:list")),
        )
        .route(
            "/system/loginLog/{id}",
            get(log_controller::login_log).route_layer(require_permission("system:loginLog:list")),
        )
        .route(
            "/system/operationLog",
            get(log_controller::operation_logs)
                .route_layer(require_permission("system:operationLog:list")),
        )
        .route(
            "/system/operationLog/{id}",
            get(log_controller::operation_log)
                .route_layer(require_permission("system:operationLog:list")),
        )
}

fn job_routes() -> Router<AppState> {
    Router::new()
        .route(
            "/system/job",
            get(job_controller::list).route_layer(require_permission("system:job:list")),
        )
        .route(
            "/system/job/scripts",
            get(job_controller::scripts).route_layer(require_permission("system:job:list")),
        )
        .route(
            "/system/job/{id}",
            get(job_controller::detail).route_layer(require_permission("system:job:list")),
        )
        .route(
            "/system/job",
            post(job_controller::create).route_layer(require_permission("system:job:create")),
        )
        .route(
            "/system/job/{id}",
            put(job_controller::update).route_layer(require_permission("system:job:update")),
        )
        .route(
            "/system/job/{id}",
            delete(job_controller::delete).route_layer(require_permission("system:job:delete")),
        )
        .route(
            "/system/job/{id}/status",
            put(job_controller::status).route_layer(require_permission("system:job:update")),
        )
        .route(
            "/system/job/{id}/run",
            post(job_controller::run).route_layer(require_permission("system:job:run")),
        )
        .route(
            "/system/job/{id}/runLog",
            get(job_controller::run_logs).route_layer(require_permission("system:job:list")),
        )
        .route(
            "/system/jobRunLog",
            get(job_controller::all_run_logs).route_layer(require_permission("system:job:history")),
        )
}

fn file_routes() -> Router<AppState> {
    Router::new().route(
        "/system/files",
        post(file_controller::upload).route_layer(require_permission("system:file:upload")),
    )
}

fn print_template_routes() -> Router<AppState> {
    Router::new()
        .route(
            "/system/print-template",
            get(print_template_controller::list)
                .route_layer(require_permission("system:printTemplate:list")),
        )
        .route(
            "/system/print-template/{id}",
            get(print_template_controller::detail)
                .route_layer(require_permission("system:printTemplate:list")),
        )
        .route(
            "/system/print-template",
            post(print_template_controller::create)
                .route_layer(require_permission("system:printTemplate:create")),
        )
        .route(
            "/system/print-template/{id}/copy",
            post(print_template_controller::copy)
                .route_layer(require_permission("system:printTemplate:create")),
        )
        .route(
            "/system/print-template/{id}",
            put(print_template_controller::update)
                .route_layer(require_permission("system:printTemplate:update")),
        )
        .route(
            "/system/print-template/{id}",
            delete(print_template_controller::delete)
                .route_layer(require_permission("system:printTemplate:delete")),
        )
        .route(
            "/system/print-template/{id}/status",
            put(print_template_controller::status)
                .route_layer(require_permission("system:printTemplate:update")),
        )
}
