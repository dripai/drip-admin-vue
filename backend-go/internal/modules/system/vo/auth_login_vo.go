package vo

type AuthLoginVo struct {
	Token                string `json:"token"`
	ExpireAt             string `json:"expireAt"`
	ActiveTimeoutSeconds int64  `json:"activeTimeoutSeconds"`
	TokenTimeoutSeconds  int64  `json:"tokenTimeoutSeconds"`
	DeviceType           string `json:"deviceType"`
}
