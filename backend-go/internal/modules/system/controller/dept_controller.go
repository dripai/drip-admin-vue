package controller

import "github.com/gin-gonic/gin"

func (ctl *Controller) Depts(c *gin.Context) {
	ctl.svc.Depts(c)
}

func (ctl *Controller) Dept(c *gin.Context) {
	ctl.svc.Dept(c)
}

func (ctl *Controller) CreateDept(c *gin.Context) {
	ctl.svc.CreateDept(c)
}

func (ctl *Controller) UpdateDept(c *gin.Context) {
	ctl.svc.UpdateDept(c)
}

func (ctl *Controller) DeleteDept(c *gin.Context) {
	ctl.svc.DeleteDept(c)
}

func (ctl *Controller) DeptStatus(c *gin.Context) {
	ctl.svc.DeptStatus(c)
}
