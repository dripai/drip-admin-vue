use chrono::{Duration, Local, NaiveDate};
use std::fs::{self, File, OpenOptions};
use std::io::{self, Write};
use std::path::{Path, PathBuf};
use std::sync::{Arc, Mutex};
use tracing_subscriber::layer::SubscriberExt;
use tracing_subscriber::util::SubscriberInitExt;
use tracing_subscriber::EnvFilter;

const LOG_PREFIX: &str = "drip-admin-rust";
const LOG_RETENTION_DAYS: i64 = 7;

#[derive(Clone)]
struct DailyFileWriter {
    state: Arc<Mutex<DailyLogFile>>,
}

struct DailyLogFile {
    directory: PathBuf,
    date: NaiveDate,
    file: File,
}

impl DailyFileWriter {
    fn new(directory: impl Into<PathBuf>) -> io::Result<Self> {
        let directory = directory.into();
        fs::create_dir_all(&directory)?;
        let date = Local::now().date_naive();
        cleanup_expired_logs(&directory, date)?;
        Ok(Self {
            state: Arc::new(Mutex::new(DailyLogFile {
                file: open_log_file(&directory, date)?,
                directory,
                date,
            })),
        })
    }
}

impl Write for DailyFileWriter {
    fn write(&mut self, buffer: &[u8]) -> io::Result<usize> {
        let mut state = self
            .state
            .lock()
            .map_err(|_| io::Error::other("log writer lock is poisoned"))?;
        let today = Local::now().date_naive();
        if state.date != today {
            cleanup_expired_logs(&state.directory, today)?;
            state.file = open_log_file(&state.directory, today)?;
            state.date = today;
        }
        state.file.write(buffer)
    }

    fn flush(&mut self) -> io::Result<()> {
        self.state
            .lock()
            .map_err(|_| io::Error::other("log writer lock is poisoned"))?
            .file
            .flush()
    }
}

pub fn init(default_level: &str) -> io::Result<tracing_appender::non_blocking::WorkerGuard> {
    let filter = EnvFilter::try_from_default_env().unwrap_or_else(|_| EnvFilter::new(default_level));
    let (file_writer, guard) = tracing_appender::non_blocking(DailyFileWriter::new("logs")?);
    tracing_subscriber::registry()
        .with(filter)
        .with(tracing_subscriber::fmt::layer())
        .with(
            tracing_subscriber::fmt::layer()
                .with_ansi(false)
                .with_writer(file_writer),
        )
        .init();
    Ok(guard)
}

fn open_log_file(directory: &Path, date: NaiveDate) -> io::Result<File> {
    OpenOptions::new()
        .create(true)
        .append(true)
        .open(directory.join(format!("{LOG_PREFIX}.{date}.log")))
}

fn cleanup_expired_logs(directory: &Path, today: NaiveDate) -> io::Result<()> {
    let keep_after = today - Duration::days(LOG_RETENTION_DAYS - 1);
    for entry in fs::read_dir(directory)? {
        let entry = entry?;
        if !entry.file_type()?.is_file() {
            continue;
        }
        let Some(name) = entry.file_name().to_str().map(str::to_string) else {
            continue;
        };
        let Some(date) = log_date(&name) else {
            continue;
        };
        if date < keep_after {
            fs::remove_file(entry.path())?;
        }
    }
    Ok(())
}

fn log_date(name: &str) -> Option<NaiveDate> {
    let value = name
        .strip_prefix(&format!("{LOG_PREFIX}."))?
        .strip_suffix(".log")?;
    NaiveDate::parse_from_str(value, "%Y-%m-%d").ok()
}

#[cfg(test)]
mod tests {
    use super::{cleanup_expired_logs, LOG_PREFIX};
    use chrono::{Duration, NaiveDate};
    use std::fs;

    #[test]
    fn retains_only_the_latest_seven_daily_logs() {
        let today = NaiveDate::from_ymd_opt(2026, 7, 12).unwrap();
        let directory = std::env::temp_dir().join(format!(
            "drip-admin-rust-log-test-{}",
            std::process::id()
        ));
        let _ = fs::remove_dir_all(&directory);
        fs::create_dir_all(&directory).unwrap();
        for age in [0_i64, 6, 7] {
            let date = today - Duration::days(age);
            fs::write(directory.join(format!("{LOG_PREFIX}.{date}.log")), "log").unwrap();
        }

        cleanup_expired_logs(&directory, today).unwrap();

        assert!(directory.join("drip-admin-rust.2026-07-12.log").exists());
        assert!(directory.join("drip-admin-rust.2026-07-06.log").exists());
        assert!(!directory.join("drip-admin-rust.2026-07-05.log").exists());
        fs::remove_dir_all(directory).unwrap();
    }
}
