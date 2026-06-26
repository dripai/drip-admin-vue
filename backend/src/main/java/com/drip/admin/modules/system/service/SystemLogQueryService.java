package com.drip.admin.modules.system.service;

import com.drip.admin.common.response.PageResult;

import java.util.Map;

public interface SystemLogQueryService {
    PageResult<Map<String, Object>> loginLogs(Map<String, String> q);

    Map<String, Object> loginLog(long id);

    PageResult<Map<String, Object>> operationLogs(Map<String, String> q);

    Map<String, Object> operationLog(long id);
}
