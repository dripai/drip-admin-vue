package com.drip.admin.modules.system.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drip.admin.common.exception.BusinessException;
import com.drip.admin.common.log.LogService;
import com.drip.admin.infrastructure.redis.LoginAttemptService;
import com.drip.admin.infrastructure.redis.OnlineSessionService;
import com.drip.admin.modules.system.dto.LoginRequest;
import com.drip.admin.modules.system.dto.PasswordRequest;
import com.drip.admin.modules.system.dto.ProfileUpdateRequest;
import com.drip.admin.modules.system.entity.SysMenuEntity;
import com.drip.admin.modules.system.entity.SysRoleEntity;
import com.drip.admin.modules.system.entity.SysRoleMenuEntity;
import com.drip.admin.modules.system.entity.SysUserEntity;
import com.drip.admin.modules.system.entity.SysUserRoleEntity;
import com.drip.admin.modules.system.mapper.SysMenuMapper;
import com.drip.admin.modules.system.mapper.SysRoleMapper;
import com.drip.admin.modules.system.mapper.SysRoleMenuMapper;
import com.drip.admin.modules.system.mapper.SysUserMapper;
import com.drip.admin.modules.system.mapper.SysUserRoleMapper;
import com.drip.admin.modules.system.service.AuthService;
import com.drip.admin.modules.system.vo.AuthLoginVo;
import com.drip.admin.modules.system.vo.AuthMeVo;
import com.drip.admin.modules.system.vo.MenuTreeVo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.drip.admin.shared.utils.AdminUtils.currentUserId;
import static com.drip.admin.shared.utils.AdminUtils.hashPassword;

@Service
public class AuthServiceImpl extends ServiceImpl<SysUserMapper, SysUserEntity> implements AuthService {
    private static final String LOGIN_FAILED_MESSAGE = "用户名或密码错误";
    private static final String ACCOUNT_DISABLED_MESSAGE = "账号已禁用";

    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysMenuMapper menuMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final LogService logService;
    private final OnlineSessionService onlineSessionService;
    private final LoginAttemptService loginAttemptService;
    private final long activeTimeout;
    private final long tokenTimeout;

    public AuthServiceImpl(SysRoleMapper roleMapper, SysUserRoleMapper userRoleMapper, SysMenuMapper menuMapper, SysRoleMenuMapper roleMenuMapper, LogService logService, OnlineSessionService onlineSessionService, LoginAttemptService loginAttemptService, @Value("${sa-token.active-timeout}") long activeTimeout, @Value("${sa-token.timeout}") long tokenTimeout) {
        this.roleMapper = roleMapper; this.userRoleMapper = userRoleMapper; this.menuMapper = menuMapper; this.roleMenuMapper = roleMenuMapper; this.logService = logService; this.onlineSessionService = onlineSessionService; this.loginAttemptService = loginAttemptService; this.activeTimeout = activeTimeout; this.tokenTimeout = tokenTimeout;
    }

    @Override
    @Transactional
    public AuthLoginVo login(LoginRequest request, HttpServletRequest servletRequest) {
        SysUserEntity user = getOne(new QueryWrapper<SysUserEntity>().eq("username", request.username()), false);
        if (user == null) { logService.login(null, request.username(), null, "LOGIN", "FAIL", LOGIN_FAILED_MESSAGE, servletRequest, request.deviceType()); throw new BusinessException(401000, LOGIN_FAILED_MESSAGE); }
        if (!Objects.equals(user.getStatus(), 1) || Objects.equals(user.getDeleted(), 1)) { logService.login(user.getId(), request.username(), user.getRealName(), "LOGIN", "FAIL", ACCOUNT_DISABLED_MESSAGE, servletRequest, request.deviceType()); throw new BusinessException(401000, ACCOUNT_DISABLED_MESSAGE); }
        loginAttemptService.assertNotLocked(request.username());
        String expected = hashPassword(request.password(), user.getPasswordSalt());
        if (!expected.equals(user.getPasswordHash())) { logService.login(user.getId(), request.username(), user.getRealName(), "LOGIN", "FAIL", LOGIN_FAILED_MESSAGE, servletRequest, request.deviceType()); int remaining = loginAttemptService.recordFailure(request.username()); throw new BusinessException(401000, LOGIN_FAILED_MESSAGE + "，还剩" + remaining + "次机会"); }
        loginAttemptService.clear(request.username()); Long userId = user.getId(); StpUtil.login(userId); String token = StpUtil.getTokenValue(); LocalDateTime now = LocalDateTime.now();
        StpUtil.getSession().set("deviceType", request.deviceType()); StpUtil.getSession().set("loginAt", now.toString()); StpUtil.getSession().set("lastActiveAt", now.toString()); StpUtil.getSession().set("tokenId", token); StpUtil.getSession().set("username", user.getUsername()); StpUtil.getSession().set("realName", user.getRealName());
        SysUserEntity update = new SysUserEntity(); update.setId(userId); update.setLastLoginAt(now); updateById(update);
        logService.login(userId, request.username(), user.getRealName(), "LOGIN", "SUCCESS", null, servletRequest, request.deviceType());
        onlineSessionService.register(userId, toOnlineMap(user), token, request.deviceType(), activeTimeout, servletRequest);
        return new AuthLoginVo(token, now.plusSeconds(activeTimeout).atZone(ZoneId.systemDefault()).toInstant().toString(), activeTimeout, tokenTimeout, request.deviceType());
    }

    @Override
    @Transactional
    public void logout(HttpServletRequest request) {
        Long userId = currentUserId(); SysUserEntity user = userDetail(userId); String deviceType = String.valueOf(StpUtil.getSession().get("deviceType", ""));
        logService.login(userId, user.getUsername(), user.getRealName(), "LOGOUT", "SUCCESS", null, request, deviceType); onlineSessionService.remove(StpUtil.getTokenValue()); StpUtil.logout();
    }

    @Override
    public AuthMeVo me(long userId) {
        SysUserEntity user = userDetail(userId);
        return new AuthMeVo(user.getId(), user.getUsername(), user.getRealName(), user.getPhone(), user.getEmail(), user.getAvatar(), user.getDeptId(), roleCodes(userId), permissionCodes(userId), menuTree(userId));
    }

    @Override
    @Transactional
    public void changePassword(long userId, PasswordRequest request) {
        SysUserEntity user = userDetail(userId); String currentHash = hashPassword(request.oldPassword(), user.getPasswordSalt());
        if (!currentHash.equals(user.getPasswordHash())) throw new BusinessException(400000, "旧密码错误");
        String salt = "salt" + System.nanoTime(); SysUserEntity update = new SysUserEntity(); update.setId(userId); update.setPasswordSalt(salt); update.setPasswordHash(hashPassword(request.newPassword(), salt)); updateById(update);
    }

    @Override
    @Transactional
    public void updateProfile(long userId, ProfileUpdateRequest request) {
        userDetail(userId);
        SysUserEntity update = new SysUserEntity();
        update.setId(userId);
        update.setRealName(request.realName());
        update.setPhone(trimToEmpty(request.phone()));
        update.setEmail(trimToEmpty(request.email()));
        updateById(update);
        StpUtil.getSession().set("realName", request.realName());
    }

    @Override
    public List<String> roleCodes(long userId) {
        List<Long> roleIds = userRoleMapper.selectList(new QueryWrapper<SysUserRoleEntity>().eq("user_id", userId)).stream().map(SysUserRoleEntity::getRoleId).toList();
        if (roleIds.isEmpty()) return List.of();
        return roleMapper.selectBatchIds(roleIds).stream().filter(role -> Objects.equals(role.getDeleted(), 0) && Objects.equals(role.getStatus(), 1)).map(SysRoleEntity::getRoleCode).toList();
    }

    @Override
    public List<String> permissionCodes(long userId) {
        if (roleCodes(userId).contains("SUPER_ADMIN")) return menuMapper.selectList(new QueryWrapper<SysMenuEntity>().eq("status", 1).isNotNull("permission_code")).stream().map(SysMenuEntity::getPermissionCode).toList();
        List<Long> roleIds = userRoleMapper.selectList(new QueryWrapper<SysUserRoleEntity>().eq("user_id", userId)).stream().map(SysUserRoleEntity::getRoleId).toList();
        if (roleIds.isEmpty()) return List.of();
        List<Long> menuIds = roleMenuMapper.selectList(new QueryWrapper<SysRoleMenuEntity>().in("role_id", roleIds)).stream().map(SysRoleMenuEntity::getMenuId).distinct().toList();
        if (menuIds.isEmpty()) return List.of();
        return menuRowsWithAncestors(menuIds).stream().filter(menu -> Objects.equals(menu.getDeleted(), 0) && Objects.equals(menu.getStatus(), 1) && menu.getPermissionCode() != null).map(SysMenuEntity::getPermissionCode).distinct().toList();
    }

    private SysUserEntity userDetail(long userId) { SysUserEntity user = getById(userId); if (user == null) throw new BusinessException(404000, "operation failed"); return user; }

    private static String trimToEmpty(String value) { return value == null ? "" : value.trim(); }

    private List<MenuTreeVo> menuTree(Long userId) {
        List<SysMenuEntity> rows;
        if (userId == null || roleCodes(userId).contains("SUPER_ADMIN")) rows = menuMapper.selectList(new QueryWrapper<SysMenuEntity>().eq("status", 1).orderByAsc("sort", "id"));
        else {
            List<Long> roleIds = userRoleMapper.selectList(new QueryWrapper<SysUserRoleEntity>().eq("user_id", userId)).stream().map(SysUserRoleEntity::getRoleId).toList();
            if (roleIds.isEmpty()) return List.of();
            List<Long> menuIds = roleMenuMapper.selectList(new QueryWrapper<SysRoleMenuEntity>().in("role_id", roleIds)).stream().map(SysRoleMenuEntity::getMenuId).distinct().toList();
            if (menuIds.isEmpty()) return List.of();
            rows = menuRowsWithAncestors(menuIds);
        }
        return buildTree(rows.stream().filter(row -> !"BUTTON".equals(row.getType())).map(this::toTreeVo).toList());
    }

    private List<SysMenuEntity> menuRowsWithAncestors(List<Long> menuIds) {
        List<SysMenuEntity> allRows = menuMapper.selectList(new QueryWrapper<SysMenuEntity>().eq("status", 1).orderByAsc("sort", "id"));
        Map<Long, SysMenuEntity> byId = new LinkedHashMap<>();
        for (SysMenuEntity row : allRows) byId.put(row.getId(), row);
        Set<Long> visibleIds = new HashSet<>();
        for (Long menuId : menuIds) {
            Long currentId = menuId;
            while (currentId != null && currentId != 0 && byId.containsKey(currentId)) {
                if (!visibleIds.add(currentId)) break;
                currentId = byId.get(currentId).getParentId();
            }
        }
        return allRows.stream().filter(row -> visibleIds.contains(row.getId())).toList();
    }

    private MenuTreeVo toTreeVo(SysMenuEntity entity) { MenuTreeVo vo = new MenuTreeVo(); vo.setId(entity.getId()); vo.setParentId(entity.getParentId()); vo.setName(entity.getName()); vo.setType(entity.getType()); vo.setPath(entity.getPath()); vo.setComponent(entity.getComponent()); vo.setPermissionCode(entity.getPermissionCode()); vo.setIcon(entity.getIcon()); vo.setSort(entity.getSort()); vo.setVisible(entity.getVisible()); vo.setStatus(entity.getStatus()); return vo; }
    private static List<MenuTreeVo> buildTree(List<MenuTreeVo> rows) { Map<Long, MenuTreeVo> byId = new LinkedHashMap<>(); rows.forEach(row -> byId.put(row.getId(), row)); List<MenuTreeVo> roots = new ArrayList<>(); for (MenuTreeVo row : rows) { Long parentId = row.getParentId() == null ? 0L : row.getParentId(); if (parentId == 0 || !byId.containsKey(parentId)) roots.add(row); else byId.get(parentId).getChildren().add(row); } return roots; }
    private static Map<String, Object> toOnlineMap(SysUserEntity user) { Map<String, Object> map = new LinkedHashMap<>(); map.put("username", user.getUsername()); map.put("real_name", user.getRealName()); return map; }
}
