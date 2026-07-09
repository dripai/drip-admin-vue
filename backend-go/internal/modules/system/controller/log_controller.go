package controller

import (
	"drip-admin/backend-go/internal/common"
	"github.com/gin-gonic/gin"
)

var _ = common.ApiResponse{}

// LoginLogs returns login logs.
// @Summary List login logs
// @Tags login-log
// @Produce json
// @Security ApiKeyAuth
// @Param page query int false "Page"
// @Param pageSize query int false "Page size"
// @Param username query string false "Username"
// @Param status query string false "Status"
// @Param loginType query string false "Login type"
// @Param deviceType query string false "Device type"
// @Param ip query string false "IP"
// @Success 200 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Router /system/loginLog [get]
func (ctl *Controller) LoginLogs(c *gin.Context) {
	ctl.svc.LoginLogs(c)
}

// LoginLog returns a login log detail.
// @Summary Login log detail
// @Tags login-log
// @Produce json
// @Security ApiKeyAuth
// @Param id path string true "Login log ID"
// @Success 200 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Failure 404 {object} common.ApiResponse
// @Router /system/loginLog/{id} [get]
func (ctl *Controller) LoginLog(c *gin.Context) {
	ctl.svc.LoginLog(c)
}

// OperationLogs returns operation logs.
// @Summary List operation logs
// @Tags operation-log
// @Produce json
// @Security ApiKeyAuth
// @Param page query int false "Page"
// @Param pageSize query int false "Page size"
// @Param operator query string false "Operator"
// @Param module query string false "Module"
// @Param action query string false "Action"
// @Param status query string false "Status"
// @Param path query string false "Path"
// @Success 200 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Router /system/operationLog [get]
func (ctl *Controller) OperationLogs(c *gin.Context) {
	ctl.svc.OperationLogs(c)
}

// OperationLog returns an operation log detail.
// @Summary Operation log detail
// @Tags operation-log
// @Produce json
// @Security ApiKeyAuth
// @Param id path string true "Operation log ID"
// @Success 200 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Failure 404 {object} common.ApiResponse
// @Router /system/operationLog/{id} [get]
func (ctl *Controller) OperationLog(c *gin.Context) {
	ctl.svc.OperationLog(c)
}
