package com.drip.admin.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.drip.admin.common.response.PageResult;
import com.drip.admin.modules.system.dto.JobQuery;
import com.drip.admin.modules.system.dto.JobRunLogQuery;
import com.drip.admin.modules.system.dto.JobSaveRequest;
import com.drip.admin.modules.system.entity.SysJobEntity;
import com.drip.admin.modules.system.entity.SysJobRunLogEntity;

public interface JobService extends IService<SysJobEntity> {
    PageResult<SysJobEntity> page(JobQuery query);
    SysJobEntity detail(long id);
    Long create(JobSaveRequest request);
    void update(long id, JobSaveRequest request);
    void delete(long id);
    void updateStatus(long id, int status);
    void run(long id);
    PageResult<SysJobRunLogEntity> runLogs(long jobId, JobRunLogQuery query);
}
