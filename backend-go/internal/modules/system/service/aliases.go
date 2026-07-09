package service

import (
	"drip-admin/backend-go/internal/modules/system/dto"
	"drip-admin/backend-go/internal/modules/system/entity"
	"drip-admin/backend-go/internal/modules/system/vo"
)

type (
	SysConfig        = entity.SysConfig
	SysDept          = entity.SysDept
	SysDictItem      = entity.SysDictItem
	SysDictType      = entity.SysDictType
	SysJob           = entity.SysJob
	SysJobRunLog     = entity.SysJobRunLog
	SysLoginLog      = entity.SysLoginLog
	SysMenu          = entity.SysMenu
	SysOperationLog  = entity.SysOperationLog
	SysPrintTemplate = entity.SysPrintTemplate
	SysRole          = entity.SysRole
	SysRoleMenu      = entity.SysRoleMenu
	SysUser          = entity.SysUser
	SysUserRole      = entity.SysUserRole

	ConfigSaveRequest        = dto.ConfigSaveRequest
	DeptSaveRequest          = dto.DeptSaveRequest
	DictItemSaveRequest      = dto.DictItemSaveRequest
	DictTypeSaveRequest      = dto.DictTypeSaveRequest
	JobSaveRequest           = dto.JobSaveRequest
	LoginRequest             = dto.LoginRequest
	MenuAssignRequest        = dto.MenuAssignRequest
	MenuSaveRequest          = dto.MenuSaveRequest
	PasswordRequest          = dto.PasswordRequest
	PasswordResetRequest     = dto.PasswordResetRequest
	PrintTemplateCopyRequest = dto.PrintTemplateCopyRequest
	PrintTemplateSaveRequest = dto.PrintTemplateSaveRequest
	ProfileUpdateRequest     = dto.ProfileUpdateRequest
	RoleAssignRequest        = dto.RoleAssignRequest
	RoleSaveRequest          = dto.RoleSaveRequest
	StatusUpdateRequest      = dto.StatusUpdateRequest
	UserSaveRequest          = dto.UserSaveRequest

	AuthLoginVo      = vo.AuthLoginVo
	AuthMeVo         = vo.AuthMeVo
	DeptSummaryVo    = vo.DeptSummaryVo
	DeptTreeVo       = vo.DeptTreeVo
	FileUploadVo     = vo.FileUploadVo
	HealthVo         = vo.HealthVo
	MenuTreeVo       = vo.MenuTreeVo
	OnlineUserVo     = vo.OnlineUserVo
	OperationLogVo   = vo.OperationLogVo
	RolePermissionVo = vo.RolePermissionVo
	RoleSummaryVo    = vo.RoleSummaryVo
	UserListVo       = vo.UserListVo
)
