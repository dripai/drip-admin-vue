#!/usr/bin/env python3
"""Create a gzipped MySQL backup for the drip-admin local database."""

from __future__ import annotations

import gzip
import os
import shutil
import subprocess
from datetime import datetime, timedelta
from pathlib import Path


SCRIPT_DIR = Path(__file__).resolve().parent
DEFAULT_BACKUP_DIR = SCRIPT_DIR.parent / "backups" / "mysql"
MYSQLDUMP_EXE = r"D:\tool\mysql8\bin\mysqldump.exe"


def env(name: str, default: str) -> str:
    return os.environ.get(name, default)


def log(log_file: Path, message: str) -> None:
    log_file.parent.mkdir(parents=True, exist_ok=True)
    line = f"[{datetime.now():%Y-%m-%d %H:%M:%S}] {message}\n"
    with log_file.open("a", encoding="utf-8") as file:
        file.write(line)


def remove_old_backups(backup_dir: Path, keep_days: int) -> None:
    limit = datetime.now() - timedelta(days=keep_days)
    for path in backup_dir.glob("*.sql.gz"):
        modified = datetime.fromtimestamp(path.stat().st_mtime)
        if modified < limit:
            path.unlink()


def main() -> None:
    mysqldump = MYSQLDUMP_EXE
    host = env("MYSQL_HOST", "127.0.0.1")
    port = env("MYSQL_PORT", "3307")
    user = env("MYSQL_USER", "root")
    password = env("MYSQL_PASSWORD", "root")
    database = env("MYSQL_DATABASE", "drip-manager")
    backup_dir = Path(env("BACKUP_DIR", str(DEFAULT_BACKUP_DIR))).resolve()
    keep_days = int(env("KEEP_DAYS", "14"))

    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    backup_dir.mkdir(parents=True, exist_ok=True)
    sql_file = backup_dir / f"{database}-{timestamp}.sql"
    gzip_file = sql_file.with_suffix(sql_file.suffix + ".gz")
    log_file = backup_dir / "logs" / "mysql-backup.log"

    log(log_file, f"backup start: {database}")
    command = [
        mysqldump,
        "--single-transaction",
        "--routines",
        "--triggers",
        "--events",
        "--default-character-set=utf8mb4",
        f"--host={host}",
        f"--port={port}",
        f"--user={user}",
        f"--result-file={sql_file}",
        database,
    ]
    run_env = os.environ.copy()
    run_env["MYSQL_PWD"] = password

    try:
        subprocess.run(command, env=run_env, check=True)
        with sql_file.open("rb") as source, gzip.open(gzip_file, "wb", compresslevel=9) as target:
            shutil.copyfileobj(source, target)
        sql_file.unlink()
        remove_old_backups(backup_dir, keep_days)
    except subprocess.CalledProcessError as exc:
        sql_file.unlink(missing_ok=True)
        gzip_file.unlink(missing_ok=True)
        log(log_file, f"backup failed: mysqldump exit code {exc.returncode}")
        raise SystemExit(exc.returncode) from exc
    except OSError as exc:
        sql_file.unlink(missing_ok=True)
        gzip_file.unlink(missing_ok=True)
        log(log_file, f"backup failed: {exc}")
        raise SystemExit(1) from exc

    log(log_file, f"backup success: {gzip_file}")
    print(f"Backup completed: {gzip_file}")


if __name__ == "__main__":
    main()
