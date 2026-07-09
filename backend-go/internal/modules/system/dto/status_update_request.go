package dto

type StatusUpdateRequest struct {
	Status *int `json:"status"`
}

func (r StatusUpdateRequest) StatusOrDefault() int {
	if r.Status == nil {
		return 1
	}
	return *r.Status
}
