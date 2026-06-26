package com.drip.admin.shared.enums;

import com.drip.admin.common.exception.BusinessException;

import java.util.Arrays;

public enum TableMeta {
    SYS_USER("sys_user", true),
    SYS_ROLE("sys_role", true),
    SYS_MENU("sys_menu", true),
    SYS_DEPT("sys_dept", true),
    SYS_DICT_TYPE("sys_dict_type", true),
    SYS_DICT_ITEM("sys_dict_item", true),
    SYS_CONFIG("sys_config", true),
    SYS_JOB("sys_job", true),
    SYS_LOGIN_LOG("sys_login_log", false),
    SYS_OPERATION_LOG("sys_operation_log", false),
    SYS_JOB_RUN_LOG("sys_job_run_log", false),
    SYS_DB_BACKUP("sys_db_backup", false);

    public final String table;
    public final boolean softDelete;

    TableMeta(String table, boolean softDelete) {
        this.table = table;
        this.softDelete = softDelete;
    }

   public static void require(String table) {
        Arrays.stream(values()).filter(v -> v.table.equals(table)).findFirst().orElseThrow(() -> new BusinessException(400000, "Illegal table name"));
    }

   public static boolean softDelete(String table) {
        return Arrays.stream(values()).filter(v -> v.table.equals(table)).findFirst().map(v -> v.softDelete).orElse(false);
    }
}
