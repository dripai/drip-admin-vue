package com.drip.admin.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drip.admin.common.exception.BusinessException;
import com.drip.admin.common.response.PageResult;
import com.drip.admin.modules.system.dto.DictItemSaveRequest;
import com.drip.admin.modules.system.dto.DictTypeQuery;
import com.drip.admin.modules.system.dto.DictTypeSaveRequest;
import com.drip.admin.modules.system.entity.SysDictItemEntity;
import com.drip.admin.modules.system.entity.SysDictTypeEntity;
import com.drip.admin.modules.system.mapper.SysDictItemMapper;
import com.drip.admin.modules.system.mapper.SysDictTypeMapper;
import com.drip.admin.modules.system.service.DictService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DictServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictTypeEntity> implements DictService {
    private final SysDictItemMapper dictItemMapper;
    private final Map<String, List<SysDictItemEntity>> dictCache = new HashMap<>();

    public DictServiceImpl(SysDictItemMapper dictItemMapper) { this.dictItemMapper = dictItemMapper; }

    @Override
    public PageResult<SysDictTypeEntity> types(DictTypeQuery query) {
        int page = query.pageOrDefault(); int pageSize = query.pageSizeOrDefault(); QueryWrapper<SysDictTypeEntity> wrapper = new QueryWrapper<>();
        likeIfPresent(wrapper, "dict_name", query.getDictName()); likeIfPresent(wrapper, "dict_code", query.getDictCode()); eqIfPresent(wrapper, "status", query.getStatus()); wrapper.orderByDesc("created_at");
        Page<SysDictTypeEntity> result = page(new Page<>(page, pageSize), wrapper); return new PageResult<>(result.getRecords(), result.getTotal(), page, pageSize);
    }

    @Override public List<SysDictItemEntity> items(long dictTypeId) { typeDetail(dictTypeId); return dictItemMapper.selectList(new QueryWrapper<SysDictItemEntity>().eq("dict_type_id", dictTypeId).orderByAsc("sort", "id")); }
    @Override public SysDictTypeEntity typeDetail(long id) { SysDictTypeEntity entity = getById(id); if (entity == null) throw new BusinessException(404000, "???????"); return entity; }
    @Override public SysDictItemEntity itemDetail(long id) { SysDictItemEntity entity = dictItemMapper.selectById(id); if (entity == null) throw new BusinessException(404000, "??????"); return entity; }
    @Override @Transactional public Long createType(DictTypeSaveRequest request) { requireText(request.getDictName(), "dictName"); requireText(request.getDictCode(), "dictCode"); SysDictTypeEntity entity = new SysDictTypeEntity(); applyType(entity, request); save(entity); refreshCache(); return entity.getId(); }
    @Override @Transactional public void updateType(long id, DictTypeSaveRequest request) { typeDetail(id); SysDictTypeEntity entity = new SysDictTypeEntity(); entity.setId(id); applyType(entity, request); updateById(entity); refreshCache(); }
    @Override @Transactional public void deleteType(long id) { typeDetail(id); Long count = dictItemMapper.selectCount(new QueryWrapper<SysDictItemEntity>().eq("dict_type_id", id)); if (count != null && count > 0) throw new BusinessException(400501, "??????????"); removeById(id); refreshCache(); }
    @Override @Transactional public Long createItem(DictItemSaveRequest request) { requireText(request.getLabel(), "label"); requireText(request.getValue(), "value"); if (request.getDictTypeId() == null) throw new BusinessException(400000, "dictTypeId????"); typeDetail(request.getDictTypeId()); SysDictItemEntity entity = new SysDictItemEntity(); applyItem(entity, request); dictItemMapper.insert(entity); refreshCache(); return entity.getId(); }
    @Override @Transactional public void updateItem(long id, DictItemSaveRequest request) { itemDetail(id); if (request.getDictTypeId() != null) typeDetail(request.getDictTypeId()); SysDictItemEntity entity = new SysDictItemEntity(); entity.setId(id); applyItem(entity, request); dictItemMapper.updateById(entity); refreshCache(); }
    @Override @Transactional public void deleteItem(long id) { SysDictItemEntity item = itemDetail(id); SysDictTypeEntity type = typeDetail(item.getDictTypeId()); if ("common_status".equals(type.getDictCode()) && commonStatusValueReferenced(item.getValue())) throw new BusinessException(400501, "???????"); dictItemMapper.deleteById(id); refreshCache(); }
    @Override @Transactional public void updateItemStatus(long id, int status) { itemDetail(id); SysDictItemEntity entity = new SysDictItemEntity(); entity.setId(id); entity.setStatus(status); dictItemMapper.updateById(entity); refreshCache(); }
    @Override public void refreshCache() { dictCache.clear(); for (SysDictTypeEntity type : list(new QueryWrapper<SysDictTypeEntity>().eq("status", 1))) dictCache.put(type.getDictCode(), dictItemMapper.selectList(new QueryWrapper<SysDictItemEntity>().eq("dict_type_id", type.getId()).orderByAsc("sort", "id"))); }
    private boolean commonStatusValueReferenced(String value) { int status = Integer.parseInt(value); List<String> tables = List.of("sys_user", "sys_role", "sys_menu", "sys_dept", "sys_dict_type", "sys_dict_item", "sys_config", "sys_job"); for (String table : tables) { Long count = baseMapper.selectCount(new QueryWrapper<SysDictTypeEntity>().apply("exists (select 1 from " + table + " where status = {0} and deleted = 0)", status)); if (count != null && count > 0) return true; } return false; }
    private static void applyType(SysDictTypeEntity entity, DictTypeSaveRequest request) { entity.setDictName(request.getDictName()); entity.setDictCode(request.getDictCode()); entity.setStatus(request.getStatus()); entity.setRemark(request.getRemark()); }
    private static void applyItem(SysDictItemEntity entity, DictItemSaveRequest request) { entity.setDictTypeId(request.getDictTypeId()); entity.setLabel(request.getLabel()); entity.setValue(request.getValue()); entity.setColor(request.getColor()); entity.setSort(request.getSort()); entity.setStatus(request.getStatus()); }
    private static void likeIfPresent(QueryWrapper<SysDictTypeEntity> wrapper, String column, String value) { if (value != null && !value.isBlank()) wrapper.like(column, value); }
    private static void eqIfPresent(QueryWrapper<SysDictTypeEntity> wrapper, String column, Object value) { if (value != null) wrapper.eq(column, value); }
    private static void requireText(String value, String field) { if (value == null || value.isBlank()) throw new BusinessException(400000, field + "????"); }
}
