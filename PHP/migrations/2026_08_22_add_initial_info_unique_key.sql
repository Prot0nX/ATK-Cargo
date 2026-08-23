-- PHP/migrations/2026_08_22_add_initial_info_unique_key.sql
--
-
-- InitialInfo (loadingQuotaNumber, shipName, loadingWarehouse, shippingCompany,
-- cargoType). idx_initial_info_composite فعلی روی همین ستون‌ها غیریکتاست، پس
-- چیزی مانع ثبت دو ردیف با کلید طبیعی یکسان نیست — دقیقاً پیش‌شرط باگ
-- بیش‌شماری JOIN که در بخش Database گزارش مستند شده.
--
-- ⚠️ قبل از اجرا حتماً کوئری زیر را روی production بزنید. اگر ردیفی برگرداند
-- یعنی همین الان تکراری وجود دارد و ALTER TABLE پایین با خطای
-- «Duplicate entry ... for key uk_initial_natural» رد می‌شود (بی‌خطر و
-- atomic؛ هیچ داده‌ای تغییر نمی‌کند) — ابتدا باید دستی تصمیم بگیرید کدام
-- ردیف تکراری درست است (رکوردهای CargoInfo مرتبط، remainingWeight/percentage
-- فعلی و تاریخچه‌ی هرکدام را بررسی کنید) و بقیه را ادغام/حذف کنید، سپس این
-- migration را دوباره اجرا کنید. تشخیص خودکار اینجا عمداً انجام نشده چون
-- انتخاب نادرست می‌تواند داده‌ی کوتاژ واقعی را از بین ببرد.
--
-- SELECT loadingQuotaNumber, shipName, loadingWarehouse, shippingCompany, cargoType,
-- COUNT(*) AS dup_count, GROUP_CONCAT(id ORDER BY id) AS row_ids
-- FROM InitialInfo
-- GROUP BY loadingQuotaNumber, shipName, loadingWarehouse, shippingCompany, cargoType
-- HAVING dup_count > 1;
--
-- این migration مستقل و بدون downtime قابل اجراست (InnoDB online DDL، فقط
-- افزودن ایندکس). مرحله‌ی ۲ (2026_08_22_add_cargo_info_initial_info_fk.sql)
-- به اجرای موفق همین فایل روی همان دیتابیس وابسته است — بدون آن، ستون
-- initial_info_id نمی‌تواند با اطمینان backfill شود چون کلید طبیعی هنوز
-- می‌تواند به چند ردیف اشاره کند.

ALTER TABLE `InitialInfo`
  ADD UNIQUE KEY `uk_initial_natural`
    (`loadingQuotaNumber`, `shipName`, `loadingWarehouse`, `shippingCompany`, `cargoType`);
