package dto

import (
	"drip-admin/backend-go/internal/common"
)

type DictItemSaveRequest struct {
	DictTypeID *common.Int64String `json:"dictTypeId"`
	Label      string              `json:"label"`
	Value      string              `json:"value"`
	IsDefault  *int                `json:"isDefault"`
	Sort       *int                `json:"sort"`
	Status     *int                `json:"status"`
	Builtin    *int                `json:"builtin"`
}
