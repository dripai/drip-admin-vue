package com.drip.admin.modules.system.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
    @NotBlank(message = "realName is required")
    @Size(max = 64, message = "realName length must be <= 64")
    String realName,
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "phone format is invalid")
    String phone,
    @Email(message = "email format is invalid")
    @Size(max = 128, message = "email length must be <= 128")
    String email
) {
}
