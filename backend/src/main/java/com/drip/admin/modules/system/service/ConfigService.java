package com.drip.admin.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.drip.admin.common.response.PageResult;
import com.drip.admin.modules.system.entity.SysConfigEntity;

import java.util.Map;

public interface ConfigService extends IService<SysConfigEntity> {
    PageResult<SysConfigEntity> page(Map<String, String> q);

    SysConfigEntity detail(long id);

    Long create(Map<String, Object> body);

    void update(long id, Map<String, Object> body);

    void delete(long id);

    void updateStatus(long id, int status);
}
