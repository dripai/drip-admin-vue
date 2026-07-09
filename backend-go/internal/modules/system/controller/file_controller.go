package controller

import "github.com/gin-gonic/gin"

func (ctl *Controller) UploadFile(c *gin.Context) {
	ctl.svc.UploadFile(c)
}
