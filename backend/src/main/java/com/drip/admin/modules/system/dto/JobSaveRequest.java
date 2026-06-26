package com.drip.admin.modules.system.dto;

public class JobSaveRequest {
    private String jobName; private String jobCode; private String cronExpression; private String beanName; private String methodName; private String params; private Integer status; private String remark;
    public String getJobName() { return jobName; } public void setJobName(String jobName) { this.jobName = jobName; }
    public String getJobCode() { return jobCode; } public void setJobCode(String jobCode) { this.jobCode = jobCode; }
    public String getCronExpression() { return cronExpression; } public void setCronExpression(String cronExpression) { this.cronExpression = cronExpression; }
    public String getBeanName() { return beanName; } public void setBeanName(String beanName) { this.beanName = beanName; }
    public String getMethodName() { return methodName; } public void setMethodName(String methodName) { this.methodName = methodName; }
    public String getParams() { return params; } public void setParams(String params) { this.params = params; }
    public Integer getStatus() { return status; } public void setStatus(Integer status) { this.status = status; }
    public String getRemark() { return remark; } public void setRemark(String remark) { this.remark = remark; }
}
