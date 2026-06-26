package com.drip.admin.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordRequest(
    @NotBlank(message = "oldPassword is required") String oldPassword,
    @NotBlank(message = "newPassword is required") @Size(min = 8, max = 64, message = "newPassword length must be 8 to 64") String newPassword
) {
}
