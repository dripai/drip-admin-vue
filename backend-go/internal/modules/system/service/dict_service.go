package service

import (
	"drip-admin/backend-go/internal/common"
	"github.com/gin-gonic/gin"
)

func (s *Server) DictTypes(c *gin.Context) {
	var rows []SysDictType
	err := s.db.Order("created_at desc").Find(&rows).Error
	successOrError(c, rows, err)
}

func (s *Server) CreateDictType(c *gin.Context) {
	var request DictTypeSaveRequest
	if err := common.BindJSON(c, &request); err != nil {
		common.HandleError(c, err)
		return
	}
	if err := common.RequiredString(request.DictName, "dictName"); err != nil {
		common.HandleError(c, err)
		return
	}
	if err := common.RequiredString(request.DictCode, "dictCode"); err != nil {
		common.HandleError(c, err)
		return
	}
	row := SysDictType{ID: common.NewID(), DictName: request.DictName, DictCode: request.DictCode, Status: intOrDefault(request.Status, 1), Builtin: intOrDefault(request.Builtin, 0), Remark: optionalString(request.Remark)}
	successOrError(c, row.ID, s.db.Create(&row).Error)
}

func (s *Server) UpdateDictType(c *gin.Context) {
	id, err := idParam(c)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	var request DictTypeSaveRequest
	if err := common.BindJSON(c, &request); err != nil {
		common.HandleError(c, err)
		return
	}
	successOrError(c, nil, s.db.Model(&SysDictType{}).Where("id = ?", id.Int64()).Updates(map[string]any{
		"dict_name": request.DictName,
		"dict_code": request.DictCode,
		"status":    intOrDefault(request.Status, 1),
		"builtin":   intOrDefault(request.Builtin, 0),
		"remark":    optionalString(request.Remark),
	}).Error)
}

func (s *Server) DeleteDictType(c *gin.Context) {
	id, err := idParam(c)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	var row SysDictType
	if err := s.db.Where("id = ?", id.Int64()).First(&row).Error; err != nil {
		common.HandleError(c, common.NotFound("operation failed"))
		return
	}
	if row.Builtin == 1 {
		common.HandleError(c, common.NewBusinessError(400501, "内置字典类型不能删除"))
		return
	}
	var count int64
	if err := s.db.Model(&SysDictItem{}).Where("dict_type_id = ?", id.Int64()).Count(&count).Error; err != nil {
		common.HandleError(c, err)
		return
	}
	if count > 0 {
		common.HandleError(c, common.NewBusinessError(400501, "字典类型下存在字典项，不能删除"))
		return
	}
	successOrError(c, nil, s.db.Delete(&SysDictType{}, id.Int64()).Error)
}

func (s *Server) DictItems(c *gin.Context) {
	id, err := idParam(c)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	var rows []SysDictItem
	err = s.db.Where("dict_type_id = ?", id.Int64()).Order("sort asc, id asc").Find(&rows).Error
	successOrError(c, rows, err)
}

func (s *Server) CreateDictItem(c *gin.Context) {
	var request DictItemSaveRequest
	if err := common.BindJSON(c, &request); err != nil {
		common.HandleError(c, err)
		return
	}
	id, err := s.saveDictItem(0, request, true)
	successOrError(c, id, err)
}

func (s *Server) UpdateDictItem(c *gin.Context) {
	id, err := idParam(c)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	var request DictItemSaveRequest
	if err := common.BindJSON(c, &request); err != nil {
		common.HandleError(c, err)
		return
	}
	_, err = s.saveDictItem(id, request, false)
	successOrError(c, nil, err)
}

func (s *Server) DeleteDictItem(c *gin.Context) {
	id, err := idParam(c)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	var row SysDictItem
	if err := s.db.Where("id = ?", id.Int64()).First(&row).Error; err != nil {
		common.HandleError(c, common.NotFound("operation failed"))
		return
	}
	if row.Builtin == 1 {
		common.HandleError(c, common.NewBusinessError(400501, "内置字典项不能删除"))
		return
	}
	successOrError(c, nil, s.db.Delete(&SysDictItem{}, id.Int64()).Error)
}

func (s *Server) DictItemStatus(c *gin.Context) {
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
	successOrError(c, nil, s.db.Model(&SysDictItem{}).Where("id = ?", id.Int64()).Update("status", status).Error)
}

func (s *Server) RefreshDictCache(c *gin.Context) {
	common.Success(c, nil)
}

func (s *Server) saveDictItem(id common.Int64String, request DictItemSaveRequest, create bool) (common.Int64String, error) {
	if request.DictTypeID == nil {
		return 0, common.NewBusinessError(400000, "dictTypeId is required")
	}
	if err := common.RequiredString(request.Label, "label"); err != nil {
		return 0, err
	}
	if err := common.RequiredString(request.Value, "value"); err != nil {
		return 0, err
	}
	var count int64
	if err := s.db.Model(&SysDictType{}).Where("id = ?", request.DictTypeID.Int64()).Count(&count).Error; err != nil {
		return 0, err
	}
	if count == 0 {
		return 0, common.NotFound("operation failed")
	}
	if intOrDefault(request.IsDefault, 0) == 1 {
		query := s.db.Model(&SysDictItem{}).Where("dict_type_id = ? and is_default = 1", request.DictTypeID.Int64())
		if !create {
			query = query.Where("id <> ?", id.Int64())
		}
		if err := query.Update("is_default", 0).Error; err != nil {
			return 0, err
		}
	}
	if create {
		row := SysDictItem{ID: common.NewID(), DictTypeID: *request.DictTypeID, Label: request.Label, Value: request.Value, IsDefault: intOrDefault(request.IsDefault, 0), Sort: intOrDefault(request.Sort, 0), Status: intOrDefault(request.Status, 1), Builtin: intOrDefault(request.Builtin, 0)}
		return row.ID, s.db.Create(&row).Error
	}
	return id, s.db.Model(&SysDictItem{}).Where("id = ?", id.Int64()).Updates(map[string]any{
		"dict_type_id": request.DictTypeID,
		"label":        request.Label,
		"value":        request.Value,
		"is_default":   intOrDefault(request.IsDefault, 0),
		"sort":         intOrDefault(request.Sort, 0),
		"status":       intOrDefault(request.Status, 1),
		"builtin":      intOrDefault(request.Builtin, 0),
	}).Error
}
