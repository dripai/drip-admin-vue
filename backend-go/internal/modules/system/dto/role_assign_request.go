package dto

import (
	"drip-admin/backend-go/internal/common"
)

type RoleAssignRequest struct {
	RoleIDs []common.Int64String `json:"roleIds"`
}
