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

@Service
public class JobServiceImpl extends ServiceImpl<SysJobMapper, SysJobEntity> implements JobService {
    private final SysJobRunLogMapper jobRunLogMapper;
    private final JobExecutorRegistry jobExecutorRegistry;
    public JobServiceImpl(SysJobRunLogMapper jobRunLogMapper, JobExecutorRegistry jobExecutorRegistry) { this.jobRunLogMapper = jobRunLogMapper; this.jobExecutorRegistry = jobExecutorRegistry; }
    @Override public PageResult<SysJobEntity> page(JobQuery query) { int page = query.pageOrDefault(); int pageSize = query.pageSizeOrDefault(); QueryWrapper<SysJobEntity> wrapper = new QueryWrapper<>(); likeIfPresent(wrapper, "job_name", query.getJobName()); likeIfPresent(wrapper, "job_code", query.getJobCode()); eqIfPresent(wrapper, "status", query.getStatus()); likeIfPresent(wrapper, "created_at", query.getCreatedAt()); wrapper.orderByDesc("created_at"); Page<SysJobEntity> result = page(new Page<>(page, pageSize), wrapper); return new PageResult<>(result.getRecords(), result.getTotal(), page, pageSize); }
    @Override public SysJobEntity detail(long id) { SysJobEntity entity = getById(id); if (entity == null) throw new BusinessException(404000, "?????"); return entity; }
    @Override @Transactional public Long create(JobSaveRequest request) { requireText(request.getJobName(), "jobName"); requireText(request.getJobCode(), "jobCode"); requireText(request.getCronExpression(), "cronExpression"); validateCron(request.getCronExpression()); SysJobEntity entity = new SysJobEntity(); apply(entity, request); save(entity); return entity.getId(); }
    @Override @Transactional public void update(long id, JobSaveRequest request) { detail(id); if (request.getCronExpression() != null) validateCron(request.getCronExpression()); SysJobEntity entity = new SysJobEntity(); entity.setId(id); apply(entity, request); updateById(entity); }
    @Override @Transactional public void delete(long id) { detail(id); removeById(id); }
    @Override @Transactional public void updateStatus(long id, int status) { detail(id); SysJobEntity entity = new SysJobEntity(); entity.setId(id); entity.setStatus(status); updateById(entity); }
    @Override public void run(long id) { SysJobEntity job = detail(id); LocalDateTime started = LocalDateTime.now(); long startedMs = System.currentTimeMillis(); try { jobExecutorRegistry.execute(job.getBeanName(), job.getMethodName()); SysJobRunLogEntity log = new SysJobRunLogEntity(); log.setJobId(id); log.setJobName(job.getJobName()); log.setStatus("SUCCESS"); log.setStartedAt(started); log.setFinishedAt(LocalDateTime.now()); log.setCostMs(System.currentTimeMillis() - startedMs); jobRunLogMapper.insert(log); } catch (RuntimeException ex) { SysJobRunLogEntity log = new SysJobRunLogEntity(); log.setJobId(id); log.setJobName(job.getJobName()); log.setStatus("FAIL"); log.setStartedAt(started); log.setFinishedAt(LocalDateTime.now()); log.setCostMs(System.currentTimeMillis() - startedMs); log.setErrorMessage(ex.getMessage()); jobRunLogMapper.insert(log); throw ex; } }
    @Override public PageResult<SysJobRunLogEntity> runLogs(long jobId, JobRunLogQuery query) { int page = query.pageOrDefault(); int pageSize = query.pageSizeOrDefault(); Page<SysJobRunLogEntity> result = jobRunLogMapper.selectPage(new Page<>(page, pageSize), new QueryWrapper<SysJobRunLogEntity>().eq("job_id", jobId).orderByDesc("started_at")); return new PageResult<>(result.getRecords(), result.getTotal(), page, pageSize); }
    private void validateCron(String cron) { if (cron == null || cron.isBlank() || cron.length() > 64 || cron.split("\\s+").length < 5) throw new BusinessException(400000, "cronExpression ???"); }
    private static void apply(SysJobEntity entity, JobSaveRequest request) { entity.setJobName(request.getJobName()); entity.setJobCode(request.getJobCode()); entity.setCronExpression(request.getCronExpression()); entity.setBeanName(request.getBeanName()); entity.setMethodName(request.getMethodName()); entity.setParams(request.getParams()); entity.setStatus(request.getStatus()); entity.setRemark(request.getRemark()); }
    private static void likeIfPresent(QueryWrapper<SysJobEntity> wrapper, String column, String value) { if (value != null && !value.isBlank()) wrapper.like(column, value); }
    private static void eqIfPresent(QueryWrapper<SysJobEntity> wrapper, String column, Object value) { if (value != null) wrapper.eq(column, value); }
    private static void requireText(String value, String field) { if (value == null || value.isBlank()) throw new BusinessException(400000, field + "????"); }
}
