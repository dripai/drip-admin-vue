package com.drip.admin.modules.system.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.drip.admin.common.exception.BusinessException;
import com.drip.admin.common.response.PageResult;
import com.drip.admin.infrastructure.redis.OnlineSessionService;
import com.drip.admin.modules.system.service.OnlineUserService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

@Service
public class OnlineUserServiceImpl implements OnlineUserService {
    private final OnlineSessionService onlineSessionService;

    public OnlineUserServiceImpl(OnlineSessionService onlineSessionService) {
        this.onlineSessionService = onlineSessionService;
    }

    @Override
    public PageResult<Map<String, Object>> page(Map<String, String> q) {
        return onlineSessionService.page(q);
    }

    @Override
    public Map<String, Object> detail(String tokenId) {
        return onlineSessionService.detail(tokenId);
    }

    @Override
    public void kickout(String tokenId) {
        if (Objects.equals(tokenId, StpUtil.getTokenValue())) {
            throw new BusinessException(400000, "current token cannot be kicked out");
        }
        StpUtil.logoutByTokenValue(tokenId);
        onlineSessionService.remove(tokenId);
    }
}
