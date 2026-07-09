package dto

type PrintTemplateCopyRequest struct {
	Code   string `json:"code"`
	Name   string `json:"name"`
	Status *int   `json:"status"`
}
