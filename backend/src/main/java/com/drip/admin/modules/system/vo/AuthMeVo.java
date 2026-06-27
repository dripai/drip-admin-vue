package com.drip.admin.modules.system.vo;

import java.util.List;

public record AuthMeVo(
    Long userId,
    String username,
    String realName,
    String phone,
    String email,
    String avatar,
    Long deptId,
    List<String> roles,
    List<String> permissions,
    List<MenuTreeVo> menus
) {
}
