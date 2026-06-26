package com.drip.admin.modules.system.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class PageQuery {
    @Min(value = 1, message = "page must be >= 1")
    private Integer page;

    @Min(value = 1, message = "pageSize must be >= 1")
    @Max(value = 100, message = "pageSize must be <= 100")
    private Integer pageSize;

    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }
    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }

    public int pageOrDefault() { return page == null || page < 1 ? 1 : page; }
    public int pageSizeOrDefault() { return pageSize == null ? 20 : Math.min(100, Math.max(1, pageSize)); }
}
