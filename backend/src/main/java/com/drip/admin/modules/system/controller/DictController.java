package com.drip.admin.modules.system.controller;

import com.drip.admin.common.log.OperationLog;
import com.drip.admin.common.response.ApiResponse;
import com.drip.admin.common.security.RequirePermission;
import com.drip.admin.modules.system.dto.DictItemSaveRequest;
import com.drip.admin.modules.system.dto.DictTypeSaveRequest;
import com.drip.admin.modules.system.dto.StatusUpdateRequest;
import com.drip.admin.modules.system.entity.SysDictItemEntity;
import com.drip.admin.modules.system.entity.SysDictTypeEntity;
import com.drip.admin.modules.system.service.DictService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/system")
public class DictController {
    private final DictService dictService;

    public DictController(DictService dictService) {
        this.dictService = dictService;
    }

    @GetMapping("/dict/type")
    @RequirePermission("system:dict:list")
    public ApiResponse<List<SysDictTypeEntity>> dictTypes() {
        return ApiResponse.success(dictService.types());
    }

    @PostMapping("/dict/type")
    @RequirePermission("system:dict:create")
    @OperationLog(module = "字典管理", action = "新增字典类型")
    public ApiResponse<Long> createDictType(@Valid @RequestBody DictTypeSaveRequest request) {
        return ApiResponse.success(dictService.createType(request));
    }

    @PutMapping("/dict/type/{id}")
    @RequirePermission("system:dict:update")
    @OperationLog(module = "字典管理", action = "编辑字典类型")
    public ApiResponse<Void> updateDictType(@PathVariable long id, @Valid @RequestBody DictTypeSaveRequest request) {
        dictService.updateType(id, request);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/dict/type/{id}")
    @RequirePermission("system:dict:delete")
    @OperationLog(module = "字典管理", action = "删除字典类型")
    public ApiResponse<Void> deleteDictType(@PathVariable long id) {
        dictService.deleteType(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/dict/type/{id}/item")
    @RequirePermission("system:dict:list")
    public ApiResponse<List<SysDictItemEntity>> dictItems(@PathVariable long id) {
        return ApiResponse.success(dictService.items(id));
    }

    @PostMapping("/dict/item")
    @RequirePermission("system:dict:create")
    @OperationLog(module = "字典管理", action = "新增字典项")
    public ApiResponse<Long> createDictItem(@Valid @RequestBody DictItemSaveRequest request) {
        return ApiResponse.success(dictService.createItem(request));
    }

    @PutMapping("/dict/item/{id}")
    @RequirePermission("system:dict:update")
    @OperationLog(module = "字典管理", action = "编辑字典项")
    public ApiResponse<Void> updateDictItem(@PathVariable long id, @Valid @RequestBody DictItemSaveRequest request) {
        dictService.updateItem(id, request);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/dict/item/{id}")
    @RequirePermission("system:dict:delete")
    @OperationLog(module = "字典管理", action = "删除字典项")
    public ApiResponse<Void> deleteDictItem(@PathVariable long id) {
        dictService.deleteItem(id);
        return ApiResponse.success(null);
    }

    @PutMapping("/dict/item/{id}/status")
    @RequirePermission("system:dict:update")
    @OperationLog(module = "字典管理", action = "变更字典项状态")
    public ApiResponse<Void> dictItemStatus(@PathVariable long id, @Valid @RequestBody StatusUpdateRequest request) {
        dictService.updateItemStatus(id, request.statusOrDefault());
        return ApiResponse.success(null);
    }

    @PostMapping("/dict/cache/refresh")
    @RequirePermission("system:dict:update")
    public ApiResponse<Void> refreshDictCache() {
        dictService.refreshCache();
        return ApiResponse.success(null);
    }
}
