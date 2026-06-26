package com.drip.admin.modules.system.job.controller;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drip.admin.common.exception.BusinessException;
import com.drip.admin.common.log.OperationLog;
import com.drip.admin.common.log.LogService;
import com.drip.admin.common.response.ApiResponse;
import com.drip.admin.common.response.BackupFile;
import com.drip.admin.common.response.PageResult;
import com.drip.admin.common.security.RequirePermission;
import com.drip.admin.modules.auth.dto.LoginRequest;
import com.drip.admin.modules.auth.dto.PasswordRequest;
import com.drip.admin.modules.auth.service.AuthService;
import com.drip.admin.modules.system.service.AdminService;
import com.drip.admin.shared.enums.TableMeta;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.apache.ibatis.annotations.Mapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static com.drip.admin.shared.utils.AdminUtils.*;


@RestController
@RequestMapping("/api/system")
public class JobController {
    private final AdminService adminService;

   public JobController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/jobs")
    @RequirePermission("system:job:list")
    public ApiResponse<PageResult<Map<String, Object>>> jobs(@RequestParam Map<String, String> q) {
        return ApiResponse.success(adminService.page("sys_job", q, List.of("job_name", "job_code", "status", "created_at")));
    }

    @GetMapping("/jobs/{id}")
    @RequirePermission("system:job:list")
    public ApiResponse<Map<String, Object>> job(@PathVariable long id) {
        return ApiResponse.success(adminService.detail("sys_job", id));
    }

    @PostMapping("/jobs")
    @RequirePermission("system:job:list")
    @OperationLog(module = "定时任务", action = "新增任务")
    public ApiResponse<Long> createJob(@RequestBody Map<String, Object> body) {
        adminService.validateCron(stringValue(body, "cron_expression", ""));
        return ApiResponse.success(adminService.insert("sys_job", body, Set.of("job_name", "job_code", "cron_expression", "bean_name", "method_name", "params", "status", "remark")));
    }

    @PutMapping("/jobs/{id}")
    @RequirePermission("system:job:list")
    @OperationLog(module = "定时任务", action = "编辑任务")
    public ApiResponse<Void> updateJob(@PathVariable long id, @RequestBody Map<String, Object> body) {
    if (body.containsKey("cron_expression")) adminService.validateCron(String.valueOf(body.get("cron_expression")));
        adminService.update("sys_job", id, body, Set.of("job_name", "job_code", "cron_expression", "bean_name", "method_name", "params", "status", "remark"));
        return ApiResponse.success(null);
    }

    @DeleteMapping("/jobs/{id}")
    @RequirePermission("system:job:list")
    @OperationLog(module = "定时任务", action = "删除任务")
    public ApiResponse<Void> deleteJob(@PathVariable long id) {
        adminService.softDelete("sys_job", id);
        return ApiResponse.success(null);
    }

    @PatchMapping("/jobs/{id}/status")
    @RequirePermission("system:job:list")
    @OperationLog(module = "定时任务", action = "变更任务状态")
    public ApiResponse<Void> jobStatus(@PathVariable long id, @RequestBody Map<String, Object> body) {
        adminService.updateStatus("sys_job", id, intValue(body, "status", 1), true);
        return ApiResponse.success(null);
    }

    @PostMapping("/jobs/{id}/run")
    @RequirePermission("system:job:list")
    @OperationLog(module = "定时任务", action = "手动执行任务")
    public ApiResponse<Void> runJob(@PathVariable long id) {
        adminService.runJob(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/jobs/{id}/run-logs")
    @RequirePermission("system:job:list")
    public ApiResponse<PageResult<Map<String, Object>>> jobLogs(@PathVariable long id, @RequestParam Map<String, String> q) {
        return ApiResponse.success(adminService.jobLogs(id, q));
    }
}
