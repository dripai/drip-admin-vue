package com.drip.admin.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drip.admin.common.exception.BusinessException;
import com.drip.admin.common.response.PageResult;
import com.drip.admin.infrastructure.external.JobExecutorRegistry;
import com.drip.admin.modules.system.entity.SysJobEntity;
import com.drip.admin.modules.system.entity.SysJobRunLogEntity;
import com.drip.admin.modules.system.mapper.SysJobMapper;
import com.drip.admin.modules.system.mapper.SysJobRunLogMapper;
import com.drip.admin.modules.system.service.JobService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

import static com.drip.admin.shared.utils.AdminUtils.intOf;
import static com.drip.admin.shared.utils.AdminUtils.parseInt;
import static com.drip.admin.shared.utils.AdminUtils.requireNonBlank;

@Service
public class JobServiceImpl extends ServiceImpl<SysJobMapper, SysJobEntity> implements JobService {
    private final SysJobRunLogMapper jobRunLogMapper;
    private final JobExecutorRegistry jobExecutorRegistry;

    public JobServiceImpl(SysJobRunLogMapper jobRunLogMapper, JobExecutorRegistry jobExecutorRegistry) {
        this.jobRunLogMapper = jobRunLogMapper;
        this.jobExecutorRegistry = jobExecutorRegistry;
    }

    @Override
    public PageResult<SysJobEntity> page(Map<String, String> q) {
        int page = Math.max(1, parseInt(q.get("page"), 1)); int pageSize = Math.min(100, Math.max(1, parseInt(q.get("pageSize"), 20)));
        QueryWrapper<SysJobEntity> wrapper = new QueryWrapper<>();
        likeIfPresent(wrapper, "job_name", q.getOrDefault("job_name", q.get("jobName"))); likeIfPresent(wrapper, "job_code", q.getOrDefault("job_code", q.get("jobCode")));
        likeIfPresent(wrapper, "status", q.get("status")); likeIfPresent(wrapper, "created_at", q.getOrDefault("created_at", q.get("createdAt"))); wrapper.orderByDesc("created_at");
        Page<SysJobEntity> result = page(new Page<>(page, pageSize), wrapper); return new PageResult<>(result.getRecords(), result.getTotal(), page, pageSize);
    }

    @Override
    public SysJobEntity detail(long id) { SysJobEntity entity = getById(id); if (entity == null) throw new BusinessException(404000, "?????"); return entity; }

    @Override
    @Transactional
    public Long create(Map<String, Object> body) { requireNonBlank(body, "job_name", "jobName"); requireNonBlank(body, "job_code", "jobCode"); requireNonBlank(body, "cron_expression", "cronExpression"); validateCron(stringValue(body, "cron_expression")); SysJobEntity entity = new SysJobEntity(); apply(entity, body); save(entity); return entity.getId(); }

    @Override
    @Transactional
    public void update(long id, Map<String, Object> body) { detail(id); if (body.containsKey("cron_expression") || body.containsKey("cronExpression")) validateCron(stringValue(body, "cron_expression")); SysJobEntity entity = new SysJobEntity(); entity.setId(id); apply(entity, body); updateById(entity); }

    @Override
    @Transactional
    public void delete(long id) { detail(id); removeById(id); }

    @Override
    @Transactional
    public void updateStatus(long id, int status) { detail(id); SysJobEntity entity = new SysJobEntity(); entity.setId(id); entity.setStatus(status); updateById(entity); }

    @Override
    public void run(long id) {
        SysJobEntity job = detail(id); LocalDateTime started = LocalDateTime.now(); long startedMs = System.currentTimeMillis();
        try {
            jobExecutorRegistry.execute(job.getBeanName(), job.getMethodName());
            SysJobRunLogEntity log = new SysJobRunLogEntity(); log.setJobId(id); log.setJobName(job.getJobName()); log.setStatus("SUCCESS"); log.setStartedAt(started); log.setFinishedAt(LocalDateTime.now()); log.setCostMs(System.currentTimeMillis() - startedMs); jobRunLogMapper.insert(log);
        } catch (RuntimeException ex) {
            SysJobRunLogEntity log = new SysJobRunLogEntity(); log.setJobId(id); log.setJobName(job.getJobName()); log.setStatus("FAIL"); log.setStartedAt(started); log.setFinishedAt(LocalDateTime.now()); log.setCostMs(System.currentTimeMillis() - startedMs); log.setErrorMessage(ex.getMessage()); jobRunLogMapper.insert(log); throw ex;
        }
    }

    @Override
    public PageResult<SysJobRunLogEntity> runLogs(long jobId, Map<String, String> q) {
        int page = Math.max(1, parseInt(q.get("page"), 1)); int pageSize = Math.min(100, Math.max(1, parseInt(q.get("pageSize"), 20)));
        Page<SysJobRunLogEntity> result = jobRunLogMapper.selectPage(new Page<>(page, pageSize), new QueryWrapper<SysJobRunLogEntity>().eq("job_id", jobId).orderByDesc("started_at"));
        return new PageResult<>(result.getRecords(), result.getTotal(), page, pageSize);
    }

    private void validateCron(String cron) { if (cron == null || cron.isBlank() || cron.length() > 64 || cron.split("\\s+").length < 5) throw new BusinessException(400000, "cronExpression ????"); }
    private static void apply(SysJobEntity entity, Map<String, Object> body) { setString(body, "job_name", "jobName", entity::setJobName); setString(body, "job_code", "jobCode", entity::setJobCode); setString(body, "cron_expression", "cronExpression", entity::setCronExpression); setString(body, "bean_name", "beanName", entity::setBeanName); setString(body, "method_name", "methodName", entity::setMethodName); setString(body, "params", entity::setParams); setInteger(body, "status", entity::setStatus); setString(body, "remark", entity::setRemark); }
    private static String stringValue(Map<String, Object> body, String column) { Object value = body.containsKey(column) ? body.get(column) : body.get(snakeToCamel(column)); return value == null ? null : String.valueOf(value); }
    private static String snakeToCamel(String value) { StringBuilder out = new StringBuilder(); boolean upper = false; for (char c : value.toCharArray()) { if (c == '_') upper = true; else if (upper) { out.append(Character.toUpperCase(c)); upper = false; } else out.append(c); } return out.toString(); }
    private static void likeIfPresent(QueryWrapper<SysJobEntity> wrapper, String column, String value) { if (value != null && !value.isBlank()) wrapper.like(column, value); }
    private static void setString(Map<String, Object> body, String key, java.util.function.Consumer<String> setter) { if (body.containsKey(key)) setter.accept(String.valueOf(body.get(key))); }
    private static void setString(Map<String, Object> body, String snake, String camel, java.util.function.Consumer<String> setter) { Object value = body.containsKey(snake) ? body.get(snake) : body.get(camel); if (value != null) setter.accept(String.valueOf(value)); }
    private static void setInteger(Map<String, Object> body, String key, java.util.function.Consumer<Integer> setter) { if (body.containsKey(key)) setter.accept(intOf(body.get(key))); }
}
