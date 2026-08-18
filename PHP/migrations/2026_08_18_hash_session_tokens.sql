-- PHP/migrations/2026_08_18_hash_session_tokens.sql
--
-- DEEP_CODE_AUDIT.md #Phase2.1 — session_token/refresh_token در user_sessions
-- از این commit به بعد به‌صورت SHA-256(token) ذخیره می‌شوند، نه plaintext.
-- ردیف‌های موجود قبل از این migration هنوز plaintext هستند و بدون این
-- migration، هیچ نشست فعال فعلی دیگر معتبر شناخته نمی‌شود (چون کوئری‌های
-- جدید ورودی را هش می‌کنند و با مقدار plaintext ذخیره‌شده مطابقت نمی‌یابد).
--
-- این migration باید دقیقاً هم‌زمان با deploy کد جدید اجرا شود (نه قبل،
-- نه بعد) — قبل از آن، نشست‌های فعال با کد قدیمی (که plaintext مقایسه
-- می‌کند) کار می‌کنند؛ اگر migration زودتر اجرا شود، کد قدیمی دیگر نمی‌تواند
-- نشست‌ها را معتبر تشخیص دهد.
--
-- جایگزین ساده‌تر (اگر باطل‌شدن همه‌ی نشست‌های فعال در لحظه‌ی deploy قابل
-- قبول است، به‌جای اجرای این migration):
--   UPDATE user_sessions SET is_active = 0;

UPDATE user_sessions
SET session_token = SHA2(session_token, 256)
WHERE session_token IS NOT NULL;

UPDATE user_sessions
SET refresh_token = SHA2(refresh_token, 256)
WHERE refresh_token IS NOT NULL;
