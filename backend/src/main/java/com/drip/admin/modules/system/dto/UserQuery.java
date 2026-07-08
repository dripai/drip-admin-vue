package com.drip.admin.modules.system.dto;

import com.drip.admin.common.dto.PageQuery;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public class UserQuery extends PageQuery {
    private String username; private String realName; private String phone; private Integer status; private Long roleId; private Long deptId;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdFrom;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdTo;
    public String getUsername() { return username; } public void setUsername(String username) { this.username = username; }
    public String getRealName() { return realName; } public void setRealName(String realName) { this.realName = realName; }
    public String getPhone() { return phone; } public void setPhone(String phone) { this.phone = phone; }
    public Integer getStatus() { return status; } public void setStatus(Integer status) { this.status = status; }
    public Long getRoleId() { return roleId; } public void setRoleId(Long roleId) { this.roleId = roleId; }
    public Long getDeptId() { return deptId; } public void setDeptId(Long deptId) { this.deptId = deptId; }
    public LocalDateTime getCreatedFrom() { return createdFrom; } public void setCreatedFrom(LocalDateTime createdFrom) { this.createdFrom = createdFrom; }
    public LocalDateTime getCreatedTo() { return createdTo; } public void setCreatedTo(LocalDateTime createdTo) { this.createdTo = createdTo; }
}
