package com.drip.admin.modules.system.service;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drip.admin.common.exception.BusinessException;
import com.drip.admin.common.log.OperationLog;
import com.drip.admin.common.log.LogService;
import com.drip.admin.common.response.ApiResponse;
import com.drip.admin.common.response.BackupFile;
import com.drip.admin.common.response.PageResult;
import com.drip.admin.common.security.RequirePermission;
import com.drip.admin.infrastructure.external.JobExecutorRegistry;
import com.drip.admin.infrastructure.redis.OnlineSessionService;
import com.drip.admin.modules.auth.dto.LoginRequest;
import com.drip.admin.modules.auth.dto.PasswordRequest;
import com.drip.admin.modules.auth.service.AuthService;
import com.drip.admin.modules.system.service.AdminService;
import com.drip.admin.shared.enums.TableMeta;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.apache.ibatis.annotations.Mapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.net.URI;
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
import java.util.*;
import java.util.stream.Collectors;

import static com.drip.admin.shared.utils.AdminUtils.*;

@Service
public class AdminService {
    private final JdbcTemplate jdbc;
    private final Map<String, List<Map<String, Object>>> dictCache = new HashMap<>();
    private final long maxUploadSize;
    private final List<String> allowedUploadTypes;
    private final Path backupDir;
    private final OnlineSessionService onlineSessionService;
    private final JobExecutorRegistry jobExecutorRegistry;
    private final String datasourceUrl;
    private final String datasourceUsername;
    private final String datasourcePassword;
    private final String mysqldumpCommand;
    private final String mysqlCommand;

    public AdminService(JdbcTemplate jdbc, OnlineSessionService onlineSessionService, JobExecutorRegistry jobExecutorRegistry,
                 @Value("${drip.upload.max-size-bytes}") long maxUploadSize,
                 @Value("${drip.upload.allowed-types}") String allowedUploadTypes,
                 @Value("${drip.database.backup-dir}") String backupDir,
                 @Value("${spring.datasource.url}") String datasourceUrl,
                 @Value("${spring.datasource.username}") String datasourceUsername,
                 @Value("${spring.datasource.password}") String datasourcePassword,
                 @Value("${drip.database.mysqldump-command:mysqldump}") String mysqldumpCommand,
                 @Value("${drip.database.mysql-command:mysql}") String mysqlCommand) {
        this.jdbc = jdbc;
        this.onlineSessionService = onlineSessionService;
        this.jobExecutorRegistry = jobExecutorRegistry;
        this.maxUploadSize = maxUploadSize;
        this.allowedUploadTypes = Arrays.stream(allowedUploadTypes.split(",")).map(String::trim).filter(v -> !v.isBlank()).toList();
        this.backupDir = Path.of(backupDir);
        this.datasourceUrl = datasourceUrl;
        this.datasourceUsername = datasourceUsername;
        this.datasourcePassword = datasourcePassword;
        this.mysqldumpCommand = mysqldumpCommand;
        this.mysqlCommand = mysqlCommand;
    }

   public  PageResult<Map<String, Object>> page(String table, Map<String, String> q, List<String> allowedFilters) {
    return pageInternal(table, q, "created_at", allowedFilters, true);
    }

   public  PageResult<Map<String, Object>> pageReadonly(String table, Map<String, String> q, String orderColumn) {
    return pageInternal(table, q, orderColumn, List.of(), false);
    }

   public  PageResult<Map<String, Object>> configs(Map<String, String> q) {
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

   public  Map<String, Object> detail(String table, long id) {
        TableMeta.require(table);
        String deleted = TableMeta.softDelete(table) ? " and deleted = 0" : "";
        List<Map<String, Object>> rows = jdbc.queryForList("select * from " + table + " where id = ?" + deleted, id);
    if (rows.isEmpty()) throw new BusinessException(404000, "资源不存在");
        Map<String, Object> row = new LinkedHashMap<>(rows.getFirst());
    if ("sys_config".equals(table)) maskSensitiveConfig(row);
        return row;
    }

    @Transactional
    public Long createUser(Map<String, Object> body) {
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
    public void updateUser(long id, Map<String, Object> body) {
    assertNotSuperAdminTarget(id);
    update("sys_user", id, body, Set.of("username", "real_name", "phone", "email", "status", "dept_id", "remark"));
    }

    @Transactional
    public void deleteUser(long currentUserId, long id) {
    if (currentUserId == id) throw new BusinessException(400000, "不能删除当前登录用户");
    assertNotSuperAdminTarget(id);
    softDelete("sys_user", id);
    }

    @Transactional
    public void resetPassword(long id, String password) {
    assertNotSuperAdminTarget(id);
        String salt = "salt" + System.nanoTime();
        jdbc.update("update sys_user set password_salt = ?, password_hash = ? where id = ? and deleted = 0", salt, hashPassword(password, salt), id);
    }

    @Transactional
    public void assignUserRoles(long userId, List<Long> roleIds) {
    assertNotSuperAdminTarget(userId);
        jdbc.update("delete from sys_user_role where user_id = ?", userId);
    for (Long roleId : roleIds) jdbc.update("insert into sys_user_role (user_id, role_id) values (?, ?)", userId, roleId);
    }

    @Transactional
    public void deleteRole(long id) {
        Map<String, Object> role = detail("sys_role", id);
    if (intOf(role.get("builtin")) == 1) throw new BusinessException(400000, "内置角色禁止删除");
        Long count = jdbc.queryForObject("select count(1) from sys_user_role where role_id = ?", Long.class, id);
    if (count != null && count > 0) throw new BusinessException(409000, "角色已分配用户，不能删除");
    softDelete("sys_role", id);
    }

    @Transactional
    public void assignRoleMenus(long roleId, List<Long> menuIds) {
    detail("sys_role", roleId);
        jdbc.update("delete from sys_role_menu where role_id = ?", roleId);
    for (Long menuId : menuIds) jdbc.update("insert into sys_role_menu (role_id, menu_id) values (?, ?)", roleId, menuId);
    }

    @Transactional
    public void deleteMenu(long id) {
        Long count = jdbc.queryForObject("select count(1) from sys_menu where parent_id = ? and deleted = 0", Long.class, id);
    if (count != null && count > 0) throw new BusinessException(400301, "菜单存在子节点，不能删除");
    softDelete("sys_menu", id);
    }

    @Transactional
    public void updateDept(long id, Map<String, Object> body) {
    if (body.containsKey("parent_id")) {
            long parentId = longOf(body.get("parent_id"));
    if (parentId == id || descendantDeptIds(id).contains(parentId)) {
    throw new BusinessException(400000, "不能把部门移动到自己的子部门下");
            }
        }
    update("sys_dept", id, body, Set.of("parent_id", "dept_name", "dept_code", "leader_user_id", "sort", "status"));
    }

    @Transactional
    public void deleteDept(long id) {
        Long childCount = jdbc.queryForObject("select count(1) from sys_dept where parent_id = ? and deleted = 0", Long.class, id);
    if (childCount != null && childCount > 0) throw new BusinessException(400401, "部门存在子节点，不能删除");
        Long userCount = jdbc.queryForObject("select count(1) from sys_user where dept_id = ? and deleted = 0", Long.class, id);
    if (userCount != null && userCount > 0) throw new BusinessException(400401, "部门存在用户，不能删除");
    softDelete("sys_dept", id);
    }

    @Transactional
    public void deleteConfig(long id) {
        Map<String, Object> config = detail("sys_config", id);
    if (intOf(config.get("builtin")) == 1) throw new BusinessException(400000, "内置配置禁止删除");
    softDelete("sys_config", id);
    }

    @Transactional
    public void deleteDictType(long id) {
        detail("sys_dict_type", id);
        Long count = jdbc.queryForObject("select count(1) from sys_dict_item where dict_type_id = ? and deleted = 0", Long.class, id);
    if (count != null && count > 0) throw new BusinessException(400501, "字典类型存在字典项，不能删除");
    softDelete("sys_dict_type", id);
    }

    @Transactional
    public void deleteDictItem(long id) {
        Map<String, Object> item = detail("sys_dict_item", id);
        Map<String, Object> type = detail("sys_dict_type", longOf(item.get("dict_type_id")));
        String dictCode = stringOf(type.get("dict_code"));
        String value = stringOf(item.get("value"));
    if ("common_status".equals(dictCode) && commonStatusValueReferenced(value)) {
    throw new BusinessException(400501, "字典项被引用，不能删除");
        }
    softDelete("sys_dict_item", id);
    }

    private boolean commonStatusValueReferenced(String value) {
        int status = intOf(value);
        List<String> tables = List.of("sys_user", "sys_role", "sys_menu", "sys_dept", "sys_dict_type", "sys_dict_item", "sys_config", "sys_job");
    for (String table : tables) {
            Long count = jdbc.queryForObject("select count(1) from " + table + " where status = ? and deleted = 0", Long.class, status);
    if (count != null && count > 0) return true;
        }
        return false;
    }

    @Transactional
    public Long insert(String table, Map<String, Object> body, Set<String> allowed) {
        TableMeta.require(table);
        LinkedHashMap<String, Object> values = columns(body, allowed);
    if (values.isEmpty()) throw new BusinessException(400000, "请求参数错误");
        String cols = String.join(", ", values.keySet());
        String placeholders = values.keySet().stream().map(k -> "?").collect(Collectors.joining(", "));
        jdbc.update("insert into " + table + " (" + cols + ") values (" + placeholders + ")", values.values().toArray());
        return jdbc.queryForObject("select last_insert_id()", Long.class);
    }

    @Transactional
    public void update(String table, long id, Map<String, Object> body, Set<String> allowed) {
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
    public void updateStatus(String table, long id, int status, boolean guardSuper) {
    if (guardSuper && "sys_user".equals(table)) assertNotSuperAdminTarget(id);
    detail(table, id);
        jdbc.update("update " + table + " set status = ? where id = ?", status, id);
    }

    @Transactional
    public void softDelete(String table, long id) {
    detail(table, id);
        jdbc.update("update " + table + " set deleted = 1 where id = ?", id);
    }

   public  List<String> roleCodes(long userId) {
        return jdbc.queryForList("""
            select r.role_code
            from sys_role r
            join sys_user_role ur on ur.role_id = r.id
            where ur.user_id = ? and r.deleted = 0 and r.status = 1
            """, String.class, userId);
    }

   public  List<String> permissionCodes(long userId) {
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

   public  boolean hasPermission(long userId, String permission) {
    return roleCodes(userId).contains("SUPER_ADMIN") || permissionCodes(userId).contains(permission);
    }

   public  PageResult<Map<String, Object>> roleUsers(long roleId, Map<String, String> q) {
        detail("sys_role", roleId);
        int page = Math.max(1, parseInt(q.get("page"), 1));
        int pageSize = Math.min(100, Math.max(1, parseInt(q.get("pageSize"), 20)));
        Long total = jdbc.queryForObject("""
            select count(1)
            from sys_user u
            join sys_user_role ur on ur.user_id = u.id
            where ur.role_id = ? and u.deleted = 0
            """, Long.class, roleId);
        List<Map<String, Object>> rows = jdbc.queryForList("""
            select u.id, u.username, u.real_name, u.phone, u.email, u.status, u.dept_id, u.created_at
            from sys_user u
            join sys_user_role ur on ur.user_id = u.id
            where ur.role_id = ? and u.deleted = 0
            order by u.created_at desc
            limit ?, ?
            """, roleId, (page - 1) * pageSize, pageSize);
        return new PageResult<>(rows, total == null ? 0 : total, page, pageSize);
    }

   public  List<Map<String, Object>> menuTree(Long userId) {
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

   public  List<Map<String, Object>> tree(String table, String parentColumn, String orderColumn) {
        TableMeta.require(table);
        List<Map<String, Object>> rows = jdbc.queryForList("select * from " + table + " where deleted = 0 order by " + orderColumn + " asc, id asc");
    return buildTree(rows.stream().map(LinkedHashMap::new).collect(Collectors.toList()), parentColumn);
    }

   public  List<Map<String, Object>> dictItems(long dictTypeId) {
        return jdbc.queryForList("select * from sys_dict_item where dict_type_id = ? and deleted = 0 order by sort asc, id asc", dictTypeId);
    }

   public  void refreshDictCache() {
        dictCache.clear();
    for (Map<String, Object> type : jdbc.queryForList("select * from sys_dict_type where deleted = 0 and status = 1")) {
            dictCache.put(stringOf(type.get("dict_code")), dictItems(longOf(type.get("id"))));
        }
    }

   public  PageResult<Map<String, Object>> onlineUsers(Map<String, String> q) {
        return onlineSessionService.page(q);
    }

   public  Map<String, Object> onlineUser(String tokenId) {
        return onlineSessionService.detail(tokenId);
    }

   public  void kickout(String tokenId) {
    if (Objects.equals(tokenId, StpUtil.getTokenValue())) throw new BusinessException(400000, "默认不允许强制下线当前登录用户");
        StpUtil.logoutByTokenValue(tokenId);
        onlineSessionService.remove(tokenId);
    }

   public  void validateCron(String cron) {
    if (cron == null || cron.isBlank() || cron.length() > 64 || cron.split("\\s+").length < 5) {
    throw new BusinessException(400000, "cronExpression 格式错误");
        }
    }

    public void runJob(long id) {
        Map<String, Object> job = detail("sys_job", id);
        LocalDateTime started = LocalDateTime.now();
        long startedMs = System.currentTimeMillis();
        try {
            jobExecutorRegistry.execute(stringOf(job.get("bean_name")), stringOf(job.get("method_name")));
            jdbc.update("insert into sys_job_run_log (job_id, job_name, status, started_at, finished_at, cost_ms) values (?, ?, 'SUCCESS', ?, ?, ?)",
                id, job.get("job_name"), started, LocalDateTime.now(), System.currentTimeMillis() - startedMs);
        } catch (RuntimeException ex) {
            jdbc.update("insert into sys_job_run_log (job_id, job_name, status, started_at, finished_at, cost_ms, error_message) values (?, ?, 'FAIL', ?, ?, ?, ?)",
                id, job.get("job_name"), started, LocalDateTime.now(), System.currentTimeMillis() - startedMs, ex.getMessage());
            throw ex;
        }
    }

   public  PageResult<Map<String, Object>> jobLogs(long jobId, Map<String, String> q) {
        int page = Math.max(1, parseInt(q.get("page"), 1));
        int pageSize = Math.min(100, Math.max(1, parseInt(q.get("pageSize"), 20)));
        Long total = jdbc.queryForObject("select count(1) from sys_job_run_log where job_id = ?", Long.class, jobId);
        List<Map<String, Object>> rows = jdbc.queryForList("select * from sys_job_run_log where job_id = ? order by started_at desc limit ?, ?", jobId, (page - 1) * pageSize, pageSize);
        return new PageResult<>(rows, total == null ? 0 : total, page, pageSize);
    }

    public Long createBackup(Map<String, Object> body, long userId) {
        try {
            Files.createDirectories(backupDir);
            String name = "backup-" + System.currentTimeMillis() + ".sql";
            Path normalizedDir = backupDir.toAbsolutePath().normalize();
            Path target = normalizedDir.resolve(name).normalize();
    if (!target.startsWith(normalizedDir)) throw new BusinessException(400000, "备份路径非法");
            MysqlTarget mysqlTarget = parseMysqlTarget();
            runProcess(List.of(mysqldumpCommand, "-h", mysqlTarget.host(), "-P", String.valueOf(mysqlTarget.port()),
                "-u" + datasourceUsername, "--single-transaction", "--routines",
                "--default-character-set=utf8mb4", mysqlTarget.database()), target, null);
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

   public  BackupFile downloadBackup(long id) throws IOException {
        Map<String, Object> backup = detail("sys_db_backup", id);
        Path path = Path.of(stringOf(backup.get("file_path"))).normalize();
    if (!Files.exists(path)) throw new BusinessException(404000, "备份文件不存在");
    return new BackupFile(stringOf(backup.get("backup_name")), Files.readAllBytes(path));
    }

   public  void restoreBackup(long id, Map<String, Object> body) {
        Map<String, Object> backup = detail("sys_db_backup", id);
    if (!Boolean.TRUE.equals(body.get("confirmed"))) throw new BusinessException(400000, "恢复数据库备份需要二次确认");
        Path path = Path.of(stringOf(backup.get("file_path"))).toAbsolutePath().normalize();
    if (!Files.exists(path)) throw new BusinessException(404000, "备份文件不存在");
        MysqlTarget mysqlTarget = parseMysqlTarget();
        runProcess(List.of(mysqlCommand, "-h", mysqlTarget.host(), "-P", String.valueOf(mysqlTarget.port()),
            "-u" + datasourceUsername, "--default-character-set=utf8mb4",
            mysqlTarget.database()), null, path);
    }

   public  void deleteBackup(long id) {
        Map<String, Object> backup = detail("sys_db_backup", id);
        Path path = Path.of(stringOf(backup.get("file_path"))).toAbsolutePath().normalize();
        try {
            Files.deleteIfExists(path);
        } catch (IOException ex) {
            throw new BusinessException(500000, "删除备份文件失败");
        }
        jdbc.update("delete from sys_db_backup where id = ?", id);
    }

   public  Map<String, Object> upload(MultipartFile file) throws IOException {
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

    private MysqlTarget parseMysqlTarget() {
        try {
            String raw = datasourceUrl.substring("jdbc:".length());
            URI uri = URI.create(raw);
            String database = uri.getPath() == null ? "" : uri.getPath().replaceFirst("^/", "");
            if (database.isBlank()) {
                throw new BusinessException(500000, "MySQL database name is missing");
            }
            return new MysqlTarget(uri.getHost(), uri.getPort() > 0 ? uri.getPort() : 3306, database);
        } catch (IllegalArgumentException | IndexOutOfBoundsException ex) {
            throw new BusinessException(500000, "Invalid MySQL datasource url");
        }
    }

    private void runProcess(List<String> command, Path stdoutFile, Path stdinFile) {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.environment().put("MYSQL_PWD", datasourcePassword);
        Path tempStdout = null;
        Path tempStderr = null;
        Path actualStdout = stdoutFile;
        Path actualStderr = null;
        if (actualStdout == null) {
            try {
                tempStdout = Files.createTempFile("drip-db-command-", ".out");
                actualStdout = tempStdout;
            } catch (IOException ex) {
                throw new BusinessException(500000, "database command temp output is not available");
            }
        }
        try {
            tempStderr = Files.createTempFile("drip-db-command-", ".err");
            actualStderr = tempStderr;
        } catch (IOException ex) {
            throw new BusinessException(500000, "database command temp error output is not available");
        }
        if (stdoutFile != null) {
            builder.redirectOutput(stdoutFile.toFile());
        } else {
            builder.redirectOutput(actualStdout.toFile());
        }
        if (stdinFile != null) {
            builder.redirectInput(stdinFile.toFile());
        }
        builder.redirectError(actualStderr.toFile());
        builder.redirectErrorStream(false);
        try {
            if (stdoutFile != null) {
                Files.deleteIfExists(stdoutFile);
            }
            Process process = builder.start();
            int exit = process.waitFor();
            String stdout = actualStdout == null || !Files.exists(actualStdout) ? "" : Files.readString(actualStdout, StandardCharsets.UTF_8);
            String stderr = actualStderr == null || !Files.exists(actualStderr) ? "" : Files.readString(actualStderr, StandardCharsets.UTF_8);
            if (exit != 0) {
                if (stdoutFile != null) {
                    Files.deleteIfExists(stdoutFile);
                }
                throw new BusinessException(500000, "database command failed: " + (stderr.isBlank() ? stdout : stderr));
            }
        } catch (IOException ex) {
            throw new BusinessException(500000, "database command is not available: " + command.getFirst());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new BusinessException(500000, "database command interrupted");
        } finally {
            try {
                if (tempStdout != null) Files.deleteIfExists(tempStdout);
                if (tempStderr != null) Files.deleteIfExists(tempStderr);
            } catch (IOException ignored) {
                // Temporary command output cleanup must not mask the command result.
            }
        }
    }

    private record MysqlTarget(String host, int port, String database) {
    }
}
