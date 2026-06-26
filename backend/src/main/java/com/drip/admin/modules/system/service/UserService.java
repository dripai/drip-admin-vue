package com.drip.admin.modules.system.service;

import com.drip.admin.common.response.PageResult;

import java.util.List;
import java.util.Map;

public interface UserService {
    PageResult<Map<String, Object>> page(Map<String, String> q);

    Map<String, Object> detail(long id);

    Long create(Map<String, Object> body);

    void update(long id, Map<String, Object> body);

    void delete(long id);

    void updateStatus(long id, int status);

    void resetPassword(long id, String password);

    void assignRoles(long userId, List<Long> roleIds);
}
