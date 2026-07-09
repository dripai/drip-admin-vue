package entity

import (
	"drip-admin/backend-go/internal/common"
	"time"
)

type SysOperationLog struct {
	ID             common.Int64String  `gorm:"column:id;primaryKey" json:"id"`
	OperatorID     *common.Int64String `gorm:"column:operator_id" json:"operatorId"`
	OperatorName   *string             `gorm:"column:operator_name" json:"operatorName"`
	Module         string              `gorm:"column:module" json:"module"`
	Action         string              `gorm:"column:action" json:"action"`
	Method         string              `gorm:"column:method" json:"method"`
	Path           string              `gorm:"column:path" json:"path"`
	RequestParams  *string             `gorm:"column:request_params" json:"requestParams"`
	ResponseStatus string              `gorm:"column:response_status" json:"responseStatus"`
	ErrorMessage   *string             `gorm:"column:error_message" json:"errorMessage"`
	CostMs         common.Int64String  `gorm:"column:cost_ms" json:"costMs"`
	CreatedAt      *time.Time          `gorm:"column:created_at" json:"createdAt"`
}

func (SysOperationLog) TableName() string { return "sys_operation_log" }
