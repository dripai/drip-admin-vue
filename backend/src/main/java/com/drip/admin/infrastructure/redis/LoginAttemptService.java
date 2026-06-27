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
            throw new BusinessException(401000, lockedMessage(username));
        }
    }

    public int recordFailure(String username) {
        String key = key(username);
        try {
            Long failures = redis.opsForValue().increment(key);
            if (failures == null) {
                throw new BusinessException(500000, "failed to update login failure limit");
            }
            long lockSeconds = configService.requiredLong("login.lockSeconds");
            redis.expire(key, lockSeconds, TimeUnit.SECONDS);
            int maxFailures = configService.requiredInt("login.maxFailures");
            if (failures >= maxFailures) {
                throw new BusinessException(401000, lockedMessage(lockSeconds));
            }
            return (int) (maxFailures - failures);
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

    public void unlock(String username) {
        clear(username);
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

    private String lockedMessage(String username) {
        String key = key(username);
        try {
            Long seconds = redis.getExpire(key, TimeUnit.SECONDS);
            if (seconds == null || seconds <= 0) {
                seconds = configService.requiredLong("login.lockSeconds");
            }
            return lockedMessage(seconds);
        } catch (RuntimeException ex) {
            throw new BusinessException(500000, "failed to read login failure limit");
        }
    }

    private static String lockedMessage(long seconds) {
        return "账号已锁定，请" + formatDuration(seconds) + "后再试";
    }

    private static String formatDuration(long seconds) {
        if (seconds <= 0) {
            return "稍后";
        }
        long minutes = seconds / 60;
        long remainSeconds = seconds % 60;
        if (minutes == 0) {
            return seconds + "秒";
        }
        if (remainSeconds == 0) {
            return minutes + "分钟";
        }
        return minutes + "分" + remainSeconds + "秒";
    }

    private static String key(String username) {
        return KEY_PREFIX + username.trim().toLowerCase(Locale.ROOT);
    }
}
