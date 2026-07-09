package infrastructure

import (
	"drip-admin/backend-go/internal/config"
	"github.com/redis/go-redis/v9"
)

func OpenRedis(cfg config.Config) *redis.Client {
	return redis.NewClient(&redis.Options{
		Addr:     cfg.Redis.Addr(),
		Password: cfg.Redis.Password,
		DB:       cfg.Redis.DB,
	})
}
