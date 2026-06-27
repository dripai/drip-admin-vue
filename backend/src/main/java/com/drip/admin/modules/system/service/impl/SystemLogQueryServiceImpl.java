package com.drip.admin.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drip.admin.common.exception.BusinessException;
import com.drip.admin.common.response.PageResult;
import com.drip.admin.modules.system.dto.LoginLogQuery;
import com.drip.admin.modules.system.dto.OperationLogQuery;
import com.drip.admin.modules.system.entity.SysLoginLogEntity;
import com.drip.admin.modules.system.entity.SysOperationLogEntity;
import com.drip.admin.modules.system.mapper.SysLoginLogMapper;
import com.drip.admin.modules.system.mapper.SysOperationLogMapper;
import com.drip.admin.modules.system.service.SystemLogQueryService;
import com.drip.admin.modules.system.vo.OperationLogVo;
import org.springframework.stereotype.Service;

@Service
public class SystemLogQueryServiceImpl extends ServiceImpl<SysLoginLogMapper, SysLoginLogEntity> implements SystemLogQueryService {
    private final SysOperationLogMapper operationLogMapper;
    public SystemLogQueryServiceImpl(SysOperationLogMapper operationLogMapper) { this.operationLogMapper = operationLogMapper; }
    @Override public PageResult<SysLoginLogEntity> loginLogs(LoginLogQuery query) { int page = query.pageOrDefault(); int pageSize = query.pageSizeOrDefault(); QueryWrapper<SysLoginLogEntity> wrapper = new QueryWrapper<>(); likeIfPresent(wrapper, "username", query.getUsername()); likeIfPresent(wrapper, "status", query.getStatus()); likeIfPresent(wrapper, "login_type", query.getLoginType()); likeIfPresent(wrapper, "device_type", query.getDeviceType()); likeIfPresent(wrapper, "ip", query.getIp()); if (query.getLoginFrom() != null) wrapper.ge("login_at", query.getLoginFrom()); if (query.getLoginTo() != null) wrapper.le("login_at", query.getLoginTo()); wrapper.orderByDesc("login_at"); Page<SysLoginLogEntity> result = page(new Page<>(page, pageSize), wrapper); return new PageResult<>(result.getRecords(), result.getTotal(), page, pageSize); }
    @Override public SysLoginLogEntity loginLog(long id) { SysLoginLogEntity entity = getById(id); if (entity == null) throw new BusinessException(404000, "operation failed"); return entity; }
    @Override public PageResult<OperationLogVo> operationLogs(OperationLogQuery query) { int page = query.pageOrDefault(); int pageSize = query.pageSizeOrDefault(); QueryWrapper<SysOperationLogEntity> wrapper = new QueryWrapper<>(); likeIfPresent(wrapper, "operator_name", query.getOperator()); likeIfPresent(wrapper, "module", query.getModule()); likeIfPresent(wrapper, "action", query.getAction()); likeIfPresent(wrapper, "response_status", query.getStatus()); likeIfPresent(wrapper, "path", query.getPath()); if (query.getCreatedFrom() != null) wrapper.ge("created_at", query.getCreatedFrom()); if (query.getCreatedTo() != null) wrapper.le("created_at", query.getCreatedTo()); wrapper.orderByDesc("created_at"); Page<SysOperationLogEntity> result = operationLogMapper.selectPage(new Page<>(page, pageSize), wrapper); return new PageResult<>(result.getRecords().stream().map(this::toVo).toList(), result.getTotal(), page, pageSize); }
    @Override public OperationLogVo operationLog(long id) { SysOperationLogEntity entity = operationLogMapper.selectById(id); if (entity == null) throw new BusinessException(404000, "operation failed"); return toVo(entity); }
    private OperationLogVo toVo(SysOperationLogEntity entity) { return new OperationLogVo(entity.getId(), entity.getOperatorId(), entity.getOperatorName(), entity.getModule(), entity.getAction(), entity.getMethod(), entity.getPath(), entity.getRequestParams(), entity.getResponseStatus(), entity.getErrorMessage(), entity.getCostMs(), entity.getCreatedAt()); }
    private static void likeIfPresent(QueryWrapper<?> wrapper, String column, String value) { if (value != null && !value.isBlank()) wrapper.like(column, value); }
}
