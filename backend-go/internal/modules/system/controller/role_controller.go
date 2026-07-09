package controller

import (
	"drip-admin/backend-go/internal/common"
	"drip-admin/backend-go/internal/modules/system/dto"
	"github.com/gin-gonic/gin"
)

var (
	_ = common.ApiResponse{}
	_ = dto.RoleSaveRequest{}
)

// Roles returns a paged role list.
// @Summary List roles
// @Tags role
// @Produce json
// @Security ApiKeyAuth
// @Param page query int false "Page"
// @Param pageSize query int false "Page size"
// @Param roleName query string false "Role name"
// @Param roleCode query string false "Role code"
// @Param status query int false "Status"
// @Param createdAt query string false "Created time"
// @Success 200 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Router /system/role [get]
func (ctl *Controller) Roles(c *gin.Context) {
	ctl.svc.Roles(c)
}

// Role returns a role detail.
// @Summary Role detail
// @Tags role
// @Produce json
// @Security ApiKeyAuth
// @Param id path string true "Role ID"
// @Success 200 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Failure 404 {object} common.ApiResponse
// @Router /system/role/{id} [get]
func (ctl *Controller) Role(c *gin.Context) {
	ctl.svc.Role(c)
}

// RoleUsers returns users assigned to a role.
// @Summary Role users
// @Tags role
// @Produce json
// @Security ApiKeyAuth
// @Param id path string true "Role ID"
// @Param page query int false "Page"
// @Param pageSize query int false "Page size"
// @Success 200 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Router /system/role/{id}/user [get]
func (ctl *Controller) RoleUsers(c *gin.Context) {
	ctl.svc.RoleUsers(c)
}

// RolePermissions returns role menu permissions.
// @Summary Role permissions
// @Tags role
// @Produce json
// @Security ApiKeyAuth
// @Param id path string true "Role ID"
// @Success 200 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Router /system/role/{id}/permission [get]
func (ctl *Controller) RolePermissions(c *gin.Context) {
	ctl.svc.RolePermissions(c)
}

// RoleOptions returns role options.
// @Summary Role options
// @Tags role
// @Produce json
// @Security ApiKeyAuth
// @Success 200 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Router /system/role/option [get]
func (ctl *Controller) RoleOptions(c *gin.Context) {
	ctl.svc.RoleOptions(c)
}

// CreateRole creates a role.
// @Summary Create role
// @Tags role
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param request body dto.RoleSaveRequest true "Request"
// @Success 200 {object} common.ApiResponse
// @Failure 400 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Router /system/role [post]
func (ctl *Controller) CreateRole(c *gin.Context) {
	ctl.svc.CreateRole(c)
}

// UpdateRole updates a role.
// @Summary Update role
// @Tags role
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param id path string true "Role ID"
// @Param request body dto.RoleSaveRequest true "Request"
// @Success 200 {object} common.ApiResponse
// @Failure 400 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Failure 404 {object} common.ApiResponse
// @Router /system/role/{id} [put]
func (ctl *Controller) UpdateRole(c *gin.Context) {
	ctl.svc.UpdateRole(c)
}

// DeleteRole deletes a role.
// @Summary Delete role
// @Tags role
// @Produce json
// @Security ApiKeyAuth
// @Param id path string true "Role ID"
// @Success 200 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Failure 404 {object} common.ApiResponse
// @Failure 409 {object} common.ApiResponse
// @Router /system/role/{id} [delete]
func (ctl *Controller) DeleteRole(c *gin.Context) {
	ctl.svc.DeleteRole(c)
}

// RoleStatus updates a role status.
// @Summary Update role status
// @Tags role
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param id path string true "Role ID"
// @Param request body dto.StatusUpdateRequest true "Request"
// @Success 200 {object} common.ApiResponse
// @Failure 400 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Router /system/role/{id}/status [put]
func (ctl *Controller) RoleStatus(c *gin.Context) {
	ctl.svc.RoleStatus(c)
}

// AssignRoleMenus assigns menus to a role.
// @Summary Assign role menus
// @Tags role
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param id path string true "Role ID"
// @Param request body dto.MenuAssignRequest true "Request"
// @Success 200 {object} common.ApiResponse
// @Failure 400 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Router /system/role/{id}/permission [put]
func (ctl *Controller) AssignRoleMenus(c *gin.Context) {
	ctl.svc.AssignRoleMenus(c)
}
