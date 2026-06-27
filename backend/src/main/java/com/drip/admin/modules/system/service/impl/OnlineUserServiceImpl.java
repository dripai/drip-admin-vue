package com.drip.admin.modules.system.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.drip.admin.common.exception.BusinessException;
import com.drip.admin.common.response.PageResult;
import com.drip.admin.infrastructure.redis.OnlineSessionService;
import com.drip.admin.modules.system.dto.OnlineUserQuery;
import com.drip.admin.modules.system.service.OnlineUserService;
import com.drip.admin.modules.system.vo.OnlineUserVo;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class OnlineUserServiceImpl implements OnlineUserService {
    private final OnlineSessionService onlineSessionService;
    public OnlineUserServiceImpl(OnlineSessionService onlineSessionService) { this.onlineSessionService = onlineSessionService; }
    @Override public PageResult<OnlineUserVo> page(OnlineUserQuery query) { Map<String, String> params = new HashMap<>(); params.put("page", String.valueOf(query.pageOrDefault())); params.put("pageSize", String.valueOf(query.pageSizeOrDefault())); if (query.getUsername() != null) params.put("username", query.getUsername()); if (query.getIp() != null) params.put("ip", query.getIp()); if (query.getDeviceType() != null) params.put("deviceType", query.getDeviceType()); var page = onlineSessionService.page(params); return new PageResult<>(page.list().stream().map(OnlineUserServiceImpl::toVo).toList(), page.total(), page.page(), page.pageSize()); }
    @Override public OnlineUserVo detail(String tokenId) { return toVo(onlineSessionService.detail(tokenId)); }
    @Override public void kickout(String tokenId) { if (java.util.Objects.equals(tokenId, StpUtil.getTokenValue())) throw new BusinessException(400000, "operation failed"); StpUtil.logoutByTokenValue(tokenId); onlineSessionService.remove(tokenId); }
    private static OnlineUserVo toVo(Map<String, Object> row) {
        String tokenId = string(row.get("tokenId"));
        return new OnlineUserVo(
            tokenId,
            longValue(row.get("userId")),
            string(row.get("username")),
            string(row.get("realName")),
            string(row.get("deviceType")),
            string(row.get("ip")),
            string(row.get("userAgent")),
            string(row.get("loginAt")),
            string(row.get("lastActiveAt")),
            string(row.get("expireAt")),
            java.util.Objects.equals(tokenId, currentToken())
        );
    }
    private static String currentToken() {
        try {
            return StpUtil.getTokenValue();
        } catch (RuntimeException ex) {
            return null;
        }
    }
    private static String string(Object value) { return value == null ? null : String.valueOf(value); }
    private static Long longValue(Object value) { if (value instanceof Number n) return n.longValue(); return value == null ? null : Long.parseLong(String.valueOf(value)); }
}
