package com.drip.admin.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drip.admin.common.exception.BusinessException;
import com.drip.admin.common.response.PageResult;
import com.drip.admin.modules.system.dto.ConfigQuery;
import com.drip.admin.modules.system.dto.ConfigSaveRequest;
import com.drip.admin.modules.system.entity.SysConfigEntity;
import com.drip.admin.modules.system.mapper.SysConfigMapper;
import com.drip.admin.modules.system.service.ConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class ConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfigEntity> implements ConfigService {
    @Override
    public PageResult<SysConfigEntity> page(ConfigQuery query) {
        int page = query.pageOrDefault(); int pageSize = query.pageSizeOrDefault(); QueryWrapper<SysConfigEntity> wrapper = new QueryWrapper<>();
        likeIfPresent(wrapper, "config_name", query.getConfigName()); likeIfPresent(wrapper, "config_key", query.getConfigKey()); eqIfPresent(wrapper, "status", query.getStatus()); wrapper.orderByDesc("created_at");
        Page<SysConfigEntity> result = page(new Page<>(page, pageSize), wrapper); return new PageResult<>(result.getRecords(), result.getTotal(), page, pageSize);
    }
    @Override public SysConfigEntity detail(long id) { return rawDetail(id); }
    @Override @Transactional public Long create(ConfigSaveRequest request) { requireText(request.getConfigName(), "configName"); requireText(request.getConfigKey(), "configKey"); requireValuePresent(request.getConfigValue(), "configValue"); SysConfigEntity entity = new SysConfigEntity(); apply(entity, request, true); save(entity); return entity.getId(); }
    @Override @Transactional public void update(long id, ConfigSaveRequest request) { SysConfigEntity current = rawDetail(id); requireValuePresent(request.getConfigValue(), "configValue"); SysConfigEntity entity = new SysConfigEntity(); entity.setId(id); apply(entity, request, false, !Objects.equals(current.getBuiltin(), 1)); updateById(entity); }
    @Override @Transactional public void delete(long id) { SysConfigEntity config = rawDetail(id); if (Objects.equals(config.getBuiltin(), 1)) throw new BusinessException(400000, "operation failed"); removeById(id); }
    @Override @Transactional public void updateStatus(long id, int status) { SysConfigEntity config = rawDetail(id); if (Objects.equals(config.getBuiltin(), 1)) throw new BusinessException(400000, "operation failed"); SysConfigEntity entity = new SysConfigEntity(); entity.setId(id); entity.setStatus(status); updateById(entity); }
    @Override public String requiredValue(String configKey) { SysConfigEntity entity = activeConfig(configKey); if (entity == null || entity.getConfigValue() == null || entity.getConfigValue().isBlank()) throw new BusinessException(500000, "system config missing: " + configKey); return entity.getConfigValue(); }
    @Override public String valueOrDefault(String configKey, String defaultValue) { SysConfigEntity entity = activeConfig(configKey); if (entity == null || entity.getConfigValue() == null) return defaultValue; return entity.getConfigValue(); }
    @Override public int requiredInt(String configKey) { try { return Integer.parseInt(requiredValue(configKey)); } catch (NumberFormatException ex) { throw new BusinessException(500000, "system config invalid: " + configKey); } }
    @Override public long requiredLong(String configKey) { try { return Long.parseLong(requiredValue(configKey)); } catch (NumberFormatException ex) { throw new BusinessException(500000, "system config invalid: " + configKey); } }
    private SysConfigEntity rawDetail(long id) { SysConfigEntity entity = getById(id); if (entity == null) throw new BusinessException(404000, "operation failed"); return entity; }
    private SysConfigEntity activeConfig(String configKey) { return getOne(new QueryWrapper<SysConfigEntity>().eq("config_key", configKey).and(wrapper -> wrapper.eq("builtin", 1).or().eq("status", 1)), false); }
    private static void apply(SysConfigEntity entity, ConfigSaveRequest request, boolean includeBuiltin) { apply(entity, request, includeBuiltin, true); }
    private static void apply(SysConfigEntity entity, ConfigSaveRequest request, boolean includeBuiltin, boolean includeStatus) { entity.setConfigName(request.getConfigName()); entity.setConfigKey(request.getConfigKey()); entity.setConfigValue(request.getConfigValue()); if (includeBuiltin) entity.setBuiltin(0); if (includeStatus) entity.setStatus(request.getStatus()); entity.setRemark(request.getRemark()); }
    private static void likeIfPresent(QueryWrapper<SysConfigEntity> wrapper, String column, String value) { if (value != null && !value.isBlank()) wrapper.like(column, value); }
    private static void eqIfPresent(QueryWrapper<SysConfigEntity> wrapper, String column, Object value) { if (value != null) wrapper.eq(column, value); }
    private static void requireText(String value, String field) { if (value == null || value.isBlank()) throw new BusinessException(400000, field + " is required"); }
    private static void requireValuePresent(String value, String field) { if (value == null) throw new BusinessException(400000, field + " is required"); }
}
