use crate::common::{next_id, AppError, I64String, PageParams, PageResult};
use crate::config::Settings;
use crate::modules::system::dto::job_request::JobSaveRequest;
use crate::modules::system::entity::sys_job::SysJob;
use crate::modules::system::entity::sys_job_run_log::SysJobRunLog;
use rbatis::RBatis;
use serde::Deserialize;
use std::path::PathBuf;
use std::sync::Arc;
use std::time::{Duration, Instant};

pub async fn list(
    database: Option<&Arc<RBatis>>,
    page: PageParams,
) -> Result<PageResult<SysJob>, AppError> {
    let database = require_database(database)?;
    let rows: Vec<SysJob> = database
        .exec_decode(
            "select id, job_name, cron_expression, executor_type, script_file, script_args, status, remark, deleted from sys_job where deleted = 0 order by created_at desc limit ? offset ?",
            vec![
                rbs::value!(page.page_size),
                rbs::value!(((page.page - 1) * page.page_size) as i64),
            ],
        )
        .await
        .map_err(map_database_error)?;
    let totals: Vec<Count> = database
        .exec_decode("select count(*) as total from sys_job where deleted = 0", vec![])
        .await
        .map_err(map_database_error)?;
    Ok(PageResult {
        list: rows,
        total: I64String(totals.first().map(|row| row.total).unwrap_or(0)),
        page: page.page,
        page_size: page.page_size,
    })
}

pub async fn scripts(settings: &Settings) -> Result<Vec<String>, AppError> {
    let root = script_root(settings)?;
    let mut directory = tokio::fs::read_dir(root)
        .await
        .map_err(|error| AppError::system(error.to_string()))?;
    let mut scripts = Vec::new();
    while let Some(entry) = directory
        .next_entry()
        .await
        .map_err(|error| AppError::system(error.to_string()))?
    {
        if entry
            .file_type()
            .await
            .map_err(|error| AppError::system(error.to_string()))?
            .is_file()
        {
            scripts.push(entry.file_name().to_string_lossy().to_string());
        }
    }
    scripts.sort();
    Ok(scripts)
}

pub async fn detail(database: Option<&Arc<RBatis>>, id: i64) -> Result<SysJob, AppError> {
    let rows: Vec<SysJob> = require_database(database)?
        .exec_decode(
            "select id, job_name, cron_expression, executor_type, script_file, script_args, status, remark, deleted from sys_job where id = ? and deleted = 0",
            vec![rbs::value!(id)],
        )
        .await
        .map_err(map_database_error)?;
    rows.into_iter()
        .next()
        .ok_or_else(|| AppError::not_found("资源不存在"))
}

pub async fn create(
    database: Option<&Arc<RBatis>>,
    request: JobSaveRequest,
) -> Result<I64String, AppError> {
    validate(&request)?;
    let id = next_id();
    require_database(database)?
        .exec(
            "insert into sys_job(id, job_name, cron_expression, executor_type, script_file, script_args, status, remark, deleted) values (?, ?, ?, ?, ?, ?, ?, ?, 0)",
            vec![
                rbs::value!(id),
                rbs::value!(request.job_name),
                rbs::value!(request.cron_expression),
                rbs::value!(request.executor_type.trim().to_ascii_lowercase()),
                rbs::value!(request.script_file),
                rbs::value!(request.script_args),
                rbs::value!(request.status.unwrap_or(1)),
                rbs::value!(request.remark),
            ],
        )
        .await
        .map_err(map_database_error)?;
    Ok(I64String(id))
}

pub async fn update(
    database: Option<&Arc<RBatis>>,
    id: i64,
    request: JobSaveRequest,
) -> Result<(), AppError> {
    detail(database, id).await?;
    validate(&request)?;
    require_database(database)?
        .exec(
            "update sys_job set job_name = ?, cron_expression = ?, executor_type = ?, script_file = ?, script_args = ?, status = ?, remark = ? where id = ?",
            vec![
                rbs::value!(request.job_name),
                rbs::value!(request.cron_expression),
                rbs::value!(request.executor_type.trim().to_ascii_lowercase()),
                rbs::value!(request.script_file),
                rbs::value!(request.script_args),
                rbs::value!(request.status.unwrap_or(1)),
                rbs::value!(request.remark),
                rbs::value!(id),
            ],
        )
        .await
        .map_err(map_database_error)?;
    Ok(())
}

pub async fn delete(database: Option<&Arc<RBatis>>, id: i64) -> Result<(), AppError> {
    detail(database, id).await?;
    require_database(database)?
        .exec("update sys_job set deleted = 1 where id = ?", vec![rbs::value!(id)])
        .await
        .map_err(map_database_error)?;
    Ok(())
}

pub async fn status(database: Option<&Arc<RBatis>>, id: i64, status: i32) -> Result<(), AppError> {
    if !matches!(status, 0 | 1) {
        return Err(AppError::bad_request("status is invalid"));
    }
    detail(database, id).await?;
    require_database(database)?
        .exec(
            "update sys_job set status = ? where id = ?",
            vec![rbs::value!(status), rbs::value!(id)],
        )
        .await
        .map_err(map_database_error)?;
    Ok(())
}

pub async fn run(database: Arc<RBatis>, settings: Arc<Settings>, id: i64) -> Result<(), AppError> {
    let job = detail(Some(&database), id).await?;
    let log_id = next_id();
    database
        .exec(
            "insert into sys_job_run_log(id, job_id, job_name, status, started_at, cost_ms) values (?, ?, ?, 'RUNNING', now(), 0)",
            vec![rbs::value!(log_id), rbs::value!(id), rbs::value!(&job.job_name)],
        )
        .await
        .map_err(map_database_error)?;
    tokio::spawn(async move {
        let started = Instant::now();
        let result = execute(&job, &settings).await;
        let (status, error_message) = match result {
            Ok(()) => ("SUCCESS", None),
            Err(error) => ("FAIL", Some(error.message.chars().take(512).collect::<String>())),
        };
        let _ = database
            .exec(
                "update sys_job_run_log set status = ?, finished_at = now(), cost_ms = ?, error_message = ? where id = ?",
                vec![
                    rbs::value!(status),
                    rbs::value!(started.elapsed().as_millis() as i64),
                    rbs::value!(error_message),
                    rbs::value!(log_id),
                ],
            )
            .await;
    });
    Ok(())
}

pub async fn run_logs(
    database: Option<&Arc<RBatis>>,
    page: PageParams,
    job_id: Option<i64>,
) -> Result<PageResult<SysJobRunLog>, AppError> {
    let database = require_database(database)?;
    let where_sql = if job_id.is_some() { "where job_id = ?" } else { "" };
    let mut args = Vec::new();
    if let Some(job_id) = job_id {
        args.push(rbs::value!(job_id));
    }
    let count_args = args.clone();
    args.push(rbs::value!(page.page_size));
    args.push(rbs::value!(((page.page - 1) * page.page_size) as i64));
    let rows: Vec<SysJobRunLog> = database
        .exec_decode(
            &format!("select id, job_id, job_name, status, started_at, finished_at, cost_ms, error_message from sys_job_run_log {where_sql} order by started_at desc, id desc limit ? offset ?"),
            args,
        )
        .await
        .map_err(map_database_error)?;
    let totals: Vec<Count> = database
        .exec_decode(
            &format!("select count(*) as total from sys_job_run_log {where_sql}"),
            count_args,
        )
        .await
        .map_err(map_database_error)?;
    Ok(PageResult {
        list: rows,
        total: I64String(totals.first().map(|row| row.total).unwrap_or(0)),
        page: page.page,
        page_size: page.page_size,
    })
}

async fn execute(job: &SysJob, settings: &Settings) -> Result<(), AppError> {
    let script = job
        .script_file
        .as_deref()
        .ok_or_else(|| AppError::bad_request("scriptFile is required"))?;
    let root = script_root(settings)?;
    let path = root
        .join(script)
        .canonicalize()
        .map_err(|_| AppError::bad_request("script path is not allowed"))?;
    if !path.starts_with(&root) {
        return Err(AppError::bad_request("script path is not allowed"));
    }
    let mut command = match job.executor_type.as_str() {
        "shell" => {
            let mut command = tokio::process::Command::new("bash");
            command.arg(&path);
            command
        }
        "bat" => {
            let mut command = tokio::process::Command::new("cmd.exe");
            command.args(["/c", path.to_string_lossy().as_ref()]);
            command
        }
        "powershell" | "ps1" => {
            let mut command = tokio::process::Command::new("powershell.exe");
            command.args(["-ExecutionPolicy", "Bypass", "-File", path.to_string_lossy().as_ref()]);
            command
        }
        "python" => {
            let mut command = tokio::process::Command::new("python");
            command.arg(&path);
            command
        }
        _ => return Err(AppError::bad_request("executorType is not supported")),
    };
    command.current_dir(&root);
    if let Some(args) = job.script_args.as_deref() {
        command.args(args.split_whitespace());
    }
    let result = tokio::time::timeout(Duration::from_secs(1800), command.status())
        .await
        .map_err(|_| AppError::system("script execution timed out"))?
        .map_err(|error| AppError::system(error.to_string()))?;
    if result.success() {
        Ok(())
    } else {
        Err(AppError::system(format!(
            "script execution failed, exit code: {}",
            result.code().unwrap_or(-1)
        )))
    }
}

fn script_root(settings: &Settings) -> Result<PathBuf, AppError> {
    PathBuf::from(&settings.job.script_dir)
        .canonicalize()
        .map_err(|_| AppError::system("scripts directory is not configured"))
}

fn require_database(database: Option<&Arc<RBatis>>) -> Result<&RBatis, AppError> {
    database
        .map(AsRef::as_ref)
        .ok_or_else(|| AppError::system("Rbatis database is not configured"))
}

fn validate(request: &JobSaveRequest) -> Result<(), AppError> {
    if request.job_name.trim().is_empty()
        || request.cron_expression.split_whitespace().count() < 5
        || request.cron_expression.len() > 64
    {
        return Err(AppError::bad_request("jobName or cronExpression is invalid"));
    }
    if !matches!(
        request.executor_type.trim().to_ascii_lowercase().as_str(),
        "shell" | "bat" | "powershell" | "ps1" | "python"
    ) {
        return Err(AppError::bad_request("executorType is not supported"));
    }
    if request.script_file.trim().is_empty() {
        return Err(AppError::bad_request("scriptFile is required"));
    }
    Ok(())
}

fn map_database_error(error: rbatis::Error) -> AppError {
    AppError::system(error.to_string())
}

#[derive(Deserialize)]
struct Count {
    total: i64,
}
