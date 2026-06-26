package com.drip.admin.modules.system.service;

import com.drip.admin.modules.system.dto.LoginRequest;
import com.drip.admin.modules.system.dto.PasswordRequest;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

public interface AuthService {
    Map<String, Object> login(LoginRequest request, HttpServletRequest servletRequest);

    void logout(HttpServletRequest request);

    Map<String, Object> me(long userId);

    void changePassword(long userId, PasswordRequest request);

    List<String> roleCodes(long userId);

    List<String> permissionCodes(long userId);
}
