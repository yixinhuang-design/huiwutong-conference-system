-- MySQL dump 10.13  Distrib 9.6.0, for Win64 (x86_64)
--
-- Host: localhost    Database: conference_registration
-- ------------------------------------------------------
-- Server version	9.6.0

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
SET @MYSQLDUMP_TEMP_LOG_BIN = @@SESSION.SQL_LOG_BIN;
SET @@SESSION.SQL_LOG_BIN= 0;

--
-- GTID state at the beginning of the backup 
--

SET @@GLOBAL.GTID_PURGED=/*!80000 '+'*/ '4dff381a-12e8-11f1-a1af-f02f744d927a:1-70477';

--
-- Table structure for table `conf_group`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conf_group` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `tenant_id` bigint NOT NULL,
  `meeting_id` bigint NOT NULL COMMENT '??ID',
  `group_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '????',
  `group_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '????',
  `max_members` int DEFAULT '0' COMMENT '????',
  `current_members` int DEFAULT '0' COMMENT '????',
  `leader_id` bigint DEFAULT NULL COMMENT '??ID',
  `sort` int DEFAULT '0' COMMENT '??',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_meeting_id` (`meeting_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='???';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `conf_group_member`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conf_group_member` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL DEFAULT '0',
  `group_id` bigint NOT NULL COMMENT '??ID',
  `registration_id` bigint NOT NULL COMMENT '??ID',
  `is_leader` tinyint DEFAULT '0' COMMENT '????',
  `join_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_registration` (`group_id`,`registration_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='?????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `conf_meeting_staff`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conf_meeting_staff` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '??',
  `meeting_id` bigint NOT NULL COMMENT '??ID',
  `staff_id` bigint NOT NULL COMMENT '????ID',
  `staff_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '??????',
  `staff_phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '??????',
  `role` int DEFAULT '0' COMMENT '??(0=????, 1=??, 2=??)',
  `permissions` json DEFAULT NULL COMMENT '????(JSON??)',
  `tenant_id` bigint NOT NULL COMMENT '??ID',
  `deleted` int DEFAULT '0' COMMENT '????(0=?, 1=?)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_meeting_staff` (`meeting_id`,`staff_id`,`tenant_id`),
  KEY `idx_meeting_id` (`meeting_id`),
  KEY `idx_staff_id` (`staff_id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='???????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `conf_notification_template`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conf_notification_template` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '??',
  `meeting_id` bigint DEFAULT NULL COMMENT '??ID(?NULL??????)',
  `template_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '????',
  `template_type` int DEFAULT '0' COMMENT '????(0=??, 1=??, 2=??)',
  `template_content` longtext COLLATE utf8mb4_unicode_ci COMMENT '????',
  `variables` json DEFAULT NULL COMMENT '????(JSON??)',
  `is_default` int DEFAULT '0' COMMENT '???????(0=?, 1=?)',
  `send_time` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '????(?: registration_start)',
  `tenant_id` bigint NOT NULL COMMENT '??ID',
  `deleted` int DEFAULT '0' COMMENT '????(0=?, 1=?)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  PRIMARY KEY (`id`),
  KEY `idx_meeting_id` (`meeting_id`),
  KEY `idx_template_type` (`template_type`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='?????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `conf_qrcode`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conf_qrcode` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '??',
  `meeting_id` bigint NOT NULL COMMENT '??ID',
  `qr_type` int DEFAULT '0' COMMENT '?????(0=??, 1=??, 2=????)',
  `qr_code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '?????',
  `qr_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '?????URL',
  `qr_text` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '???????',
  `valid_start_time` datetime DEFAULT NULL COMMENT '?????',
  `valid_end_time` datetime DEFAULT NULL COMMENT '?????',
  `is_active` int DEFAULT '1' COMMENT '????(0=?, 1=?)',
  `tenant_id` bigint NOT NULL COMMENT '??ID',
  `deleted` int DEFAULT '0' COMMENT '????(0=?, 1=?)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_meeting_type` (`meeting_id`,`qr_type`,`tenant_id`),
  KEY `idx_meeting_id` (`meeting_id`),
  KEY `idx_qr_type` (`qr_type`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_deleted` (`deleted`),
  KEY `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='??????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `conf_schedule_attachment`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conf_schedule_attachment` (
  `id` bigint NOT NULL COMMENT '??ID',
  `schedule_id` bigint NOT NULL COMMENT '??ID',
  `meeting_id` bigint NOT NULL COMMENT '??ID',
  `tenant_id` bigint NOT NULL COMMENT '??ID',
  `file_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '???',
  `file_size` bigint DEFAULT NULL COMMENT '????????',
  `file_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '?????ppt, pptx, pdf, doc, docx??',
  `file_url` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '????URL',
  `file_hash` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '?????????????',
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '????',
  `upload_by` bigint DEFAULT NULL COMMENT '???ID',
  `upload_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `download_count` int DEFAULT '0' COMMENT '????',
  `deleted` tinyint DEFAULT '0' COMMENT '????',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_schedule_file` (`schedule_id`,`file_hash`,`deleted`),
  KEY `idx_schedule_id` (`schedule_id`),
  KEY `idx_meeting_id` (`meeting_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='?????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `conf_schedule_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conf_schedule_log` (
  `id` bigint NOT NULL COMMENT '??ID',
  `schedule_id` bigint NOT NULL COMMENT '??ID',
  `meeting_id` bigint NOT NULL COMMENT '??ID',
  `tenant_id` bigint NOT NULL COMMENT '??ID',
  `operator_id` bigint NOT NULL COMMENT '???ID',
  `operator_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '?????',
  `action_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '?????create, update, delete, publish, cancel, view',
  `action_detail` longtext COLLATE utf8mb4_unicode_ci COMMENT '?????JSON?',
  `old_value` longtext COLLATE utf8mb4_unicode_ci COMMENT '?????JSON?',
  `new_value` longtext COLLATE utf8mb4_unicode_ci COMMENT '?????JSON?',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `deleted` tinyint DEFAULT '0' COMMENT '????',
  PRIMARY KEY (`id`),
  KEY `idx_schedule_id` (`schedule_id`),
  KEY `idx_meeting_id` (`meeting_id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_operator_id` (`operator_id`),
  KEY `idx_created_time` (`created_time`),
  KEY `idx_action_type` (`action_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='?????????????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `conf_schedule_participant`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conf_schedule_participant` (
  `id` bigint NOT NULL COMMENT '????ID',
  `schedule_id` bigint NOT NULL COMMENT '??ID',
  `meeting_id` bigint NOT NULL COMMENT '??ID',
  `tenant_id` bigint NOT NULL COMMENT '??ID',
  `participant_id` bigint NOT NULL COMMENT '????ID',
  `participant_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '??????',
  `participant_phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '??????',
  `participant_role` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '???????speaker??????host??????attendee?????',
  `status` tinyint DEFAULT '0' COMMENT '?????0-?????, 1-???, 2-???, 3-???, 4-??',
  `checkin_time` datetime DEFAULT NULL COMMENT '????',
  `checkin_location` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '????',
  `notes` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '??',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  `deleted` tinyint DEFAULT '0' COMMENT '????',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_schedule_participant` (`schedule_id`,`participant_id`,`deleted`),
  KEY `idx_schedule_id` (`schedule_id`),
  KEY `idx_participant_id` (`participant_id`),
  KEY `idx_meeting_id` (`meeting_id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='???????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `conf_schedule_reminder`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conf_schedule_reminder` (
  `id` bigint NOT NULL COMMENT '????ID',
  `schedule_id` bigint NOT NULL COMMENT '??ID',
  `meeting_id` bigint NOT NULL COMMENT '??ID',
  `tenant_id` bigint NOT NULL COMMENT '??ID',
  `target_user_id` bigint NOT NULL COMMENT '????ID',
  `reminder_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '?????pre_start????????start???????end??????',
  `reminder_channel` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '?????app, sms, wechat',
  `send_time` datetime DEFAULT NULL COMMENT '????',
  `status` tinyint DEFAULT '0' COMMENT '?????0-???, 1-???, 2-??, 3-??',
  `failure_reason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '????',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  `deleted` tinyint DEFAULT '0' COMMENT '????',
  PRIMARY KEY (`id`),
  KEY `idx_schedule_id` (`schedule_id`),
  KEY `idx_target_user_id` (`target_user_id`),
  KEY `idx_meeting_id` (`meeting_id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_send_time` (`send_time`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='???????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `conf_schedule_settings`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conf_schedule_settings` (
  `id` bigint NOT NULL COMMENT '??ID',
  `schedule_id` bigint NOT NULL COMMENT '??ID',
  `meeting_id` bigint NOT NULL COMMENT '??ID',
  `tenant_id` bigint NOT NULL COMMENT '??ID',
  `need_report` tinyint DEFAULT '0' COMMENT '??????',
  `report_method` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '?????print?????????qrcode???????',
  `report_description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '??????',
  `need_checkin` tinyint DEFAULT '0' COMMENT '??????',
  `checkin_method` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '?????print???????qrcode???????',
  `checkin_description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '??????',
  `need_reminder` tinyint DEFAULT '0' COMMENT '??????',
  `reminder_target` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '?????staff???????all???????user????????',
  `reminder_time` int DEFAULT '15' COMMENT '??????????',
  `reminder_methods` json DEFAULT NULL COMMENT '???????["app", "sms", "wechat"]',
  `allow_change_location` tinyint DEFAULT '1' COMMENT '????????',
  `auto_broadcast` tinyint DEFAULT '0' COMMENT '????????',
  `broadcast_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '????',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  `deleted` tinyint DEFAULT '0' COMMENT '????',
  PRIMARY KEY (`id`),
  UNIQUE KEY `schedule_id` (`schedule_id`),
  KEY `idx_schedule_id` (`schedule_id`),
  KEY `idx_meeting_id` (`meeting_id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_need_checkin` (`need_checkin`),
  KEY `idx_need_reminder` (`need_reminder`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='?????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `conf_schedule_template`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conf_schedule_template` (
  `id` bigint NOT NULL COMMENT '??ID',
  `meeting_id` bigint DEFAULT NULL COMMENT '??ID????NULL????????',
  `tenant_id` bigint NOT NULL COMMENT '??ID',
  `template_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '????',
  `template_description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '????',
  `duration_minutes` int DEFAULT NULL COMMENT '????????',
  `location` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '????',
  `host` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '?????',
  `settings` json DEFAULT NULL COMMENT '?????JSON?',
  `usage_count` int DEFAULT '0' COMMENT '????',
  `created_by` bigint DEFAULT NULL COMMENT '???ID',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  `deleted` tinyint DEFAULT '0' COMMENT '????',
  PRIMARY KEY (`id`),
  KEY `idx_meeting_id` (`meeting_id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='?????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `conf_archive_business_data`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conf_archive_business_data` (
  `id` bigint NOT NULL COMMENT '涓婚敭ID',
  `meeting_id` bigint NOT NULL COMMENT '浼氳ID',
  `tenant_id` bigint NOT NULL COMMENT '绉熸埛ID',
  `data_type` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鏁版嵁绫诲瀷: report_rate/checkin_rate/dormitory_rate',
  `data_label` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鏁版嵁鏍囩',
  `data_datetime` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '鏃堕棿鎻忚堪',
  `expected_count` int DEFAULT '0' COMMENT '搴斿埌浜烘暟',
  `actual_count` int DEFAULT '0' COMMENT '瀹炲埌浜烘暟',
  `rate` decimal(5,2) DEFAULT '0.00' COMMENT '姣旂巼',
  `sort_order` int DEFAULT '0' COMMENT '鎺掑簭',
  `deleted` int DEFAULT '0' COMMENT '閫昏緫鍒犻櫎',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_meeting_type` (`meeting_id`,`data_type`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='涓氬姟鏁版嵁褰掓。琛?;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `conf_archive_config`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conf_archive_config` (
  `id` bigint NOT NULL COMMENT '涓婚敭ID',
  `meeting_id` bigint NOT NULL COMMENT '浼氳ID',
  `tenant_id` bigint NOT NULL COMMENT '绉熸埛ID',
  `allow_student_upload` tinyint(1) DEFAULT '1' COMMENT '鏄惁鍏佽瀛﹀憳涓婁紶',
  `is_packed` tinyint(1) DEFAULT '0' COMMENT '鏄惁宸叉墦鍖呬笅杞?,
  `pack_time` datetime DEFAULT NULL COMMENT '鎵撳寘鏃堕棿',
  `deleted` int DEFAULT '0' COMMENT '閫昏緫鍒犻櫎',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_meeting_id` (`meeting_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='褰掓。閰嶇疆琛?;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `conf_archive_material`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conf_archive_material` (
  `id` bigint NOT NULL COMMENT '涓婚敭ID',
  `meeting_id` bigint NOT NULL COMMENT '浼氳ID',
  `tenant_id` bigint NOT NULL COMMENT '绉熸埛ID',
  `category` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鍒嗙被: courseware-璇句欢, interaction-浜掑姩',
  `sub_category` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '瀛愬垎绫? ppt/pdf/word/video/image 鎴?photo/video/experience/exchange',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鏍囬',
  `file_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '鏂囦欢URL',
  `file_size` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '鏂囦欢澶у皬鎻忚堪',
  `file_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '鏂囦欢MIME绫诲瀷',
  `thumbnail_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '缂╃暐鍥綰RL',
  `content` text COLLATE utf8mb4_unicode_ci COMMENT '鏂囨湰鍐呭',
  `author` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '浣滆€?,
  `upload_by` bigint DEFAULT NULL COMMENT '涓婁紶鑰呯敤鎴稩D',
  `upload_time` datetime DEFAULT NULL COMMENT '涓婁紶鏃堕棿',
  `download_count` int DEFAULT '0' COMMENT '涓嬭浇娆℃暟',
  `sort_order` int DEFAULT '0' COMMENT '鎺掑簭',
  `deleted` int DEFAULT '0' COMMENT '閫昏緫鍒犻櫎 0-姝ｅ父 1-鍒犻櫎',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`),
  KEY `idx_meeting_category` (`meeting_id`,`category`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='褰掓。璧勬枡琛?;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `conf_archive_message`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conf_archive_message` (
  `id` bigint NOT NULL COMMENT '涓婚敭ID',
  `group_id` bigint NOT NULL COMMENT '缇ょ粍ID',
  `meeting_id` bigint NOT NULL COMMENT '浼氳ID',
  `tenant_id` bigint NOT NULL COMMENT '绉熸埛ID',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '娑堟伅鍐呭',
  `sender` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '鍙戦€佽€?,
  `send_time` datetime DEFAULT NULL COMMENT '鍙戦€佹椂闂?,
  `message_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'text' COMMENT '娑堟伅绫诲瀷',
  `deleted` int DEFAULT '0' COMMENT '閫昏緫鍒犻櫎',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_group_id` (`group_id`),
  KEY `idx_meeting_id` (`meeting_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='娑堟伅璁板綍褰掓。琛?;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `conf_archive_message_group`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conf_archive_message_group` (
  `id` bigint NOT NULL COMMENT '涓婚敭ID',
  `meeting_id` bigint NOT NULL COMMENT '浼氳ID',
  `tenant_id` bigint NOT NULL COMMENT '绉熸埛ID',
  `group_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '缇ょ粍鍚嶇О',
  `message_count` int DEFAULT '0' COMMENT '娑堟伅鏉℃暟',
  `last_active_time` datetime DEFAULT NULL COMMENT '鏈€鍚庢椿璺冩椂闂?,
  `deleted` int DEFAULT '0' COMMENT '閫昏緫鍒犻櫎',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_meeting_id` (`meeting_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='娑堟伅缇ょ粍褰掓。琛?;
/*!40101 SET character_set_client = @saved_cs_client */;
SET @@SESSION.SQL_LOG_BIN = @MYSQLDUMP_TEMP_LOG_BIN;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-24  1:15:41
