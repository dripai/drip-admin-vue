-- Convert existing system tables from database auto-increment IDs to
-- application-assigned MyBatis-Plus ASSIGN_ID snowflake IDs.
--
-- Existing row IDs are preserved. Run this once after deploying the backend
-- version that uses id-type: assign_id.

SET FOREIGN_KEY_CHECKS = 0;

ALTER TABLE `sys_dept` MODIFY COLUMN `id` bigint NOT NULL;
ALTER TABLE `sys_user` MODIFY COLUMN `id` bigint NOT NULL;
ALTER TABLE `sys_role` MODIFY COLUMN `id` bigint NOT NULL;
ALTER TABLE `sys_user_role` MODIFY COLUMN `id` bigint NOT NULL;
ALTER TABLE `sys_menu` MODIFY COLUMN `id` bigint NOT NULL;
ALTER TABLE `sys_role_menu` MODIFY COLUMN `id` bigint NOT NULL;
ALTER TABLE `sys_dict_type` MODIFY COLUMN `id` bigint NOT NULL;
ALTER TABLE `sys_dict_item` MODIFY COLUMN `id` bigint NOT NULL;
ALTER TABLE `sys_config` MODIFY COLUMN `id` bigint NOT NULL;
ALTER TABLE `sys_login_log` MODIFY COLUMN `id` bigint NOT NULL;
ALTER TABLE `sys_operation_log` MODIFY COLUMN `id` bigint NOT NULL;
ALTER TABLE `sys_job` MODIFY COLUMN `id` bigint NOT NULL;
ALTER TABLE `sys_job_run_log` MODIFY COLUMN `id` bigint NOT NULL;
ALTER TABLE `sys_db_backup` MODIFY COLUMN `id` bigint NOT NULL;

SET FOREIGN_KEY_CHECKS = 1;
