package com.drip.admin.modules.system.dto;

public class StatusUpdateRequest {
    private Integer status;
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public int statusOrDefault() { return status == null ? 1 : status; }
}
