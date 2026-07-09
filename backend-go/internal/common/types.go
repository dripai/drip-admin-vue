package common

import (
	"encoding/json"
	"fmt"
	"strconv"
	"strings"

	"github.com/gin-gonic/gin"
)

type Int64String int64

func (v Int64String) Int64() int64 {
	return int64(v)
}

func (v Int64String) MarshalJSON() ([]byte, error) {
	return json.Marshal(strconv.FormatInt(int64(v), 10))
}

func (v *Int64String) UnmarshalJSON(data []byte) error {
	var text string
	if err := json.Unmarshal(data, &text); err == nil {
		if strings.TrimSpace(text) == "" {
			*v = 0
			return nil
		}
		parsed, err := strconv.ParseInt(text, 10, 64)
		if err != nil {
			return err
		}
		*v = Int64String(parsed)
		return nil
	}
	var number int64
	if err := json.Unmarshal(data, &number); err != nil {
		return err
	}
	*v = Int64String(number)
	return nil
}

func IDPtr(id Int64String) *Int64String {
	return &id
}

type ApiResponse struct {
	Code    int    `json:"code"`
	Message string `json:"message"`
	Data    any    `json:"data"`
}

type PageResult[T any] struct {
	List     []T         `json:"list"`
	Total    Int64String `json:"total"`
	Page     int         `json:"page"`
	PageSize int         `json:"pageSize"`
}

type PageQuery struct {
	Page     int
	PageSize int
}

func Success(c *gin.Context, data any) {
	c.JSON(200, ApiResponse{Code: 0, Message: "success", Data: data})
}

func Fail(c *gin.Context, status int, code int, message string) {
	c.JSON(status, ApiResponse{Code: code, Message: message, Data: nil})
}

func ParsePage(c *gin.Context) (PageQuery, error) {
	page, err := parsePositiveInt(c.Query("page"), 1)
	if err != nil {
		return PageQuery{}, NewBusinessError(400000, "page must be >= 1")
	}
	pageSize, err := parsePositiveInt(c.Query("pageSize"), 10)
	if err != nil {
		return PageQuery{}, NewBusinessError(400000, "pageSize must be >= 1")
	}
	if pageSize > 100 {
		return PageQuery{}, NewBusinessError(400000, "pageSize must be <= 100")
	}
	return PageQuery{Page: page, PageSize: pageSize}, nil
}

func BindJSON(c *gin.Context, out any) error {
	if err := c.ShouldBindJSON(out); err != nil {
		return NewBusinessError(400000, "请求体 JSON 格式错误")
	}
	return nil
}

func RequiredString(value string, field string) error {
	if strings.TrimSpace(value) == "" {
		return NewBusinessError(400000, fmt.Sprintf("%s is required", field))
	}
	return nil
}

func OptionalStringPtr(value string) *string {
	if value == "" {
		empty := ""
		return &empty
	}
	return &value
}

func StringValue(value *string) string {
	if value == nil {
		return ""
	}
	return *value
}

func parsePositiveInt(value string, defaultValue int) (int, error) {
	if strings.TrimSpace(value) == "" {
		return defaultValue, nil
	}
	parsed, err := strconv.Atoi(value)
	if err != nil || parsed < 1 {
		return 0, fmt.Errorf("invalid positive int")
	}
	return parsed, nil
}
