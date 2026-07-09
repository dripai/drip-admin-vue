package entity

import (
	"drip-admin/backend-go/internal/common"
	"time"
)

type SysDept struct {
	ID           common.Int64String  `gorm:"column:id;primaryKey" json:"id"`
	ParentID     common.Int64String  `gorm:"column:parent_id" json:"parentId"`
	DeptName     string              `gorm:"column:dept_name" json:"deptName"`
	DeptCode     string              `gorm:"column:dept_code" json:"deptCode"`
	LeaderUserID *common.Int64String `gorm:"column:leader_user_id" json:"leaderUserId"`
	Sort         int                 `gorm:"column:sort" json:"sort"`
	Status       int                 `gorm:"column:status" json:"status"`
	Deleted      int                 `gorm:"column:deleted" json:"deleted"`
	CreatedAt    *time.Time          `gorm:"column:created_at" json:"createdAt"`
	UpdatedAt    *time.Time          `gorm:"column:updated_at" json:"updatedAt"`
}

func (SysDept) TableName() string { return "sys_dept" }
