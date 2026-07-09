package entity

import (
	"drip-admin/backend-go/internal/common"
	"time"
)

type SysRoleMenu struct {
	ID        common.Int64String `gorm:"column:id;primaryKey" json:"id"`
	RoleID    common.Int64String `gorm:"column:role_id" json:"roleId"`
	MenuID    common.Int64String `gorm:"column:menu_id" json:"menuId"`
	CreatedAt *time.Time         `gorm:"column:created_at" json:"createdAt"`
}

func (SysRoleMenu) TableName() string { return "sys_role_menu" }
