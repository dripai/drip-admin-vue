package com.drip.admin.modules.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class DictItemSaveRequest {
    @NotNull(message = "dictTypeId is required")
    private Long dictTypeId;
    @NotBlank(message = "label is required")
    @Size(max = 64, message = "label length must be <= 64")
    private String label;
    @NotBlank(message = "value is required")
    @Size(max = 64, message = "value length must be <= 64")
    private String value;
    private Integer isDefault; private Integer sort; private Integer status; private Integer builtin;
    public Long getDictTypeId() { return dictTypeId; } public void setDictTypeId(Long dictTypeId) { this.dictTypeId = dictTypeId; }
    public String getLabel() { return label; } public void setLabel(String label) { this.label = label; }
    public String getValue() { return value; } public void setValue(String value) { this.value = value; }
    public Integer getIsDefault() { return isDefault; } public void setIsDefault(Integer isDefault) { this.isDefault = isDefault; }
    public Integer getSort() { return sort; } public void setSort(Integer sort) { this.sort = sort; }
    public Integer getStatus() { return status; } public void setStatus(Integer status) { this.status = status; }
    public Integer getBuiltin() { return builtin; } public void setBuiltin(Integer builtin) { this.builtin = builtin; }
}
