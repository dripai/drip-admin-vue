package com.drip.admin.common.log;

import com.drip.admin.common.log.OperationLog;
import com.drip.admin.common.log.LogService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static com.drip.admin.shared.utils.AdminUtils.*;

@Aspect
@Component
public class OperationLogAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(OperationLogAspect.class);

    private final LogService logService;

    public OperationLogAspect(LogService logService) {
        this.logService = logService;
    }

    @Around("@annotation(operationLog)")
    public Object write(ProceedingJoinPoint point, OperationLog operationLog) throws Throwable {
        long started = System.currentTimeMillis();
        HttpServletRequest request = currentRequest();
        String params = maskSensitive(Arrays.toString(point.getArgs()));
        try {
            Object result = point.proceed();
    safeLog(operationLog, request, params, "SUCCESS", null, System.currentTimeMillis() - started);
            return result;
        } catch (Throwable ex) {
    safeLog(operationLog, request, params, "FAIL", ex.getMessage(), System.currentTimeMillis() - started);
            throw ex;
        }
    }

   private void safeLog(OperationLog operationLog, HttpServletRequest request, String params, String status, String errorMessage, long costMs) {
        try {
            logService.operation(operationLog.module(), operationLog.action(), request.getMethod(), request.getRequestURI(), params, status, errorMessage, costMs);
        } catch (Exception ex) {
            LOGGER.error("Business operation log write failed: module={}, action={}, path={}",
                operationLog.module(), operationLog.action(), request.getRequestURI(), ex);
        }
    }
}
