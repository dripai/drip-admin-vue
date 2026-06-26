package com.drip.admin.modules.system.dto;

import jakarta.validation.constraints.Size;

public class PasswordResetRequest {
    @Size(min = 8, max = 64, message = "password length must be 8 to 64")
    private String password;
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String passwordOrDefault() { return password == null || password.isBlank() ? "Admin@123456" : password; }
}
