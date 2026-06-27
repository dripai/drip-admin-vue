package com.drip.admin.modules.system.dto;

public class JobQuery extends PageQuery {
    private String jobName; private String remark; private Integer status; private String createdAt;
    public String getJobName() { return jobName; } public void setJobName(String jobName) { this.jobName = jobName; }
    public String getRemark() { return remark; } public void setRemark(String remark) { this.remark = remark; }
    public Integer getStatus() { return status; } public void setStatus(Integer status) { this.status = status; }
    public String getCreatedAt() { return createdAt; } public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
