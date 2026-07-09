package controller

import (
	"drip-admin/backend-go/internal/common"
	"drip-admin/backend-go/internal/modules/system/dto"
	"github.com/gin-gonic/gin"
)

var (
	_ = common.ApiResponse{}
	_ = dto.PrintTemplateSaveRequest{}
)

// PrintTemplates returns print templates.
// @Summary List print templates
// @Tags print-template
// @Produce json
// @Security ApiKeyAuth
// @Param page query int false "Page"
// @Param pageSize query int false "Page size"
// @Param code query string false "Code"
// @Param name query string false "Name"
// @Param status query int false "Status"
// @Success 200 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Router /system/print-template [get]
func (ctl *Controller) PrintTemplates(c *gin.Context) {
	ctl.svc.PrintTemplates(c)
}

// PrintTemplate returns a print template detail.
// @Summary Print template detail
// @Tags print-template
// @Produce json
// @Security ApiKeyAuth
// @Param id path string true "Print template ID"
// @Success 200 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Failure 404 {object} common.ApiResponse
// @Router /system/print-template/{id} [get]
func (ctl *Controller) PrintTemplate(c *gin.Context) {
	ctl.svc.PrintTemplate(c)
}

// CreatePrintTemplate creates a print template.
// @Summary Create print template
// @Tags print-template
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param request body dto.PrintTemplateSaveRequest true "Request"
// @Success 200 {object} common.ApiResponse
// @Failure 400 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Router /system/print-template [post]
func (ctl *Controller) CreatePrintTemplate(c *gin.Context) {
	ctl.svc.CreatePrintTemplate(c)
}

// CopyPrintTemplate copies a print template.
// @Summary Copy print template
// @Tags print-template
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param id path string true "Print template ID"
// @Param request body dto.PrintTemplateCopyRequest true "Request"
// @Success 200 {object} common.ApiResponse
// @Failure 400 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Failure 404 {object} common.ApiResponse
// @Router /system/print-template/{id}/copy [post]
func (ctl *Controller) CopyPrintTemplate(c *gin.Context) {
	ctl.svc.CopyPrintTemplate(c)
}

// UpdatePrintTemplate updates a print template.
// @Summary Update print template
// @Tags print-template
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param id path string true "Print template ID"
// @Param request body dto.PrintTemplateSaveRequest true "Request"
// @Success 200 {object} common.ApiResponse
// @Failure 400 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Failure 404 {object} common.ApiResponse
// @Router /system/print-template/{id} [put]
func (ctl *Controller) UpdatePrintTemplate(c *gin.Context) {
	ctl.svc.UpdatePrintTemplate(c)
}

// DeletePrintTemplate deletes a print template.
// @Summary Delete print template
// @Tags print-template
// @Produce json
// @Security ApiKeyAuth
// @Param id path string true "Print template ID"
// @Success 200 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Failure 404 {object} common.ApiResponse
// @Router /system/print-template/{id} [delete]
func (ctl *Controller) DeletePrintTemplate(c *gin.Context) {
	ctl.svc.DeletePrintTemplate(c)
}

// PrintTemplateStatus updates a print template status.
// @Summary Update print template status
// @Tags print-template
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param id path string true "Print template ID"
// @Param request body dto.StatusUpdateRequest true "Request"
// @Success 200 {object} common.ApiResponse
// @Failure 400 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Router /system/print-template/{id}/status [put]
func (ctl *Controller) PrintTemplateStatus(c *gin.Context) {
	ctl.svc.PrintTemplateStatus(c)
}
