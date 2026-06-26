package com.drip.admin.modules.system.dto;

public class UserSaveRequest {
    private String username; private String realName; private String phone; private String email; private Integer status; private Long deptId; private String remark; private String password;
    public String getUsername() { return username; } public void setUsername(String username) { this.username = username; }
    public String getRealName() { return realName; } public void setRealName(String realName) { this.realName = realName; }
    public String getPhone() { return phone; } public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; } public void setEmail(String email) { this.email = email; }
    public Integer getStatus() { return status; } public void setStatus(Integer status) { this.status = status; }
    public Long getDeptId() { return deptId; } public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getRemark() { return remark; } public void setRemark(String remark) { this.remark = remark; }
    public String getPassword() { return password; } public void setPassword(String password) { this.password = password; }
}
