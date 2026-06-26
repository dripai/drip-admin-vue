package com.drip.admin.modules.system.service;

import com.drip.admin.common.response.PageResult;
import com.drip.admin.modules.system.vo.OnlineUserVo;

import java.util.Map;

public interface OnlineUserService {
    PageResult<OnlineUserVo> page(Map<String, String> q);

    OnlineUserVo detail(String tokenId);

    void kickout(String tokenId);
}
