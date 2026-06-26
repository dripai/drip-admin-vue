package com.drip.admin.modules.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DeptSaveRequest {
    private Long parentId;
    @NotBlank(message = "deptName is required")
    @Size(max = 64, message = "deptName length must be <= 64")
    private String deptName;
    @NotBlank(message = "deptCode is required")
    @Size(max = 64, message = "deptCode length must be <= 64")
    private String deptCode;
    private Long leaderUserId; private Integer sort; private Integer status;
    public Long getParentId() { return parentId; } public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getDeptName() { return deptName; } public void setDeptName(String deptName) { this.deptName = deptName; }
    public String getDeptCode() { return deptCode; } public void setDeptCode(String deptCode) { this.deptCode = deptCode; }
    public Long getLeaderUserId() { return leaderUserId; } public void setLeaderUserId(Long leaderUserId) { this.leaderUserId = leaderUserId; }
    public Integer getSort() { return sort; } public void setSort(Integer sort) { this.sort = sort; }
    public Integer getStatus() { return status; } public void setStatus(Integer status) { this.status = status; }
}
