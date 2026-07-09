package infrastructure

import (
	"drip-admin/backend-go/internal/config"
	"go.uber.org/zap"
)

func NewLogger(config.Config) *zap.Logger {
	logger, err := zap.NewProduction()
	if err != nil {
		panic(err)
	}
	return logger
}
