package com.drip.admin.modules.system.service.impl;

import com.drip.admin.modules.system.mapper.SysLoginLogMapper;
import com.drip.admin.modules.system.mapper.SysOperationLogMapper;
import com.drip.admin.modules.system.service.SystemLogQueryService;

import com.drip.admin.common.exception.BusinessException;
import com.drip.admin.common.response.PageResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.drip.admin.shared.utils.AdminUtils.parseInt;
import static com.drip.admin.shared.utils.AdminUtils.snakeToCamel;

@Service
public class SystemLogQueryServiceImpl implements SystemLogQueryService {
    private final JdbcTemplate jdbc;
    private final SysLoginLogMapper loginLogMapper;
    private final SysOperationLogMapper operationLogMapper;

    public SystemLogQueryServiceImpl(JdbcTemplate jdbc, SysLoginLogMapper loginLogMapper, SysOperationLogMapper operationLogMapper) {
        this.jdbc = jdbc;
        this.loginLogMapper = loginLogMapper;
        this.operationLogMapper = operationLogMapper;
    }

    public PageResult<Map<String, Object>> loginLogs(Map<String, String> q) {
        return page("sys_login_log", q, "login_at", List.of("username", "status", "login_type", "device_type", "ip"));
    }

    public Map<String, Object> loginLog(long id) {
        return detail("sys_login_log", id);
    }

    public PageResult<Map<String, Object>> operationLogs(Map<String, String> q) {
        return page("sys_operation_log", q, "created_at", List.of("operator_name", "module", "action", "response_status", "path"));
    }

    public Map<String, Object> operationLog(long id) {
        return detail("sys_operation_log", id);
    }

    private PageResult<Map<String, Object>> page(String table, Map<String, String> q, String orderColumn, List<String> filters) {
        int page = Math.max(1, parseInt(q.get("page"), 1));
        int pageSize = Math.min(100, Math.max(1, parseInt(q.get("pageSize"), 20)));
        List<Object> args = new ArrayList<>();
        StringBuilder where = new StringBuilder(" where 1 = 1");
        for (String filter : filters) {
            String value = q.getOrDefault(filter, q.get(snakeToCamel(filter)));
            if (value != null && !value.isBlank()) {
                where.append(" and ").append(filter).append(" like ?");
                args.add("%" + value + "%");
            }
        }
        Long total = jdbc.queryForObject("select count(1) from " + table + where, Long.class, args.toArray());
        List<Object> listArgs = new ArrayList<>(args);
        listArgs.add((page - 1) * pageSize);
        listArgs.add(pageSize);
        List<Map<String, Object>> rows = jdbc.queryForList(
            "select * from " + table + where + " order by " + orderColumn + " desc limit ?, ?",
            listArgs.toArray()
        );
        return new PageResult<>(rows, total == null ? 0 : total, page, pageSize);
    }

    private Map<String, Object> detail(String table, long id) {
        List<Map<String, Object>> rows = jdbc.queryForList("select * from " + table + " where id = ?", id);
        if (rows.isEmpty()) {
            throw new BusinessException(404000, "资源不存在");
        }
        return new LinkedHashMap<>(rows.getFirst());
    }
}
