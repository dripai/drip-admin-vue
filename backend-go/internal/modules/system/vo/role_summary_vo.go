package vo

import (
	"drip-admin/backend-go/internal/common"
)

type RoleSummaryVo struct {
	ID       common.Int64String `json:"id"`
	RoleName string             `json:"roleName"`
	RoleCode string             `json:"roleCode"`
}
