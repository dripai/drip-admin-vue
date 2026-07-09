package entity

import (
	"drip-admin/backend-go/internal/common"
	"time"
)

type SysUser struct {
	ID           common.Int64String  `gorm:"column:id;primaryKey" json:"id"`
	Username     string              `gorm:"column:username" json:"username"`
	PasswordHash string              `gorm:"column:password_hash" json:"passwordHash"`
	PasswordSalt string              `gorm:"column:password_salt" json:"passwordSalt"`
	RealName     string              `gorm:"column:real_name" json:"realName"`
	Phone        *string             `gorm:"column:phone" json:"phone"`
	Email        *string             `gorm:"column:email" json:"email"`
	Avatar       *string             `gorm:"column:avatar" json:"avatar"`
	Status       int                 `gorm:"column:status" json:"status"`
	DeptID       *common.Int64String `gorm:"column:dept_id" json:"deptId"`
	Remark       *string             `gorm:"column:remark" json:"remark"`
	LastLoginAt  *time.Time          `gorm:"column:last_login_at" json:"lastLoginAt"`
	Deleted      int                 `gorm:"column:deleted" json:"deleted"`
	CreatedAt    *time.Time          `gorm:"column:created_at" json:"createdAt"`
	UpdatedAt    *time.Time          `gorm:"column:updated_at" json:"updatedAt"`
}

func (SysUser) TableName() string { return "sys_user" }
