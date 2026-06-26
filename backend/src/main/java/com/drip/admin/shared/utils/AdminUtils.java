package com.drip.admin.shared.utils;

import cn.dev33.satoken.stp.StpUtil;
import com.drip.admin.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AdminUtils {
    private AdminUtils() {
    }

   public static long currentUserId() {
        return StpUtil.getLoginIdAsLong();
    }

   public static HttpServletRequest currentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attrs.getRequest();
    }

   public static String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest((salt + ":" + password).getBytes(StandardCharsets.UTF_8));
            StringBuilder out = new StringBuilder();
    for (byte b : bytes) out.append(String.format("%02x", b));
            return out.toString();
        } catch (Exception ex) {
    throw new IllegalStateException("Cannot hash password", ex);
        }
    }

   public static List<Map<String, Object>> buildTree(List<LinkedHashMap<String, Object>> rows, String parentColumn) {
        Map<Long, LinkedHashMap<String, Object>> byId = new LinkedHashMap<>();
    for (LinkedHashMap<String, Object> row : rows) {
            row.put("children", new ArrayList<Map<String, Object>>());
            byId.put(longOf(row.get("id")), row);
        }
        List<Map<String, Object>> roots = new ArrayList<>();
    for (LinkedHashMap<String, Object> row : rows) {
            long parentId = longOf(row.get(parentColumn));
    if (parentId == 0 || !byId.containsKey(parentId)) {
                roots.add(row);
            } else {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> children = (List<Map<String, Object>>) byId.get(parentId).get("children");
                children.add(row);
            }
        }
        return roots;
    }

   public static String maskSensitive(String value) {
    if (value == null) return null;
        return value.replaceAll("(?i)(password|token|secret)=[^,}\\]]+", "$1=******");
    }

   public static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",")[0].trim();
        return request.getRemoteAddr();
    }

   public static void requireNonBlank(Map<String, Object> body, String... names) {
    for (String name : names) {
    if (body.containsKey(name) && String.valueOf(body.get(name)).isBlank()) throw new BusinessException(400000, name + " must not be blank");
        }
    }

   public static List<Long> longList(Object raw) {
    if (raw == null) return List.of();
    if (!(raw instanceof List<?> list)) throw new BusinessException(400000, "ID list is required");
        return list.stream().map(AdminUtils::longOf).toList();
    }

   public static int parseInt(String value, int defaultValue) {
    if (value == null || value.isBlank()) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

   public static int intValue(Map<String, Object> body, String key, int defaultValue) {
        return body.containsKey(key) ? intOf(body.get(key)) : defaultValue;
    }

   public static int intOf(Object value) {
    if (value instanceof Number n) return n.intValue();
    if (value == null) return 0;
        return Integer.parseInt(String.valueOf(value));
    }

   public static long longOf(Object value) {
    if (value instanceof Number n) return n.longValue();
    if (value == null) return 0;
        return Long.parseLong(String.valueOf(value));
    }

   public static String stringValue(Map<String, Object> body, String key, String defaultValue) {
        Object value = body.get(key);
        return value == null ? defaultValue : String.valueOf(value);
    }

   public static String stringOf(Object value) {
        return value == null ? null : String.valueOf(value);
    }

   public static String snakeToCamel(String value) {
        StringBuilder out = new StringBuilder();
        boolean upper = false;
    for (char c : value.toCharArray()) {
    if (c == '_') {
                upper = true;
            } else if (upper) {
                out.append(Character.toUpperCase(c));
                upper = false;
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }
}
