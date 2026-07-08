package com.drip.admin.modules.system.controller;

import com.drip.admin.common.log.OperationLog;
import com.drip.admin.common.response.ApiResponse;
import com.drip.admin.common.response.PageResult;
import com.drip.admin.common.security.RequirePermission;
import com.drip.admin.modules.system.dto.PrintTemplateCopyRequest;
import com.drip.admin.modules.system.dto.PrintTemplateQuery;
import com.drip.admin.modules.system.dto.PrintTemplateSaveRequest;
import com.drip.admin.modules.system.dto.StatusUpdateRequest;
import com.drip.admin.modules.system.entity.SysPrintTemplateEntity;
import com.drip.admin.modules.system.service.PrintTemplateService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/print-template")
public class PrintTemplateController {
    private final PrintTemplateService printTemplateService;

    public PrintTemplateController(PrintTemplateService printTemplateService) {
        this.printTemplateService = printTemplateService;
    }

    @GetMapping
    @RequirePermission("system:printTemplate:list")
    public ApiResponse<PageResult<SysPrintTemplateEntity>> templates(@Valid PrintTemplateQuery query) {
        return ApiResponse.success(printTemplateService.page(query));
    }

    @GetMapping("/{id}")
    @RequirePermission("system:printTemplate:list")
    public ApiResponse<SysPrintTemplateEntity> detail(@PathVariable long id) {
        return ApiResponse.success(printTemplateService.detail(id));
    }

    @PostMapping
    @RequirePermission("system:printTemplate:create")
    @OperationLog(module = "打印模板", action = "新增打印模板")
    public ApiResponse<Long> create(@Valid @RequestBody PrintTemplateSaveRequest request) {
        return ApiResponse.success(printTemplateService.create(request));
    }

    @PostMapping("/{id}/copy")
    @RequirePermission("system:printTemplate:create")
    @OperationLog(module = "打印模板", action = "复制打印模板")
    public ApiResponse<Long> copy(@PathVariable long id, @Valid @RequestBody PrintTemplateCopyRequest request) {
        return ApiResponse.success(printTemplateService.copy(id, request));
    }

    @PutMapping("/{id}")
    @RequirePermission("system:printTemplate:update")
    @OperationLog(module = "打印模板", action = "编辑打印模板")
    public ApiResponse<Void> update(@PathVariable long id, @Valid @RequestBody PrintTemplateSaveRequest request) {
        printTemplateService.update(id, request);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    @RequirePermission("system:printTemplate:delete")
    @OperationLog(module = "打印模板", action = "删除打印模板")
    public ApiResponse<Void> delete(@PathVariable long id) {
        printTemplateService.delete(id);
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}/status")
    @RequirePermission("system:printTemplate:update")
    @OperationLog(module = "打印模板", action = "变更打印模板状态")
    public ApiResponse<Void> status(@PathVariable long id, @Valid @RequestBody StatusUpdateRequest request) {
        printTemplateService.updateStatus(id, request.statusOrDefault());
        return ApiResponse.success(null);
    }
}
