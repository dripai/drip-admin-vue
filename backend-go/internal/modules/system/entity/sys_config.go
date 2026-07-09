package entity

import (
	"drip-admin/backend-go/internal/common"
	"time"
)

type SysConfig struct {
	ID          common.Int64String `gorm:"column:id;primaryKey" json:"id"`
	ConfigName  string             `gorm:"column:config_name" json:"configName"`
	ConfigKey   string             `gorm:"column:config_key" json:"configKey"`
	ConfigValue string             `gorm:"column:config_value" json:"configValue"`
	ValueType   string             `gorm:"column:value_type" json:"valueType"`
	Builtin     int                `gorm:"column:builtin" json:"builtin"`
	Status      int                `gorm:"column:status" json:"status"`
	Remark      *string            `gorm:"column:remark" json:"remark"`
	Deleted     int                `gorm:"column:deleted" json:"deleted"`
	CreatedAt   *time.Time         `gorm:"column:created_at" json:"createdAt"`
	UpdatedAt   *time.Time         `gorm:"column:updated_at" json:"updatedAt"`
}

func (SysConfig) TableName() string { return "sys_config" }
