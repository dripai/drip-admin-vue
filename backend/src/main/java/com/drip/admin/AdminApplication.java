package com.drip.admin;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.baomidou.mybatisplus.annotation.DbType;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.apache.ibatis.annotations.Mapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@SpringBootApplication
public class AdminApplication {
    private static final Logger log = LoggerFactory.getLogger(AdminApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }

    @Bean
    OpenAPI openAPI() {
        return new OpenAPI().info(new Info().title("Drip Admin Backend API").version("0.1.0"));
    }

    @Bean
    MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    @Bean
    ApplicationRunner flywayMigrationRunner(DataSource dataSource) {
        return args -> {
            Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .encoding("UTF-8")
                .baselineOnMigrate(true)
                .load();
            flyway.migrate();
        };
    }

    @Bean
    WebMvcConfigurer webMvcConfigurer(SessionInterceptor sessionInterceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(sessionInterceptor)
                    .addPathPatterns("/api/**")
                    .excludePathPatterns("/api/auth/login");
            }

            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOriginPatterns("*")
                    .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true);
            }
        };
    }

    @RestController
    @RequestMapping("/api/auth")
    static class AuthController {
        private final AuthService authService;

        AuthController(AuthService authService) {
            this.authService = authService;
        }

        @PostMapping("/login")
        ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
            return ApiResponse.success(authService.login(request, servletRequest));
        }

        @PostMapping("/logout")
        ApiResponse<Void> logout(HttpServletRequest request) {
            authService.logout(request);
            return ApiResponse.success(null);
        }

        @GetMapping("/me")
        ApiResponse<Map<String, Object>> me() {
            return ApiResponse.success(authService.me(currentUserId()));
        }

        @PutMapping("/password")
        ApiResponse<Void> password(@Valid @RequestBody PasswordRequest request) {
            authService.changePassword(currentUserId(), request);
            return ApiResponse.success(null);
        }
    }

    @RestController
    @RequestMapping("/api/system")
    static class SystemController {
        private final AdminService adminService;

        SystemController(AdminService adminService) {
            this.adminService = adminService;
        }

        @GetMapping("/users")
        @RequirePermission("system:user:list")
        ApiResponse<PageResult<Map<String, Object>>> users(@RequestParam Map<String, String> q) {
            return ApiResponse.success(adminService.page("sys_user", q, List.of("username", "real_name", "phone", "status", "dept_id", "created_at")));
        }

        @GetMapping("/users/{id}")
        @RequirePermission("system:user:detail")
        ApiResponse<Map<String, Object>> user(@PathVariable long id) {
            return ApiResponse.success(adminService.detail("sys_user", id));
        }

        @PostMapping("/users")
        @RequirePermission("system:user:create")
        @OperationLog(module = "用户管理", action = "新增用户")
        ApiResponse<Long> createUser(@RequestBody Map<String, Object> body) {
            return ApiResponse.success(adminService.createUser(body));
        }

        @PutMapping("/users/{id}")
        @RequirePermission("system:user:update")
        @OperationLog(module = "用户管理", action = "编辑用户")
        ApiResponse<Void> updateUser(@PathVariable long id, @RequestBody Map<String, Object> body) {
            adminService.updateUser(id, body);
            return ApiResponse.success(null);
        }

        @DeleteMapping("/users/{id}")
        @RequirePermission("system:user:delete")
        @OperationLog(module = "用户管理", action = "删除用户")
        ApiResponse<Void> deleteUser(@PathVariable long id) {
            adminService.deleteUser(currentUserId(), id);
            return ApiResponse.success(null);
        }

        @PatchMapping("/users/{id}/status")
        @RequirePermission("system:user:disable")
        @OperationLog(module = "用户管理", action = "变更用户状态")
        ApiResponse<Void> userStatus(@PathVariable long id, @RequestBody Map<String, Object> body) {
            adminService.updateStatus("sys_user", id, intValue(body, "status", 1), true);
            return ApiResponse.success(null);
        }

        @PutMapping("/users/{id}/roles")
        @RequirePermission("system:user:assign-role")
        @OperationLog(module = "用户管理", action = "分配角色")
        ApiResponse<Void> userRoles(@PathVariable long id, @RequestBody Map<String, Object> body) {
            adminService.assignUserRoles(id, longList(body.get("roleIds")));
            return ApiResponse.success(null);
        }

        @PostMapping("/users/{id}/reset-password")
        @RequirePermission("system:user:reset-password")
        @OperationLog(module = "用户管理", action = "重置密码")
        ApiResponse<Void> resetPassword(@PathVariable long id, @RequestBody Map<String, Object> body) {
            adminService.resetPassword(id, stringValue(body, "password", "Admin@123456"));
            return ApiResponse.success(null);
        }

        @GetMapping("/roles")
        @RequirePermission("system:role:list")
        ApiResponse<PageResult<Map<String, Object>>> roles(@RequestParam Map<String, String> q) {
            return ApiResponse.success(adminService.page("sys_role", q, List.of("role_name", "role_code", "status", "created_at")));
        }

        @GetMapping("/roles/{id}")
        @RequirePermission("system:role:list")
        ApiResponse<Map<String, Object>> role(@PathVariable long id) {
            return ApiResponse.success(adminService.detail("sys_role", id));
        }

        @PostMapping("/roles")
        @RequirePermission("system:role:create")
        @OperationLog(module = "角色管理", action = "新增角色")
        ApiResponse<Long> createRole(@RequestBody Map<String, Object> body) {
            return ApiResponse.success(adminService.insert("sys_role", body, Set.of("role_name", "role_code", "status", "remark")));
        }

        @PutMapping("/roles/{id}")
        @RequirePermission("system:role:update")
        @OperationLog(module = "角色管理", action = "编辑角色")
        ApiResponse<Void> updateRole(@PathVariable long id, @RequestBody Map<String, Object> body) {
            adminService.update("sys_role", id, body, Set.of("role_name", "role_code", "status", "remark"));
            return ApiResponse.success(null);
        }

        @DeleteMapping("/roles/{id}")
        @RequirePermission("system:role:delete")
        @OperationLog(module = "角色管理", action = "删除角色")
        ApiResponse<Void> deleteRole(@PathVariable long id) {
            adminService.deleteRole(id);
            return ApiResponse.success(null);
        }

        @PatchMapping("/roles/{id}/status")
        @RequirePermission("system:role:update")
        @OperationLog(module = "角色管理", action = "变更角色状态")
        ApiResponse<Void> roleStatus(@PathVariable long id, @RequestBody Map<String, Object> body) {
            adminService.updateStatus("sys_role", id, intValue(body, "status", 1), true);
            return ApiResponse.success(null);
        }

        @PutMapping("/roles/{id}/permissions")
        @RequirePermission("system:role:permission")
        @OperationLog(module = "角色管理", action = "角色授权")
        ApiResponse<Void> rolePermissions(@PathVariable long id, @RequestBody Map<String, Object> body) {
            adminService.assignRoleMenus(id, longList(body.get("menuIds")));
            return ApiResponse.success(null);
        }

        @GetMapping("/menus")
        @RequirePermission("system:menu:list")
        ApiResponse<List<Map<String, Object>>> menus() {
            return ApiResponse.success(adminService.menuTree(null));
        }

        @PostMapping("/menus")
        @RequirePermission("system:menu:write")
        @OperationLog(module = "菜单管理", action = "新增菜单")
        ApiResponse<Long> createMenu(@RequestBody Map<String, Object> body) {
            return ApiResponse.success(adminService.insert("sys_menu", body, Set.of("parent_id", "name", "type", "path", "component", "permission_code", "icon", "sort", "visible", "status")));
        }

        @PutMapping("/menus/{id}")
        @RequirePermission("system:menu:write")
        @OperationLog(module = "菜单管理", action = "编辑菜单")
        ApiResponse<Void> updateMenu(@PathVariable long id, @RequestBody Map<String, Object> body) {
            adminService.update("sys_menu", id, body, Set.of("parent_id", "name", "type", "path", "component", "permission_code", "icon", "sort", "visible", "status"));
            return ApiResponse.success(null);
        }

        @DeleteMapping("/menus/{id}")
        @RequirePermission("system:menu:write")
        @OperationLog(module = "菜单管理", action = "删除菜单")
        ApiResponse<Void> deleteMenu(@PathVariable long id) {
            adminService.deleteMenu(id);
            return ApiResponse.success(null);
        }

        @PatchMapping("/menus/{id}/status")
        @RequirePermission("system:menu:write")
        @OperationLog(module = "菜单管理", action = "变更菜单状态")
        ApiResponse<Void> menuStatus(@PathVariable long id, @RequestBody Map<String, Object> body) {
            adminService.updateStatus("sys_menu", id, intValue(body, "status", 1), true);
            return ApiResponse.success(null);
        }

        @GetMapping("/depts")
        @RequirePermission("system:dept:list")
        ApiResponse<List<Map<String, Object>>> depts() {
            return ApiResponse.success(adminService.tree("sys_dept", "parent_id", "sort"));
        }

        @PostMapping("/depts")
        @RequirePermission("system:dept:list")
        @OperationLog(module = "部门管理", action = "新增部门")
        ApiResponse<Long> createDept(@RequestBody Map<String, Object> body) {
            return ApiResponse.success(adminService.insert("sys_dept", body, Set.of("parent_id", "dept_name", "dept_code", "leader_user_id", "sort", "status")));
        }

        @PutMapping("/depts/{id}")
        @RequirePermission("system:dept:list")
        @OperationLog(module = "部门管理", action = "编辑部门")
        ApiResponse<Void> updateDept(@PathVariable long id, @RequestBody Map<String, Object> body) {
            adminService.updateDept(id, body);
            return ApiResponse.success(null);
        }

        @DeleteMapping("/depts/{id}")
        @RequirePermission("system:dept:list")
        @OperationLog(module = "部门管理", action = "删除部门")
        ApiResponse<Void> deleteDept(@PathVariable long id) {
            adminService.deleteDept(id);
            return ApiResponse.success(null);
        }

        @PatchMapping("/depts/{id}/status")
        @RequirePermission("system:dept:list")
        @OperationLog(module = "部门管理", action = "变更部门状态")
        ApiResponse<Void> deptStatus(@PathVariable long id, @RequestBody Map<String, Object> body) {
            adminService.updateStatus("sys_dept", id, intValue(body, "status", 1), true);
            return ApiResponse.success(null);
        }

        @GetMapping("/dicts/types")
        @RequirePermission("system:dict:list")
        ApiResponse<PageResult<Map<String, Object>>> dictTypes(@RequestParam Map<String, String> q) {
            return ApiResponse.success(adminService.page("sys_dict_type", q, List.of("dict_name", "dict_code", "status")));
        }

        @PostMapping("/dicts/types")
        @RequirePermission("system:dict:list")
        @OperationLog(module = "字典管理", action = "新增字典类型")
        ApiResponse<Long> createDictType(@RequestBody Map<String, Object> body) {
            return ApiResponse.success(adminService.insert("sys_dict_type", body, Set.of("dict_name", "dict_code", "status", "remark")));
        }

        @PutMapping("/dicts/types/{id}")
        @RequirePermission("system:dict:list")
        @OperationLog(module = "字典管理", action = "编辑字典类型")
        ApiResponse<Void> updateDictType(@PathVariable long id, @RequestBody Map<String, Object> body) {
            adminService.update("sys_dict_type", id, body, Set.of("dict_name", "dict_code", "status", "remark"));
            adminService.refreshDictCache();
            return ApiResponse.success(null);
        }

        @DeleteMapping("/dicts/types/{id}")
        @RequirePermission("system:dict:list")
        @OperationLog(module = "字典管理", action = "删除字典类型")
        ApiResponse<Void> deleteDictType(@PathVariable long id) {
            adminService.softDelete("sys_dict_type", id);
            adminService.refreshDictCache();
            return ApiResponse.success(null);
        }

        @GetMapping("/dicts/types/{id}/items")
        @RequirePermission("system:dict:list")
        ApiResponse<List<Map<String, Object>>> dictItems(@PathVariable long id) {
            return ApiResponse.success(adminService.dictItems(id));
        }

        @PostMapping("/dicts/items")
        @RequirePermission("system:dict:list")
        @OperationLog(module = "字典管理", action = "新增字典项")
        ApiResponse<Long> createDictItem(@RequestBody Map<String, Object> body) {
            Long id = adminService.insert("sys_dict_item", body, Set.of("dict_type_id", "label", "value", "color", "sort", "status"));
            adminService.refreshDictCache();
            return ApiResponse.success(id);
        }

        @PutMapping("/dicts/items/{id}")
        @RequirePermission("system:dict:list")
        @OperationLog(module = "字典管理", action = "编辑字典项")
        ApiResponse<Void> updateDictItem(@PathVariable long id, @RequestBody Map<String, Object> body) {
            adminService.update("sys_dict_item", id, body, Set.of("dict_type_id", "label", "value", "color", "sort", "status"));
            adminService.refreshDictCache();
            return ApiResponse.success(null);
        }

        @DeleteMapping("/dicts/items/{id}")
        @RequirePermission("system:dict:list")
        @OperationLog(module = "字典管理", action = "删除字典项")
        ApiResponse<Void> deleteDictItem(@PathVariable long id) {
            adminService.softDelete("sys_dict_item", id);
            adminService.refreshDictCache();
            return ApiResponse.success(null);
        }

        @PatchMapping("/dicts/items/{id}/status")
        @RequirePermission("system:dict:list")
        @OperationLog(module = "字典管理", action = "变更字典项状态")
        ApiResponse<Void> dictItemStatus(@PathVariable long id, @RequestBody Map<String, Object> body) {
            adminService.updateStatus("sys_dict_item", id, intValue(body, "status", 1), true);
            adminService.refreshDictCache();
            return ApiResponse.success(null);
        }

        @PostMapping("/dicts/cache/refresh")
        @RequirePermission("system:dict:list")
        ApiResponse<Void> refreshDictCache() {
            adminService.refreshDictCache();
            return ApiResponse.success(null);
        }

        @GetMapping("/configs")
        @RequirePermission("system:config:list")
        ApiResponse<PageResult<Map<String, Object>>> configs(@RequestParam Map<String, String> q) {
            return ApiResponse.success(adminService.configs(q));
        }

        @PostMapping("/configs")
        @RequirePermission("system:config:list")
        @OperationLog(module = "系统配置", action = "新增配置")
        ApiResponse<Long> createConfig(@RequestBody Map<String, Object> body) {
            return ApiResponse.success(adminService.insert("sys_config", body, Set.of("config_name", "config_key", "config_value", "group_code", "is_sensitive", "builtin", "status", "remark")));
        }

        @PutMapping("/configs/{id}")
        @RequirePermission("system:config:list")
        @OperationLog(module = "系统配置", action = "编辑配置")
        ApiResponse<Void> updateConfig(@PathVariable long id, @RequestBody Map<String, Object> body) {
            adminService.update("sys_config", id, body, Set.of("config_name", "config_key", "config_value", "group_code", "is_sensitive", "status", "remark"));
            return ApiResponse.success(null);
        }

        @DeleteMapping("/configs/{id}")
        @RequirePermission("system:config:list")
        @OperationLog(module = "系统配置", action = "删除配置")
        ApiResponse<Void> deleteConfig(@PathVariable long id) {
            adminService.deleteConfig(id);
            return ApiResponse.success(null);
        }

        @PatchMapping("/configs/{id}/status")
        @RequirePermission("system:config:list")
        @OperationLog(module = "系统配置", action = "变更配置状态")
        ApiResponse<Void> configStatus(@PathVariable long id, @RequestBody Map<String, Object> body) {
            adminService.updateStatus("sys_config", id, intValue(body, "status", 1), true);
            return ApiResponse.success(null);
        }

        @GetMapping("/login-logs")
        @RequirePermission("system:login-log:list")
        ApiResponse<PageResult<Map<String, Object>>> loginLogs(@RequestParam Map<String, String> q) {
            return ApiResponse.success(adminService.pageReadonly("sys_login_log", q, "login_at"));
        }

        @GetMapping("/login-logs/{id}")
        @RequirePermission("system:login-log:list")
        ApiResponse<Map<String, Object>> loginLog(@PathVariable long id) {
            return ApiResponse.success(adminService.detail("sys_login_log", id));
        }

        @GetMapping("/operation-logs")
        @RequirePermission("system:operation-log:list")
        ApiResponse<PageResult<Map<String, Object>>> operationLogs(@RequestParam Map<String, String> q) {
            return ApiResponse.success(adminService.pageReadonly("sys_operation_log", q, "created_at"));
        }

        @GetMapping("/operation-logs/{id}")
        @RequirePermission("system:operation-log:list")
        ApiResponse<Map<String, Object>> operationLog(@PathVariable long id) {
            return ApiResponse.success(adminService.detail("sys_operation_log", id));
        }

        @GetMapping("/online-users")
        @RequirePermission("system:online:list")
        ApiResponse<PageResult<Map<String, Object>>> onlineUsers(@RequestParam Map<String, String> q) {
            return ApiResponse.success(adminService.onlineUsers(q));
        }

        @GetMapping("/online-users/{tokenId}")
        @RequirePermission("system:online:list")
        ApiResponse<Map<String, Object>> onlineUser(@PathVariable String tokenId) {
            return ApiResponse.success(adminService.onlineUser(tokenId));
        }

        @PostMapping("/online-users/{tokenId}/kickout")
        @RequirePermission("system:online:list")
        @OperationLog(module = "在线用户", action = "强制下线")
        ApiResponse<Void> kickout(@PathVariable String tokenId) {
            adminService.kickout(tokenId);
            return ApiResponse.success(null);
        }

        @GetMapping("/jobs")
        @RequirePermission("system:job:list")
        ApiResponse<PageResult<Map<String, Object>>> jobs(@RequestParam Map<String, String> q) {
            return ApiResponse.success(adminService.page("sys_job", q, List.of("job_name", "job_code", "status", "created_at")));
        }

        @GetMapping("/jobs/{id}")
        @RequirePermission("system:job:list")
        ApiResponse<Map<String, Object>> job(@PathVariable long id) {
            return ApiResponse.success(adminService.detail("sys_job", id));
        }

        @PostMapping("/jobs")
        @RequirePermission("system:job:list")
        @OperationLog(module = "定时任务", action = "新增任务")
        ApiResponse<Long> createJob(@RequestBody Map<String, Object> body) {
            adminService.validateCron(stringValue(body, "cron_expression", ""));
            return ApiResponse.success(adminService.insert("sys_job", body, Set.of("job_name", "job_code", "cron_expression", "bean_name", "method_name", "params", "status", "remark")));
        }

        @PutMapping("/jobs/{id}")
        @RequirePermission("system:job:list")
        @OperationLog(module = "定时任务", action = "编辑任务")
        ApiResponse<Void> updateJob(@PathVariable long id, @RequestBody Map<String, Object> body) {
            if (body.containsKey("cron_expression")) adminService.validateCron(String.valueOf(body.get("cron_expression")));
            adminService.update("sys_job", id, body, Set.of("job_name", "job_code", "cron_expression", "bean_name", "method_name", "params", "status", "remark"));
            return ApiResponse.success(null);
        }

        @DeleteMapping("/jobs/{id}")
        @RequirePermission("system:job:list")
        @OperationLog(module = "定时任务", action = "删除任务")
        ApiResponse<Void> deleteJob(@PathVariable long id) {
            adminService.softDelete("sys_job", id);
            return ApiResponse.success(null);
        }

        @PatchMapping("/jobs/{id}/status")
        @RequirePermission("system:job:list")
        @OperationLog(module = "定时任务", action = "变更任务状态")
        ApiResponse<Void> jobStatus(@PathVariable long id, @RequestBody Map<String, Object> body) {
            adminService.updateStatus("sys_job", id, intValue(body, "status", 1), true);
            return ApiResponse.success(null);
        }

        @PostMapping("/jobs/{id}/run")
        @RequirePermission("system:job:list")
        @OperationLog(module = "定时任务", action = "手动执行任务")
        ApiResponse<Void> runJob(@PathVariable long id) {
            adminService.runJob(id);
            return ApiResponse.success(null);
        }

        @GetMapping("/jobs/{id}/run-logs")
        @RequirePermission("system:job:list")
        ApiResponse<PageResult<Map<String, Object>>> jobLogs(@PathVariable long id, @RequestParam Map<String, String> q) {
            return ApiResponse.success(adminService.jobLogs(id, q));
        }

        @GetMapping("/database/backups")
        @RequirePermission("system:database:backup:list")
        ApiResponse<PageResult<Map<String, Object>>> backups(@RequestParam Map<String, String> q) {
            return ApiResponse.success(adminService.pageReadonly("sys_db_backup", q, "created_at"));
        }

        @PostMapping("/database/backups")
        @RequirePermission("system:database:backup:create")
        @OperationLog(module = "数据库备份", action = "创建备份")
        ApiResponse<Long> createBackup(@RequestBody(required = false) Map<String, Object> body) {
            return ApiResponse.success(adminService.createBackup(Optional.ofNullable(body).orElseGet(Map::of), currentUserId()));
        }

        @GetMapping(value = "/database/backups/{id}/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
        @RequirePermission("system:database:backup:download")
        ResponseEntity<byte[]> downloadBackup(@PathVariable long id) throws IOException {
            BackupFile file = adminService.downloadBackup(id);
            return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + file.name() + "\"")
                .body(file.content());
        }

        @PostMapping("/database/backups/{id}/restore")
        @RequirePermission("system:database:backup:restore")
        @OperationLog(module = "数据库备份", action = "恢复备份")
        ApiResponse<Void> restoreBackup(@PathVariable long id, @RequestBody Map<String, Object> body) {
            adminService.restoreBackup(id, body);
            return ApiResponse.success(null);
        }

        @DeleteMapping("/database/backups/{id}")
        @RequirePermission("system:database:backup:create")
        @OperationLog(module = "数据库备份", action = "删除备份记录")
        ApiResponse<Void> deleteBackup(@PathVariable long id) {
            adminService.deleteBackup(id);
            return ApiResponse.success(null);
        }

        @PostMapping("/files")
        @OperationLog(module = "文件上传", action = "上传文件")
        ApiResponse<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) throws IOException {
            return ApiResponse.success(adminService.upload(file));
        }
    }

    @org.springframework.stereotype.Service
    static class AuthService {
        private final JdbcTemplate jdbc;
        private final LogService logService;
        private final AdminService adminService;
        private final long idleTimeout;
        private final long maxDuration;

        AuthService(JdbcTemplate jdbc, LogService logService, AdminService adminService,
                    @Value("${drip.session.idle-timeout-seconds}") long idleTimeout,
                    @Value("${drip.session.max-duration-seconds}") long maxDuration) {
            this.jdbc = jdbc;
            this.logService = logService;
            this.adminService = adminService;
            this.idleTimeout = idleTimeout;
            this.maxDuration = maxDuration;
        }

        @Transactional
        Map<String, Object> login(LoginRequest request, HttpServletRequest servletRequest) {
            Map<String, Object> user = findUserByUsername(request.username());
            if (user.isEmpty()) {
                logService.login(null, request.username(), null, "LOGIN", "FAIL", "用户名或密码错误", servletRequest, request.deviceType());
                throw new BusinessException(401000, "用户名或密码错误");
            }
            if (intOf(user.get("status")) != 1 || intOf(user.get("deleted")) == 1) {
                logService.login(longOf(user.get("id")), request.username(), stringOf(user.get("real_name")), "LOGIN", "FAIL", "用户已禁用或删除", servletRequest, request.deviceType());
                throw new BusinessException(401000, "用户名或密码错误");
            }
            String expected = hashPassword(request.password(), stringOf(user.get("password_salt")));
            if (!expected.equals(stringOf(user.get("password_hash")))) {
                logService.login(longOf(user.get("id")), request.username(), stringOf(user.get("real_name")), "LOGIN", "FAIL", "用户名或密码错误", servletRequest, request.deviceType());
                throw new BusinessException(401000, "用户名或密码错误");
            }
            Long userId = longOf(user.get("id"));
            StpUtil.login(userId);
            String token = StpUtil.getTokenValue();
            LocalDateTime now = LocalDateTime.now();
            StpUtil.getSession().set("deviceType", request.deviceType());
            StpUtil.getSession().set("loginAt", now.toString());
            StpUtil.getSession().set("lastActiveAt", now.toString());
            StpUtil.getSession().set("tokenId", token);
            jdbc.update("update sys_user set last_login_at = now() where id = ?", userId);
            logService.login(userId, request.username(), stringOf(user.get("real_name")), "LOGIN", "SUCCESS", null, servletRequest, request.deviceType());
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("token", token);
            data.put("expireAt", now.plusSeconds(idleTimeout).atZone(ZoneId.systemDefault()).toInstant().toString());
            data.put("idleTimeout", idleTimeout);
            data.put("maxSessionDuration", maxDuration);
            data.put("deviceType", request.deviceType());
            return data;
        }

        @Transactional
        void logout(HttpServletRequest request) {
            Long userId = currentUserId();
            Map<String, Object> user = adminService.detail("sys_user", userId);
            String deviceType = String.valueOf(StpUtil.getSession().get("deviceType", ""));
            logService.login(userId, stringOf(user.get("username")), stringOf(user.get("real_name")), "LOGOUT", "SUCCESS", null, request, deviceType);
            StpUtil.logout();
        }

        Map<String, Object> me(long userId) {
            Map<String, Object> user = adminService.detail("sys_user", userId);
            List<String> roles = adminService.roleCodes(userId);
            List<String> permissions = adminService.permissionCodes(userId);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("userId", user.get("id"));
            result.put("username", user.get("username"));
            result.put("realName", user.get("real_name"));
            result.put("avatar", user.get("avatar"));
            result.put("deptId", user.get("dept_id"));
            result.put("roles", roles);
            result.put("permissions", permissions);
            result.put("menus", adminService.menuTree(userId));
            return result;
        }

        @Transactional
        void changePassword(long userId, PasswordRequest request) {
            Map<String, Object> user = adminService.detail("sys_user", userId);
            String currentHash = hashPassword(request.oldPassword(), stringOf(user.get("password_salt")));
            if (!currentHash.equals(stringOf(user.get("password_hash")))) {
                throw new BusinessException(400000, "原密码错误");
            }
            String salt = "salt" + System.nanoTime();
            jdbc.update("update sys_user set password_salt = ?, password_hash = ? where id = ?", salt, hashPassword(request.newPassword(), salt), userId);
        }

        private Map<String, Object> findUserByUsername(String username) {
            List<Map<String, Object>> rows = jdbc.queryForList("select * from sys_user where username = ? and deleted = 0", username);
            return rows.isEmpty() ? Map.of() : rows.getFirst();
        }
    }

    @org.springframework.stereotype.Service
    static class AdminService {
        private final JdbcTemplate jdbc;
        private final Map<String, List<Map<String, Object>>> dictCache = new HashMap<>();
        private final long maxUploadSize;
        private final List<String> allowedUploadTypes;
        private final Path backupDir;

        AdminService(JdbcTemplate jdbc,
                     @Value("${drip.upload.max-size-bytes}") long maxUploadSize,
                     @Value("${drip.upload.allowed-types}") String allowedUploadTypes,
                     @Value("${drip.database.backup-dir}") String backupDir) {
            this.jdbc = jdbc;
            this.maxUploadSize = maxUploadSize;
            this.allowedUploadTypes = Arrays.stream(allowedUploadTypes.split(",")).map(String::trim).filter(v -> !v.isBlank()).toList();
            this.backupDir = Path.of(backupDir);
        }

        PageResult<Map<String, Object>> page(String table, Map<String, String> q, List<String> allowedFilters) {
            return pageInternal(table, q, "created_at", allowedFilters, true);
        }

        PageResult<Map<String, Object>> pageReadonly(String table, Map<String, String> q, String orderColumn) {
            return pageInternal(table, q, orderColumn, List.of(), false);
        }

        PageResult<Map<String, Object>> configs(Map<String, String> q) {
            PageResult<Map<String, Object>> page = page("sys_config", q, List.of("config_name", "config_key", "group_code", "status"));
            page.list().forEach(this::maskSensitiveConfig);
            return page;
        }

        private PageResult<Map<String, Object>> pageInternal(String table, Map<String, String> q, String orderColumn, List<String> allowedFilters, boolean softDelete) {
            TableMeta.require(table);
            int page = Math.max(1, parseInt(q.get("page"), 1));
            int pageSize = Math.min(100, Math.max(1, parseInt(q.get("pageSize"), 20)));
            List<Object> args = new ArrayList<>();
            StringBuilder where = new StringBuilder(" where 1 = 1");
            if (softDelete) where.append(" and deleted = 0");
            for (String filter : allowedFilters) {
                String camel = snakeToCamel(filter);
                String value = q.getOrDefault(filter, q.get(camel));
                if (value != null && !value.isBlank()) {
                    where.append(" and ").append(filter).append(" like ?");
                    args.add("%" + value + "%");
                }
            }
            Long total = jdbc.queryForObject("select count(1) from " + table + where, Long.class, args.toArray());
            List<Object> listArgs = new ArrayList<>(args);
            listArgs.add((page - 1) * pageSize);
            listArgs.add(pageSize);
            List<Map<String, Object>> rows = jdbc.queryForList("select * from " + table + where + " order by " + orderColumn + " desc limit ?, ?", listArgs.toArray());
            return new PageResult<>(rows, total == null ? 0 : total, page, pageSize);
        }

        Map<String, Object> detail(String table, long id) {
            TableMeta.require(table);
            String deleted = TableMeta.softDelete(table) ? " and deleted = 0" : "";
            List<Map<String, Object>> rows = jdbc.queryForList("select * from " + table + " where id = ?" + deleted, id);
            if (rows.isEmpty()) throw new BusinessException(404000, "资源不存在");
            Map<String, Object> row = new LinkedHashMap<>(rows.getFirst());
            if ("sys_config".equals(table)) maskSensitiveConfig(row);
            return row;
        }

        @Transactional
        Long createUser(Map<String, Object> body) {
            requireNonBlank(body, "username");
            requireNonBlank(body, "real_name", "realName");
            String password = stringValue(body, "password", "Admin@123456");
            String salt = "salt" + System.nanoTime();
            body.put("password_salt", salt);
            body.put("password_hash", hashPassword(password, salt));
            body.putIfAbsent("status", 1);
            return insert("sys_user", body, Set.of("username", "password_hash", "password_salt", "real_name", "phone", "email", "status", "dept_id", "remark"));
        }

        @Transactional
        void updateUser(long id, Map<String, Object> body) {
            assertNotSuperAdminTarget(id);
            update("sys_user", id, body, Set.of("username", "real_name", "phone", "email", "status", "dept_id", "remark"));
        }

        @Transactional
        void deleteUser(long currentUserId, long id) {
            if (currentUserId == id) throw new BusinessException(400000, "不能删除当前登录用户");
            assertNotSuperAdminTarget(id);
            softDelete("sys_user", id);
        }

        @Transactional
        void resetPassword(long id, String password) {
            assertNotSuperAdminTarget(id);
            String salt = "salt" + System.nanoTime();
            jdbc.update("update sys_user set password_salt = ?, password_hash = ? where id = ? and deleted = 0", salt, hashPassword(password, salt), id);
        }

        @Transactional
        void assignUserRoles(long userId, List<Long> roleIds) {
            assertNotSuperAdminTarget(userId);
            jdbc.update("delete from sys_user_role where user_id = ?", userId);
            for (Long roleId : roleIds) jdbc.update("insert into sys_user_role (user_id, role_id) values (?, ?)", userId, roleId);
        }

        @Transactional
        void deleteRole(long id) {
            Map<String, Object> role = detail("sys_role", id);
            if (intOf(role.get("builtin")) == 1) throw new BusinessException(400000, "内置角色禁止删除");
            Long count = jdbc.queryForObject("select count(1) from sys_user_role where role_id = ?", Long.class, id);
            if (count != null && count > 0) throw new BusinessException(409000, "角色已分配用户，不能删除");
            softDelete("sys_role", id);
        }

        @Transactional
        void assignRoleMenus(long roleId, List<Long> menuIds) {
            detail("sys_role", roleId);
            jdbc.update("delete from sys_role_menu where role_id = ?", roleId);
            for (Long menuId : menuIds) jdbc.update("insert into sys_role_menu (role_id, menu_id) values (?, ?)", roleId, menuId);
        }

        @Transactional
        void deleteMenu(long id) {
            Long count = jdbc.queryForObject("select count(1) from sys_menu where parent_id = ? and deleted = 0", Long.class, id);
            if (count != null && count > 0) throw new BusinessException(400301, "菜单存在子节点，不能删除");
            softDelete("sys_menu", id);
        }

        @Transactional
        void updateDept(long id, Map<String, Object> body) {
            if (body.containsKey("parent_id")) {
                long parentId = longOf(body.get("parent_id"));
                if (parentId == id || descendantDeptIds(id).contains(parentId)) {
                    throw new BusinessException(400000, "不能把部门移动到自己的子部门下");
                }
            }
            update("sys_dept", id, body, Set.of("parent_id", "dept_name", "dept_code", "leader_user_id", "sort", "status"));
        }

        @Transactional
        void deleteDept(long id) {
            Long childCount = jdbc.queryForObject("select count(1) from sys_dept where parent_id = ? and deleted = 0", Long.class, id);
            if (childCount != null && childCount > 0) throw new BusinessException(400401, "部门存在子节点，不能删除");
            Long userCount = jdbc.queryForObject("select count(1) from sys_user where dept_id = ? and deleted = 0", Long.class, id);
            if (userCount != null && userCount > 0) throw new BusinessException(400401, "部门存在用户，不能删除");
            softDelete("sys_dept", id);
        }

        @Transactional
        void deleteConfig(long id) {
            Map<String, Object> config = detail("sys_config", id);
            if (intOf(config.get("builtin")) == 1) throw new BusinessException(400000, "内置配置禁止删除");
            softDelete("sys_config", id);
        }

        @Transactional
        Long insert(String table, Map<String, Object> body, Set<String> allowed) {
            TableMeta.require(table);
            LinkedHashMap<String, Object> values = columns(body, allowed);
            if (values.isEmpty()) throw new BusinessException(400000, "请求参数错误");
            String cols = String.join(", ", values.keySet());
            String placeholders = values.keySet().stream().map(k -> "?").collect(Collectors.joining(", "));
            jdbc.update("insert into " + table + " (" + cols + ") values (" + placeholders + ")", values.values().toArray());
            return jdbc.queryForObject("select last_insert_id()", Long.class);
        }

        @Transactional
        void update(String table, long id, Map<String, Object> body, Set<String> allowed) {
            TableMeta.require(table);
            detail(table, id);
            LinkedHashMap<String, Object> values = columns(body, allowed);
            if (values.isEmpty()) return;
            String set = values.keySet().stream().map(k -> k + " = ?").collect(Collectors.joining(", "));
            List<Object> args = new ArrayList<>(values.values());
            args.add(id);
            jdbc.update("update " + table + " set " + set + " where id = ?", args.toArray());
        }

        @Transactional
        void updateStatus(String table, long id, int status, boolean guardSuper) {
            if (guardSuper && "sys_user".equals(table)) assertNotSuperAdminTarget(id);
            detail(table, id);
            jdbc.update("update " + table + " set status = ? where id = ?", status, id);
        }

        @Transactional
        void softDelete(String table, long id) {
            detail(table, id);
            jdbc.update("update " + table + " set deleted = 1 where id = ?", id);
        }

        List<String> roleCodes(long userId) {
            return jdbc.queryForList("""
                select r.role_code
                from sys_role r
                join sys_user_role ur on ur.role_id = r.id
                where ur.user_id = ? and r.deleted = 0 and r.status = 1
                """, String.class, userId);
        }

        List<String> permissionCodes(long userId) {
            if (roleCodes(userId).contains("SUPER_ADMIN")) {
                return jdbc.queryForList("select permission_code from sys_menu where deleted = 0 and status = 1 and permission_code is not null", String.class);
            }
            return jdbc.queryForList("""
                select distinct m.permission_code
                from sys_menu m
                join sys_role_menu rm on rm.menu_id = m.id
                join sys_user_role ur on ur.role_id = rm.role_id
                join sys_role r on r.id = ur.role_id
                where ur.user_id = ? and m.deleted = 0 and m.status = 1 and r.deleted = 0 and r.status = 1 and m.permission_code is not null
                """, String.class, userId);
        }

        boolean hasPermission(long userId, String permission) {
            return roleCodes(userId).contains("SUPER_ADMIN") || permissionCodes(userId).contains(permission);
        }

        List<Map<String, Object>> menuTree(Long userId) {
            List<Map<String, Object>> rows;
            if (userId == null || roleCodes(userId).contains("SUPER_ADMIN")) {
                rows = jdbc.queryForList("select id, parent_id, name, type, path, component, permission_code, icon, sort, visible from sys_menu where deleted = 0 and status = 1 order by sort asc, id asc");
            } else {
                rows = jdbc.queryForList("""
                    select distinct m.id, m.parent_id, m.name, m.type, m.path, m.component, m.permission_code, m.icon, m.sort, m.visible
                    from sys_menu m
                    join sys_role_menu rm on rm.menu_id = m.id
                    join sys_user_role ur on ur.role_id = rm.role_id
                    where ur.user_id = ? and m.deleted = 0 and m.status = 1
                    order by m.sort asc, m.id asc
                    """, userId);
            }
            return buildTree(rows.stream().filter(r -> !"BUTTON".equals(r.get("type"))).map(LinkedHashMap::new).collect(Collectors.toList()), "parent_id");
        }

        List<Map<String, Object>> tree(String table, String parentColumn, String orderColumn) {
            TableMeta.require(table);
            List<Map<String, Object>> rows = jdbc.queryForList("select * from " + table + " where deleted = 0 order by " + orderColumn + " asc, id asc");
            return buildTree(rows.stream().map(LinkedHashMap::new).collect(Collectors.toList()), parentColumn);
        }

        List<Map<String, Object>> dictItems(long dictTypeId) {
            return jdbc.queryForList("select * from sys_dict_item where dict_type_id = ? and deleted = 0 order by sort asc, id asc", dictTypeId);
        }

        void refreshDictCache() {
            dictCache.clear();
            for (Map<String, Object> type : jdbc.queryForList("select * from sys_dict_type where deleted = 0 and status = 1")) {
                dictCache.put(stringOf(type.get("dict_code")), dictItems(longOf(type.get("id"))));
            }
        }

        PageResult<Map<String, Object>> onlineUsers(Map<String, String> q) {
            List<Map<String, Object>> rows = jdbc.queryForList("""
                select id userId, username, real_name realName, last_login_at loginAt
                from sys_user
                where deleted = 0 and last_login_at is not null
                order by last_login_at desc
                """);
            int page = Math.max(1, parseInt(q.get("page"), 1));
            int pageSize = Math.min(100, Math.max(1, parseInt(q.get("pageSize"), 20)));
            int from = Math.min(rows.size(), (page - 1) * pageSize);
            int to = Math.min(rows.size(), from + pageSize);
            return new PageResult<>(rows.subList(from, to), rows.size(), page, pageSize);
        }

        Map<String, Object> onlineUser(String tokenId) {
            return Map.of("tokenId", tokenId, "status", "ONLINE_STATUS_REQUIRES_SA_TOKEN_SESSION_LOOKUP");
        }

        void kickout(String tokenId) {
            if (Objects.equals(tokenId, StpUtil.getTokenValue())) throw new BusinessException(400000, "默认不允许强制下线当前登录用户");
            StpUtil.logoutByTokenValue(tokenId);
        }

        void validateCron(String cron) {
            if (cron == null || cron.isBlank() || cron.length() > 64 || cron.split("\\s+").length < 5) {
                throw new BusinessException(400000, "cronExpression 格式错误");
            }
        }

        @Transactional
        void runJob(long id) {
            Map<String, Object> job = detail("sys_job", id);
            LocalDateTime started = LocalDateTime.now();
            jdbc.update("insert into sys_job_run_log (job_id, job_name, status, started_at, finished_at, cost_ms) values (?, ?, 'SUCCESS', ?, ?, 0)",
                id, job.get("job_name"), started, LocalDateTime.now());
        }

        PageResult<Map<String, Object>> jobLogs(long jobId, Map<String, String> q) {
            int page = Math.max(1, parseInt(q.get("page"), 1));
            int pageSize = Math.min(100, Math.max(1, parseInt(q.get("pageSize"), 20)));
            Long total = jdbc.queryForObject("select count(1) from sys_job_run_log where job_id = ?", Long.class, jobId);
            List<Map<String, Object>> rows = jdbc.queryForList("select * from sys_job_run_log where job_id = ? order by started_at desc limit ?, ?", jobId, (page - 1) * pageSize, pageSize);
            return new PageResult<>(rows, total == null ? 0 : total, page, pageSize);
        }

        @Transactional
        Long createBackup(Map<String, Object> body, long userId) {
            try {
                Files.createDirectories(backupDir);
                String name = "backup-" + System.currentTimeMillis() + ".sql";
                Path target = backupDir.resolve(name).normalize();
                if (!target.startsWith(backupDir.normalize())) throw new BusinessException(400000, "备份路径非法");
                Files.writeString(target, "-- backup placeholder generated by backend service\n", StandardCharsets.UTF_8);
                return insert("sys_db_backup", Map.of(
                    "backup_name", name,
                    "file_path", target.toString(),
                    "file_size", Files.size(target),
                    "status", "SUCCESS",
                    "created_by", userId,
                    "remark", stringValue(body, "remark", "")
                ), Set.of("backup_name", "file_path", "file_size", "status", "created_by", "remark"));
            } catch (IOException ex) {
                throw new BusinessException(500000, "创建数据库备份失败");
            }
        }

        BackupFile downloadBackup(long id) throws IOException {
            Map<String, Object> backup = detail("sys_db_backup", id);
            Path path = Path.of(stringOf(backup.get("file_path"))).normalize();
            if (!Files.exists(path)) throw new BusinessException(404000, "备份文件不存在");
            return new BackupFile(stringOf(backup.get("backup_name")), Files.readAllBytes(path));
        }

        void restoreBackup(long id, Map<String, Object> body) {
            detail("sys_db_backup", id);
            if (!Boolean.TRUE.equals(body.get("confirmed"))) throw new BusinessException(400000, "恢复数据库备份需要二次确认");
        }

        void deleteBackup(long id) {
            jdbc.update("delete from sys_db_backup where id = ?", id);
        }

        Map<String, Object> upload(MultipartFile file) throws IOException {
            if (file.isEmpty()) throw new BusinessException(400000, "文件不能为空");
            if (file.getSize() > maxUploadSize) throw new BusinessException(400000, "文件超过大小限制");
            if (!allowedUploadTypes.contains(file.getContentType())) throw new BusinessException(400000, "文件类型不允许");
            return Map.of("fileId", "local-" + System.currentTimeMillis(), "url", "", "fileName", file.getOriginalFilename(), "size", file.getSize());
        }

        private void assertNotSuperAdminTarget(long userId) {
            if (roleCodes(currentUserId()).contains("SUPER_ADMIN")) return;
            if (roleCodes(userId).contains("SUPER_ADMIN")) throw new BusinessException(403000, "普通管理员不能操作超级管理员");
        }

        private Set<Long> descendantDeptIds(long id) {
            Set<Long> result = new HashSet<>();
            collectDept(id, result);
            return result;
        }

        private void collectDept(long id, Set<Long> result) {
            List<Long> children = jdbc.queryForList("select id from sys_dept where parent_id = ? and deleted = 0", Long.class, id);
            for (Long child : children) {
                result.add(child);
                collectDept(child, result);
            }
        }

        private LinkedHashMap<String, Object> columns(Map<String, Object> body, Set<String> allowed) {
            LinkedHashMap<String, Object> values = new LinkedHashMap<>();
            for (String col : allowed) {
                String camel = snakeToCamel(col);
                if (body.containsKey(col)) values.put(col, body.get(col));
                else if (body.containsKey(camel)) values.put(col, body.get(camel));
                else if ("is_sensitive".equals(col) && body.containsKey("sensitive")) values.put(col, body.get("sensitive"));
            }
            return values;
        }

        private void maskSensitiveConfig(Map<String, Object> row) {
            if (intOf(row.get("is_sensitive")) == 1) row.put("config_value", "******");
        }
    }

    @org.springframework.stereotype.Service
    static class LogService {
        private final JdbcTemplate jdbc;

        LogService(JdbcTemplate jdbc) {
            this.jdbc = jdbc;
        }

        void login(Long userId, String username, String realName, String loginType, String status, String reason, HttpServletRequest request, String deviceType) {
            jdbc.update("""
                insert into sys_login_log (user_id, username, real_name, login_type, status, failure_reason, ip, user_agent, device_type)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, userId, username, realName, loginType, status, reason, clientIp(request), request.getHeader("User-Agent"), deviceType);
        }

        void operation(String module, String action, String method, String path, String requestParams, String responseStatus, String errorMessage, long costMs) {
            Long userId = null;
            String operatorName = null;
            if (StpUtil.isLogin()) {
                userId = currentUserId();
                operatorName = String.valueOf(userId);
            }
            jdbc.update("""
                insert into sys_operation_log (operator_id, operator_name, module, action, method, path, request_params, response_status, error_message, cost_ms)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, userId, operatorName, module, action, method, path, requestParams, responseStatus, errorMessage, costMs);
        }
    }

    @org.springframework.stereotype.Component
    static class SessionInterceptor implements HandlerInterceptor {
        private final long maxDuration;

        SessionInterceptor(@Value("${drip.session.max-duration-seconds}") long maxDuration) {
            this.maxDuration = maxDuration;
        }

        @Override
        public boolean preHandle(HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response, Object handler) {
            StpUtil.checkLogin();
            Object loginAtRaw = StpUtil.getSession().get("loginAt");
            if (loginAtRaw != null) {
                LocalDateTime loginAt = LocalDateTime.parse(String.valueOf(loginAtRaw));
                if (loginAt.plusSeconds(maxDuration).isBefore(LocalDateTime.now())) {
                    StpUtil.logout();
                    throw new BusinessException(401000, "会话已超过最大时长，请重新登录");
                }
            }
            StpUtil.getSession().set("lastActiveAt", LocalDateTime.now().toString());
            return true;
        }
    }

    @org.springframework.stereotype.Component
    static class PermissionProvider implements StpInterface {
        private final AdminService adminService;

        PermissionProvider(AdminService adminService) {
            this.adminService = adminService;
        }

        @Override
        public List<String> getPermissionList(Object loginId, String loginType) {
            return adminService.permissionCodes(Long.parseLong(String.valueOf(loginId)));
        }

        @Override
        public List<String> getRoleList(Object loginId, String loginType) {
            return adminService.roleCodes(Long.parseLong(String.valueOf(loginId)));
        }
    }

    @Aspect
    @org.springframework.stereotype.Component
    static class PermissionAspect {
        @Around("@annotation(requirePermission)")
        Object check(ProceedingJoinPoint point, RequirePermission requirePermission) throws Throwable {
            StpUtil.checkPermission(requirePermission.value());
            return point.proceed();
        }
    }

    @Aspect
    @org.springframework.stereotype.Component
    static class OperationLogAspect {
        private final LogService logService;

        OperationLogAspect(LogService logService) {
            this.logService = logService;
        }

        @Around("@annotation(operationLog)")
        Object write(ProceedingJoinPoint point, OperationLog operationLog) throws Throwable {
            long started = System.currentTimeMillis();
            HttpServletRequest request = currentRequest();
            String params = maskSensitive(Arrays.toString(point.getArgs()));
            try {
                Object result = point.proceed();
                safeLog(operationLog, request, params, "SUCCESS", null, System.currentTimeMillis() - started);
                return result;
            } catch (Throwable ex) {
                safeLog(operationLog, request, params, "FAIL", ex.getMessage(), System.currentTimeMillis() - started);
                throw ex;
            }
        }

        private void safeLog(OperationLog operationLog, HttpServletRequest request, String params, String status, String errorMessage, long costMs) {
            try {
                logService.operation(operationLog.module(), operationLog.action(), request.getMethod(), request.getRequestURI(), params, status, errorMessage, costMs);
            } catch (Exception ignored) {
                // Business operation logging must not break the primary business flow.
            }
        }
    }

    @RestControllerAdvice
    static class GlobalExceptionHandler {
        @ExceptionHandler(MethodArgumentNotValidException.class)
        ResponseEntity<ApiResponse<Void>> validation(MethodArgumentNotValidException ex) {
            String message = ex.getBindingResult().getFieldErrors().stream().findFirst().map(FieldError::getDefaultMessage).orElse("请求参数错误");
            return ResponseEntity.badRequest().body(ApiResponse.fail(400000, message));
        }

        @ExceptionHandler(NotLoginException.class)
        ResponseEntity<ApiResponse<Void>> notLogin(NotLoginException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail(401000, "未登录或 token 失效"));
        }

        @ExceptionHandler(NotPermissionException.class)
        ResponseEntity<ApiResponse<Void>> noPermission(NotPermissionException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.fail(403000, "无权限"));
        }

        @ExceptionHandler(BusinessException.class)
        ResponseEntity<ApiResponse<Void>> business(BusinessException ex) {
            HttpStatus status = ex.code == 401000 ? HttpStatus.UNAUTHORIZED : ex.code == 403000 ? HttpStatus.FORBIDDEN : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(ApiResponse.fail(ex.code, ex.getMessage()));
        }

        @ExceptionHandler(DuplicateKeyException.class)
        ResponseEntity<ApiResponse<Void>> duplicate(DuplicateKeyException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.fail(409000, "数据冲突"));
        }

        @ExceptionHandler(Exception.class)
        ResponseEntity<ApiResponse<Void>> system(Exception ex) {
            log.error("Unhandled system exception", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail(500000, "系统内部错误"));
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface RequirePermission {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface OperationLog {
        String module();
        String action();
    }

    record LoginRequest(
        @NotBlank(message = "用户名不能为空") String username,
        @NotBlank(message = "密码不能为空") String password,
        @NotBlank(message = "deviceType 不能为空")
        @Size(max = 64, message = "deviceType 长度不能超过 64")
        @Pattern(regexp = "^[A-Za-z0-9_.:-]+$", message = "deviceType 包含非法字符") String deviceType
    ) {
    }

    record PasswordRequest(
        @NotBlank(message = "原密码不能为空") String oldPassword,
        @NotBlank(message = "新密码不能为空") @Size(min = 8, max = 64, message = "新密码长度必须为 8 到 64") String newPassword
    ) {
    }

    record ApiResponse<T>(int code, String message, T data) {
        static <T> ApiResponse<T> success(T data) {
            return new ApiResponse<>(0, "success", data);
        }

        static ApiResponse<Void> fail(int code, String message) {
            return new ApiResponse<>(code, message, null);
        }
    }

    record PageResult<T>(List<T> list, long total, int page, int pageSize) {
    }

    record BackupFile(String name, byte[] content) {
    }

    static class BusinessException extends RuntimeException {
        final int code;

        BusinessException(int code, String message) {
            super(message);
            this.code = code;
        }
    }

    enum TableMeta {
        SYS_USER("sys_user", true),
        SYS_ROLE("sys_role", true),
        SYS_MENU("sys_menu", true),
        SYS_DEPT("sys_dept", true),
        SYS_DICT_TYPE("sys_dict_type", true),
        SYS_DICT_ITEM("sys_dict_item", true),
        SYS_CONFIG("sys_config", true),
        SYS_JOB("sys_job", true),
        SYS_LOGIN_LOG("sys_login_log", false),
        SYS_OPERATION_LOG("sys_operation_log", false),
        SYS_JOB_RUN_LOG("sys_job_run_log", false),
        SYS_DB_BACKUP("sys_db_backup", false);

        final String table;
        final boolean softDelete;

        TableMeta(String table, boolean softDelete) {
            this.table = table;
            this.softDelete = softDelete;
        }

        static void require(String table) {
            Arrays.stream(values()).filter(v -> v.table.equals(table)).findFirst().orElseThrow(() -> new BusinessException(400000, "非法表名"));
        }

        static boolean softDelete(String table) {
            return Arrays.stream(values()).filter(v -> v.table.equals(table)).findFirst().map(v -> v.softDelete).orElse(false);
        }
    }

    @TableName("sys_user")
    static class SysUserEntity {
        @TableId
        public Long id;
        public String username;
        public String realName;
    }

    @Mapper
    interface SysUserMapper extends BaseMapper<SysUserEntity> {
    }

    static long currentUserId() {
        return StpUtil.getLoginIdAsLong();
    }

    static HttpServletRequest currentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attrs.getRequest();
    }

    static String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest((salt + ":" + password).getBytes(StandardCharsets.UTF_8));
            StringBuilder out = new StringBuilder();
            for (byte b : bytes) out.append(String.format("%02x", b));
            return out.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot hash password", ex);
        }
    }

    static List<Map<String, Object>> buildTree(List<LinkedHashMap<String, Object>> rows, String parentColumn) {
        Map<Long, LinkedHashMap<String, Object>> byId = new LinkedHashMap<>();
        for (LinkedHashMap<String, Object> row : rows) {
            row.put("children", new ArrayList<Map<String, Object>>());
            byId.put(longOf(row.get("id")), row);
        }
        List<Map<String, Object>> roots = new ArrayList<>();
        for (LinkedHashMap<String, Object> row : rows) {
            long parentId = longOf(row.get(parentColumn));
            if (parentId == 0 || !byId.containsKey(parentId)) {
                roots.add(row);
            } else {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> children = (List<Map<String, Object>>) byId.get(parentId).get("children");
                children.add(row);
            }
        }
        return roots;
    }

    static String maskSensitive(String value) {
        if (value == null) return null;
        return value.replaceAll("(?i)(password|token|secret)=[^,}\\]]+", "$1=******");
    }

    static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",")[0].trim();
        return request.getRemoteAddr();
    }

    static void requireNonBlank(Map<String, Object> body, String... names) {
        for (String name : names) {
            if (body.containsKey(name) && String.valueOf(body.get(name)).isBlank()) throw new BusinessException(400000, name + " 不能为空");
        }
    }

    static List<Long> longList(Object raw) {
        if (raw == null) return List.of();
        if (!(raw instanceof List<?> list)) throw new BusinessException(400000, "必须传 ID 数组");
        return list.stream().map(AdminApplication::longOf).toList();
    }

    static int parseInt(String value, int defaultValue) {
        if (value == null || value.isBlank()) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    static int intValue(Map<String, Object> body, String key, int defaultValue) {
        return body.containsKey(key) ? intOf(body.get(key)) : defaultValue;
    }

    static int intOf(Object value) {
        if (value instanceof Number n) return n.intValue();
        if (value == null) return 0;
        return Integer.parseInt(String.valueOf(value));
    }

    static long longOf(Object value) {
        if (value instanceof Number n) return n.longValue();
        if (value == null) return 0;
        return Long.parseLong(String.valueOf(value));
    }

    static String stringValue(Map<String, Object> body, String key, String defaultValue) {
        Object value = body.get(key);
        return value == null ? defaultValue : String.valueOf(value);
    }

    static String stringOf(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    static String snakeToCamel(String value) {
        StringBuilder out = new StringBuilder();
        boolean upper = false;
        for (char c : value.toCharArray()) {
            if (c == '_') {
                upper = true;
            } else if (upper) {
                out.append(Character.toUpperCase(c));
                upper = false;
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }
}
