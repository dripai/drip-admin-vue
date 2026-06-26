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
    @NotBlank(message = "configValue is required")
    private String configValue;
    private String groupCode; private Integer isSensitive; private Integer builtin; private Integer status; private String remark;
    public String getConfigName() { return configName; } public void setConfigName(String configName) { this.configName = configName; }
    public String getConfigKey() { return configKey; } public void setConfigKey(String configKey) { this.configKey = configKey; }
    public String getConfigValue() { return configValue; } public void setConfigValue(String configValue) { this.configValue = configValue; }
    public String getGroupCode() { return groupCode; } public void setGroupCode(String groupCode) { this.groupCode = groupCode; }
    public Integer getIsSensitive() { return isSensitive; } public void setIsSensitive(Integer isSensitive) { this.isSensitive = isSensitive; }
    public Integer getBuiltin() { return builtin; } public void setBuiltin(Integer builtin) { this.builtin = builtin; }
    public Integer getStatus() { return status; } public void setStatus(Integer status) { this.status = status; }
    public String getRemark() { return remark; } public void setRemark(String remark) { this.remark = remark; }
}
