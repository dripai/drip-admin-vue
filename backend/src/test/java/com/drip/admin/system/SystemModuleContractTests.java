package com.drip.admin.system;

import cn.dev33.satoken.stp.StpUtil;
import com.drip.admin.common.exception.BusinessException;
import com.drip.admin.common.log.OperationLog;
import com.drip.admin.common.log.OperationLogAspect;
import com.drip.admin.common.log.OperationLogRecorder;
import com.drip.admin.common.response.PageResult;
import com.drip.admin.infrastructure.external.JobRunner;
import com.drip.admin.infrastructure.external.JobScriptCatalog;
import com.drip.admin.infrastructure.redis.LoginAttemptService;
import com.drip.admin.infrastructure.redis.OnlineSessionService;
import com.drip.admin.modules.system.controller.*;
import com.drip.admin.modules.system.dto.*;
import com.drip.admin.modules.system.entity.SysJobEntity;
import com.drip.admin.modules.system.service.*;
import com.drip.admin.modules.system.service.impl.FileServiceImpl;
import com.drip.admin.modules.system.service.impl.OnlineUserServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SystemModuleContractTests {
    @Test
    @SuppressWarnings("unchecked")
    void loginAttemptServiceLocksWhenFailureLimitReached() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> values = mock(ValueOperations.class);
        ConfigService configService = mock(ConfigService.class);
        LoginAttemptService service = new LoginAttemptService(redis, configService);

        when(redis.opsForValue()).thenReturn(values);
        when(values.get("drip:login:fail:demo")).thenReturn("4", "5");
        when(redis.getExpire("drip:login:fail:demo", TimeUnit.SECONDS)).thenReturn(899L);
        when(configService.requiredInt("login.maxFailures")).thenReturn(5);

        assertDoesNotThrow(() -> service.assertNotLocked("Demo"));
        BusinessException error = assertThrows(BusinessException.class, () -> service.assertNotLocked("Demo"));

        assertEquals(401000, error.code());
        assertEquals("账号已锁定，请14分59秒后再试", error.getMessage());

        when(values.increment("drip:login:fail:demo")).thenReturn(5L);
        when(configService.requiredLong("login.lockSeconds")).thenReturn(900L);

        BusinessException recordError = assertThrows(BusinessException.class, () -> service.recordFailure("Demo"));

        assertEquals(401000, recordError.code());
        assertEquals("账号已锁定，请15分钟后再试", recordError.getMessage());
        verify(redis).expire("drip:login:fail:demo", 900L, TimeUnit.SECONDS);
    }

    @Test
    void roleAuthorizationPersistsMenuIdsOnly() {
        RoleService roleService = mock(RoleService.class);
        RoleController controller = new RoleController(roleService);

        MenuAssignRequest request = new MenuAssignRequest();
        request.setMenuIds(List.of(1L, 2L, 3L));

        controller.rolePermissions(7L, request);

        verify(roleService).assignMenus(7L, List.of(1L, 2L, 3L));
    }

    @Test
    void menuControllerDelegatesWritesToMenuService() {
        MenuService menuService = mock(MenuService.class);
        MenuController controller = new MenuController(menuService);

        MenuSaveRequest request = new MenuSaveRequest();
        request.setName("Menu");

        controller.updateMenu(3L, request);

        verify(menuService).update(eq(3L), argThat(body -> "Menu".equals(body.getName())));
    }

    @Test
    void userControllerDelegatesRoleAssignmentToUserService() {
        UserService userService = mock(UserService.class);
        UserController controller = new UserController(userService);

        RoleAssignRequest request = new RoleAssignRequest();
        request.setRoleIds(List.of(1L, 2L));

        controller.userRoles(10L, request);

        verify(userService).assignRoles(10L, List.of(1L, 2L));
    }

    @Test
    void deptControllerDelegatesWritesToDeptService() {
        DeptService deptService = mock(DeptService.class);
        DeptController controller = new DeptController(deptService);

        DeptSaveRequest request = new DeptSaveRequest();
        request.setDeptName("Research");

        controller.updateDept(3L, request);

        verify(deptService).update(eq(3L), argThat(body -> "Research".equals(body.getDeptName())));
    }

    @Test
    void dictControllerDelegatesItemWritesToDictService() {
        DictService dictService = mock(DictService.class);
        DictController controller = new DictController(dictService);

        DictItemSaveRequest request = new DictItemSaveRequest();
        request.setLabel("Enabled");

        controller.updateDictItem(9L, request);

        verify(dictService).updateItem(eq(9L), argThat(body -> "Enabled".equals(body.getLabel())));
    }

    @Test
    void configControllerDelegatesWritesToConfigService() {
        ConfigService configService = mock(ConfigService.class);
        ConfigController controller = new ConfigController(configService);

        ConfigSaveRequest request = new ConfigSaveRequest();
        request.setConfigValue("demo");

        controller.updateConfig(6L, request);

        verify(configService).update(eq(6L), argThat(body -> "demo".equals(body.getConfigValue())));
    }

    @Test
    void printTemplateControllerDelegatesWritesToPrintTemplateService() {
        PrintTemplateService printTemplateService = mock(PrintTemplateService.class);
        PrintTemplateController controller = new PrintTemplateController(printTemplateService);

        PrintTemplateSaveRequest request = new PrintTemplateSaveRequest();
        request.setName("Template");

        controller.update(6L, request);

        verify(printTemplateService).update(eq(6L), argThat(body -> "Template".equals(body.getName())));
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
        var page = new PageResult<>(List.of(Map.<String, Object>of("tokenId", "token-1")), 1, 1, 10);

        when(onlineSessionService.page(Map.of("page", "1", "pageSize", "10"))).thenReturn(page);

        OnlineUserQuery query = new OnlineUserQuery();
        query.setPage(1);
        assertEquals("token-1", onlineUserService.page(query).list().getFirst().tokenId());
    }

    @Test
    void jobControllerDelegatesManualRunToJobService() {
        JobService jobService = mock(JobService.class);
        JobScriptCatalog jobScriptCatalog = mock(JobScriptCatalog.class);
        JobRunner jobRunner = mock(JobRunner.class);
        SysJobEntity job = new SysJobEntity();
        job.setId(15L);
        when(jobService.detail(15L)).thenReturn(job);
        JobController controller = new JobController(jobService, jobScriptCatalog, jobRunner);

        controller.runJob(15L);

        verify(jobRunner).submit(job);
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
    void operationLogFailureDoesNotRollbackBusinessResult() throws Throwable {
        OperationLogRecorder operationLogRecorder = mock(OperationLogRecorder.class);
        OperationLogAspect aspect = new OperationLogAspect(operationLogRecorder);
        ProceedingJoinPoint point = mock(ProceedingJoinPoint.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        OperationLog operationLog = UserController.class.getMethod("createUser", UserSaveRequest.class).getAnnotation(OperationLog.class);

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        UserSaveRequest createUserRequest = new UserSaveRequest();
        createUserRequest.setUsername("demo");
        when(point.getArgs()).thenReturn(new Object[]{createUserRequest});
        when(point.proceed()).thenReturn("ok");
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/system/users");
        doThrow(new IllegalStateException("log store down")).when(operationLogRecorder)
            .operation(any(), any(), any(), any(), any(), any(), any(), anyLong());

        try {
            assertEquals("ok", aspect.write(point, operationLog));
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    void fileServiceRejectsDisallowedExtension() {
        ConfigService configService = mock(ConfigService.class);
        FileServiceImpl fileService = new FileServiceImpl(configService);
        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(10L);
        when(file.getOriginalFilename()).thenReturn("demo.exe");
        when(configService.requiredLong("upload.maxSizeBytes")).thenReturn(1024L);
        when(configService.requiredValue("upload.allowedExtensions")).thenReturn("png");

        BusinessException error = assertThrows(BusinessException.class, () -> fileService.upload(file));

        assertEquals(400000, error.code());
    }
}
