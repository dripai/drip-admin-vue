package com.drip.admin.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.drip.admin.common.response.PageResult;
import com.drip.admin.modules.system.entity.SysDictItemEntity;
import com.drip.admin.modules.system.entity.SysDictTypeEntity;

import java.util.List;
import java.util.Map;

public interface DictService extends IService<SysDictTypeEntity> {
    PageResult<SysDictTypeEntity> types(Map<String, String> q);

    List<SysDictItemEntity> items(long dictTypeId);

    SysDictTypeEntity typeDetail(long id);

    SysDictItemEntity itemDetail(long id);

    Long createType(Map<String, Object> body);

    void updateType(long id, Map<String, Object> body);

    void deleteType(long id);

    Long createItem(Map<String, Object> body);

    void updateItem(long id, Map<String, Object> body);

    void deleteItem(long id);

    void updateItemStatus(long id, int status);

    void refreshCache();
}
