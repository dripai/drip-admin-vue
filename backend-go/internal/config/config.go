package config

import (
	"errors"
	"fmt"
	"os"
	"strings"

	"github.com/spf13/viper"
)

type Config struct {
	Server ServerConfig
	MySQL  MySQLConfig
	Redis  RedisConfig
	Token  TokenConfig
	Job    JobConfig
}

type ServerConfig struct {
	Port string
}

type MySQLConfig struct {
	Host     string
	Port     int
	Database string
	Username string
	Password string
	Params   string
}

type RedisConfig struct {
	Host     string
	Port     int
	Password string
	DB       int
}

type TokenConfig struct {
	Name                 string
	TimeoutSeconds       int64
	ActiveTimeoutSeconds int64
}

type JobConfig struct {
	ScriptDir string
}

func Load() (Config, error) {
	v := viper.New()
	v.SetConfigFile(configFile())
	v.SetConfigType("yaml")
	v.SetEnvPrefix("DRIP_GO")
	v.SetEnvKeyReplacer(strings.NewReplacer(".", "_", "-", "_"))
	v.AutomaticEnv()

	setDefaults(v)
	if err := v.ReadInConfig(); err != nil {
		var notFound viper.ConfigFileNotFoundError
		if errors.As(err, &notFound) || os.IsNotExist(err) {
			return Config{}, fmt.Errorf("config file not found: %s", configFile())
		}
		return Config{}, err
	}

	return Config{
		Server: ServerConfig{Port: v.GetString("server.port")},
		MySQL: MySQLConfig{
			Host:     v.GetString("mysql.host"),
			Port:     v.GetInt("mysql.port"),
			Database: v.GetString("mysql.database"),
			Username: v.GetString("mysql.username"),
			Password: v.GetString("mysql.password"),
			Params:   v.GetString("mysql.params"),
		},
		Redis: RedisConfig{
			Host:     v.GetString("redis.host"),
			Port:     v.GetInt("redis.port"),
			Password: v.GetString("redis.password"),
			DB:       v.GetInt("redis.db"),
		},
		Token: TokenConfig{
			Name:                 v.GetString("token.name"),
			TimeoutSeconds:       v.GetInt64("token.timeout-seconds"),
			ActiveTimeoutSeconds: v.GetInt64("token.active-timeout-seconds"),
		},
		Job: JobConfig{ScriptDir: v.GetString("job.script-dir")},
	}, nil
}

func (c MySQLConfig) DSN() string {
	dsn := fmt.Sprintf("%s:%s@tcp(%s:%d)/%s", c.Username, c.Password, c.Host, c.Port, c.Database)
	if strings.TrimSpace(c.Params) != "" {
		dsn += "?" + c.Params
	}
	return dsn
}

func (c RedisConfig) Addr() string {
	return fmt.Sprintf("%s:%d", c.Host, c.Port)
}

func configFile() string {
	if value := strings.TrimSpace(os.Getenv("DRIP_GO_CONFIG")); value != "" {
		return value
	}
	return "config.yaml"
}

func setDefaults(v *viper.Viper) {
	v.SetDefault("server.port", "9001")
	v.SetDefault("redis.host", "localhost")
	v.SetDefault("redis.port", 6379)
	v.SetDefault("redis.password", "")
	v.SetDefault("redis.db", 0)
	v.SetDefault("token.name", "Authorization")
	v.SetDefault("token.timeout-seconds", 28800)
	v.SetDefault("token.active-timeout-seconds", 1800)
	v.SetDefault("job.script-dir", "../scripts")
}
