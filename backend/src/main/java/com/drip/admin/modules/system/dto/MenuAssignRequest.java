package com.drip.admin.modules.system.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public class MenuAssignRequest {
    @NotNull(message = "menuIds is required")
    private List<Long> menuIds;
    public List<Long> getMenuIds() { return menuIds; }
    public void setMenuIds(List<Long> menuIds) { this.menuIds = menuIds; }
}
