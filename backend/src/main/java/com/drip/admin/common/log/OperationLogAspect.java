package com.drip.admin.common.log;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import static com.drip.admin.shared.utils.AdminUtils.*;

@Aspect
@Component
public class OperationLogAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(OperationLogAspect.class);

    private final OperationLogRecorder operationLogRecorder;

    public OperationLogAspect(OperationLogRecorder operationLogRecorder) {
        this.operationLogRecorder = operationLogRecorder;
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
            operationLogRecorder.operation(operationLog.module(), operationLog.action(), request.getMethod(), request.getRequestURI(), params, status, errorMessage, costMs);
        } catch (Exception ex) {
            LOGGER.error("Business operation log write failed: module={}, action={}, path={}",
                operationLog.module(), operationLog.action(), request.getRequestURI(), ex);
        }
    }
}
