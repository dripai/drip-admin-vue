package com.drip.admin.modules.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class JobSaveRequest {
    @NotBlank(message = "jobName is required")
    @Size(max = 128, message = "jobName length must be <= 128")
    private String jobName;
    @NotBlank(message = "cronExpression is required")
    @Size(max = 64, message = "cronExpression length must be <= 64")
    private String cronExpression;
    private String executorType;
    private String scriptFile;
    private String scriptArgs;
    private String className;
    private String methodName;
    private Integer status; private String remark;
    public String getJobName() { return jobName; } public void setJobName(String jobName) { this.jobName = jobName; }
    public String getCronExpression() { return cronExpression; } public void setCronExpression(String cronExpression) { this.cronExpression = cronExpression; }
    public String getExecutorType() { return executorType; } public void setExecutorType(String executorType) { this.executorType = executorType; }
    public String getScriptFile() { return scriptFile; } public void setScriptFile(String scriptFile) { this.scriptFile = scriptFile; }
    public String getScriptArgs() { return scriptArgs; } public void setScriptArgs(String scriptArgs) { this.scriptArgs = scriptArgs; }
    public String getClassName() { return className; } public void setClassName(String className) { this.className = className; }
    public String getMethodName() { return methodName; } public void setMethodName(String methodName) { this.methodName = methodName; }
    public Integer getStatus() { return status; } public void setStatus(Integer status) { this.status = status; }
    public String getRemark() { return remark; } public void setRemark(String remark) { this.remark = remark; }
}
