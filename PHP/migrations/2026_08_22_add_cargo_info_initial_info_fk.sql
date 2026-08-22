-- PHP/migrations/2026_08_22_add_cargo_info_initial_info_fk.sql
--
-- DEEP_CODE_AUDIT.md فاز۴ #۳۷ (مرحله ۲ از ۲) — افزودن initial_info_id به
-- CargoInfo، backfill از روی کلید طبیعی موجود، و اعمال FK واقعی به InitialInfo.
--
-- ⚠️ پیش‌نیاز اجباری: 2026_08_22_add_initial_info_unique_key.sql باید قبلاً
-- روی همین دیتابیس با موفقیت اجرا شده باشد. بدون آن، اگر InitialInfo هنوز
-- ردیف‌های تکراری با کلید طبیعی یکسان داشته باشد، UPDATE...JOIN بخش ۲ ممکن
-- است هر ردیف CargoInfo را به‌طور غیرقطعی به یکی از چند InitialInfo تطبیق‌دار
-- وصل کند. با کوئری زیر تأیید کنید ایندکس یکتا از قبل وجود دارد:
--
--   SHOW INDEX FROM InitialInfo WHERE Key_name = 'uk_initial_natural';
--
-- اگر خروجی خالی بود، ابتدا migration مرحله ۱ را اجرا کنید.
--
-- بعد از اجرای این فایل، CargoRepository::insertCargo باید initial_info_id
-- را هم پر کند (تغییر کد همراه در همین commit) وگرنه رکوردهای جدید بعد از
-- این migration با initial_info_id = NULL ثبت می‌شوند و FK فقط رکوردهای
-- قدیمی backfill‌شده را پوشش می‌دهد، نه داده‌ی آینده را.
--
-- تصمیم عمدی — ON DELETE CASCADE: دقیقاً هم‌رفتار با QuotaService::deleteQuota
-- فعلی که خودش صریحاً ابتدا CargoInfo مرتبط و سپس InitialInfo را حذف می‌کند
-- (src/Services/QuotaService.php:622-630) — این FK فقط همان رفتار موجود
-- اپلیکیشن را در سطح دیتابیس هم تضمین می‌کند، رفتار جدیدی اضافه نمی‌کند.
--
-- initial_info_id عمداً NULL-پذیر است: رکوردهای CargoInfo قدیمی که کوتاژشان
-- قبلاً (پیش از این migration) حذف شده، کلید طبیعی منطبقی در InitialInfo
-- ندارند و باید بدون خطا NULL بمانند — FK در MySQL مقادیر NULL را از بررسی
-- ارجاعی معاف می‌کند، پس این ردیف‌های یتیم قدیمی مانع ADD CONSTRAINT نمی‌شوند.

-- ============================================================
-- بخش ۱: افزودن ستون
-- ============================================================

ALTER TABLE `CargoInfo`
  ADD COLUMN `initial_info_id` int DEFAULT NULL AFTER `loadingQuotaNumber`;

-- ============================================================
-- بخش ۲: backfill از روی کلید طبیعی موجود
-- ============================================================

UPDATE `CargoInfo` c
JOIN `InitialInfo` i
  ON c.loadingQuotaNumber = i.loadingQuotaNumber
  AND c.shipName = i.shipName
  AND c.loadingWarehouse = i.loadingWarehouse
  AND c.shippingCompany = i.shippingCompany
  AND c.cargoType = i.cargoType
SET c.initial_info_id = i.id
WHERE c.initial_info_id IS NULL;

-- بعد از UPDATE بالا، این کوئری را برای بازبینی دستی اجرا کنید — ردیف‌های
-- برگشتی، حواله‌های قدیمی متعلق به کوتاژی هستند که دیگر در InitialInfo وجود
-- ندارد (مثلاً از قبل حذف شده)؛ رفتار عادی است، initial_info_id آن‌ها برای
-- همیشه NULL می‌ماند مگر با اصلاح دستی:
--
--   SELECT id, loadingQuotaNumber, shipName, loadingWarehouse, shippingCompany, cargoType
--   FROM CargoInfo WHERE initial_info_id IS NULL;

-- ============================================================
-- بخش ۳: افزودن قید FK (به کلید اصلی InitialInfo.id، نه کلید طبیعی)
-- ============================================================

ALTER TABLE `CargoInfo`
  ADD CONSTRAINT `fk_cargo_initial_info`
  FOREIGN KEY (`initial_info_id`) REFERENCES `InitialInfo` (`id`)
  ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `CargoInfo`
  ADD KEY `idx_cargo_initial_info_id` (`initial_info_id`);
