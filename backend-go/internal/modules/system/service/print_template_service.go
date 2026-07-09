package service

import (
	"drip-admin/backend-go/internal/common"
	"github.com/gin-gonic/gin"
)

func (s *Server) PrintTemplates(c *gin.Context) {
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
	query := s.db.Model(&SysPrintTemplate{}).Where("deleted = 0")
	query = likeIfPresent(query, "code", c.Query("code"))
	query = likeIfPresent(query, "name", c.Query("name"))
	query = eqIfPresent(query, "status", status)
	query = query.Order("updated_at desc, id desc")
	var rows []SysPrintTemplate
	total, err := s.pageResult(query, page, &rows)
	successOrError(c, common.PageResult[SysPrintTemplate]{List: rows, Total: total, Page: page.Page, PageSize: page.PageSize}, err)
}

func (s *Server) PrintTemplate(c *gin.Context) {
	id, err := idParam(c)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	var row SysPrintTemplate
	if err := s.db.Where("id = ? and deleted = 0", id.Int64()).First(&row).Error; err != nil {
		common.HandleError(c, common.NewBusinessError(404000, "print template not found"))
		return
	}
	common.Success(c, row)
}

func (s *Server) CreatePrintTemplate(c *gin.Context) {
	var request PrintTemplateSaveRequest
	if err := common.BindJSON(c, &request); err != nil {
		common.HandleError(c, err)
		return
	}
	id, err := s.savePrintTemplate(0, request, true)
	successOrError(c, id, err)
}

func (s *Server) CopyPrintTemplate(c *gin.Context) {
	id, err := idParam(c)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	var request PrintTemplateCopyRequest
	if err := common.BindJSON(c, &request); err != nil {
		common.HandleError(c, err)
		return
	}
	if err := s.ensurePrintCodeAvailable(request.Code, nil); err != nil {
		common.HandleError(c, err)
		return
	}
	var source SysPrintTemplate
	if err := s.db.Where("id = ? and deleted = 0", id.Int64()).First(&source).Error; err != nil {
		common.HandleError(c, common.NewBusinessError(404000, "print template not found"))
		return
	}
	row := SysPrintTemplate{ID: common.NewID(), Code: request.Code, Name: request.Name, PaperType: source.PaperType, TemplateJSON: source.TemplateJSON, Status: intOrDefault(request.Status, 1)}
	successOrError(c, row.ID, s.db.Create(&row).Error)
}

func (s *Server) UpdatePrintTemplate(c *gin.Context) {
	id, err := idParam(c)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	var request PrintTemplateSaveRequest
	if err := common.BindJSON(c, &request); err != nil {
		common.HandleError(c, err)
		return
	}
	_, err = s.savePrintTemplate(id, request, false)
	successOrError(c, nil, err)
}

func (s *Server) DeletePrintTemplate(c *gin.Context) {
	id, err := idParam(c)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	successOrError(c, nil, s.db.Delete(&SysPrintTemplate{}, id.Int64()).Error)
}

func (s *Server) PrintTemplateStatus(c *gin.Context) {
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
	successOrError(c, nil, s.db.Model(&SysPrintTemplate{}).Where("id = ? and deleted = 0", id.Int64()).Update("status", status).Error)
}

func (s *Server) savePrintTemplate(id common.Int64String, request PrintTemplateSaveRequest, create bool) (common.Int64String, error) {
	if err := common.RequiredString(request.Code, "code"); err != nil {
		return 0, err
	}
	if err := common.RequiredString(request.Name, "name"); err != nil {
		return 0, err
	}
	if err := common.RequiredString(request.PaperType, "paperType"); err != nil {
		return 0, err
	}
	if err := common.RequiredString(request.TemplateJSON, "templateJson"); err != nil {
		return 0, err
	}
	if request.Status == nil {
		return 0, common.NewBusinessError(400000, "status is required")
	}
	if err := s.ensurePrintCodeAvailable(request.Code, &id); err != nil && !create {
		return 0, err
	}
	if create {
		if err := s.ensurePrintCodeAvailable(request.Code, nil); err != nil {
			return 0, err
		}
		row := SysPrintTemplate{ID: common.NewID(), Code: request.Code, Name: request.Name, PaperType: request.PaperType, TemplateJSON: request.TemplateJSON, Status: *request.Status}
		return row.ID, s.db.Create(&row).Error
	}
	return id, s.db.Model(&SysPrintTemplate{}).Where("id = ? and deleted = 0", id.Int64()).Updates(map[string]any{
		"code":          request.Code,
		"name":          request.Name,
		"paper_type":    request.PaperType,
		"template_json": request.TemplateJSON,
		"status":        *request.Status,
	}).Error
}

func (s *Server) ensurePrintCodeAvailable(code string, currentID *common.Int64String) error {
	query := s.db.Model(&SysPrintTemplate{}).Where("code = ? and deleted = 0", code)
	if currentID != nil && *currentID != 0 {
		query = query.Where("id <> ?", currentID.Int64())
	}
	var count int64
	if err := query.Count(&count).Error; err != nil {
		return err
	}
	if count > 0 {
		return common.NewBusinessError(400000, "print template code already exists")
	}
	return nil
}
