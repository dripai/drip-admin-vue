package com.drip.admin.modules.system.log.controller;

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
public class SystemLogController {
    private final AdminService adminService;

   public SystemLogController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/login-logs")
    @RequirePermission("system:login-log:list")
    public ApiResponse<PageResult<Map<String, Object>>> loginLogs(@RequestParam Map<String, String> q) {
        return ApiResponse.success(adminService.pageReadonly("sys_login_log", q, "login_at"));
    }

    @GetMapping("/login-logs/{id}")
    @RequirePermission("system:login-log:list")
    public ApiResponse<Map<String, Object>> loginLog(@PathVariable long id) {
        return ApiResponse.success(adminService.detail("sys_login_log", id));
    }

    @GetMapping("/operation-logs")
    @RequirePermission("system:operation-log:list")
    public ApiResponse<PageResult<Map<String, Object>>> operationLogs(@RequestParam Map<String, String> q) {
        return ApiResponse.success(adminService.pageReadonly("sys_operation_log", q, "created_at"));
    }

    @GetMapping("/operation-logs/{id}")
    @RequirePermission("system:operation-log:list")
    public ApiResponse<Map<String, Object>> operationLog(@PathVariable long id) {
        return ApiResponse.success(adminService.detail("sys_operation_log", id));
}
}
