# پلن بازساختاردهی PHP به `public/` (DEEP_CODE_AUDIT.md #Phase3.13)

**وضعیت:** فقط طرح/مستند — **هیچ فایلی جابجا نشده است.** طبق تصمیم صریح در جلسه‌ی
ممیزی: این تغییر باید هم‌زمان با تغییر Document Root وب‌سرور تولید انجام شود که
کاملاً بیرون از این repo و بیرون از دسترسی دستیار است؛ اجرای کورکورانه‌ی
جابجایی فایل بدون هماهنگی سرور یعنی قطعی کامل همه‌ی endpointها (شامل چیزی که
اپ اندروید بهش وصل می‌شود).

## چرا لازم است

- `PHP/vendor/` (شامل phpunit، phpstan، nikic/php-parser) کامیت‌شده در گیت و
  **در web root فعلی مستقر** است — یعنی از طریق HTTP قابل دسترسی مستقیم است
  مگر این‌که .htaccess جلویش را بگیرد (که خودش شکننده است، نگاه کنید به
  بخش «چرا فقط .htaccess کافی نیست»).
- `PHP/log/` و `PHP/logs/` (دو پوشه‌ی جدا برای همان مفهوم — خودش یک findای
  جداگانه است) هم داخل web root هستند.
- `PHP/config/config.php` شامل اطلاعات اتصال دیتابیس است و داخل web root است.
- تنها محافظ این‌ها همین لحظه چند بلوک `.htaccess` است.

### چرا فقط .htaccess کافی نیست

بلوک‌های `<Directory>` در `PHP/.htaccess` (خطوط ۶۰-۶۸) از نظر Apache فقط در
پیکربندی اصلی سرور (`httpd.conf` / vhost) معتبرند، **نه در فایل `.htaccess`**؛
روی بسیاری از هاست‌های اشتراکی این بلوک‌ها بی‌صدا نادیده گرفته می‌شوند (بدون
خطا). یعنی محافظت واقعی `config/`/`log/`/`logs/` امروز به پیکربندی دقیق همان
هاست وابسته و غیرقابل‌اعتماد است. تنها راه قطعی: این پوشه‌ها اصلاً زیر
Document Root نباشند.

## ساختار فعلی (مستند شده در این جلسه)

```
PHP/                              ← Document Root فعلی وب‌سرور
├── .htaccess
├── PermissionManager.php         # پنل ادمین (session-based auth)
├── SessionManager.php            # (احتمالاً نسخه‌ی قدیمی/جایگزین‌شده — بررسی شود)
├── check_signature.php
├── check_update.php
├── export_schema.php             # CLI-only guard دارد
├── file_manager.php
├── get_csrf_token.php
├── get_license_info.php
├── jdf.php
├── quota_remaining_api.php
├── update_config.php
├── update_fcm_token.php
├── validate_license.php
├── api/
│   └── v2/index.php              # تنها نقطه‌ی ورود API فعال (بعد از حذف protected_proxy.php در Phase3.1)
├── User/                         # ابزار جدای «مدیریت کاربران آنلاین» — HTML/CSS/JS + PHP API + کپی SessionManager.php/jdf.php
├── assets/
├── config/
│   └── config.php                # اتصال دیتابیس — حساس
├── log/                          # پروکسی قدیمی اینجا می‌نوشت (حذف شده، احتمالاً یتیم است)
├── logs/                         # Logger فعلی اینجا می‌نویسد
├── migrations/
├── src/                          # PSR-4 App\ → اینجا (composer.json)
├── tests/
└── vendor/                       # کامیت‌شده در گیت، شامل dev deps
```

## ساختار هدف

```
project-root/
├── public/                       ← Document Root جدید (فقط همین)
│   ├── index.html
│   ├── assets/
│   ├── api/v2/index.php
│   ├── check_update.php
│   ├── get_csrf_token.php
│   ├── ... (بقیه‌ی ۱۰ فایل entry فعال)
│   └── User/ (اگر همچنان لازم است؛ در غیر این صورت جدا بررسی و حذف شود)
├── src/                          ← خارج از web root
├── config/                       ← خارج از web root
├── storage/
│   └── logs/                     ← یکی‌شده از log/ + logs/، خارج از web root
├── migrations/
├── tests/
├── vendor/                       ← خارج از web root، و از گیت gitignore شود (finding جدا، #4/Phase2)
├── composer.json
└── jdf.php
```

## مراحل اجرا (ترتیب دقیق مهم است)

> ⚠️ این مراحل را **روی یک کپی/staging از سرور تولید** تمرین کنید، نه مستقیم
> روی production. برای production یک پنجره‌ی نگهداری (maintenance window) با
> امکان rollback سریع در نظر بگیرید.

1. **بک‌آپ کامل** از کل `PHP/` (فایل‌ها) و دیتابیس، قبل از هر تغییری.

2. **ساخت ساختار جدید در کنار ساختار فعلی** (نه جایگزینی فوری):
   ```
   git mv PHP/src        src
   git mv PHP/config     config
   git mv PHP/migrations migrations
   git mv PHP/tests      tests
   git mv PHP/vendor     vendor        # + از .gitignore خارج نشود، اضافه شود (finding جدا)
   git mv PHP/jdf.php    jdf.php
   mkdir storage
   git mv PHP/logs storage/logs        # log/ را جدا بررسی کنید: اگر واقعاً یتیم است (نوشته نمی‌شود)، حذف شود نه انتقال
   mkdir public
   git mv PHP/api            public/api
   git mv PHP/assets         public/assets
   git mv PHP/index.html     public/index.html
   git mv PHP/check_update.php public/check_update.php
   # ... بقیه‌ی فایل‌های entry فعال، یکی‌یکی
   ```
   `PermissionManager.php`، `User/`، `export_schema.php` را جداگانه تصمیم
   بگیرید — `export_schema.php` قبلاً guard دارد که فقط CLI اجرا شود (پس اصلاً
   نباید در `public/` باشد)؛ `User/` را بررسی کنید که هنوز استفاده می‌شود یا
   یک ابزار قدیمی موازی/یتیم است (اگر یتیم است، حذف بهتر از انتقال است).

3. **بروزرسانی مسیرهای require در هر فایل entyry منتقل‌شده** — الگوی فعلی
   دو شکل دارد که باید یکسان شوند:
   ```php
   // قبل (وقتی entry کنار src/ بود):
   require_once __DIR__ . '/src/bootstrap.php';
   // بعد (وقتی entry در public/ و src/ یک پله بالاتر است):
   require_once __DIR__ . '/../src/bootstrap.php';
   ```
   `api/v2/index.php` از قبل با `__DIR__ . '/../../src/bootstrap.php'` دو پله
   بالا می‌رود (چون در `api/v2/` است) — باید به `__DIR__ . '/../../../src/bootstrap.php'`
   تغییر کند (چون یک پوشه‌ی `public/` هم اضافه شده).

4. **بروزرسانی `bootstrap.php`** — `APP_ROOT = dirname(__DIR__)` امروز یعنی
   «پوشه‌ی بالای `src/`» که همان `PHP/` (web root فعلی) بود. بعد از انتقال
   `src/` به بیرون از `public/`، این تعریف همچنان درست کار می‌کند (چون هنوز
   یک پله بالای `src/` است — که حالا ریشه‌ی پروژه است)، **اما** هر جای دیگری
   که فرض کرده «APP_ROOT همان web root است» (مثلاً برای ساخت لینک دانلود یا
   مسیر فایل استاتیک) باید جدا بررسی و به `PUBLIC_ROOT` (ثابت جدید) ارجاع
   داده شود.

5. **بروزرسانی `composer.json`** در صورت جابجایی خودش — اگر `composer.json`
   هم به ریشه‌ی پروژه (کنار `public/`) منتقل شود، مسیر PSR-4 (`App\ → src/`)
   بدون تغییر درست می‌ماند چون نسبت به خودِ `composer.json` تعریف شده. بعد از
   جابجایی حتماً `composer dump-autoload` اجرا شود.

6. **`.htaccess` جدید در `public/`** — بلوک مسیریابی v2 را نگه دارید:
   ```apache
   RewriteCond %{REQUEST_FILENAME} !-f
   RewriteCond %{REQUEST_FILENAME} !-d
   RewriteRule ^api/v2/(.*)$ api/v2/index.php [L,QSA]
   ```
   بلوک‌های `<Directory config>` و `<DirectoryMatch (log|logs)>` را می‌توانید
   کاملاً حذف کنید — دیگر لازم نیستند چون آن پوشه‌ها اصلاً زیر Document Root
   نیستند.

7. **⚠️ گام حیاتی و هم‌زمان: تغییر Document Root وب‌سرور تولید به `public/`.**
   این گام را کسی که به کنترل‌پنل هاست/سرور تولید دسترسی دارد باید انجام دهد
   — **دقیقاً هم‌زمان** با deploy کد جدید (نه قبل، نه بعد):
   - اگر قبل از deploy تغییر کند: سرور فایل‌های قدیمی entry را دیگر در
     `public/` پیدا نمی‌کند (چون هنوز منتقل نشده‌اند) → 404 روی همه‌چیز.
   - اگر بعد از deploy تغییر کند: Document Root هنوز `PHP/` است ولی فایل‌های
     entry از آنجا به `public/` منتقل شده‌اند → همان 404.
   - راه امن‌تر برای کاهش این ریسک: **مرحله‌ی گذار موقت** — به‌جای حذف
     فایل‌های entry از `PHP/` (ریشه‌ی قدیمی)، آن‌ها را به یک `require`/redirect
     ساده به معادل‌شان در `public/` تبدیل کنید تا هر دو مسیر هم‌زمان کار کنند،
     سپس بعد از تأیید کارکرد کامل و تغییر واقعی Document Root، نسخه‌های قدیمی
     را حذف کنید.

8. **تست کامل روی staging** — همه‌ی endpointهایی که اپ اندروید صدا می‌زند
   (`ApiService.kt`/`ApiServiceV2.kt`) را دستی یا با یک اسکریپت smoke-test
   بزنید، از جمله لاگین، ثبت حواله، آپلود، آپدیت اپ.

9. **Deploy production** طبق چک‌لیست بالا، با پنجره‌ی نگهداری و rollback
   آماده (نگه‌داشتن نسخه‌ی قبلی فایل‌ها + Document Root قدیمی به‌عنوان fallback
   سریع در صورت مشکل).

## پیش‌نیازهای جانبی که باید قبل از این کار حل شوند

- **`vendor/` باید از گیت خارج شود** (`.gitignore`) — در غیر این صورت با هر
  clone تازه دوباره در جایی نامناسب سبز می‌شود. این finding جداگانه‌ای در
  گزارش است (ردیف #4/Phase2) و باید قبل یا هم‌زمان با این کار انجام شود.
- **`log/` در برابر `logs/`** — قبل از یکی‌سازی، مشخص کنید کدام یک واقعاً در
  حال حاضر نوشته می‌شود (به نظر می‌رسد `logs/` فعال و `log/` یتیم از دوران
  `protected_proxy.php` حذف‌شده باشد؛ با `grep -rn "__DIR__ . '/log'"` و
  `grep -rn "APP_ROOT . '/logs'"` در `src/` تأیید کنید) تا داده‌ای گم نشود.
- **`User/`** — بررسی شود که این ابزار مدیریت کاربران آنلاین هنوز در
  دسترس/استفاده است یا یک ابزار قدیمی موازی و یتیم؛ اگر یتیم است، حذف آن قبل
  از این بازساختاردهی، حجم کار را کم می‌کند.

## معیار موفقیت

- [ ] هیچ فایل PHP خارج از `public/` از طریق HTTP مستقیماً قابل درخواست نیست
      (تست: `curl -I https://.../../src/bootstrap.php` باید 403/404 بدهد، نه
      200).
- [ ] `vendor/`, `config/`, `storage/logs/` هیچ‌کدام از طریق HTTP در دسترس
      نیستند.
- [ ] همه‌ی endpointهای فعلی اپ اندروید (بدون تغییر URL) دقیقاً مثل قبل کار
      می‌کنند.
- [ ] `composer install` روی یک checkout تازه در محیط production بدون خطا
      اجرا می‌شود و ساختار جدید را می‌سازد.
