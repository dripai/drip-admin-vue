package com.drip.admin.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drip.admin.common.exception.BusinessException;
import com.drip.admin.common.response.PageResult;
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
import java.util.Objects;

import static com.drip.admin.shared.utils.AdminUtils.intOf;
import static com.drip.admin.shared.utils.AdminUtils.longOf;
import static com.drip.admin.shared.utils.AdminUtils.parseInt;
import static com.drip.admin.shared.utils.AdminUtils.requireNonBlank;

@Service
public class DictServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictTypeEntity> implements DictService {
    private final SysDictItemMapper dictItemMapper;
    private final Map<String, List<SysDictItemEntity>> dictCache = new HashMap<>();

    public DictServiceImpl(SysDictItemMapper dictItemMapper) {
        this.dictItemMapper = dictItemMapper;
    }

    @Override
    public PageResult<SysDictTypeEntity> types(Map<String, String> q) {
        int page = Math.max(1, parseInt(q.get("page"), 1));
        int pageSize = Math.min(100, Math.max(1, parseInt(q.get("pageSize"), 20)));
        QueryWrapper<SysDictTypeEntity> wrapper = new QueryWrapper<>();
        likeIfPresent(wrapper, "dict_name", q.getOrDefault("dict_name", q.get("dictName")));
        likeIfPresent(wrapper, "dict_code", q.getOrDefault("dict_code", q.get("dictCode")));
        likeIfPresent(wrapper, "status", q.get("status"));
        wrapper.orderByDesc("created_at");
        Page<SysDictTypeEntity> result = page(new Page<>(page, pageSize), wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, pageSize);
    }

    @Override
    public List<SysDictItemEntity> items(long dictTypeId) {
        typeDetail(dictTypeId);
        return dictItemMapper.selectList(new QueryWrapper<SysDictItemEntity>().eq("dict_type_id", dictTypeId).orderByAsc("sort", "id"));
    }

    @Override
    public SysDictTypeEntity typeDetail(long id) {
        SysDictTypeEntity entity = getById(id);
        if (entity == null) throw new BusinessException(404000, "?????");
        return entity;
    }

    @Override
    public SysDictItemEntity itemDetail(long id) {
        SysDictItemEntity entity = dictItemMapper.selectById(id);
        if (entity == null) throw new BusinessException(404000, "?????");
        return entity;
    }

    @Override
    @Transactional
    public Long createType(Map<String, Object> body) {
        requireNonBlank(body, "dict_name", "dictName"); requireNonBlank(body, "dict_code", "dictCode");
        SysDictTypeEntity entity = new SysDictTypeEntity(); applyType(entity, body); save(entity); refreshCache(); return entity.getId();
    }

    @Override
    @Transactional
    public void updateType(long id, Map<String, Object> body) {
        typeDetail(id); SysDictTypeEntity entity = new SysDictTypeEntity(); entity.setId(id); applyType(entity, body); updateById(entity); refreshCache();
    }

    @Override
    @Transactional
    public void deleteType(long id) {
        typeDetail(id);
        Long count = dictItemMapper.selectCount(new QueryWrapper<SysDictItemEntity>().eq("dict_type_id", id));
        if (count != null && count > 0) throw new BusinessException(400501, "??????????????");
        removeById(id); refreshCache();
    }

    @Override
    @Transactional
    public Long createItem(Map<String, Object> body) {
        requireNonBlank(body, "label"); requireNonBlank(body, "value"); typeDetail(longOf(value(body, "dict_type_id")));
        SysDictItemEntity entity = new SysDictItemEntity(); applyItem(entity, body); dictItemMapper.insert(entity); refreshCache(); return entity.getId();
    }

    @Override
    @Transactional
    public void updateItem(long id, Map<String, Object> body) {
        itemDetail(id); Object dictTypeId = value(body, "dict_type_id"); if (dictTypeId != null) typeDetail(longOf(dictTypeId));
        SysDictItemEntity entity = new SysDictItemEntity(); entity.setId(id); applyItem(entity, body); dictItemMapper.updateById(entity); refreshCache();
    }

    @Override
    @Transactional
    public void deleteItem(long id) {
        SysDictItemEntity item = itemDetail(id);
        SysDictTypeEntity type = typeDetail(item.getDictTypeId());
        if ("common_status".equals(type.getDictCode()) && commonStatusValueReferenced(item.getValue())) {
            throw new BusinessException(400501, "???????????");
        }
        dictItemMapper.deleteById(id); refreshCache();
    }

    @Override
    @Transactional
    public void updateItemStatus(long id, int status) {
        itemDetail(id); SysDictItemEntity entity = new SysDictItemEntity(); entity.setId(id); entity.setStatus(status); dictItemMapper.updateById(entity); refreshCache();
    }

    @Override
    public void refreshCache() {
        dictCache.clear();
        for (SysDictTypeEntity type : list(new QueryWrapper<SysDictTypeEntity>().eq("status", 1))) {
            dictCache.put(type.getDictCode(), dictItemMapper.selectList(new QueryWrapper<SysDictItemEntity>().eq("dict_type_id", type.getId()).orderByAsc("sort", "id")));
        }
    }

    private boolean commonStatusValueReferenced(String value) {
        int status = intOf(value);
        List<String> tables = List.of("sys_user", "sys_role", "sys_menu", "sys_dept", "sys_dict_type", "sys_dict_item", "sys_config", "sys_job");
        for (String table : tables) {
            Long count = baseMapper.selectCount(new QueryWrapper<SysDictTypeEntity>().apply("exists (select 1 from " + table + " where status = {0} and deleted = 0)", status));
            if (count != null && count > 0) return true;
        }
        return false;
    }

    private static void applyType(SysDictTypeEntity entity, Map<String, Object> body) {
        setString(body, "dict_name", "dictName", entity::setDictName); setString(body, "dict_code", "dictCode", entity::setDictCode); setInteger(body, "status", entity::setStatus); setString(body, "remark", entity::setRemark);
    }
    private static void applyItem(SysDictItemEntity entity, Map<String, Object> body) {
        setLong(body, "dict_type_id", "dictTypeId", entity::setDictTypeId); setString(body, "label", entity::setLabel); setString(body, "value", entity::setValue); setString(body, "color", entity::setColor); setInteger(body, "sort", entity::setSort); setInteger(body, "status", entity::setStatus);
    }
    private static Object value(Map<String, Object> body, String column) { String camel = snakeToCamel(column); return body.containsKey(column) ? body.get(column) : body.get(camel); }
    private static String snakeToCamel(String value) { StringBuilder out = new StringBuilder(); boolean upper = false; for (char c : value.toCharArray()) { if (c == '_') upper = true; else if (upper) { out.append(Character.toUpperCase(c)); upper = false; } else out.append(c); } return out.toString(); }
    private static void likeIfPresent(QueryWrapper<SysDictTypeEntity> wrapper, String column, String value) { if (value != null && !value.isBlank()) wrapper.like(column, value); }
    private static void setString(Map<String, Object> body, String key, java.util.function.Consumer<String> setter) { if (body.containsKey(key)) setter.accept(String.valueOf(body.get(key))); }
    private static void setString(Map<String, Object> body, String snake, String camel, java.util.function.Consumer<String> setter) { Object value = body.containsKey(snake) ? body.get(snake) : body.get(camel); if (value != null) setter.accept(String.valueOf(value)); }
    private static void setInteger(Map<String, Object> body, String key, java.util.function.Consumer<Integer> setter) { if (body.containsKey(key)) setter.accept(intOf(body.get(key))); }
    private static void setLong(Map<String, Object> body, String snake, String camel, java.util.function.Consumer<Long> setter) { Object value = body.containsKey(snake) ? body.get(snake) : body.get(camel); if (value != null) setter.accept(longOf(value)); }
}
