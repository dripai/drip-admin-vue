package com.drip.admin.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drip.admin.common.exception.BusinessException;
import com.drip.admin.common.response.PageResult;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.drip.admin.shared.utils.AdminUtils.intOf;
import static com.drip.admin.shared.utils.AdminUtils.parseInt;
import static com.drip.admin.shared.utils.AdminUtils.requireNonBlank;

@Service
public class RoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRoleEntity> implements RoleService {
    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysMenuMapper menuMapper;
    private final SysRoleMenuMapper roleMenuMapper;

    public RoleServiceImpl(SysUserMapper userMapper, SysUserRoleMapper userRoleMapper, SysMenuMapper menuMapper, SysRoleMenuMapper roleMenuMapper) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.menuMapper = menuMapper;
        this.roleMenuMapper = roleMenuMapper;
    }

    @Override
    public PageResult<SysRoleEntity> page(Map<String, String> q) {
        int page = Math.max(1, parseInt(q.get("page"), 1));
        int pageSize = Math.min(100, Math.max(1, parseInt(q.get("pageSize"), 20)));
        QueryWrapper<SysRoleEntity> wrapper = new QueryWrapper<>();
        likeIfPresent(wrapper, "role_name", q.getOrDefault("role_name", q.get("roleName")));
        likeIfPresent(wrapper, "role_code", q.getOrDefault("role_code", q.get("roleCode")));
        likeIfPresent(wrapper, "status", q.get("status"));
        likeIfPresent(wrapper, "created_at", q.getOrDefault("created_at", q.get("createdAt")));
        wrapper.orderByDesc("created_at");
        Page<SysRoleEntity> result = page(new Page<>(page, pageSize), wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, pageSize);
    }

    @Override
    public SysRoleEntity detail(long id) {
        SysRoleEntity entity = getById(id);
        if (entity == null) throw new BusinessException(404000, "?????");
        return entity;
    }

    @Override
    public PageResult<SysUserEntity> users(long roleId, Map<String, String> q) {
        detail(roleId);
        int page = Math.max(1, parseInt(q.get("page"), 1));
        int pageSize = Math.min(100, Math.max(1, parseInt(q.get("pageSize"), 20)));
        List<Long> userIds = userRoleMapper.selectList(new QueryWrapper<SysUserRoleEntity>().eq("role_id", roleId))
            .stream().map(SysUserRoleEntity::getUserId).toList();
        if (userIds.isEmpty()) return new PageResult<>(List.of(), 0, page, pageSize);
        Page<SysUserEntity> result = userMapper.selectPage(new Page<>(page, pageSize), new QueryWrapper<SysUserEntity>().in("id", userIds).orderByDesc("created_at"));
        return new PageResult<>(result.getRecords(), result.getTotal(), page, pageSize);
    }

    @Override
    @Transactional
    public Long create(Map<String, Object> body) {
        requireNonBlank(body, "role_name", "roleName");
        requireNonBlank(body, "role_code", "roleCode");
        SysRoleEntity entity = new SysRoleEntity();
        apply(entity, body);
        save(entity);
        return entity.getId();
    }

    @Override
    @Transactional
    public void update(long id, Map<String, Object> body) {
        detail(id);
        SysRoleEntity entity = new SysRoleEntity();
        entity.setId(id);
        apply(entity, body);
        updateById(entity);
    }

    @Override
    @Transactional
    public void delete(long id) {
        SysRoleEntity role = detail(id);
        if (Objects.equals(role.getBuiltin(), 1)) throw new BusinessException(400000, "????????");
        Long count = userRoleMapper.selectCount(new QueryWrapper<SysUserRoleEntity>().eq("role_id", id));
        if (count != null && count > 0) throw new BusinessException(409000, "????????????");
        removeById(id);
    }

    @Override
    @Transactional
    public void updateStatus(long id, int status) {
        detail(id);
        SysRoleEntity entity = new SysRoleEntity();
        entity.setId(id);
        entity.setStatus(status);
        updateById(entity);
    }

    @Override
    @Transactional
    public void assignMenus(long roleId, List<Long> menuIds) {
        detail(roleId);
        assertExistingMenus(menuIds);
        roleMenuMapper.delete(new QueryWrapper<SysRoleMenuEntity>().eq("role_id", roleId));
        for (Long menuId : menuIds) {
            SysRoleMenuEntity relation = new SysRoleMenuEntity();
            relation.setRoleId(roleId);
            relation.setMenuId(menuId);
            roleMenuMapper.insert(relation);
        }
    }

    private void assertExistingMenus(List<Long> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) return;
        List<Long> uniqueIds = menuIds.stream().filter(Objects::nonNull).distinct().toList();
        if (uniqueIds.size() != menuIds.size()) throw new BusinessException(400000, "???????");
        Long count = menuMapper.selectCount(new QueryWrapper<SysMenuEntity>().in("id", uniqueIds));
        if (count == null || count != uniqueIds.size()) throw new BusinessException(400000, "???????");
    }

    private static void apply(SysRoleEntity entity, Map<String, Object> body) {
        setString(body, "role_name", "roleName", entity::setRoleName);
        setString(body, "role_code", "roleCode", entity::setRoleCode);
        setInteger(body, "status", entity::setStatus);
        setString(body, "remark", entity::setRemark);
    }

    private static void likeIfPresent(QueryWrapper<SysRoleEntity> wrapper, String column, String value) { if (value != null && !value.isBlank()) wrapper.like(column, value); }
    private static void setString(Map<String, Object> body, String key, java.util.function.Consumer<String> setter) { if (body.containsKey(key)) setter.accept(String.valueOf(body.get(key))); }
    private static void setString(Map<String, Object> body, String snake, String camel, java.util.function.Consumer<String> setter) { Object value = body.containsKey(snake) ? body.get(snake) : body.get(camel); if (value != null) setter.accept(String.valueOf(value)); }
    private static void setInteger(Map<String, Object> body, String key, java.util.function.Consumer<Integer> setter) { if (body.containsKey(key)) setter.accept(intOf(body.get(key))); }
}
