use crate::common::I64String;
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize, utoipa::ToSchema)]
#[serde(rename_all(serialize = "camelCase", deserialize = "snake_case"))]
pub struct SysConfig {
    pub id: I64String,
    pub config_name: String,
    pub config_key: String,
    pub config_value: String,
    pub value_type: String,
    pub builtin: i32,
    pub status: i32,
    pub remark: Option<String>,
    pub deleted: i32,
}

#[cfg(test)]
mod tests {
    use super::SysConfig;

    #[test]
    fn accepts_database_column_names() {
        let config: SysConfig = serde_json::from_str(
            r#"{"id":"1","config_name":"系统名称","config_key":"system.name","config_value":"Drip Admin","value_type":"string","builtin":1,"status":1,"remark":null,"deleted":0}"#,
        )
        .unwrap();
        assert_eq!(config.config_name, "系统名称");
        assert_eq!(serde_json::to_value(config).unwrap()["configName"], "系统名称");
    }
}
