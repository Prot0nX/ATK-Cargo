-- PHP/migrations/2026_08_19_permissions_to_database.sql
--
-
-- config/permissions.json به دو جدول دیتابیس. تا امروز هر بررسی مجوز
-- (PermissionService::getUserPermissions) یک فایل JSON مشترک را می‌خواند —
-- بدون تراکنش، بدون قفل ردیفی، و نوشتن هم‌زمان دو ادمین در بدترین حالت با
-- rename اتمیک (Phase 2.7) فقط از corrupt شدن فایل جلوگیری می‌کرد، نه از
-- گم‌شدن یکی از دو تغییر (آخرین rename برنده است).
--
-- ⚠️ قبل از اجرا از دیتابیس backup بگیرید. این migration را هم‌زمان با
-- deploy کد Phase 4.7 اجرا کنید — کد جدید (PermissionService) اگر این
-- جداول را نیابد، به‌صورت خودکار و بی‌خطا به فایل permissions.json قدیمی
-- برمی‌گردد (fallback عمدی، نه رفتار خراب)، پس ترتیب اجرا (قبل/بعد از
-- deploy) بحرانی نیست — اما تا اجرای این فایل، فایل JSON هنوز منبع واقعی
-- است و PermissionManager.php مدیریت آن را نشان نمی‌دهد.
--
-- مقادیر seed زیر دقیقاً از config/permissions.json فعلی این ریپازیتوری
-- کپی شده‌اند (بخش users آن در این ریپازیتوری خالی است، پس هیچ ردیفی برای
-- user_permissions seed نمی‌شود).

CREATE TABLE IF NOT EXISTS role_permissions (
  role VARCHAR(50) NOT NULL,
  feature VARCHAR(50) NOT NULL,
  allowed TINYINT(1) NOT NULL DEFAULT 0,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (role, feature)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- username به‌جای FK روی Users.id عمداً روی Users.username (که خودش
-- UNIQUE KEY دارد) تعریف شده: PermissionService/PermissionManager در
-- سراسر کد فقط username رشته‌ای دارند، نه شناسه‌ی عددی کاربر (همان الگویی
-- که user_sessions/admin_chat_messages هم استفاده می‌کنند).
CREATE TABLE IF NOT EXISTS user_permissions (
  username VARCHAR(50) NOT NULL,
  feature VARCHAR(50) NOT NULL,
  allowed TINYINT(1) NOT NULL DEFAULT 0,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (username, feature),
  CONSTRAINT fk_user_permissions_username FOREIGN KEY (username)
    REFERENCES Users(username) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO role_permissions (role, feature, allowed) VALUES
  ('admin', 'initial_info', 1),
  ('admin', 'select_info', 1),
  ('admin', 'cargo_counter', 1),
  ('admin', 'manage_ships', 1),
  ('admin', 'manage_users', 1),
  ('admin', 'admin_chat', 1),
  ('admin', 'edit_cargo', 1),
  ('admin', 'delete_cargo', 1),
  ('admin', 'view_reports', 1),
  ('admin', 'active_quotas', 1),
  ('admin', 'tonnage_warning', 1),
  ('admin', 'manage_quotas', 1),
  ('admin', 'view_monitoring', 1),

  ('operator', 'initial_info', 1),
  ('operator', 'select_info', 1),
  ('operator', 'cargo_counter', 0),
  ('operator', 'manage_ships', 0),
  ('operator', 'manage_users', 0),
  ('operator', 'admin_chat', 0),
  ('operator', 'edit_cargo', 0),
  ('operator', 'delete_cargo', 0),
  ('operator', 'view_reports', 0),
  ('operator', 'active_quotas', 0),
  ('operator', 'tonnage_warning', 0),
  ('operator', 'manage_quotas', 0),
  ('operator', 'view_monitoring', 0),

  ('verifier', 'initial_info', 0),
  ('verifier', 'select_info', 0),
  ('verifier', 'cargo_counter', 1),
  ('verifier', 'manage_ships', 0),
  ('verifier', 'manage_users', 0),
  ('verifier', 'admin_chat', 0),
  ('verifier', 'edit_cargo', 0),
  ('verifier', 'delete_cargo', 0),
  ('verifier', 'view_reports', 0),
  ('verifier', 'active_quotas', 0),
  ('verifier', 'tonnage_warning', 0),
  ('verifier', 'manage_quotas', 0),
  ('verifier', 'view_monitoring', 0);

-- ⚠️ اگر روی دیتابیس تولید شما config/permissions.json حاوی مقادیر متفاوت
-- یا بخش "users" غیرخالی است (تنظیمات اختصاصی کاربر)، قبل از اجرای این
-- فایل آن را باز کنید و INSERTهای بالا / یک بخش user_permissions مشابه را
-- متناسب با آن اصلاح کنید — این seed فقط بازتاب دقیق نسخه‌ی این ریپازیتوری
-- در تاریخ نوشتن migration است، نه لزوماً آنچه امروز روی سرور شماست.
