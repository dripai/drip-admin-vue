package service

import (
	"context"
	"encoding/json"
	"strconv"
	"strings"
	"time"

	"drip-admin/backend-go/internal/common"
	"drip-admin/backend-go/internal/config"
	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"github.com/redis/go-redis/v9"
	"go.uber.org/zap"
	"gorm.io/gorm"
)

const (
	sessionKeyPrefix      = "drip:online:"
	loginAttemptKeyPrefix = "drip:login:fail:"
)

type Server struct {
	cfg    config.Config
	db     *gorm.DB
	redis  *redis.Client
	logger *zap.Logger
}

type SessionData struct {
	TokenID      string             `json:"tokenId"`
	UserID       common.Int64String `json:"userId"`
	Username     string             `json:"username"`
	RealName     string             `json:"realName"`
	DeviceType   string             `json:"deviceType"`
	IP           string             `json:"ip"`
	UserAgent    string             `json:"userAgent"`
	LoginAt      string             `json:"loginAt"`
	LastActiveAt string             `json:"lastActiveAt"`
	ExpireAt     string             `json:"expireAt"`
}

func NewServer(cfg config.Config, db *gorm.DB, redis *redis.Client, logger *zap.Logger) *Server {
	return &Server{cfg: cfg, db: db, redis: redis, logger: logger}
}

func (s *Server) AuthMiddleware() gin.HandlerFunc {
	return func(c *gin.Context) {
		token := strings.TrimSpace(c.GetHeader(s.cfg.Token.Name))
		if token == "" {
			common.Fail(c, 401, 401000, "未登录或 token 失效")
			c.Abort()
			return
		}
		session, err := s.sessionByToken(c.Request.Context(), token)
		if err != nil {
			common.HandleError(c, err)
			c.Abort()
			return
		}
		if session.TokenID == "" {
			common.Fail(c, 401, 401000, "未登录或 token 失效")
			c.Abort()
			return
		}
		if err := s.touchSession(c.Request.Context(), session); err != nil {
			common.HandleError(c, err)
			c.Abort()
			return
		}
		c.Set("session", session)
		c.Next()
	}
}

func (s *Server) RequirePermission(permission string) gin.HandlerFunc {
	return func(c *gin.Context) {
		session, ok := currentSession(c)
		if !ok {
			common.Fail(c, 401, 401000, "未登录或 token 失效")
			c.Abort()
			return
		}
		permissions, err := s.permissionCodes(session.UserID)
		if err != nil {
			common.HandleError(c, err)
			c.Abort()
			return
		}
		for _, item := range permissions {
			if item == permission {
				c.Next()
				return
			}
		}
		common.Fail(c, 403, 403000, "无权限")
		c.Abort()
	}
}

func currentSession(c *gin.Context) (SessionData, bool) {
	value, ok := c.Get("session")
	if !ok {
		return SessionData{}, false
	}
	session, ok := value.(SessionData)
	return session, ok
}

func (s *Server) sessionByToken(ctx context.Context, token string) (SessionData, error) {
	iter := s.redis.Scan(ctx, 0, sessionKeyPrefix+"*", 100).Iterator()
	for iter.Next(ctx) {
		value, err := s.redis.Get(ctx, iter.Val()).Result()
		if err != nil {
			continue
		}
		var session SessionData
		if err := json.Unmarshal([]byte(value), &session); err != nil {
			_ = s.redis.Del(ctx, iter.Val()).Err()
			continue
		}
		if session.TokenID == token {
			return session, nil
		}
	}
	if err := iter.Err(); err != nil {
		return SessionData{}, common.NewBusinessError(500000, "failed to read online session")
	}
	return SessionData{}, nil
}

func (s *Server) writeSession(ctx context.Context, session SessionData) error {
	payload, err := json.Marshal(session)
	if err != nil {
		return common.NewBusinessError(500000, "failed to write online session")
	}
	key := onlineSessionKey(session.UserID, session.DeviceType)
	if err := s.redis.Set(ctx, key, payload, time.Duration(s.cfg.Token.ActiveTimeoutSeconds)*time.Second).Err(); err != nil {
		return common.NewBusinessError(500000, "failed to write online session")
	}
	return nil
}

func (s *Server) touchSession(ctx context.Context, session SessionData) error {
	now := time.Now()
	session.LastActiveAt = now.Format(time.RFC3339)
	session.ExpireAt = now.Add(time.Duration(s.cfg.Token.ActiveTimeoutSeconds) * time.Second).Format(time.RFC3339)
	return s.writeSession(ctx, session)
}

func (s *Server) removeSession(ctx context.Context, token string) error {
	session, err := s.sessionByToken(ctx, token)
	if err != nil {
		return err
	}
	if session.TokenID == "" {
		return nil
	}
	return s.redis.Del(ctx, onlineSessionKey(session.UserID, session.DeviceType)).Err()
}

func onlineSessionKey(userID common.Int64String, deviceType string) string {
	return sessionKeyPrefix + userIDString(userID) + ":" + normalizeDeviceType(deviceType)
}

func userIDString(userID common.Int64String) string {
	return strconv.FormatInt(userID.Int64(), 10)
}

func normalizeDeviceType(deviceType string) string {
	normalized := strings.ToLower(strings.TrimSpace(deviceType))
	switch normalized {
	case "web", "desktop", "windows", "mac", "linux":
		return "pc"
	case "phone":
		return "mobile"
	case "pad", "ipad":
		return "tablet"
	case "pc", "mobile", "tablet":
		return normalized
	default:
		return "unknown"
	}
}

func newToken() string {
	return uuid.NewString()
}

func idParam(c *gin.Context) (common.Int64String, error) {
	var id common.Int64String
	if err := id.UnmarshalJSON([]byte(c.Param("id"))); err != nil {
		return 0, common.NewBusinessError(400000, "id is invalid")
	}
	return id, nil
}
