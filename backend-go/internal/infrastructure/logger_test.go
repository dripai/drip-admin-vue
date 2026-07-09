package infrastructure

import (
	"testing"

	"drip-admin/backend-go/internal/config"
	"github.com/stretchr/testify/require"
	"go.uber.org/zap/zapcore"
)

func TestNewLoggerUsesConfiguredLevel(t *testing.T) {
	logger := NewLogger(config.Config{Logging: config.LoggingConfig{Level: "error"}})

	require.False(t, logger.Core().Enabled(zapcore.InfoLevel))
	require.True(t, logger.Core().Enabled(zapcore.ErrorLevel))
}

func TestNewLoggerRejectsInvalidLevel(t *testing.T) {
	require.Panics(t, func() {
		_ = NewLogger(config.Config{Logging: config.LoggingConfig{Level: "loud"}})
	})
}
