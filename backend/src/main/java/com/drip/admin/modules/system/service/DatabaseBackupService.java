package com.drip.admin.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.drip.admin.common.response.BackupFile;
import com.drip.admin.common.response.PageResult;
import com.drip.admin.modules.system.dto.DatabaseBackupCreateRequest;
import com.drip.admin.modules.system.dto.DatabaseBackupQuery;
import com.drip.admin.modules.system.dto.DatabaseRestoreRequest;
import com.drip.admin.modules.system.entity.SysDbBackupEntity;

import java.io.IOException;

public interface DatabaseBackupService extends IService<SysDbBackupEntity> {
    PageResult<SysDbBackupEntity> page(DatabaseBackupQuery query);
    Long create(DatabaseBackupCreateRequest request, long userId);
    BackupFile download(long id) throws IOException;
    void restore(long id, DatabaseRestoreRequest request);
    void delete(long id);
}
