package com.drip.admin.modules.system.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public class RoleAssignRequest {
    @NotNull(message = "roleIds is required")
    private List<Long> roleIds;
    public List<Long> getRoleIds() { return roleIds; }
    public void setRoleIds(List<Long> roleIds) { this.roleIds = roleIds; }
}
