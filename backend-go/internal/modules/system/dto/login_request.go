package dto

type LoginRequest struct {
	Username   string `json:"username"`
	Password   string `json:"password"`
	DeviceType string `json:"deviceType"`
}
