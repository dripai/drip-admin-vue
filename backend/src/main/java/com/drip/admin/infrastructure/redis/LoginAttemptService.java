package com.drip.admin.infrastructure.redis;

import com.drip.admin.common.exception.BusinessException;
import com.drip.admin.modules.system.service.ConfigService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {
    private static final String KEY_PREFIX = "drip:login:fail:";

    private final StringRedisTemplate redis;
    private final ConfigService configService;

    public LoginAttemptService(StringRedisTemplate redis, ConfigService configService) {
        this.redis = redis;
        this.configService = configService;
    }

    public void assertNotLocked(String username) {
        if (failureCount(username) >= configService.requiredInt("login.maxFailures")) {
            throw new BusinessException(401000, "用户名或密码错误");
        }
    }

    public void recordFailure(String username) {
        String key = key(username);
        try {
            Long failures = redis.opsForValue().increment(key);
            if (failures == null) {
                throw new BusinessException(500000, "failed to update login failure limit");
            }
            redis.expire(key, configService.requiredLong("login.lockSeconds"), TimeUnit.SECONDS);
        } catch (BusinessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new BusinessException(500000, "failed to update login failure limit");
        }
    }

    public void clear(String username) {
        try {
            redis.delete(key(username));
        } catch (RuntimeException ex) {
            throw new BusinessException(500000, "failed to clear login failure limit");
        }
    }

    private int failureCount(String username) {
        try {
            String value = redis.opsForValue().get(key(username));
            if (value == null || value.isBlank()) {
                return 0;
            }
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            redis.delete(key(username));
            return 0;
        } catch (RuntimeException ex) {
            throw new BusinessException(500000, "failed to read login failure limit");
        }
    }

    private static String key(String username) {
        return KEY_PREFIX + username.trim().toLowerCase(Locale.ROOT);
    }
}
