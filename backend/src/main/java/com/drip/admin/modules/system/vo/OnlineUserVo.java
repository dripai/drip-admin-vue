package com.drip.admin.modules.system.vo;

public record OnlineUserVo(
    String tokenId,
    Long userId,
    String username,
    String realName,
    String deviceType,
    String ip,
    String userAgent,
    String loginAt,
    String lastActiveAt
) {
}
