package vo

import (
	"drip-admin/backend-go/internal/common"
	"time"
)

type DeptTreeVo struct {
	ID           common.Int64String  `json:"id"`
	ParentID     common.Int64String  `json:"parentId"`
	DeptName     string              `json:"deptName"`
	DeptCode     string              `json:"deptCode"`
	LeaderUserID *common.Int64String `json:"leaderUserId"`
	Sort         int                 `json:"sort"`
	Status       int                 `json:"status"`
	CreatedAt    *time.Time          `json:"createdAt"`
	UpdatedAt    *time.Time          `json:"updatedAt"`
	Children     []DeptTreeVo        `json:"children"`
}
