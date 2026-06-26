package com.drip.admin.modules.system.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.drip.admin.common.exception.BusinessException;
import com.drip.admin.common.log.LogService;
import com.drip.admin.infrastructure.redis.LoginAttemptService;
import com.drip.admin.infrastructure.redis.OnlineSessionService;
import com.drip.admin.modules.system.dto.LoginRequest;
import com.drip.admin.modules.system.dto.PasswordRequest;
import com.drip.admin.modules.system.mapper.SysMenuMapper;
import com.drip.admin.modules.system.mapper.SysRoleMapper;
import com.drip.admin.modules.system.mapper.SysRoleMenuMapper;
import com.drip.admin.modules.system.mapper.SysUserMapper;
import com.drip.admin.modules.system.mapper.SysUserRoleMapper;
import com.drip.admin.modules.system.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.drip.admin.shared.utils.AdminUtils.buildTree;
import static com.drip.admin.shared.utils.AdminUtils.currentUserId;
import static com.drip.admin.shared.utils.AdminUtils.hashPassword;
import static com.drip.admin.shared.utils.AdminUtils.intOf;
import static com.drip.admin.shared.utils.AdminUtils.longOf;
import static com.drip.admin.shared.utils.AdminUtils.stringOf;

@Service
public class AuthServiceImpl implements AuthService {
    private final JdbcTemplate jdbc;
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysMenuMapper menuMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final LogService logService;
    private final OnlineSessionService onlineSessionService;
    private final LoginAttemptService loginAttemptService;
    private final long idleTimeout;
    private final long maxDuration;

    public AuthServiceImpl(
        JdbcTemplate jdbc,
        SysUserMapper userMapper,
        SysRoleMapper roleMapper,
        SysUserRoleMapper userRoleMapper,
        SysMenuMapper menuMapper,
        SysRoleMenuMapper roleMenuMapper,
        LogService logService,
        OnlineSessionService onlineSessionService,
        LoginAttemptService loginAttemptService,
        @Value("${drip.session.idle-timeout-seconds}") long idleTimeout,
        @Value("${drip.session.max-duration-seconds}") long maxDuration
    ) {
        this.jdbc = jdbc;
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.menuMapper = menuMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.logService = logService;
        this.onlineSessionService = onlineSessionService;
        this.loginAttemptService = loginAttemptService;
        this.idleTimeout = idleTimeout;
        this.maxDuration = maxDuration;
    }

    @Override
    @Transactional
    public Map<String, Object> login(LoginRequest request, HttpServletRequest servletRequest) {
        loginAttemptService.assertNotLocked(request.username());
        Map<String, Object> user = findUserByUsername(request.username());
        if (user.isEmpty()) {
            logService.login(null, request.username(), null, "LOGIN", "FAIL", "用户名或密码错误", servletRequest, request.deviceType());
            loginAttemptService.recordFailure(request.username());
            throw new BusinessException(401000, "用户名或密码错误");
        }
        if (intOf(user.get("status")) != 1 || intOf(user.get("deleted")) == 1) {
            logService.login(longOf(user.get("id")), request.username(), stringOf(user.get("real_name")), "LOGIN", "FAIL", "用户已禁用或删除", servletRequest, request.deviceType());
            loginAttemptService.recordFailure(request.username());
            throw new BusinessException(401000, "用户名或密码错误");
        }
        String expected = hashPassword(request.password(), stringOf(user.get("password_salt")));
        if (!expected.equals(stringOf(user.get("password_hash")))) {
            logService.login(longOf(user.get("id")), request.username(), stringOf(user.get("real_name")), "LOGIN", "FAIL", "用户名或密码错误", servletRequest, request.deviceType());
            loginAttemptService.recordFailure(request.username());
            throw new BusinessException(401000, "用户名或密码错误");
        }
        loginAttemptService.clear(request.username());
        Long userId = longOf(user.get("id"));
        StpUtil.login(userId);
        String token = StpUtil.getTokenValue();
        LocalDateTime now = LocalDateTime.now();
        StpUtil.getSession().set("deviceType", request.deviceType());
        StpUtil.getSession().set("loginAt", now.toString());
        StpUtil.getSession().set("lastActiveAt", now.toString());
        StpUtil.getSession().set("tokenId", token);
        jdbc.update("update sys_user set last_login_at = now() where id = ?", userId);
        logService.login(userId, request.username(), stringOf(user.get("real_name")), "LOGIN", "SUCCESS", null, servletRequest, request.deviceType());
        onlineSessionService.register(userId, user, token, request.deviceType(), idleTimeout, maxDuration, servletRequest);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("token", token);
        data.put("expireAt", now.plusSeconds(idleTimeout).atZone(ZoneId.systemDefault()).toInstant().toString());
        data.put("idleTimeout", idleTimeout);
        data.put("maxSessionDuration", maxDuration);
        data.put("deviceType", request.deviceType());
        return data;
    }

    @Override
    @Transactional
    public void logout(HttpServletRequest request) {
        Long userId = currentUserId();
        Map<String, Object> user = userDetail(userId);
        String deviceType = String.valueOf(StpUtil.getSession().get("deviceType", ""));
        logService.login(userId, stringOf(user.get("username")), stringOf(user.get("real_name")), "LOGOUT", "SUCCESS", null, request, deviceType);
        onlineSessionService.remove(StpUtil.getTokenValue());
        StpUtil.logout();
    }

    @Override
    public Map<String, Object> me(long userId) {
        Map<String, Object> user = userDetail(userId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", user.get("id"));
        result.put("username", user.get("username"));
        result.put("realName", user.get("real_name"));
        result.put("avatar", user.get("avatar"));
        result.put("deptId", user.get("dept_id"));
        result.put("roles", roleCodes(userId));
        result.put("permissions", permissionCodes(userId));
        result.put("menus", menuTree(userId));
        return result;
    }

    @Override
    @Transactional
    public void changePassword(long userId, PasswordRequest request) {
        Map<String, Object> user = userDetail(userId);
        String currentHash = hashPassword(request.oldPassword(), stringOf(user.get("password_salt")));
        if (!currentHash.equals(stringOf(user.get("password_hash")))) {
            throw new BusinessException(400000, "原密码错误");
        }
        String salt = "salt" + System.nanoTime();
        jdbc.update("update sys_user set password_salt = ?, password_hash = ? where id = ?", salt, hashPassword(request.newPassword(), salt), userId);
    }

    @Override
    public List<String> roleCodes(long userId) {
        return jdbc.queryForList("""
            select r.role_code
            from sys_role r
            join sys_user_role ur on ur.role_id = r.id
            where ur.user_id = ? and r.deleted = 0 and r.status = 1
            """, String.class, userId);
    }

    @Override
    public List<String> permissionCodes(long userId) {
        if (roleCodes(userId).contains("SUPER_ADMIN")) {
            return jdbc.queryForList("select permission_code from sys_menu where deleted = 0 and status = 1 and permission_code is not null", String.class);
        }
        return jdbc.queryForList("""
            select distinct m.permission_code
            from sys_menu m
            join sys_role_menu rm on rm.menu_id = m.id
            join sys_user_role ur on ur.role_id = rm.role_id
            join sys_role r on r.id = ur.role_id
            where ur.user_id = ? and m.deleted = 0 and m.status = 1 and r.deleted = 0 and r.status = 1 and m.permission_code is not null
            """, String.class, userId);
    }

    private Map<String, Object> findUserByUsername(String username) {
        List<Map<String, Object>> rows = jdbc.queryForList("select * from sys_user where username = ? and deleted = 0", username);
        return rows.isEmpty() ? Map.of() : rows.getFirst();
    }

    private Map<String, Object> userDetail(long userId) {
        List<Map<String, Object>> rows = jdbc.queryForList("select * from sys_user where id = ? and deleted = 0", userId);
        if (rows.isEmpty()) {
            throw new BusinessException(404000, "资源不存在");
        }
        return rows.getFirst();
    }

    private List<Map<String, Object>> menuTree(Long userId) {
        List<Map<String, Object>> rows;
        if (userId == null || roleCodes(userId).contains("SUPER_ADMIN")) {
            rows = jdbc.queryForList("select id, parent_id, name, type, path, component, permission_code, icon, sort, visible from sys_menu where deleted = 0 and status = 1 order by sort asc, id asc");
        } else {
            rows = jdbc.queryForList("""
                select distinct m.id, m.parent_id, m.name, m.type, m.path, m.component, m.permission_code, m.icon, m.sort, m.visible
                from sys_menu m
                join sys_role_menu rm on rm.menu_id = m.id
                join sys_user_role ur on ur.role_id = rm.role_id
                where ur.user_id = ? and m.deleted = 0 and m.status = 1
                order by m.sort asc, m.id asc
                """, userId);
        }
        return buildTree(rows.stream()
            .filter(row -> !"BUTTON".equals(row.get("type")))
            .map(LinkedHashMap::new)
            .collect(Collectors.toList()), "parent_id");
    }
}
