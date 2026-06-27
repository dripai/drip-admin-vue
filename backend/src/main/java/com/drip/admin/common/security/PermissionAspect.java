package com.drip.admin.common.security;

import cn.dev33.satoken.stp.StpUtil;
import com.drip.admin.common.security.RequirePermission;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static com.drip.admin.shared.utils.AdminUtils.*;

@Aspect
@Component
public class PermissionAspect {
    @Around("@annotation(requirePermission)")
    public Object check(ProceedingJoinPoint point, RequirePermission requirePermission) throws Throwable {
        StpUtil.checkPermission(requirePermission.value());
        return point.proceed();
    }
}
