package com.drip.admin.modules.system.dict.controller;

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
public class DictController {
    private final AdminService adminService;

   public DictController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dicts/types")
    @RequirePermission("system:dict:list")
    public ApiResponse<PageResult<Map<String, Object>>> dictTypes(@RequestParam Map<String, String> q) {
        return ApiResponse.success(adminService.page("sys_dict_type", q, List.of("dict_name", "dict_code", "status")));
    }

    @PostMapping("/dicts/types")
    @RequirePermission("system:dict:create")
    @OperationLog(module = "字典管理", action = "新增字典类型")
    public ApiResponse<Long> createDictType(@RequestBody Map<String, Object> body) {
        return ApiResponse.success(adminService.insert("sys_dict_type", body, Set.of("dict_name", "dict_code", "status", "remark")));
    }

    @PutMapping("/dicts/types/{id}")
    @RequirePermission("system:dict:update")
    @OperationLog(module = "字典管理", action = "编辑字典类型")
    public ApiResponse<Void> updateDictType(@PathVariable long id, @RequestBody Map<String, Object> body) {
        adminService.update("sys_dict_type", id, body, Set.of("dict_name", "dict_code", "status", "remark"));
        adminService.refreshDictCache();
        return ApiResponse.success(null);
    }

    @DeleteMapping("/dicts/types/{id}")
    @RequirePermission("system:dict:delete")
    @OperationLog(module = "字典管理", action = "删除字典类型")
    public ApiResponse<Void> deleteDictType(@PathVariable long id) {
        adminService.deleteDictType(id);
        adminService.refreshDictCache();
        return ApiResponse.success(null);
    }

    @GetMapping("/dicts/types/{id}/items")
    @RequirePermission("system:dict:list")
    public ApiResponse<List<Map<String, Object>>> dictItems(@PathVariable long id) {
        return ApiResponse.success(adminService.dictItems(id));
    }

    @PostMapping("/dicts/items")
    @RequirePermission("system:dict:create")
    @OperationLog(module = "字典管理", action = "新增字典项")
    public ApiResponse<Long> createDictItem(@RequestBody Map<String, Object> body) {
        Long id = adminService.insert("sys_dict_item", body, Set.of("dict_type_id", "label", "value", "color", "sort", "status"));
        adminService.refreshDictCache();
        return ApiResponse.success(id);
    }

    @PutMapping("/dicts/items/{id}")
    @RequirePermission("system:dict:update")
    @OperationLog(module = "字典管理", action = "编辑字典项")
    public ApiResponse<Void> updateDictItem(@PathVariable long id, @RequestBody Map<String, Object> body) {
        adminService.update("sys_dict_item", id, body, Set.of("dict_type_id", "label", "value", "color", "sort", "status"));
        adminService.refreshDictCache();
        return ApiResponse.success(null);
    }

    @DeleteMapping("/dicts/items/{id}")
    @RequirePermission("system:dict:delete")
    @OperationLog(module = "字典管理", action = "删除字典项")
    public ApiResponse<Void> deleteDictItem(@PathVariable long id) {
        adminService.deleteDictItem(id);
        adminService.refreshDictCache();
        return ApiResponse.success(null);
    }

    @PatchMapping("/dicts/items/{id}/status")
    @RequirePermission("system:dict:update")
    @OperationLog(module = "字典管理", action = "变更字典项状态")
    public ApiResponse<Void> dictItemStatus(@PathVariable long id, @RequestBody Map<String, Object> body) {
        adminService.updateStatus("sys_dict_item", id, intValue(body, "status", 1), true);
        adminService.refreshDictCache();
        return ApiResponse.success(null);
    }

    @PostMapping("/dicts/cache/refresh")
    @RequirePermission("system:dict:update")
    public ApiResponse<Void> refreshDictCache() {
        adminService.refreshDictCache();
        return ApiResponse.success(null);
    }
}
