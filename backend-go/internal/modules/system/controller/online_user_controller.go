package controller

import "github.com/gin-gonic/gin"

func (ctl *Controller) OnlineUsers(c *gin.Context) {
	ctl.svc.OnlineUsers(c)
}

func (ctl *Controller) OnlineUser(c *gin.Context) {
	ctl.svc.OnlineUser(c)
}

func (ctl *Controller) KickoutOnlineUser(c *gin.Context) {
	ctl.svc.KickoutOnlineUser(c)
}
