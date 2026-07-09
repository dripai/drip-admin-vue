package vo

import (
	"drip-admin/backend-go/internal/common"
)

type RolePermissionVo struct {
	MenuIDs        []common.Int64String `json:"menuIds"`
	PermissionCode []string             `json:"permissionCodes"`
}
