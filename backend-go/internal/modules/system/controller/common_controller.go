package controller

import "github.com/gin-gonic/gin"

func (ctl *Controller) Root(c *gin.Context) {
	ctl.svc.Root(c)
}

func (ctl *Controller) Favicon(c *gin.Context) {
	ctl.svc.Favicon(c)
}

func (ctl *Controller) Health(c *gin.Context) {
	ctl.svc.Health(c)
}

func (ctl *Controller) OpenAPIDocs(c *gin.Context) {
	ctl.svc.OpenAPIDocs(c)
}
