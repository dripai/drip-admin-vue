package service

import (
	"io"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"
	"unicode/utf8"

	"drip-admin/backend-go/internal/config"
	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/require"
)

func TestOperationLogDefinitionForJavaAnnotatedRoutes(t *testing.T) {
	definition, ok := operationLogDefinitionFor(http.MethodPost, "/api/system/user")
	require.True(t, ok)
	require.Equal(t, "用户管理", definition.Module)
	require.Equal(t, "新增用户", definition.Action)

	definition, ok = operationLogDefinitionFor(http.MethodPut, "/api/system/profile")
	require.True(t, ok)
	require.Equal(t, "个人中心", definition.Module)
	require.Equal(t, "编辑资料", definition.Action)

	_, ok = operationLogDefinitionFor(http.MethodPost, "/api/system/dict/cache/refresh")
	require.False(t, ok)
}

func TestOperationRequestParamsMasksSensitiveValues(t *testing.T) {
	gin.SetMode(gin.TestMode)
	request := httptest.NewRequest(http.MethodPost, "/api/system/user/1/resetPassword?token=abc", strings.NewReader(`{"password":"Admin@123456","profile":{"secret":"value"},"username":"demo"}`))
	request.Header.Set("Content-Type", "application/json")
	context, _ := gin.CreateTestContext(httptest.NewRecorder())
	context.Request = request
	context.Params = gin.Params{{Key: "id", Value: "1"}}

	params := NewServer(configForTest(), nil, nil, nil).operationRequestParams(context)
	require.NotNil(t, params)
	require.Contains(t, *params, `"password":"******"`)
	require.Contains(t, *params, `"secret":"******"`)
	require.Contains(t, *params, `"token":["******"]`)
	require.Contains(t, *params, `"username":"demo"`)

	restored, err := io.ReadAll(context.Request.Body)
	require.NoError(t, err)
	require.JSONEq(t, `{"password":"Admin@123456","profile":{"secret":"value"},"username":"demo"}`, string(restored))
}

func TestGinFullPathIsAvailableBeforeNext(t *testing.T) {
	gin.SetMode(gin.TestMode)
	var fullPath string
	router := gin.New()
	router.Use(func(c *gin.Context) {
		fullPath = c.FullPath()
		c.Next()
	})
	router.PUT("/api/system/user/:id/status", func(c *gin.Context) {
		c.Status(http.StatusNoContent)
	})

	recorder := httptest.NewRecorder()
	router.ServeHTTP(recorder, httptest.NewRequest(http.MethodPut, "/api/system/user/1/status", nil))

	require.Equal(t, http.StatusNoContent, recorder.Code)
	require.Equal(t, "/api/system/user/:id/status", fullPath)
}

func TestLimitOperationLogTextKeepsUTF8Boundary(t *testing.T) {
	value := strings.Repeat("a", operationLogTextLimit-1) + "中文"
	limited := limitOperationLogText(value)

	require.LessOrEqual(t, len(limited), operationLogTextLimit)
	require.True(t, utf8.ValidString(limited))
}

func configForTest() config.Config {
	return config.Config{}
}
