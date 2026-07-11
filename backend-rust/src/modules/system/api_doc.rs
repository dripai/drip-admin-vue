use serde::Serialize;
use utoipa::{OpenApi, ToSchema};
use crate::common::{ApiResponse, I64String, PageQuery, PageResult};
use crate::modules::system::dto::auth_request::{LoginRequest, PasswordRequest, ProfileUpdateRequest};
use crate::modules::system::dto::config_request::{ConfigQuery, ConfigSaveRequest};
use crate::modules::system::dto::dept_request::DeptSaveRequest;
use crate::modules::system::dto::dict_request::{DictItemSaveRequest, DictTypeSaveRequest};
use crate::modules::system::dto::job_request::JobSaveRequest;
use crate::modules::system::dto::log_request::LogQuery;
use crate::modules::system::dto::menu_request::MenuSaveRequest;
use crate::modules::system::dto::print_template_request::{PrintTemplateCopyRequest, PrintTemplateSaveRequest};
use crate::modules::system::dto::role_request::{RolePermissionAssignRequest, RoleQuery, RoleSaveRequest};
use crate::modules::system::dto::status_update_request::StatusUpdateRequest;
use crate::modules::system::dto::user_request::{PasswordResetRequest, UserQuery, UserRoleAssignRequest, UserSaveRequest};
use crate::modules::system::entity::sys_config::SysConfig;
use crate::modules::system::entity::sys_db_backup::SysDbBackup;
use crate::modules::system::entity::sys_dept::SysDept;
use crate::modules::system::entity::sys_dict_item::SysDictItem;
use crate::modules::system::entity::sys_dict_type::SysDictType;
use crate::modules::system::entity::sys_job::SysJob;
use crate::modules::system::entity::sys_job_run_log::SysJobRunLog;
use crate::modules::system::entity::sys_login_log::SysLoginLog;
use crate::modules::system::entity::sys_menu::SysMenu;
use crate::modules::system::entity::sys_operation_log::SysOperationLog;
use crate::modules::system::entity::sys_print_template::SysPrintTemplate;
use crate::modules::system::entity::sys_role::SysRole;
use crate::modules::system::entity::sys_role_menu::SysRoleMenu;
use crate::modules::system::entity::sys_user::SysUser;
use crate::modules::system::entity::sys_user_role::SysUserRole;
use crate::modules::system::service::login_log_service::LoginLog;
use crate::modules::system::service::print_template_service::PrintTemplate;
use crate::modules::system::vo::auth_vo::{AuthLoginVo, AuthMeVo};
use crate::modules::system::vo::dept_summary_vo::DeptSummaryVo;
use crate::modules::system::vo::dept_tree_vo::DeptTreeVo;
use crate::modules::system::vo::file_upload_vo::FileUploadVo;
use crate::modules::system::vo::health_vo::HealthVo;
use crate::modules::system::vo::menu_tree_vo::MenuTreeVo;
use crate::modules::system::vo::online_user_vo::OnlineUserVo;
use crate::modules::system::vo::operation_log_vo::OperationLogVo;
use crate::modules::system::vo::role_permission_vo::RolePermissionVo;
use crate::modules::system::vo::role_summary_vo::RoleSummaryVo;
use crate::modules::system::vo::user_list_vo::UserListVo;

#[derive(Serialize, ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct ApiResponseDocument {
    pub code: i32,
    pub message: String,
    pub data: serde_json::Value,
}

macro_rules! documented_endpoint {
    ($name:ident, $method:ident, $path:literal) => {
        #[allow(dead_code)]
        #[utoipa::path($method, path = $path, responses((status = 200, body = ApiResponseDocument)))]
        fn $name() {}
    };
}

documented_endpoint!(public_config, get, "/system/publicConfig");
#[allow(dead_code)]
#[utoipa::path(post, path = "/system/login", request_body = LoginRequest, responses((status = 200, body = ApiResponse<AuthLoginVo>)))]
fn login() {}
#[allow(dead_code)]
#[utoipa::path(post, path = "/system/logout", responses((status = 200, body = ApiResponseDocument)))]
fn logout() {}
#[allow(dead_code)]
#[utoipa::path(get, path = "/system/me", responses((status = 200, body = ApiResponse<AuthMeVo>)))]
fn me() {}
#[allow(dead_code)]
#[utoipa::path(put, path = "/system/password", request_body = PasswordRequest, responses((status = 200, body = ApiResponseDocument)))]
fn password() {}
#[allow(dead_code)]
#[utoipa::path(put, path = "/system/profile", request_body = ProfileUpdateRequest, responses((status = 200, body = ApiResponseDocument)))]
fn profile() {}
documented_endpoint!(users, get, "/system/user");
documented_endpoint!(create_user, post, "/system/user");
documented_endpoint!(user, get, "/system/user/{id}");
documented_endpoint!(update_user, put, "/system/user/{id}");
documented_endpoint!(delete_user, delete, "/system/user/{id}");
documented_endpoint!(user_status, put, "/system/user/{id}/status");
documented_endpoint!(user_unlock, post, "/system/user/{id}/unlock");
documented_endpoint!(user_role, put, "/system/user/{id}/role");
documented_endpoint!(user_reset_password, post, "/system/user/{id}/resetPassword");
documented_endpoint!(roles, get, "/system/role");
documented_endpoint!(create_role, post, "/system/role");
documented_endpoint!(role_option, get, "/system/role/option");
documented_endpoint!(role, get, "/system/role/{id}");
documented_endpoint!(update_role, put, "/system/role/{id}");
documented_endpoint!(delete_role, delete, "/system/role/{id}");
documented_endpoint!(role_users, get, "/system/role/{id}/user");
documented_endpoint!(role_permission, get, "/system/role/{id}/permission");
documented_endpoint!(assign_role_permission, put, "/system/role/{id}/permission");
documented_endpoint!(role_status, put, "/system/role/{id}/status");
documented_endpoint!(menus, get, "/system/menu");
documented_endpoint!(create_menu, post, "/system/menu");
documented_endpoint!(update_menu, put, "/system/menu/{id}");
documented_endpoint!(delete_menu, delete, "/system/menu/{id}");
documented_endpoint!(menu_status, put, "/system/menu/{id}/status");
documented_endpoint!(depts, get, "/system/dept");
documented_endpoint!(create_dept, post, "/system/dept");
documented_endpoint!(dept, get, "/system/dept/{id}");
documented_endpoint!(update_dept, put, "/system/dept/{id}");
documented_endpoint!(delete_dept, delete, "/system/dept/{id}");
documented_endpoint!(dept_status, put, "/system/dept/{id}/status");
documented_endpoint!(configs, get, "/system/config");
documented_endpoint!(create_config, post, "/system/config");
documented_endpoint!(config, get, "/system/config/{id}");
documented_endpoint!(update_config, put, "/system/config/{id}");
documented_endpoint!(delete_config, delete, "/system/config/{id}");
documented_endpoint!(config_status, put, "/system/config/{id}/status");
documented_endpoint!(dict_types, get, "/system/dict/type");
documented_endpoint!(create_dict_type, post, "/system/dict/type");
documented_endpoint!(update_dict_type, put, "/system/dict/type/{id}");
documented_endpoint!(delete_dict_type, delete, "/system/dict/type/{id}");
documented_endpoint!(dict_items, get, "/system/dict/type/{id}/item");
documented_endpoint!(create_dict_item, post, "/system/dict/item");
documented_endpoint!(update_dict_item, put, "/system/dict/item/{id}");
documented_endpoint!(delete_dict_item, delete, "/system/dict/item/{id}");
documented_endpoint!(dict_item_status, put, "/system/dict/item/{id}/status");
documented_endpoint!(dict_refresh, post, "/system/dict/cache/refresh");
documented_endpoint!(online_users, get, "/system/onlineUser");
documented_endpoint!(online_user, get, "/system/onlineUser/{tokenId}");
documented_endpoint!(online_user_kickout, post, "/system/onlineUser/{tokenId}/kickout");
documented_endpoint!(login_logs, get, "/system/loginLog");
documented_endpoint!(login_log, get, "/system/loginLog/{id}");
documented_endpoint!(operation_logs, get, "/system/operationLog");
documented_endpoint!(operation_log, get, "/system/operationLog/{id}");
documented_endpoint!(jobs, get, "/system/job");
documented_endpoint!(create_job, post, "/system/job");
documented_endpoint!(job_scripts, get, "/system/job/scripts");
documented_endpoint!(job, get, "/system/job/{id}");
documented_endpoint!(update_job, put, "/system/job/{id}");
documented_endpoint!(delete_job, delete, "/system/job/{id}");
documented_endpoint!(job_status, put, "/system/job/{id}/status");
documented_endpoint!(job_run, post, "/system/job/{id}/run");
documented_endpoint!(job_run_logs, get, "/system/job/{id}/runLog");
documented_endpoint!(all_job_run_logs, get, "/system/jobRunLog");
documented_endpoint!(files, post, "/system/files");
documented_endpoint!(print_templates, get, "/system/print-template");
documented_endpoint!(create_print_template, post, "/system/print-template");
documented_endpoint!(print_template, get, "/system/print-template/{id}");
documented_endpoint!(update_print_template, put, "/system/print-template/{id}");
documented_endpoint!(delete_print_template, delete, "/system/print-template/{id}");
documented_endpoint!(copy_print_template, post, "/system/print-template/{id}/copy");
documented_endpoint!(print_template_status, put, "/system/print-template/{id}/status");

#[derive(OpenApi)]
#[openapi(
    info(title = "Drip Admin Rust API", version = "0.1.0"),
    components(schemas(
        ApiResponseDocument, ApiResponse<AuthLoginVo>, ApiResponse<AuthMeVo>,
        I64String, PageQuery, PageResult<AuthLoginVo>, LoginRequest, PasswordRequest,
        ProfileUpdateRequest, AuthLoginVo, AuthMeVo, MenuTreeVo,
        ConfigQuery, ConfigSaveRequest, DeptSaveRequest, DictTypeSaveRequest,
        DictItemSaveRequest, JobSaveRequest, LogQuery, MenuSaveRequest,
        PrintTemplateSaveRequest, PrintTemplateCopyRequest, RoleQuery, RoleSaveRequest,
        RolePermissionAssignRequest, StatusUpdateRequest, UserSaveRequest, UserQuery,
        UserRoleAssignRequest, PasswordResetRequest,
        SysConfig, SysDbBackup, SysDept, SysDictType, SysDictItem, SysJob, SysJobRunLog,
        SysLoginLog, SysMenu, SysOperationLog, SysPrintTemplate, SysRole, SysRoleMenu,
        SysUser, SysUserRole, LoginLog, PrintTemplate,
        DeptSummaryVo, DeptTreeVo, FileUploadVo, HealthVo, OnlineUserVo, OperationLogVo,
        RolePermissionVo, RoleSummaryVo, UserListVo,
        PageResult<UserListVo>, PageResult<LoginLog>, PageResult<OperationLogVo>,
        PageResult<SysJob>, PageResult<SysJobRunLog>, PageResult<SysPrintTemplate>,
        PageResult<PrintTemplate>
    )),
    paths(
        public_config, login, logout, me, password, profile, users, create_user, user,
        update_user, delete_user, user_status, user_unlock, user_role, user_reset_password,
        roles, create_role, role_option, role, update_role, delete_role, role_users,
        role_permission, assign_role_permission, role_status, menus, create_menu, update_menu,
        delete_menu, menu_status, depts, create_dept, dept, update_dept, delete_dept,
        dept_status, configs, create_config, config, update_config, delete_config, config_status,
        dict_types, create_dict_type, update_dict_type, delete_dict_type, dict_items,
        create_dict_item, update_dict_item, delete_dict_item, dict_item_status, dict_refresh,
        online_users, online_user, online_user_kickout, login_logs, login_log, operation_logs,
        operation_log, jobs, create_job, job_scripts, job, update_job, delete_job, job_status,
        job_run, job_run_logs, all_job_run_logs, files, print_templates, create_print_template,
        print_template, update_print_template, delete_print_template, copy_print_template,
        print_template_status
    )
)]
pub struct ApiDoc;

pub fn openapi() -> utoipa::openapi::OpenApi {
    ApiDoc::openapi()
}

#[cfg(test)]
mod tests {
    use super::openapi;

    #[test]
    fn generated_document_contains_core_routes_and_response_schema() {
        let document = serde_json::to_value(openapi()).unwrap();
        assert!(document["paths"].as_object().unwrap().len() >= 50);
        assert!(document["paths"]["/system/user"]["get"].is_object());
        assert!(document["paths"]["/system/print-template"]["post"].is_object());
        assert!(document["components"]["schemas"]["ApiResponseDocument"].is_object());
        assert!(document["components"]["schemas"]["UserSaveRequest"].is_object());
        assert!(document["components"]["schemas"]["LoginLog"].is_object());
        assert!(document["components"]["schemas"]["OperationLogVo"].is_object());
        assert!(document["components"]["schemas"]["SysJobRunLog"].is_object());
    }
}
