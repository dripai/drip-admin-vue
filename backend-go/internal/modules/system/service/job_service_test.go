package service

import (
	"testing"

	"drip-admin/backend-go/internal/common"
	"github.com/stretchr/testify/require"
)

func TestValidateJobRejectsJavaExecutor(t *testing.T) {
	err := validateJob(JobSaveRequest{
		JobName:        "Java Job",
		CronExpression: "0 0 * * *",
		ExecutorType:   "java",
		ClassName:      "com.example.Job",
		MethodName:     "run",
	})

	require.Error(t, err)
	var business common.BusinessError
	require.ErrorAs(t, err, &business)
	require.Equal(t, 400000, business.Code)
	require.Equal(t, "executorType is not supported", business.Message)
}

func TestValidateJobAcceptsScriptExecutors(t *testing.T) {
	for _, executorType := range []string{"python", "shell", "bat", "powershell", "ps1"} {
		err := validateJob(JobSaveRequest{
			JobName:        executorType + " Job",
			CronExpression: "0 0 * * *",
			ExecutorType:   executorType,
			ScriptFile:     "job.py",
		})

		require.NoError(t, err, executorType)
	}
}

func TestExecuteJobTargetRejectsExistingJavaJob(t *testing.T) {
	err := NewServer(configForTest(), nil, nil, nil).executeJobTarget(SysJob{
		ExecutorType: "java",
		ClassName:    common.OptionalStringPtr("com.example.Job"),
		MethodName:   common.OptionalStringPtr("run"),
	})

	require.Error(t, err)
	var business common.BusinessError
	require.ErrorAs(t, err, &business)
	require.Equal(t, "executorType is not supported", business.Message)
}
