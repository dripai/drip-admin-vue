package com.drip.admin.modules.system.controller;

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
import com.drip.admin.modules.system.dto.*;
import com.drip.admin.modules.system.service.AuthService;
import com.drip.admin.modules.system.entity.SysRoleEntity;
import com.drip.admin.modules.system.entity.SysUserEntity;
import com.drip.admin.modules.system.service.RoleService;
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
public class RoleController {
    private final RoleService roleService;

   public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping("/roles")
    @RequirePermission("system:role:list")
    public ApiResponse<PageResult<SysRoleEntity>> roles(RoleQuery query) {
        return ApiResponse.success(roleService.page(query));
    }

    @GetMapping("/roles/{id}")
    @RequirePermission("system:role:list")
    public ApiResponse<SysRoleEntity> role(@PathVariable long id) {
        return ApiResponse.success(roleService.detail(id));
    }

    @GetMapping("/roles/{id}/users")
    @RequirePermission("system:role:list")
    public ApiResponse<PageResult<SysUserEntity>> roleUsers(@PathVariable long id, RoleQuery query) {
        return ApiResponse.success(roleService.users(id, query));
    }

    @PostMapping("/roles")
    @RequirePermission("system:role:create")
    @OperationLog(module = "角色管理", action = "新增角色")
    public ApiResponse<Long> createRole(@RequestBody RoleSaveRequest request) {
        return ApiResponse.success(roleService.create(request));
    }

    @PutMapping("/roles/{id}")
    @RequirePermission("system:role:update")
    @OperationLog(module = "角色管理", action = "编辑角色")
    public ApiResponse<Void> updateRole(@PathVariable long id, @RequestBody RoleSaveRequest request) {
        roleService.update(id, request);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/roles/{id}")
    @RequirePermission("system:role:delete")
    @OperationLog(module = "角色管理", action = "删除角色")
    public ApiResponse<Void> deleteRole(@PathVariable long id) {
        roleService.delete(id);
        return ApiResponse.success(null);
    }

    @PatchMapping("/roles/{id}/status")
    @RequirePermission("system:role:update")
    @OperationLog(module = "角色管理", action = "变更角色状态")
    public ApiResponse<Void> roleStatus(@PathVariable long id, @RequestBody StatusUpdateRequest request) {
        roleService.updateStatus(id, request.statusOrDefault());
        return ApiResponse.success(null);
    }

    @PutMapping("/roles/{id}/permissions")
    @RequirePermission("system:role:permission")
    @OperationLog(module = "角色管理", action = "角色授权")
    public ApiResponse<Void> rolePermissions(@PathVariable long id, @RequestBody MenuAssignRequest request) {
        roleService.assignMenus(id, request.getMenuIds());
        return ApiResponse.success(null);
    }
}
