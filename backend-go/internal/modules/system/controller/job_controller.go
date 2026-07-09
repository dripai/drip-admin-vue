package controller

import (
	"drip-admin/backend-go/internal/common"
	"drip-admin/backend-go/internal/modules/system/dto"
	"github.com/gin-gonic/gin"
)

var (
	_ = common.ApiResponse{}
	_ = dto.JobSaveRequest{}
)

// Jobs returns scheduled jobs.
// @Summary List jobs
// @Tags job
// @Produce json
// @Security ApiKeyAuth
// @Param page query int false "Page"
// @Param pageSize query int false "Page size"
// @Param jobName query string false "Job name"
// @Param remark query string false "Remark"
// @Param status query int false "Status"
// @Param createdAt query string false "Created time"
// @Success 200 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Router /system/job [get]
func (ctl *Controller) Jobs(c *gin.Context) {
	ctl.svc.Jobs(c)
}

// Job returns a scheduled job detail.
// @Summary Job detail
// @Tags job
// @Produce json
// @Security ApiKeyAuth
// @Param id path string true "Job ID"
// @Success 200 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Failure 404 {object} common.ApiResponse
// @Router /system/job/{id} [get]
func (ctl *Controller) Job(c *gin.Context) {
	ctl.svc.Job(c)
}

// JobScripts returns available job scripts.
// @Summary Job scripts
// @Tags job
// @Produce json
// @Security ApiKeyAuth
// @Param executorType query string false "Executor type: python, shell, bat, powershell, ps1"
// @Success 200 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Router /system/job/scripts [get]
func (ctl *Controller) JobScripts(c *gin.Context) {
	ctl.svc.JobScripts(c)
}

// CreateJob creates a scheduled job.
// @Summary Create job
// @Tags job
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param request body dto.JobSaveRequest true "Request"
// @Success 200 {object} common.ApiResponse
// @Failure 400 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Router /system/job [post]
func (ctl *Controller) CreateJob(c *gin.Context) {
	ctl.svc.CreateJob(c)
}

// UpdateJob updates a scheduled job.
// @Summary Update job
// @Tags job
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param id path string true "Job ID"
// @Param request body dto.JobSaveRequest true "Request"
// @Success 200 {object} common.ApiResponse
// @Failure 400 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Failure 404 {object} common.ApiResponse
// @Router /system/job/{id} [put]
func (ctl *Controller) UpdateJob(c *gin.Context) {
	ctl.svc.UpdateJob(c)
}

// DeleteJob deletes a scheduled job.
// @Summary Delete job
// @Tags job
// @Produce json
// @Security ApiKeyAuth
// @Param id path string true "Job ID"
// @Success 200 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Failure 404 {object} common.ApiResponse
// @Router /system/job/{id} [delete]
func (ctl *Controller) DeleteJob(c *gin.Context) {
	ctl.svc.DeleteJob(c)
}

// JobStatus updates a scheduled job status.
// @Summary Update job status
// @Tags job
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param id path string true "Job ID"
// @Param request body dto.StatusUpdateRequest true "Request"
// @Success 200 {object} common.ApiResponse
// @Failure 400 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Router /system/job/{id}/status [put]
func (ctl *Controller) JobStatus(c *gin.Context) {
	ctl.svc.JobStatus(c)
}

// RunJob runs a scheduled job once.
// @Summary Run job
// @Tags job
// @Produce json
// @Security ApiKeyAuth
// @Param id path string true "Job ID"
// @Success 200 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Failure 404 {object} common.ApiResponse
// @Router /system/job/{id}/run [post]
func (ctl *Controller) RunJob(c *gin.Context) {
	ctl.svc.RunJob(c)
}

// JobLogs returns run logs for a scheduled job.
// @Summary Job run logs
// @Tags job
// @Produce json
// @Security ApiKeyAuth
// @Param id path string true "Job ID"
// @Param page query int false "Page"
// @Param pageSize query int false "Page size"
// @Success 200 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Router /system/job/{id}/runLog [get]
func (ctl *Controller) JobLogs(c *gin.Context) {
	ctl.svc.JobLogs(c)
}

// JobRunLogs returns all job run logs.
// @Summary List job run logs
// @Tags job
// @Produce json
// @Security ApiKeyAuth
// @Param page query int false "Page"
// @Param pageSize query int false "Page size"
// @Param jobName query string false "Job name"
// @Param status query string false "Status"
// @Success 200 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Router /system/jobRunLog [get]
func (ctl *Controller) JobRunLogs(c *gin.Context) {
	ctl.svc.JobRunLogs(c)
}
