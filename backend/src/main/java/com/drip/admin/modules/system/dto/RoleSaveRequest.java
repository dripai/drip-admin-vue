package com.drip.admin.modules.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RoleSaveRequest {
    @NotBlank(message = "roleName is required")
    @Size(max = 64, message = "roleName length must be <= 64")
    private String roleName;
    @NotBlank(message = "roleCode is required")
    @Pattern(regexp = "^[A-Z][A-Z0-9_]{1,63}$", message = "roleCode format is invalid")
    private String roleCode;
    private Integer status; private String remark;
    public String getRoleName() { return roleName; } public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getRoleCode() { return roleCode; } public void setRoleCode(String roleCode) { this.roleCode = roleCode; }
    public Integer getStatus() { return status; } public void setStatus(Integer status) { this.status = status; }
    public String getRemark() { return remark; } public void setRemark(String remark) { this.remark = remark; }
}
