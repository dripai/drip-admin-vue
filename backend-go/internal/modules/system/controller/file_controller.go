package controller

import (
	"drip-admin/backend-go/internal/common"
	"github.com/gin-gonic/gin"
)

var _ = common.ApiResponse{}

// UploadFile uploads a file.
// @Summary Upload file
// @Tags file
// @Accept multipart/form-data
// @Produce json
// @Security ApiKeyAuth
// @Param file formData file true "File"
// @Success 200 {object} common.ApiResponse
// @Failure 400 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Router /system/files [post]
func (ctl *Controller) UploadFile(c *gin.Context) {
	ctl.svc.UploadFile(c)
}
