package config

import (
	"fmt"
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

func Load() Config {
	v := viper.New()
	v.SetEnvPrefix("DRIP_GO")
	v.SetEnvKeyReplacer(strings.NewReplacer(".", "_", "-", "_"))
	v.AutomaticEnv()

	setDefaults(v)

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
	}
}

func (c MySQLConfig) DSN() string {
	return fmt.Sprintf("%s:%s@tcp(%s:%d)/%s?%s", c.Username, c.Password, c.Host, c.Port, c.Database, c.Params)
}

func (c RedisConfig) Addr() string {
	return fmt.Sprintf("%s:%d", c.Host, c.Port)
}

func setDefaults(v *viper.Viper) {
	v.SetDefault("server.port", "9001")
	v.SetDefault("mysql.host", "localhost")
	v.SetDefault("mysql.port", 3307)
	v.SetDefault("mysql.database", "drip-manager")
	v.SetDefault("mysql.username", "root")
	v.SetDefault("mysql.password", "root")
	v.SetDefault("mysql.params", "charset=utf8mb4&parseTime=True&loc=Local")
	v.SetDefault("redis.host", "localhost")
	v.SetDefault("redis.port", 6379)
	v.SetDefault("redis.password", "")
	v.SetDefault("redis.db", 0)
	v.SetDefault("token.name", "Authorization")
	v.SetDefault("token.timeout-seconds", 28800)
	v.SetDefault("token.active-timeout-seconds", 1800)
	v.SetDefault("job.script-dir", "../scripts")
}
