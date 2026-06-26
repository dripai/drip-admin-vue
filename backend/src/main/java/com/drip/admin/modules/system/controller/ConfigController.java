package com.drip.admin.modules.system.controller;

import com.drip.admin.common.log.OperationLog;
import com.drip.admin.common.response.ApiResponse;
import com.drip.admin.common.response.PageResult;
import com.drip.admin.common.security.RequirePermission;
import com.drip.admin.modules.system.dto.ConfigQuery;
import com.drip.admin.modules.system.dto.ConfigSaveRequest;
import com.drip.admin.modules.system.dto.StatusUpdateRequest;
import com.drip.admin.modules.system.entity.SysConfigEntity;
import com.drip.admin.modules.system.service.ConfigService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.drip.admin.shared.utils.AdminUtils.intValue;

@RestController
@RequestMapping("/api/system")
public class ConfigController {
    private final ConfigService configService;

    public ConfigController(ConfigService configService) {
        this.configService = configService;
    }

    @GetMapping("/configs")
    @RequirePermission("system:config:list")
    public ApiResponse<PageResult<SysConfigEntity>> configs(ConfigQuery query) {
        return ApiResponse.success(configService.page(query));
    }

    @PostMapping("/configs")
    @RequirePermission("system:config:create")
    @OperationLog(module = "系统配置", action = "新增配置")
    public ApiResponse<Long> createConfig(@RequestBody ConfigSaveRequest request) {
        return ApiResponse.success(configService.create(request));
    }

    @PutMapping("/configs/{id}")
    @RequirePermission("system:config:update")
    @OperationLog(module = "系统配置", action = "编辑配置")
    public ApiResponse<Void> updateConfig(@PathVariable long id, @RequestBody ConfigSaveRequest request) {
        configService.update(id, request);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/configs/{id}")
    @RequirePermission("system:config:delete")
    @OperationLog(module = "系统配置", action = "删除配置")
    public ApiResponse<Void> deleteConfig(@PathVariable long id) {
        configService.delete(id);
        return ApiResponse.success(null);
    }

    @PatchMapping("/configs/{id}/status")
    @RequirePermission("system:config:update")
    @OperationLog(module = "系统配置", action = "变更配置状态")
    public ApiResponse<Void> configStatus(@PathVariable long id, @RequestBody StatusUpdateRequest request) {
        configService.updateStatus(id, request.statusOrDefault());
        return ApiResponse.success(null);
    }
}
