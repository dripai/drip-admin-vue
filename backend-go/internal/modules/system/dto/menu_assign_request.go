package dto

import (
	"drip-admin/backend-go/internal/common"
)

type MenuAssignRequest struct {
	MenuIDs []common.Int64String `json:"menuIds"`
}
