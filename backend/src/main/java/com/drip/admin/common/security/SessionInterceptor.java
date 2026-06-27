package com.drip.admin.common.security;

import cn.dev33.satoken.stp.StpUtil;
import com.drip.admin.common.exception.BusinessException;
import com.drip.admin.infrastructure.redis.OnlineSessionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;
import java.util.*;

import static com.drip.admin.shared.utils.AdminUtils.*;

@Component
public class SessionInterceptor implements HandlerInterceptor {
    private final long maxDuration;
    private final long idleTimeout;
    private final OnlineSessionService onlineSessionService;

    public SessionInterceptor(@Value("${drip.session.max-duration-seconds}") long maxDuration,
                              @Value("${drip.session.idle-timeout-seconds}") long idleTimeout,
                              OnlineSessionService onlineSessionService) {
        this.maxDuration = maxDuration;
        this.idleTimeout = idleTimeout;
        this.onlineSessionService = onlineSessionService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response, Object handler) {
        if (isPublicPath(request)) {
            return true;
        }
        StpUtil.checkLogin();
        Object loginAtRaw = StpUtil.getSession().get("loginAt");
    if (loginAtRaw != null) {
            LocalDateTime loginAt = LocalDateTime.parse(String.valueOf(loginAtRaw));
    if (loginAt.plusSeconds(maxDuration).isBefore(LocalDateTime.now())) {
                StpUtil.logout();
    throw new BusinessException(401000, "会话已超过最大时长，请重新登录");
            }
        }
        StpUtil.getSession().set("lastActiveAt", LocalDateTime.now().toString());
        onlineSessionService.touchCurrent(idleTimeout);
        return true;
    }

    private static boolean isPublicPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isBlank() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        return path.equals("/")
            || path.equals("/favicon.ico")
            || path.equals("/health")
            || path.equals("/system/login")
            || path.equals("/system/publicConfig")
            || path.equals("/swagger-ui.html")
            || path.startsWith("/swagger-ui/")
            || path.startsWith("/v3/api-docs/")
            || path.equals("/actuator")
            || path.startsWith("/actuator/")
            || path.equals("/admin")
            || path.startsWith("/admin/");
    }
}
