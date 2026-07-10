use crate::common::{AppError, I64String};
use deadpool_redis::Pool;
use deadpool_redis::redis::AsyncCommands;
use serde::{Deserialize, Serialize};

const SESSION_KEY_PREFIX: &str = "drip:session:";
const USER_TOKEN_KEY_PREFIX: &str = "drip:user-token:";

#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct SessionData {
    pub user_id: I64String,
    pub username: String,
    pub real_name: String,
    pub token: String,
    pub device_type: String,
    pub login_at: String,
    pub last_active_at: String,
}

pub async fn register_session(
    pool: &Pool,
    session: &SessionData,
    timeout_seconds: i64,
    active_timeout_seconds: i64,
) -> Result<(), AppError> {
    let mut connection = pool
        .get()
        .await
        .map_err(|err| AppError::system(err.to_string()))?;
    let user_key = user_token_key(session.user_id.value());
    if let Some(old_token) = connection
        .get::<_, Option<String>>(&user_key)
        .await
        .map_err(|err| AppError::system(err.to_string()))?
    {
        let _: () = connection
            .del(session_key(&old_token))
            .await
            .map_err(|err| AppError::system(err.to_string()))?;
    }
    let payload =
        serde_json::to_string(session).map_err(|err| AppError::system(err.to_string()))?;
    let _: () = connection
        .set_ex(
            session_key(&session.token),
            payload,
            active_timeout_seconds as u64,
        )
        .await
        .map_err(|err| AppError::system(err.to_string()))?;
    let _: () = connection
        .set_ex(user_key, &session.token, timeout_seconds as u64)
        .await
        .map_err(|err| AppError::system(err.to_string()))?;
    Ok(())
}

pub async fn load_session(
    pool: &Pool,
    token: &str,
    active_timeout_seconds: i64,
) -> Result<SessionData, AppError> {
    let mut connection = pool
        .get()
        .await
        .map_err(|err| AppError::system(err.to_string()))?;
    let payload = connection
        .get::<_, Option<String>>(session_key(token))
        .await
        .map_err(|err| AppError::system(err.to_string()))?
        .ok_or_else(|| AppError::unauthorized("未登录或 token 失效"))?;
    let session: SessionData =
        serde_json::from_str(&payload).map_err(|err| AppError::system(err.to_string()))?;
    let _: () = connection
        .expire(session_key(token), active_timeout_seconds)
        .await
        .map_err(|err| AppError::system(err.to_string()))?;
    Ok(session)
}

pub async fn remove_session(pool: &Pool, token: &str, user_id: i64) -> Result<(), AppError> {
    let mut connection = pool
        .get()
        .await
        .map_err(|err| AppError::system(err.to_string()))?;
    let _: () = connection
        .del(session_key(token))
        .await
        .map_err(|err| AppError::system(err.to_string()))?;
    let _: () = connection
        .del(user_token_key(user_id))
        .await
        .map_err(|err| AppError::system(err.to_string()))?;
    Ok(())
}

fn session_key(token: &str) -> String {
    format!("{SESSION_KEY_PREFIX}{token}")
}

fn user_token_key(user_id: i64) -> String {
    format!("{USER_TOKEN_KEY_PREFIX}{user_id}")
}
