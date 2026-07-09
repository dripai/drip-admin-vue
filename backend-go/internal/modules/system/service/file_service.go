package service

import (
	"path/filepath"
	"strings"
	"time"

	"drip-admin/backend-go/internal/common"
	"github.com/gin-gonic/gin"
)

func (s *Server) UploadFile(c *gin.Context) {
	file, err := c.FormFile("file")
	if err != nil || file.Size == 0 {
		common.HandleError(c, common.NewBusinessError(400000, "file must not be empty"))
		return
	}
	maxSize, err := s.requiredLongConfig("upload.maxSizeBytes")
	if err != nil {
		common.HandleError(c, err)
		return
	}
	if file.Size > maxSize {
		common.HandleError(c, common.NewBusinessError(400000, "file exceeds max upload size"))
		return
	}
	allowed := strings.Split(s.configOrDefault("upload.allowedExtensions", ""), ",")
	ext := strings.TrimPrefix(strings.ToLower(filepath.Ext(file.Filename)), ".")
	ok := false
	for _, item := range allowed {
		if strings.Trim(strings.ToLower(item), ". ") == ext {
			ok = true
			break
		}
	}
	if !ok {
		common.HandleError(c, common.NewBusinessError(400000, "file extension is not allowed"))
		return
	}
	common.Success(c, FileUploadVo{FileID: "local-" + time.Now().Format("20060102150405"), URL: "", FileName: file.Filename, Size: common.Int64String(file.Size)})
}
