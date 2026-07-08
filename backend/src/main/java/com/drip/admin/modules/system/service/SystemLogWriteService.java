package com.drip.admin.modules.system.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.drip.admin.common.log.OperationLogRecorder;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.drip.admin.shared.utils.AdminUtils.clientIp;
import static com.drip.admin.shared.utils.AdminUtils.currentUserId;

@Service
public class SystemLogWriteService implements OperationLogRecorder {
    private static final int OPERATION_LOG_TEXT_LIMIT = 8192;

    private final JdbcTemplate jdbc;

    public SystemLogWriteService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void login(Long userId, String username, String realName, String loginType, String status,
                      String reason, HttpServletRequest request, String deviceType) {
        jdbc.update("""
            insert into sys_login_log (id, user_id, username, real_name, login_type, status, failure_reason, ip, user_agent, device_type)
            values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """, IdWorker.getId(), userId, username, realName, loginType, status, reason,
            clientIp(request), request.getHeader("User-Agent"), deviceType);
    }

    @Override
    public void operation(String module, String action, String method, String path, String requestParams,
                          String responseStatus, String errorMessage, long costMs) {
        Long userId = null;
        String operatorName = null;
        if (StpUtil.isLogin()) {
            userId = currentUserId();
            String realName = String.valueOf(StpUtil.getSession().get("realName", ""));
            String username = String.valueOf(StpUtil.getSession().get("username", ""));
            operatorName = !realName.isBlank() ? realName : (!username.isBlank() ? username : userDisplayName(userId));
        }
        jdbc.update("""
            insert into sys_operation_log (id, operator_id, operator_name, module, action, method, path, request_params, response_status, error_message, cost_ms)
            values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """, IdWorker.getId(), userId, operatorName, module, action, method, path,
            limitText(requestParams), responseStatus, limitText(errorMessage), costMs);
    }

    private static String limitText(String value) {
        if (value == null || value.length() <= OPERATION_LOG_TEXT_LIMIT) return value;
        return value.substring(0, OPERATION_LOG_TEXT_LIMIT);
    }

    private String userDisplayName(Long userId) {
        if (userId == null) return null;
        List<String> names = jdbc.query("""
            select coalesce(nullif(real_name, ''), nullif(username, ''), cast(id as char))
            from sys_user
            where id = ? and deleted = 0
            """, (rs, rowNum) -> rs.getString(1), userId);
        return names.isEmpty() ? String.valueOf(userId) : names.get(0);
    }
}
