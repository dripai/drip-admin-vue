package com.drip.admin.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drip.admin.common.exception.BusinessException;
import com.drip.admin.modules.system.dto.DictItemSaveRequest;
import com.drip.admin.modules.system.dto.DictTypeSaveRequest;
import com.drip.admin.modules.system.entity.SysDictItemEntity;
import com.drip.admin.modules.system.entity.SysDictTypeEntity;
import com.drip.admin.modules.system.mapper.SysDictItemMapper;
import com.drip.admin.modules.system.mapper.SysDictTypeMapper;
import com.drip.admin.modules.system.service.DictService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DictServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictTypeEntity> implements DictService {
    private final SysDictItemMapper dictItemMapper;
    private volatile List<SysDictTypeEntity> dictTypeCache = List.of();
    private volatile Map<Long, List<SysDictItemEntity>> dictItemCache = Map.of();

    public DictServiceImpl(SysDictItemMapper dictItemMapper) { this.dictItemMapper = dictItemMapper; }

    @PostConstruct
    public void initCache() {
        refreshCache();
    }

    @Override
    public List<SysDictTypeEntity> types() {
        return dictTypeCache;
    }

    @Override public List<SysDictItemEntity> items(long dictTypeId) { typeDetail(dictTypeId); return dictItemCache.getOrDefault(dictTypeId, List.of()); }
    @Override public SysDictTypeEntity typeDetail(long id) { return dictTypeCache.stream().filter(type -> type.getId() != null && type.getId() == id).findFirst().orElseThrow(() -> new BusinessException(404000, "operation failed")); }
    @Override public SysDictItemEntity itemDetail(long id) { SysDictItemEntity entity = dictItemMapper.selectById(id); if (entity == null) throw new BusinessException(404000, "operation failed"); return entity; }
    @Override @Transactional public Long createType(DictTypeSaveRequest request) { requireText(request.getDictName(), "dictName"); requireText(request.getDictCode(), "dictCode"); SysDictTypeEntity entity = new SysDictTypeEntity(); applyType(entity, request); if (entity.getBuiltin() == null) entity.setBuiltin(0); save(entity); refreshCache(); return entity.getId(); }
    @Override @Transactional public void updateType(long id, DictTypeSaveRequest request) { typeDetail(id); SysDictTypeEntity entity = new SysDictTypeEntity(); entity.setId(id); applyType(entity, request); updateById(entity); refreshCache(); }
    @Override @Transactional public void deleteType(long id) { SysDictTypeEntity type = typeDetail(id); if (isBuiltin(type.getBuiltin())) throw new BusinessException(400501, "内置字典类型不能删除"); Long count = dictItemMapper.selectCount(new QueryWrapper<SysDictItemEntity>().eq("dict_type_id", id)); if (count != null && count > 0) throw new BusinessException(400501, "字典类型下存在字典项，不能删除"); removeById(id); refreshCache(); }
    @Override @Transactional public Long createItem(DictItemSaveRequest request) { requireText(request.getLabel(), "label"); requireText(request.getValue(), "value"); if (request.getDictTypeId() == null) throw new BusinessException(400000, "dictTypeId is required"); typeDetail(request.getDictTypeId()); if (isDefault(request.getIsDefault())) clearDefaultItem(request.getDictTypeId(), null); SysDictItemEntity entity = new SysDictItemEntity(); applyItem(entity, request); if (entity.getBuiltin() == null) entity.setBuiltin(0); dictItemMapper.insert(entity); refreshCache(); return entity.getId(); }
    @Override @Transactional public void updateItem(long id, DictItemSaveRequest request) { itemDetail(id); if (request.getDictTypeId() != null) typeDetail(request.getDictTypeId()); if (isDefault(request.getIsDefault())) clearDefaultItem(request.getDictTypeId(), id); SysDictItemEntity entity = new SysDictItemEntity(); entity.setId(id); applyItem(entity, request); dictItemMapper.updateById(entity); refreshCache(); }
    @Override @Transactional public void deleteItem(long id) { SysDictItemEntity item = itemDetail(id); if (isBuiltin(item.getBuiltin())) throw new BusinessException(400501, "内置字典项不能删除"); SysDictTypeEntity type = typeDetail(item.getDictTypeId()); if ("common_status".equals(type.getDictCode()) && commonStatusValueReferenced(item.getValue())) throw new BusinessException(400501, "字典项已被使用，不能删除"); dictItemMapper.deleteById(id); refreshCache(); }
    @Override @Transactional public void updateItemStatus(long id, int status) { itemDetail(id); SysDictItemEntity entity = new SysDictItemEntity(); entity.setId(id); entity.setStatus(status); dictItemMapper.updateById(entity); refreshCache(); }
    @Override public synchronized void refreshCache() {
        List<SysDictTypeEntity> types = List.copyOf(list(new QueryWrapper<SysDictTypeEntity>().orderByDesc("created_at")));
        List<SysDictItemEntity> items = dictItemMapper.selectList(new QueryWrapper<SysDictItemEntity>().orderByAsc("sort", "id"));
        Map<Long, List<SysDictItemEntity>> grouped = new LinkedHashMap<>();
        for (SysDictItemEntity item : items) {
            grouped.computeIfAbsent(item.getDictTypeId(), ignored -> new ArrayList<>()).add(item);
        }
        Map<Long, List<SysDictItemEntity>> snapshot = new LinkedHashMap<>();
        for (Map.Entry<Long, List<SysDictItemEntity>> entry : grouped.entrySet()) {
            snapshot.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        dictTypeCache = types;
        dictItemCache = Map.copyOf(snapshot);
    }
    private boolean commonStatusValueReferenced(String value) { int status = Integer.parseInt(value); List<String> softDeleteTables = List.of("sys_user", "sys_role", "sys_menu", "sys_dept", "sys_config", "sys_job"); for (String table : softDeleteTables) { Long count = baseMapper.selectCount(new QueryWrapper<SysDictTypeEntity>().apply("exists (select 1 from " + table + " where status = {0} and deleted = 0)", status)); if (count != null && count > 0) return true; } Long dictTypeCount = baseMapper.selectCount(new QueryWrapper<SysDictTypeEntity>().apply("exists (select 1 from sys_dict_type where status = {0})", status)); if (dictTypeCount != null && dictTypeCount > 0) return true; Long dictItemCount = baseMapper.selectCount(new QueryWrapper<SysDictTypeEntity>().apply("exists (select 1 from sys_dict_item where status = {0})", status)); return dictItemCount != null && dictItemCount > 0; }
    private static void applyType(SysDictTypeEntity entity, DictTypeSaveRequest request) { entity.setDictName(request.getDictName()); entity.setDictCode(request.getDictCode()); entity.setStatus(request.getStatus()); entity.setBuiltin(request.getBuiltin()); entity.setRemark(request.getRemark()); }
    private static boolean isBuiltin(Integer value) { return value != null && value == 1; }
    private void clearDefaultItem(Long dictTypeId, Long excludeId) { UpdateWrapper<SysDictItemEntity> wrapper = new UpdateWrapper<SysDictItemEntity>().eq("dict_type_id", dictTypeId).eq("is_default", 1).set("is_default", 0); if (excludeId != null) wrapper.ne("id", excludeId); dictItemMapper.update(null, wrapper); }
    private static boolean isDefault(Integer value) { return value != null && value == 1; }
    private static void applyItem(SysDictItemEntity entity, DictItemSaveRequest request) { entity.setDictTypeId(request.getDictTypeId()); entity.setLabel(request.getLabel()); entity.setValue(request.getValue()); entity.setIsDefault(request.getIsDefault()); entity.setSort(request.getSort()); entity.setStatus(request.getStatus()); entity.setBuiltin(request.getBuiltin()); }
    private static void requireText(String value, String field) { if (value == null || value.isBlank()) throw new BusinessException(400000, field + " is required"); }
}
