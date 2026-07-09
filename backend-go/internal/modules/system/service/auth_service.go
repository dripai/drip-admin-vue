package service

import (
	"fmt"
	"strings"
	"time"

	"drip-admin/backend-go/internal/common"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

func (s *Server) Login(c *gin.Context) {
	var request LoginRequest
	if err := common.BindJSON(c, &request); err != nil {
		common.HandleError(c, err)
		return
	}
	if err := common.RequiredString(request.Username, "username"); err != nil {
		common.HandleError(c, err)
		return
	}
	if err := common.RequiredString(request.Password, "password"); err != nil {
		common.HandleError(c, err)
		return
	}
	if err := common.RequiredString(request.DeviceType, "deviceType"); err != nil {
		common.HandleError(c, err)
		return
	}
	var user SysUser
	err := s.db.Where("username = ?", request.Username).First(&user).Error
	if err == gorm.ErrRecordNotFound {
		s.writeLoginLog(c, nil, request.Username, nil, "LOGIN", "FAIL", "用户名或密码错误", request.DeviceType)
		common.HandleError(c, common.NewBusinessError(401000, "用户名或密码错误"))
		return
	}
	if err != nil {
		common.HandleError(c, err)
		return
	}
	if user.Status != 1 || user.Deleted == 1 {
		s.writeLoginLog(c, common.IDPtr(user.ID), request.Username, &user.RealName, "LOGIN", "FAIL", "账号已禁用", request.DeviceType)
		common.HandleError(c, common.NewBusinessError(401000, "账号已禁用"))
		return
	}
	if err := s.assertLoginNotLocked(c.Request.Context(), request.Username); err != nil {
		common.HandleError(c, err)
		return
	}
	if common.HashPassword(request.Password, user.PasswordSalt) != user.PasswordHash {
		s.writeLoginLog(c, common.IDPtr(user.ID), request.Username, &user.RealName, "LOGIN", "FAIL", "用户名或密码错误", request.DeviceType)
		remaining, err := s.recordLoginFailure(c.Request.Context(), request.Username)
		if err != nil {
			common.HandleError(c, err)
			return
		}
		common.HandleError(c, common.NewBusinessError(401000, fmt.Sprintf("用户名或密码错误，还剩%d次机会", remaining)))
		return
	}
	_ = s.redis.Del(c.Request.Context(), loginAttemptKey(request.Username)).Err()
	now := time.Now()
	token := newToken()
	deviceType := normalizeDeviceType(request.DeviceType)
	session := SessionData{
		TokenID:      token,
		UserID:       user.ID,
		Username:     user.Username,
		RealName:     user.RealName,
		DeviceType:   deviceType,
		IP:           clientIP(c),
		UserAgent:    c.GetHeader("User-Agent"),
		LoginAt:      now.Format(time.RFC3339),
		LastActiveAt: now.Format(time.RFC3339),
		ExpireAt:     now.Add(time.Duration(s.cfg.Token.ActiveTimeoutSeconds) * time.Second).Format(time.RFC3339),
	}
	_ = s.removeExistingUserDeviceSession(c.Request.Context(), user.ID, deviceType)
	if err := s.writeSession(c.Request.Context(), session); err != nil {
		common.HandleError(c, err)
		return
	}
	s.db.Model(&SysUser{}).Where("id = ?", user.ID.Int64()).Update("last_login_at", now)
	s.writeLoginLog(c, common.IDPtr(user.ID), user.Username, &user.RealName, "LOGIN", "SUCCESS", "", request.DeviceType)
	common.Success(c, AuthLoginVo{
		Token:                token,
		ExpireAt:             session.ExpireAt,
		ActiveTimeoutSeconds: s.cfg.Token.ActiveTimeoutSeconds,
		TokenTimeoutSeconds:  s.cfg.Token.TimeoutSeconds,
		DeviceType:           deviceType,
	})
}

func (s *Server) Logout(c *gin.Context) {
	session, ok := currentSession(c)
	if ok {
		s.writeLoginLog(c, common.IDPtr(session.UserID), session.Username, &session.RealName, "LOGOUT", "SUCCESS", "", session.DeviceType)
		_ = s.removeSession(c.Request.Context(), session.TokenID)
	}
	common.Success(c, nil)
}

func (s *Server) Me(c *gin.Context) {
	session, _ := currentSession(c)
	user, err := s.userDetail(session.UserID)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	roles, err := s.roleCodes(user.ID)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	permissions, err := s.permissionCodes(user.ID)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	menus, err := s.menuTreeForUser(user.ID)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	common.Success(c, AuthMeVo{
		ID:          user.ID,
		Username:    user.Username,
		RealName:    user.RealName,
		Phone:       user.Phone,
		Email:       user.Email,
		Avatar:      user.Avatar,
		DeptID:      user.DeptID,
		Roles:       roles,
		Permissions: permissions,
		Menus:       menus,
	})
}

func (s *Server) Password(c *gin.Context) {
	session, _ := currentSession(c)
	var request PasswordRequest
	if err := common.BindJSON(c, &request); err != nil {
		common.HandleError(c, err)
		return
	}
	if err := common.RequiredString(request.OldPassword, "oldPassword"); err != nil {
		common.HandleError(c, err)
		return
	}
	if err := common.RequiredString(request.NewPassword, "newPassword"); err != nil {
		common.HandleError(c, err)
		return
	}
	if len(request.NewPassword) < 8 || len(request.NewPassword) > 64 {
		common.HandleError(c, common.NewBusinessError(400000, "newPassword length must be 8 to 64"))
		return
	}
	user, err := s.userDetail(session.UserID)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	if common.HashPassword(request.OldPassword, user.PasswordSalt) != user.PasswordHash {
		common.HandleError(c, common.NewBusinessError(400000, "旧密码错误"))
		return
	}
	salt := common.NewSalt()
	err = s.db.Model(&SysUser{}).Where("id = ?", user.ID.Int64()).Updates(map[string]any{
		"password_salt": salt,
		"password_hash": common.HashPassword(request.NewPassword, salt),
	}).Error
	successOrError(c, nil, err)
}

func (s *Server) Profile(c *gin.Context) {
	session, _ := currentSession(c)
	var request ProfileUpdateRequest
	if err := common.BindJSON(c, &request); err != nil {
		common.HandleError(c, err)
		return
	}
	if err := common.RequiredString(request.RealName, "realName"); err != nil {
		common.HandleError(c, err)
		return
	}
	err := s.db.Model(&SysUser{}).Where("id = ?", session.UserID.Int64()).Updates(map[string]any{
		"real_name": request.RealName,
		"phone":     strings.TrimSpace(request.Phone),
		"email":     strings.TrimSpace(request.Email),
	}).Error
	if err == nil {
		session.RealName = request.RealName
		_ = s.writeSession(c.Request.Context(), session)
	}
	successOrError(c, nil, err)
}
