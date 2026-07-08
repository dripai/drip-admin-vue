package com.drip.admin.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drip.admin.modules.system.entity.SysPrintTemplateEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

public interface SysPrintTemplateMapper extends BaseMapper<SysPrintTemplateEntity> {
    @Delete("DELETE FROM sys_print_template WHERE id = #{id}")
    int physicalDeleteById(@Param("id") long id);
}
