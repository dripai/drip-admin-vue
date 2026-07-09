package com.drip.admin.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drip.admin.common.exception.BusinessException;
import com.drip.admin.common.response.PageResult;
import com.drip.admin.infrastructure.external.JobExecutorRegistry;
import com.drip.admin.modules.system.dto.JobQuery;
import com.drip.admin.modules.system.dto.JobRunLogQuery;
import com.drip.admin.modules.system.dto.JobSaveRequest;
import com.drip.admin.modules.system.entity.SysJobEntity;
import com.drip.admin.modules.system.entity.SysJobRunLogEntity;
import com.drip.admin.modules.system.mapper.SysJobMapper;
import com.drip.admin.modules.system.mapper.SysJobRunLogMapper;
import com.drip.admin.modules.system.service.JobService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class JobServiceImpl extends ServiceImpl<SysJobMapper, SysJobEntity> implements JobService {
    private static final Set<String> SCRIPT_EXECUTOR_TYPES = Set.of("shell", "bat", "powershell", "ps1", "python");
    private final SysJobRunLogMapper jobRunLogMapper;
    private final JobExecutorRegistry jobExecutorRegistry;
    public JobServiceImpl(SysJobRunLogMapper jobRunLogMapper, JobExecutorRegistry jobExecutorRegistry) { this.jobRunLogMapper = jobRunLogMapper; this.jobExecutorRegistry = jobExecutorRegistry; }
    @Override public PageResult<SysJobEntity> page(JobQuery query) { int page = query.pageOrDefault(); int pageSize = query.pageSizeOrDefault(); QueryWrapper<SysJobEntity> wrapper = new QueryWrapper<>(); likeIfPresent(wrapper, "job_name", query.getJobName()); likeIfPresent(wrapper, "remark", query.getRemark()); eqIfPresent(wrapper, "status", query.getStatus()); likeIfPresent(wrapper, "created_at", query.getCreatedAt()); wrapper.orderByDesc("created_at"); Page<SysJobEntity> result = page(new Page<>(page, pageSize), wrapper); return new PageResult<>(result.getRecords(), result.getTotal(), page, pageSize); }
    @Override public SysJobEntity detail(long id) { SysJobEntity entity = getById(id); if (entity == null) throw new BusinessException(404000, "operation failed"); return entity; }
    @Override @Transactional public Long create(JobSaveRequest request) { requireText(request.getJobName(), "jobName"); requireText(request.getCronExpression(), "cronExpression"); validateCron(request.getCronExpression()); validateExecutor(request); SysJobEntity entity = new SysJobEntity(); apply(entity, request); save(entity); return entity.getId(); }
    @Override @Transactional public void update(long id, JobSaveRequest request) { detail(id); if (request.getCronExpression() != null) validateCron(request.getCronExpression()); validateExecutor(request); SysJobEntity entity = new SysJobEntity(); entity.setId(id); apply(entity, request); updateById(entity); }
    @Override @Transactional public void delete(long id) { detail(id); removeById(id); }
    @Override @Transactional public void updateStatus(long id, int status) { detail(id); SysJobEntity entity = new SysJobEntity(); entity.setId(id); entity.setStatus(status); updateById(entity); }
    @Override public void run(long id) { run(detail(id)); }
    @Override public void run(SysJobEntity job) { LocalDateTime started = LocalDateTime.now(); long startedMs = System.currentTimeMillis(); SysJobRunLogEntity log = insertRunLog(job, "RUNNING", started, startedMs, null); try { jobExecutorRegistry.execute(job); updateRunLog(log, "SUCCESS", startedMs, null); } catch (RuntimeException ex) { updateRunLog(log, "FAIL", startedMs, ex.getMessage()); throw ex; } }
    @Override public PageResult<SysJobRunLogEntity> runLogs(long jobId, JobRunLogQuery query) { QueryWrapper<SysJobRunLogEntity> wrapper = runLogWrapper(query).eq("job_id", jobId); return runLogPage(query, wrapper); }
    @Override public PageResult<SysJobRunLogEntity> runLogs(JobRunLogQuery query) { return runLogPage(query, runLogWrapper(query)); }
    private void validateCron(String cron) { if (cron == null || cron.isBlank() || cron.length() > 64 || cron.split("\\s+").length < 5) throw new BusinessException(400000, "cronExpression format is invalid"); }
    private static void apply(SysJobEntity entity, JobSaveRequest request) { entity.setJobName(request.getJobName()); entity.setCronExpression(request.getCronExpression()); entity.setExecutorType(normalizeExecutorType(request.getExecutorType())); entity.setScriptFile(request.getScriptFile()); entity.setScriptArgs(request.getScriptArgs()); entity.setStatus(request.getStatus()); entity.setRemark(request.getRemark()); }
    private static void likeIfPresent(QueryWrapper<SysJobEntity> wrapper, String column, String value) { if (value != null && !value.isBlank()) wrapper.like(column, value); }
    private static void eqIfPresent(QueryWrapper<SysJobEntity> wrapper, String column, Object value) { if (value != null) wrapper.eq(column, value); }
    private static void requireText(String value, String field) { if (value == null || value.isBlank()) throw new BusinessException(400000, field + " is required"); }
    private static void validateExecutor(JobSaveRequest request) { String executorType = normalizeExecutorType(request.getExecutorType()); if (!SCRIPT_EXECUTOR_TYPES.contains(executorType)) throw new BusinessException(400000, "executorType is not supported"); requireText(request.getScriptFile(), "scriptFile"); }
    private static String normalizeExecutorType(String executorType) { if (executorType == null || executorType.isBlank()) throw new BusinessException(400000, "executorType is required"); return executorType.trim().toLowerCase(Locale.ROOT); }
    private SysJobRunLogEntity insertRunLog(SysJobEntity job, String status, LocalDateTime started, long startedMs, String errorMessage) { SysJobRunLogEntity log = new SysJobRunLogEntity(); log.setJobId(job.getId()); log.setJobName(job.getJobName()); log.setStatus(status); log.setStartedAt(started); log.setCostMs(System.currentTimeMillis() - startedMs); log.setErrorMessage(truncate(errorMessage)); jobRunLogMapper.insert(log); return log; }
    private void updateRunLog(SysJobRunLogEntity log, String status, long startedMs, String errorMessage) { SysJobRunLogEntity update = new SysJobRunLogEntity(); update.setId(log.getId()); update.setStatus(status); update.setFinishedAt(LocalDateTime.now()); update.setCostMs(System.currentTimeMillis() - startedMs); update.setErrorMessage(truncate(errorMessage)); jobRunLogMapper.updateById(update); }
    private PageResult<SysJobRunLogEntity> runLogPage(JobRunLogQuery query, QueryWrapper<SysJobRunLogEntity> wrapper) { int page = query.pageOrDefault(); int pageSize = query.pageSizeOrDefault(); wrapper.orderByDesc("started_at"); Page<SysJobRunLogEntity> result = jobRunLogMapper.selectPage(new Page<>(page, pageSize), wrapper); return new PageResult<>(result.getRecords(), result.getTotal(), page, pageSize); }
    private static QueryWrapper<SysJobRunLogEntity> runLogWrapper(JobRunLogQuery query) { QueryWrapper<SysJobRunLogEntity> wrapper = new QueryWrapper<>(); if (query.getJobName() != null && !query.getJobName().isBlank()) wrapper.like("job_name", query.getJobName()); if (query.getStatus() != null && !query.getStatus().isBlank()) wrapper.eq("status", query.getStatus()); List<String> range = query.getStartedRange(); if (range != null && range.size() == 2) wrapper.between("started_at", range.get(0), range.get(1)); return wrapper; }
    private static String truncate(String value) { if (value == null || value.length() <= 512) return value; return value.substring(0, 512); }
}
