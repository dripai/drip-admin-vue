package com.drip.admin.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.drip.admin.common.response.PageResult;
import com.drip.admin.modules.system.entity.SysLoginLogEntity;
import com.drip.admin.modules.system.entity.SysOperationLogEntity;

import java.util.Map;

public interface SystemLogQueryService extends IService<SysLoginLogEntity> {
    PageResult<SysLoginLogEntity> loginLogs(Map<String, String> q);

    SysLoginLogEntity loginLog(long id);

    PageResult<SysOperationLogEntity> operationLogs(Map<String, String> q);

    SysOperationLogEntity operationLog(long id);
}
