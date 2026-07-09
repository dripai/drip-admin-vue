package vo

import (
	"drip-admin/backend-go/internal/common"
)

type MenuTreeVo struct {
	ID             common.Int64String `json:"id"`
	ParentID       common.Int64String `json:"parentId"`
	Name           string             `json:"name"`
	Type           string             `json:"type"`
	Path           *string            `json:"path"`
	Component      *string            `json:"component"`
	PermissionCode *string            `json:"permissionCode"`
	Icon           *string            `json:"icon"`
	Sort           int                `json:"sort"`
	Visible        int                `json:"visible"`
	Status         int                `json:"status"`
	Children       []MenuTreeVo       `json:"children"`
}
