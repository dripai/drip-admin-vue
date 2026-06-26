update sys_dept set dept_name = '总部' where id = 1;

update sys_user set real_name = '超级管理员', remark = '初始化管理员' where id = 1;

update sys_role set role_name = '超级管理员', remark = '系统内置角色' where id = 1;
update sys_role set role_name = '普通管理员', remark = '默认管理员角色' where id = 2;

update sys_menu set name = '系统管理' where id = 1;
update sys_menu set name = '用户管理' where id = 2;
update sys_menu set name = '用户详情' where id = 3;
update sys_menu set name = '新增用户' where id = 4;
update sys_menu set name = '编辑用户' where id = 5;
update sys_menu set name = '删除用户' where id = 6;
update sys_menu set name = '变更用户状态' where id = 7;
update sys_menu set name = '重置密码' where id = 8;
update sys_menu set name = '分配角色' where id = 9;
update sys_menu set name = '角色管理' where id = 10;
update sys_menu set name = '新增角色' where id = 11;
update sys_menu set name = '编辑角色' where id = 12;
update sys_menu set name = '删除角色' where id = 13;
update sys_menu set name = '角色授权' where id = 14;
update sys_menu set name = '菜单管理' where id = 15;
update sys_menu set name = '维护菜单' where id = 16;
update sys_menu set name = '部门管理' where id = 17;
update sys_menu set name = '字典管理' where id = 18;
update sys_menu set name = '系统配置' where id = 19;
update sys_menu set name = '登录日志' where id = 20;
update sys_menu set name = '操作日志' where id = 21;
update sys_menu set name = '在线用户' where id = 22;
update sys_menu set name = '定时任务' where id = 23;
update sys_menu set name = '数据库备份' where id = 24;
update sys_menu set name = '创建备份' where id = 25;
update sys_menu set name = '下载备份' where id = 26;
update sys_menu set name = '恢复备份' where id = 27;

update sys_dict_type set dict_name = '状态', remark = '通用启停状态' where id = 1;
update sys_dict_item set label = '启用' where dict_type_id = 1 and value = '1';
update sys_dict_item set label = '禁用' where dict_type_id = 1 and value = '0';

update sys_config set config_name = '系统名称', config_value = 'Drip Admin', remark = '后台系统名称' where config_key = 'system.name';
update sys_config set config_name = '会话最大时长', remark = '秒' where config_key = 'session.maxDuration';
update sys_config set config_name = '数据库备份目录', remark = '本地备份目录' where config_key = 'database.backupDir';
