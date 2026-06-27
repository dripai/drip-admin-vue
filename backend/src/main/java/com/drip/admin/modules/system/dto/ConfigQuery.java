package com.drip.admin.modules.system.dto;

public class ConfigQuery extends PageQuery {
    private String configName; private String configKey; private Integer status;
    public String getConfigName() { return configName; } public void setConfigName(String configName) { this.configName = configName; }
    public String getConfigKey() { return configKey; } public void setConfigKey(String configKey) { this.configKey = configKey; }
    public Integer getStatus() { return status; } public void setStatus(Integer status) { this.status = status; }
}
