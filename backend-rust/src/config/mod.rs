use config::{Config, File};
use serde::Deserialize;
use std::env;
use std::path::Path;

#[derive(Debug, Clone, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct Settings {
    pub server: ServerSettings,
    pub mysql: MysqlSettings,
    pub redis: RedisSettings,
    pub token: TokenSettings,
    pub job: JobSettings,
    pub file: FileSettings,
}

#[derive(Debug, Clone, Deserialize)]
pub struct ServerSettings {
    pub host: String,
    pub port: u16,
}

#[derive(Debug, Clone, Deserialize)]
pub struct MysqlSettings {
    pub host: String,
    pub port: u16,
    pub database: String,
    pub username: String,
    pub password: String,
}

#[derive(Debug, Clone, Deserialize)]
pub struct RedisSettings {
    pub host: String,
    pub port: u16,
    pub database: i64,
}

#[derive(Debug, Clone, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct TokenSettings {
    pub name: String,
    pub timeout_seconds: i64,
    pub active_timeout_seconds: i64,
}

#[derive(Debug, Clone, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct JobSettings {
    pub script_dir: String,
}

#[derive(Debug, Clone, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct FileSettings {
    pub upload_dir: String,
    pub public_base_url: String,
}

impl Settings {
    pub fn load() -> Result<Self, config::ConfigError> {
        let path = env::var("DRIP_RUST_CONFIG").unwrap_or_else(|_| "config.yaml".to_string());
        if Path::new(&path).exists() {
            Config::builder()
                .add_source(File::with_name(&path))
                .build()?
                .try_deserialize()
        } else {
            Ok(Self::default())
        }
    }
}

impl MysqlSettings {
    pub fn url(&self) -> String {
        format!(
            "mysql://{}:{}@{}:{}/{}",
            self.username, self.password, self.host, self.port, self.database
        )
    }
}

impl RedisSettings {
    pub fn url(&self) -> String {
        format!("redis://{}:{}/{}", self.host, self.port, self.database)
    }
}

impl Default for Settings {
    fn default() -> Self {
        Self {
            server: ServerSettings {
                host: "0.0.0.0".to_string(),
                port: 9001,
            },
            mysql: MysqlSettings {
                host: "localhost".to_string(),
                port: 3307,
                database: "drip-manager".to_string(),
                username: "root".to_string(),
                password: "root".to_string(),
            },
            redis: RedisSettings {
                host: "localhost".to_string(),
                port: 6379,
                database: 0,
            },
            token: TokenSettings {
                name: "Authorization".to_string(),
                timeout_seconds: 28800,
                active_timeout_seconds: 1800,
            },
            job: JobSettings {
                script_dir: "../scripts".to_string(),
            },
            file: FileSettings {
                upload_dir: "uploads".to_string(),
                public_base_url: "/uploads".to_string(),
            },
        }
    }
}
