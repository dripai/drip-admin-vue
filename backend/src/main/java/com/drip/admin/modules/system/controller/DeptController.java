package com.drip.admin.modules.system.controller;

import com.drip.admin.common.log.OperationLog;
import com.drip.admin.common.response.ApiResponse;
import com.drip.admin.common.security.RequirePermission;
import com.drip.admin.modules.system.service.DeptService;
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
import java.util.Map;

import static com.drip.admin.shared.utils.AdminUtils.intValue;

@RestController
@RequestMapping("/api/system")
public class DeptController {
    private final DeptService deptService;

    public DeptController(DeptService deptService) {
        this.deptService = deptService;
    }

    @GetMapping("/depts")
    @RequirePermission("system:dept:list")
    public ApiResponse<List<Map<String, Object>>> depts() {
        return ApiResponse.success(deptService.tree());
    }

    @PostMapping("/depts")
    @RequirePermission("system:dept:create")
    @OperationLog(module = "部门管理", action = "新增部门")
    public ApiResponse<Long> createDept(@RequestBody Map<String, Object> body) {
        return ApiResponse.success(deptService.create(body));
    }

    @PutMapping("/depts/{id}")
    @RequirePermission("system:dept:update")
    @OperationLog(module = "部门管理", action = "编辑部门")
    public ApiResponse<Void> updateDept(@PathVariable long id, @RequestBody Map<String, Object> body) {
        deptService.update(id, body);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/depts/{id}")
    @RequirePermission("system:dept:delete")
    @OperationLog(module = "部门管理", action = "删除部门")
    public ApiResponse<Void> deleteDept(@PathVariable long id) {
        deptService.delete(id);
        return ApiResponse.success(null);
    }

    @PatchMapping("/depts/{id}/status")
    @RequirePermission("system:dept:update")
    @OperationLog(module = "部门管理", action = "变更部门状态")
    public ApiResponse<Void> deptStatus(@PathVariable long id, @RequestBody Map<String, Object> body) {
        deptService.updateStatus(id, intValue(body, "status", 1));
        return ApiResponse.success(null);
    }
}
