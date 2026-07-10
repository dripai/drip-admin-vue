use crate::common::{AppError, I64String, next_id};
use crate::modules::system::dto::dept_request::DeptSaveRequest;
use crate::modules::system::entity::sys_dept::SysDept;
use crate::modules::system::vo::dept_tree_vo::DeptTreeVo;
use rbatis::RBatis;
use serde::Deserialize;
use std::collections::{BTreeMap, BTreeSet};
use std::sync::Arc;

#[derive(Debug, Deserialize)]
struct CountRow {
    total: i64,
}

pub async fn list(database: Option<&Arc<RBatis>>) -> Result<Vec<DeptTreeVo>, AppError> {
    let database = require_database(database)?;
    let rows = all_depts(database).await?;
    Ok(build_tree(rows))
}

pub async fn detail(database: Option<&Arc<RBatis>>, id: i64) -> Result<SysDept, AppError> {
    let database = require_database(database)?;
    detail_with_database(database, id).await
}

pub async fn create(
    database: Option<&Arc<RBatis>>,
    request: DeptSaveRequest,
) -> Result<I64String, AppError> {
    let database = require_database(database)?;
    validate_request(&request)?;
    let parent_id = request.parent_id.unwrap_or(0);
    assert_existing_parent(database, parent_id).await?;
    assert_unique_dept_code(database, None, &request.dept_code).await?;
    assert_existing_leader(database, request.leader_user_id).await?;

    let id = next_id();
    database
        .exec(
            "insert into sys_dept (id, parent_id, dept_name, dept_code, leader_user_id, sort, status, deleted) values (?, ?, ?, ?, ?, ?, ?, 0)",
            vec![
                rbs::value!(id),
                rbs::value!(parent_id),
                rbs::value!(request.dept_name),
                rbs::value!(request.dept_code),
                rbs::value!(request.leader_user_id),
                rbs::value!(request.sort.unwrap_or(0)),
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
    request: DeptSaveRequest,
) -> Result<(), AppError> {
    let database = require_database(database)?;
    validate_request(&request)?;
    detail_with_database(database, id).await?;
    let parent_id = request.parent_id.unwrap_or(0);
    assert_valid_parent(database, id, parent_id).await?;
    assert_unique_dept_code(database, Some(id), &request.dept_code).await?;
    assert_existing_leader(database, request.leader_user_id).await?;

    database
        .exec(
            "update sys_dept set parent_id = ?, dept_name = ?, dept_code = ?, leader_user_id = ?, sort = ?, status = ? where id = ? and deleted = 0",
            vec![
                rbs::value!(parent_id),
                rbs::value!(request.dept_name),
                rbs::value!(request.dept_code),
                rbs::value!(request.leader_user_id),
                rbs::value!(request.sort.unwrap_or(0)),
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
    detail_with_database(database, id).await?;
    let child_count = count(
        database,
        "select count(*) as total from sys_dept where parent_id = ? and deleted = 0",
        vec![rbs::value!(id)],
    )
    .await?;
    if child_count > 0 {
        return Err(AppError::bad_request("存在子部门，不能删除"));
    }
    let user_count = count(
        database,
        "select count(*) as total from sys_user where dept_id = ? and deleted = 0",
        vec![rbs::value!(id)],
    )
    .await?;
    if user_count > 0 {
        return Err(AppError::bad_request("部门下存在用户，不能删除"));
    }
    database
        .exec(
            "update sys_dept set deleted = 1 where id = ? and deleted = 0",
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
            "update sys_dept set status = ? where id = ? and deleted = 0",
            vec![rbs::value!(status), rbs::value!(id)],
        )
        .await
        .map_err(map_database_error)?;
    Ok(())
}

async fn all_depts(database: &RBatis) -> Result<Vec<SysDept>, AppError> {
    database
        .exec_decode(
            "select id, parent_id, dept_name, dept_code, leader_user_id, sort, status, deleted, created_at, updated_at from sys_dept where deleted = 0 order by sort asc, id asc",
            vec![],
        )
        .await
        .map_err(map_database_error)
}

async fn detail_with_database(database: &RBatis, id: i64) -> Result<SysDept, AppError> {
    let rows: Vec<SysDept> = database
        .exec_decode(
            "select id, parent_id, dept_name, dept_code, leader_user_id, sort, status, deleted, created_at, updated_at from sys_dept where id = ? and deleted = 0",
            vec![rbs::value!(id)],
        )
        .await
        .map_err(map_database_error)?;
    rows.into_iter()
        .next()
        .ok_or_else(|| AppError::not_found("资源不存在"))
}

async fn assert_valid_parent(database: &RBatis, id: i64, parent_id: i64) -> Result<(), AppError> {
    if parent_id == 0 {
        return Ok(());
    }
    if parent_id == id
        || descendant_dept_ids(database, id)
            .await?
            .contains(&parent_id)
    {
        return Err(AppError::bad_request("上级部门不能选择自己或子部门"));
    }
    assert_existing_parent(database, parent_id).await
}

async fn assert_existing_parent(database: &RBatis, parent_id: i64) -> Result<(), AppError> {
    if parent_id == 0 {
        return Ok(());
    }
    detail_with_database(database, parent_id).await?;
    Ok(())
}

async fn assert_unique_dept_code(
    database: &RBatis,
    id: Option<i64>,
    dept_code: &str,
) -> Result<(), AppError> {
    let total = match id {
        Some(id) => {
            count(
                database,
                "select count(*) as total from sys_dept where dept_code = ? and id <> ? and deleted = 0",
                vec![rbs::value!(dept_code), rbs::value!(id)],
            )
            .await?
        }
        None => {
            count(
                database,
                "select count(*) as total from sys_dept where dept_code = ? and deleted = 0",
                vec![rbs::value!(dept_code)],
            )
            .await?
        }
    };
    if total > 0 {
        return Err(AppError::bad_request("部门编码已存在"));
    }
    Ok(())
}

async fn assert_existing_leader(
    database: &RBatis,
    leader_user_id: Option<i64>,
) -> Result<(), AppError> {
    let Some(leader_user_id) = leader_user_id else {
        return Ok(());
    };
    let total = count(
        database,
        "select count(*) as total from sys_user where id = ? and deleted = 0",
        vec![rbs::value!(leader_user_id)],
    )
    .await?;
    if total == 0 {
        return Err(AppError::bad_request("负责人用户不存在"));
    }
    Ok(())
}

async fn descendant_dept_ids(database: &RBatis, id: i64) -> Result<BTreeSet<i64>, AppError> {
    let rows = all_depts(database).await?;
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

fn build_tree(rows: Vec<SysDept>) -> Vec<DeptTreeVo> {
    let ids: BTreeSet<i64> = rows.iter().map(|row| row.id.value()).collect();
    let mut children_by_parent: BTreeMap<i64, Vec<SysDept>> = BTreeMap::new();
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
    children_by_parent: &mut BTreeMap<i64, Vec<SysDept>>,
) -> Vec<DeptTreeVo> {
    let rows = children_by_parent.remove(&parent_id).unwrap_or_default();
    rows.into_iter()
        .map(|row| {
            let id = row.id.value();
            DeptTreeVo {
                id: row.id,
                parent_id: row.parent_id,
                dept_name: row.dept_name,
                dept_code: row.dept_code,
                leader_user_id: row.leader_user_id,
                sort: row.sort,
                status: row.status,
                created_at: row.created_at,
                updated_at: row.updated_at,
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

fn validate_request(request: &DeptSaveRequest) -> Result<(), AppError> {
    require_text(&request.dept_name, "deptName")?;
    require_text(&request.dept_code, "deptCode")?;
    validate_length(&request.dept_name, "deptName", 64)?;
    validate_length(&request.dept_code, "deptCode", 64)?;
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

fn map_database_error(err: rbatis::Error) -> AppError {
    let message = err.to_string();
    if message.to_ascii_lowercase().contains("duplicate") {
        AppError::conflict("数据冲突")
    } else {
        AppError::system(message)
    }
}
