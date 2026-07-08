package com.drip.admin.common.security;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class SessionInterceptor implements HandlerInterceptor {
    private final long activeTimeout;
    private final SessionActivityRecorder sessionActivityRecorder;

    public SessionInterceptor(@Value("${sa-token.active-timeout}") long activeTimeout,
                              SessionActivityRecorder sessionActivityRecorder) {
        this.activeTimeout = activeTimeout;
        this.sessionActivityRecorder = sessionActivityRecorder;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response, Object handler) {
        StpUtil.checkLogin();
        sessionActivityRecorder.touchCurrent(activeTimeout);
        return true;
    }
}
