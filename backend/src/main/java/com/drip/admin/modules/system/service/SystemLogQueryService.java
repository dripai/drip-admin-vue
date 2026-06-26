package com.drip.admin.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.drip.admin.common.response.PageResult;
import com.drip.admin.modules.system.dto.LoginLogQuery;
import com.drip.admin.modules.system.dto.OperationLogQuery;
import com.drip.admin.modules.system.entity.SysLoginLogEntity;
import com.drip.admin.modules.system.entity.SysOperationLogEntity;

public interface SystemLogQueryService extends IService<SysLoginLogEntity> {
    PageResult<SysLoginLogEntity> loginLogs(LoginLogQuery query);
    SysLoginLogEntity loginLog(long id);
    PageResult<SysOperationLogEntity> operationLogs(OperationLogQuery query);
    SysOperationLogEntity operationLog(long id);
}
