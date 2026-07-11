use config::{Config, File};
use serde::Deserialize;
use std::env;

#[derive(Debug, Clone, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct Settings {
    pub server: ServerSettings,
    pub logging: LoggingSettings,
    pub mysql: MysqlSettings,
    pub redis: RedisSettings,
    pub token: TokenSettings,
    pub job: JobSettings,
    pub file: FileSettings,
}

#[derive(Debug, Clone, Deserialize)]
pub struct LoggingSettings {
    pub level: String,
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
    pub params: String,
}

#[derive(Debug, Clone, Deserialize)]
pub struct RedisSettings {
    pub host: String,
    pub port: u16,
    pub password: String,
    #[serde(rename = "db")]
    pub database: i64,
}

#[derive(Debug, Clone, Deserialize)]
#[serde(rename_all = "kebab-case")]
pub struct TokenSettings {
    pub name: String,
    pub timeout_seconds: i64,
    pub active_timeout_seconds: i64,
}

#[derive(Debug, Clone, Deserialize)]
#[serde(rename_all = "kebab-case")]
pub struct JobSettings {
    pub script_dir: String,
}

#[derive(Debug, Clone, Deserialize)]
#[serde(rename_all = "kebab-case")]
pub struct FileSettings {
    pub upload_dir: String,
    #[serde(default = "default_public_base_url")]
    pub public_base_url: String,
}

impl Settings {
    pub fn load() -> Result<Self, config::ConfigError> {
        let path = env::var("DRIP_RUST_CONFIG").unwrap_or_else(|_| "config.yaml".to_string());
        Config::builder()
            .add_source(File::with_name(&path).required(true))
            .build()?
            .try_deserialize()
    }
}

impl MysqlSettings {
    pub fn url(&self) -> String {
        let mut url = format!(
            "mysql://{}:{}@{}:{}/{}",
            self.username, self.password, self.host, self.port, self.database
        );
        if !self.params.trim().is_empty() {
            url.push('?');
            url.push_str(&self.params);
        }
        url
    }
}

impl RedisSettings {
    pub fn url(&self) -> String {
        let credential = if self.password.is_empty() {
            String::new()
        } else {
            format!(":{}@", self.password)
        };
        format!("redis://{credential}{}:{}/{}", self.host, self.port, self.database)
    }
}

impl Default for Settings {
    fn default() -> Self {
        Self {
            server: ServerSettings {
                host: "0.0.0.0".to_string(),
                port: 9001,
            },
            logging: LoggingSettings {
                level: "info".to_string(),
            },
            mysql: MysqlSettings {
                host: "localhost".to_string(),
                port: 3307,
                database: "drip-manager".to_string(),
                username: "root".to_string(),
                password: "root".to_string(),
                params: "charset=utf8mb4".to_string(),
            },
            redis: RedisSettings {
                host: "localhost".to_string(),
                port: 6379,
                password: String::new(),
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
                upload_dir: "../uploads".to_string(),
                public_base_url: default_public_base_url(),
            },
        }
    }
}

fn default_public_base_url() -> String {
    "/uploads".to_string()
}

#[cfg(test)]
mod tests {
    use super::Settings;

    #[test]
    fn config_yaml_uses_shared_backend_contract() {
        let settings = Settings::load().unwrap();
        assert_eq!(settings.logging.level, "info");
        assert_eq!(settings.redis.database, 0);
        assert_eq!(settings.token.timeout_seconds, 28800);
        assert_eq!(settings.token.active_timeout_seconds, 1800);
        assert_eq!(settings.job.script_dir, "../scripts");
        assert_eq!(settings.file.upload_dir, "../uploads");
    }
}
