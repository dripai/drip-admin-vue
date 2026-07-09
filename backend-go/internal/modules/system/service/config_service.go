package service

import (
	"strconv"
	"strings"

	"drip-admin/backend-go/internal/common"
	"github.com/gin-gonic/gin"
)

func (s *Server) Configs(c *gin.Context) {
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
	query := s.db.Model(&SysConfig{}).Where("deleted = 0")
	query = likeIfPresent(query, "config_name", c.Query("configName"))
	query = likeIfPresent(query, "config_key", c.Query("configKey"))
	query = eqIfPresent(query, "status", status)
	query = query.Order("created_at desc")
	var rows []SysConfig
	total, err := s.pageResult(query, page, &rows)
	successOrError(c, common.PageResult[SysConfig]{List: rows, Total: total, Page: page.Page, PageSize: page.PageSize}, err)
}

func (s *Server) PublicConfig(c *gin.Context) {
	systemName, err := s.requiredConfig("system.name")
	if err != nil {
		common.HandleError(c, err)
		return
	}
	common.Success(c, map[string]string{
		"systemName":         systemName,
		"companyFullName":    s.configOrDefault("system.company.fullName", ""),
		"logoUrl":            s.configOrDefault("system.logo", ""),
		"watermarkEnabled":   s.configOrDefault("system.watermark.enabled", "false"),
		"silentPrintEnabled": s.configOrDefault("print.silent.enabled", "false"),
	})
}

func (s *Server) CreateConfig(c *gin.Context) {
	var request ConfigSaveRequest
	if err := common.BindJSON(c, &request); err != nil {
		common.HandleError(c, err)
		return
	}
	id, err := s.saveConfig(0, request, true)
	successOrError(c, id, err)
}

func (s *Server) UpdateConfig(c *gin.Context) {
	id, err := idParam(c)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	var request ConfigSaveRequest
	if err := common.BindJSON(c, &request); err != nil {
		common.HandleError(c, err)
		return
	}
	_, err = s.saveConfig(id, request, false)
	successOrError(c, nil, err)
}

func (s *Server) DeleteConfig(c *gin.Context) {
	id, err := idParam(c)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	var row SysConfig
	if err := s.configDetail(id, &row); err != nil {
		common.HandleError(c, err)
		return
	}
	if row.Builtin == 1 {
		common.HandleError(c, common.NewBusinessError(400000, "operation failed"))
		return
	}
	successOrError(c, nil, s.db.Model(&SysConfig{}).Where("id = ?", id.Int64()).Update("deleted", 1).Error)
}

func (s *Server) ConfigStatus(c *gin.Context) {
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
	var row SysConfig
	if err := s.configDetail(id, &row); err != nil {
		common.HandleError(c, err)
		return
	}
	if row.Builtin == 1 {
		common.HandleError(c, common.NewBusinessError(400000, "operation failed"))
		return
	}
	successOrError(c, nil, s.db.Model(&SysConfig{}).Where("id = ?", id.Int64()).Update("status", status).Error)
}

func (s *Server) saveConfig(id common.Int64String, request ConfigSaveRequest, create bool) (common.Int64String, error) {
	if err := common.RequiredString(request.ConfigName, "configName"); err != nil {
		return 0, err
	}
	if err := common.RequiredString(request.ConfigKey, "configKey"); err != nil {
		return 0, err
	}
	if err := validateConfigValue(typeOrDefault(request.ValueType), request.ConfigValue); err != nil {
		return 0, err
	}
	if create {
		row := SysConfig{
			ID:          common.NewID(),
			ConfigName:  request.ConfigName,
			ConfigKey:   request.ConfigKey,
			ConfigValue: request.ConfigValue,
			ValueType:   typeOrDefault(request.ValueType),
			Builtin:     0,
			Status:      intOrDefault(request.Status, 1),
			Remark:      optionalString(request.Remark),
		}
		return row.ID, s.db.Create(&row).Error
	}
	var current SysConfig
	if err := s.configDetail(id, &current); err != nil {
		return 0, err
	}
	updates := map[string]any{
		"config_name":  request.ConfigName,
		"config_key":   request.ConfigKey,
		"config_value": request.ConfigValue,
		"value_type":   typeOrDefault(request.ValueType),
		"remark":       optionalString(request.Remark),
	}
	if current.Builtin != 1 {
		updates["status"] = intOrDefault(request.Status, 1)
	}
	return id, s.db.Model(&SysConfig{}).Where("id = ?", id.Int64()).Updates(updates).Error
}

func (s *Server) configDetail(id common.Int64String, out *SysConfig) error {
	err := s.db.Where("id = ? and deleted = 0", id.Int64()).First(out).Error
	if err != nil {
		return common.NotFound("operation failed")
	}
	return nil
}

func (s *Server) requiredConfig(key string) (string, error) {
	var row SysConfig
	err := s.db.Where("config_key = ? and (builtin = 1 or status = 1) and deleted = 0", key).First(&row).Error
	if err != nil || strings.TrimSpace(row.ConfigValue) == "" {
		return "", common.NewBusinessError(500000, "system config missing: "+key)
	}
	return row.ConfigValue, nil
}

func (s *Server) configOrDefault(key string, defaultValue string) string {
	var row SysConfig
	if err := s.db.Where("config_key = ? and (builtin = 1 or status = 1) and deleted = 0", key).First(&row).Error; err != nil {
		return defaultValue
	}
	return row.ConfigValue
}

func (s *Server) requiredIntConfig(key string) (int, error) {
	value, err := s.requiredConfig(key)
	if err != nil {
		return 0, err
	}
	parsed, err := strconv.Atoi(value)
	if err != nil {
		return 0, common.NewBusinessError(500000, "system config invalid: "+key)
	}
	return parsed, nil
}

func (s *Server) requiredLongConfig(key string) (int64, error) {
	value, err := s.requiredConfig(key)
	if err != nil {
		return 0, err
	}
	parsed, err := strconv.ParseInt(value, 10, 64)
	if err != nil {
		return 0, common.NewBusinessError(500000, "system config invalid: "+key)
	}
	return parsed, nil
}

func typeOrDefault(value string) string {
	if strings.TrimSpace(value) == "" {
		return "string"
	}
	return value
}

func validateConfigValue(valueType string, value string) error {
	switch valueType {
	case "boolean":
		if !strings.EqualFold(value, "true") && !strings.EqualFold(value, "false") {
			return common.NewBusinessError(400000, "configValue must be true or false")
		}
	case "number":
		if _, err := strconv.ParseFloat(value, 64); err != nil {
			return common.NewBusinessError(400000, "configValue must be number")
		}
	case "string":
	default:
		return common.NewBusinessError(400000, "valueType must be string, boolean or number")
	}
	return nil
}
