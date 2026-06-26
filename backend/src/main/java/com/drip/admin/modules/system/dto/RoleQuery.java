package com.drip.admin.modules.system.dto;

public class RoleQuery extends PageQuery {
    private String roleName; private String roleCode; private Integer status; private String createdAt;
    public String getRoleName() { return roleName; } public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getRoleCode() { return roleCode; } public void setRoleCode(String roleCode) { this.roleCode = roleCode; }
    public Integer getStatus() { return status; } public void setStatus(Integer status) { this.status = status; }
    public String getCreatedAt() { return createdAt; } public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
