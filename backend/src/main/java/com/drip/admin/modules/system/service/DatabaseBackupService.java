package com.drip.admin.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.drip.admin.common.response.BackupFile;
import com.drip.admin.common.response.PageResult;
import com.drip.admin.modules.system.entity.SysDbBackupEntity;

import java.io.IOException;
import java.util.Map;

public interface DatabaseBackupService extends IService<SysDbBackupEntity> {
    PageResult<SysDbBackupEntity> page(Map<String, String> q);

    Long create(Map<String, Object> body, long userId);

    BackupFile download(long id) throws IOException;

    void restore(long id, Map<String, Object> body);

    void delete(long id);
}
