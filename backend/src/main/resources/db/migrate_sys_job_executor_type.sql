SET NAMES utf8mb4;

SET @schema_name = DATABASE();

SET @sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `sys_job` ADD COLUMN `executor_type` varchar(32) NOT NULL DEFAULT ''java'' AFTER `cron_expression`',
    'SELECT 1'
  )
  FROM information_schema.columns
  WHERE table_schema = @schema_name
    AND table_name = 'sys_job'
    AND column_name = 'executor_type'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `sys_job` ADD COLUMN `script_file` varchar(255) DEFAULT NULL AFTER `executor_type`',
    'SELECT 1'
  )
  FROM information_schema.columns
  WHERE table_schema = @schema_name
    AND table_name = 'sys_job'
    AND column_name = 'script_file'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `sys_job` ADD COLUMN `script_args` varchar(1024) DEFAULT NULL AFTER `script_file`',
    'SELECT 1'
  )
  FROM information_schema.columns
  WHERE table_schema = @schema_name
    AND table_name = 'sys_job'
    AND column_name = 'script_args'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `sys_job` ADD COLUMN `class_name` varchar(255) DEFAULT NULL AFTER `script_args`',
    'SELECT 1'
  )
  FROM information_schema.columns
  WHERE table_schema = @schema_name
    AND table_name = 'sys_job'
    AND column_name = 'class_name'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE `sys_job`
  MODIFY COLUMN `method_name` varchar(128) DEFAULT NULL;

SET @has_bean_name = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = @schema_name
    AND table_name = 'sys_job'
    AND column_name = 'bean_name'
);

SET @sql = IF(
  @has_bean_name > 0,
  'UPDATE `sys_job`
   SET
     `executor_type` = ''java'',
     `class_name` = CASE
       WHEN `bean_name` = ''smokeJob'' THEN ''com.drip.admin.infrastructure.external.SystemHealthJob''
       WHEN `bean_name` = ''systemHealthJob'' THEN ''com.drip.admin.infrastructure.external.SystemHealthJob''
       ELSE `bean_name`
     END
   WHERE `executor_type` IS NULL OR `executor_type` = ''''',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    COUNT(*) > 0,
    'ALTER TABLE `sys_job` DROP COLUMN `bean_name`',
    'SELECT 1'
  )
  FROM information_schema.columns
  WHERE table_schema = @schema_name
    AND table_name = 'sys_job'
    AND column_name = 'bean_name'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    COUNT(*) > 0,
    'ALTER TABLE `sys_job` DROP COLUMN `params`',
    'SELECT 1'
  )
  FROM information_schema.columns
  WHERE table_schema = @schema_name
    AND table_name = 'sys_job'
    AND column_name = 'params'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    COUNT(*) > 0,
    'ALTER TABLE `sys_job` DROP INDEX `uk_sys_job_code`',
    'SELECT 1'
  )
  FROM information_schema.statistics
  WHERE table_schema = @schema_name
    AND table_name = 'sys_job'
    AND index_name = 'uk_sys_job_code'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    COUNT(*) > 0,
    'ALTER TABLE `sys_job` DROP COLUMN `job_code`',
    'SELECT 1'
  )
  FROM information_schema.columns
  WHERE table_schema = @schema_name
    AND table_name = 'sys_job'
    AND column_name = 'job_code'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

INSERT INTO `sys_job` (
  `id`,
  `job_name`,
  `cron_expression`,
  `executor_type`,
  `script_file`,
  `script_args`,
  `class_name`,
  `method_name`,
  `status`,
  `remark`,
  `deleted`,
  `created_at`,
  `updated_at`
)
SELECT
  2,
  'MySQL数据库备份',
  '0 2 * * *',
  'bat',
  'mysql-backup.cmd',
  NULL,
  NULL,
  NULL,
  1,
  '每日凌晨执行 MySQL 备份脚本',
  0,
  NOW(),
  NOW()
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_job` WHERE `job_name` = 'MySQL数据库备份'
);

SET @sql = (
  SELECT IF(
    COUNT(*) = 0,
    'INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission_code`, `icon`, `sort`, `visible`, `status`, `deleted`, `created_at`, `updated_at`) VALUES (49, 1, ''任务历史'', ''MENU'', ''/system/jobHistory'', ''system/job-history/index'', ''system:job:history'', ''history'', 105, 1, 1, 0, NOW(), NOW())',
    'SELECT 1'
  )
  FROM `sys_menu`
  WHERE `id` = 49 OR `permission_code` = 'system:job:history'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

INSERT IGNORE INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_at`)
SELECT COALESCE(MAX(`id`), 0) + 1, 1, 49, NOW()
FROM `sys_role_menu`
WHERE EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 49)
  AND NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 49);
