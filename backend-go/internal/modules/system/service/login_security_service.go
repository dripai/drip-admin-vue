package service

import (
	"context"
	"fmt"
	"strings"
	"time"

	"drip-admin/backend-go/internal/common"
	"github.com/redis/go-redis/v9"
)

func (s *Server) assertLoginNotLocked(ctx context.Context, username string) error {
	failures, err := s.loginFailureCount(ctx, username)
	if err != nil {
		return err
	}
	maxFailures, err := s.requiredIntConfig("login.maxFailures")
	if err != nil {
		return err
	}
	if failures >= maxFailures {
		ttl := s.redis.TTL(ctx, loginAttemptKey(username)).Val()
		return common.NewBusinessError(401000, "账号已锁定，请"+formatDuration(ttl)+"后再试")
	}
	return nil
}

func (s *Server) recordLoginFailure(ctx context.Context, username string) (int, error) {
	key := loginAttemptKey(username)
	failures, err := s.redis.Incr(ctx, key).Result()
	if err != nil {
		return 0, common.NewBusinessError(500000, "failed to update login failure limit")
	}
	lockSeconds, err := s.requiredLongConfig("login.lockSeconds")
	if err != nil {
		return 0, err
	}
	if err := s.redis.Expire(ctx, key, time.Duration(lockSeconds)*time.Second).Err(); err != nil {
		return 0, common.NewBusinessError(500000, "failed to update login failure limit")
	}
	maxFailures, err := s.requiredIntConfig("login.maxFailures")
	if err != nil {
		return 0, err
	}
	if int(failures) >= maxFailures {
		return 0, common.NewBusinessError(401000, "账号已锁定，请"+formatDuration(time.Duration(lockSeconds)*time.Second)+"后再试")
	}
	return maxFailures - int(failures), nil
}

func (s *Server) loginFailureCount(ctx context.Context, username string) (int, error) {
	value, err := s.redis.Get(ctx, loginAttemptKey(username)).Result()
	if err == redisNil() {
		return 0, nil
	}
	if err != nil {
		return 0, common.NewBusinessError(500000, "failed to read login failure limit")
	}
	var count int
	_, scanErr := fmt.Sscanf(value, "%d", &count)
	if scanErr != nil {
		_ = s.redis.Del(ctx, loginAttemptKey(username)).Err()
		return 0, nil
	}
	return count, nil
}

func loginAttemptKey(username string) string {
	return loginAttemptKeyPrefix + strings.ToLower(strings.TrimSpace(username))
}

func (s *Server) removeExistingUserDeviceSession(ctx context.Context, userID common.Int64String, deviceType string) error {
	return s.redis.Del(ctx, onlineSessionKey(userID, deviceType)).Err()
}

func redisNil() error {
	return redis.Nil
}

func formatDuration(duration time.Duration) string {
	seconds := int64(duration.Seconds())
	if seconds <= 0 {
		return "稍后"
	}
	minutes := seconds / 60
	remain := seconds % 60
	if minutes == 0 {
		return fmt.Sprintf("%d秒", seconds)
	}
	if remain == 0 {
		return fmt.Sprintf("%d分钟", minutes)
	}
	return fmt.Sprintf("%d分%d秒", minutes, remain)
}
