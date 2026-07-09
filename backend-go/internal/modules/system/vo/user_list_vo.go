package vo

import (
	"drip-admin/backend-go/internal/common"
	"time"
)

type UserListVo struct {
	ID          common.Int64String `json:"id"`
	Username    string             `json:"username"`
	RealName    string             `json:"realName"`
	Phone       *string            `json:"phone"`
	Email       *string            `json:"email"`
	Status      int                `json:"status"`
	Dept        *DeptSummaryVo     `json:"dept"`
	Roles       []RoleSummaryVo    `json:"roles"`
	CreatedAt   *time.Time         `json:"createdAt"`
	LastLoginAt *time.Time         `json:"lastLoginAt"`
}
