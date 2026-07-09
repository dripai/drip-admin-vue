package controller

import (
	"drip-admin/backend-go/internal/common"
	"drip-admin/backend-go/internal/modules/system/dto"
	"github.com/gin-gonic/gin"
)

var (
	_ = common.ApiResponse{}
	_ = dto.MenuSaveRequest{}
)

// Menus returns the menu tree.
// @Summary List menus
// @Tags menu
// @Produce json
// @Security ApiKeyAuth
// @Success 200 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Router /system/menu [get]
func (ctl *Controller) Menus(c *gin.Context) {
	ctl.svc.Menus(c)
}

// CreateMenu creates a menu.
// @Summary Create menu
// @Tags menu
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param request body dto.MenuSaveRequest true "Request"
// @Success 200 {object} common.ApiResponse
// @Failure 400 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Router /system/menu [post]
func (ctl *Controller) CreateMenu(c *gin.Context) {
	ctl.svc.CreateMenu(c)
}

// UpdateMenu updates a menu.
// @Summary Update menu
// @Tags menu
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param id path string true "Menu ID"
// @Param request body dto.MenuSaveRequest true "Request"
// @Success 200 {object} common.ApiResponse
// @Failure 400 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Router /system/menu/{id} [put]
func (ctl *Controller) UpdateMenu(c *gin.Context) {
	ctl.svc.UpdateMenu(c)
}

// DeleteMenu deletes a menu.
// @Summary Delete menu
// @Tags menu
// @Produce json
// @Security ApiKeyAuth
// @Param id path string true "Menu ID"
// @Success 200 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Failure 409 {object} common.ApiResponse
// @Router /system/menu/{id} [delete]
func (ctl *Controller) DeleteMenu(c *gin.Context) {
	ctl.svc.DeleteMenu(c)
}

// MenuStatus updates a menu status.
// @Summary Update menu status
// @Tags menu
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param id path string true "Menu ID"
// @Param request body dto.StatusUpdateRequest true "Request"
// @Success 200 {object} common.ApiResponse
// @Failure 400 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Router /system/menu/{id}/status [put]
func (ctl *Controller) MenuStatus(c *gin.Context) {
	ctl.svc.MenuStatus(c)
}
