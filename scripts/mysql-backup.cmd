@echo off
setlocal EnableExtensions

set "MYSQLDUMP_EXE=D:\tool\mysql8\bin\mysqldump.exe"
set "MYSQL_HOST=127.0.0.1"
set "MYSQL_PORT=3307"
set "MYSQL_USER=root"
set "MYSQL_PASSWORD=root"
set "MYSQL_DATABASE=drip-manager"
set "BACKUP_DIR=%~dp0..\backups\mysql"
set "KEEP_DAYS=14"

if not exist "%MYSQLDUMP_EXE%" (
  set "MYSQLDUMP_EXE=mysqldump.exe"
)

for /f %%i in ('powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "Get-Date -Format yyyyMMdd_HHmmss"') do set "TIMESTAMP=%%i"

if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"
if not exist "%BACKUP_DIR%\logs" mkdir "%BACKUP_DIR%\logs"

set "SQL_FILE=%BACKUP_DIR%\%MYSQL_DATABASE%-%TIMESTAMP%.sql"
set "GZIP_FILE=%SQL_FILE%.gz"
set "LOG_FILE=%BACKUP_DIR%\logs\mysql-backup.log"

call :log "backup start: %MYSQL_DATABASE%"

set "MYSQL_PWD=%MYSQL_PASSWORD%"
"%MYSQLDUMP_EXE%" --single-transaction --routines --triggers --events --default-character-set=utf8mb4 --host="%MYSQL_HOST%" --port="%MYSQL_PORT%" --user="%MYSQL_USER%" --result-file="%SQL_FILE%" "%MYSQL_DATABASE%"
set "DUMP_EXIT=%ERRORLEVEL%"
set "MYSQL_PWD="

if not "%DUMP_EXIT%"=="0" (
  if exist "%SQL_FILE%" del /f /q "%SQL_FILE%" >nul 2>nul
  call :log "backup failed: mysqldump exit code %DUMP_EXIT%"
  echo mysqldump failed, exit code: %DUMP_EXIT%
  exit /b %DUMP_EXIT%
)

powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; $source=[System.IO.File]::OpenRead($env:SQL_FILE); try { $target=[System.IO.File]::Create($env:GZIP_FILE); try { $gzip=[System.IO.Compression.GzipStream]::new($target, [System.IO.Compression.CompressionLevel]::Optimal); try { $source.CopyTo($gzip) } finally { $gzip.Dispose() } } finally { $target.Dispose() } } finally { $source.Dispose() }"
if errorlevel 1 (
  if exist "%SQL_FILE%" del /f /q "%SQL_FILE%" >nul 2>nul
  if exist "%GZIP_FILE%" del /f /q "%GZIP_FILE%" >nul 2>nul
  call :log "backup failed: gzip compression failed"
  echo gzip compression failed.
  exit /b 1
)

del /f /q "%SQL_FILE%" >nul 2>nul

powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "$limit=(Get-Date).AddDays(-[int]$env:KEEP_DAYS); Get-ChildItem -LiteralPath $env:BACKUP_DIR -Filter '*.sql.gz' | Where-Object { $_.LastWriteTime -lt $limit } | Remove-Item -Force"

call :log "backup success: %GZIP_FILE%"
echo Backup completed: %GZIP_FILE%
exit /b 0

:log
set "LOG_MESSAGE=%~1"
powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "$message=$env:LOG_MESSAGE; $line='[' + (Get-Date -Format 'yyyy-MM-dd HH:mm:ss') + '] ' + $message; Add-Content -LiteralPath $env:LOG_FILE -Encoding UTF8 -Value $line"
exit /b 0
