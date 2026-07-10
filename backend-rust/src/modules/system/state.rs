use crate::config::Settings;
use deadpool_redis::Pool;
use rbatis::RBatis;
use std::sync::Arc;

#[derive(Clone)]
pub struct AppState {
    pub settings: Arc<Settings>,
    pub database: Option<Arc<RBatis>>,
    pub redis_pool: Option<Pool>,
}

impl AppState {
    pub fn new(
        settings: Arc<Settings>,
        database: Option<Arc<RBatis>>,
        redis_pool: Option<Pool>,
    ) -> Self {
        Self {
            settings,
            database,
            redis_pool,
        }
    }
}
