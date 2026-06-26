package com.drip.admin.modules.system.dto;

public class DictTypeSaveRequest {
    private String dictName; private String dictCode; private Integer status; private String remark;
    public String getDictName() { return dictName; } public void setDictName(String dictName) { this.dictName = dictName; }
    public String getDictCode() { return dictCode; } public void setDictCode(String dictCode) { this.dictCode = dictCode; }
    public Integer getStatus() { return status; } public void setStatus(Integer status) { this.status = status; }
    public String getRemark() { return remark; } public void setRemark(String remark) { this.remark = remark; }
}
