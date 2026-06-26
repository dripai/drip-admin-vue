package com.drip.admin.modules.system.service.impl;

import com.drip.admin.modules.system.mapper.SysConfigMapper;
import com.drip.admin.modules.system.service.ConfigService;

import com.drip.admin.common.exception.BusinessException;
import com.drip.admin.common.response.PageResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.drip.admin.shared.utils.AdminUtils.intOf;
import static com.drip.admin.shared.utils.AdminUtils.parseInt;
import static com.drip.admin.shared.utils.AdminUtils.requireNonBlank;
import static com.drip.admin.shared.utils.AdminUtils.snakeToCamel;

@Service
public class ConfigServiceImpl implements ConfigService {
    private static final Set<String> CREATE_COLUMNS = Set.of(
        "config_name", "config_key", "config_value", "group_code", "is_sensitive", "builtin", "status", "remark"
    );
    private static final Set<String> UPDATE_COLUMNS = Set.of(
        "config_name", "config_key", "config_value", "group_code", "is_sensitive", "status", "remark"
    );

    private final JdbcTemplate jdbc;
    private final SysConfigMapper configMapper;

    public ConfigServiceImpl(JdbcTemplate jdbc, SysConfigMapper configMapper) {
        this.jdbc = jdbc;
        this.configMapper = configMapper;
    }

    public PageResult<Map<String, Object>> page(Map<String, String> q) {
        int page = Math.max(1, parseInt(q.get("page"), 1));
        int pageSize = Math.min(100, Math.max(1, parseInt(q.get("pageSize"), 20)));
        List<Object> args = new ArrayList<>();
        StringBuilder where = new StringBuilder(" where deleted = 0");
        for (String filter : List.of("config_name", "config_key", "group_code", "status")) {
            String value = q.getOrDefault(filter, q.get(snakeToCamel(filter)));
            if (value != null && !value.isBlank()) {
                where.append(" and ").append(filter).append(" like ?");
                args.add("%" + value + "%");
            }
        }
        Long total = jdbc.queryForObject("select count(1) from sys_config" + where, Long.class, args.toArray());
        List<Object> listArgs = new ArrayList<>(args);
        listArgs.add((page - 1) * pageSize);
        listArgs.add(pageSize);
        List<Map<String, Object>> rows = jdbc.queryForList(
            "select * from sys_config" + where + " order by created_at desc limit ?, ?",
            listArgs.toArray()
        );
        rows.forEach(this::maskSensitive);
        return new PageResult<>(rows, total == null ? 0 : total, page, pageSize);
    }

    public Map<String, Object> detail(long id) {
        Map<String, Object> row = rawDetail(id);
        maskSensitive(row);
        return row;
    }

    @Transactional
    public Long create(Map<String, Object> body) {
        requireNonBlank(body, "config_name", "configName");
        requireNonBlank(body, "config_key", "configKey");
        requireNonBlank(body, "config_value", "configValue");
        LinkedHashMap<String, Object> values = columns(body, CREATE_COLUMNS);
        if (values.isEmpty()) {
            throw new BusinessException(400000, "请求参数错误");
        }
        String cols = String.join(", ", values.keySet());
        String placeholders = values.keySet().stream().map(k -> "?").collect(Collectors.joining(", "));
        jdbc.update("insert into sys_config (" + cols + ") values (" + placeholders + ")", values.values().toArray());
        return jdbc.queryForObject("select last_insert_id()", Long.class);
    }

    @Transactional
    public void update(long id, Map<String, Object> body) {
        rawDetail(id);
        LinkedHashMap<String, Object> values = columns(body, UPDATE_COLUMNS);
        if (values.isEmpty()) {
            return;
        }
        String set = values.keySet().stream().map(k -> k + " = ?").collect(Collectors.joining(", "));
        List<Object> args = new ArrayList<>(values.values());
        args.add(id);
        jdbc.update("update sys_config set " + set + " where id = ? and deleted = 0", args.toArray());
    }

    @Transactional
    public void delete(long id) {
        Map<String, Object> config = rawDetail(id);
        if (intOf(config.get("builtin")) == 1) {
            throw new BusinessException(400000, "内置配置禁止删除");
        }
        jdbc.update("update sys_config set deleted = 1 where id = ?", id);
    }

    @Transactional
    public void updateStatus(long id, int status) {
        rawDetail(id);
        jdbc.update("update sys_config set status = ? where id = ? and deleted = 0", status, id);
    }

    private Map<String, Object> rawDetail(long id) {
        List<Map<String, Object>> rows = jdbc.queryForList("select * from sys_config where id = ? and deleted = 0", id);
        if (rows.isEmpty()) {
            throw new BusinessException(404000, "资源不存在");
        }
        return new LinkedHashMap<>(rows.getFirst());
    }

    private void maskSensitive(Map<String, Object> row) {
        if (intOf(row.get("is_sensitive")) == 1) {
            row.put("config_value", "******");
        }
    }

    private static LinkedHashMap<String, Object> columns(Map<String, Object> body, Set<String> allowed) {
        LinkedHashMap<String, Object> values = new LinkedHashMap<>();
        for (String col : allowed) {
            String camel = snakeToCamel(col);
            if (body.containsKey(col)) {
                values.put(col, body.get(col));
            } else if (body.containsKey(camel)) {
                values.put(col, body.get(camel));
            } else if ("is_sensitive".equals(col) && body.containsKey("sensitive")) {
                values.put(col, body.get("sensitive"));
            }
        }
        return values;
    }
}
