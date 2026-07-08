package com.drip.admin.common.export;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public class ExportRequest<T> {
    @Valid
    private T query;

    @Valid
    @NotEmpty(message = "export columns required")
    @Size(max = 100, message = "export columns size must be <= 100")
    private List<ExportColumnRequest> columns;

    public T getQuery() { return query; }
    public void setQuery(T query) { this.query = query; }
    public List<ExportColumnRequest> getColumns() { return columns; }
    public void setColumns(List<ExportColumnRequest> columns) { this.columns = columns; }
}
