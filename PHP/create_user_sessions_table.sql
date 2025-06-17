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
  `userType` varchar(20) DEFAULT NULL COMMENT 'نوع کاربر (admin, operator, verifier)',
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
  KEY `idx_userType` (`userType`),
  UNIQUE KEY `unique_active_session` (`username`, `device_id`, `is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='جدول مدیریت جلسات کاربران';

-- ایجاد ایندکس‌های ترکیبی برای بهبود عملکرد کوئری‌ها با بررسی وجود قبلی
-- برای سازگاری با نسخه‌های قدیمی‌تر MySQL از روش امن استفاده می‌کنیم

-- ایجاد ایندکس username_active اگر وجود نداشته باشد
SELECT COUNT(*) INTO @idx_username_active_exists 
FROM information_schema.statistics 
WHERE table_schema = DATABASE() 
AND table_name = 'user_sessions' 
AND index_name = 'idx_username_active';

SET @create_idx_username_active = 'CREATE INDEX `idx_username_active` ON `user_sessions` (`username`, `is_active`)';

SET @sqlquery = IF(@idx_username_active_exists = 0, @create_idx_username_active, 'SELECT "ایندکس idx_username_active از قبل وجود دارد"');
PREPARE stmt FROM @sqlquery;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ایجاد ایندکس device_active اگر وجود نداشته باشد
SELECT COUNT(*) INTO @idx_device_active_exists 
FROM information_schema.statistics 
WHERE table_schema = DATABASE() 
AND table_name = 'user_sessions' 
AND index_name = 'idx_device_active';

SET @create_idx_device_active = 'CREATE INDEX `idx_device_active` ON `user_sessions` (`device_id`, `is_active`)';

SET @sqlquery = IF(@idx_device_active_exists = 0, @create_idx_device_active, 'SELECT "ایندکس idx_device_active از قبل وجود دارد"');
PREPARE stmt FROM @sqlquery;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ایجاد ایندکس active_activity اگر وجود نداشته باشد
SELECT COUNT(*) INTO @idx_active_activity_exists 
FROM information_schema.statistics 
WHERE table_schema = DATABASE() 
AND table_name = 'user_sessions' 
AND index_name = 'idx_active_activity';

SET @create_idx_active_activity = 'CREATE INDEX `idx_active_activity` ON `user_sessions` (`is_active`, `last_activity`)';

SET @sqlquery = IF(@idx_active_activity_exists = 0, @create_idx_active_activity, 'SELECT "ایندکس idx_active_activity از قبل وجود دارد"');
PREPARE stmt FROM @sqlquery;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ایجاد ایندکس username_device_active اگر وجود نداشته باشد
SELECT COUNT(*) INTO @idx_username_device_active_exists 
FROM information_schema.statistics 
WHERE table_schema = DATABASE() 
AND table_name = 'user_sessions' 
AND index_name = 'idx_username_device_active';

SET @create_idx_username_device_active = 'CREATE INDEX `idx_username_device_active` ON `user_sessions` (`username`, `device_id`, `is_active`)';

SET @sqlquery = IF(@idx_username_device_active_exists = 0, @create_idx_username_device_active, 'SELECT "ایندکس idx_username_device_active از قبل وجود دارد"');
PREPARE stmt FROM @sqlquery;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ایجاد ایندکس userType_active اگر وجود نداشته باشد
SELECT COUNT(*) INTO @idx_userType_active_exists 
FROM information_schema.statistics 
WHERE table_schema = DATABASE() 
AND table_name = 'user_sessions' 
AND index_name = 'idx_userType_active';

SET @create_idx_userType_active = 'CREATE INDEX `idx_userType_active` ON `user_sessions` (`userType`, `is_active`)';

SET @sqlquery = IF(@idx_userType_active_exists = 0, @create_idx_userType_active, 'SELECT "ایندکس idx_userType_active از قبل وجود دارد"');
PREPARE stmt FROM @sqlquery;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- اضافه کردن فیلدهای جدید به جدول موجود (در صورت نیاز)
-- برای سازگاری با نسخه‌های قدیمی‌تر MySQL، از روش مطمئن‌تر استفاده می‌کنیم
-- ابتدا بررسی می‌کنیم آیا ستون وجود دارد و سپس آن را اضافه می‌کنیم

-- اضافه کردن فیلد userType اگر وجود نداشته باشد
SELECT COUNT(*) INTO @usertype_exists 
FROM information_schema.columns 
WHERE table_schema = DATABASE() AND table_name = 'user_sessions' AND column_name = 'userType';

SET @add_usertype = CONCAT('ALTER TABLE `user_sessions` ADD COLUMN `userType` varchar(20) DEFAULT NULL COMMENT "نوع کاربر (admin, operator, verifier)" AFTER `ip_address`');

SET @sqlquery = IF(@usertype_exists = 0, @add_usertype, 'SELECT "فیلد userType از قبل وجود دارد"');
PREPARE stmt FROM @sqlquery;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- اضافه کردن فیلد last_activity اگر وجود نداشته باشد
SELECT COUNT(*) INTO @last_activity_exists 
FROM information_schema.columns 
WHERE table_schema = DATABASE() AND table_name = 'user_sessions' AND column_name = 'last_activity';

SET @add_last_activity = CONCAT('ALTER TABLE `user_sessions` ADD COLUMN `last_activity` datetime DEFAULT NULL COMMENT "آخرین فعالیت کاربر" AFTER `userType`');

SET @sqlquery = IF(@last_activity_exists = 0, @add_last_activity, 'SELECT "فیلد last_activity از قبل وجود دارد"');
PREPARE stmt FROM @sqlquery;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- اضافه کردن فیلد session_token اگر وجود نداشته باشد
SELECT COUNT(*) INTO @token_exists 
FROM information_schema.columns 
WHERE table_schema = DATABASE() AND table_name = 'user_sessions' AND column_name = 'session_token';

SET @add_token = CONCAT('ALTER TABLE `user_sessions` ADD COLUMN `session_token` varchar(128) DEFAULT NULL COMMENT "توکن جلسه برای امنیت بیشتر" AFTER `last_activity`');

SET @sqlquery = IF(@token_exists = 0, @add_token, 'SELECT "فیلد session_token از قبل وجود دارد"');
PREPARE stmt FROM @sqlquery;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- اضافه کردن سایر ایندکس‌های ضروری اگر وجود نداشته باشند
-- ایندکس برای last_activity
SELECT COUNT(*) INTO @idx_last_activity_exists 
FROM information_schema.statistics 
WHERE table_schema = DATABASE() 
AND table_name = 'user_sessions' 
AND index_name = 'idx_last_activity';

SET @create_idx_last_activity = 'CREATE INDEX `idx_last_activity` ON `user_sessions` (`last_activity`)';

SET @sqlquery = IF(@idx_last_activity_exists = 0, @create_idx_last_activity, 'SELECT "ایندکس idx_last_activity از قبل وجود دارد"');
PREPARE stmt FROM @sqlquery;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ایندکس برای session_token
SELECT COUNT(*) INTO @idx_session_token_exists 
FROM information_schema.statistics 
WHERE table_schema = DATABASE() 
AND table_name = 'user_sessions' 
AND index_name = 'idx_session_token';

SET @create_idx_session_token = 'CREATE INDEX `idx_session_token` ON `user_sessions` (`session_token`)';

SET @sqlquery = IF(@idx_session_token_exists = 0, @create_idx_session_token, 'SELECT "ایندکس idx_session_token از قبل وجود دارد"');
PREPARE stmt FROM @sqlquery;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ایندکس برای userType
SELECT COUNT(*) INTO @idx_userType_exists 
FROM information_schema.statistics 
WHERE table_schema = DATABASE() 
AND table_name = 'user_sessions' 
AND index_name = 'idx_userType';

SET @create_idx_userType = 'CREATE INDEX `idx_userType` ON `user_sessions` (`userType`)';

SET @sqlquery = IF(@idx_userType_exists = 0, @create_idx_userType, 'SELECT "ایندکس idx_userType از قبل وجود دارد"');
PREPARE stmt FROM @sqlquery;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- نمایش ساختار جدول
DESCRIBE `user_sessions`;

-- کوئری‌های مفید برای مدیریت جلسات:

-- 1. نمایش تمام جلسات فعال:
-- SELECT username, userType, device_model, login_time, ip_address FROM user_sessions WHERE is_active = 1 ORDER BY login_time DESC;

-- 2. بررسی جلسه فعال کاربر مشخص:
-- SELECT * FROM user_sessions WHERE username = 'نام_کاربری' AND is_active = 1;

-- 3. تاریخچه کامل ورود کاربر:
-- SELECT login_time, logout_time, userType, device_model, ip_address FROM user_sessions WHERE username = 'نام_کاربری' ORDER BY login_time DESC;

-- 4. آمار جلسات روزانه:
-- SELECT DATE(login_time) as date, COUNT(*) as login_count FROM user_sessions WHERE login_time >= CURDATE() - INTERVAL 7 DAY GROUP BY DATE(login_time) ORDER BY date DESC;

-- 5. کاربران با بیشترین زمان آنلاین:
-- SELECT username, userType, SUM(TIMESTAMPDIFF(SECOND, login_time, COALESCE(logout_time, NOW()))) as total_online_seconds FROM user_sessions GROUP BY username, userType ORDER BY total_online_seconds DESC;

-- 6. پاکسازی جلسات قدیمی (بیش از 30 روز):
-- DELETE FROM user_sessions WHERE created_at < DATE_SUB(NOW(), INTERVAL 30 DAY) AND is_active = 0;

-- 7. جلسات منقضی شده که هنوز فعال هستند:
-- SELECT * FROM user_sessions WHERE is_active = 1 AND TIMESTAMPDIFF(HOUR, COALESCE(last_activity, login_time), NOW()) > 1;

-- 8. تعداد کاربران آنلاین در هر ساعت:
-- SELECT HOUR(login_time) as hour, userType, COUNT(DISTINCT username) as unique_users FROM user_sessions WHERE DATE(login_time) = CURDATE() AND is_active = 1 GROUP BY HOUR(login_time), userType ORDER BY hour;

-- 9. آمار کاربران آنلاین بر اساس نوع کاربری:
-- SELECT userType, COUNT(DISTINCT username) as user_count FROM user_sessions WHERE is_active = 1 GROUP BY userType;