package com.drip.admin.modules.system.vo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DeptTreeVo {
    private Long id;
    private Long parentId;
    private String deptName;
    private String deptCode;
    private Long leaderUserId;
    private Integer sort;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<DeptTreeVo> children = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public String getDeptCode() { return deptCode; }
    public void setDeptCode(String deptCode) { this.deptCode = deptCode; }
    public Long getLeaderUserId() { return leaderUserId; }
    public void setLeaderUserId(Long leaderUserId) { this.leaderUserId = leaderUserId; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<DeptTreeVo> getChildren() { return children; }
    public void setChildren(List<DeptTreeVo> children) { this.children = children; }
}
