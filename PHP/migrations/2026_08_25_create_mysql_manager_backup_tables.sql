-- PHP/migrations/2026_08_25_create_mysql_manager_backup_tables.sql
--
-- ساخت جدول‌های پشتیبان‌گیری/زمان‌بندی/پاکسازی برای پنل PHP/MySQL_Manager (فاز ۳).
--
-- db_backups: متادیتای هر فایل پشتیبان تولیدشده (نام فایل واقعی روی دیسک در
-- PHP/storage/mysql_backups/ خارج از دسترسی مستقیم HTTP نگهداری می‌شود؛ دانلود
-- فقط از طریق api.php?action=downloadBackup با احراز هویت مجاز است).
--
-- db_maintenance_settings: تک‌ردیفه (id همیشه ۱) — تنظیمات Backup خودکار/Retention.
-- ردیف پیش‌فرض همینجا INSERT می‌شود تا کد همیشه یک ردیف تنظیمات پیدا کند و
-- مجبور به branch جدا برای «هنوز تنظیم نشده» نباشد.
--
-- db_cleanup_rules: Ruleهای صریح پاکسازی — طبق قانون spec، هیچ جدولی بدون Rule
-- از پیش تعریف‌شده و فعال پاک نمی‌شود؛ این جدول عمداً از ابتدا خالی می‌ماند.
--
-- CREATE TABLE IF NOT EXISTS ایمن است.

CREATE TABLE IF NOT EXISTS `db_backups` (
  `id` int NOT NULL AUTO_INCREMENT,
  `filename` varchar(255) NOT NULL,
  `backup_type` enum('full','tables','structure','data') NOT NULL,
  `compression` enum('none','gzip') NOT NULL DEFAULT 'gzip',
  `tables_included` json DEFAULT NULL,
  `file_size_bytes` bigint unsigned DEFAULT NULL,
  `checksum_sha256` varchar(64) DEFAULT NULL,
  `status` enum('running','success','failed','verified') NOT NULL DEFAULT 'running',
  `is_automatic` tinyint(1) NOT NULL DEFAULT '0',
  `duration_ms` int unsigned DEFAULT NULL,
  `error_message` text,
  `created_by` varchar(150) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `db_maintenance_settings` (
  `id` tinyint NOT NULL DEFAULT '1',
  `auto_backup_enabled` tinyint(1) NOT NULL DEFAULT '0',
  `auto_backup_frequency` enum('daily','weekly','monthly') NOT NULL DEFAULT 'daily',
  `auto_backup_time` time NOT NULL DEFAULT '02:00:00',
  `auto_backup_retention` int unsigned NOT NULL DEFAULT '7',
  `auto_backup_compression` tinyint(1) NOT NULL DEFAULT '1',
  `auto_backup_include_structure` tinyint(1) NOT NULL DEFAULT '1',
  `auto_backup_include_data` tinyint(1) NOT NULL DEFAULT '1',
  `auto_analyze_weekly` tinyint(1) NOT NULL DEFAULT '0',
  `auto_optimize_weekly` tinyint(1) NOT NULL DEFAULT '0',
  `auto_cleanup_sessions` tinyint(1) NOT NULL DEFAULT '0',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  CONSTRAINT `chk_settings_singleton` CHECK (`id` = 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO `db_maintenance_settings` (`id`) VALUES (1);

CREATE TABLE IF NOT EXISTS `db_cleanup_rules` (
  `id` int NOT NULL AUTO_INCREMENT,
  `table_name` varchar(128) NOT NULL,
  `date_column` varchar(128) NOT NULL,
  `retention_days` int unsigned NOT NULL,
  `is_enabled` tinyint(1) NOT NULL DEFAULT '1',
  `last_run_at` datetime DEFAULT NULL,
  `last_run_deleted_count` int unsigned DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_table` (`table_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
