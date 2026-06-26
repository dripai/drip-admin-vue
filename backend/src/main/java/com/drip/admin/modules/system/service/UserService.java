package com.drip.admin.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.drip.admin.common.response.PageResult;
import com.drip.admin.modules.system.dto.UserQuery;
import com.drip.admin.modules.system.dto.UserSaveRequest;
import com.drip.admin.modules.system.entity.SysUserEntity;
import com.drip.admin.modules.system.vo.UserListVo;

import java.util.List;

public interface UserService extends IService<SysUserEntity> {
    PageResult<UserListVo> page(UserQuery query);
    SysUserEntity detail(long id);
    Long create(UserSaveRequest request);
    void update(long id, UserSaveRequest request);
    void delete(long id);
    void updateStatus(long id, int status);
    void resetPassword(long id, String password);
    void assignRoles(long userId, List<Long> roleIds);
}
