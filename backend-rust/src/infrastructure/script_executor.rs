use crate::common::AppError;
use std::path::{Path, PathBuf};
use tokio::fs;

#[derive(Debug, Clone)]
pub struct ScriptExecutor {
    script_dir: PathBuf,
}

impl ScriptExecutor {
    pub fn new(script_dir: impl Into<PathBuf>) -> Self {
        Self {
            script_dir: script_dir.into(),
        }
    }

    pub async fn list_scripts(&self, script_type: &str) -> Result<Vec<String>, AppError> {
        let extension = match script_type {
            "python" => "py",
            "shell" => "sh",
            "powershell" => "ps1",
            _ => return Err(AppError::bad_request("unsupported script type")),
        };
        if !Path::new(&self.script_dir).exists() {
            return Ok(Vec::new());
        }
        let mut entries = fs::read_dir(&self.script_dir)
            .await
            .map_err(|err| AppError::system(err.to_string()))?;
        let mut scripts = Vec::new();
        while let Some(entry) = entries
            .next_entry()
            .await
            .map_err(|err| AppError::system(err.to_string()))?
        {
            let path = entry.path();
            if path.extension().and_then(|value| value.to_str()) == Some(extension) {
                if let Some(name) = path.file_name().and_then(|value| value.to_str()) {
                    scripts.push(name.to_string());
                }
            }
        }
        scripts.sort();
        Ok(scripts)
    }
}
