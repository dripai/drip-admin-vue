package com.drip.admin.modules.system.service.impl;

import com.drip.admin.modules.system.service.UserService;

import com.drip.admin.common.exception.BusinessException;
import com.drip.admin.common.response.PageResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.drip.admin.shared.utils.AdminUtils.currentUserId;
import static com.drip.admin.shared.utils.AdminUtils.hashPassword;
import static com.drip.admin.shared.utils.AdminUtils.intOf;
import static com.drip.admin.shared.utils.AdminUtils.parseInt;
import static com.drip.admin.shared.utils.AdminUtils.requireNonBlank;
import static com.drip.admin.shared.utils.AdminUtils.snakeToCamel;
import static com.drip.admin.shared.utils.AdminUtils.stringValue;

@Service
public class UserServiceImpl implements UserService {
    private static final Set<String> USER_COLUMNS = Set.of(
        "username", "real_name", "phone", "email", "status", "dept_id", "remark"
    );

    private final JdbcTemplate jdbc;

    public UserServiceImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public PageResult<Map<String, Object>> page(Map<String, String> q) {
        int page = Math.max(1, parseInt(q.get("page"), 1));
        int pageSize = Math.min(100, Math.max(1, parseInt(q.get("pageSize"), 20)));
        List<Object> args = new ArrayList<>();
        StringBuilder where = new StringBuilder(" where deleted = 0");
        for (String filter : List.of("username", "real_name", "phone", "status", "dept_id", "created_at")) {
            String value = q.getOrDefault(filter, q.get(snakeToCamel(filter)));
            if (value != null && !value.isBlank()) {
                where.append(" and ").append(filter).append(" like ?");
                args.add("%" + value + "%");
            }
        }
        Long total = jdbc.queryForObject("select count(1) from sys_user" + where, Long.class, args.toArray());
        List<Object> listArgs = new ArrayList<>(args);
        listArgs.add((page - 1) * pageSize);
        listArgs.add(pageSize);
        List<Map<String, Object>> rows = jdbc.queryForList(
            "select * from sys_user" + where + " order by created_at desc limit ?, ?",
            listArgs.toArray()
        );
        return new PageResult<>(rows, total == null ? 0 : total, page, pageSize);
    }

    public Map<String, Object> detail(long id) {
        List<Map<String, Object>> rows = jdbc.queryForList("select * from sys_user where id = ? and deleted = 0", id);
        if (rows.isEmpty()) {
            throw new BusinessException(404000, "资源不存在");
        }
        return new LinkedHashMap<>(rows.getFirst());
    }

    @Transactional
    public Long create(Map<String, Object> body) {
        requireNonBlank(body, "username");
        requireNonBlank(body, "real_name", "realName");
        LinkedHashMap<String, Object> values = columns(body);
        String password = stringValue(body, "password", "Admin@123456");
        String salt = "salt" + System.nanoTime();
        values.put("password_salt", salt);
        values.put("password_hash", hashPassword(password, salt));
        values.putIfAbsent("status", 1);
        String cols = String.join(", ", values.keySet());
        String placeholders = values.keySet().stream().map(k -> "?").collect(Collectors.joining(", "));
        jdbc.update("insert into sys_user (" + cols + ") values (" + placeholders + ")", values.values().toArray());
        return jdbc.queryForObject("select last_insert_id()", Long.class);
    }

    @Transactional
    public void update(long id, Map<String, Object> body) {
        assertNotSuperAdminTarget(id);
        detail(id);
        LinkedHashMap<String, Object> values = columns(body);
        if (values.isEmpty()) {
            return;
        }
        String set = values.keySet().stream().map(k -> k + " = ?").collect(Collectors.joining(", "));
        List<Object> args = new ArrayList<>(values.values());
        args.add(id);
        jdbc.update("update sys_user set " + set + " where id = ? and deleted = 0", args.toArray());
    }

    @Transactional
    public void delete(long id) {
        if (currentUserId() == id) {
            throw new BusinessException(400000, "不能删除当前登录用户");
        }
        assertNotSuperAdminTarget(id);
        detail(id);
        jdbc.update("update sys_user set deleted = 1 where id = ?", id);
    }

    @Transactional
    public void updateStatus(long id, int status) {
        if (id == currentUserId() && status != 1) {
            throw new BusinessException(400000, "不能禁用当前登录用户");
        }
        assertNotSuperAdminTarget(id);
        detail(id);
        jdbc.update("update sys_user set status = ? where id = ? and deleted = 0", status, id);
    }

    @Transactional
    public void resetPassword(long id, String password) {
        assertNotSuperAdminTarget(id);
        detail(id);
        String salt = "salt" + System.nanoTime();
        jdbc.update("update sys_user set password_salt = ?, password_hash = ? where id = ? and deleted = 0",
            salt, hashPassword(password, salt), id);
    }

    @Transactional
    public void assignRoles(long userId, List<Long> roleIds) {
        assertExistingRoles(roleIds);
        assertNotSuperAdminTarget(userId);
        detail(userId);
        jdbc.update("delete from sys_user_role where user_id = ?", userId);
        for (Long roleId : roleIds) {
            jdbc.update("insert into sys_user_role (user_id, role_id) values (?, ?)", userId, roleId);
        }
    }

    private void assertNotSuperAdminTarget(long userId) {
        if (roleCodes(currentUserId()).contains("SUPER_ADMIN")) {
            return;
        }
        if (roleCodes(userId).contains("SUPER_ADMIN")) {
            throw new BusinessException(403000, "普通管理员不能操作超级管理员");
        }
    }

    private List<String> roleCodes(long userId) {
        return jdbc.queryForList("""
            select r.role_code
            from sys_role r
            join sys_user_role ur on ur.role_id = r.id
            where ur.user_id = ? and r.deleted = 0 and r.status = 1
            """, String.class, userId);
    }

    private void assertExistingRoles(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        List<Long> uniqueIds = roleIds.stream().filter(Objects::nonNull).distinct().toList();
        if (uniqueIds.size() != roleIds.size()) {
            throw new BusinessException(400000, "角色不存在");
        }
        String placeholders = uniqueIds.stream().map(id -> "?").collect(Collectors.joining(", "));
        Long count = jdbc.queryForObject(
            "select count(1) from sys_role where id in (" + placeholders + ") and deleted = 0",
            Long.class,
            uniqueIds.toArray()
        );
        if (count == null || count != uniqueIds.size()) {
            throw new BusinessException(400000, "角色不存在");
        }
    }

    private static LinkedHashMap<String, Object> columns(Map<String, Object> body) {
        LinkedHashMap<String, Object> values = new LinkedHashMap<>();
        for (String col : USER_COLUMNS) {
            String camel = snakeToCamel(col);
            if (body.containsKey(col)) {
                values.put(col, body.get(col));
            } else if (body.containsKey(camel)) {
                values.put(col, body.get(camel));
            }
        }
        return values;
    }
}
