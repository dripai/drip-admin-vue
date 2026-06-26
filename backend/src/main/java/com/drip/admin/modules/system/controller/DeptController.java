package com.drip.admin.modules.system.controller;

import com.drip.admin.common.log.OperationLog;
import com.drip.admin.common.response.ApiResponse;
import com.drip.admin.common.security.RequirePermission;
import com.drip.admin.modules.system.dto.DeptSaveRequest;
import com.drip.admin.modules.system.dto.StatusUpdateRequest;
import com.drip.admin.modules.system.entity.SysDeptEntity;
import com.drip.admin.modules.system.service.DeptService;
import com.drip.admin.modules.system.vo.DeptTreeVo;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
@RequestMapping("/system")
public class DeptController {
    private final DeptService deptService;

    public DeptController(DeptService deptService) {
        this.deptService = deptService;
    }

    @GetMapping("/dept")
    @RequirePermission("system:dept:list")
    public ApiResponse<List<DeptTreeVo>> depts() {
        return ApiResponse.success(deptService.tree());
    }

    @GetMapping("/dept/{id}")
    @RequirePermission("system:dept:list")
    public ApiResponse<SysDeptEntity> dept(@PathVariable long id) {
        return ApiResponse.success(deptService.detail(id));
    }

    @PostMapping("/dept")
    @RequirePermission("system:dept:create")
    @OperationLog(module = "部门管理", action = "新增部门")
    public ApiResponse<Long> createDept(@Valid @RequestBody DeptSaveRequest request) {
        return ApiResponse.success(deptService.create(request));
    }

    @PutMapping("/dept/{id}")
    @RequirePermission("system:dept:update")
    @OperationLog(module = "部门管理", action = "编辑部门")
    public ApiResponse<Void> updateDept(@PathVariable long id, @Valid @RequestBody DeptSaveRequest request) {
        deptService.update(id, request);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/dept/{id}")
    @RequirePermission("system:dept:delete")
    @OperationLog(module = "部门管理", action = "删除部门")
    public ApiResponse<Void> deleteDept(@PathVariable long id) {
        deptService.delete(id);
        return ApiResponse.success(null);
    }

    @PutMapping("/dept/{id}/status")
    @RequirePermission("system:dept:update")
    @OperationLog(module = "部门管理", action = "变更部门状态")
    public ApiResponse<Void> deptStatus(@PathVariable long id, @Valid @RequestBody StatusUpdateRequest request) {
        deptService.updateStatus(id, request.statusOrDefault());
        return ApiResponse.success(null);
    }
}
