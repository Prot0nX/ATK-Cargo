-- PHP/migrations/2026_08_19_add_cargo_tracking_index.sql
--
-
-- (`WHERE trackingNumber = ? ORDER BY entryTime DESC`) و CargoRepository.php:32
-- (`WHERE shipName = ? AND trackingNumber = ? AND updated_at >= ?`) روی
-- CargoInfo هیچ ایندکسی که با trackingNumber شروع شود ندارند → full table
-- scan (type=ALL) + Using filesort روی جستجوی حواله، یکی از پرکاربردترین
-- مسیرهای اپ.
--
-- با یک دیتابیس throwaway محلی (۱۰٬۰۰۰ ردیف تصادفی) تأیید شد:
-- قبل: type=ALL, rows=10000, Extra=Using where; Using filesort
-- بعد: type=ref, rows=1, Extra=Using where (بدون filesort)
--
-- بدون قفل طولانی روی جدول تولید لازم نیست؛ افزودن ایندکس روی InnoDB به‌صورت
-- online انجام می‌شود (ALGORITHM=INPLACE پیش‌فرض از MySQL 5.6 / MariaDB 10.0+).

ALTER TABLE CargoInfo
  ADD INDEX idx_cargo_tracking (trackingNumber, entryTime);

ALTER TABLE CargoInfo
  ADD INDEX idx_cargo_ship_tracking (shipName, trackingNumber, updated_at);
