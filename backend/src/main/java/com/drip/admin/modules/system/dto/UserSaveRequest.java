package com.drip.admin.modules.system.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserSaveRequest {
    @NotBlank(message = "username is required")
    @Size(max = 64, message = "username length must be <= 64")
    private String username;
    @NotBlank(message = "realName is required")
    @Size(max = 64, message = "realName length must be <= 64")
    private String realName;
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "phone format is invalid")
    private String phone;
    @Email(message = "email format is invalid")
    @Size(max = 128, message = "email length must be <= 128")
    private String email;
    private Integer status; private Long deptId; private String remark;
    @Size(min = 8, max = 64, message = "password length must be 8 to 64")
    private String password;
    public String getUsername() { return username; } public void setUsername(String username) { this.username = username; }
    public String getRealName() { return realName; } public void setRealName(String realName) { this.realName = realName; }
    public String getPhone() { return phone; } public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; } public void setEmail(String email) { this.email = email; }
    public Integer getStatus() { return status; } public void setStatus(Integer status) { this.status = status; }
    public Long getDeptId() { return deptId; } public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getRemark() { return remark; } public void setRemark(String remark) { this.remark = remark; }
    public String getPassword() { return password; } public void setPassword(String password) { this.password = password; }
}
