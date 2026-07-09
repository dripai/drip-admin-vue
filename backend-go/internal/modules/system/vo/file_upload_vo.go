package vo

import (
	"drip-admin/backend-go/internal/common"
)

type FileUploadVo struct {
	FileID   string             `json:"fileId"`
	URL      string             `json:"url"`
	FileName string             `json:"fileName"`
	Size     common.Int64String `json:"size"`
}
