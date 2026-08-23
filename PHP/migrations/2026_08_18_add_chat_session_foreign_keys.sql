-- PHP/migrations/2026_08_18_add_chat_session_foreign_keys.sql
--
-
-- (admin_chat_messages، admin_chat_reads) و نشست (user_sessions) که تا امروز
-- فقط با username/message_id خام (بدون قید ارجاعی) به Users/admin_chat_messages
-- وصل بودند — هیچ FK ای قبلاً در schema.sql تعریف نشده بود.
--
-- ⚠️ قبل از اجرا حتماً از دیتابیس backup بگیرید. این فایل دو بخش دارد:
-- بخش ۱ رکوردهای یتیم را حذف می‌کند (غیرقابل بازگشت، از پیش بررسی کنید چند
-- ردیف تحت تأثیر قرار می‌گیرد)، بخش ۲ قیدهای FK را اضافه می‌کند. اگر بخش ۲
-- با خطای «Cannot add foreign key constraint» شکست خورد یعنی هنوز رکورد
-- یتیمی باقی مانده — با کوئری‌های زیر پیدایش کنید و بخش ۱ را دوباره اجرا
-- کنید:
--
-- SELECT * FROM user_sessions WHERE username NOT IN (SELECT username FROM Users);
-- SELECT * FROM admin_chat_messages WHERE username NOT IN (SELECT username FROM Users);
-- SELECT * FROM admin_chat_reads WHERE username NOT IN (SELECT username FROM Users)
-- OR message_id NOT IN (SELECT id FROM admin_chat_messages);
--
-- تصمیم عمدی — دامنه: audit_log و CargoInfo/InitialInfo عمداً بیرون از این
-- migration ماندند؛ دقیقاً همان دو گروه («چت، نشست») را مشخص کرده.
-- audit_log باید حتی بعد از حذف کاربر باقی بماند (سابقه‌ی ممیزی)، پس گرفتن
-- FK رو به Users برایش عمداً رد شد.
--
-- تصمیم عمدی — ON DELETE: برای user_sessions از CASCADE استفاده شده
-- (نشست‌ها موقتی‌اند؛ حذف کاربر باید نشست‌هایش را هم پاک کند — دقیقاً همان
-- کاری که UserService::deleteUser با deactivateAllSessions از قبل به‌صورت
-- دستی انجام می‌دهد). برای admin_chat_messages/admin_chat_reads از RESTRICT
-- استفاده شده تا حذف یک کاربر که سابقه‌ی چت دارد، آن سابقه را بی‌صدا از بین
-- نبرد (جدول از قبل ستون is_deleted برای soft-delete دارد؛ حذف واقعی ردیف
-- عملاً نباید در جریان عادی رخ دهد). اگر رفتار دلخواه شما پاک‌شدن خودکار
-- چت‌های کاربر حذف‌شده است، RESTRICT را در بخش ۲ به CASCADE تغییر دهید
-- توجه: چون ستون username این دو جدول NOT NULL است، گزینه‌ی SET NULL بدون
-- تغییر تعریف ستون به NULL-پذیر ممکن نیست.
--
-- این migration مستقل از session_token migration قبلی است و می‌تواند جدا
-- (نه لزوماً هم‌زمان با deploy) اجرا شود.

-- ============================================================
-- بخش ۱: پاک‌سازی رکوردهای یتیم (باید قبل از بخش ۲ اجرا شود)
-- ============================================================

-- ترتیب مهم است: ابتدا فرزند (admin_chat_reads) سپس والد (admin_chat_messages)،
-- وگرنه حذف پیام‌ها پیش از حذف read-هایشان چیزی را از قلم نمی‌اندازد ولی
-- ترتیب معکوس ممکن است رکوردهای یتیم جدید در admin_chat_reads بسازد.

DELETE FROM admin_chat_reads
WHERE username NOT IN (SELECT username FROM Users);

DELETE FROM admin_chat_messages
WHERE username NOT IN (SELECT username FROM Users);

-- بعد از حذف پیام‌های یتیم بالا، هر read مربوط به همان پیام‌ها هم یتیم شده:
DELETE FROM admin_chat_reads
WHERE message_id NOT IN (SELECT id FROM admin_chat_messages);

DELETE FROM user_sessions
WHERE username NOT IN (SELECT username FROM Users);

-- ============================================================
-- بخش ۲: افزودن قیدهای FK
-- ============================================================

ALTER TABLE `user_sessions`
  ADD CONSTRAINT `fk_user_sessions_username`
  FOREIGN KEY (`username`) REFERENCES `Users` (`username`)
  ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `admin_chat_messages`
  ADD CONSTRAINT `fk_chat_messages_username`
  FOREIGN KEY (`username`) REFERENCES `Users` (`username`)
  ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE `admin_chat_reads`
  ADD CONSTRAINT `fk_chat_reads_message_id`
  FOREIGN KEY (`message_id`) REFERENCES `admin_chat_messages` (`id`)
  ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `admin_chat_reads`
  ADD CONSTRAINT `fk_chat_reads_username`
  FOREIGN KEY (`username`) REFERENCES `Users` (`username`)
  ON DELETE RESTRICT ON UPDATE CASCADE;
