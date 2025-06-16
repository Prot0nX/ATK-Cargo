-- اسکریپت ایجاد جدول user_sessions برای مدیریت بهینه جلسات کاربران
-- نسخه بهبود یافته با قابلیت‌های پیشرفته مدیریت جلسات

CREATE TABLE IF NOT EXISTS `user_sessions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT 'نام کاربری',
  `device_id` varchar(100) NOT NULL COMMENT 'شناسه منحصر به فرد دستگاه',
  `device_model` varchar(100) DEFAULT NULL COMMENT 'مدل دستگاه',
  `android_version` varchar(20) DEFAULT NULL COMMENT 'نسخه اندروید',
  `login_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'زمان ورود',
  `logout_time` datetime DEFAULT NULL COMMENT 'زمان خروج',
  `is_active` tinyint(1) NOT NULL DEFAULT 1 COMMENT 'وضعیت فعال بودن جلسه',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'آدرس IP کاربر',
  `last_activity` datetime DEFAULT NULL COMMENT 'آخرین فعالیت کاربر',
  `session_token` varchar(128) DEFAULT NULL COMMENT 'توکن جلسه برای امنیت بیشتر',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'زمان ایجاد رکورد',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'زمان آخرین به‌روزرسانی',
  PRIMARY KEY (`id`),
  KEY `idx_username` (`username`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_is_active` (`is_active`),
  KEY `idx_login_time` (`login_time`),
  KEY `idx_last_activity` (`last_activity`),
  KEY `idx_session_token` (`session_token`),
  UNIQUE KEY `unique_active_session` (`username`, `device_id`, `is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='جدول مدیریت جلسات کاربران';

-- ایجاد ایندکس‌های ترکیبی برای بهبود عملکرد کوئری‌ها
CREATE INDEX `idx_username_active` ON `user_sessions` (`username`, `is_active`);
CREATE INDEX `idx_device_active` ON `user_sessions` (`device_id`, `is_active`);
CREATE INDEX `idx_active_activity` ON `user_sessions` (`is_active`, `last_activity`);
CREATE INDEX `idx_username_device_active` ON `user_sessions` (`username`, `device_id`, `is_active`);

-- اضافه کردن فیلدهای جدید به جدول موجود (در صورت نیاز)
ALTER TABLE `user_sessions` 
  ADD COLUMN IF NOT EXISTS `last_activity` datetime DEFAULT NULL COMMENT 'آخرین فعالیت کاربر' AFTER `ip_address`,
  ADD COLUMN IF NOT EXISTS `session_token` varchar(128) DEFAULT NULL COMMENT 'توکن جلسه برای امنیت بیشتر' AFTER `last_activity`;

-- اضافه کردن ایندکس‌های جدید
CREATE INDEX IF NOT EXISTS `idx_last_activity` ON `user_sessions` (`last_activity`);
CREATE INDEX IF NOT EXISTS `idx_session_token` ON `user_sessions` (`session_token`);
CREATE INDEX IF NOT EXISTS `idx_active_activity` ON `user_sessions` (`is_active`, `last_activity`);
CREATE INDEX IF NOT EXISTS `idx_username_device_active` ON `user_sessions` (`username`, `device_id`, `is_active`);

-- نمایش ساختار جدول
DESCRIBE `user_sessions`;

-- کوئری‌های مفید برای مدیریت جلسات:

-- 1. نمایش تمام جلسات فعال:
-- SELECT username, device_model, login_time, ip_address FROM user_sessions WHERE is_active = 1 ORDER BY login_time DESC;

-- 2. بررسی جلسه فعال کاربر مشخص:
-- SELECT * FROM user_sessions WHERE username = 'نام_کاربری' AND is_active = 1;

-- 3. تاریخچه کامل ورود کاربر:
-- SELECT login_time, logout_time, device_model, ip_address FROM user_sessions WHERE username = 'نام_کاربری' ORDER BY login_time DESC;

-- 4. آمار جلسات روزانه:
-- SELECT DATE(login_time) as date, COUNT(*) as login_count FROM user_sessions WHERE login_time >= CURDATE() - INTERVAL 7 DAY GROUP BY DATE(login_time) ORDER BY date DESC;

-- 5. کاربران با بیشترین زمان آنلاین:
-- SELECT username, SUM(TIMESTAMPDIFF(SECOND, login_time, COALESCE(logout_time, NOW()))) as total_online_seconds FROM user_sessions GROUP BY username ORDER BY total_online_seconds DESC;

-- 6. پاکسازی جلسات قدیمی (بیش از 30 روز):
-- DELETE FROM user_sessions WHERE created_at < DATE_SUB(NOW(), INTERVAL 30 DAY) AND is_active = 0;

-- 7. جلسات منقضی شده که هنوز فعال هستند:
-- SELECT * FROM user_sessions WHERE is_active = 1 AND TIMESTAMPDIFF(HOUR, COALESCE(last_activity, login_time), NOW()) > 1;

-- 8. تعداد کاربران آنلاین در هر ساعت:
-- SELECT HOUR(login_time) as hour, COUNT(DISTINCT username) as unique_users FROM user_sessions WHERE DATE(login_time) = CURDATE() AND is_active = 1 GROUP BY HOUR(login_time) ORDER BY hour;