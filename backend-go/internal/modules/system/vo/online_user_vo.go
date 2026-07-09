package vo

import (
	"drip-admin/backend-go/internal/common"
)

type OnlineUserVo struct {
	TokenID      string             `json:"tokenId"`
	UserID       common.Int64String `json:"userId"`
	Username     string             `json:"username"`
	RealName     string             `json:"realName"`
	DeviceType   string             `json:"deviceType"`
	IP           string             `json:"ip"`
	UserAgent    string             `json:"userAgent"`
	LoginAt      string             `json:"loginAt"`
	LastActiveAt string             `json:"lastActiveAt"`
	ExpireAt     string             `json:"expireAt"`
	Current      bool               `json:"current"`
}
