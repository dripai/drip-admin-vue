package com.drip.admin;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drip.admin.common.exception.BusinessException;
import com.drip.admin.common.exception.GlobalExceptionHandler;
import com.drip.admin.common.log.LogService;
import com.drip.admin.common.log.OperationLog;
import com.drip.admin.common.log.OperationLogAspect;
import com.drip.admin.common.response.ApiResponse;
import com.drip.admin.common.response.PageResult;
import com.drip.admin.common.security.RequirePermission;
import com.drip.admin.config.MybatisPlusConfig;
import com.drip.admin.infrastructure.external.JobExecutorRegistry;
import com.drip.admin.infrastructure.redis.LoginAttemptService;
import com.drip.admin.infrastructure.redis.OnlineSessionService;
import com.drip.admin.modules.system.dto.LoginRequest;
import com.drip.admin.modules.system.entity.SysConfigEntity;
import com.drip.admin.modules.system.entity.SysDbBackupEntity;
import com.drip.admin.modules.system.entity.SysDeptEntity;
import com.drip.admin.modules.system.entity.SysDictItemEntity;
import com.drip.admin.modules.system.entity.SysDictTypeEntity;
import com.drip.admin.modules.system.entity.SysJobEntity;
import com.drip.admin.modules.system.entity.SysJobRunLogEntity;
import com.drip.admin.modules.system.entity.SysLoginLogEntity;
import com.drip.admin.modules.system.entity.SysMenuEntity;
import com.drip.admin.modules.system.entity.SysOperationLogEntity;
import com.drip.admin.modules.system.entity.SysRoleEntity;
import com.drip.admin.modules.system.entity.SysRoleMenuEntity;
import com.drip.admin.modules.system.entity.SysUserEntity;
import com.drip.admin.modules.system.entity.SysUserRoleEntity;
import com.drip.admin.modules.system.mapper.SysConfigMapper;
import com.drip.admin.modules.system.mapper.SysDbBackupMapper;
import com.drip.admin.modules.system.mapper.SysDeptMapper;
import com.drip.admin.modules.system.mapper.SysDictItemMapper;
import com.drip.admin.modules.system.mapper.SysDictTypeMapper;
import com.drip.admin.modules.system.mapper.SysJobMapper;
import com.drip.admin.modules.system.mapper.SysJobRunLogMapper;
import com.drip.admin.modules.system.mapper.SysLoginLogMapper;
import com.drip.admin.modules.system.mapper.SysMenuMapper;
import com.drip.admin.modules.system.mapper.SysOperationLogMapper;
import com.drip.admin.modules.system.mapper.SysRoleMapper;
import com.drip.admin.modules.system.mapper.SysRoleMenuMapper;
import com.drip.admin.modules.system.mapper.SysUserMapper;
import com.drip.admin.modules.system.mapper.SysUserRoleMapper;
import com.drip.admin.modules.system.service.AuthService;
import com.drip.admin.modules.system.service.impl.AuthServiceImpl;
import com.drip.admin.modules.system.controller.DatabaseBackupController;
import com.drip.admin.modules.system.service.DatabaseBackupService;
import com.drip.admin.modules.system.service.impl.DatabaseBackupServiceImpl;
import com.drip.admin.modules.system.controller.HealthController;
import com.drip.admin.modules.system.controller.JobController;
import com.drip.admin.modules.system.service.JobService;
import com.drip.admin.modules.system.service.impl.JobServiceImpl;
import com.drip.admin.modules.system.controller.ConfigController;
import com.drip.admin.modules.system.service.ConfigService;
import com.drip.admin.modules.system.controller.DeptController;
import com.drip.admin.modules.system.service.DeptService;
import com.drip.admin.modules.system.controller.DictController;
import com.drip.admin.modules.system.service.DictService;
import com.drip.admin.modules.system.controller.MenuController;
import com.drip.admin.modules.system.service.MenuService;
import com.drip.admin.modules.system.controller.SystemLogController;
import com.drip.admin.modules.system.service.SystemLogQueryService;
import com.drip.admin.modules.system.controller.RoleController;
import com.drip.admin.modules.system.service.RoleService;
import com.drip.admin.modules.system.service.AdminService;
import com.drip.admin.modules.system.controller.FileController;
import com.drip.admin.modules.system.service.FileService;
import com.drip.admin.modules.system.service.impl.FileServiceImpl;
import com.drip.admin.modules.system.controller.OnlineUserController;
import com.drip.admin.modules.system.service.OnlineUserService;
import com.drip.admin.modules.system.service.impl.OnlineUserServiceImpl;
import com.drip.admin.modules.system.service.impl.ConfigServiceImpl;
import com.drip.admin.modules.system.service.impl.DeptServiceImpl;
import com.drip.admin.modules.system.service.impl.DictServiceImpl;
import com.drip.admin.modules.system.service.impl.MenuServiceImpl;
import com.drip.admin.modules.system.service.impl.SystemLogQueryServiceImpl;
import com.drip.admin.modules.system.service.impl.RoleServiceImpl;
import com.drip.admin.modules.system.service.impl.UserServiceImpl;
import com.drip.admin.modules.system.controller.UserController;
import com.drip.admin.modules.system.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.mybatis.spring.annotation.MapperScan;
import org.junit.jupiter.api.Test;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

class BackendContractTests {
    @Test
    void loginFailureWritesLoginLogAndReturnsBusinessError() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        LogService logService = mock(LogService.class);
        OnlineSessionService onlineSessionService = mock(OnlineSessionService.class);
        LoginAttemptService loginAttemptService = mock(LoginAttemptService.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        AuthService authService = new AuthServiceImpl(jdbc, logService, onlineSessionService, loginAttemptService, 1800, 28800);

        when(jdbc.queryForList(eq("select * from sys_user where username = ? and deleted = 0"), eq("missing"))).thenReturn(List.of());

        BusinessException error = assertThrows(BusinessException.class,
            () -> authService.login(new LoginRequest("missing", "bad-password", "web"), request));

        assertEquals(401000, error.code());
        verify(logService).login(null, "missing", null, "LOGIN", "FAIL", "用户名或密码错误", request, "web");
        verify(loginAttemptService).assertNotLocked("missing");
        verify(loginAttemptService).recordFailure("missing");
    }

    @Test
    void currentUserResponseAggregatesUserRolesMenusAndPermissions() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        LogService logService = mock(LogService.class);
        OnlineSessionService onlineSessionService = mock(OnlineSessionService.class);
        AuthService authService = new AuthServiceImpl(jdbc, logService, onlineSessionService, mock(LoginAttemptService.class), 1800, 28800);

        when(jdbc.queryForList(eq("select * from sys_user where id = ? and deleted = 0"), eq(1L))).thenReturn(List.of(Map.of(
            "id", 1L,
            "username", "admin",
            "real_name", "Administrator",
            "dept_id", 10L
        )));
        when(jdbc.queryForList(contains("select r.role_code"), eq(String.class), eq(1L))).thenReturn(List.of("SUPER_ADMIN"));
        when(jdbc.queryForList(
            eq("select permission_code from sys_menu where deleted = 0 and status = 1 and permission_code is not null"),
            eq(String.class)
        )).thenReturn(List.of("system:user:list"));
        when(jdbc.queryForList(eq("select id, parent_id, name, type, path, component, permission_code, icon, sort, visible from sys_menu where deleted = 0 and status = 1 order by sort asc, id asc")))
            .thenReturn(List.of(Map.of("id", 1L, "parent_id", 0L, "name", "System", "type", "MENU")));

        Map<String, Object> me = authService.me(1L);

        assertEquals("admin", me.get("username"));
        assertEquals(List.of("SUPER_ADMIN"), me.get("roles"));
        assertEquals(List.of("system:user:list"), me.get("permissions"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> menus = (List<Map<String, Object>>) me.get("menus");
        assertEquals("System", menus.getFirst().get("name"));
    }

    @Test
    void lockedLoginAttemptStopsBeforeCredentialLookup() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        LogService logService = mock(LogService.class);
        OnlineSessionService onlineSessionService = mock(OnlineSessionService.class);
        LoginAttemptService loginAttemptService = mock(LoginAttemptService.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        AuthService authService = new AuthServiceImpl(jdbc, logService, onlineSessionService, loginAttemptService, 1800, 28800);

        doThrow(new BusinessException(401000, "用户名或密码错误")).when(loginAttemptService).assertNotLocked("locked");

        BusinessException error = assertThrows(BusinessException.class,
            () -> authService.login(new LoginRequest("locked", "bad-password", "web"), request));

        assertEquals(401000, error.code());
        verifyNoInteractions(jdbc, logService, onlineSessionService);
    }

    @Test
    @SuppressWarnings("unchecked")
    void loginAttemptServiceLocksWhenFailureLimitReached() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> values = mock(ValueOperations.class);
        LoginAttemptService service = new LoginAttemptService(redis, 5, 900);

        when(redis.opsForValue()).thenReturn(values);
        when(values.get("drip:login:fail:demo")).thenReturn("4", "5");

        assertDoesNotThrow(() -> service.assertNotLocked("Demo"));
        BusinessException error = assertThrows(BusinessException.class, () -> service.assertNotLocked("Demo"));

        assertEquals(401000, error.code());
    }

    @Test
    void roleAuthorizationPersistsMenuIdsOnly() {
        RoleService roleService = mock(RoleService.class);
        RoleController controller = new RoleController(roleService);

        controller.rolePermissions(7L, Map.of("menuIds", List.of(1, 2, 3)));

        verify(roleService).assignMenus(7L, List.of(1L, 2L, 3L));
    }

    @Test
    void menuControllerDelegatesWritesToMenuService() {
        MenuService menuService = mock(MenuService.class);
        MenuController controller = new MenuController(menuService);

        controller.updateMenu(3L, Map.of("name", "菜单"));

        verify(menuService).update(3L, Map.of("name", "菜单"));
    }

    @Test
    void userControllerDelegatesRoleAssignmentToUserService() {
        UserService userService = mock(UserService.class);
        UserController controller = new UserController(userService);

        controller.userRoles(10L, Map.of("roleIds", List.of(1, 2)));

        verify(userService).assignRoles(10L, List.of(1L, 2L));
    }

    @Test
    void deptControllerDelegatesWritesToDeptService() {
        DeptService deptService = mock(DeptService.class);
        DeptController controller = new DeptController(deptService);

        controller.updateDept(3L, Map.of("deptName", "Research"));

        verify(deptService).update(3L, Map.of("deptName", "Research"));
    }

    @Test
    void dictControllerDelegatesItemWritesToDictService() {
        DictService dictService = mock(DictService.class);
        DictController controller = new DictController(dictService);

        controller.updateDictItem(9L, Map.of("label", "Enabled"));

        verify(dictService).updateItem(9L, Map.of("label", "Enabled"));
    }

    @Test
    void configControllerDelegatesWritesToConfigService() {
        ConfigService configService = mock(ConfigService.class);
        ConfigController controller = new ConfigController(configService);

        controller.updateConfig(6L, Map.of("configValue", "demo"));

        verify(configService).update(6L, Map.of("configValue", "demo"));
    }

    @Test
    void logControllerDelegatesQueriesToLogQueryService() {
        SystemLogQueryService logQueryService = mock(SystemLogQueryService.class);
        SystemLogController controller = new SystemLogController(logQueryService);

        controller.operationLog(12L);

        verify(logQueryService).operationLog(12L);
    }

    @Test
    void onlineUserControllerDelegatesKickoutToOnlineUserService() {
        OnlineUserService onlineUserService = mock(OnlineUserService.class);
        OnlineUserController controller = new OnlineUserController(onlineUserService);

        controller.kickout("token-1");

        verify(onlineUserService).kickout("token-1");
    }

    @Test
    void onlineUserServiceReadsSessionsFromRedisBackedService() {
        OnlineSessionService onlineSessionService = mock(OnlineSessionService.class);
        OnlineUserService onlineUserService = new OnlineUserServiceImpl(onlineSessionService);
        PageResult<Map<String, Object>> page = new PageResult<>(List.of(Map.of("tokenId", "token-1")), 1, 1, 20);

        when(onlineSessionService.page(Map.of("page", "1"))).thenReturn(page);

        assertEquals(page, onlineUserService.page(Map.of("page", "1")));
    }

    @Test
    void jobControllerDelegatesManualRunToJobService() {
        JobService jobService = mock(JobService.class);
        JobController controller = new JobController(jobService);

        controller.runJob(15L);

        verify(jobService).run(15L);
    }

    @Test
    void databaseBackupControllerDelegatesRestoreToDatabaseBackupService() {
        DatabaseBackupService databaseBackupService = mock(DatabaseBackupService.class);
        DatabaseBackupController controller = new DatabaseBackupController(databaseBackupService);

        controller.restoreBackup(21L, Map.of("confirmed", true));

        verify(databaseBackupService).restore(21L, Map.of("confirmed", true));
    }

    @Test
    void fileControllerDelegatesUploadToFileService() throws Exception {
        FileService fileService = mock(FileService.class);
        FileController controller = new FileController(fileService);
        MultipartFile file = mock(MultipartFile.class);

        controller.upload(file);

        verify(fileService).upload(file);
    }

    @Test
    void healthEndpointReturnsUpStatus() {
        ApiResponse<Map<String, Object>> response = new HealthController().health();

        assertEquals(0, response.code());
        assertEquals("UP", response.data().get("status"));
    }

    @Test
    void criticalWriteOperationsHaveBusinessOperationLogs() throws Exception {
        assertOperationLogged(UserController.class, "createUser", Map.class);
        assertOperationLogged(RoleController.class, "rolePermissions", long.class, Map.class);
        assertOperationLogged(JobController.class, "runJob", long.class);
        assertOperationLogged(DatabaseBackupController.class, "restoreBackup", long.class, Map.class);
    }

    @Test
    void fileUploadRequiresPermissionCode() throws Exception {
        Method method = FileController.class.getMethod("upload", org.springframework.web.multipart.MultipartFile.class);
        RequirePermission permission = method.getAnnotation(RequirePermission.class);

        assertEquals("system:file:upload", permission.value());
    }

    @Test
    void saTokenUsesSingleTokenSessionMode() throws Exception {
        String application = Files.readString(Path.of("src/main/resources/application.yml"));

        assertTrue(application.contains("is-concurrent: false"));
        assertTrue(application.contains("is-share: false"));
    }

    @Test
    void p1WriteOperationsUseActionPermissions() throws Exception {
        assertPermission(MenuController.class, "createMenu", "system:menu:create", Map.class);
        assertPermission(MenuController.class, "updateMenu", "system:menu:update", long.class, Map.class);
        assertPermission(MenuController.class, "deleteMenu", "system:menu:delete", long.class);
        assertPermission(MenuController.class, "menuStatus", "system:menu:status", long.class, Map.class);
        assertPermission(DeptController.class, "createDept", "system:dept:create", Map.class);
        assertPermission(DictController.class, "createDictType", "system:dict:create", Map.class);
        assertPermission(ConfigController.class, "createConfig", "system:config:create", Map.class);
        assertPermission(JobController.class, "createJob", "system:job:create", Map.class);
        assertPermission(JobController.class, "runJob", "system:job:run", long.class);
        assertPermission(OnlineUserController.class, "kickout", "system:online:kickout", String.class);
    }

    @Test
    void operationLogFailureDoesNotRollbackBusinessResult() throws Throwable {
        LogService logService = mock(LogService.class);
        OperationLogAspect aspect = new OperationLogAspect(logService);
        ProceedingJoinPoint point = mock(ProceedingJoinPoint.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        OperationLog operationLog = UserController.class.getMethod("createUser", Map.class).getAnnotation(OperationLog.class);

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        when(point.getArgs()).thenReturn(new Object[]{Map.of("username", "demo")});
        when(point.proceed()).thenReturn("ok");
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/system/users");
        doThrow(new IllegalStateException("log store down")).when(logService)
            .operation(any(), any(), any(), any(), any(), any(), any(), anyLong());

        try {
            assertEquals("ok", aspect.write(point, operationLog));
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    void externalCommandOperationsDoNotRunInsideDeclarativeTransactions() throws Exception {
        assertEquals(null, AdminService.class.getMethod("runJob", long.class).getAnnotation(Transactional.class));
        assertEquals(null, JobServiceImpl.class.getMethod("run", long.class).getAnnotation(Transactional.class));
        assertEquals(null, AdminService.class.getMethod("createBackup", Map.class, long.class).getAnnotation(Transactional.class));
        assertEquals(null, AdminService.class.getMethod("restoreBackup", long.class, Map.class).getAnnotation(Transactional.class));
        assertEquals(null, DatabaseBackupServiceImpl.class.getMethod("create", Map.class, long.class).getAnnotation(Transactional.class));
        assertEquals(null, DatabaseBackupServiceImpl.class.getMethod("restore", long.class, Map.class).getAnnotation(Transactional.class));
    }

    @Test
    void jobServiceWritesSuccessRunLogAfterWhitelistedExecution() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        JobExecutorRegistry registry = mock(JobExecutorRegistry.class);
        JobServiceImpl jobService = new JobServiceImpl(jdbc, registry);

        when(jdbc.queryForList("select * from sys_job where id = ? and deleted = 0", 9L))
            .thenReturn(List.of(Map.of(
                "id", 9L,
                "job_name", "health",
                "bean_name", "systemHealthJob",
                "method_name", "run"
            )));

        jobService.run(9L);

        verify(registry).execute("systemHealthJob", "run");
        verify(jdbc).update(
            eq("insert into sys_job_run_log (job_id, job_name, status, started_at, finished_at, cost_ms) values (?, ?, 'SUCCESS', ?, ?, ?)"),
            eq(9L),
            eq("health"),
            any(),
            any(),
            any()
        );
    }

    @Test
    void databaseBackupServiceRequiresRestoreConfirmationBeforeRunningCommand() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        DatabaseBackupServiceImpl service = new DatabaseBackupServiceImpl(
            jdbc,
            "./backups",
            "jdbc:mysql://localhost:3307/drip-manager",
            "root",
            "root",
            "mysqldump",
            "mysql"
        );

        when(jdbc.queryForList("select * from sys_db_backup where id = ?", 3L))
            .thenReturn(List.of(Map.of("id", 3L, "file_path", "./backups/demo.sql", "backup_name", "demo.sql")));

        BusinessException error = assertThrows(BusinessException.class, () -> service.restore(3L, Map.of("confirmed", false)));

        assertEquals(400000, error.code());
    }

    @Test
    void fileServiceRejectsDisallowedContentType() {
        FileServiceImpl fileService = new FileServiceImpl(1024, "image/png");
        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(10L);
        when(file.getContentType()).thenReturn("application/x-msdownload");

        BusinessException error = assertThrows(BusinessException.class, () -> fileService.upload(file));

        assertEquals(400000, error.code());
    }

    @Test
    void roleAuthorizationRejectsUnknownMenuIdsBeforeReplacingPermissions() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        AdminService adminService = new AdminService(
            jdbc,
            mock(OnlineSessionService.class),
            mock(JobExecutorRegistry.class),
            1024,
            "image/png",
            "./backups",
            "jdbc:mysql://localhost:3307/drip-manager",
            "root",
            "root",
            "mysqldump",
            "mysql"
        );

        when(jdbc.queryForList("select * from sys_role where id = ? and deleted = 0", 7L))
            .thenReturn(List.of(Map.of("id", 7L)));
        when(jdbc.queryForObject("select count(1) from sys_menu where id in (?, ?) and deleted = 0", Long.class, 1L, 99L))
            .thenReturn(1L);

        BusinessException error = assertThrows(BusinessException.class, () -> adminService.assignRoleMenus(7L, List.of(1L, 99L)));

        assertEquals(400000, error.code());
        verify(jdbc, never()).update("delete from sys_role_menu where role_id = ?", 7L);
    }

    @Test
    void userServiceRejectsUnknownRoleIdsBeforeReplacingRoles() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        UserService userService = new UserServiceImpl(jdbc);

        when(jdbc.queryForObject("select count(1) from sys_role where id in (?, ?) and deleted = 0", Long.class, 1L, 99L))
            .thenReturn(1L);

        BusinessException error = assertThrows(BusinessException.class, () -> userService.assignRoles(10L, List.of(1L, 99L)));

        assertEquals(400000, error.code());
        verify(jdbc, never()).update("delete from sys_user_role where user_id = ?", 10L);
    }

    @Test
    void menuServiceRejectsDeletingParentMenu() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        MenuService menuService = new MenuServiceImpl(jdbc);

        when(jdbc.queryForObject("select count(1) from sys_menu where parent_id = ? and deleted = 0", Long.class, 15L))
            .thenReturn(1L);

        BusinessException error = assertThrows(BusinessException.class, () -> menuService.delete(15L));

        assertEquals(400301, error.code());
        verify(jdbc, never()).update("update sys_menu set deleted = 1 where id = ?", 15L);
    }

    @Test
    void deptServiceRejectsMovingDeptUnderDescendant() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        DeptService deptService = new DeptServiceImpl(jdbc);

        when(jdbc.queryForList("select * from sys_dept where id = ? and deleted = 0", 10L))
            .thenReturn(List.of(Map.of("id", 10L)));
        when(jdbc.queryForList("select id from sys_dept where parent_id = ? and deleted = 0", Long.class, 10L))
            .thenReturn(List.of(11L));
        when(jdbc.queryForList("select id from sys_dept where parent_id = ? and deleted = 0", Long.class, 11L))
            .thenReturn(List.of());

        BusinessException error = assertThrows(BusinessException.class,
            () -> deptService.update(10L, Map.of("parentId", 11L)));

        assertEquals(400000, error.code());
        verify(jdbc, never()).update(eq("update sys_dept set parent_id = ? where id = ? and deleted = 0"), any(Object[].class));
    }

    @Test
    void referencedCommonStatusDictItemCannotBeDeleted() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        DictService dictService = new DictServiceImpl(jdbc);

        when(jdbc.queryForList("select * from sys_dict_item where id = ? and deleted = 0", 5L))
            .thenReturn(List.of(Map.of("id", 5L, "dict_type_id", 1L, "value", "1")));
        when(jdbc.queryForList("select * from sys_dict_type where id = ? and deleted = 0", 1L))
            .thenReturn(List.of(Map.of("id", 1L, "dict_code", "common_status")));
        when(jdbc.queryForObject("select count(1) from sys_user where status = ? and deleted = 0", Long.class, 1))
            .thenReturn(1L);

        BusinessException error = assertThrows(BusinessException.class, () -> dictService.deleteItem(5L));

        assertEquals(400501, error.code());
    }

    @Test
    void configServiceMasksSensitiveConfigValues() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        ConfigService configService = new ConfigServiceImpl(jdbc);

        when(jdbc.queryForList("select * from sys_config where id = ? and deleted = 0", 8L))
            .thenReturn(List.of(Map.of("id", 8L, "config_value", "secret", "is_sensitive", 1)));

        Map<String, Object> row = configService.detail(8L);

        assertEquals("******", row.get("config_value"));
    }

    @Test
    void builtinConfigCannotBeDeleted() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        ConfigService configService = new ConfigServiceImpl(jdbc);

        when(jdbc.queryForList("select * from sys_config where id = ? and deleted = 0", 8L))
            .thenReturn(List.of(Map.of("id", 8L, "builtin", 1)));

        BusinessException error = assertThrows(BusinessException.class, () -> configService.delete(8L));

        assertEquals(400000, error.code());
        verify(jdbc, never()).update("update sys_config set deleted = 1 where id = ?", 8L);
    }

    @Test
    void logQueryServiceAppliesOperationLogFilters() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        SystemLogQueryService logQueryService = new SystemLogQueryServiceImpl(jdbc);

        when(jdbc.queryForObject(
            "select count(1) from sys_operation_log where 1 = 1 and module like ? and response_status like ?",
            Long.class,
            "%用户管理%",
            "%SUCCESS%"
        )).thenReturn(1L);
        when(jdbc.queryForList(
            "select * from sys_operation_log where 1 = 1 and module like ? and response_status like ? order by created_at desc limit ?, ?",
            "%用户管理%",
            "%SUCCESS%",
            0,
            20
        )).thenReturn(List.of(Map.of("id", 1L)));

        PageResult<Map<String, Object>> result = logQueryService.operationLogs(Map.of(
            "module", "用户管理",
            "responseStatus", "SUCCESS"
        ));

        assertEquals(1, result.total());
    }

    @Test
    void mybatisPlusEntitiesAndMappersCoverSystemTables() throws Exception {
        Object[][] mappings = {
            {SysUserEntity.class, SysUserMapper.class, "sys_user"},
            {SysRoleEntity.class, SysRoleMapper.class, "sys_role"},
            {SysUserRoleEntity.class, SysUserRoleMapper.class, "sys_user_role"},
            {SysMenuEntity.class, SysMenuMapper.class, "sys_menu"},
            {SysRoleMenuEntity.class, SysRoleMenuMapper.class, "sys_role_menu"},
            {SysDeptEntity.class, SysDeptMapper.class, "sys_dept"},
            {SysDictTypeEntity.class, SysDictTypeMapper.class, "sys_dict_type"},
            {SysDictItemEntity.class, SysDictItemMapper.class, "sys_dict_item"},
            {SysLoginLogEntity.class, SysLoginLogMapper.class, "sys_login_log"},
            {SysOperationLogEntity.class, SysOperationLogMapper.class, "sys_operation_log"},
            {SysJobEntity.class, SysJobMapper.class, "sys_job"},
            {SysJobRunLogEntity.class, SysJobRunLogMapper.class, "sys_job_run_log"},
            {SysDbBackupEntity.class, SysDbBackupMapper.class, "sys_db_backup"},
            {SysConfigEntity.class, SysConfigMapper.class, "sys_config"}
        };
        Set<Class<?>> logicDeleteEntities = Set.of(
            SysUserEntity.class,
            SysRoleEntity.class,
            SysMenuEntity.class,
            SysDeptEntity.class,
            SysDictTypeEntity.class,
            SysDictItemEntity.class,
            SysJobEntity.class,
            SysConfigEntity.class
        );

        assertEquals(14, mappings.length);
        for (Object[] mapping : mappings) {
            Class<?> entityType = (Class<?>) mapping[0];
            Class<?> mapperType = (Class<?>) mapping[1];
            String tableName = (String) mapping[2];

            assertEquals(tableName, entityType.getAnnotation(TableName.class).value());
            assertTrue(BaseMapper.class.isAssignableFrom(mapperType));
            assertTrue(entityType.getDeclaredField("id").isAnnotationPresent(TableId.class));
            assertEquals(logicDeleteEntities.contains(entityType), hasTableLogicField(entityType));
        }

        MapperScan mapperScan = MybatisPlusConfig.class.getAnnotation(MapperScan.class);
        assertEquals("com.drip.admin.modules.system.mapper", mapperScan.value()[0]);
    }

    @Test
    void businessExceptionHttpStatusMatchesErrorCode() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        assertEquals(HttpStatus.UNAUTHORIZED, handler.business(new BusinessException(401000, "unauthorized")).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, handler.business(new BusinessException(403000, "forbidden")).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, handler.business(new BusinessException(404000, "not found")).getStatusCode());
        assertEquals(HttpStatus.CONFLICT, handler.business(new BusinessException(409000, "conflict")).getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, handler.business(new BusinessException(500000, "system")).getStatusCode());
    }

    @Test
    void globalExceptionHandlerIsRegisteredAsControllerAdvice() {
        assertTrue(GlobalExceptionHandler.class.isAnnotationPresent(RestControllerAdvice.class));
    }

    private static void assertOperationLogged(Class<?> type, String methodName, Class<?>... parameterTypes) throws Exception {
        Method method = type.getMethod(methodName, parameterTypes);
        OperationLog operationLog = method.getAnnotation(OperationLog.class);

        assertTrue(operationLog != null && !operationLog.module().isBlank() && !operationLog.action().isBlank());
    }

    private static void assertPermission(Class<?> type, String methodName, String permissionCode, Class<?>... parameterTypes) throws Exception {
        Method method = type.getMethod(methodName, parameterTypes);
        RequirePermission permission = method.getAnnotation(RequirePermission.class);

        assertEquals(permissionCode, permission.value());
    }

    private static boolean hasTableLogicField(Class<?> entityType) {
        for (Field field : entityType.getDeclaredFields()) {
            if (field.isAnnotationPresent(TableLogic.class)) {
                return true;
            }
        }
        return false;
    }
}
