package com.drip.admin.modules.system.service;

import com.drip.admin.common.exception.BusinessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.drip.admin.shared.utils.AdminUtils.buildTree;
import static com.drip.admin.shared.utils.AdminUtils.longOf;
import static com.drip.admin.shared.utils.AdminUtils.requireNonBlank;
import static com.drip.admin.shared.utils.AdminUtils.snakeToCamel;

@Service
public class DeptService {
    private static final Set<String> DEPT_COLUMNS = Set.of(
        "parent_id", "dept_name", "dept_code", "leader_user_id", "sort", "status"
    );

    private final JdbcTemplate jdbc;

    public DeptService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> tree() {
        List<Map<String, Object>> rows = jdbc.queryForList("""
            select *
            from sys_dept
            where deleted = 0
            order by sort asc, id asc
            """);
        return buildTree(rows.stream().map(LinkedHashMap::new).collect(Collectors.toList()), "parent_id");
    }

    public Map<String, Object> detail(long id) {
        List<Map<String, Object>> rows = jdbc.queryForList("select * from sys_dept where id = ? and deleted = 0", id);
        if (rows.isEmpty()) {
            throw new BusinessException(404000, "资源不存在");
        }
        return new LinkedHashMap<>(rows.getFirst());
    }

    @Transactional
    public Long create(Map<String, Object> body) {
        requireNonBlank(body, "dept_name", "deptName");
        requireNonBlank(body, "dept_code", "deptCode");
        LinkedHashMap<String, Object> values = columns(body);
        if (values.isEmpty()) {
            throw new BusinessException(400000, "请求参数错误");
        }
        String cols = String.join(", ", values.keySet());
        String placeholders = values.keySet().stream().map(k -> "?").collect(Collectors.joining(", "));
        jdbc.update("insert into sys_dept (" + cols + ") values (" + placeholders + ")", values.values().toArray());
        return jdbc.queryForObject("select last_insert_id()", Long.class);
    }

    @Transactional
    public void update(long id, Map<String, Object> body) {
        detail(id);
        assertValidParent(id, body);
        LinkedHashMap<String, Object> values = columns(body);
        if (values.isEmpty()) {
            return;
        }
        String set = values.keySet().stream().map(k -> k + " = ?").collect(Collectors.joining(", "));
        List<Object> args = new ArrayList<>(values.values());
        args.add(id);
        jdbc.update("update sys_dept set " + set + " where id = ? and deleted = 0", args.toArray());
    }

    @Transactional
    public void delete(long id) {
        detail(id);
        Long childCount = jdbc.queryForObject("select count(1) from sys_dept where parent_id = ? and deleted = 0", Long.class, id);
        if (childCount != null && childCount > 0) {
            throw new BusinessException(400401, "部门存在子节点，不能删除");
        }
        Long userCount = jdbc.queryForObject("select count(1) from sys_user where dept_id = ? and deleted = 0", Long.class, id);
        if (userCount != null && userCount > 0) {
            throw new BusinessException(400401, "部门存在用户，不能删除");
        }
        jdbc.update("update sys_dept set deleted = 1 where id = ?", id);
    }

    @Transactional
    public void updateStatus(long id, int status) {
        detail(id);
        jdbc.update("update sys_dept set status = ? where id = ? and deleted = 0", status, id);
    }

    private void assertValidParent(long id, Map<String, Object> body) {
        Object rawParentId = body.containsKey("parent_id") ? body.get("parent_id") : body.get("parentId");
        if (rawParentId == null) {
            return;
        }
        long parentId = longOf(rawParentId);
        if (parentId == id || descendantDeptIds(id).contains(parentId)) {
            throw new BusinessException(400000, "不能把部门移动到自身的子部门下");
        }
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

    private static LinkedHashMap<String, Object> columns(Map<String, Object> body) {
        LinkedHashMap<String, Object> values = new LinkedHashMap<>();
        for (String col : DEPT_COLUMNS) {
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
