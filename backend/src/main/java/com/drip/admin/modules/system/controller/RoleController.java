package com.drip.admin.modules.system.controller;

import com.drip.admin.common.log.OperationLog;
import com.drip.admin.common.response.ApiResponse;
import com.drip.admin.common.response.PageResult;
import com.drip.admin.common.security.RequirePermission;
import com.drip.admin.modules.system.dto.*;
import com.drip.admin.modules.system.entity.SysRoleEntity;
import com.drip.admin.modules.system.entity.SysUserEntity;
import com.drip.admin.modules.system.service.RoleService;
import com.drip.admin.modules.system.vo.RolePermissionVo;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static com.drip.admin.shared.utils.AdminUtils.*;


@RestController
@RequestMapping("/system")
public class RoleController {
    private final RoleService roleService;

   public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping("/role")
    @RequirePermission("system:role:list")
    public ApiResponse<PageResult<SysRoleEntity>> roles(@Valid RoleQuery query) {
        return ApiResponse.success(roleService.page(query));
    }

    @GetMapping("/role/{id}")
    @RequirePermission("system:role:list")
    public ApiResponse<SysRoleEntity> role(@PathVariable long id) {
        return ApiResponse.success(roleService.detail(id));
    }

    @GetMapping("/role/{id}/user")
    @RequirePermission("system:role:list")
    public ApiResponse<PageResult<SysUserEntity>> roleUsers(@PathVariable long id, @Valid RoleQuery query) {
        return ApiResponse.success(roleService.users(id, query));
    }

    @GetMapping("/role/{id}/permission")
    @RequirePermission("system:role:permission")
    public ApiResponse<RolePermissionVo> rolePermissions(@PathVariable long id) {
        return ApiResponse.success(roleService.permissions(id));
    }

    @GetMapping("/role/option")
    @RequirePermission("system:role:list")
    public ApiResponse<List<SysRoleEntity>> roleOptions() {
        return ApiResponse.success(roleService.list());
    }

    @PostMapping("/role")
    @RequirePermission("system:role:create")
    @OperationLog(module = "角色管理", action = "新增角色")
    public ApiResponse<Long> createRole(@Valid @RequestBody RoleSaveRequest request) {
        return ApiResponse.success(roleService.create(request));
    }

    @PutMapping("/role/{id}")
    @RequirePermission("system:role:update")
    @OperationLog(module = "角色管理", action = "编辑角色")
    public ApiResponse<Void> updateRole(@PathVariable long id, @Valid @RequestBody RoleSaveRequest request) {
        roleService.update(id, request);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/role/{id}")
    @RequirePermission("system:role:delete")
    @OperationLog(module = "角色管理", action = "删除角色")
    public ApiResponse<Void> deleteRole(@PathVariable long id) {
        roleService.delete(id);
        return ApiResponse.success(null);
    }

    @PutMapping("/role/{id}/status")
    @RequirePermission("system:role:update")
    @OperationLog(module = "角色管理", action = "变更角色状态")
    public ApiResponse<Void> roleStatus(@PathVariable long id, @Valid @RequestBody StatusUpdateRequest request) {
        roleService.updateStatus(id, request.statusOrDefault());
        return ApiResponse.success(null);
    }

    @PutMapping("/role/{id}/permission")
    @RequirePermission("system:role:permission")
    @OperationLog(module = "角色管理", action = "角色授权")
    public ApiResponse<Void> rolePermissions(@PathVariable long id, @Valid @RequestBody MenuAssignRequest request) {
        roleService.assignMenus(id, request.getMenuIds());
        return ApiResponse.success(null);
    }
}
