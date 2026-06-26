package com.drip.admin.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.drip.admin.common.response.PageResult;
import com.drip.admin.modules.system.entity.SysRoleEntity;
import com.drip.admin.modules.system.entity.SysUserEntity;

import java.util.List;
import java.util.Map;

public interface RoleService extends IService<SysRoleEntity> {
    PageResult<SysRoleEntity> page(Map<String, String> q);

    SysRoleEntity detail(long id);

    PageResult<SysUserEntity> users(long roleId, Map<String, String> q);

    Long create(Map<String, Object> body);

    void update(long id, Map<String, Object> body);

    void delete(long id);

    void updateStatus(long id, int status);

    void assignMenus(long roleId, List<Long> menuIds);
}
