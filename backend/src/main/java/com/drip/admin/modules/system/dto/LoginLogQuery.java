package com.drip.admin.modules.system.dto;

import java.time.LocalDateTime;

public class LoginLogQuery extends PageQuery {
    private String username; private String status; private String loginType; private String deviceType; private String ip;
    private LocalDateTime loginFrom; private LocalDateTime loginTo;
    public String getUsername() { return username; } public void setUsername(String username) { this.username = username; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public String getLoginType() { return loginType; } public void setLoginType(String loginType) { this.loginType = loginType; }
    public String getDeviceType() { return deviceType; } public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
    public String getIp() { return ip; } public void setIp(String ip) { this.ip = ip; }
    public LocalDateTime getLoginFrom() { return loginFrom; } public void setLoginFrom(LocalDateTime loginFrom) { this.loginFrom = loginFrom; }
    public LocalDateTime getLoginTo() { return loginTo; } public void setLoginTo(LocalDateTime loginTo) { this.loginTo = loginTo; }
}
