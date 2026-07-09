package service

import (
	"strings"
	"time"

	"drip-admin/backend-go/internal/common"
	"github.com/gin-gonic/gin"
)

func (s *Server) OperationLogMiddleware() gin.HandlerFunc {
	return func(c *gin.Context) {
		started := time.Now()
		c.Next()
		if !shouldWriteOperationLog(c.Request.Method, c.FullPath()) {
			return
		}
		status := "SUCCESS"
		var errorMessage *string
		if len(c.Errors) > 0 || c.Writer.Status() >= 400 {
			status = "FAIL"
			message := c.Errors.String()
			errorMessage = &message
		}
		session, ok := currentSession(c)
		var operatorID *common.Int64String
		var operatorName *string
		if ok {
			operatorID = common.IDPtr(session.UserID)
			name := session.RealName
			if strings.TrimSpace(name) == "" {
				name = session.Username
			}
			operatorName = &name
		}
		row := SysOperationLog{
			ID:             common.NewID(),
			OperatorID:     operatorID,
			OperatorName:   operatorName,
			Module:         "",
			Action:         "",
			Method:         c.Request.Method,
			Path:           c.Request.URL.Path,
			ResponseStatus: status,
			ErrorMessage:   errorMessage,
			CostMs:         common.Int64String(time.Since(started).Milliseconds()),
		}
		_ = s.db.Create(&row).Error
	}
}

func shouldWriteOperationLog(method string, path string) bool {
	if method == "GET" || path == "" {
		return false
	}
	return strings.HasPrefix(path, "/api/system/")
}
