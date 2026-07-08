package com.drip.admin.modules.system.security;

import cn.dev33.satoken.stp.StpInterface;
import com.drip.admin.modules.system.service.AuthService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SystemPermissionProvider implements StpInterface {
    private final AuthService authService;

    public SystemPermissionProvider(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return authService.permissionCodes(Long.parseLong(String.valueOf(loginId)));
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return authService.roleCodes(Long.parseLong(String.valueOf(loginId)));
    }
}
