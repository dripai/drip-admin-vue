package controller

import "github.com/gin-gonic/gin"

func (ctl *Controller) Menus(c *gin.Context) {
	ctl.svc.Menus(c)
}

func (ctl *Controller) CreateMenu(c *gin.Context) {
	ctl.svc.CreateMenu(c)
}

func (ctl *Controller) UpdateMenu(c *gin.Context) {
	ctl.svc.UpdateMenu(c)
}

func (ctl *Controller) DeleteMenu(c *gin.Context) {
	ctl.svc.DeleteMenu(c)
}

func (ctl *Controller) MenuStatus(c *gin.Context) {
	ctl.svc.MenuStatus(c)
}
