package com.drip.admin.common.security;

import cn.dev33.satoken.stp.StpUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PermissionAspect {
    @Around("@annotation(requirePermission)")
    public Object check(ProceedingJoinPoint point, RequirePermission requirePermission) throws Throwable {
        StpUtil.checkPermission(requirePermission.value());
        return point.proceed();
    }
}
