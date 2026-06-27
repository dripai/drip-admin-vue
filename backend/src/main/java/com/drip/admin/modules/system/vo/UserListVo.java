package com.drip.admin.modules.system.vo;

import java.time.LocalDateTime;
import java.util.List;

public record UserListVo(
    Long id,
    String username,
    String realName,
    String phone,
    String email,
    Integer status,
    DeptSummaryVo dept,
    List<RoleSummaryVo> roles,
    LocalDateTime createdAt,
    LocalDateTime lastLoginAt
) {
}
