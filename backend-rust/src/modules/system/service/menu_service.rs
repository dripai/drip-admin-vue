use crate::common::{AppError, I64String, next_id};
use crate::modules::system::dto::menu_request::MenuSaveRequest;
use crate::modules::system::entity::sys_menu::SysMenu;
use crate::modules::system::vo::menu_tree_vo::MenuTreeVo;
use rbatis::RBatis;
use serde::Deserialize;
use std::collections::{BTreeMap, BTreeSet};
use std::sync::Arc;

#[derive(Debug, Deserialize)]
struct CountRow {
    total: i64,
}

pub async fn list(database: Option<&Arc<RBatis>>) -> Result<Vec<MenuTreeVo>, AppError> {
    let database = require_database(database)?;
    let rows = all_menus(database).await?;
    Ok(build_tree(rows))
}

pub async fn create(
    database: Option<&Arc<RBatis>>,
    request: MenuSaveRequest,
) -> Result<I64String, AppError> {
    let database = require_database(database)?;
    validate_request(&request)?;
    let parent_id = request.parent_id.unwrap_or(0);
    assert_existing_parent(database, parent_id).await?;
    let id = next_id();
    database
        .exec(
            "insert into sys_menu (id, parent_id, name, type, path, component, permission_code, icon, sort, visible, status, deleted) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)",
            vec![
                rbs::value!(id),
                rbs::value!(parent_id),
                rbs::value!(request.name),
                rbs::value!(request.r#type),
                rbs::value!(request.path),
                rbs::value!(request.component),
                rbs::value!(empty_to_none(request.permission_code)),
                rbs::value!(request.icon),
                rbs::value!(request.sort.unwrap_or(0)),
                rbs::value!(request.visible.unwrap_or(1)),
                rbs::value!(request.status.unwrap_or(1)),
            ],
        )
        .await
        .map_err(map_database_error)?;
    Ok(I64String(id))
}

pub async fn update(
    database: Option<&Arc<RBatis>>,
    id: i64,
    request: MenuSaveRequest,
) -> Result<(), AppError> {
    let database = require_database(database)?;
    detail_with_database(database, id).await?;
    validate_request(&request)?;
    let parent_id = request.parent_id.unwrap_or(0);
    assert_existing_parent(database, parent_id).await?;
    assert_valid_parent(database, id, parent_id).await?;
    database
        .exec(
            "update sys_menu set parent_id = ?, name = ?, type = ?, path = ?, component = ?, permission_code = ?, icon = ?, sort = ?, visible = ?, status = ? where id = ? and deleted = 0",
            vec![
                rbs::value!(parent_id),
                rbs::value!(request.name),
                rbs::value!(request.r#type),
                rbs::value!(request.path),
                rbs::value!(request.component),
                rbs::value!(empty_to_none(request.permission_code)),
                rbs::value!(request.icon),
                rbs::value!(request.sort.unwrap_or(0)),
                rbs::value!(request.visible.unwrap_or(1)),
                rbs::value!(request.status.unwrap_or(1)),
                rbs::value!(id),
            ],
        )
        .await
        .map_err(map_database_error)?;
    Ok(())
}

pub async fn delete(database: Option<&Arc<RBatis>>, id: i64) -> Result<(), AppError> {
    let database = require_database(database)?;
    let child_count = count(
        database,
        "select count(*) as total from sys_menu where parent_id = ? and deleted = 0",
        vec![rbs::value!(id)],
    )
    .await?;
    if child_count > 0 {
        return Err(AppError::bad_request("operation failed"));
    }
    detail_with_database(database, id).await?;
    database
        .exec(
            "update sys_menu set deleted = 1 where id = ? and deleted = 0",
            vec![rbs::value!(id)],
        )
        .await
        .map_err(map_database_error)?;
    Ok(())
}

pub async fn update_status(
    database: Option<&Arc<RBatis>>,
    id: i64,
    status: i32,
) -> Result<(), AppError> {
    let database = require_database(database)?;
    detail_with_database(database, id).await?;
    database
        .exec(
            "update sys_menu set status = ? where id = ? and deleted = 0",
            vec![rbs::value!(status), rbs::value!(id)],
        )
        .await
        .map_err(map_database_error)?;
    Ok(())
}

async fn all_menus(database: &RBatis) -> Result<Vec<SysMenu>, AppError> {
    database
        .exec_decode(
            "select id, parent_id, name, type, path, component, permission_code, icon, sort, visible, status, deleted, created_at, updated_at from sys_menu where deleted = 0 order by sort asc, id asc",
            vec![],
        )
        .await
        .map_err(map_database_error)
}

async fn detail_with_database(database: &RBatis, id: i64) -> Result<SysMenu, AppError> {
    let rows: Vec<SysMenu> = database
        .exec_decode(
            "select id, parent_id, name, type, path, component, permission_code, icon, sort, visible, status, deleted, created_at, updated_at from sys_menu where id = ? and deleted = 0",
            vec![rbs::value!(id)],
        )
        .await
        .map_err(map_database_error)?;
    rows.into_iter()
        .next()
        .ok_or_else(|| AppError::not_found("资源不存在"))
}

async fn assert_existing_parent(database: &RBatis, parent_id: i64) -> Result<(), AppError> {
    if parent_id == 0 {
        return Ok(());
    }
    detail_with_database(database, parent_id).await?;
    Ok(())
}

async fn assert_valid_parent(database: &RBatis, id: i64, parent_id: i64) -> Result<(), AppError> {
    if parent_id == 0 {
        return Ok(());
    }
    if parent_id == id
        || descendant_menu_ids(database, id)
            .await?
            .contains(&parent_id)
    {
        return Err(AppError::bad_request("operation failed"));
    }
    Ok(())
}

async fn descendant_menu_ids(database: &RBatis, id: i64) -> Result<BTreeSet<i64>, AppError> {
    let rows = all_menus(database).await?;
    let mut children_by_parent: BTreeMap<i64, Vec<i64>> = BTreeMap::new();
    for row in rows {
        children_by_parent
            .entry(row.parent_id.value())
            .or_default()
            .push(row.id.value());
    }
    let mut result = BTreeSet::new();
    collect_descendants(id, &children_by_parent, &mut result);
    Ok(result)
}

fn collect_descendants(
    id: i64,
    children_by_parent: &BTreeMap<i64, Vec<i64>>,
    result: &mut BTreeSet<i64>,
) {
    if let Some(children) = children_by_parent.get(&id) {
        for child_id in children {
            if result.insert(*child_id) {
                collect_descendants(*child_id, children_by_parent, result);
            }
        }
    }
}

async fn count(database: &RBatis, sql: &str, args: Vec<rbs::Value>) -> Result<i64, AppError> {
    let counts: Vec<CountRow> = database
        .exec_decode(sql, args)
        .await
        .map_err(map_database_error)?;
    Ok(counts.first().map(|row| row.total).unwrap_or(0))
}

fn build_tree(rows: Vec<SysMenu>) -> Vec<MenuTreeVo> {
    let ids: BTreeSet<i64> = rows.iter().map(|row| row.id.value()).collect();
    let mut children_by_parent: BTreeMap<i64, Vec<SysMenu>> = BTreeMap::new();
    for row in rows {
        let parent_id = row.parent_id.value();
        let key = if parent_id == 0 || !ids.contains(&parent_id) {
            0
        } else {
            parent_id
        };
        children_by_parent.entry(key).or_default().push(row);
    }
    build_children(0, &mut children_by_parent)
}

fn build_children(
    parent_id: i64,
    children_by_parent: &mut BTreeMap<i64, Vec<SysMenu>>,
) -> Vec<MenuTreeVo> {
    let rows = children_by_parent.remove(&parent_id).unwrap_or_default();
    rows.into_iter()
        .map(|row| {
            let id = row.id.value();
            MenuTreeVo {
                id: row.id,
                parent_id: row.parent_id,
                name: row.name,
                r#type: row.r#type,
                path: row.path,
                component: row.component,
                permission_code: row.permission_code,
                icon: row.icon,
                sort: row.sort,
                visible: row.visible,
                status: row.status,
                children: build_children(id, children_by_parent),
            }
        })
        .collect()
}

fn require_database(database: Option<&Arc<RBatis>>) -> Result<&RBatis, AppError> {
    database
        .map(|database| database.as_ref())
        .ok_or_else(|| AppError::system("Rbatis database is not configured"))
}

fn validate_request(request: &MenuSaveRequest) -> Result<(), AppError> {
    require_text(&request.name, "name")?;
    require_text(&request.r#type, "type")?;
    validate_length(&request.name, "name", 64)?;
    match request.r#type.as_str() {
        "DIRECTORY" | "MENU" | "BUTTON" => {}
        _ => {
            return Err(AppError::bad_request(
                "type must be DIRECTORY, MENU or BUTTON",
            ));
        }
    }
    if !valid_permission_code(request.permission_code.as_deref()) {
        return Err(AppError::bad_request("permissionCode format is invalid"));
    }
    Ok(())
}

fn require_text(value: &str, field: &str) -> Result<(), AppError> {
    if value.trim().is_empty() {
        return Err(AppError::bad_request(format!("{field} is required")));
    }
    Ok(())
}

fn validate_length(value: &str, field: &str, max: usize) -> Result<(), AppError> {
    if value.chars().count() > max {
        return Err(AppError::bad_request(format!(
            "{field} length must be <= {max}"
        )));
    }
    Ok(())
}

fn valid_permission_code(value: Option<&str>) -> bool {
    let Some(value) = value.map(str::trim).filter(|value| !value.is_empty()) else {
        return true;
    };
    let segments: Vec<&str> = value.split(':').collect();
    if segments.is_empty() || segments.len() > 3 {
        return false;
    }
    segments.iter().all(|segment| {
        let mut chars = segment.chars();
        matches!(chars.next(), Some(first) if first.is_ascii_lowercase())
            && chars.all(|ch| ch.is_ascii_alphanumeric())
    })
}

fn empty_to_none(value: Option<String>) -> Option<String> {
    value.and_then(|value| {
        let trimmed = value.trim();
        if trimmed.is_empty() {
            None
        } else {
            Some(trimmed.to_string())
        }
    })
}

fn map_database_error(err: rbatis::Error) -> AppError {
    let message = err.to_string();
    if message.to_ascii_lowercase().contains("duplicate") {
        AppError::conflict("数据冲突")
    } else {
        AppError::system(message)
    }
}
