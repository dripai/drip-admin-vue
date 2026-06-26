package com.drip.admin.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.drip.admin.common.response.PageResult;
import com.drip.admin.modules.system.dto.RoleQuery;
import com.drip.admin.modules.system.dto.RoleSaveRequest;
import com.drip.admin.modules.system.entity.SysRoleEntity;
import com.drip.admin.modules.system.entity.SysUserEntity;

import java.util.List;

public interface RoleService extends IService<SysRoleEntity> {
    PageResult<SysRoleEntity> page(RoleQuery query);
    SysRoleEntity detail(long id);
    PageResult<SysUserEntity> users(long roleId, RoleQuery query);
    Long create(RoleSaveRequest request);
    void update(long id, RoleSaveRequest request);
    void delete(long id);
    void updateStatus(long id, int status);
    void assignMenus(long roleId, List<Long> menuIds);
}
