package entity

import (
	"drip-admin/backend-go/internal/common"
	"time"
)

type SysDictType struct {
	ID        common.Int64String `gorm:"column:id;primaryKey" json:"id"`
	DictName  string             `gorm:"column:dict_name" json:"dictName"`
	DictCode  string             `gorm:"column:dict_code" json:"dictCode"`
	Status    int                `gorm:"column:status" json:"status"`
	Builtin   int                `gorm:"column:builtin" json:"builtin"`
	Remark    *string            `gorm:"column:remark" json:"remark"`
	CreatedAt *time.Time         `gorm:"column:created_at" json:"createdAt"`
	UpdatedAt *time.Time         `gorm:"column:updated_at" json:"updatedAt"`
}

func (SysDictType) TableName() string { return "sys_dict_type" }
