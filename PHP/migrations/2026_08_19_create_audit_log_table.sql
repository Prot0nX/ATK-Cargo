-- PHP/migrations/2026_08_19_create_audit_log_table.sql
--
-- جدول audit_log از قبل در schema.sql (مرجع نصب تازه) و در کد
-- (AuditLogger.php) وجود دارد، اما هیچ migration واقعی آن را روی دیتابیس
-- تولید موجود نساخته بود — یعنی هر فراخوانی AuditLogger::log() (ایجاد/ویرایش/
-- حذف کاربر، ویرایش/فعال‌سازی/حذف کوتاژ، تنظیم تناژ موقت) تا امروز با خطای
-- «Table 'audit_log' doesn't exist» بی‌صدا شکست می‌خورد — عمداً بی‌صدا، چون
-- AuditLogger هرگز اجازه نمی‌دهد شکست ثبت لاگ، خودِ عملیات اصلی را متوقف کند
-- (فقط با error_log ثبت می‌شود). این یعنی از روز اول deploy این ویژگی، هیچ
-- رکوردی در audit_log ذخیره نشده است.
--
-- IF NOT EXISTS ایمن است — اگر جایی از قبل این جدول ساخته شده باشد، این
-- migration بدون خطا و بدون تغییر رد می‌شود.

CREATE TABLE IF NOT EXISTS `audit_log` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `action` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `entity_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `entity_id` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `details` json DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_audit_entity` (`entity_type`,`entity_id`),
  KEY `idx_audit_username_created` (`username`,`created_at`),
  KEY `idx_audit_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
