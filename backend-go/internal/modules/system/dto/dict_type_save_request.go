package dto

type DictTypeSaveRequest struct {
	DictName string `json:"dictName"`
	DictCode string `json:"dictCode"`
	Status   *int   `json:"status"`
	Builtin  *int   `json:"builtin"`
	Remark   string `json:"remark"`
}
