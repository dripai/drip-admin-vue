package com.drip.admin.modules.system.dto;

public class LoginLogQuery extends PageQuery {
    private String username; private String status; private String loginType; private String deviceType; private String ip;
    public String getUsername() { return username; } public void setUsername(String username) { this.username = username; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public String getLoginType() { return loginType; } public void setLoginType(String loginType) { this.loginType = loginType; }
    public String getDeviceType() { return deviceType; } public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
    public String getIp() { return ip; } public void setIp(String ip) { this.ip = ip; }
}
