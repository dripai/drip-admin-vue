package com.drip.admin.modules.system.controller;

import com.drip.admin.common.log.OperationLog;
import com.drip.admin.common.response.ApiResponse;
import com.drip.admin.common.response.PageResult;
import com.drip.admin.common.security.RequirePermission;
import com.drip.admin.modules.system.service.OnlineUserService;
import com.drip.admin.modules.system.vo.OnlineUserVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/system")
public class OnlineUserController {
    private final OnlineUserService onlineUserService;

    public OnlineUserController(OnlineUserService onlineUserService) {
        this.onlineUserService = onlineUserService;
    }

    @GetMapping("/online-users")
    @RequirePermission("system:online:list")
    public ApiResponse<PageResult<OnlineUserVo>> onlineUsers(@RequestParam Map<String, String> q) {
        return ApiResponse.success(onlineUserService.page(q));
    }

    @GetMapping("/online-users/{tokenId}")
    @RequirePermission("system:online:list")
    public ApiResponse<OnlineUserVo> onlineUser(@PathVariable String tokenId) {
        return ApiResponse.success(onlineUserService.detail(tokenId));
    }

    @PostMapping("/online-users/{tokenId}/kickout")
    @RequirePermission("system:online:kickout")
    @OperationLog(module = "在线用户", action = "强制下线")
    public ApiResponse<Void> kickout(@PathVariable String tokenId) {
        onlineUserService.kickout(tokenId);
        return ApiResponse.success(null);
    }
}
