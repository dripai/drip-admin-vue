package config

import (
	"os"
	"path/filepath"
	"testing"

	"github.com/stretchr/testify/require"
)

func TestLoadLoggingLevelFromConfig(t *testing.T) {
	path := writeConfigForTest(t, "debug")
	t.Setenv("DRIP_GO_CONFIG", path)

	cfg, err := Load()

	require.NoError(t, err)
	require.Equal(t, "debug", cfg.Logging.Level)
}

func TestLoadLoggingLevelFromEnv(t *testing.T) {
	path := writeConfigForTest(t, "info")
	t.Setenv("DRIP_GO_CONFIG", path)
	t.Setenv("DRIP_GO_LOGGING_LEVEL", "warn")

	cfg, err := Load()

	require.NoError(t, err)
	require.Equal(t, "warn", cfg.Logging.Level)
}

func writeConfigForTest(t *testing.T, level string) string {
	t.Helper()
	path := filepath.Join(t.TempDir(), "config.yaml")
	content := []byte("logging:\n  level: \"" + level + "\"\n")
	require.NoError(t, os.WriteFile(path, content, 0o600))
	return path
}
