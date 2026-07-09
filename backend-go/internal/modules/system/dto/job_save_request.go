package dto

type JobSaveRequest struct {
	JobName        string `json:"jobName"`
	CronExpression string `json:"cronExpression"`
	ExecutorType   string `json:"executorType"`
	ScriptFile     string `json:"scriptFile"`
	ScriptArgs     string `json:"scriptArgs"`
	ClassName      string `json:"className"`
	MethodName     string `json:"methodName"`
	Status         *int   `json:"status"`
	Remark         string `json:"remark"`
}
