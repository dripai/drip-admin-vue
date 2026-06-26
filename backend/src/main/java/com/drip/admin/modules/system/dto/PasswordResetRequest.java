package com.drip.admin.modules.system.dto;

public class PasswordResetRequest {
    private String password;
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String passwordOrDefault() { return password == null || password.isBlank() ? "Admin@123456" : password; }
}
