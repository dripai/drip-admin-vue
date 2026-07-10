use crate::common::AppError;
use std::path::{Path, PathBuf};
use tokio::fs;
use uuid::Uuid;

#[derive(Debug, Clone)]
pub struct FileStorage {
    upload_dir: PathBuf,
    public_base_url: String,
}

impl FileStorage {
    pub fn new(upload_dir: impl Into<PathBuf>, public_base_url: impl Into<String>) -> Self {
        Self {
            upload_dir: upload_dir.into(),
            public_base_url: public_base_url.into(),
        }
    }

    pub async fn save(&self, original_name: &str, bytes: &[u8]) -> Result<String, AppError> {
        fs::create_dir_all(&self.upload_dir)
            .await
            .map_err(|err| AppError::system(err.to_string()))?;
        let extension = Path::new(original_name)
            .extension()
            .and_then(|value| value.to_str())
            .unwrap_or("");
        let filename = if extension.is_empty() {
            Uuid::new_v4().to_string()
        } else {
            format!("{}.{}", Uuid::new_v4(), extension)
        };
        let path = self.upload_dir.join(&filename);
        fs::write(path, bytes)
            .await
            .map_err(|err| AppError::system(err.to_string()))?;
        Ok(format!(
            "{}/{}",
            self.public_base_url.trim_end_matches('/'),
            filename
        ))
    }
}
