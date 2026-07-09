package dto

type RoleSaveRequest struct {
	RoleName string `json:"roleName"`
	RoleCode string `json:"roleCode"`
	Status   *int   `json:"status"`
	Remark   string `json:"remark"`
}
