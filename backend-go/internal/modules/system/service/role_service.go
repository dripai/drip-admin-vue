package service

import (
	"drip-admin/backend-go/internal/common"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

func (s *Server) Roles(c *gin.Context) {
	page, err := common.ParsePage(c)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	status, err := parseOptionalInt(c, "status")
	if err != nil {
		common.HandleError(c, err)
		return
	}
	query := s.db.Model(&SysRole{}).Where("deleted = 0")
	query = likeIfPresent(query, "role_name", c.Query("roleName"))
	query = likeIfPresent(query, "role_code", c.Query("roleCode"))
	query = eqIfPresent(query, "status", status)
	query = likeIfPresent(query, "created_at", c.Query("createdAt")).Order("created_at desc")
	var rows []SysRole
	total, err := s.pageResult(query, page, &rows)
	successOrError(c, common.PageResult[SysRole]{List: rows, Total: total, Page: page.Page, PageSize: page.PageSize}, err)
}

func (s *Server) Role(c *gin.Context) {
	id, err := idParam(c)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	var row SysRole
	err = s.db.Where("id = ? and deleted = 0", id.Int64()).First(&row).Error
	if err != nil {
		common.HandleError(c, common.NotFound("operation failed"))
		return
	}
	common.Success(c, row)
}

func (s *Server) RoleUsers(c *gin.Context) {
	id, err := idParam(c)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	page, err := common.ParsePage(c)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	query := s.db.Model(&SysUser{}).Where("deleted = 0 and id in (?)", s.db.Table("sys_user_role").Select("user_id").Where("role_id = ?", id.Int64())).Order("created_at desc")
	var rows []SysUser
	total, err := s.pageResult(query, page, &rows)
	successOrError(c, common.PageResult[SysUser]{List: rows, Total: total, Page: page.Page, PageSize: page.PageSize}, err)
}

func (s *Server) RolePermissions(c *gin.Context) {
	id, err := idParam(c)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	var rows []SysRoleMenu
	if err := s.db.Where("role_id = ?", id.Int64()).Find(&rows).Error; err != nil {
		common.HandleError(c, err)
		return
	}
	menuIDs := make([]common.Int64String, 0, len(rows))
	for _, row := range rows {
		menuIDs = append(menuIDs, row.MenuID)
	}
	common.Success(c, RolePermissionVo{MenuIDs: menuIDs, PermissionCode: []string{}})
}

func (s *Server) RoleOptions(c *gin.Context) {
	var rows []SysRole
	err := s.db.Where("deleted = 0").Find(&rows).Error
	successOrError(c, rows, err)
}

func (s *Server) CreateRole(c *gin.Context) {
	var request RoleSaveRequest
	if err := common.BindJSON(c, &request); err != nil {
		common.HandleError(c, err)
		return
	}
	id, err := s.saveRole(0, request, true)
	successOrError(c, id, err)
}

func (s *Server) UpdateRole(c *gin.Context) {
	id, err := idParam(c)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	var request RoleSaveRequest
	if err := common.BindJSON(c, &request); err != nil {
		common.HandleError(c, err)
		return
	}
	_, err = s.saveRole(id, request, false)
	successOrError(c, nil, err)
}

func (s *Server) DeleteRole(c *gin.Context) {
	id, err := idParam(c)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	var role SysRole
	if err := s.db.Where("id = ? and deleted = 0", id.Int64()).First(&role).Error; err != nil {
		common.HandleError(c, common.NotFound("operation failed"))
		return
	}
	if role.Builtin == 1 {
		common.HandleError(c, common.NewBusinessError(400000, "operation failed"))
		return
	}
	var count int64
	if err := s.db.Model(&SysUserRole{}).Where("role_id = ?", id.Int64()).Count(&count).Error; err != nil {
		common.HandleError(c, err)
		return
	}
	if count > 0 {
		common.HandleError(c, common.NewBusinessError(409000, "operation failed"))
		return
	}
	successOrError(c, nil, s.db.Model(&SysRole{}).Where("id = ?", id.Int64()).Update("deleted", 1).Error)
}

func (s *Server) RoleStatus(c *gin.Context) {
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
	successOrError(c, nil, s.db.Model(&SysRole{}).Where("id = ? and deleted = 0", id.Int64()).Update("status", status).Error)
}

func (s *Server) AssignRoleMenus(c *gin.Context) {
	id, err := idParam(c)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	var request MenuAssignRequest
	if err := common.BindJSON(c, &request); err != nil {
		common.HandleError(c, err)
		return
	}
	successOrError(c, nil, s.assignRoleMenuIDs(id, request.MenuIDs))
}

func (s *Server) saveRole(id common.Int64String, request RoleSaveRequest, create bool) (common.Int64String, error) {
	if err := common.RequiredString(request.RoleName, "roleName"); err != nil {
		return 0, err
	}
	if err := common.RequiredString(request.RoleCode, "roleCode"); err != nil {
		return 0, err
	}
	if create {
		row := SysRole{ID: common.NewID(), RoleName: request.RoleName, RoleCode: request.RoleCode, Status: intOrDefault(request.Status, 1), Remark: optionalString(request.Remark)}
		return row.ID, s.db.Create(&row).Error
	}
	return id, s.db.Model(&SysRole{}).Where("id = ? and deleted = 0", id.Int64()).Updates(map[string]any{
		"role_name": request.RoleName,
		"role_code": request.RoleCode,
		"status":    intOrDefault(request.Status, 1),
		"remark":    optionalString(request.Remark),
	}).Error
}

func (s *Server) assignRoleMenuIDs(roleID common.Int64String, menuIDs []common.Int64String) error {
	var role SysRole
	if err := s.db.Where("id = ? and deleted = 0", roleID.Int64()).First(&role).Error; err != nil {
		return common.NotFound("operation failed")
	}
	if err := s.assertExistingMenus(menuIDs); err != nil {
		return err
	}
	return s.db.Transaction(func(tx *gorm.DB) error {
		if err := tx.Where("role_id = ?", roleID.Int64()).Delete(&SysRoleMenu{}).Error; err != nil {
			return err
		}
		for _, menuID := range menuIDs {
			row := SysRoleMenu{ID: common.NewID(), RoleID: roleID, MenuID: menuID}
			if err := tx.Create(&row).Error; err != nil {
				return err
			}
		}
		return nil
	})
}

func (s *Server) assertExistingMenus(menuIDs []common.Int64String) error {
	seen := map[int64]bool{}
	ids := make([]int64, 0, len(menuIDs))
	for _, id := range menuIDs {
		if seen[id.Int64()] {
			return common.NewBusinessError(400000, "operation failed")
		}
		seen[id.Int64()] = true
		ids = append(ids, id.Int64())
	}
	if len(ids) == 0 {
		return nil
	}
	var count int64
	if err := s.db.Model(&SysMenu{}).Where("id in ? and deleted = 0", ids).Count(&count).Error; err != nil {
		return err
	}
	if int(count) != len(ids) {
		return common.NewBusinessError(400000, "operation failed")
	}
	return nil
}
