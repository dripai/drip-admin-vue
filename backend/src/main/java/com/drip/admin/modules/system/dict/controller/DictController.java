package com.drip.admin.modules.system.dict.controller;

import com.drip.admin.common.log.OperationLog;
import com.drip.admin.common.response.ApiResponse;
import com.drip.admin.common.response.PageResult;
import com.drip.admin.common.security.RequirePermission;
import com.drip.admin.modules.system.dict.service.DictService;
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

import java.util.List;
import java.util.Map;

import static com.drip.admin.shared.utils.AdminUtils.intValue;

@RestController
@RequestMapping("/api/system")
public class DictController {
    private final DictService dictService;

    public DictController(DictService dictService) {
        this.dictService = dictService;
    }

    @GetMapping("/dicts/types")
    @RequirePermission("system:dict:list")
    public ApiResponse<PageResult<Map<String, Object>>> dictTypes(@RequestParam Map<String, String> q) {
        return ApiResponse.success(dictService.types(q));
    }

    @PostMapping("/dicts/types")
    @RequirePermission("system:dict:create")
    @OperationLog(module = "字典管理", action = "新增字典类型")
    public ApiResponse<Long> createDictType(@RequestBody Map<String, Object> body) {
        return ApiResponse.success(dictService.createType(body));
    }

    @PutMapping("/dicts/types/{id}")
    @RequirePermission("system:dict:update")
    @OperationLog(module = "字典管理", action = "编辑字典类型")
    public ApiResponse<Void> updateDictType(@PathVariable long id, @RequestBody Map<String, Object> body) {
        dictService.updateType(id, body);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/dicts/types/{id}")
    @RequirePermission("system:dict:delete")
    @OperationLog(module = "字典管理", action = "删除字典类型")
    public ApiResponse<Void> deleteDictType(@PathVariable long id) {
        dictService.deleteType(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/dicts/types/{id}/items")
    @RequirePermission("system:dict:list")
    public ApiResponse<List<Map<String, Object>>> dictItems(@PathVariable long id) {
        return ApiResponse.success(dictService.items(id));
    }

    @PostMapping("/dicts/items")
    @RequirePermission("system:dict:create")
    @OperationLog(module = "字典管理", action = "新增字典项")
    public ApiResponse<Long> createDictItem(@RequestBody Map<String, Object> body) {
        return ApiResponse.success(dictService.createItem(body));
    }

    @PutMapping("/dicts/items/{id}")
    @RequirePermission("system:dict:update")
    @OperationLog(module = "字典管理", action = "编辑字典项")
    public ApiResponse<Void> updateDictItem(@PathVariable long id, @RequestBody Map<String, Object> body) {
        dictService.updateItem(id, body);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/dicts/items/{id}")
    @RequirePermission("system:dict:delete")
    @OperationLog(module = "字典管理", action = "删除字典项")
    public ApiResponse<Void> deleteDictItem(@PathVariable long id) {
        dictService.deleteItem(id);
        return ApiResponse.success(null);
    }

    @PatchMapping("/dicts/items/{id}/status")
    @RequirePermission("system:dict:update")
    @OperationLog(module = "字典管理", action = "变更字典项状态")
    public ApiResponse<Void> dictItemStatus(@PathVariable long id, @RequestBody Map<String, Object> body) {
        dictService.updateItemStatus(id, intValue(body, "status", 1));
        return ApiResponse.success(null);
    }

    @PostMapping("/dicts/cache/refresh")
    @RequirePermission("system:dict:update")
    public ApiResponse<Void> refreshDictCache() {
        dictService.refreshCache();
        return ApiResponse.success(null);
    }
}
