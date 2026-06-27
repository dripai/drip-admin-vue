package com.drip.admin.modules.system.controller;

import com.drip.admin.common.log.OperationLog;
import com.drip.admin.common.response.ApiResponse;
import com.drip.admin.common.response.PageResult;
import com.drip.admin.common.security.RequirePermission;
import com.drip.admin.modules.system.dto.*;
import com.drip.admin.modules.system.entity.SysUserEntity;
import com.drip.admin.modules.system.service.UserService;
import com.drip.admin.modules.system.vo.UserListVo;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static com.drip.admin.shared.utils.AdminUtils.*;


@RestController
@RequestMapping("/system")
public class UserController {
    private final UserService userService;

   public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/user")
    @RequirePermission("system:user:list")
    public ApiResponse<PageResult<UserListVo>> users(@Valid UserQuery query) {
        return ApiResponse.success(userService.page(query));
    }

    @GetMapping("/user/{id}")
    @RequirePermission("system:user:detail")
    public ApiResponse<SysUserEntity> user(@PathVariable long id) {
        return ApiResponse.success(userService.detail(id));
    }

    @PostMapping("/user")
    @RequirePermission("system:user:create")
    @OperationLog(module = "用户管理", action = "新增用户")
    public ApiResponse<Long> createUser(@Valid @RequestBody UserSaveRequest request) {
        return ApiResponse.success(userService.create(request));
    }

    @PutMapping("/user/{id}")
    @RequirePermission("system:user:update")
    @OperationLog(module = "用户管理", action = "编辑用户")
    public ApiResponse<Void> updateUser(@PathVariable long id, @Valid @RequestBody UserSaveRequest request) {
        userService.update(id, request);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/user/{id}")
    @RequirePermission("system:user:delete")
    @OperationLog(module = "用户管理", action = "删除用户")
    public ApiResponse<Void> deleteUser(@PathVariable long id) {
        userService.delete(id);
        return ApiResponse.success(null);
    }

    @PutMapping("/user/{id}/status")
    @RequirePermission("system:user:disable")
    @OperationLog(module = "用户管理", action = "变更用户状态")
    public ApiResponse<Void> userStatus(@PathVariable long id, @Valid @RequestBody StatusUpdateRequest request) {
        userService.updateStatus(id, request.statusOrDefault());
        return ApiResponse.success(null);
    }

    @PostMapping("/user/{id}/unlock")
    @RequirePermission("system:user:unlock")
    @OperationLog(module = "用户管理", action = "解除登录锁定")
    public ApiResponse<Void> unlockUser(@PathVariable long id) {
        userService.unlockLogin(id);
        return ApiResponse.success(null);
    }

    @PutMapping("/user/{id}/role")
    @RequirePermission("system:user:assignRole")
    @OperationLog(module = "用户管理", action = "分配角色")
    public ApiResponse<Void> userRoles(@PathVariable long id, @Valid @RequestBody RoleAssignRequest request) {
        userService.assignRoles(id, request.getRoleIds());
        return ApiResponse.success(null);
    }

    @PostMapping("/user/{id}/resetPassword")
    @RequirePermission("system:user:resetPassword")
    @OperationLog(module = "用户管理", action = "重置密码")
    public ApiResponse<Void> resetPassword(@PathVariable long id, @Valid @RequestBody PasswordResetRequest request) {
        userService.resetPassword(id, request.passwordOrDefault());
        return ApiResponse.success(null);
    }
}
