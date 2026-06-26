package com.drip.admin.modules.system.service.impl;

import com.drip.admin.modules.system.mapper.SysMenuMapper;
import com.drip.admin.modules.system.mapper.SysRoleMapper;
import com.drip.admin.modules.system.mapper.SysRoleMenuMapper;
import com.drip.admin.modules.system.mapper.SysUserMapper;
import com.drip.admin.modules.system.mapper.SysUserRoleMapper;
import com.drip.admin.modules.system.service.RoleService;

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

import static com.drip.admin.shared.utils.AdminUtils.intOf;
import static com.drip.admin.shared.utils.AdminUtils.parseInt;
import static com.drip.admin.shared.utils.AdminUtils.requireNonBlank;
import static com.drip.admin.shared.utils.AdminUtils.snakeToCamel;

@Service
public class RoleServiceImpl implements RoleService {
    private static final Set<String> ROLE_COLUMNS = Set.of("role_name", "role_code", "status", "remark");
    private final JdbcTemplate jdbc;
    private final SysRoleMapper roleMapper;
    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysMenuMapper menuMapper;
    private final SysRoleMenuMapper roleMenuMapper;

    public RoleServiceImpl(
        JdbcTemplate jdbc,
        SysRoleMapper roleMapper,
        SysUserMapper userMapper,
        SysUserRoleMapper userRoleMapper,
        SysMenuMapper menuMapper,
        SysRoleMenuMapper roleMenuMapper
    ) {
        this.jdbc = jdbc;
        this.roleMapper = roleMapper;
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.menuMapper = menuMapper;
        this.roleMenuMapper = roleMenuMapper;
    }

    public PageResult<Map<String, Object>> page(Map<String, String> q) {
        int page = Math.max(1, parseInt(q.get("page"), 1));
        int pageSize = Math.min(100, Math.max(1, parseInt(q.get("pageSize"), 20)));
        List<Object> args = new ArrayList<>();
        StringBuilder where = new StringBuilder(" where deleted = 0");
        for (String filter : List.of("role_name", "role_code", "status", "created_at")) {
            String value = q.getOrDefault(filter, q.get(snakeToCamel(filter)));
            if (value != null && !value.isBlank()) {
                where.append(" and ").append(filter).append(" like ?");
                args.add("%" + value + "%");
            }
        }
        Long total = jdbc.queryForObject("select count(1) from sys_role" + where, Long.class, args.toArray());
        List<Object> listArgs = new ArrayList<>(args);
        listArgs.add((page - 1) * pageSize);
        listArgs.add(pageSize);
        List<Map<String, Object>> rows = jdbc.queryForList(
            "select * from sys_role" + where + " order by created_at desc limit ?, ?",
            listArgs.toArray()
        );
        return new PageResult<>(rows, total == null ? 0 : total, page, pageSize);
    }

    public Map<String, Object> detail(long id) {
        List<Map<String, Object>> rows = jdbc.queryForList("select * from sys_role where id = ? and deleted = 0", id);
        if (rows.isEmpty()) {
            throw new BusinessException(404000, "资源不存在");
        }
        return new LinkedHashMap<>(rows.getFirst());
    }

    public PageResult<Map<String, Object>> users(long roleId, Map<String, String> q) {
        detail(roleId);
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

    @Transactional
    public Long create(Map<String, Object> body) {
        requireNonBlank(body, "role_name", "roleName");
        requireNonBlank(body, "role_code", "roleCode");
        LinkedHashMap<String, Object> values = columns(body);
        String cols = String.join(", ", values.keySet());
        String placeholders = values.keySet().stream().map(k -> "?").collect(Collectors.joining(", "));
        jdbc.update("insert into sys_role (" + cols + ") values (" + placeholders + ")", values.values().toArray());
        return jdbc.queryForObject("select last_insert_id()", Long.class);
    }

    @Transactional
    public void update(long id, Map<String, Object> body) {
        detail(id);
        LinkedHashMap<String, Object> values = columns(body);
        if (values.isEmpty()) {
            return;
        }
        String set = values.keySet().stream().map(k -> k + " = ?").collect(Collectors.joining(", "));
        List<Object> args = new ArrayList<>(values.values());
        args.add(id);
        jdbc.update("update sys_role set " + set + " where id = ? and deleted = 0", args.toArray());
    }

    @Transactional
    public void delete(long id) {
        Map<String, Object> role = detail(id);
        if (intOf(role.get("builtin")) == 1) {
            throw new BusinessException(400000, "内置角色禁止删除");
        }
        Long count = jdbc.queryForObject("select count(1) from sys_user_role where role_id = ?", Long.class, id);
        if (count != null && count > 0) {
            throw new BusinessException(409000, "角色已分配用户，不能删除");
        }
        jdbc.update("update sys_role set deleted = 1 where id = ?", id);
    }

    @Transactional
    public void updateStatus(long id, int status) {
        detail(id);
        jdbc.update("update sys_role set status = ? where id = ? and deleted = 0", status, id);
    }

    @Transactional
    public void assignMenus(long roleId, List<Long> menuIds) {
        detail(roleId);
        assertExistingMenus(menuIds);
        jdbc.update("delete from sys_role_menu where role_id = ?", roleId);
        for (Long menuId : menuIds) {
            jdbc.update("insert into sys_role_menu (role_id, menu_id) values (?, ?)", roleId, menuId);
        }
    }

    private void assertExistingMenus(List<Long> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            return;
        }
        List<Long> uniqueIds = menuIds.stream().filter(Objects::nonNull).distinct().toList();
        if (uniqueIds.size() != menuIds.size()) {
            throw new BusinessException(400000, "菜单权限不存在");
        }
        String placeholders = uniqueIds.stream().map(id -> "?").collect(Collectors.joining(", "));
        Long count = jdbc.queryForObject(
            "select count(1) from sys_menu where id in (" + placeholders + ") and deleted = 0",
            Long.class,
            uniqueIds.toArray()
        );
        if (count == null || count != uniqueIds.size()) {
            throw new BusinessException(400000, "菜单权限不存在");
        }
    }

    private static LinkedHashMap<String, Object> columns(Map<String, Object> body) {
        LinkedHashMap<String, Object> values = new LinkedHashMap<>();
        for (String col : ROLE_COLUMNS) {
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
