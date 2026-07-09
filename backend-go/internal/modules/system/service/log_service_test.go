package service

import (
	"encoding/json"
	"testing"

	"drip-admin/backend-go/internal/common"
	"github.com/stretchr/testify/require"
)

func TestOperationLogVoUsesJavaContractFields(t *testing.T) {
	operator := "admin"
	row := SysOperationLog{
		ID:             common.Int64String(1),
		OperatorID:     common.IDPtr(common.Int64String(2)),
		OperatorName:   &operator,
		Module:         "system",
		Action:         "update",
		Method:         "PUT",
		Path:           "/api/system/config/1",
		ResponseStatus: "SUCCESS",
		CostMs:         common.Int64String(12),
	}

	payload, err := json.Marshal(toOperationLogVo(row))
	require.NoError(t, err)
	require.JSONEq(t, `{"id":"1","operatorId":"2","operator":"admin","module":"system","action":"update","method":"PUT","path":"/api/system/config/1","requestParams":null,"status":"SUCCESS","errorMessage":null,"duration":"12","createdAt":null}`, string(payload))
	require.NotContains(t, string(payload), "operatorName")
	require.NotContains(t, string(payload), "responseStatus")
	require.NotContains(t, string(payload), "costMs")
}
