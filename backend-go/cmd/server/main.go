package main

import (
	"log"

	"drip-admin/backend-go/internal/config"
	"drip-admin/backend-go/internal/infrastructure"
	"drip-admin/backend-go/internal/modules/system"
)

func main() {
	cfg := config.Load()
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
