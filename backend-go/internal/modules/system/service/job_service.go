package service

import (
	"context"
	"os"
	"os/exec"
	"path/filepath"
	"sort"
	"strings"
	"time"

	"drip-admin/backend-go/internal/common"
	"github.com/gin-gonic/gin"
)

func (s *Server) Jobs(c *gin.Context) {
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
	query := s.db.Model(&SysJob{}).Where("deleted = 0")
	query = likeIfPresent(query, "job_name", c.Query("jobName"))
	query = likeIfPresent(query, "remark", c.Query("remark"))
	query = eqIfPresent(query, "status", status)
	query = likeIfPresent(query, "created_at", c.Query("createdAt")).Order("created_at desc")
	var rows []SysJob
	total, err := s.pageResult(query, page, &rows)
	successOrError(c, common.PageResult[SysJob]{List: rows, Total: total, Page: page.Page, PageSize: page.PageSize}, err)
}

func (s *Server) Job(c *gin.Context) {
	id, err := idParam(c)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	var row SysJob
	if err := s.db.Where("id = ? and deleted = 0", id.Int64()).First(&row).Error; err != nil {
		common.HandleError(c, common.NotFound("operation failed"))
		return
	}
	common.Success(c, row)
}

func (s *Server) JobScripts(c *gin.Context) {
	extensions, err := scriptExtensions(c.Query("executorType"))
	if err != nil {
		common.HandleError(c, err)
		return
	}
	entries, err := os.ReadDir(s.cfg.Job.ScriptDir)
	if err != nil {
		common.Success(c, []string{})
		return
	}
	files := []string{}
	for _, entry := range entries {
		if entry.IsDir() {
			continue
		}
		ext := strings.ToLower(filepath.Ext(entry.Name()))
		if extensions[ext] {
			files = append(files, entry.Name())
		}
	}
	sort.Strings(files)
	common.Success(c, files)
}

func (s *Server) CreateJob(c *gin.Context) {
	var request JobSaveRequest
	if err := common.BindJSON(c, &request); err != nil {
		common.HandleError(c, err)
		return
	}
	id, err := s.saveJob(0, request, true)
	successOrError(c, id, err)
}

func (s *Server) UpdateJob(c *gin.Context) {
	id, err := idParam(c)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	var request JobSaveRequest
	if err := common.BindJSON(c, &request); err != nil {
		common.HandleError(c, err)
		return
	}
	_, err = s.saveJob(id, request, false)
	successOrError(c, nil, err)
}

func (s *Server) DeleteJob(c *gin.Context) {
	id, err := idParam(c)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	successOrError(c, nil, s.db.Model(&SysJob{}).Where("id = ?", id.Int64()).Update("deleted", 1).Error)
}

func (s *Server) JobStatus(c *gin.Context) {
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
	successOrError(c, nil, s.db.Model(&SysJob{}).Where("id = ? and deleted = 0", id.Int64()).Update("status", status).Error)
}

func (s *Server) RunJob(c *gin.Context) {
	id, err := idParam(c)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	var job SysJob
	if err := s.db.Where("id = ? and deleted = 0", id.Int64()).First(&job).Error; err != nil {
		common.HandleError(c, common.NotFound("operation failed"))
		return
	}
	go s.executeJob(job)
	common.Success(c, nil)
}

func (s *Server) JobLogs(c *gin.Context) {
	id, err := idParam(c)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	s.jobRunLogPage(c, &id)
}

func (s *Server) JobRunLogs(c *gin.Context) {
	s.jobRunLogPage(c, nil)
}

func (s *Server) jobRunLogPage(c *gin.Context, jobID *common.Int64String) {
	page, err := common.ParsePage(c)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	query := s.db.Model(&SysJobRunLog{})
	if jobID != nil {
		query = query.Where("job_id = ?", jobID.Int64())
	}
	query = likeIfPresent(query, "job_name", c.Query("jobName"))
	query = likeIfPresent(query, "status", c.Query("status")).Order("started_at desc")
	var rows []SysJobRunLog
	total, err := s.pageResult(query, page, &rows)
	successOrError(c, common.PageResult[SysJobRunLog]{List: rows, Total: total, Page: page.Page, PageSize: page.PageSize}, err)
}

func (s *Server) saveJob(id common.Int64String, request JobSaveRequest, create bool) (common.Int64String, error) {
	if err := validateJob(request); err != nil {
		return 0, err
	}
	if create {
		row := SysJob{ID: common.NewID(), JobName: request.JobName, CronExpression: request.CronExpression, ExecutorType: request.ExecutorType, ScriptFile: optionalString(request.ScriptFile), ScriptArgs: optionalString(request.ScriptArgs), ClassName: optionalString(request.ClassName), MethodName: optionalString(request.MethodName), Status: intOrDefault(request.Status, 1), Remark: optionalString(request.Remark)}
		return row.ID, s.db.Create(&row).Error
	}
	return id, s.db.Model(&SysJob{}).Where("id = ? and deleted = 0", id.Int64()).Updates(map[string]any{
		"job_name":        request.JobName,
		"cron_expression": request.CronExpression,
		"executor_type":   request.ExecutorType,
		"script_file":     optionalString(request.ScriptFile),
		"script_args":     optionalString(request.ScriptArgs),
		"class_name":      optionalString(request.ClassName),
		"method_name":     optionalString(request.MethodName),
		"status":          intOrDefault(request.Status, 1),
		"remark":          optionalString(request.Remark),
	}).Error
}

func validateJob(request JobSaveRequest) error {
	if err := common.RequiredString(request.JobName, "jobName"); err != nil {
		return err
	}
	if err := common.RequiredString(request.CronExpression, "cronExpression"); err != nil {
		return err
	}
	if len(strings.Fields(request.CronExpression)) < 5 || len(request.CronExpression) > 64 {
		return common.NewBusinessError(400000, "cronExpression format is invalid")
	}
	if err := common.RequiredString(request.ExecutorType, "executorType"); err != nil {
		return err
	}
	if strings.EqualFold(request.ExecutorType, "java") {
		if err := common.RequiredString(request.ClassName, "className"); err != nil {
			return err
		}
		return common.RequiredString(request.MethodName, "methodName")
	}
	return common.RequiredString(request.ScriptFile, "scriptFile")
}

func scriptExtensions(executorType string) (map[string]bool, error) {
	switch strings.ToLower(strings.TrimSpace(executorType)) {
	case "shell":
		return map[string]bool{".sh": true}, nil
	case "bat":
		return map[string]bool{".bat": true, ".cmd": true}, nil
	case "powershell", "ps1":
		return map[string]bool{".ps1": true}, nil
	case "python":
		return map[string]bool{".py": true}, nil
	default:
		return nil, common.NewBusinessError(400000, "executorType is not supported")
	}
}

func (s *Server) executeJob(job SysJob) {
	started := time.Now()
	runLog := SysJobRunLog{ID: common.NewID(), JobID: job.ID, JobName: job.JobName, Status: "RUNNING", StartedAt: &started}
	_ = s.db.Create(&runLog).Error
	err := s.executeJobTarget(job)
	finished := time.Now()
	status := "SUCCESS"
	var message *string
	if err != nil {
		status = "FAIL"
		text := err.Error()
		if len(text) > 512 {
			text = text[:512]
		}
		message = &text
	}
	_ = s.db.Model(&SysJobRunLog{}).Where("id = ?", runLog.ID.Int64()).Updates(map[string]any{
		"status":        status,
		"finished_at":   finished,
		"cost_ms":       common.Int64String(finished.Sub(started).Milliseconds()),
		"error_message": message,
	}).Error
}

func (s *Server) executeJobTarget(job SysJob) error {
	executorType := strings.ToLower(strings.TrimSpace(job.ExecutorType))
	if executorType == "java" {
		return nil
	}
	if job.ScriptFile == nil || strings.TrimSpace(*job.ScriptFile) == "" {
		return common.NewBusinessError(400000, "scriptFile is required")
	}
	script := filepath.Clean(filepath.Join(s.cfg.Job.ScriptDir, *job.ScriptFile))
	root, _ := filepath.Abs(s.cfg.Job.ScriptDir)
	full, _ := filepath.Abs(script)
	if !strings.HasPrefix(full, root) {
		return common.NewBusinessError(400000, "script path is not allowed")
	}
	command, err := jobCommand(executorType, full, common.StringValue(job.ScriptArgs))
	if err != nil {
		return err
	}
	ctx, cancel := context.WithTimeout(context.Background(), 30*time.Minute)
	defer cancel()
	return exec.CommandContext(ctx, command[0], command[1:]...).Run()
}

func jobCommand(executorType string, script string, args string) ([]string, error) {
	command := []string{}
	switch executorType {
	case "shell":
		command = append(command, "bash", script)
	case "bat":
		command = append(command, "cmd.exe", "/c", script)
	case "powershell", "ps1":
		command = append(command, "powershell.exe", "-ExecutionPolicy", "Bypass", "-File", script)
	case "python":
		command = append(command, "python", script)
	default:
		return nil, common.NewBusinessError(400000, "executorType is not supported")
	}
	command = append(command, strings.Fields(args)...)
	return command, nil
}
