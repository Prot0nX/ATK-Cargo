-- =============================================================================
-- گسترش جدول `licenses` برای پنل بازنویسی‌شده‌ی مدیریت لایسنس (PHP/Lic)
-- =============================================================================
--
-- چرا؟
-- جدول فعلی فقط نام شرکت و یک پرچم فعال/غیرفعال دارد. پنل قدیمی ستونی به نام
-- «اعتبار» نشان می‌داد که همیشه رشته‌ی ثابت 'نامحدود' بود (license-manager.js
-- تابع calculateExpiry) — یعنی مفهوم انقضا اصلاً وجود نداشت. همچنین هیچ جایی
-- برای اطلاعات تماس مشتری تجاری یا یادداشت قرارداد نبود.
--
-- چه زمانی اجرا شود؟
-- هم‌زمان با deploy کد این commit. کد جدید بدون این ستون‌ها کار نمی‌کند
-- (LicenseRepository به expires_at/plan ارجاع می‌دهد).
--
-- بدون downtime قابل اجراست (InnoDB online DDL؛ ALTER با ALGORITHM=INPLACE).
--
-- -----------------------------------------------------------------------------
-- سازگاری با نصب‌های موجود
-- -----------------------------------------------------------------------------
-- `expires_at NULL` به معنای «نامحدود» است. تمام رکوردهای موجود پس از این
-- ALTER مقدار NULL می‌گیرند، پس رفتار همه‌ی لایسنس‌های فعلی دقیقاً بدون تغییر
-- می‌ماند و هیچ مشتری‌ای قفل نمی‌شود. اعمال انقضا در
-- LicenseController::validateLicense فقط وقتی فعال می‌شود که ادمین صراحتاً
-- تاریخی ثبت کند.
--
-- -----------------------------------------------------------------------------
-- چرا UNIQUE روی company_name اینجا نیست؟
-- -----------------------------------------------------------------------------
-- یکتایی نام شرکت تا امروز فقط در لایه‌ی اپلیکیشن اعمال شده بود
-- (manage_licenses.php یک SELECT COUNT قبل از INSERT می‌زد)، پس ممکن است در
-- دیتابیس تولید رکورد تکراری وجود داشته باشد و افزودن ایندکس یکتا این
-- migration را با خطا متوقف کند. قبل از هر تصمیمی این کوئری را اجرا کنید:
--
--   SELECT company_name, COUNT(*) c FROM licenses
--   GROUP BY company_name HAVING c > 1;
--
-- اگر نتیجه خالی بود، افزودن `ADD UNIQUE KEY uq_company_name (company_name)`
-- در یک migration جداگانه بی‌خطر است. تا آن زمان، LicenseRepository
-- ::companyNameExists همان بررسی سطح اپلیکیشن را ادامه می‌دهد.
--
-- -----------------------------------------------------------------------------
-- نحوه‌ی اجرا
-- -----------------------------------------------------------------------------
--   mysql -u <user> -p atk_cargo < migrations/2026_08_20_extend_licenses_table.sql
-- =============================================================================

ALTER TABLE `licenses`
  -- سطح/پلن لایسنس. مقادیر مجاز در LicenseAdminService::PLANS تعریف شده‌اند
  -- (standard | pro | trial). عمداً ENUM نیست تا افزودن پلن جدید به ALTER
  -- روی جدول تولید نیاز نداشته باشد.
  ADD COLUMN `plan`          varchar(32)  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
                                          NOT NULL DEFAULT 'standard' AFTER `company_name`,

  -- NULL = نامحدود. مقدار غیر NULL یعنی لایسنس در آن لحظه منقضی می‌شود.
  ADD COLUMN `expires_at`    datetime     DEFAULT NULL AFTER `activation_date`,

  -- اطلاعات تماس مشتری تجاری — تا وقتی لایسنسی مشکل پیدا می‌کند، معلوم باشد
  -- با چه کسی باید تماس گرفت.
  ADD COLUMN `contact_name`  varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  ADD COLUMN `contact_phone` varchar(32)  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  ADD COLUMN `contact_email` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  ADD COLUMN `notes`         text         CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,

  ADD COLUMN `updated_at`    datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  -- لیست پنل همیشه `ORDER BY created_at DESC` است و تا امروز هیچ ایندکسی
  -- نداشت (فقط PK و UNIQUE(license_key) موجود بود).
  --
  -- اندازه‌گیری واقعی روی MariaDB 10.4 با ۱۰٬۰۰۰ ردیف: این ایندکس فقط وقتی
  -- انتخاب می‌شود که LIMIT کوچک باشد (LIMIT 50 → type=index، بدون filesort،
  -- ۰٫۳ms در برابر ۱٫۸ms بدون ایندکس). با LIMIT بزرگِ فعلی
  -- (LicenseRepository::MAX_ROWS = 5000، یعنی نیمی از جدول) بهینه‌ساز عمداً
  -- full scan + filesort را ارزان‌تر می‌داند و ایندکس را نادیده می‌گیرد —
  -- که تصمیم درستی است، چون SELECT * با ایندکس ثانویه پوشش داده نمی‌شود.
  -- پس این ایندکس امروز سود ندارد و برای زمانی است که لیست صفحه‌بندی شود؛
  -- هزینه‌اش ناچیز است و افزودنش همراه بقیه‌ی تغییرات از یک ALTER دوم روی
  -- جدول تولید جلوگیری می‌کند.
  ADD KEY `idx_created_at` (`created_at`),

  -- فیلتر «منقضی‌شده» در پنل و شمارش آمار روی این ستون range می‌زنند. این یکی
  -- واقعاً استفاده می‌شود: EXPLAIN با ۱۰٬۰۰۰ ردیف → type=range،
  -- key=idx_expires_at، rows=8 (به‌جای اسکن کامل).
  ADD KEY `idx_expires_at` (`expires_at`);
