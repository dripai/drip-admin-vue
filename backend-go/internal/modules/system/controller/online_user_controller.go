package controller

import (
	"drip-admin/backend-go/internal/common"
	"github.com/gin-gonic/gin"
)

var _ = common.ApiResponse{}

// OnlineUsers returns online sessions.
// @Summary List online users
// @Tags online-user
// @Produce json
// @Security ApiKeyAuth
// @Param page query int false "Page"
// @Param pageSize query int false "Page size"
// @Param username query string false "Username"
// @Param ip query string false "IP"
// @Param deviceType query string false "Device type"
// @Success 200 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Router /system/onlineUser [get]
func (ctl *Controller) OnlineUsers(c *gin.Context) {
	ctl.svc.OnlineUsers(c)
}

// OnlineUser returns an online session detail.
// @Summary Online user detail
// @Tags online-user
// @Produce json
// @Security ApiKeyAuth
// @Param tokenId path string true "Token ID"
// @Success 200 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Failure 404 {object} common.ApiResponse
// @Router /system/onlineUser/{tokenId} [get]
func (ctl *Controller) OnlineUser(c *gin.Context) {
	ctl.svc.OnlineUser(c)
}

// KickoutOnlineUser kicks out an online session.
// @Summary Kick out online user
// @Tags online-user
// @Produce json
// @Security ApiKeyAuth
// @Param tokenId path string true "Token ID"
// @Success 200 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Failure 404 {object} common.ApiResponse
// @Router /system/onlineUser/{tokenId}/kickout [post]
func (ctl *Controller) KickoutOnlineUser(c *gin.Context) {
	ctl.svc.KickoutOnlineUser(c)
}
