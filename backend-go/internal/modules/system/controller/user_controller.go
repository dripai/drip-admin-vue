package controller

import "github.com/gin-gonic/gin"

func (ctl *Controller) Users(c *gin.Context) {
	ctl.svc.Users(c)
}

func (ctl *Controller) User(c *gin.Context) {
	ctl.svc.User(c)
}

func (ctl *Controller) CreateUser(c *gin.Context) {
	ctl.svc.CreateUser(c)
}

func (ctl *Controller) UpdateUser(c *gin.Context) {
	ctl.svc.UpdateUser(c)
}

func (ctl *Controller) DeleteUser(c *gin.Context) {
	ctl.svc.DeleteUser(c)
}

func (ctl *Controller) UserStatus(c *gin.Context) {
	ctl.svc.UserStatus(c)
}

func (ctl *Controller) UnlockUser(c *gin.Context) {
	ctl.svc.UnlockUser(c)
}

func (ctl *Controller) UserRoles(c *gin.Context) {
	ctl.svc.UserRoles(c)
}

func (ctl *Controller) ResetPassword(c *gin.Context) {
	ctl.svc.ResetPassword(c)
}
