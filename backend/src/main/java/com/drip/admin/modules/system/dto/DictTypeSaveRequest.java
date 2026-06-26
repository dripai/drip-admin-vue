package com.drip.admin.modules.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DictTypeSaveRequest {
    @NotBlank(message = "dictName is required")
    @Size(max = 64, message = "dictName length must be <= 64")
    private String dictName;
    @NotBlank(message = "dictCode is required")
    @Size(max = 64, message = "dictCode length must be <= 64")
    private String dictCode;
    private Integer status; private String remark;
    public String getDictName() { return dictName; } public void setDictName(String dictName) { this.dictName = dictName; }
    public String getDictCode() { return dictCode; } public void setDictCode(String dictCode) { this.dictCode = dictCode; }
    public Integer getStatus() { return status; } public void setStatus(Integer status) { this.status = status; }
    public String getRemark() { return remark; } public void setRemark(String remark) { this.remark = remark; }
}
