package com.drip.admin.modules.system.dto;

import com.drip.admin.common.dto.PageQuery;

public class PrintTemplateQuery extends PageQuery {
    private String code;

    private String name;

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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
