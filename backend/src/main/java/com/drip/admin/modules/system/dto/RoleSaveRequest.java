package com.drip.admin.modules.system.dto;

public class RoleSaveRequest {
    private String roleName; private String roleCode; private Integer status; private String remark;
    public String getRoleName() { return roleName; } public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getRoleCode() { return roleCode; } public void setRoleCode(String roleCode) { this.roleCode = roleCode; }
    public Integer getStatus() { return status; } public void setStatus(Integer status) { this.status = status; }
    public String getRemark() { return remark; } public void setRemark(String remark) { this.remark = remark; }
}
