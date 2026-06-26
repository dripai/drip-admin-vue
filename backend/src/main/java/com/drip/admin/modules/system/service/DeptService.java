package com.drip.admin.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.drip.admin.modules.system.dto.DeptSaveRequest;
import com.drip.admin.modules.system.entity.SysDeptEntity;
import com.drip.admin.modules.system.vo.DeptTreeVo;

import java.util.List;

public interface DeptService extends IService<SysDeptEntity> {
    List<DeptTreeVo> tree();
    SysDeptEntity detail(long id);
    Long create(DeptSaveRequest request);
    void update(long id, DeptSaveRequest request);
    void delete(long id);
    void updateStatus(long id, int status);
}
