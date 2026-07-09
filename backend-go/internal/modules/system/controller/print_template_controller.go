package controller

import "github.com/gin-gonic/gin"

func (ctl *Controller) PrintTemplates(c *gin.Context) {
	ctl.svc.PrintTemplates(c)
}

func (ctl *Controller) PrintTemplate(c *gin.Context) {
	ctl.svc.PrintTemplate(c)
}

func (ctl *Controller) CreatePrintTemplate(c *gin.Context) {
	ctl.svc.CreatePrintTemplate(c)
}

func (ctl *Controller) CopyPrintTemplate(c *gin.Context) {
	ctl.svc.CopyPrintTemplate(c)
}

func (ctl *Controller) UpdatePrintTemplate(c *gin.Context) {
	ctl.svc.UpdatePrintTemplate(c)
}

func (ctl *Controller) DeletePrintTemplate(c *gin.Context) {
	ctl.svc.DeletePrintTemplate(c)
}

func (ctl *Controller) PrintTemplateStatus(c *gin.Context) {
	ctl.svc.PrintTemplateStatus(c)
}
