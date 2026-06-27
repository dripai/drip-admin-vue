package com.drip.admin.common.security;

import cn.dev33.satoken.stp.StpInterface;
import com.drip.admin.modules.system.service.AuthService;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static com.drip.admin.shared.utils.AdminUtils.*;

@Component
public class PermissionProvider implements StpInterface {
    private final AuthService authService;

    public PermissionProvider(AuthService authService) {
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
