package com.drip.admin.modules.system.vo;

public record AuthLoginVo(
    String token,
    String expireAt,
    long idleTimeout,
    long maxSessionDuration,
    String deviceType
) {
}
