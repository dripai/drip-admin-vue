package com.drip.admin.modules.system.service.impl;

import com.drip.admin.modules.system.mapper.SysJobMapper;
import com.drip.admin.modules.system.mapper.SysJobRunLogMapper;
import com.drip.admin.common.exception.BusinessException;
import com.drip.admin.common.response.PageResult;
import com.drip.admin.infrastructure.external.JobExecutorRegistry;
import com.drip.admin.modules.system.service.JobService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.drip.admin.shared.utils.AdminUtils.parseInt;
import static com.drip.admin.shared.utils.AdminUtils.requireNonBlank;
import static com.drip.admin.shared.utils.AdminUtils.snakeToCamel;
import static com.drip.admin.shared.utils.AdminUtils.stringOf;

@Service
public class JobServiceImpl implements JobService {
    private static final Set<String> JOB_COLUMNS = Set.of(
        "job_name", "job_code", "cron_expression", "bean_name", "method_name", "params", "status", "remark"
    );

    private final JdbcTemplate jdbc;
    private final SysJobMapper jobMapper;
    private final SysJobRunLogMapper jobRunLogMapper;
    private final JobExecutorRegistry jobExecutorRegistry;

    public JobServiceImpl(JdbcTemplate jdbc, SysJobMapper jobMapper, SysJobRunLogMapper jobRunLogMapper, JobExecutorRegistry jobExecutorRegistry) {
        this.jdbc = jdbc;
        this.jobMapper = jobMapper;
        this.jobRunLogMapper = jobRunLogMapper;
        this.jobExecutorRegistry = jobExecutorRegistry;
    }

    @Override
    public PageResult<Map<String, Object>> page(Map<String, String> q) {
        int page = Math.max(1, parseInt(q.get("page"), 1));
        int pageSize = Math.min(100, Math.max(1, parseInt(q.get("pageSize"), 20)));
        List<Object> args = new ArrayList<>();
        StringBuilder where = new StringBuilder(" where deleted = 0");
        for (String filter : List.of("job_name", "job_code", "status", "created_at")) {
            String value = q.getOrDefault(filter, q.get(snakeToCamel(filter)));
            if (value != null && !value.isBlank()) {
                where.append(" and ").append(filter).append(" like ?");
                args.add("%" + value + "%");
            }
        }
        Long total = jdbc.queryForObject("select count(1) from sys_job" + where, Long.class, args.toArray());
        List<Object> listArgs = new ArrayList<>(args);
        listArgs.add((page - 1) * pageSize);
        listArgs.add(pageSize);
        List<Map<String, Object>> rows = jdbc.queryForList(
            "select * from sys_job" + where + " order by created_at desc limit ?, ?",
            listArgs.toArray()
        );
        return new PageResult<>(rows, total == null ? 0 : total, page, pageSize);
    }

    @Override
    public Map<String, Object> detail(long id) {
        List<Map<String, Object>> rows = jdbc.queryForList("select * from sys_job where id = ? and deleted = 0", id);
        if (rows.isEmpty()) {
            throw new BusinessException(404000, "resource not found");
        }
        return new LinkedHashMap<>(rows.getFirst());
    }

    @Override
    @Transactional
    public Long create(Map<String, Object> body) {
        requireNonBlank(body, "job_name", "jobName");
        requireNonBlank(body, "job_code", "jobCode");
        requireNonBlank(body, "cron_expression", "cronExpression");
        requireNonBlank(body, "bean_name", "beanName");
        requireNonBlank(body, "method_name", "methodName");
        validateCron(stringValue(body, "cron_expression"));
        LinkedHashMap<String, Object> values = columns(body);
        String cols = String.join(", ", values.keySet());
        String placeholders = values.keySet().stream().map(k -> "?").collect(Collectors.joining(", "));
        jdbc.update("insert into sys_job (" + cols + ") values (" + placeholders + ")", values.values().toArray());
        return jdbc.queryForObject("select last_insert_id()", Long.class);
    }

    @Override
    @Transactional
    public void update(long id, Map<String, Object> body) {
        detail(id);
        Object cron = value(body, "cron_expression");
        if (cron != null) {
            validateCron(String.valueOf(cron));
        }
        LinkedHashMap<String, Object> values = columns(body);
        if (values.isEmpty()) {
            return;
        }
        String set = values.keySet().stream().map(k -> k + " = ?").collect(Collectors.joining(", "));
        List<Object> args = new ArrayList<>(values.values());
        args.add(id);
        jdbc.update("update sys_job set " + set + " where id = ? and deleted = 0", args.toArray());
    }

    @Override
    @Transactional
    public void delete(long id) {
        detail(id);
        jdbc.update("update sys_job set deleted = 1 where id = ?", id);
    }

    @Override
    @Transactional
    public void updateStatus(long id, int status) {
        detail(id);
        jdbc.update("update sys_job set status = ? where id = ? and deleted = 0", status, id);
    }

    @Override
    public void run(long id) {
        Map<String, Object> job = detail(id);
        LocalDateTime started = LocalDateTime.now();
        long startedMs = System.currentTimeMillis();
        try {
            jobExecutorRegistry.execute(stringOf(job.get("bean_name")), stringOf(job.get("method_name")));
            jdbc.update(
                "insert into sys_job_run_log (job_id, job_name, status, started_at, finished_at, cost_ms) values (?, ?, 'SUCCESS', ?, ?, ?)",
                id,
                job.get("job_name"),
                started,
                LocalDateTime.now(),
                System.currentTimeMillis() - startedMs
            );
        } catch (RuntimeException ex) {
            jdbc.update(
                "insert into sys_job_run_log (job_id, job_name, status, started_at, finished_at, cost_ms, error_message) values (?, ?, 'FAIL', ?, ?, ?, ?)",
                id,
                job.get("job_name"),
                started,
                LocalDateTime.now(),
                System.currentTimeMillis() - startedMs,
                ex.getMessage()
            );
            throw ex;
        }
    }

    @Override
    public PageResult<Map<String, Object>> runLogs(long jobId, Map<String, String> q) {
        int page = Math.max(1, parseInt(q.get("page"), 1));
        int pageSize = Math.min(100, Math.max(1, parseInt(q.get("pageSize"), 20)));
        Long total = jdbc.queryForObject("select count(1) from sys_job_run_log where job_id = ?", Long.class, jobId);
        List<Map<String, Object>> rows = jdbc.queryForList(
            "select * from sys_job_run_log where job_id = ? order by started_at desc limit ?, ?",
            jobId,
            (page - 1) * pageSize,
            pageSize
        );
        return new PageResult<>(rows, total == null ? 0 : total, page, pageSize);
    }

    private static void validateCron(String cron) {
        if (cron == null || cron.isBlank() || cron.length() > 64 || cron.split("\\s+").length < 5) {
            throw new BusinessException(400000, "cronExpression format is invalid");
        }
    }

    private static LinkedHashMap<String, Object> columns(Map<String, Object> body) {
        LinkedHashMap<String, Object> values = new LinkedHashMap<>();
        for (String col : JOB_COLUMNS) {
            Object raw = value(body, col);
            if (raw != null) {
                values.put(col, raw);
            }
        }
        return values;
    }

    private static Object value(Map<String, Object> body, String column) {
        String camel = snakeToCamel(column);
        if (body.containsKey(column)) {
            return body.get(column);
        }
        if (body.containsKey(camel)) {
            return body.get(camel);
        }
        return null;
    }

    private static String stringValue(Map<String, Object> body, String column) {
        Object raw = value(body, column);
        return raw == null ? null : String.valueOf(raw);
    }
}
