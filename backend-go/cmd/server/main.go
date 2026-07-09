package main

import (
	"log"

	"drip-admin/backend-go/internal/config"
	"drip-admin/backend-go/internal/infrastructure"
	"drip-admin/backend-go/internal/modules/system"
)

// @title Drip Admin Go API
// @version 1.0.0
// @description Go backend API aligned with the Java backend contract.
// @BasePath /api
// @schemes http
// @securityDefinitions.apikey ApiKeyAuth
// @in header
// @name Authorization
func main() {
	cfg, err := config.Load()
	if err != nil {
		log.Fatalf("load config: %v", err)
	}
	logger := infrastructure.NewLogger(cfg)
	db, err := infrastructure.OpenDatabase(cfg, logger)
	if err != nil {
		log.Fatalf("open database: %v", err)
	}
	redisClient := infrastructure.OpenRedis(cfg)
	router := system.NewServer(cfg, db, redisClient, logger).Router()
	if err := router.Run(":" + cfg.Server.Port); err != nil {
		log.Fatalf("start server: %v", err)
	}
}
