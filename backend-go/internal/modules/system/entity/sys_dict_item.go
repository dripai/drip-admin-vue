package entity

import (
	"drip-admin/backend-go/internal/common"
	"time"
)

type SysDictItem struct {
	ID         common.Int64String `gorm:"column:id;primaryKey" json:"id"`
	DictTypeID common.Int64String `gorm:"column:dict_type_id" json:"dictTypeId"`
	Label      string             `gorm:"column:label" json:"label"`
	Value      string             `gorm:"column:value" json:"value"`
	IsDefault  int                `gorm:"column:is_default" json:"isDefault"`
	Sort       int                `gorm:"column:sort" json:"sort"`
	Status     int                `gorm:"column:status" json:"status"`
	Builtin    int                `gorm:"column:builtin" json:"builtin"`
	CreatedAt  *time.Time         `gorm:"column:created_at" json:"createdAt"`
	UpdatedAt  *time.Time         `gorm:"column:updated_at" json:"updatedAt"`
}

func (SysDictItem) TableName() string { return "sys_dict_item" }
