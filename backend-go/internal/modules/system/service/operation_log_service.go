package service

import (
	"bytes"
	"encoding/json"
	"io"
	"net/http"
	"net/url"
	"regexp"
	"strings"
	"time"

	"drip-admin/backend-go/internal/common"
	"github.com/gin-gonic/gin"
	"go.uber.org/zap"
)

const operationLogTextLimit = 8192

var rawSensitivePattern = regexp.MustCompile(`(?i)(password|token|secret|authorization)(["']?\s*[:=]\s*["']?)[^,"'}\]\s]+`)

type operationLogDefinition struct {
	Module string
	Action string
}

var operationLogDefinitions = map[string]operationLogDefinition{
	operationLogKey("PUT", "/api/system/profile"):                      {Module: "个人中心", Action: "编辑资料"},
	operationLogKey("POST", "/api/system/user"):                        {Module: "用户管理", Action: "新增用户"},
	operationLogKey("PUT", "/api/system/user/:id"):                     {Module: "用户管理", Action: "编辑用户"},
	operationLogKey("DELETE", "/api/system/user/:id"):                  {Module: "用户管理", Action: "删除用户"},
	operationLogKey("PUT", "/api/system/user/:id/status"):              {Module: "用户管理", Action: "变更用户状态"},
	operationLogKey("POST", "/api/system/user/:id/unlock"):             {Module: "用户管理", Action: "解除登录锁定"},
	operationLogKey("PUT", "/api/system/user/:id/role"):                {Module: "用户管理", Action: "分配角色"},
	operationLogKey("POST", "/api/system/user/:id/resetPassword"):      {Module: "用户管理", Action: "重置密码"},
	operationLogKey("POST", "/api/system/role"):                        {Module: "角色管理", Action: "新增角色"},
	operationLogKey("PUT", "/api/system/role/:id"):                     {Module: "角色管理", Action: "编辑角色"},
	operationLogKey("DELETE", "/api/system/role/:id"):                  {Module: "角色管理", Action: "删除角色"},
	operationLogKey("PUT", "/api/system/role/:id/status"):              {Module: "角色管理", Action: "变更角色状态"},
	operationLogKey("PUT", "/api/system/role/:id/permission"):          {Module: "角色管理", Action: "角色授权"},
	operationLogKey("POST", "/api/system/menu"):                        {Module: "菜单管理", Action: "新增菜单"},
	operationLogKey("PUT", "/api/system/menu/:id"):                     {Module: "菜单管理", Action: "编辑菜单"},
	operationLogKey("DELETE", "/api/system/menu/:id"):                  {Module: "菜单管理", Action: "删除菜单"},
	operationLogKey("PUT", "/api/system/menu/:id/status"):              {Module: "菜单管理", Action: "变更菜单状态"},
	operationLogKey("POST", "/api/system/dept"):                        {Module: "部门管理", Action: "新增部门"},
	operationLogKey("PUT", "/api/system/dept/:id"):                     {Module: "部门管理", Action: "编辑部门"},
	operationLogKey("DELETE", "/api/system/dept/:id"):                  {Module: "部门管理", Action: "删除部门"},
	operationLogKey("PUT", "/api/system/dept/:id/status"):              {Module: "部门管理", Action: "变更部门状态"},
	operationLogKey("POST", "/api/system/config"):                      {Module: "系统配置", Action: "新增配置"},
	operationLogKey("PUT", "/api/system/config/:id"):                   {Module: "系统配置", Action: "编辑配置"},
	operationLogKey("DELETE", "/api/system/config/:id"):                {Module: "系统配置", Action: "删除配置"},
	operationLogKey("PUT", "/api/system/config/:id/status"):            {Module: "系统配置", Action: "变更配置状态"},
	operationLogKey("POST", "/api/system/dict/type"):                   {Module: "字典管理", Action: "新增字典类型"},
	operationLogKey("PUT", "/api/system/dict/type/:id"):                {Module: "字典管理", Action: "编辑字典类型"},
	operationLogKey("DELETE", "/api/system/dict/type/:id"):             {Module: "字典管理", Action: "删除字典类型"},
	operationLogKey("POST", "/api/system/dict/item"):                   {Module: "字典管理", Action: "新增字典项"},
	operationLogKey("PUT", "/api/system/dict/item/:id"):                {Module: "字典管理", Action: "编辑字典项"},
	operationLogKey("DELETE", "/api/system/dict/item/:id"):             {Module: "字典管理", Action: "删除字典项"},
	operationLogKey("PUT", "/api/system/dict/item/:id/status"):         {Module: "字典管理", Action: "变更字典项状态"},
	operationLogKey("POST", "/api/system/onlineUser/:tokenId/kickout"): {Module: "在线用户", Action: "强制下线"},
	operationLogKey("POST", "/api/system/job"):                         {Module: "定时任务", Action: "新增任务"},
	operationLogKey("PUT", "/api/system/job/:id"):                      {Module: "定时任务", Action: "编辑任务"},
	operationLogKey("DELETE", "/api/system/job/:id"):                   {Module: "定时任务", Action: "删除任务"},
	operationLogKey("PUT", "/api/system/job/:id/status"):               {Module: "定时任务", Action: "变更任务状态"},
	operationLogKey("POST", "/api/system/job/:id/run"):                 {Module: "定时任务", Action: "手动执行任务"},
	operationLogKey("POST", "/api/system/files"):                       {Module: "文件上传", Action: "上传文件"},
	operationLogKey("POST", "/api/system/print-template"):              {Module: "打印模板", Action: "新增打印模板"},
	operationLogKey("POST", "/api/system/print-template/:id/copy"):     {Module: "打印模板", Action: "复制打印模板"},
	operationLogKey("PUT", "/api/system/print-template/:id"):           {Module: "打印模板", Action: "编辑打印模板"},
	operationLogKey("DELETE", "/api/system/print-template/:id"):        {Module: "打印模板", Action: "删除打印模板"},
	operationLogKey("PUT", "/api/system/print-template/:id/status"):    {Module: "打印模板", Action: "变更打印模板状态"},
}

func (s *Server) OperationLogMiddleware() gin.HandlerFunc {
	return func(c *gin.Context) {
		definition, ok := operationLogDefinitionFor(c.Request.Method, c.FullPath())
		if !ok {
			c.Next()
			return
		}

		started := time.Now()
		requestParams := s.operationRequestParams(c)
		c.Next()

		status := "SUCCESS"
		var errorMessage *string
		if c.Writer.Status() >= http.StatusBadRequest || len(c.Errors) > 0 {
			status = "FAIL"
			errorMessage = operationErrorMessage(c)
		}

		session, ok := currentSession(c)
		var operatorID *common.Int64String
		var operatorName *string
		if ok {
			operatorID = common.IDPtr(session.UserID)
			name := session.RealName
			if strings.TrimSpace(name) == "" {
				name = session.Username
			}
			if strings.TrimSpace(name) != "" {
				operatorName = &name
			}
		}

		row := SysOperationLog{
			ID:             common.NewID(),
			OperatorID:     operatorID,
			OperatorName:   operatorName,
			Module:         definition.Module,
			Action:         definition.Action,
			Method:         c.Request.Method,
			Path:           c.Request.URL.Path,
			RequestParams:  requestParams,
			ResponseStatus: status,
			ErrorMessage:   errorMessage,
			CostMs:         common.Int64String(time.Since(started).Milliseconds()),
		}
		if s.db == nil {
			s.logger.Error("operation log write failed", zap.String("reason", "database is not configured"), zap.String("module", row.Module), zap.String("action", row.Action), zap.String("path", row.Path))
			return
		}
		if err := s.db.Create(&row).Error; err != nil {
			s.logger.Error("operation log write failed", zap.String("module", row.Module), zap.String("action", row.Action), zap.String("path", row.Path), zap.Error(err))
		}
	}
}

func operationLogKey(method string, path string) string {
	return method + " " + path
}

func operationLogDefinitionFor(method string, path string) (operationLogDefinition, bool) {
	definition, ok := operationLogDefinitions[operationLogKey(method, path)]
	return definition, ok
}

func (s *Server) operationRequestParams(c *gin.Context) *string {
	params := map[string]any{}
	if len(c.Params) > 0 {
		pathParams := map[string]string{}
		for _, item := range c.Params {
			pathParams[item.Key] = item.Value
		}
		params["path"] = pathParams
	}
	if c.Request.URL.RawQuery != "" {
		params["query"] = maskSensitive(c.Request.URL.Query())
	}
	if shouldCaptureBody(c) {
		body, err := readAndRestoreRequestBody(c)
		if err != nil {
			s.logger.Warn("operation log request body read failed", zap.String("path", c.Request.URL.Path), zap.Error(err))
		} else if strings.TrimSpace(body) != "" {
			params["body"] = parseAndMaskBody(body)
		}
	} else if strings.HasPrefix(c.ContentType(), "multipart/") {
		params["contentType"] = c.ContentType()
	}
	if len(params) == 0 {
		return nil
	}
	payload, err := json.Marshal(params)
	if err != nil {
		s.logger.Warn("operation log request params marshal failed", zap.String("path", c.Request.URL.Path), zap.Error(err))
		return nil
	}
	text := limitOperationLogText(string(payload))
	return &text
}

func shouldCaptureBody(c *gin.Context) bool {
	if c.Request.Body == nil {
		return false
	}
	contentType := c.ContentType()
	if contentType == "" {
		return false
	}
	return !strings.HasPrefix(contentType, "multipart/")
}

func readAndRestoreRequestBody(c *gin.Context) (string, error) {
	body, err := io.ReadAll(c.Request.Body)
	if err != nil {
		return "", err
	}
	c.Request.Body = io.NopCloser(bytes.NewReader(body))
	return string(body), nil
}

func parseAndMaskBody(body string) any {
	var parsed any
	if err := json.Unmarshal([]byte(body), &parsed); err == nil {
		return maskSensitive(parsed)
	}
	return rawSensitivePattern.ReplaceAllString(limitOperationLogText(body), `$1$2******`)
}

func maskSensitive(value any) any {
	switch typed := value.(type) {
	case map[string]any:
		out := map[string]any{}
		for key, item := range typed {
			if isSensitiveKey(key) {
				out[key] = "******"
				continue
			}
			out[key] = maskSensitive(item)
		}
		return out
	case map[string][]string:
		out := map[string][]string{}
		for key, item := range typed {
			if isSensitiveKey(key) {
				out[key] = []string{"******"}
				continue
			}
			out[key] = item
		}
		return out
	case url.Values:
		out := url.Values{}
		for key, item := range typed {
			if isSensitiveKey(key) {
				out[key] = []string{"******"}
				continue
			}
			out[key] = item
		}
		return out
	case []any:
		out := make([]any, 0, len(typed))
		for _, item := range typed {
			out = append(out, maskSensitive(item))
		}
		return out
	default:
		return value
	}
}

func isSensitiveKey(key string) bool {
	normalized := strings.ToLower(key)
	return strings.Contains(normalized, "password") ||
		strings.Contains(normalized, "token") ||
		strings.Contains(normalized, "secret") ||
		strings.Contains(normalized, "authorization")
}

func operationErrorMessage(c *gin.Context) *string {
	if len(c.Errors) > 0 {
		message := limitOperationLogText(c.Errors.Last().Error())
		return &message
	}
	message := http.StatusText(c.Writer.Status())
	if strings.TrimSpace(message) == "" {
		return nil
	}
	message = limitOperationLogText(message)
	return &message
}

func limitOperationLogText(value string) string {
	if len(value) <= operationLogTextLimit {
		return value
	}
	end := 0
	for index := range value {
		if index > operationLogTextLimit {
			break
		}
		end = index
	}
	if end == 0 {
		return ""
	}
	return value[:end]
}
