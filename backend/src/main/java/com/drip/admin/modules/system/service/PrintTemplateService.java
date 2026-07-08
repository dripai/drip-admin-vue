package com.drip.admin.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.drip.admin.common.response.PageResult;
import com.drip.admin.modules.system.dto.PrintTemplateCopyRequest;
import com.drip.admin.modules.system.dto.PrintTemplateQuery;
import com.drip.admin.modules.system.dto.PrintTemplateSaveRequest;
import com.drip.admin.modules.system.entity.SysPrintTemplateEntity;

public interface PrintTemplateService extends IService<SysPrintTemplateEntity> {
    PageResult<SysPrintTemplateEntity> page(PrintTemplateQuery query);

    SysPrintTemplateEntity detail(long id);

    Long create(PrintTemplateSaveRequest request);

    Long copy(long id, PrintTemplateCopyRequest request);

    void update(long id, PrintTemplateSaveRequest request);

    void delete(long id);

    void updateStatus(long id, int status);
}
