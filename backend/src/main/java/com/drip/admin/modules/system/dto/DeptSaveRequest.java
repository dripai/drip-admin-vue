package com.drip.admin.modules.system.dto;

public class DeptSaveRequest {
    private Long parentId; private String deptName; private String deptCode; private Long leaderUserId; private Integer sort; private Integer status;
    public Long getParentId() { return parentId; } public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getDeptName() { return deptName; } public void setDeptName(String deptName) { this.deptName = deptName; }
    public String getDeptCode() { return deptCode; } public void setDeptCode(String deptCode) { this.deptCode = deptCode; }
    public Long getLeaderUserId() { return leaderUserId; } public void setLeaderUserId(Long leaderUserId) { this.leaderUserId = leaderUserId; }
    public Integer getSort() { return sort; } public void setSort(Integer sort) { this.sort = sort; }
    public Integer getStatus() { return status; } public void setStatus(Integer status) { this.status = status; }
}
