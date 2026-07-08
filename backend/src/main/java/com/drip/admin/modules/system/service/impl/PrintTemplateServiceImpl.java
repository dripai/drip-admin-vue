package com.drip.admin.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drip.admin.common.exception.BusinessException;
import com.drip.admin.common.response.PageResult;
import com.drip.admin.modules.system.dto.PrintTemplateCopyRequest;
import com.drip.admin.modules.system.dto.PrintTemplateQuery;
import com.drip.admin.modules.system.dto.PrintTemplateSaveRequest;
import com.drip.admin.modules.system.entity.SysPrintTemplateEntity;
import com.drip.admin.modules.system.mapper.SysPrintTemplateMapper;
import com.drip.admin.modules.system.service.PrintTemplateService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PrintTemplateServiceImpl extends ServiceImpl<SysPrintTemplateMapper, SysPrintTemplateEntity>
    implements PrintTemplateService {
    @Override
    public PageResult<SysPrintTemplateEntity> page(PrintTemplateQuery query) {
        int page = query.pageOrDefault();
        int pageSize = query.pageSizeOrDefault();
        QueryWrapper<SysPrintTemplateEntity> wrapper = new QueryWrapper<>();
        likeIfPresent(wrapper, "code", query.getCode());
        likeIfPresent(wrapper, "name", query.getName());
        eqIfPresent(wrapper, "status", query.getStatus());
        wrapper.orderByDesc("updated_at").orderByDesc("id");
        Page<SysPrintTemplateEntity> result = page(new Page<>(page, pageSize), wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, pageSize);
    }

    @Override
    public SysPrintTemplateEntity detail(long id) {
        return rawDetail(id);
    }

    @Transactional
    public Long create(PrintTemplateSaveRequest request) {
        ensureCodeAvailable(request.getCode(), null);
        SysPrintTemplateEntity entity = new SysPrintTemplateEntity();
        apply(entity, request, true);
        save(entity);
        return entity.getId();
    }

    @Override
    @Transactional
    public Long copy(long id, PrintTemplateCopyRequest request) {
        SysPrintTemplateEntity source = rawDetail(id);
        ensureCodeAvailable(request.getCode(), null);
        SysPrintTemplateEntity entity = new SysPrintTemplateEntity();
        entity.setCode(request.getCode());
        entity.setName(request.getName());
        entity.setPaperType(source.getPaperType());
        entity.setTemplateJson(source.getTemplateJson());
        entity.setStatus(request.getStatus());
        save(entity);
        return entity.getId();
    }

    @Override
    @Transactional
    public void update(long id, PrintTemplateSaveRequest request) {
        rawDetail(id);
        ensureCodeAvailable(request.getCode(), id);
        SysPrintTemplateEntity entity = new SysPrintTemplateEntity();
        entity.setId(id);
        apply(entity, request, true);
        updateById(entity);
    }

    @Override
    @Transactional
    public void delete(long id) {
        rawDetail(id);
        baseMapper.physicalDeleteById(id);
    }

    @Override
    @Transactional
    public void updateStatus(long id, int status) {
        rawDetail(id);
        SysPrintTemplateEntity entity = new SysPrintTemplateEntity();
        entity.setId(id);
        entity.setStatus(status);
        updateById(entity);
    }

    private SysPrintTemplateEntity rawDetail(long id) {
        SysPrintTemplateEntity entity = getById(id);
        if (entity == null) {
            throw new BusinessException(404000, "print template not found");
        }
        return entity;
    }

    private void ensureCodeAvailable(String code, Long currentId) {
        QueryWrapper<SysPrintTemplateEntity> wrapper = new QueryWrapper<SysPrintTemplateEntity>().eq("code", code);
        if (currentId != null) {
            wrapper.ne("id", currentId);
        }
        if (count(wrapper) > 0) {
            throw new BusinessException(400000, "print template code already exists");
        }
    }

    private static void apply(SysPrintTemplateEntity entity, PrintTemplateSaveRequest request, boolean includeCode) {
        if (includeCode) {
            entity.setCode(request.getCode());
        }
        entity.setName(request.getName());
        entity.setPaperType(request.getPaperType());
        entity.setTemplateJson(request.getTemplateJson());
        entity.setStatus(request.statusValue());
    }

    private static void likeIfPresent(QueryWrapper<SysPrintTemplateEntity> wrapper, String column, String value) {
        if (value != null && !value.isBlank()) {
            wrapper.like(column, value);
        }
    }

    private static void eqIfPresent(QueryWrapper<SysPrintTemplateEntity> wrapper, String column, Object value) {
        if (value != null) {
            wrapper.eq(column, value);
        }
    }
}
