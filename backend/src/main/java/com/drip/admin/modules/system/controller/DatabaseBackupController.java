package com.drip.admin.modules.system.controller;

import com.drip.admin.common.log.OperationLog;
import com.drip.admin.common.response.ApiResponse;
import com.drip.admin.common.response.BackupFile;
import com.drip.admin.common.response.PageResult;
import com.drip.admin.common.security.RequirePermission;
import com.drip.admin.modules.system.dto.DatabaseBackupCreateRequest;
import com.drip.admin.modules.system.dto.DatabaseBackupQuery;
import com.drip.admin.modules.system.dto.DatabaseRestoreRequest;
import com.drip.admin.modules.system.entity.SysDbBackupEntity;
import com.drip.admin.modules.system.service.DatabaseBackupService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import static com.drip.admin.shared.utils.AdminUtils.currentUserId;

@RestController
@RequestMapping("/system")
public class DatabaseBackupController {
    private final DatabaseBackupService databaseBackupService;

    public DatabaseBackupController(DatabaseBackupService databaseBackupService) {
        this.databaseBackupService = databaseBackupService;
    }

    @GetMapping("/databaseBackup")
    @RequirePermission("system:database:backup:list")
    public ApiResponse<PageResult<SysDbBackupEntity>> backups(@Valid DatabaseBackupQuery query) {
        return ApiResponse.success(databaseBackupService.page(query));
    }

    @PostMapping("/databaseBackup")
    @RequirePermission("system:database:backup:create")
    @OperationLog(module = "数据库备份", action = "创建备份")
    public ApiResponse<Long> createBackup(@Valid @RequestBody(required = false) DatabaseBackupCreateRequest request) {
        return ApiResponse.success(databaseBackupService.create(request, currentUserId()));
    }

    @GetMapping(value = "/databaseBackup/{id}/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @RequirePermission("system:database:backup:download")
    public ResponseEntity<byte[]> downloadBackup(@PathVariable long id) throws IOException {
        BackupFile file = databaseBackupService.download(id);
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=\"" + file.name() + "\"")
            .body(file.content());
    }

    @PostMapping("/databaseBackup/{id}/restore")
    @RequirePermission("system:database:backup:restore")
    @OperationLog(module = "数据库备份", action = "恢复备份")
    public ApiResponse<Void> restoreBackup(@PathVariable long id, @Valid @RequestBody DatabaseRestoreRequest request) {
        databaseBackupService.restore(id, request);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/databaseBackup/{id}")
    @RequirePermission("system:database:backup:delete")
    @OperationLog(module = "数据库备份", action = "删除备份记录")
    public ApiResponse<Void> deleteBackup(@PathVariable long id) {
        databaseBackupService.delete(id);
        return ApiResponse.success(null);
    }
}
