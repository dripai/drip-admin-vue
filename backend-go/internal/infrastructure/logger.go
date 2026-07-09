package infrastructure

import (
	"fmt"
	"strings"

	"drip-admin/backend-go/internal/config"
	"go.uber.org/zap"
	"go.uber.org/zap/zapcore"
)

func NewLogger(cfg config.Config) *zap.Logger {
	level, err := parseLogLevel(cfg.Logging.Level)
	if err != nil {
		panic(err)
	}
	zapConfig := zap.NewProductionConfig()
	zapConfig.Level.SetLevel(level)
	logger, err := zapConfig.Build()
	if err != nil {
		panic(err)
	}
	return logger
}

func parseLogLevel(value string) (zapcore.Level, error) {
	levelText := strings.TrimSpace(strings.ToLower(value))
	if levelText == "" {
		return zapcore.InfoLevel, nil
	}
	var level zapcore.Level
	if err := level.UnmarshalText([]byte(levelText)); err != nil {
		return zapcore.InfoLevel, fmt.Errorf("invalid logging.level: %s", value)
	}
	return level, nil
}
