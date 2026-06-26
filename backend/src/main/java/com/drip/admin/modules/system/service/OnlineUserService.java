package com.drip.admin.modules.system.service;

import com.drip.admin.common.response.PageResult;

import java.util.Map;

public interface OnlineUserService {
    PageResult<Map<String, Object>> page(Map<String, String> q);

    Map<String, Object> detail(String tokenId);

    void kickout(String tokenId);
}
