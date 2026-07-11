use crate::common::next_id;
use crate::modules::system::service::session_service;
use crate::modules::system::AppState;
use axum::{
    extract::{Request, State},
    middleware::Next,
    response::Response,
};
use std::time::Instant;

pub async fn record_request(
    State(state): State<AppState>,
    request: Request,
    next: Next,
) -> Response {
    let method = request.method().as_str().to_string();
    let path = request.uri().path().to_string();
    let definition = definition(&method, &path);
    let session = if definition.is_some() {
        match (
            request
                .headers()
                .get(&state.settings.token.name)
                .and_then(|value| value.to_str().ok()),
            state.redis_pool.as_ref(),
        ) {
            (Some(token), Some(pool)) => session_service::load_session(
                pool,
                token,
                state.settings.token.active_timeout_seconds,
            )
            .await
            .ok(),
            _ => None,
        }
    } else {
        None
    };
    let started = Instant::now();
    let response = next.run(request).await;
    let cost_ms = started.elapsed().as_millis() as i64;
    let status_code = response.status().as_u16();

    tracing::info!(%method, %path, status_code, cost_ms, "request completed");

    if let Some((module, action)) = definition {
        if let Some(database) = state.database.as_ref() {
            let (operator_id, operator_name) = session
                .map(|session| {
                    let name = if session.real_name.trim().is_empty() {
                        session.username
                    } else {
                        session.real_name
                    };
                    (Some(session.user_id.value()), Some(name))
                })
                .unwrap_or((None, None));
            let response_status = if status_code < 400 { "SUCCESS" } else { "FAIL" };
            let error_message = (status_code >= 400)
                .then(|| response.status().canonical_reason().map(str::to_string))
                .flatten();
            if let Err(error) = database
                .exec(
                    "insert into sys_operation_log(id, operator_id, operator_name, module, action, method, path, response_status, error_message, cost_ms) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    vec![
                        rbs::value!(next_id()),
                        rbs::value!(operator_id),
                        rbs::value!(operator_name),
                        rbs::value!(module),
                        rbs::value!(action),
                        rbs::value!(&method),
                        rbs::value!(&path),
                        rbs::value!(response_status),
                        rbs::value!(error_message),
                        rbs::value!(cost_ms),
                    ],
                )
                .await
            {
                tracing::error!(%method, %path, %module, %action, error = %error, "operation log write failed");
            }
        } else {
            tracing::error!(%method, %path, %module, %action, "operation log skipped: database is not configured");
        }
    }

    response
}

fn definition(method: &str, path: &str) -> Option<(&'static str, &'static str)> {
    let definition = match (method, path) {
        ("PUT", "/api/system/profile") => ("个人中心", "编辑资料"),
        ("POST", "/api/system/user") => ("用户管理", "新增用户"),
        ("PUT", value) if is_entity_path(value, "/api/system/user") => ("用户管理", "编辑用户"),
        ("DELETE", value) if is_entity_path(value, "/api/system/user") => ("用户管理", "删除用户"),
        ("PUT", value) if is_entity_action_path(value, "/api/system/user", "status") => ("用户管理", "变更用户状态"),
        ("POST", value) if is_entity_action_path(value, "/api/system/user", "unlock") => ("用户管理", "解除登录锁定"),
        ("PUT", value) if is_entity_action_path(value, "/api/system/user", "role") => ("用户管理", "分配角色"),
        ("POST", value) if is_entity_action_path(value, "/api/system/user", "resetPassword") => ("用户管理", "重置密码"),
        ("POST", "/api/system/role") => ("角色管理", "新增角色"),
        ("PUT", value) if is_entity_path(value, "/api/system/role") => ("角色管理", "编辑角色"),
        ("DELETE", value) if is_entity_path(value, "/api/system/role") => ("角色管理", "删除角色"),
        ("PUT", value) if is_entity_action_path(value, "/api/system/role", "status") => ("角色管理", "变更角色状态"),
        ("PUT", value) if is_entity_action_path(value, "/api/system/role", "permission") => ("角色管理", "角色授权"),
        ("POST", "/api/system/menu") => ("菜单管理", "新增菜单"),
        ("PUT", value) if is_entity_path(value, "/api/system/menu") => ("菜单管理", "编辑菜单"),
        ("DELETE", value) if is_entity_path(value, "/api/system/menu") => ("菜单管理", "删除菜单"),
        ("PUT", value) if is_entity_action_path(value, "/api/system/menu", "status") => ("菜单管理", "变更菜单状态"),
        ("POST", "/api/system/dept") => ("部门管理", "新增部门"),
        ("PUT", value) if is_entity_path(value, "/api/system/dept") => ("部门管理", "编辑部门"),
        ("DELETE", value) if is_entity_path(value, "/api/system/dept") => ("部门管理", "删除部门"),
        ("PUT", value) if is_entity_action_path(value, "/api/system/dept", "status") => ("部门管理", "变更部门状态"),
        ("POST", "/api/system/config") => ("系统配置", "新增配置"),
        ("PUT", value) if is_entity_path(value, "/api/system/config") => ("系统配置", "编辑配置"),
        ("DELETE", value) if is_entity_path(value, "/api/system/config") => ("系统配置", "删除配置"),
        ("PUT", value) if is_entity_action_path(value, "/api/system/config", "status") => ("系统配置", "变更配置状态"),
        ("POST", "/api/system/dict/type") => ("字典管理", "新增字典类型"),
        ("PUT", value) if is_entity_path(value, "/api/system/dict/type") => ("字典管理", "编辑字典类型"),
        ("DELETE", value) if is_entity_path(value, "/api/system/dict/type") => ("字典管理", "删除字典类型"),
        ("POST", "/api/system/dict/item") => ("字典管理", "新增字典项"),
        ("PUT", value) if is_entity_path(value, "/api/system/dict/item") => ("字典管理", "编辑字典项"),
        ("DELETE", value) if is_entity_path(value, "/api/system/dict/item") => ("字典管理", "删除字典项"),
        ("PUT", value) if is_entity_action_path(value, "/api/system/dict/item", "status") => ("字典管理", "变更字典项状态"),
        ("POST", value) if is_entity_action_path(value, "/api/system/onlineUser", "kickout") => ("在线用户", "强制下线"),
        ("POST", "/api/system/job") => ("定时任务", "新增任务"),
        ("PUT", value) if is_entity_path(value, "/api/system/job") => ("定时任务", "编辑任务"),
        ("DELETE", value) if is_entity_path(value, "/api/system/job") => ("定时任务", "删除任务"),
        ("PUT", value) if is_entity_action_path(value, "/api/system/job", "status") => ("定时任务", "变更任务状态"),
        ("POST", value) if is_entity_action_path(value, "/api/system/job", "run") => ("定时任务", "手动执行任务"),
        ("POST", "/api/system/files") => ("文件上传", "上传文件"),
        ("POST", "/api/system/print-template") => ("打印模板", "新增打印模板"),
        ("POST", value) if is_entity_action_path(value, "/api/system/print-template", "copy") => ("打印模板", "复制打印模板"),
        ("PUT", value) if is_entity_path(value, "/api/system/print-template") => ("打印模板", "编辑打印模板"),
        ("DELETE", value) if is_entity_path(value, "/api/system/print-template") => ("打印模板", "删除打印模板"),
        ("PUT", value) if is_entity_action_path(value, "/api/system/print-template", "status") => ("打印模板", "变更打印模板状态"),
        _ => return None,
    };
    Some(definition)
}

fn is_entity_path(path: &str, prefix: &str) -> bool {
    path.strip_prefix(prefix)
        .and_then(|value| value.strip_prefix('/'))
        .is_some_and(|value| !value.is_empty() && !value.contains('/'))
}

fn is_entity_action_path(path: &str, prefix: &str, action: &str) -> bool {
    path.strip_suffix(&format!("/{action}"))
        .is_some_and(|value| is_entity_path(value, prefix))
}

#[cfg(test)]
mod tests {
    use super::definition;

    #[test]
    fn maps_concrete_paths_to_operation_metadata() {
        assert_eq!(
            definition("POST", "/api/system/print-template/1/copy"),
            Some(("打印模板", "复制打印模板"))
        );
        assert_eq!(
            definition("PUT", "/api/system/config/1"),
            Some(("系统配置", "编辑配置"))
        );
        assert_eq!(definition("GET", "/api/system/user"), None);
    }
}
