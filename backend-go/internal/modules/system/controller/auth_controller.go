package controller

import (
	"drip-admin/backend-go/internal/common"
	"drip-admin/backend-go/internal/modules/system/dto"
	"github.com/gin-gonic/gin"
)

var (
	_ = common.ApiResponse{}
	_ = dto.LoginRequest{}
)

// Login authenticates a user.
// @Summary Login
// @Tags auth
// @Accept json
// @Produce json
// @Param request body dto.LoginRequest true "Request"
// @Success 200 {object} common.ApiResponse
// @Failure 400 {object} common.ApiResponse
// @Router /system/login [post]
func (ctl *Controller) Login(c *gin.Context) {
	ctl.svc.Login(c)
}

// Logout clears the current session.
// @Summary Logout
// @Tags auth
// @Produce json
// @Security ApiKeyAuth
// @Success 200 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Router /system/logout [post]
func (ctl *Controller) Logout(c *gin.Context) {
	ctl.svc.Logout(c)
}

// Me returns the current user profile and permissions.
// @Summary Current user
// @Tags auth
// @Produce json
// @Security ApiKeyAuth
// @Success 200 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Router /system/me [get]
func (ctl *Controller) Me(c *gin.Context) {
	ctl.svc.Me(c)
}

// Password changes the current user's password.
// @Summary Change password
// @Tags auth
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param request body dto.PasswordRequest true "Request"
// @Success 200 {object} common.ApiResponse
// @Failure 400 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Router /system/password [put]
func (ctl *Controller) Password(c *gin.Context) {
	ctl.svc.Password(c)
}

// Profile updates the current user's profile.
// @Summary Update profile
// @Tags auth
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param request body dto.ProfileUpdateRequest true "Request"
// @Success 200 {object} common.ApiResponse
// @Failure 400 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Router /system/profile [put]
func (ctl *Controller) Profile(c *gin.Context) {
	ctl.svc.Profile(c)
}
