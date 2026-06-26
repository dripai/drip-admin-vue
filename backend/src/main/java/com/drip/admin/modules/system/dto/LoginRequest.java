package com.drip.admin.modules.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @NotBlank(message = "username is required") String username,
    @NotBlank(message = "password is required") String password,
    @NotBlank(message = "deviceType is required")
    @Size(max = 64, message = "deviceType length must be <= 64")
    @Pattern(regexp = "^[A-Za-z0-9_.:-]+$", message = "deviceType contains invalid characters") String deviceType
) {
}
