use drip_admin_rust::config::Settings;
use drip_admin_rust::infrastructure::{database, logging, redis_client};
use drip_admin_rust::modules::system::{AppState, router};
use std::net::SocketAddr;
use std::sync::Arc;

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    let settings = Settings::load()?;
    let _log_guard = logging::init(&settings.logging.level);
    let database = database::connect_mysql(&settings.mysql).await?;
    let redis_pool = redis_client::create_pool(&settings.redis)?;
    let state = AppState::new(
        Arc::new(settings.clone()),
        Some(Arc::new(database)),
        Some(redis_pool),
    );
    let app = router::create_router(state);
    let addr: SocketAddr = format!("{}:{}", settings.server.host, settings.server.port).parse()?;
    tracing::info!("drip-admin-rust listening on {}", addr);
    let listener = tokio::net::TcpListener::bind(addr).await?;
    axum::serve(listener, app).await?;
    Ok(())
}
