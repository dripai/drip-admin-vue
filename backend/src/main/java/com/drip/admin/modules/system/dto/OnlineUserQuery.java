package com.drip.admin.modules.system.dto;

public class OnlineUserQuery extends PageQuery {
    private String username; private String ip;
    public String getUsername() { return username; } public void setUsername(String username) { this.username = username; }
    public String getIp() { return ip; } public void setIp(String ip) { this.ip = ip; }
}
