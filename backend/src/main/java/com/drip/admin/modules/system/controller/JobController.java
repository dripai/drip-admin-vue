package com.drip.admin.modules.system.controller;

import com.drip.admin.common.log.OperationLog;
import com.drip.admin.common.response.ApiResponse;
import com.drip.admin.common.response.PageResult;
import com.drip.admin.common.security.RequirePermission;
import com.drip.admin.modules.system.dto.JobQuery;
import com.drip.admin.modules.system.dto.JobRunLogQuery;
import com.drip.admin.modules.system.dto.JobSaveRequest;
import com.drip.admin.modules.system.dto.StatusUpdateRequest;
import com.drip.admin.modules.system.entity.SysJobEntity;
import com.drip.admin.modules.system.entity.SysJobRunLogEntity;
import com.drip.admin.modules.system.service.JobService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/system")
public class JobController {
    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping("/job")
    @RequirePermission("system:job:list")
    public ApiResponse<PageResult<SysJobEntity>> jobs(@Valid JobQuery query) {
        return ApiResponse.success(jobService.page(query));
    }

    @GetMapping("/job/{id}")
    @RequirePermission("system:job:list")
    public ApiResponse<SysJobEntity> job(@PathVariable long id) {
        return ApiResponse.success(jobService.detail(id));
    }

    @PostMapping("/job")
    @RequirePermission("system:job:create")
    @OperationLog(module = "定时任务", action = "新增任务")
    public ApiResponse<Long> createJob(@Valid @RequestBody JobSaveRequest request) {
        return ApiResponse.success(jobService.create(request));
    }

    @PutMapping("/job/{id}")
    @RequirePermission("system:job:update")
    @OperationLog(module = "定时任务", action = "编辑任务")
    public ApiResponse<Void> updateJob(@PathVariable long id, @Valid @RequestBody JobSaveRequest request) {
        jobService.update(id, request);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/job/{id}")
    @RequirePermission("system:job:delete")
    @OperationLog(module = "定时任务", action = "删除任务")
    public ApiResponse<Void> deleteJob(@PathVariable long id) {
        jobService.delete(id);
        return ApiResponse.success(null);
    }

    @PutMapping("/job/{id}/status")
    @RequirePermission("system:job:update")
    @OperationLog(module = "定时任务", action = "变更任务状态")
    public ApiResponse<Void> jobStatus(@PathVariable long id, @Valid @RequestBody StatusUpdateRequest request) {
        jobService.updateStatus(id, request.statusOrDefault());
        return ApiResponse.success(null);
    }

    @PostMapping("/job/{id}/run")
    @RequirePermission("system:job:run")
    @OperationLog(module = "定时任务", action = "手动执行任务")
    public ApiResponse<Void> runJob(@PathVariable long id) {
        jobService.run(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/job/{id}/runLog")
    @RequirePermission("system:job:list")
    public ApiResponse<PageResult<SysJobRunLogEntity>> jobLogs(@PathVariable long id, @Valid JobRunLogQuery query) {
        return ApiResponse.success(jobService.runLogs(id, query));
    }
}
