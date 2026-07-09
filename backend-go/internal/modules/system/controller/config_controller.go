package controller

import "github.com/gin-gonic/gin"

func (ctl *Controller) Configs(c *gin.Context) {
	ctl.svc.Configs(c)
}

func (ctl *Controller) PublicConfig(c *gin.Context) {
	ctl.svc.PublicConfig(c)
}

func (ctl *Controller) CreateConfig(c *gin.Context) {
	ctl.svc.CreateConfig(c)
}

func (ctl *Controller) UpdateConfig(c *gin.Context) {
	ctl.svc.UpdateConfig(c)
}

func (ctl *Controller) DeleteConfig(c *gin.Context) {
	ctl.svc.DeleteConfig(c)
}

func (ctl *Controller) ConfigStatus(c *gin.Context) {
	ctl.svc.ConfigStatus(c)
}
