package com.drip.admin.modules.system.service;

import com.drip.admin.common.response.PageResult;

import java.util.List;
import java.util.Map;

public interface RoleService {
    PageResult<Map<String, Object>> page(Map<String, String> q);

    Map<String, Object> detail(long id);

    PageResult<Map<String, Object>> users(long roleId, Map<String, String> q);

    Long create(Map<String, Object> body);

    void update(long id, Map<String, Object> body);

    void delete(long id);

    void updateStatus(long id, int status);

    void assignMenus(long roleId, List<Long> menuIds);
}
