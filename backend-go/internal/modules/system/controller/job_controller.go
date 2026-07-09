package controller

import "github.com/gin-gonic/gin"

func (ctl *Controller) Jobs(c *gin.Context) {
	ctl.svc.Jobs(c)
}

func (ctl *Controller) Job(c *gin.Context) {
	ctl.svc.Job(c)
}

func (ctl *Controller) JobScripts(c *gin.Context) {
	ctl.svc.JobScripts(c)
}

func (ctl *Controller) CreateJob(c *gin.Context) {
	ctl.svc.CreateJob(c)
}

func (ctl *Controller) UpdateJob(c *gin.Context) {
	ctl.svc.UpdateJob(c)
}

func (ctl *Controller) DeleteJob(c *gin.Context) {
	ctl.svc.DeleteJob(c)
}

func (ctl *Controller) JobStatus(c *gin.Context) {
	ctl.svc.JobStatus(c)
}

func (ctl *Controller) RunJob(c *gin.Context) {
	ctl.svc.RunJob(c)
}

func (ctl *Controller) JobLogs(c *gin.Context) {
	ctl.svc.JobLogs(c)
}

func (ctl *Controller) JobRunLogs(c *gin.Context) {
	ctl.svc.JobRunLogs(c)
}
