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
import com.drip.admin.modules.system.dto.*;
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
import static org.mockito.ArgumentMatchers.argThat;
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
        var page = new PageResult<>(List.of(Map.<String, Object>of("tokenId", "token-1")), 1, 1, 20);

        when(onlineSessionService.page(Map.of("page", "1", "pageSize", "20"))).thenReturn(page);

        OnlineUserQuery query = new OnlineUserQuery();
        query.setPage(1);
        assertEquals("token-1", onlineUserService.page(query).list().getFirst().tokenId());
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

        DatabaseRestoreRequest request = new DatabaseRestoreRequest();
        request.setConfirmed(true);

        controller.restoreBackup(21L, request);

        verify(databaseBackupService).restore(eq(21L), argThat(body -> Boolean.TRUE.equals(body.getConfirmed())));
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
        ApiResponse<com.drip.admin.modules.system.vo.HealthVo> response = new HealthController().health();

        assertEquals(0, response.code());
        assertEquals("UP", response.data().status());
    }

    @Test
    void criticalWriteOperationsHaveBusinessOperationLogs() throws Exception {
        assertOperationLogged(UserController.class, "createUser", UserSaveRequest.class);
        assertOperationLogged(RoleController.class, "rolePermissions", long.class, MenuAssignRequest.class);
        assertOperationLogged(JobController.class, "runJob", long.class);
        assertOperationLogged(DatabaseBackupController.class, "restoreBackup", long.class, DatabaseRestoreRequest.class);
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
        assertPermission(MenuController.class, "createMenu", "system:menu:create", MenuSaveRequest.class);
        assertPermission(MenuController.class, "updateMenu", "system:menu:update", long.class, MenuSaveRequest.class);
        assertPermission(MenuController.class, "deleteMenu", "system:menu:delete", long.class);
        assertPermission(MenuController.class, "menuStatus", "system:menu:status", long.class, StatusUpdateRequest.class);
        assertPermission(DeptController.class, "createDept", "system:dept:create", DeptSaveRequest.class);
        assertPermission(DictController.class, "createDictType", "system:dict:create", DictTypeSaveRequest.class);
        assertPermission(ConfigController.class, "createConfig", "system:config:create", ConfigSaveRequest.class);
        assertPermission(JobController.class, "createJob", "system:job:create", JobSaveRequest.class);
        assertPermission(JobController.class, "runJob", "system:job:run", long.class);
        assertPermission(OnlineUserController.class, "kickout", "system:online:kickout", String.class);
    }

    @Test
    void operationLogFailureDoesNotRollbackBusinessResult() throws Throwable {
        LogService logService = mock(LogService.class);
        OperationLogAspect aspect = new OperationLogAspect(logService);
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
        doThrow(new IllegalStateException("log store down")).when(logService)
            .operation(any(), any(), any(), any(), any(), any(), any(), anyLong());

        try {
            assertEquals("ok", aspect.write(point, operationLog));
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
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
