package com.drip.admin.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drip.admin.common.exception.BusinessException;
import com.drip.admin.common.response.PageResult;
import com.drip.admin.infrastructure.redis.LoginAttemptService;
import com.drip.admin.modules.system.dto.UserQuery;
import com.drip.admin.modules.system.dto.UserSaveRequest;
import com.drip.admin.modules.system.entity.SysDeptEntity;
import com.drip.admin.modules.system.entity.SysRoleEntity;
import com.drip.admin.modules.system.entity.SysUserEntity;
import com.drip.admin.modules.system.entity.SysUserRoleEntity;
import com.drip.admin.modules.system.mapper.SysDeptMapper;
import com.drip.admin.modules.system.mapper.SysRoleMapper;
import com.drip.admin.modules.system.mapper.SysUserMapper;
import com.drip.admin.modules.system.mapper.SysUserRoleMapper;
import com.drip.admin.modules.system.service.UserService;
import com.drip.admin.modules.system.vo.DeptSummaryVo;
import com.drip.admin.modules.system.vo.RoleSummaryVo;
import com.drip.admin.modules.system.vo.UserListVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.drip.admin.shared.utils.AdminUtils.currentUserId;
import static com.drip.admin.shared.utils.AdminUtils.hashPassword;

@Service
public class UserServiceImpl extends ServiceImpl<SysUserMapper, SysUserEntity> implements UserService {
    private final SysDeptMapper deptMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final LoginAttemptService loginAttemptService;

    public UserServiceImpl(SysDeptMapper deptMapper, SysRoleMapper roleMapper, SysUserRoleMapper userRoleMapper, LoginAttemptService loginAttemptService) {
        this.deptMapper = deptMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    public PageResult<UserListVo> page(UserQuery query) {
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
        return new PageResult<>(toUserList(result.getRecords()), result.getTotal(), page, pageSize);
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
        assertExistingDept(request.getDeptId());
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
        assertExistingDept(request.getDeptId());
        SysUserEntity entity = new SysUserEntity();
        entity.setId(id);
        apply(entity, request);
        update(entity, new UpdateWrapper<SysUserEntity>().eq("id", id).set("dept_id", request.getDeptId()));
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
    public void unlockLogin(long id) {
        SysUserEntity user = detail(id);
        loginAttemptService.unlock(user.getUsername());
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

    private List<UserListVo> toUserList(List<SysUserEntity> users) {
        if (users.isEmpty()) return List.of();
        List<Long> userIds = users.stream().map(SysUserEntity::getId).toList();
        List<SysUserRoleEntity> relations = userRoleMapper.selectList(new QueryWrapper<SysUserRoleEntity>().in("user_id", userIds));
        Map<Long, SysRoleEntity> rolesById = rolesById(relations);
        Map<Long, SysDeptEntity> deptsById = deptsById(users);
        Map<Long, List<RoleSummaryVo>> rolesByUserId = relations.stream()
            .filter(relation -> rolesById.containsKey(relation.getRoleId()))
            .collect(Collectors.groupingBy(
                SysUserRoleEntity::getUserId,
                Collectors.mapping(relation -> toRoleSummary(rolesById.get(relation.getRoleId())), Collectors.toList())
            ));
        return users.stream()
            .map(user -> new UserListVo(
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getPhone(),
                user.getEmail(),
                user.getStatus(),
                toDeptSummary(deptsById.get(user.getDeptId())),
                rolesByUserId.getOrDefault(user.getId(), List.of()),
                user.getCreatedAt(),
                user.getLastLoginAt()
            ))
            .toList();
    }

    private Map<Long, SysRoleEntity> rolesById(List<SysUserRoleEntity> relations) {
        List<Long> roleIds = relations.stream().map(SysUserRoleEntity::getRoleId).filter(Objects::nonNull).distinct().toList();
        if (roleIds.isEmpty()) return Map.of();
        return roleMapper.selectBatchIds(roleIds).stream()
            .filter(role -> Objects.equals(role.getDeleted(), 0))
            .collect(Collectors.toMap(SysRoleEntity::getId, role -> role));
    }

    private Map<Long, SysDeptEntity> deptsById(List<SysUserEntity> users) {
        List<Long> deptIds = users.stream().map(SysUserEntity::getDeptId).filter(Objects::nonNull).distinct().toList();
        if (deptIds.isEmpty()) return Map.of();
        return deptMapper.selectBatchIds(deptIds).stream()
            .filter(dept -> Objects.equals(dept.getDeleted(), 0))
            .collect(Collectors.toMap(SysDeptEntity::getId, dept -> dept));
    }

    private static DeptSummaryVo toDeptSummary(SysDeptEntity dept) {
        return dept == null ? null : new DeptSummaryVo(dept.getId(), dept.getDeptName());
    }

    private static RoleSummaryVo toRoleSummary(SysRoleEntity role) {
        return new RoleSummaryVo(role.getId(), role.getRoleName(), role.getRoleCode());
    }

    private void assertExistingRoles(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) return;
        List<Long> uniqueIds = roleIds.stream().filter(Objects::nonNull).distinct().toList();
        if (uniqueIds.size() != roleIds.size()) throw new BusinessException(400000, "operation failed");
        Long count = roleMapper.selectCount(new QueryWrapper<SysRoleEntity>().in("id", uniqueIds));
        if (count == null || count != uniqueIds.size()) throw new BusinessException(400000, "operation failed");
    }

    private void assertExistingDept(Long deptId) {
        if (deptId == null) return;
        Long count = deptMapper.selectCount(new QueryWrapper<SysDeptEntity>().eq("id", deptId));
        if (count == null || count == 0) throw new BusinessException(400000, "部门不存在");
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
