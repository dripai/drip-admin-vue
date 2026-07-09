package service

import (
	"drip-admin/backend-go/internal/common"
	"github.com/gin-gonic/gin"
	"go.uber.org/zap"
)

func (s *Server) writeLoginLog(c *gin.Context, userID *common.Int64String, username string, realName *string, loginType string, status string, reason string, deviceType string) {
	var failureReason *string
	if reason != "" {
		failureReason = &reason
	}
	ip := clientIP(c)
	userAgent := c.GetHeader("User-Agent")
	row := SysLoginLog{
		ID:            common.NewID(),
		UserID:        userID,
		Username:      username,
		RealName:      realName,
		LoginType:     loginType,
		Status:        status,
		FailureReason: failureReason,
		IP:            &ip,
		UserAgent:     &userAgent,
		DeviceType:    &deviceType,
	}
	if s.db == nil {
		s.logger.Error("login log write failed", zap.String("reason", "database is not configured"), zap.String("username", username), zap.String("status", status))
		return
	}
	if err := s.db.Create(&row).Error; err != nil {
		s.logger.Error("login log write failed", zap.String("username", username), zap.String("status", status), zap.Error(err))
	}
}
