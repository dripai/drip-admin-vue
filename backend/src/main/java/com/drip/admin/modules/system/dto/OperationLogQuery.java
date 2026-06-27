package com.drip.admin.modules.system.dto;

import java.time.LocalDateTime;

public class OperationLogQuery extends PageQuery {
    private String operator; private String module; private String action; private String status; private String path;
    private LocalDateTime createdFrom; private LocalDateTime createdTo;
    public String getOperator() { return operator; } public void setOperator(String operator) { this.operator = operator; }
    public String getModule() { return module; } public void setModule(String module) { this.module = module; }
    public String getAction() { return action; } public void setAction(String action) { this.action = action; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public String getPath() { return path; } public void setPath(String path) { this.path = path; }
    public LocalDateTime getCreatedFrom() { return createdFrom; } public void setCreatedFrom(LocalDateTime createdFrom) { this.createdFrom = createdFrom; }
    public LocalDateTime getCreatedTo() { return createdTo; } public void setCreatedTo(LocalDateTime createdTo) { this.createdTo = createdTo; }
}
