-- Baseline generated from current drip-manager database.
-- Replaces historical migrations for fresh database initialization.

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for `sys_dept`
-- ----------------------------
CREATE TABLE `sys_dept` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `parent_id` bigint NOT NULL DEFAULT '0',
  `dept_name` varchar(64) NOT NULL,
  `dept_code` varchar(64) NOT NULL,
  `leader_user_id` bigint DEFAULT NULL,
  `sort` int NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '1',
  `deleted` tinyint NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_dept_code` (`dept_code`),
  KEY `idx_sys_dept_parent` (`parent_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for `sys_user`
-- ----------------------------
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(64) NOT NULL,
  `password_hash` varchar(128) NOT NULL,
  `password_salt` varchar(64) NOT NULL,
  `real_name` varchar(64) NOT NULL,
  `phone` varchar(32) DEFAULT NULL,
  `email` varchar(128) DEFAULT NULL,
  `avatar` varchar(255) DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT '1',
  `dept_id` bigint DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  `last_login_at` datetime DEFAULT NULL,
  `deleted` tinyint NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_user_username` (`username`),
  KEY `idx_sys_user_dept` (`dept_id`),
  KEY `idx_sys_user_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for `sys_role`
-- ----------------------------
CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_name` varchar(64) NOT NULL,
  `role_code` varchar(64) NOT NULL,
  `builtin` tinyint NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '1',
  `remark` varchar(255) DEFAULT NULL,
  `deleted` tinyint NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_role_code` (`role_code`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for `sys_user_role`
-- ----------------------------
CREATE TABLE `sys_user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_user_role` (`user_id`,`role_id`),
  KEY `idx_sys_user_role_role` (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for `sys_menu`
-- ----------------------------
CREATE TABLE `sys_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `parent_id` bigint NOT NULL DEFAULT '0',
  `name` varchar(64) NOT NULL,
  `type` varchar(16) NOT NULL,
  `path` varchar(255) DEFAULT NULL,
  `component` varchar(255) DEFAULT NULL,
  `permission_code` varchar(128) DEFAULT NULL,
  `icon` varchar(64) DEFAULT NULL,
  `sort` int NOT NULL DEFAULT '0',
  `visible` tinyint NOT NULL DEFAULT '1',
  `status` tinyint NOT NULL DEFAULT '1',
  `deleted` tinyint NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_menu_permission` (`permission_code`),
  KEY `idx_sys_menu_parent` (`parent_id`),
  KEY `idx_sys_menu_sort` (`sort`)
) ENGINE=InnoDB AUTO_INCREMENT=48 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for `sys_role_menu`
-- ----------------------------
CREATE TABLE `sys_role_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_id` bigint NOT NULL,
  `menu_id` bigint NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_role_menu` (`role_id`,`menu_id`),
  KEY `idx_sys_role_menu_menu` (`menu_id`)
) ENGINE=InnoDB AUTO_INCREMENT=52 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for `sys_dict_type`
-- ----------------------------
CREATE TABLE `sys_dict_type` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dict_name` varchar(64) NOT NULL,
  `dict_code` varchar(64) NOT NULL,
  `status` tinyint NOT NULL DEFAULT '1',
  `remark` varchar(255) DEFAULT NULL,
  `deleted` tinyint NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_dict_type_code` (`dict_code`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for `sys_dict_item`
-- ----------------------------
CREATE TABLE `sys_dict_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dict_type_id` bigint NOT NULL,
  `label` varchar(64) NOT NULL,
  `value` varchar(64) NOT NULL,
  `color` varchar(32) DEFAULT NULL,
  `sort` int NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '1',
  `deleted` tinyint NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_dict_item_value` (`dict_type_id`,`value`),
  KEY `idx_sys_dict_item_type` (`dict_type_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for `sys_config`
-- ----------------------------
CREATE TABLE `sys_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `config_name` varchar(64) NOT NULL,
  `config_key` varchar(128) NOT NULL,
  `config_value` varchar(1024) NOT NULL,
  `value_type` varchar(32) NOT NULL DEFAULT 'string',
  `builtin` tinyint NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '1',
  `remark` varchar(255) DEFAULT NULL,
  `deleted` tinyint NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_config_key` (`config_key`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for `sys_login_log`
-- ----------------------------
CREATE TABLE `sys_login_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL,
  `username` varchar(64) NOT NULL,
  `real_name` varchar(64) DEFAULT NULL,
  `login_type` varchar(32) NOT NULL,
  `status` varchar(16) NOT NULL,
  `failure_reason` varchar(255) DEFAULT NULL,
  `ip` varchar(64) DEFAULT NULL,
  `user_agent` varchar(512) DEFAULT NULL,
  `device_type` varchar(64) DEFAULT NULL,
  `login_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_sys_login_log_username` (`username`),
  KEY `idx_sys_login_log_status` (`status`),
  KEY `idx_sys_login_log_login_at` (`login_at`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for `sys_operation_log`
-- ----------------------------
CREATE TABLE `sys_operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `operator_id` bigint DEFAULT NULL,
  `operator_name` varchar(64) DEFAULT NULL,
  `module` varchar(64) NOT NULL,
  `action` varchar(64) NOT NULL,
  `method` varchar(16) NOT NULL,
  `path` varchar(255) NOT NULL,
  `request_params` text,
  `response_status` varchar(16) NOT NULL,
  `error_message` text,
  `cost_ms` bigint NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_sys_operation_log_operator` (`operator_id`),
  KEY `idx_sys_operation_log_module` (`module`),
  KEY `idx_sys_operation_log_created_at` (`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for `sys_job`
-- ----------------------------
CREATE TABLE `sys_job` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `job_name` varchar(64) NOT NULL,
  `job_code` varchar(64) NOT NULL,
  `cron_expression` varchar(64) NOT NULL,
  `bean_name` varchar(128) NOT NULL,
  `method_name` varchar(128) NOT NULL,
  `params` varchar(1024) DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT '1',
  `remark` varchar(255) DEFAULT NULL,
  `deleted` tinyint NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_job_code` (`job_code`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for `sys_job_run_log`
-- ----------------------------
CREATE TABLE `sys_job_run_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `job_id` bigint NOT NULL,
  `job_name` varchar(64) NOT NULL,
  `status` varchar(16) NOT NULL,
  `started_at` datetime NOT NULL,
  `finished_at` datetime DEFAULT NULL,
  `cost_ms` bigint NOT NULL DEFAULT '0',
  `error_message` varchar(512) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_sys_job_run_log_job` (`job_id`),
  KEY `idx_sys_job_run_log_started` (`started_at`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for `sys_db_backup`
-- ----------------------------
CREATE TABLE `sys_db_backup` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `backup_name` varchar(128) NOT NULL,
  `file_path` varchar(512) NOT NULL,
  `file_size` bigint NOT NULL DEFAULT '0',
  `status` varchar(16) NOT NULL,
  `created_by` bigint NOT NULL,
  `remark` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_sys_db_backup_created_at` (`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Data for `sys_dept`
-- ----------------------------
INSERT INTO `sys_dept` (`id`, `parent_id`, `dept_name`, `dept_code`, `leader_user_id`, `sort`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (1, 0, '总部', 'HQ', NULL, 1, 1, 0, '2026-06-26 12:27:47', '2026-06-26 12:27:47');

-- ----------------------------
-- Data for `sys_user`
-- ----------------------------
INSERT INTO `sys_user` (`id`, `username`, `password_hash`, `password_salt`, `real_name`, `phone`, `email`, `avatar`, `status`, `dept_id`, `remark`, `last_login_at`, `deleted`, `created_at`, `updated_at`) VALUES (1, 'admin', 'e0b53c1673e188861664190da8ed43ebc2b4e540784cb167c55c919e8ab87a9f', 'salt1', '超级管理员', '13800000000', 'admin@example.com', NULL, 1, 1, '初始化管理员', '2026-06-26 23:25:41', 0, '2026-06-26 12:27:47', '2026-06-26 23:25:41');
INSERT INTO `sys_user` (`id`, `username`, `password_hash`, `password_salt`, `real_name`, `phone`, `email`, `avatar`, `status`, `dept_id`, `remark`, `last_login_at`, `deleted`, `created_at`, `updated_at`) VALUES (2, 'user1782448243', 'efeaa5ffbb24fa0d88b1723ddd13faf5ae2f136c7b32e17144acde8bc3042814', 'salt22869432346900', 'Normal User', NULL, NULL, NULL, 1, 1, NULL, '2026-06-26 12:30:43', 0, '2026-06-26 12:30:43', '2026-06-26 12:30:43');
INSERT INTO `sys_user` (`id`, `username`, `password_hash`, `password_salt`, `real_name`, `phone`, `email`, `avatar`, `status`, `dept_id`, `remark`, `last_login_at`, `deleted`, `created_at`, `updated_at`) VALUES (3, 'noperm1782471282455', '1444d89c2c16e91ef9a686c9be73ec3f70f7e5f9c241c77c89296f21a866c221', 'salt45908387494300', 'No Permission', '13900000001', 'noperm1782471282455@example.com', NULL, 1, 1, NULL, '2026-06-26 18:54:43', 0, '2026-06-26 18:54:42', '2026-06-26 18:54:42');

-- ----------------------------
-- Data for `sys_role`
-- ----------------------------
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `builtin`, `status`, `remark`, `deleted`, `created_at`, `updated_at`) VALUES (1, '超级管理员', 'SUPER_ADMIN', 1, 1, '系统内置角色', 0, '2026-06-26 12:27:47', '2026-06-26 12:27:47');
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `builtin`, `status`, `remark`, `deleted`, `created_at`, `updated_at`) VALUES (2, '普通管理员', 'ADMIN', 0, 1, '默认管理员角色', 0, '2026-06-26 12:27:47', '2026-06-26 12:27:47');
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `builtin`, `status`, `remark`, `deleted`, `created_at`, `updated_at`) VALUES (3, '????', 'TEST_1782471266417', 0, 1, 'runtime check', 0, '2026-06-26 18:54:26', '2026-06-26 18:54:26');

-- ----------------------------
-- Data for `sys_user_role`
-- ----------------------------
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`, `created_at`) VALUES (1, 1, 1, '2026-06-26 12:27:47');
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`, `created_at`) VALUES (2, 2, 2, '2026-06-26 12:30:43');

-- ----------------------------
-- Data for `sys_menu`
-- ----------------------------
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (1, 0, '系统管理', 'DIRECTORY', '/system', NULL, 'system', 'settings', 1, 1, 1, 0, '2026-06-26 12:27:47', '2026-06-26 12:27:47');
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (47, 1, '个人中心', 'MENU', '/system/profile', 'system/profile/index', NULL, 'user', 5, 1, 1, 0, '2026-06-26 12:27:47', '2026-06-26 12:27:47');
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (2, 1, '用户管理', 'MENU', '/system/user', 'system/user/index', 'system:user:list', 'user', 10, 1, 1, 0, '2026-06-26 12:27:47', '2026-06-26 23:24:45');
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (3, 2, '用户详情', 'BUTTON', NULL, NULL, 'system:user:detail', NULL, 11, 0, 1, 0, '2026-06-26 12:27:47', '2026-06-26 12:27:47');
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (4, 2, '新增用户', 'BUTTON', NULL, NULL, 'system:user:create', NULL, 12, 0, 1, 0, '2026-06-26 12:27:47', '2026-06-26 12:27:47');
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (5, 2, '编辑用户', 'BUTTON', NULL, NULL, 'system:user:update', NULL, 13, 0, 1, 0, '2026-06-26 12:27:47', '2026-06-26 12:27:47');
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (6, 2, '删除用户', 'BUTTON', NULL, NULL, 'system:user:delete', NULL, 14, 0, 1, 0, '2026-06-26 12:27:47', '2026-06-26 12:27:47');
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (7, 2, '变更用户状态', 'BUTTON', NULL, NULL, 'system:user:disable', NULL, 15, 0, 1, 0, '2026-06-26 12:27:47', '2026-06-26 12:27:47');
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (8, 2, '重置密码', 'BUTTON', NULL, NULL, 'system:user:resetPassword', NULL, 16, 0, 1, 0, '2026-06-26 12:27:47', '2026-06-26 18:50:44');
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (9, 2, '分配角色', 'BUTTON', NULL, NULL, 'system:user:assignRole', NULL, 17, 0, 1, 0, '2026-06-26 12:27:47', '2026-06-26 18:50:44');
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (48, 2, '解除登录锁定', 'BUTTON', NULL, NULL, 'system:user:unlock', NULL, 18, 0, 1, 0, '2026-06-26 17:57:08', '2026-06-26 17:57:08');
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (10, 1, '角色管理', 'MENU', '/system/role', 'system/role/index', 'system:role:list', 'shield', 20, 1, 1, 0, '2026-06-26 12:27:47', '2026-06-26 23:24:45');
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (11, 10, '新增角色', 'BUTTON', NULL, NULL, 'system:role:create', NULL, 21, 0, 1, 0, '2026-06-26 12:27:47', '2026-06-26 12:27:47');
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (12, 10, '编辑角色', 'BUTTON', NULL, NULL, 'system:role:update', NULL, 22, 0, 1, 0, '2026-06-26 12:27:47', '2026-06-26 12:27:47');
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (13, 10, '删除角色', 'BUTTON', NULL, NULL, 'system:role:delete', NULL, 23, 0, 1, 0, '2026-06-26 12:27:47', '2026-06-26 12:27:47');
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (14, 10, '角色授权', 'BUTTON', NULL, NULL, 'system:role:permission', NULL, 24, 0, 1, 0, '2026-06-26 12:27:47', '2026-06-26 12:27:47');
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (15, 1, '菜单管理', 'MENU', '/system/menu', 'system/menu/index', 'system:menu:list', 'menu', 30, 1, 1, 0, '2026-06-26 12:27:47', '2026-06-26 23:24:45');
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (16, 15, '新增菜单', 'BUTTON', NULL, NULL, 'system:menu:create', NULL, 31, 0, 1, 0, '2026-06-26 12:27:47', '2026-06-26 17:57:08');
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (17, 1, '部门管理', 'MENU', '/system/dept', 'system/dept/index', 'system:dept:list', 'building', 40, 1, 1, 0, '2026-06-26 12:27:47', '2026-06-26 23:24:45');
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (18, 1, '字典管理', 'MENU', '/system/dict', 'system/dict/index', 'system:dict:list', 'book', 50, 1, 1, 0, '2026-06-26 12:27:47', '2026-06-26 23:24:45');
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (19, 1, '系统配置', 'MENU', '/system/config', 'system/config/index', 'system:config:list', 'sliders', 60, 1, 1, 0, '2026-06-26 12:27:47', '2026-06-26 23:24:45');
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (20, 1, '登录日志', 'MENU', '/system/loginLog', 'system/loginLog/index', 'system:loginLog:list', 'log-in', 70, 1, 1, 0, '2026-06-26 12:27:47', '2026-06-26 23:24:45');
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (21, 1, '操作日志', 'MENU', '/system/operationLog', 'system/operationLog/index', 'system:operationLog:list', 'file-clock', 80, 1, 1, 0, '2026-06-26 12:27:47', '2026-06-26 23:24:45');
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (22, 1, '在线用户', 'MENU', '/system/onlineUser', 'system/onlineUser/index', 'system:online:list', 'monitor', 90, 1, 1, 0, '2026-06-26 12:27:47', '2026-06-26 23:24:45');
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (23, 1, '定时任务', 'MENU', '/system/job', 'system/job/index', 'system:job:list', 'clock', 100, 1, 1, 0, '2026-06-26 12:27:47', '2026-06-26 23:24:45');
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (24, 1, '数据库备份', 'MENU', '/system/databaseBackup', 'system/database/index', 'system:database:backup:list', 'database', 110, 1, 1, 0, '2026-06-26 12:27:47', '2026-06-26 23:24:45');
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (25, 24, '创建备份', 'BUTTON', NULL, NULL, 'system:database:backup:create', NULL, 111, 0, 1, 0, '2026-06-26 12:27:47', '2026-06-26 12:27:47');
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (26, 24, '下载备份', 'BUTTON', NULL, NULL, 'system:database:backup:download', NULL, 112, 0, 1, 0, '2026-06-26 12:27:47', '2026-06-26 12:27:47');
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (27, 24, '恢复备份', 'BUTTON', NULL, NULL, 'system:database:backup:restore', NULL, 113, 0, 1, 0, '2026-06-26 12:27:47', '2026-06-26 12:27:47');
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (28, 1, '文件上传', 'BUTTON', NULL, NULL, 'system:file:upload', NULL, 120, 0, 1, 0, '2026-06-26 17:57:08', '2026-06-26 17:57:08');
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (29, 17, '新增部门', 'BUTTON', NULL, NULL, 'system:dept:create', NULL, 41, 0, 1, 0, '2026-06-26 17:57:08', '2026-06-26 17:57:08');
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (30, 17, '编辑部门', 'BUTTON', NULL, NULL, 'system:dept:update', NULL, 42, 0, 1, 0, '2026-06-26 17:57:08', '2026-06-26 17:57:08');
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (31, 17, '删除部门', 'BUTTON', NULL, NULL, 'system:dept:delete', NULL, 43, 0, 1, 0, '2026-06-26 17:57:08', '2026-06-26 17:57:08');
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (32, 18, '新增字典', 'BUTTON', NULL, NULL, 'system:dict:create', NULL, 51, 0, 1, 0, '2026-06-26 17:57:08', '2026-06-26 17:57:08');
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (33, 18, '编辑字典', 'BUTTON', NULL, NULL, 'system:dict:update', NULL, 52, 0, 1, 0, '2026-06-26 17:57:08', '2026-06-26 17:57:08');
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (34, 18, '删除字典', 'BUTTON', NULL, NULL, 'system:dict:delete', NULL, 53, 0, 1, 0, '2026-06-26 17:57:08', '2026-06-26 17:57:08');
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (35, 19, '新增配置', 'BUTTON', NULL, NULL, 'system:config:create', NULL, 61, 0, 1, 0, '2026-06-26 17:57:08', '2026-06-26 17:57:08');
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (36, 19, '编辑配置', 'BUTTON', NULL, NULL, 'system:config:update', NULL, 62, 0, 1, 0, '2026-06-26 17:57:08', '2026-06-26 17:57:08');
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (37, 19, '删除配置', 'BUTTON', NULL, NULL, 'system:config:delete', NULL, 63, 0, 1, 0, '2026-06-26 17:57:08', '2026-06-26 17:57:08');
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (38, 22, '强制下线', 'BUTTON', NULL, NULL, 'system:online:kickout', NULL, 91, 0, 1, 0, '2026-06-26 17:57:08', '2026-06-26 17:57:08');
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (39, 23, '新增任务', 'BUTTON', NULL, NULL, 'system:job:create', NULL, 101, 0, 1, 0, '2026-06-26 17:57:08', '2026-06-26 17:57:08');
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (40, 23, '编辑任务', 'BUTTON', NULL, NULL, 'system:job:update', NULL, 102, 0, 1, 0, '2026-06-26 17:57:08', '2026-06-26 17:57:08');
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (41, 23, '删除任务', 'BUTTON', NULL, NULL, 'system:job:delete', NULL, 103, 0, 1, 0, '2026-06-26 17:57:08', '2026-06-26 17:57:08');
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (42, 23, '执行任务', 'BUTTON', NULL, NULL, 'system:job:run', NULL, 104, 0, 1, 0, '2026-06-26 17:57:08', '2026-06-26 17:57:08');
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (43, 24, '删除备份', 'BUTTON', NULL, NULL, 'system:database:backup:delete', NULL, 114, 0, 1, 0, '2026-06-26 17:57:08', '2026-06-26 17:57:08');
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (44, 15, '编辑菜单', 'BUTTON', NULL, NULL, 'system:menu:update', NULL, 32, 0, 1, 0, '2026-06-26 17:57:08', '2026-06-26 17:57:08');
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (45, 15, '删除菜单', 'BUTTON', NULL, NULL, 'system:menu:delete', NULL, 33, 0, 1, 0, '2026-06-26 17:57:08', '2026-06-26 17:57:08');
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (46, 15, '变更菜单状态', 'BUTTON', NULL, NULL, 'system:menu:status', NULL, 34, 0, 1, 0, '2026-06-26 17:57:08', '2026-06-26 17:57:08');

-- ----------------------------
-- Data for `sys_role_menu`
-- ----------------------------
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (1, 1, 1, '2026-06-26 12:27:47');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (2, 1, 2, '2026-06-26 12:27:47');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (3, 1, 3, '2026-06-26 12:27:47');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (4, 1, 4, '2026-06-26 12:27:47');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (5, 1, 5, '2026-06-26 12:27:47');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (6, 1, 6, '2026-06-26 12:27:47');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (7, 1, 7, '2026-06-26 12:27:47');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (8, 1, 8, '2026-06-26 12:27:47');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (9, 1, 9, '2026-06-26 12:27:47');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (10, 1, 10, '2026-06-26 12:27:47');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (11, 1, 11, '2026-06-26 12:27:47');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (12, 1, 12, '2026-06-26 12:27:47');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (13, 1, 13, '2026-06-26 12:27:47');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (14, 1, 14, '2026-06-26 12:27:47');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (15, 1, 15, '2026-06-26 12:27:47');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (16, 1, 16, '2026-06-26 12:27:47');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (17, 1, 17, '2026-06-26 12:27:47');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (18, 1, 18, '2026-06-26 12:27:47');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (19, 1, 19, '2026-06-26 12:27:47');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (20, 1, 20, '2026-06-26 12:27:47');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (21, 1, 21, '2026-06-26 12:27:47');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (22, 1, 22, '2026-06-26 12:27:47');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (23, 1, 23, '2026-06-26 12:27:47');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (24, 1, 24, '2026-06-26 12:27:47');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (25, 1, 25, '2026-06-26 12:27:47');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (26, 1, 26, '2026-06-26 12:27:47');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (27, 1, 27, '2026-06-26 12:27:47');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (32, 1, 28, '2026-06-26 17:57:08');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (33, 1, 35, '2026-06-26 17:57:08');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (34, 1, 37, '2026-06-26 17:57:08');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (35, 1, 36, '2026-06-26 17:57:08');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (36, 1, 43, '2026-06-26 17:57:08');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (37, 1, 29, '2026-06-26 17:57:08');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (38, 1, 31, '2026-06-26 17:57:08');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (39, 1, 30, '2026-06-26 17:57:08');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (40, 1, 32, '2026-06-26 17:57:08');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (41, 1, 34, '2026-06-26 17:57:08');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (42, 1, 33, '2026-06-26 17:57:08');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (43, 1, 39, '2026-06-26 17:57:08');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (44, 1, 41, '2026-06-26 17:57:08');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (45, 1, 42, '2026-06-26 17:57:08');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (46, 1, 40, '2026-06-26 17:57:08');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (47, 1, 38, '2026-06-26 17:57:08');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (48, 1, 45, '2026-06-26 17:57:08');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (49, 1, 46, '2026-06-26 17:57:08');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (50, 1, 44, '2026-06-26 17:57:08');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (51, 1, 47, '2026-06-26 17:57:08');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`) VALUES (52, 1, 48, '2026-06-26 17:57:08');

-- ----------------------------
-- Data for `sys_dict_type`
-- ----------------------------
INSERT INTO `sys_dict_type` (`id`, `dict_name`, `dict_code`, `status`, `remark`, `deleted`, `created_at`, `updated_at`) VALUES (1, '状态', 'common_status', 1, '通用启停状态', 0, '2026-06-26 12:27:47', '2026-06-26 12:27:47');

-- ----------------------------
-- Data for `sys_dict_item`
-- ----------------------------
INSERT INTO `sys_dict_item` (`id`, `dict_type_id`, `label`, `value`, `color`, `sort`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (1, 1, '启用', '1', 'green', 1, 1, 0, '2026-06-26 12:27:47', '2026-06-26 12:27:47');
INSERT INTO `sys_dict_item` (`id`, `dict_type_id`, `label`, `value`, `color`, `sort`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (2, 1, '禁用', '0', 'red', 2, 1, 0, '2026-06-26 12:27:47', '2026-06-26 12:27:47');

-- ----------------------------
-- Data for `sys_config`
-- ----------------------------
INSERT INTO `sys_config` (`id`, `config_name`, `config_key`, `config_value`, `value_type`, `builtin`, `status`, `remark`, `deleted`, `created_at`, `updated_at`) VALUES (1, '系统名称', 'system.name', 'Drip Admin', 'string', 1, 1, '后台系统名称', 0, '2026-06-26 12:27:47', '2026-06-26 12:27:47');
INSERT INTO `sys_config` (`id`, `config_name`, `config_key`, `config_value`, `value_type`, `builtin`, `status`, `remark`, `deleted`, `created_at`, `updated_at`) VALUES (2, '上传文件大小限制', 'upload.maxSizeBytes', '10485760', 'number', 1, 1, '字节', 0, '2026-06-26 12:27:47', '2026-06-26 12:27:47');
INSERT INTO `sys_config` (`id`, `config_name`, `config_key`, `config_value`, `value_type`, `builtin`, `status`, `remark`, `deleted`, `created_at`, `updated_at`) VALUES (3, '上传文件后缀限制', 'upload.allowedExtensions', 'png,jpg,jpeg,pdf', 'string', 1, 1, '逗号分隔的文件后缀', 0, '2026-06-26 12:27:47', '2026-06-26 12:27:47');
INSERT INTO `sys_config` (`id`, `config_name`, `config_key`, `config_value`, `value_type`, `builtin`, `status`, `remark`, `deleted`, `created_at`, `updated_at`) VALUES (4, '登录失败锁定次数', 'login.maxFailures', '5', 'number', 1, 1, '连续失败次数', 0, '2026-06-26 12:27:47', '2026-06-26 12:27:47');
INSERT INTO `sys_config` (`id`, `config_name`, `config_key`, `config_value`, `value_type`, `builtin`, `status`, `remark`, `deleted`, `created_at`, `updated_at`) VALUES (5, '登录失败锁定时长', 'login.lockSeconds', '900', 'number', 1, 1, '秒', 0, '2026-06-26 12:27:47', '2026-06-26 12:27:47');
INSERT INTO `sys_config` (`id`, `config_name`, `config_key`, `config_value`, `value_type`, `builtin`, `status`, `remark`, `deleted`, `created_at`, `updated_at`) VALUES (6, '系统Logo', 'system.logo', '', 'string', 1, 1, '图片 URL，为空时使用系统名称缩写', 0, '2026-06-26 12:27:47', '2026-06-26 12:27:47');
INSERT INTO `sys_config` (`id`, `config_name`, `config_key`, `config_value`, `value_type`, `builtin`, `status`, `remark`, `deleted`, `created_at`, `updated_at`) VALUES (7, '启用水印', 'system.watermark.enabled', 'false', 'boolean', 1, 1, '是否启用系统水印', 0, '2026-06-26 12:27:47', '2026-06-26 12:27:47');

-- ----------------------------
-- Data for `sys_login_log`
-- ----------------------------
INSERT INTO `sys_login_log` (`id`, `user_id`, `username`, `real_name`, `login_type`, `status`, `failure_reason`, `ip`, `user_agent`, `device_type`, `login_at`) VALUES (1, 1, 'admin', '超级管理员', 'LOGIN', 'SUCCESS', NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.26100.8655', 'web', '2026-06-26 12:28:22');
INSERT INTO `sys_login_log` (`id`, `user_id`, `username`, `real_name`, `login_type`, `status`, `failure_reason`, `ip`, `user_agent`, `device_type`, `login_at`) VALUES (2, 1, 'admin', '超级管理员', 'LOGIN', 'SUCCESS', NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.26100.8655', 'web', '2026-06-26 12:30:43');
INSERT INTO `sys_login_log` (`id`, `user_id`, `username`, `real_name`, `login_type`, `status`, `failure_reason`, `ip`, `user_agent`, `device_type`, `login_at`) VALUES (3, 2, 'user1782448243', 'Normal User', 'LOGIN', 'SUCCESS', NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.26100.8655', 'web', '2026-06-26 12:30:43');
INSERT INTO `sys_login_log` (`id`, `user_id`, `username`, `real_name`, `login_type`, `status`, `failure_reason`, `ip`, `user_agent`, `device_type`, `login_at`) VALUES (4, 1, 'admin', '超级管理员', 'LOGOUT', 'SUCCESS', NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.26100.8655', 'web', '2026-06-26 12:30:43');
INSERT INTO `sys_login_log` (`id`, `user_id`, `username`, `real_name`, `login_type`, `status`, `failure_reason`, `ip`, `user_agent`, `device_type`, `login_at`) VALUES (5, 1, 'admin', '超级管理员', 'LOGIN', 'SUCCESS', NULL, '0:0:0:0:0:0:0:1', 'Python-urllib/3.8', 'web', '2026-06-26 12:30:57');
INSERT INTO `sys_login_log` (`id`, `user_id`, `username`, `real_name`, `login_type`, `status`, `failure_reason`, `ip`, `user_agent`, `device_type`, `login_at`) VALUES (6, 1, 'admin', '超级管理员', 'LOGIN', 'SUCCESS', NULL, '0:0:0:0:0:0:0:1', 'Python-urllib/3.8', 'web', '2026-06-26 12:31:26');
INSERT INTO `sys_login_log` (`id`, `user_id`, `username`, `real_name`, `login_type`, `status`, `failure_reason`, `ip`, `user_agent`, `device_type`, `login_at`) VALUES (8, 1, 'admin', '超级管理员', 'LOGIN', 'SUCCESS', NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.26100.8655', 'web', '2026-06-26 18:54:03');
INSERT INTO `sys_login_log` (`id`, `user_id`, `username`, `real_name`, `login_type`, `status`, `failure_reason`, `ip`, `user_agent`, `device_type`, `login_at`) VALUES (9, 1, 'admin', '超级管理员', 'LOGIN', 'SUCCESS', NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.26100.8655', 'web', '2026-06-26 18:54:26');
INSERT INTO `sys_login_log` (`id`, `user_id`, `username`, `real_name`, `login_type`, `status`, `failure_reason`, `ip`, `user_agent`, `device_type`, `login_at`) VALUES (10, 1, 'admin', '超级管理员', 'LOGIN', 'SUCCESS', NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.26100.8655', 'web', '2026-06-26 18:54:42');
INSERT INTO `sys_login_log` (`id`, `user_id`, `username`, `real_name`, `login_type`, `status`, `failure_reason`, `ip`, `user_agent`, `device_type`, `login_at`) VALUES (11, 3, 'noperm1782471282455', 'No Permission', 'LOGIN', 'SUCCESS', NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.26100.8655', 'web', '2026-06-26 18:54:42');
INSERT INTO `sys_login_log` (`id`, `user_id`, `username`, `real_name`, `login_type`, `status`, `failure_reason`, `ip`, `user_agent`, `device_type`, `login_at`) VALUES (13, 1, 'admin', '超级管理员', 'LOGIN', 'SUCCESS', NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36', 'pc', '2026-06-26 22:52:51');
INSERT INTO `sys_login_log` (`id`, `user_id`, `username`, `real_name`, `login_type`, `status`, `failure_reason`, `ip`, `user_agent`, `device_type`, `login_at`) VALUES (14, 1, 'admin', '超级管理员', 'LOGIN', 'SUCCESS', NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36', 'pc', '2026-06-26 22:53:04');
INSERT INTO `sys_login_log` (`id`, `user_id`, `username`, `real_name`, `login_type`, `status`, `failure_reason`, `ip`, `user_agent`, `device_type`, `login_at`) VALUES (16, 1, 'admin', '超级管理员', 'LOGIN', 'SUCCESS', NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36', 'pc', '2026-06-26 22:57:36');
INSERT INTO `sys_login_log` (`id`, `user_id`, `username`, `real_name`, `login_type`, `status`, `failure_reason`, `ip`, `user_agent`, `device_type`, `login_at`) VALUES (17, 1, 'admin', '超级管理员', 'LOGOUT', 'SUCCESS', NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36', 'pc', '2026-06-26 22:58:12');
INSERT INTO `sys_login_log` (`id`, `user_id`, `username`, `real_name`, `login_type`, `status`, `failure_reason`, `ip`, `user_agent`, `device_type`, `login_at`) VALUES (18, 1, 'admin', '超级管理员', 'LOGIN', 'SUCCESS', NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36', 'pc', '2026-06-26 23:10:53');
INSERT INTO `sys_login_log` (`id`, `user_id`, `username`, `real_name`, `login_type`, `status`, `failure_reason`, `ip`, `user_agent`, `device_type`, `login_at`) VALUES (19, 1, 'admin', '超级管理员', 'LOGIN', 'SUCCESS', NULL, '0:0:0:0:0:0:0:1', 'Python-urllib/3.8', 'WEB', '2026-06-26 23:12:27');
INSERT INTO `sys_login_log` (`id`, `user_id`, `username`, `real_name`, `login_type`, `status`, `failure_reason`, `ip`, `user_agent`, `device_type`, `login_at`) VALUES (20, 1, 'admin', '超级管理员', 'LOGIN', 'SUCCESS', NULL, '0:0:0:0:0:0:0:1', 'Python-urllib/3.8', 'WEB', '2026-06-26 23:12:55');
INSERT INTO `sys_login_log` (`id`, `user_id`, `username`, `real_name`, `login_type`, `status`, `failure_reason`, `ip`, `user_agent`, `device_type`, `login_at`) VALUES (21, 1, 'admin', '超级管理员', 'LOGIN', 'SUCCESS', NULL, '0:0:0:0:0:0:0:1', 'Python-urllib/3.8', 'WEB', '2026-06-26 23:25:04');
INSERT INTO `sys_login_log` (`id`, `user_id`, `username`, `real_name`, `login_type`, `status`, `failure_reason`, `ip`, `user_agent`, `device_type`, `login_at`) VALUES (22, 1, 'admin', '超级管理员', 'LOGIN', 'SUCCESS', NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36', 'pc', '2026-06-26 23:25:41');

-- ----------------------------
-- Data for `sys_operation_log`
-- ----------------------------
INSERT INTO `sys_operation_log` (`id`, `operator_id`, `operator_name`, `module`, `action`, `method`, `path`, `request_params`, `response_status`, `error_message`, `cost_ms`, `created_at`) VALUES (1, 1, '1', '用户管理', '新增用户', 'POST', '/api/system/users', '[{username=user1782448243, status=1, real_name=Normal User, password=******, dept_id=1}]', 'SUCCESS', NULL, 7, '2026-06-26 12:30:43');
INSERT INTO `sys_operation_log` (`id`, `operator_id`, `operator_name`, `module`, `action`, `method`, `path`, `request_params`, `response_status`, `error_message`, `cost_ms`, `created_at`) VALUES (2, 1, '1', '用户管理', '分配角色', 'PUT', '/api/system/users/2/roles', '[2, {roleIds=[2]}]', 'SUCCESS', NULL, 7, '2026-06-26 12:30:43');
INSERT INTO `sys_operation_log` (`id`, `operator_id`, `operator_name`, `module`, `action`, `method`, `path`, `request_params`, `response_status`, `error_message`, `cost_ms`, `created_at`) VALUES (3, 1, '1', '定时任务', '新增任务', 'POST', '/api/system/jobs', '[{job_name=Smoke Job, job_code=smoke_1782448286693237600, cron_expression=0 0 * * *, bean_name=smokeJob, method_name=run, status=1}]', 'SUCCESS', NULL, 3, '2026-06-26 12:31:26');
INSERT INTO `sys_operation_log` (`id`, `operator_id`, `operator_name`, `module`, `action`, `method`, `path`, `request_params`, `response_status`, `error_message`, `cost_ms`, `created_at`) VALUES (4, 1, '1', '定时任务', '手动执行任务', 'POST', '/api/system/jobs/1/run', '[1]', 'SUCCESS', NULL, 7, '2026-06-26 12:31:26');
INSERT INTO `sys_operation_log` (`id`, `operator_id`, `operator_name`, `module`, `action`, `method`, `path`, `request_params`, `response_status`, `error_message`, `cost_ms`, `created_at`) VALUES (5, 1, '1', '数据库备份', '创建备份', 'POST', '/api/system/database/backups', '[{remark=smoke}]', 'SUCCESS', NULL, 6, '2026-06-26 12:31:26');
INSERT INTO `sys_operation_log` (`id`, `operator_id`, `operator_name`, `module`, `action`, `method`, `path`, `request_params`, `response_status`, `error_message`, `cost_ms`, `created_at`) VALUES (6, 1, '1', '数据库备份', '恢复备份', 'POST', '/api/system/database/backups/1/restore', '[1, {confirmed=true}]', 'SUCCESS', NULL, 3, '2026-06-26 12:31:26');
INSERT INTO `sys_operation_log` (`id`, `operator_id`, `operator_name`, `module`, `action`, `method`, `path`, `request_params`, `response_status`, `error_message`, `cost_ms`, `created_at`) VALUES (7, 1, '1', '角色管理', '新增角色', 'POST', '/api/system/roles', '[com.drip.admin.modules.system.dto.RoleSaveRequest@540d5de2]', 'SUCCESS', NULL, 14, '2026-06-26 18:54:26');
INSERT INTO `sys_operation_log` (`id`, `operator_id`, `operator_name`, `module`, `action`, `method`, `path`, `request_params`, `response_status`, `error_message`, `cost_ms`, `created_at`) VALUES (8, 1, '1', '用户管理', '新增用户', 'POST', '/api/system/users', '[com.drip.admin.modules.system.dto.UserSaveRequest@757f2523]', 'SUCCESS', NULL, 11, '2026-06-26 18:54:42');

-- ----------------------------
-- Data for `sys_job`
-- ----------------------------
INSERT INTO `sys_job` (`id`, `job_name`, `job_code`, `cron_expression`, `bean_name`, `method_name`, `params`, `status`, `remark`, `deleted`, `created_at`, `updated_at`) VALUES (1, 'Smoke Job', 'smoke_1782448286693237600', '0 0 * * *', 'smokeJob', 'run', NULL, 1, NULL, 0, '2026-06-26 12:31:26', '2026-06-26 12:31:26');

-- ----------------------------
-- Data for `sys_job_run_log`
-- ----------------------------
INSERT INTO `sys_job_run_log` (`id`, `job_id`, `job_name`, `status`, `started_at`, `finished_at`, `cost_ms`, `error_message`) VALUES (1, 1, 'Smoke Job', 'SUCCESS', '2026-06-26 12:31:27', '2026-06-26 12:31:27', 0, NULL);

-- ----------------------------
-- Data for `sys_db_backup`
-- ----------------------------
INSERT INTO `sys_db_backup` (`id`, `backup_name`, `file_path`, `file_size`, `status`, `created_by`, `remark`, `created_at`) VALUES (1, 'backup-1782448286786.sql', 'backups\\backup-1782448286786.sql', 51, 'SUCCESS', 1, 'smoke', '2026-06-26 12:31:26');

SET FOREIGN_KEY_CHECKS = 1;
