package controller

import "github.com/gin-gonic/gin"

func (ctl *Controller) LoginLogs(c *gin.Context) {
	ctl.svc.LoginLogs(c)
}

func (ctl *Controller) LoginLog(c *gin.Context) {
	ctl.svc.LoginLog(c)
}

func (ctl *Controller) OperationLogs(c *gin.Context) {
	ctl.svc.OperationLogs(c)
}

func (ctl *Controller) OperationLog(c *gin.Context) {
	ctl.svc.OperationLog(c)
}
