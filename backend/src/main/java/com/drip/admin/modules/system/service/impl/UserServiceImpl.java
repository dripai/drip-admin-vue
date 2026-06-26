package com.drip.admin.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drip.admin.common.exception.BusinessException;
import com.drip.admin.common.response.PageResult;
import com.drip.admin.modules.system.dto.UserQuery;
import com.drip.admin.modules.system.dto.UserSaveRequest;
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
import java.util.Objects;

import static com.drip.admin.shared.utils.AdminUtils.currentUserId;
import static com.drip.admin.shared.utils.AdminUtils.hashPassword;

@Service
public class UserServiceImpl extends ServiceImpl<SysUserMapper, SysUserEntity> implements UserService {
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;

    public UserServiceImpl(SysRoleMapper roleMapper, SysUserRoleMapper userRoleMapper) {
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
    }

    @Override
    public PageResult<SysUserEntity> page(UserQuery query) {
        int page = query.pageOrDefault();
        int pageSize = query.pageSizeOrDefault();
        QueryWrapper<SysUserEntity> wrapper = new QueryWrapper<>();
        likeIfPresent(wrapper, "username", query.getUsername());
        likeIfPresent(wrapper, "real_name", query.getRealName());
        likeIfPresent(wrapper, "phone", query.getPhone());
        eqIfPresent(wrapper, "status", query.getStatus());
        eqIfPresent(wrapper, "dept_id", query.getDeptId());
        if (query.getCreatedFrom() != null) wrapper.ge("created_at", query.getCreatedFrom());
        if (query.getCreatedTo() != null) wrapper.le("created_at", query.getCreatedTo());
        if (query.getRoleId() != null) {
            List<Long> userIds = userRoleMapper.selectList(new QueryWrapper<SysUserRoleEntity>().eq("role_id", query.getRoleId()))
                .stream().map(SysUserRoleEntity::getUserId).distinct().toList();
            if (userIds.isEmpty()) return new PageResult<>(List.of(), 0, page, pageSize);
            wrapper.in("id", userIds);
        }
        wrapper.orderByDesc("created_at");
        Page<SysUserEntity> result = page(new Page<>(page, pageSize), wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, pageSize);
    }

    @Override
    public SysUserEntity detail(long id) {
        SysUserEntity entity = getById(id);
        if (entity == null) throw new BusinessException(404000, "operation failed");
        return entity;
    }

    @Override
    @Transactional
    public Long create(UserSaveRequest request) {
        requireText(request.getUsername(), "username");
        requireText(request.getRealName(), "realName");
        SysUserEntity entity = new SysUserEntity();
        apply(entity, request);
        String password = request.getPassword() == null || request.getPassword().isBlank() ? "Admin@123456" : request.getPassword();
        String salt = "salt" + System.nanoTime();
        entity.setPasswordSalt(salt);
        entity.setPasswordHash(hashPassword(password, salt));
        if (entity.getStatus() == null) entity.setStatus(1);
        save(entity);
        return entity.getId();
    }

    @Override
    @Transactional
    public void update(long id, UserSaveRequest request) {
        assertNotSuperAdminTarget(id);
        detail(id);
        SysUserEntity entity = new SysUserEntity();
        entity.setId(id);
        apply(entity, request);
        updateById(entity);
    }

    @Override
    @Transactional
    public void delete(long id) {
        if (currentUserId() == id) throw new BusinessException(400000, "operation failed");
        assertNotSuperAdminTarget(id);
        detail(id);
        removeById(id);
    }

    @Override
    @Transactional
    public void updateStatus(long id, int status) {
        if (id == currentUserId() && status != 1) throw new BusinessException(400000, "operation failed");
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
        if (roleCodes(currentUserId()).contains("SUPER_ADMIN")) return;
        if (roleCodes(userId).contains("SUPER_ADMIN")) throw new BusinessException(403000, "operation failed");
    }

    private List<String> roleCodes(long userId) {
        List<Long> roleIds = userRoleMapper.selectList(new QueryWrapper<SysUserRoleEntity>().eq("user_id", userId))
            .stream().map(SysUserRoleEntity::getRoleId).toList();
        if (roleIds.isEmpty()) return List.of();
        return roleMapper.selectBatchIds(roleIds).stream()
            .filter(role -> Objects.equals(role.getDeleted(), 0) && Objects.equals(role.getStatus(), 1))
            .map(SysRoleEntity::getRoleCode)
            .toList();
    }

    private void assertExistingRoles(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) return;
        List<Long> uniqueIds = roleIds.stream().filter(Objects::nonNull).distinct().toList();
        if (uniqueIds.size() != roleIds.size()) throw new BusinessException(400000, "operation failed");
        Long count = roleMapper.selectCount(new QueryWrapper<SysRoleEntity>().in("id", uniqueIds));
        if (count == null || count != uniqueIds.size()) throw new BusinessException(400000, "operation failed");
    }

    private static void apply(SysUserEntity entity, UserSaveRequest request) {
        entity.setUsername(request.getUsername());
        entity.setRealName(request.getRealName());
        entity.setPhone(request.getPhone());
        entity.setEmail(request.getEmail());
        entity.setStatus(request.getStatus());
        entity.setDeptId(request.getDeptId());
        entity.setRemark(request.getRemark());
    }

    private static void likeIfPresent(QueryWrapper<SysUserEntity> wrapper, String column, String value) { if (value != null && !value.isBlank()) wrapper.like(column, value); }
    private static void eqIfPresent(QueryWrapper<SysUserEntity> wrapper, String column, Object value) { if (value != null) wrapper.eq(column, value); }
    private static void requireText(String value, String field) { if (value == null || value.isBlank()) throw new BusinessException(400000, field + " is required"); }
}
