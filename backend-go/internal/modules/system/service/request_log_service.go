package service

import (
	"fmt"
	"net/http"
	"runtime/debug"
	"time"

	"drip-admin/backend-go/internal/common"
	"github.com/gin-gonic/gin"
	"go.uber.org/zap"
)

func (s *Server) RequestLoggerMiddleware() gin.HandlerFunc {
	return func(c *gin.Context) {
		started := time.Now()
		c.Next()

		cost := time.Since(started)
		status := c.Writer.Status()
		fields := []zap.Field{
			zap.String("method", c.Request.Method),
			zap.String("path", c.Request.URL.Path),
			zap.String("fullPath", c.FullPath()),
			zap.String("query", c.Request.URL.RawQuery),
			zap.Int("status", status),
			zap.Duration("cost", cost),
			zap.Int64("costMs", cost.Milliseconds()),
			zap.String("ip", clientIP(c)),
			zap.String("userAgent", c.GetHeader("User-Agent")),
		}
		if len(c.Errors) > 0 {
			fields = append(fields, zap.String("errors", c.Errors.String()))
		}
		switch {
		case status >= http.StatusInternalServerError:
			s.logger.Error("http request failed", fields...)
		case status >= http.StatusBadRequest:
			s.logger.Warn("http request rejected", fields...)
		default:
			s.logger.Info("http request completed", fields...)
		}
	}
}

func (s *Server) RecoveryMiddleware() gin.HandlerFunc {
	return gin.CustomRecovery(func(c *gin.Context, recovered any) {
		err := fmt.Errorf("panic: %v", recovered)
		_ = c.Error(err)
		s.logger.Error("http request panic",
			zap.String("method", c.Request.Method),
			zap.String("path", c.Request.URL.Path),
			zap.String("fullPath", c.FullPath()),
			zap.String("ip", clientIP(c)),
			zap.Any("panic", recovered),
			zap.ByteString("stack", debug.Stack()),
		)
		common.Fail(c, http.StatusInternalServerError, 500000, "系统内部错误")
		c.Abort()
	})
}
