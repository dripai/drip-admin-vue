package dto

type PrintTemplateSaveRequest struct {
	Code         string `json:"code"`
	Name         string `json:"name"`
	PaperType    string `json:"paperType"`
	TemplateJSON string `json:"templateJson"`
	Status       *int   `json:"status"`
}
