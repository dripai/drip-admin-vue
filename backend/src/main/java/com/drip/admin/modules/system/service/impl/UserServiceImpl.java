package com.drip.admin.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drip.admin.common.exception.BusinessException;
import com.drip.admin.common.response.PageResult;
import com.drip.admin.modules.system.entity.SysRoleEntity;
import com.drip.admin.modules.system.entity.SysUserEntity;
import com.drip.admin.modules.system.entity.SysUserRoleEntity;
import com.drip.admin.modules.system.mapper.SysRoleMapper;
import com.drip.admin.modules.system.mapper.SysUserMapper;
import com.drip.admin.modules.system.mapper.SysUserRoleMapper;
import com.drip.admin.modules.system.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.drip.admin.shared.utils.AdminUtils.currentUserId;
import static com.drip.admin.shared.utils.AdminUtils.hashPassword;
import static com.drip.admin.shared.utils.AdminUtils.intOf;
import static com.drip.admin.shared.utils.AdminUtils.longOf;
import static com.drip.admin.shared.utils.AdminUtils.parseInt;
import static com.drip.admin.shared.utils.AdminUtils.requireNonBlank;
import static com.drip.admin.shared.utils.AdminUtils.stringValue;

@Service
public class UserServiceImpl extends ServiceImpl<SysUserMapper, SysUserEntity> implements UserService {
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;

    public UserServiceImpl(SysRoleMapper roleMapper, SysUserRoleMapper userRoleMapper) {
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
    }

    @Override
    public PageResult<SysUserEntity> page(Map<String, String> q) {
        int page = Math.max(1, parseInt(q.get("page"), 1));
        int pageSize = Math.min(100, Math.max(1, parseInt(q.get("pageSize"), 20)));
        QueryWrapper<SysUserEntity> wrapper = new QueryWrapper<>();
        likeIfPresent(wrapper, "username", q.get("username"));
        likeIfPresent(wrapper, "real_name", q.getOrDefault("real_name", q.get("realName")));
        likeIfPresent(wrapper, "phone", q.get("phone"));
        likeIfPresent(wrapper, "status", q.get("status"));
        likeIfPresent(wrapper, "dept_id", q.getOrDefault("dept_id", q.get("deptId")));
        likeIfPresent(wrapper, "created_at", q.getOrDefault("created_at", q.get("createdAt")));
        wrapper.orderByDesc("created_at");
        Page<SysUserEntity> result = page(new Page<>(page, pageSize), wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, pageSize);
    }

    @Override
    public SysUserEntity detail(long id) {
        SysUserEntity entity = getById(id);
        if (entity == null) {
            throw new BusinessException(404000, "?????");
        }
        return entity;
    }

    @Override
    @Transactional
    public Long create(Map<String, Object> body) {
        requireNonBlank(body, "username");
        requireNonBlank(body, "real_name", "realName");
        SysUserEntity entity = new SysUserEntity();
        apply(entity, body);
        String password = stringValue(body, "password", "Admin@123456");
        String salt = "salt" + System.nanoTime();
        entity.setPasswordSalt(salt);
        entity.setPasswordHash(hashPassword(password, salt));
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        save(entity);
        return entity.getId();
    }

    @Override
    @Transactional
    public void update(long id, Map<String, Object> body) {
        assertNotSuperAdminTarget(id);
        detail(id);
        SysUserEntity entity = new SysUserEntity();
        entity.setId(id);
        apply(entity, body);
        updateById(entity);
    }

    @Override
    @Transactional
    public void delete(long id) {
        if (currentUserId() == id) {
            throw new BusinessException(400000, "??????????");
        }
        assertNotSuperAdminTarget(id);
        detail(id);
        removeById(id);
    }

    @Override
    @Transactional
    public void updateStatus(long id, int status) {
        if (id == currentUserId() && status != 1) {
            throw new BusinessException(400000, "??????????");
        }
        assertNotSuperAdminTarget(id);
        detail(id);
        SysUserEntity entity = new SysUserEntity();
        entity.setId(id);
        entity.setStatus(status);
        updateById(entity);
    }

    @Override
    @Transactional
    public void resetPassword(long id, String password) {
        assertNotSuperAdminTarget(id);
        detail(id);
        String salt = "salt" + System.nanoTime();
        SysUserEntity entity = new SysUserEntity();
        entity.setId(id);
        entity.setPasswordSalt(salt);
        entity.setPasswordHash(hashPassword(password, salt));
        updateById(entity);
    }

    @Override
    @Transactional
    public void assignRoles(long userId, List<Long> roleIds) {
        assertExistingRoles(roleIds);
        assertNotSuperAdminTarget(userId);
        detail(userId);
        userRoleMapper.delete(new QueryWrapper<SysUserRoleEntity>().eq("user_id", userId));
        for (Long roleId : roleIds) {
            SysUserRoleEntity relation = new SysUserRoleEntity();
            relation.setUserId(userId);
            relation.setRoleId(roleId);
            userRoleMapper.insert(relation);
        }
    }

    private void assertNotSuperAdminTarget(long userId) {
        if (roleCodes(currentUserId()).contains("SUPER_ADMIN")) {
            return;
        }
        if (roleCodes(userId).contains("SUPER_ADMIN")) {
            throw new BusinessException(403000, "??????????????");
        }
    }

    private List<String> roleCodes(long userId) {
        List<Long> roleIds = userRoleMapper.selectList(new QueryWrapper<SysUserRoleEntity>().eq("user_id", userId))
            .stream().map(SysUserRoleEntity::getRoleId).toList();
        if (roleIds.isEmpty()) {
            return List.of();
        }
        return roleMapper.selectBatchIds(roleIds).stream()
            .filter(role -> Objects.equals(role.getDeleted(), 0) && Objects.equals(role.getStatus(), 1))
            .map(SysRoleEntity::getRoleCode)
            .toList();
    }

    private void assertExistingRoles(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        List<Long> uniqueIds = roleIds.stream().filter(Objects::nonNull).distinct().toList();
        if (uniqueIds.size() != roleIds.size()) {
            throw new BusinessException(400000, "?????");
        }
        Long count = roleMapper.selectCount(new QueryWrapper<SysRoleEntity>().in("id", uniqueIds));
        if (count == null || count != uniqueIds.size()) {
            throw new BusinessException(400000, "?????");
        }
    }

    private static void apply(SysUserEntity entity, Map<String, Object> body) {
        setString(body, "username", entity::setUsername);
        setString(body, "real_name", "realName", entity::setRealName);
        setString(body, "phone", entity::setPhone);
        setString(body, "email", entity::setEmail);
        setInteger(body, "status", entity::setStatus);
        setLong(body, "dept_id", "deptId", entity::setDeptId);
        setString(body, "remark", entity::setRemark);
    }

    private static void likeIfPresent(QueryWrapper<SysUserEntity> wrapper, String column, String value) {
        if (value != null && !value.isBlank()) {
            wrapper.like(column, value);
        }
    }

    private static void setString(Map<String, Object> body, String key, java.util.function.Consumer<String> setter) {
        if (body.containsKey(key)) setter.accept(String.valueOf(body.get(key)));
    }

    private static void setString(Map<String, Object> body, String snake, String camel, java.util.function.Consumer<String> setter) {
        Object value = body.containsKey(snake) ? body.get(snake) : body.get(camel);
        if (value != null) setter.accept(String.valueOf(value));
    }

    private static void setInteger(Map<String, Object> body, String key, java.util.function.Consumer<Integer> setter) {
        if (body.containsKey(key)) setter.accept(intOf(body.get(key)));
    }

    private static void setLong(Map<String, Object> body, String snake, String camel, java.util.function.Consumer<Long> setter) {
        Object value = body.containsKey(snake) ? body.get(snake) : body.get(camel);
        if (value != null) setter.accept(longOf(value));
    }
}
