package controller

import "github.com/gin-gonic/gin"

func (ctl *Controller) Roles(c *gin.Context) {
	ctl.svc.Roles(c)
}

func (ctl *Controller) Role(c *gin.Context) {
	ctl.svc.Role(c)
}

func (ctl *Controller) RoleUsers(c *gin.Context) {
	ctl.svc.RoleUsers(c)
}

func (ctl *Controller) RolePermissions(c *gin.Context) {
	ctl.svc.RolePermissions(c)
}

func (ctl *Controller) RoleOptions(c *gin.Context) {
	ctl.svc.RoleOptions(c)
}

func (ctl *Controller) CreateRole(c *gin.Context) {
	ctl.svc.CreateRole(c)
}

func (ctl *Controller) UpdateRole(c *gin.Context) {
	ctl.svc.UpdateRole(c)
}

func (ctl *Controller) DeleteRole(c *gin.Context) {
	ctl.svc.DeleteRole(c)
}

func (ctl *Controller) RoleStatus(c *gin.Context) {
	ctl.svc.RoleStatus(c)
}

func (ctl *Controller) AssignRoleMenus(c *gin.Context) {
	ctl.svc.AssignRoleMenus(c)
}
