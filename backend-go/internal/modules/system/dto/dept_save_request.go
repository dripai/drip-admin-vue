package dto

import (
	"drip-admin/backend-go/internal/common"
)

type DeptSaveRequest struct {
	ParentID     *common.Int64String `json:"parentId"`
	DeptName     string              `json:"deptName"`
	DeptCode     string              `json:"deptCode"`
	LeaderUserID *common.Int64String `json:"leaderUserId"`
	Sort         *int                `json:"sort"`
	Status       *int                `json:"status"`
}
