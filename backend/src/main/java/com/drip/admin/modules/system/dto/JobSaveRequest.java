package com.drip.admin.modules.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class JobSaveRequest {
    @NotBlank(message = "jobName is required")
    @Size(max = 128, message = "jobName length must be <= 128")
    private String jobName;
    @NotBlank(message = "jobCode is required")
    @Size(max = 128, message = "jobCode length must be <= 128")
    private String jobCode;
    @NotBlank(message = "cronExpression is required")
    @Size(max = 64, message = "cronExpression length must be <= 64")
    private String cronExpression;
    @NotBlank(message = "beanName is required")
    private String beanName;
    @NotBlank(message = "methodName is required")
    private String methodName;
    private String params; private Integer status; private String remark;
    public String getJobName() { return jobName; } public void setJobName(String jobName) { this.jobName = jobName; }
    public String getJobCode() { return jobCode; } public void setJobCode(String jobCode) { this.jobCode = jobCode; }
    public String getCronExpression() { return cronExpression; } public void setCronExpression(String cronExpression) { this.cronExpression = cronExpression; }
    public String getBeanName() { return beanName; } public void setBeanName(String beanName) { this.beanName = beanName; }
    public String getMethodName() { return methodName; } public void setMethodName(String methodName) { this.methodName = methodName; }
    public String getParams() { return params; } public void setParams(String params) { this.params = params; }
    public Integer getStatus() { return status; } public void setStatus(Integer status) { this.status = status; }
    public String getRemark() { return remark; } public void setRemark(String remark) { this.remark = remark; }
}
