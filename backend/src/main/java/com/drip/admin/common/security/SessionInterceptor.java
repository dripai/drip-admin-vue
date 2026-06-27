package com.drip.admin.common.security;

import cn.dev33.satoken.stp.StpUtil;
import com.drip.admin.infrastructure.redis.OnlineSessionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class SessionInterceptor implements HandlerInterceptor {
    private final long activeTimeout;
    private final OnlineSessionService onlineSessionService;

    public SessionInterceptor(@Value("${sa-token.active-timeout}") long activeTimeout,
                              OnlineSessionService onlineSessionService) {
        this.activeTimeout = activeTimeout;
        this.onlineSessionService = onlineSessionService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response, Object handler) {
        if (isPublicPath(request)) {
            return true;
        }
        StpUtil.checkLogin();
        onlineSessionService.touchCurrent(activeTimeout);
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
