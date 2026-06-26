package com.drip.admin.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drip.admin.common.exception.BusinessException;
import com.drip.admin.modules.system.entity.SysMenuEntity;
import com.drip.admin.modules.system.mapper.SysMenuMapper;
import com.drip.admin.modules.system.service.MenuService;
import com.drip.admin.modules.system.vo.MenuTreeVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.drip.admin.shared.utils.AdminUtils.intOf;
import static com.drip.admin.shared.utils.AdminUtils.longOf;
import static com.drip.admin.shared.utils.AdminUtils.requireNonBlank;

@Service
public class MenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenuEntity> implements MenuService {
    @Override
    public List<MenuTreeVo> tree() {
        List<SysMenuEntity> rows = list(new QueryWrapper<SysMenuEntity>()
            .eq("status", 1)
            .orderByAsc("sort", "id"));
        return buildTree(rows.stream().filter(row -> !"BUTTON".equals(row.getType())).map(this::toTreeVo).toList());
    }

    @Override
    public SysMenuEntity detail(long id) {
        SysMenuEntity entity = getById(id);
        if (entity == null) throw new BusinessException(404000, "?????");
        return entity;
    }

    @Override
    @Transactional
    public Long create(Map<String, Object> body) {
        requireNonBlank(body, "name");
        requireNonBlank(body, "type");
        SysMenuEntity entity = new SysMenuEntity();
        apply(entity, body);
        save(entity);
        return entity.getId();
    }

    @Override
    @Transactional
    public void update(long id, Map<String, Object> body) {
        detail(id);
        SysMenuEntity entity = new SysMenuEntity();
        entity.setId(id);
        apply(entity, body);
        updateById(entity);
    }

    @Override
    @Transactional
    public void delete(long id) {
        Long count = count(new QueryWrapper<SysMenuEntity>().eq("parent_id", id));
        if (count != null && count > 0) throw new BusinessException(400301, "????????????");
        detail(id);
        removeById(id);
    }

    @Override
    @Transactional
    public void updateStatus(long id, int status) {
        detail(id);
        SysMenuEntity entity = new SysMenuEntity();
        entity.setId(id);
        entity.setStatus(status);
        updateById(entity);
    }

    private static void apply(SysMenuEntity entity, Map<String, Object> body) {
        setLong(body, "parent_id", "parentId", entity::setParentId);
        setString(body, "name", entity::setName);
        setString(body, "type", entity::setType);
        setString(body, "path", entity::setPath);
        setString(body, "component", entity::setComponent);
        setString(body, "permission_code", "permissionCode", entity::setPermissionCode);
        setString(body, "icon", entity::setIcon);
        setInteger(body, "sort", entity::setSort);
        setInteger(body, "visible", entity::setVisible);
        setInteger(body, "status", entity::setStatus);
    }

    private MenuTreeVo toTreeVo(SysMenuEntity entity) {
        MenuTreeVo vo = new MenuTreeVo();
        vo.setId(entity.getId());
        vo.setParentId(entity.getParentId());
        vo.setName(entity.getName());
        vo.setType(entity.getType());
        vo.setPath(entity.getPath());
        vo.setComponent(entity.getComponent());
        vo.setPermissionCode(entity.getPermissionCode());
        vo.setIcon(entity.getIcon());
        vo.setSort(entity.getSort());
        vo.setVisible(entity.getVisible());
        return vo;
    }

    private static List<MenuTreeVo> buildTree(List<MenuTreeVo> rows) {
        Map<Long, MenuTreeVo> byId = new LinkedHashMap<>();
        rows.forEach(row -> byId.put(row.getId(), row));
        List<MenuTreeVo> roots = new ArrayList<>();
        for (MenuTreeVo row : rows) {
            Long parentId = row.getParentId() == null ? 0L : row.getParentId();
            if (parentId == 0 || !byId.containsKey(parentId)) roots.add(row);
            else byId.get(parentId).getChildren().add(row);
        }
        return roots;
    }

    private static void setString(Map<String, Object> body, String key, java.util.function.Consumer<String> setter) { if (body.containsKey(key)) setter.accept(String.valueOf(body.get(key))); }
    private static void setString(Map<String, Object> body, String snake, String camel, java.util.function.Consumer<String> setter) { Object value = body.containsKey(snake) ? body.get(snake) : body.get(camel); if (value != null) setter.accept(String.valueOf(value)); }
    private static void setInteger(Map<String, Object> body, String key, java.util.function.Consumer<Integer> setter) { if (body.containsKey(key)) setter.accept(intOf(body.get(key))); }
    private static void setLong(Map<String, Object> body, String snake, String camel, java.util.function.Consumer<Long> setter) { Object value = body.containsKey(snake) ? body.get(snake) : body.get(camel); if (value != null) setter.accept(longOf(value)); }
}
