package com.drip.admin.modules.system.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("sys_user")
public class SysUserEntity {
    @TableId
    public Long id;
    public String username;
    public String realName;
}
