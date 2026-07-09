package entity

import (
	"drip-admin/backend-go/internal/common"
	"time"
)

type SysUserRole struct {
	ID        common.Int64String `gorm:"column:id;primaryKey" json:"id"`
	UserID    common.Int64String `gorm:"column:user_id" json:"userId"`
	RoleID    common.Int64String `gorm:"column:role_id" json:"roleId"`
	CreatedAt *time.Time         `gorm:"column:created_at" json:"createdAt"`
}

func (SysUserRole) TableName() string { return "sys_user_role" }
