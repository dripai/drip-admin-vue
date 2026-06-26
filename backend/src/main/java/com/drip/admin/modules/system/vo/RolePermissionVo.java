package com.drip.admin.modules.system.vo;

import java.util.List;

public record RolePermissionVo(List<Long> menuIds, List<String> permissionCodes) {
}
