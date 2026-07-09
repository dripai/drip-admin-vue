package entity

import (
	"drip-admin/backend-go/internal/common"
	"time"
)

type SysLoginLog struct {
	ID            common.Int64String  `gorm:"column:id;primaryKey" json:"id"`
	UserID        *common.Int64String `gorm:"column:user_id" json:"userId"`
	Username      string              `gorm:"column:username" json:"username"`
	RealName      *string             `gorm:"column:real_name" json:"realName"`
	LoginType     string              `gorm:"column:login_type" json:"loginType"`
	Status        string              `gorm:"column:status" json:"status"`
	FailureReason *string             `gorm:"column:failure_reason" json:"failureReason"`
	IP            *string             `gorm:"column:ip" json:"ip"`
	UserAgent     *string             `gorm:"column:user_agent" json:"userAgent"`
	DeviceType    *string             `gorm:"column:device_type" json:"deviceType"`
	LoginAt       *time.Time          `gorm:"column:login_at" json:"loginAt"`
}

func (SysLoginLog) TableName() string { return "sys_login_log" }
