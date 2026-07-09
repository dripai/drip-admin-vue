package controller

import (
	"drip-admin/backend-go/internal/common"
	"drip-admin/backend-go/internal/modules/system/dto"
	"github.com/gin-gonic/gin"
)

var (
	_ = common.ApiResponse{}
	_ = dto.DeptSaveRequest{}
)

// Depts returns the department tree.
// @Summary List departments
// @Tags dept
// @Produce json
// @Security ApiKeyAuth
// @Success 200 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Router /system/dept [get]
func (ctl *Controller) Depts(c *gin.Context) {
	ctl.svc.Depts(c)
}

// Dept returns a department detail.
// @Summary Department detail
// @Tags dept
// @Produce json
// @Security ApiKeyAuth
// @Param id path string true "Department ID"
// @Success 200 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Failure 404 {object} common.ApiResponse
// @Router /system/dept/{id} [get]
func (ctl *Controller) Dept(c *gin.Context) {
	ctl.svc.Dept(c)
}

// CreateDept creates a department.
// @Summary Create department
// @Tags dept
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param request body dto.DeptSaveRequest true "Request"
// @Success 200 {object} common.ApiResponse
// @Failure 400 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Router /system/dept [post]
func (ctl *Controller) CreateDept(c *gin.Context) {
	ctl.svc.CreateDept(c)
}

// UpdateDept updates a department.
// @Summary Update department
// @Tags dept
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param id path string true "Department ID"
// @Param request body dto.DeptSaveRequest true "Request"
// @Success 200 {object} common.ApiResponse
// @Failure 400 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Failure 404 {object} common.ApiResponse
// @Router /system/dept/{id} [put]
func (ctl *Controller) UpdateDept(c *gin.Context) {
	ctl.svc.UpdateDept(c)
}

// DeleteDept deletes a department.
// @Summary Delete department
// @Tags dept
// @Produce json
// @Security ApiKeyAuth
// @Param id path string true "Department ID"
// @Success 200 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Failure 409 {object} common.ApiResponse
// @Router /system/dept/{id} [delete]
func (ctl *Controller) DeleteDept(c *gin.Context) {
	ctl.svc.DeleteDept(c)
}

// DeptStatus updates a department status.
// @Summary Update department status
// @Tags dept
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param id path string true "Department ID"
// @Param request body dto.StatusUpdateRequest true "Request"
// @Success 200 {object} common.ApiResponse
// @Failure 400 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Router /system/dept/{id}/status [put]
func (ctl *Controller) DeptStatus(c *gin.Context) {
	ctl.svc.DeptStatus(c)
}
