package com.drip.admin.common.export;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ExportColumnRequest {
    @NotBlank(message = "column key is required")
    @Size(max = 128, message = "column key length must be <= 128")
    private String key;

    @NotBlank(message = "column title is required")
    @Size(max = 64, message = "column title length must be <= 64")
    private String title;

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}
