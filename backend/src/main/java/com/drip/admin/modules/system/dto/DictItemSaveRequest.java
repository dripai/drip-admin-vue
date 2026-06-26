package com.drip.admin.modules.system.dto;

public class DictItemSaveRequest {
    private Long dictTypeId; private String label; private String value; private String color; private Integer sort; private Integer status;
    public Long getDictTypeId() { return dictTypeId; } public void setDictTypeId(Long dictTypeId) { this.dictTypeId = dictTypeId; }
    public String getLabel() { return label; } public void setLabel(String label) { this.label = label; }
    public String getValue() { return value; } public void setValue(String value) { this.value = value; }
    public String getColor() { return color; } public void setColor(String color) { this.color = color; }
    public Integer getSort() { return sort; } public void setSort(Integer sort) { this.sort = sort; }
    public Integer getStatus() { return status; } public void setStatus(Integer status) { this.status = status; }
}
