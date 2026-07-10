use crate::common::AppError;
use deadpool_redis::Pool;
use deadpool_redis::redis::AsyncCommands;

const LOGIN_FAILURE_KEY_PREFIX: &str = "drip:login-failure:";
const LOGIN_LOCK_KEY_PREFIX: &str = "drip:login-lock:";
const MAX_FAILURES: i64 = 3;
const LOCK_SECONDS: u64 = 900;

pub async fn assert_not_locked(pool: &Pool, username: &str) -> Result<(), AppError> {
    let mut connection = pool
        .get()
        .await
        .map_err(|err| AppError::system(err.to_string()))?;
    let locked = connection
        .get::<_, Option<String>>(lock_key(username))
        .await
        .map_err(|err| AppError::system(err.to_string()))?
        .is_some();
    if locked {
        return Err(AppError::unauthorized("账号已锁定，请稍后再试"));
    }
    Ok(())
}

pub async fn record_failure(pool: &Pool, username: &str) -> Result<i64, AppError> {
    let mut connection = pool
        .get()
        .await
        .map_err(|err| AppError::system(err.to_string()))?;
    let key = failure_key(username);
    let failures = connection
        .incr::<_, _, i64>(&key, 1)
        .await
        .map_err(|err| AppError::system(err.to_string()))?;
    let _: () = connection
        .expire(&key, LOCK_SECONDS as i64)
        .await
        .map_err(|err| AppError::system(err.to_string()))?;
    if failures >= MAX_FAILURES {
        let _: () = connection
            .set_ex(lock_key(username), "1", LOCK_SECONDS)
            .await
            .map_err(|err| AppError::system(err.to_string()))?;
    }
    Ok((MAX_FAILURES - failures).max(0))
}

pub async fn clear_failures(pool: &Pool, username: &str) -> Result<(), AppError> {
    let mut connection = pool
        .get()
        .await
        .map_err(|err| AppError::system(err.to_string()))?;
    let _: () = connection
        .del(failure_key(username))
        .await
        .map_err(|err| AppError::system(err.to_string()))?;
    let _: () = connection
        .del(lock_key(username))
        .await
        .map_err(|err| AppError::system(err.to_string()))?;
    Ok(())
}

fn failure_key(username: &str) -> String {
    format!("{LOGIN_FAILURE_KEY_PREFIX}{username}")
}

fn lock_key(username: &str) -> String {
    format!("{LOGIN_LOCK_KEY_PREFIX}{username}")
}
