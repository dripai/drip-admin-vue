package com.drip.admin.modules.system.vo;

import java.time.LocalDateTime;

public record OperationLogVo(
    Long id,
    Long operatorId,
    String operator,
    String module,
    String action,
    String method,
    String path,
    String requestParams,
    String status,
    String errorMessage,
    Long duration,
    LocalDateTime createdAt
) {
}
