package controller

import (
	"drip-admin/backend-go/internal/common"
	"drip-admin/backend-go/internal/modules/system/dto"
	"github.com/gin-gonic/gin"
)

var (
	_ = common.ApiResponse{}
	_ = dto.ConfigSaveRequest{}
)

// Configs returns a paged config list.
// @Summary List configs
// @Tags config
// @Produce json
// @Security ApiKeyAuth
// @Param page query int false "Page"
// @Param pageSize query int false "Page size"
// @Param configName query string false "Config name"
// @Param configKey query string false "Config key"
// @Param status query int false "Status"
// @Success 200 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Router /system/config [get]
func (ctl *Controller) Configs(c *gin.Context) {
	ctl.svc.Configs(c)
}

// PublicConfig returns enabled public system configs.
// @Summary Public configs
// @Tags config
// @Produce json
// @Success 200 {object} common.ApiResponse
// @Failure 500 {object} common.ApiResponse
// @Router /system/publicConfig [get]
func (ctl *Controller) PublicConfig(c *gin.Context) {
	ctl.svc.PublicConfig(c)
}

// CreateConfig creates a config.
// @Summary Create config
// @Tags config
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param request body dto.ConfigSaveRequest true "Request"
// @Success 200 {object} common.ApiResponse
// @Failure 400 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Router /system/config [post]
func (ctl *Controller) CreateConfig(c *gin.Context) {
	ctl.svc.CreateConfig(c)
}

// UpdateConfig updates a config.
// @Summary Update config
// @Tags config
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param id path string true "Config ID"
// @Param request body dto.ConfigSaveRequest true "Request"
// @Success 200 {object} common.ApiResponse
// @Failure 400 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Failure 404 {object} common.ApiResponse
// @Router /system/config/{id} [put]
func (ctl *Controller) UpdateConfig(c *gin.Context) {
	ctl.svc.UpdateConfig(c)
}

// DeleteConfig deletes a config.
// @Summary Delete config
// @Tags config
// @Produce json
// @Security ApiKeyAuth
// @Param id path string true "Config ID"
// @Success 200 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Failure 404 {object} common.ApiResponse
// @Router /system/config/{id} [delete]
func (ctl *Controller) DeleteConfig(c *gin.Context) {
	ctl.svc.DeleteConfig(c)
}

// ConfigStatus updates a config status.
// @Summary Update config status
// @Tags config
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param id path string true "Config ID"
// @Param request body dto.StatusUpdateRequest true "Request"
// @Success 200 {object} common.ApiResponse
// @Failure 400 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Router /system/config/{id}/status [put]
func (ctl *Controller) ConfigStatus(c *gin.Context) {
	ctl.svc.ConfigStatus(c)
}
