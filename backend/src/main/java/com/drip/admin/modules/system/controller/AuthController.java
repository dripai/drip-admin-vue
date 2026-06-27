package com.drip.admin.modules.system.controller;

import com.drip.admin.common.log.OperationLog;
import com.drip.admin.common.response.ApiResponse;
import com.drip.admin.modules.system.dto.LoginRequest;
import com.drip.admin.modules.system.dto.PasswordRequest;
import com.drip.admin.modules.system.dto.ProfileUpdateRequest;
import com.drip.admin.modules.system.service.AuthService;
import com.drip.admin.modules.system.vo.AuthLoginVo;
import com.drip.admin.modules.system.vo.AuthMeVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static com.drip.admin.shared.utils.AdminUtils.*;

@RestController
@RequestMapping("/system")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<AuthLoginVo> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.success(authService.login(request, servletRequest));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        authService.logout(request);
        return ApiResponse.success(null);
    }

    @GetMapping("/me")
    public ApiResponse<AuthMeVo> me() {
        return ApiResponse.success(authService.me(currentUserId()));
    }

    @PutMapping("/password")
    public ApiResponse<Void> password(@Valid @RequestBody PasswordRequest request) {
        authService.changePassword(currentUserId(), request);
        return ApiResponse.success(null);
    }

    @PutMapping("/profile")
    @OperationLog(module = "个人中心", action = "编辑资料")
    public ApiResponse<Void> profile(@Valid @RequestBody ProfileUpdateRequest request) {
        authService.updateProfile(currentUserId(), request);
        return ApiResponse.success(null);
    }
}
