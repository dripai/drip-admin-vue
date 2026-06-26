package com.drip.admin.modules.system.database.controller;

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
public class DatabaseBackupController {
    private final AdminService adminService;

   public DatabaseBackupController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/database/backups")
    @RequirePermission("system:database:backup:list")
    public ApiResponse<PageResult<Map<String, Object>>> backups(@RequestParam Map<String, String> q) {
        return ApiResponse.success(adminService.pageReadonly("sys_db_backup", q, "created_at"));
    }

    @PostMapping("/database/backups")
    @RequirePermission("system:database:backup:create")
    @OperationLog(module = "数据库备份", action = "创建备份")
    public ApiResponse<Long> createBackup(@RequestBody(required = false) Map<String, Object> body) {
        return ApiResponse.success(adminService.createBackup(Optional.ofNullable(body).orElseGet(Map::of), currentUserId()));
    }

    @GetMapping(value = "/database/backups/{id}/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @RequirePermission("system:database:backup:download")
    public ResponseEntity<byte[]> downloadBackup(@PathVariable long id) throws IOException {
        BackupFile file = adminService.downloadBackup(id);
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=\"" + file.name() + "\"")
            .body(file.content());
    }

    @PostMapping("/database/backups/{id}/restore")
    @RequirePermission("system:database:backup:restore")
    @OperationLog(module = "数据库备份", action = "恢复备份")
    public ApiResponse<Void> restoreBackup(@PathVariable long id, @RequestBody Map<String, Object> body) {
        adminService.restoreBackup(id, body);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/database/backups/{id}")
    @RequirePermission("system:database:backup:delete")
    @OperationLog(module = "数据库备份", action = "删除备份记录")
    public ApiResponse<Void> deleteBackup(@PathVariable long id) {
        adminService.deleteBackup(id);
        return ApiResponse.success(null);
    }
}
