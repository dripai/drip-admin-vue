package com.drip.admin.common.log;

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
import com.drip.admin.modules.system.dto.LoginRequest;
import com.drip.admin.modules.system.dto.PasswordRequest;
import com.drip.admin.modules.system.service.AuthService;
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
public class LogService {
    private static final int OPERATION_LOG_TEXT_LIMIT = 8192;

    private final JdbcTemplate jdbc;

    public LogService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

   public  void login(Long userId, String username, String realName, String loginType, String status, String reason, HttpServletRequest request, String deviceType) {
        jdbc.update("""
    insert into sys_login_log (user_id, username, real_name, login_type, status, failure_reason, ip, user_agent, device_type)
    values (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """, userId, username, realName, loginType, status, reason, clientIp(request), request.getHeader("User-Agent"), deviceType);
    }

   public  void operation(String module, String action, String method, String path, String requestParams, String responseStatus, String errorMessage, long costMs) {
        Long userId = null;
        String operatorName = null;
    if (StpUtil.isLogin()) {
            userId = currentUserId();
            String realName = String.valueOf(StpUtil.getSession().get("realName", ""));
            String username = String.valueOf(StpUtil.getSession().get("username", ""));
            operatorName = !realName.isBlank() ? realName : (!username.isBlank() ? username : userDisplayName(userId));
        }
        jdbc.update("""
    insert into sys_operation_log (operator_id, operator_name, module, action, method, path, request_params, response_status, error_message, cost_ms)
    values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """, userId, operatorName, module, action, method, path, limitText(requestParams), responseStatus, limitText(errorMessage), costMs);
    }

    private static String limitText(String value) {
        if (value == null || value.length() <= OPERATION_LOG_TEXT_LIMIT) return value;
        return value.substring(0, OPERATION_LOG_TEXT_LIMIT);
    }

    private String userDisplayName(Long userId) {
        if (userId == null) return null;
        List<String> names = jdbc.query("""
            select coalesce(nullif(real_name, ''), nullif(username, ''), cast(id as char))
            from sys_user
            where id = ? and deleted = 0
            """, (rs, rowNum) -> rs.getString(1), userId);
        return names.isEmpty() ? String.valueOf(userId) : names.get(0);
    }
}
