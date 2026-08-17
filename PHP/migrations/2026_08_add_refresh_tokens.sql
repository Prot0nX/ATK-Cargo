-- I-05: افزودن access token کوتاه‌مدت (۳۰ دقیقه) + refresh token (۲۴ ساعت،
-- sliding) به جدول نشست‌ها. session_token فعلی همان access token باقی می‌ماند
-- (بدون تغییر نام/نوع ستون)؛ فقط دو ستون جدید اضافه می‌شود.
--
-- نشست‌های فعال قبل از این migration مقدار NULL برای این دو ستون می‌گیرند —
-- یعنی بلافاصله «access token منقضی» در نظر گرفته می‌شوند و کاربر باید یک‌بار
-- دوباره وارد شود (طبق تصمیم: عمر ۳۰ دقیقه‌ای از همین الان روی همه‌ی نشست‌ها
-- اعمال می‌شود، نه فقط نشست‌های جدید). این طبیعی و یک‌باره است، نه یک باگ.
--
-- توجه: این migration قبلاً توسط کاربر روی دیتابیس واقعی اجرا شده است
-- (تأیید شده با تست کامل login/refresh/reuse-detection روی atk-nk.ir/Cargo/test_api).
-- این فایل برای مستندسازی و بازتولیدپذیری نگه داشته می‌شود.

ALTER TABLE user_sessions
    ADD COLUMN access_token_expires_at DATETIME NULL AFTER session_token,
    ADD COLUMN refresh_token VARCHAR(64) NULL AFTER access_token_expires_at,
    ADD COLUMN refresh_token_expires_at DATETIME NULL AFTER refresh_token,
    ADD UNIQUE KEY idx_refresh_token (refresh_token);

-- ==================== Rollback ====================
-- ALTER TABLE user_sessions
--     DROP KEY idx_refresh_token,
--     DROP COLUMN refresh_token_expires_at,
--     DROP COLUMN refresh_token,
--     DROP COLUMN access_token_expires_at;
