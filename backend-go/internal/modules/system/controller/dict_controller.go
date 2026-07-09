package controller

import "github.com/gin-gonic/gin"

func (ctl *Controller) DictTypes(c *gin.Context) {
	ctl.svc.DictTypes(c)
}

func (ctl *Controller) CreateDictType(c *gin.Context) {
	ctl.svc.CreateDictType(c)
}

func (ctl *Controller) UpdateDictType(c *gin.Context) {
	ctl.svc.UpdateDictType(c)
}

func (ctl *Controller) DeleteDictType(c *gin.Context) {
	ctl.svc.DeleteDictType(c)
}

func (ctl *Controller) DictItems(c *gin.Context) {
	ctl.svc.DictItems(c)
}

func (ctl *Controller) CreateDictItem(c *gin.Context) {
	ctl.svc.CreateDictItem(c)
}

func (ctl *Controller) UpdateDictItem(c *gin.Context) {
	ctl.svc.UpdateDictItem(c)
}

func (ctl *Controller) DeleteDictItem(c *gin.Context) {
	ctl.svc.DeleteDictItem(c)
}

func (ctl *Controller) DictItemStatus(c *gin.Context) {
	ctl.svc.DictItemStatus(c)
}

func (ctl *Controller) RefreshDictCache(c *gin.Context) {
	ctl.svc.RefreshDictCache(c)
}
