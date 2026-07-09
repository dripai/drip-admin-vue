package service

import (
	"drip-admin/backend-go/internal/common"
	"github.com/gin-gonic/gin"
)

func (s *Server) Depts(c *gin.Context) {
	var rows []SysDept
	if err := s.db.Where("deleted = 0").Order("sort asc, id asc").Find(&rows).Error; err != nil {
		common.HandleError(c, err)
		return
	}
	common.Success(c, buildDeptTree(rows))
}

func (s *Server) Dept(c *gin.Context) {
	id, err := idParam(c)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	var row SysDept
	if err := s.db.Where("id = ? and deleted = 0", id.Int64()).First(&row).Error; err != nil {
		common.HandleError(c, common.NotFound("operation failed"))
		return
	}
	common.Success(c, row)
}

func (s *Server) CreateDept(c *gin.Context) {
	var request DeptSaveRequest
	if err := common.BindJSON(c, &request); err != nil {
		common.HandleError(c, err)
		return
	}
	id, err := s.saveDept(0, request, true)
	successOrError(c, id, err)
}

func (s *Server) UpdateDept(c *gin.Context) {
	id, err := idParam(c)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	var request DeptSaveRequest
	if err := common.BindJSON(c, &request); err != nil {
		common.HandleError(c, err)
		return
	}
	_, err = s.saveDept(id, request, false)
	successOrError(c, nil, err)
}

func (s *Server) DeleteDept(c *gin.Context) {
	id, err := idParam(c)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	var childCount int64
	if err := s.db.Model(&SysDept{}).Where("parent_id = ? and deleted = 0", id.Int64()).Count(&childCount).Error; err != nil {
		common.HandleError(c, err)
		return
	}
	if childCount > 0 {
		common.HandleError(c, common.NewBusinessError(400401, "存在子部门，不能删除"))
		return
	}
	var userCount int64
	if err := s.db.Model(&SysUser{}).Where("dept_id = ? and deleted = 0", id.Int64()).Count(&userCount).Error; err != nil {
		common.HandleError(c, err)
		return
	}
	if userCount > 0 {
		common.HandleError(c, common.NewBusinessError(400401, "部门下存在用户，不能删除"))
		return
	}
	successOrError(c, nil, s.db.Model(&SysDept{}).Where("id = ?", id.Int64()).Update("deleted", 1).Error)
}

func (s *Server) DeptStatus(c *gin.Context) {
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
	successOrError(c, nil, s.db.Model(&SysDept{}).Where("id = ? and deleted = 0", id.Int64()).Update("status", status).Error)
}

func (s *Server) saveDept(id common.Int64String, request DeptSaveRequest, create bool) (common.Int64String, error) {
	if err := common.RequiredString(request.DeptName, "deptName"); err != nil {
		return 0, err
	}
	if err := common.RequiredString(request.DeptCode, "deptCode"); err != nil {
		return 0, err
	}
	parentID := idOrZero(request.ParentID)
	if !create && (parentID == id || s.isDeptDescendant(id, parentID)) {
		return 0, common.NewBusinessError(400000, "上级部门不能选择自己或子部门")
	}
	if parentID != 0 {
		var count int64
		if err := s.db.Model(&SysDept{}).Where("id = ? and deleted = 0", parentID.Int64()).Count(&count).Error; err != nil {
			return 0, err
		}
		if count == 0 {
			return 0, common.NotFound("operation failed")
		}
	}
	var duplicate int64
	dupQuery := s.db.Model(&SysDept{}).Where("dept_code = ? and deleted = 0", request.DeptCode)
	if !create {
		dupQuery = dupQuery.Where("id <> ?", id.Int64())
	}
	if err := dupQuery.Count(&duplicate).Error; err != nil {
		return 0, err
	}
	if duplicate > 0 {
		return 0, common.NewBusinessError(400000, "部门编码已存在")
	}
	if request.LeaderUserID != nil {
		var count int64
		if err := s.db.Model(&SysUser{}).Where("id = ? and deleted = 0", request.LeaderUserID.Int64()).Count(&count).Error; err != nil {
			return 0, err
		}
		if count == 0 {
			return 0, common.NewBusinessError(400000, "负责人用户不存在")
		}
	}
	if create {
		row := SysDept{ID: common.NewID(), ParentID: parentID, DeptName: request.DeptName, DeptCode: request.DeptCode, LeaderUserID: request.LeaderUserID, Sort: intOrDefault(request.Sort, 0), Status: intOrDefault(request.Status, 1)}
		return row.ID, s.db.Create(&row).Error
	}
	return id, s.db.Model(&SysDept{}).Where("id = ? and deleted = 0", id.Int64()).Updates(map[string]any{
		"parent_id":      parentID,
		"dept_name":      request.DeptName,
		"dept_code":      request.DeptCode,
		"leader_user_id": request.LeaderUserID,
		"sort":           intOrDefault(request.Sort, 0),
		"status":         intOrDefault(request.Status, 1),
	}).Error
}

func (s *Server) isDeptDescendant(id common.Int64String, maybeChild common.Int64String) bool {
	if maybeChild == 0 {
		return false
	}
	var rows []SysDept
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

func buildDeptTree(rows []SysDept) []DeptTreeVo {
	byID := map[int64]*DeptTreeVo{}
	order := make([]common.Int64String, 0, len(rows))
	for _, row := range rows {
		vo := DeptTreeVo{ID: row.ID, ParentID: row.ParentID, DeptName: row.DeptName, DeptCode: row.DeptCode, LeaderUserID: row.LeaderUserID, Sort: row.Sort, Status: row.Status, CreatedAt: row.CreatedAt, UpdatedAt: row.UpdatedAt, Children: []DeptTreeVo{}}
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
	roots := []DeptTreeVo{}
	for _, id := range order {
		row := byID[id.Int64()]
		if row.ParentID == 0 || byID[row.ParentID.Int64()] == nil {
			roots = append(roots, *row)
		}
	}
	return roots
}
