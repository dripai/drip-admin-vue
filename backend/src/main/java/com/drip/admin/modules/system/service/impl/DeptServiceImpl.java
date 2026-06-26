package com.drip.admin.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drip.admin.common.exception.BusinessException;
import com.drip.admin.modules.system.dto.DeptSaveRequest;
import com.drip.admin.modules.system.entity.SysDeptEntity;
import com.drip.admin.modules.system.entity.SysUserEntity;
import com.drip.admin.modules.system.mapper.SysDeptMapper;
import com.drip.admin.modules.system.mapper.SysUserMapper;
import com.drip.admin.modules.system.service.DeptService;
import com.drip.admin.modules.system.vo.DeptTreeVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class DeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDeptEntity> implements DeptService {
    private final SysUserMapper userMapper;
    public DeptServiceImpl(SysUserMapper userMapper) { this.userMapper = userMapper; }

    @Override
    public List<DeptTreeVo> tree() { List<SysDeptEntity> rows = list(new QueryWrapper<SysDeptEntity>().orderByAsc("sort", "id")); return buildTree(rows.stream().map(this::toTreeVo).toList()); }

    @Override
    public SysDeptEntity detail(long id) { SysDeptEntity entity = getById(id); if (entity == null) throw new BusinessException(404000, "operation failed"); return entity; }

    @Override
    @Transactional
    public Long create(DeptSaveRequest request) { requireText(request.getDeptName(), "deptName"); requireText(request.getDeptCode(), "deptCode"); SysDeptEntity entity = new SysDeptEntity(); apply(entity, request); save(entity); return entity.getId(); }

    @Override
    @Transactional
    public void update(long id, DeptSaveRequest request) { detail(id); assertValidParent(id, request); SysDeptEntity entity = new SysDeptEntity(); entity.setId(id); apply(entity, request); updateById(entity); }

    @Override
    @Transactional
    public void delete(long id) { detail(id); Long childCount = count(new QueryWrapper<SysDeptEntity>().eq("parent_id", id)); if (childCount != null && childCount > 0) throw new BusinessException(400401, "operation failed"); Long userCount = userMapper.selectCount(new QueryWrapper<SysUserEntity>().eq("dept_id", id)); if (userCount != null && userCount > 0) throw new BusinessException(400401, "operation failed"); removeById(id); }

    @Override
    @Transactional
    public void updateStatus(long id, int status) { detail(id); SysDeptEntity entity = new SysDeptEntity(); entity.setId(id); entity.setStatus(status); updateById(entity); }

    private void assertValidParent(long id, DeptSaveRequest request) { Long parentId = request.getParentId(); if (parentId == null) return; if (parentId == id || descendantDeptIds(id).contains(parentId)) throw new BusinessException(400000, "operation failed"); }
    private Set<Long> descendantDeptIds(long id) { Set<Long> result = new HashSet<>(); collectDept(id, result); return result; }
    private void collectDept(long id, Set<Long> result) { for (SysDeptEntity child : list(new QueryWrapper<SysDeptEntity>().eq("parent_id", id))) { result.add(child.getId()); collectDept(child.getId(), result); } }

    private DeptTreeVo toTreeVo(SysDeptEntity entity) { DeptTreeVo vo = new DeptTreeVo(); vo.setId(entity.getId()); vo.setParentId(entity.getParentId()); vo.setDeptName(entity.getDeptName()); vo.setDeptCode(entity.getDeptCode()); vo.setLeaderUserId(entity.getLeaderUserId()); vo.setSort(entity.getSort()); vo.setStatus(entity.getStatus()); vo.setCreatedAt(entity.getCreatedAt()); vo.setUpdatedAt(entity.getUpdatedAt()); return vo; }
    private static List<DeptTreeVo> buildTree(List<DeptTreeVo> rows) { Map<Long, DeptTreeVo> byId = new LinkedHashMap<>(); rows.forEach(row -> byId.put(row.getId(), row)); List<DeptTreeVo> roots = new ArrayList<>(); for (DeptTreeVo row : rows) { Long parentId = row.getParentId() == null ? 0L : row.getParentId(); if (parentId == 0 || !byId.containsKey(parentId)) roots.add(row); else byId.get(parentId).getChildren().add(row); } return roots; }
    private static void apply(SysDeptEntity entity, DeptSaveRequest request) { entity.setParentId(request.getParentId()); entity.setDeptName(request.getDeptName()); entity.setDeptCode(request.getDeptCode()); entity.setLeaderUserId(request.getLeaderUserId()); entity.setSort(request.getSort()); entity.setStatus(request.getStatus()); }
    private static void requireText(String value, String field) { if (value == null || value.isBlank()) throw new BusinessException(400000, field + " is required"); }
}
