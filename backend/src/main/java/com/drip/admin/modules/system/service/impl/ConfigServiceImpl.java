package com.drip.admin.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drip.admin.common.exception.BusinessException;
import com.drip.admin.common.response.PageResult;
import com.drip.admin.modules.system.entity.SysConfigEntity;
import com.drip.admin.modules.system.mapper.SysConfigMapper;
import com.drip.admin.modules.system.service.ConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Objects;

import static com.drip.admin.shared.utils.AdminUtils.intOf;
import static com.drip.admin.shared.utils.AdminUtils.parseInt;
import static com.drip.admin.shared.utils.AdminUtils.requireNonBlank;

@Service
public class ConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfigEntity> implements ConfigService {
    @Override
    public PageResult<SysConfigEntity> page(Map<String, String> q) {
        int page = Math.max(1, parseInt(q.get("page"), 1));
        int pageSize = Math.min(100, Math.max(1, parseInt(q.get("pageSize"), 20)));
        QueryWrapper<SysConfigEntity> wrapper = new QueryWrapper<>();
        likeIfPresent(wrapper, "config_name", q.getOrDefault("config_name", q.get("configName")));
        likeIfPresent(wrapper, "config_key", q.getOrDefault("config_key", q.get("configKey")));
        likeIfPresent(wrapper, "group_code", q.getOrDefault("group_code", q.get("groupCode")));
        likeIfPresent(wrapper, "status", q.get("status"));
        wrapper.orderByDesc("created_at");
        Page<SysConfigEntity> result = page(new Page<>(page, pageSize), wrapper);
        result.getRecords().forEach(this::maskSensitive);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, pageSize);
    }

    @Override
    public SysConfigEntity detail(long id) {
        SysConfigEntity entity = rawDetail(id);
        maskSensitive(entity);
        return entity;
    }

    @Override
    @Transactional
    public Long create(Map<String, Object> body) {
        requireNonBlank(body, "config_name", "configName");
        requireNonBlank(body, "config_key", "configKey");
        requireNonBlank(body, "config_value", "configValue");
        SysConfigEntity entity = new SysConfigEntity();
        apply(entity, body, true);
        save(entity);
        return entity.getId();
    }

    @Override
    @Transactional
    public void update(long id, Map<String, Object> body) {
        rawDetail(id);
        SysConfigEntity entity = new SysConfigEntity();
        entity.setId(id);
        apply(entity, body, false);
        updateById(entity);
    }

    @Override
    @Transactional
    public void delete(long id) {
        SysConfigEntity config = rawDetail(id);
        if (Objects.equals(config.getBuiltin(), 1)) throw new BusinessException(400000, "????????");
        removeById(id);
    }

    @Override
    @Transactional
    public void updateStatus(long id, int status) {
        rawDetail(id);
        SysConfigEntity entity = new SysConfigEntity();
        entity.setId(id);
        entity.setStatus(status);
        updateById(entity);
    }

    private SysConfigEntity rawDetail(long id) {
        SysConfigEntity entity = getById(id);
        if (entity == null) throw new BusinessException(404000, "?????");
        return entity;
    }

    private void maskSensitive(SysConfigEntity entity) {
        if (Objects.equals(entity.getIsSensitive(), 1)) entity.setConfigValue("******");
    }

    private static void apply(SysConfigEntity entity, Map<String, Object> body, boolean includeBuiltin) {
        setString(body, "config_name", "configName", entity::setConfigName); setString(body, "config_key", "configKey", entity::setConfigKey);
        setString(body, "config_value", "configValue", entity::setConfigValue); setString(body, "group_code", "groupCode", entity::setGroupCode);
        setInteger(body, "is_sensitive", "isSensitive", entity::setIsSensitive); if (body.containsKey("sensitive")) entity.setIsSensitive(intOf(body.get("sensitive")));
        if (includeBuiltin) setInteger(body, "builtin", entity::setBuiltin); setInteger(body, "status", entity::setStatus); setString(body, "remark", entity::setRemark);
    }
    private static void likeIfPresent(QueryWrapper<SysConfigEntity> wrapper, String column, String value) { if (value != null && !value.isBlank()) wrapper.like(column, value); }
    private static void setString(Map<String, Object> body, String key, java.util.function.Consumer<String> setter) { if (body.containsKey(key)) setter.accept(String.valueOf(body.get(key))); }
    private static void setString(Map<String, Object> body, String snake, String camel, java.util.function.Consumer<String> setter) { Object value = body.containsKey(snake) ? body.get(snake) : body.get(camel); if (value != null) setter.accept(String.valueOf(value)); }
    private static void setInteger(Map<String, Object> body, String key, java.util.function.Consumer<Integer> setter) { if (body.containsKey(key)) setter.accept(intOf(body.get(key))); }
    private static void setInteger(Map<String, Object> body, String snake, String camel, java.util.function.Consumer<Integer> setter) { Object value = body.containsKey(snake) ? body.get(snake) : body.get(camel); if (value != null) setter.accept(intOf(value)); }
}
