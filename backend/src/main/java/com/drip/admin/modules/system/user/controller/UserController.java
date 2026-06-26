package com.drip.admin.modules.system.user.controller;

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
public class UserController {
    private final AdminService adminService;

   public UserController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    @RequirePermission("system:user:list")
    public ApiResponse<PageResult<Map<String, Object>>> users(@RequestParam Map<String, String> q) {
        return ApiResponse.success(adminService.page("sys_user", q, List.of("username", "real_name", "phone", "status", "dept_id", "created_at")));
    }

    @GetMapping("/users/{id}")
    @RequirePermission("system:user:detail")
    public ApiResponse<Map<String, Object>> user(@PathVariable long id) {
        return ApiResponse.success(adminService.detail("sys_user", id));
    }

    @PostMapping("/users")
    @RequirePermission("system:user:create")
    @OperationLog(module = "用户管理", action = "新增用户")
    public ApiResponse<Long> createUser(@RequestBody Map<String, Object> body) {
        return ApiResponse.success(adminService.createUser(body));
    }

    @PutMapping("/users/{id}")
    @RequirePermission("system:user:update")
    @OperationLog(module = "用户管理", action = "编辑用户")
    public ApiResponse<Void> updateUser(@PathVariable long id, @RequestBody Map<String, Object> body) {
        adminService.updateUser(id, body);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/users/{id}")
    @RequirePermission("system:user:delete")
    @OperationLog(module = "用户管理", action = "删除用户")
    public ApiResponse<Void> deleteUser(@PathVariable long id) {
        adminService.deleteUser(currentUserId(), id);
        return ApiResponse.success(null);
    }

    @PatchMapping("/users/{id}/status")
    @RequirePermission("system:user:disable")
    @OperationLog(module = "用户管理", action = "变更用户状态")
    public ApiResponse<Void> userStatus(@PathVariable long id, @RequestBody Map<String, Object> body) {
        int status = intValue(body, "status", 1);
        if (id == currentUserId() && status != 1) {
            throw new BusinessException(400000, "不能禁用当前登录用户");
        }
        adminService.updateStatus("sys_user", id, status, true);
        return ApiResponse.success(null);
    }

    @PutMapping("/users/{id}/roles")
    @RequirePermission("system:user:assign-role")
    @OperationLog(module = "用户管理", action = "分配角色")
    public ApiResponse<Void> userRoles(@PathVariable long id, @RequestBody Map<String, Object> body) {
        adminService.assignUserRoles(id, longList(body.get("roleIds")));
        return ApiResponse.success(null);
    }

    @PostMapping("/users/{id}/reset-password")
    @RequirePermission("system:user:reset-password")
    @OperationLog(module = "用户管理", action = "重置密码")
    public ApiResponse<Void> resetPassword(@PathVariable long id, @RequestBody Map<String, Object> body) {
        adminService.resetPassword(id, stringValue(body, "password", "Admin@123456"));
        return ApiResponse.success(null);
    }
}
