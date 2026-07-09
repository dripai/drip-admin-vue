package vo

import (
	"time"

	"drip-admin/backend-go/internal/common"
)

type OperationLogVo struct {
	ID            common.Int64String  `json:"id"`
	OperatorID    *common.Int64String `json:"operatorId"`
	Operator      *string             `json:"operator"`
	Module        string              `json:"module"`
	Action        string              `json:"action"`
	Method        string              `json:"method"`
	Path          string              `json:"path"`
	RequestParams *string             `json:"requestParams"`
	Status        string              `json:"status"`
	ErrorMessage  *string             `json:"errorMessage"`
	Duration      common.Int64String  `json:"duration"`
	CreatedAt     *time.Time          `json:"createdAt"`
}
