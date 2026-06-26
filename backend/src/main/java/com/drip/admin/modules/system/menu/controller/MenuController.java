package com.drip.admin.modules.system.menu.controller;

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
public class MenuController {
    private final AdminService adminService;

   public MenuController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/menus")
    @RequirePermission("system:menu:list")
    public ApiResponse<List<Map<String, Object>>> menus() {
        return ApiResponse.success(adminService.menuTree(null));
    }

    @PostMapping("/menus")
    @RequirePermission("system:menu:create")
    @OperationLog(module = "菜单管理", action = "新增菜单")
    public ApiResponse<Long> createMenu(@RequestBody Map<String, Object> body) {
        return ApiResponse.success(adminService.insert("sys_menu", body, Set.of("parent_id", "name", "type", "path", "component", "permission_code", "icon", "sort", "visible", "status")));
    }

    @PutMapping("/menus/{id}")
    @RequirePermission("system:menu:update")
    @OperationLog(module = "菜单管理", action = "编辑菜单")
    public ApiResponse<Void> updateMenu(@PathVariable long id, @RequestBody Map<String, Object> body) {
        adminService.update("sys_menu", id, body, Set.of("parent_id", "name", "type", "path", "component", "permission_code", "icon", "sort", "visible", "status"));
        return ApiResponse.success(null);
    }

    @DeleteMapping("/menus/{id}")
    @RequirePermission("system:menu:delete")
    @OperationLog(module = "菜单管理", action = "删除菜单")
    public ApiResponse<Void> deleteMenu(@PathVariable long id) {
        adminService.deleteMenu(id);
        return ApiResponse.success(null);
    }

    @PatchMapping("/menus/{id}/status")
    @RequirePermission("system:menu:status")
    @OperationLog(module = "菜单管理", action = "变更菜单状态")
    public ApiResponse<Void> menuStatus(@PathVariable long id, @RequestBody Map<String, Object> body) {
        adminService.updateStatus("sys_menu", id, intValue(body, "status", 1), true);
        return ApiResponse.success(null);
    }
}
