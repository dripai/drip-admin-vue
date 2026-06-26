package com.drip.admin.modules.system.dto;

import jakarta.validation.constraints.AssertTrue;

public class DatabaseRestoreRequest {
    @AssertTrue(message = "confirmed must be true")
    private Boolean confirmed;
    public Boolean getConfirmed() { return confirmed; }
    public void setConfirmed(Boolean confirmed) { this.confirmed = confirmed; }
}
