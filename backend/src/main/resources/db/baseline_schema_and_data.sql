-- Baseline generated from current drip-manager database.
-- Replaces historical migrations for fresh database initialization.

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
DROP TABLE IF EXISTS `flyway_schema_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flyway_schema_history` (
  `installed_rank` int NOT NULL,
  `version` varchar(50) DEFAULT NULL,
  `description` varchar(200) NOT NULL,
  `type` varchar(20) NOT NULL,
  `script` varchar(1000) NOT NULL,
  `checksum` int DEFAULT NULL,
  `installed_by` varchar(100) NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`),
  KEY `flyway_schema_history_s_idx` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `flyway_schema_history` WRITE;
/*!40000 ALTER TABLE `flyway_schema_history` DISABLE KEYS */;
INSERT INTO `flyway_schema_history` VALUES (1,'1','baseline schema and data','SQL','V1__baseline_schema_and_data.sql',761245083,'root','2026-06-26 15:45:14',336,1);
/*!40000 ALTER TABLE `flyway_schema_history` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `sys_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_config` (
  `id` bigint NOT NULL,
  `config_name` varchar(64) NOT NULL,
  `config_key` varchar(128) NOT NULL,
  `config_value` varchar(1024) NOT NULL,
  `value_type` varchar(32) NOT NULL DEFAULT 'string',
  `builtin` tinyint NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '1',
  `remark` varchar(255) DEFAULT NULL,
  `deleted` tinyint NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `sys_config` WRITE;
/*!40000 ALTER TABLE `sys_config` DISABLE KEYS */;
INSERT INTO `sys_config` VALUES (1,'系统名称','system.name','Drip ERP','string',1,1,'后台系统名称',0,'2026-06-27 12:26:45','2026-06-27 13:09:46'),(2,'上传文件大小限制','upload.maxSizeBytes','10485760','number',1,1,'字节',0,'2026-06-27 12:26:45','2026-06-27 15:53:25'),(3,'上传文件后缀限制','upload.allowedExtensions','png,jpg,jpeg,pdf','string',1,1,'逗号分隔的文件后缀',0,'2026-06-27 12:26:45','2026-06-27 14:02:42'),(4,'登录失败锁定次数','login.maxFailures','3','number',1,1,'连续失败次数',0,'2026-06-27 12:26:45','2026-06-27 17:02:10'),(5,'登录失败锁定时长','login.lockSeconds','900','number',1,1,'秒',0,'2026-06-27 12:26:45','2026-06-27 15:53:25'),(6,'系统Logo','system.logo','','string',1,1,'图片 URL，为空时使用系统名称缩写',0,'2026-06-27 13:16:23','2026-06-27 13:16:39'),(7,'启用水印','system.watermark.enabled','true','boolean',1,1,'是否启用系统水印',0,'2026-06-27 17:56:58','2026-06-27 18:14:22');
/*!40000 ALTER TABLE `sys_config` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `sys_db_backup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_db_backup` (
  `id` bigint NOT NULL,
  `backup_name` varchar(128) NOT NULL,
  `file_path` varchar(512) NOT NULL,
  `file_size` bigint NOT NULL DEFAULT '0',
  `status` varchar(16) NOT NULL,
  `created_by` bigint NOT NULL,
  `remark` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_sys_db_backup_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `sys_db_backup` WRITE;
/*!40000 ALTER TABLE `sys_db_backup` DISABLE KEYS */;
INSERT INTO `sys_db_backup` VALUES (1,'backup-1782448286786.sql','backups\\backup-1782448286786.sql',51,'SUCCESS',1,'smoke','2026-06-26 12:31:26');
/*!40000 ALTER TABLE `sys_db_backup` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `sys_dept`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_dept` (
  `id` bigint NOT NULL,
  `parent_id` bigint NOT NULL DEFAULT '0',
  `dept_name` varchar(64) NOT NULL,
  `dept_code` varchar(64) NOT NULL,
  `leader_user_id` bigint DEFAULT NULL,
  `sort` int NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '1',
  `deleted` tinyint NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_dept_code` (`dept_code`),
  KEY `idx_sys_dept_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `sys_dept` WRITE;
/*!40000 ALTER TABLE `sys_dept` DISABLE KEYS */;
INSERT INTO `sys_dept` VALUES (1,0,'张江集团','HQ',NULL,1,1,0,'2026-06-26 12:27:47','2026-06-27 14:35:42'),(6,1,'采购部','PO',NULL,0,1,0,'2026-06-27 14:35:27','2026-06-27 14:36:25'),(7,1,'销售部','SO',NULL,0,1,0,'2026-06-27 14:36:15','2026-06-27 14:36:15'),(8,1,'财务部','FIN',NULL,0,1,0,'2026-06-27 14:36:57','2026-06-27 14:36:57');
/*!40000 ALTER TABLE `sys_dept` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `sys_dict_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_dict_item` (
  `id` bigint NOT NULL,
  `dict_type_id` bigint NOT NULL,
  `label` varchar(64) NOT NULL,
  `value` varchar(64) NOT NULL,
  `is_default` tinyint NOT NULL DEFAULT '0',
  `sort` int NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '1',
  `builtin` tinyint NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_dict_item_value` (`dict_type_id`,`value`),
  KEY `idx_sys_dict_item_type` (`dict_type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `sys_dict_item` WRITE;
/*!40000 ALTER TABLE `sys_dict_item` DISABLE KEYS */;
INSERT INTO `sys_dict_item` VALUES (1,1,'启用','1',1,1,1,1,'2026-06-26 12:27:47','2026-06-28 04:15:02'),(2,1,'禁用','0',0,2,1,1,'2026-06-26 12:27:47','2026-06-28 04:15:02');
/*!40000 ALTER TABLE `sys_dict_item` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `sys_dict_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_dict_type` (
  `id` bigint NOT NULL,
  `dict_name` varchar(64) NOT NULL,
  `dict_code` varchar(64) NOT NULL,
  `status` tinyint NOT NULL DEFAULT '1',
  `builtin` tinyint NOT NULL DEFAULT '0',
  `remark` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_dict_type_code` (`dict_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `sys_dict_type` WRITE;
/*!40000 ALTER TABLE `sys_dict_type` DISABLE KEYS */;
INSERT INTO `sys_dict_type` VALUES (1,'状态','common_status',1,1,'通用启停状态','2026-06-26 12:27:47','2026-06-28 04:12:26'),(2070828962304897025,'供应商类型','VD_TYPE1',1,0,NULL,'2026-06-27 19:17:53','2026-06-28 04:07:08'),(2070959624609583106,'客户类型','CM_TYPE',1,0,NULL,'2026-06-28 03:57:05','2026-06-28 04:06:52'),(2070962232942272513,'供应商类型','VD_TYPE',1,0,NULL,'2026-06-28 04:07:27','2026-06-28 04:07:27');
/*!40000 ALTER TABLE `sys_dict_type` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `sys_job`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_job` (
  `id` bigint NOT NULL,
  `job_name` varchar(64) NOT NULL,
  `cron_expression` varchar(64) NOT NULL,
  `executor_type` varchar(32) NOT NULL DEFAULT 'java',
  `script_file` varchar(255) DEFAULT NULL,
  `script_args` varchar(1024) DEFAULT NULL,
  `class_name` varchar(255) DEFAULT NULL,
  `method_name` varchar(128) DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT '1',
  `remark` varchar(255) DEFAULT NULL,
  `deleted` tinyint NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `sys_job` WRITE;
/*!40000 ALTER TABLE `sys_job` DISABLE KEYS */;
INSERT INTO `sys_job` VALUES (1,'Smoke Job','0 0 * * *','java',NULL,NULL,NULL,'run',0,NULL,0,'2026-06-26 12:31:26','2026-06-27 12:23:43'),(2,'MySQL数据库备份','0 2 * * *','bat','mysql-backup.cmd',NULL,NULL,NULL,1,'每日凌晨执行 MySQL 备份脚本',0,'2026-06-28 02:35:16','2026-06-28 03:32:58');
/*!40000 ALTER TABLE `sys_job` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `sys_job_run_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_job_run_log` (
  `id` bigint NOT NULL,
  `job_id` bigint NOT NULL,
  `job_name` varchar(64) NOT NULL,
  `status` varchar(16) NOT NULL,
  `started_at` datetime NOT NULL,
  `finished_at` datetime DEFAULT NULL,
  `cost_ms` bigint NOT NULL DEFAULT '0',
  `error_message` varchar(512) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_sys_job_run_log_job` (`job_id`),
  KEY `idx_sys_job_run_log_started` (`started_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `sys_job_run_log` WRITE;
/*!40000 ALTER TABLE `sys_job_run_log` DISABLE KEYS */;
INSERT INTO `sys_job_run_log` VALUES (1,1,'Smoke Job','SUCCESS','2026-06-26 12:31:27','2026-06-26 12:31:27',0,NULL),(2070943128944517123,2,'MySQL数据库备份','FAIL','2026-06-28 02:51:33','2026-06-28 02:51:33',288,'script execution failed, exit code: 1'),(2070948082577035265,2,'MySQL数据库备份','RUNNING','2026-06-28 03:11:14',NULL,0,NULL),(2070955369228128258,2,'MySQL数据库备份','RUNNING','2026-06-28 03:40:11',NULL,0,NULL),(2070956159535665153,2,'MySQL数据库备份','SUCCESS','2026-06-28 03:43:20','2026-06-28 03:43:21',1086,NULL),(2070956186559565826,2,'MySQL数据库备份','SUCCESS','2026-06-28 03:43:26','2026-06-28 03:43:27',1106,NULL),(2070956198341365762,2,'MySQL数据库备份','SUCCESS','2026-06-28 03:43:29','2026-06-28 03:43:30',1077,NULL),(2070956281921261569,2,'MySQL数据库备份','RUNNING','2026-06-28 03:43:49',NULL,0,NULL);
/*!40000 ALTER TABLE `sys_job_run_log` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `sys_login_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_login_log` (
  `id` bigint NOT NULL,
  `user_id` bigint DEFAULT NULL,
  `username` varchar(64) NOT NULL,
  `real_name` varchar(64) DEFAULT NULL,
  `login_type` varchar(32) NOT NULL,
  `status` varchar(16) NOT NULL,
  `failure_reason` varchar(255) DEFAULT NULL,
  `ip` varchar(64) DEFAULT NULL,
  `user_agent` varchar(512) DEFAULT NULL,
  `device_type` varchar(64) DEFAULT NULL,
  `login_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_sys_login_log_username` (`username`),
  KEY `idx_sys_login_log_status` (`status`),
  KEY `idx_sys_login_log_login_at` (`login_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `sys_login_log` WRITE;
/*!40000 ALTER TABLE `sys_login_log` DISABLE KEYS */;
INSERT INTO `sys_login_log` VALUES (1,1,'admin','超级管理员','LOGOUT','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 13:51:30'),(4,1,'admin','超级管理员','LOGIN','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 13:51:46'),(6,5,'demo','测试','LOGIN','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 16:34:02'),(7,5,'demo','测试','LOGOUT','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 16:34:37'),(8,1,'admin','超级管理员','LOGIN','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 16:35:22'),(9,1,'admin','超级管理员','LOGOUT','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 16:35:27'),(10,1,'admin','超级管理员','LOGIN','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 16:35:31'),(11,1,'admin','超级管理员','LOGOUT','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 16:38:28'),(17,5,'demo','测试','LOGIN','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 16:53:57'),(18,5,'demo','测试','LOGOUT','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 16:54:02'),(22,5,'demo','测试','LOGIN','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 17:01:00'),(23,5,'demo','测试','LOGOUT','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 17:01:31'),(24,5,'demo','测试','LOGIN','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 17:01:38'),(25,5,'demo','测试','LOGOUT','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 17:01:42'),(26,1,'admin','超级管理员','LOGIN','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 17:01:48'),(27,1,'admin','超级管理员','LOGOUT','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 17:02:14'),(31,1,'admin','超级管理员','LOGIN','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 17:02:39'),(32,1,'admin','超级管理员','LOGOUT','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 17:02:57'),(34,5,'demo','测试','LOGIN','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 17:03:06'),(35,5,'demo','测试','LOGOUT','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 17:03:40'),(36,1,'admin','超级管理员','LOGIN','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 17:03:52'),(37,1,'admin','超级管理员','LOGOUT','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 17:04:11'),(42,1,'admin','超级管理员','LOGIN','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 17:04:32'),(43,1,'admin','超级管理员','LOGIN','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 17:09:56'),(44,1,'admin','超级管理员','LOGIN','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 17:12:42'),(45,1,'admin','超级管理员','LOGIN','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 17:15:27'),(46,1,'admin','超级管理员','LOGIN','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 17:17:51'),(47,1,'admin','超级管理员','LOGOUT','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 17:19:51'),(49,5,'demo','测试','LOGIN','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 17:20:14'),(50,1,'admin','超级管理员','LOGIN','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 17:25:43'),(51,5,'demo','测试','LOGOUT','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 17:41:30'),(52,5,'demo','测试','LOGIN','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 17:41:37'),(53,5,'demo','测试','LOGOUT','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 17:48:41'),(54,1,'admin','超级管理员','LOGIN','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 17:48:46'),(55,1,'admin','超级管理员','LOGOUT','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 18:12:32'),(56,1,'admin','超级管理员','LOGIN','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 18:12:53'),(57,1,'admin','超级管理员','LOGOUT','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 18:13:08'),(58,5,'demo','测试','LOGIN','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 18:13:14'),(59,5,'demo','测试','LOGOUT','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 18:13:19'),(60,1,'admin','超级管理员','LOGIN','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 18:13:24'),(61,1,'admin','超级管理员','LOGOUT','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 18:13:45'),(64,5,'demo','测试','LOGIN','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 18:14:00'),(65,5,'demo','测试','LOGOUT','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 18:14:10'),(66,1,'admin','超级管理员','LOGIN','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 18:14:15'),(2070824026598998017,1,'admin','超级管理员','LOGIN','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 18:58:16'),(2070824072056864769,1,'admin','超级管理员','LOGOUT','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 18:58:27'),(2070824099454058498,1,'admin','超级管理员','LOGIN','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 18:58:34'),(2070828766305071105,1,'admin','超级管理员','LOGOUT','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 19:17:06'),(2070828789017227266,1,'admin','超级管理员','LOGIN','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 19:17:12'),(2070856284781289474,1,'admin','超级管理员','LOGIN','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 21:06:27'),(2070866610725740545,1,'admin','超级管理员','LOGIN','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-27 21:47:29'),(2070904685183090690,1,'admin','超级管理员','LOGIN','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-28 00:18:47'),(2070908355073159169,1,'admin','超级管理员','LOGOUT','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-28 00:33:22'),(2070908410903539713,1,'admin','超级管理员','LOGIN','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-28 00:33:35'),(2070916847771201537,1,'admin','超级管理员','LOGIN','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-28 01:07:06'),(2070929272348213250,1,'admin','超级管理员','LOGIN','SUCCESS',NULL,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','pc','2026-06-28 01:56:29');
/*!40000 ALTER TABLE `sys_login_log` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `sys_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_menu` (
  `id` bigint NOT NULL,
  `parent_id` bigint NOT NULL DEFAULT '0',
  `name` varchar(64) NOT NULL,
  `type` varchar(16) NOT NULL,
  `path` varchar(255) DEFAULT NULL,
  `component` varchar(255) DEFAULT NULL,
  `permission_code` varchar(128) DEFAULT NULL,
  `icon` varchar(64) DEFAULT NULL,
  `sort` int NOT NULL DEFAULT '0',
  `visible` tinyint NOT NULL DEFAULT '1',
  `status` tinyint NOT NULL DEFAULT '1',
  `deleted` tinyint NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_menu_permission` (`permission_code`),
  KEY `idx_sys_menu_parent` (`parent_id`),
  KEY `idx_sys_menu_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `sys_menu` WRITE;
/*!40000 ALTER TABLE `sys_menu` DISABLE KEYS */;
INSERT INTO `sys_menu` VALUES (1,0,'系统管理','DIRECTORY','/system',NULL,'system','settings',1,1,1,0,'2026-06-26 12:27:47','2026-06-26 12:27:47'),(2,1,'用户管理','MENU','/system/user','system/user/index','system:user:list','user',10,1,1,0,'2026-06-26 12:27:47','2026-06-26 23:24:45'),(3,2,'用户详情','BUTTON',NULL,NULL,'system:user:detail',NULL,11,0,1,0,'2026-06-26 12:27:47','2026-06-26 12:27:47'),(4,2,'新增用户','BUTTON',NULL,NULL,'system:user:create',NULL,12,0,1,0,'2026-06-26 12:27:47','2026-06-26 12:27:47'),(5,2,'编辑用户','BUTTON',NULL,NULL,'system:user:update',NULL,13,0,1,0,'2026-06-26 12:27:47','2026-06-26 12:27:47'),(6,2,'删除用户','BUTTON',NULL,NULL,'system:user:delete',NULL,14,0,1,0,'2026-06-26 12:27:47','2026-06-26 12:27:47'),(7,2,'变更用户状态','BUTTON',NULL,NULL,'system:user:disable',NULL,15,0,1,0,'2026-06-26 12:27:47','2026-06-26 12:27:47'),(8,2,'重置密码','BUTTON',NULL,NULL,'system:user:resetPassword',NULL,16,0,1,0,'2026-06-26 12:27:47','2026-06-26 18:50:44'),(9,2,'分配角色','BUTTON',NULL,NULL,'system:user:assignRole',NULL,17,0,1,0,'2026-06-26 12:27:47','2026-06-26 18:50:44'),(10,1,'角色管理','MENU','/system/role','system/role/index','system:role:list','shield',20,1,1,0,'2026-06-26 12:27:47','2026-06-26 23:24:45'),(11,10,'新增角色','BUTTON',NULL,NULL,'system:role:create',NULL,21,0,1,0,'2026-06-26 12:27:47','2026-06-26 12:27:47'),(12,10,'编辑角色','BUTTON',NULL,NULL,'system:role:update',NULL,22,0,1,0,'2026-06-26 12:27:47','2026-06-26 12:27:47'),(13,10,'删除角色','BUTTON',NULL,NULL,'system:role:delete',NULL,23,0,1,0,'2026-06-26 12:27:47','2026-06-26 12:27:47'),(14,10,'角色授权','BUTTON',NULL,NULL,'system:role:permission',NULL,24,0,1,0,'2026-06-26 12:27:47','2026-06-26 12:27:47'),(15,1,'菜单管理','MENU','/system/menu','system/menu/index','system:menu:list','menu',30,1,1,0,'2026-06-26 12:27:47','2026-06-26 23:24:45'),(16,15,'新增菜单','BUTTON',NULL,NULL,'system:menu:create',NULL,31,0,1,0,'2026-06-26 12:27:47','2026-06-26 17:57:08'),(17,1,'部门管理','MENU','/system/dept','system/dept/index','system:dept:list','building',40,1,1,0,'2026-06-26 12:27:47','2026-06-26 23:24:45'),(18,1,'字典管理','MENU','/system/dict','system/dict/index','system:dict:list','book',50,1,1,0,'2026-06-26 12:27:47','2026-06-26 23:24:45'),(19,1,'系统配置','MENU','/system/config','system/config/index','system:config:list','sliders',60,1,1,0,'2026-06-26 12:27:47','2026-06-26 23:24:45'),(20,1,'登录日志','MENU','/system/loginLog','system/loginLog/index','system:loginLog:list','log-in',70,1,1,0,'2026-06-26 12:27:47','2026-06-26 23:24:45'),(21,1,'操作日志','MENU','/system/operationLog','system/operationLog/index','system:operationLog:list','file-clock',80,1,1,0,'2026-06-26 12:27:47','2026-06-26 23:24:45'),(22,1,'在线用户','MENU','/system/onlineUser','system/onlineUser/index','system:online:list','monitor',90,1,1,0,'2026-06-26 12:27:47','2026-06-26 23:24:45'),(23,1,'定时任务','MENU','/system/job','system/job/index','system:job:list','clock',100,1,1,0,'2026-06-26 12:27:47','2026-06-26 23:24:45'),(24,1,'数据库备份','MENU','/system/databaseBackup','system/database/index','system:database:backup:list','database',110,1,1,1,'2026-06-26 12:27:47','2026-06-28 02:49:48'),(25,24,'创建备份','BUTTON',NULL,NULL,'system:database:backup:create',NULL,111,0,1,1,'2026-06-26 12:27:47','2026-06-28 02:49:19'),(26,24,'下载备份','BUTTON',NULL,NULL,'system:database:backup:download',NULL,112,0,1,1,'2026-06-26 12:27:47','2026-06-28 02:49:06'),(27,24,'恢复备份','BUTTON',NULL,NULL,'system:database:backup:restore',NULL,113,0,1,1,'2026-06-26 12:27:47','2026-06-28 02:49:03'),(28,1,'文件上传','BUTTON',NULL,NULL,'system:file:upload',NULL,120,0,1,0,'2026-06-26 17:57:08','2026-06-26 17:57:08'),(29,17,'新增部门','BUTTON',NULL,NULL,'system:dept:create',NULL,41,0,1,0,'2026-06-26 17:57:08','2026-06-26 17:57:08'),(30,17,'编辑部门','BUTTON',NULL,NULL,'system:dept:update',NULL,42,0,1,0,'2026-06-26 17:57:08','2026-06-26 17:57:08'),(31,17,'删除部门','BUTTON',NULL,NULL,'system:dept:delete',NULL,43,0,1,0,'2026-06-26 17:57:08','2026-06-26 17:57:08'),(32,18,'新增字典','BUTTON',NULL,NULL,'system:dict:create',NULL,51,0,1,0,'2026-06-26 17:57:08','2026-06-26 17:57:08'),(33,18,'编辑字典','BUTTON',NULL,NULL,'system:dict:update',NULL,52,0,1,0,'2026-06-26 17:57:08','2026-06-26 17:57:08'),(34,18,'删除字典','BUTTON',NULL,NULL,'system:dict:delete',NULL,53,0,1,0,'2026-06-26 17:57:08','2026-06-26 17:57:08'),(35,19,'新增配置','BUTTON',NULL,NULL,'system:config:create',NULL,61,0,1,0,'2026-06-26 17:57:08','2026-06-26 17:57:08'),(36,19,'编辑配置','BUTTON',NULL,NULL,'system:config:update',NULL,62,0,1,0,'2026-06-26 17:57:08','2026-06-26 17:57:08'),(37,19,'删除配置','BUTTON',NULL,NULL,'system:config:delete',NULL,63,0,1,0,'2026-06-26 17:57:08','2026-06-26 17:57:08'),(38,22,'强制下线','BUTTON',NULL,NULL,'system:online:kickout',NULL,91,0,1,0,'2026-06-26 17:57:08','2026-06-26 17:57:08'),(39,23,'新增任务','BUTTON',NULL,NULL,'system:job:create',NULL,101,0,1,0,'2026-06-26 17:57:08','2026-06-26 17:57:08'),(40,23,'编辑任务','BUTTON',NULL,NULL,'system:job:update',NULL,102,0,1,0,'2026-06-26 17:57:08','2026-06-26 17:57:08'),(41,23,'删除任务','BUTTON',NULL,NULL,'system:job:delete',NULL,103,0,1,0,'2026-06-26 17:57:08','2026-06-26 17:57:08'),(42,23,'执行任务','BUTTON',NULL,NULL,'system:job:run',NULL,104,0,1,0,'2026-06-26 17:57:08','2026-06-26 17:57:08'),(43,24,'删除备份','BUTTON',NULL,NULL,'system:database:backup:delete',NULL,114,0,1,1,'2026-06-26 17:57:08','2026-06-28 02:49:00'),(44,15,'编辑菜单','BUTTON',NULL,NULL,'system:menu:update',NULL,32,0,1,0,'2026-06-26 17:57:08','2026-06-26 17:57:08'),(45,15,'删除菜单','BUTTON',NULL,NULL,'system:menu:delete',NULL,33,0,1,0,'2026-06-26 17:57:08','2026-06-26 17:57:08'),(46,15,'变更菜单状态','BUTTON',NULL,NULL,'system:menu:status',NULL,34,0,1,0,'2026-06-26 17:57:08','2026-06-26 17:57:08'),(47,1,'个人中心','MENU','/system/profile','system/profile/index',NULL,'user',5,1,1,0,'2026-06-27 13:37:11','2026-06-27 13:37:11'),(48,2,'解除登录锁定','BUTTON',NULL,NULL,'system:user:unlock',NULL,18,0,1,0,'2026-06-27 16:28:53','2026-06-27 16:28:53'),(49,1,'任务历史','MENU','/system/jobHistory','system/job-history/index','system:job:history','history',105,1,1,0,'2026-06-28 02:35:16','2026-06-28 02:35:16');
/*!40000 ALTER TABLE `sys_menu` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `sys_operation_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_operation_log` (
  `id` bigint NOT NULL,
  `operator_id` bigint DEFAULT NULL,
  `operator_name` varchar(64) DEFAULT NULL,
  `module` varchar(64) NOT NULL,
  `action` varchar(64) NOT NULL,
  `method` varchar(16) NOT NULL,
  `path` varchar(255) NOT NULL,
  `request_params` text,
  `response_status` varchar(16) NOT NULL,
  `error_message` text,
  `cost_ms` bigint NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_sys_operation_log_operator` (`operator_id`),
  KEY `idx_sys_operation_log_module` (`module`),
  KEY `idx_sys_operation_log_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `sys_operation_log` WRITE;
/*!40000 ALTER TABLE `sys_operation_log` DISABLE KEYS */;
INSERT INTO `sys_operation_log` VALUES (1,1,'超级管理员','系统配置','编辑配置','PUT','/api/system/config/4','[4, com.drip.admin.modules.system.dto.ConfigSaveRequest@3ff4170b]','SUCCESS',NULL,14,'2026-06-27 13:54:49'),(2,1,'超级管理员','用户管理','分配角色','PUT','/api/system/user/5/role','[5, com.drip.admin.modules.system.dto.RoleAssignRequest@32d83a93]','SUCCESS',NULL,15,'2026-06-27 14:07:09'),(3,1,'超级管理员','系统配置','编辑配置','PUT','/api/system/config/4','[4, com.drip.admin.modules.system.dto.ConfigSaveRequest@5e9d81b]','SUCCESS',NULL,6,'2026-06-27 14:12:16'),(4,1,'超级管理员','部门管理','删除部门','DELETE','/api/system/dept/1','[1]','FAIL','operation failed',16,'2026-06-27 14:25:22'),(5,1,'超级管理员','部门管理','变更部门状态','PUT','/api/system/dept/1/status','[1, com.drip.admin.modules.system.dto.StatusUpdateRequest@662a3526]','SUCCESS',NULL,12,'2026-06-27 14:25:30'),(6,1,'超级管理员','部门管理','变更部门状态','PUT','/api/system/dept/1/status','[1, com.drip.admin.modules.system.dto.StatusUpdateRequest@6b389b41]','SUCCESS',NULL,8,'2026-06-27 14:25:34'),(7,1,'超级管理员','部门管理','新增部门','POST','/api/system/dept','[com.drip.admin.modules.system.dto.DeptSaveRequest@3f24e26a]','FAIL','\r\n### Error updating database.  Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'HQ\' for key \'sys_dept.uk_sys_dept_code\'\r\n### The error may exist in com/drip/admin/modules/system/mapper/SysDeptMapper.java (best guess)\r\n### The error may involve com.drip.admin.modules.system.mapper.SysDeptMapper.insert-Inline\r\n### The error occurred while setting parameters\r\n### SQL: INSERT INTO sys_dept  ( parent_id, dept_name, dept_code,  sort, status )  VALUES (  ?, ?, ?,  ?, ?  )\r\n### Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'HQ\' for key \'sys_dept.uk_sys_dept_code\'\n; Duplicate entry \'HQ\' for key \'sys_dept.uk_sys_dept_code\'',90,'2026-06-27 14:25:52'),(8,1,'超级管理员','部门管理','新增部门','POST','/api/system/dept','[com.drip.admin.modules.system.dto.DeptSaveRequest@7ab584e7]','FAIL','\r\n### Error updating database.  Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'HQ\' for key \'sys_dept.uk_sys_dept_code\'\r\n### The error may exist in com/drip/admin/modules/system/mapper/SysDeptMapper.java (best guess)\r\n### The error may involve com.drip.admin.modules.system.mapper.SysDeptMapper.insert-Inline\r\n### The error occurred while setting parameters\r\n### SQL: INSERT INTO sys_dept  ( parent_id, dept_name, dept_code,  sort, status )  VALUES (  ?, ?, ?,  ?, ?  )\r\n### Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'HQ\' for key \'sys_dept.uk_sys_dept_code\'\n; Duplicate entry \'HQ\' for key \'sys_dept.uk_sys_dept_code\'',7,'2026-06-27 14:25:54'),(9,1,'超级管理员','部门管理','删除部门','DELETE','/api/system/dept/1','[1]','FAIL','部门下存在用户，不能删除',16,'2026-06-27 14:35:01'),(10,1,'超级管理员','部门管理','新增部门','POST','/api/system/dept','[com.drip.admin.modules.system.dto.DeptSaveRequest@2689d4a0]','SUCCESS',NULL,17,'2026-06-27 14:35:27'),(11,1,'超级管理员','部门管理','编辑部门','PUT','/api/system/dept/1','[1, com.drip.admin.modules.system.dto.DeptSaveRequest@a88f08f]','SUCCESS',NULL,11,'2026-06-27 14:35:42'),(12,1,'超级管理员','部门管理','新增部门','POST','/api/system/dept','[com.drip.admin.modules.system.dto.DeptSaveRequest@7eefb421]','SUCCESS',NULL,11,'2026-06-27 14:36:15'),(13,1,'超级管理员','部门管理','编辑部门','PUT','/api/system/dept/6','[6, com.drip.admin.modules.system.dto.DeptSaveRequest@74ae539f]','SUCCESS',NULL,9,'2026-06-27 14:36:25'),(14,1,'超级管理员','部门管理','新增部门','POST','/api/system/dept','[com.drip.admin.modules.system.dto.DeptSaveRequest@3643d97]','SUCCESS',NULL,9,'2026-06-27 14:36:57'),(15,1,'超级管理员','用户管理','编辑用户','PUT','/api/system/user/5','[5, com.drip.admin.modules.system.dto.UserSaveRequest@499952e4]','SUCCESS',NULL,11,'2026-06-27 14:50:57'),(16,1,'超级管理员','用户管理','编辑用户','PUT','/api/system/user/3','[3, com.drip.admin.modules.system.dto.UserSaveRequest@720b5606]','SUCCESS',NULL,22,'2026-06-27 14:51:38'),(17,1,'超级管理员','用户管理','编辑用户','PUT','/api/system/user/2','[2, com.drip.admin.modules.system.dto.UserSaveRequest@34606bf6]','SUCCESS',NULL,10,'2026-06-27 14:51:42'),(18,1,'超级管理员','用户管理','编辑用户','PUT','/api/system/user/1','[1, com.drip.admin.modules.system.dto.UserSaveRequest@1b39c0b9]','SUCCESS',NULL,10,'2026-06-27 14:51:47'),(19,1,'超级管理员','用户管理','编辑用户','PUT','/api/system/user/3','[3, com.drip.admin.modules.system.dto.UserSaveRequest@1d670c77]','SUCCESS',NULL,14,'2026-06-27 14:58:15'),(20,1,'超级管理员','用户管理','编辑用户','PUT','/api/system/user/5','[5, com.drip.admin.modules.system.dto.UserSaveRequest@54969931]','SUCCESS',NULL,19,'2026-06-27 16:38:23'),(21,1,'超级管理员','系统配置','编辑配置','PUT','/api/system/config/4','[4, com.drip.admin.modules.system.dto.ConfigSaveRequest@3f1cd533]','SUCCESS',NULL,11,'2026-06-27 17:02:11'),(22,1,'超级管理员','用户管理','解除登录锁定','POST','/api/system/user/5/unlock','[5]','SUCCESS',NULL,6,'2026-06-27 17:02:49'),(23,1,'超级管理员','用户管理','编辑用户','PUT','/api/system/user/5','[5, com.drip.admin.modules.system.dto.UserSaveRequest@24d51ca8]','SUCCESS',NULL,18,'2026-06-27 17:04:07'),(24,1,'超级管理员','角色管理','角色授权','PUT','/api/system/role/1/permission','[1, com.drip.admin.modules.system.dto.MenuAssignRequest@345a6a16]','SUCCESS',NULL,30,'2026-06-27 17:11:11'),(25,1,'超级管理员','角色管理','角色授权','PUT','/api/system/role/2/permission','[2, com.drip.admin.modules.system.dto.MenuAssignRequest@1afddadb]','SUCCESS',NULL,16,'2026-06-27 17:13:20'),(26,1,'超级管理员','角色管理','角色授权','PUT','/api/system/role/2/permission','[2, com.drip.admin.modules.system.dto.MenuAssignRequest@2228054e]','SUCCESS',NULL,12,'2026-06-27 17:16:59'),(27,1,'超级管理员','角色管理','角色授权','PUT','/api/system/role/2/permission','[2, com.drip.admin.modules.system.dto.MenuAssignRequest@1e00682f]','SUCCESS',NULL,31,'2026-06-27 17:19:48'),(28,1,'超级管理员','用户管理','分配角色','PUT','/api/system/user/5/role','[5, com.drip.admin.modules.system.dto.RoleAssignRequest@2439bb97]','SUCCESS',NULL,29,'2026-06-27 17:26:45'),(29,5,'测试','角色管理','新增角色','POST','/api/system/role','[com.drip.admin.modules.system.dto.RoleSaveRequest@e370f63]','SUCCESS',NULL,18,'2026-06-27 17:28:09'),(30,5,'测试','用户管理','分配角色','PUT','/api/system/user/5/role','[5, com.drip.admin.modules.system.dto.RoleAssignRequest@3fdf562c]','SUCCESS',NULL,15,'2026-06-27 17:30:06'),(31,5,'测试','角色管理','角色授权','PUT','/api/system/role/4/permission','[4, com.drip.admin.modules.system.dto.MenuAssignRequest@6a839624]','SUCCESS',NULL,11,'2026-06-27 17:30:16'),(32,5,'测试','用户管理','编辑用户','PUT','/api/system/user/3','[3, com.drip.admin.modules.system.dto.UserSaveRequest@25832535]','FAIL','operation failed',14,'2026-06-27 17:31:12'),(33,5,'测试','用户管理','编辑用户','PUT','/api/system/user/3','[3, com.drip.admin.modules.system.dto.UserSaveRequest@66952b19]','FAIL','operation failed',13,'2026-06-27 17:31:15'),(34,5,'测试','用户管理','编辑用户','PUT','/api/system/user/3','[3, com.drip.admin.modules.system.dto.UserSaveRequest@2a041611]','FAIL','operation failed',7,'2026-06-27 17:32:45'),(35,5,'测试','用户管理','编辑用户','PUT','/api/system/user/3','[3, com.drip.admin.modules.system.dto.UserSaveRequest@2936529f]','FAIL','operation failed',18,'2026-06-27 17:41:57'),(36,5,'测试','用户管理','编辑用户','PUT','/api/system/user/3','[3, com.drip.admin.modules.system.dto.UserSaveRequest@20e75701]','FAIL','operation failed',16,'2026-06-27 17:43:20'),(37,5,'测试','用户管理','编辑用户','PUT','/api/system/user/3','[3, com.drip.admin.modules.system.dto.UserSaveRequest@58b6156b]','FAIL','operation failed',13,'2026-06-27 17:44:07'),(38,5,'测试','用户管理','编辑用户','PUT','/api/system/user/3','[3, com.drip.admin.modules.system.dto.UserSaveRequest@3de5d20e]','FAIL','operation failed',12,'2026-06-27 17:44:20'),(39,5,'测试','用户管理','编辑用户','PUT','/api/system/user/3','[3, com.drip.admin.modules.system.dto.UserSaveRequest@507119b8]','FAIL','不能操作超级管理员',20,'2026-06-27 17:47:58'),(40,1,'超级管理员','用户管理','编辑用户','PUT','/api/system/user/3','[3, com.drip.admin.modules.system.dto.UserSaveRequest@28f789ba]','SUCCESS',NULL,12,'2026-06-27 17:49:10'),(41,1,'超级管理员','用户管理','编辑用户','PUT','/api/system/user/3','[3, com.drip.admin.modules.system.dto.UserSaveRequest@13edeefe]','SUCCESS',NULL,9,'2026-06-27 17:49:39'),(42,1,'超级管理员','系统配置','编辑配置','PUT','/api/system/config/7','[7, com.drip.admin.modules.system.dto.ConfigSaveRequest@3c6101ea]','SUCCESS',NULL,7,'2026-06-27 17:59:12'),(43,1,'超级管理员','系统配置','编辑配置','PUT','/api/system/config/7','[7, com.drip.admin.modules.system.dto.ConfigSaveRequest@6b326e9f]','SUCCESS',NULL,10,'2026-06-27 18:13:39'),(44,1,'超级管理员','系统配置','编辑配置','PUT','/api/system/config/7','[7, com.drip.admin.modules.system.dto.ConfigSaveRequest@6363f1c1]','SUCCESS',NULL,7,'2026-06-27 18:14:22'),(2070828962304897026,1,'超级管理员','字典管理','新增字典类型','POST','/api/system/dict/type','[com.drip.admin.modules.system.dto.DictTypeSaveRequest@3790f597]','SUCCESS',NULL,13,'2026-06-27 19:17:53'),(2070942489623535618,1,'超级管理员','菜单管理','删除菜单','DELETE','/api/system/menu/43','[43]','SUCCESS',NULL,23,'2026-06-28 02:49:00'),(2070942503150166018,1,'超级管理员','菜单管理','删除菜单','DELETE','/api/system/menu/27','[27]','SUCCESS',NULL,8,'2026-06-28 02:49:03'),(2070942516316086274,1,'超级管理员','菜单管理','删除菜单','DELETE','/api/system/menu/26','[26]','SUCCESS',NULL,9,'2026-06-28 02:49:06'),(2070942567666950145,1,'超级管理员','菜单管理','删除菜单','DELETE','/api/system/menu/25','[25]','SUCCESS',NULL,11,'2026-06-28 02:49:19'),(2070942692296499201,1,'超级管理员','菜单管理','删除菜单','DELETE','/api/system/menu/24','[24]','SUCCESS',NULL,9,'2026-06-28 02:49:48'),(2070943128944517122,1,'超级管理员','定时任务','手动执行任务','POST','/api/system/job/2/run','[2]','SUCCESS',NULL,5,'2026-06-28 02:51:32'),(2070948082728030210,1,'超级管理员','定时任务','手动执行任务','POST','/api/system/job/2/run','[2]','SUCCESS',NULL,6,'2026-06-28 03:11:13'),(2070955369228128259,1,'超级管理员','定时任务','手动执行任务','POST','/api/system/job/2/run','[2]','SUCCESS',NULL,5,'2026-06-28 03:40:11'),(2070956159535665154,1,'超级管理员','定时任务','手动执行任务','POST','/api/system/job/2/run','[2]','SUCCESS',NULL,3,'2026-06-28 03:43:19'),(2070956186559565827,1,'超级管理员','定时任务','手动执行任务','POST','/api/system/job/2/run','[2]','SUCCESS',NULL,4,'2026-06-28 03:43:26'),(2070956198341365763,1,'超级管理员','定时任务','手动执行任务','POST','/api/system/job/2/run','[2]','SUCCESS',NULL,4,'2026-06-28 03:43:28'),(2070956281984176130,1,'超级管理员','定时任务','手动执行任务','POST','/api/system/job/2/run','[2]','SUCCESS',NULL,5,'2026-06-28 03:43:48'),(2070959577973116930,1,'超级管理员','字典管理','新增字典类型','POST','/api/system/dict/type','[com.drip.admin.modules.system.dto.DictTypeSaveRequest@23241870]','FAIL','\r\n### Error updating database.  Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'VD_TYPE\' for key \'sys_dict_type.uk_sys_dict_type_code\'\r\n### The error may exist in com/drip/admin/modules/system/mapper/SysDictTypeMapper.java (best guess)\r\n### The error may involve com.drip.admin.modules.system.mapper.SysDictTypeMapper.insert-Inline\r\n### The error occurred while setting parameters\r\n### SQL: INSERT INTO sys_dict_type  ( id, dict_name, dict_code, status )  VALUES (  ?, ?, ?, ?  )\r\n### Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'VD_TYPE\' for key \'sys_dict_type.uk_sys_dict_type_code\'\n; Duplicate entry \'VD_TYPE\' for key \'sys_dict_type.uk_sys_dict_type_code\'',91,'2026-06-28 03:56:54'),(2070959624672497665,1,'超级管理员','字典管理','新增字典类型','POST','/api/system/dict/type','[com.drip.admin.modules.system.dto.DictTypeSaveRequest@53c0668a]','SUCCESS',NULL,8,'2026-06-28 03:57:05'),(2070959730788388865,1,'超级管理员','字典管理','编辑字典类型','PUT','/api/system/dict/type/2070959624609583106','[2070959624609583106, com.drip.admin.modules.system.dto.DictTypeSaveRequest@29184975]','SUCCESS',NULL,9,'2026-06-28 03:57:31'),(2070961698189594625,1,'超级管理员','字典管理','删除字典类型','DELETE','/api/system/dict/type/2070959624609583106','[2070959624609583106]','FAIL','\r\n### Error querying database.  Cause: java.sql.SQLSyntaxErrorException: Unknown column \'color\' in \'field list\'\r\n### The error may exist in com/drip/admin/modules/system/mapper/SysDictItemMapper.java (best guess)\r\n### The error may involve defaultParameterMap\r\n### The error occurred while setting parameters\r\n### SQL: SELECT  id,dict_type_id,label,value,color,sort,status,deleted,created_at,updated_at  FROM sys_dict_item  WHERE deleted=0       ORDER BY sort ASC,id ASC\r\n### Cause: java.sql.SQLSyntaxErrorException: Unknown column \'color\' in \'field list\'\n; bad SQL grammar []',15,'2026-06-28 04:05:20'),(2070961832684146690,1,'超级管理员','字典管理','删除字典类型','DELETE','/api/system/dict/type/2070959624609583106','[2070959624609583106]','FAIL','\r\n### Error querying database.  Cause: java.sql.SQLSyntaxErrorException: Unknown column \'color\' in \'field list\'\r\n### The error may exist in com/drip/admin/modules/system/mapper/SysDictItemMapper.java (best guess)\r\n### The error may involve defaultParameterMap\r\n### The error occurred while setting parameters\r\n### SQL: SELECT  id,dict_type_id,label,value,color,sort,status,deleted,created_at,updated_at  FROM sys_dict_item  WHERE deleted=0       ORDER BY sort ASC,id ASC\r\n### Cause: java.sql.SQLSyntaxErrorException: Unknown column \'color\' in \'field list\'\n; bad SQL grammar []',8,'2026-06-28 04:05:52'),(2070962087601250306,1,'超级管理员','字典管理','删除字典类型','DELETE','/api/system/dict/type/2070959624609583106','[2070959624609583106]','SUCCESS',NULL,23,'2026-06-28 04:06:52'),(2070962134728450049,1,'超级管理员','字典管理','编辑字典类型','PUT','/api/system/dict/type/2070828962304897025','[2070828962304897025, com.drip.admin.modules.system.dto.DictTypeSaveRequest@eff0b0c]','SUCCESS',NULL,13,'2026-06-28 04:07:04'),(2070962151937679362,1,'超级管理员','字典管理','删除字典类型','DELETE','/api/system/dict/type/2070828962304897025','[2070828962304897025]','SUCCESS',NULL,8,'2026-06-28 04:07:08'),(2070962233009381378,1,'超级管理员','字典管理','新增字典类型','POST','/api/system/dict/type','[com.drip.admin.modules.system.dto.DictTypeSaveRequest@c0cced0]','SUCCESS',NULL,10,'2026-06-28 04:07:27'),(2070962291280846850,1,'超级管理员','字典管理','新增字典类型','POST','/api/system/dict/type','[com.drip.admin.modules.system.dto.DictTypeSaveRequest@312c97fe]','FAIL','\r\n### Error updating database.  Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'CM_TYPE\' for key \'sys_dict_type.uk_sys_dict_type_code\'\r\n### The error may exist in com/drip/admin/modules/system/mapper/SysDictTypeMapper.java (best guess)\r\n### The error may involve com.drip.admin.modules.system.mapper.SysDictTypeMapper.insert-Inline\r\n### The error occurred while setting parameters\r\n### SQL: INSERT INTO sys_dict_type  ( id, dict_name, dict_code, status )  VALUES (  ?, ?, ?, ?  )\r\n### Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'CM_TYPE\' for key \'sys_dict_type.uk_sys_dict_type_code\'\n; Duplicate entry \'CM_TYPE\' for key \'sys_dict_type.uk_sys_dict_type_code\'',90,'2026-06-28 04:07:41'),(2070962379931656193,1,'超级管理员','字典管理','新增字典类型','POST','/api/system/dict/type','[com.drip.admin.modules.system.dto.DictTypeSaveRequest@518aefa7]','FAIL','\r\n### Error updating database.  Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'CM_TYPE\' for key \'sys_dict_type.uk_sys_dict_type_code\'\r\n### The error may exist in com/drip/admin/modules/system/mapper/SysDictTypeMapper.java (best guess)\r\n### The error may involve com.drip.admin.modules.system.mapper.SysDictTypeMapper.insert-Inline\r\n### The error occurred while setting parameters\r\n### SQL: INSERT INTO sys_dict_type  ( id, dict_name, dict_code, status )  VALUES (  ?, ?, ?, ?  )\r\n### Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'CM_TYPE\' for key \'sys_dict_type.uk_sys_dict_type_code\'\n; Duplicate entry \'CM_TYPE\' for key \'sys_dict_type.uk_sys_dict_type_code\'',8,'2026-06-28 04:08:02');
/*!40000 ALTER TABLE `sys_operation_log` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `sys_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role` (
  `id` bigint NOT NULL,
  `role_name` varchar(64) NOT NULL,
  `role_code` varchar(64) NOT NULL,
  `builtin` tinyint NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '1',
  `remark` varchar(255) DEFAULT NULL,
  `deleted` tinyint NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `sys_role` WRITE;
/*!40000 ALTER TABLE `sys_role` DISABLE KEYS */;
INSERT INTO `sys_role` VALUES (1,'超级管理员','SUPER_ADMIN',1,1,'系统内置角色',0,'2026-06-26 12:27:47','2026-06-26 12:27:47'),(2,'普通管理员','ADMIN',0,1,'默认管理员角色',0,'2026-06-26 12:27:47','2026-06-26 12:27:47'),(3,'????','TEST_1782471266417',0,0,'runtime check',1,'2026-06-26 18:54:26','2026-06-27 01:06:29'),(4,'仓管员','WAREHOUSE',0,1,'仓库管理员',0,'2026-06-27 17:28:09','2026-06-27 17:28:09');
/*!40000 ALTER TABLE `sys_role` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `sys_role_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role_menu` (
  `id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  `menu_id` bigint NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_role_menu` (`role_id`,`menu_id`),
  KEY `idx_sys_role_menu_menu` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `sys_role_menu` WRITE;
/*!40000 ALTER TABLE `sys_role_menu` DISABLE KEYS */;
INSERT INTO `sys_role_menu` VALUES (53,1,1,'2026-06-27 17:11:11'),(54,1,2,'2026-06-27 17:11:11'),(55,1,3,'2026-06-27 17:11:11'),(56,1,4,'2026-06-27 17:11:11'),(57,1,5,'2026-06-27 17:11:11'),(58,1,6,'2026-06-27 17:11:11'),(59,1,7,'2026-06-27 17:11:11'),(60,1,8,'2026-06-27 17:11:11'),(61,1,9,'2026-06-27 17:11:11'),(62,1,10,'2026-06-27 17:11:11'),(63,1,11,'2026-06-27 17:11:11'),(64,1,12,'2026-06-27 17:11:11'),(65,1,13,'2026-06-27 17:11:11'),(66,1,14,'2026-06-27 17:11:11'),(67,1,15,'2026-06-27 17:11:11'),(68,1,16,'2026-06-27 17:11:11'),(69,1,17,'2026-06-27 17:11:11'),(70,1,18,'2026-06-27 17:11:11'),(71,1,19,'2026-06-27 17:11:11'),(72,1,20,'2026-06-27 17:11:11'),(73,1,21,'2026-06-27 17:11:11'),(74,1,22,'2026-06-27 17:11:11'),(75,1,23,'2026-06-27 17:11:11'),(76,1,24,'2026-06-27 17:11:11'),(77,1,25,'2026-06-27 17:11:11'),(78,1,26,'2026-06-27 17:11:11'),(79,1,27,'2026-06-27 17:11:11'),(80,1,28,'2026-06-27 17:11:11'),(81,1,35,'2026-06-27 17:11:11'),(82,1,37,'2026-06-27 17:11:11'),(83,1,36,'2026-06-27 17:11:11'),(84,1,43,'2026-06-27 17:11:11'),(85,1,29,'2026-06-27 17:11:11'),(86,1,31,'2026-06-27 17:11:11'),(87,1,30,'2026-06-27 17:11:11'),(88,1,32,'2026-06-27 17:11:11'),(89,1,34,'2026-06-27 17:11:11'),(90,1,33,'2026-06-27 17:11:11'),(91,1,39,'2026-06-27 17:11:11'),(92,1,41,'2026-06-27 17:11:11'),(93,1,42,'2026-06-27 17:11:11'),(94,1,40,'2026-06-27 17:11:11'),(95,1,38,'2026-06-27 17:11:11'),(96,1,45,'2026-06-27 17:11:11'),(97,1,46,'2026-06-27 17:11:11'),(98,1,44,'2026-06-27 17:11:11'),(99,1,47,'2026-06-27 17:11:11'),(100,1,48,'2026-06-27 17:11:11'),(109,2,15,'2026-06-27 17:19:48'),(110,2,17,'2026-06-27 17:19:48'),(111,2,20,'2026-06-27 17:19:48'),(112,2,21,'2026-06-27 17:19:48'),(113,2,16,'2026-06-27 17:19:48'),(114,2,44,'2026-06-27 17:19:48'),(115,2,45,'2026-06-27 17:19:48'),(116,2,46,'2026-06-27 17:19:48'),(117,2,29,'2026-06-27 17:19:48'),(118,2,30,'2026-06-27 17:19:48'),(119,2,31,'2026-06-27 17:19:48'),(120,2,3,'2026-06-27 17:19:48'),(121,2,4,'2026-06-27 17:19:48'),(122,2,5,'2026-06-27 17:19:48'),(123,2,7,'2026-06-27 17:19:48'),(124,2,6,'2026-06-27 17:19:48'),(125,2,8,'2026-06-27 17:19:48'),(126,2,9,'2026-06-27 17:19:48'),(127,2,48,'2026-06-27 17:19:48'),(128,2,2,'2026-06-27 17:19:48'),(129,2,10,'2026-06-27 17:19:48'),(130,2,11,'2026-06-27 17:19:48'),(131,2,12,'2026-06-27 17:19:48'),(132,2,13,'2026-06-27 17:19:48'),(133,2,14,'2026-06-27 17:19:48'),(134,2,18,'2026-06-27 17:19:48'),(135,2,32,'2026-06-27 17:19:48'),(136,2,33,'2026-06-27 17:19:48'),(137,2,34,'2026-06-27 17:19:48'),(138,2,47,'2026-06-27 17:19:48'),(139,4,22,'2026-06-27 17:30:16'),(140,4,38,'2026-06-27 17:30:16'),(141,1,49,'2026-06-28 02:35:34');
/*!40000 ALTER TABLE `sys_role_menu` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL,
  `username` varchar(64) NOT NULL,
  `password_hash` varchar(128) NOT NULL,
  `password_salt` varchar(64) NOT NULL,
  `real_name` varchar(64) NOT NULL,
  `phone` varchar(32) DEFAULT NULL,
  `email` varchar(128) DEFAULT NULL,
  `avatar` varchar(255) DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT '1',
  `dept_id` bigint DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  `last_login_at` datetime DEFAULT NULL,
  `deleted` tinyint NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_user_username` (`username`),
  KEY `idx_sys_user_dept` (`dept_id`),
  KEY `idx_sys_user_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;
INSERT INTO `sys_user` VALUES (1,'admin','e0b53c1673e188861664190da8ed43ebc2b4e540784cb167c55c919e8ab87a9f','salt1','超级管理员','13800000000','admin@example.com',NULL,1,8,'初始化管理员','2026-06-28 01:56:29',0,'2026-06-26 12:27:47','2026-06-28 01:56:29'),(2,'user1782448243','b3388c793ed24480bea2d0d19a9a98c4e6bc79934e97ec79073d478cdc0abcbb','salt3502742109300','Normal User',NULL,NULL,NULL,0,7,NULL,'2026-06-26 12:30:43',0,'2026-06-26 12:30:43','2026-06-27 14:51:42'),(3,'noperm1782471282455','1444d89c2c16e91ef9a686c9be73ec3f70f7e5f9c241c77c89296f21a866c221','salt45908387494300','SuperPur','13666666666','super_admin@example.com',NULL,1,6,NULL,'2026-06-26 18:54:43',0,'2026-06-26 18:54:42','2026-06-27 17:49:39'),(4,'crud1782493042884','750f02892b509793d9d2ef5323a161550a10a68f0ca2a6eb1320df8b92c9004a','salt7040553575700','CRUD测试用户已编辑','13900002222','crud1782493042884@example.com',NULL,1,NULL,NULL,NULL,1,'2026-06-27 00:58:18','2026-06-27 01:02:47'),(5,'demo','d0c492599557ca7a0ee5f3d6665d290cc3b8f5859ae2c66fba9ce90abcbea542','salt11109894748200','测试','','',NULL,1,6,NULL,'2026-06-27 18:14:00',0,'2026-06-27 13:38:03','2026-06-27 18:14:00');
/*!40000 ALTER TABLE `sys_user` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `sys_user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user_role` (
  `id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_user_role` (`user_id`,`role_id`),
  KEY `idx_sys_user_role_role` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `sys_user_role` WRITE;
/*!40000 ALTER TABLE `sys_user_role` DISABLE KEYS */;
INSERT INTO `sys_user_role` VALUES (1,1,1,'2026-06-26 12:27:47'),(3,2,1,'2026-06-27 01:04:36'),(4,3,1,'2026-06-27 01:04:50'),(7,5,2,'2026-06-27 17:30:06'),(8,5,4,'2026-06-27 17:30:06');
/*!40000 ALTER TABLE `sys_user_role` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

