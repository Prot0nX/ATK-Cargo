# Migrations

این پروژه migration runner خودکار ندارد (پروژه‌ای کوچک با یک سرور، بدون
pipeline). فایل‌های این پوشه فقط SQL ساده‌اند که باید **دستی** و **به ترتیب
نام فایل** روی دیتابیس تولید اجرا شوند، معمولاً هم‌زمان با deploy همان
commit.

## قرارداد نام‌گذاری

```
YYYY_MM_DD_<توضیح-کوتاه-انگلیسی>.sql
```

## نحوه‌ی اجرا

```bash
mysql -u <user> -p atk_cargo < migrations/<filename>.sql
```

هر فایل کامنت بالای خودش را دارد که توضیح می‌دهد چرا لازم است و چه زمانی
(قبل/بعد/هم‌زمان با deploy کد) باید اجرا شود — قبل از اجرا آن را بخوانید.

## وضعیت schema.sql

`PHP/schema.sql` یک export ساختاری کامل و به‌روز از دیتابیس تولید است (نه
migration تجمعی) — برای مرجع/مستندسازی و بازسازی یک نمونه‌ی تازه از صفر،
نه برای اعمال روی یک دیتابیس موجود.

## migrationهای اعمال‌شده تاکنون

| فایل | چه کاری می‌کند | وضعیت |
|---|---|---|
| `2026_08_18_hash_session_tokens.sql` | تبدیل `session_token`/`refresh_token` موجود در `user_sessions` از plaintext به SHA-256 | باید هم‌زمان با deploy کد Phase 2.1 اجرا شود |
| `2026_08_18_add_chat_session_foreign_keys.sql` | حذف رکوردهای یتیم و افزودن FK به `user_sessions`/`admin_chat_messages`/`admin_chat_reads` (Phase 3.9) | نوشته شده، **هنوز روی هیچ دیتابیسی اجرا نشده** — قبل از اجرا کامنت‌های بالای فایل (به‌خصوص تصمیم ON DELETE) را بخوانید |
| `2026_08_19_permissions_to_database.sql` | ساخت `role_permissions`/`user_permissions` + seed از `config/permissions.json` فعلی (Phase 4.7) | نوشته شده و با یک دیتابیس MariaDB واقعی (throwaway) end-to-end تست شد — **هنوز روی دیتابیس تولید اجرا نشده**. تا اجرا نشود، `PermissionService` خودکار به فایل JSON قدیمی fallback می‌کند و `PermissionManager.php` یک بنر هشدار نشان می‌دهد؛ اگر `permissions.json` تولید با نسخه‌ی این ریپازیتوری فرق دارد، seed را قبل از اجرا اصلاح کنید (کامنت انتهای فایل را ببینید) |
| `2026_08_19_add_cargo_tracking_index.sql` | افزودن `idx_cargo_tracking`/`idx_cargo_ship_tracking` روی `CargoInfo` (DEEP_CODE_REVIEW.md Phase1.7) | نوشته شده و روی MariaDB throwaway با ۱۰٬۰۰۰ ردیف تست شد (`EXPLAIN`: type از `ALL`+filesort به `ref`/`range` بدون filesort رسید) — **هنوز روی دیتابیس تولید اجرا نشده**؛ بدون downtime قابل اجراست (InnoDB online DDL) |
| `2026_08_19_create_audit_log_table.sql` | ساخت جدول `audit_log` روی دیتابیس تولید موجود (Phase 5.19) | `CREATE TABLE IF NOT EXISTS` — بی‌خطر اگر جدول از قبل وجود داشته باشد. اگر این جدول هرگز روی production ساخته نشده، همه‌ی فراخوانی‌های `AuditLogger::log()` تا امروز بی‌صدا شکست خورده‌اند؛ برای دیدن لاگ‌های audit جدید (از جمله رفع Phase 5.19) باید همین migration هم اجرا شود |
| `2026_08_20_extend_licenses_table.sql` | افزودن `plan`/`expires_at`/`contact_*`/`notes`/`updated_at` و ایندکس‌های `idx_created_at`/`idx_expires_at` به `licenses` (بازنویسی پنل `PHP/Lic`) | روی MariaDB 10.4 throwaway با ۱۰٬۰۰۰ ردیف end-to-end تست شد — **هنوز روی دیتابیس تولید اجرا نشده**؛ باید هم‌زمان با deploy همین commit اجرا شود، چون `LicenseRepository` به این ستون‌ها ارجاع می‌دهد. رکوردهای موجود `expires_at = NULL` (نامحدود) می‌گیرند، پس رفتار لایسنس‌های فعلی و پاسخ `license/validate` برایشان تغییر نمی‌کند. بدون downtime (InnoDB online DDL). توجه: `idx_expires_at` واقعاً استفاده می‌شود (`type=range`)، اما `idx_created_at` با `LIMIT` بزرگِ فعلی انتخاب نمی‌شود و برای صفحه‌بندی آینده است — توضیح کامل در کامنت خود فایل |
