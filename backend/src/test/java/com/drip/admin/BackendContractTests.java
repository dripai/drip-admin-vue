package com.drip.admin;

import com.drip.admin.common.exception.BusinessException;
import com.drip.admin.common.log.LogService;
import com.drip.admin.common.log.OperationLog;
import com.drip.admin.common.response.ApiResponse;
import com.drip.admin.infrastructure.redis.OnlineSessionService;
import com.drip.admin.modules.auth.dto.LoginRequest;
import com.drip.admin.modules.auth.service.AuthService;
import com.drip.admin.modules.system.database.controller.DatabaseBackupController;
import com.drip.admin.modules.system.health.controller.HealthController;
import com.drip.admin.modules.system.job.controller.JobController;
import com.drip.admin.modules.system.role.controller.RoleController;
import com.drip.admin.modules.system.service.AdminService;
import com.drip.admin.modules.system.user.controller.UserController;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    private static void assertOperationLogged(Class<?> type, String methodName, Class<?>... parameterTypes) throws Exception {
        Method method = type.getMethod(methodName, parameterTypes);
        OperationLog operationLog = method.getAnnotation(OperationLog.class);

        assertTrue(operationLog != null && !operationLog.module().isBlank() && !operationLog.action().isBlank());
    }
}
