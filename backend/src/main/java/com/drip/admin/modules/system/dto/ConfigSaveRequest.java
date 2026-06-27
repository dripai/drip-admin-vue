package com.drip.admin.modules.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ConfigSaveRequest {
    @NotBlank(message = "configName is required")
    @Size(max = 128, message = "configName length must be <= 128")
    private String configName;
    @NotBlank(message = "configKey is required")
    @Size(max = 128, message = "configKey length must be <= 128")
    private String configKey;
    @Size(max = 1024, message = "configValue length must be <= 1024")
    private String configValue;
    private Integer status; private String remark;
    public String getConfigName() { return configName; } public void setConfigName(String configName) { this.configName = configName; }
    public String getConfigKey() { return configKey; } public void setConfigKey(String configKey) { this.configKey = configKey; }
    public String getConfigValue() { return configValue; } public void setConfigValue(String configValue) { this.configValue = configValue; }
    public Integer getStatus() { return status; } public void setStatus(Integer status) { this.status = status; }
    public String getRemark() { return remark; } public void setRemark(String remark) { this.remark = remark; }
}
