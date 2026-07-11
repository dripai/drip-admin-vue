use super::{ok, ok_null};
use crate::common::{ApiResponse, AppError, PageQuery, PageResult};
use crate::modules::system::AppState;
use crate::modules::system::service::online_user_service;
use crate::modules::system::vo::online_user_vo::OnlineUserVo;
use axum::extract::{Path, Query, State};
use axum::Json;
#[derive(serde::Deserialize)] #[serde(rename_all = "camelCase")] pub struct OnlineUserQuery { pub page: Option<i32>, pub page_size: Option<i32>, pub username: Option<String> }
impl OnlineUserQuery { fn page(&self) -> Result<crate::common::PageParams, AppError> { PageQuery { page: self.page, page_size: self.page_size }.normalize() } }
pub async fn list(State(state): State<AppState>, Query(query): Query<OnlineUserQuery>) -> Result<Json<ApiResponse<PageResult<OnlineUserVo>>>, AppError> { Ok(ok(online_user_service::list(state.redis_pool.as_ref(), query.page()?, query.username.as_deref()).await?)) }
pub async fn detail(State(state): State<AppState>, Path(token_id): Path<String>) -> Result<Json<ApiResponse<OnlineUserVo>>, AppError> { Ok(ok(online_user_service::detail(state.redis_pool.as_ref(), &token_id).await?)) }
pub async fn kickout(State(state): State<AppState>, Path(token_id): Path<String>) -> Result<Json<ApiResponse<()>>, AppError> { online_user_service::kickout(state.redis_pool.as_ref(), &token_id).await?; Ok(ok_null()) }
