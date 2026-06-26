package com.drip.admin.modules.system.controller;

import com.drip.admin.common.response.ApiResponse;
import com.drip.admin.common.response.PageResult;
import com.drip.admin.common.security.RequirePermission;
import com.drip.admin.modules.system.dto.LoginLogQuery;
import com.drip.admin.modules.system.dto.OperationLogQuery;
import com.drip.admin.modules.system.entity.SysLoginLogEntity;
import com.drip.admin.modules.system.entity.SysOperationLogEntity;
import com.drip.admin.modules.system.service.SystemLogQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system")
public class SystemLogController {
    private final SystemLogQueryService logQueryService;

    public SystemLogController(SystemLogQueryService logQueryService) {
        this.logQueryService = logQueryService;
    }

    @GetMapping("/loginLogs")
    @RequirePermission("system:loginLog:list")
    public ApiResponse<PageResult<SysLoginLogEntity>> loginLogs(LoginLogQuery query) {
        return ApiResponse.success(logQueryService.loginLogs(query));
    }

    @GetMapping("/loginLogs/{id}")
    @RequirePermission("system:loginLog:list")
    public ApiResponse<SysLoginLogEntity> loginLog(@PathVariable long id) {
        return ApiResponse.success(logQueryService.loginLog(id));
    }

    @GetMapping("/operationLogs")
    @RequirePermission("system:operationLog:list")
    public ApiResponse<PageResult<SysOperationLogEntity>> operationLogs(OperationLogQuery query) {
        return ApiResponse.success(logQueryService.operationLogs(query));
    }

    @GetMapping("/operationLogs/{id}")
    @RequirePermission("system:operationLog:list")
    public ApiResponse<SysOperationLogEntity> operationLog(@PathVariable long id) {
        return ApiResponse.success(logQueryService.operationLog(id));
    }
}
