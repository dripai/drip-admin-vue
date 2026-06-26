package com.drip.admin.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drip.admin.common.exception.BusinessException;
import com.drip.admin.common.response.PageResult;
import com.drip.admin.modules.system.dto.RoleQuery;
import com.drip.admin.modules.system.dto.RoleSaveRequest;
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
import com.drip.admin.modules.system.service.RoleService;
import com.drip.admin.modules.system.vo.RolePermissionVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class RoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRoleEntity> implements RoleService {
    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysMenuMapper menuMapper;
    private final SysRoleMenuMapper roleMenuMapper;

    public RoleServiceImpl(SysUserMapper userMapper, SysUserRoleMapper userRoleMapper, SysMenuMapper menuMapper, SysRoleMenuMapper roleMenuMapper) {
        this.userMapper = userMapper; this.userRoleMapper = userRoleMapper; this.menuMapper = menuMapper; this.roleMenuMapper = roleMenuMapper;
    }

    @Override
    public PageResult<SysRoleEntity> page(RoleQuery query) {
        int page = query.pageOrDefault(); int pageSize = query.pageSizeOrDefault();
        QueryWrapper<SysRoleEntity> wrapper = new QueryWrapper<>();
        likeIfPresent(wrapper, "role_name", query.getRoleName()); likeIfPresent(wrapper, "role_code", query.getRoleCode());
        eqIfPresent(wrapper, "status", query.getStatus()); likeIfPresent(wrapper, "created_at", query.getCreatedAt()); wrapper.orderByDesc("created_at");
        Page<SysRoleEntity> result = page(new Page<>(page, pageSize), wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, pageSize);
    }

    @Override
    public SysRoleEntity detail(long id) { SysRoleEntity entity = getById(id); if (entity == null) throw new BusinessException(404000, "operation failed"); return entity; }

    @Override
    public PageResult<SysUserEntity> users(long roleId, RoleQuery query) {
        detail(roleId); int page = query.pageOrDefault(); int pageSize = query.pageSizeOrDefault();
        List<Long> userIds = userRoleMapper.selectList(new QueryWrapper<SysUserRoleEntity>().eq("role_id", roleId)).stream().map(SysUserRoleEntity::getUserId).toList();
        if (userIds.isEmpty()) return new PageResult<>(List.of(), 0, page, pageSize);
        Page<SysUserEntity> result = userMapper.selectPage(new Page<>(page, pageSize), new QueryWrapper<SysUserEntity>().in("id", userIds).orderByDesc("created_at"));
        return new PageResult<>(result.getRecords(), result.getTotal(), page, pageSize);
    }

    @Override
    public RolePermissionVo permissions(long roleId) {
        detail(roleId);
        List<Long> menuIds = roleMenuMapper.selectList(new QueryWrapper<SysRoleMenuEntity>().eq("role_id", roleId))
            .stream()
            .map(SysRoleMenuEntity::getMenuId)
            .toList();
        return new RolePermissionVo(menuIds, List.of());
    }

    @Override
    @Transactional
    public Long create(RoleSaveRequest request) {
        requireText(request.getRoleName(), "roleName"); requireText(request.getRoleCode(), "roleCode");
        SysRoleEntity entity = new SysRoleEntity(); apply(entity, request); save(entity); return entity.getId();
    }

    @Override
    @Transactional
    public void update(long id, RoleSaveRequest request) { detail(id); SysRoleEntity entity = new SysRoleEntity(); entity.setId(id); apply(entity, request); updateById(entity); }

    @Override
    @Transactional
    public void delete(long id) {
        SysRoleEntity role = detail(id); if (Objects.equals(role.getBuiltin(), 1)) throw new BusinessException(400000, "operation failed");
        Long count = userRoleMapper.selectCount(new QueryWrapper<SysUserRoleEntity>().eq("role_id", id));
        if (count != null && count > 0) throw new BusinessException(409000, "operation failed");
        removeById(id);
    }

    @Override
    @Transactional
    public void updateStatus(long id, int status) { detail(id); SysRoleEntity entity = new SysRoleEntity(); entity.setId(id); entity.setStatus(status); updateById(entity); }

    @Override
    @Transactional
    public void assignMenus(long roleId, List<Long> menuIds) {
        detail(roleId); assertExistingMenus(menuIds); roleMenuMapper.delete(new QueryWrapper<SysRoleMenuEntity>().eq("role_id", roleId));
        for (Long menuId : menuIds) { SysRoleMenuEntity relation = new SysRoleMenuEntity(); relation.setRoleId(roleId); relation.setMenuId(menuId); roleMenuMapper.insert(relation); }
    }

    private void assertExistingMenus(List<Long> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) return;
        List<Long> uniqueIds = menuIds.stream().filter(Objects::nonNull).distinct().toList();
        if (uniqueIds.size() != menuIds.size()) throw new BusinessException(400000, "operation failed");
        Long count = menuMapper.selectCount(new QueryWrapper<SysMenuEntity>().in("id", uniqueIds));
        if (count == null || count != uniqueIds.size()) throw new BusinessException(400000, "operation failed");
    }

    private static void apply(SysRoleEntity entity, RoleSaveRequest request) { entity.setRoleName(request.getRoleName()); entity.setRoleCode(request.getRoleCode()); entity.setStatus(request.getStatus()); entity.setRemark(request.getRemark()); }
    private static void likeIfPresent(QueryWrapper<SysRoleEntity> wrapper, String column, String value) { if (value != null && !value.isBlank()) wrapper.like(column, value); }
    private static void eqIfPresent(QueryWrapper<SysRoleEntity> wrapper, String column, Object value) { if (value != null) wrapper.eq(column, value); }
    private static void requireText(String value, String field) { if (value == null || value.isBlank()) throw new BusinessException(400000, field + " is required"); }
}
