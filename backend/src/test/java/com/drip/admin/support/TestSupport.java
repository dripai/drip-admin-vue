package com.drip.admin.support;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.drip.admin.common.log.OperationLog;
import com.drip.admin.common.security.RequirePermission;
import org.springframework.mock.web.MockHttpServletRequest;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class TestSupport {
    private TestSupport() {
    }

    public static void assertOperationLogged(Class<?> type, String methodName, Class<?>... parameterTypes) throws Exception {
        Method method = type.getMethod(methodName, parameterTypes);
        OperationLog operationLog = method.getAnnotation(OperationLog.class);

        assertTrue(operationLog != null && !operationLog.module().isBlank() && !operationLog.action().isBlank());
    }

    public static void assertPermission(Class<?> type, String methodName, String permissionCode, Class<?>... parameterTypes) throws Exception {
        Method method = type.getMethod(methodName, parameterTypes);
        RequirePermission permission = method.getAnnotation(RequirePermission.class);

        assertEquals(permissionCode, permission.value());
    }

    public static <T extends java.lang.annotation.Annotation> void assertMapping(Class<?> type, String methodName, Class<T> annotationType, String path, Class<?>... parameterTypes) throws Exception {
        Method method = type.getMethod(methodName, parameterTypes);
        java.lang.annotation.Annotation annotation = method.getAnnotation(annotationType);
        Method value = annotationType.getMethod("value");

        assertEquals(path, ((String[]) value.invoke(annotation))[0]);
    }

    public static MockHttpServletRequest request(String contextPath, String requestUri) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath(contextPath);
        request.setRequestURI(requestUri);
        return request;
    }

    public static boolean hasTableLogicField(Class<?> entityType) {
        for (Field field : entityType.getDeclaredFields()) {
            if (field.isAnnotationPresent(TableLogic.class)) {
                return true;
            }
        }
        return false;
    }
}
