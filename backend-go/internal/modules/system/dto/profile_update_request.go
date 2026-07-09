package dto

type ProfileUpdateRequest struct {
	RealName string `json:"realName"`
	Phone    string `json:"phone"`
	Email    string `json:"email"`
}
