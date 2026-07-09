package entity

import (
	"drip-admin/backend-go/internal/common"
	"time"
)

type SysPrintTemplate struct {
	ID           common.Int64String `gorm:"column:id;primaryKey" json:"id"`
	Code         string             `gorm:"column:code" json:"code"`
	Name         string             `gorm:"column:name" json:"name"`
	PaperType    string             `gorm:"column:paper_type" json:"paperType"`
	TemplateJSON string             `gorm:"column:template_json" json:"templateJson"`
	Status       int                `gorm:"column:status" json:"status"`
	Deleted      int                `gorm:"column:deleted" json:"deleted"`
	CreatedAt    *time.Time         `gorm:"column:created_at" json:"createdAt"`
	UpdatedAt    *time.Time         `gorm:"column:updated_at" json:"updatedAt"`
}

func (SysPrintTemplate) TableName() string { return "sys_print_template" }
