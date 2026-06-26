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
        likeIfPresent(wrapper, "config_name", query.getConfigName()); likeIfPresent(wrapper, "config_key", query.getConfigKey()); likeIfPresent(wrapper, "group_code", query.getGroupCode()); eqIfPresent(wrapper, "status", query.getStatus()); wrapper.orderByDesc("created_at");
        Page<SysConfigEntity> result = page(new Page<>(page, pageSize), wrapper); result.getRecords().forEach(this::maskSensitive); return new PageResult<>(result.getRecords(), result.getTotal(), page, pageSize);
    }
    @Override public SysConfigEntity detail(long id) { SysConfigEntity entity = rawDetail(id); maskSensitive(entity); return entity; }
    @Override @Transactional public Long create(ConfigSaveRequest request) { requireText(request.getConfigName(), "configName"); requireText(request.getConfigKey(), "configKey"); requireText(request.getConfigValue(), "configValue"); SysConfigEntity entity = new SysConfigEntity(); apply(entity, request, true); save(entity); return entity.getId(); }
    @Override @Transactional public void update(long id, ConfigSaveRequest request) { rawDetail(id); SysConfigEntity entity = new SysConfigEntity(); entity.setId(id); apply(entity, request, false); updateById(entity); }
    @Override @Transactional public void delete(long id) { SysConfigEntity config = rawDetail(id); if (Objects.equals(config.getBuiltin(), 1)) throw new BusinessException(400000, "????????"); removeById(id); }
    @Override @Transactional public void updateStatus(long id, int status) { rawDetail(id); SysConfigEntity entity = new SysConfigEntity(); entity.setId(id); entity.setStatus(status); updateById(entity); }
    private SysConfigEntity rawDetail(long id) { SysConfigEntity entity = getById(id); if (entity == null) throw new BusinessException(404000, "?????"); return entity; }
    private void maskSensitive(SysConfigEntity entity) { if (Objects.equals(entity.getIsSensitive(), 1)) entity.setConfigValue("******"); }
    private static void apply(SysConfigEntity entity, ConfigSaveRequest request, boolean includeBuiltin) { entity.setConfigName(request.getConfigName()); entity.setConfigKey(request.getConfigKey()); entity.setConfigValue(request.getConfigValue()); entity.setGroupCode(request.getGroupCode()); entity.setIsSensitive(request.getIsSensitive()); if (includeBuiltin) entity.setBuiltin(request.getBuiltin()); entity.setStatus(request.getStatus()); entity.setRemark(request.getRemark()); }
    private static void likeIfPresent(QueryWrapper<SysConfigEntity> wrapper, String column, String value) { if (value != null && !value.isBlank()) wrapper.like(column, value); }
    private static void eqIfPresent(QueryWrapper<SysConfigEntity> wrapper, String column, Object value) { if (value != null) wrapper.eq(column, value); }
    private static void requireText(String value, String field) { if (value == null || value.isBlank()) throw new BusinessException(400000, field + "????"); }
}
