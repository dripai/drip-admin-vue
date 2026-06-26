package com.drip.admin.modules.system.dto;

public class UserQuery extends PageQuery {
    private String username; private String realName; private String phone; private Integer status; private Long deptId; private String createdAt;
    public String getUsername() { return username; } public void setUsername(String username) { this.username = username; }
    public String getRealName() { return realName; } public void setRealName(String realName) { this.realName = realName; }
    public String getPhone() { return phone; } public void setPhone(String phone) { this.phone = phone; }
    public Integer getStatus() { return status; } public void setStatus(Integer status) { this.status = status; }
    public Long getDeptId() { return deptId; } public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getCreatedAt() { return createdAt; } public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
