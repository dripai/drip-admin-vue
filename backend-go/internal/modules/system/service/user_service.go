package service

import (
	"strings"

	"drip-admin/backend-go/internal/common"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

func (s *Server) Users(c *gin.Context) {
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
	query := s.db.Model(&SysUser{}).Where("deleted = 0")
	query = likeIfPresent(query, "username", c.Query("username"))
	query = likeIfPresent(query, "real_name", c.Query("realName"))
	query = likeIfPresent(query, "phone", c.Query("phone"))
	query = eqIfPresent(query, "status", status)
	if deptID := strings.TrimSpace(c.Query("deptId")); deptID != "" {
		var id common.Int64String
		if err := id.UnmarshalJSON([]byte(deptID)); err != nil {
			common.HandleError(c, common.NewBusinessError(400000, "deptId is invalid"))
			return
		}
		query = query.Where("dept_id = ?", id.Int64())
	}
	if roleID := strings.TrimSpace(c.Query("roleId")); roleID != "" {
		var id common.Int64String
		if err := id.UnmarshalJSON([]byte(roleID)); err != nil {
			common.HandleError(c, common.NewBusinessError(400000, "roleId is invalid"))
			return
		}
		query = query.Where("id in (?)", s.db.Table("sys_user_role").Select("user_id").Where("role_id = ?", id.Int64()))
	}
	query = query.Order("created_at desc")
	var users []SysUser
	total, err := s.pageResult(query, page, &users)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	list, err := s.toUserList(users)
	successOrError(c, common.PageResult[UserListVo]{List: list, Total: total, Page: page.Page, PageSize: page.PageSize}, err)
}

func (s *Server) User(c *gin.Context) {
	id, err := idParam(c)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	row, err := s.userDetail(id)
	successOrError(c, row, err)
}

func (s *Server) CreateUser(c *gin.Context) {
	var request UserSaveRequest
	if err := common.BindJSON(c, &request); err != nil {
		common.HandleError(c, err)
		return
	}
	id, err := s.saveUser(0, request, true)
	successOrError(c, id, err)
}

func (s *Server) UpdateUser(c *gin.Context) {
	id, err := idParam(c)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	var request UserSaveRequest
	if err := common.BindJSON(c, &request); err != nil {
		common.HandleError(c, err)
		return
	}
	_, err = s.saveUser(id, request, false)
	successOrError(c, nil, err)
}

func (s *Server) DeleteUser(c *gin.Context) {
	id, err := idParam(c)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	session, _ := currentSession(c)
	if id == session.UserID {
		common.HandleError(c, common.NewBusinessError(400000, "operation failed"))
		return
	}
	if err := s.assertNotSuperAdminTarget(session.UserID, id); err != nil {
		common.HandleError(c, err)
		return
	}
	_, err = s.userDetail(id)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	successOrError(c, nil, s.db.Model(&SysUser{}).Where("id = ?", id.Int64()).Update("deleted", 1).Error)
}

func (s *Server) UserStatus(c *gin.Context) {
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
	session, _ := currentSession(c)
	if id == session.UserID && status != 1 {
		common.HandleError(c, common.NewBusinessError(400000, "operation failed"))
		return
	}
	if err := s.assertNotSuperAdminTarget(session.UserID, id); err != nil {
		common.HandleError(c, err)
		return
	}
	successOrError(c, nil, s.db.Model(&SysUser{}).Where("id = ? and deleted = 0", id.Int64()).Update("status", status).Error)
}

func (s *Server) UnlockUser(c *gin.Context) {
	id, err := idParam(c)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	user, err := s.userDetail(id)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	successOrError(c, nil, s.redis.Del(c.Request.Context(), loginAttemptKey(user.Username)).Err())
}

func (s *Server) UserRoles(c *gin.Context) {
	id, err := idParam(c)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	var request RoleAssignRequest
	if err := common.BindJSON(c, &request); err != nil {
		common.HandleError(c, err)
		return
	}
	session, _ := currentSession(c)
	if err := s.assertNotSuperAdminTarget(session.UserID, id); err != nil {
		common.HandleError(c, err)
		return
	}
	successOrError(c, nil, s.assignUserRoles(id, request.RoleIDs))
}

func (s *Server) ResetPassword(c *gin.Context) {
	id, err := idParam(c)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	var request PasswordResetRequest
	if err := common.BindJSON(c, &request); err != nil {
		common.HandleError(c, err)
		return
	}
	password := request.Password
	if strings.TrimSpace(password) == "" {
		password = "Admin@123456"
	}
	if len(password) < 8 || len(password) > 64 {
		common.HandleError(c, common.NewBusinessError(400000, "password length must be 8 to 64"))
		return
	}
	session, _ := currentSession(c)
	if err := s.assertNotSuperAdminTarget(session.UserID, id); err != nil {
		common.HandleError(c, err)
		return
	}
	salt := common.NewSalt()
	err = s.db.Model(&SysUser{}).Where("id = ? and deleted = 0", id.Int64()).Updates(map[string]any{
		"password_salt": salt,
		"password_hash": common.HashPassword(password, salt),
	}).Error
	successOrError(c, nil, err)
}

func (s *Server) saveUser(id common.Int64String, request UserSaveRequest, create bool) (common.Int64String, error) {
	if err := common.RequiredString(request.Username, "username"); err != nil {
		return 0, err
	}
	if err := common.RequiredString(request.RealName, "realName"); err != nil {
		return 0, err
	}
	if request.DeptID != nil {
		var count int64
		if err := s.db.Model(&SysDept{}).Where("id = ? and deleted = 0", request.DeptID.Int64()).Count(&count).Error; err != nil {
			return 0, err
		}
		if count == 0 {
			return 0, common.NewBusinessError(400000, "部门不存在")
		}
	}
	status := intOrDefault(request.Status, 1)
	if create {
		password := request.Password
		if strings.TrimSpace(password) == "" {
			password = "Admin@123456"
		}
		salt := common.NewSalt()
		row := SysUser{
			ID:           common.NewID(),
			Username:     request.Username,
			RealName:     request.RealName,
			Phone:        stringOrNil(request.Phone),
			Email:        stringOrNil(request.Email),
			Status:       status,
			DeptID:       request.DeptID,
			Remark:       optionalString(request.Remark),
			PasswordSalt: salt,
			PasswordHash: common.HashPassword(password, salt),
		}
		return row.ID, s.db.Create(&row).Error
	}
	_, err := s.userDetail(id)
	if err != nil {
		return 0, err
	}
	updates := map[string]any{
		"username":  request.Username,
		"real_name": request.RealName,
		"phone":     request.Phone,
		"email":     request.Email,
		"status":    status,
		"dept_id":   request.DeptID,
		"remark":    optionalString(request.Remark),
	}
	return id, s.db.Model(&SysUser{}).Where("id = ?", id.Int64()).Updates(updates).Error
}

func (s *Server) userDetail(id common.Int64String) (SysUser, error) {
	var row SysUser
	err := s.db.Where("id = ? and deleted = 0", id.Int64()).First(&row).Error
	if err != nil {
		return SysUser{}, common.NotFound("operation failed")
	}
	return row, nil
}

func (s *Server) toUserList(users []SysUser) ([]UserListVo, error) {
	out := make([]UserListVo, 0, len(users))
	for _, user := range users {
		dept, err := s.deptSummary(user.DeptID)
		if err != nil {
			return nil, err
		}
		roles, err := s.roleSummaries(user.ID)
		if err != nil {
			return nil, err
		}
		out = append(out, UserListVo{
			ID:          user.ID,
			Username:    user.Username,
			RealName:    user.RealName,
			Phone:       user.Phone,
			Email:       user.Email,
			Status:      user.Status,
			Dept:        dept,
			Roles:       roles,
			CreatedAt:   user.CreatedAt,
			LastLoginAt: user.LastLoginAt,
		})
	}
	return out, nil
}

func (s *Server) assertNotSuperAdminTarget(currentUserID common.Int64String, targetUserID common.Int64String) error {
	currentRoles, err := s.roleCodes(currentUserID)
	if err != nil {
		return err
	}
	if containsString(currentRoles, "SUPER_ADMIN") {
		return nil
	}
	targetRoles, err := s.roleCodes(targetUserID)
	if err != nil {
		return err
	}
	if containsString(targetRoles, "SUPER_ADMIN") {
		return common.NewBusinessError(403000, "不能操作超级管理员")
	}
	return nil
}

func (s *Server) assignUserRoles(userID common.Int64String, roleIDs []common.Int64String) error {
	if _, err := s.userDetail(userID); err != nil {
		return err
	}
	if err := s.assertExistingRoles(roleIDs); err != nil {
		return err
	}
	return s.db.Transaction(func(tx *gorm.DB) error {
		if err := tx.Where("user_id = ?", userID.Int64()).Delete(&SysUserRole{}).Error; err != nil {
			return err
		}
		for _, roleID := range roleIDs {
			row := SysUserRole{ID: common.NewID(), UserID: userID, RoleID: roleID}
			if err := tx.Create(&row).Error; err != nil {
				return err
			}
		}
		return nil
	})
}

func (s *Server) assertExistingRoles(roleIDs []common.Int64String) error {
	seen := map[int64]bool{}
	for _, id := range roleIDs {
		if seen[id.Int64()] {
			return common.NewBusinessError(400000, "operation failed")
		}
		seen[id.Int64()] = true
	}
	if len(roleIDs) == 0 {
		return nil
	}
	var count int64
	ids := make([]int64, 0, len(roleIDs))
	for _, id := range roleIDs {
		ids = append(ids, id.Int64())
	}
	if err := s.db.Model(&SysRole{}).Where("id in ? and deleted = 0", ids).Count(&count).Error; err != nil {
		return err
	}
	if int(count) != len(ids) {
		return common.NewBusinessError(400000, "operation failed")
	}
	return nil
}

func (s *Server) roleSummaries(userID common.Int64String) ([]RoleSummaryVo, error) {
	var rows []SysRole
	err := s.db.Table("sys_role").
		Select("sys_role.*").
		Joins("join sys_user_role on sys_user_role.role_id = sys_role.id").
		Where("sys_user_role.user_id = ? and sys_role.deleted = 0", userID.Int64()).
		Find(&rows).Error
	if err != nil {
		return nil, err
	}
	out := make([]RoleSummaryVo, 0, len(rows))
	for _, row := range rows {
		out = append(out, RoleSummaryVo{ID: row.ID, RoleName: row.RoleName, RoleCode: row.RoleCode})
	}
	return out, nil
}

func (s *Server) deptSummary(id *common.Int64String) (*DeptSummaryVo, error) {
	if id == nil {
		return nil, nil
	}
	var row SysDept
	if err := s.db.Where("id = ? and deleted = 0", id.Int64()).First(&row).Error; err != nil {
		return nil, nil
	}
	return &DeptSummaryVo{ID: row.ID, DeptName: row.DeptName}, nil
}
