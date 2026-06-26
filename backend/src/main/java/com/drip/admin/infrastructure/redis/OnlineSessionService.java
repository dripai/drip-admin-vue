package com.drip.admin.infrastructure.redis;

import cn.dev33.satoken.stp.StpUtil;
import com.drip.admin.common.exception.BusinessException;
import com.drip.admin.common.response.PageResult;
import com.drip.admin.shared.utils.AdminUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.Cursor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class OnlineSessionService {
    private static final String KEY_PREFIX = "drip:online:";
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public OnlineSessionService(StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    public void register(long userId, Map<String, Object> user, String tokenId, String deviceType,
                         long idleTimeoutSeconds, long maxDurationSeconds, HttpServletRequest request) {
        LocalDateTime now = LocalDateTime.now();
        Map<String, Object> session = new LinkedHashMap<>();
        session.put("tokenId", tokenId);
        session.put("userId", userId);
        session.put("username", user.get("username"));
        session.put("realName", user.get("real_name"));
        session.put("deviceType", deviceType);
        session.put("ip", AdminUtils.clientIp(request));
        session.put("userAgent", request.getHeader("User-Agent"));
        session.put("loginAt", now.toString());
        session.put("lastActiveAt", now.toString());
        session.put("expireAt", now.plusSeconds(idleTimeoutSeconds).atZone(ZoneId.systemDefault()).toInstant().toString());
        write(tokenId, session, Math.max(idleTimeoutSeconds, maxDurationSeconds));
    }

    public void touchCurrent(long idleTimeoutSeconds) {
        if (!StpUtil.isLogin()) {
            return;
        }
        String tokenId = StpUtil.getTokenValue();
        if (tokenId == null || tokenId.isBlank()) {
            return;
        }
        Map<String, Object> session = get(tokenId);
        if (session.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        session.put("lastActiveAt", now.toString());
        session.put("expireAt", now.plusSeconds(idleTimeoutSeconds).atZone(ZoneId.systemDefault()).toInstant().toString());
        write(tokenId, session, idleTimeoutSeconds);
    }

    public PageResult<Map<String, Object>> page(Map<String, String> q) {
        List<Map<String, Object>> rows = list();
        rows = filter(rows, "username", q.get("username"));
        rows = filter(rows, "ip", q.get("ip"));
        rows = filter(rows, "deviceType", q.get("deviceType"));
        int page = Math.max(1, AdminUtils.parseInt(q.get("page"), 1));
        int pageSize = Math.min(100, Math.max(1, AdminUtils.parseInt(q.get("pageSize"), 10)));
        int from = Math.min(rows.size(), (page - 1) * pageSize);
        int to = Math.min(rows.size(), from + pageSize);
        return new PageResult<>(rows.subList(from, to), rows.size(), page, pageSize);
    }

    public Map<String, Object> detail(String tokenId) {
        Map<String, Object> session = get(tokenId);
        if (session.isEmpty()) {
            throw new BusinessException(404000, "online session not found");
        }
        return session;
    }

    public void remove(String tokenId) {
        if (tokenId != null && !tokenId.isBlank()) {
            redis.delete(key(tokenId));
        }
    }

    private List<Map<String, Object>> list() {
        List<Map<String, Object>> rows = new ArrayList<>();
        ScanOptions options = ScanOptions.scanOptions().match(KEY_PREFIX + "*").count(100).build();
        try (Cursor<String> cursor = redis.scan(options)) {
            while (cursor.hasNext()) {
                String key = cursor.next();
                String value = redis.opsForValue().get(key);
                if (value == null || value.isBlank()) {
                    continue;
                }
                try {
                    rows.add(objectMapper.readValue(value, MAP_TYPE));
                } catch (Exception ex) {
                    redis.delete(key);
                }
            }
        }
        rows.sort(Comparator.comparing(v -> String.valueOf(v.getOrDefault("lastActiveAt", "")), Comparator.reverseOrder()));
        return rows;
    }

    private static List<Map<String, Object>> filter(List<Map<String, Object>> rows, String key, String value) {
        if (value == null || value.isBlank()) {
            return rows;
        }
        String needle = value.trim().toLowerCase();
        return rows.stream()
            .filter(row -> String.valueOf(row.getOrDefault(key, "")).toLowerCase().contains(needle))
            .toList();
    }

    private Map<String, Object> get(String tokenId) {
        String value = redis.opsForValue().get(key(tokenId));
        if (value == null || value.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(value, MAP_TYPE);
        } catch (Exception ex) {
            redis.delete(key(tokenId));
            return Map.of();
        }
    }

    private void write(String tokenId, Map<String, Object> session, long ttlSeconds) {
        try {
            redis.opsForValue().set(key(tokenId), objectMapper.writeValueAsString(session), ttlSeconds, TimeUnit.SECONDS);
        } catch (Exception ex) {
            throw new BusinessException(500000, "failed to write online session");
        }
    }

    private static String key(String tokenId) {
        return KEY_PREFIX + tokenId;
    }
}
