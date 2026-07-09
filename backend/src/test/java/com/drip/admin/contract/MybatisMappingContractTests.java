package com.drip.admin.contract;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drip.admin.config.MybatisPlusConfig;
import com.drip.admin.modules.system.entity.*;
import com.drip.admin.modules.system.mapper.*;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;

import java.util.Set;

import static com.drip.admin.support.TestSupport.hasTableLogicField;
import static org.junit.jupiter.api.Assertions.*;

class MybatisMappingContractTests {
    @Test
    void mybatisPlusEntitiesAndMappersCoverSystemTables() throws Exception {
        Object[][] mappings = {
            {SysUserEntity.class, SysUserMapper.class, "sys_user"},
            {SysRoleEntity.class, SysRoleMapper.class, "sys_role"},
            {SysUserRoleEntity.class, SysUserRoleMapper.class, "sys_user_role"},
            {SysMenuEntity.class, SysMenuMapper.class, "sys_menu"},
            {SysRoleMenuEntity.class, SysRoleMenuMapper.class, "sys_role_menu"},
            {SysDeptEntity.class, SysDeptMapper.class, "sys_dept"},
            {SysDictTypeEntity.class, SysDictTypeMapper.class, "sys_dict_type"},
            {SysDictItemEntity.class, SysDictItemMapper.class, "sys_dict_item"},
            {SysLoginLogEntity.class, SysLoginLogMapper.class, "sys_login_log"},
            {SysOperationLogEntity.class, SysOperationLogMapper.class, "sys_operation_log"},
            {SysJobEntity.class, SysJobMapper.class, "sys_job"},
            {SysJobRunLogEntity.class, SysJobRunLogMapper.class, "sys_job_run_log"},
            {SysConfigEntity.class, SysConfigMapper.class, "sys_config"},
            {SysPrintTemplateEntity.class, SysPrintTemplateMapper.class, "sys_print_template"}
        };
        Set<Class<?>> logicDeleteEntities = Set.of(
            SysUserEntity.class,
            SysRoleEntity.class,
            SysMenuEntity.class,
            SysDeptEntity.class,
            SysJobEntity.class,
            SysConfigEntity.class,
            SysPrintTemplateEntity.class
        );

        assertEquals(14, mappings.length);
        for (Object[] mapping : mappings) {
            Class<?> entityType = (Class<?>) mapping[0];
            Class<?> mapperType = (Class<?>) mapping[1];
            String tableName = (String) mapping[2];

            assertEquals(tableName, entityType.getAnnotation(TableName.class).value());
            assertTrue(BaseMapper.class.isAssignableFrom(mapperType));
            assertTrue(entityType.getDeclaredField("id").isAnnotationPresent(TableId.class));
            assertEquals(IdType.ASSIGN_ID, entityType.getDeclaredField("id").getAnnotation(TableId.class).type());
            assertEquals(logicDeleteEntities.contains(entityType), hasTableLogicField(entityType));
        }

        MapperScan mapperScan = MybatisPlusConfig.class.getAnnotation(MapperScan.class);
        assertEquals("com.drip.admin.modules", mapperScan.basePackages()[0]);
        assertEquals(BaseMapper.class, mapperScan.markerInterface());
    }
}
