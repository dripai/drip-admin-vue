package service

import (
	"drip-admin/backend-go/internal/common"
	"github.com/gin-gonic/gin"
)

func (s *Server) Menus(c *gin.Context) {
	rows, err := s.allMenuTree()
	successOrError(c, rows, err)
}

func (s *Server) CreateMenu(c *gin.Context) {
	var request MenuSaveRequest
	if err := common.BindJSON(c, &request); err != nil {
		common.HandleError(c, err)
		return
	}
	id, err := s.saveMenu(0, request, true)
	successOrError(c, id, err)
}

func (s *Server) UpdateMenu(c *gin.Context) {
	id, err := idParam(c)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	var request MenuSaveRequest
	if err := common.BindJSON(c, &request); err != nil {
		common.HandleError(c, err)
		return
	}
	_, err = s.saveMenu(id, request, false)
	successOrError(c, nil, err)
}

func (s *Server) DeleteMenu(c *gin.Context) {
	id, err := idParam(c)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	var count int64
	if err := s.db.Model(&SysMenu{}).Where("parent_id = ? and deleted = 0", id.Int64()).Count(&count).Error; err != nil {
		common.HandleError(c, err)
		return
	}
	if count > 0 {
		common.HandleError(c, common.NewBusinessError(400301, "operation failed"))
		return
	}
	successOrError(c, nil, s.db.Model(&SysMenu{}).Where("id = ?", id.Int64()).Update("deleted", 1).Error)
}

func (s *Server) MenuStatus(c *gin.Context) {
	id, err := idParam(c)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	status, err := statusRequest(c)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	successOrError(c, nil, s.db.Model(&SysMenu{}).Where("id = ? and deleted = 0", id.Int64()).Update("status", status).Error)
}

func (s *Server) saveMenu(id common.Int64String, request MenuSaveRequest, create bool) (common.Int64String, error) {
	if err := common.RequiredString(request.Name, "name"); err != nil {
		return 0, err
	}
	if err := common.RequiredString(request.Type, "type"); err != nil {
		return 0, err
	}
	parentID := idOrZero(request.ParentID)
	if parentID != 0 {
		var count int64
		if err := s.db.Model(&SysMenu{}).Where("id = ? and deleted = 0", parentID.Int64()).Count(&count).Error; err != nil {
			return 0, err
		}
		if count == 0 {
			return 0, common.NotFound("operation failed")
		}
	}
	if !create && (parentID == id || s.isMenuDescendant(id, parentID)) {
		return 0, common.NewBusinessError(400000, "operation failed")
	}
	values := map[string]any{
		"parent_id":       parentID,
		"name":            request.Name,
		"type":            request.Type,
		"path":            optionalString(request.Path),
		"component":       optionalString(request.Component),
		"permission_code": optionalString(request.PermissionCode),
		"icon":            optionalString(request.Icon),
		"sort":            intOrDefault(request.Sort, 0),
		"visible":         intOrDefault(request.Visible, 1),
		"status":          intOrDefault(request.Status, 1),
	}
	if create {
		row := SysMenu{
			ID:             common.NewID(),
			ParentID:       parentID,
			Name:           request.Name,
			Type:           request.Type,
			Path:           optionalString(request.Path),
			Component:      optionalString(request.Component),
			PermissionCode: optionalString(request.PermissionCode),
			Icon:           optionalString(request.Icon),
			Sort:           intOrDefault(request.Sort, 0),
			Visible:        intOrDefault(request.Visible, 1),
			Status:         intOrDefault(request.Status, 1),
		}
		return row.ID, s.db.Create(&row).Error
	}
	return id, s.db.Model(&SysMenu{}).Where("id = ? and deleted = 0", id.Int64()).Updates(values).Error
}

func (s *Server) isMenuDescendant(id common.Int64String, maybeChild common.Int64String) bool {
	if maybeChild == 0 {
		return false
	}
	var rows []SysMenu
	_ = s.db.Select("id", "parent_id").Where("deleted = 0").Find(&rows).Error
	children := map[int64][]common.Int64String{}
	for _, row := range rows {
		children[row.ParentID.Int64()] = append(children[row.ParentID.Int64()], row.ID)
	}
	var walk func(common.Int64String) bool
	walk = func(current common.Int64String) bool {
		for _, child := range children[current.Int64()] {
			if child == maybeChild || walk(child) {
				return true
			}
		}
		return false
	}
	return walk(id)
}

func (s *Server) allMenuTree() ([]MenuTreeVo, error) {
	var rows []SysMenu
	if err := s.db.Where("deleted = 0").Order("sort asc, id asc").Find(&rows).Error; err != nil {
		return nil, err
	}
	return buildMenuTree(rows, false), nil
}

func (s *Server) menuTreeForUser(userID common.Int64String) ([]MenuTreeVo, error) {
	roles, err := s.roleCodes(userID)
	if err != nil {
		return nil, err
	}
	var rows []SysMenu
	if containsString(roles, "SUPER_ADMIN") {
		if err := s.db.Where("status = 1 and deleted = 0").Order("sort asc, id asc").Find(&rows).Error; err != nil {
			return nil, err
		}
		return buildMenuTree(rows, true), nil
	}
	var allRows []SysMenu
	if err := s.db.Where("status = 1 and deleted = 0").Order("sort asc, id asc").Find(&allRows).Error; err != nil {
		return nil, err
	}
	var directRows []SysRoleMenu
	if err := s.db.Table("sys_role_menu").
		Select("distinct sys_role_menu.*").
		Joins("join sys_user_role on sys_user_role.role_id = sys_role_menu.role_id").
		Where("sys_user_role.user_id = ?", userID.Int64()).
		Find(&directRows).Error; err != nil {
		return nil, err
	}
	byID := map[int64]SysMenu{}
	for _, row := range allRows {
		byID[row.ID.Int64()] = row
	}
	visible := map[int64]bool{}
	for _, direct := range directRows {
		current := direct.MenuID
		for current != 0 {
			menu, ok := byID[current.Int64()]
			if !ok || visible[current.Int64()] {
				break
			}
			visible[current.Int64()] = true
			current = menu.ParentID
		}
	}
	rows = make([]SysMenu, 0, len(allRows))
	for _, row := range allRows {
		if visible[row.ID.Int64()] {
			rows = append(rows, row)
		}
	}
	return buildMenuTree(rows, true), nil
}

func buildMenuTree(rows []SysMenu, excludeButton bool) []MenuTreeVo {
	byID := map[int64]*MenuTreeVo{}
	order := make([]common.Int64String, 0, len(rows))
	for _, row := range rows {
		if excludeButton && row.Type == "BUTTON" {
			continue
		}
		vo := MenuTreeVo{ID: row.ID, ParentID: row.ParentID, Name: row.Name, Type: row.Type, Path: row.Path, Component: row.Component, PermissionCode: row.PermissionCode, Icon: row.Icon, Sort: row.Sort, Visible: row.Visible, Status: row.Status, Children: []MenuTreeVo{}}
		byID[row.ID.Int64()] = &vo
		order = append(order, row.ID)
	}
	for _, id := range order {
		row := byID[id.Int64()]
		parent := byID[row.ParentID.Int64()]
		if row.ParentID != 0 && parent != nil {
			parent.Children = append(parent.Children, *row)
		}
	}
	roots := []MenuTreeVo{}
	for _, id := range order {
		row := byID[id.Int64()]
		if row.ParentID == 0 || byID[row.ParentID.Int64()] == nil {
			roots = append(roots, *row)
		}
	}
	return roots
}
