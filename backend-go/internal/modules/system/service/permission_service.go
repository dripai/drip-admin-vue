package service

import (
	"strings"

	"drip-admin/backend-go/internal/common"
)

func (s *Server) roleCodes(userID common.Int64String) ([]string, error) {
	var roles []SysRole
	err := s.db.Table("sys_role").
		Select("sys_role.*").
		Joins("join sys_user_role on sys_user_role.role_id = sys_role.id").
		Where("sys_user_role.user_id = ? and sys_role.deleted = 0 and sys_role.status = 1", userID.Int64()).
		Find(&roles).Error
	if err != nil {
		return nil, err
	}
	out := make([]string, 0, len(roles))
	for _, role := range roles {
		out = append(out, role.RoleCode)
	}
	return out, nil
}

func (s *Server) permissionCodes(userID common.Int64String) ([]string, error) {
	roles, err := s.roleCodes(userID)
	if err != nil {
		return nil, err
	}
	if containsString(roles, "SUPER_ADMIN") {
		var menus []SysMenu
		if err := s.db.Where("status = 1 and permission_code is not null").Find(&menus).Error; err != nil {
			return nil, err
		}
		return permissionCodesFromMenus(menus), nil
	}
	var menus []SysMenu
	err = s.db.Table("sys_menu").
		Select("distinct sys_menu.*").
		Joins("join sys_role_menu on sys_role_menu.menu_id = sys_menu.id").
		Joins("join sys_user_role on sys_user_role.role_id = sys_role_menu.role_id").
		Where("sys_user_role.user_id = ? and sys_menu.status = 1 and sys_menu.permission_code is not null", userID.Int64()).
		Find(&menus).Error
	if err != nil {
		return nil, err
	}
	return permissionCodesFromMenus(menus), nil
}

func permissionCodesFromMenus(menus []SysMenu) []string {
	seen := map[string]bool{}
	out := make([]string, 0, len(menus))
	for _, menu := range menus {
		if menu.PermissionCode == nil || strings.TrimSpace(*menu.PermissionCode) == "" {
			continue
		}
		if !seen[*menu.PermissionCode] {
			out = append(out, *menu.PermissionCode)
			seen[*menu.PermissionCode] = true
		}
	}
	return out
}

func containsString(values []string, needle string) bool {
	for _, value := range values {
		if value == needle {
			return true
		}
	}
	return false
}
