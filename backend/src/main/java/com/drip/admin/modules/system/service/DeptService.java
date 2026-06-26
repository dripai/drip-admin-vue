package com.drip.admin.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.drip.admin.modules.system.entity.SysDeptEntity;
import com.drip.admin.modules.system.vo.DeptTreeVo;

import java.util.List;
import java.util.Map;

public interface DeptService extends IService<SysDeptEntity> {
    List<DeptTreeVo> tree();

    SysDeptEntity detail(long id);

    Long create(Map<String, Object> body);

    void update(long id, Map<String, Object> body);

    void delete(long id);

    void updateStatus(long id, int status);
}
