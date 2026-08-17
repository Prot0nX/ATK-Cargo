# Deep Code Audit Report

**پروژه:** ATK-Cargo (Android + Kotlin + Jetpack Compose + PHP REST Backend + MySQL)
**تاریخ ممیزی:** 1405/05/26 (2026-08-17)
**دامنه:** کل repository — `app/` (160 فایل Kotlin، ~51,600 خط)، `PHP/` (81 فایل PHP خارج از vendor، ~11,000 خط)، `gradle/`، `baselineprofile/`
**نوع:** Static Deep Audit (بدون اجرای کد، بدون تغییر source)
**نسخه بررسی‌شده:** versionName `4.0.1` / versionCode `11` — commit `06d693f`

---

## Executive Summary

### وضعیت کلی

ATK-Cargo یک سیستم عملیاتی واقعی و در حال استفاده است (مدیریت حواله‌های بارگیری کشتی، کوتاژ، انبار، بارشماری) که **نشانه‌های واضحی از یک دور ممیزی قبلی و اصلاحات جدی دارد**: prepared statement در همه‌جا، Android Keystore برای توکن‌ها، Certificate Pinning، rate-limit ورود، audit trail، refresh token، گیت مجوز در سطح router. این‌ها کارهای درستی هستند و باید به رسمیت شناخته شوند.

اما پروژه در یک **وضعیت گذار نیمه‌تمام** گیر کرده است، و بیشترین ریسک فعلی دقیقاً از همین نیمه‌تمام بودن می‌آید:

1. **دو API stack موازی وجود دارد و کلاینت از stack ضعیف‌تر استفاده می‌کند.** یک Router نسخه ۲ کامل و امن (`PHP/src/routes/api_v2.php`، ۵۹۹ خط، opt-in، گیت auth/permission در سطح router) نوشته شده — اما **هیچ‌کدام از ۶۰+ متد `ApiService.kt` از آن استفاده نمی‌کنند**؛ همه از `protected_proxy.php?target=` (مدل opt-out) عبور می‌کنند. یعنی تمام بهبودهای امنیتی v2 عملاً کد مرده‌اند.
2. **مدل رمز عبور، ضعیف‌ترین حلقه‌ی کل سیستم است.** رمزها در UI فقط عددی و حداقل ۴ رقم هستند، کلاینت SHA-256 بدون salt می‌فرستد، و قفل تلاش ناموفق روی `username+IP` کلید می‌خورد — یعنی با چرخش IP کاملاً دور زده می‌شود.
3. **لایه‌ی Compose هیچ lifecycle-awareness ندارد** — ۸۵ مورد `collectAsState()` و صفر مورد `collectAsStateWithLifecycle()`.
4. **تست عملاً وجود ندارد** — ۱ تست PHP، ۱ تست واقعی Kotlin، در برابر ~۶۲,۰۰۰ خط کد.

### مهم‌ترین ریسک‌های امنیتی

| # | ریسک | شدت |
|---|------|-----|
| 1 | فضای رمز عبور ۴ رقمی عددی + SHA-256 بدون salt + قفل قابل دور زدن با چرخش IP → brute-force کامل حساب‌ها | **CRITICAL** |
| 2 | `protected_proxy.php` هیچ مرز امنیتی واقعی نیست؛ همه‌ی endpointها مستقیماً هم قابل فراخوانی‌اند (rate-limit و blocked-IP دور می‌خورند) | **HIGH** |
| 3 | session/refresh token به‌صورت plaintext در `user_sessions` ذخیره می‌شوند | **HIGH** |
| 4 | `PHP/vendor/` (شامل phpunit و phpstan) در web root و در گیت است | **HIGH** |
| 5 | `PermissionManager.php` بدون `session_regenerate_id()` و بدون قفل brute-force | **HIGH** |
| 6 | تأیید SHA-256 فایل APK آپدیت در صورت خالی بودن فیلد سرور، بی‌صدا رد می‌شود (fail-open) | **HIGH** |
| 7 | `app_api.php` کل داده‌ی گزارش‌گیری را بدون بررسی مجوز `view_reports` به هر کاربر احرازشده می‌دهد | **HIGH** |

### مهم‌ترین مشکلات Performance

- `collectAsState()` بدون lifecycle → جمع‌آوری Flowها (از جمله DataStore) در background ادامه دارد.
- ۲۲ مورد از ۳۵ فراخوانی `items(...)` در لیست‌های Lazy بدون `key` → از دست رفتن identity و recomposition/re-layout کامل.
- `PermissionService` فایل `permissions.json` را در **هر بررسی مجوز** از دیسک می‌خواند و json_decode می‌کند.
- جدول `user_sessions` با **۱۳ ایندکس** (چند تای‌شان prefix تکراری) در حالی که در هر درخواست احرازشده UPDATE می‌شود.
- ViewModelهای خدا با ۲۰+ `StateFlow` مجزا به‌جای یک `UiState` واحد.

### مهم‌ترین مشکلات Architecture

- دو API stack موازی (v1 proxy فعال / v2 router بلااستفاده).
- Koin به‌عنوان DI وجود دارد ولی `CargoViewModel` مستقیماً `RetrofitClient.apiService` را import می‌کند.
- `SelectInfoScreen.kt` با **۲۲۲۳ خط** و `CargoViewModel.kt` با **۹۸۰ خط** — God Composable / God ViewModel.
- منطق تبدیل تاریخ جلالی دو بار و با دو الگوریتم مستقل پیاده‌سازی شده.
- لایه‌ی Domain فقط در بعضی featureها وجود دارد؛ در بقیه ViewModel مستقیم به API می‌رود.

### Technical Debt

بدهی فنی غالب، **بدهی مهاجرت** است نه بدهی کیفیت: v2 نوشته شده ولی مصرف نمی‌شود؛ audit_log کد دارد ولی migration ندارد؛ `schema.sql` جداول `admin_chat_messages`/`admin_chat_reads`/`audit_log` را ندارد؛ `migrations/` خالی است.

### Production Readiness

**مشروط.** سیستم امروز کار می‌کند و در تولید است، اما با فضای رمز ۴ رقمی و قفل قابل دور زدن، **یک مهاجم با انگیزه‌ی متوسط می‌تواند ظرف چند ساعت به حساب admin برسد**. تا اصلاح Phase 1، وضعیت «آماده‌ی تولید» نیست.

### Overall Score

# **5.2 / 10**

---

## Project Overview

```
ATK-Cargo/
├── app/                          # Android application (Kotlin + Compose)
│   └── src/main/
│       ├── java/com/atk/atk_cargo/
│       │   ├── api/              # ApiService, Retrofit, AuthSession, Update, Notification (22 فایل)
│       │   ├── core/             # navigation, startup, ui/components, extensions
│       │   ├── data/             # db (Room), model, repository
│       │   ├── di/               # AppModule.kt (Koin)
│       │   ├── feature/          # admin, auth, cargo, cargo_counter, cargo_details,
│       │   │                     # cargo_entry, cargo_registration, chat, home,
│       │   │                     # reports, startup, update
│       │   ├── security/         # CryptoManager, SecurityVerifier, SecurityScreen
│       │   ├── ui/               # theme/ (18 فایل)، screens/، viewmodel/
│       │   ├── utils/            # JalaliDateUtils, SecurityUtils
│       │   └── workers/          # ChatNotificationWorker
│       ├── cpp/                  # secrets.cpp (JNI)
│       └── res/xml/              # network_security_config, file_path, backup_rules
├── baselineprofile/              # Macrobenchmark module
├── PHP/                          # Backend
│   ├── *.php                     # ۳۰ فایل entry نسخه‌ی ۱ (wrapper نازک روی کنترلرها)
│   ├── protected_proxy.php       # پروکسی v1 (?target=)
│   ├── PermissionManager.php     # پنل وب مدیریت دسترسی (۶۱۶ خط)
│   ├── api/v2/index.php          # Router v2 (?route=)
│   ├── src/
│   │   ├── Core/                 # Router, Request, Response, Database, Config, Logger,
│   │   │                         # ApiAuthGate, AuthenticatesRequests, MicroCache, MinVersionGate
│   │   ├── Controllers/          # 9 کنترلر
│   │   ├── Services/             # 10 سرویس
│   │   ├── Repositories/         # Cargo, Session, User
│   │   ├── Validators/           # InputValidator
│   │   └── routes/api_v2.php     # جدول route نسخه ۲
│   ├── config/permissions.json   # منبع حقیقت مجوزها (فایل، نه DB)
│   ├── migrations/               # ← خالی
│   ├── tests/                    # ← ۱ فایل
│   └── vendor/                   # ← کامیت‌شده در گیت (۱۱۶۲ فایل)
└── gradle/libs.versions.toml     # Version catalog
```

**آمار:**

| مورد | تعداد |
|---|---|
| فایل Kotlin | 160 |
| خط Kotlin (`app/src/main`) | 51,637 |
| توابع `@Composable` | 526 |
| فایل PHP (بدون vendor) | 81 |
| خط PHP (بدون vendor) | ~11,049 |
| تست Kotlin | 3 فایل (۲ تای آن stub تولیدشده) |
| تست PHP | 1 فایل |
| endpoint نسخه ۱ | ~30 |
| route نسخه ۲ | 46 |
| متد در `ApiService.kt` | 44 |

---

## Architecture Overview

### جریان واقعی داده (آنچه امروز اجرا می‌شود)

```
Compose Screen  (collectAsState — بدون lifecycle)
      ↓
ViewModel  (CargoViewModel / ReportsViewModel / AuthViewModel / StartupViewModel)
      ↓                                    ↘ (میان‌بر: import مستقیم RetrofitClient.apiService)
UseCase (فقط در auth و cargo)                ↓
      ↓                                      ↓
Repository (ReportsRepository / ChatRepository / AuthRepositoryImpl)
      ↓
ApiService (Retrofit)  ──►  OkHttp (+ headersInterceptor: X-Username / X-Device-Id /
      ↓                       X-Session-Token / X-App-Version, + TokenAuthenticator)
      ▼
protected_proxy.php?target=<file>.php      ← ۱۰۰٪ ترافیک کلاینت از این مسیر
      ↓  (include پویا)
<file>.php  →  Controller  →  AuthenticatesRequests::requireAuthenticatedSession()
      ↓                            ↓
   Service                   SessionService → SessionRepository → PDO → MySQL
      ↓
 Repository → mysqli/PDO → MySQL
```

### جریان طراحی‌شده ولی بلااستفاده

```
(هیچ کلاینتی) ──► api/v2/index.php?route=<path> ──► Router::dispatch
                                                       ↓
                                              ApiAuthGate::requireAuthenticated
                                              ApiAuthGate::requirePermission
                                                       ↓
                                              route handler → Controller
```
**تنها مصرف‌کننده‌ی v2 در کل کلاینت:** `TokenRefresher.kt:55` (`?route=auth/refresh`).

### نقاط مرزی پرریسک بین لایه‌ها

| مرز | مشکل |
|---|---|
| Compose ↔ ViewModel | ۲۰+ StateFlow مجزا به‌جای یک UiState؛ هر کدام یک subscription جدا |
| ViewModel ↔ Repository | `CargoViewModel` هر دو را استفاده می‌کند (repository + apiService مستقیم) |
| Repository ↔ Retrofit | خطاها در `Exception(String)` بسته‌بندی می‌شوند و نوع/کد وضعیت گم می‌شود |
| کلاینت ↔ سرور | دو قرارداد خطای متفاوت: `{error: true, message}` و `{error: "متن"}` |
| Controller ↔ Service | permission در بعضی مسیرها در کنترلر، در بعضی در router — و در `app_api.php` اصلاً نیست |
| Service ↔ فایل‌سیستم | `permissions.json` منبع حقیقت مجوزهاست، بدون قفل نوشتن و بدون کش خواندن |

---

## Overall Score

| Category | Score |
| -------------------- | ----: |
| Security             | 5/10 |
| Architecture         | 5/10 |
| Kotlin               | 6.5/10 |
| Jetpack Compose      | 5/10 |
| Android              | 6/10 |
| PHP Backend          | 6/10 |
| API Design           | 4/10 |
| Database             | 6/10 |
| Performance          | 6/10 |
| Memory Management    | 7/10 |
| Error Handling       | 5/10 |
| Testing              | 1.5/10 |
| Code Quality         | 5/10 |
| Maintainability      | 5/10 |
| Scalability          | 5/10 |
| Production Readiness | 5/10 |

**Overall: 5.2 / 10**

---
## Security Audit

### Critical Issues

---

### [CRITICAL] فضای رمز عبور ۴ رقمی عددی + هش بدون salt + قفل قابل دور زدن = تصاحب کامل حساب

**File:**
`app/src/main/java/com/atk/atk_cargo/feature/admin/presentation/UserManagementDialogsSection.kt`
`app/src/main/java/com/atk/atk_cargo/utils/SecurityUtils.kt`
`PHP/src/Services/LoginAttemptLimiter.php`
`PHP/src/Services/UserService.php`

**Location:**
`UserManagementDialogsSection.kt:200` و `:447` (فیلتر ورودی)، `:270` (حداقل طول)
`SecurityUtils.kt:10-19` (هش)
`LoginAttemptLimiter.php:50` (کلید قفل)
`UserService.php:59-67` (مقایسه و migration)

**Problem:**
سه ضعف مستقل که هرکدام به‌تنهایی MEDIUM هستند، در ترکیب یک آسیب‌پذیری CRITICAL می‌سازند:

۱. **فضای کلید ۱۰⁴.** فیلد رمز در هر دو دیالوگ ساخت و ویرایش کاربر ورودی را به رقم محدود می‌کند و حداقل را ۴ می‌گذارد:
```kotlin
// UserManagementDialogsSection.kt:200
onValueChange = { password = it.filter { char -> char.isDigit() } }
// UserManagementDialogsSection.kt:270
password.length < 4 -> errorMessage = "رمز عبور باید حداقل ۴ رقم باشد"
```
هیچ سقفی هم اعمال نمی‌شود، اما در عمل رمزها PIN عددی کوتاه‌اند. فضای جستجو برای یک PIN چهاررقمی **۱۰,۰۰۰ حالت** است.

۲. **قفل تلاش ناموفق با IP کلید می‌خورد و با چرخش IP بی‌اثر است.**
```php
// LoginAttemptLimiter.php:49-51
private function attemptsKey(string $username, string $ipAddress): string {
    return self::CACHE_PREFIX . strtolower($username) . '_' . $ipAddress;
}
```
کامنت بالای همین کلاس صراحتاً می‌گوید این کلاس اضافه شد چون rate-limit قبلی «با چرخش IP عملاً بدون محدودیت» بود — اما کلید جدید هنوز شامل IP است، پس دقیقاً همان دور زدن روی خودش هم کار می‌کند. مهاجم با یک IP جدید، شمارنده‌ای تازه دارد. ۱۰,۰۰۰ حالت ÷ ۵ تلاش = ۲۰۰۰ IP، که با یک proxy pool ارزان در چند ساعت قابل انجام است.

۳. **هش بدون salt و pass-the-hash.** کلاینت رمز خام را نمی‌فرستد؛ SHA-256 بدون salt می‌فرستد:
```kotlin
// SecurityUtils.kt:12-14 — بدون salt، بدون KDF، بدون iteration
val digest = MessageDigest.getInstance("SHA-256")
val bytes = digest.digest(password.toByteArray(Charsets.UTF_8))
```
سرور همان رشته را به‌عنوان «رمز» مصرف می‌کند: در مسیر legacy با `hash_equals` مستقیم مقایسه می‌کند و سپس **همان digest را** bcrypt می‌کند:
```php
// UserService.php:59-67
if (!hash_equals($storedPassword, $password)) { return null; }
$upgradedHash = password_hash($storedPassword, PASSWORD_BCRYPT, ['cost' => 12]);
```
نتیجه: راز مؤثر سیستم، digest است نه رمز. یعنی (الف) هر رکورد legacy که هنوز migrate نشده، مستقیماً به‌عنوان اعتبارنامه قابل استفاده است (pass-the-hash — نیازی به شکستن هش نیست)، و (ب) bcrypt با cost=12 روی فضای ۱۰,۰۰۰ تایی تقریباً بی‌معناست: کل فضا در چند دقیقه روی یک GPU/CPU معمولی قابل شمارش است.

**Why it matters:**
هر سه ضعف روی یک مسیر مشترک قرار دارند: `POST check_Auth.php` (و `api/v2/auth/login`). این endpoint بدون auth است (طبیعتاً)، پاسخ آن ۲۰۰ با `success:false` است (پس حتی کد وضعیت هم مانع اسکریپت‌نویسی نمی‌شود)، و هیچ CAPTCHA یا تأخیر تصاعدی ندارد.

**Impact:**
تصاحب حساب هر کاربر شناخته‌شده — و نام کاربری‌ها راز نیستند چون `users_api.php?action=getAllUsers` برای هر کاربر احرازشده کل فهرست کاربران را با `userType` برمی‌گرداند (بخش بعدی). با رسیدن به یک حساب `admin`: مدیریت کامل کاربران، حذف حواله، تغییر کوتاژ، دسترسی به کل داده‌ی عملیاتی و مالی.

**Recommended Fix:**
1. **فوری (کم‌هزینه):** کلید قفل را از IP جدا کنید — دو شمارنده‌ی مستقل:
```php
// قفل به‌ازای نام کاربری (مستقل از IP) — سدّ اصلی brute-force
private function userKey(string $username): string {
    return self::CACHE_PREFIX . 'u_' . strtolower($username);
}
// قفل به‌ازای IP — سدّ credential-stuffing گسترده
private function ipKey(string $ipAddress): string {
    return self::CACHE_PREFIX . 'ip_' . $ipAddress;
}
public function isLocked(string $u, string $ip): bool {
    return $this->count($this->userKey($u)) >= 5
        || $this->count($this->ipKey($ip)) >= 50;
}
```
همراه با تأخیر تصاعدی (مثلاً `sleep(min(2^n, 8))`) روی هر تلاش ناموفق.
2. **فوری:** حداقل طول را به ۸ و ورودی را به الفبا-عددی تغییر دهید (`UserManagementDialogsSection.kt:200,447`) و در سرور هم اعمال کنید (`InputValidator`) تا کلاینت دستکاری‌شده آن را دور نزند.
3. **کوتاه‌مدت:** `hashPassword` سمت کلاینت را حذف کنید و رمز خام را روی همان کانال TLS پین‌شده بفرستید؛ hashing باید فقط سمت سرور و با bcrypt/Argon2id روی **رمز خام** انجام شود. برای مهاجرت، ستون `password_scheme` اضافه کنید و در اولین ورود موفق هر کاربر، رکورد را به scheme جدید ارتقا دهید.
4. **کوتاه‌مدت:** تمام رکوردهای legacy (غیر `$2y$`/`$2a$`) را با یک reset اجباری منقضی کنید.

**Priority:** CRITICAL
**Estimated Effort:** Low (موارد ۱–۲) / Medium (موارد ۳–۴، نیاز به هماهنگی release کلاینت و سرور)

---

### High Issues

---

### [HIGH] `protected_proxy.php` یک مرز امنیتی نیست — همه‌ی endpointها مستقیماً هم قابل فراخوانی‌اند

**File:** `PHP/protected_proxy.php`
**Location:** خطوط ۶۳ (whitelist)، ۱۰۴–۱۶۴ (rate-limit)، ۲۰۲–۲۰۷ (blocked IP)، ۱۷۳–۱۹۷ (`include $target`)

**Problem:**
پروکسی سه کنترل امنیتی پیاده می‌کند: مسدودسازی IP، rate-limit ۶۰ درخواست/دقیقه، و لاگ دسترسی. اما تمام فایل‌های هدف — `app_api.php`، `check_Auth.php`، `users_api.php`، `chat_api.php`، `deleteCargoInfo.php` و بقیه — **خودشان فایل‌های PHP در همان web root هستند** و مستقیماً از طریق HTTP قابل فراخوانی‌اند. `.htaccess` هیچ قاعده‌ای برای مسدودسازی آن‌ها ندارد (و طبق کامنت خود پروژه در `.htaccess:52-60`، `mod_rewrite` روی این هاست اصلاً فعال نیست).

یعنی `POST /Cargo/test_api/check_Auth.php` دقیقاً همان کار `POST /Cargo/test_api/protected_proxy.php?target=check_Auth.php` را می‌کند، **بدون** rate-limit، **بدون** بررسی IP مسدود، و **بدون** ثبت در `proxy_access.log`.

علاوه بر این، whitelist با `glob('*.php')` ساخته می‌شود (opt-out):
```php
// protected_proxy.php:63
$this->whitelist = array_values(array_diff(glob('*.php') ?: [], PROXY_EXCLUDED_FILES));
```
هر فایل PHP جدیدی که در آینده به این پوشه اضافه شود، به‌طور خودکار یک endpoint قابل اجرا از راه دور می‌شود مگر اینکه کسی یادش بماند به `PROXY_EXCLUDED_FILES` اضافه کند. `glob()` هم نسبی است و به CWD فرایند PHP وابسته است.

**Why it matters:**
سه کنترل امنیتی که تیم فکر می‌کند فعال‌اند، در عمل اختیاری‌اند — یک مهاجم صرفاً با حذف `protected_proxy.php?target=` از URL آن‌ها را کنار می‌گذارد. این مستقیماً روی brute-force بالا اثر می‌گذارد: rate-limit پروکسی هیچ سدّی برای حمله به `check_Auth.php` نیست.

**Impact:**
حذف مؤثر rate-limiting و IP blocking در سطح کل API؛ افزایش سطح حمله با هر فایل PHP جدید؛ نبود لاگ دسترسی برای ترافیک مستقیم (یعنی حمله در `proxy_access.log` دیده نمی‌شود).

**Recommended Fix:**
مهاجرت کامل کلاینت به Router v2 (که opt-in است و از قبل نوشته شده)، سپس:
1. تمام فایل‌های entry نسخه ۱ را به یک پوشه‌ی خارج از web root منتقل کنید و فقط `api/v2/index.php` را قابل دسترس نگه دارید.
2. تا زمان مهاجرت، در `.htaccess` تمام `.php` را جز نقاط ورود مجاز مسدود کنید:
```apache
<FilesMatch "\.php$">
    Require all denied
</FilesMatch>
<FilesMatch "^(protected_proxy|index)\.php$">
    Require all granted
</FilesMatch>
```
3. rate-limit و IP-block را از پروکسی به یک نقطه‌ی مشترک (مثلاً `bootstrap.php` یا `ApiAuthGate`) منتقل کنید تا مستقل از مسیر ورود اعمال شود.

**Priority:** HIGH
**Estimated Effort:** Medium

---

### [HIGH] session token و refresh token به‌صورت plaintext در دیتابیس ذخیره می‌شوند

**File:** `PHP/src/Repositories/SessionRepository.php`
**Location:** خطوط ۲۳۰–۲۵۱ (`createSession`)، ۲۵۸–۲۸۲ (`rotateTokens`)، ۵۳–۶۸ (`isValidToken`)

**Problem:**
هر دو توکن دقیقاً همان مقداری که به کلاینت داده شده در ستون‌های `session_token` و `refresh_token` ذخیره می‌شوند و اعتبارسنجی با مقایسه‌ی مستقیم SQL انجام می‌شود:
```php
// SessionRepository.php:56-57
WHERE username = :username AND device_id = :device_id
  AND session_token = :token AND is_active = 1
```
توکن‌ها به‌درستی با `random_bytes(32)` تولید می‌شوند (`SessionService.php:113-115`) — یعنی مشکل در تصادفی‌بودن نیست، در **ذخیره‌سازی** است.

**Why it matters:**
یک SQL Injection در آینده، یک بکاپ لو رفته، دسترسی خواندنی یک اپراتور DB، یا حتی خطای پیکربندی phpMyAdmin ⇒ مهاجم توکن‌های زنده را مستقیماً دارد و می‌تواند بدون دانستن هیچ رمزی جای هر کاربر (از جمله admin) جا بزند. رمزها bcrypt شده‌اند اما توکن‌ها — که دقیقاً همان سطح دسترسی را می‌دهند — نشده‌اند. این یک ناهماهنگی در مدل تهدید است.

**Impact:**
تبدیل «افشای خواندنی دیتابیس» به «جعل هویت کامل و فوری همه‌ی کاربران فعال».

**Recommended Fix:**
هش SHA-256 توکن را ذخیره کنید (توکن ۲۵۶ بیتی تصادفی است، پس نیازی به bcrypt/salt نیست و کارایی lookup حفظ می‌شود):
```php
// هنگام ساخت/چرخش
':session_token' => hash('sha256', $accessToken),
':refresh_token' => hash('sha256', $refreshToken),

// هنگام اعتبارسنجی
$stmt->execute([':token' => hash('sha256', $token), ...]);
```
ایندکس `idx_session_token` روی مقدار هش‌شده هم دقیقاً همان کارایی را دارد. مهاجرت: یک migration که ستون‌ها را با `SHA2(session_token, 256)` به‌روزرسانی کند، یا ساده‌تر — همه‌ی نشست‌های فعال را در زمان deploy باطل کنید (`UPDATE user_sessions SET is_active = 0`).

**Priority:** HIGH
**Estimated Effort:** Low

---

### [HIGH] `PHP/vendor/` (شامل phpunit و phpstan) در web root و کامیت‌شده در گیت

**File:** `PHP/vendor/` (۱۱۶۲ فایل tracked)، `PHP/composer.json:24-27`، `.gitignore`

**Problem:**
`composer.json` این‌ها را به‌درستی به‌عنوان `require-dev` تعریف کرده:
```json
"require-dev": {
    "phpstan/phpstan": "^1.10",
    "phpunit/phpunit": "^9.6"
}
```
اما `vendor/` با تمام dev dependencyها در گیت است (`git ls-files PHP/vendor | wc -l` → **1162**) و `.gitignore` فقط سه فایل منفرد از آن را نادیده می‌گیرد. چون کل پوشه‌ی `PHP/` همان web root است، این یعنی `phpunit`، `phpstan`، `nikic/php-parser`، `sebastian/*` و باینری‌های `vendor/bin/` روی سرور تولید مستقر و از طریق HTTP قابل دسترس‌اند. هیچ `vendor/.htaccess` وجود ندارد.

**Why it matters:**
پوشه‌ی `vendor` در web root یک الگوی کلاسیک RCE است. نسخه‌ی فعلی phpunit (9.6) فایل آسیب‌پذیر تاریخی `eval-stdin.php` را ندارد (تأیید شد: `find vendor -iname "eval-stdin*"` نتیجه‌ای نداشت، پس CVE-2017-9841 اینجا صدق نمی‌کند) — اما این یک ضمانت پایدار نیست: هر به‌روزرسانی یا افزودن یک dev dependency جدید می‌تواند فایل قابل اجرای جدیدی وارد web root کند، بدون اینکه کسی متوجه شود. علاوه بر این، فایل‌های `vendor/composer/installed.json` نسخه‌ی دقیق تمام کتابخانه‌ها را برای شناسایی آسیب‌پذیری افشا می‌کنند.

**Impact:**
افزایش قابل‌توجه سطح حمله؛ افشای فهرست نسخه‌ی dependencyها؛ ریسک RCE در صورت افزوده شدن هر کتابخانه‌ی دارای فایل قابل اجرا در آینده؛ حجم اضافی repository.

**Recommended Fix:**
1. `PHP/vendor/` را به `.gitignore` اضافه و از گیت حذف کنید (`git rm -r --cached PHP/vendor`).
2. در deploy فقط `composer install --no-dev --optimize-autoloader` اجرا شود.
3. Document root وب‌سرور را به یک زیرپوشه‌ی `public/` منتقل کنید و `src/`، `vendor/`، `config/`، `tests/` را کاملاً خارج از آن نگه دارید. اگر این ممکن نیست، حداقل:
```apache
# PHP/vendor/.htaccess
Require all denied
```

**Priority:** HIGH
**Estimated Effort:** Low (htaccess) / Medium (بازساختاردهی به public/)

---

### [HIGH] `PermissionManager.php`: نبود `session_regenerate_id` و نبود قفل brute-force

**File:** `PHP/PermissionManager.php`
**Location:** خطوط ۳۸–۵۰ (login)، ۵۳–۵۷ (logout)، ۱۵ (`session_start`)

**Problem:**
پنل وب مدیریت مجوزها چند کار را درست انجام می‌دهد (CSRF با `hash_equals`، رمز ادمین با `password_verify` روی bcrypt، انقضای ۳۰ دقیقه‌ای). اما:

۱. **Session Fixation:** پس از ورود موفق، شناسه‌ی نشست بازتولید نمی‌شود:
```php
// PermissionManager.php:43-47
} elseif (password_verify($_POST['password'], $ADMIN_PASSWORD_HASH)) {
    $_SESSION['perm_manager_auth'] = true;   // ← بدون session_regenerate_id(true)
    $_SESSION['last_activity'] = time();
    header("Location: PermissionManager.php");
```
۲. **بدون قفل تلاش ناموفق:** برخلاف `LoginAttemptLimiter` که برای API نوشته شده، این مسیر هیچ شمارنده‌ای ندارد — رمز ادمین بدون هیچ محدودیتی قابل brute-force است.
۳. **بدون تنظیم امن کوکی نشست:** هیچ `session_set_cookie_params(['httponly'=>true,'secure'=>true,'samesite'=>'Lax'])` قبل از `session_start()` وجود ندارد.
۴. **`session_destroy()` بدون پاک کردن `$_SESSION` و کوکی** (خط ۵۴) — داده‌ی نشست در همان درخواست زنده می‌ماند.

**Why it matters:**
این پنل **منبع حقیقت تمام مجوزهای سیستم** است (`config/permissions.json` را می‌نویسد که `PermissionService` در هر بررسی مجوز می‌خواند). تصاحب آن یعنی توانایی دادن `manage_users`/`manage_quotas`/`delete_cargo` به هر کاربری. `file_manager.php` هم از همان `$_SESSION['perm_manager_auth']` استفاده می‌کند.

**Impact:**
Privilege escalation کامل در سطح برنامه.

**Recommended Fix:**
```php
// قبل از session_start()
session_set_cookie_params([
    'lifetime' => 0, 'path' => '/', 'secure' => true,
    'httponly' => true, 'samesite' => 'Lax',
]);
session_start();

// در مسیر ورود موفق
} elseif (password_verify($_POST['password'], $ADMIN_PASSWORD_HASH)) {
    session_regenerate_id(true);          // ← اضافه شود
    $_SESSION['perm_manager_auth'] = true;
    $_SESSION['last_activity'] = time();

// در مسیر خروج
$_SESSION = [];
if (ini_get('session.use_cookies')) {
    $p = session_get_cookie_params();
    setcookie(session_name(), '', time() - 42000, $p['path'], $p['domain'], $p['secure'], $p['httponly']);
}
session_destroy();
```
و `LoginAttemptLimiter` موجود را با کلید `'permmgr_' . $ip` در همین مسیر استفاده کنید (بدون نیاز به کد جدید).

**Priority:** HIGH
**Estimated Effort:** Low

---

### [HIGH] تأیید هش APK آپدیت fail-open است

**File:** `app/src/main/java/com/atk/atk_cargo/api/UpdateManager.kt`
**Location:** خطوط ۳۳۴–۳۴۰

**Problem:**
```kotlin
val expectedSha256 = _updateInfo.value?.sha256.orEmpty()
if (expectedSha256.isNotEmpty() && !verifyFileSha256(currentDownloadFile, expectedSha256)) {
    currentDownloadFile.delete()
    _downloadState.value = DownloadState.Error("فایل دانلودشده معتبر نیست (عدم تطابق هش)")
} else {
    _downloadState.value = DownloadState.Completed
}
```
اگر سرور فیلد `sha256` را نفرستد یا خالی بفرستد، شرط اول `false` می‌شود، شاخه‌ی `else` اجرا می‌شود و فایل **بدون هیچ تأییدی** به‌عنوان `Completed` علامت می‌خورد و در `installUpdate` به `ACTION_VIEW` تحویل داده می‌شود.

`update_config.php:11-32` یک `getFileSha256()` با کش دارد، پس در حالت عادی این فیلد پر است — اما یک خطای پیکربندی، یک `filemtime` ناموفق، یا هر تغییری در فرمت پاسخ، بی‌صدا تأیید یکپارچگی را خاموش می‌کند.

**Why it matters:**
این تنها تأیید یکپارچگی سطح برنامه روی یک باینری اجرایی است. Certificate Pinning در برابر MITM محافظت می‌کند و امضای APK اندروید مانع نصب APK با کلید متفاوت می‌شود — اما هیچ‌کدام در برابر «فایل خراب/ناقص/جایگزین‌شده روی خود سرور» محافظت نمی‌کنند. یک بررسی امنیتی که می‌تواند بی‌صدا خاموش شود، یک بررسی امنیتی نیست.

**Impact:**
نصب یک APK تأییدنشده روی دستگاه کاربر (نیازمند دسترسی به سرور یا خرابی فایل). مقدار `REQUEST_INSTALL_PACKAGES` در مانیفست این مسیر را عملی می‌کند.

**Recommended Fix:**
fail-closed کنید:
```kotlin
val expectedSha256 = _updateInfo.value?.sha256.orEmpty()
if (expectedSha256.isEmpty()) {
    currentDownloadFile.delete()
    _downloadState.value = DownloadState.Error("امکان تأیید یکپارچگی فایل وجود ندارد؛ به‌روزرسانی لغو شد.")
    return
}
if (!verifyFileSha256(currentDownloadFile, expectedSha256)) {
    currentDownloadFile.delete()
    _downloadState.value = DownloadState.Error("فایل دانلودشده معتبر نیست (عدم تطابق هش)")
    return
}
_downloadState.value = DownloadState.Completed
```
و در سرور، اگر `hash_file` شکست خورد، اصلاً `download_url` را برنگردانید.

**Priority:** HIGH
**Estimated Effort:** Low

---

### [HIGH] `app_api.php` تمام داده‌ی گزارش‌گیری را بدون بررسی مجوز `view_reports` می‌دهد

**File:** `PHP/src/Controllers/AppApiController.php`
**Location:** خطوط ۹۱–۱۹۸ (تمام `case`های خواندنی)

**Problem:**
در `AppApiController::handle()` فقط actionهای نوشتنی گیت مجوز دارند:
```php
case 'updateTemporaryTonnage':
    $this->requirePermission('manage_quotas');   // ✓
```
اما هیچ‌کدام از actionهای خواندنی — `getShipsList`، `getShipDetails`، `getWarehouseDetails`، `getQuotaDetails`، `getQuotasList`، `getFilteredQuotas`، `getFilteredSummary`، `getGroupedQuotas`، `getLoadableTonnage`، `getRealTimeData` — هیچ `requirePermission` ندارند. تنها گیت، `requireAuthenticatedSession()` است.

این با مسیر معادل در Router v2 ناسازگار است:
```php
// routes/api_v2.php:584-589 — همین داده، ولی با مجوز
['method'=>'GET','path'=>'analytics/realtime','auth'=>true,'permission'=>'view_reports', ...]
```
و همچنین با `AnalyticsController` که برای هر ۴ اکشن `view_reports` را چک می‌کند.

**Why it matters:**
یک Broken Function Level Authorization واقعی: کاربری با نقش «بارشمار» یا «اپراتور» که مجوز `view_reports` ندارد، با یک درخواست ساده به `app_api.php?action=getShipsList` دقیقاً همان داده‌ای را می‌گیرد که مسیر گزارش‌ها پشت `view_reports` قفل کرده. کنترل دسترسی فقط در UI اعمال می‌شود، نه در سرور.

**Impact:**
افشای کل داده‌ی عملیاتی و تجاری (کشتی‌ها، کوتاژها، تناژ، حواله‌ها، آمار انبار) به هر کاربر احرازشده، صرف‌نظر از نقش.

**Recommended Fix:**
یک گیت خواندنی مشترک اضافه کنید — دقیقاً هم‌راستا با تصمیمی که در `api_v2.php` گرفته شده:
```php
private const READ_ACTIONS_REQUIRING_REPORTS = [
    'getShipsList', 'getShipDetails', 'getWarehouseDetails', 'getQuotaDetails',
    'getQuotasList', 'getFilteredQuotas', 'getFilteredSummary', 'getGroupedQuotas',
    'getRealTimeData',
];
// بعد از requireAuthenticatedSession و تعیین $action:
if (in_array($action, self::READ_ACTIONS_REQUIRING_REPORTS, true)) {
    $this->requirePermission('view_reports');
}
```
سپس همان مجوز را در routeهای `ships/*` و `quotas/*` در `api_v2.php` هم از `null` به `'view_reports'` تغییر دهید تا دو مسیر واقعاً یکسان باشند. **قبل از deploy** بررسی کنید که نقش‌های موجود در `config/permissions.json` که امروز از این صفحات استفاده می‌کنند این مجوز را داشته باشند، وگرنه کاربران فعلی قفل می‌شوند.

**Priority:** HIGH
**Estimated Effort:** Low (کد) / Medium (بازبینی نقش‌ها)

---

### [HIGH] «رازها» با XOR تک‌بایتی محافظت می‌شوند و سورس آن‌ها در گیت است

**File:** `app/src/main/cpp/secrets.cpp`، `app/src/main/java/com/atk/atk_cargo/api/Secrets.kt`
**Location:** `secrets.cpp:6` (کلید)، `:8-14` (تابع رمزگشایی)، `:16+` (هر تابع `n0`..`n10`)

**Problem:**
```cpp
const uint8_t XOR_KEY = 0x5A;
std::string decryptXor(const uint8_t* encryptedBytes, size_t length) {
    for (size_t i = 0; i < length; ++i) decrypted += (char)(encryptedBytes[i] ^ XOR_KEY);
}
```
هفت مقدار حساس از این مسیر می‌آیند: `getBaseUrl`، `getExpectedSignatureHash`، `getSignatureCheckUrl`، `getLicenseCheckUrl`، `getLicenseInfoUrl`، `getLicenseKey`، `getApiKey`. XOR با یک بایت ثابت رمزنگاری نیست — obfuscation است، و ضعیف‌ترین نوع آن: با یک اسکریپت چند خطی روی `libsecrets.so` (یا حتی با `xortool`) در چند ثانیه بازیابی می‌شود.

مهم‌تر: **آرایه‌های بایت به‌همراه کلید، مستقیماً در repository کامیت شده‌اند.** هرکسی که به گیت دسترسی دارد (یا هر نشتی از repository) این مقادیر را بدون هیچ تلاشی دارد. تأیید شد که مقادیر واقعاً قابل بازگشایی‌اند (مثلاً `n0` به base URL برنامه رمزگشایی می‌شود).

`UPDATE_CHECK_API_KEY` در `.env.example` هم صراحتاً اشاره می‌کند که باید با `Secrets.getApiKey()` یکسان باشد — یعنی یک راز مشترک بین کلاینت و سرور که در کلاینت قابل استخراج است.

**Why it matters:**
هر رازی که در یک اپلیکیشن موبایل توزیع‌شده قرار بگیرد، در نهایت راز نیست — این یک اصل است، نه یک ایراد پیاده‌سازی. اما تفاوت مهم این است: مدل امنیتی فعلی طوری نوشته شده که **انگار** این‌ها راز هستند. `EXPECTED_SIGNATURE_HASH` که در `SecurityVerifier.kt:148` با امضای واقعی مقایسه می‌شود، به‌سادگی قابل پیدا کردن و patch شدن است؛ `LICENSE_KEY` قابل استخراج و استفاده در یک کلاینت دلخواه است.

**Impact:**
دور زدن کامل بررسی امضا/لایسنس (که هدف اصلی `SecurityVerifier` است)؛ افشای API key بررسی به‌روزرسانی.

**Recommended Fix:**
1. **تغییر ذهنیت:** هیچ کنترل امنیتی مهمی نباید به این مقادیر تکیه کند. `SecurityVerifier` را به‌عنوان یک لایه‌ی «افزایش هزینه‌ی حمله» ببینید، نه یک کنترل دسترسی. کنترل واقعی باید سمت سرور و مبتنی بر نشست/مجوز باشد (که خوشبختانه از قبل وجود دارد).
2. `secrets.cpp` را از گیت خارج کنید و در build از یک فایل تولیدشده در زمان build (از متغیر محیطی CI یا `local.properties`) بسازید.
3. کلید XOR تک‌بایتی را با یک طرح مشتق‌شده جایگزین کنید (مثلاً AES-GCM با کلیدی که خود از چند منبع runtime مشتق می‌شود) — این حمله را کند می‌کند، ولی حل نمی‌کند.
4. `UPDATE_CHECK_API_KEY` را به‌عنوان یک راز واقعی حساب نکنید؛ `check_update.php` را idempotent و بی‌ضرر نگه دارید (که هست).

**Priority:** HIGH
**Estimated Effort:** Medium

---

### Medium Issues

---

### [MEDIUM] `getAllUsers` فهرست کامل کاربران را به هر کاربر احرازشده می‌دهد

**File:** `PHP/src/Controllers/UserController.php`، `PHP/src/Repositories/UserRepository.php`
**Location:** `UserController.php:30-36` (فهرست ADMIN_ONLY)، `:82-85`، `UserRepository.php:42`

**Problem:**
`getAllUsers` عمداً در `ADMIN_ONLY_ACTIONS` نیست (طبق کامنت، چون صفحه‌ی «تنظیمات پروفایل» از آن استفاده می‌کند)، و کوئری زیر را برای هر کاربر احرازشده اجرا می‌کند:
```php
SELECT id, username, fullName, userType, created_at, updated_at FROM Users ORDER BY created_at DESC
```
یعنی هر اپراتور، فهرست کامل نام کاربری‌ها، نام واقعی و **نقش** همه را می‌بیند.

**Why it matters:**
این مستقیماً حمله‌ی brute-force بالا را عملی می‌کند: مهاجم لازم نیست نام کاربری admin را حدس بزند، `userType='admin'` را در همین پاسخ می‌بیند. Excessive Data Exposure کلاسیک (OWASP API3).

**Impact:**
شمارش کاربران و شناسایی هدف‌های با ارزش بالا.

**Recommended Fix:**
یک action جدید `getSelfProfile` بسازید که فقط رکورد `$this->authenticatedUsername` را برمی‌گرداند، صفحه‌ی پروفایل را به آن منتقل کنید، و `getAllUsers` را به `ADMIN_ONLY_ACTIONS` اضافه کنید. این هم‌زمان مشکل ناکارآمدی `UserController.php:159-160` را حل می‌کند (که برای پیدا کردن رکورد خود کاربر، **کل جدول کاربران** را می‌خواند و در PHP فیلتر می‌کند).

**Priority:** MEDIUM
**Estimated Effort:** Low

---

### [MEDIUM] تغییر رمز عبور بدون تأیید رمز فعلی

**File:** `PHP/src/Controllers/UserController.php`
**Location:** خطوط ۱۴۶–۱۸۵ (`case 'updateUser'`)

**Problem:**
کاربر غیرادمین می‌تواند رکورد خودش را ویرایش کند و `password` یکی از فیلدهای مجاز است (خطوط ۱۷۶–۱۷۸). هیچ‌جا رمز فعلی خواسته یا بررسی نمی‌شود.

**Why it matters:**
یک session token دزدیده‌شده (از دستگاه گم‌شده، بکاپ، یا لاگ) امروز فقط تا انقضای نشست کار می‌کند. با این مسیر، مهاجم می‌تواند رمز را عوض کند و دسترسی دائمی بگیرد — یعنی «سرقت نشست» به «تصاحب دائمی حساب» ارتقا پیدا می‌کند. توجه کنید که `UserService::updateUser` بعد از تغییر، همه‌ی نشست‌ها را باطل می‌کند (`UserService.php:209-210`) — که یعنی قربانی هم بیرون انداخته می‌شود.

**Impact:**
ارتقای سرقت نشست به تصاحب کامل حساب.

**Recommended Fix:**
در مسیر self-service، `currentPassword` را الزامی کنید و با `UserService::verifyCredentials` بررسی کنید:
```php
if (!$isAdmin && isset($params['password'])) {
    $current = (string)($params['currentPassword'] ?? '');
    if ($current === '' || $this->userService->verifyCredentials((string)$this->authenticatedUsername, $current) === null) {
        throw new ApiException('رمز عبور فعلی نادرست است.', 403);
    }
}
```
و شمارنده‌ی تلاش ناموفق (`PasswordGateService` یا `LoginAttemptLimiter`) را روی این مسیر هم اعمال کنید. سمت کلاینت، `ProfileSettingsDialogSection.kt:308` باید فیلد رمز فعلی را اضافه کند.

**Priority:** MEDIUM
**Estimated Effort:** Low

---

### [MEDIUM] نشت جزئیات خطای SQL و استثنا به کلاینت

**File:** `PHP/src/Controllers/ChatController.php`، `PHP/src/Controllers/AppApiController.php`، `PHP/src/Controllers/CargoController.php`، `PHP/src/routes/api_v2.php`
**Location:** `ChatController.php:307,309` و `:94-98`؛ `AppApiController.php:345-347`؛ `CargoController.php:614,632`؛ `api_v2.php:69-71`

**Problem:**
`ChatController::prepareAndExecute` پیام خام mysqli را در استثنا می‌گذارد، و `handleChatRequest` همان را مستقیماً به کلاینت echo می‌کند:
```php
// ChatController.php:307
if (!$stmt) throw new Exception('SQL Error: ' . $this->conn->error);
// ChatController.php:95-97
http_response_code(400);
echo json_encode(['success' => false, 'message' => $e->getMessage()], JSON_UNESCAPED_UNICODE);
```
همین الگو در `AppApiController::handle` (`Response::json(['error' => $e->getMessage()], 500)`) و `api_v2.php`'s `$safeCall` تکرار شده. `CargoController::saveInitialInfo` هم `$this->conn->error` و `$stmt->error` را در پیام استثنا می‌گذارد که سپس در خط ۶۴۰ به کلاینت می‌رود.

این در حالی است که در جاهای دیگر همین کدبیس رفتار درست پیاده شده — `DatabaseManager::prepare` (خطوط ۲۳–۲۶) و `CargoController::confirmCargo` (خطوط ۲۶۵–۲۷۰) صراحتاً پیام داخلی را فقط لاگ می‌کنند. یعنی الگوی درست وجود دارد ولی یکنواخت اعمال نشده.

**Why it matters:**
افشای نام جدول/ستون و ساختار کوئری به مهاجم، که مرحله‌ی شناسایی حملات بعدی را تسریع می‌کند.

**Impact:**
Information Disclosure؛ کمک به طراحی حملات هدفمند.

**Recommended Fix:**
یک الگوی واحد اعمال کنید — پیام‌های `ApiException` (که فارسی و عمدی‌اند) به کلاینت، بقیه فقط لاگ:
```php
} catch (ApiException $e) {
    Response::json(['error' => $e->getMessage()], $e->getStatusCode());
} catch (\Throwable $e) {
    error_log(static::class . ': ' . $e->getMessage());
    Response::json(['error' => 'خطای داخلی سرور رخ داده است.'], 500);
}
```
`app_api.php:19-25` این کار را از قبل درست انجام می‌دهد — همان را به `AppApiController::handle`، `ChatController` و `$safeCall` منتقل کنید.

**Priority:** MEDIUM
**Estimated Effort:** Low

---

### [MEDIUM] `limit` در چت بدون سقف

**File:** `PHP/src/Controllers/ChatController.php`
**Location:** خط ۵۳

**Problem:**
```php
$limit = (int)$this->request->get('limit', self::MESSAGE_FETCH_LIMIT);
```
مقدار مستقیماً به `LIMIT ?` می‌رود (خطوط ۱۲۹، ۱۴۱). ثابت `MESSAGE_FETCH_LIMIT = 100` فقط پیش‌فرض است، نه سقف. یک کاربر ادمین می‌تواند `limit=100000000` بفرستد.

نکته: SQL Injection وجود ندارد (bind شده به‌عنوان `i`) و فقط ادمین‌ها به این مسیر دسترسی دارند — به همین دلیل MEDIUM و نه HIGH.

**Why it matters:**
کوئری با subquery همبسته (`read_by_names` که به‌ازای هر ردیف `GROUP_CONCAT` روی `admin_chat_reads` می‌زند) روی یک بازه‌ی بزرگ می‌تواند سرور را برای مدت طولانی مشغول کند.

**Impact:**
Resource exhaustion / DoS داخلی.

**Recommended Fix:**
```php
$limit = max(1, min((int)$this->request->get('limit', self::MESSAGE_FETCH_LIMIT), self::MESSAGE_FETCH_LIMIT));
```

**Priority:** MEDIUM
**Estimated Effort:** Low

---

### [MEDIUM] `InputValidator::sanitize` روی مقادیری که در `WHERE =` مقایسه می‌شوند

**File:** `PHP/src/Validators/InputValidator.php`، `PHP/src/Controllers/AuthController.php`
**Location:** `InputValidator.php:14-16`؛ `AuthController.php:56` و `:167`، `:217`، `:270`

**Problem:**
```php
public static function sanitize(string $value): string {
    return htmlspecialchars(strip_tags(trim($value)), ENT_QUOTES, 'UTF-8');
}
```
این تابع روی `username` قبل از جستجو در دیتابیس اعمال می‌شود. کامنت خود پروژه در `InputValidator.php:76-83` دقیقاً این مشکل را برای `validateIdentifier` تشخیص داده و حل کرده («نامی مثل M&V به M&amp;V تبدیل و مقایسه با دیتابیس شکسته می‌شود») — اما همان اصل روی `username` اعمال نشده.

**Why it matters:**
دو اثر: (الف) اگر نام کاربری شامل `&`, `'`, `"`, `<` باشد، ورود شکست می‌خورد بدون پیام معنادار؛ (ب) escaping به‌عنوان دفاع در برابر SQLi اینجا بی‌معناست (prepared statement کار را انجام می‌دهد) و یک حس امنیت کاذب می‌سازد. برای XSS هم درست نیست: encoding باید در **خروجی** انجام شود، نه در **ورودی** — وگرنه داده در دیتابیس دائماً آلوده به entity می‌شود (که در `CargoController::searchByTracking:406-424` قابل مشاهده است، جایی که دوباره `htmlspecialchars` هم اعمال می‌شود → double-encoding).

**Impact:**
باگ‌های داده‌ای، double-encoding، و یک لایه‌ی دفاعی که واقعی نیست.

**Recommended Fix:**
`sanitize` را برای مقادیر مقایسه‌ای با `validateIdentifier` (فقط `trim` + محدودیت طول) جایگزین کنید. encoding را فقط در لایه‌ی خروجی (و ترجیحاً فقط برای مصرف‌کننده‌های HTML، نه کلاینت اندروید که JSON می‌خواند) نگه دارید. `CargoController::sanitizeString:690-692` این تصمیم درست را از قبل گرفته — همان را در `AuthController` هم اعمال کنید.

**Priority:** MEDIUM
**Estimated Effort:** Low

---

### [MEDIUM] `permissions.json` به‌عنوان منبع حقیقت مجوزها بدون قفل نوشتن

**File:** `PHP/src/Services/PermissionService.php`، `PHP/PermissionManager.php`
**Location:** `PermissionService.php:21-35`؛ `PermissionManager.php:70-84`

**Problem:**
مجوزها در یک فایل JSON نگه‌داری می‌شوند. `PermissionService` آن را در **هر بررسی مجوز** می‌خواند و decode می‌کند (بدون کش). `PermissionManager` آن را با `file_put_contents` می‌نویسد (بدون `LOCK_EX`، بدون نوشتن اتمیک).

**Why it matters:**
دو ریسک: (الف) اگر دو ادمین هم‌زمان ذخیره کنند یا نوشتن نیمه‌تمام بماند، فایل خراب می‌شود؛ چون `json_decode` ناموفق به `return []` منجر می‌شود (خط ۲۷)، **همه‌ی مجوزها برای همه false می‌شوند** — یعنی از کار افتادن کامل سیستم. خوشبختانه این fail-closed است (نه fail-open)، که تصمیم درستی است، ولی همچنان یک outage است. (ب) خواندن دیسک در هر بررسی مجوز، یک هزینه‌ی I/O در مسیر داغ است.

**Impact:**
ریسک outage کامل؛ سربار I/O در هر درخواست مجوزدار.

**Recommended Fix:**
1. نوشتن اتمیک در `PermissionManager`:
```php
$tmp = $permissions_file . '.tmp';
file_put_contents($tmp, json_encode($data, JSON_PRETTY_PRINT|JSON_UNESCAPED_UNICODE), LOCK_EX);
rename($tmp, $permissions_file);   // atomic روی همان فایل‌سیستم
```
2. کش خواندن در `PermissionService` با `MicroCache` که از قبل وجود دارد:
```php
$allData = MicroCache::remember('permissions_file', 30, fn() =>
    json_decode((string)file_get_contents(self::PERMISSIONS_FILE), true));
```
و `MicroCache::forget('permissions_file')` در `PermissionManager` بعد از هر ذخیره.
3. **میان‌مدت:** انتقال مجوزها به دیتابیس (جدول `role_permissions` + `user_permissions`) — این هم مشکل قفل را حل می‌کند، هم امکان audit و rollback می‌دهد.

**Priority:** MEDIUM
**Estimated Effort:** Low (موارد ۱–۲) / Medium (مورد ۳)

---

### [MEDIUM] `.env.example` با `APP_DEBUG=true` به‌عنوان پیش‌فرض

**File:** `PHP/.env.example`
**Location:** خطوط ۴–۵

**Problem:**
```
APP_ENV=development
APP_DEBUG=true
```
این فایل الگویی است که مستقیماً به `.env` کپی می‌شود. هرچند تأیید شد که هیچ کدی امروز `APP_DEBUG` را نمی‌خواند (پس اثر عملی فعلی ندارد)، اما یک تله‌ی پیکربندی برای آینده است: اولین کدی که `if ($_ENV['APP_DEBUG'])` بنویسد، در تولید فعال خواهد بود.

**Impact:**
Potential Issue — ریسک آینده، نه آسیب‌پذیری فعلی.

**Recommended Fix:**
پیش‌فرض‌ها را امن کنید (`APP_ENV=production`, `APP_DEBUG=false`) و در همان فایل توضیح دهید که برای توسعه‌ی محلی باید تغییر کنند.

**Priority:** MEDIUM
**Estimated Effort:** Low

---

### Low Issues

---

### [LOW] `get_csrf_token.php` بدون هدر امنیتی مشترک و بدون تنظیم امن کوکی

**File:** `PHP/get_csrf_token.php`
**Location:** خطوط ۱–۱۵

**Problem:**
تنها فایلی است که `session_start()` را بدون بارگذاری `bootstrap.php` صدا می‌زند و هدرها را دستی تکرار می‌کند (به‌جای `Response::sendSecurityHeaders()`). `Cache-Control` هم ندارد — یعنی یک پروکسی میانی می‌تواند پاسخ حاوی توکن CSRF را کش کند.

**Recommended Fix:** استفاده از `Response::sendSecurityHeaders()` و افزودن `Cache-Control: no-store`.

**Priority:** LOW · **Effort:** Low

---

### [LOW] `.htaccess` قواعدی دارد که روی هاست فعلی اجرا نمی‌شوند

**File:** `PHP/.htaccess`، `PHP/config/.htaccess`
**Location:** `.htaccess:44-48` (hotlink)، `:61-66` (rewrite v2)؛ `config/.htaccess:88-95`

**Problem:**
کامنت خط ۵۲–۵۹ خودِ فایل تأیید می‌کند که `mod_rewrite` از طریق `.htaccess` روی این هاست فعال نیست. یعنی قواعد `RewriteRule` بی‌اثرند. همچنین `config/.htaccess` یک بلوک `BrowserMatchNoCase ... curl` دارد که هم بی‌اثر است و هم اگر اثر داشت، دفاع معناداری نبود (User-Agent قابل جعل است). بلوک‌های `<Directory>` هم در `.htaccess` مجاز نیستند و توسط Apache نادیده گرفته می‌شوند (یا خطای ۵۰۰ می‌دهند).

**Why it matters:** فایل امنیتی که بخش زیادی از آن اجرا نمی‌شود، یک حس امنیت کاذب می‌سازد و بازبینی را سخت می‌کند.

**Recommended Fix:** قواعد بی‌اثر را حذف کنید و آن‌هایی که واقعاً لازم‌اند (`FilesMatch` برای `.env`، `.sql`، `.log`) را نگه دارید. `<Directory>` را با `<Files>`/`<FilesMatch>` یا `.htaccess` داخل همان پوشه جایگزین کنید.

**Priority:** LOW · **Effort:** Low

---

### [LOW] `REQUEST_INSTALL_PACKAGES` و `WRITE_EXTERNAL_STORAGE` در مانیفست

**File:** `app/src/main/AndroidManifest.xml`
**Location:** خطوط ۳۳–۳۸

**Problem:**
`REQUEST_INSTALL_PACKAGES` برای مکانیزم به‌روزرسانی داخلی لازم است (اپ از Play توزیع نمی‌شود)، پس قابل توجیه است — اما در ترکیب با fail-open بودن تأیید هش (بالا) سطح ریسک را بالا می‌برد. `WRITE_EXTERNAL_STORAGE`/`READ_EXTERNAL_STORAGE` با `maxSdkVersion="32"` محدود شده‌اند که درست است.

**Recommended Fix:** پس از اصلاح fail-open، این مورد قابل قبول است. مستند کنید که چرا لازم است.

**Priority:** LOW · **Effort:** Low

---
## Android Audit

### نکات مثبت (تأییدشده)

| مورد | فایل | ارزیابی |
|---|---|---|
| `cleartextTrafficPermitted="false"` در base و domain config | `res/xml/network_security_config.xml` | ✅ درست |
| Certificate Pinning روی CA میانی+ریشه با `expiration` | همان، `pin-set` | ✅ انتخاب مهندسی درست و مستند (پین leaf باعث قطع با هر تمدید می‌شد) |
| `allowBackup="false"` + exclude کامل در هر دو backup rules | `AndroidManifest.xml:47-49` | ✅ درست |
| Android Keystore + AES/GCM/NoPadding با IV تصادفی | `security/CryptoManager.kt` | ✅ درست |
| هیچ activity/service/provider غیرضروری exported نیست | `AndroidManifest.xml` | ✅ درست |
| `BootReceiver` با `android:permission` محافظت شده | `AndroidManifest.xml:89` | ✅ درست |
| `FileProvider` محدود به `updates/` و `Documents/` | `res/xml/file_path.xml` | ✅ اصلاح‌شده (قبلاً `path="."`) |
| `HttpLoggingInterceptor` فقط در DEBUG با `Level.BODY` | `api/RetrofitClient.kt:101-107` | ✅ درست |
| هیچ WebView در پروژه وجود ندارد | — | ✅ کل دسته‌ی آسیب‌پذیری WebView منتفی است |
| `lint.abortOnError = true` و `checkReleaseBuilds = true` | `app/build.gradle.kts:114-115` | ✅ درست |

### یافته‌ها

---

### [MEDIUM] `targetSdk = 34` در حالی که `compileSdk = 36` است

**File:** `app/build.gradle.kts`
**Location:** خطوط ۱۳، ۱۸

**Problem:**
`compileSdk = 36` / `targetSdk = 34`. اپ روی دستگاه‌های Android 15/16 در حالت سازگاری اجرا می‌شود و رفتارهای جدید پلتفرم (edge-to-edge اجباری، محدودیت‌های foreground service، تغییرات JobScheduler، محدودیت‌های اشتراک‌گذاری فایل) را دریافت نمی‌کند.

**Why it matters:**
چون اپ از Play توزیع نمی‌شود، الزام سیاستی Play اعمال نمی‌شود — پس این یک مشکل انطباق نیست، یک بدهی فنی انباشته است. هرچه فاصله بیشتر شود، جهش بعدی پرهزینه‌تر و پرریسک‌تر می‌شود، به‌ویژه با توجه به اینکه اپ از `FOREGROUND_SERVICE_DATA_SYNC` و `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` استفاده می‌کند که دقیقاً همان حوزه‌هایی هستند که در نسخه‌های اخیر سخت‌گیرانه‌تر شده‌اند.

**Impact:** بدهی فنی انباشته؛ ریسک شکست در نسخه‌های آینده‌ی اندروید.

**Recommended Fix:** `targetSdk = 35` را در یک شاخه‌ی جداگانه تست کنید (تمرکز روی `LoadingNotificationService` و رفتار edge-to-edge در Compose)، سپس ۳۶.

**Priority:** MEDIUM · **Effort:** Medium

---

### [MEDIUM] `viewBinding = true` در یک اپ کاملاً Compose

**File:** `app/build.gradle.kts:94`، `gradle.properties:80`

**Problem:**
`viewBinding` هم در `build.gradle.kts` و هم در `gradle.properties` فعال است، اما جستجوی کل کدبیس هیچ استفاده‌ای پیدا نکرد (۰ ارجاع). AGP برای هر layout XML یک کلاس binding تولید می‌کند.

**Impact:** زمان build اضافی و کلاس‌های بی‌مصرف (که R8 حذفشان می‌کند، پس اثر روی اندازه‌ی APK ندارد).

**Recommended Fix:** `viewBinding = false` (و حذف خط از `gradle.properties`). همچنین `dataBinding`/`aidl`/... از قبل درست `false` شده‌اند.

**Priority:** MEDIUM · **Effort:** Low

---

### [MEDIUM] وابستگی‌های بلااستفاده در گراف build

**File:** `app/build.gradle.kts`، `gradle/libs.versions.toml`

**Problem:**
جستجوی import در کل ۱۶۰ فایل Kotlin نشان می‌دهد این وابستگی‌ها **صفر استفاده** دارند:

| وابستگی | خط در `build.gradle.kts` | استفاده |
|---|---|---|
| `androidx.appcompat` | 232 | 0 |
| `androidx.constraintlayout` | 234 | 0 |
| `androidx.navigation.fragment.ktx` | 255 | 0 |
| `androidx.navigation.ui.ktx` | 256 | 0 |
| `com.google.android.material` (`libs.material`) | 251 | 0 |
| `coil-compose` | 288 | 0 |

علاوه بر این، `libs.versions.toml` نسخه‌هایی برای `tensorflow-lite*`, `poi`, `poi-ooxml`, `jxl`, `icu4j`, `konfetti-compose`, `media3-exoplayer`, `json`, `tasks-vision` تعریف کرده که هیچ‌کدام در `build.gradle.kts` استفاده نمی‌شوند — بقایای featureهای حذف‌شده.

همچنین هر دو `androidx.compose.material` (M2، خط ۲۴۹) و `androidx.compose.material3` (خط ۲۴۸) وارد شده‌اند — duplicate functionality.

`androidx.material.icons.extended` (خط ۲۵۰) کل مجموعه‌ی آیکون Material را وارد می‌کند (چند هزار vector)؛ R8 آن را shrink می‌کند ولی زمان build را محسوس افزایش می‌دهد.

**Why it matters:**
هر وابستگی یک سطح حمله، یک منبع هشدار lint، و یک هزینه‌ی نگهداری/به‌روزرسانی است. `appcompat` + `material` + `constraintlayout` نشانه‌ی بقایای معماری View-based قبلی هستند.

**Impact:** زمان build بالاتر، گراف وابستگی گمراه‌کننده، سطح حمله‌ی غیرضروری.

**Recommended Fix:** حذف شش وابستگی بالا و پاک‌سازی entryهای مرده در `libs.versions.toml`. برای M2 در برابر M3، بررسی کنید کدام composableها هنوز `androidx.compose.material.*` را import می‌کنند و به M3 مهاجرت دهید. برای آیکون‌ها، اگر تعداد آیکون‌های مصرفی کم است، فقط `material-icons-core` را نگه دارید.

**Priority:** MEDIUM · **Effort:** Low (حذف) / Medium (مهاجرت M2→M3)

---

### [LOW] `versionCode = 11` در برابر `versionName = "4.0.1"`

**File:** `app/build.gradle.kts:19-20`

**Problem:** `versionCode` (۱۱) با شماره‌گذاری معنایی `versionName` هم‌راستا نیست. چون گیت `min_allowed_version` و `latest_version` سمت سرور همگی روی `versionName` کار می‌کنند (`AuthenticatesRequests::enforceMinAppVersion` با `version_compare` روی رشته)، این ناهماهنگی امروز باگی ایجاد نمی‌کند، اما مقایسه‌ی نسخه را به parsing رشته‌ای وابسته نگه می‌دارد.

**Recommended Fix:** `versionCode` را از `versionName` مشتق کنید (مثلاً `4.0.1` → `40001`) تا هر دو همیشه هم‌راستا بمانند.

**Priority:** LOW · **Effort:** Low

---

## Kotlin Audit

### نکات مثبت

- **استفاده‌ی بسیار محدود از `!!`** — تنها ۱۰ مورد در ۵۱,۶۰۰ خط (اکثراً `response.body()!!` بلافاصله بعد از `isSuccessful && body()?.success == true`، یعنی از نظر منطقی امن).
- **`lateinit` فقط ۲ مورد** (`UpdateManager.currentDownloadFile`) با محافظ `::isInitialized` در همه‌ی مصرف‌ها.
- **`@Volatile` درست روی `AuthSession`** — این آبجکت از threadهای مختلف (interceptor OkHttp، coroutineهای UI) خوانده/نوشته می‌شود.
- **`GlobalScope` صفر مورد** — تمام coroutineها به `viewModelScope` یا یک scope با چرخه‌ی حیات مشخص وصل‌اند.
- **`sealed class` برای stateهای پیچیده** (`UpdateManager.DownloadState`, `LoginResult`) — درست.
- **`structured concurrency` درست در `CargoViewModel.kt:699`** — کامنت صریح توضیح می‌دهد چرا از `launch` ساده به‌جای یک `CoroutineScope` مستقل استفاده شده.

### یافته‌ها

---

### [HIGH] باگ منطقی: مسیر پاک‌سازی نشست منقضی در `PermissionPoller` هرگز اجرا نمی‌شود

**File:** `app/src/main/java/com/atk/atk_cargo/api/PermissionPoller.kt`
**Location:** خطوط ۱۱۵–۱۳۵

**Problem:**
```kotlin
if (response.isSuccessful) {          // ← فقط 2xx
    val body = response.body()
    if (body?.success == true) {
        ...
    } else if (response.code() == 401) {     // ← داخل شاخه‌ی 2xx
        Log.w(TAG, "Session expired, clearing credentials")
        userPreferencesManager.clearUserCredentials()
        stop()
    }
} else {
    Log.w(TAG, "Sync failed: HTTP ${response.code()}")
}
```
`response.isSuccessful` در Retrofit یعنی کد وضعیت در بازه‌ی ۲۰۰–۲۹۹ است. بنابراین `response.code() == 401` داخل این بلوک **هرگز true نمی‌شود**. کل شاخه کد مرده است.

**Why it matters:**
این تنها مسیری است که `PermissionPoller` برای واکنش به باطل شدن نشست دارد. وقتی ادمین یک کاربر را force-logout می‌کند یا نشست منقضی می‌شود، سرور ۴۰۱ برمی‌گرداند، شاخه‌ی `else` اجرا می‌شود، فقط یک `Log.w` نوشته می‌شود، و **poller هر ۳ دقیقه تا ابد به تلاش ادامه می‌دهد** بدون اینکه اعتبارنامه‌های محلی پاک شوند یا کاربر به صفحه‌ی ورود برود.

**Impact:**
یک کاربر force-logout شده در UI همچنان «وارد شده» به‌نظر می‌رسد؛ درخواست‌های شبکه‌ی بی‌فایده‌ی دائمی هر ۳ دقیقه؛ اثر ضعیف‌شدن کنترل «خروج اجباری» که یک قابلیت امنیتی است.

**Recommended Fix:**
```kotlin
if (response.code() == 401) {
    Log.w(TAG, "Session expired, clearing credentials")
    userPreferencesManager.clearUserCredentials()
    stop()
    return
}
if (!response.isSuccessful) {
    Log.w(TAG, "Sync failed: HTTP ${response.code()}")
    return
}
val body = response.body()
if (body?.success == true) { /* ... */ }
```
توجه: `syncPermissions` امروز از `protected_proxy.php` عبور می‌کند و `UtilityController::syncPermissions` طبق کامنت `api_v2.php:482-485` در v1 توکن را بررسی نمی‌کند — پس ممکن است اصلاً ۴۰۱ ندهد. این را هم باید هم‌زمان بررسی کرد.

**Priority:** HIGH · **Effort:** Low

---

### [MEDIUM] `runBlocking` داخل `Authenticator` روی thread شبکه‌ی OkHttp

**File:** `app/src/main/java/com/atk/atk_cargo/api/TokenAuthenticator.kt`
**Location:** خطوط ۴۳–۵۶

**Problem:**
```kotlin
val newAccessToken = runBlocking {
    mutex.withLock { ... TokenRefresher.refresh(baseUrl, userPreferencesManager) }
}
```
`okhttp3.Authenticator.authenticate` یک API همگام است، پس `runBlocking` اینجا **اجتناب‌ناپذیر و درست** است — این ایراد طراحی نیست. اما دو نکته:

۱. `TokenRefresher.refresh` یک `OkHttpClient` **مجزا** می‌سازد (`TokenRefresher.kt:24-29`) که هیچ‌کدام از تنظیمات کلاینت اصلی را ندارد. Certificate Pinning همچنان اعمال می‌شود (چون از طریق `network_security_config` در سطح پلتفرم است، نه OkHttp) — پس شکاف امنیتی نیست. ولی timeoutها، connection pool و retry متفاوت‌اند و یک نمونه‌ی اضافی از کل استک OkHttp در حافظه نگه داشته می‌شود.

۲. `mutex.withLock` داخل `runBlocking` روی threadهای dispatcher خود OkHttp اجرا می‌شود. اگر چند درخواست هم‌زمان ۴۰۱ بگیرند، threadهای dispatcher تا اتمام refresh بلاک می‌مانند. با `maxRequests` پیش‌فرض OkHttp (۶۴) این خطر deadlock ندارد، ولی می‌تواند یک توقف کوتاه سراسری ایجاد کند.

**Impact:** مصرف حافظه‌ی اضافی؛ ریسک توقف کوتاه‌مدت در شرایط انقضای هم‌زمان توکن.

**Recommended Fix:** یک `OkHttpClient` پایه‌ی مشترک بسازید و در `TokenRefresher` از `baseClient.newBuilder()` استفاده کنید (بدون `authenticator` تا حلقه ایجاد نشود). این هم پیکربندی را یکنواخت می‌کند هم یک نمونه کمتر نگه می‌دارد.

**Priority:** MEDIUM · **Effort:** Low

---

### [MEDIUM] بسته‌بندی مکرر استثنا که نوع و stack trace را نابود می‌کند

**File:** `app/src/main/java/com/atk/atk_cargo/data/repository/ReportsRepository.kt`
**Location:** خطوط ۵۱–۵۳، ۹۵–۹۷، ۱۰۹–۱۱۱، ۱۲۲–۱۲۴، ۱۴۲–۱۴۴، ۲۲۹–۲۳۱، ۲۴۸–۲۵۰، ۲۶۲–۲۶۴، ۲۷۹–۲۸۱، ۳۰۰–۳۰۲، ۳۲۵–۳۲۷، ۳۷۵–۳۷۷، ۳۸۸–۳۹۰

**Problem:**
سیزده متد این الگو را تکرار می‌کنند:
```kotlin
} catch (e: Exception) {
    throw Exception("Error fetching quota details: ${e.message}")
}
```
و در یک مورد، الگو کاملاً بی‌اثر است:
```kotlin
// ReportsRepository.kt:51-53
} catch (e: Exception) {
    throw e            // ← catch/rethrow خالص، بدون هیچ اثری
}
```
نتیجه: `UnknownHostException`, `SocketTimeoutException`, `SSLPeerUnverifiedException`, `HttpStatusException` — همه به یک `Exception` عمومی تبدیل می‌شوند. لایه‌ی بالاتر دیگر نمی‌تواند بین «اینترنت قطع است»، «سرور کند است»، «pinning شکست خورد» و «کوتاژ یافت نشد» تفاوت بگذارد و مجبور است به تطبیق رشته‌ی فارسی پیام تکیه کند.

نکته‌ی مثبت: `getShipDetails` (خط ۶۵)، `getRealTimeLoadingData` (۳۹۷) و `getComprehensiveAnalysis` (۵۰۲) این کار را **نمی‌کنند** و `HttpStatusException` را با کد وضعیت منتشر می‌کنند — و کامنت‌های خطوط ۳۹۳–۳۹۶ و ۴۹۷–۵۰۱ دقیقاً توضیح می‌دهند چرا. یعنی الگوی درست شناخته شده است اما فقط در سه متد اعمال شده.

**Impact:**
پیام‌های خطای عمومی و غیرکاربردی برای کاربر نهایی؛ عیب‌یابی میدانی تقریباً غیرممکن (stack trace اصلی حذف می‌شود)؛ عدم امکان retry هوشمند (نمی‌دانیم خطا گذرا بود یا دائمی).

**Recommended Fix:**
یک نوع خطای دامنه‌ای بسازید و همه‌ی متدها را روی آن یکسان کنید:
```kotlin
sealed class DataError : Exception() {
    data class Network(override val cause: Throwable) : DataError()
    data class Http(val statusCode: Int, val serverMessage: String?) : DataError()
    data class Empty(val what: String) : DataError()
    data class Unknown(override val cause: Throwable) : DataError()
}

private inline fun <T> apiCall(block: () -> Response<T>): T {
    val response = try { block() }
        catch (e: IOException) { throw DataError.Network(e) }
        catch (e: Exception) { throw DataError.Unknown(e) }
    if (!response.isSuccessful) throw DataError.Http(response.code(), response.errorBody()?.string())
    return response.body() ?: throw DataError.Empty(response.raw().request.url.toString())
}
```
حداقل، در همه‌ی موارد `cause = e` را حفظ کنید: `throw Exception("...", e)`.

**Priority:** MEDIUM · **Effort:** Medium

---

### [MEDIUM] بلعیدن خطا و بازگرداندن نتیجه‌ی خالی در جستجوها

**File:** `app/src/main/java/com/atk/atk_cargo/data/repository/ReportsRepository.kt`
**Location:** خطوط ۴۰۹–۴۲۵ (`getCargoInfoByReceiptNumber`)، ۴۲۷–۴۶۰ (`getCargoInfoByTrackingNumber`)

**Problem:**
```kotlin
} else {
    null            // HTTP 500? خطای شبکه؟ → «یافت نشد»
}
} catch (e: Exception) {
    null            // pinning شکست خورد؟ → «یافت نشد»
}
```
و در متد دوم `emptyList()` در چهار مسیر مختلف بازگردانده می‌شود.

**Why it matters:**
کاربر پیام «هیچ نتیجه‌ای یافت نشد» می‌بیند در حالی که واقعاً سرور down است یا اینترنت قطع است. این در یک سیستم عملیاتی بندری خطرناک است: کاربر ممکن است نتیجه بگیرد که حواله ثبت نشده و آن را دوباره ثبت کند.

**Impact:** تصمیم عملیاتی اشتباه بر اساس اطلاعات نادرست؛ احتمال ثبت داده‌ی تکراری.

**Recommended Fix:** خطای شبکه/سرور را از «نتیجه‌ی خالی» جدا کنید — یا با `Result<T>` یا با `DataError` بالا. ۴۰۴ صریح سرور = خالی؛ هر چیز دیگر = خطا.

**Priority:** MEDIUM · **Effort:** Low

---

### [MEDIUM] پیاده‌سازی دوگانه‌ی تبدیل تاریخ جلالی

**File:** `app/src/main/java/com/atk/atk_cargo/ui/viewmodel/CargoViewModel.kt` و `app/src/main/java/com/atk/atk_cargo/utils/JalaliDateUtils.kt`
**Location:** `CargoViewModel.kt:947-979` در برابر `JalaliDateUtils.kt:70-122`

**Problem:**
دو الگوریتم مستقل برای همان تبدیل:
- `JalaliDateUtils.gregorianToJalali(gy, gm, gd)` — مبتنی بر `java.time.ZonedDateTime` (با core library desugaring).
- `CargoViewModel.gregorianToJalali(Calendar)` — مبتنی بر `java.util.Calendar` با ثابت‌های عددی دستی (`355666`, `12053`, `1461`).

`CargoViewModel.getCurrentDate()` (خط ۸۷۴) از نسخه‌ی محلی استفاده می‌کند و مقدار حاصل مستقیماً در `exitDate` رکوردهای حواله ذخیره می‌شود.

**Why it matters:**
تاریخ خروج حواله یک داده‌ی تجاری/مالی است و در سرور برای فیلتر بازه استفاده می‌شود (`CargoController::getInitialInfo:564`, `exitDate >= ? AND exitDate <= ?`). دو الگوریتم مستقل یعنی امکان اختلاف یک‌روزه در مرزها (سال کبیسه، آخر اسفند، تغییر ساعت). حتی اگر امروز هر دو یکسان باشند، هیچ تستی این را تضمین نمی‌کند و هر اصلاح باگ در یکی، دیگری را نادرست می‌گذارد.

**Impact:** ریسک ناسازگاری داده‌ی تاریخ؛ نقض DRY؛ باگ‌های آینده در مرزهای تقویمی.

**Recommended Fix:**
`gregorianToJalali` و `getCurrentDate` را از `CargoViewModel` حذف و به `JalaliDateUtils` واگذار کنید. سپس یک تست پارامتری برای مرزها بنویسید (۱ فروردین، ۲۹/۳۰ اسفند، سال‌های کبیسه‌ی ۱۴۰۳ و ۱۴۰۸).

**Priority:** MEDIUM · **Effort:** Low

---

### [LOW] `Strictness.LENIENT` در Gson

**File:** `app/src/main/java/com/atk/atk_cargo/api/RetrofitClient.kt:95`

**Problem:** `setStrictness(Strictness.LENIENT)` باعث می‌شود JSON نامعتبر (کاما اضافی، رشته‌ی بدون نقل‌قول) بی‌صدا پذیرفته شود.

**Why it matters:** یک پاسخ سرور که به‌طور تصادفی HTML یا warning در ابتدا دارد ممکن است تا حدی پارس شود به‌جای اینکه صریحاً شکست بخورد. با توجه به `FloatTypeAdapter` که هر مقدار غیرقابل‌پارس را به `0f` تبدیل می‌کند (خطوط ۶۹–۸۹)، ترکیب این دو می‌تواند یک تناژ نامعتبر را به «۰» تبدیل کند — که کامنت خط ۷۱–۷۵ خودش به آن اشاره کرده و لاگ اضافه کرده، ولی مقدار همچنان بی‌صدا جایگزین می‌شود.

**Recommended Fix:** `Strictness.STRICT` را در یک شاخه‌ی آزمایشی تست کنید. برای `FloatTypeAdapter`، به‌جای `0f` مقدار `null` (با تغییر امضا به `Float?`) برگردانید تا لایه‌ی UI بتواند «نامشخص» را از «صفر» تشخیص دهد.

**Priority:** LOW · **Effort:** Medium

---

## Jetpack Compose Audit

---

### [HIGH] صفر مورد `collectAsStateWithLifecycle` در برابر ۸۵ مورد `collectAsState`

**File:** سراسر `app/src/main/java/com/atk/atk_cargo/feature/**` و `core/**`
**Location:** ۸۵ فراخوانی در کل کدبیس؛ `androidx.lifecycle:lifecycle-runtime-compose` اصلاً در `build.gradle.kts` نیست

**Problem:**
`collectAsState()` جمع‌آوری Flow را به عمر **composition** گره می‌زند، نه به **lifecycle**. یعنی وقتی اپ به background می‌رود (اما Composition زنده می‌ماند — که در Single-Activity + Navigation Compose حالت عادی است)، جمع‌آوری ادامه دارد.

برای `StateFlow`های ساده‌ی ViewModel هزینه‌ی این تقریباً صفر است (هیچ upstream فعالی وجود ندارد). اما در این پروژه Flowهایی جمع‌آوری می‌شوند که **upstream واقعی دارند**:
- `UserPreferencesManager.username/userType/permissions/sessionToken/themeColor/chat*` — هرکدام یک `DataStore.data` با خواندن دیسک و رمزگشایی Keystore (`CryptoManager.decrypt`) در `map`.
- `PermissionPoller.livePermissions`.
- Flowهای مشتق‌شده از Room در `ChatRepository`.

**Why it matters:**
دو اثر مشخص: (الف) مصرف باتری و CPU در پس‌زمینه برای کاری که هیچ‌کس نمی‌بیند — که در یک اپ میدانی که کاربرانش کل شیفت با آن کار می‌کنند، مستقیماً روی عمر باتری اثر دارد؛ (ب) رمزگشایی Keystore در هر emission، که یک عملیات نسبتاً گران است.

**Impact:** مصرف باتری و CPU در پس‌زمینه؛ کار غیرضروری روی دیسک و Keystore. (Medium→High بسته به مدت زمانی که اپ در پس‌زمینه می‌ماند.)

**Recommended Fix:**
```kotlin
// gradle/libs.versions.toml
androidx-lifecycle-runtime-compose = { module = "androidx.lifecycle:lifecycle-runtime-compose", version.ref = "lifecycle-runtime-ktx" }
// app/build.gradle.kts
implementation(libs.androidx.lifecycle.runtime.compose)
```
سپس جایگزینی سراسری:
```kotlin
import androidx.lifecycle.compose.collectAsStateWithLifecycle
val state by viewModel.someState.collectAsStateWithLifecycle()
```
این یک تغییر مکانیکی و کم‌ریسک است. اولویت را به فایل‌هایی بدهید که Flowهای DataStore/Room را جمع می‌کنند (`HomeScreen.kt`, `ChatScreen.kt`, `MainScreen.kt`, `SelectInfoScreen.kt`).

**Priority:** HIGH · **Effort:** Low

---

### [MEDIUM] ۲۲ مورد از ۳۵ فراخوانی `items(...)` بدون `key`

**File:** موارد شاخص:
`feature/reports/presentation/quota_details/QuotaDetailsScreen.kt:300`
`feature/cargo_entry/presentation/components/QuotaSelectionDialog.kt:88`
`feature/cargo_entry/presentation/components/ShipSelectionDialog.kt:282`
`feature/chat/presentation/components/ChatInputBar.kt:104,275,332`
`feature/chat/presentation/components/ChatToolbar.kt:266,477`
`feature/reports/presentation/dialogs/QuotaWarningDialog.kt:172`
`feature/home/presentation/components/HomeThemeColorPickerRow.kt:140`
`core/ui/components/DateRangePicker.kt:300,330,364`
`feature/cargo_registration/presentation/components/dialogs/DuplicateTrackingNumbersDialog.kt:73`

**Problem:**
```kotlin
// QuotaDetailsScreen.kt:300
items(companyQuotas) { quota -> ... }
```
بدون `key`, Compose آیتم‌ها را با **موقعیت** شناسایی می‌کند. وقتی لیست به‌روزرسانی می‌شود (که در این اپ هر ۳۰ ثانیه از طریق polling اتفاق می‌افتد)، هر جابه‌جایی/درج/حذف باعث می‌شود همه‌ی آیتم‌ها از آن نقطه به بعد نامعتبر شوند: تمام `remember`های داخل آیتم reset می‌شوند، انیمیشن‌های در حال اجرا قطع می‌شوند، و وضعیت داخلی (مثلاً باز/بسته بودن یک کارت) به آیتم اشتباه می‌چسبد.

نکته‌ی مهم: ۱۳ مورد **دارای** key هستند (مثلاً `SelectInfoScreen.kt:851`, `HomeScreen.kt:624`) — یعنی تیم الگو را می‌داند، فقط یکنواخت اعمال نکرده. موارد `DateRangePicker.kt` که روی `items(12)`/`items(daysInMonth)` (شمارنده‌ی ثابت) کار می‌کنند بی‌خطرند و نیازی به key ندارند.

**Why it matters:**
با polling ۳۰ ثانیه‌ای در `SelectInfoScreen`, `CargoDetailsScreen`, `CargoCounterScreen`, هر بار که لیست به‌روز می‌شود کل ناحیه‌ی قابل مشاهده دوباره ساخته می‌شود — jank محسوس و از دست رفتن وضعیت UI زیر انگشت کاربر.

**Impact:** پرش/لرزش لیست هنگام refresh؛ از دست رفتن state داخلی آیتم‌ها؛ recomposition و re-layout غیرضروری.

**Recommended Fix:**
```kotlin
items(companyQuotas, key = { it.id }) { quota -> ... }
// اگر id یکتا نیست:
items(companyQuotas, key = { "${it.quotaNumber}-${it.warehouse}-${it.cargoType}" }) { ... }
```
**هشدار:** key باید در کل لیست یکتا باشد وگرنه Compose در زمان اجرا crash می‌کند. `CargoViewModel.checkForDuplicateTrackingNumbers` (خط ۲۰۷) نشان می‌دهد که `trackingNumber` **می‌تواند تکراری باشد** — پس آن را به‌تنهایی به‌عنوان key استفاده نکنید.

**Priority:** MEDIUM · **Effort:** Low

---

### [HIGH] God Composable — فایل‌های نمایشی با بیش از ۱۰۰۰ خط

**File / Location:**

| فایل | خط |
|---|---:|
| `feature/cargo_entry/presentation/SelectInfoScreen.kt` | **2223** |
| `feature/reports/presentation/dialogs/QuotaManagementDialog.kt` | **1688** |
| `feature/reports/presentation/quota_details/QuotasListScreen.kt` | **1590** |
| `feature/reports/presentation/dialogs/CargoEditSearchDialogsSection.kt` | **1234** |
| `feature/cargo_entry/presentation/InitialInfoScreen.kt` | **1164** |
| `security/SecurityScreen.kt` | **1112** |
| `feature/cargo_counter/presentation/components/CargoCounterComponents.kt` | **1105** |
| `feature/reports/presentation/dialogs/QuotaAnalysisSection.kt` | **1061** |
| `feature/admin/presentation/UserManagementScreen.kt` | **1051** |
| `ui/screens/ManageReportsScreen.kt` | **1039** |

**Problem:**
یک فایل Compose با ۲۲۲۳ خط چند مسئولیت را در هم می‌آمیزد: layout، state محلی، فراخوانی شبکه (`refreshData` در `LaunchedEffect`)، منطق تجاری (اعتبارسنجی کوتاژ)، فرمت‌بندی، و مدیریت دیالوگ‌ها. `SelectInfoScreen` هم‌زمان یک polling loop 30 ثانیه‌ای (خطوط ۳۷۴–۳۸۲) و چندین دیالوگ تودرتو دارد.

**Why it matters:**
سه پیامد عینی: (۱) هر state محلی که در بالای این composable تعریف شود، تغییرش کل درخت را recompose می‌کند — Compose نمی‌تواند دامنه‌ی recomposition را کوچک کند چون همه‌چیز در یک scope است. (۲) `@Preview` عملاً غیرممکن است، پس هیچ بازخورد بصری سریعی وجود ندارد. (۳) تست UI غیرممکن است و بررسی تغییرات (code review) بی‌کیفیت می‌شود.

**Impact:** recomposition گسترده و غیرضروری؛ نگهداری‌ناپذیری؛ عدم امکان تست و preview.

**Recommended Fix:**
الگوی سه‌لایه را اعمال کنید — که در همین پروژه در جاهایی درست انجام شده (`feature/chat/presentation/components/`, `feature/cargo_registration/presentation/components/dialogs/`):
1. **Screen (stateful):** فقط ViewModel را می‌گیرد، state را جمع می‌کند، و یک composable بی‌حالت را صدا می‌زند.
2. **Content (stateless):** فقط `uiState: UiState` و lambdaهای رویداد می‌گیرد — قابل preview و تست.
3. **Components:** هر بخش قابل تفکیک (هدر، فهرست، هر دیالوگ) در فایل خودش.

برای `SelectInfoScreen` مشخصاً: دیالوگ‌ها از قبل جدا شده‌اند (`QuotaEntryDialog`, `QuotaSelectionDialog`, `ShipSelectionDialog`, `ActiveQuotasDialogSection`) — قدم بعدی جدا کردن بدنه‌ی فهرست و هدر است.

**Priority:** HIGH · **Effort:** High

---

### [MEDIUM] نبود `UiState` واحد — ۲۰+ `StateFlow` مجزا در هر ViewModel

**File:** `app/src/main/java/com/atk/atk_cargo/ui/viewmodel/CargoViewModel.kt`
**Location:** خطوط ۵۶–۱۱۱

**Problem:**
`CargoViewModel` بیش از **۲۰ `StateFlow` عمومی مستقل** دارد: `cargoInfoList`, `scaleReceiptNumber`, `clearInputFields`, `initialInfo`, `totalNetWeight`, `resultMessage`, `showAnimatedMessage`, `messageType`, `showNetWeightDialog`, `isSubmitting`, `showDuplicateConfirmationDialog`, `duplicateWarningMessage`, `filteredCargoInfoList`, `loadableTonnage`, `loadableTrucks18Wheeler`, `loadableTrucks10Wheeler`, `duplicateTrackingNumbers`, `showDuplicateDialog`, `selectedShipNames`, ...

**Why it matters:**
سه مشکل: (۱) هر `collectAsState()` یک subscription جداست — صفحه‌ای که ۱۵ تای این‌ها را جمع می‌کند، ۱۵ نقطه‌ی recomposition مستقل دارد. (۲) **حالت‌های ناسازگار قابل نمایش‌اند**: هیچ چیز مانع نمی‌شود که هم‌زمان `isSubmitting=true` و `showDuplicateDialog=true` و `showNetWeightDialog=true` باشد. (۳) تست کردن یعنی جمع کردن ۲۰ Flow جداگانه.

**Impact:** recomposition پراکنده؛ باگ‌های حالت غیرممکن؛ دشواری تست.

**Recommended Fix:**
```kotlin
data class CargoUiState(
    val cargoList: List<CargoInfo> = emptyList(),
    val initialInfo: InitialInfo? = null,
    val isSubmitting: Boolean = false,
    val tonnage: TonnageInfo = TonnageInfo(),
    val activeDialog: CargoDialog? = null,   // sealed — فقط یک دیالوگ در آنِ واحد
    val searchQuery: String = "",
)
sealed interface CargoDialog {
    data class NetWeight(val receiptNumber: String) : CargoDialog
    data class DuplicateConfirmation(val message: String) : CargoDialog
    data class DuplicateTracking(val numbers: List<String>) : CargoDialog
}
private val _uiState = MutableStateFlow(CargoUiState())
val uiState: StateFlow<CargoUiState> = _uiState.asStateFlow()
```
`activeDialog` به‌تنهایی یک دسته‌ی کامل از باگ‌های «دو دیالوگ روی هم» را حذف می‌کند — که کامنت `CargoViewModel.kt:225-232` نشان می‌دهد قبلاً واقعاً اتفاق افتاده است.

**Priority:** MEDIUM · **Effort:** High

---

### [MEDIUM] استفاده‌ی کم از `derivedStateOf` در جاهایی که واقعاً لازم است

**File:** `app/src/main/java/com/atk/atk_cargo/ui/viewmodel/CargoViewModel.kt:166-173` و صفحات مصرف‌کننده

**Problem:**
فقط ۱۰ مورد `derivedStateOf` در کل کدبیس. در مقابل، فیلترکردن لیست به‌صورت دستی در ViewModel انجام و در یک StateFlow دوم نگه داشته می‌شود:
```kotlin
fun filterCargoInfoList(query: String) {
    lastSearchQuery = query
    _filteredCargoInfoList.value = if (query.isEmpty()) _cargoInfoList.value
        else _cargoInfoList.value.filter { it.trackingNumber.contains(query, ignoreCase = true) }
}
```
این نیازمند فراخوانی دستی `filterCargoInfoList(lastSearchQuery)` در هر نقطه‌ای است که لیست تغییر می‌کند (خطوط ۱۸۷، ۶۸۲) — و کامنت خطوط ۹۰–۹۲ توضیح می‌دهد که فراموش کردن همین فراخوانی قبلاً یک باگ واقعی ایجاد کرده بود.

**Why it matters:**
حالت مشتق‌شده‌ای که به‌صورت دستی همگام می‌شود، دیر یا زود از همگامی خارج می‌شود. این دقیقاً همان مسئله‌ای است که `derivedStateOf` (در Compose) و `combine` (در Flow) حل می‌کنند.

**Recommended Fix:**
```kotlin
val filteredCargoList: StateFlow<List<CargoInfo>> =
    combine(_cargoInfoList, _searchQuery) { list, query ->
        if (query.isEmpty()) list
        else list.filter { it.trackingNumber.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
```
هیچ فراخوانی دستی لازم نیست و امکان ناهمگامی از بین می‌رود.

**Priority:** MEDIUM · **Effort:** Medium

---

### [LOW] تمام رشته‌های UI به‌صورت literal در کد

**File:** `app/src/main/res/values/strings.xml` (فقط ۱۹ خط) در برابر ۵۲۶ تابع `@Composable`

**Problem:** عملاً تمام متن‌های فارسی رابط کاربری به‌صورت رشته‌ی درون‌خطی در Kotlin نوشته شده‌اند — از جمله پیام‌های خطا در ViewModelها (`CargoViewModel.kt:301,339,363,459,476,479,...`).

**Why it matters:** غیرقابل ترجمه؛ تغییر یک عبارت نیازمند grep در ۱۶۰ فایل است؛ پیام‌های خطا در ViewModel، لایه‌ی ارائه را به لایه‌ی منطق می‌آورد و تست ViewModel را به تطبیق رشته وابسته می‌کند.

**Recommended Fix:** حداقل پیام‌های خطا و متون تکرارشونده را به `strings.xml` منتقل کنید. در ViewModel به‌جای رشته، یک `@StringRes Int` یا یک نوع `UiText` sealed منتشر کنید.

**Priority:** LOW · **Effort:** High

---

## Animation Audit

### وضعیت کلی

انیمیشن‌ها در `ui/theme/Motion.kt` متمرکز شده‌اند (نشانه‌ی خوبی از یک design system) و `HardwarePerformanceEvaluator.kt` + `AnimationManager.kt` در `feature/startup/domain/` نشان می‌دهند که تیم به عملکرد روی دستگاه‌های ضعیف فکر کرده است — این یک بلوغ غیرمعمول و ارزشمند است.

### یافته‌ها

---

### [MEDIUM] نبود پشتیبانی از Reduce Motion / دسترس‌پذیری

**File:** `app/src/main/java/com/atk/atk_cargo/ui/theme/Motion.kt`، `feature/startup/domain/AnimationManager.kt`

**Problem:**
هیچ‌جا `Settings.Global.ANIMATOR_DURATION_SCALE` یا `TRANSITION_ANIMATION_SCALE` خوانده نمی‌شود. کاربری که در تنظیمات سیستم انیمیشن‌ها را خاموش کرده (یک تنظیم دسترس‌پذیری برای افراد حساس به حرکت، و همچنین یک تنظیم رایج برای صرفه‌جویی باتری) همچنان تمام انیمیشن‌های اپ را می‌بیند.

**Why it matters:**
این یک الزام دسترس‌پذیری است، نه یک قابلیت اختیاری. برای کاربرانی با اختلال دهلیزی، انیمیشن‌های بزرگ صفحه‌به‌صفحه می‌توانند واقعاً ناخوشایند باشند.

**Recommended Fix:**
```kotlin
@Composable
fun rememberAnimationsEnabled(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE, 1f
        ) != 0f
    }
}
```
سپس در `Motion.kt` مدت‌زمان‌ها را از یک `CompositionLocal` بگیرید که وقتی این مقدار `false` است، همه را صفر می‌کند. چون انیمیشن‌ها از قبل متمرکزند، این تغییر نقطه‌ای است.

**Priority:** MEDIUM · **Effort:** Low

---

### [LOW] انیمیشن شمارنده‌ی معکوس ثانیه‌ای که هر ثانیه recomposition ایجاد می‌کند

**File:** `app/src/main/java/com/atk/atk_cargo/feature/cargo_registration/presentation/RegisterCargoScreen.kt`
**Location:** خطوط ۵۴۰–۵۴۸

**Problem:**
```kotlin
while (true) {
    delay(1000L.milliseconds)
    secondsUntilNextRefresh -= 1
    if (secondsUntilNextRefresh <= 0) { triggerSilentAutoRefresh() ... }
}
```
اگر `secondsUntilNextRefresh` یک `mutableStateOf` در سطح بالای صفحه باشد، هر ثانیه کل scope خواننده‌ی آن recompose می‌شود.

**Why it matters:**
در صفحه‌ای با ۸۳۵ خط و یک `LazyColumn`، اگر این مقدار در scope والد خوانده شود، هر ثانیه بخش قابل توجهی از درخت بازسازی می‌شود.

**Recommended Fix:**
شمارنده را در یک composable برگ (leaf) جداگانه ایزوله کنید که فقط خودش state را می‌خواند، یا از `derivedStateOf` برای تبدیل به متن استفاده کنید تا فقط وقتی رشته‌ی نمایشی تغییر کند recomposition رخ دهد:
```kotlin
@Composable
private fun RefreshCountdown(seconds: Int) {   // فقط این composable recompose می‌شود
    Text("بروزرسانی بعدی: ${seconds}s")
}
```

**Priority:** LOW · **Effort:** Low

---

### [INFO] نقاط قوت انیمیشن

- استفاده از `updateTransition`/`AnimatedVisibility` به‌جای انیمیشن دستی در بیشتر موارد.
- `pointerInput` + `awaitPointerEventScope` برای بازخورد فشار در `ReportsCommonWidgets.kt:239-266,397-403` — این روش درست است (بدون تخصیص در هر فریم) و روی thread ورودی اجرا می‌شود، نه با recomposition.
- `HardwarePerformanceEvaluator` که امتیاز سخت‌افزار را در DataStore کش می‌کند (`hardwareScore`) تا انیمیشن‌ها روی دستگاه ضعیف کاهش یابند — الگوی بسیار خوبی است که کمتر پروژه‌ای پیاده می‌کند.

---
## Performance Audit

### خلاصه‌ی اثر

| # | یافته | لایه | اثر |
|---|---|---|---|
| P1 | `collectAsState` بدون lifecycle (۸۵ مورد) | Android | **High** |
| P2 | `permissions.json` خوانده و decode می‌شود در هر بررسی مجوز | Backend | **High** |
| P3 | ۲۲ لیست Lazy بدون `key` + polling ۳۰ ثانیه‌ای | Android UI | **Medium** |
| P4 | ۱۳ ایندکس روی `user_sessions` که در هر درخواست UPDATE می‌شود | Database | **Medium** |
| P5 | فراخوانی دوباره‌ی `refreshCargoInfo()` بعد از هر ثبت | Network | **Medium** |
| P6 | `UserController::updateUser` کل جدول کاربران را برای یافتن خود کاربر می‌خواند | Backend | **Medium** |
| P7 | subquery همبسته‌ی `read_by_names` در هر ردیف پیام چت | Database | **Medium** |
| P8 | نبود صفحه‌بندی در `getAllUsers` / `getOnlineUsers` / گزارش‌ها | API | **Medium** |
| P9 | `material-icons-extended` (زمان build) | Build | **Low** |

### نکات مثبت تأییدشده

- **`MicroCache` با APCu و fallback بی‌خطر** (`src/Core/MicroCache.php`) — الگوی درست، با `forget` صریح بعد از نوشتن‌هایی که کش را منسوخ می‌کنند (`CargoController.php:158,227,635`).
- **ETag + `Cache-Control: private, max-age`** روی endpointهای پرتکرار (`AppApiController::sendCacheableJsonResponse:358-370`) با محاسبه‌ی ETag از داده‌ی پایدار (نه `timestamp`) — یک تصمیم دقیق و درست.
- **`Response::sendSecurityHeaders` که `Cache-Control` از پیش تنظیم‌شده را بازنویسی نمی‌کند** (`Response.php:30-33`) — جزئیاتی که معمولاً از قلم می‌افتد و کل مکانیزم کش را خنثی می‌کند.
- **`touchLastActivityThrottled`** (`SessionRepository.php:132-143`) — UPDATE فقط اگر بیش از ۶۰ ثانیه گذشته باشد، به‌جای هر درخواست.
- **`validateTokenAndGetUserType`** — ادغام ۳ کوئری در ۱ کوئری در مسیر احراز هویت.
- **`Logger` با بافر و `register_shutdown_function`** (`Logger.php:39-46`) — I/O دیسک از مسیر داغ خارج شده.
- **`fastcgi_finish_request()` قبل از نوشتن لاگ** در `protected_proxy.php:255-260`.
- **Baseline Profile module** (`baselineprofile/`) — startup time بهینه شده.
- **`ConnectionPool(10, 5, MINUTES)`** با توجیه صریح برای چند صفحه‌ی هم‌زمان poll کننده.
- **حذف gzip سطح PHP** به نفع `mod_deflate` (`Response.php:52-56`) — درست.

### یافته‌ها

---

### [HIGH] `permissions.json` در هر بررسی مجوز از دیسک خوانده و parse می‌شود

**File:** `PHP/src/Services/PermissionService.php`
**Location:** خطوط ۲۰–۳۵

**Problem:**
```php
public function getUserPermissions(string $username, string $userType): array {
    if (!file_exists(self::PERMISSIONS_FILE)) return [];
    $allData = json_decode((string)file_get_contents(self::PERMISSIONS_FILE), true);
    ...
}
public function hasPermission(...): bool {
    $permissions = $this->getUserPermissions($username, $userType);   // ← هر بار از نو
    return $permissions[$feature] ?? false;
}
```
هیچ کشی وجود ندارد — نه در حافظه‌ی همان درخواست، نه بین درخواست‌ها. بدتر: مصرف‌کننده‌ها یک نمونه‌ی تازه می‌سازند:
```php
// ApiAuthGate.php:42
if (!(new PermissionService())->hasPermission($username, $userType, $feature)) {
```
```php
// AuthenticatesRequests.php:119-120
$permissionService = new PermissionService();
if (!$permissionService->hasPermission(...)) {
```
و در `UserController.php:155-156` یک نمونه‌ی سوم در همان درخواست.

**Why it matters:**
`file_exists` + `file_get_contents` + `json_decode` در مسیر داغ هر درخواست مجوزدار. Page cache سیستم‌عامل خواندن دیسک فیزیکی را مهار می‌کند، ولی `json_decode` هزینه‌ی CPU واقعی دارد و در یک درخواست چند بار تکرار می‌شود.

**Impact:** Medium→High بسته به بار — چند برابر شدن هزینه‌ی CPU پارس در هر درخواست.

**Recommended Fix:**
```php
final class PermissionService {
    private static ?array $cache = null;   // کش سطح درخواست

    private function loadAll(): array {
        if (self::$cache !== null) return self::$cache;
        self::$cache = MicroCache::remember('permissions_file', 30, function () {
            if (!file_exists(self::PERMISSIONS_FILE)) return [];
            return json_decode((string)file_get_contents(self::PERMISSIONS_FILE), true) ?: [];
        });
        return self::$cache;
    }
}
```
`MicroCache` از قبل وجود دارد و همین الگو را برای `SHIPS_LIST_KEY` پیاده کرده. `PermissionManager` بعد از ذخیره باید `MicroCache::forget('permissions_file')` را صدا بزند.

**Priority:** HIGH · **Effort:** Low

---

### [MEDIUM] `user_sessions` با ۱۳ ایندکس که در هر درخواست به‌روزرسانی می‌شود

**File:** `PHP/schema.sql`
**Location:** تعریف `user_sessions`

**Problem:**
```sql
KEY `idx_username` (`username`),
KEY `idx_device_id` (`device_id`),
KEY `idx_is_active` (`is_active`),
KEY `idx_login_time` (`login_time`),
KEY `idx_last_activity` (`last_activity`),
KEY `idx_session_token` (`session_token`),
KEY `idx_userType` (`userType`),
KEY `idx_username_active` (`username`,`is_active`),
KEY `idx_device_active` (`device_id`,`is_active`),
KEY `idx_active_activity` (`is_active`,`last_activity`),
KEY `idx_username_device_active` (`username`,`device_id`,`is_active`),
KEY `idx_userType_active` (`userType`,`is_active`)
```
چند ایندکس صرفاً prefix ایندکس دیگری هستند و کاملاً زائدند:
- `idx_username` ⊂ `idx_username_active` ⊂ `idx_username_device_active`
- `idx_device_id` ⊂ `idx_device_active`
- `idx_is_active` ⊂ `idx_active_activity`
- `idx_userType` ⊂ `idx_userType_active`

**Why it matters:**
InnoDB باید در هر `INSERT`/`UPDATE` همه‌ی ایندکس‌های تحت تأثیر را نگه‌داری کند. این جدول در **هر درخواست احرازشده** با `touchLastActivityThrottled` (که `last_activity` را می‌نویسد) و در هر login/refresh با `rotateTokens` (که ۵ ستون را می‌نویسد) به‌روزرسانی می‌شود. `idx_last_activity` و `idx_active_activity` هر دو شامل ستونی هستند که مرتباً تغییر می‌کند — یعنی هر UPDATE هر دو را بازسازی می‌کند.

**Impact:** هزینه‌ی نوشتن چند برابر در پرترافیک‌ترین جدول سیستم؛ مصرف فضای دیسک و buffer pool.

**Recommended Fix:**
چهار ایندکس زائد را حذف کنید:
```sql
ALTER TABLE user_sessions
  DROP INDEX idx_username,
  DROP INDEX idx_device_id,
  DROP INDEX idx_is_active,
  DROP INDEX idx_userType;
```
و برای کوئری اصلی احراز هویت (`validateTokenAndGetUserType`) یک ایندکس پوشا بسازید که هر سه شرط WHERE و ستون SELECT را در بر بگیرد:
```sql
CREATE INDEX idx_session_auth
  ON user_sessions (username, device_id, session_token, is_active, last_activity, access_token_expires_at, userType);
```
**قبل از اجرا** با `EXPLAIN` روی داده‌ی واقعی تأیید کنید — این پیشنهاد بر اساس خواندن کد است، نه پروفایل اجرا.

**Priority:** MEDIUM · **Effort:** Low

---

### [MEDIUM] `updateUser` کل جدول کاربران را برای یافتن رکورد خود کاربر می‌خواند

**File:** `PHP/src/Controllers/UserController.php`
**Location:** خطوط ۱۵۸–۱۶۳

**Problem:**
```php
if (!$isAdmin) {
    $selfUser = $this->userService->getAllUsers();     // SELECT * FROM Users
    $selfRecord = current(array_filter($selfUser, fn($u) => $u['username'] === $this->authenticatedUsername));
    if (!$selfRecord || (int)$selfRecord['id'] !== $id) { ... }
}
```
`UserRepository::getByUsername` از قبل وجود دارد و دقیقاً همین کار را با یک کوئری ایندکس‌شده (`UNIQUE KEY username`) انجام می‌دهد.

**Why it matters:**
یک full table scan + انتقال کل جدول به PHP + فیلتر در حافظه، به‌جای یک lookup تک‌ردیفی. علاوه بر هزینه، این کد رمز عبور همه‌ی کاربران را هم به حافظه می‌آورد (چون `getAll` ستون `password` را برنمی‌گرداند — تأیید شد که برنمی‌گرداند، پس نشت داده نیست، فقط ناکارآمدی است).

**Recommended Fix:**
```php
$selfRecord = (new \App\Repositories\UserRepository())->getByUsername((string)$this->authenticatedUsername);
if (!$selfRecord || (int)$selfRecord['id'] !== $id) {
    throw new ApiException('شما فقط مجاز به ویرایش حساب خودتان هستید.', 403);
}
```

**Priority:** MEDIUM · **Effort:** Low

---

### [MEDIUM] درخواست‌های شبکه‌ی زائد بعد از هر ثبت حواله

**File:** `app/src/main/java/com/atk/atk_cargo/ui/viewmodel/CargoViewModel.kt`
**Location:** خطوط ۵۲۱–۵۲۴ و ۷۸۴–۷۸۵

**Problem:**
`handleSuccessResponse` ابتدا لیست محلی را به‌روزرسانی می‌کند (`updateLocalCargoListForExit`) و **سپس** `refreshCargoInfo()` را صدا می‌زند که یک `getCargoInfo` کامل + یک `getLoadableTonnage` می‌فرستد. در `updateCargoInfo` همین الگو تکرار می‌شود: به‌روزرسانی خوش‌بینانه‌ی محلی، سپس `refreshCargoInfo()`.

`getCargoInfo` سمت سرور (`CargoController::getInitialInfo`) **سه کوئری** اجرا می‌کند (InitialInfo + آمار aggregate + فهرست CargoInfo).

**Why it matters:**
هر ثبت حواله = ۱ نوشتن + ۳ کوئری خواندن + ۱ کوئری تناژ. در ساعات اوج بارگیری که چند بارشمار هم‌زمان کار می‌کنند، این ضریب مستقیماً روی بار دیتابیس اثر می‌گذارد. به‌روزرسانی محلی از قبل انجام شده، پس refresh فوری صرفاً برای اطمینان است.

**Impact:** ~۴ برابر شدن بار خواندن در ازای هر عملیات نوشتن.

**Recommended Fix:**
یا (الف) به به‌روزرسانی خوش‌بینانه‌ی محلی اکتفا کنید و به polling ۳۰ ثانیه‌ای موجود اجازه دهید همگام‌سازی را انجام دهد؛ یا (ب) سرور در پاسخ `saveOrUpdateCargoInfo` رکورد نهایی و مقادیر aggregate را برگرداند تا هیچ refreshی لازم نباشد. گزینه‌ی (ب) بهتر است چون یک round-trip را کاملاً حذف می‌کند.

**Priority:** MEDIUM · **Effort:** Medium

---

### [MEDIUM] نبود صفحه‌بندی در endpointهای فهرستی

**File:** `PHP/src/Repositories/UserRepository.php:42`, `SessionRepository.php:303-316`, `PHP/src/Services/QuotaService.php`, `ShipService.php`

**Problem:**
`getAll()`, `getOnlineUsers()`, `getLatestSessionsForAllUsers()`, `getQuotasList()`, `getFilteredQuotas()` هیچ‌کدام `LIMIT`/`OFFSET` ندارند. کل نتیجه یک‌جا خوانده، به JSON تبدیل و به کلاینت فرستاده می‌شود.

**Why it matters:**
امروز با تعداد کاربران و کوتاژهای محدود مشکلی ندارد. اما `CargoInfo` جدولی است که با هر حواله رشد می‌کند و `getFilteredQuotas` بازه‌ی تاریخی دلخواه می‌گیرد — یک بازه‌ی وسیع می‌تواند پاسخ بسیار بزرگی تولید کند که هم سرور را مشغول می‌کند هم در کلاینت به `OutOfMemoryError` نزدیک می‌شود (لیست کامل در حافظه‌ی `StateFlow` نگه داشته می‌شود).

**Impact:** ریسک مقیاس‌پذیری میان‌مدت؛ payload بزرگ روی شبکه‌ی موبایل.

**Recommended Fix:**
صفحه‌بندی مبتنی بر cursor (نه offset، که در جداول در حال رشد ناپایدار است) برای فهرست‌های رو به رشد اضافه کنید. سمت کلاینت از `Paging 3` استفاده کنید یا حداقل یک سقف سخت‌گیرانه اعمال کنید. برای بازه‌ی تاریخی، یک حداکثر بازه (مثلاً ۹۰ روز) در سرور اعمال کنید.

**Priority:** MEDIUM · **Effort:** High

---

### [LOW] subquery همبسته در فهرست پیام‌های چت

**File:** `PHP/src/Controllers/ChatController.php`
**Location:** خطوط ۱۰۶–۱۲۳

**Problem:**
`read_by_names` یک `GROUP_CONCAT` با `JOIN` است که به‌ازای **هر ردیف** پیام اجرا می‌شود، به‌علاوه یک `EXISTS` برای `is_read_by_me`.

**Why it matters:** با `limit` پیش‌فرض ۱۰۰ قابل تحمل است، اما در ترکیب با نبود سقف روی `limit` (یافته‌ی MEDIUM بالا) می‌تواند گران شود.

**Recommended Fix:** پس از اعمال سقف `limit`، این را با یک `LEFT JOIN` + `GROUP BY` روی مجموعه‌ی محدودشده جایگزین کنید، یا `read_by_names` را فقط برای پیام‌های قابل مشاهده در viewport به‌صورت درخواست دوم بگیرید.

**Priority:** LOW · **Effort:** Medium

---

## Memory Audit

### وضعیت کلی: **۷/۱۰ — بهترین حوزه‌ی پروژه**

بررسی هدفمند نشتی‌های رایج اندروید هیچ نشتی واضحی پیدا نکرد:

| الگوی نشت | وضعیت |
|---|---|
| `GlobalScope` | ✅ صفر مورد |
| نگه‌داشتن `Context` اکتیویتی در singleton | ✅ همه‌جا `applicationContext` (`RetrofitClient.kt:38`, `UpdateManager.kt:38`) |
| coroutine بدون scope | ✅ همه به `viewModelScope` یا scope با چرخه‌ی حیات وصل‌اند |
| `PermissionPoller` scope رها شده | ✅ `destroy()` وجود دارد و `pollerScope.cancel()` را صدا می‌زند (خطوط ۸۴–۸۹) |
| polling در Composable | ✅ داخل `repeatOnLifecycle(RESUMED)` (نه `LaunchedEffect(Unit)` خالی) |
| listener ثبت‌شده بدون حذف | ✅ موردی یافت نشد |
| `UpdateManager` job رها شده | ✅ `onCleared()` با `downloadJob?.cancel()` + `cleanupDownloadFiles()` |
| Bitmap/تصویر بزرگ | ✅ Coil استفاده نمی‌شود؛ تصاویر از `res/` و vector هستند |

### یافته‌ها

---

### [MEDIUM] فهرست‌های بدون سقف در `StateFlow`

**File:** `app/src/main/java/com/atk/atk_cargo/ui/viewmodel/CargoViewModel.kt:56,88`، `ui/viewmodel/ReportsViewModel.kt`

**Problem:**
`_cargoInfoList` و `_filteredCargoInfoList` **دو کپی کامل** از همان فهرست را نگه می‌دارند (نه دو ارجاع — `filter` یک لیست جدید می‌سازد). با فهرست‌های بزرگ (که به‌خاطر نبود صفحه‌بندی ممکن است) این دو برابر شدن مصرف حافظه است.

**Why it matters:** روی دستگاه‌های ضعیف (که `HardwarePerformanceEvaluator` وجودشان را تصدیق می‌کند) و با یک بازه‌ی تاریخی وسیع، این می‌تواند به فشار حافظه منجر شود.

**Recommended Fix:** با اعمال راه‌حل `combine` (بخش Compose)، فهرست فیلترشده به یک `StateFlow` مشتق‌شده تبدیل می‌شود که با `SharingStarted.WhileSubscribed` وقتی هیچ‌کس مشترک نیست آزاد می‌شود.

**Priority:** MEDIUM · **Effort:** Medium

---

### [LOW] `ChatNotificationWorker` و چرخه‌ی حیات دیتابیس

**File:** `app/src/main/java/com/atk/atk_cargo/workers/ChatNotificationWorker.kt`، `data/db/AppDatabase.kt`

**Problem:**
`AppDatabase.getDatabase(context)` یک singleton است که در `AppModule.kt:36` هم به‌عنوان `single` ثبت شده. اگر Worker نمونه‌ی جداگانه‌ای بسازد، دو نمونه‌ی Room با دو connection pool خواهیم داشت.

**Why it matters:** Potential Issue — نیازمند بررسی دقیق‌تر است. اگر `getDatabase` خودش یک singleton درست با `synchronized` باشد، مشکلی نیست.

**Recommended Fix:** تأیید کنید که `AppDatabase.getDatabase` واقعاً یک نمونه برمی‌گرداند و Worker از همان طریق (یا از Koin) آن را می‌گیرد.

**Priority:** LOW · **Effort:** Low

---

## PHP Backend Audit

### نکات مثبت

- **همه‌ی کوئری‌ها prepared statement هستند.** جستجوی هدفمند برای الحاق رشته در SQL، هیچ موردی با ورودی کاربر پیدا نکرد. سه مورد درج مستقیم متغیر در SQL وجود دارد که همگی امن‌اند:
  - `SessionRepository.php:201` — `$placeholders` که فقط از `str_repeat('?,')` ساخته می‌شود.
  - `UserRepository.php:104` — `$fieldsStr` که با allow-list صریح `UPDATABLE_COLUMNS` محافظت شده (خطوط ۷۸–۹۸) و در صورت کلید غیرمجاز `InvalidArgumentException` می‌اندازد.
  - `export_schema.php:54,178,188` — که CLI-only است (خط ۲۰۵).
- **`PDO::ATTR_EMULATE_PREPARES => false`** (`Database.php:45`) — prepared statement واقعی سمت سرور، نه شبیه‌سازی.
- **`declare(strict_types=1)`** در تمام فایل‌های `src/`.
- **`hash_equals` برای مقایسه‌ی توکن و CSRF** — مقاوم به timing attack.
- **`random_bytes` برای تولید توکن** — CSPRNG درست.
- **`bcrypt` با `cost => 12`** و `password_needs_rehash`.
- **Refresh token rotation با تشخیص reuse** (`SessionService.php:139-143`) — وقتی یک refresh token قبلاً چرخانده‌شده دوباره استفاده شود، **تمام** نشست‌های کاربر باطل می‌شوند. این یک الگوی پیشرفته و درست است.
- **`export_schema.php` به CLI محدود شده** با کامنت صریح درباره‌ی آسیب‌پذیری قبلی — نمونه‌ی خوبی از اصلاح مستندشده.
- **PHPStan و PHPUnit پیکربندی شده‌اند** (`phpstan.neon`, `phpunit.xml`).

### یافته‌ها

---

### [HIGH] `migrations/` خالی است و `schema.sql` ناقص — جدول `audit_log` هرگز ساخته نمی‌شود

**File:** `PHP/migrations/` (خالی)، `PHP/schema.sql`، `PHP/src/Services/AuditLogger.php`

**Problem:**
`AuditLogger::log` در `INSERT INTO audit_log ...` می‌نویسد و صراحتاً هر خطا را می‌بلعد:
```php
} catch (\Throwable $e) {
    error_log('AuditLogger: failed to write audit log entry - ' . $e->getMessage());
}
```
کامنت بالای کلاس دلیل را می‌گوید: «قبل از اجرای migration مربوطه، جدول audit_log هنوز وجود ندارد». اما:
- پوشه‌ی `migrations/` **کاملاً خالی است** — هیچ migrationی وجود ندارد.
- `schema.sql` جدول `audit_log` را **ندارد** (تأیید شد: `grep -c "admin_chat\|audit" schema.sql` → **0**).
- `schema.sql` جداول `admin_chat_messages` و `admin_chat_reads` را هم ندارد، در حالی که `ChatController` کاملاً به آن‌ها وابسته است.

**Why it matters:**
دو پیامد جدی: (۱) **audit trail یک no-op بی‌صدا است** مگر اینکه کسی دستی جدول را ساخته باشد — یعنی `createUser`, `updateUser`, `deleteUser`, `editQuota`, `deleteQuota` هیچ ردی از خود به‌جا نمی‌گذارند، در حالی که کد طوری نوشته شده که انگار می‌گذارند. برای سیستمی با اثر مالی/عملیاتی، نبود audit trail قابل اتکا یک نقص جدی است. (۲) `schema.sql` به‌عنوان مستند ساختار دیتابیس **غیرقابل اعتماد** است — نمی‌توان با آن یک محیط تازه ساخت.

**Impact:**
نبود قابلیت ردیابی عملیات حساس؛ عدم امکان بازسازی محیط؛ ریسک واگرایی بین محیط‌ها.

**Recommended Fix:**
1. `schema.sql` را با `export_schema.php` (که CLI-only و آماده است) از دیتابیس تولید بازتولید کنید:
```bash
php PHP/export_schema.php --db=atk_cargo --output=PHP/schema.sql
```
2. یک مکانیزم migration واقعی اضافه کنید (حتی ساده: فایل‌های `NNN_description.sql` + یک جدول `schema_migrations` + یک اسکریپت اجرای CLI).
3. `AuditLogger` را طوری تغییر دهید که **نبود جدول** را از **خطای گذرا** تفکیک کند و در حالت اول یک هشدار برجسته لاگ کند (نه یک `error_log` معمولی که گم می‌شود).
4. یک health-check endpoint (فقط برای ادمین) که وجود جداول لازم را تأیید کند.

**Priority:** HIGH · **Effort:** Medium

---

### [MEDIUM] `execute()` در پروکسی فقط `Exception` را می‌گیرد، نه `Throwable`

**File:** `PHP/protected_proxy.php`
**Location:** خطوط ۱۷۹–۱۹۰، و بلوک بیرونی ۲۴۲–۲۵۳

**Problem:**
```php
ob_start();
try {
    include $target;
    $output = ob_get_contents();
} catch (Exception $e) {          // ← نه Throwable
    $output = json_encode(['error' => 'Execution error: ' . $e->getMessage(), ...]);
} finally {
    ob_end_clean();
}
```
در PHP 7+ خطاهای مهلک (`TypeError`, `ArgumentCountError`, `Error`) از `Exception` ارث نمی‌برند بلکه از `Throwable`. یک `TypeError` در فایل هدف از این `catch` عبور می‌کند، `ob_end_clean()` در `finally` بافر را پاک می‌کند، و پاسخ ممکن است ناقص یا شامل خروجی خطای PHP باشد.

علاوه بر این، پیام `$e->getMessage()` مستقیماً به کلاینت برمی‌گردد (نشت جزئیات داخلی — هم‌راستا با یافته‌ی MEDIUM قبلی).

**Recommended Fix:**
```php
} catch (\Throwable $e) {
    error_log('Proxy target error (' . $target . '): ' . $e->getMessage());
    $output = json_encode(['error' => 'خطای داخلی سرور رخ داده است.'], JSON_UNESCAPED_UNICODE);
}
```
همین را در بلوک بیرونی (خط ۲۴۵) هم اعمال کنید.

**Priority:** MEDIUM · **Effort:** Low

---

### [MEDIUM] `flushLog` پروکسی وقتی هدف `exit` می‌کند اجرا نمی‌شود

**File:** `PHP/protected_proxy.php`
**Location:** خطوط ۹۶–۱۰۲، ۲۵۵–۲۶۰

**Problem:**
`Response::json()` (که تقریباً همه‌ی کنترلرها استفاده می‌کنند) با `exit` تمام می‌شود (`Response.php:58`). وقتی `include $target` به آن می‌رسد، اسکریپت خاتمه می‌یابد و خطوط ۲۵۵–۲۶۰ (`fastcgi_finish_request()` و `$proxy->flushLog()`) **هرگز اجرا نمی‌شوند**.

**Why it matters:**
`proxy_access.log` عملاً فقط مسیرهای خطا (`MISSING_TARGET`, `INVALID_TARGET`, `RATE_LIMIT_EXCEEDED`, `BLOCKED_IP_ACCESS`) را ثبت می‌کند — یعنی دقیقاً همان `PROXY_ACCESS` موفق که برای تحلیل الگوی ترافیک و تشخیص نفوذ لازم است، ثبت نمی‌شود.

**Impact:** لاگ دسترسی ناقص؛ کاهش قابلیت تشخیص و پاسخ به حادثه.

**Recommended Fix:**
```php
$proxy = new ProtectedProxy();
register_shutdown_function([$proxy, 'flushLog']);   // ← مستقل از exit اجرا می‌شود
$proxy->handle();
```
همان الگویی که `Logger::log` (خطوط ۴۳–۴۶) از قبل درست استفاده می‌کند.

**Priority:** MEDIUM · **Effort:** Low

---

### [LOW] `Config::$settings['session_timeout']` و `admin_password_hash` بلااستفاده‌اند

**File:** `PHP/src/Core/Config.php:24-25`

**Problem:** هیچ کدی این دو کلید را نمی‌خواند (`session_timeout` واقعی در `SessionRepository::SESSION_TIMEOUT_SECONDS` است، و `PermissionManager` مستقیماً از env می‌خواند). کد مرده که می‌تواند گمراه‌کننده باشد.

**Recommended Fix:** حذف کنید، یا `SessionRepository` را وادار کنید مقدارش را از `Config` بگیرد تا یک منبع حقیقت واحد باشد.

**Priority:** LOW · **Effort:** Low

---

## API Audit

### فهرست endpointها

#### نسخه ۱ — از طریق `protected_proxy.php?target=` (۱۰۰٪ ترافیک کلاینت)

| Target | Method | Auth | Permission | ورودی | مشکلات |
|---|---|---|---|---|---|
| `check_Auth.php` | POST | ❌ (نقطه‌ی ورود) | — | JSON body | همیشه HTTP 200 حتی در شکست؛ brute-force |
| `check_session.php` | POST | ❌ | — | JSON body | ۲۰۰ در شکست (عمدی، مستند) |
| `check_logout.php` | POST | ❌ | — | JSON body | **بدون auth** — نیازمند بررسی |
| `app_api.php` | GET/POST | ✅ | ⚠️ فقط write | `action=` | **بدون `view_reports` روی خواندن** |
| `users_api.php` | GET/POST | ✅ | جزئی | `action=` | `getAllUsers` بدون مجوز |
| `chat_api.php` | GET/POST | ✅ | isAdmin داخلی | `action=` | نشت خطای SQL؛ `limit` بدون سقف |
| `realTimeLoadingData.php` | GET/POST | ✅ | `view_reports` | `action=` | ✅ |
| `quota_remaining_api.php` | GET | ✅ | ? | query | — |
| `saveOrUpdateCargoInfo.php` | POST | ✅ | — | JSON body | ✅ هویت از نشست |
| `updateCargoInfo.php` | POST | ✅ | `edit_cargo` | JSON body | ✅ |
| `deleteCargoInfo.php` | POST | ✅ | `delete_cargo` + رمز | JSON body | ✅ الگوی بسیار خوب |
| `confirm_cargo.php` | POST | ✅ | `cargo_counter` | JSON body | ✅ ضد-IDOR |
| `saveInitialInfo.php` | POST | ✅ | `initial_info` | JSON body | نشت `$stmt->error` |
| `getInitialInfo.php` | GET | ✅ | — | query | ✅ |
| `getActiveShips.php` | GET | ✅ | — | — | ✅ |
| `checkExistence.php` | POST | ✅ | — | JSON | — |
| `check_password.php` | POST | ✅ | — | form | — |
| `check_scale_receipt.php` | GET | ✅ | — | query | ✅ |
| `search_by_tracking.php` | GET | ✅ | — | query | ✅ |
| `search_by_scaleReceipt.php` | GET | ✅ | — | query | ✅ |
| `sync_permissions.php` | POST | ⚠️ | — | JSON | طبق کامنت `api_v2.php:482`, در v1 توکن بررسی نمی‌شود |
| `update_fcm_token.php` | POST | ✅ | — | JSON | ✅ user_id از نشست |
| `check_update.php` | GET | ❌ (API key) | — | query | — |
| `check_signature.php` | POST | ❌ | — | JSON | — |
| `validate_license.php` | POST | ❌ | — | JSON | — |
| `get_license_info.php` | GET | ❌ | — | query | — |

#### نسخه ۲ — `api/v2/index.php?route=` (۴۶ route، فقط `auth/refresh` مصرف می‌شود)

معماری بهتر: `auth` و `permission` صریح در جدول route، گیت در سطح Router (`Router.php:75-80`)، مرتب‌سازی پایدار route بر اساس تعداد پارامتر (`Router.php:35-37`)، و پاسخ ۴۰۵ دقیق به‌جای ۴۰۴ عمومی (`Router.php:86-89`).

### یافته‌ها

---

### [HIGH] دو API stack موازی — نسخه‌ی امن‌تر بلااستفاده است

**File:** `PHP/src/routes/api_v2.php` (۵۹۹ خط)، `PHP/api/v2/index.php`، `PHP/src/Core/Router.php`, `ApiAuthGate.php`, `MinVersionGate.php` در برابر `app/src/main/java/com/atk/atk_cargo/api/ApiService.kt`

**Problem:**
تمام ۴۴ متد `ApiService.kt` به `protected_proxy.php` اشاره می‌کنند:
```kotlin
@GET("protected_proxy.php")
suspend fun getShipsList(@Query("target") target: String = "app_api.php", ...)
```
تنها مصرف‌کننده‌ی v2 در کل کلاینت `TokenRefresher.kt:55` است.

یعنی این‌ها همگی کد بلااستفاده‌اند: `Router.php`, `ApiAuthGate.php`, `MinVersionGate.php`, `api/v2/index.php`, و ۴۵ route از ۴۶ route در `api_v2.php` (~۵۸۰ خط).

**Why it matters:**
سه هزینه‌ی مشخص: (۱) **تمام بهبودهای امنیتی v2 اثری ندارند** — از جمله بستن شکاف `syncPermissions` که کامنت `api_v2.php:482-485` صراحتاً به آن اشاره می‌کند. (۲) **بدهی نگهداری دوبرابر** — هر تغییر منطق باید در هر دو مسیر بررسی شود، و کامنت‌های `api_v2.php` نشان می‌دهند که نویسنده کاملاً از این خطر آگاه بوده (مثلاً «اگر یکی تغییر کرد، دیگری هم باید عمداً بازبینی شود»). (۳) **ریسک واگرایی خاموش** — یک اصلاح امنیتی در یک مسیر ممکن است در دیگری فراموش شود.

**Impact:**
سرمایه‌گذاری انجام‌شده روی v2 بازدهی ندارد؛ ریسک واگرایی امنیتی بین دو مسیر.

**Recommended Fix:**
یک مهاجرت مرحله‌ای و قابل بازگشت:
1. یک `ApiServiceV2` جدید کنار قدیمی بسازید که به `api/v2/index.php?route=...` اشاره کند (الگوی `TokenRefresher` از قبل ثابت کرده که این مسیر روی هاست فعلی کار می‌کند).
2. یک flag محلی (`BuildConfig` یا DataStore) برای انتخاب پیاده‌سازی در `AppModule.kt` اضافه کنید.
3. گروه به گروه مهاجرت دهید — با گروه‌های read-only کم‌ریسک شروع کنید (`ships`, `quotas`).
4. پس از تأیید هر گروه در تولید، مسیر v1 متناظر را از whitelist پروکسی خارج کنید.
5. در پایان، `protected_proxy.php` و فایل‌های entry نسخه ۱ را حذف کنید.

**نکته‌ی مهم:** چون گیت `min_allowed_version` سمت سرور فقط برای کلاینت‌هایی اثر دارد که هدر `X-App-Version` می‌فرستند، و نصب‌های قدیمی همچنان v1 را صدا می‌زنند، حذف v1 باید بعد از اطمینان از به‌روزرسانی همه‌ی نصب‌ها انجام شود.

**Priority:** HIGH · **Effort:** High

---

### [MEDIUM] طراحی API غیر-RESTful و ناسازگار

**File:** `app/src/main/java/com/atk/atk_cargo/api/ApiService.kt`، `PHP/src/Controllers/*`

**Problem:**
چند ناسازگاری ساختاری:

۱. **عملیات در query string، نه در مسیر:** `?target=app_api.php&action=editQuota` — دو لایه dispatch مبتنی بر رشته.

۲. **POST با پارامترها در query string:** `editQuota`, `updateQuotaPercentage`, `deleteQuota` همه `@POST` هستند ولی همه‌ی پارامترها `@Query` هستند:
```kotlin
@POST("protected_proxy.php")
suspend fun deleteQuota(
    @Query("target") target: String = "app_api.php",
    @Query("action") action: String = "deleteQuota",
    @Query("quotaNumber") quotaNumber: String, ...
```
این دقیقاً همان چیزی است که کامنت `AppApiController.php:55-58` می‌خواست از آن اجتناب کند («نبود پارامترهای حساس در Query String لاگ‌های وب‌سرور») — متد POST درست انتخاب شده ولی پارامترها همچنان در URL هستند و در access log ثبت می‌شوند.

۳. **سه شکل متفاوت پاسخ خطا:**
   - `{"error": true, "message": "..."}` — CargoController، UtilityController
   - `{"error": "متن پیام"}` — AppApiController، AnalyticsController
   - `{"success": false, "message": "..."}` — AuthController، UserController، ChatController

   `AuthenticatesRequests.php:57-66` این را مستند کرده و با override قابل تنظیم کرده — یک راه‌حل عملی، ولی ریشه‌ی مشکل حل نشده.

۴. **کد وضعیت HTTP معنادار نیست:** `check_Auth.php` برای «رمز اشتباه» هم ۲۰۰ برمی‌گرداند («برای پایداری با کلاینت اندروید»).

۵. **نوع بازگشتی ناسازگار در Retrofit:** بیشتر متدها `Response<T>` برمی‌گردانند ولی `getAllUsers`, `getAllUsersWithStatus` مستقیماً `List<User>` و `updateUser`/`deleteUser` مستقیماً `ApiResponse` — یعنی برای این‌ها خطاهای HTTP به `HttpException` تبدیل می‌شوند و باید متفاوت مدیریت شوند.

۶. **پارامترهای مرده:** `getChatMessages(username = ...)` و `getUnreadChatCount(username = ...)` هنوز `username` می‌فرستند در حالی که `ChatController.php:45` صراحتاً آن را نادیده می‌گیرد و از نشست می‌خواند.

**Impact:**
منحنی یادگیری بالا؛ خطاهای مدیریت‌نشده در کلاینت؛ عدم امکان تولید مستندات خودکار؛ لاگ شدن پارامترها در وب‌سرور.

**Recommended Fix:**
در جریان مهاجرت به v2 (که از قبل REST-مانند است) این‌ها را حل کنید:
- پارامترهای POST را به body منتقل کنید (`@Body`).
- یک قرارداد خطای واحد: `{"success": bool, "error": {"code": "...", "message": "..."}}`.
- کدهای HTTP معنادار (۴۰۱/۴۰۳/۴۰۹/۴۲۲/۴۲۹) — کلاینت جدید از قبل `HttpStatusException` را دارد.
- همه‌ی متدهای Retrofit `Response<T>` برگردانند.
- پارامترهای مرده را حذف کنید.
- یک فایل OpenAPI بنویسید تا قرارداد یک منبع حقیقت مکتوب داشته باشد.

**Priority:** MEDIUM · **Effort:** High

---

## Database Audit

### ساختار (از `schema.sql` — که ناقص است)

| جدول | PK | ایندکس‌ها | FK |
|---|---|---|---|
| `CargoInfo` | `id` | UNIQUE `scaleReceiptNumber` + ۳ ایندکس ترکیبی پوشا | ❌ |
| `InitialInfo` | `id` | ۱ ایندکس ترکیبی ۵ ستونی | ❌ |
| `Users` | `id` | UNIQUE `username`, `idx_username_userType` | ❌ |
| `user_sessions` | `id` | **۱۳ ایندکس** | ❌ |
| `Passwords` | `id` | — | ❌ |
| `SignChecker` | `id` | — | ❌ |
| `licenses` | `id` | UNIQUE `license_key` | ❌ |
| `admin_chat_messages` | — | **در schema.sql نیست** | ❌ |
| `admin_chat_reads` | — | **در schema.sql نیست** | ❌ |
| `audit_log` | — | **در schema.sql نیست** | ❌ |

### نکات مثبت

- **ایندکس‌های ترکیبی پوشا روی `CargoInfo`** دقیقاً منطبق بر کوئری‌های واقعی:
  `idx_cargo_status_group (loadingQuotaNumber, shipName, loadingWarehouse, shippingCompany, cargoType, status, netWeight)` — دقیقاً همان ستون‌های WHERE در `CargoController::getInitialInfo:507`. این نشان می‌دهد ایندکس‌ها از روی کوئری‌های واقعی طراحی شده‌اند، نه حدسی.
- **`UNIQUE KEY uk_cargo_scale_receipt_number`** — یکتایی شماره‌ی قبض باسکول در سطح دیتابیس اعمال می‌شود، نه فقط در برنامه. (چون MySQL چند `NULL` را در unique index می‌پذیرد، رکوردهای «ورود» که هنوز باسکول نشده‌اند مشکلی ایجاد نمی‌کنند.)
- **`InnoDB` + `utf8mb4_unicode_ci`** در همه‌جا — درست برای فارسی.
- **پشتیبانی از transaction** در `DatabaseManager` (خطوط ۴۲–۵۲).
- **`COALESCE` در SELECT برای سازگاری با قرارداد non-null کلاینت** (`CargoController.php:544-563`) — یک راه‌حل عملی برای مشکل واقعی Gson، با کامنت عالی.

### یافته‌ها

---

### [MEDIUM] هیچ FOREIGN KEY در کل schema وجود ندارد

**File:** `PHP/schema.sql`

**Problem:**
روابط منطقی زیر هیچ محدودیت ارجاعی ندارند:
- `user_sessions.username` → `Users.username`
- `CargoInfo.username` → `Users.username`
- `CargoInfo.loadingQuotaNumber` + کلیدهای مرکب → `InitialInfo`
- `admin_chat_reads.message_id` → `admin_chat_messages.id`
- `admin_chat_messages.username` → `Users.username`

**Why it matters:**
`UserService::deleteUser` (خط ۲۳۹) کاربر را حذف می‌کند و سپس نشست‌ها را غیرفعال (نه حذف) می‌کند. اما رکوردهای `CargoInfo` و `admin_chat_messages` آن کاربر با یک `username` که دیگر وجود ندارد باقی می‌مانند. `SessionRepository::getOnlineUsers` یک `JOIN Users` دارد (خط ۳۱۰) که آن ردیف‌ها را بی‌صدا حذف می‌کند — یعنی نشست‌های یتیم از گزارش‌ها ناپدید می‌شوند بدون هیچ خطایی.

برای `CargoInfo` این عمداً درست است (سابقه‌ی تاریخی حواله نباید با حذف کاربر از بین برود) — اما این تصمیم باید صریح باشد (`ON DELETE SET NULL` یا نگه‌داشتن `username` به‌عنوان یک snapshot تاریخی)، نه نتیجه‌ی نبود محدودیت.

**Impact:** رکوردهای یتیم؛ ناسازگاری خاموش داده؛ عدم امکان تشخیص خرابی داده.

**Recommended Fix:**
FKها را به‌صورت گزینشی و با معنای صریح اضافه کنید:
```sql
ALTER TABLE admin_chat_reads
  ADD CONSTRAINT fk_reads_message FOREIGN KEY (message_id)
  REFERENCES admin_chat_messages(id) ON DELETE CASCADE;

ALTER TABLE user_sessions
  ADD CONSTRAINT fk_sessions_user FOREIGN KEY (username)
  REFERENCES Users(username) ON DELETE CASCADE ON UPDATE CASCADE;
```
برای `CargoInfo` **FK اضافه نکنید** — به‌جای آن مستند کنید که `username`/`userType` یک snapshot تاریخی هستند و عمداً denormalized شده‌اند.
**قبل از اجرا** رکوردهای یتیم موجود را پیدا و پاک‌سازی کنید، وگرنه `ALTER TABLE` شکست می‌خورد.

**Priority:** MEDIUM · **Effort:** Medium

---

### [MEDIUM] `schema.sql` قدیمی و ناقص است

(جزئیات و راه‌حل در بخش «PHP Backend Audit → migrations خالی» آمده — همان یافته با اثر روی دیتابیس.)

**Priority:** MEDIUM (بخشی از یافته‌ی HIGH بالا)

---

### [LOW] `SET SESSION sql_mode = 'NO_ENGINE_SUBSTITUTION'` حالت strict را خاموش می‌کند

**File:** `PHP/src/Core/Database.php`
**Location:** خط ۷۷

**Problem:**
```php
$this->mysqli->query("SET SESSION sql_mode = 'NO_ENGINE_SUBSTITUTION'");
```
این مقدار، `STRICT_TRANS_TABLES` را (که در MySQL 5.7+ پیش‌فرض است) حذف می‌کند. بدون strict mode، MySQL داده‌ی نامعتبر را به‌جای رد کردن، **بی‌صدا تبدیل می‌کند**: رشته‌ی خیلی بلند بریده می‌شود، مقدار عددی نامعتبر صفر می‌شود، تاریخ نامعتبر به `0000-00-00` تبدیل می‌شود.

توجه: این فقط برای اتصال `mysqli` است؛ اتصال PDO (که `SessionRepository` و `UserRepository` استفاده می‌کنند) دست‌نخورده و strict می‌ماند. یعنی **دو اتصال با رفتار متفاوت** در همان برنامه.

**Why it matters:**
`CargoController`, `ChatController`, `CargoRepository`, `PasswordGateService` همگی از mysqli استفاده می‌کنند — یعنی نوشتن داده‌ی حواله (وزن، تناژ، تاریخ) در حالت غیر-strict انجام می‌شود. یک `netWeight` نامعتبر به‌جای خطا، صفر ذخیره می‌شود.

**Impact:** ریسک خرابی خاموش داده در مسیر نوشتن حواله.

**Recommended Fix:**
```php
$this->mysqli->query("SET SESSION sql_mode = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION'");
```
**احتیاط:** این ممکن است نوشتن‌هایی را که امروز بی‌صدا موفق می‌شوند به خطا تبدیل کند. ابتدا در staging با داده‌ی واقعی تست کنید و لاگ خطاها را بررسی کنید — احتمالاً چند مورد داده‌ی نامعتبر که تا امروز پنهان بوده‌اند آشکار می‌شوند (که خودش یک یافته‌ی ارزشمند است).

**Priority:** LOW (ولی با اثر بالقوه بالا) · **Effort:** Medium

---
## Authentication & Authorization

### جریان کامل احراز هویت

```
LoginScreen → AuthViewModel → LoginUseCase → AuthRepositoryImpl
    ↓ hashPassword(password)  ← SHA-256 بدون salt  ⚠️
    ↓ getOrCreateDeviceId()   ← ANDROID_ID، fallback به UUID در DataStore
    ↓ POST protected_proxy.php?target=check_Auth.php
                ↓
        AuthController::login
            ├─ LoginAttemptLimiter::isLocked(username, ip)   ⚠️ کلید شامل IP
            ├─ UserService::verifyCredentials                 ⚠️ bcrypt(sha256)
            ├─ PermissionService::getUserPermissions          ← permissions.json
            └─ SessionService::createMobileSession
                    ├─ تک‌دستگاهی: نشست فعال روی دستگاه دیگر → 409
                    ├─ generateTokenPair: random_bytes(32) × 2
                    │     access  TTL = 1800s  (۳۰ دقیقه)
                    │     refresh TTL = 86400s (۲۴ ساعت)
                    └─ SessionRepository::createSession       ⚠️ plaintext
                ↓
        پاسخ: session_token, refresh_token, userType, permissions
                ↓
UserPreferencesManager.saveUserCredentials / saveRefreshToken
    → CryptoManager.encrypt (AES-GCM، Android Keystore)  ✅
    → DataStore  +  AuthSession (in-memory)

── در هر درخواست بعدی ──
RetrofitClient.headersInterceptor
    → X-Username / X-Device-Id / X-Session-Token / X-App-Version
                ↓
AuthenticatesRequests::requireAuthenticatedSession
    ├─ enforceMinAppVersion (update_config.php)
    ├─ SessionService::validateAndGetUserType  ← ۱ کوئری، userType از خود جدول نشست
    ├─ touchLastActivityThrottled              ← UPDATE فقط اگر >۶۰ ثانیه
    └─ در شکست: تمایز access_token_expired / session_invalid  ✅
                ↓
      401 + code=access_token_expired
                ↓
TokenAuthenticator (OkHttp) → TokenRefresher → POST api/v2/index.php?route=auth/refresh
    → SessionService::refreshTokens
         ├─ hash_equals روی refresh token        ✅
         ├─ عدم تطابق → deactivateAllSessions + لاگ REFRESH_TOKEN_REUSE_DETECTED  ✅
         └─ چرخش هر دو توکن (sliding)            ✅
```

### ارزیابی

| جنبه | ارزیابی |
|---|---|
| تولید توکن | ✅ `random_bytes(32)` — CSPRNG، ۲۵۶ بیت |
| ذخیره‌سازی توکن در کلاینت | ✅ AES-GCM با Android Keystore |
| ذخیره‌سازی توکن در سرور | ❌ **plaintext** |
| چرخش refresh token | ✅ sliding + تشخیص reuse + باطل‌سازی سراسری |
| انقضای access token | ✅ ۳۰ دقیقه |
| انقضای بر اساس عدم فعالیت | ✅ ۲۴ ساعت (`SESSION_TIMEOUT_SECONDS`) |
| پاک‌سازی نشست‌های منقضی | ✅ `cleanupExpiredSessions` وجود دارد |
| تک‌دستگاهی بودن | ✅ اعمال می‌شود (۴۰۹) |
| باطل‌سازی نشست پس از تغییر کاربر | ✅ `UserService::updateUser/deleteUser` |
| مقایسه‌ی مقاوم به timing | ✅ `hash_equals` |
| قدرت رمز عبور | ❌ **۴ رقم عددی** |
| هش رمز | ❌ **SHA-256 بدون salt سمت کلاینت** |
| قفل brute-force | ❌ **قابل دور زدن با چرخش IP** |
| گیت مجوز روی نوشتن | ✅ یکنواخت |
| گیت مجوز روی خواندن | ❌ **در `app_api.php` وجود ندارد** |
| هویت از نشست، نه از ورودی | ✅ اصلاح‌شده و مستند در چند کنترلر |
| تأیید رمز فعلی هنگام تغییر رمز | ❌ وجود ندارد |

### یافته‌ی اضافی

---

### [MEDIUM] `check_logout.php` بدون احراز هویت

**File:** `PHP/src/Controllers/AuthController.php`
**Location:** خطوط ۲۵۸–۲۷۹؛ همچنین `routes/api_v2.php:416-419` (`'auth/logout', 'auth' => false`)

**Problem:**
`logout()` فقط `username` می‌گیرد و بدون هیچ اعتبارسنجی توکنی، `deactivateSession` را صدا می‌زند که **تمام نشست‌های فعال آن کاربر** را باطل می‌کند:
```php
$username = $this->request->get('username');
if (!$username) { Response::error('نام کاربری الزامی است.', 400); }
$username = InputValidator::sanitize((string)$username);
$result = $this->sessionService->deactivateSession($username, $deviceId);
```
هیچ‌جا بررسی نمی‌شود که درخواست‌کننده واقعاً همان کاربر است.

**Why it matters:**
یک مهاجم ناشناس که فقط نام کاربری را می‌داند (که از `getAllUsers` قابل استخراج است) می‌تواند مکرراً هر کاربری — از جمله همه‌ی ادمین‌ها — را از سیستم بیرون بیندازد. این یک **Denial of Service هدفمند** است: با یک اسکریپت ساده که هر چند ثانیه logout همه‌ی کاربران را صدا می‌زند، سیستم عملاً غیرقابل استفاده می‌شود.

نکته: نبود auth روی logout یک تصمیم آگاهانه‌ی رایج است (تا کاربر با توکن منقضی هم بتواند خارج شود)، اما در آن صورت باید **حداقل توکن نشست را به‌عنوان یک ورودی اختیاری بپذیرد و فقط همان نشست را ببندد**، نه همه را بر اساس نام کاربری تنها.

**Impact:** DoS هدفمند علیه همه‌ی کاربران؛ اختلال عملیاتی.

**Recommended Fix:**
```php
public function logout(): void {
    // ... خواندن ورودی‌ها
    $sessionToken = (string)($this->request->get('sessionToken') ?? $this->request->getHeader('X-Session-Token') ?? '');

    // فقط نشستی که توکن معتبرش ارائه شده بسته می‌شود
    if ($deviceId === null || $sessionToken === ''
        || !$this->sessionService->isValidToken($username, $deviceId, $sessionToken)) {
        // پاسخ موفق عمومی می‌دهیم تا از شمارش کاربران جلوگیری شود،
        // ولی هیچ نشستی باطل نمی‌کنیم
        Response::json(['success' => true, 'message' => 'خروج انجام شد.']);
    }
    $result = $this->sessionService->forceLogoutFromDevice($username, $deviceId);
    Response::json(['success' => $result['success'], 'message' => $result['message']]);
}
```
اگر «خروج از همه‌ی دستگاه‌ها» یک قابلیت لازم است، آن را به‌عنوان یک action جداگانه پشت auth کامل قرار دهید. توجه کنید که `users_api.php?action=forceLogout` از قبل این کار را **با** مجوز `manage_users` انجام می‌دهد — یعنی الگوی درست وجود دارد.

**Priority:** MEDIUM · **Effort:** Low

---

## Error Handling

### یافته‌های تجمیعی

| # | مشکل | فایل | شدت |
|---|---|---|---|
| E1 | بسته‌بندی مکرر استثنا (۱۳ مورد) | `ReportsRepository.kt` | MEDIUM |
| E2 | بلعیدن خطا و بازگرداندن `null`/`emptyList()` | `ReportsRepository.kt:409-460` | MEDIUM |
| E3 | نشت پیام استثنای داخلی به کلاینت | `ChatController`, `AppApiController`, `api_v2.php` | MEDIUM |
| E4 | `catch (Exception)` به‌جای `catch (Throwable)` | `protected_proxy.php:183` | MEDIUM |
| E5 | شاخه‌ی ۴۰۱ غیرقابل‌دسترس | `PermissionPoller.kt:127` | HIGH |
| E6 | `catch (e: Exception) { throw e }` بی‌اثر | `ReportsRepository.kt:51-53` | LOW |
| E7 | HTTP 200 برای شکست احراز هویت | `AuthController::login` | LOW (عمدی، مستند) |
| E8 | `FloatTypeAdapter` هر مقدار نامعتبر را به `0f` تبدیل می‌کند | `RetrofitClient.kt:69-89` | LOW |

### نکات مثبت

- **`check_session.php` با ۵۰۳ به‌جای ۲۰۰ در خطای گذرا** (خطوط ۱۶–۱۹) — با کامنت عالی: «۲۰۰ باعث خروج اجباری همه کاربران با هر قطعی موقت دیتابیس/سرور می‌شد». این نوع تفکر fail-safe نادر و ارزشمند است.
- **تمایز `access_token_expired` از `session_invalid`** (`AuthenticatesRequests.php:43-50`) — کلاینت می‌داند آیا ارزش تلاش برای refresh را دارد.
- **`ConflictException` با کد HTTP معنادار خودش** (`CargoController.php:74-79`) — ۴۰۹ به‌جای ۵۰۰ عمومی.
- **`confirmCargo` با ۴۰۹ به‌جای ۲۰۰ در تأیید تکراری** (خطوط ۲۵۴–۲۶۰) — کامنت توضیح می‌دهد که ۲۰۰ باعث می‌شد کلاینت وضعیت محلی را به‌اشتباه «تأیید شده» علامت بزند.
- **`HttpStatusException` در سه متد Repository** — الگوی درست، فقط ناقص اعمال شده.
- **`CryptoManager` که در خطا هرگز مقدار خام برنمی‌گرداند** (خطوط ۶۳–۶۸، ۸۴–۸۹) — تصمیم امنیتی درست با کامنت.

### [MEDIUM] نبود مکانیزم مرکزی گزارش خطا (Crash Reporting)

**Problem:**
هیچ Crashlytics، Sentry، یا هر ابزار گزارش خطای دیگری در پروژه نیست. تنها مکانیزم، `Log.e` است که فقط در Logcat دستگاه دیده می‌شود.

**Why it matters:**
`archiveReleaseMapping` (در `build.gradle.kts:208-217`) با دقت `mapping.txt` را برای هر release آرشیو می‌کند — یعنی تیم به deobfuscate کردن کرش‌های تولید فکر کرده. اما هیچ مکانیزمی برای **دریافت** آن کرش‌ها وجود ندارد. نقشه‌ی گنج بدون گنج.

**Impact:**
کرش‌های تولید فقط وقتی کشف می‌شوند که کاربر تماس بگیرد؛ هیچ داده‌ای درباره‌ی نرخ کرش، دستگاه‌های متأثر، یا رگرسیون پس از release وجود ندارد.

**Recommended Fix:**
یک راه‌حل سبک و خودمیزبان اضافه کنید که با معماری فعلی سازگار باشد: یک `Thread.setDefaultUncaughtExceptionHandler` که stack trace را در DataStore ذخیره می‌کند و در اولین اجرای بعدی به یک endpoint `POST /api/v2/diagnostics/crash` می‌فرستد. با `mapping.txt` آرشیوشده، این کاملاً کافی است و نیازی به سرویس شخص ثالث ندارد.

**Priority:** MEDIUM · **Effort:** Medium

---

## Logging & Observability

### آنچه لاگ می‌شود

| مقصد | محتوا | فایل |
|---|---|---|
| `PHP/logs/{category}.log` | INFO/ERROR/SECURITY/DATABASE با IP و timestamp | `src/Core/Logger.php` |
| `PHP/logs/session_activity.log` | LOGIN / LOGOUT_ALL_SESSIONS / FORCE_LOGOUT / REFRESH_TOKEN_REUSE_DETECTED | `SessionService::logActivity` |
| `PHP/log/proxy_access.log` | IP, Method, Action, Target, User-Agent | `protected_proxy.php` (⚠️ ناقص) |
| `error_log` سیستم | استثناهای غیرمنتظره | سراسر |
| جدول `audit_log` | createUser/updateUser/deleteUser/editQuota/... | `AuditLogger` (⚠️ جدول وجود ندارد) |
| Logcat | ~۹۰ فراخوانی `Log.*` | سراسر کلاینت |

### نکات مثبت

- **`Logger::sanitizeMessage`** (خطوط ۷۵–۸۳) الگوهای `password`, `csrf_token`, `session_token` را در JSON با `***` جایگزین می‌کند.
- **بافر کردن لاگ + `register_shutdown_function`** — I/O از مسیر داغ خارج شده.
- **`AuditLogger` فقط نام فیلدهای تغییریافته را ثبت می‌کند، نه مقدار رمز** (`UserService.php:213-216`).
- **`HttpLoggingInterceptor` در release روی `NONE`**.
- **`.htaccess` پوشه‌های `log|logs` را مسدود می‌کند** (`DirectoryMatch`) — هرچند طبق کامنت خود فایل، `<DirectoryMatch>` در `.htaccess` معتبر نیست و توسط Apache نادیده گرفته می‌شود. **این باید با `.htaccess` داخل خود پوشه‌ی `logs/` جایگزین شود.**

### یافته‌ها

---

### [HIGH] پوشه‌های لاگ ممکن است از طریق وب قابل دسترس باشند

**File:** `PHP/.htaccess:73-76`، `PHP/src/Core/Logger.php:15-18`، `PHP/protected_proxy.php:26-28`

**Problem:**
`Logger` پوشه‌ی `APP_ROOT . '/logs'` و پروکسی پوشه‌ی `__DIR__ . '/log'` را با `mkdir(0755)` **داخل web root** می‌سازند. تنها محافظت، این بلوک در `.htaccess` است:
```apache
<DirectoryMatch "(^|/)(log|logs|backups|private|secret)/">
    Order allow,deny
    Deny from all
</DirectoryMatch>
```
اما `<DirectoryMatch>` یک directive سطح `httpd.conf` است و **در `.htaccess` مجاز نیست** — Apache یا آن را نادیده می‌گیرد یا خطای ۵۰۰ می‌دهد. بلوک `<FilesMatch "\.(...|log|...)$">` بالاتر در همان فایل، فایل‌های با پسوند `.log` را مسدود می‌کند — که خوشبختانه اکثر فایل‌های لاگ را پوشش می‌دهد. اما فایل‌های `PasswordGateService`/`LoginAttemptLimiter` پسوند `.json` دارند:
```php
// LoginAttemptLimiter.php:78
return $dir . "/loginlimit_$safeKey.json";
// PasswordGateService.php:116
return $dir . "/pwdgate_$safeKey.json";
```
و `protected_proxy.php:129` فایل‌های `rate_limit_*.txt` می‌سازد، و `blocked_ips.txt`. هیچ‌کدام از این‌ها با `FilesMatch` موجود مسدود نمی‌شوند.

**Why it matters:**
نام فایل‌های `loginlimit_*.json` شامل **نام کاربری و IP** است (`loginlimit_admin_1_2_3_4.json`) — یعنی اگر directory listing فعال باشد یا نام قابل حدس زدن باشد، مهاجم می‌تواند وضعیت قفل حساب‌ها را بخواند و بفهمد کدام حساب‌ها هدف حمله‌اند یا قفل شده‌اند. `Options -Indexes` در ابتدای `.htaccess` هست که listing را می‌بندد، ولی حدس زدن نام برای یک نام کاربری شناخته‌شده ساده است.

**Impact:** افشای وضعیت قفل حساب و IPهای مرتبط؛ نشت اطلاعات کمکی برای مهاجم.

**Recommended Fix:**
1. یک `.htaccess` مستقیماً در هر پوشه‌ی `logs/` و `log/` بسازید (این تنها روش قابل اتکاست):
```apache
Require all denied
```
2. بهتر: پوشه‌های لاگ و state را کاملاً خارج از web root منتقل کنید:
```php
// در bootstrap.php
define('APP_STORAGE', dirname(APP_ROOT) . '/atk_storage');
```
3. `FilesMatch` را گسترش دهید تا `.json` و `.txt` در این مسیرها را هم بگیرد.

**Priority:** HIGH · **Effort:** Low

---

### [MEDIUM] نبود مانیتورینگ و هشدار

**Problem:**
هیچ health-check endpoint، هیچ متریک، و هیچ مکانیزم هشداری وجود ندارد. رویدادهای امنیتی مهم مثل `REFRESH_TOKEN_REUSE_DETECTED` (که نشانه‌ی احتمالی سرقت توکن است) فقط در یک فایل متنی نوشته می‌شوند که کسی نمی‌خواند.

**Recommended Fix:**
1. یک `GET /api/v2/health` که وضعیت اتصال DB، وجود جداول لازم، و در دسترس بودن APCu را برمی‌گرداند.
2. برای رویدادهای امنیتی بحرانی (`REFRESH_TOKEN_REUSE_DETECTED`، قفل شدن مکرر یک حساب، دسترسی از IP مسدود) یک اعلان فوری (ایمیل/تلگرام) بفرستید.
3. یک چرخش لاگ (logrotate) تنظیم کنید — در حال حاضر فایل‌های لاگ بی‌نهایت رشد می‌کنند.

**Priority:** MEDIUM · **Effort:** Medium

---

### [LOW] ~۹۰ فراخوانی `Log.*` در کد تولید

**File:** بیشترین تراکم: `ChatRepository.kt` (15)، `SecurityVerifier.kt` (12)، `ChatViewModel.kt` (9)، `PermissionPoller.kt` (8)، `CargoViewModel.kt` (7)

**Problem:**
`Log.d`/`Log.i`/`Log.w`/`Log.e` در بیلد release هم اجرا می‌شوند (ProGuard به‌طور پیش‌فرض آن‌ها را حذف نمی‌کند مگر با قاعده‌ی صریح). بررسی نمونه‌ای نشان داد که هیچ رمز یا توکنی مستقیماً لاگ نمی‌شود — این خوب است. اما `PermissionPoller.kt:125` کلیدهای مجوز کاربر و `SecurityVerifier` جزئیات محیط اجرا را لاگ می‌کند.

**Recommended Fix:**
در `proguard-rules.pro`:
```proguard
-assumenosideeffects class android.util.Log {
    public static int d(...);
    public static int v(...);
    public static int i(...);
}
```
(`Log.w`/`Log.e` را نگه دارید — برای عیب‌یابی میدانی لازم‌اند.) **قبل از اعمال** مطمئن شوید هیچ `Log.d` با side effect (مثل فراخوانی تابعی که کار مفیدی می‌کند) وجود ندارد.

**Priority:** LOW · **Effort:** Low

---

## Dependency Audit

### Android

| وابستگی | نسخه | ارزیابی |
|---|---|---|
| AGP | 8.13.0 | ✅ به‌روز |
| Kotlin | 2.2.20 | ✅ به‌روز |
| KSP | 2.2.20-2.0.3 | ✅ هم‌راستا با Kotlin |
| Compose BOM | 2025.09.00 | ✅ به‌روز |
| Retrofit | 3.0.0 | ✅ به‌روز |
| OkHttp | 5.1.0 | ✅ به‌روز |
| Room | 2.7.0 | ✅ |
| Koin | 3.5.6 | ⚠️ نسخه‌ی ۴.x موجود است |
| Coroutines | 1.10.2 | ✅ |
| Navigation Compose | 2.9.4 | ✅ |
| WorkManager | 2.10.4 | ✅ |
| `itextpdf` | **5.5.13.4** | ⚠️ **پایان پشتیبانی** — iText 5 در حالت maintenance است و مجوز AGPL دارد |
| `material3` | 1.3.2 | ⚠️ عقب‌تر از BOM؛ نسخه‌ی صریح BOM را override می‌کند |
| `zxing-android-embedded` | 4.3.0 | ✅ استفاده می‌شود (`journeyapps`) |
| `lottie-compose` | 6.6.9 | ✅ استفاده می‌شود |
| `appcompat`, `constraintlayout`, `navigation-fragment/ui`, `material`, `coil` | — | ❌ **بلااستفاده** |
| `tensorflow-lite*`, `poi*`, `jxl`, `icu4j`, `konfetti`, `media3*`, `json`, `tasks-vision` | — | ❌ **در catalog تعریف شده ولی استفاده نمی‌شود** |
| `kotlinx-coroutines-test`, `turbine`, `mockk`, `robolectric` | — | ❌ **وجود ندارند** (مانع نوشتن تست) |

---

### [MEDIUM] `itextpdf 5.5.13.4` — مجوز AGPL و پایان پشتیبانی

**File:** `app/build.gradle.kts:297`, `gradle/libs.versions.toml`

**Problem:**
iText 5 تحت **AGPLv3** منتشر می‌شود. برای یک محصول اختصاصی و بسته (که `composer.json` صراحتاً `"license": "proprietary"` دارد)، توزیع نرم‌افزاری که با AGPL لینک شده، الزام انتشار کد منبع کل برنامه را ایجاد می‌کند — مگر اینکه مجوز تجاری خریداری شده باشد.

علاوه بر این، iText 5 دیگر ویژگی جدید یا رفع باگ امنیتی دریافت نمی‌کند.

**Why it matters:**
این یک ریسک حقوقی است، نه فقط فنی. تنها یک فایل از آن استفاده می‌کند (`feature/reports/domain/ExportPdfUseCase.kt`) — یعنی هزینه‌ی تعویض کم است.

**Recommended Fix:**
یکی از این سه:
1. **مجوز تجاری iText** بخرید (اگر ویژگی‌های آن لازم است).
2. مهاجرت به **`android.graphics.pdf.PdfDocument`** (بخشی از خود اندروید، بدون وابستگی) — برای گزارش‌های جدولی ساده کافی است.
3. مهاجرت به **Apache PDFBox (Android port)** با مجوز Apache 2.0.

با توجه به اینکه فقط یک use case دارد، گزینه‌ی ۲ احتمالاً کم‌هزینه‌ترین است. **توجه:** پشتیبانی از فارسی و RTL باید در هر گزینه‌ی جایگزین تأیید شود — این احتمالاً دلیل انتخاب iText بوده است.

**Priority:** MEDIUM (حقوقی: HIGH) · **Effort:** Medium

---

### [LOW] `material3 = "1.3.2"` صریح، نسخه‌ی BOM را override می‌کند

**File:** `gradle/libs.versions.toml`

**Problem:** وقتی `platform(libs.compose.bom)` استفاده می‌شود، نسخه‌های Compose باید از BOM بیایند. تعریف صریح `material3 = "1.3.2"` هدف BOM (تضمین سازگاری) را نقض می‌کند و می‌تواند به ناسازگاری نسخه منجر شود.

**Recommended Fix:** نسخه را از `libs.versions.toml` حذف و به `androidx-material3 = { module = "androidx.compose.material3:material3" }` (بدون `version.ref`) تغییر دهید تا BOM آن را مدیریت کند. همین کار را برای `foundation`, `ui`, `ui-tooling*` هم انجام دهید.

**Priority:** LOW · **Effort:** Low

---

### PHP

| وابستگی | نسخه | ارزیابی |
|---|---|---|
| `php` | `>=7.4` | ⚠️ PHP 7.4 در ۲۰۲۲ EOL شد؛ کد از `str_starts_with` (PHP 8) استفاده می‌کند → **قید نادرست است** |
| `phpstan/phpstan` | `^1.10` | ⚠️ نسخه‌ی ۲.x موجود است |
| `phpunit/phpunit` | `^9.6` | ⚠️ نسخه‌ی ۱۱.x موجود است |

---

### [MEDIUM] `composer.json` قید `php: >=7.4` دارد ولی کد PHP 8 لازم دارد

**File:** `PHP/composer.json:14-16`، `PHP/src/Services/UserService.php:41`

**Problem:**
```json
"require": { "php": ">=7.4" }
```
اما کد از توابع PHP 8.0 استفاده می‌کند:
```php
// UserService.php:41
&& (str_starts_with($storedPassword, '$2y$') || str_starts_with($storedPassword, '$2a$'))
```
`str_starts_with` در PHP 8.0 اضافه شد. همچنین `str_contains` در `protected_proxy.php:167`.

**Why it matters:**
اگر کسی این پروژه را روی PHP 7.4 (که composer اجازه می‌دهد) نصب کند، `composer install` موفق می‌شود و برنامه در زمان اجرا با `Call to undefined function` از کار می‌افتد — و دقیقاً در مسیر ورود کاربر. علاوه بر این، PHP 7.4 دیگر هیچ به‌روزرسانی امنیتی دریافت نمی‌کند.

**Recommended Fix:**
```json
"require": { "php": ">=8.1" }
```
و در `phpstan.neon` مقدار `phpVersion` را هم‌راستا کنید. نسخه‌ی PHP سرور تولید را تأیید کنید (احتمالاً از قبل ۸.x است، وگرنه سیستم امروز کار نمی‌کرد).

**Priority:** MEDIUM · **Effort:** Low

---

## Testing Audit

### وضعیت فعلی

| نوع | تعداد | فایل‌ها |
|---|---:|---|
| Unit Test (Kotlin) | **1 واقعی** | `test/.../feature/reports/domain/ReportsDomainTest.kt` |
| Stub تولیدشده | 2 | `ExampleUnitTest.kt`, `ExampleInstrumentedTest.kt` |
| Unit Test (Kotlin) — امنیتی | 1 | `SecurityUtilsTest.kt` |
| UI/Compose Test | **0** | — |
| ViewModel Test | **0** | — |
| Repository Test | **0** | — |
| Unit Test (PHP) | **1** | `tests/Unit/Services/QuotaCalculatorTest.php` (۱۸۶ خط) |
| Integration/API Test | **0** | — |

**نسبت تقریبی: ۳ فایل تست معنادار در برابر ~۶۲,۰۰۰ خط کد.**

علاوه بر این، وابستگی‌های لازم برای نوشتن تست معنادار وجود ندارند: نه `kotlinx-coroutines-test`، نه `turbine`، نه `mockk`/`mockito`، نه `robolectric`. یعنی حتی اگر کسی امروز بخواهد یک تست ViewModel بنویسد، اول باید build را تغییر دهد.

### [CRITICAL] نبود پوشش تست روی مسیرهای بحرانی

**Why it matters:**
این سیستم داده‌ی مالی/عملیاتی می‌نویسد: تناژ، وزن خالص، کسری و اضافه بار، شماره‌ی قبض باسکول. یک خطای محاسباتی مستقیماً به اختلاف مالی منجر می‌شود. با این حال:
- `QuotaCalculator` تست دارد ✅ (تنها بخش منطق تجاری که تست شده)
- `QuotaService` (۷۰۴ خط، منطق اصلی کوتاژ) — بدون تست
- `CargoService` (۳۸۱ خط) — بدون تست
- `SessionService`/`SessionRepository` (کل منطق احراز هویت و چرخش توکن) — بدون تست
- `PermissionService` (کل کنترل دسترسی) — بدون تست
- `QuotaValidationUseCase`، `InitialInfoValidator`، `HomeQuotaParser` — بدون تست
- تبدیل تاریخ جلالی (دو پیاده‌سازی متفاوت!) — بدون تست

منطق چرخش refresh token با تشخیص reuse (`SessionService::refreshTokens`) یکی از پیچیده‌ترین و حساس‌ترین بخش‌های کد است و هیچ تستی ندارد — در حالی که دقیقاً همان نوع منطقی است که یک باگ خاموش در آن می‌تواند یا کاربران را قفل کند یا امنیت را بشکند.

### Test Caseهای پیشنهادی (به ترتیب اولویت)

**Phase 1 — منطق تجاری خالص (بدون نیاز به mock، سریع):**
```php
// PHP — QuotaServiceTest
- محاسبه‌ی تناژ باقی‌مانده با/بدون تناژ موقت
- رفتار در مرز درصد مجاز (۹۹.۹٪ / ۱۰۰٪ / ۱۰۰.۱٪)
- checkQuotaStatus وقتی کوتاژ غیرفعال است
```
```kotlin
// Kotlin — JalaliDateUtilsTest  (دو پیاده‌سازی را با هم مقایسه کنید!)
- ۱ فروردین، ۲۹ و ۳۰ اسفند، سال کبیسه ۱۴۰۳ و ۱۴۰۸
- QuotaValidationUseCaseTest: هر شاخه‌ی isValid/percentageReached/tempTonnage
- InitialInfoValidatorTest, HomeQuotaParserTest
```

**Phase 2 — احراز هویت و مجوز (نیازمند DB تست):**
```php
- SessionServiceTest: چرخش موفق، refresh token منقضی، reuse detection
- PermissionServiceTest: fallback نقش→کاربر، فایل مفقود (باید fail-closed باشد)
- LoginAttemptLimiterTest: شمارش، قفل، reset  ← و تست رگرسیون برای کلید جدید
```

**Phase 3 — ViewModel و Repository:**
```kotlin
- CargoViewModelTest با mock ReportsRepository + Turbine روی StateFlow
- AuthRepositoryImplTest با MockWebServer
- TokenAuthenticatorTest: ۴۰۱ با/بدون code، جلوگیری از حلقه، رفتار هم‌زمانی
```

**Phase 4 — Compose:**
```kotlin
- تست‌های createComposeRule روی composableهای بی‌حالت (که پس از refactor به‌وجود می‌آیند)
```

**Priority:** CRITICAL (به‌عنوان یک قابلیت زیرساختی) · **Effort:** High

---

## Code Quality

### Code Smells شناسایی‌شده

| Smell | محل | جزئیات |
|---|---|---|
| **God Composable** | ۱۰ فایل بالای ۱۰۰۰ خط | `SelectInfoScreen.kt` (2223)، `QuotaManagementDialog.kt` (1688)، `QuotasListScreen.kt` (1590) |
| **God ViewModel** | `CargoViewModel.kt` (980)، `ReportsViewModel.kt` (925) | ۲۰+ StateFlow، شبکه + منطق + فرمت |
| **God Controller** | `CargoController.php` (723) | ۹ endpoint در یک کلاس |
| **God Service** | `QuotaService.php` (704)، `AnalyticsController.php` (693) | — |
| **Duplicate Code** | `gregorianToJalali` × ۲ | `CargoViewModel.kt:947` و `JalaliDateUtils.kt:70` |
| **Duplicate Code** | `enforceMinAppVersion` × ۲ | `AuthenticatesRequests.php:89` و `MinVersionGate.php:18` (عمدی و مستند) |
| **Duplicate Code** | الگوی fallback APCu→فایل × ۳ | `LoginAttemptLimiter`, `PasswordGateService`, `ProtectedProxy::checkRateLimit` |
| **Duplicate Code** | بلوک `htmlspecialchars` × ۲۰ فیلد | `CargoController.php:345-366` و `:403-425` |
| **Dead Code** | ۴۵ از ۴۶ route نسخه ۲ | `api_v2.php` |
| **Dead Code** | شاخه‌ی ۴۰۱ در PermissionPoller | `PermissionPoller.kt:127-132` |
| **Dead Code** | `Config::session_timeout`, `admin_password_hash` | `Config.php:24-25` |
| **Dead Code** | پارامتر `username` در APIهای چت | `ApiService.kt:377,405` |
| **Dead Config** | `viewBinding`, ۶ وابستگی بلااستفاده، ۹ entry در catalog | `build.gradle.kts`, `libs.versions.toml` |
| **Magic Numbers** | `25000.0` / `15000.0` (ظرفیت کامیون) | `CargoViewModel.kt:924-925` |
| **Magic Numbers** | `300` (آستانه‌ی آنلاین) | `UserService.php:103` |
| **Magic Numbers** | `30000` (فاصله‌ی polling) × ۴ فایل | `SelectInfoScreen`, `CargoDetailsScreen`, `CargoCounterScreen`, ... |
| **Magic Strings** | `"ورود"` / `"خروج"` / `"تائید شده"` | سراسر کلاینت و سرور — بدون enum |
| **Primitive Obsession** | وزن/تناژ به‌عنوان `String` | `CargoInfo.netWeight: String`, `shortageWeight: String` |
| **Long Method** | `CargoController::getInitialInfo` (140 خط) | `CargoController.php:448-587` |
| **Long Method** | `AppApiController::handle` (280 خط switch) | `AppApiController.php:68-348` |
| **Long Parameter List** | `SessionService::createMobileSession` (۷ پارامتر) | `SessionService.php:24-32` |
| **Long Parameter List** | `CargoViewModel::prepareCargoInfoForSubmission` (۹ پارامتر) | `CargoViewModel.kt:391-401` |
| **Feature Envy** | `UserController` که `getAllUsers` را برای یافتن یک رکورد می‌خواند | `UserController.php:159-160` |
| **Shotgun Surgery** | افزودن یک endpoint = تغییر در ۴ جا | `ApiService.kt` + entry file + Controller + `api_v2.php` |
| **Tight Coupling** | `CargoViewModel` → `RetrofitClient` (singleton) | `CargoViewModel.kt:8` |

### مهم‌ترین‌ها با جزئیات

---

### [HIGH] Primitive Obsession روی مقادیر وزن و تناژ

**File:** `app/src/main/java/com/atk/atk_cargo/api/DataModel.kt` / `data/model/CargoModels.kt`

**Problem:**
`netWeight`, `shortageWeight`, `excessWeight`, `loadingQuotaNumber` همگی `String` هستند. در سراسر کد این تبدیل‌ها تکرار می‌شوند:
```kotlin
// CargoViewModel.kt:801
val netWeights = exitedCargos.mapNotNull { it.netWeight.toFloatOrNull() }
// CargoViewModel.kt:804
val cargoWeightValue = _cargoWeight.value.replace(",", "").toFloatOrNull() ?: 0f
// CargoViewModel.kt:817
val loadableTonnageValue = _loadableTonnage.value.replace(",", "").toDoubleOrNull() ?: 0.0
```
توجه کنید که `_loadableTonnage` **با کاما فرمت شده** ذخیره می‌شود (خط ۷۱۸: `DecimalFormat("#,###").format(...)`) و بعد برای محاسبه دوباره کاماها حذف می‌شوند — یعنی مقدار نمایشی و مقدار محاسباتی در همان متغیر ذخیره شده‌اند.

**Why it matters:**
هر `?: 0f` یک خطای بی‌صدا است: یک وزن نامعتبر به صفر تبدیل می‌شود و در محاسبه‌ی «تناژ باقی‌مانده» و «تعداد سرویس باقی‌مانده» وارد می‌شود — که مستقیماً تصمیم عملیاتی می‌سازد. در ترکیب با `FloatTypeAdapter` که آن هم `0f` برمی‌گرداند، دو لایه‌ی مستقل «تبدیل بی‌صدا به صفر» روی همان مسیر داریم.

**Impact:** ریسک محاسبه‌ی نادرست تناژ و تعداد سرویس — با اثر عملیاتی و مالی مستقیم.

**Recommended Fix:**
1. مدل‌های دامنه را از DTO جدا کنید. DTO می‌تواند `String` باشد (برای سازگاری با سرور)، اما مدل دامنه باید تایپ‌شده باشد:
```kotlin
@JvmInline value class Kilograms(val value: Long)
data class CargoRecord(
    val netWeight: Kilograms?,          // null = هنوز باسکول نشده — با ۰ فرق دارد
    val shortage: Kilograms?,
    ...
)
```
2. فرمت‌بندی (`DecimalFormat`) را به لایه‌ی UI منتقل کنید؛ ViewModel باید عدد نگه دارد، نه رشته‌ی فرمت‌شده.
3. تمایز `null` (نامشخص) از `0` (واقعاً صفر) را حفظ کنید — این تمایز امروز کاملاً گم شده است.

**Priority:** HIGH · **Effort:** High

---

### [MEDIUM] Magic Strings برای وضعیت حواله

**File:** سراسر — `CargoViewModel.kt:417,800`, `CargoController.php:503-505,564`, `CargoDetailsScreen.kt`, ...

**Problem:**
```kotlin
status = if (isExit) "خروج" else "ورود"
val exitedCargos = _cargoInfoList.value.filter { it.status == "خروج" }
```
```php
COALESCE(SUM(CASE WHEN status = 'خروج' THEN netWeight ELSE 0 END), 0) as totalNetWeight,
... WHERE ... AND (status = 'ورود' OR (status = 'خروج' AND ...))
```
همین برای `confirm = "تائید شده"` و `confirmation = "no"/"yes"`.

**Why it matters:**
این رشته‌های فارسی یک قرارداد بین کلاینت و سرور هستند که هیچ‌جا تعریف نشده. یک تفاوت در نویسه (مثلاً «تأیید شده» با همزه در برابر «تائید شده» با یای) یا یک فاصله‌ی اضافی، بی‌صدا باعث می‌شود فیلتر هیچ نتیجه‌ای برنگرداند — بدون هیچ خطایی. در فارسی این ریسک به‌خاطر تنوع نویسه‌ها (ی/ي، ک/ك، همزه) به‌طور ویژه بالاست.

**Recommended Fix:**
```kotlin
enum class CargoStatus(val wireValue: String) {
    ENTERED("ورود"), EXITED("خروج");
    companion object {
        fun fromWire(v: String?) = entries.firstOrNull { it.wireValue == v?.trim() }
    }
}
```
و در PHP یک `enum CargoStatus: string` (PHP 8.1+). این تغییر را می‌توان به‌صورت تدریجی و بدون تغییر مقادیر دیتابیس انجام داد.

**Priority:** MEDIUM · **Effort:** Medium

---

### [MEDIUM] Magic Numbers بدون نام

**File:** `app/src/main/java/com/atk/atk_cargo/ui/viewmodel/CargoViewModel.kt:923-928`

**Problem:**
```kotlin
private fun updateLoadableTrucksCount(loadableTonnage: Double) {
    val trucks18Wheeler = if (loadableTonnage > 0) (loadableTonnage / 25000.0).toInt() else 0
    val trucks10Wheeler = if (loadableTonnage > 0) (loadableTonnage / 15000.0).toInt() else 0
```
۲۵,۰۰۰ و ۱۵,۰۰۰ ظرفیت کامیون‌های ۱۸ و ۱۰ چرخ به کیلوگرم هستند — یک قاعده‌ی تجاری واقعی که در کد دفن شده. اگر این ظرفیت‌ها تغییر کنند (تغییر مقررات، نوع کامیون)، باید در کد پیدا و تغییر داده شوند.

**Recommended Fix:**
```kotlin
private object TruckCapacity {
    const val EIGHTEEN_WHEELER_KG = 25_000.0
    const val TEN_WHEELER_KG = 15_000.0
}
```
و بهتر: این مقادیر را از سرور (پیکربندی) بگیرید تا بدون انتشار نسخه‌ی جدید قابل تغییر باشند. همین برای `30000` (فاصله‌ی polling) که در ۴ فایل تکرار شده و باید در یک ثابت مشترک باشد.

**Priority:** MEDIUM · **Effort:** Low

---

### [MEDIUM] DTO و مدل دامنه تفکیک نشده‌اند

**File:** `app/src/main/java/com/atk/atk_cargo/data/model/CargoModels.kt`, `AuthModels.kt`, `ReportModels.kt`

**Problem:**
data classهایی که با `@SerializedName` برای Gson حاشیه‌نویسی شده‌اند، **مستقیماً در Composable مصرف می‌شوند**. یعنی شکل پاسخ سرور مستقیماً شکل UI را تعیین می‌کند.

**Why it matters:**
سه پیامد: (۱) هر تغییر در قرارداد API، UI را می‌شکند. (۲) پایداری Compose آسیب می‌بیند — کلاسی با فیلدهای nullable و mutable ممکن است ناپایدار (`unstable`) در نظر گرفته شود و recomposition اضافی ایجاد کند. (۳) کامنت `CargoController.php:534-543` دقیقاً به یک کرش واقعی ناشی از همین اشاره می‌کند: مدل Kotlin فیلدها را non-null تعریف کرده، Gson با null پرشان می‌کند، و کرش در لایه‌ی UI رخ می‌دهد. راه‌حل فعلی (`COALESCE` در SQL) درمان علامت است، نه علت.

**Recommended Fix:**
یک لایه‌ی mapper بین DTO و مدل دامنه اضافه کنید:
```kotlin
// data/model/dto/CargoInfoDto.kt  — دقیقاً مطابق سرور، همه nullable
data class CargoInfoDto(@SerializedName("netWeight") val netWeight: String?, ...)

// domain/model/CargoRecord.kt — تایپ‌شده، Immutable، پایدار برای Compose
@Immutable data class CargoRecord(val netWeight: Kilograms?, ...)

// data/mapper/CargoMapper.kt — تنها جایی که با null سروکار دارد
fun CargoInfoDto.toDomain(): CargoRecord? = ...
```
این هم‌زمان مشکل Primitive Obsession و ناپایداری Compose را حل می‌کند و امکان حذف `COALESCE`های دفاعی سمت سرور را می‌دهد.

**Priority:** MEDIUM · **Effort:** High

---

## Project Structure

### آنچه خوب است ✅

- **تفکیک `feature/`** با ۱۲ feature مستقل — پایه‌ی درستی برای modularization آینده.
- **زیرساخت لایه‌بندی درون featureها** (`presentation/`, `domain/`, `data/`, `navigation/`, `components/`) در جاهایی درست پیاده شده — به‌ویژه `feature/auth/` که کامل‌ترین نمونه است (data → domain → presentation → viewmodel).
- **`ui/theme/` بسیار بالغ** — ۱۸ فایل شامل `Spacing`, `Dimensions`, `Elevation`, `Motion`, `SemanticColors`, `ComponentDefaults`, `AdaptiveLayout`. این یک design system واقعی است، نه فقط چند رنگ.
- **`core/ui/components/`** با ۸ کامپوننت مشترک (`EmptyState`, `ErrorState`, `LoadingOverlay`, `SearchBar`, ...) — reuse درست.
- **PHP: تفکیک Controller / Service / Repository / Validator / Core** — یک ساختار لایه‌ای تمیز و قابل تشخیص.
- **`baselineprofile/` به‌عنوان ماژول جدا** — درست.

### آنچه باید تغییر کند ❌

| مشکل | توضیح |
|---|---|
| `ui/viewmodel/` در برابر `feature/*/viewmodel/` | `CargoViewModel` و `ReportsViewModel` در `ui/viewmodel/` هستند ولی `AuthViewModel` در `feature/auth/viewmodel/` — دو قرارداد متناقض |
| `ui/screens/ManageReportsScreen.kt` (1039 خط) | تنها بازمانده در `ui/screens/`؛ باید به `feature/reports/presentation/` برود |
| `api/` به‌عنوان انبار همه‌چیز | ۲۲ فایل شامل `ChatViewModel`, `ChatMessage`, `DataModel`, `AtkCargoApplication`, `BootReceiver`, `AppNotificationManager` — هیچ‌کدام «api» نیستند |
| `data/ColorPicker.kt` | یک کامپوننت UI در پوشه‌ی `data/` |
| `feature/chat` در برابر `api/ChatViewModel.kt` | ViewModel چت خارج از feature خودش است |
| ماژول تکی `:app` | ۵۱,۶۰۰ خط در یک ماژول — build کامل در هر تغییر |
| PHP: ۳۰ فایل entry در ریشه | باید در `public/` یا کاملاً حذف شوند |
| `PHP/User/` | `SessionManager.php` و `jdf.php` تکراری با ریشه — کد مرده‌ی احتمالی |
| `PHP/jdf.php` (647 خط) | کتابخانه‌ی شخص ثالث در ریشه، خارج از `vendor/` و بدون namespace |
| `migrations/` خالی | — |

### ساختار پیشنهادی

```
ATK-Cargo/
├── app/                        # فقط Application، MainActivity، Navigation، DI
├── core/
│   ├── designsystem/           # ← ui/theme/ فعلی (۱۸ فایل، آماده‌ی استخراج)
│   ├── ui/                     # ← core/ui/components/ فعلی
│   ├── common/                 # JalaliDateUtils، extensions، Result/DataError
│   ├── network/                # Retrofit، OkHttp، AuthSession، TokenAuthenticator، Interceptors
│   ├── database/               # Room (AppDatabase، DAO، Entity)
│   ├── datastore/              # UserPreferencesManager، CryptoManager
│   └── security/               # SecurityVerifier
├── feature/
│   ├── auth/          {data, domain, presentation}   ← الگوی مرجع (از قبل درست است)
│   ├── cargo-entry/   {data, domain, presentation}
│   ├── cargo-registration/
│   ├── cargo-counter/
│   ├── cargo-details/
│   ├── reports/
│   ├── chat/
│   ├── admin/
│   ├── home/
│   └── update/
└── baselineprofile/

PHP/
├── public/                     # ← تنها web root
│   ├── index.php               # نقطه‌ی ورود واحد (Router v2)
│   └── .htaccess
├── src/                        # خارج از web root
│   ├── Core/  Controllers/  Services/  Repositories/  Validators/  Routes/
├── config/                     # خارج از web root
├── storage/                    # ← logs/، state فایلی — خارج از web root
├── migrations/                 # migrationهای واقعی
├── tests/
└── vendor/                     # خارج از web root، gitignored
```

**نکته‌ی مهم:** modularization کامل یک پروژه‌ی بزرگ است. توصیه‌ی عملی: **از `core:designsystem` شروع کنید** — چون `ui/theme/` از قبل کاملاً مستقل و بدون وابستگی به feature است، استخراج آن کم‌ریسک‌ترین قدم اول است و بلافاصله زمان build را بهبود می‌دهد.

---
## Technical Debt

بدهی فنی این پروژه غیرمعمول است: بیشتر آن **بدهی مهاجرت نیمه‌تمام** است، نه بدهی بی‌دقتی. تقریباً هر مورد، نتیجه‌ی یک تصمیم درست است که اجرای آن تمام نشده.

| # | بدهی | وضعیت | هزینه‌ی روزانه | هزینه‌ی پرداخت |
|---|---|---|---|---|
| D1 | Router v2 نوشته شده، مصرف نمی‌شود | ~۵۸۰ خط کد مرده | نگهداری دوگانه‌ی هر تغییر منطق | High |
| D2 | مدل رمز عبور (SHA-256 کلاینت + PIN عددی) | فعال در تولید | ریسک امنیتی روزانه | Medium |
| D3 | `migrations/` خالی، `schema.sql` ناقص | ۳ جدول مفقود | عدم امکان بازسازی محیط | Medium |
| D4 | audit trail بدون جدول پشتیبان | no-op بی‌صدا | نبود ردیابی عملیات حساس | Medium |
| D5 | `collectAsState` بدون lifecycle (۸۵ مورد) | فعال | باتری و CPU | Low |
| D6 | God Composableها (۱۰ فایل >۱۰۰۰ خط) | فعال | کندی توسعه، عدم امکان تست | High |
| D7 | نبود تست (۳ فایل معنادار) | — | هر تغییر یک قمار است | High |
| D8 | DTO = مدل دامنه | فعال | شکنندگی در برابر تغییر API | High |
| D9 | Koin وجود دارد ولی دور زده می‌شود | جزئی | عدم امکان تست ViewModel | Low |
| D10 | ۶ وابستگی بلااستفاده + ۹ entry مرده در catalog | — | زمان build | Low |
| D11 | `viewBinding` فعال در اپ Compose | — | زمان build | Low |
| D12 | `itextpdf 5` با مجوز AGPL | فعال | ریسک حقوقی | Medium |
| D13 | ماژول تکی `:app` (۵۱,۶۰۰ خط) | — | زمان build کامل | High |
| D14 | `PHP/User/` و `PHP/jdf.php` — کد تکراری/بی‌مالک | احتمالاً مرده | سردرگمی | Low |
| D15 | رشته‌های UI به‌صورت literal (strings.xml با ۱۹ خط) | — | غیرقابل ترجمه | High |

### شاخص سلامت بدهی

نکته‌ی مثبت و قابل توجه: **کیفیت کامنت‌های این کدبیس بسیار بالاست.** کامنت‌ها «چه کاری می‌کند» را توضیح نمی‌دهند، بلکه **«چرا این تصمیم گرفته شد و چه چیزی قبلاً خراب بود»** را ثبت می‌کنند. نمونه‌ها:

- `AppApiController.php:55-58` — چرا write actions باید POST باشند
- `network_security_config.xml:52-70` — چرا پین روی CA و نه leaf
- `CargoController.php:682-689` — چرا `htmlspecialchars` اینجا اشتباه بود
- `check_session.php:16-19` — چرا ۵۰۳ و نه ۲۰۰
- `Response.php:23-29` — چرا `no-store` نباید همیشه اعمال شود
- `api_v2.php:17-31` — دو الگوی مهاجرت و دلیل هرکدام

این یعنی **دانش سازمانی در کد ثبت شده است**، نه فقط در ذهن افراد. این یک دارایی واقعی است که بازپرداخت بدهی را به‌مراتب کم‌ریسک‌تر می‌کند و باید به‌عنوان یک نقطه‌ی قوت جدی حفظ شود.

---

## Production Readiness

| مورد | وضعیت | توضیح |
|---|---|---|
| **Security** | ⚠️ مشروط | یک یافته‌ی CRITICAL و ۷ HIGH |
| **Stability** | ✅ خوب | نشتی حافظه‌ی واضحی یافت نشد؛ coroutineها درست scope شده‌اند |
| **Crash handling** | ❌ ناکافی | هیچ گزارش کرشی جمع نمی‌شود؛ `mapping.txt` آرشیو می‌شود ولی کرشی نمی‌رسد |
| **Logging** | ⚠️ جزئی | لاگ خوب سرور، لاگ دسترسی ناقص، پوشه‌ی لاگ احتمالاً قابل دسترس |
| **Monitoring** | ❌ وجود ندارد | نه health-check، نه متریک، نه هشدار |
| **Performance** | ✅ خوب | Baseline Profile، ETag، MicroCache، ایندکس‌های هدفمند |
| **Configuration** | ⚠️ جزئی | `.env` وجود دارد؛ ولی `update_config.php` یک فایل PHP است نه پیکربندی |
| **Environment separation** | ❌ ضعیف | فقط یک محیط؛ `.env.example` با `APP_DEBUG=true` |
| **Debug/Release** | ✅ خوب | `applicationIdSuffix=".debug"`، logging فقط در debug، minify در release |
| **ProGuard/R8** | ✅ خوب | `isMinifyEnabled`، `isShrinkResources`، `enableR8.fullMode`، آرشیو mapping |
| **Obfuscation** | ✅ فعال | R8 full mode |
| **API security** | ⚠️ مشروط | prepared statement ✅، ولی مجوز خواندن ناقص و proxy قابل دور زدن |
| **Database security** | ⚠️ جزئی | prepared statement ✅، ولی توکن plaintext و بدون FK |
| **Backup** | ❌ نامشخص | هیچ استراتژی بکاپ در repo مستند نشده |
| **Testing** | ❌ بحرانی | ~۰٪ پوشش مؤثر |
| **CI/CD** | ⚠️ جزئی | `.github/workflows/` وجود دارد — محتوای آن باید بررسی شود |
| **Documentation** | ⚠️ جزئی | `PHP/docs/` دو سند طراحی خوب دارد؛ ولی هیچ README یا مستند API نیست |

### حکم

**آماده‌ی تولید: خیر — تا اتمام Phase 1.**

سیستم امروز کار می‌کند و پایدار است. اما با ترکیب «رمز ۴ رقمی + قفل قابل دور زدن + شمارش کاربران»، هزینه‌ی حمله برای رسیدن به یک حساب admin بسیار پایین است. این تنها مسئله‌ای است که مانع حکم «آماده» می‌شود؛ بقیه‌ی یافته‌ها مهم‌اند ولی بازدارنده نیستند.

پس از Phase 1 (تخمین: ۳–۵ روز کاری)، وضعیت به «آماده با پایش» تغییر می‌کند.

---

## Prioritized Action Plan

### Phase 1 — Immediate (این هفته — بازدارنده‌های تولید)

| # | اقدام | فایل | Effort |
|---|---|---|---|
| 1.1 | جدا کردن کلید قفل ورود از IP (شمارنده‌ی مستقل username + شمارنده‌ی IP) + تأخیر تصاعدی | `LoginAttemptLimiter.php` | Low |
| 1.2 | افزایش حداقل طول رمز به ۸ و حذف محدودیت «فقط رقم» در UI **و** اعتبارسنجی سمت سرور | `UserManagementDialogsSection.kt:200,270,447`، `InputValidator.php` | Low |
| 1.3 | `getAllUsers` → `ADMIN_ONLY_ACTIONS` + افزودن `getSelfProfile` | `UserController.php` | Low |
| 1.4 | گیت `view_reports` روی actionهای خواندنی `app_api.php` (پس از بازبینی نقش‌ها) | `AppApiController.php` | Low |
| 1.5 | fail-closed کردن تأیید SHA-256 آپدیت | `UpdateManager.kt:334-340` | Low |
| 1.6 | `session_regenerate_id(true)` + قفل brute-force + کوکی امن در پنل مدیریت | `PermissionManager.php` | Low |
| 1.7 | `.htaccess` با `Require all denied` در `PHP/logs/`، `PHP/log/` و `PHP/vendor/` | — | Low |
| 1.8 | محدود کردن `check_logout.php` به نشستی که توکن معتبرش ارائه شده | `AuthController.php:258` | Low |
| 1.9 | رفع شاخه‌ی ۴۰۱ غیرقابل‌دسترس | `PermissionPoller.kt:127` | Low |
| 1.10 | سقف روی `limit` چت | `ChatController.php:53` | Low |

**نتیجه‌ی مورد انتظار:** حذف یافته‌ی CRITICAL و ۵ مورد از HIGHها. Security از ۵ به ~۷.

---

### Phase 2 — High Priority (۲ تا ۴ هفته)

| # | اقدام | Effort |
|---|---|---|
| 2.1 | هش کردن توکن‌های نشست در دیتابیس (`hash('sha256', $token)`) | Low |
| 2.2 | حذف `vendor/` از گیت + `composer install --no-dev` در deploy | Low |
| 2.3 | بازتولید `schema.sql` + ساخت جدول `audit_log` + مکانیزم migration | Medium |
| 2.4 | یکنواخت کردن مدیریت خطا در سرور (فقط `ApiException` به کلاینت می‌رود) | Low |
| 2.5 | `collectAsStateWithLifecycle` در همه‌جا (+ افزودن `lifecycle-runtime-compose`) | Low |
| 2.6 | افزودن `key` به ۲۲ لیست Lazy | Low |
| 2.7 | کش کردن `permissions.json` با `MicroCache` + نوشتن اتمیک | Low |
| 2.8 | تأیید رمز فعلی هنگام تغییر رمز توسط خود کاربر | Low |
| 2.9 | حذف ۶ وابستگی بلااستفاده + `viewBinding = false` + پاک‌سازی catalog | Low |
| 2.10 | `composer.json` → `php: >=8.1` | Low |
| 2.11 | حذف ایندکس‌های زائد `user_sessions` (پس از `EXPLAIN`) | Low |
| 2.12 | حذف `gregorianToJalali` تکراری از `CargoViewModel` | Low |
| 2.13 | افزودن گزارش کرش خودمیزبان + health-check endpoint | Medium |
| 2.14 | تصمیم درباره‌ی مجوز `itextpdf` (خرید یا مهاجرت) | Medium |

**نتیجه:** Security ~۸، Performance ~۷.۵، Production Readiness ~۷.

---

### Phase 3 — Medium Priority (۱ تا ۳ ماه)

| # | اقدام | Effort |
|---|---|---|
| 3.1 | **مهاجرت مرحله‌ای کلاینت به Router v2** (گروه به گروه، با flag) | High |
| 3.2 | حذف `protected_proxy.php` و فایل‌های entry نسخه ۱ پس از تکمیل ۳.۱ | Medium |
| 3.3 | نوشتن تست‌های Phase 1 و 2 (منطق تجاری + احراز هویت) | High |
| 3.4 | افزودن وابستگی‌های تست (`coroutines-test`, `turbine`, `mockk`) | Low |
| 3.5 | معرفی `UiState` واحد در `CargoViewModel` و `ReportsViewModel` | High |
| 3.6 | تفکیک DTO از مدل دامنه + mapper + value classes برای وزن/تناژ | High |
| 3.7 | شکستن ۵ God Composable بزرگ‌تر به Screen/Content/Components | High |
| 3.8 | `enum` برای وضعیت حواله در هر دو سمت | Medium |
| 3.9 | افزودن FKهای گزینشی (چت، نشست) پس از پاک‌سازی رکوردهای یتیم | Medium |
| 3.10 | صفحه‌بندی cursor-based روی فهرست‌های رو به رشد | High |
| 3.11 | حذف `hashPassword` سمت کلاینت + مهاجرت به bcrypt روی رمز خام | Medium |
| 3.12 | پشتیبانی از Reduce Motion | Low |
| 3.13 | بازساختاردهی PHP به `public/` + انتقال `logs`/`vendor`/`config` خارج از web root | Medium |

---

### Phase 4 — Optimization (۳ تا ۶ ماه)

| # | اقدام | Effort |
|---|---|---|
| 4.1 | استخراج `core:designsystem` به‌عنوان اولین ماژول (کم‌ریسک‌ترین) | Medium |
| 4.2 | ادامه‌ی modularization: `core:network`, `core:database`, `core:common` | High |
| 4.3 | تبدیل هر feature به یک ماژول Gradle | High |
| 4.4 | `targetSdk` → ۳۵ سپس ۳۶ | Medium |
| 4.5 | مهاجرت کامل Material 2 → Material 3 | Medium |
| 4.6 | انتقال رشته‌های UI به `strings.xml` + نوع `UiText` | High |
| 4.7 | انتقال مجوزها از `permissions.json` به دیتابیس | Medium |
| 4.8 | مستندسازی OpenAPI برای API v2 | Medium |
| 4.9 | تست‌های Compose روی composableهای بی‌حالت | Medium |
| 4.10 | فعال کردن `STRICT_TRANS_TABLES` پس از پاک‌سازی داده | Medium |
| 4.11 | logrotate + آرشیو لاگ | Low |

---

## Recommended Architecture

```
┌──────────────────────────────────────────────────────────────┐
│  Presentation (Compose)                                       │
│  ├── XScreen(viewModel)         ← stateful، فقط اتصال         │
│  ├── XContent(uiState, onEvent) ← stateless، قابل preview/تست │
│  └── components/                ← اجزای برگ                   │
│      collectAsStateWithLifecycle • items(key=) • @Immutable   │
└───────────────────────────┬──────────────────────────────────┘
                            │ UiState (یک data class)  /  Event (sealed)
┌───────────────────────────▼──────────────────────────────────┐
│  ViewModel                                                    │
│  • یک StateFlow<UiState>                                      │
│  • activeDialog: sealed  ← حالت‌های ناسازگار غیرممکن می‌شوند   │
│  • فقط UseCase صدا می‌زند (هرگز ApiService مستقیم)             │
└───────────────────────────┬──────────────────────────────────┘
                            │ مدل دامنه (تایپ‌شده، Kilograms، enum)
┌───────────────────────────▼──────────────────────────────────┐
│  Domain (UseCase + Model + قواعد تجاری خالص)                  │
│  • بدون وابستگی به Android/Retrofit → کاملاً قابل unit test    │
└───────────────────────────┬──────────────────────────────────┘
                            │ interface Repository
┌───────────────────────────▼──────────────────────────────────┐
│  Data                                                         │
│  ├── RepositoryImpl → Result<T> / DataError (نوع‌دار)          │
│  ├── remote/ ApiService + DTO + Mapper                        │
│  └── local/  Room + DataStore + CryptoManager                 │
└───────────────────────────┬──────────────────────────────────┘
                            │ HTTPS + Cert Pinning + X-Session-Token
┌───────────────────────────▼──────────────────────────────────┐
│  PHP — نقطه‌ی ورود واحد: public/index.php                      │
│  Router (opt-in) → ApiAuthGate (auth + permission)             │
│      → Controller (فقط parse/response)                        │
│          → Service (منطق تجاری، قابل تست)                     │
│              → Repository (فقط prepared statement)            │
│                  → MySQL (FK، ایندکس هدفمند، strict mode)     │
└──────────────────────────────────────────────────────────────┘
```

**اصول کلیدی که این معماری اعمال می‌کند:**

1. **گیت مجوز در یک نقطه** — `ApiAuthGate` در سطح Router، نه پراکنده در کنترلرها. (Router v2 از قبل این را دارد.)
2. **هویت همیشه از نشست، هرگز از ورودی** — این از قبل در بیشتر کنترلرها اعمال شده.
3. **مدل دامنه ≠ DTO** — تغییر قرارداد API نباید UI را بشکند.
4. **خطای نوع‌دار** — لایه‌ی بالا باید بتواند «شبکه قطع» را از «یافت نشد» تشخیص دهد.
5. **Domain بدون وابستگی به framework** — تنها راه رسیدن به تست معنادار.
6. **یک منبع حقیقت برای هر داده** — نه دو الگوریتم تاریخ، نه دو کپی از فهرست.

---

## Recommended Project Structure

(ساختار کامل در بخش «Project Structure» بالا آمده است.)

**ترتیب پیشنهادی اجرا — از کم‌ریسک به پرریسک:**

1. `core:designsystem` (از `ui/theme/` — از قبل مستقل است) ← **شروع از اینجا**
2. `core:common` (`JalaliDateUtils`, extensions, `DataError`)
3. `core:network` + `core:datastore` + `core:database`
4. جابه‌جایی فایل‌های بدجا: `ui/screens/ManageReportsScreen.kt` → `feature/reports/`، `api/ChatViewModel.kt` → `feature/chat/`، `data/ColorPicker.kt` → `core/ui/`
5. یکنواخت‌سازی `ui/viewmodel/` → `feature/*/viewmodel/`
6. تبدیل featureها به ماژول (یکی‌یکی، با `feature/auth` که ساختارش از قبل درست است به‌عنوان الگو)

---

## Final Score

| Category | Score | مبنای امتیاز |
| -------------------- | ----: | --- |
| Security             | 5/10 | prepared statement/Keystore/pinning عالی؛ ولی ۱ CRITICAL و ۷ HIGH |
| Architecture         | 5/10 | لایه‌بندی درست موجود ولی ناقص؛ دو stack موازی |
| Kotlin               | 6.5/10 | استفاده‌ی تمیز از زبان؛ ولی مدیریت خطا و تکرار |
| Jetpack Compose      | 5/10 | design system عالی؛ ولی صفر lifecycle-awareness و God Composable |
| Android              | 6/10 | پیکربندی امنیتی و build خوب؛ targetSdk عقب، وابستگی مرده |
| PHP Backend          | 6/10 | لایه‌بندی و prepared statement خوب؛ ولی migration/schema ناقص |
| API Design           | 4/10 | غیر-RESTful، سه شکل خطا، پارامتر در query، دو stack |
| Database             | 6/10 | ایندکس‌های هدفمند عالی؛ بدون FK، schema ناقص، over-index |
| Performance          | 6/10 | MicroCache/ETag/BaselineProfile عالی؛ ولی چند نقطه‌ی داغ |
| Memory Management    | 7/10 | بهترین حوزه — بدون نشتی واضح |
| Error Handling       | 5/10 | تصمیمات fail-safe هوشمند؛ ولی بسته‌بندی و بلعیدن استثنا |
| Testing              | 1.5/10 | ۳ فایل معنادار در ۶۲,۰۰۰ خط |
| Code Quality         | 5/10 | کامنت‌های استثنایی؛ ولی God classها و تکرار |
| Maintainability      | 5/10 | دانش در کد ثبت شده ✅؛ ولی فایل‌های غول‌پیکر |
| Scalability          | 5/10 | بدون صفحه‌بندی، ماژول تکی، مجوز در فایل |
| Production Readiness | 5/10 | پایدار ولی با بازدارنده‌ی امنیتی و بدون مانیتورینگ |

# **Overall: 5.2 / 10**

**تفسیر:** این یک پروژه‌ی «نیمه‌راه» است — نه یک کدبیس بی‌کیفیت. تصمیمات مهندسی درست زیادی در آن گرفته شده (بعضی‌شان از پروژه‌های با امتیاز بالاتر هم بهترند: certificate pinning با توجیه مکتوب، refresh token reuse detection، ETag محاسبه‌شده از داده‌ی پایدار، ارزیابی سخت‌افزار برای انیمیشن). امتیاز پایین عمدتاً از **اجرای ناتمام** می‌آید، نه از ندانستن.

---

## Final Recommendations

### سه کاری که اگر فقط سه کار بتوانید بکنید

1. **قفل ورود را از IP جدا کنید و رمزها را قوی‌تر کنید** (Phase 1.1 + 1.2). این تنها یافته‌ی CRITICAL است و اصلاح آن کمتر از یک روز کار دارد. هیچ کار دیگری در این گزارش به این نسبت بازدهی ندارد.

2. **مهاجرت به Router v2 را تمام کنید** (Phase 3.1). شما از قبل هزینه‌ی نوشتنش را پرداخته‌اید ولی هیچ بهره‌ای نمی‌برید. این کار هم‌زمان چهار یافته را حل می‌کند: دور زدن پروکسی، شکاف مجوز `app_api.php`، شکاف `syncPermissions`، و بدهی نگهداری دوگانه.

3. **تست‌های منطق تجاری خالص را بنویسید** (Phase 3.3، شروع با `QuotaService` و `JalaliDateUtils`). این‌ها به mock نیاز ندارند، سریع اجرا می‌شوند، و دقیقاً همان کدی را پوشش می‌دهند که اشتباهش پول واقعی هزینه دارد. `QuotaCalculatorTest` نشان می‌دهد که این کار در این پروژه شدنی است.

### آنچه باید حفظ شود

- **فرهنگ کامنت‌نویسی «چرا».** این نادرترین و ارزشمندترین ویژگی این کدبیس است. آن را در هر refactor حفظ کنید — کامنت‌ها را با کد منتقل کنید، حذفشان نکنید.
- **`ui/theme/` به‌عنوان design system.** ۱۸ فایل با `Motion`, `Elevation`, `SemanticColors`, `AdaptiveLayout` — این سطحی از بلوغ است که اغلب پروژه‌ها به آن نمی‌رسند.
- **`HardwarePerformanceEvaluator`.** تطبیق انیمیشن با توان دستگاه، یک تصمیم کاربر-محور واقعی است.
- **الگوی `deleteCargoInfo`** — بررسی رمز و عملیات در یک درخواست واحد، با شمارنده‌ی تلاش روی هویت نشست. این را به‌عنوان الگوی مرجع برای هر عملیات حساس آینده استفاده کنید.

### هشدار درباره‌ی ترتیب

Phase 1.4 (گیت `view_reports`) و Phase 4.10 (`STRICT_TRANS_TABLES`) هر دو **قابلیت شکستن رفتار موجود** دارند. قبل از هرکدام:
- برای ۱.۴: فهرست نقش‌های `config/permissions.json` را بررسی کنید و مطمئن شوید نقش‌هایی که امروز از صفحات گزارش استفاده می‌کنند این مجوز را دارند.
- برای ۴.۱۰: ابتدا در staging اجرا کنید و خطاهای تولیدشده را بررسی کنید — احتمالاً داده‌ی نامعتبری را آشکار می‌کند که تا امروز پنهان بوده.

---

## Top 20 Priority List

| # | Issue | Category | Severity | File | Effort |
|--:|-------|----------|----------|------|--------|
| 1 | رمز ۴ رقمی + SHA-256 بدون salt + قفل قابل دور زدن با چرخش IP | Security | **CRITICAL** | `LoginAttemptLimiter.php:50`, `UserManagementDialogsSection.kt:200,270`, `SecurityUtils.kt:10` | Low |
| 2 | `protected_proxy.php` قابل دور زدن — rate-limit و IP-block بی‌اثر | Security | HIGH | `protected_proxy.php`, `.htaccess` | Medium |
| 3 | توکن‌های نشست و refresh به‌صورت plaintext در دیتابیس | Security | HIGH | `SessionRepository.php:230,258` | Low |
| 4 | `app_api.php` بدون گیت `view_reports` روی actionهای خواندنی | AuthZ | HIGH | `AppApiController.php:91-198` | Low |
| 5 | تأیید SHA-256 آپدیت APK به‌صورت fail-open | Security | HIGH | `UpdateManager.kt:334-340` | Low |
| 6 | `PermissionManager.php` بدون `session_regenerate_id` و بدون قفل brute-force | Security | HIGH | `PermissionManager.php:43-47` | Low |
| 7 | `vendor/` (شامل dev deps) در web root و در گیت | Security | HIGH | `PHP/vendor/`, `.gitignore` | Low |
| 8 | پوشه‌های `logs/`/`log/` احتمالاً از وب قابل دسترس (`.json`/`.txt` بدون محافظت) | Security | HIGH | `.htaccess`, `LoginAttemptLimiter.php:78` | Low |
| 9 | `migrations/` خالی + `schema.sql` ناقص → `audit_log` هرگز ساخته نمی‌شود | Data/Ops | HIGH | `PHP/migrations/`, `AuditLogger.php` | Medium |
| 10 | Router v2 کامل ولی بلااستفاده — ۵۸۰ خط کد مرده + نگهداری دوگانه | Architecture | HIGH | `api_v2.php`, `ApiService.kt` | High |
| 11 | صفر مورد `collectAsStateWithLifecycle` در برابر ۸۵ `collectAsState` | Performance | HIGH | سراسر `feature/**` | Low |
| 12 | باگ: شاخه‌ی ۴۰۱ در `PermissionPoller` غیرقابل‌دسترس — نشست منقضی پاک نمی‌شود | Bug | HIGH | `PermissionPoller.kt:127` | Low |
| 13 | God Composable — ۱۰ فایل بالای ۱۰۰۰ خط (بیشینه ۲۲۲۳) | Code Quality | HIGH | `SelectInfoScreen.kt` و ۹ فایل دیگر | High |
| 14 | نبود پوشش تست روی منطق مالی/عملیاتی و احراز هویت | Testing | HIGH | کل پروژه | High |
| 15 | `permissions.json` در هر بررسی مجوز از دیسک خوانده و parse می‌شود | Performance | HIGH | `PermissionService.php:20-35` | Low |
| 16 | Primitive Obsession روی وزن/تناژ (`String` + `?: 0f`) | Correctness | HIGH | `CargoViewModel.kt:801-817`, `CargoModels.kt` | High |
| 17 | `check_logout.php` بدون auth → DoS هدفمند علیه همه‌ی کاربران | Security | MEDIUM | `AuthController.php:258-279` | Low |
| 18 | `getAllUsers` فهرست کامل کاربران و نقش‌ها را به هر کاربر می‌دهد | Security | MEDIUM | `UserController.php:82-85` | Low |
| 19 | ۲۲ لیست Lazy بدون `key` در ترکیب با polling ۳۰ ثانیه‌ای | Performance | MEDIUM | ۱۳ فایل | Low |
| 20 | `itextpdf 5` با مجوز AGPL در یک محصول proprietary | Legal/Deps | MEDIUM | `build.gradle.kts:297` | Medium |

---

*این گزارش صرفاً حاصل تحلیل ایستای کد است. هیچ فایلی از source تغییر داده نشده، هیچ dependency به‌روزرسانی نشده، و هیچ migration اجرا نشده است. یافته‌هایی که با «Potential Issue» علامت خورده‌اند نیازمند تأیید در محیط اجرا هستند. پیشنهادهای مربوط به ایندکس دیتابیس باید قبل از اعمال با `EXPLAIN` روی داده‌ی واقعی تأیید شوند.*





