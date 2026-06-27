package com.drip.admin.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.drip.admin.modules.system.dto.DictItemSaveRequest;
import com.drip.admin.modules.system.dto.DictTypeSaveRequest;
import com.drip.admin.modules.system.entity.SysDictItemEntity;
import com.drip.admin.modules.system.entity.SysDictTypeEntity;

import java.util.List;

public interface DictService extends IService<SysDictTypeEntity> {
    List<SysDictTypeEntity> types();
    List<SysDictItemEntity> items(long dictTypeId);
    SysDictTypeEntity typeDetail(long id);
    SysDictItemEntity itemDetail(long id);
    Long createType(DictTypeSaveRequest request);
    void updateType(long id, DictTypeSaveRequest request);
    void deleteType(long id);
    Long createItem(DictItemSaveRequest request);
    void updateItem(long id, DictItemSaveRequest request);
    void deleteItem(long id);
    void updateItemStatus(long id, int status);
    void refreshCache();
}
