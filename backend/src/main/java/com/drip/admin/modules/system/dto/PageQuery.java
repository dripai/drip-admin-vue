package com.drip.admin.modules.system.dto;

public class PageQuery {
    private Integer page;
    private Integer pageSize;

    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }
    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }

    public int pageOrDefault() { return page == null || page < 1 ? 1 : page; }
    public int pageSizeOrDefault() { return pageSize == null ? 20 : Math.min(100, Math.max(1, pageSize)); }
}
