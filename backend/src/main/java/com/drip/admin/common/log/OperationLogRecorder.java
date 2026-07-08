package com.drip.admin.common.log;

public interface OperationLogRecorder {
    void operation(String module, String action, String method, String path, String requestParams,
                   String responseStatus, String errorMessage, long costMs);
}
