param(
    [string]$MysqlHost = "127.0.0.1",
    [int]$MysqlPort = 3307,
    [string]$MysqlUser = "root",
    [string]$MysqlPassword = "root",
    [string]$Database = "drip-manager",
    [string]$OutputFile = "$PSScriptRoot\..\backend\src\main\resources\db\baseline_schema_and_data.sql",
    [string]$MysqlDump = $env:MYSQLDUMP_COMMAND
)

$ErrorActionPreference = "Stop"

function Resolve-MySqlDump {
    param([string]$CommandValue)

    if (-not [string]::IsNullOrWhiteSpace($CommandValue)) {
        return $CommandValue
    }

    $pathCommand = Get-Command "mysqldump.exe" -ErrorAction SilentlyContinue
    if ($pathCommand) {
        return $pathCommand.Source
    }

    $candidates = @(
        "D:\tool\mysql8\bin\mysqldump.exe",
        "D:\tool\mysql\bin\mysqldump.exe",
        "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqldump.exe",
        "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysqldump.exe"
    )

    foreach ($candidate in $candidates) {
        if (Test-Path $candidate) {
            return $candidate
        }
    }

    throw "mysqldump.exe not found. Set MYSQLDUMP_COMMAND or add MySQL 8 bin directory to PATH."
}

$MysqlDump = Resolve-MySqlDump $MysqlDump
$OutputFile = [System.IO.Path]::GetFullPath($OutputFile)
$OutputDir = Split-Path $OutputFile
$TempFile = "$OutputFile.tmp"
$PreviousMysqlPwd = $env:MYSQL_PWD

New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null

$dumpArgs = @(
    "--single-transaction",
    "--routines",
    "--triggers",
    "--events",
    "--default-character-set=utf8mb4",
    "--skip-comments",
    "--column-statistics=0",
    "--set-gtid-purged=OFF",
    "--host=$MysqlHost",
    "--port=$MysqlPort",
    "--user=$MysqlUser",
    "--result-file=$TempFile",
    $Database
)

try {
    $env:MYSQL_PWD = $MysqlPassword
    & $MysqlDump @dumpArgs
    if ($LASTEXITCODE -ne 0) {
        if (Test-Path $TempFile) {
            Remove-Item -LiteralPath $TempFile -Force
        }
        throw "mysqldump failed, exit code: $LASTEXITCODE"
    }

    $dump = [System.IO.File]::ReadAllText($TempFile, [System.Text.Encoding]::UTF8)
    $header = @"
-- Baseline generated from current drip-manager database.
-- Replaces historical migrations for fresh database initialization.

"@

    [System.IO.File]::WriteAllText($OutputFile, $header + $dump, [System.Text.UTF8Encoding]::new($false))
    Remove-Item -LiteralPath $TempFile -Force

    Write-Host "Baseline exported: $OutputFile"
} finally {
    $env:MYSQL_PWD = $PreviousMysqlPwd
}
