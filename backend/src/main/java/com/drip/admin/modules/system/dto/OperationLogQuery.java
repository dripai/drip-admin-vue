package com.drip.admin.modules.system.dto;

public class OperationLogQuery extends PageQuery {
    private String operatorName; private String module; private String action; private String responseStatus; private String path;
    public String getOperatorName() { return operatorName; } public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public String getModule() { return module; } public void setModule(String module) { this.module = module; }
    public String getAction() { return action; } public void setAction(String action) { this.action = action; }
    public String getResponseStatus() { return responseStatus; } public void setResponseStatus(String responseStatus) { this.responseStatus = responseStatus; }
    public String getPath() { return path; } public void setPath(String path) { this.path = path; }
}
