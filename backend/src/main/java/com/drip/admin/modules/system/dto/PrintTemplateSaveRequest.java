package com.drip.admin.modules.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PrintTemplateSaveRequest {
    @NotBlank(message = "code is required")
    @Size(max = 64, message = "code length must be <= 64")
    private String code;

    @NotBlank(message = "name is required")
    @Size(max = 100, message = "name length must be <= 100")
    private String name;

    @NotBlank(message = "paperType is required")
    @Size(max = 32, message = "paperType length must be <= 32")
    private String paperType;

    @NotBlank(message = "templateJson is required")
    private String templateJson;

    @NotNull(message = "status is required")
    @Min(value = 0, message = "status must be 0 or 1")
    @Max(value = 1, message = "status must be 0 or 1")
    private Integer status;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPaperType() {
        return paperType;
    }

    public void setPaperType(String paperType) {
        this.paperType = paperType;
    }

    public String getTemplateJson() {
        return templateJson;
    }

    public void setTemplateJson(String templateJson) {
        this.templateJson = templateJson;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer statusValue() {
        return status;
    }

}
