package com.drip.admin.common.export;

import java.util.function.Function;

public class ExportColumn<T> {
    private final Function<T, Object> valueResolver;

    private ExportColumn(Function<T, Object> valueResolver) {
        this.valueResolver = valueResolver;
    }

    public static <T> ExportColumn<T> of(Function<T, Object> valueResolver) {
        return new ExportColumn<>(valueResolver);
    }

    public Object value(T row) {
        Object value = valueResolver.apply(row);
        return value == null ? "" : value;
    }
}
