package com.drip.admin.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.drip.admin.modules.system.dto.MenuSaveRequest;
import com.drip.admin.modules.system.entity.SysMenuEntity;
import com.drip.admin.modules.system.vo.MenuTreeVo;

import java.util.List;

public interface MenuService extends IService<SysMenuEntity> {
    List<MenuTreeVo> tree();
    SysMenuEntity detail(long id);
    Long create(MenuSaveRequest request);
    void update(long id, MenuSaveRequest request);
    void delete(long id);
    void updateStatus(long id, int status);
}
