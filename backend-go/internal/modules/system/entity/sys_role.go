package entity

import (
	"drip-admin/backend-go/internal/common"
	"time"
)

type SysRole struct {
	ID        common.Int64String `gorm:"column:id;primaryKey" json:"id"`
	RoleName  string             `gorm:"column:role_name" json:"roleName"`
	RoleCode  string             `gorm:"column:role_code" json:"roleCode"`
	Builtin   int                `gorm:"column:builtin" json:"builtin"`
	Status    int                `gorm:"column:status" json:"status"`
	Remark    *string            `gorm:"column:remark" json:"remark"`
	Deleted   int                `gorm:"column:deleted" json:"deleted"`
	CreatedAt *time.Time         `gorm:"column:created_at" json:"createdAt"`
	UpdatedAt *time.Time         `gorm:"column:updated_at" json:"updatedAt"`
}

func (SysRole) TableName() string { return "sys_role" }
