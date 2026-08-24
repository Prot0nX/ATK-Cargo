-- PHP/migrations/2026_08_24_create_db_optimization_history_table.sql
--
-- ساخت جدول db_optimization_history برای پنل PHP/MySQL_Manager (فاز ۲: بهینه‌سازی).
-- هر اجرای OPTIMIZE/ANALYZE/CHECK/REPAIR TABLE از طریق DatabaseOptimizationService
-- یک ردیف اینجا ثبت می‌کند (وضعیت، پیام MySQL، حجم قبل/بعد، مدت اجرا) تا هم در
-- تاریخچه‌ی صفحه‌ی Optimization نمایش داده شود و هم در محاسبه‌ی Health Score/
-- «Tables With Errors» داشبورد استفاده شود. مستقل از audit_log است — audit_log
-- برای ردیابی «چه کسی چه زمانی» است، این جدول برای ردیابی «نتیجه‌ی فنی عملیات».
--
-- CREATE TABLE IF NOT EXISTS ایمن است.

CREATE TABLE IF NOT EXISTS `db_optimization_history` (
  `id` int NOT NULL AUTO_INCREMENT,
  `table_name` varchar(128) NOT NULL,
  `operation` enum('optimize','analyze','check','repair') NOT NULL,
  `status` enum('ok','warning','error') NOT NULL,
  `message` text,
  `size_before_bytes` bigint unsigned DEFAULT NULL,
  `size_after_bytes` bigint unsigned DEFAULT NULL,
  `duration_ms` int unsigned DEFAULT NULL,
  `performed_by` varchar(150) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_table_created` (`table_name`,`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
