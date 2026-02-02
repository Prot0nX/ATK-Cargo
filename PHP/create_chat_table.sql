-- ===== DATABASE SCHEMA FOR ADMIN CHAT SYSTEM =====
-- این اسکریپت جدول مورد نیاز برای سیستم چت داخلی ادمین‌ها را ایجاد می‌کند

-- ایجاد جدول پیام‌های چت ادمین
CREATE TABLE IF NOT EXISTS admin_chat_messages (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT 'شناسه یکتای پیام',
    username VARCHAR(50) NOT NULL COMMENT 'نام کاربری فرستنده',
    message TEXT NOT NULL COMMENT 'متن پیام (حداکثر 1000 کاراکتر)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'زمان ارسال پیام',
    is_read BOOLEAN DEFAULT FALSE COMMENT 'وضعیت خوانده شدن پیام',
    
    -- ایندکس‌ها برای بهینه‌سازی کوئری‌ها
    INDEX idx_created_at (created_at DESC) COMMENT 'ایندکس برای مرتب‌سازی بر اساس زمان',
    INDEX idx_username (username) COMMENT 'ایندکس برای جستجوی بر اساس کاربر',
    INDEX idx_is_read (is_read) COMMENT 'ایندکس برای فیلتر پیام‌های خوانده نشده',
    
    -- کلید خارجی برای اطمینان از یکپارچگی داده‌ها
    FOREIGN KEY (username) REFERENCES Users(username) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='جدول ذخیره پیام‌های چت داخلی ادمین‌ها';

-- ایجاد ایندکس ترکیبی برای بهینه‌سازی کوئری‌های پیچیده
CREATE INDEX idx_username_created_at ON admin_chat_messages(username, created_at DESC);

-- نمایش ساختار جدول برای تأیید
SHOW CREATE TABLE admin_chat_messages;

-- کوئری تستی برای بررسی عملکرد
SELECT 
    c.id,
    c.username,
    u.fullName,
    c.message,
    c.created_at,
    c.is_read
FROM admin_chat_messages c
JOIN Users u ON c.username = u.username
WHERE u.userType = 'admin'
ORDER BY c.created_at DESC
LIMIT 10;
