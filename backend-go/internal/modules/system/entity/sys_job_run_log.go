package entity

import (
	"drip-admin/backend-go/internal/common"
	"time"
)

type SysJobRunLog struct {
	ID           common.Int64String `gorm:"column:id;primaryKey" json:"id"`
	JobID        common.Int64String `gorm:"column:job_id" json:"jobId"`
	JobName      string             `gorm:"column:job_name" json:"jobName"`
	Status       string             `gorm:"column:status" json:"status"`
	StartedAt    *time.Time         `gorm:"column:started_at" json:"startedAt"`
	FinishedAt   *time.Time         `gorm:"column:finished_at" json:"finishedAt"`
	CostMs       common.Int64String `gorm:"column:cost_ms" json:"costMs"`
	ErrorMessage *string            `gorm:"column:error_message" json:"errorMessage"`
}

func (SysJobRunLog) TableName() string { return "sys_job_run_log" }
