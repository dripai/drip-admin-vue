insert into sys_dept (id, parent_id, dept_name, dept_code, sort, status)
values (1, 0, '总部', 'HQ', 1, 1);

insert into sys_user (id, username, password_hash, password_salt, real_name, phone, email, status, dept_id, remark)
values (1, 'admin', '0e47ea190b930cfc52b06737a30930f9a3626c3f426edc679975e8112e996b4c', 'drip', '超级管理员', '13800000000', 'admin@example.com', 1, 1, '初始化管理员');

insert into sys_role (id, role_name, role_code, builtin, status, remark)
values (1, '超级管理员', 'SUPER_ADMIN', 1, 1, '系统内置角色'),
       (2, '普通管理员', 'ADMIN', 0, 1, '默认管理员角色');

insert into sys_user_role (user_id, role_id) values (1, 1);

insert into sys_menu (id, parent_id, name, type, path, component, permission_code, icon, sort, visible, status) values
(1, 0, '系统管理', 'DIRECTORY', '/system', null, 'system', 'settings', 1, 1, 1),
(2, 1, '用户管理', 'MENU', '/system/users', 'system/user/index', 'system:user:list', 'user', 10, 1, 1),
(3, 2, '用户详情', 'BUTTON', null, null, 'system:user:detail', null, 11, 0, 1),
(4, 2, '新增用户', 'BUTTON', null, null, 'system:user:create', null, 12, 0, 1),
(5, 2, '编辑用户', 'BUTTON', null, null, 'system:user:update', null, 13, 0, 1),
(6, 2, '删除用户', 'BUTTON', null, null, 'system:user:delete', null, 14, 0, 1),
(7, 2, '变更用户状态', 'BUTTON', null, null, 'system:user:disable', null, 15, 0, 1),
(8, 2, '重置密码', 'BUTTON', null, null, 'system:user:reset-password', null, 16, 0, 1),
(9, 2, '分配角色', 'BUTTON', null, null, 'system:user:assign-role', null, 17, 0, 1),
(10, 1, '角色管理', 'MENU', '/system/roles', 'system/role/index', 'system:role:list', 'shield', 20, 1, 1),
(11, 10, '新增角色', 'BUTTON', null, null, 'system:role:create', null, 21, 0, 1),
(12, 10, '编辑角色', 'BUTTON', null, null, 'system:role:update', null, 22, 0, 1),
(13, 10, '删除角色', 'BUTTON', null, null, 'system:role:delete', null, 23, 0, 1),
(14, 10, '角色授权', 'BUTTON', null, null, 'system:role:permission', null, 24, 0, 1),
(15, 1, '菜单管理', 'MENU', '/system/menus', 'system/menu/index', 'system:menu:list', 'menu', 30, 1, 1),
(16, 15, '维护菜单', 'BUTTON', null, null, 'system:menu:write', null, 31, 0, 1),
(17, 1, '部门管理', 'MENU', '/system/depts', 'system/dept/index', 'system:dept:list', 'building', 40, 1, 1),
(18, 1, '字典管理', 'MENU', '/system/dicts', 'system/dict/index', 'system:dict:list', 'book', 50, 1, 1),
(19, 1, '系统配置', 'MENU', '/system/configs', 'system/config/index', 'system:config:list', 'sliders', 60, 1, 1),
(20, 1, '登录日志', 'MENU', '/system/login-logs', 'system/log/login', 'system:login-log:list', 'log-in', 70, 1, 1),
(21, 1, '操作日志', 'MENU', '/system/operation-logs', 'system/log/operation', 'system:operation-log:list', 'file-clock', 80, 1, 1),
(22, 1, '在线用户', 'MENU', '/system/online-users', 'system/online/index', 'system:online:list', 'monitor', 90, 1, 1),
(23, 1, '定时任务', 'MENU', '/system/jobs', 'system/job/index', 'system:job:list', 'clock', 100, 1, 1),
(24, 1, '数据库备份', 'MENU', '/system/database/backups', 'system/database/index', 'system:database:backup:list', 'database', 110, 1, 1),
(25, 24, '创建备份', 'BUTTON', null, null, 'system:database:backup:create', null, 111, 0, 1),
(26, 24, '下载备份', 'BUTTON', null, null, 'system:database:backup:download', null, 112, 0, 1),
(27, 24, '恢复备份', 'BUTTON', null, null, 'system:database:backup:restore', null, 113, 0, 1);

insert into sys_role_menu (role_id, menu_id)
select 1, id from sys_menu;

insert into sys_dict_type (id, dict_name, dict_code, status, remark)
values (1, '状态', 'common_status', 1, '通用启停状态');

insert into sys_dict_item (dict_type_id, label, value, color, sort, status)
values (1, '启用', '1', 'green', 1, 1),
       (1, '禁用', '0', 'red', 2, 1);

insert into sys_config (config_name, config_key, config_value, group_code, is_sensitive, builtin, status, remark)
values ('系统名称', 'system.name', 'Drip Admin', 'system', 0, 1, 1, '后台系统名称'),
       ('会话最大时长', 'session.maxDuration', '28800', 'security', 0, 1, 1, '秒'),
       ('数据库备份目录', 'database.backupDir', './backups', 'database', 1, 1, 1, '本地备份目录');
