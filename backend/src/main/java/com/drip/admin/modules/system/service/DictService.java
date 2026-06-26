package com.drip.admin.modules.system.service;

import com.drip.admin.common.response.PageResult;

import java.util.List;
import java.util.Map;

public interface DictService {
    PageResult<Map<String, Object>> types(Map<String, String> q);

    List<Map<String, Object>> items(long dictTypeId);

    Map<String, Object> typeDetail(long id);

    Map<String, Object> itemDetail(long id);

    Long createType(Map<String, Object> body);

    void updateType(long id, Map<String, Object> body);

    void deleteType(long id);

    Long createItem(Map<String, Object> body);

    void updateItem(long id, Map<String, Object> body);

    void deleteItem(long id);

    void updateItemStatus(long id, int status);

    void refreshCache();
}
