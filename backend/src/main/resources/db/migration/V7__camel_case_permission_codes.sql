update sys_menu set permission_code = 'system:user:resetPassword' where permission_code = 'system:user:reset-password';
update sys_menu set permission_code = 'system:user:assignRole' where permission_code = 'system:user:assign-role';
update sys_menu set permission_code = 'system:loginLog:list', path = '/system/loginLogs' where permission_code = 'system:login-log:list';
update sys_menu set permission_code = 'system:operationLog:list', path = '/system/operationLogs' where permission_code = 'system:operation-log:list';
update sys_menu set path = '/system/onlineUsers' where path = '/system/online-users';
