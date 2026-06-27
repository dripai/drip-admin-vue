package com.drip.admin.modules.system.controller;

import com.drip.admin.common.log.OperationLog;
import com.drip.admin.common.response.ApiResponse;
import com.drip.admin.common.security.RequirePermission;
import com.drip.admin.modules.system.dto.*;
import com.drip.admin.modules.system.service.MenuService;
import com.drip.admin.modules.system.vo.MenuTreeVo;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static com.drip.admin.shared.utils.AdminUtils.*;


@RestController
@RequestMapping("/system")
public class MenuController {
    private final MenuService menuService;

   public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/menu")
    @RequirePermission("system:menu:list")
    public ApiResponse<List<MenuTreeVo>> menus() {
        return ApiResponse.success(menuService.tree());
    }

    @PostMapping("/menu")
    @RequirePermission("system:menu:create")
    @OperationLog(module = "菜单管理", action = "新增菜单")
    public ApiResponse<Long> createMenu(@Valid @RequestBody MenuSaveRequest request) {
        return ApiResponse.success(menuService.create(request));
    }

    @PutMapping("/menu/{id}")
    @RequirePermission("system:menu:update")
    @OperationLog(module = "菜单管理", action = "编辑菜单")
    public ApiResponse<Void> updateMenu(@PathVariable long id, @Valid @RequestBody MenuSaveRequest request) {
        menuService.update(id, request);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/menu/{id}")
    @RequirePermission("system:menu:delete")
    @OperationLog(module = "菜单管理", action = "删除菜单")
    public ApiResponse<Void> deleteMenu(@PathVariable long id) {
        menuService.delete(id);
        return ApiResponse.success(null);
    }

    @PutMapping("/menu/{id}/status")
    @RequirePermission("system:menu:status")
    @OperationLog(module = "菜单管理", action = "变更菜单状态")
    public ApiResponse<Void> menuStatus(@PathVariable long id, @Valid @RequestBody StatusUpdateRequest request) {
        menuService.updateStatus(id, request.statusOrDefault());
        return ApiResponse.success(null);
    }
}
