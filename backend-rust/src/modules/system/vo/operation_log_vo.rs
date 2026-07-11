use crate::common::I64String;
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Deserialize, Serialize, utoipa::ToSchema)]
#[serde(rename_all(serialize = "camelCase", deserialize = "snake_case"))]
pub struct OperationLogVo {
    pub id: I64String,
    pub operator_id: Option<I64String>,
    pub operator: Option<String>,
    pub module: String,
    pub action: String,
    pub method: String,
    pub path: String,
    pub request_params: Option<String>,
    pub status: String,
    pub error_message: Option<String>,
    pub duration: I64String,
    pub created_at: Option<String>,
}

#[cfg(test)]
mod tests {
    use super::OperationLogVo;

    #[test]
    fn accepts_operation_log_query_columns() {
        let log: OperationLogVo = serde_json::from_str(
            r#"{"id":"1","operator_id":"1","operator":"管理员","module":"系统配置","action":"编辑配置","method":"PUT","path":"/api/system/config/1","request_params":null,"status":"SUCCESS","error_message":null,"duration":"12","created_at":"2026-07-11 09:22:05"}"#,
        )
        .unwrap();
        let output = serde_json::to_value(log).unwrap();
        assert_eq!(output["operator"], "管理员");
        assert_eq!(output["status"], "SUCCESS");
        assert_eq!(output["duration"], "12");
    }
}
