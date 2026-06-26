package com.drip.admin.modules.auth.service;

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
import com.drip.admin.infrastructure.redis.LoginAttemptService;
import com.drip.admin.infrastructure.redis.OnlineSessionService;
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

@Service
public class AuthService {
    private final JdbcTemplate jdbc;
    private final LogService logService;
    private final AdminService adminService;
    private final OnlineSessionService onlineSessionService;
    private final LoginAttemptService loginAttemptService;
    private final long idleTimeout;
    private final long maxDuration;

    public AuthService(JdbcTemplate jdbc, LogService logService, AdminService adminService, OnlineSessionService onlineSessionService,
                LoginAttemptService loginAttemptService,
                @Value("${drip.session.idle-timeout-seconds}") long idleTimeout,
                @Value("${drip.session.max-duration-seconds}") long maxDuration) {
        this.jdbc = jdbc;
        this.logService = logService;
        this.adminService = adminService;
        this.onlineSessionService = onlineSessionService;
        this.loginAttemptService = loginAttemptService;
        this.idleTimeout = idleTimeout;
        this.maxDuration = maxDuration;
    }

    @Transactional
    public Map<String, Object> login(LoginRequest request, HttpServletRequest servletRequest) {
        loginAttemptService.assertNotLocked(request.username());
        Map<String, Object> user = findUserByUsername(request.username());
    if (user.isEmpty()) {
            logService.login(null, request.username(), null, "LOGIN", "FAIL", "用户名或密码错误", servletRequest, request.deviceType());
            loginAttemptService.recordFailure(request.username());
    throw new BusinessException(401000, "用户名或密码错误");
        }
    if (intOf(user.get("status")) != 1 || intOf(user.get("deleted")) == 1) {
            logService.login(longOf(user.get("id")), request.username(), stringOf(user.get("real_name")), "LOGIN", "FAIL", "用户已禁用或删除", servletRequest, request.deviceType());
            loginAttemptService.recordFailure(request.username());
    throw new BusinessException(401000, "用户名或密码错误");
        }
        String expected = hashPassword(request.password(), stringOf(user.get("password_salt")));
    if (!expected.equals(stringOf(user.get("password_hash")))) {
            logService.login(longOf(user.get("id")), request.username(), stringOf(user.get("real_name")), "LOGIN", "FAIL", "用户名或密码错误", servletRequest, request.deviceType());
            loginAttemptService.recordFailure(request.username());
    throw new BusinessException(401000, "用户名或密码错误");
        }
        loginAttemptService.clear(request.username());
        Long userId = longOf(user.get("id"));
        StpUtil.login(userId);
        String token = StpUtil.getTokenValue();
        LocalDateTime now = LocalDateTime.now();
        StpUtil.getSession().set("deviceType", request.deviceType());
        StpUtil.getSession().set("loginAt", now.toString());
        StpUtil.getSession().set("lastActiveAt", now.toString());
        StpUtil.getSession().set("tokenId", token);
        jdbc.update("update sys_user set last_login_at = now() where id = ?", userId);
        logService.login(userId, request.username(), stringOf(user.get("real_name")), "LOGIN", "SUCCESS", null, servletRequest, request.deviceType());
        onlineSessionService.register(userId, user, token, request.deviceType(), idleTimeout, maxDuration, servletRequest);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("token", token);
        data.put("expireAt", now.plusSeconds(idleTimeout).atZone(ZoneId.systemDefault()).toInstant().toString());
        data.put("idleTimeout", idleTimeout);
        data.put("maxSessionDuration", maxDuration);
        data.put("deviceType", request.deviceType());
        return data;
    }

    @Transactional
    public void logout(HttpServletRequest request) {
        Long userId = currentUserId();
        Map<String, Object> user = adminService.detail("sys_user", userId);
        String deviceType = String.valueOf(StpUtil.getSession().get("deviceType", ""));
        logService.login(userId, stringOf(user.get("username")), stringOf(user.get("real_name")), "LOGOUT", "SUCCESS", null, request, deviceType);
        onlineSessionService.remove(StpUtil.getTokenValue());
        StpUtil.logout();
    }

   public  Map<String, Object> me(long userId) {
        Map<String, Object> user = adminService.detail("sys_user", userId);
        List<String> roles = adminService.roleCodes(userId);
        List<String> permissions = adminService.permissionCodes(userId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", user.get("id"));
        result.put("username", user.get("username"));
        result.put("realName", user.get("real_name"));
        result.put("avatar", user.get("avatar"));
        result.put("deptId", user.get("dept_id"));
        result.put("roles", roles);
        result.put("permissions", permissions);
        result.put("menus", adminService.menuTree(userId));
        return result;
    }

    @Transactional
    public void changePassword(long userId, PasswordRequest request) {
        Map<String, Object> user = adminService.detail("sys_user", userId);
        String currentHash = hashPassword(request.oldPassword(), stringOf(user.get("password_salt")));
    if (!currentHash.equals(stringOf(user.get("password_hash")))) {
    throw new BusinessException(400000, "原密码错误");
        }
        String salt = "salt" + System.nanoTime();
        jdbc.update("update sys_user set password_salt = ?, password_hash = ? where id = ?", salt, hashPassword(request.newPassword(), salt), userId);
    }

   private Map<String, Object> findUserByUsername(String username) {
        List<Map<String, Object>> rows = jdbc.queryForList("select * from sys_user where username = ? and deleted = 0", username);
        return rows.isEmpty() ? Map.of() : rows.getFirst();
    }
}
