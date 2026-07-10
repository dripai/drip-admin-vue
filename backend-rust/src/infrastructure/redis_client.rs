use crate::config::RedisSettings;
use deadpool_redis::{Config, Pool, Runtime};

pub fn create_pool(settings: &RedisSettings) -> Result<Pool, deadpool_redis::CreatePoolError> {
    let config = Config::from_url(settings.url());
    config.create_pool(Some(Runtime::Tokio1))
}
