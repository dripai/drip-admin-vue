package com.drip.admin.modules.system.service;

import com.drip.admin.common.response.PageResult;

import java.util.Map;

public interface JobService {
    PageResult<Map<String, Object>> page(Map<String, String> q);

    Map<String, Object> detail(long id);

    Long create(Map<String, Object> body);

    void update(long id, Map<String, Object> body);

    void delete(long id);

    void updateStatus(long id, int status);

    void run(long id);

    PageResult<Map<String, Object>> runLogs(long jobId, Map<String, String> q);
}
