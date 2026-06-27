@echo off
setlocal EnableExtensions

set "MYSQL_EXE=D:\tool\mysql8\bin\mysql.exe"
set "MYSQL_HOST=127.0.0.1"
set "MYSQL_PORT=3307"
set "MYSQL_USER=root"
set "MYSQL_PASSWORD=root"
set "MYSQL_DATABASE=drip-manager"

if "%~1"=="" (
  echo backup file parameter is required.
  echo Usage: %~nx0 backup-file.sql.gz
  exit /b 1
)

set "BACKUP_FILE=%~f1"
if not exist "%BACKUP_FILE%" (
  echo backup file not found: %BACKUP_FILE%
  exit /b 1
)

set "BACKUP_NAME=%~nx1"
if /i not "%BACKUP_NAME:~-7%"==".sql.gz" (
  echo backup file must end with .sql.gz
  exit /b 1
)

if not exist "%MYSQL_EXE%" (
  set "MYSQL_EXE=mysql.exe"
)

set "TEMP_SQL=%TEMP%\mysql-restore-%RANDOM%-%RANDOM%.sql"

echo DANGER: this will restore database "%MYSQL_DATABASE%" from:
echo %BACKUP_FILE%

powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; $source=[System.IO.File]::OpenRead($env:BACKUP_FILE); try { $gzip=[System.IO.Compression.GzipStream]::new($source, [System.IO.Compression.CompressionMode]::Decompress); try { $target=[System.IO.File]::Create($env:TEMP_SQL); try { $gzip.CopyTo($target) } finally { $target.Dispose() } } finally { $gzip.Dispose() } } finally { $source.Dispose() }"
if errorlevel 1 (
  if exist "%TEMP_SQL%" del /f /q "%TEMP_SQL%" >nul 2>nul
  echo gzip decompress failed.
  exit /b 1
)

set "MYSQL_PWD=%MYSQL_PASSWORD%"
"%MYSQL_EXE%" --default-character-set=utf8mb4 --host="%MYSQL_HOST%" --port="%MYSQL_PORT%" --user="%MYSQL_USER%" "%MYSQL_DATABASE%" < "%TEMP_SQL%"
set "RESTORE_EXIT=%ERRORLEVEL%"
set "MYSQL_PWD="

if exist "%TEMP_SQL%" del /f /q "%TEMP_SQL%" >nul 2>nul

if not "%RESTORE_EXIT%"=="0" (
  echo mysql restore failed, exit code: %RESTORE_EXIT%
  exit /b %RESTORE_EXIT%
)

echo Restore completed.
exit /b 0
