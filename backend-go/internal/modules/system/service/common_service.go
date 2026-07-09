package service

import (
	"errors"
	"net/http"
	"strconv"
	"strings"
	"time"

	"drip-admin/backend-go/internal/common"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

func (s *Server) Root(c *gin.Context) {
	s.redirectSwaggerUI(c)
}

func (s *Server) SwaggerUI(c *gin.Context) {
	s.redirectSwaggerUI(c)
}

func (s *Server) redirectSwaggerUI(c *gin.Context) {
	c.Header("Location", "swagger-ui/index.html")
	c.Status(http.StatusFound)
}

func (s *Server) Favicon(c *gin.Context) {
	c.Status(http.StatusNoContent)
}

func (s *Server) Health(c *gin.Context) {
	common.Success(c, HealthVo{
		Status:    "UP",
		Service:   "drip-admin-backend",
		Timestamp: time.Now().UTC().Format(time.RFC3339),
	})
}

func (s *Server) pageResult(db *gorm.DB, page common.PageQuery, out any) (common.Int64String, error) {
	var total int64
	if err := db.Count(&total).Error; err != nil {
		return 0, err
	}
	if err := db.Offset((page.Page - 1) * page.PageSize).Limit(page.PageSize).Find(out).Error; err != nil {
		return 0, err
	}
	return common.Int64String(total), nil
}

func (s *Server) firstByID(out any, id common.Int64String, message string) error {
	err := s.db.Where("id = ?", id.Int64()).First(out).Error
	if errors.Is(err, gorm.ErrRecordNotFound) {
		return common.NotFound(message)
	}
	return err
}

func likeIfPresent(db *gorm.DB, column string, value string) *gorm.DB {
	if strings.TrimSpace(value) == "" {
		return db
	}
	return db.Where(column+" like ?", "%"+strings.TrimSpace(value)+"%")
}

func eqIfPresent[T comparable](db *gorm.DB, column string, value *T) *gorm.DB {
	if value == nil {
		return db
	}
	return db.Where(column+" = ?", *value)
}

func statusRequest(c *gin.Context) (int, error) {
	var request StatusUpdateRequest
	if err := common.BindJSON(c, &request); err != nil {
		return 0, err
	}
	status := request.StatusOrDefault()
	if status < 0 || status > 1 {
		return 0, common.NewBusinessError(400000, "status must be 0 or 1")
	}
	return status, nil
}

func optionalString(value string) *string {
	if value == "" {
		return nil
	}
	return &value
}

func stringOrNil(value string) *string {
	return &value
}

func intOrDefault(value *int, defaultValue int) int {
	if value == nil {
		return defaultValue
	}
	return *value
}

func idOrZero(value *common.Int64String) common.Int64String {
	if value == nil {
		return 0
	}
	return *value
}

func parseOptionalInt(c *gin.Context, name string) (*int, error) {
	value := strings.TrimSpace(c.Query(name))
	if value == "" {
		return nil, nil
	}
	parsed, err := strconv.Atoi(value)
	if err != nil {
		return nil, common.NewBusinessError(400000, name+" is invalid")
	}
	return &parsed, nil
}

func clientIP(c *gin.Context) string {
	forwarded := strings.TrimSpace(c.GetHeader("X-Forwarded-For"))
	if forwarded != "" {
		return strings.TrimSpace(strings.Split(forwarded, ",")[0])
	}
	return c.ClientIP()
}

func successOrError(c *gin.Context, data any, err error) {
	if err != nil {
		common.HandleError(c, err)
		return
	}
	common.Success(c, data)
}
