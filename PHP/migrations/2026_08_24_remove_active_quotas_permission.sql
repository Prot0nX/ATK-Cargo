-- PHP/migrations/2026_08_24_remove_active_quotas_permission.sql
--
-- کلید مجوز active_quotas ("گزارش کوتاژهای فعال") هیچ‌جا در کد فعلی چک
-- نمی‌شود — endpoint قدیمی‌اش (analytics/quota-remaining) حذف شده و پنل
-- Quota_Reports سیستم auth مستقل خودش را دارد. این migration ردیف‌های
-- باقی‌مانده از آن را از دیتابیس پاک می‌کند.

DELETE FROM role_permissions WHERE feature = 'active_quotas';
DELETE FROM user_permissions WHERE feature = 'active_quotas';
