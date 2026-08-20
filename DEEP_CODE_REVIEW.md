# Deep Code Audit Report

**پروژه:** ATK-Cargo (Android + Kotlin + Jetpack Compose + PHP Backend + MySQL)
**تاریخ Audit:** 2026-08-19
**Commit مبنا:** `993e42d` (branch `main`)
**دامنه بررسی:** کل repository — ۱۸۰ فایل Kotlin (۵۲٬۷۳۱ خط)، ۶۲ فایل PHP اپلیکیشنی (۱۲٬۱۸۳ خط، بدون vendor)، schema دیتابیس، پیکربندی build، CI، و اطلاعات سرور production

> **توجه:** فایل `DEEP_CODE_AUDIT.md` (۳۲۰KB) از قبل در ریشه پروژه وجود دارد و بسیاری از یافته‌های آن قبلاً برطرف شده‌اند (ارجاعات آن در کامنت‌های کد دیده می‌شود). این گزارش یک بررسی **مستقل و از صفر** است و صرفاً وضعیت فعلی کد را ارزیابی می‌کند، نه تاریخچه‌ی آن.

---

## Executive Summary

### وضعیت کلی

ATK-Cargo یک پروژه‌ی **بالغ‌تر از حد انتظار** است. برخلاف الگوی رایج پروژه‌های مشابه، بخش قابل‌توجهی از کار امنیتی سنگین قبلاً و به‌درستی انجام شده است:

**آنچه واقعاً خوب است (تأییدشده با بررسی کد، نه فرض):**

- **صفر مورد SQL Injection.** تمام کوئری‌ها prepared statement هستند. سه نقطه‌ی ساخت SQL پویا (`UserRepository::update`، `QuotaService`، `ChatController`) بررسی شدند و هر سه با allow-list ستون یا قطعات ثابت محافظت می‌شوند.
- **مدیریت نشست در سطح production واقعی:** توکن‌های ۲۵۶ بیتی، ذخیره‌ی SHA-256 شده در دیتابیس، چرخش (rotation) هر دو توکن، تشخیص refresh token reuse با ابطال کل نشست‌ها + هشدار امنیتی.
- **bcrypt با cost=12** و مسیر مهاجرت تدریجی.
- **Certificate Pinning** با پین پشتیبان و تاریخ انقضا، `cleartextTrafficPermitted="false"`.
- **رمزنگاری توکن‌ها روی دستگاه** با AES-GCM از طریق AndroidKeyStore.
- **Router صریح opt-in** با گیت auth/permission در سطح router (نه در سطح کنترلر).
- **ایندکس‌گذاری دیتابیس در سطح حرفه‌ای** — از جمله covering index های چندستونی.
- **بهداشت Compose عالی:** ۱۰۲ مورد `collectAsStateWithLifecycle` و **صفر** مورد `collectAsState()` ساده؛ عملاً تمام `LazyColumn`ها key دارند.
- **Kotlin تمیز:** فقط ۹ مورد `!!`، صفر `GlobalScope`، صفر `println`.

### مهم‌ترین ریسک‌های امنیتی

| # | ریسک | Severity |
|---|------|----------|
| ۱ | کلید API و کلید لایسنس با **XOR تک‌بایتی** «مبهم‌سازی» شده و در git ذخیره‌اند — در چند ثانیه بازیابی شدند | **CRITICAL** |
| ۲ | سرور production روی **PHP 8.1.2** اجرا می‌شود که از دی ۱۴۰۴ پشتیبانی امنیتی ندارد | **HIGH** |
| ۳ | مسیر fallback احراز هویت، **رمز عبور متن‌خام** در دیتابیس را می‌پذیرد | **HIGH** |
| ۴ | **phpMyAdmin 5.1.1** روی سرور production نصب است | **HIGH** |
| ۵ | endpointهای لایسنس بدون auth و بدون rate limit، با کلید در **query string** | **MEDIUM** |

### مهم‌ترین مشکلات Architecture

مشکل اصلی معماری امنیتی نیست — **مرزبندی لایه‌هاست**:

- **۹ فایل Composable مستقیماً `RetrofitClient.apiServiceV2` را صدا می‌زنند** و ViewModel و Repository را کاملاً دور می‌زنند.
- بدتر: این تماس‌ها داخل `rememberCoroutineScope()` اجرا می‌شوند. یعنی **یک عملیات نوشتن (POST) با خروج کاربر از صفحه، در میانه‌ی راه cancel می‌شود** — این یک باگ یکپارچگی داده است، نه صرفاً یک ایراد سبک کدنویسی.
- ماژول‌بندی نیمه‌کاره: **۸۱٪ از کل کد (۴۲٬۵۸۴ از ۵۲٬۷۳۱ خط) هنوز در ماژول `app` است.**

### مهم‌ترین مشکلات Performance

- `WHERE trackingNumber = ?` روی جدول `CargoInfo` **بدون ایندکس** → full table scan + filesort.
- انیمیشن‌ها به‌جای `graphicsLayer` مستقیماً در Modifier خوانده می‌شوند → **recomposition در هر فریم** به‌جای صرفاً redraw.
- ۲۱ مورد `rememberInfiniteTransition` — از جمله روی صفحه‌ی اصلی — که حلقه‌ی فریم را دائماً فعال نگه می‌دارند.

### Technical Debt

۳۱ فایل Kotlin بیش از ۶۰۰ خط دارند (بزرگ‌ترین: ۱٬۴۲۰ خط). دو ViewModel خدای‌گونه (۹۶۲ و ۸۷۹ خط). پوشش تست تقریباً صفر است (۹ فایل تست برای ۵۲ هزار خط) و **CI اصلاً تست اندروید را اجرا نمی‌کند**.

### Production Readiness

پروژه **آماده‌ی production نیست**، اما فاصله‌اش کم است. سه مانع قطعی: چرخش secrets، ارتقای PHP، و افزودن signing config برای release. با ۲–۳ هفته کار متمرکز قابل رفع است.

### Overall Score: **6.1 / 10**

---

## Project Overview

```
ATK-Cargo/
├── app/                      42,584 خط Kotlin (۸۱٪ کل کد) ← بیش از حد بزرگ
├── core/
│   ├── common/                  137 خط
│   ├── database/                 98 خط  (Room — فقط کش چت)
│   ├── designsystem/          1,043 خط
│   ├── domain/                  283 خط
│   └── network/               2,002 خط  (Retrofit + توکن + Secrets)
├── feature/
│   ├── auth/                  1,216 خط
│   ├── admin/                 1,887 خط
│   ├── chat/                  2,594 خط
│   └── startup/                 834 خط
├── baselineprofile/
├── PHP/
│   ├── api/v2/index.php          نقطه ورود Router v2
│   ├── src/
│   │   ├── Core/                 Router, Request, Response, Database, ApiAuthGate
│   │   ├── Controllers/          ۱۰ کنترلر
│   │   ├── Services/             ۱۲ سرویس
│   │   ├── Repositories/         ۴ ریپازیتوری
│   │   ├── Validators/           InputValidator
│   │   └── routes/api_v2.php     جدول route صریح (۶۳۲ خط)
│   ├── PermissionManager.php     پنل وب مدیریت دسترسی (۶۷۵ خط)
│   ├── migrations/               ۳ migration
│   └── tests/Unit/               ۵ فایل تست
└── .github/workflows/ci.yml
```

**Stack:** Kotlin 2.2.20 · AGP 8.13.0 · Compose BOM 2025.09.00 · Koin 4.1.1 · Retrofit 3.0.0 · OkHttp 5.1.0 · Room 2.7.0 · PHP 8.1 · MySQL 8.0 · Apache 2.4.52

---

## Architecture Overview

### جریان واقعی داده (آنچه *باید* باشد)

```
Compose UI → ViewModel → UseCase → Repository → ApiServiceV2 → Router v2 (PHP)
                                                                    ↓
                                                            ApiAuthGate (auth + permission)
                                                                    ↓
                                                            Controller → Service → Repository → MySQL
```

### جریان واقعی داده (آنچه در ۹ فایل *هست*)

```
Compose UI ──────────────────────────────────→ ApiServiceV2 → Router v2 (PHP)
     ↑
 rememberCoroutineScope()
 (با خروج از صفحه cancel می‌شود)
```

سمت **سرور** معماری تمیزی دارد: تفکیک Controller/Service/Repository رعایت شده، تزریق وابستگی برای تست‌پذیری در `SessionService` وجود دارد، و گیت مجوز در سطح router متمرکز است.

سمت **کلاینت** معماری اعلام‌شده (MVVM + Repository) در بخش‌هایی واقعی و در بخش‌هایی صرفاً اسمی است.

### ارزیابی اصول

| اصل | وضعیت | توضیح |
|-----|-------|-------|
| Separation of Concerns | ⚠️ نسبی | سرور خوب؛ کلاینت در ۹ فایل نقض شده |
| Single Responsibility | ❌ | `CargoViewModel` (۹۶۲ خط)، `ActiveQuotasContent.kt` (۱٬۴۲۰ خط) |
| Dependency Inversion | ⚠️ نسبی | `UserPreferencesStore` در core:domain الگوی درستی است؛ اما UseCaseها `RetrofitClient` singleton را مستقیم می‌گیرند |
| Open/Closed | ⚠️ | کنترلرهای مبتنی بر `switch($action)` با هر action جدید تغییر می‌کنند |
| DRY | ✅ عمدتاً | `AuthenticatesRequests` trait تکرار گیت auth را حذف کرده |
| KISS | ✅ | Router عمداً ساده و بدون include پویا |

---

## Overall Score

| Category | Score |
| -------------------- | ----: |
| Security | 6/10 |
| Architecture | 4/10 |
| Kotlin | 8/10 |
| Jetpack Compose | 6/10 |
| Android | 7/10 |
| PHP Backend | 8/10 |
| API Design | 6/10 |
| Database | 7/10 |
| Performance | 6/10 |
| Memory Management | 7/10 |
| Error Handling | 6/10 |
| Testing | 3/10 |
| Code Quality | 5/10 |
| Maintainability | 6/10 |
| Scalability | 6/10 |
| Production Readiness | 5/10 |

**Overall: 6.1 / 10**

---

# Security Audit

## Critical Issues

### [CRITICAL] کلیدهای API و لایسنس با XOR تک‌بایتی محافظت شده و در git ذخیره‌اند

**File:** `app/src/main/cpp/secrets.cpp`

**Location:** خط ۶ (کلید)، خطوط ۱۶–۹۵ (داده‌ها)

**Problem:**

کل مکانیزم «مخفی‌سازی» رازها یک XOR تک‌بایتی با کلید ثابت `0x5A` است:

```cpp
const uint8_t XOR_KEY = 0x5A;                     // خط ۶

std::string decryptXor(const uint8_t* encryptedBytes, size_t length) {
    std::string decrypted;
    for (size_t i = 0; i < length; ++i) {
        decrypted += (char)(encryptedBytes[i] ^ XOR_KEY);   // XOR تک‌بایتی
    }
    return decrypted;
}
```

این فایل در git ردیابی می‌شود (`git ls-files` تأیید شد). طی این Audit، رازها **در کمتر از یک دقیقه و فقط با خواندن سورس** بازیابی شدند:

| تابع | مقدار بازیابی‌شده |
|------|-------------------|
| `getApiKey()` | `atk_nk_9290VV42-38XQ02DI-F2WY4L2K-EJA7V682` |
| `getLicenseKey()` | `13F71ADCB4585F1BE632FFB919F06691` |
| `getBaseUrl()` | `https://atk-nk.ir/Cargo/test_api/` |
| `getLicenseStatusPrefKey()` | `e1f2g3h4i5j6k7l8m9n0o1p2q3r4s5t6` |

**Why it matters:**

XOR تک‌بایتی رمزنگاری نیست — یک جایگزینی حرفی است. حتی بدون دسترسی به سورس، استخراج از فایل `.so` نهایی هم بی‌اهمیت است: کلید `0x5A` به‌صورت literal در باینری وجود دارد و الگوی XOR با یک frequency analysis ساده روی هر رشته‌ی ASCII شکسته می‌شود. قرار دادن رازها در NDK فقط **سطح دشواری را از «صفر» به «تقریباً صفر»** می‌برد.

مهم‌تر اینکه این مقادیر در **تاریخچه‌ی git** هستند. حتی با اصلاح فایل، مقادیر قدیمی تا زمانی که تاریخچه بازنویسی نشود قابل بازیابی‌اند.

**Impact:**

- `getApiKey()` گیت `check_update.php` را دور می‌زند (`UtilityController.php:199` با `hash_equals` بررسی می‌کند) — مهاجم می‌تواند مکانیزم به‌روزرسانی را جستجو و تحلیل کند.
- `getLicenseKey()` امکان فراخوانی `validate_license.php` و `get_license_info.php` را می‌دهد که **نام شرکت مشتری و وضعیت لایسنس** را برمی‌گردانند و یک `UPDATE` بدون احراز هویت روی جدول `licenses` می‌زنند.
- کل مدل «تأیید لایسنس» بی‌اثر می‌شود، چون کلیدی که قرار بود نصب مجاز را اثبات کند در دست هر کسی است که APK را دارد.

**Recommended Fix:**

۱. **فوراً هر دو کلید را چرخش دهید** — کلید فعلی را باید افشاشده فرض کرد.

۲. `API_KEY` را از کلاینت حذف کنید. یک راز مشترک که در هر نصب وجود دارد، راز نیست. `check_update.php` را به‌جای آن با گیت نشست معمولی (`X-Session-Token`) محافظت کنید، یا اگر باید بدون auth بماند، صرفاً به rate limiting اتکا کنید و تظاهر به امنیت نکنید.

۳. ~~برای `LICENSE_KEY`: اعتبارسنجی لایسنس را به یک challenge–response با امضای سمت سرور تبدیل کنید~~ — **اصلاحیه (۲۰۲۶-۰۸-۲۰):** طبق توضیح صریح کاربر، مدل لایسنس این پروژه عمداً per-customer و client-side است (هر مشتری یک `LICENSE_KEY` اختصاصی در زمان build داخل `secrets.cpp` embed می‌شود، سرور آن را در جدول `licenses` چک می‌کند). این پیشنهاد (امضای سمت سرور) فرض اشتباهی از این گزارش بود و **نباید اجرا شود** — جزئیات در بخش «endpointهای لایسنس بدون احراز هویت» (Phase2 #10).

۴. تاریخچه‌ی git را با `git filter-repo` پاک‌سازی کنید (پس از چرخش کلیدها، نه به‌جای آن).

**Priority:** CRITICAL
**Estimated Effort:** Medium (چرخش: Low · بازطراحی لایسنس: ~~Medium~~ حذف شد — مدل فعلی درست است)

**Status:** ⚠️ چرخش واقعی کلید هنوز روی سرور انجام نشده (دسترسی سرور در دسترس نبود). `scripts/xor_secret_codec.php` نوشته و با راستی‌آزمایی round-trip روی مقدار واقعی `getBaseUrl()` تست شد (decode مقدار موجود در `secrets.cpp:16-21` دقیقاً `https://atk-nk.ir/Cargo/test_api/` را برگرداند؛ encode همان رشته دقیقاً همان بایت‌های موجود در فایل را بازتولید کرد). راهنمای گام‌به‌گام در `scripts/ROTATE_SECRETS.md`. **چرخش واقعی کلید (تولید مقدار جدید + جایگزینی در سرور + جایگزینی در `secrets.cpp` + build/deploy هماهنگ) باقی مانده و باید توسط شما با دسترسی سرور انجام شود.**

بند ۴ (پاک‌سازی تاریخچه) با `git filter-repo` در Phase2 #17 (۲۰۲۶-۰۸-۲۰) انجام شد — جزئیات کامل در همان بخش. **این پاک‌سازی جایگزین چرخش کلید نیست**: نسخه‌ی فعلی `secrets.cpp` که دوباره در انتهای تاریخچه‌ی جدید commit شد، همچنان همان `LICENSE_KEY`/`API_KEY` چرخش‌نیافته را دارد — فقط ۳۲ نسخه‌ی قدیمی از تاریخچه پاک شدند.

---

## High Issues

### [HIGH] سرور production روی نسخه‌ی PHP بدون پشتیبانی امنیتی اجرا می‌شود

**File:** `php_server_info_20260819_104131.txt`

**Location:** خروجی `php -v`

**Problem:**

```
PHP 8.1.2-1ubuntu2.21 (cli) (built: Mar 24 2025 19:04:23) (NTS)
Server version: Apache/2.4.52 (Ubuntu)
```

PHP 8.1 در **۳۱ دسامبر ۲۰۲۵** به پایان پشتیبانی امنیتی رسید. در تاریخ این Audit (۱۹ اوت ۲۰۲۶)، این سرور حدود **۸ ماه** است که هیچ وصله‌ی امنیتی upstream دریافت نمی‌کند.

نکته‌ی ظریف: نسخه‌ی `8.1.2-1ubuntu2.21` یعنی Ubuntu همچنان backport محدود ارائه می‌دهد (تا EOL خود 22.04)، اما این پوشش کامل upstream نیست.

**Why it matters:** هر CVE منتشرشده در PHP 8.1 پس از دسامبر ۲۰۲۵ روی این سرور بدون وصله باقی می‌ماند و کد اپلیکیشن هیچ کنترلی روی آن ندارد.

**Impact:** بسته به CVE — از افشای اطلاعات تا RCE. این ریسکی است که کیفیت کد اپلیکیشن نمی‌تواند جبرانش کند.

**Recommended Fix:** ارتقا به PHP 8.3 یا 8.4. کد فعلی از `declare(strict_types=1)`، enum، و `str_starts_with` استفاده می‌کند — همگی با 8.3+ سازگارند. قبل از ارتقا `vendor/bin/phpstan analyse` را با `phpVersion` هدف اجرا کنید.

**Priority:** HIGH
**Estimated Effort:** Medium (نیازمند هماهنگی با میزبان)

---

### [HIGH] مسیر fallback احراز هویت، رمز عبور متن‌خام را می‌پذیرد

**File:** `PHP/src/Services/UserService.php`

**Location:** خطوط ۶۹–۷۶

**Problem:**

```php
// ===== حالت ۳: SHA-256 یا متن خام مستقیم در دیتابیس (سیستم بسیار قدیمی) =====
if (hash_equals($storedPassword, $password) || hash_equals($storedPassword, hash('sha256', $password))) {
    $upgradedHash = password_hash($password, PASSWORD_BCRYPT, ['cost' => 12]);
    $this->userRepository->updatePassword($user['id'], $upgradedHash);
    return $user;
}
```

`hash_equals($storedPassword, $password)` یعنی: اگر ستون `password` در دیتابیس **متن خام** باشد، مقایسه‌ی مستقیم انجام می‌شود و کاربر وارد می‌شود.

استراتژی مهاجرت خودکار (upgrade-on-login) طراحی درستی است، اما دو مشکل دارد:

۱. **بدون مهلت زمانی است.** تا وقتی حتی یک کاربر لاگین نکند، رمز او متن‌خام در دیتابیس باقی می‌ماند — نامحدود.
۲. حالت ۲ (خط ۶۱) `bcrypt(SHA-256(رمز))` را می‌پذیرد. یعنی اگر مهاجم SHA-256 رمز را از یک نشت قدیمی به‌دست آورد، در حالت ۳ خودِ آن مقدار هم مستقیماً به‌عنوان رمز پذیرفته می‌شود — یک pass-the-hash کلاسیک.

**Why it matters:** یک دامپ دیتابیس (از بکاپ، یا از phpMyAdmin — مورد بعدی را ببینید) رمزهای متن‌خام کاربران مهاجرت‌نکرده را فاش می‌کند. کاربران معمولاً رمز را در سرویس‌های دیگر تکرار می‌کنند، پس دامنه‌ی آسیب از این اپ فراتر می‌رود.

**Impact:** افشای اعتبارنامه در صورت هرگونه دسترسی خواندنی به دیتابیس.

**Recommended Fix:**

۱. یک کوئری شمارشی اجرا کنید تا ببینید چند رکورد هنوز مهاجرت نکرده‌اند:

```sql
SELECT COUNT(*) FROM Users WHERE password NOT LIKE '$2y$%' AND password NOT LIKE '$2a$%';
```

۲. اگر صفر است — **همین امروز حالت ۳ را حذف کنید**.

۳. اگر صفر نیست: یک مهلت اعلام کنید (مثلاً ۳۰ روز)، پس از آن حالت ۳ را حذف و آن حساب‌ها را به reset اجباری رمز هدایت کنید.

۴. حالت ۲ را نیز پس از اطمینان از آپدیت همه‌ی کلاینت‌ها حذف کنید. `min_allowed_version` در `update_config.php` ابزار لازم برای اجبار این کار را از قبل فراهم می‌کند.

**Priority:** HIGH
**Estimated Effort:** Low

**Status:** ⚠️ Mitigated (2026-08-19) — چون به دیتابیس production دسترسی نبود (نمی‌شد شمارش کاربران مهاجرت‌نشده را اجرا کرد)، حالت ۳ **حذف کامل نشد** بلکه پشت فلگ `ALLOW_LEGACY_PLAINTEXT_LOGIN` (پیش‌فرض `false`) قرار گرفت — یعنی از هم‌اکنون **غیرفعال** است مگر عمداً در `.env` روشن شود. با یک MariaDB throwaway محلی (سه کاربر: bcrypt، plaintext، SHA-256) تأیید شد:
- پیش‌فرض (فلگ خاموش): کاربر bcrypt طبیعی وارد می‌شود؛ کاربر plaintext و SHA-256 **رد** می‌شوند (رفتار قبلی که رمز خام را می‌پذیرفت، دیگر رخ نمی‌دهد).
- با فلگ روشن: کاربر plaintext وارد می‌شود و هش او بی‌صدا به bcrypt ارتقا می‌یابد (silent migration همچنان کار می‌کند).
- `vendor/bin/phpunit` (۶۸ تست) بدون شکست.

**اقدام باقی‌مانده برای شما:** روی دیتابیس production کوئری شمارشی گزارش را اجرا کنید؛ اگر صفر بود، بلوک حالت ۳ و متغیر `ALLOW_LEGACY_PLAINTEXT_LOGIN` را کامل از `UserService.php` حذف کنید (دیگر لازم نیست حتی پشت فلگ بماند).

---

### [HIGH] phpMyAdmin روی سرور production نصب است

**File:** `php_server_info_20260819_104131.txt`

**Location:** خروجی فهرست بسته‌ها

**Problem:**

```
ii  phpmyadmin  4:5.1.1+dfsg1-5ubuntu1  all  MySQL web administration tool
```

phpMyAdmin نسخه‌ی **5.1.1** روی همان میزبانی که API را سرو می‌کند نصب است. این نسخه از سال ۲۰۲۱ است.

**Why it matters:** phpMyAdmin یکی از پرهدف‌ترین اهداف اسکن خودکار در اینترنت است. یک رابط وب با دسترسی کامل به دیتابیس، در کنار API، سطح حمله‌ای ایجاد می‌کند که هیچ‌یک از کنترل‌های امنیتی خوب موجود در کد اپلیکیشن آن را پوشش نمی‌دهند — مهاجم اصلاً وارد مسیر Router یا `ApiAuthGate` نمی‌شود.

نکته: قوانین `.htaccess` پروژه فقط پوشه‌ی `PHP/` را محافظت می‌کنند و هیچ اثری روی `/phpmyadmin` ندارند.

**Impact:** در صورت اکسپلویت یا حدس اعتبارنامه — دسترسی کامل خواندن/نوشتن به کل دیتابیس، شامل جدول `Users` (با رمزهای متن‌خام مورد قبلی) و `user_sessions`.

**Recommended Fix (به ترتیب اولویت):**

۱. **حذف کامل** از سرور production: `apt purge phpmyadmin`. مدیریت دیتابیس را از طریق تونل SSH انجام دهید.
۲. اگر حذف ممکن نیست: محدود کردن به IP خاص در سطح Apache و افزودن HTTP Basic Auth مستقل.

**Priority:** HIGH
**Estimated Effort:** Low

**Status:** ⚠️ چون کاملاً سمت سرور است، فقط چک‌لیست اجرا آماده شد: `scripts/REMOVE_PHPMYADMIN.md` (هر دو گزینه‌ی حذف کامل و محدودسازی IP + Basic Auth، با دستورات راستی‌آزمایی `curl`). **اجرای واقعی روی سرور با شماست.**

---

### [HIGH] Composableها مستقیماً شبکه را صدا می‌زنند و عملیات نوشتن با ناوبری cancel می‌شود

**File:** `app/src/main/java/com/atk/atk_cargo/feature/cargo_entry/presentation/InitialInfoScreen.kt`

**Location:** خط ۱۱۵ (`val scope = rememberCoroutineScope()`) و خط ۶۳۶

**Problem:**

```kotlin
val scope = rememberCoroutineScope()          // خط ۱۱۵
...
scope.launch {                                 // خط ۶۲۰
    try {
        val initialInfo = InitialInfo(...)
        val response = RetrofitClient.apiServiceV2.saveInitialInfo(initialInfo)   // خط ۶۳۶
        if (response.isSuccessful) { ... }
    } catch (e: Exception) { ... }
}
```

`rememberCoroutineScope()` یک scope تولید می‌کند که **به عمر composition گره خورده است**. وقتی Composable از composition خارج شود (کاربر back بزند، ناوبری کند، یا صفحه recompose ساختاری شود)، این scope `cancel()` می‌شود.

اینجا در حال ارسال یک عملیات **نوشتن** (`saveInitialInfo`) هستیم.

**Why it matters:**

سناریوی شکست مشخص: کاربر روی «ثبت» می‌زند → درخواست POST ارسال می‌شود → کاربر بلافاصله back می‌زند (یا شبکه کند است و کاربر بی‌حوصله می‌شود) → composition از بین می‌رود → `scope` کنسل می‌شود → OkHttp call رها می‌شود.

نتیجه: **کاربر نمی‌داند اطلاعات ثبت شد یا نه.** اگر سرور درخواست را دریافت و پردازش کرده باشد اما پاسخ به کلاینت نرسد، رکورد در دیتابیس هست ولی UI هیچ بازخوردی نداده. در یک سیستم مدیریت بار کشتی که «کوتاژ» و «حواله» ثبت می‌کند، این یعنی داده‌ی نامتناقض.

**Impact:** از دست رفتن یا تکرار داده‌ی عملیاتی؛ عدم امکان بازیابی وضعیت پس از تغییر پیکربندی (چرخش صفحه)؛ غیرقابل تست بودن منطق تجاری.

**Recommended Fix:**

منطق را به یک ViewModel منتقل کنید تا در `viewModelScope` اجرا شود (که در برابر recomposition و تغییر پیکربندی مقاوم است):

```kotlin
// InitialInfoViewModel.kt
class InitialInfoViewModel(
    private val repository: InitialInfoRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(InitialInfoUiState())
    val uiState: StateFlow<InitialInfoUiState> = _uiState.asStateFlow()

    fun saveInitialInfo(info: InitialInfo) {
        viewModelScope.launch {                       // با ناوبری کنسل نمی‌شود
            _uiState.update { it.copy(isSubmitting = true) }
            val result = repository.saveInitialInfo(info)
            _uiState.update { it.copy(isSubmitting = false, result = result) }
        }
    }
}

// InitialInfoScreen.kt — Composable فقط رویداد را بالا می‌فرستد
onConfirm = { viewModel.saveInitialInfo(info) }
```

**فایل‌های نیازمند همین اصلاح:**

| فایل | خطوط |
|------|------|
| ✅ `feature/cargo_entry/presentation/InitialInfoScreen.kt` | ۵۴۳، ۶۳۶ |
| ✅ `feature/cargo_details/presentation/CargoDetailsScreen.kt` | ۱۱۸ |
| ✅ `feature/cargo_counter/presentation/CargoCounterScreen.kt` | ۱۴۶، ۲۸۲ |
| ✅ `feature/home/presentation/components/ProfileMenu.kt` | ۳۱۳ |
| ✅ `feature/home/presentation/ProfileSettingsDialogSection.kt` | ۳۳۲ |
| ✅ `feature/reports/presentation/dialogs/QuotaManagementDialog.kt` | ۱۰۷ |
| ✅ `feature/admin/.../UserManagementScreen.kt` | ۱۲۶، ۱۲۸، ۳۲۴، ۳۵۰، ۳۷۷، ۳۸۷ |
| ✅ `feature/admin/.../UserManagementDialogsSection.kt` | ۲۸۰ |

**Priority:** HIGH
**Estimated Effort:** High (۹ فایل، نیازمند ساخت ViewModel و Repository جدید)

**Status:** ✅ Done (2026-08-20) — فایل‌به‌فایل با تأیید جدا در ۸ کامیت انجام شد (Phase2 #11)، ۹ از ۹ فایل تمام. جزئیات هر فایل:

`InitialInfoScreen.kt` — هر دو تماس (`checkExistence` خط ۵۴۳ خواندنی، `saveInitialInfo` خط ۶۳۶ نوشتنی و پرریسک‌تر) به `InitialInfoViewModel` جدید (`viewModelScope`) منتقل شدند؛ نتیجه از طریق `Channel<InitialInfoEvent>` + `repeatOnLifecycle(STARTED)` (همان الگوی موجود در `StartupViewModel`/`MainActivity`) به UI برمی‌گردد. `rememberCoroutineScope()` و import مستقیم `RetrofitClient` کاملاً حذف شدند. یک نقطه‌ی فراخوانی مرده (`CargoEntryNavigation.kt::initialInfoScreen`) و یک نقطه‌ی فراخوانی واقعی (`MainScreen.kt:171`، جایی که route واقعاً ثبت می‌شود) هر دو با امضای جدید هماهنگ شدند. با `compileDebugKotlin`، `lintDebug`، و `testDebugUnitTest` تأیید شد.

**محدودیت تست:** رسیدن به این صفحه نیازمند لاگین با حساب واقعی روی بک‌اند production (`test_api`) است که به آن دسترسی نداشتم؛ پس فقط در سطح کد/کامپایل/لینت راستی‌آزمایی شد، **نه با کلیک واقعی در اپ روی امولاتور**. منطق سطربه‌سطر عیناً از نسخه‌ی قبلی کپی شد (فقط محل اجرا از `rememberCoroutineScope()` به `viewModelScope` منتقل شد) تا ریسک این محدودیت کم شود.

**۳ از ۹ فایل تمام شد:**

`CargoDetailsScreen.kt` — برخلاف فایل اول، این صفحه از قبل یک `CargoViewModel` بزرگ (همان God ViewModel که در بخش Code Quality هم به آن اشاره شده) داشت که `loadCargoInfoList`/`updateCargoConfirmation`/`showMessage` را از قبل با `viewModelScope` پیاده‌سازی کرده بود؛ فقط توابع مستقل `confirmCargo`/`handleCargoConfirmation` (تأیید حواله — یک عملیات نوشتن) بیرون از آن مانده و با `rememberCoroutineScope()` فراخوانی می‌شدند. به‌جای ساخت یک ViewModel جدید، منطق (عیناً، پیام به پیام) به‌عنوان متد `confirmCargo()` داخل همان `CargoViewModel` موجود منتقل شد — سازگارتر با معماری فعلی فایل نسبت به افزودن یک لایه‌ی موازی. `compileDebugKotlin`، `lintDebug`، `testDebugUnitTest` سبز؛ همان محدودیت عدم دسترسی به حساب واقعی برای تست کلیکی برقرار است.

`CargoCounterScreen.kt` — این فایل هم از قبل یک `CargoCounterViewModel` محلی داشت (فقط برای state انتخاب کشتی/تب، بدون تماس شبکه). هر دو تماس (`getActiveShips` خط ۱۴۶، `getRealTimeLoadingData` خط ۲۸۲) به‌عنوان متد داخل همان ViewModel منتقل شدند؛ چون `SnackbarHostState` یک نگرانی UI است (نه دامنه‌ی ViewModel)، متدهای جدید فقط با callback (`onSuccess`/`onError`/...) نتیجه را برمی‌گردانند و Composable همچنان خودش تصمیم می‌گیرد پیام را کجا نشان دهد (دقیقاً مثل قبل — بدون تغییر رفتار قابل‌مشاهده). چون این ViewModel حالا وابستگی (`ApiServiceV2`) دارد، از factory پیش‌فرض `viewModel()` به `koinViewModel()` تغییر کرد و در `AppModule.kt` ثبت شد.

**۵ از ۹ فایل تمام شد:**

`ProfileMenu.kt` + `ProfileSettingsDialogSection.kt` — این دو فایل با هم منتقل شدند چون تنگاتنگ به هم وابسته‌اند (`ProfileMenu` دیالوگ `ProfileSettingsDialog` را نمایش می‌دهد و `currentUser` بینشان مشترک است). یک `ProfileViewModel` جدید (`feature/home/presentation/ProfileViewModel.kt`) با دو متد ساخته شد: `loadSelfProfile()` (تماس خواندنی `getSelfProfile`، قبلاً داخل `LaunchedEffect(Unit)`) و `changePassword()` (تماس نوشتنی `updateUser` برای تغییر رمز عبور — پرریسک‌تر، قبلاً با `rememberCoroutineScope()` که با ناوبری کنسل می‌شد). هر دو با `viewModelScope` اجرا می‌شوند و نتیجه از طریق callback برمی‌گردد. یک نمونه‌ی `ProfileViewModel` در `ProfileMenu` با `koinViewModel()` ساخته و به `ProfileSettingsDialog` (که حالا پارامتر `viewModel` می‌گیرد) پاس داده می‌شود تا هر دو از یک instance استفاده کنند. `delay(600ms)` + Toast بعد از موفقیت (صرفاً UI، نه تماس شبکه) در Composable با همان `rememberCoroutineScope()` باقی ماند چون کنسل‌شدنش پس از موفقیت سرور بی‌ضرر است. در `AppModule.kt` با `viewModel { ProfileViewModel(get()) }` ثبت شد. `compileDebugKotlin`، `lintDebug`، `testDebugUnitTest` سبز؛ همان محدودیت عدم دسترسی به حساب واقعی برای تست کلیکی برقرار است.

**۶ از ۹ فایل تمام شد:**

`QuotaManagementDialog.kt` — برخلاف سه فایل قبلی، این Composable از قبل `viewModel: ReportsViewModel` را به‌عنوان پارامتر دریافت می‌کرد (فراخوان بالادستی آن را با `koinViewModel()` می‌سازد)، فقط تماس `getGroupedQuotas` (خط ۱۰۷، داخل `LaunchedEffect(currentShipName, refreshTrigger)`) مستقیماً `RetrofitClient.apiServiceV2` را صدا می‌زد. به‌جای ساخت ViewModel جدید، دو لایه اضافه شد: (۱) متد `ReportsRepository.getGroupedQuotas(shipName)` که پاسخ ناموفق را با همان پیام فارسی قبلی (`"خطا در دریافت داده‌ها: ${code}"`) به‌عنوان Exception پرتاب می‌کند، (۲) متد `ReportsViewModel.loadGroupedQuotas(shipName, onResult: (Result<...>) -> Unit)` با `viewModelScope`، دقیقاً هم‌الگو با `performAdvancedSearch`/`performAdvancedSearchByTracking` که از قبل در همین ViewModel وجود داشتند. state محلی دیالوگ (`quotaData`/`isLoading`/`errorMessage`/...) دست‌نخورده ماند — فقط منبع تماس شبکه از Composable به ViewModel منتقل شد؛ رفتار قابل‌مشاهده (پیام خطا، ترتیب loading) عیناً حفظ شد. `compileDebugKotlin`، `lintDebug`، `testDebugUnitTest` (۱۶ تست) سبز.

**۷ و ۸ از ۹ فایل تمام شد (آخرین دو فایل):**

`UserManagementScreen.kt` + `UserManagementDialogsSection.kt` — بزرگ‌ترین و پرریسک‌ترین جفت از ۹ فایل (۱٬۸۶۷ خط مجموعاً، ۶ تماس شبکه‌ی مختلف: `getAllUsersWithStatus`/`getAllUsers` fallback، `updateUser`، `deleteUser`، `getActiveDeviceId`+`forceLogoutUser`، و `createUser` در فایل دوم). برخلاف ۶ فایل قبلی، این دو در ماژول Gradle جدای `feature:admin` هستند نه در `app` — پس یک ViewModel جدید مستقیماً داخل همان ماژول ساخته شد: `feature/admin/.../UserManagementViewModel.kt` با پنج متد callback-محور (`fetchUsersWithStatus`، `updateUser`، `deleteUser`، `forceLogoutUser`، `createUser`) که هر کدام دقیقاً همان try/catch/finally و پیام‌های فارسی قبلی را با `viewModelScope` بازتولید می‌کنند. چون `feature:admin` قبلاً وابستگی `androidx.lifecycle.viewmodel.ktx` نداشت (تنها ماژولی از ۹ فایل که نداشت)، این وابستگی — که در کاتالوگ نسخه‌ها (`libs.versions.toml`) از قبل موجود بود و در ماژول‌های دیگر (`app`، `feature:auth`) استفاده می‌شد — به `feature/admin/build.gradle.kts` اضافه شد. یک نمونه‌ی `UserManagementViewModel` در `UserManagementDialog` (تابع اصلی) با `koinViewModel()` ساخته و به `EnhancedAddUserDialog` (که حالا پارامتر `viewModel` می‌گیرد) پاس داده می‌شود. در `AppModule.kt` با `viewModel { UserManagementViewModel(get()) }` ثبت شد (چون `app` از قبل به `feature:admin` وابسته است). `compileDebugKotlin`، `lintDebug`، `testDebugUnitTest` (۳۵ تست) سبز. **این آخرین فایل از ۹ فایل بود — Phase 2.11 (انتقال تماس‌های شبکه از Composable به ViewModel) اکنون ۹ از ۹ کامل است.**

---

### [HIGH] build release هیچ signingConfig ندارد

**File:** `app/build.gradle.kts`

**Location:** خطوط ۴۹–۶۴

**Problem:**

```kotlin
release {
    isMinifyEnabled = true
    isShrinkResources = true
    isDebuggable = false
    proguardFiles(...)
    ndk { abiFilters += listOf("arm64-v8a") }
    multiDexEnabled = true
    // ← هیچ signingConfig تعریف نشده
}
```

هیچ بلوک `signingConfigs { ... }` در کل فایل وجود ندارد. تنها ارجاع به امضا در variant `benchmark` است که عمداً از `signingConfigs.getByName("debug")` استفاده می‌کند.

**Why it matters:**

این اپ از طریق Play Store توزیع نمی‌شود — خودش را از `downloads/app-release.apk` به‌روز می‌کند و `SecurityVerifier` هش امضای APK را با `getExpectedSignatureHash()` مقایسه می‌کند. یعنی **امضای صحیح یک شرط عملکردی است، نه فقط یک تشریفات انتشار**. اگر یک build با کلید اشتباه (یا کلید debug) امضا شود، تمام نصب‌های موجود پس از به‌روزرسانی با خطای تأیید امنیتی مواجه می‌شوند.

وابسته بودن به پیکربندی امضا در IDE یعنی build قابل بازتولید نیست و در CI قابل ساخت نیست.

**Impact:** ریسک انتشار APK امضانشده یا با امضای اشتباه؛ عدم امکان خودکارسازی انتشار.

**Recommended Fix:**

```kotlin
// keystore.properties (خارج از git) یا متغیر محیطی CI
val keystoreProps = Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) load(f.inputStream())
}

android {
    signingConfigs {
        create("release") {
            storeFile = file(keystoreProps.getProperty("storeFile") ?: System.getenv("KEYSTORE_PATH"))
            storePassword = keystoreProps.getProperty("storePassword") ?: System.getenv("KEYSTORE_PASSWORD")
            keyAlias = keystoreProps.getProperty("keyAlias") ?: System.getenv("KEY_ALIAS")
            keyPassword = keystoreProps.getProperty("keyPassword") ?: System.getenv("KEY_PASSWORD")
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            // ...
        }
    }
}
```

`keystore.properties` را به `.gitignore` اضافه کنید.

**Priority:** HIGH
**Estimated Effort:** Low

**Status:** ✅ Fixed (2026-08-19) — `signingConfigs`/`keystore.properties` (یا متغیرهای محیطی `KEYSTORE_*`) به `app/build.gradle.kts` اضافه شد؛ اگر پیکربندی نباشد release بدون خطا و بدون امضا build می‌شود (برای لینت/CI فعلی کافی است). با یک کیستور throwaway محلی end-to-end تست شد:
- بدون `keystore.properties`: خروجی `app-release-unsigned.apk` (رفتار قبلی، بدون شکست).
- با `keystore.properties`: خروجی `app-release.apk` و `apksigner verify --print-certs` امضا را با گواهی throwaway تأیید کرد.
- `./gradlew :app:lintRelease` (همان مرحله‌ای که CI اجرا می‌کند) هم سبز بود.

`keystore.properties.example` به‌عنوان الگو اضافه و به `.gitignore` هم `keystore.properties` اضافه شد.

**بروزرسانی (فاز ۲):** وقتی کیستور واقعی production را در ریشه‌ی پروژه قرار دادید، یک باگ در پیاده‌سازی اولیه پیدا و رفع شد — مسیر نسبی `storeFile` نسبت به پوشه‌ی ماژول `app/` resolve می‌شد نه ریشه‌ی پروژه (جایی که `keystore.properties` واقعاً هست)، پس `file(...)` باید به `rootProject.file(...)` تغییر می‌کرد. با کیستور واقعی و `apksigner verify --print-certs` روی خروجی `assembleRelease` تأیید شد که امضا با گواهی واقعی production انجام می‌شود.

---

## Medium Issues

### [MEDIUM] endpointهای لایسنس بدون احراز هویت و بدون rate limit

**File:** `PHP/src/Controllers/LicenseController.php`

**Location:** خط ۲۹ (`validateLicense`)، خط ۱۲۰ (`getLicenseInfo`)

**Problem:**

هیچ‌کدام از این دو متد `requireAuthenticatedSession()` را صدا نمی‌زنند (تأیید شد — کلاس اصلاً trait مربوطه را use نمی‌کند). در `routes/api_v2.php` هم مسیر ندارند؛ فقط از طریق shimهای مستقیم `validate_license.php` و `get_license_info.php` در دسترس‌اند که **Router و گیت آن را کاملاً دور می‌زنند**.

دو مشکل مشخص:

۱. **کلید لایسنس در query string** (خط ۱۳۳):
```php
$licenseKey = trim((string)$this->request->get('licenseKey', ''));
```
`getLicenseInfo` یک `GET` است، پس کلید در URL می‌آید → در `access.log` آپاچی، تاریخچه‌ی proxy، و هر میان‌افزار شبکه ثبت می‌شود.

۲. **نوشتن در دیتابیس بدون احراز هویت** (خطوط ۷۲–۷۷):
```php
if ($updateLastCheck) {
    $updateStmt = $this->conn->prepare("UPDATE licenses SET last_check = CURRENT_TIMESTAMP WHERE license_key = ?");
    ...
}
```
هر کسی با کلید (که طبق مورد CRITICAL بازیابی‌شدنی است) می‌تواند نامحدود `UPDATE` بزند.

**Why it matters:** ترکیب کلید افشاشده + بدون rate limit + نوشتن در DB = یک بردار DoS ساده و امکان استخراج `company_name` مشتریان.

**Impact:** افشای اطلاعات مشتری؛ فشار نوشتن روی دیتابیس؛ ثبت کلید لایسنس در لاگ‌های سرور.

**Recommended Fix:**

۱. `getLicenseInfo` را از `GET` با query به `POST` با بدنه‌ی JSON تغییر دهید تا کلید در لاگ نیفتد.
۲. همان `LoginAttemptLimiter` که در `AuthController` استفاده می‌شود را روی این دو endpoint اعمال کنید (کلید: `'license_' . $ip`).
۳. `company_name` را از پاسخ حذف کنید مگر واقعاً در UI استفاده شود.

**Priority:** MEDIUM
**Estimated Effort:** Low

**Status:** ✅ Fixed (2026-08-20) — بند ۲ (rate limit) در Phase2.14 انجام شد: هر دو `validateLicense()` و `getLicenseInfo()` با `LoginAttemptLimiter` و کلید `license_<IP>` محافظت می‌شوند (سقف مؤثر ۵ درخواست/۱۵دقیقه)؛ با هارنس مستقل تأیید شد که این باکت از `crash_` و از شمارنده‌های واقعی لاگین ایزوله است.

بند ۱ (کلید در URL) با یک راه‌حل جایگزین بسته شد، نه دقیقاً پیشنهاد اولیه‌ی گزارش. **توضیح مهم از کاربر:** مدل لایسنس این پروژه per-customer است — هر مشتری یک `LICENSE_KEY` اختصاصی در زمان build داخل `secrets.cpp` embed می‌شود و در جدول `licenses` سرور چک می‌شود (در برابر `SignChecker` هم که ثابت است، برای همه‌ی مشتری‌ها یکسان). یعنی «بازطراحی لایسنس با امضای سمت سرور» (پیشنهاد اولیه‌ی Phase2 #10) فرض غلطی بود — این مدل عمداً کاملاً کلاینت‌محور طراحی شده و نباید به سمت سرور منتقل شود. تنها مشکل واقعی همان نشت کلید در query string بود، نه کل مدل auth. به‌جای تغییر `getLicenseInfo` از GET به POST (که شکل endpoint را عوض می‌کرد)، کلید از هدر سفارشی `X-License-Key` خوانده می‌شود (`LicenseController.php`) و کلاینت (`SecurityVerifier.kt::fetchLicenseInfo`) هم به همین ترتیب تغییر کرد — نتیجه یکسان (کلید دیگر در URL/لاگ سرور ثبت نمی‌شود) با تغییر کمتر. طبق تأیید صریح کاربر، چون سرور فعلی صرفاً محیط تست است، **بدون fallback به GET قدیمی** (سازگاری با نصب‌های موجود لازم نیست). بند ۳ (حذف `company_name` از پاسخ) هنوز انجام نشده — خارج از دامنه‌ی این تغییر بود. `php -l`، `phpstan` (سطح ۵)، `phpunit` (۶۸ تست)، `compileDebugKotlin` سبز.

---

### [MEDIUM] پیام خطای داخلی به کلاینت نشت می‌کند

**File:** `PHP/src/Controllers/UserController.php`

**Location:** خط ۸۳

**Problem:**

```php
} catch (\Exception $e) {
    error_log("Error in UserController: " . $e->getMessage());
    Response::json([
        'success' => false,
        'message' => 'خطایی در پردازش درخواست رخ داده است: ' . $e->getMessage()   // ← نشت
    ], 400);
}
```

`$e->getMessage()` مستقیماً به پاسخ HTTP اضافه می‌شود.

این با الگوی درست بقیه‌ی پروژه ناسازگار است. مقایسه کنید با `ChatController.php:103-110` که همین حالت را درست مدیریت می‌کند:

```php
} catch (\Throwable $e) {
    $this->logger->error('ChatController: ' . $e->getMessage());
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'خطای داخلی سرور رخ داده است.'], ...);
}
```

**Why it matters:** استثناهای PDO/mysqli معمولاً نام جدول، نام ستون و بخشی از کوئری را در پیام دارند. این اطلاعات به مهاجم در نگاشت schema کمک می‌کند.

**Impact:** افشای ساختار دیتابیس. (توجه: به‌دلیل نبود SQL Injection، این به‌تنهایی قابل زنجیر شدن به نفوذ نیست — لذا MEDIUM و نه HIGH.)

**Recommended Fix:** خط ۸۳ را به پیام عمومی تغییر دهید، دقیقاً مانند `ChatController`:

```php
'message' => 'خطایی در پردازش درخواست رخ داده است.'
```

**Priority:** MEDIUM
**Estimated Effort:** Low

**Status:** ✅ Fixed (2026-08-19) — خط ۸۳ به پیام عمومی تغییر کرد؛ `php -l` تأیید شد، هیچ تست/کلاینتی به متن قدیمی پیام وابسته نبود.

---

### [MEDIUM] دستورات `<Directory>` و `<DirectoryMatch>` در `.htaccess` نامعتبرند

**File:** `PHP/.htaccess`

**Location:** خطوط ۲۰–۲۶ (`<Directory "uploads">`)، ۷۰–۷۳ (`<Directory "config">`)، ۷۵–۷۸ (`<DirectoryMatch ...>`)

**Problem:**

```apache
<Directory "uploads">
    <FilesMatch "\.php$">
        Order allow,deny
        Deny from all
    </FilesMatch>
</Directory>
...
<DirectoryMatch "(^|/)(log|logs|backups|private|secret)/">
    Order allow,deny
    Deny from all
</DirectoryMatch>
```

`<Directory>` و `<DirectoryMatch>` **فقط در `httpd.conf` مجازند و در `.htaccess` معتبر نیستند**. آپاچی در برخورد با آن‌ها `500 Internal Server Error` برمی‌گرداند.

جالب اینکه فایل `PHP/logs/.htaccess` خودش این را به‌درستی تشخیص داده و در کامنتش نوشته: «`<DirectoryMatch>` در `.htaccess` ریشه‌ی PHP/ نامعتبر است» — اما دستور نامعتبر در فایل ریشه **حذف نشده**.

**Why it matters:**

دو حالت ممکن است، هر دو مشکل‌دار:

- اگر `AllowOverride All` فعال باشد → سایت باید ۵۰۰ بدهد. چون ظاهراً کار می‌کند، احتمالاً حالت دوم برقرار است.
- اگر `AllowOverride` محدود باشد → **کل `.htaccess` نادیده گرفته می‌شود**، یعنی هیچ‌کدام از محافظت‌ها (`Options -Indexes`، مسدودسازی `config.php`، rewrite برای `api/v2/`) اعمال نمی‌شوند.

حالت دوم جدی است، اما چون rewrite مربوط به `api/v2/` هم در همان فایل است و API ظاهراً کار می‌کند، احتمالاً `AllowOverride` فعال است و آپاچی این دستورات را به‌شکلی تحمل می‌کند.

**این یک `Potential Issue` است** — برای قطعیت نیاز به بررسی روی سرور دارد.

**برای تأیید لازم است:**

```bash
apachectl -t                                    # بررسی syntax
curl -sI https://atk-nk.ir/Cargo/test_api/config/config.php   # باید 403 باشد
curl -sI https://atk-nk.ir/Cargo/test_api/logs/session_activity.log  # باید 403 باشد
```

**Recommended Fix:** دستورات `<Directory>` و `<DirectoryMatch>` را از `.htaccess` حذف و با فایل `.htaccess` داخل خود آن پوشه‌ها جایگزین کنید — دقیقاً همان الگویی که `PHP/logs/.htaccess` و `PHP/log/.htaccess` از قبل درست پیاده کرده‌اند.

**Priority:** MEDIUM
**Estimated Effort:** Low

---

### [MEDIUM] فیلتر User-Agent، ابزارهای مانیتورینگ را مسدود می‌کند

**File:** `PHP/config/.htaccess`

**Location:** انتهای فایل

**Problem:**

```apache
BrowserMatchNoCase "curl" bots
BrowserMatchNoCase "wget" bots
Order Allow,Deny
Allow from all
Deny from env=bots
```

`curl` و `wget` مسدود شده‌اند.

**Why it matters:** این کار امنیت اضافه نمی‌کند — تغییر User-Agent یک فلگ `-A` است. اما **health check ها و اسکریپت‌های مانیتورینگ را می‌شکند**. پروژه یک endpoint سلامت دارد (`DiagnosticsController::health`) که عمداً بدون auth طراحی شده تا «برای ابزار مانیتورینگ خارجی در دسترس باشد» — و بیشتر آن ابزارها با curl کار می‌کنند.

**Impact:** ایجاد اصطکاک عملیاتی بدون سود امنیتی؛ حس کاذب امنیت.

**Recommended Fix:** این بلوک را حذف کنید. برای محافظت واقعی در برابر bot از rate limiting (`mod_ratelimit` یا fail2ban) استفاده کنید.

**Priority:** MEDIUM
**Estimated Effort:** Low

---

### [MEDIUM] گزارش کرش بدون احراز هویت و بدون محدودیت نرخ

**File:** `PHP/src/Controllers/DiagnosticsController.php`

**Location:** خطوط ۷۰–۱۱۲

**Problem:**

`reportCrash` عمداً بدون auth است (دلیل معتبر: کرش ممکن است قبل از لاگین رخ دهد). محدودیت طول ۸۰۰۰ کاراکتر اعمال شده (خط ۹۲) که خوب است. اما **هیچ محدودیت نرخی وجود ندارد**:

```php
file_put_contents(
    $logDir . '/crash_reports.log',
    json_encode($entry, JSON_UNESCAPED_UNICODE) . "\n",
    FILE_APPEND | LOCK_EX
);
```

**Why it matters:** یک مهاجم می‌تواند در یک حلقه‌ی ساده هر بار ~۸KB بنویسد. با ۱۰۰۰ درخواست در دقیقه، حدود ۸MB در دقیقه ≈ ۴۸۰MB در ساعت. پر شدن دیسک باعث از کار افتادن کل سرویس (شامل MySQL) می‌شود.

پروژه `scripts/rotate_logs.php` و `deploy/logrotate.d` دارد که آسیب را محدود می‌کند، اما logrotate معمولاً روزانه اجرا می‌شود — بسیار کندتر از سرعت حمله.

**Impact:** DoS از طریق پر کردن دیسک.

**Recommended Fix:**

۱. rate limit مبتنی بر IP (همان `LoginAttemptLimiter` قابل استفاده است — مثلاً حداکثر ۱۰ گزارش در ساعت به‌ازای هر IP).
۲. سقف اندازه‌ی فایل: قبل از نوشتن، اگر `filesize($logFile) > 50 * 1024 * 1024` بود، نوشتن را رد کنید.

**Priority:** MEDIUM
**Estimated Effort:** Low

**Status:** ✅ Fixed (2026-08-19) — همان `LoginAttemptLimiter` موجود با کلید مجزا (`crash_<IP>`) استفاده شد؛ سقف مؤثر آن ۵ درخواست در ۱۵ دقیقه به‌ازای IP است (به‌جای ۱۰/ساعت پیشنهادی، اما از همان مکانیزم موجود بدون کد جدید). سقف حجم فایل (۵۰MB) هم اضافه شد. با یک هارنس PHP مستقل (بدون APCu، مسیر fallback فایلی) تأیید شد که کلید `crash_` بعد از ۵ تلاش قفل می‌شود و کاملاً از شمارنده‌های واقعی لاگین (حتی با همان IP) و از کلید `license_` (مورد بعدی) ایزوله است. `vendor/bin/phpunit` (۶۸ تست) و `vendor/bin/phpstan analyse` سبز.

---

### [MEDIUM] آدرس دانلود به‌روزرسانی اعتبارسنجی دامنه نمی‌شود

**File:** `app/src/main/java/com/atk/atk_cargo/api/UpdateManager.kt`

**Location:** خطوط ۱۷۱–۱۷۳ (دریافت URL)، ۴۱۵ (استفاده)

**Problem:**

```kotlin
val downloadUrl = jsonResponse.optString("download_url", "").ifEmpty {
    jsonResponse.optString("downloadUrl", "")
}
```

این URL بدون هیچ بررسی‌ای برای دانلود APK استفاده می‌شود.

**نکته‌ی منصفانه:** تأیید SHA-256 به‌درستی و **fail-closed** پیاده شده (خطوط ۳۳۳–۳۴۶) — اگر هش نباشد، به‌روزرسانی لغو می‌شود. این طراحی درستی است.

اما هم `download_url` و هم `sha256` از **یک پاسخ واحد** می‌آیند. مهاجمی که بتواند آن پاسخ را کنترل کند، هر دو را کنترل می‌کند و بررسی هش بی‌اثر می‌شود.

**Why it matters:** Certificate pinning (در `network_security_config.xml`) این را در برابر MITM شبکه‌ای محافظت می‌کند — پس این یک آسیب‌پذیری قابل اکسپلویت از راه دور نیست. اما در برابر **نفوذ به خود سرور** هیچ لایه‌ی دفاعی وجود ندارد: مهاجم با دسترسی به سرور می‌تواند APK دلخواه توزیع کند. با توجه به اینکه اپ مجوز `REQUEST_INSTALL_PACKAGES` دارد، این مسیر مستقیم به اجرای کد روی همه‌ی دستگاه‌هاست.

**Impact:** تشدید‌کننده‌ی نفوذ به سرور — تبدیل «نفوذ به سرور» به «نفوذ به تمام دستگاه‌های کاربران».

**Recommended Fix:**

allow-list دامنه اضافه کنید:

```kotlin
private fun isTrustedDownloadUrl(url: String): Boolean = runCatching {
    val u = java.net.URI(url)
    u.scheme == "https" && (u.host == "atk-nk.ir" || u.host.endsWith(".atk-nk.ir"))
}.getOrDefault(false)

// پیش از شروع دانلود:
if (!isTrustedDownloadUrl(downloadUrl)) {
    _downloadState.value = DownloadState.Error("آدرس به‌روزرسانی نامعتبر است.")
    return@withContext
}
```

به‌عنوان لایه‌ی بعدی، امضای APK دانلودشده را قبل از نصب با `PackageManager.getPackageArchiveInfo()` بررسی و با `Secrets.getExpectedSignatureHash()` مقایسه کنید.

**Priority:** MEDIUM
**Estimated Effort:** Low

**Status:** ✅ Fixed (2026-08-19) — `isTrustedDownloadUrl()` اضافه شد (در ابتدای `startDownload()` که هم مسیر دانلود اولیه و هم `resumeDownload()` از آن عبور می‌کنند). به‌جای hardcode کردن `atk-nk.ir`، میزبان از `Constants.BASE_URL` (همان `Secrets.getBaseUrl()`) خوانده می‌شود تا اگر دامنه‌ی سرور تغییر کرد دو جا از هم واگرا نشوند. با یک هارنس Java مستقل (همان الگوریتم `java.net.URI`) روی ۸ سناریو تست شد: میزبان معتبر، ساب‌دامین معتبر، میزبان مهاجم، ترفند suffix (`notatk-nk.ir`)، `http` به‌جای `https`، تعبیه‌ی دامنه در path، URL بدفرمت، و ترفند userinfo (`https://atk-nk.ir@evil.com/...`) — همه‌ی ۸ مورد مطابق انتظار (fail-closed برای همه‌ی تلاش‌های spoof). `compileDebugKotlin` و `lintDebug` هم سبز.

---

### [MEDIUM] `Log.w` و `Log.e` در build release حذف نمی‌شوند

**File:** `app/proguard-rules.pro`

**Location:** خطوط ۱۷۹–۱۸۴

**Problem:**

```proguard
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
```

فقط `v`، `d`، `i` حذف می‌شوند. در پروژه **۱۹ مورد `Log.w` و ۵۴ مورد `Log.e`** وجود دارد که همگی در release باقی می‌مانند.

**Why it matters:**

بررسی شد و هیچ‌کدام مستقیماً توکن یا رمز لاگ نمی‌کنند (این خوب است). اما بسیاری شیء استثنا را پاس می‌دهند:

```kotlin
Log.e("SecurityVerifier", "Error in server signature authentication: ${e.message}", e)  // SecurityVerifier.kt:198
Log.w("TokenRefresher", "خطا هنگام تمدید access token", e)                              // TokenRefresher.kt:86
```

استثناهای Retrofit/OkHttp می‌توانند URL کامل (شامل query string) و بخشی از بدنه‌ی پاسخ را حمل کنند. روی دستگاهی که ADB روی آن فعال است، این‌ها با logcat قابل خواندن‌اند.

**Impact:** افشای اطلاعات از طریق logcat روی دستگاه‌های در دسترس.

**Recommended Fix:**

`w` را هم حذف کنید و برای `e` از یک لایه‌ی گزارش‌دهی استفاده کنید که در release فقط به `CrashReporter` می‌فرستد (که پروژه از قبل دارد) نه به logcat:

```proguard
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
}
```

**Priority:** MEDIUM
**Estimated Effort:** Low

**Status:** ✅ Fixed (2026-08-19) — `w(...)` به `-assumenosideeffects` اضافه شد. با `assembleRelease` واقعی (کیستور production واقعی، نه throwaway) و بررسی `classes.dex` با `dexdump` تأیید شد: تقریباً همه‌ی فراخوانی‌های `Log.w`/`Log.d` حذف شدند و فقط **یک مورد از هرکدام** باقی ماند — نه به‌خاطر نقص proguard، بلکه یک اثر جانبی Kotlin: در [ReportsRepository.kt:456](app/src/main/java/com/atk/atk_cargo/data/repository/ReportsRepository.kt:456) الگوی `} ?: Log.w(...)` دو شاخه‌ی نوع ناسازگار (`Boolean` از `.add()` در برابر `Int` از `Log.w`) به کامپایلر می‌دهد که مجبور به autobox کردن نتیجه می‌شود؛ چون R8 نتیجه را «استفاده‌شده» می‌بیند نمی‌تواند فراخوانی را حذف کند. رفع کامل آن نیازمند بازنویسی آن خط است — خارج از scope همین آیتم (که فقط `proguard-rules.pro` بود).

---

## Low Issues

### [LOW] تابع native تعریف‌شده اما هرگز استفاده نشده

**File:** `core/network/src/main/java/com/atk/atk_cargo/api/Secrets.kt` (خط ۱۱) و `app/src/main/cpp/secrets.cpp` (تابع `n2`)

**Problem:** `getLicenseStatusPrefKey()` اعلام شده و در `secrets.cpp` پیاده‌سازی شده اما هیچ فراخوانی‌ای در کل کدبیس ندارد (تأیید با grep روی همه‌ی `.kt`).

**Impact:** کد مرده که یک رشته‌ی شبه‌راز (`e1f2g3h4i5j6k7l8m9n0o1p2q3r4s5t6`) را در باینری نگه می‌دارد.

**Recommended Fix:** حذف از هر دو فایل و از آرایه‌ی `kSecretsMethods`.

**Priority:** LOW · **Effort:** Low

---

### [LOW] فایل اطلاعات سرور در `.gitignore` نیست

**File:** `php_server_info_20260819_104131.txt` (ریشه پروژه)

**Problem:** این فایل untracked است اما `git check-ignore` تأیید کرد که ignore **نشده** — یک `git add .` آن را commit می‌کند. محتوای آن شامل نسخه‌های دقیق نرم‌افزار سرور، فهرست بسته‌ها، پیکربندی PHP و برنامه‌ی cron بکاپ است.

**Impact:** اگر repository عمومی شود، یک نقشه‌ی کامل شناسایی (reconnaissance) در اختیار مهاجم قرار می‌گیرد.

**Recommended Fix:** افزودن `php_server_info_*.txt` به `.gitignore`.

**Priority:** LOW · **Effort:** Low

**Status:** ✅ Fixed (2026-08-19, commit `6794d9c`) — پرونده یافت شد که علاوه بر untracked نبودن، **در واقع در آخرین کامیت (`f4792bb`) commit و روی `origin/main` push هم شده بود** (فرض اولیه‌ی گزارش نادرست بود). با `git rm --cached` از ردیابی خارج و الگو به `.gitignore` اضافه شد. تاریخچه‌ی git هنوز حاوی نسخه‌ی قدیمی فایل بود — با پاک‌سازی کامل تاریخچه در Phase 2 #17 (۲۰۲۶-۰۸-۲۰) این فایل هم به‌طور کامل حذف شد؛ `git fsck --full --unreachable` صفر ارجاع باقی‌مانده نشان داد.

---

### [LOW] استفاده از `!!` روی state در Composable

**File:** `app/src/main/java/com/atk/atk_cargo/ui/screens/ManageReportsScreen.kt`

**Location:** خطوط ۳۹۰، ۴۰۴

```kotlin
if (errorMessage != null) {
    AlertDialog(
        text = { Text(errorMessage!!) },     // خواندن با تأخیر داخل lambda
```

**Problem:** `errorMessage` بین بررسی `!= null` و اجرای lambda ممکن است تغییر کند.

**ارزیابی صادقانه:** در عمل، بلوک `if` و lambda در یک recompose scope هستند و سیستم snapshot کامپوز ابتدا `if` را ارزیابی می‌کند، پس کرش بسیار بعید است. این را به‌عنوان **مسئله‌ی سبک کد** گزارش می‌کنم نه باگ قطعی.

**Recommended Fix:**

```kotlin
errorMessage?.let { msg ->
    AlertDialog(
        onDismissRequest = { errorMessage = null },
        text = { Text(msg) },   // capture محلی — امن
        ...
    )
}
```

**Priority:** LOW · **Effort:** Low

---

## مواردی که بررسی شدند و مشکلی نداشتند

برای شفافیت، این موارد به‌طور فعال بررسی و **رد** شدند (تا در بازبینی‌های بعدی دوباره وقت صرف آن‌ها نشود):

| مورد بررسی‌شده | نتیجه |
|----------------|-------|
| SQL Injection در تمام لایه‌ها | ❌ یافت نشد — همه prepared statement |
| `AnalyticsController.php:406` (`$workdayBoundary` در SQL) | ✅ امن — ثابت کلاس است نه ورودی کاربر |
| `UserRepository.php:117` (ستون پویا) | ✅ امن — `UPDATABLE_COLUMNS` allow-list |
| `QuotaService.php:450` (`implode` در WHERE) | ✅ امن — قطعات ثابت + bind_param |
| `export_schema.php` (استخراج schema) | ✅ امن — گیت `php_sapi_name() === 'cli'` |
| `OnlineUsersController` (بدون trait auth) | ✅ امن — گیت `$_SESSION['perm_manager_auth']` |
| `file_manager.php` | ✅ امن — همان گیت نشست |
| کلیدهای `LazyColumn` | ✅ فقط ۲ مورد بدون key، هر دو `items(count)` که نیازی ندارند |
| `collectAsState()` بدون lifecycle | ✅ صفر مورد — همه ۱۰۲ مورد `collectAsStateWithLifecycle` |
| `GlobalScope` | ✅ صفر مورد |
| `runBlocking` در `TokenAuthenticator.kt:43` | ✅ صحیح — `Authenticator` یک API مسدودکننده است |
| مهاجرت Room 1→2 (کرش؟) | ✅ کرش نمی‌کند — `fallbackToDestructiveMigration(false)` فعال است؛ فقط کش چت پاک می‌شود |
| `response.body()!!` در `CargoViewModel` | ✅ امن — با `body()?.success == true` گارد شده |
| حلقه‌های polling | ✅ همه در `repeatOnLifecycle(RESUMED)` |
| `allowBackup` / قوانین backup | ✅ `false` + exclude کامل |
| exported components | ✅ فقط `MainActivity` (launcher) و `BootReceiver` (با permission) |
| WebView | ✅ در پروژه وجود ندارد |
| File upload | ✅ در پروژه وجود ندارد |

---

# Android Audit

## نقاط قوت

**AndroidManifest.xml** — تمیزتر از اکثر پروژه‌های production:

```xml
android:allowBackup="false"
android:networkSecurityConfig="@xml/network_security_config"
android:dataExtractionRules="@xml/data_extraction_rules"
```

- تنها کامپوننت exported واقعی `MainActivity` است (launcher، بدون deep link → بدون سطح حمله‌ی intent).
- `BootReceiver` با `android:permission="android.permission.RECEIVE_BOOT_COMPLETED"` محافظت شده.
- `FileProvider` با `exported="false"` و مسیرهای **محدودشده** در `file_path.xml` — فقط `updates/` و `Documents/`، نه `path="."`.

**network_security_config.xml** — سطح حرفه‌ای:
- `cleartextTrafficPermitted="false"` در base-config و domain-config
- Certificate pinning روی CA میانی *و* ریشه (پین پشتیبان)
- `expiration="2028-08-17"` به‌عنوان سوپاپ اطمینان
- استدلال انتخاب پین روی CA به‌جای leaf در کامنت مستند شده و **درست** است

## مشکلات

### [MEDIUM] `targetSdk = 34` در حالی که `compileSdk = 36`

**File:** `app/build.gradle.kts` خطوط ۱۳، ۱۸

```kotlin
compileSdk = 36
targetSdk = 34
```

**Problem:** فاصله‌ی دو نسخه‌ای. اپ رفتارهای سازگاری Android 15/16 را دریافت نمی‌کند.

**نکته‌ی زمینه‌ای:** چون این اپ از Play Store توزیع نمی‌شود، الزام `targetSdk` فروشگاه اعمال نمی‌شود. پس این یک مسدودکننده نیست — اما اپ از بهبودهای امنیتی و عملکردی نسخه‌های جدید محروم می‌ماند و بدهی فنی انباشته می‌شود.

**Recommended Fix:** ارتقای تدریجی به ۳۵ سپس ۳۶، با تست ویژه روی foreground service (`dataSync`) و `REQUEST_INSTALL_PACKAGES` که هر دو در نسخه‌های اخیر محدودتر شده‌اند.

**Priority:** MEDIUM · **Effort:** Medium

---

### [LOW] پرچم منسوخ در `gradle.properties`

**File:** `gradle.properties`

```properties
android.enableResourceOptimizations=true
```

این پرچم در AGP 8 حذف شده و اثری ندارد.

**Recommended Fix:** حذف شود. `isShrinkResources = true` در `build.gradle.kts` کار لازم را انجام می‌دهد.

**Priority:** LOW · **Effort:** Low

---

# Kotlin Audit

## ارزیابی: 8/10 — تمیزترین بخش پروژه

| معیار | تعداد | ارزیابی |
|-------|-------|---------|
| `!!` | ۹ | عالی برای ۵۲ هزار خط |
| `GlobalScope` | ۰ | عالی |
| `runBlocking` | ۱ (موجه) | عالی |
| `lateinit` | ۳ | عالی |
| `println` | ۰ | عالی |
| `collectAsStateWithLifecycle` | ۱۰۲ | عالی |

**Structured Concurrency** به‌درستی رعایت شده. `AuthSession` از `@Volatile` روی هر فیلد استفاده می‌کند که برای الگوی خواندن/نوشتن ساده‌ی آن کافی است.

## مشکلات

### [MEDIUM] UseCaseها به‌جای تزریق وابستگی، singleton را مستقیم می‌گیرند

**Files:**
- `app/src/main/java/com/atk/atk_cargo/feature/cargo/domain/usecase/CheckQuotaUseCase.kt:8`
- `app/src/main/java/com/atk/atk_cargo/feature/cargo/domain/usecase/SubmitCargoUseCase.kt:10`
- `app/src/main/java/com/atk/atk_cargo/ui/viewmodel/CargoViewModel.kt:90`

**Problem:**

```kotlin
// CheckQuotaUseCase.kt:8
private val apiServiceV2: ApiServiceV2 = com.atk.atk_cargo.api.RetrofitClient.apiServiceV2
```

```kotlin
// CargoViewModel.kt:90
private val quotaValidationUseCase =
    com.atk.atk_cargo.feature.cargo.domain.QuotaValidationUseCase(repository)
```

پروژه از Koin استفاده می‌کند (`di/AppModule.kt`) اما این کلاس‌ها وابستگی‌شان را مستقیماً از یک `object` سراسری برمی‌دارند.

**Why it matters:**

۱. **غیرقابل تست.** نمی‌توان `ApiServiceV2` را mock کرد چون از یک `object` singleton خوانده می‌شود. این دقیقاً توضیح می‌دهد چرا برای این کلاس‌ها هیچ تستی وجود ندارد.
۲. استفاده از نام کاملاً واجد شرایط (fully-qualified) درون بدنه‌ی کلاس، نشانه‌ی رفع سریع وابستگی حلقوی است نه یک انتخاب طراحی.

**Recommended Fix:**

```kotlin
class CheckQuotaUseCase(
    private val apiServiceV2: ApiServiceV2   // تزریق‌شده
) { ... }

// AppModule.kt
factory { CheckQuotaUseCase(get()) }
factory { SubmitCargoUseCase(get()) }
factory { QuotaValidationUseCase(get()) }

viewModel { CargoViewModel(get(), get(), get()) }   // UseCase تزریق می‌شود
```

**Priority:** MEDIUM · **Effort:** Medium

**Status:** ✅ Fixed (2026-08-20) — بررسی call-siteها قبل از اعمال fix پیشنهادی گزارش (که هر ۳ فایل را «فعال، فقط بدون DI» فرض کرده بود) نشان داد **دو فایل از سه فایل کد مرده بودند**: `CheckQuotaUseCase.kt` و `SubmitCargoUseCase.kt` هیچ‌جا instantiate نمی‌شدند — `CargoViewModel.kt` منطق معادل (`checkQuotaExistenceCargo`، `saveOrUpdateCargoInfo`) را مستقیم و تکراری با `apiServiceV2` انجام می‌داد. با تأیید کاربر، طبق دستورالعمل «قبل از حذف، همه‌ی call siteها را چک کن»، هر دو فایل حذف شدند به‌جای افزودن DI به چیزی که هرگز استفاده نمی‌شود.

`QuotaValidationUseCase.kt` (تنها فایل واقعاً فعال) اصلاح شد: خط ۳۷ که مستقیماً `apiServiceV2.getShipQuotas(...)` (singleton سراسری) را صدا می‌زد، به `repository.getShipQuotas(shipName)` (همان `ReportsRepository` که از قبل تزریق‌شده بود) تغییر کرد — رفتار fallback قبلی (لاگ خطا و ادامه‌ی اعتبارسنجی به‌جای شکست کامل) با یک try/catch محلی حفظ شد تا معنای قبلی عوض نشود. نمونه‌سازی این کلاس در `CargoViewModel.kt:90` هم از فراخوانی fully-qualified (`com.atk.atk_cargo.feature.cargo.domain.QuotaValidationUseCase(...)`) به import تمیز تغییر کرد. آن را از طریق Koin ثبت نکردیم — چون تنها وابستگی باقی‌مانده‌اش (`repository`) از قبل تزریق‌شده بود، عبور آن از یک لایه‌ی اضافه‌ی Koin تستی‌بودن بیشتری اضافه نمی‌کرد. `compileDebugKotlin`، `lintDebug`، `testDebugUnitTest` سبز.

---

# Jetpack Compose Audit

## نقاط قوت (واقعاً چشمگیر)

- **۱۰۲ مورد `collectAsStateWithLifecycle`، صفر مورد `collectAsState()`** — این یعنی هیچ Flow ای در پس‌زمینه بی‌جهت جمع‌آوری نمی‌شود.
- **کلیدهای `LazyColumn`** با دقت انتخاب شده‌اند. نمونه‌ی خوب از `ShipsListScreen.kt:277`:
  ```kotlin
  key = { ship -> "${ship.name}|${ship.cargoType.orEmpty()}" },
  contentType = { "ship" }
  ```
  حتی `contentType` هم برای بازیافت بهتر آیتم‌ها تنظیم شده — این سطح از توجه نادر است.
- استفاده از `repeatOnLifecycle(Lifecycle.State.RESUMED)` برای همه‌ی حلقه‌های polling.

## مشکلات

### [HIGH] انیمیشن‌ها باعث recomposition در هر فریم می‌شوند

**Files & Locations:**

| فایل | خطوط |
|------|------|
| `feature/cargo_entry/presentation/InitialInfoDialogs.kt` | ۸۱–۸۲، ۱۷۳–۱۷۴، ۲۴۷–۲۴۸، ۳۶۸–۳۶۹ |
| `core/ui/components/ConfirmationDialog.kt` | ۶۳–۶۴ |
| `core/ui/components/SnackbarMessage.kt` | ۹۷–۹۸ |
| `feature/cargo_counter/presentation/components/CargoCounterComponents.kt` | ۱۲۹–۱۳۰ |
| `feature/cargo_entry/presentation/components/SelectInfoSnackbar.kt` | ۱۱۶–۱۱۷ |

**Problem:**

```kotlin
// InitialInfoDialogs.kt:81-82
Modifier
    .scale(animateFloatAsState(if (isVisible) 1f else 0.9f, label = "").value)
    .alpha(animateFloatAsState(if (isVisible) 1f else 0f, label = "").value),
```

مقدار انیمیشن **مستقیماً در فاز composition خوانده می‌شود** (`.value`).

**Why it matters:**

این الگوی کلاسیک ضدالگوی Compose است. وقتی یک مقدار انیمیشن‌شونده در composition خوانده شود، هر تغییر آن — یعنی **در هر فریم، ۶۰ تا ۱۲۰ بار در ثانیه** — کل Composable را invalidate می‌کند. نتیجه: اجرای مجدد composition + measure + layout + draw برای هر فریم.

در حالی که با `graphicsLayer` و lambda، خواندن مقدار به فاز draw موکول می‌شود و **فقط redraw** اتفاق می‌افتد — composition و layout کاملاً skip می‌شوند.

روی دیالوگ‌ها که درخت UI نسبتاً بزرگی دارند، این تفاوت بین یک انیمیشن روان و jank محسوس است — به‌ویژه روی دستگاه‌های ضعیف که در محیط عملیاتی بندر رایج‌اند.

**Impact:** jank در باز/بسته شدن دیالوگ‌ها؛ مصرف CPU و باتری بیشتر.

**Recommended Fix:**

```kotlin
val scale by animateFloatAsState(
    targetValue = if (isVisible) 1f else 0.9f,
    label = "dialog_scale"
)
val alpha by animateFloatAsState(
    targetValue = if (isVisible) 1f else 0f,
    label = "dialog_alpha"
)

Modifier.graphicsLayer {          // lambda ← خواندن با تأخیر، فقط فاز draw
    scaleX = scale
    scaleY = scale
    this.alpha = alpha
}
```

ضمناً `label = ""` را با برچسب معنادار جایگزین کنید — برچسب خالی، ابزار Animation Preview در Android Studio را بی‌فایده می‌کند.

**Priority:** HIGH · **Effort:** Low (تغییر مکانیکی در ۵ فایل)

**Status:** ✅ Fixed (2026-08-19) — هر ۵ فایل به `graphicsLayer` منتقل شدند:
- `InitialInfoDialogs.kt` (۴ دیالوگ) و `ConfirmationDialog.kt` (core/ui): `.scale()/.alpha()` → `graphicsLayer { scaleX/scaleY/alpha }`؛ در `InitialInfoDialogs.kt` طبق توصیه‌ی گزارش `label = ""` هم به `"dialog_scale"`/`"dialog_alpha"` تغییر کرد.
- `SnackbarMessage.kt`، `CargoCounterComponents.kt`، `SelectInfoSnackbar.kt`: این سه فایل علاوه بر `.alpha()` یک `.offset(y = translateY)` هم داشتند (همان اثر روی composition/layout) که به `graphicsLayer { translationY = translateY.toPx() }` تبدیل شد — `GraphicsLayerScope` خودش `Density` است، پس تبدیل Dp→px بدون نیاز به `LocalDensity` انجام می‌شود.

import‌های بلااستفاده‌ی `androidx.compose.ui.draw.alpha/scale/offset` حذف و `graphicsLayer` (از `androidx.compose.ui.graphics`, نه `androidx.compose.ui.draw` — نکته‌ای که در تلاش اول اشتباه import شد و کامپایل شکست) اضافه شد. `compileDebugKotlin` و `lintDebug` هر دو سبز.

---

### [MEDIUM] Composableهای خدای‌گونه

**Files:**

| فایل | خطوط |
|------|------:|
| `feature/cargo_entry/presentation/ActiveQuotasContent.kt` | ۱٬۴۲۰ |
| `feature/reports/presentation/dialogs/CargoEditSearchDialogsSection.kt` | ۱٬۲۳۴ |
| `security/SecurityScreen.kt` | ۱٬۱۱۲ |
| `feature/reports/presentation/dialogs/QuotaAnalysisSection.kt` | ۱٬۰۶۲ |
| `feature/admin/.../UserManagementScreen.kt` | ۱٬۰۵۷ |
| `ui/screens/ManageReportsScreen.kt` | ۱٬۰۴۰ |
| `feature/home/presentation/HomeScreen.kt` | ۹۶۸ |

مجموعاً **۳۱ فایل بیش از ۶۰۰ خط**.

**Why it matters:** Composable بزرگ = محدوده‌ی recomposition بزرگ. وقتی یک state تغییر کند، کل درخت داخل آن scope دوباره ارزیابی می‌شود. تفکیک به Composableهای کوچک‌تر و stateless، محدوده‌ی invalidation را کوچک می‌کند و کامپایلر Compose می‌تواند بخش‌های بیشتری را skip کند.

**Recommended Fix:** استخراج تدریجی. برای هر فایل: بخش‌های بدون state را به Composableهای stateless با پارامترهای صریح تبدیل کنید. با `ActiveQuotasContent.kt` (۱٬۴۲۰ خط) شروع کنید.

برای اندازه‌گیری اثر:
```bash
./gradlew assembleRelease -Pandroid.experimental.enableComposeCompilerReports=true
```
(روش در `gradle.properties` از قبل مستند شده.)

**Priority:** MEDIUM · **Effort:** High

---

# Animation Audit

## آمار

| API | تعداد |
|-----|------:|
| `animateFloatAsState` | ۷۴ |
| `AnimatedVisibility` | ۶۱ |
| `infiniteRepeatable` | ۴۱ |
| `Animatable` | ۳۸ |
| `AnimatedContent` | ۲۱ |
| `rememberInfiniteTransition` | ۲۱ |
| `animateColorAsState` | ۱۲ |
| `graphicsLayer` | **۱۱** |
| `LottieAnimation` | ۶ |

**نسبت کلیدی:** ۷۴ مورد `animateFloatAsState` در برابر تنها ۱۱ مورد `graphicsLayer`. یعنی اکثریت قاطع انیمیشن‌ها از مسیر پرهزینه‌ی composition عبور می‌کنند نه مسیر بهینه‌ی draw.

## [MEDIUM] انیمیشن‌های بی‌نهایت روی صفحات پرکاربرد

**File:** `app/src/main/java/com/atk/atk_cargo/feature/home/presentation/HomeScreen.kt`

**Location:** خطوط ۳۱۹–۳۲۸، ۴۴۹–۴۵۰

```kotlin
val infiniteTransition = rememberInfiniteTransition(label = "badge_pulse")
val badgeScale by infiniteTransition.animateFloat(
    initialValue = 0.92f,
    targetValue = 1.12f,
    animationSpec = infiniteRepeatable(
        animation = tween(900, easing = LinearEasing),
        repeatMode = RepeatMode.Reverse
    ),
    label = "badge_scale"
)
```

**Problem:** `rememberInfiniteTransition` تا زمانی که Composable در composition باشد، **هرگز متوقف نمی‌شود** و یک frame callback دائمی ثبت می‌کند. صفحه‌ی اصلی (`HomeScreen`) صفحه‌ای است که کاربر بیشترین زمان را روی آن می‌گذراند.

۲۱ مورد `rememberInfiniteTransition` در ۸ فایل، شامل `HomeScreen`، `UserManagementScreen`، `RealTimeLoadingBottomSheet` و `WarehouseDetailsScreen`.

**Why it matters:** انیمیشن دائمی یعنی دستگاه هرگز به حالت idle رندر نمی‌رسد. در ترکیب با polling هر ۳۰ ثانیه، مصرف باتری در یک شیفت کاری کامل قابل توجه می‌شود.

**Impact:** مصرف باتری؛ گرم شدن؛ کاهش عمر شیفت روی دستگاه‌های میدانی.

**Recommended Fix:**

۱. انیمیشن را مشروط کنید — فقط وقتی واقعاً معنا دارد (مثلاً badge فقط وقتی پیام خوانده‌نشده هست):

```kotlin
if (unreadCount > 0) {
    // فقط اینجا rememberInfiniteTransition
}
```

۲. برای انیمیشن‌های تزئینی، به تعداد تکرار محدود تغییر دهید.

۳. احترام به تنظیم دسترسی‌پذیری کاربر:

```kotlin
val animationsEnabled = Settings.Global.getFloat(
    context.contentResolver,
    Settings.Global.ANIMATOR_DURATION_SCALE, 1f
) != 0f
```

**Priority:** MEDIUM · **Effort:** Medium

**وضعیت (Phase4 #34 — ✅ انجام شد، با دامنه‌ی عمداً محدود):**

بررسی دقیق‌تر نشان داد شمار واقعی `rememberInfiniteTransition` در کد فعلی
**۱۳ نمونه در ۸ فایل** است (نه ۲۱ مورد گزارش — عدد قدیمی، پیش از
ماژول‌بندی‌های Phase قبلی). مهم‌تر: هنگام investigate، معلوم شد یک مکانیزم
Reduce Motion **از قبل وجود داشت** (`AnimationManager` — از یک audit قدیمی‌تر،
`DEEP_CODE_AUDIT.md #Phase3.12`)، فقط در ۳ محل `AnimatedVisibility` استفاده
می‌شد و هیچ‌کدام از ۱۳ `rememberInfiniteTransition` آن را چک نمی‌کردند.

- `AnimationManager.kt` از `feature/startup/domain` به **`core/domain`**
  منتقل شد — یک `object` سراسری بدون وابستگی Compose/Context بود، پس محل
  طبیعی‌اش ماژولی است که همه‌ی featureهای دارای انیمیشن (از جمله
  `feature:admin` که به `feature:startup` وابسته نبود) بدون افزودن یک لبه‌ی
  ماژولی جدید و نامرتبط بتوانند به آن دسترسی داشته باشند. ۴ فایل مصرف‌کننده‌ی
  موجود (`MainActivity.kt` و ۳ فایل دیگر در `app`) با تغییر import آپدیت
  شدند؛ `feature/startup/build.gradle.kts` یک `implementation(project(":core:domain"))`
  جدید گرفت (برای `ServerSyncingScreen.kt`).
- **باگ مصداقی گزارش** در [HomeScreen.kt](app/src/main/java/com/atk/atk_cargo/feature/home/presentation/HomeScreen.kt) رفع شد: انیمیشن `badgeScale` حالا
  فقط وقتی `warningsCount > 0` (یعنی Badge واقعاً نمایش داده می‌شود) اجرا
  می‌شود.
- یک نمونه‌ی مشابه‌ی همین باگ هم حین کار پیدا و رفع شد:
  [WarehouseDetailsScreen.kt](app/src/main/java/com/atk/atk_cargo/feature/reports/presentation/warehouse_details/WarehouseDetailsScreen.kt) — `dotAlpha` فقط وقتی `isSelected == true`
  واقعاً روی صفحه دیده می‌شد، اما بدون قید همیشه اجرا می‌شد.
- در تمام ۱۳ نقطه (شامل `SecurityScreen.kt` ×۵، `HomeScreen.kt` ×۲،
  `UpdateDialog.kt`، `WarehouseDetailsScreen.kt`،
  `CargoEditSearchDialogsSection.kt`، `RealTimeLoadingBottomSheet.kt`،
  `UserManagementScreen.kt`، `ServerSyncingScreen.kt`)
  `AnimationManager.areAnimationsEnabled()` اضافه شد: وقتی کاربر «حذف
  انیمیشن‌ها» را در تنظیمات سیستم فعال کرده، `rememberInfiniteTransition`
  اصلاً animate نمی‌کند و یک مقدار ثابتِ «حالت مستقر» (مثلاً scale=1f،
  alpha=0f برای حلقه‌های پالس نامرئی) به‌جایش استفاده می‌شود — نه فقط
  duration را صفر کردن، بلکه کاملاً از ثبت frame callback جلوگیری می‌شود.
- **دامنه‌ی عمداً حذف‌شده:** اعمال Reduce Motion به بقیه‌ی انواع انیمیشن
  (۷۴ `animateFloatAsState`، ۳۸ `Animatable` و غیره) خارج از دامنه ماند —
  طبق تأیید کاربر، فقط انیمیشن‌های بی‌نهایت (که مصداق مستقیم مشکل battery/
  frame-callback گزارش‌اند) پوشش داده شدند.

تأیید شد: `./gradlew :app:compileDebugKotlin` (شامل `core:domain`،
`feature:startup`، `feature:admin`)، `:app:lintDebug`،
`:app:testDebugUnitTest` همگی سبز.

## [LOW] عدم پشتیبانی از Reduce Motion

هیچ‌جای پروژه `ANIMATOR_DURATION_SCALE` یا معادل آن بررسی نمی‌شود. کاربرانی که در تنظیمات سیستم انیمیشن را غیرفعال کرده‌اند (اغلب به‌دلایل پزشکی مثل حساسیت به حرکت، یا برای دستگاه‌های ضعیف) همچنان همه‌ی انیمیشن‌ها را می‌بینند.

**Recommended Fix:** یک `CompositionLocal` مرکزی تعریف کنید که مقیاس مدت انیمیشن را حمل کند و در تمام `tween(...)`ها ضرب شود.

**Priority:** LOW · **Effort:** Medium

**وضعیت (Phase4 #34 — ✅ تا حد زیادی از قبل موجود بود + تکمیل جزئی):**

برخلاف ارزیابی این بخش از گزارش، `Settings.Global.ANIMATOR_DURATION_SCALE`
از قبل در `MainActivity.kt` خوانده می‌شد و در `AnimationManager`
(اکنون در `core/domain`) ذخیره می‌شد — فقط مصرف‌کننده‌های آن ناقص بودند
(۳ محل `AnimatedVisibility`، هیچ‌کدام از ۱۳ `rememberInfiniteTransition`).
به‌جای یک `CompositionLocal` جدید و مستقل (که یک سیستم موازی می‌ساخت)،
همان `AnimationManager` موجود به ۱۳ نقطه‌ی جدید هم متصل شد — جزئیات در
بخش «انیمیشن‌های بی‌نهایت» بالا.

---

# Performance Audit

## [HIGH] ایندکس گمشده روی `trackingNumber`

**File:** `PHP/src/Repositories/CargoRepository.php`

**Location:** خط ۲۹۹

**Problem:**

```php
$query = "SELECT id, trackingNumber, numberOfPeople, username, userType, entryTime,
          netWeight, scaleReceiptNumber, shortageWeight, excessWeight, exitTime,
          exitDate, status, confirm, confirmation, shipName, loadingWarehouse,
          cargoType, shippingCompany, loadingQuotaNumber
          FROM CargoInfo WHERE trackingNumber = ? ORDER BY entryTime DESC";
```

ایندکس‌های موجود روی `CargoInfo` (از `schema.sql`):

```sql
PRIMARY KEY (`id`),
UNIQUE KEY `uk_cargo_scale_receipt_number` (`scaleReceiptNumber`),
KEY `idx_cargo_status_group`  (`loadingQuotaNumber`,`shipName`,`loadingWarehouse`,`shippingCompany`,`cargoType`,`status`,`netWeight`),
KEY `idx_cargo_ship_lookup`   (`shipName`,`loadingWarehouse`,`status`,`loadingQuotaNumber`,`shippingCompany`,`cargoType`,`netWeight`),
KEY `idx_cargo_exit_window`   (`exitDate`,`status`,`exitTime`,`netWeight`)
```

**هیچ ایندکسی با `trackingNumber` شروع نمی‌شود** — و در هیچ‌کدام حتی حضور ندارد.

**Why it matters:** این کوئری از مسیر `cargo/search/tracking` فراخوانی می‌شود — یکی از پرکاربردترین عملیات اپ (جستجوی حواله). نتیجه: `type: ALL` (full table scan) به‌علاوه‌ی `Using filesort` برای `ORDER BY entryTime DESC`.

با جدولی که هر روز حواله اضافه می‌کند، این کوئری به‌صورت **خطی** کند می‌شود. امروز شاید ۵۰ms باشد؛ با ۵۰۰ هزار رکورد چند ثانیه خواهد بود.

کوئری دوم نیز متأثر است — `CargoRepository.php:32`:
```php
WHERE shipName = ? AND trackingNumber = ? AND updated_at >= ?
```
این می‌تواند از `idx_cargo_ship_lookup` برای `shipName` استفاده کند اما بقیه‌ی فیلتر روی ردیف‌های بازیابی‌شده اعمال می‌شود.

**Impact:** High — و با رشد داده بدتر می‌شود.

**Recommended Fix:**

```sql
-- ایندکس پوشا (covering) برای هر دو کوئری بالا
ALTER TABLE CargoInfo
  ADD INDEX idx_cargo_tracking (trackingNumber, entryTime);

ALTER TABLE CargoInfo
  ADD INDEX idx_cargo_ship_tracking (shipName, trackingNumber, updated_at);
```

پیش و پس از تغییر با `EXPLAIN` اندازه‌گیری کنید:

```sql
EXPLAIN SELECT ... FROM CargoInfo WHERE trackingNumber = 'X' ORDER BY entryTime DESC;
```

انتظار: تغییر `type` از `ALL` به `ref` و حذف `Using filesort`.

**Priority:** HIGH · **Effort:** Low

**Status:** ✅ Migration نوشته شد (`PHP/migrations/2026_08_19_add_cargo_tracking_index.sql`) — **هنوز روی دیتابیس تولید اجرا نشده** (طبق قرارداد پروژه، دستی و هم‌زمان با deploy). با یک MariaDB throwaway محلی و ۱۰٬۰۰۰ ردیف تصادفی تأیید شد:
- قبل: `type=ALL, rows=10000, Extra=Using where; Using filesort`
- بعد: `type=ref, rows=1, Extra=Using where; Using index` (حتی filesort هم حذف شد، بهتر از انتظار گزارش)
- کوئری دوم (`shipName`+`trackingNumber`+`updated_at`) هم از `idx_cargo_ship_tracking` با `type=range, rows=1` استفاده کرد.

`schema.sql` نیز برای هماهنگی با migration به‌روزرسانی شد.

---

## [MEDIUM] تاریخ و زمان به‌صورت `varchar(100)` ذخیره می‌شوند

**File:** `PHP/schema.sql` — جدول `CargoInfo`

```sql
`entryTime`  varchar(100) DEFAULT NULL,
`exitTime`   varchar(100) DEFAULT NULL,
`exitDate`   varchar(100) DEFAULT NULL,
`netWeight`  int unsigned DEFAULT NULL,
`shortageWeight` varchar(100) DEFAULT NULL,   ← عدد در varchar
`excessWeight`   varchar(100) DEFAULT NULL,   ← عدد در varchar
```

**Problem:**

دو مسئله‌ی جدا:

۱. **تاریخ‌ها `varchar`اند.** دلیل قابل درک است (تقویم جلالی)، اما نتیجه این است که `ORDER BY entryTime` مرتب‌سازی **لغوی** انجام می‌دهد نه زمانی، و بازه‌گیری (`BETWEEN`) روی تاریخ‌ها به مقایسه‌ی رشته‌ای متکی است. این فقط وقتی درست کار می‌کند که فرمت **همیشه** صفرپیش‌وند و ثابت باشد (`1405/05/24`) — یک قرارداد نانوشته که هیچ constraint ای آن را تضمین نمی‌کند.

۲. **`shortageWeight` و `excessWeight` عددند اما `varchar`.** در حالی که `netWeight` به‌درستی `int unsigned` است. این ناسازگاری یعنی محاسبات روی این دو ستون نیازمند تبدیل نوع است و هیچ اعتبارسنجی سطح دیتابیس ندارند.

**Why it matters:** هر داده‌ی بدفرمت (مثلاً `1405/5/4` بدون صفر) به‌صورت خاموش در مرتب‌سازی جای اشتباه می‌گیرد — یک باگ داده که در تست دیده نمی‌شود و در گزارش‌ها ظاهر می‌شود.

**Recommended Fix (تدریجی، بدون شکستن):**

۱. یک ستون `entryTimeUtc DATETIME` اضافه کنید و در کنار ستون جلالی پر کنید (dual-write).
۲. کوئری‌های مرتب‌سازی/بازه‌گیری را به ستون جدید منتقل کنید.
۳. ستون جلالی را فقط برای نمایش نگه دارید.
۴. `shortageWeight`/`excessWeight` را به `INT` تبدیل کنید (پس از پاک‌سازی داده).

**Priority:** MEDIUM · **Effort:** High

---

## [MEDIUM] حلقه‌ی شمارش معکوس با `delay(200)`

**File:** `app/src/main/java/com/atk/atk_cargo/ui/viewmodel/ReportsViewModel.kt`

**Location:** خطوط ۲۸۳–۲۸۷

```kotlin
while (true) {
    delay(200)
    val remainingMs = nextRefreshAt - System.currentTimeMillis()
    val remainingSeconds = (remainingMs / 1000L).toInt().coerceAtLeast(0)
    _realTimeUiState.update { it.copy(secondsToNextRefresh = remainingSeconds) }
    ...
}
```

**Problem:** حلقه ۵ بار در ثانیه بیدار می‌شود تا مقداری را به‌روز کند که فقط **یک بار در ثانیه** تغییر می‌کند.

**ارزیابی منصفانه:** `StateFlow` مقادیر برابر را conflate می‌کند، پس ۴ به‌روزرسانی از هر ۵ باعث emission نمی‌شوند و recomposition اضافی رخ نمی‌دهد. اما همچنان ۵ بار در ثانیه یک شیء `data class` جدید تخصیص داده می‌شود و coroutine بیدار می‌شود.

نکته‌ی مثبت: محاسبه‌ی `nextRefreshAt` از روی هدف قبلی (نه `now + 30s`) برای جلوگیری از drift، طراحی درستی است.

**Recommended Fix:**

```kotlin
while (true) {
    val remainingMs = nextRefreshAt - System.currentTimeMillis()
    if (remainingMs <= 0) { /* refresh */ ; continue }

    _realTimeUiState.update { it.copy(secondsToNextRefresh = (remainingMs / 1000L).toInt()) }
    // فقط تا مرز ثانیه‌ی بعدی بخواب، نه ۲۰۰ms ثابت
    delay(remainingMs % 1000L + 1)
}
```

**Priority:** MEDIUM · **Effort:** Low

---

## نقاط قوت Performance

- **`MicroCache`** با TTL کوتاه برای کوئری‌های پرتکرار (`ChatController.php:275` — کش ۴ ثانیه‌ای برای badge نخوانده).
- **ETag + Cache-Control** روی endpointهای خواندنی (`AppApiController::sendCacheableJsonResponse`).
- **`touchLastActivityThrottled`** (`SessionRepository.php:144`) — `UPDATE` فقط اگر بیش از ۶۰ ثانیه گذشته باشد، به‌جای هر درخواست. تفکر عملکردی خوبی است.
- **Covering index ها** روی `CargoInfo` که شامل `netWeight` هستند تا `SUM()` بدون مراجعه به جدول انجام شود.
- **کش دیسک HTTP** ۱۰MB در OkHttp.
- **Baseline Profile** پیکربندی شده.
- **`ConnectionPool(10, 5, TimeUnit.MINUTES)`** — بزرگ‌تر از پیش‌فرض، متناسب با الگوی polling چندصفحه‌ای.

---

# Memory Audit

**ارزیابی: 7/10 — نشت آشکاری یافت نشد.**

بررسی‌های انجام‌شده:

| بررسی | نتیجه |
|-------|-------|
| نگهداری `Context` در singleton | ✅ همه‌جا `applicationContext` (`UpdateManager.kt:37`، `RetrofitClient.kt:43`) |
| نشت coroutine | ✅ همه در `viewModelScope` یا `repeatOnLifecycle` |
| listener بدون unregister | ✅ یافت نشد |
| `GlobalScope` | ✅ صفر |
| اندازه‌ی کش Room | ✅ `deleteOldMessages()` سقف ۱۰۰ پیام |
| فایل‌های APK دانلودشده | ✅ `cleanupDownloadFiles()` در `onCleared()` |

## [LOW] `lateinit var currentDownloadFile` بدون گارد کامل

**File:** `app/src/main/java/com/atk/atk_cargo/api/UpdateManager.kt` خط ۶۸

```kotlin
private lateinit var currentDownloadFile: File
```

بیشتر استفاده‌ها با `::currentDownloadFile.isInitialized` گارد شده‌اند (خطوط ۵۲۱، ۵۸۷) که خوب است. اما این الگو شکننده است — هر استفاده‌ی جدید باید گارد را به یاد بیاورد.

**Recommended Fix:** `private var currentDownloadFile: File? = null` و استفاده از `?.let { }`.

**Priority:** LOW · **Effort:** Low

---

# PHP Backend Audit

**ارزیابی: 8/10 — قوی‌ترین بخش پروژه.**

## معماری

```
api/v2/index.php  →  Router (opt-in)  →  ApiAuthGate  →  Controller  →  Service  →  Repository  →  MySQL
```

`Router.php` یک تصمیم معمارانه‌ی خوب است. کامنت خودش دلیل را دقیق بیان می‌کند: مدل قبلی «هر فایل `.php` = یک endpoint با whitelist خروجی (opt-out)» بود؛ حالا «فقط ردیف‌های تعریف‌شده در دسترس‌اند (opt-in)».

نکته‌ی ظریف و درست در `Router.php:35-37` — مرتب‌سازی routeها بر اساس تعداد پارامتر:

```php
usort($routes, static function (array $a, array $b): int {
    return self::countParams($a['path']) <=> self::countParams($b['path']);
});
```

این وابستگی به ترتیب فیزیکی ردیف‌ها را حذف می‌کند (`quotas/filtered` قبل از `quotas/{quotaNumber}`). طراحی مقاوم در برابر خطای انسانی.

## دفاع لایه‌ای (نمونه‌ی خوب)

`UserController.php:37-44` — حتی وقتی router برای یک route مقدار `permission => null` اعلام کرده، کنترلر مستقلاً بررسی می‌کند:

```php
private const ADMIN_ONLY_ACTIONS = [
    'getAllUsers', 'getAllUsersWithStatus', 'getActiveDeviceId',
    'createUser', 'deleteUser', 'forceLogout',
];
...
if (in_array($action, self::ADMIN_ONLY_ACTIONS, true)) {
    $this->requirePermission('manage_users');
}
```

**چرا این مهم است:** `Request::get()` بدنه‌ی JSON را بر `$_GET` اولویت می‌دهد (`Request.php:82-91`). Routerهای shim مقدار `$_GET['action']` را تنظیم می‌کنند. بنابراین یک کلاینت می‌تواند با فرستادن `{"action": "createUser"}` در بدنه، مقدار تنظیم‌شده توسط router را override کند. **این دفاع لایه‌ای دقیقاً همان چیزی است که آن مسیر را می‌بندد.** طراحی درستی است.

## [MEDIUM] معماری دوگانه: shimهای مستقیم، Router را دور می‌زنند

**Files:** `PHP/check_signature.php`, `check_update.php`, `get_license_info.php`, `quota_remaining_api.php`, `update_fcm_token.php`, `validate_license.php`

**Problem:**

```php
// validate_license.php
require_once __DIR__ . '/src/bootstrap.php';
(new \App\Controllers\LicenseController())->validateLicense();
```

این ۶ فایل مستقیماً کنترلر را صدا می‌زنند و **هرگز از `Router` یا `ApiAuthGate` عبور نمی‌کنند**. یعنی دو مسیر ورودی موازی با دو مدل امنیتی متفاوت:

| مسیر | گیت امنیتی |
|------|-----------|
| `/api/v2/...` | `ApiAuthGate` در سطح Router (اجباری، فراموش‌نشدنی) |
| `/validate_license.php` | فقط آنچه خودِ کنترلر صدا بزند |

**Why it matters:** طراحی `Router` صراحتاً برای رفع همین مشکل بود (کامنت `ApiAuthGate.php:16-20`: «حتی اگر یک route handler فراموش کند خودش این گیت را صدا بزند، Router آن را صدا می‌زند»). اما شش endpoint خارج از این تضمین باقی مانده‌اند — و همان‌طور که در بخش امنیت دیدیم، `LicenseController` واقعاً هیچ گیتی ندارد.

**Recommended Fix:**

هر شش را به `routes/api_v2.php` منتقل کنید و shimها را حذف یا به redirect تبدیل کنید:

```php
// routes/api_v2.php
[
    'method' => 'POST', 'path' => 'license/validate',
    'auth' => false, 'permission' => null,
    'handler' => fn() => (new LicenseController())->validateLicense(),
],
```

کلاینت (`SecurityVerifier.kt:46-48`) URLها را از `Secrets` می‌خواند، پس تغییر مسیر نیازمند به‌روزرسانی همزمان `secrets.cpp` است — که در هر صورت به‌دلیل مورد CRITICAL باید بازنویسی شود. **این دو کار را با هم انجام دهید.**

**Priority:** MEDIUM · **Effort:** Medium

**Status:** ✅ Fixed (2026-08-20) — هر ۶ متد به‌عنوان route موازی به `routes/api_v2.php` اضافه شدند (`utility/check-signature`, `utility/check-update`, `license/validate`, `license/info`, `analytics/quota-remaining`, `users/fcm-token`)، با auth/permission دقیقاً هم‌راستا با چیزی که خودِ متد داخلاً چک می‌کند. با یک هارنس PHP روی `api/v2/index.php` واقعی (با throwaway DB شامل جداول `Users`/`SignChecker`/`licenses`/`user_sessions`) هر ۶ مسیر + یک مورد ۴۰۵ (متد اشتباه) تست شد — همه‌ی کدهای HTTP (۴۰۰/۴۰۳/۲۰۰/۲۰۰/۴۰۱/۴۰۱/۴۰۵) دقیقاً مطابق انتظار بودند، و دو مسیر `auth=>true` (`analytics/quota-remaining`, `users/fcm-token`) پیش از رسیدن به handler توسط `ApiAuthGate` رد شدند.

در ادامه (۲۰۲۶-۰۸-۲۰)، هر ۶ فایل shim قدیمی (`check_signature.php`، `check_update.php`، `validate_license.php`، `get_license_info.php`، `quota_remaining_api.php`، `update_fcm_token.php`) از ریشه‌ی `PHP/` حذف شدند — بدون rewrite جایگزین برای URLهای قدیمی. طبق تأیید صریح کاربر، سرور فعلی صرفاً محیط تست/توسعه است و هنوز کاربر واقعی روی این نسخه نیست؛ حذف بدون rewrite قصداً انتخاب شد تا هیچ مسیر bypass باقی نماند. کامنت‌های داخلی کنترلرها که به نام فایل‌های حذف‌شده اشاره می‌کردند به نام مسیر Router جدید به‌روز شدند. `php -l`، `phpstan`، `phpunit` (۶۸ تست) سبز.

**همان روز، کلاینت هم با URLهای جدید هماهنگ شد** — ۳ آدرس hardcode در `secrets.cpp` (`getSignatureCheckUrl`/n3، `getLicenseCheckUrl`/n4، `getLicenseInfoUrl`/n5) از فرمت قدیمی (`.../check_signature.php` و مشابه) به فرمت جدید Router (`.../api/v2/index.php?route=utility/check-signature` و مشابه) بازرمزنگاری شدند — مقادیر قبلی با XOR رمزگشایی و مقادیر جدید با همان کلید (`0x5A`) رمزنگاری و جایگزین شدند؛ `externalNativeBuildDebug` (CMake) سبز. `UpdateManager.kt:122` هم از `${Constants.BASE_URL}/check_update.php?...` به `${Constants.BASE_URL}api/v2/index.php?route=utility/check-update&...` تغییر کرد (بدون این تغییر، چک آپدیت اپ به‌طور کامل از کار می‌افتاد چون فایل سرور دیگر وجود ندارد). ابزار داخلی `tools/secrets_generator.html` هم (که برای تولید/دیباگ همین فایل استفاده می‌شود) با همین ۳ URL جدید و مقدار صحیح `getBaseUrl` (`.../test_api/` که قبلاً در این فایل، نه در خودِ `secrets.cpp`، اشتباه بود) به‌روز شد. دو shim بدون ارجاع کلاینت فعلی (`quota_remaining_api.php`، `update_fcm_token.php`) نیازی به تغییر کلاینت نداشتند. `compileDebugKotlin` سبز.

**۲۰۲۶-۰۸-۲۰ (ادامه):** عدم-ارجاع دو shim باقی‌مانده به‌صورت مستقل دوباره تأیید شد (`grep` سراسری روی `app/src` برای `quota_remaining_api`, `update_fcm_token`, `updateFcmToken`, `quota-remaining`, `fcm-token`, `fcm` — صفر نتیجه؛ اپ اصلاً FCM ندارد). همچنین معلوم شد آرایه‌ی «defaults» خودِ `tools/secrets_generator.html` قدیمی بود: ۱۱ فیلد داشت که ۴ تای آن‌ها (`getWebUserName`, `getWebUserPass`, `getAuthUser`, `getAuthenticationX365`) اصلاً در `kSecretsMethods` فعلی `secrets.cpp` وجود ندارند (باقیمانده از نسخه‌ی قدیمی‌تر فایل native)، و مقدار `getExpectedSignatureHash` که در آن hardcode بود هم با هش واقعی فعلی فایل نمی‌خواند. ابزار کامل بازطراحی شد (ظاهر flat/مینیمال، دو تم روشن/تاریک، آیکون‌های SVG به‌جای ایموجی) و آرایه‌ی defaults به همان ۸ فیلد واقعی `secrets.cpp` (به همان ترتیب n0..n7) با مقادیر واقعی رمزگشایی‌شده محدود شد. صحت با round-trip در مرورگر تأیید شد: خروجی «Load ATK Cargo defaults» → C++ تولیدشده، بایت‌به‌بایت با `secrets.cpp` فعلی یکسان بود.

---

# API Audit

## استخراج Endpointها

| Method | Path | Auth | Permission |
|--------|------|:----:|-----------|
| POST | `auth/login` | ❌ | — |
| POST | `auth/refresh` | ❌ | — |
| POST | `auth/session` | ❌ | — |
| POST | `auth/logout` | ❌ | (توکن داخلاً بررسی می‌شود) |
| POST | `cargo` | ✅ | — |
| POST | `cargo/update` | ✅ | `edit_cargo` |
| POST | `cargo/confirm` | ✅ | `cargo_counter` |
| POST | `cargo/delete` | ✅ | `delete_cargo` |
| GET | `cargo/search/scale-receipt` | ✅ | — |
| GET | `cargo/search/tracking` | ✅ | — |
| GET/POST | `cargo/initial-info` | ✅ | — / `initial_info` |
| GET | `cargo/scale-receipt/check` | ✅ | — |
| GET | `ships/active` | ✅ | — |
| POST | `utility/check-password` | ✅ | — |
| POST | `utility/check-existence` | ✅ | — |
| POST | `utility/sync-permissions` | ✅ | — |
| GET | `users` | ✅ | (داخلی: `manage_users`) |
| GET | `users/self` | ✅ | — |
| GET | `users/admins` | ✅ | — |
| GET | `users/status` | ✅ | `manage_users` |
| GET | `users/active-device` | ✅ | `manage_users` |
| POST | `users` | ✅ | `manage_users` |
| POST | `users/{id}/update` | ✅ | (نامتقارن، داخلی) |
| POST | `users/{id}/delete` | ✅ | `manage_users` |
| POST | `users/force-logout` | ✅ | `manage_users` |
| GET/POST | `chat/messages` | ✅ | (داخلی: admin) |
| GET | `chat/unread-count` | ✅ | (داخلی: admin) |
| POST | `chat/messages/{id}/edit\|delete\|read` | ✅ | (داخلی: مالکیت) |
| GET | `analytics/kotazh\|realtime\|comprehensive` | ✅ | `view_reports` |
| POST | `analytics/export-log` | ✅ | `view_reports` |
| GET | `health` | ❌ | — (عمدی) |
| POST | `diagnostics/crash` | ❌ | — (عمدی) |
| GET/POST | Quotas (۲۰+ مسیر) | ✅ | `manage_quotas` / `view_reports` |

## [MEDIUM] عدم رعایت معنای متدهای HTTP

**File:** `PHP/src/routes/api_v2.php`

**Location:** خطوط ۴۳۷–۴۴۱، ۵۳۲–۵۳۴، ۵۷۱–۵۷۵

**Problem:**

مسیرهایی که منطقاً باید `PATCH`/`DELETE` باشند، `POST` هستند:

```
POST cargo/update              ← باید PATCH باشد
POST cargo/delete              ← باید DELETE باشد
POST users/{id}/update         ← باید PATCH باشد
POST users/{id}/delete         ← باید DELETE باشد
POST chat/messages/{id}/edit   ← باید PATCH باشد
POST chat/messages/{id}/delete ← باید DELETE باشد
```

**ارزیابی منصفانه:** کامنت‌های کد **دقیقاً می‌دانند چرا** و دلیل واقعی است:

> «POST نه PATCH — `CargoController::updateCargoInfo` داخلاً `$_SERVER['REQUEST_METHOD'] !== 'POST'` را رد می‌کند (کنترلرهای legacy فقط GET/POST را می‌شناسند)»

پس این یک اشتباه ناآگاهانه نیست، بلکه بدهی فنی آگاهانه است.

**Why it matters:** با این حال هزینه دارد: API غیرقابل‌پیش‌بینی برای مصرف‌کننده‌ی جدید، عدم امکان استفاده از idempotency طبیعی `DELETE`/`PUT`، و ناسازگاری با ابزارهای استاندارد.

**Recommended Fix:**

بررسی متد را از کنترلرها به `Request` منتقل کنید تا کنترلر متدآگنوستیک شود:

```php
// در Request.php اضافه کنید
public function isWrite(): bool {
    return in_array($this->getMethod(), ['POST', 'PUT', 'PATCH'], true);
}
```

سپس در کنترلرها `!$this->request->isPost()` را به `!$this->request->isWrite()` تغییر دهید. پس از آن، جدول route می‌تواند متدهای درست را اعلام کند بدون شکستن کلاینت (که می‌تواند تدریجی مهاجرت کند).

**Priority:** MEDIUM · **Effort:** Medium

**وضعیت (Phase4 #33 — ✅ انجام شد، با یک انحراف عمدی کلیدی از پیشنهاد گزارش):**

بررسی دقیق‌تر نشان داد `Router::dispatch()` هر route را دقیقاً با **یک**
فعل HTTP (نه چند فعل) تطبیق می‌دهد؛ یعنی راهکار «`isWrite()` در کنترلر +
جدول route با فعل درست» به‌تنهایی کلاینت‌های موجود را می‌شکند مگر Router هم
از چند فعل برای یک route پشتیبانی کند — چیزی که پیشنهاد گزارش صراحتاً به آن
اشاره نمی‌کند. چون این پروژه **یک کلاینت اندروید تک‌نسخه‌ای** با مکانیزم
به‌روزرسانی اجباری (`VersionExpiredDialog`) دارد، به‌جای ساخت زیرساخت
چندفعلی برای Router (که یک بدهی فنی جدید و مسیر migration دائمی می‌ساخت)،
**کلاینت و سرور هم‌زمان و هماهنگ** تغییر کردند — الگویی که در فازهای قبلی
این پروژه (حذف shimهای PHP، حذف v1) هم به‌کار رفته بود.

- **`Request.php`**: متد `isWrite()` اضافه شد (`POST`/`PUT`/`PATCH`/`DELETE`)
  — نه برای سازگاری با نسخه‌های قدیمی کلاینت (که رد شد)، بلکه چون
  `UserController::handle()` و `ChatController::handleChatRequest()` هرکدام
  یک شاخه‌ی مشترک دارند که چند action با فعل‌های متفاوت (مثلاً
  createUser=POST، updateUser=PATCH، deleteUser=DELETE) را سرویس می‌دهند —
  آن شاخه نمی‌تواند فقط `isPost()` باشد.
- **`routes/api_v2.php`**: ۶ route (`cargo/update`→PATCH، `cargo/delete`→DELETE،
  `users/{id}/update`→PATCH، `users/{id}/delete`→DELETE،
  `chat/messages/{id}/edit`→PATCH، `chat/messages/{id}/delete`→DELETE) به
  فعل معنایی درست تغییر کردند؛ کامنت‌های قدیمی («POST نه PATCH چون...»)
  بازنویسی شدند.
- **کنترلرها**: چک داخلی `CargoController::updateCargoInfo`/`deleteCargoInfo`
  به فعل جدید تغییر کرد؛ `UserController::handle`/`ChatController::handleChatRequest`
  از `isWrite()` استفاده می‌کنند (دلیل بالا).
- **`ApiServiceV2.kt`**: ۶ متد Retrofit از `@POST` به `@PATCH`/`@DELETE`
  تغییر کردند. **نکته‌ی فنی مهم:** `@DELETE` استاندارد Retrofit با `@Body`
  کامپایل نمی‌شود («Non-body HTTP method cannot contain @Body»)؛ ۳ متدی که
  بدنه دارند (`deleteCargo`، `deleteUser`، `deleteChatMessage`) به‌جای
  `@DELETE` از `@HTTP(method="DELETE", hasBody=true)` استفاده می‌کنند.
- **`openapi.yaml`**: ۶ کلید فعل (`post:`) متناظر به `patch:`/`delete:`
  به‌روزرسانی شدند.

تأیید شد: `php -l` تمام فایل‌های PHP تغییریافته، PHPStan بدون خطا، Kotlin
`compileDebugKotlin`/`lintDebug`/`testDebugUnitTest` سبز. `phpunit` (۶۸
تست) با ۶ خطای preexisting و نامرتبط اجرا شد — همگی در
`PermissionServiceTest` به‌خاطر `PHP/config/permissions.json` که از دیسک
حذف شده (یافته‌ی جانبی گزارش‌شده در Phase3 #22/#28/Phase4 #34، هنوز
منتظر تصمیم کاربر)، نه به‌خاطر این تغییرات.

---

## [MEDIUM] دو شکل ناسازگار برای پاسخ خطا

**File:** `PHP/src/Core/AuthenticatesRequests.php` خطوط ۵۷–۷۶

**Problem:**

```php
// دو گروه کنترلر دو شکل متفاوت انتظار دارند:
// CargoController/UtilityController → {error: true, message: "..."}
// AppApiController/AnalyticsController → {error: "..."}
```

کامنت خود کد این را «یکی‌کردن این دو شکل بدون تغییر هم‌زمان کلاینت، یکی از این دو مصرف‌کننده را خراب می‌کرد» توصیف می‌کند — تشخیص درستی است.

اما شکل سومی هم وجود دارد. `ApiAuthGate.php:51-55`:

```php
Response::json([
    'success' => false,
    'message' => '...',
    'code' => $code,
], 401);
```

پس در مجموع **سه شکل خطا** در یک API:
1. `{error: true, message}`
2. `{error: "پیام"}`
3. `{success: false, message, code}`

**Impact:** کد کلاینت باید هر سه را مدیریت کند؛ مدیریت خطا شکننده و پرتکرار می‌شود.

**Recommended Fix:**

یک قالب واحد تعریف کنید و با نسخه‌بندی مهاجرت کنید:

```json
{
  "success": false,
  "error": { "code": "access_token_expired", "message": "..." }
}
```

در یک دوره‌ی گذار، هر سه فیلد قدیمی را نیز در پاسخ نگه دارید تا کلاینت‌های قدیمی نشکنند، سپس با `min_allowed_version` قدیمی‌ها را از رده خارج کنید.

**Priority:** MEDIUM · **Effort:** Medium

---

## [LOW] نبود Rate Limiting سراسری روی Router v2

**File:** `PHP/src/Core/Router.php`

کامنت `AuthController.php:172-174` این را صریحاً اذعان می‌کند:

> «Router v2 برخلاف protected_proxy.php (v1) هیچ rate-limit عمومی‌ای ندارد»

`LoginAttemptLimiter` روی `login` و `refresh` اعمال شده که مهم‌ترین موارد را پوشش می‌دهد. اما هیچ سقف عمومی درخواست وجود ندارد.

**Recommended Fix:** یک middleware ساده در `Router::dispatch` قبل از حلقه‌ی route:

```php
if (!RateLimiter::allow($request->getClientIp(), limit: 120, windowSeconds: 60)) {
    Response::error('تعداد درخواست‌ها بیش از حد مجاز است.', 429);
}
```

**Priority:** LOW · **Effort:** Low

---

# Database Audit

**ارزیابی: 7/10**

## نقاط قوت

**ایندکس‌گذاری در سطح حرفه‌ای.** نمونه:

```sql
KEY `idx_cargo_status_group` (`loadingQuotaNumber`,`shipName`,`loadingWarehouse`,
                              `shippingCompany`,`cargoType`,`status`,`netWeight`)
```

این یک covering index است — `netWeight` در انتها قرار داده شده تا `SUM(netWeight)` بدون مراجعه به جدول اصلی محاسبه شود. این سطح از تفکر نادر است.

**جدول `user_sessions`** ایندکس‌های دقیقاً متناسب با الگوهای دسترسی دارد:
```sql
UNIQUE KEY `idx_refresh_token` (`refresh_token`),
KEY `idx_username_active` (`username`,`is_active`),
KEY `idx_active_activity` (`is_active`,`last_activity`),
KEY `idx_username_device_active` (`username`,`device_id`,`is_active`),
```
هر سه کوئری اصلی (`validateTokenAndGetUserType`، `getActiveSessionByDevice`، `cleanupExpiredSessions`) ایندکس اختصاصی دارند.

**کنترل همزمانی درست.** `CargoRepository.php:69`:

```sql
ORDER BY id DESC LIMIT 1 FOR UPDATE
```

با کامنت توضیحی دقیق درباره‌ی رفتار gap lock در REPEATABLE READ. `CargoService.php` تراکنش را با `rollback()` در هر مسیر خطا مدیریت می‌کند.

**Foreign key ها** با `ON DELETE CASCADE` در migration های اخیر اضافه شده‌اند.

## مشکلات

- **[HIGH]** ایندکس گمشده روی `trackingNumber` — به بخش Performance مراجعه کنید.
- **[MEDIUM]** تاریخ/عدد در `varchar` — به بخش Performance مراجعه کنید.

## [LOW] نبود جدول ردیابی migration

**File:** `PHP/migrations/`

سه فایل migration وجود دارد اما هیچ جدول `schema_migrations` ای نیست. `DiagnosticsController.php:29-30` این را اذعان می‌کند:

> «بدون نیاز به schema_migrations چون این پروژه چنین جدولی ندارد»

**Impact:** هیچ راه برنامه‌ای برای دانستن اینکه کدام migration روی یک سرور اجرا شده وجود ندارد. `PermissionManager.php:54-58` مجبور است این را با try/catch حدس بزند:

```php
try {
    $permissionRepository->getAllRolePermissions();
} catch (\Throwable $e) {
    $dbMigrated = false;
}
```

**Recommended Fix:**

```sql
CREATE TABLE schema_migrations (
  version VARCHAR(255) PRIMARY KEY,
  applied_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```
به‌همراه یک اسکریپت `migrate.php` که فایل‌های اجرانشده را به ترتیب اجرا و ثبت کند.

**Priority:** LOW · **Effort:** Medium

---

# Authentication & Authorization

**ارزیابی: قوی‌ترین بخش سیستم (به‌جز مسئله‌ی fallback رمز).**

## جریان کامل

```
۱. POST auth/login  { username, password, deviceId, ... }
       ↓
   LoginAttemptLimiter::isLocked(username, ip)     ← ۵ تلاش / ۱۵ دقیقه
       ↓
   UserService::verifyCredentials()                ← bcrypt cost=12
       ↓
   SessionService::createMobileSession()
       ↓  bin2hex(random_bytes(32)) × ۲
   SessionRepository::createSession()
       ↓  SHA-256(token) در دیتابیس ذخیره می‌شود
   پاسخ: { session_token, refresh_token, access_token_expires_in: 1800, ... }

۲. هر درخواست بعدی:
   هدرها: X-Username، X-Device-Id، X-Session-Token، X-App-Version
       ↓
   ApiAuthGate::requireAuthenticated()
       ↓  MinVersionGate::enforce()
       ↓  validateTokenAndGetUserType()  ← یک کوئری واحد
       ↓  touchLastActivityThrottled()   ← UPDATE فقط هر ۶۰ ثانیه
       ↓  ApiAuthGate::requirePermission()

۳. در ۴۰۱ با code=access_token_expired:
   TokenAuthenticator (OkHttp) → POST auth/refresh → چرخش هر دو توکن
```

## نقاط قوت (تأییدشده)

| ویژگی | مکان | ارزیابی |
|-------|------|---------|
| توکن ۲۵۶ بیتی CSPRNG | `SessionService.php:116,118` | ✅ |
| ذخیره‌ی هش‌شده در DB | `SessionRepository.php:38-40, 256, 288` | ✅ |
| چرخش هر دو توکن در هر refresh | `SessionRepository::rotateTokens` | ✅ |
| تشخیص refresh token reuse | `SessionService.php:144-153` | ✅ عالی |
| ابطال کل نشست‌ها در reuse + هشدار | `SessionService.php:145-151` | ✅ عالی |
| `hash_equals` (مقایسه‌ی ثابت‌زمان) | `SessionService.php:144` | ✅ |
| انقضای بی‌فعالیتی (۲۴ ساعت) | `SessionRepository.php:15, 70` | ✅ |
| محدودیت ورود تک‌دستگاهی | `SessionService.php:63-69` | ✅ |
| `getClientIp` فقط `REMOTE_ADDR` | `Request.php:108-110` | ✅ درست — هدر جعل‌پذیر استفاده نمی‌شود |
| تأیید رمز فعلی برای تغییر رمز | `UserController.php:193-205` | ✅ |
| بررسی مالکیت پیام چت | `ChatController.php:214, 243` | ✅ |
| CSRF در پنل وب | `PermissionManager.php:64-71` | ✅ |
| `session_regenerate_id(true)` پس از لاگین | `PermissionManager.php:89` | ✅ |
| کوکی `httponly`+`secure`+`samesite` | `PermissionManager.php:17-23` | ✅ |

## [MEDIUM] نشست‌های پنل وب هرگز نمی‌توانند از گیت API عبور کنند

**File:** `PHP/src/Services/SessionService.php`

**Location:** خطوط ۲۲۷–۲۳۷ (`createWebSession`) در برابر `SessionRepository.php:71`

**Problem:**

`createWebSession` نشستی می‌سازد که `access_token_expires_at` ندارد:

```php
$sessionToken = bin2hex(random_bytes(32));
$sessionId = $this->sessionRepository->createSession([
    'username' => $username,
    // ... هیچ 'access_token_expires_at' ای پاس نمی‌شود
    'session_token' => $sessionToken
]);
```

و `createSession` آن را `NULL` می‌گذارد (`SessionRepository.php:257`).

اما `isValidToken` و `validateTokenAndGetUserType` هر دو شرط زیر را دارند:

```sql
AND access_token_expires_at > NOW()
```

در SQL، `NULL > NOW()` مقدار `NULL` (نه `TRUE`) برمی‌گرداند → ردیف هرگز مطابقت نمی‌کند.

**نتیجه:** هر نشستی که با `createWebSession` ساخته شود، **هرگز نمی‌تواند از گیت احراز هویت API عبور کند**.

**ارزیابی منصفانه:** این احتمالاً در عمل مشکلی ایجاد نمی‌کند، چون `createWebSession` فقط از `User/SessionManager.php:20` و `SessionManager.php` (شیم‌های legacy) صدا زده می‌شود، و پنل وب از نشست PHP (`$_SESSION['perm_manager_auth']`) استفاده می‌کند نه از این توکن. کامنت `SessionRepository.php:239-241` هم می‌گوید createWebSession «خارج از محدوده‌ی I-05» است.

پس این **کد مرده‌ی گمراه‌کننده** است، نه یک باگ فعال. اما اگر روزی کسی بخواهد پنل وب را به API متصل کند، با یک شکست خاموش و بسیار گیج‌کننده مواجه می‌شود.

**Recommended Fix:** یا `createWebSession` را حذف کنید (به‌همراه شیم‌های `SessionManager`)، یا آن را با `generateTokenPair()` هم‌راستا کنید تا مانند `createMobileSession` رفتار کند.

**Priority:** MEDIUM · **Effort:** Low

---

# Error Handling

## نقاط قوت

`ChatController.php:99-111` الگوی مرجع پروژه است:

```php
} catch (ApiException $e) {
    http_response_code($e->getStatusCode());
    echo json_encode(['success' => false, 'message' => $e->getMessage()], JSON_UNESCAPED_UNICODE);
    exit;
} catch (\Throwable $e) {
    $this->logger->error('ChatController: ' . $e->getMessage());
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'خطای داخلی سرور رخ داده است.'], JSON_UNESCAPED_UNICODE);
    exit;
}
```

تفکیک صحیح: `ApiException` (پیام عمدی فارسی برای کاربر) در برابر بقیه (فقط لاگ می‌شود).

`FloatTypeAdapter` در `RetrofitClient.kt:61-98` مدیریت خطای مثال‌زدنی دارد — مقادیر غیرقابل پارس را به `0f` تبدیل می‌کند اما **لاگ می‌کند** (خط ۸۳) تا در عیب‌یابی میدانی گم نشوند.

## مشکلات

- **[MEDIUM]** نشت `$e->getMessage()` در `UserController.php:83` — به بخش امنیت مراجعه کنید.

## [MEDIUM] مدیریت خطا در UI به‌صورت پراکنده و تکراری

**Files:** تمام ۹ فایلی که مستقیماً شبکه را صدا می‌زنند

**Problem:**

```kotlin
// InitialInfoScreen.kt:648-651 — این الگو در ۹ فایل تکرار شده
} catch (e: Exception) {
    dialogMessage = "خطا در ارتباط با سرور: ${e.localizedMessage}"
    isErrorDialog = true
    showDialog = true
}
```

سه مشکل:

۱. `catch (e: Exception)` بیش از حد گسترده است — `CancellationException` را هم می‌گیرد (که در Kotlin زیرمجموعه‌ی `Exception` است)، پس یک coroutine کنسل‌شده به‌عنوان «خطای سرور» به کاربر نمایش داده می‌شود.
۲. `e.localizedMessage` پیام فنی انگلیسی (مثلاً `Failed to connect to /1.2.3.4:443`) را به کاربر فارسی‌زبان نشان می‌دهد.
۳. هیچ استراتژی retry ای وجود ندارد.

**Recommended Fix:**

یک لایه‌ی نگاشت خطای متمرکز:

```kotlin
sealed interface AppError {
    data object Network : AppError
    data object Timeout : AppError
    data class Server(val code: Int) : AppError
    data class Validation(val message: String) : AppError
}

fun Throwable.toAppError(): AppError = when (this) {
    is CancellationException -> throw this          // هرگز نباید بلعیده شود
    is java.net.SocketTimeoutException -> AppError.Timeout
    is java.io.IOException -> AppError.Network
    is retrofit2.HttpException -> AppError.Server(code())
    else -> AppError.Server(-1)
}

fun AppError.toUserMessage(): String = when (this) {
    AppError.Network -> "اتصال به اینترنت برقرار نیست."
    AppError.Timeout -> "زمان پاسخ سرور به پایان رسید. دوباره تلاش کنید."
    is AppError.Server -> "خطای سرور. لطفاً بعداً تلاش کنید."
    is AppError.Validation -> message
}
```

**Priority:** MEDIUM · **Effort:** Medium

**وضعیت (Phase3 #22 — ✅ انجام شد، با دامنه‌ی عمداً محدود):**

بررسی دقیق‌تر نشان داد این الگو در واقع در **۹ فایل واقعی** (نه فرضی) و
**۴۰+ نقطه‌ی `catch`** تکرار شده، اما پیام‌های فارسی هرکدام
context-specific و متفاوت‌اند (مثلاً «خطا در بررسی شماره قبض باسکول»، «خطا
در ثبت اطلاعات بار»)، نه یک پیام تکراری یکسان مثل نمونه‌ی گزارش. یکسان‌سازی
کامل همه‌ی پیام‌ها به یک `AppError.toUserMessage()` عمومی این پیام‌های
مفید و خاص را از بین می‌برد — رگرسیون UX بود، نه بهبود.

**تصمیم (تأییدشده توسط کاربر):** دامنه به رفع خودِ باگ مستقل و واقعی گزارش
محدود شد، نه یکسان‌سازی سبک پیام‌ها:

- `core/domain/AppError.kt` (جدید) اضافه شد — سلاخته‌ی `AppError`/
  `toAppError()`/`toUserMessage()` برای استفاده‌ی تدریجی/آینده، هنوز به
  کد فراخوانی‌کننده سیم‌کشی نشده.
- در تمام ۹ فایل (`CargoViewModel.kt`، `UpdateManager.kt`،
  `ReportsRepository.kt`، `CargoCounterScreen.kt`، `SelectInfoScreen.kt`،
  `ManageReportsScreen.kt`، `ActiveQuotasDialogSection.kt`؛
  `InitialInfoViewModel.kt` از قبل درست بود) پیش از هر `catch (e: Exception)`
  یک `catch (e: CancellationException) { throw e }` اضافه شد — بدون تغییر
  پیام‌های موجود. `CancellationException` در Kotlin زیرمجموعه‌ی `Exception`
  است، پس بدون این جداسازی، لغو یک coroutine (مثلاً خروج کاربر از صفحه حین
  یک درخواست شبکه) به‌اشتباه به‌عنوان «خطای سرور» به کاربر نمایش داده
  می‌شد و/یا لغوِ coroutine به‌درستی propagate نمی‌شد.
- یک مورد مشابه‌ی دقیق‌تر هم در `CargoViewModel.kt` پیدا و رفع شد: بلوک
  `catch (e: Throwable)` که در Phase3 #20 عمداً برای گرفتن `Error`های واقعی
  (مثل `LinkageError`) اضافه شده بود، چون `Throwable` ابرمجموعه‌ی
  `CancellationException` هم هست، بدون قصد لغو coroutine را هم می‌بلعید؛
  یک `catch (e: CancellationException) { throw e }` پیش از آن اضافه شد تا
  فقط لغو دوباره propagate شود، بدون شکستن هدف اصلی #20.
- `CargoEditSearchDialogsSection.kt` از فهرست ۹ فایل گزارش خارج ماند: خطای
  آن از طریق یک callback نوع `Result`/`onFailure` می‌رسد، نه یک
  `catch (e: Exception)` مستقیم — این الگوی متفاوت خارج از دامنه‌ی همین
  باگ مشخص است.

**یافته‌ی جانبی (خارج از دامنه‌ی #۲۲، به کاربر گزارش شد):** حین این کار،
یک تغییر دست‌نخورده‌ی pre-existing و نامرتبط در working tree پیدا شد — یک
اقدام IDE («حذف import استفاده‌نشده») import صریح
`androidx.compose.foundation.lazy.items` را از ۷ فایل UI (که در این فاز
لمس نشدند) حذف کرده بود؛ چون آن import در واقع extension function مورد
نیاز DSL این‌است `LazyColumn`/`LazyRow` است، حذفش کل build را می‌شکست
(کامپایل با ده‌ها خطای Type mismatch شکست می‌خورد). این ۷ فایل به `HEAD`
بازگردانده شدند (بدون از دست رفتن داده — صرفاً بازگرداندن یک ویرایش
ناخواسته‌ی بیرون از این نشست) تا کامپایل خودِ تغییرات #۲۲ قابل‌تأیید شود.
هم‌زمان `PHP/config/permissions.json` از دیسک حذف شده بود (بدون ارتباط با
این نشست یا نشست‌های قبلی Phase3) — این مورد **دست‌نخورده گزارش شد**، نه
بازگردانی‌شده، چون علتش هنوز مشخص نیست.

تأیید شد: `./gradlew :app:compileDebugKotlin`، `:app:lintDebug`،
`:app:testDebugUnitTest` (شامل ۱۲ تست موجود `CargoViewModelTest`) همگی
سبز.

---

# Logging & Observability

## وضعیت

| منبع | مکان | ارزیابی |
|------|------|---------|
| `Logger` (PHP) | `src/Core/Logger.php` | ✅ singleton، سطح‌بندی‌شده |
| لاگ نشست | `logs/session_activity.log` | ✅ محافظت‌شده با `.htaccess` |
| گزارش کرش | `logs/crash_reports.log` | ⚠️ بدون rate limit |
| هشدار امنیتی | `SecurityAlerter.php` | ✅ عالی |
| چرخش لاگ | `scripts/rotate_logs.php` + `deploy/logrotate.d` | ✅ |
| Health check | `GET /api/v2/health` | ✅ |

`SecurityAlerter` یک نقطه‌ی قوت واقعی است — تشخیص reuse توکن refresh نه فقط لاگ می‌شود بلکه هشدار فعال تولید می‌کند (`SessionService.php:147-151`).

## مشکلات

- **[MEDIUM]** `Log.w`/`Log.e` در release حذف نمی‌شوند — به بخش امنیت مراجعه کنید.

## [MEDIUM] نبود مانیتورینگ خودکار

**Problem:** `health` endpoint وجود دارد اما هیچ نشانه‌ای از مصرف آن نیست. هیچ متریک، alerting، یا داشبوردی وجود ندارد. `crash_reports.log` یک فایل متنی است که کسی باید دستی بخواند.

**Impact:** خرابی‌ها تا زمانی که کاربر گزارش دهد کشف نمی‌شوند.

**Recommended Fix (حداقلی و کم‌هزینه):**

۱. یک cron هر ۵ دقیقه که `health` را بررسی و در صورت خطا ایمیل بفرستد.
۲. یک اسکریپت روزانه که تعداد خطوط جدید `crash_reports.log` را گزارش دهد.
۳. **توجه:** فیلتر User-Agent در `config/.htaccess` (که `curl` را بلاک می‌کند) باید ابتدا حذف شود وگرنه این کار نمی‌کند.

**Priority:** MEDIUM · **Effort:** Low

**وضعیت (Phase3 #28 — ✅ انجام شد، با دو انحراف عمدی از پیشنهاد بالا):**

- **`DiagnosticsController.php`**: منطق health-check به یک متد عمومی جدید
  `evaluateHealth()` استخراج شد (بدون `Response::json`)؛ `health()` همان را
  صدا می‌زند و پاسخ HTTP را می‌سازد. هدف: یک منبع واحد حقیقت برای «سالم
  بودن»، بدون تکرار منطق بین endpoint و اسکریپت CLI.
- **`scripts/health_monitor.php`** (جدید): به‌جای curl کردن `GET /health`
  (پیشنهاد بند ۱)، مستقیماً `DiagnosticsController::evaluateHealth()` را در
  CLI صدا می‌زند. **دلیل انحراف:** این کار نیاز به دستکاری فیلتر
  User-Agent در `config/.htaccess` (بند ۳ پیشنهاد) را کاملاً حذف می‌کند —
  آن فیلتر عمداً دست‌نخورده ماند چون نقشش (مسدودسازی bot/scraper) خارج از
  محدوده‌ی این مورد است و باز کردنش برای curl سطح حمله را افزایش می‌دهد.
  یک HTTP round-trip اضافه روی خودِ سرور هم صرفه‌جویی می‌شود.
- **`scripts/crash_report_summary.php`** (جدید): به‌جای ایمیل (بند ۱ و ۲
  پیشنهاد اصلی)، هر دو اسکریپت از کانال **Telegram** موجود
  (`SecurityAlerter`) استفاده می‌کنند که از قبل برای هشدارهای امنیتی
  به‌کار می‌رفت. **دلیل انحراف:** این پروژه هیچ SMTP/mail server پیکربندی‌شده‌ای
  ندارد (هاست اشتراکی)؛ استفاده از زیرساخت هشدار موجود به‌جای افزودن یک
  کانال جدید، هم ساده‌تر بود و هم با cooldown داخلی `SecurityAlerter`
  (۵ دقیقه، با dedupeKey مجزا برای هر رویداد) از اسپم جلوگیری می‌کند. بدون
  `SECURITY_ALERT_TELEGRAM_BOT_TOKEN/CHAT_ID` در `.env`، هر دو اسکریپت
  کاملاً no-op در بخش هشدار می‌مانند (فقط در stdout/stderr گزارش می‌دهند)
  — یعنی نصب این قابلیت هیچ رفتار فعلی را نمی‌شکند.
- شمارش کرش‌های جدید در `crash_report_summary.php` یک state file
  (`logs/.crash_summary_state`, در `.gitignore` از قبل پوشش داده‌شده با
  `/PHP/logs/*`) نگه می‌دارد و نسبت به truncate شدن فایل توسط
  `rotate_logs.php` مقاوم است (اگر تعداد خط فعلی از آخرین مقدار ثبت‌شده
  کمتر باشد، یعنی rotate رخ داده — شمارش از صفر شروع می‌شود، نه منفی).
- هر دو اسکریپت فقط CLI هستند (الگوی یکسان با `rotate_logs.php`/
  `export_schema.php`: بررسی `PHP_SAPI !== 'cli'`)؛ نصب واقعی cron روی
  سرور (که یک اقدام سمت سرور است، نه تغییر کد) در کامنت بالای هر اسکریپت
  مستند شده، مشابه الگوی موجود در `rotate_logs.php`.
- تأیید شد: `php -l` هر دو اسکریپت + کنترلر تغییریافته تمیز است، PHPStan
  بدون خطا (فایل‌های `scripts/` در دامنه‌ی `paths` آن نیستند، مثل
  `rotate_logs.php`)، `phpunit` ۶۸ تست سبز. هر دو اسکریپت به‌صورت دستی
  اجرا و بررسی شدند (حالت ناسالم با دیتابیس محلی نامعتبر، و حالت «بدون
  کرش جدید»).

---

# Dependency Audit

**ارزیابی: 7/10 — نسخه‌ها به‌روزند.**

| Dependency | نسخه | ارزیابی |
|-----------|------|---------|
| AGP | 8.13.0 | ✅ به‌روز |
| Kotlin | 2.2.20 | ✅ به‌روز |
| KSP | 2.2.20-2.0.3 | ✅ هم‌راستا با Kotlin |
| Compose BOM | 2025.09.00 | ✅ به‌روز |
| Retrofit | 3.0.0 | ✅ به‌روز |
| OkHttp | 5.1.0 | ✅ به‌روز |
| Room | 2.7.0 | ✅ |
| Coroutines | 1.10.2 | ✅ |
| **Koin** | **4.1.1** | ✅ Phase4 #36 |
| PHPUnit / PHPStan | (dev-only) | ✅ در production بارگذاری نمی‌شوند |

## [MEDIUM] Compose BOM عملاً بی‌اثر است

**File:** `gradle/libs.versions.toml` + `app/build.gradle.kts`

**Problem:**

```kotlin
implementation(platform(libs.compose.bom))      // BOM 2025.09.00
implementation(libs.androidx.ui)                // اما version.ref = "ui" = 1.9.1
implementation(libs.androidx.material3)         // version.ref = "material3" = 1.3.2
```

در `libs.versions.toml` هر کتابخانه‌ی Compose یک `version.ref` صریح دارد. **نسخه‌ی صریح همیشه بر BOM اولویت دارد**، پس BOM هیچ کاری نمی‌کند.

اتفاقاً `ui = 1.9.1` با BOM 2025.09.00 هم‌راستاست، پس در حال حاضر مشکلی پیش نمی‌آید. اما `material3 = 1.3.2` عقب‌تر از چیزی است که این BOM ارائه می‌دهد.

**Why it matters:** هدف BOM تضمین سازگاری نسخه‌های Compose با یکدیگر است. با override کردن همه‌ی آن‌ها، این تضمین از بین می‌رود و ارتقای آینده می‌تواند ترکیب ناسازگار تولید کند.

**Recommended Fix:**

`version.ref` را از کتابخانه‌های تحت پوشش BOM حذف کنید:

```toml
[libraries]
androidx-ui         = { module = "androidx.compose.ui:ui" }                    # بدون نسخه
androidx-material3  = { module = "androidx.compose.material3:material3" }      # بدون نسخه
androidx-foundation = { module = "androidx.compose.foundation:foundation" }    # بدون نسخه
```

**Priority:** MEDIUM · **Effort:** Low

**Status:** ✅ Fixed (2026-08-20) — `version.ref` از هر ۱۰ کتابخانه‌ی تحت پوشش BOM حذف شد (`androidx-ui`, `androidx-ui-graphics`, `androidx-ui-tooling`, `androidx-ui-tooling-preview`, `androidx-ui-test-junit4`, `androidx-ui-test-manifest`, `androidx-foundation`, `androidx-foundation-layout`, `androidx-material3`, `androidx-material-icons-extended`)؛ ۹ کلید `version` که دیگر مصرفی نداشتند هم از `[versions]` پاک شدند. `androidTestImplementation` در `app/build.gradle.kts` به `platform(libs.compose.bom)` نیاز داشت (این configuration جدا از `implementation` است و BOM را ارث‌بری نمی‌کرد) — اضافه شد، وگرنه `androidTestImplementation(libs.androidx.ui.test.junit4)` بدون نسخه resolve نمی‌شد. با `./gradlew :app:dependencies` تأیید شد که نسخه‌ها اکنون واقعاً از طریق BOM constraint (`(c)`) می‌آیند، نه پین دستی — مثلاً تناقض‌های `ui:1.0.1 -> 1.9.1` و `foundation:1.7.0 -> 1.9.1` توسط BOM حل می‌شوند. `compileDebugKotlin`، `lintDebug`، `testDebugUnitTest` سبز.

---

## [LOW] Koin 3.5.6

نسخه‌ی ۴.x در دسترس است و بهبودهایی در یکپارچگی Compose و پیام‌های خطا دارد.

**Recommended Fix:** ارتقا به Koin 4.x. مهاجرت عمدتاً مکانیکی است. با توجه به اینکه فقط یک ماژول DI وجود دارد (`AppModule.kt`، ۶۹ خط)، ریسک پایین است.

**Priority:** LOW · **Effort:** Low

**وضعیت (Phase4 #36 — ✅ انجام شد):**

نسخه‌ی موجود در npm/Maven بررسی شد (جست‌وجوی وب) — آخرین نسخه‌ی پایدار
`4.1.1` است (نه صرفاً «۴.x»). ارتقا واقعاً مکانیکی بود:

- `gradle/libs.versions.toml`: `koin = "3.5.6"` → `"4.1.1"` (هم
  `koin-android` و هم `koin-androidx-compose` از همین یک `version.ref`
  می‌آیند، پس هر دو با یک تغییر آپدیت شدند).
- تنها breaking change‌ی مرتبط با استفاده‌ی این پروژه: ViewModel DSL ماژول
  (`viewModel { ... }` در `di/AppModule.kt`) از
  `org.koin.androidx.viewmodel.dsl` به `org.koin.core.module.dsl` منتقل
  شده (import اصلاح شد). بقیه‌ی API‌های استفاده‌شده در پروژه
  (`startKoin`، `androidLogger`، `androidContext`/`androidApplication`،
  `koinViewModel`، `koinInject`، `KoinComponent`/`inject()`، افزونه‌ی
  کلاسیک `by viewModel()` در `MainActivity.kt`) بدون تغییر باقی ماندند —
  هیچ‌کدام deprecated/removed نشده بودند.
- پروژه فقط از دو artifact (`koin-android`, `koin-compose`) در ۳ ماژول
  (`app`, `feature:auth`, `feature:admin`) استفاده می‌کند — بدون
  `koin-test`/`koin-annotations`، پس ریسک واقعاً پایین بود، مطابق پیش‌بینی
  گزارش.
- تأیید شد: `./gradlew :app:compileDebugKotlin` (شامل کامپایل
  `feature:auth`/`feature:admin`)، `:app:lintDebug`، `:app:testDebugUnitTest`
  همگی سبز، بدون هشدار deprecation مرتبط با Koin.

---

# Testing Audit

**ارزیابی: 3/10 — ضعیف‌ترین بخش پروژه.**

## وضعیت موجود

**Kotlin (۹ فایل برای ۵۲٬۷۳۱ خط):**

```
app/src/test/.../ExampleUnitTest.kt                        ← الگوی پیش‌فرض
app/src/test/.../ReportsDomainCalculationsTest.kt          ✅ واقعی
app/src/test/.../ReportsDomainTest.kt                      ✅ واقعی
app/src/test/.../JalaliDateUtilsTest.kt                    ✅ واقعی
app/src/test/.../ui/viewmodel/CargoViewModelTest.kt         ✅ واقعی (Phase3 #20، ۲۰۲۶-۰۸-۲۰، ۱۲ تست)
core/network/src/test/.../TokenAuthenticatorTest.kt        ✅ واقعی و ارزشمند
core/network/src/test/.../TokenRefresherTest.kt            ✅ واقعی و ارزشمند
app/src/androidTest/.../ExampleInstrumentedTest.kt         ← الگوی پیش‌فرض
app/src/androidTest/.../CompactStatChipTest.kt             ✅
app/src/androidTest/.../ActionButtonTest.kt                ✅
```

**PHP (۵ فایل):**
```
tests/Unit/Services/SessionServiceTest.php        ✅ ۲۴۷ خط، با mock
tests/Unit/Services/QuotaCalculatorTest.php       ✅
tests/Unit/Services/LoginAttemptLimiterTest.php   ✅
tests/Unit/Services/PermissionServiceTest.php     ✅
tests/Unit/Validators/InputValidatorTest.php      ✅
```

نکته‌ی مثبت: `SessionService` عمداً برای تست‌پذیری طراحی شده (`SessionService.php:19` — تزریق اختیاری repository). این نشان می‌دهد تیم می‌داند چطور تست بنویسد.

**Phase3 #20 (`CargoViewModel`) — ✅ Fixed (2026-08-20)، با دو کشف واقعی حین اجرا:**

۱۲ تست نوشته شد که با mock کردن `ReportsRepository`/`UserPreferencesManager` (mockk) اجرا می‌شوند: `loadCargoInfoList` (موفقیت، خطای repository، تشخیص حواله‌ی تکراری)، `filterCargoInfoList`، `updateCargoConfirmation`، `dismissDuplicateDialog`، `updateSelectedShips`، صف پیام snackbar. حین نوشتن، اجرای واقعی تست‌ها ۲ مشکل معماری واقعی را آشکار کرد — نه فقط مشکل تست:

۱. **`Secrets.<clinit>` باعث `UnsatisfiedLinkError` در JVM می‌شد.** مسیر موفقیت `loadCargoInfoList` بدون قید‌وشرط یک coroutine فرزند برای گرفتن «تناژ قابل‌بارگیری» از `apiServiceV2` (singleton سراسری) اجرا می‌کند؛ خواندن آن به `System.loadLibrary("secrets")` می‌رسد که در JVM (بدون دستگاه) شکست می‌خورد — و چون `catch (e: Exception)` بود نه `catch (e: Throwable)`، این `Error` گرفته نمی‌شد و **کل `loadCargoInfoList` را کنسل می‌کرد**، نه فقط بخش تناژ را. تغییر به `catch (e: Throwable)` (`CargoViewModel.kt`) هم مشکل تست را حل کرد هم یک شکنندگی واقعی production را (هر `Error` غیرمنتظره در این بخش جانبی، قبلاً کل بارگذاری لیست حواله‌ها را می‌شکست).
۲. **race شرطی بین تست و `Dispatchers.IO` واقعی.** `loadCargoInfoList` تماس `repository.getCargoInfo` را با `withContext(Dispatchers.IO)` می‌پیچید — یک دیسپچر واقعی که `advanceUntilIdle()` تست نمی‌تواند با آن هماهنگ شود. یک پارامتر `ioDispatcher: CoroutineDispatcher = Dispatchers.IO` به سازنده‌ی `CargoViewModel` اضافه شد (پیش‌فرض بدون تغییر برای کد واقعی؛ تست‌ها همان `StandardTestDispatcher` را تزریق می‌کنند) و هر ۵ محل `Dispatchers.IO` داخل کلاس با آن جایگزین شد.

`compileDebugKotlin`، `lintDebug`، `testDebugUnitTest` (۳۳ تست کل، همگی سبز) تأیید شد. **خارج از دامنه ماند:** `submitCargoInfo`، `confirmCargo`، `toggleQuotaStatus` هنوز مستقیماً `apiServiceV2` سراسری را صدا می‌زنند (نه از طریق `repository`/`ioDispatcher`) — همان الگوی معماری‌ای که Phase3 #18 برای فایل‌های دیگر رفع کرد، این‌جا به همان دلیل باقی ماند تا دامنه‌ی تغییر کنترل‌شده بماند.

## [HIGH] CI هیچ تست اندرویدی اجرا نمی‌کند

**File:** `.github/workflows/ci.yml`

**Location:** job `android`، خطوط ۶۹–۸۰

```yaml
- name: Compile (debug)
  run: ./gradlew :app:compileDebugKotlin --console=plain

- name: Lint (debug)
  run: ./gradlew :app:lintDebug --console=plain

- name: Lint (release)
  run: ./gradlew :app:lintRelease --console=plain
```

**هیچ `./gradlew test` ای وجود ندارد.** پنج فایل تست واقعی Kotlin نوشته شده‌اند اما در CI **هرگز اجرا نمی‌شوند**.

**Why it matters:** تستی که اجرا نمی‌شود، تست نیست. `TokenAuthenticatorTest` و `TokenRefresherTest` منطق حساس امنیتی (چرخش توکن) را پوشش می‌دهند — دقیقاً چیزی که باید در هر commit محافظت شود.

**Recommended Fix:**

```yaml
      - name: Unit tests
        run: ./gradlew testDebugUnitTest --console=plain

      - name: Upload test reports
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: test-reports
          path: '**/build/reports/tests/'
          retention-days: 14
```

توجه: باید `test` را روی همه‌ی ماژول‌ها اجرا کنید نه فقط `:app`، چون تست‌های ارزشمند در `core:network` هستند.

**Priority:** HIGH · **Effort:** Low

**Status:** ✅ Fixed (2026-08-19) — مرحله‌ی `Unit tests` (بدون qualifier ماژول، پس همه‌ی زیرپروژه‌ها را شامل می‌شود) + آپلود گزارش به `ci.yml` اضافه شد. اجرای محلی `./gradlew testDebugUnitTest` تأیید کرد: هر ۶ فایل تست (شامل `TokenAuthenticatorTest` و `TokenRefresherTest` در `core:network`) با مجموع ۴۷ تست، صفر شکست.

---

## بخش‌های بحرانی بدون تست

| بخش | ریسک | چرا بحرانی است |
|-----|------|----------------|
| `CargoViewModel` (۹۶۲ خط) | **بالا** | منطق اصلی ثبت حواله و اعتبارسنجی کوتاژ |
| `QuotaService` (۷۱۵ خط PHP) | **بالا** | محاسبات تناژ — خطا = داده‌ی مالی نادرست |
| `CargoService` (۳۸۳ خط) | **بالا** | تراکنش‌ها و قفل‌گذاری |
| `ApiAuthGate` / `Router` | **بالا** | گیت امنیتی مرکزی |
| `UserController::updateUser` | **بالا** | منطق نامتقارن مجوز (خود/ادمین) |
| `UpdateManager` | متوسط | دانلود و تأیید هش APK |
| `CryptoManager` | متوسط | رمزنگاری توکن |

## Test Caseهای پیشنهادی (اولویت‌دار)

**۱. `Router` — امنیت مسیریابی**
```php
public function testRouteWithAuthTrueRejectsRequestWithoutSessionHeaders(): void
public function testRouteWithPermissionRejectsUserLackingIt(): void
public function testMoreSpecificRouteWinsRegardlessOfDeclarationOrder(): void   // quotas/filtered vs quotas/{n}
public function testUnknownPathReturns404AndWrongMethodReturns405(): void
```

**۲. `UserController::updateUser` — مرز مجوز**
```php
public function testNonAdminCannotUpdateAnotherUsersRecord(): void
public function testNonAdminCannotChangeOwnUserType(): void
public function testPasswordChangeRequiresCorrectCurrentPassword(): void
public function testActionInJsonBodyCannotEscalateToCreateUser(): void   // ← دفاع لایه‌ای
```

**۳. `QuotaService` — صحت محاسبات**
```php
public function testLoadableTonnageNeverExceedsQuotaCeiling(): void
public function testPercentageRestrictionAppliedWhenEnabled(): void
public function testDisabledQuotaReturnsZeroLoadable(): void
```

**۴. `CargoViewModel` — پس از refactor به DI**
```kotlin
@Test fun `submitCargoInfo blocks submission when quota is inactive`()
@Test fun `submitCargoInfo surfaces duplicate tracking numbers`()
@Test fun `network failure during submit emits error message not silent success`()
```

**۵. `UpdateManager` — امنیت به‌روزرسانی**
```kotlin
@Test fun `download is rejected when server omits sha256`()
@Test fun `download is deleted when sha256 does not match`()
@Test fun `download url outside allowed host is rejected`()   // پس از افزودن allow-list
```

---

# Code Quality

## Code Smells شناسایی‌شده

### God Files (۳۱ فایل > ۶۰۰ خط)

| فایل | خطوط | نوع |
|------|-----:|-----|
| `feature/cargo_entry/presentation/ActiveQuotasContent.kt` | ۱٬۴۲۰ | God Composable |
| `feature/reports/presentation/dialogs/CargoEditSearchDialogsSection.kt` | ۱٬۲۳۴ | God Composable |
| `security/SecurityScreen.kt` | ۱٬۱۱۲ | God Composable |
| `feature/reports/presentation/dialogs/QuotaAnalysisSection.kt` | ۱٬۰۶۲ | God Composable |
| `feature/admin/.../UserManagementScreen.kt` | ۱٬۰۵۷ | God Composable |
| `ui/screens/ManageReportsScreen.kt` | ۱٬۰۴۰ | God Composable |
| `feature/home/presentation/HomeScreen.kt` | ۹۶۸ | God Composable |
| `ui/viewmodel/CargoViewModel.kt` | ۹۶۲ | **God ViewModel** |
| `ui/viewmodel/ReportsViewModel.kt` | ۸۷۹ | **God ViewModel** |
| `PHP/src/Controllers/CargoController.php` | ۷۵۶ | God Controller |
| `PHP/src/Services/QuotaService.php` | ۷۱۵ | God Service |
| `PHP/src/Controllers/AnalyticsController.php` | ۶۹۹ | God Controller |
| `PHP/PermissionManager.php` | ۶۷۵ | PHP+HTML+JS در یک فایل |

### [MEDIUM] `CargoUiState` با ۱۶ فیلد

**File:** `app/src/main/java/com/atk/atk_cargo/ui/viewmodel/CargoViewModel.kt` خطوط ۶۸–۸۳

```kotlin
data class CargoUiState(
    val cargoInfoList: List<Cargo> = emptyList(),
    val filteredCargoInfoList: List<Cargo> = emptyList(),
    val scaleReceiptNumber: String = "",
    val clearInputFields: Boolean = false,
    val initialInfo: QuotaInfo? = null,
    val totalNetWeight: String = "",
    val isSubmitting: Boolean = false,
    val showNetWeightDialog: Boolean = false,
    val showDuplicateConfirmationDialog: Boolean = false,
    val duplicateWarningMessage: String = "",
    val loadableTonnage: String = "",
    val loadableTrucks18Wheeler: String = "",
    val loadableTrucks10Wheeler: String = "",
    val duplicateTrackingNumbers: List<String> = emptyList(),
    val showDuplicateDialog: Boolean = false,
    val selectedShipNames: Set<String> = emptySet()
)
```

به‌علاوه **۹ `MutableStateFlow` جداگانه** (خطوط ۹۶–۱۰۴) که خارج از این state هستند.

**Problem:**

۱. سه پرچم `showXDialog` مجزا — امکان باز شدن همزمان چند دیالوگ (حالت نامعتبر قابل نمایش).
۲. مقادیر عددی به‌صورت `String` از پیش قالب‌بندی‌شده (`loadableTonnage: String`) — منطق نمایش در ViewModel نشت کرده.
۳. هر تغییر در هر فیلد، کل `CargoUiState` را invalidate می‌کند.

**Recommended Fix:**

```kotlin
// حالت‌های غیرمجاز غیرقابل‌نمایش می‌شوند
sealed interface CargoDialog {
    data object None : CargoDialog
    data object NetWeight : CargoDialog
    data class DuplicateConfirmation(val message: String) : CargoDialog
    data class Duplicates(val trackingNumbers: List<String>) : CargoDialog
}

data class CargoUiState(
    val cargoInfoList: List<Cargo> = emptyList(),
    val searchQuery: String = "",                       // filtered را با derivedStateOf محاسبه کن
    val initialInfo: QuotaInfo? = null,
    val isSubmitting: Boolean = false,
    val dialog: CargoDialog = CargoDialog.None,
    val loadable: LoadableCapacity? = null,             // اعداد، نه String
    val selectedShipNames: Set<String> = emptySet()
)

data class LoadableCapacity(val tonnage: Float, val trucks18: Int, val trucks10: Int)
```

`filteredCargoInfoList` را حذف کنید — با `searchQuery` قابل استخراج است و نگهداری دو لیست، ریسک ناسازگاری دارد.

**Priority:** MEDIUM · **Effort:** High

**وضعیت (Phase4 #31 — ✅ انجام شد، با دامنه‌ی عمداً محدود):**

طبق تأیید کاربر، فقط مشکل ۱ (سه پرچم boolean مستقل دیالوگ) رفع شد؛ مشکل
۲ (اعداد به‌صورت String) و مشکل ۳ (حذف `filteredCargoInfoList` با
`derivedStateOf`) خارج از دامنه ماندند — هرکدام تغییر معماری جداگانه‌ای
هستند که باید مستقل بررسی شوند.

- `sealed interface CargoDialog` (در همان `CargoViewModel.kt`) اضافه شد:
  `None` / `NetWeight` / `DuplicateConfirmation(message)` /
  `Duplicates(trackingNumbers)`. `showNetWeightDialog`،
  `showDuplicateConfirmationDialog`، `duplicateWarningMessage`،
  `showDuplicateDialog`، `duplicateTrackingNumbers` (۵ فیلد) با یک فیلد
  `dialog: CargoDialog` جایگزین شدند.
- بررسی مصرف‌کننده‌ها نشان داد فقط **یک** فایل UI واقعاً این فیلدها را از
  `CargoViewModel` می‌خواند: `RegisterCargoScreen.kt` (۳ بلوک `if` مستقل →
  یک `dialog is/as? CargoDialog.X`). ۳ فایل دیگری که در جست‌وجوی اولیه
  یافت شدند (`CargoDetailsScreen.kt`، `InitialInfoScreen.kt`،
  `ShipInfoSection.kt`) یا اصلاً منبعشان این state نبود (یک
  `remember { mutableStateOf }` محلی و بی‌ربط در `InitialInfoScreen.kt`
  با نام مشابه) یا فقط پارامتر تابع بودند.
- نکته‌ی رفتاری تأییدشده پیش از تغییر: `duplicateTrackingNumbers` هم برای
  دیالوگ هم برای هایلایت ردیف‌های تکراری در لیست اصلی استفاده می‌شد؛ چون
  `dismissDuplicateDialog()` از قبل هر دو را هم‌زمان پاک می‌کرد، ادغام‌شان
  در یک state واحد هیچ رفتاری را تغییر نداد.
- `CargoViewModelTest.kt` (۱۲ تست از Phase3 #20): ۲ assertion که مستقیماً
  `showDuplicateDialog`/`duplicateTrackingNumbers` را چک می‌کردند به
  `assertEquals(CargoDialog.Duplicates(...), state.dialog)` تغییر یافتند.

تأیید شد: `compileDebugKotlin`، `lintDebug`، `testDebugUnitTest` (هر ۱۲
تست `CargoViewModelTest` سبز) بدون تغییر رفتار قابل‌مشاهده.

---

### [LOW] نام کاملاً واجد شرایط (FQN) درون بدنه‌ی کلاس

**Files:** `CargoViewModel.kt:90, 106`, `UserController.php:119, 123, 176, 238`

```kotlin
private val quotaValidationUseCase = com.atk.atk_cargo.feature.cargo.domain.QuotaValidationUseCase(repository)
private val snackbarQueue = com.atk.atk_cargo.feature.cargo.domain.CargoSnackbarQueue()
```

```php
$sessionService = new \App\Services\SessionService();
$isAdmin = (new \App\Services\PermissionService())->hasPermission(...);
```

**Problem:** معمولاً نشانه‌ی افزودن سریع یک وابستگی بدون فکر به جایگاه معماری آن است.

**Recommended Fix:** import کنید و از طریق DI تزریق کنید.

**Priority:** LOW · **Effort:** Low

---

### [LOW] دایرکتوری‌های خالی باقی‌مانده از ماژول‌بندی

```
app/src/main/java/com/atk/atk_cargo/feature/admin/presentation/       (خالی)
app/src/main/java/com/atk/atk_cargo/feature/auth/data/                (خالی)
app/src/main/java/com/atk/atk_cargo/feature/auth/domain/              (خالی)
app/src/main/java/com/atk/atk_cargo/feature/auth/navigation/          (خالی)
app/src/main/java/com/atk/atk_cargo/feature/auth/presentation/components/  (خالی)
app/src/main/java/com/atk/atk_cargo/feature/auth/viewmodel/           (خالی)
app/src/main/java/com/atk/atk_cargo/feature/chat/domain/              (خالی)
app/src/main/java/com/atk/atk_cargo/feature/chat/navigation/          (خالی)
app/src/main/java/com/atk/atk_cargo/feature/chat/presentation/components/  (خالی)
app/src/main/java/com/atk/atk_cargo/feature/startup/domain/           (خالی)
app/src/main/java/com/atk/atk_cargo/feature/startup/presentation/     (خالی)
```

اسکلت‌های خالی از انتقال `auth`، `admin`، `chat`، `startup` به ماژول‌های مستقل. بی‌ضرر اما گیج‌کننده (به‌ویژه در جستجوی IDE که نتایج تکراری نشان می‌دهد).

**Recommended Fix:** `git clean` یا حذف دستی.

**Priority:** LOW · **Effort:** Low

---

## نکته‌ی مثبت درباره‌ی کیفیت: کامنت‌گذاری

باید صریح گفته شود: **کامنت‌گذاری این پروژه بالاتر از متوسط صنعت است.** کامنت‌ها «چه کاری» را توضیح نمی‌دهند، بلکه **«چرا»** را مستند می‌کنند — و اغلب به تصمیم و trade-off اشاره می‌کنند. نمونه‌ها:

`network_security_config.xml` — توضیح کامل اینکه چرا پین روی CA است نه leaf:
> «گواهی leaf معمولاً هر چند ماه تمدید می‌شود؛ پین‌کردن آن باعث می‌شد هر تمدید leaf، برنامه را از سرور قطع کند»

`Request.php:101-106` — توضیح اینکه چرا فقط `REMOTE_ADDR`:
> «نه هدرهای HTTP_CLIENT_IP/HTTP_X_FORWARDED_FOR که کاملاً توسط کلاینت قابل جعل‌اند»

`RetrofitClient.kt:107-111` — توضیح اینکه چرا `lazy` عمدی است:
> «eager بودن این property باعث می‌شد سطح لاگ همیشه با مقدار پیش‌فرض ساخته شود»

این کیفیت مستندسازی، هزینه‌ی refactorهای پیشنهادی این گزارش را به‌طور محسوسی کاهش می‌دهد.

---

# Project Structure

## چه چیزی خوب است

- تفکیک `core/` و `feature/` الگوی درستی است.
- `core:domain` شامل `UserPreferencesStore` به‌عنوان **اینترفیس مرزی** — راه‌حل درستی برای اینکه ماژول‌های feature به `app` وابسته نشوند (`AppModule.kt:38-42` این را توضیح می‌دهد).
- ساختار `PHP/src/` (Core / Controllers / Services / Repositories / Validators / Enums / Exceptions) استاندارد و قابل پیش‌بینی است.
- `baselineprofile` به‌عنوان ماژول مستقل.

## چه چیزی بد است

**۸۱٪ کد هنوز در `app` است.**

```
app          42,584 خط   ← ۸۱٪
core (همه)    3,563 خط   ← ۷٪
feature (همه) 6,531 خط   ← ۱۲٪
```

featureهای بزرگ هنوز داخل `app` مانده‌اند:

```
app/src/main/java/com/atk/atk_cargo/feature/
├── reports/          ۲۷ فایل   ← بزرگ‌ترین feature، هنوز در app
├── home/              ۷ فایل
├── cargo/             ۴ فایل
├── cargo_entry/       ...
├── cargo_registration/ ...
├── cargo_counter/     ...
├── cargo_details/     ...
└── update/            ۱ فایل
```

**Impact:**
- زمان build: هر تغییر در هر فایل، کل ماژول `app` را recompile می‌کند.
- ماژول‌بندی نیمه‌کاره بدترین حالت است — هزینه‌ی پیچیدگی چند ماژول را می‌پردازید بدون بهره‌ی build incremental.

# Recommended Project Structure

```
ATK-Cargo/
├── app/                          ← فقط: Application، MainActivity، Navigation، DI aggregation
│                                    هدف: < 2,000 خط
├── core/
│   ├── common/                   ← utils، extension، Result/AppError
│   ├── designsystem/             ← تم، رنگ، تایپوگرافی، کامپوننت‌های پایه
│   ├── domain/                   ← مدل‌های دامنه، اینترفیس repository
│   ├── data/                     ← ★ جدید: پیاده‌سازی repository
│   ├── network/                  ← Retrofit، interceptor، مدیریت توکن
│   ├── database/                 ← Room
│   ├── security/                 ← ★ جدید: CryptoManager، SecurityVerifier
│   └── testing/                  ← ★ جدید: fake/fixture مشترک
├── feature/
│   ├── auth/         ✅ موجود
│   ├── startup/      ✅ موجود
│   ├── admin/        ✅ موجود
│   ├── chat/         ✅ موجود
│   ├── home/                     ← ★ انتقال از app
│   ├── cargo-entry/              ← ★ انتقال از app
│   ├── cargo-registration/       ← ★ انتقال از app
│   ├── cargo-details/            ← ★ انتقال از app
│   ├── cargo-counter/            ← ★ انتقال از app
│   ├── reports/                  ← ★ انتقال از app (بزرگ‌ترین)
│   └── update/                   ← ★ انتقال از app
└── baselineprofile/
```

**قانون وابستگی:**
```
app  →  feature/*  →  core/domain  ←  core/data  →  core/{network,database}
                              ↑
                      core/designsystem
```
هیچ ماژول `feature` نباید به `feature` دیگر یا به `app` وابسته باشد.

**ساختار پیشنهادی PHP:**

```
PHP/
├── public/                       ← ★ جدید: تنها document root
│   ├── index.php                 ← تنها نقطه ورود
│   └── .htaccess
├── src/                          ← خارج از document root
│   ├── Core/  Controllers/  Services/  Repositories/  Validators/
│   └── routes/
├── config/   migrations/   tests/   vendor/   logs/
```

انتقال document root به `public/` مهم‌ترین بهبود ساختاری سمت سرور است — با آن، هیچ فایل PHP ای جز `index.php` مستقیماً از وب قابل دسترسی نیست و کل دسته‌ی مشکلات «shimهای مستقیم» به‌صورت ساختاری حذف می‌شود.

---

# Technical Debt

| بدهی | اندازه | بهره‌ی مرکب |
|------|--------|-------------|
| ۹ Composable با تماس مستقیم شبکه | ۹ فایل | هر feature جدید الگو را کپی می‌کند |
| ماژول‌بندی نیمه‌کاره | ۴۲٬۵۸۴ خط در `app` (Phase4 #29: اولین feature مستخرج شد، جزئیات پایین) | زمان build با هر فایل جدید بدتر می‌شود |
| ۳۱ فایل > ۶۰۰ خط | ~۲۵٬۰۰۰ خط | recomposition گسترده‌تر، بازبینی کد سخت‌تر |
| پوشش تست ~۰٪ | ۵۲٬۷۳۱ خط | هر refactor ریسک رگرسیون دارد |
| fallback رمز متن‌خام | ۱ تابع | تا حذف نشود، ریسک امنیتی باقی است |
| shimهای مستقیم PHP | ۶ فایل | دو مدل امنیتی موازی |
| تاریخ/عدد در `varchar` | ۵ ستون | هر گزارش جدید باید با فرمت رشته‌ای کنار بیاید |
| ۳ شکل پاسخ خطا | سراسری | کد مدیریت خطای کلاینت شکننده |
| PHP EOL | زیرساخت | با هر CVE جدید بدتر می‌شود |

**Phase3 #21 — ✅ Fixed جزئی (۲۰۲۶-۰۸-۲۰)، با یک کشف مهم حین اجرا:**

بررسی نشان داد این «۳ شکل پاسخ خطا» تصادفی نیستند — یک کامنت تفصیلی از پیش از این نشست (`PHP/src/Core/AuthenticatesRequests.php`) صراحتاً مستند کرده بود که یکسان‌سازی این دو شکل **بدون تغییر هم‌زمان کلاینت** یکی از دو مصرف‌کننده‌ی واقعی را می‌شکند، و به همین دلیل عمداً حل‌نشده رها شده بود (با یک مکانیزم override به‌جای یکسان‌سازی).

به‌جای تکرار همان تصمیم (رها کردن)، هر endpoint هدف را جداگانه با کد کلاینت (`ReportsRepository.kt`/`UpdateManager.kt`/`SecurityVerifier.kt`/`CargoViewModel.kt`) مقابله دادم تا مشخص شود کدام‌ها واقعاً بی‌خطر تبدیل‌اند:

**تبدیل شد (`Response::json(['error' => ...])` خام → `Response::error()` استاندارد، شکل `{"success":false,"message":...}`):**
- `AnalyticsController::handleRealTimeLoadingData`/`handleQuotaRemaining` (۸ نقطه) — هر ۴ اکشن Router-routed آن (`analytics/kotazh`, `analytics/realtime`, `analytics/comprehensive`, `analytics/export-log`) بررسی شد؛ کلاینت یا اصلاً این اکشن‌ها را صدا نمی‌زند (`getKotazhInfo`، `handleQuotaRemaining`) یا فقط کد وضعیت/متن خام را می‌خواند، نه فیلد `error` را ساختاریافته.
- `AppApiController::handle()` (۲ نقطه) — تأیید شد این دیسپچر قدیمی دیگر از هیچ route/shim ای صدا زده نمی‌شود (Router مستقیماً متدهای عمومی را از طریق `$safeCall` در `routes/api_v2.php` صدا می‌زند)؛ کاملاً غیرقابل‌دسترس است.
- `UserController::updateFcmToken` (۴ نقطه) — تأیید شد کلاینت فعلی اصلاً FCM ندارد (صفر ارجاع در کل سورس Kotlin).
- `UtilityController::checkSignature` (۸ نقطه) — `SecurityVerifier.kt::authenticateSignatureWithServer` روی هر پاسخ ناموفق فقط `false` برمی‌گرداند، بدنه‌ی خطا را نمی‌خواند.
- `UtilityController::checkUpdate` (۳ نقطه، فقط خطاهای ۴۰۳/۵۰۰/۴۰۰ خودش) — `UpdateManager.kt` فقط شاخه‌ی ۴۲۶ (`enforceMinAppVersion`) را ساختاریافته می‌خواند؛ این سه فقط با کد وضعیت شناسایی می‌شوند.

**عمداً دست‌نخورده ماند (تأیید شد مصرف‌کننده‌ی زنده دارند):**
- `AppApiController::sendAuthErrorResponse` override + `$safeCall` در `routes/api_v2.php` — `ReportsRepository.kt:200` (`ErrorResponse.error`) دقیقاً همین شکل را برای `checkQuotaStatus` می‌خواند (کامنت خودِ کد هم این را صراحتاً تأیید می‌کند).
- `AnalyticsController::sendAuthErrorResponse` override — گیت‌های ۴۰۱/۴۰۳ که از داخل همان ۴ اکشن Router-routed صدا زده می‌شوند؛ محدوده‌ی بررسی‌شده فقط خطاهای کسب‌وکاری بود، نه این مسیر جدا.
- `CargoController` (`{"error":true/false,"message":"..."}`) — طبق تصمیم اولیه، این یک flag درون‌بدنه‌ای روی پاسخ ۲۰۰ موفق است (نه پاسخ خطای HTTP)، معنای متفاوتی دارد و `CargoViewModel.kt` دقیقاً همین را می‌خواند.
- `UtilityController::checkPassword`/`checkExistence` (`{"status":"exists"|"partial_match"|"not_exists"|"error",...}`) — یک قرارداد enum چندحالته‌ی عمدی برای این عملیات خاص، نه یک خطای دوحالته؛ تبدیلش به `success:false` باینری معنا را از بین می‌برد.

`php -l`، `phpstan` (سطح ۵؛ یک قانون `ignoreErrors` که دیگر match نمی‌شد چون خطای متناظرش با این تغییر برطرف شد، از `phpstan.neon` حذف شد)، `phpunit` (۶۸ تست) سبز. هیچ فایل Kotlin ای تغییر نکرد — هر ۲ نقطه‌ی خواندن ساختاریافته‌ی سمت کلاینت که پیدا شد (`ReportsRepository.kt`, `UpdateManager.kt`) دقیقاً به مسیرهایی اشاره داشتند که عمداً دست‌نخورده ماندند.

**نکته‌ی مهم:** این پروژه بدهی فنی را **مستند** می‌کند (کامنت‌های ارجاع‌دهنده به `DEEP_CODE_AUDIT.md`). این نشانه‌ی سلامت است، نه بیماری — تیم می‌داند بدهی کجاست.

**Phase4 #29 — ✅ اولین feature مستخرج شد (proof-of-concept)، دامنه‌ی عمداً محدود:**

بررسی نشان داد ۷ feature باقی‌مانده در `app` (۲۸٬۸۰۰+ خط، `reports` به‌تنهایی
۱۴٬۶۱۵ خط) هیچ‌کدام واقعاً مستقل نیستند — حتی کوچک‌ترین‌شان به کد اشتراکی‌ای
که هنوز در `app/data`/`app/domain` است وابسته‌اند. طبق تأیید کاربر، فقط
کوچک‌ترین/مستقل‌ترین مورد (`feature/cargo`، ۲ فایل) استخراج شد، به‌عنوان
proof-of-concept برای الگوی migration؛ بقیه برای نشست‌های بعدی می‌مانند.

- **`core/domain/repository/QuotaRepository.kt`** (جدید): اینترفیس مرزی
  جدید، دقیقاً هم‌الگوی `UserPreferencesStore` موجود (Phase5.10/5.12) —
  فقط دو متدی که `QuotaValidationUseCase` واقعاً لازم دارد
  (`checkQuotaStatus`, `getShipQuotas`)، نه کل `ReportsRepository`.
- **`ReportsRepository.kt`** (در `app` باقی می‌ماند): حالا `QuotaRepository`
  را پیاده‌سازی می‌کند (`override` روی دو متد).
- **ماژول جدید `feature:cargo`** (بدون Compose — هر دو کلاس خالص
  Kotlin/coroutines هستند، پس build سبک‌تر): `CargoSnackbarQueue.kt` و
  `QuotaValidationUseCase.kt` منتقل شدند؛ دومی حالا به `QuotaRepository`
  (اینترفیس) وابسته است، نه به کلاس مشخص `ReportsRepository`.
  `settings.gradle.kts`/`app/build.gradle.kts` به‌روزرسانی شدند.
- `CargoViewModel.kt`: instantiation با fully-qualified name خام
  (`com.atk.atk_cargo.feature.cargo.domain.CargoSnackbarQueue()`) به یک
  import تمیز تبدیل شد (نیازی به تغییر Koin نبود — این کلاس‌ها مستقیم
  instantiate می‌شوند، نه از طریق DI).

تأیید شد: `./gradlew :app:compileDebugKotlin` (شامل `feature:cargo` جدید)،
`:app:lintDebug`، `:app:testDebugUnitTest` (شامل ۱۲ تست `CargoViewModelTest`
که غیرمستقیم از این کد استفاده می‌کنند) همگی سبز.

---

# Production Readiness

| معیار | وضعیت | یادداشت |
|-------|:-----:|---------|
| Security — Auth/Session | ✅ | سطح production واقعی |
| Security — SQL/Injection | ✅ | صفر یافته |
| Security — Secrets | ❌ | **مسدودکننده** — XOR در git |
| Security — TLS/Pinning | ✅ | عالی |
| Security — ذخیره‌سازی روی دستگاه | ✅ | AES-GCM + Keystore |
| زیرساخت — نسخه PHP | ❌ | **مسدودکننده** — EOL |
| زیرساخت — phpMyAdmin | ❌ | **مسدودکننده** |
| Build — امضای release | ❌ | **مسدودکننده** — signingConfig ندارد |
| Build — ProGuard/R8 | ✅ | `fullMode` + `shrinkResources` |
| Build — بایگانی mapping | ✅ | `archiveReleaseMapping` |
| مدیریت کرش | ⚠️ | جمع‌آوری می‌شود، اما rate limit ندارد |
| Logging | ⚠️ | `Log.w`/`Log.e` در release می‌مانند |
| Monitoring / Alerting | ✅ | Phase3 #28 — `scripts/health_monitor.php` + `scripts/crash_report_summary.php` (Telegram)؛ نصب واقعی cron روی سرور هنوز لازم است |
| Testing | ❌ | CI تست اندروید اجرا نمی‌کند |
| تفکیک محیط | ⚠️ | `.env` استفاده می‌شود؛ اما base URL روی `test_api/` |
| بکاپ دیتابیس | ✅ | cron هر ۱۵ دقیقه (`db_backup.sh`) |
| مهاجرت دیتابیس | ⚠️ | migration هست، ردیابی نیست |

## [MEDIUM] آدرس پایه به مسیر `test_api/` اشاره می‌کند

**Files:** `app/src/main/cpp/secrets.cpp` (تابع `n0`)، `PHP/update_config.php` خط ۴۷

```
BASE_URL     = https://atk-nk.ir/Cargo/test_api/
download_url = https://atk-nk.ir/Cargo/test_api/downloads/app-release.apk
```

**Problem:** build ای که `versionName = "4.0.1"` دارد و به‌عنوان `app-release.apk` توزیع می‌شود، به مسیری با نام `test_api` متصل است.

**ارزیابی منصفانه:** این احتمالاً صرفاً یک نام‌گذاری بد برای مسیر production است، نه اینکه اپ واقعاً به محیط تست وصل باشد (چون `update_config.php` که در همان مسیر است، `latest_version` واقعی را سرو می‌کند).

اما این ابهام خودش یک ریسک است: هیچ راهی برای تشخیص build تست از production وجود ندارد.

**Recommended Fix:**

از `buildTypes` برای تفکیک محیط استفاده کنید:

```kotlin
buildTypes {
    debug   { buildConfigField("String", "API_BASE", "\"https://atk-nk.ir/Cargo/staging_api/\"") }
    release { buildConfigField("String", "API_BASE", "\"https://atk-nk.ir/Cargo/api/\"") }
}
```

و مسیر سرور را از `test_api/` به `api/` تغییر نام دهید (با نگه داشتن یک redirect موقت برای نصب‌های فعلی).

**Priority:** MEDIUM · **Effort:** Medium

---

# Prioritized Action Plan

## Phase 1 — Immediate (هفته‌ی ۱)

مسدودکننده‌های production. هیچ‌کدام refactor بزرگ نیست.

| # | اقدام | فایل | Effort |
|---|-------|------|--------|
| ۱ | ⚠️ چرخش `API_KEY` و `LICENSE_KEY` — ابزار آماده شد، اجرای واقعی روی سرور باقی مانده | `secrets.cpp` + `.env` سرور | Low |
| ۲ | ⚠️ حذف/محدودسازی phpMyAdmin — چک‌لیست آماده شد، اجرای روی سرور باقی مانده | سرور | Low |
| ۳ | ✅ افزودن `signingConfig` به release (تست شد؛ کیستور واقعی باقی مانده) | `app/build.gradle.kts` | Low |
| ۴ | ⚠️ پشت فلگ `ALLOW_LEGACY_PLAINTEXT_LOGIN` (پیش‌فرض خاموش) قرار گرفت — حذف کامل بعد از شمارش روی prod باقی مانده | `UserService.php:69-76` | Low |
| ۵ | ✅ رفع نشت `$e->getMessage()` | `UserController.php:83` | Low |
| ۶ | ✅ افزودن `testDebugUnitTest` به CI | `.github/workflows/ci.yml` | Low |
| ۷ | ✅ افزودن ایندکس `trackingNumber` (migration نوشته و تست شد؛ اجرا روی prod باقی مانده) | migration جدید | Low |
| ۸ | ✅ افزودن `php_server_info_*.txt` به `.gitignore` + حذف از tracking | `.gitignore` | Low |

**بررسی پس از فاز ۱:**
```bash
# تأیید ایندکس
mysql -e "EXPLAIN SELECT id FROM CargoInfo WHERE trackingNumber='X' ORDER BY entryTime DESC;"
# انتظار: type=ref، بدون Using filesort
```

## Phase 2 — High Priority (هفته‌های ۲–۴)

| # | اقدام | فایل | Effort |
|---|-------|------|--------|
| ۹ | ارتقای PHP به 8.3 | سرور | Medium |
| ۱۰ | ✅ رفع نشت کلید لایسنس در URL — مدل per-customer client-side دست‌نخورده ماند | `LicenseController` + `SecurityVerifier` | Low |
| ۱۱ | ✅ انتقال ۹ تماس شبکه از Composable به ViewModel (۹ از ۹ انجام شد) | ۹ فایل | High |
| ۱۲ | ✅ رفع انیمیشن‌ها با `graphicsLayer` | ۵ فایل | Low |
| ۱۳ | ✅ انتقال shimهای PHP به Router (مسیرهای موازی اضافه و ۶ shim قدیمی حذف شد) | ۶ فایل | Medium |
| ۱۴ | ✅ rate limit روی `diagnostics/crash` و لایسنس | `DiagnosticsController`، `LicenseController` | Low |
| ۱۵ | ✅ حذف `Log.w` در ProGuard | `proguard-rules.pro` | Low |
| ۱۶ | ✅ allow-list دامنه برای `downloadUrl` | `UpdateManager.kt` | Low |
| ۱۷ | ✅ پاک‌سازی تاریخچه‌ی git از رازها (محلی انجام شد؛ force-push به origin تأیید جدا نیاز دارد) | `git filter-repo` | Medium |

## Phase 3 — Medium Priority (ماه‌های ۲–۳)

| # | اقدام | Effort |
|---|-------|--------|
| ۱۸ | ✅ تزریق وابستگی در UseCaseها (۲ فایل کد مرده بودند و حذف شدند؛ ۱ فایل اصلاح شد) | Medium |
| ۱۹ | نوشتن تست برای `Router`، `UserController`، `QuotaService` | Medium |
| ۲۰ | ✅ نوشتن تست برای `CargoViewModel` (۱۲ تست + رفع ۲ مشکل معماری واقعی که حین تست کشف شد) | Medium |
| ۲۱ | ✅ یکسان‌سازی شکل پاسخ خطا (بخشی؛ جزئیات و استثناهای عمدی در بدنه‌ی گزارش) | Medium |
| ۲۲ | ✅ لایه‌ی متمرکز نگاشت خطا در کلاینت (دامنه محدود؛ رفع باگ CancellationException در ۹ فایل + AppError.kt برای آینده) | Medium |
| ۲۳ | انتقال `feature/reports` به ماژول مستقل | High |
| ۲۴ | جدول `schema_migrations` + اسکریپت migrate | Medium |
| ۲۵ | انتقال document root به `public/` | Medium |
| ۲۶ | ✅ حذف `version.ref` از کتابخانه‌های Compose BOM | Low |
| ۲۷ | تفکیک محیط با `buildConfigField` | Medium |
| ۲۸ | ✅ مانیتورینگ health + هشدار کرش | Low |

## Phase 4 — Optimization (ماه‌های ۴+)

| # | اقدام | Effort |
|---|-------|--------|
| ۲۹ | ✅ انتقال بقیه featureها به ماژول مستقل (شروع شد؛ فقط feature:cargo به‌عنوان proof-of-concept — ۶ feature دیگر باقی مانده) | High |
| ۳۰ | تفکیک ۳۱ فایل بزرگ | High |
| ۳۱ | ✅ بازطراحی `CargoUiState` با sealed dialog (فقط دیالوگ‌ها؛ String→Float و حذف filteredCargoInfoList باقی مانده) | High |
| ۳۲ | ستون‌های `DATETIME` موازی برای تاریخ | High |
| ۳۳ | ✅ متدهای HTTP صحیح (PATCH/DELETE) — تغییر هم‌زمان کلاینت+سرور | Medium |
| ۳۴ | ✅ مشروط‌سازی انیمیشن‌های بی‌نهایت + Reduce Motion (دامنه محدود؛ AnimationManager موجود به ۱۳ نقطه متصل شد) | Medium |
| ۳۵ | ارتقا به `targetSdk = 36` | Medium |
| ۳۶ | ✅ ارتقای Koin به 4.x (4.1.1) | Low |

---

# Top 20 Priority List

| # | Issue | Category | Severity | File | Effort |
|--:|-------|----------|----------|------|--------|
| ۱ | رازها با XOR تک‌بایتی، در git | Security | **CRITICAL** | `app/src/main/cpp/secrets.cpp:6` | Medium |
| ۲ | PHP 8.1 بدون پشتیبانی امنیتی | Security/Infra | **HIGH** | سرور production | Medium |
| ۳ | ⚠️ fallback رمز متن‌خام (پشت فلگ خاموش، حذف کامل باقی مانده) | Security | **HIGH** | `PHP/src/Services/UserService.php:69` | Low |
| ۴ | phpMyAdmin روی production | Security/Infra | **HIGH** | سرور production | Low |
| ۵ | ✅ تماس شبکه در Composable با scope کنسل‌شونده (۹ از ۹ فایل انجام شد) | Architecture | **HIGH** | `InitialInfoScreen.kt:636` + ۸ فایل | High |
| ۶ | ✅ نبود signingConfig برای release | Build | **HIGH** | `app/build.gradle.kts:49` | Low |
| ۷ | ✅ CI تست اندروید اجرا نمی‌کند | Testing | **HIGH** | `.github/workflows/ci.yml:69` | Low |
| ۸ | ✅ ایندکس گمشده روی `trackingNumber` | Performance/DB | **HIGH** | `PHP/src/Repositories/CargoRepository.php:299` | Low |
| ۹ | ✅ recomposition در هر فریم انیمیشن | Performance | **HIGH** | `InitialInfoDialogs.kt:81` + ۴ فایل | Low |
| ۱۰ | ✅ لایسنس بدون auth/rate-limit، کلید در URL (rate-limit + انتقال کلید به هدر انجام شد) | Security | MEDIUM | `PHP/src/Controllers/LicenseController.php:120` | Low |
| ۱۱ | ✅ نشت پیام استثنا به کلاینت | Security | MEDIUM | `PHP/src/Controllers/UserController.php:83` | Low |
| ۱۲ | ✅ shimهای PHP، Router را دور می‌زدند (۶ فایل حذف شد) | Architecture | MEDIUM | ۶ فایل ریشه `PHP/` | Medium |
| ۱۳ | `<Directory>` نامعتبر در `.htaccess` | Security/Config | MEDIUM | `PHP/.htaccess:20,70,75` | Low |
| ۱۴ | ✅ `downloadUrl` بدون اعتبارسنجی دامنه | Security | MEDIUM | `UpdateManager.kt:171` | Low |
| ۱۵ | ✅ `Log.w`/`Log.e` در release باقی می‌مانند | Security/Logging | MEDIUM | `proguard-rules.pro:179` | Low |
| ۱۶ | ✅ گزارش کرش بدون rate limit | Availability | MEDIUM | `DiagnosticsController.php:100` | Low |
| ۱۷ | ✅ UseCaseها singleton را مستقیم می‌گیرند (۲ فایل کد مرده حذف شد؛ ۱ فایل اصلاح شد) | Architecture/Testing | MEDIUM | `CheckQuotaUseCase.kt:8` + ۲ فایل | Medium |
| ۱۸ | ماژول‌بندی نیمه‌کاره (۸۱٪ در `app`) | Architecture | MEDIUM | ساختار پروژه | High |
| ۱۹ | ✅ Compose BOM با نسخه‌ی صریح override شده | Dependencies | MEDIUM | `gradle/libs.versions.toml` | Low |
| ۲۰ | تاریخ/عدد در `varchar(100)` | Database | MEDIUM | `PHP/schema.sql` | High |

---

# Final Score

| Category | Score | خلاصه |
| -------------------- | ----: | ----- |
| Security | 6/10 | پایه‌ی قوی؛ رازهای افشاشده و زیرساخت EOL نمره را پایین می‌آورند |
| Architecture | 4/10 | سرور خوب؛ کلاینت مرز لایه‌ها را در ۹ نقطه نقض می‌کند |
| Kotlin | 8/10 | تمیز، ایمن، structured concurrency صحیح |
| Jetpack Compose | 6/10 | مدیریت state عالی؛ God Composable و انیمیشن پرهزینه |
| Android | 7/10 | manifest و NSC عالی؛ signing و targetSdk عقب |
| PHP Backend | 8/10 | قوی‌ترین بخش — Router صریح، دفاع لایه‌ای |
| API Design | 6/10 | opt-in خوب؛ shimهای موازی و ۳ شکل خطا |
| Database | 7/10 | ایندکس و تراکنش عالی؛ یک ایندکس گمشده و نوع‌های نادرست |
| Performance | 6/10 | کش و ETag خوب؛ full scan و انیمیشن پرهزینه |
| Memory Management | 7/10 | نشتی یافت نشد |
| Error Handling | 6/10 | سرور منسجم؛ کلاینت پراکنده |
| Testing | 3/10 | تست‌های خوب نوشته شده اما در CI اجرا نمی‌شوند |
| Code Quality | 5/10 | کامنت‌گذاری عالی؛ ۳۱ فایل خدای‌گونه |
| Maintainability | 6/10 | مستندسازی «چرا» هزینه‌ی نگهداری را کم می‌کند |
| Scalability | 6/10 | تا رشد متوسط داده مشکلی نیست |
| Production Readiness | 5/10 | ۴ مسدودکننده، همگی قابل رفع در فاز ۱–۲ |

## **Overall Score: 6.1 / 10**

---

# Final Recommendations

## ارزیابی صادقانه

ATK-Cargo پروژه‌ای است که **کارهای سخت را انجام داده و کارهای آسان را جا انداخته**.

کار سخت — امنیت احراز هویت — به‌درستی انجام شده: چرخش توکن با تشخیص reuse، bcrypt با cost مناسب، certificate pinning با استدلال درست، رمزنگاری Keystore، کنترل همزمانی با `FOR UPDATE`. اینها چیزهایی هستند که معمولاً در پروژه‌های مشابه یافت نمی‌شوند.

کار آسان — چرخش یک کلید، افزودن یک ایندکس، افزودن یک خط به CI — جا مانده است.

این ترکیب غیرمعمول اما امیدوارکننده است: **فاصله تا production کوتاه است، چون بخش پرزحمت قبلاً تمام شده.**

## سه توصیه‌ی کلیدی

### ۱. رازها را همین امروز بچرخانید — سپس مدل را عوض کنید

کلید `atk_nk_9290VV42-38XQ02DI-F2WY4L2K-EJA7V682` را باید از همین لحظه افشاشده فرض کنید. چرخش آن یک کار ۳۰ دقیقه‌ای است.

اما چرخش کافی نیست. تا وقتی مدل «راز مشترک جاسازی‌شده در APK» است، هر کلید جدید هم در روز اول افشاشده است. **راز پایدار در کلاینت وجود ندارد** — نه با XOR، نه با AES، نه با NDK. تنها راه‌حل واقعی، انتقال اعتماد به سرور است: امضای سمت سرور با کلید عمومی در کلاینت.

### ۲. مرز لایه‌ها را ببندید — این باگ است، نه سلیقه

نه فایل Composable که مستقیماً `RetrofitClient` را صدا می‌زنند، صرفاً «کد ناتمیز» نیستند. آن‌ها با `rememberCoroutineScope()` عملیات **نوشتن** انجام می‌دهند — و آن scope با ناوبری کنسل می‌شود.

در یک سیستم که حواله‌ی بار کشتی ثبت می‌کند، «کاربر نمی‌داند ثبت شد یا نه» یک باگ عملیاتی است. اولویت آن باید بالاتر از یک بازسازی معماری معمولی باشد.

### ۳. تست‌هایی که دارید را اجرا کنید

`TokenAuthenticatorTest` و `TokenRefresherTest` منطق چرخش توکن — حساس‌ترین بخش امنیتی کلاینت — را پوشش می‌دهند. آن‌ها نوشته شده‌اند، کار می‌کنند، و **در CI هرگز اجرا نمی‌شوند**.

افزودن یک مرحله‌ی `./gradlew testDebugUnitTest` به `ci.yml` کمتر از ۱۰ دقیقه طول می‌کشد و بلافاصله ارزش کاری که قبلاً انجام شده را آزاد می‌کند. این بالاترین نسبت بازده به تلاش در کل این گزارش است.

## Roadmap عملی

```
هفته ۱      ├─ چرخش رازها + حذف phpMyAdmin + signingConfig
            ├─ حذف fallback رمز متن‌خام
            ├─ ایندکس trackingNumber
            └─ افزودن تست به CI
              ↓ خروجی: ۴ مسدودکننده‌ی production برطرف

هفته ۲–۴    ├─ ارتقای PHP 8.3
            ├─ بازطراحی لایسنس (challenge-response)
            ├─ انتقال ۹ تماس شبکه به ViewModel   ← بزرگ‌ترین کار
            ├─ graphicsLayer برای انیمیشن‌ها
            └─ انتقال shimها به Router
              ↓ خروجی: قابل انتشار به production

ماه ۲–۳     ├─ DI در UseCaseها → سپس تست‌نویسی
            ├─ یکسان‌سازی خطا (سرور + کلاینت)
            ├─ انتقال feature/reports به ماژول
            └─ مانیتورینگ
              ↓ خروجی: قابل نگهداری توسط تیم

ماه ۴+      ├─ تکمیل ماژول‌بندی
            ├─ تفکیک فایل‌های بزرگ
            └─ ستون‌های DATETIME + متدهای HTTP صحیح
              ↓ خروجی: قابل مقیاس
```

## معیار موفقیت

اگر فقط ۸ مورد فاز ۱ انجام شود، امتیاز از **6.1** به حدود **7.2** می‌رسد و هر چهار مسدودکننده‌ی production برطرف می‌شود — با تخمین **کمتر از یک هفته کار**.

اگر فاز ۲ نیز کامل شود، امتیاز به حدود **8.0** می‌رسد و پروژه به یک محصول production واقعاً آماده تبدیل می‌شود.

---

*این گزارش با بررسی مستقیم سورس کد تولید شده است. هر یافته با مسیر فایل و شماره خط قابل راستی‌آزمایی است. مواردی که قطعیت نداشتند صراحتاً با برچسب «Potential Issue» و شرح آنچه برای تأیید لازم است مشخص شده‌اند. مواردی که بررسی و رد شدند نیز در بخش «مواردی که بررسی شدند و مشکلی نداشتند» فهرست شده‌اند تا در بازبینی‌های بعدی وقت مضاعف صرف نشود.*
