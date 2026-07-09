package vo

import (
	"time"

	"drip-admin/backend-go/internal/common"
)

type OperationLogVo struct {
	ID             common.Int64String  `json:"id"`
	OperatorID     *common.Int64String `json:"operatorId"`
	OperatorName   *string             `json:"operatorName"`
	Module         string              `json:"module"`
	Action         string              `json:"action"`
	Method         string              `json:"method"`
	Path           string              `json:"path"`
	RequestParams  *string             `json:"requestParams"`
	ResponseStatus string              `json:"responseStatus"`
	ErrorMessage   *string             `json:"errorMessage"`
	CostMs         common.Int64String  `json:"costMs"`
	CreatedAt      *time.Time          `json:"createdAt"`
}
