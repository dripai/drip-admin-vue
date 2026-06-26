package com.drip.admin;

import com.drip.admin.common.exception.BusinessException;
import com.drip.admin.common.log.LogService;
import com.drip.admin.common.log.OperationLog;
import com.drip.admin.common.log.OperationLogAspect;
import com.drip.admin.common.response.ApiResponse;
import com.drip.admin.common.security.RequirePermission;
import com.drip.admin.infrastructure.external.JobExecutorRegistry;
import com.drip.admin.infrastructure.redis.OnlineSessionService;
import com.drip.admin.modules.auth.dto.LoginRequest;
import com.drip.admin.modules.auth.service.AuthService;
import com.drip.admin.modules.system.database.controller.DatabaseBackupController;
import com.drip.admin.modules.system.health.controller.HealthController;
import com.drip.admin.modules.system.job.controller.JobController;
import com.drip.admin.modules.system.config.controller.ConfigController;
import com.drip.admin.modules.system.dept.controller.DeptController;
import com.drip.admin.modules.system.dict.controller.DictController;
import com.drip.admin.modules.system.role.controller.RoleController;
import com.drip.admin.modules.system.service.AdminService;
import com.drip.admin.modules.system.file.controller.FileController;
import com.drip.admin.modules.system.online.controller.OnlineUserController;
import com.drip.admin.modules.system.user.controller.UserController;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

class BackendContractTests {
    @Test
    void loginFailureWritesLoginLogAndReturnsBusinessError() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        LogService logService = mock(LogService.class);
        AdminService adminService = mock(AdminService.class);
        OnlineSessionService onlineSessionService = mock(OnlineSessionService.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        AuthService authService = new AuthService(jdbc, logService, adminService, onlineSessionService, 1800, 28800);

        when(jdbc.queryForList(eq("select * from sys_user where username = ? and deleted = 0"), eq("missing"))).thenReturn(List.of());

        BusinessException error = assertThrows(BusinessException.class,
            () -> authService.login(new LoginRequest("missing", "bad-password", "web"), request));

        assertEquals(401000, error.code());
        verify(logService).login(null, "missing", null, "LOGIN", "FAIL", "用户名或密码错误", request, "web");
    }

    @Test
    void currentUserResponseAggregatesUserRolesMenusAndPermissions() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        LogService logService = mock(LogService.class);
        AdminService adminService = mock(AdminService.class);
        OnlineSessionService onlineSessionService = mock(OnlineSessionService.class);
        AuthService authService = new AuthService(jdbc, logService, adminService, onlineSessionService, 1800, 28800);

        when(adminService.detail("sys_user", 1L)).thenReturn(Map.of(
            "id", 1L,
            "username", "admin",
            "real_name", "Administrator",
            "dept_id", 10L
        ));
        when(adminService.roleCodes(1L)).thenReturn(List.of("SUPER_ADMIN"));
        when(adminService.permissionCodes(1L)).thenReturn(List.of("system:user:list"));
        when(adminService.menuTree(1L)).thenReturn(List.of(Map.of("name", "System")));

        Map<String, Object> me = authService.me(1L);

        assertEquals("admin", me.get("username"));
        assertEquals(List.of("SUPER_ADMIN"), me.get("roles"));
        assertEquals(List.of("system:user:list"), me.get("permissions"));
        assertEquals(List.of(Map.of("name", "System")), me.get("menus"));
    }

    @Test
    void roleAuthorizationPersistsMenuIdsOnly() {
        AdminService adminService = mock(AdminService.class);
        RoleController controller = new RoleController(adminService);

        controller.rolePermissions(7L, Map.of("menuIds", List.of(1, 2, 3)));

        verify(adminService).assignRoleMenus(7L, List.of(1L, 2L, 3L));
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
        assertEquals(null, AdminService.class.getMethod("createBackup", Map.class, long.class).getAnnotation(Transactional.class));
        assertEquals(null, AdminService.class.getMethod("restoreBackup", long.class, Map.class).getAnnotation(Transactional.class));
    }

    @Test
    void referencedCommonStatusDictItemCannotBeDeleted() {
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

        when(jdbc.queryForList("select * from sys_dict_item where id = ? and deleted = 0", 5L))
            .thenReturn(List.of(Map.of("id", 5L, "dict_type_id", 1L, "value", "1")));
        when(jdbc.queryForList("select * from sys_dict_type where id = ? and deleted = 0", 1L))
            .thenReturn(List.of(Map.of("id", 1L, "dict_code", "common_status")));
        when(jdbc.queryForObject("select count(1) from sys_user where status = ? and deleted = 0", Long.class, 1))
            .thenReturn(1L);

        BusinessException error = assertThrows(BusinessException.class, () -> adminService.deleteDictItem(5L));

        assertEquals(400501, error.code());
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
}
