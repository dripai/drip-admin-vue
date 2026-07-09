package vo

import (
	"drip-admin/backend-go/internal/common"
)

type AuthMeVo struct {
	ID          common.Int64String  `json:"id"`
	Username    string              `json:"username"`
	RealName    string              `json:"realName"`
	Phone       *string             `json:"phone"`
	Email       *string             `json:"email"`
	Avatar      *string             `json:"avatar"`
	DeptID      *common.Int64String `json:"deptId"`
	Roles       []string            `json:"roles"`
	Permissions []string            `json:"permissions"`
	Menus       []MenuTreeVo        `json:"menus"`
}
