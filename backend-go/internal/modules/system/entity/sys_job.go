package entity

import (
	"drip-admin/backend-go/internal/common"
	"time"
)

type SysJob struct {
	ID             common.Int64String `gorm:"column:id;primaryKey" json:"id"`
	JobName        string             `gorm:"column:job_name" json:"jobName"`
	CronExpression string             `gorm:"column:cron_expression" json:"cronExpression"`
	ExecutorType   string             `gorm:"column:executor_type" json:"executorType"`
	ScriptFile     *string            `gorm:"column:script_file" json:"scriptFile"`
	ScriptArgs     *string            `gorm:"column:script_args" json:"scriptArgs"`
	ClassName      *string            `gorm:"column:class_name" json:"className"`
	MethodName     *string            `gorm:"column:method_name" json:"methodName"`
	Status         int                `gorm:"column:status" json:"status"`
	Remark         *string            `gorm:"column:remark" json:"remark"`
	Deleted        int                `gorm:"column:deleted" json:"deleted"`
	CreatedAt      *time.Time         `gorm:"column:created_at" json:"createdAt"`
	UpdatedAt      *time.Time         `gorm:"column:updated_at" json:"updatedAt"`
}

func (SysJob) TableName() string { return "sys_job" }
