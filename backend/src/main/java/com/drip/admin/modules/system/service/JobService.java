package com.drip.admin.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.drip.admin.common.response.PageResult;
import com.drip.admin.modules.system.entity.SysJobEntity;
import com.drip.admin.modules.system.entity.SysJobRunLogEntity;

import java.util.Map;

public interface JobService extends IService<SysJobEntity> {
    PageResult<SysJobEntity> page(Map<String, String> q);

    SysJobEntity detail(long id);

    Long create(Map<String, Object> body);

    void update(long id, Map<String, Object> body);

    void delete(long id);

    void updateStatus(long id, int status);

    void run(long id);

    PageResult<SysJobRunLogEntity> runLogs(long jobId, Map<String, String> q);
}
