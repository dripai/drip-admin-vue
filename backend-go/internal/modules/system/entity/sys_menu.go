package entity

import (
	"drip-admin/backend-go/internal/common"
	"time"
)

type SysMenu struct {
	ID             common.Int64String `gorm:"column:id;primaryKey" json:"id"`
	ParentID       common.Int64String `gorm:"column:parent_id" json:"parentId"`
	Name           string             `gorm:"column:name" json:"name"`
	Type           string             `gorm:"column:type" json:"type"`
	Path           *string            `gorm:"column:path" json:"path"`
	Component      *string            `gorm:"column:component" json:"component"`
	PermissionCode *string            `gorm:"column:permission_code" json:"permissionCode"`
	Icon           *string            `gorm:"column:icon" json:"icon"`
	Sort           int                `gorm:"column:sort" json:"sort"`
	Visible        int                `gorm:"column:visible" json:"visible"`
	Status         int                `gorm:"column:status" json:"status"`
	Deleted        int                `gorm:"column:deleted" json:"deleted"`
	CreatedAt      *time.Time         `gorm:"column:created_at" json:"createdAt"`
	UpdatedAt      *time.Time         `gorm:"column:updated_at" json:"updatedAt"`
}

func (SysMenu) TableName() string { return "sys_menu" }
