package com.drip.admin.modules.system.service.impl;

import com.drip.admin.modules.system.mapper.SysMenuMapper;
import com.drip.admin.modules.system.service.MenuService;

import com.drip.admin.common.exception.BusinessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.drip.admin.shared.utils.AdminUtils.buildTree;
import static com.drip.admin.shared.utils.AdminUtils.requireNonBlank;
import static com.drip.admin.shared.utils.AdminUtils.snakeToCamel;

@Service
public class MenuServiceImpl implements MenuService {
    private static final Set<String> MENU_COLUMNS = Set.of(
        "parent_id", "name", "type", "path", "component", "permission_code", "icon", "sort", "visible", "status"
    );

    private final JdbcTemplate jdbc;
    private final SysMenuMapper menuMapper;

    public MenuServiceImpl(JdbcTemplate jdbc, SysMenuMapper menuMapper) {
        this.jdbc = jdbc;
        this.menuMapper = menuMapper;
    }

    public List<Map<String, Object>> tree() {
        List<Map<String, Object>> rows = jdbc.queryForList("""
            select id, parent_id, name, type, path, component, permission_code, icon, sort, visible
            from sys_menu
            where deleted = 0 and status = 1
            order by sort asc, id asc
            """);
        return buildTree(rows.stream()
            .filter(row -> !"BUTTON".equals(row.get("type")))
            .map(LinkedHashMap::new)
            .collect(Collectors.toList()), "parent_id");
    }

    public Map<String, Object> detail(long id) {
        List<Map<String, Object>> rows = jdbc.queryForList("select * from sys_menu where id = ? and deleted = 0", id);
        if (rows.isEmpty()) {
            throw new BusinessException(404000, "资源不存在");
        }
        return new LinkedHashMap<>(rows.getFirst());
    }

    @Transactional
    public Long create(Map<String, Object> body) {
        requireNonBlank(body, "name");
        requireNonBlank(body, "type");
        LinkedHashMap<String, Object> values = columns(body);
        if (values.isEmpty()) {
            throw new BusinessException(400000, "请求参数错误");
        }
        String cols = String.join(", ", values.keySet());
        String placeholders = values.keySet().stream().map(k -> "?").collect(Collectors.joining(", "));
        jdbc.update("insert into sys_menu (" + cols + ") values (" + placeholders + ")", values.values().toArray());
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
        jdbc.update("update sys_menu set " + set + " where id = ? and deleted = 0", args.toArray());
    }

    @Transactional
    public void delete(long id) {
        Long count = jdbc.queryForObject("select count(1) from sys_menu where parent_id = ? and deleted = 0", Long.class, id);
        if (count != null && count > 0) {
            throw new BusinessException(400301, "菜单存在子节点，不能删除");
        }
        detail(id);
        jdbc.update("update sys_menu set deleted = 1 where id = ?", id);
    }

    @Transactional
    public void updateStatus(long id, int status) {
        detail(id);
        jdbc.update("update sys_menu set status = ? where id = ? and deleted = 0", status, id);
    }

    private static LinkedHashMap<String, Object> columns(Map<String, Object> body) {
        LinkedHashMap<String, Object> values = new LinkedHashMap<>();
        for (String col : MENU_COLUMNS) {
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
