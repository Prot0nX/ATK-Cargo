-- PHP/migrations/2026_08_22_create_monitoring_events_table.sql
--
-- DEEP_CODE_AUDIT.md #۳۲ (بازبینی‌شده) — سرور تولید هیچ دسترسی خروجی به
-- اینترنت ندارد، پس اتصال health_monitor.php به هشدار Telegram طبق برنامه‌ی
-- اصلی ممکن نیست (SecurityAlerter از قبل no-op می‌ماند چون توکن/چت‌آیدی هرگز
-- قابل‌تنظیم نبوده). راه‌حل جایگزین: نوشتن رویدادها در همین جدول و افشای آن‌ها
-- از طریق REST (`GET/POST api/v2/monitoring/*`) تا داشبورد وب و بخش اندروید
-- آینده با poll این‌ها را بخوانند — بدون هیچ push خروجی از سرور.
--
-- نوشتن ردیف‌ها از داخل SecurityAlerter::alert() (best-effort، هرگز throw
-- نمی‌کند — دقیقاً مثل AuditLogger::log()) انجام می‌شود، پس این دو مسیر
-- موجود بدون تغییر اضافه‌ای رویداد اینجا هم ثبت می‌کنند:
--   - health_monitor.php (رویدادهای HEALTH_CHECK_FAILED)
--   - رویدادهای امنیتی (REFRESH_TOKEN_REUSE_DETECTED و مشابه)
--
-- dedupe_key معنای مشابه cooldown موجود SecurityAlerter را دارد (پیش‌فرض:
-- خود event) اما هیچ منطق dedup‌ای در سطح DB اعمال نمی‌شود — هر فراخوانی
-- alert() یک ردیف جدید می‌نویسد. فیلتر «باز/تایید نشده» در endpoint لیست،
-- نه در سطح insert، جلوی شلوغی داشبورد را می‌گیرد.
--
-- IF NOT EXISTS ایمن است، مطابق قرارداد 2026_08_19_create_audit_log_table.sql.

CREATE TABLE IF NOT EXISTS `monitoring_events` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `event_type` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `severity` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'warning',
  `message` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `source` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `dedupe_key` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `acknowledged_at` datetime DEFAULT NULL,
  `acknowledged_by` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_monitoring_unresolved` (`acknowledged_at`,`created_at`),
  KEY `idx_monitoring_created` (`created_at`),
  KEY `idx_monitoring_dedupe` (`dedupe_key`,`created_at`),
  KEY `idx_monitoring_source` (`source`,`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
