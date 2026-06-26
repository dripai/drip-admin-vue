package com.drip.admin.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drip.admin.common.exception.BusinessException;
import com.drip.admin.modules.system.dto.MenuSaveRequest;
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

@Service
public class MenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenuEntity> implements MenuService {
    @Override
    public List<MenuTreeVo> tree() {
        List<SysMenuEntity> rows = list(new QueryWrapper<SysMenuEntity>().eq("status", 1).orderByAsc("sort", "id"));
        return buildTree(rows.stream().filter(row -> !"BUTTON".equals(row.getType())).map(this::toTreeVo).toList());
    }

    @Override
    public SysMenuEntity detail(long id) { SysMenuEntity entity = getById(id); if (entity == null) throw new BusinessException(404000, "?????"); return entity; }

    @Override
    @Transactional
    public Long create(MenuSaveRequest request) { requireText(request.getName(), "name"); requireText(request.getType(), "type"); SysMenuEntity entity = new SysMenuEntity(); apply(entity, request); save(entity); return entity.getId(); }

    @Override
    @Transactional
    public void update(long id, MenuSaveRequest request) { detail(id); SysMenuEntity entity = new SysMenuEntity(); entity.setId(id); apply(entity, request); updateById(entity); }

    @Override
    @Transactional
    public void delete(long id) { Long count = count(new QueryWrapper<SysMenuEntity>().eq("parent_id", id)); if (count != null && count > 0) throw new BusinessException(400301, "?????????"); detail(id); removeById(id); }

    @Override
    @Transactional
    public void updateStatus(long id, int status) { detail(id); SysMenuEntity entity = new SysMenuEntity(); entity.setId(id); entity.setStatus(status); updateById(entity); }

    private static void apply(SysMenuEntity entity, MenuSaveRequest request) {
        entity.setParentId(request.getParentId()); entity.setName(request.getName()); entity.setType(request.getType()); entity.setPath(request.getPath());
        entity.setComponent(request.getComponent()); entity.setPermissionCode(request.getPermissionCode()); entity.setIcon(request.getIcon());
        entity.setSort(request.getSort()); entity.setVisible(request.getVisible()); entity.setStatus(request.getStatus());
    }

    private MenuTreeVo toTreeVo(SysMenuEntity entity) {
        MenuTreeVo vo = new MenuTreeVo(); vo.setId(entity.getId()); vo.setParentId(entity.getParentId()); vo.setName(entity.getName()); vo.setType(entity.getType());
        vo.setPath(entity.getPath()); vo.setComponent(entity.getComponent()); vo.setPermissionCode(entity.getPermissionCode()); vo.setIcon(entity.getIcon()); vo.setSort(entity.getSort()); vo.setVisible(entity.getVisible()); return vo;
    }

    private static List<MenuTreeVo> buildTree(List<MenuTreeVo> rows) {
        Map<Long, MenuTreeVo> byId = new LinkedHashMap<>(); rows.forEach(row -> byId.put(row.getId(), row));
        List<MenuTreeVo> roots = new ArrayList<>();
        for (MenuTreeVo row : rows) { Long parentId = row.getParentId() == null ? 0L : row.getParentId(); if (parentId == 0 || !byId.containsKey(parentId)) roots.add(row); else byId.get(parentId).getChildren().add(row); }
        return roots;
    }
    private static void requireText(String value, String field) { if (value == null || value.isBlank()) throw new BusinessException(400000, field + "????"); }
}
