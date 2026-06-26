package com.drip.admin.modules.system.service.impl;

import com.drip.admin.common.exception.BusinessException;
import com.drip.admin.common.response.BackupFile;
import com.drip.admin.common.response.PageResult;
import com.drip.admin.modules.system.service.DatabaseBackupService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.drip.admin.shared.utils.AdminUtils.parseInt;
import static com.drip.admin.shared.utils.AdminUtils.stringOf;
import static com.drip.admin.shared.utils.AdminUtils.stringValue;

@Service
public class DatabaseBackupServiceImpl implements DatabaseBackupService {
    private final JdbcTemplate jdbc;
    private final Path backupDir;
    private final String datasourceUrl;
    private final String datasourceUsername;
    private final String datasourcePassword;
    private final String mysqldumpCommand;
    private final String mysqlCommand;

    public DatabaseBackupServiceImpl(
        JdbcTemplate jdbc,
        @Value("${drip.database.backup-dir}") String backupDir,
        @Value("${spring.datasource.url}") String datasourceUrl,
        @Value("${spring.datasource.username}") String datasourceUsername,
        @Value("${spring.datasource.password}") String datasourcePassword,
        @Value("${drip.database.mysqldump-command:mysqldump}") String mysqldumpCommand,
        @Value("${drip.database.mysql-command:mysql}") String mysqlCommand
    ) {
        this.jdbc = jdbc;
        this.backupDir = Path.of(backupDir);
        this.datasourceUrl = datasourceUrl;
        this.datasourceUsername = datasourceUsername;
        this.datasourcePassword = datasourcePassword;
        this.mysqldumpCommand = mysqldumpCommand;
        this.mysqlCommand = mysqlCommand;
    }

    @Override
    public PageResult<Map<String, Object>> page(Map<String, String> q) {
        int page = Math.max(1, parseInt(q.get("page"), 1));
        int pageSize = Math.min(100, Math.max(1, parseInt(q.get("pageSize"), 20)));
        Long total = jdbc.queryForObject("select count(1) from sys_db_backup where 1 = 1", Long.class);
        List<Map<String, Object>> rows = jdbc.queryForList(
            "select * from sys_db_backup where 1 = 1 order by created_at desc limit ?, ?",
            (page - 1) * pageSize,
            pageSize
        );
        return new PageResult<>(rows, total == null ? 0 : total, page, pageSize);
    }

    @Override
    public Long create(Map<String, Object> body, long userId) {
        try {
            Files.createDirectories(backupDir);
            String name = "backup-" + System.currentTimeMillis() + ".sql";
            Path normalizedDir = backupDir.toAbsolutePath().normalize();
            Path target = normalizedDir.resolve(name).normalize();
            if (!target.startsWith(normalizedDir)) {
                throw new BusinessException(400000, "backup path is invalid");
            }
            MysqlTarget mysqlTarget = parseMysqlTarget();
            runProcess(List.of(
                mysqldumpCommand,
                "-h",
                mysqlTarget.host(),
                "-P",
                String.valueOf(mysqlTarget.port()),
                "-u" + datasourceUsername,
                "--single-transaction",
                "--routines",
                "--default-character-set=utf8mb4",
                mysqlTarget.database()
            ), target, null);
            jdbc.update(
                "insert into sys_db_backup (backup_name, file_path, file_size, status, created_by, remark) values (?, ?, ?, ?, ?, ?)",
                name,
                target.toString(),
                Files.size(target),
                "SUCCESS",
                userId,
                stringValue(body, "remark", "")
            );
            return jdbc.queryForObject("select last_insert_id()", Long.class);
        } catch (IOException ex) {
            throw new BusinessException(500000, "failed to create database backup");
        }
    }

    @Override
    public BackupFile download(long id) throws IOException {
        Map<String, Object> backup = detail(id);
        Path path = Path.of(stringOf(backup.get("file_path"))).normalize();
        if (!Files.exists(path)) {
            throw new BusinessException(404000, "backup file not found");
        }
        return new BackupFile(stringOf(backup.get("backup_name")), Files.readAllBytes(path));
    }

    @Override
    public void restore(long id, Map<String, Object> body) {
        Map<String, Object> backup = detail(id);
        if (!Boolean.TRUE.equals(body.get("confirmed"))) {
            throw new BusinessException(400000, "restore confirmation is required");
        }
        Path path = Path.of(stringOf(backup.get("file_path"))).toAbsolutePath().normalize();
        if (!Files.exists(path)) {
            throw new BusinessException(404000, "backup file not found");
        }
        MysqlTarget mysqlTarget = parseMysqlTarget();
        runProcess(List.of(
            mysqlCommand,
            "-h",
            mysqlTarget.host(),
            "-P",
            String.valueOf(mysqlTarget.port()),
            "-u" + datasourceUsername,
            "--default-character-set=utf8mb4",
            mysqlTarget.database()
        ), null, path);
    }

    @Override
    public void delete(long id) {
        Map<String, Object> backup = detail(id);
        Path path = Path.of(stringOf(backup.get("file_path"))).toAbsolutePath().normalize();
        try {
            Files.deleteIfExists(path);
        } catch (IOException ex) {
            throw new BusinessException(500000, "failed to delete backup file");
        }
        jdbc.update("delete from sys_db_backup where id = ?", id);
    }

    private Map<String, Object> detail(long id) {
        List<Map<String, Object>> rows = jdbc.queryForList("select * from sys_db_backup where id = ?", id);
        if (rows.isEmpty()) {
            throw new BusinessException(404000, "resource not found");
        }
        return new LinkedHashMap<>(rows.getFirst());
    }

    private MysqlTarget parseMysqlTarget() {
        try {
            String raw = datasourceUrl.substring("jdbc:".length());
            URI uri = URI.create(raw);
            String database = uri.getPath() == null ? "" : uri.getPath().replaceFirst("^/", "");
            if (database.isBlank()) {
                throw new BusinessException(500000, "MySQL database name is missing");
            }
            return new MysqlTarget(uri.getHost(), uri.getPort() > 0 ? uri.getPort() : 3306, database);
        } catch (IllegalArgumentException | IndexOutOfBoundsException ex) {
            throw new BusinessException(500000, "Invalid MySQL datasource url");
        }
    }

    private void runProcess(List<String> command, Path stdoutFile, Path stdinFile) {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.environment().put("MYSQL_PWD", datasourcePassword);
        Path tempStdout = null;
        Path tempStderr = null;
        Path actualStdout = stdoutFile;
        Path actualStderr;
        if (actualStdout == null) {
            try {
                tempStdout = Files.createTempFile("drip-db-command-", ".out");
                actualStdout = tempStdout;
            } catch (IOException ex) {
                throw new BusinessException(500000, "database command temp output is not available");
            }
        }
        try {
            tempStderr = Files.createTempFile("drip-db-command-", ".err");
            actualStderr = tempStderr;
        } catch (IOException ex) {
            throw new BusinessException(500000, "database command temp error output is not available");
        }
        builder.redirectOutput(actualStdout.toFile());
        if (stdinFile != null) {
            builder.redirectInput(stdinFile.toFile());
        }
        builder.redirectError(actualStderr.toFile());
        builder.redirectErrorStream(false);
        try {
            if (stdoutFile != null) {
                Files.deleteIfExists(stdoutFile);
            }
            Process process = builder.start();
            int exit = process.waitFor();
            String stdout = Files.exists(actualStdout) ? Files.readString(actualStdout, StandardCharsets.UTF_8) : "";
            String stderr = Files.exists(actualStderr) ? Files.readString(actualStderr, StandardCharsets.UTF_8) : "";
            if (exit != 0) {
                if (stdoutFile != null) {
                    Files.deleteIfExists(stdoutFile);
                }
                throw new BusinessException(500000, "database command failed: " + (stderr.isBlank() ? stdout : stderr));
            }
        } catch (IOException ex) {
            throw new BusinessException(500000, "database command is not available: " + command.getFirst());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new BusinessException(500000, "database command interrupted");
        } finally {
            try {
                if (tempStdout != null) {
                    Files.deleteIfExists(tempStdout);
                }
                if (tempStderr != null) {
                    Files.deleteIfExists(tempStderr);
                }
            } catch (IOException ignored) {
                // Temporary command output cleanup must not mask the command result.
            }
        }
    }

    private record MysqlTarget(String host, int port, String database) {
    }
}
