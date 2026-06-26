package com.drip.admin.common.response;

import java.util.List;

public record PageResult<T>(List<T> list, long total, int page, int pageSize) {
}
