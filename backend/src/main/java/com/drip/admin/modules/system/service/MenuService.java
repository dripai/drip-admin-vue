package com.drip.admin.modules.system.service;

import java.util.List;
import java.util.Map;

public interface MenuService {
    List<Map<String, Object>> tree();

    Map<String, Object> detail(long id);

    Long create(Map<String, Object> body);

    void update(long id, Map<String, Object> body);

    void delete(long id);

    void updateStatus(long id, int status);
}
