-- آپدیت جدول پیام‌های چت برای افزودن قابلیت‌های جدید
-- اجرای این اسکریپت برای بازطراحی سیستم چت الزامی است

ALTER TABLE admin_chat_messages ADD COLUMN is_deleted TINYINT(1) DEFAULT 0;
ALTER TABLE admin_chat_messages ADD COLUMN updated_at DATETIME NULL;

-- ایجاد ایندکس برای بهبود سرعت در فیلتر کردن پیام‌های حذف شده
CREATE INDEX idx_is_deleted ON admin_chat_messages(is_deleted);

-- ایجاد جدول خوانده شدن پیام‌های چت
CREATE TABLE IF NOT EXISTS admin_chat_reads (
    id INT AUTO_INCREMENT PRIMARY KEY,
    message_id INT NOT NULL,
    username VARCHAR(50) NOT NULL,
    read_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_read (message_id, username),
    FOREIGN KEY (message_id) REFERENCES admin_chat_messages(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

