package service

import (
	"drip-admin/backend-go/internal/common"
	"github.com/gin-gonic/gin"
)

func (s *Server) LoginLogs(c *gin.Context) {
	page, err := common.ParsePage(c)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	query := s.db.Model(&SysLoginLog{})
	query = likeIfPresent(query, "username", c.Query("username"))
	query = likeIfPresent(query, "status", c.Query("status"))
	query = likeIfPresent(query, "login_type", c.Query("loginType"))
	query = likeIfPresent(query, "device_type", c.Query("deviceType"))
	query = likeIfPresent(query, "ip", c.Query("ip")).Order("login_at desc")
	var rows []SysLoginLog
	total, err := s.pageResult(query, page, &rows)
	successOrError(c, common.PageResult[SysLoginLog]{List: rows, Total: total, Page: page.Page, PageSize: page.PageSize}, err)
}

func (s *Server) LoginLog(c *gin.Context) {
	id, err := idParam(c)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	var row SysLoginLog
	err = s.firstByID(&row, id, "operation failed")
	successOrError(c, row, err)
}

func (s *Server) OperationLogs(c *gin.Context) {
	page, err := common.ParsePage(c)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	query := s.db.Model(&SysOperationLog{})
	query = likeIfPresent(query, "operator_name", c.Query("operator"))
	query = likeIfPresent(query, "module", c.Query("module"))
	query = likeIfPresent(query, "action", c.Query("action"))
	query = likeIfPresent(query, "response_status", c.Query("status"))
	query = likeIfPresent(query, "path", c.Query("path")).Order("created_at desc")
	var rows []SysOperationLog
	total, err := s.pageResult(query, page, &rows)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	out := make([]OperationLogVo, 0, len(rows))
	for _, row := range rows {
		out = append(out, toOperationLogVo(row))
	}
	common.Success(c, common.PageResult[OperationLogVo]{List: out, Total: total, Page: page.Page, PageSize: page.PageSize})
}

func (s *Server) OperationLog(c *gin.Context) {
	id, err := idParam(c)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	var row SysOperationLog
	if err := s.firstByID(&row, id, "operation failed"); err != nil {
		common.HandleError(c, err)
		return
	}
	common.Success(c, toOperationLogVo(row))
}

func toOperationLogVo(row SysOperationLog) OperationLogVo {
	return OperationLogVo{ID: row.ID, OperatorID: row.OperatorID, Operator: row.OperatorName, Module: row.Module, Action: row.Action, Method: row.Method, Path: row.Path, RequestParams: row.RequestParams, Status: row.ResponseStatus, ErrorMessage: row.ErrorMessage, Duration: row.CostMs, CreatedAt: row.CreatedAt}
}
