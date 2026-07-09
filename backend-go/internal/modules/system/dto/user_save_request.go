package dto

import (
	"drip-admin/backend-go/internal/common"
)

type UserSaveRequest struct {
	Username string              `json:"username"`
	RealName string              `json:"realName"`
	Phone    string              `json:"phone"`
	Email    string              `json:"email"`
	Status   *int                `json:"status"`
	DeptID   *common.Int64String `json:"deptId"`
	Remark   string              `json:"remark"`
	Password string              `json:"password"`
}
