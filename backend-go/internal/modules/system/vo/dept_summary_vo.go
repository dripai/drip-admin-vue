package vo

import (
	"drip-admin/backend-go/internal/common"
)

type DeptSummaryVo struct {
	ID       common.Int64String `json:"id"`
	DeptName string             `json:"deptName"`
}
