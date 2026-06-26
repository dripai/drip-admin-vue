package com.drip.admin.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.drip.admin.common.response.PageResult;
import com.drip.admin.modules.system.dto.ConfigQuery;
import com.drip.admin.modules.system.dto.ConfigSaveRequest;
import com.drip.admin.modules.system.entity.SysConfigEntity;

public interface ConfigService extends IService<SysConfigEntity> {
    PageResult<SysConfigEntity> page(ConfigQuery query);
    SysConfigEntity detail(long id);
    Long create(ConfigSaveRequest request);
    void update(long id, ConfigSaveRequest request);
    void delete(long id);
    void updateStatus(long id, int status);
}
