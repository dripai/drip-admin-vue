package controller

import "github.com/gin-gonic/gin"

func (ctl *Controller) Login(c *gin.Context) {
	ctl.svc.Login(c)
}

func (ctl *Controller) Logout(c *gin.Context) {
	ctl.svc.Logout(c)
}

func (ctl *Controller) Me(c *gin.Context) {
	ctl.svc.Me(c)
}

func (ctl *Controller) Password(c *gin.Context) {
	ctl.svc.Password(c)
}

func (ctl *Controller) Profile(c *gin.Context) {
	ctl.svc.Profile(c)
}
