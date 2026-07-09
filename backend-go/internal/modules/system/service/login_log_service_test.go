package service

import (
	"net/http"
	"net/http/httptest"
	"testing"

	"drip-admin/backend-go/internal/common"
	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/require"
)

func TestNewLoginLogRowSetsLoginAt(t *testing.T) {
	gin.SetMode(gin.TestMode)
	request := httptest.NewRequest(http.MethodPost, "/api/system/login", nil)
	request.Header.Set("User-Agent", "test-agent")
	recorder := httptest.NewRecorder()
	context, _ := gin.CreateTestContext(recorder)
	context.Request = request

	realName := "Admin"
	row := newLoginLogRow(context, common.IDPtr(common.Int64String(1)), "admin", &realName, "LOGIN", "SUCCESS", "", "pc")

	require.NotNil(t, row.LoginAt)
	require.Equal(t, "admin", row.Username)
	require.Equal(t, "LOGIN", row.LoginType)
	require.Equal(t, "SUCCESS", row.Status)
	require.Nil(t, row.FailureReason)
	require.NotNil(t, row.UserAgent)
	require.Equal(t, "test-agent", *row.UserAgent)
}
