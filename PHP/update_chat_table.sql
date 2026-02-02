-- آپدیت جدول پیام‌های چت برای افزودن قابلیت‌های جدید
-- اجرای این اسکریپت برای بازطراحی سیستم چت الزامی است

ALTER TABLE admin_chat_messages ADD COLUMN is_deleted TINYINT(1) DEFAULT 0;
ALTER TABLE admin_chat_messages ADD COLUMN updated_at DATETIME NULL;

-- ایجاد ایندکس برای بهبود سرعت در فیلتر کردن پیام‌های حذف شده (اختیاری ولی توصیه شده)
CREATE INDEX idx_is_deleted ON admin_chat_messages(is_deleted);
