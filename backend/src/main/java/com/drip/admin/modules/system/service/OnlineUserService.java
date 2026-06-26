package com.drip.admin.modules.system.service;

import com.drip.admin.common.response.PageResult;
import com.drip.admin.modules.system.dto.OnlineUserQuery;
import com.drip.admin.modules.system.vo.OnlineUserVo;

public interface OnlineUserService {
    PageResult<OnlineUserVo> page(OnlineUserQuery query);
    OnlineUserVo detail(String tokenId);
    void kickout(String tokenId);
}
