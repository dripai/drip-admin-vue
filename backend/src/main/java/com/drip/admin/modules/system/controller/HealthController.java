package com.drip.admin.modules.system.controller;

import com.drip.admin.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import com.drip.admin.modules.system.vo.HealthVo;

@RestController
@RequestMapping("/api")
public class HealthController {
    @GetMapping("/health")
    public ApiResponse<HealthVo> health() {
        return ApiResponse.success(new HealthVo("UP", "drip-admin-backend", Instant.now().toString()));
    }
}
