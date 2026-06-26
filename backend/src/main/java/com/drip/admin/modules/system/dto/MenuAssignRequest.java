package com.drip.admin.modules.system.dto;

import java.util.List;

public class MenuAssignRequest {
    private List<Long> menuIds;
    public List<Long> getMenuIds() { return menuIds; }
    public void setMenuIds(List<Long> menuIds) { this.menuIds = menuIds; }
}
