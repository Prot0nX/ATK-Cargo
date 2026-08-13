-- ============================================================
-- ATK-Cargo Database Schema Exporter
-- Database Target: atk_cargo
-- Exported Date  : 2026-08-13 16:05:23
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;
SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET NAMES utf8mb4;
SET time_zone = "+00:00";

-- ------------------------------------------------------------
-- Tables Structure (7 tables)
-- ------------------------------------------------------------

--
-- Table structure for table `CargoInfo`
--
DROP TABLE IF EXISTS `CargoInfo`;
CREATE TABLE `CargoInfo` (
  `id` int NOT NULL AUTO_INCREMENT,
  `trackingNumber` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `numberOfPeople` int DEFAULT NULL,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `userType` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `entryTime` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `netWeight` int unsigned DEFAULT NULL,
  `scaleReceiptNumber` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `shortageWeight` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `excessWeight` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `exitTime` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `exitDate` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `confirm_username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `confirm_usertype` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `shipName` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `loadingWarehouse` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cargoType` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `shippingCompany` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `loadingQuotaNumber` int DEFAULT NULL,
  `confirm` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `confirmation` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cargo_scale_receipt_number` (`scaleReceiptNumber`),
  KEY `idx_cargo_status_group` (`loadingQuotaNumber`,`shipName`,`loadingWarehouse`,`shippingCompany`,`cargoType`,`status`,`netWeight`),
  KEY `idx_cargo_ship_lookup` (`shipName`,`loadingWarehouse`,`status`,`loadingQuotaNumber`,`shippingCompany`,`cargoType`,`netWeight`),
  KEY `idx_cargo_exit_window` (`exitDate`,`status`,`exitTime`,`netWeight`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Table structure for table `InitialInfo`
--
DROP TABLE IF EXISTS `InitialInfo`;
CREATE TABLE `InitialInfo` (
  `id` int NOT NULL AUTO_INCREMENT,
  `shipName` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `loadingWarehouse` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cargoType` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `shippingCompany` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cargoOwner` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `cargoWeight` int DEFAULT NULL,
  `loadingQuotaNumber` int DEFAULT NULL,
  `remainingWeight` int DEFAULT NULL,
  `totalNetWeight` int DEFAULT NULL,
  `averageNetWeight` int DEFAULT NULL,
  `remainingServices` int DEFAULT NULL,
  `isActive` tinyint(1) NOT NULL DEFAULT '1',
  `percentage` decimal(4,2) DEFAULT '0.50',
  `is_enabled` tinyint(1) DEFAULT '1',
  `temp_tonnage_status` tinyint(1) DEFAULT '0' COMMENT 'وضعیت تناژ موقت: 0=غیرفعال، 1=فعال',
  `temp_tonnage_amount` int DEFAULT '0' COMMENT 'مقدار تناژ موقت',
  PRIMARY KEY (`id`),
  KEY `idx_initial_info_composite` (`loadingQuotaNumber`,`shipName`,`cargoType`,`shippingCompany`,`isActive`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Table structure for table `Passwords`
--
DROP TABLE IF EXISTS `Passwords`;
CREATE TABLE `Passwords` (
  `id` int NOT NULL AUTO_INCREMENT,
  `password` varchar(100) DEFAULT NULL,
  `passwordType` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Table structure for table `SignChecker`
--
DROP TABLE IF EXISTS `SignChecker`;
CREATE TABLE `SignChecker` (
  `id` int NOT NULL AUTO_INCREMENT,
  `app_signature` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Table structure for table `Users`
--
DROP TABLE IF EXISTS `Users`;
CREATE TABLE `Users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `fullName` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `userType` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  KEY `idx_username_userType` (`username`,`userType`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Table structure for table `licenses`
--
DROP TABLE IF EXISTS `licenses`;
CREATE TABLE `licenses` (
  `id` int NOT NULL AUTO_INCREMENT,
  `license_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `company_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `activation_date` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_active` tinyint(1) DEFAULT '1',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `last_check` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `license_key` (`license_key`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Table structure for table `user_sessions`
--
DROP TABLE IF EXISTS `user_sessions`;
CREATE TABLE `user_sessions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'نام کاربری',
  `device_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'شناسه منحصر به فرد دستگاه',
  `device_model` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'مدل دستگاه',
  `android_version` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'نسخه اندروید',
  `app_version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `login_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'زمان ورود',
  `logout_time` datetime DEFAULT NULL COMMENT 'زمان خروج',
  `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT 'وضعیت فعال بودن جلسه',
  `ip_address` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'آدرس IP کاربر',
  `userType` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'نوع کاربر (admin, operator, verifier)',
  `last_activity` datetime DEFAULT NULL COMMENT 'آخرین فعالیت کاربر',
  `session_token` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'توکن جلسه برای امنیت بیشتر',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'زمان ایجاد رکورد',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'زمان آخرین به‌روزرسانی',
  PRIMARY KEY (`id`),
  KEY `idx_username` (`username`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_is_active` (`is_active`),
  KEY `idx_login_time` (`login_time`),
  KEY `idx_last_activity` (`last_activity`),
  KEY `idx_session_token` (`session_token`),
  KEY `idx_userType` (`userType`),
  KEY `idx_username_active` (`username`,`is_active`),
  KEY `idx_device_active` (`device_id`,`is_active`),
  KEY `idx_active_activity` (`is_active`,`last_activity`),
  KEY `idx_username_device_active` (`username`,`device_id`,`is_active`),
  KEY `idx_userType_active` (`userType`,`is_active`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='جدول مدیریت جلسات کاربران';

SET FOREIGN_KEY_CHECKS = 1;
