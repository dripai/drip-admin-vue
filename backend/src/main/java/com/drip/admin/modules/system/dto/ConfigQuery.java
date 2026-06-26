package com.drip.admin.modules.system.dto;

public class ConfigQuery extends PageQuery {
    private String configName; private String configKey; private String groupCode; private Integer status;
    public String getConfigName() { return configName; } public void setConfigName(String configName) { this.configName = configName; }
    public String getConfigKey() { return configKey; } public void setConfigKey(String configKey) { this.configKey = configKey; }
    public String getGroupCode() { return groupCode; } public void setGroupCode(String groupCode) { this.groupCode = groupCode; }
    public Integer getStatus() { return status; } public void setStatus(Integer status) { this.status = status; }
}
