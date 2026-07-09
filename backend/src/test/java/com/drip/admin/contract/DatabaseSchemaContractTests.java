package com.drip.admin.contract;

import cn.dev33.satoken.stp.StpUtil;
import com.drip.admin.modules.system.service.SystemLogWriteService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DatabaseSchemaContractTests {
    @Test
    void baselineSchemaUsesApplicationAssignedSnowflakeIds() throws Exception {
        String baselineSql = Files.readString(Path.of("..", "scripts", "db", "schema.sql"));

        assertFalse(baselineSql.contains("AUTO_INCREMENT"));
        assertFalse(baselineSql.matches("(?s).*ENGINE=InnoDB\\d+.*"));
        assertFalse(baselineSql.contains("flyway_schema_history"));
    }

    @Test
    void databaseScriptsKeepSingleBaselineSql() throws Exception {
        List<Path> sqlFiles = new ArrayList<>();

        try (var paths = Files.walk(Path.of("..", "scripts", "db"))) {
            for (Path path : paths.filter(path -> path.toString().endsWith(".sql")).toList()) {
                sqlFiles.add(path.getFileName());
            }
        }

        assertEquals(List.of(Path.of("schema.sql")), sqlFiles);
    }

    @Test
    void backendResourcesDoNotOwnDatabaseSql() throws Exception {
        List<Path> sqlFiles = new ArrayList<>();

        try (var paths = Files.walk(Path.of("src/main/resources"))) {
            for (Path path : paths.filter(path -> path.toString().endsWith(".sql")).toList()) {
                sqlFiles.add(path);
            }
        }

        assertEquals(List.of(), sqlFiles);
    }

    @Test
    void javaSourceDoesNotContainDatabaseDdl() throws Exception {
        Pattern ddl = Pattern.compile("(?is)\\b(create|alter|drop|truncate)\\s+(table|index|database)\\b");
        List<Path> violations = new ArrayList<>();

        try (var paths = Files.walk(Path.of("src/main/java"))) {
            for (Path path : paths.filter(path -> path.toString().endsWith(".java")).toList()) {
                if (ddl.matcher(Files.readString(path)).find()) {
                    violations.add(path);
                }
            }
        }

        assertEquals(List.of(), violations);
    }

    @Test
    void jdbcLogWritesIncludeApplicationAssignedIds() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        SystemLogWriteService logService = new SystemLogWriteService(jdbc);
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("JUnit");

        logService.login(7L, "demo", "Demo", "LOGIN", "SUCCESS", null, request, "web");
        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::isLogin).thenReturn(false);
            logService.operation("module", "action", "POST", "/demo", "{}", "SUCCESS", null, 12L);
        }

        verify(jdbc).update(
            contains("insert into sys_login_log (id,"),
            anyLong(), eq(7L), eq("demo"), eq("Demo"), eq("LOGIN"), eq("SUCCESS"), any(), eq("10.0.0.1"), eq("JUnit"), eq("web")
        );
        verify(jdbc).update(
            contains("insert into sys_operation_log (id,"),
            anyLong(), any(), any(), eq("module"), eq("action"), eq("POST"), eq("/demo"), eq("{}"), eq("SUCCESS"), any(), eq(12L)
        );
    }

    @Test
    void rawSystemTableInsertsIncludeApplicationAssignedIds() throws Exception {
        Pattern rawSystemInsert = Pattern.compile("(?is)insert\\s+into\\s+sys_[a-z_]+\\s*\\((?!\\s*id\\s*,)");
        List<Path> violations = new ArrayList<>();

        try (var paths = Files.walk(Path.of("src/main/java"))) {
            for (Path path : paths.filter(path -> path.toString().endsWith(".java")).toList()) {
                String source = Files.readString(path);
                if (rawSystemInsert.matcher(source).find()) {
                    violations.add(path);
                }
            }
        }

        assertEquals(List.of(), violations);
    }
}
