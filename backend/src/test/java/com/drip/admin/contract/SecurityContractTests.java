package com.drip.admin.contract;

import cn.dev33.satoken.stp.StpUtil;
import com.drip.admin.common.security.RequirePermission;
import com.drip.admin.common.security.SessionActivityRecorder;
import com.drip.admin.common.security.SessionInterceptor;
import com.drip.admin.modules.system.controller.*;
import com.drip.admin.modules.system.dto.*;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.drip.admin.support.TestSupport.assertOperationLogged;
import static com.drip.admin.support.TestSupport.assertPermission;
import static com.drip.admin.support.TestSupport.request;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityContractTests {
    @Test
    void sessionInterceptorChecksLoginAndTouchesProtectedRequest() {
        SessionActivityRecorder sessionActivityRecorder = mock(SessionActivityRecorder.class);
        SessionInterceptor interceptor = new SessionInterceptor(1800L, sessionActivityRecorder);

        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            assertDoesNotThrow(() -> interceptor.preHandle(request("/api", "/api/system/user"), new MockHttpServletResponse(), new Object()));

            stp.verify(StpUtil::checkLogin);
            verify(sessionActivityRecorder).touchCurrent(1800L);
        }
    }

    @Test
    void criticalWriteOperationsHaveBusinessOperationLogs() throws Exception {
        assertOperationLogged(UserController.class, "createUser", UserSaveRequest.class);
        assertOperationLogged(RoleController.class, "rolePermissions", long.class, MenuAssignRequest.class);
        assertOperationLogged(JobController.class, "runJob", long.class);
        assertOperationLogged(PrintTemplateController.class, "create", PrintTemplateSaveRequest.class);
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
        assertTrue(application.contains("timeout: 28800"));
        assertTrue(application.contains("active-timeout: 1800"));
        assertFalse(application.contains("drip:\n  session:"));
        assertTrue(application.contains("id-type: assign_id"));
    }

    @Test
    void p1WriteOperationsUseActionPermissions() throws Exception {
        assertPermission(MenuController.class, "createMenu", "system:menu:create", MenuSaveRequest.class);
        assertPermission(MenuController.class, "updateMenu", "system:menu:update", long.class, MenuSaveRequest.class);
        assertPermission(MenuController.class, "deleteMenu", "system:menu:delete", long.class);
        assertPermission(MenuController.class, "menuStatus", "system:menu:status", long.class, StatusUpdateRequest.class);
        assertPermission(UserController.class, "resetPassword", "system:user:resetPassword", long.class, PasswordResetRequest.class);
        assertPermission(UserController.class, "userRoles", "system:user:assignRole", long.class, RoleAssignRequest.class);
        assertPermission(DeptController.class, "createDept", "system:dept:create", DeptSaveRequest.class);
        assertPermission(DictController.class, "createDictType", "system:dict:create", DictTypeSaveRequest.class);
        assertPermission(ConfigController.class, "createConfig", "system:config:create", ConfigSaveRequest.class);
        assertPermission(PrintTemplateController.class, "templates", "system:printTemplate:list", PrintTemplateQuery.class);
        assertPermission(PrintTemplateController.class, "create", "system:printTemplate:create", PrintTemplateSaveRequest.class);
        assertPermission(PrintTemplateController.class, "update", "system:printTemplate:update", long.class, PrintTemplateSaveRequest.class);
        assertPermission(PrintTemplateController.class, "delete", "system:printTemplate:delete", long.class);
        assertPermission(SystemLogController.class, "loginLogs", "system:loginLog:list", LoginLogQuery.class);
        assertPermission(SystemLogController.class, "operationLogs", "system:operationLog:list", OperationLogQuery.class);
        assertPermission(JobController.class, "createJob", "system:job:create", JobSaveRequest.class);
        assertPermission(JobController.class, "runJob", "system:job:run", long.class);
        assertPermission(JobController.class, "jobRunLogs", "system:job:history", JobRunLogQuery.class);
        assertPermission(OnlineUserController.class, "kickout", "system:online:kickout", String.class);
    }
}
