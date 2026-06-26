package com.drip.admin.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drip.admin.common.exception.BusinessException;
import com.drip.admin.common.response.PageResult;
import com.drip.admin.modules.system.entity.SysLoginLogEntity;
import com.drip.admin.modules.system.entity.SysOperationLogEntity;
import com.drip.admin.modules.system.mapper.SysLoginLogMapper;
import com.drip.admin.modules.system.mapper.SysOperationLogMapper;
import com.drip.admin.modules.system.service.SystemLogQueryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.drip.admin.shared.utils.AdminUtils.parseInt;

@Service
public class SystemLogQueryServiceImpl extends ServiceImpl<SysLoginLogMapper, SysLoginLogEntity> implements SystemLogQueryService {
    private final SysOperationLogMapper operationLogMapper;

    public SystemLogQueryServiceImpl(SysOperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    @Override
    public PageResult<SysLoginLogEntity> loginLogs(Map<String, String> q) {
        return pageLogin(q, "login_at", List.of("username", "status", "login_type", "device_type", "ip"));
    }

    @Override
    public SysLoginLogEntity loginLog(long id) {
        SysLoginLogEntity entity = getById(id); if (entity == null) throw new BusinessException(404000, "?????"); return entity;
    }

    @Override
    public PageResult<SysOperationLogEntity> operationLogs(Map<String, String> q) {
        int page = Math.max(1, parseInt(q.get("page"), 1)); int pageSize = Math.min(100, Math.max(1, parseInt(q.get("pageSize"), 20)));
        QueryWrapper<SysOperationLogEntity> wrapper = new QueryWrapper<>();
        for (String filter : List.of("operator_name", "module", "action", "response_status", "path")) likeIfPresent(wrapper, filter, q.getOrDefault(filter, q.get(snakeToCamel(filter))));
        wrapper.orderByDesc("created_at");
        Page<SysOperationLogEntity> result = operationLogMapper.selectPage(new Page<>(page, pageSize), wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, pageSize);
    }

    @Override
    public SysOperationLogEntity operationLog(long id) {
        SysOperationLogEntity entity = operationLogMapper.selectById(id); if (entity == null) throw new BusinessException(404000, "?????"); return entity;
    }

    private PageResult<SysLoginLogEntity> pageLogin(Map<String, String> q, String orderColumn, List<String> filters) {
        int page = Math.max(1, parseInt(q.get("page"), 1)); int pageSize = Math.min(100, Math.max(1, parseInt(q.get("pageSize"), 20)));
        QueryWrapper<SysLoginLogEntity> wrapper = new QueryWrapper<>(); for (String filter : filters) likeIfPresent(wrapper, filter, q.getOrDefault(filter, q.get(snakeToCamel(filter)))); wrapper.orderByDesc(orderColumn);
        Page<SysLoginLogEntity> result = page(new Page<>(page, pageSize), wrapper); return new PageResult<>(result.getRecords(), result.getTotal(), page, pageSize);
    }
    private static void likeIfPresent(QueryWrapper<?> wrapper, String column, String value) { if (value != null && !value.isBlank()) wrapper.like(column, value); }
    private static String snakeToCamel(String value) { StringBuilder out = new StringBuilder(); boolean upper = false; for (char c : value.toCharArray()) { if (c == '_') upper = true; else if (upper) { out.append(Character.toUpperCase(c)); upper = false; } else out.append(c); } return out.toString(); }
}
