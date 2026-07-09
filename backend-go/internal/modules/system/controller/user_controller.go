package controller

import (
	"drip-admin/backend-go/internal/common"
	"drip-admin/backend-go/internal/modules/system/dto"
	"github.com/gin-gonic/gin"
)

var (
	_ = common.ApiResponse{}
	_ = dto.UserSaveRequest{}
)

// Users returns a paged user list.
// @Summary List users
// @Tags user
// @Produce json
// @Security ApiKeyAuth
// @Param page query int false "Page"
// @Param pageSize query int false "Page size"
// @Param username query string false "Username"
// @Param realName query string false "Real name"
// @Param phone query string false "Phone"
// @Param status query int false "Status"
// @Param deptId query string false "Department ID"
// @Param roleId query string false "Role ID"
// @Success 200 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Router /system/user [get]
func (ctl *Controller) Users(c *gin.Context) {
	ctl.svc.Users(c)
}

// User returns a user detail.
// @Summary User detail
// @Tags user
// @Produce json
// @Security ApiKeyAuth
// @Param id path string true "User ID"
// @Success 200 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Failure 404 {object} common.ApiResponse
// @Router /system/user/{id} [get]
func (ctl *Controller) User(c *gin.Context) {
	ctl.svc.User(c)
}

// CreateUser creates a user.
// @Summary Create user
// @Tags user
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param request body dto.UserSaveRequest true "Request"
// @Success 200 {object} common.ApiResponse
// @Failure 400 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Router /system/user [post]
func (ctl *Controller) CreateUser(c *gin.Context) {
	ctl.svc.CreateUser(c)
}

// UpdateUser updates a user.
// @Summary Update user
// @Tags user
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param id path string true "User ID"
// @Param request body dto.UserSaveRequest true "Request"
// @Success 200 {object} common.ApiResponse
// @Failure 400 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Failure 404 {object} common.ApiResponse
// @Router /system/user/{id} [put]
func (ctl *Controller) UpdateUser(c *gin.Context) {
	ctl.svc.UpdateUser(c)
}

// DeleteUser deletes a user.
// @Summary Delete user
// @Tags user
// @Produce json
// @Security ApiKeyAuth
// @Param id path string true "User ID"
// @Success 200 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Failure 404 {object} common.ApiResponse
// @Router /system/user/{id} [delete]
func (ctl *Controller) DeleteUser(c *gin.Context) {
	ctl.svc.DeleteUser(c)
}

// UserStatus updates a user's status.
// @Summary Update user status
// @Tags user
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param id path string true "User ID"
// @Param request body dto.StatusUpdateRequest true "Request"
// @Success 200 {object} common.ApiResponse
// @Failure 400 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Router /system/user/{id}/status [put]
func (ctl *Controller) UserStatus(c *gin.Context) {
	ctl.svc.UserStatus(c)
}

// UnlockUser clears a user's login lock.
// @Summary Unlock user
// @Tags user
// @Produce json
// @Security ApiKeyAuth
// @Param id path string true "User ID"
// @Success 200 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Failure 404 {object} common.ApiResponse
// @Router /system/user/{id}/unlock [post]
func (ctl *Controller) UnlockUser(c *gin.Context) {
	ctl.svc.UnlockUser(c)
}

// UserRoles assigns roles to a user.
// @Summary Assign user roles
// @Tags user
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param id path string true "User ID"
// @Param request body dto.RoleAssignRequest true "Request"
// @Success 200 {object} common.ApiResponse
// @Failure 400 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Router /system/user/{id}/role [put]
func (ctl *Controller) UserRoles(c *gin.Context) {
	ctl.svc.UserRoles(c)
}

// ResetPassword resets a user's password.
// @Summary Reset user password
// @Tags user
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param id path string true "User ID"
// @Param request body dto.PasswordResetRequest true "Request"
// @Success 200 {object} common.ApiResponse
// @Failure 400 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Router /system/user/{id}/resetPassword [post]
func (ctl *Controller) ResetPassword(c *gin.Context) {
	ctl.svc.ResetPassword(c)
}
