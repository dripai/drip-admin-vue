package com.drip.admin.modules.system.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class StatusUpdateRequest {
    @Min(value = 0, message = "status must be 0 or 1")
    @Max(value = 1, message = "status must be 0 or 1")
    private Integer status;
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public int statusOrDefault() { return status == null ? 1 : status; }
}
