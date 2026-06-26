package com.drip.admin.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.drip.admin.modules.system.dto.LoginRequest;
import com.drip.admin.modules.system.dto.PasswordRequest;
import com.drip.admin.modules.system.entity.SysUserEntity;
import com.drip.admin.modules.system.vo.AuthLoginVo;
import com.drip.admin.modules.system.vo.AuthMeVo;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface AuthService extends IService<SysUserEntity> {
    AuthLoginVo login(LoginRequest request, HttpServletRequest servletRequest);

    void logout(HttpServletRequest request);

    AuthMeVo me(long userId);

    void changePassword(long userId, PasswordRequest request);

    List<String> roleCodes(long userId);

    List<String> permissionCodes(long userId);
}
