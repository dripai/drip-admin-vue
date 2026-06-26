package com.drip.admin.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.drip.admin.common.response.PageResult;
import com.drip.admin.modules.system.entity.SysUserEntity;

import java.util.List;
import java.util.Map;

public interface UserService extends IService<SysUserEntity> {
    PageResult<SysUserEntity> page(Map<String, String> q);

    SysUserEntity detail(long id);

    Long create(Map<String, Object> body);

    void update(long id, Map<String, Object> body);

    void delete(long id);

    void updateStatus(long id, int status);

    void resetPassword(long id, String password);

    void assignRoles(long userId, List<Long> roleIds);
}
