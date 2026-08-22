# Deep Code Audit Report

**پروژه:** ATK-Cargo (اپلیکیشن اندروید + بک‌اند PHP)
**تاریخ ممیزی:** ۲۰۲۶-۰۸-۲۱
**کامیت مبنا:** `ec07029`
**دامنه‌ی بررسی:** کل ریپازیتوری — ۱۵ ماژول Gradle (۵۱٬۷۳۹ خط Kotlin)، بک‌اند PHP (۱۱٬۹۳۳ خط)، schema دیتابیس، CI، پیکربندی build و امنیت.

---

## Executive Summary

### وضعیت کلی

این پروژه **به‌طور محسوسی از یک ممیزی قبلی عبور کرده است** و آثار آن در همه‌جا دیده می‌شود: ارجاع‌های `DEEP_CODE_REVIEW.md #PhaseX` در کامنت‌ها، هش‌شدن توکن‌های نشست، rotation توکن، certificate pinning، CSRF، bcrypt، prepared statement در ۱۰۰٪ کوئری‌ها، و یک CI با PHPStan. **این پایه‌ی امنیتی خوبی است و نباید دست‌کم گرفته شود.**

اما پروژه در سه محور همچنان از استاندارد production فاصله دارد:

1. **معماری اندروید نیمه‌تمام است** — Repository Pattern فقط روی ۵ فیچر از ۱۴ فیچر اعمال شده؛ بقیه‌ی ViewModelها مستقیم `ApiServiceV2` را صدا می‌زنند. ۷ پکیج بین ماژول‌های مختلف split شده‌اند. یک صفحه‌ی کامل فیچر (۱۰۳۴ خط) داخل ماژول `:app` مانده است.
2. **معماری polling-محور** — سه حلقه‌ی ۳۰ ثانیه‌ای در Composableها + یک poller مجوز ۳ دقیقه‌ای + یک Foreground Service با poll پنج‌دقیقه‌ای دائمی. این آخری روی Android 14+ (که targetSdk پروژه است) با سقف زمانی `dataSync` تداخل دارد.
3. **تست تقریباً وجود ندارد** — ۸۵۲ خط تست در برابر ۵۱٬۷۳۹ خط کد production (نسبت ۱.۶٪)، و CI هرگز `assembleRelease` یا `lint` اجرا نمی‌کند؛ یعنی مسیر R8/ProGuard اصلاً گیت نشده است.

### مهم‌ترین ریسک‌های امنیتی

| # | مسئله | شدت |
|---|-------|------|
| ۱ | `sleep()` مسدودکننده در مسیر لاگین + فراخوانی HTTP همگام تلگرام → تخلیه‌ی worker pool سرور | CRITICAL |
| ۲ | «رازها» در `secrets.cpp` با XOR تک‌بایتی (کلید `0x5A`) — API key و License key در چند ثانیه قابل استخراج | HIGH |
| ۳ | گیت `min_allowed_version` با حذف هدر `X-App-Version` کاملاً دور زده می‌شود | HIGH |
| ۴ | `diagnostics/crash` بدون auth با rate-limiter‌ای که `sleep(8)` می‌کند | HIGH |
| ۵ | شمارش‌گر rate-limit فایل‌محور دچار race است (read-modify-write غیراتمیک) | MEDIUM |
| ۶ | `checkSession` امکان شمارش نام‌های کاربری و افشای `userType` بدون احراز هویت را می‌دهد | MEDIUM |

### مهم‌ترین مشکلات Performance

| # | مسئله | اثر |
|---|-------|------|
| ۱ | اسپلش اجباری ۳۸۰۰ میلی‌ثانیه‌ای در هر cold start، حتی وقتی کار شبکه تمام شده | HIGH |
| ۲ | Foreground Service با poll ۵ دقیقه‌ای دائمی (نقض سقف `dataSync` در Android 14+) | HIGH |
| ۳ | محاسبات `filter/groupBy/sortedWith` بدون `remember` داخل composition (با `sumOf` داخل comparator) | HIGH |
| ۴ | هیچ pagination‌ای در هیچ endpoint گزارش‌گیری وجود ندارد | HIGH |
| ۵ | سه استک HTTP موازی (Retrofit، OkHttp خام، HttpURLConnection) با سه connection pool جدا | MEDIUM |

### مهم‌ترین مشکلات Architecture

- نشت لایه: ViewModel → ApiService بدون Repository (۶ ViewModel)
- ۷ پکیج split‌شده بین ماژول‌ها (`com.atk.atk_cargo.api` در سه ماژول)
- God ViewModel: `CargoViewModel` با ۹۹۸ خط و ۱۰ StateFlow موازی در کنار یک `UiState`
- `AppApiController` عمدتاً یک Middle Man خالص (۲۲ متد pass-through)
- کلیدهای JOIN ناسازگار بین کوئری‌های تحلیلی و سرویس‌ها → ریسک over-counting

### Technical Debt

- کد مرده‌ی قابل اثبات: `AnimationManager.setPerformanceScore()` هرگز صدا زده نمی‌شود، `saveHardwareScore()` هرگز صدا زده نمی‌شود، `ChatDao.markAsRead()` مرده، endpoint `chat/messages/{id}/read` مرده، کل زنجیره‌ی FCM (ستون DB + endpoint + route) بدون هیچ SDK فایربیسی، قوانین ProGuard برای `itextpdf`/`coil`/`vico`/`ThirdPartyApiService` که هیچ‌کدام در پروژه نیستند.
- ۹۱ مگابایت خروجی `graphify-out/` (۴۷ فایل) کامیت‌شده در گیت.
- یک heap dump ۱.۶۹ گیگابایتی (`java_pid23540.hprof`) در ریشه‌ی working tree.

### Production Readiness

**آماده نیست — اما فاصله‌اش زیاد نیست.** با اصلاح ۵ مورد Phase 1 (تخمین ۲–۳ روز کاری) بزرگ‌ترین ریسک‌های در دسترس بسته می‌شوند. مسائل معماری و تست یک کار میان‌مدت‌اند و نباید انتشار را بلاک کنند.

### Overall Score: **5.8 / 10**

---

## Project Overview

### ساختار ماژول‌ها

| ماژول | فایل | خط | نقش |
|-------|------|-----|------|
| `:app` | ۳۱ | ۶٬۲۷۲ | Application، MainActivity، DI، امنیت، ناوبری، **+یک صفحه‌ی فیچر** |
| `:core:common` | ۱ | ۱۳۳ | `JalaliDateUtils` |
| `:core:database` | ۳ | ۹۸ | Room (فقط چت) |
| `:core:designsystem` | ۱۸ | ۱٬۲۷۸ | تم و کامپوننت‌های مشترک |
| `:core:domain` | ۱۱ | ۳۲۹ | اینترفیس‌های مرزی، `AnimationManager` |
| `:core:network` | ۱۶ | ۱٬۸۶۷ | Retrofit، توکن، `Secrets` |
| `:feature:cargo-workflow` | ۴۰ | ۱۵٬۱۲۶ | ثبت/شمارش/جزئیات بار |
| `:feature:reports` | ۳۱ | ۱۶٬۰۳۰ | گزارش‌ها و کوتاژ |
| `:feature:chat` | ۱۰ | ۲٬۵۷۳ | چت ادمین |
| `:feature:home` | ۸ | ۲٬۲۲۴ | خانه و پروفایل |
| `:feature:admin` | ۴ | ۱٬۹۸۶ | مدیریت کاربران |
| `:feature:cargo` | ۴ | ۱٬۴۴۸ | `CargoViewModel` |
| `:feature:update` | ۲ | ۱٬۲۴۷ | به‌روزرسانی درون‌برنامه‌ای |
| `:feature:auth` | ۶ | ۱٬۱۵۵ | ورود/خروج |
| `:feature:startup` | ۴ | ۸۲۵ | اسپلش و همگام‌سازی |

### استک

**اندروید:** Kotlin 2.2.20 · AGP 8.13.0 · Compose BOM 2025.09.00 · Koin 4.1.1 · Retrofit 3.0.0 · OkHttp 5.1.0 · Room 2.7.0 · WorkManager 2.10.4 · CameraX 1.5.0 · ML Kit · Lottie · minSdk 28 / targetSdk 34 / compileSdk 36

**بک‌اند:** PHP ≥8.1 (`declare(strict_types=1)`) · MySQL/InnoDB · معماری Controller→Service→Repository · Router صریح v2 · PHPStan level 5 · PHPUnit 9.6

> نسخه‌ی وابستگی‌ها به‌روز و قابل قبول است — این یکی از نقاط قوت پروژه است.

---

## Architecture Overview

### جریان واقعی داده (استخراج‌شده از کد)

```text
مسیر «درست» (فقط ۵ فیچر):
Compose UI → ViewModel → Repository → ApiServiceV2 → api/v2/index.php
    → Router → ApiAuthGate → Controller → Service → Repository → PDO/mysqli → MySQL

مسیر «میان‌بر» (۶ ViewModel):
Compose UI → ViewModel ──────────────→ ApiServiceV2 → ...   ← لایه‌ی Repository حذف شده

مسیر امنیتی (استک HTTP سوم):
SecurityVerifier → HttpURLConnection → check_signature / license  ← بدون OkHttp، بدون interceptor
```

### مشاهدات کلیدی

- **`api/v2/index.php?route=...`** تنها نقطه‌ی ورود است. چون `mod_rewrite` روی هاست production فعال نیست، مسیر از query string خوانده می‌شود. این تصمیم مستند شده و منطقی است، اما یعنی API عملاً RESTful نیست.
- **دو الگوی کنترلری هم‌زمان:** «Direct passthrough» (متد مستقل با auth/permission در route table) و «Shim» (کنترلر چندعملیاتی با `action=` که مجوز را داخلی چک می‌کند). این دوگانگی در `src/routes/api_v2.php` صریحاً مستند شده اما سطح حمله را دوبرابر می‌کند: هر بار که یک `action` جدید به `UserController` اضافه شود، باید یادت باشد آن را به `ADMIN_ONLY_ACTIONS` هم اضافه کنی.
- **دو مسیر دسترسی به دیتابیس:** `PDO` (Repositoryها) و `mysqli` (Controllerها و `DatabaseManager`). تراکنش‌ها فقط روی mysqli کار می‌کنند.

---

## Overall Score

| Category | Score |
| -------------------- | ----: |
| Security             | 7/10 |
| Architecture         | 5/10 |
| Kotlin               | 7/10 |
| Jetpack Compose      | 6/10 |
| Android              | 6/10 |
| PHP Backend          | 7/10 |
| API Design           | 5/10 |
| Database             | 6/10 |
| Performance          | 5/10 |
| Memory Management    | 7/10 |
| Error Handling       | 6/10 |
| Testing              | 2/10 |
| Code Quality         | 5/10 |
| Maintainability      | 5/10 |
| Scalability          | 5/10 |
| Production Readiness | 6/10 |

**Overall: 5.8 / 10**

---

## Security Audit

### Critical Issues

#### [CRITICAL] تخلیه‌ی worker pool سرور از طریق `sleep()` مسدودکننده در مسیر لاگین

> ✅ **رفع شد:** `sleep()` از `LoginAttemptLimiter` کاملاً حذف شد؛ `SecurityAlerter::sendTelegram` به `register_shutdown_function` (+ `fastcgi_finish_request` در صورت وجود) منتقل شد تا تماس تلگرام دیگر مسیر بحرانی را مسدود نکند.

**File:**
`PHP/src/Services/LoginAttemptLimiter.php`

**Location:**
خط ۵۷–۵۹ (و `PHP/src/Services/SecurityAlerter.php` خط ۸۱)

**Problem:**
```php
// LoginAttemptLimiter.php:57-59
$delay = min(2 ** $userAttempts, self::MAX_BACKOFF_SECONDS); // تا ۸ ثانیه
($this->sleeper)($delay);   // پیش‌فرض: sleep($seconds)
```
هر تلاش ناموفق ورود، PHP worker را تا **۸ ثانیه** بلوکه می‌کند. علاوه بر آن، دقیقاً در تلاش پنجم `SecurityAlerter::alert()` صدا زده می‌شود که یک درخواست **همگام** به Telegram API با timeout سه‌ثانیه‌ای می‌فرستد:
```php
// SecurityAlerter.php:81
@file_get_contents($url, false, $context);   // timeout = 3s
```
یعنی یک درخواست ناموفق ورود می‌تواند تا **۱۱ ثانیه** یک worker را نگه دارد.

**Why it matters:**
تعداد workerهای PHP-FPM محدود است (معمولاً ۵–۵۰ روی هاست اشتراکی). مهاجم بدون نیاز به حدس زدن رمز، فقط با ارسال درخواست‌های موازی `POST auth/login` با نام‌های کاربری تصادفی، همه‌ی workerها را در حالت `sleep` نگه می‌دارد. سقف `MAX_IP_ATTEMPTS = 50` جلوی این را نمی‌گیرد، چون `sleep` پیش از رسیدن به سقف هم اجرا می‌شود و مهاجم می‌تواند IP بچرخاند. `AuthController::refresh()` (خط ۱۶۸) و `DiagnosticsController::reportCrash()` (خط ۸۷) هم همین متد را صدا می‌زنند.

**Impact:**
Denial of Service کامل روی کل API — شامل خود اپلیکیشن اندروید که برای هر درخواست به همان سرور وابسته است. عملیات انبار متوقف می‌شود.

**Recommended Fix:**
تأخیر تصاعدی را از «مسدود کردن worker» به «رد کردن سریع» تبدیل کن:
```php
public function isLocked(string $username, string $ipAddress): bool {
    if ($this->getCount($this->userKey($username)) >= self::MAX_USER_ATTEMPTS) {
        return true;
    }
    // به‌جای sleep: زمان مجاز تلاش بعدی ذخیره می‌شود و فوراً ۴۲۹ برمی‌گردد
    return $this->getNextAllowedAt($this->userKey($username)) > time();
}

public function registerFailedAttempt(string $username, string $ipAddress): void {
    $attempts = $this->increment($this->userKey($username));
    $backoff  = min(2 ** $attempts, self::MAX_BACKOFF_SECONDS);
    $this->setNextAllowedAt($this->userKey($username), time() + $backoff);
    // هیچ sleep()ای — پاسخ فوراً برمی‌گردد
}
```
و `SecurityAlerter::sendTelegram()` را از مسیر درخواست خارج کن (صف، cron، یا حداقل `register_shutdown_function` بعد از `fastcgi_finish_request()`).

**Priority:**
CRITICAL

**Estimated Effort:**
Low (حدود نصف روز)

---

### High Issues

#### [HIGH] رازهای کلاینت با XOR تک‌بایتی «محافظت» شده‌اند

**File:**
`app/src/main/cpp/secrets.cpp`

**Location:**
خط ۶ (`const uint8_t XOR_KEY = 0x5A;`)، توابع `n0`–`n7`

**Problem:**
`API_KEY`، `LICENSE_KEY`، `EXPECTED_SIGNATURE_HASH` و همه‌ی URLها با XOR تک‌بایتی و کلید ثابت `0x5A` در باینری نیتیو ذخیره شده‌اند. استخراج آن‌ها به این سادگی است:
```bash
unzip app.apk lib/arm64-v8a/libsecrets.so
python3 -c "import sys;d=open('libsecrets.so','rb').read();sys.stdout.buffer.write(bytes(b^0x5A for b in d))" | strings
```
`RegisterNatives` نام متدها را از symbol table پنهان می‌کند و ProGuard هم `.so` را لمس نمی‌کند — هیچ‌کدام کمکی نمی‌کنند، چون بایت‌های رمزشده در `.rodata` قابل مشاهده‌اند و کلید فقط یک بایت است.

**Why it matters:**
`UPDATE_CHECK_API_KEY` تنها گیت endpoint `utility/check-update` است (`UtilityController.php`، حوالی خط ۲۰۰). `LICENSE_KEY` تنها چیزی است که `license/validate` را باز می‌کند. با استخراج این دو، مکانیزم لایسنس عملاً بی‌اثر می‌شود.

> این مسئله **قبلاً شناسایی شده** و `scripts/ROTATE_SECRETS.md` راهنمای چرخش آن را دارد، اما چرخش انجام نشده و خودِ راهنما تصریح می‌کند «XOR تک‌بایتی رمزنگاری نیست».

**Impact:**
دور زدن کامل لایسنس؛ دسترسی به endpointهای بدون auth که با API key محافظت می‌شوند. مقادیر فعلی در تاریخچه‌ی گیت هم باقی مانده‌اند.

**Recommended Fix:**
۱. **فوری:** طبق `scripts/ROTATE_SECRETS.md` هر دو مقدار را بچرخان (کاهش پنجره، نه رفع مسئله).
۲. **واقعی:** راز را از کلاینت حذف کن و به مدل challenge–response برو:
```text
کلاینت → POST /license/challenge  { deviceId }
سرور   → { nonce, expiresAt }
کلاینت → POST /license/validate   { deviceId, nonce, hmac = HMAC(deviceKey, nonce) }
```
که `deviceKey` هنگام اولین فعال‌سازی صادر و در Android Keystore ذخیره می‌شود — زیرساخت `CryptoManager` از قبل موجود است.

**Priority:**
HIGH

**Estimated Effort:**
Medium (چرخش: ۲ ساعت · challenge–response: ۲–۳ روز)

---

#### [HIGH] گیت نسخه‌ی حداقلی با حذف یک هدر دور زده می‌شود

> ✅ **رفع شد:** هر دو پیاده‌سازی (`MinVersionGate::enforce` و `AuthenticatesRequests::enforceMinAppVersion`) اکنون نبود/خالی‌بودن هدر `X-App-Version` را رد می‌کنند (۴۲۶) به‌جای عبور آزاد.

**File:**
`PHP/src/Core/MinVersionGate.php` و `PHP/src/Core/AuthenticatesRequests.php`

**Location:**
`MinVersionGate.php:11-14` · `AuthenticatesRequests.php:52-55`

**Problem:**
```php
public static function enforce(Request $request): void {
    $appVersion = $request->getHeader('X-App-Version');
    if (!$appVersion) {
        return;                       // ← نبود هدر = عبور آزاد
    }
    ...
}
```
کامنت `RetrofitClient.kt:120` هدف را صریح بیان می‌کند: «قفل نسخه‌ی منقضی قبلاً فقط سمت کلاینت بود؛ یک کلاینت دستکاری‌شده می‌توانست با دور زدن دیالوگ همچنان به همه‌ی APIها دسترسی داشته باشد». اما پیاده‌سازی سمت سرور دقیقاً همان دور زدن را با یک `curl` بدون هدر مجاز می‌کند.

**Why it matters:**
هدف این گیت جلوگیری از کار کردن نسخه‌های قدیمی با قراردادهای API تغییرکرده است. حذف یک هدر، کل گیت را خنثی می‌کند.

**Impact:**
اجرای سیاست نسخه‌ی حداقلی بی‌اثر است؛ نسخه‌های قدیمی می‌توانند به قراردادهای تغییرکرده داده‌ی نامعتبر بفرستند.

**Recommended Fix:**
نبود هدر را رد کن، نه قبول:
```php
$appVersion = $request->getHeader('X-App-Version');
if ($appVersion === null || $appVersion === '') {
    Response::error('هدر X-App-Version الزامی است.', 426);
}
```
اگر نگران کلاینت‌های قدیمی هستی، یک تاریخ cutoff بگذار و تا آن تاریخ فقط لاگ کن.

**Priority:**
HIGH

**Estimated Effort:**
Low (یک ساعت + هماهنگی انتشار)

---

#### [HIGH] endpoint گزارش کرش بدون auth با rate-limiter مسدودکننده

> ✅ **رفع شد:** یک `CrashReportRateLimiter` اختصاصی و غیرمسدودکننده (سقف ۶۰ گزارش/ساعت به‌ازای هر IP، بدون `sleep`) جایگزین `LoginAttemptLimiter` شد؛ عبور از سقف حالا بی‌صدا `200` برمی‌گرداند نه `429`.

**File:**
`PHP/src/Controllers/DiagnosticsController.php`

**Location:**
خط ۸۳–۸۷

**Problem:**
```php
$rateLimitKey = 'crash_' . $this->request->getClientIp();
if ($this->rateLimiter->isLocked($rateLimitKey, $rateLimitKey)) {
    Response::json([...], 429);
}
$this->rateLimiter->registerFailedAttempt($rateLimitKey, $rateLimitKey);  // ← sleep(2..8)
```
`LoginAttemptLimiter` برای شمارش تلاش‌های ناموفق **ورود** طراحی شده و `sleep()` بخشی از قرارداد آن است. استفاده‌ی مجدد از آن به‌عنوان rate-limiter عمومی دو پیامد دارد:

۱. هر گزارش کرش قانونی حداقل ۲ و تا ۸ ثانیه یک worker را نگه می‌دارد.
۲. `MAX_USER_ATTEMPTS = 5` یعنی بعد از **۵ کرش در ۱۵ دقیقه** از یک IP، گزارش‌ها ۴۲۹ می‌گیرند — دقیقاً وقتی اپ در حال کرش پیاپی است و بیشترین نیاز به داده را داری.

**Why it matters:**
این مسیر auth ندارد (`src/routes/api_v2.php:556`, `'auth' => false`)، پس هرکسی می‌تواند آن را برای بستن workerها صدا بزند. همچنین قابلیت observability دقیقاً در بدترین لحظه از کار می‌افتد.

**Impact:**
هم بردار DoS، هم از دست رفتن داده‌ی کرش در رویدادهای انبوه (مثلاً یک رگرسیون در نسخه‌ی جدید).

**Recommended Fix:**
یک rate-limiter سبک و غیرمسدودکننده‌ی مخصوص همین endpoint بنویس (token bucket روی APCu، بدون `sleep`)، سقف را واقع‌بینانه بگذار (مثلاً ۶۰ گزارش در ساعت به‌ازای هر IP)، و در صورت عبور از سقف **بی‌صدا drop کن و ۲۰۰ برگردان** تا کلاینت retry نکند.

**Priority:**
HIGH

**Estimated Effort:**
Low

---

#### [HIGH] ~~مسیر build نسخه‌ی release هرگز در CI اجرا نمی‌شود~~ — تصحیح‌شده، سپس رفع شد

> **تصحیح:** خواندن اولیه‌ی من از `.github/workflows/ci.yml` با `head -80` بخش پایانی فایل را قطع کرده بود. `lintDebug`/`lintRelease` **از قبل** در CI وجود داشتند. چیزی که واقعاً غایب بود فقط `assembleRelease` (اجرای واقعی R8/shrinking، نه فقط قوانین Lint) بود. متن زیر برای حفظ سابقه دست‌نخورده مانده؛ رجوع کنید به «وضعیت» پایین برای اصلاح.

**File:**
`.github/workflows/ci.yml`

**Location:**
job `android`

**Problem:**
`lintDebug`/`lintRelease` اجرا می‌شدند، اما `assembleRelease` — یعنی خودِ اجرای R8/ProGuard — هیچ‌جا نبود.

**Why it matters:**
پیکربندی release این پروژه تهاجمی است: `isMinifyEnabled = true` + `android.enableR8.fullMode=true` + `-repackageclasses` + دیکشنری obfuscation. مدل‌های Gson مثل `ChatMessage` و `ChatMessagesResponse` در پکیج `com.atk.atk_cargo.api` زندگی می‌کنند که **خارج از** قانون `-keep class com.atk.atk_cargo.data.model.**` است و فقط با قانون `-keepclassmembers,allowobfuscation ... @SerializedName <fields>` زنده می‌مانند. کافی است کسی یک فیلد بدون `@SerializedName` اضافه کند تا پارس JSON **فقط در release** بشکند — و `lintRelease` به‌تنهایی این را نمی‌گرفت چون Lint شامل اجرای واقعی R8 نیست.

**Impact:**
شکست‌های مختص R8 (بازتاب، keep rule گمشده) تا انتشار روی دستگاه واقعی کشف نمی‌شدند. برای اپی که خودش را از سرور به‌روز می‌کند، یک نسخه‌ی خراب یعنی مداخله‌ی دستی روی تک‌تک دستگاه‌ها.

**وضعیت:** ✅ رفع شد — مرحله‌ی `Assemble release (R8 gate)` (`./gradlew :app:assembleRelease`) و آپلود `mapping.txt` به `.github/workflows/ci.yml` اضافه شد.

**Priority:**
HIGH

**Estimated Effort:**
Low

---

#### [HIGH] Foreground Service با poll دائمی، ناسازگار با سقف `dataSync` در Android 14+

> ✅ **رفع شد (فاز ۲، مورد ۹):** `LoadingNotificationService` حذف و با `LoadingNotificationWorker` مبتنی بر `WorkManager` جایگزین شد؛ همان الگوی `ChatNotificationWorker`. فاصله‌ی polling به حداقل مجاز `PeriodicWorkRequest` (۱۵ دقیقه) افزایش یافت. رفرش فوری (بی‌صداکردن/فعال‌سازی مجدد کشتی) با `OneTimeWorkRequest` انجام می‌شود.

**File:**
`app/src/main/java/com/atk/atk_cargo/api/LoadingNotificationService.kt`

**Location:**
خط ۱۳۷–۱۵۴ · `app/src/main/AndroidManifest.xml:77-81`

**Problem:**
```kotlin
while (isActive) {
    try { fetchAndNotify(false) } catch (e: Exception) { ... }
    delay(TimeUnit.MINUTES.toMillis(UPDATE_INTERVAL_MINUTES).milliseconds)  // ۵ دقیقه
}
```
سرویس با `android:foregroundServiceType="dataSync"` اعلام شده و `targetSdk = 34` است. از Android 14 به بعد سرویس‌های `dataSync` سقف تجمعی **۶ ساعت در هر ۲۴ ساعت** دارند؛ پس از آن سیستم `Service.onTimeout()` را صدا می‌زند و اگر سرویس متوقف نشود ANR می‌دهد. این حلقه هرگز خودش را متوقف نمی‌کند.

نکته‌ی دوم: بررسی `userType != "admin"` **داخل** `fetchAndNotify` است (خط ۱۶۰)، نه پیش از حلقه — پس برای کاربران غیرادمین هم سرویس و حلقه‌ی ۵ دقیقه‌ای زنده می‌ماند و فقط بی‌کار می‌چرخد.

**Impact:**
قطع بی‌صدای یک قابلیت اصلی روی نسخه‌های جدید اندروید، به‌علاوه‌ی مصرف باتری برای کاربرانی که اصلاً نباید سرویس داشته باشند.

**Recommended Fix:**
این سرویس را با `PeriodicWorkRequest` جایگزین کن — دقیقاً همان الگویی که پروژه از قبل برای `ChatNotificationWorker` استفاده می‌کند (`StartupViewModel.kt:330`). WorkManager سقف FGS ندارد، در برابر بوت مقاوم است و توسط سیستم زمان‌بندی می‌شود. اگر نوتیفیکیشن دائمی واقعاً لازم است، `onTimeout()` را پیاده کن و بررسی `admin` را پیش از `startForeground` منتقل کن.

**Priority:**
HIGH

**Estimated Effort:**
Medium
---

### Medium Issues

#### [MEDIUM] شمارنده‌ی rate-limit فایل‌محور دچار race condition است

**File:**
`PHP/src/Services/LoginAttemptLimiter.php` · `PHP/src/Services/PasswordGateService.php`

**Location:**
`LoginAttemptLimiter.php:75-86` · `PasswordGateService.php:77-88`

**Problem:**
وقتی APCu در دسترس نباشد (که روی هاست اشتراکی رایج است)، شمارنده به فایل fallback می‌کند:
```php
private function increment(string $key): int {
    $count = $this->getCount($key) + 1;       // خواندن
    ...
    $file = $this->fallbackFile($key);
    file_put_contents($file, json_encode([...]));   // نوشتن — بدون قفل
    return $count;
}
```
این یک read-modify-write غیراتمیک بدون `LOCK_EX` است. ده درخواست هم‌زمان همگی `getCount()` را `0` می‌خوانند و همگی `1` می‌نویسند.

**Why it matters:**
سقف ۵ تلاش با موازی‌سازی به‌سادگی دور زده می‌شود. وجود فایل‌های `PHP/log/loginlimit_*.json` در ریپازیتوری نشان می‌دهد مسیر fallback واقعاً روی این محیط فعال است، نه APCu.

**Impact:**
brute-force رمز عبور با درخواست‌های موازی — سقف ۵ تلاشی عملاً به تعداد workerهای هم‌زمان تبدیل می‌شود.

**Recommended Fix:**
شمارنده را به دیتابیس منتقل کن (که از قبل تراکنشی است) یا حداقل قفل انحصاری بگیر:
```php
$fp = fopen($file, 'c+');
flock($fp, LOCK_EX);
$data  = json_decode(stream_get_contents($fp), true) ?: [];
$count = (($data['expires'] ?? 0) < time() ? 0 : ($data['count'] ?? 0)) + 1;
ftruncate($fp, 0); rewind($fp);
fwrite($fp, json_encode(['count' => $count, 'expires' => time() + self::LOCKOUT_WINDOW_SECONDS]));
flock($fp, LOCK_UN); fclose($fp);
```
راه‌حل بهتر: جدول `login_attempts` با `INSERT ... ON DUPLICATE KEY UPDATE count = count + 1`.

**Priority:**
MEDIUM

**Estimated Effort:**
Low

---

#### [MEDIUM] شمارش نام کاربری و افشای `userType` بدون احراز هویت

> ✅ **رفع شد (فاز ۲، مورد ۱۹):** `AuthController::checkSession` بازنویسی شد تا مستقیماً از `SessionService::validateAndGetUserType` استفاده کند (به‌جای `UserRepository::getByUsername` + `isValidToken` جدا). پاسخ برای «کاربر وجود ندارد» و «نشست نامعتبر» اکنون کاملاً یکسان است (همان پیام، همان کد ۲۰۰، `userType: null`)؛ `userType` فقط وقتی نشست واقعاً معتبر باشد برمی‌گردد. هر دو مصرف‌کننده‌ی کلاینت (`StartupViewModel`, `SessionValidator`) فقط `success` را می‌خوانند، پس بدون تغییر سازگارند.


**File:**
`PHP/src/Controllers/AuthController.php`

**Location:**
خط ۲۰۶–۲۳۱

**Problem:**
```php
if (!$user) {
    Response::json(['success' => false, 'message' => 'کاربر در سیستم وجود ندارد', 'userType' => null], 200);
}
...
} else {
    Response::json([
        'success' => false,
        'message' => 'جلسه کاربر فعال نیست. لطفاً وارد شوید.',
        'userType' => $user['userType']          // ← افشا حتی وقتی نشست نامعتبر است
    ]);
}
```
مسیر `auth/session` با `'auth' => false` ثبت شده (`api_v2.php:375`). پاسخ برای «کاربر وجود ندارد» و «کاربر وجود دارد ولی نشست فعال نیست» متفاوت است، و در حالت دوم `userType` هم برمی‌گردد.

**Why it matters:**
یک مهاجم بدون هیچ اعتبارنامه‌ای می‌تواند فهرست کامل نام‌های کاربری معتبر را استخراج کند و بفهمد کدام‌ها `admin` هستند — یعنی دقیقاً بداند brute-force را روی چه هدفی متمرکز کند. هیچ rate-limiting هم روی این مسیر نیست.

**Impact:**
شناسایی هدف پیش از حمله؛ افشای اینکه کدام حساب‌ها امتیاز مدیریتی دارند.

**Recommended Fix:**
پاسخ‌ها را یکسان کن و `userType` را فقط در حالت نشست معتبر برگردان:
```php
$isActive = ($deviceId && $sessionToken)
    ? $this->sessionService->isValidToken($username, $deviceId, $sessionToken)
    : false;

if (!$isActive) {
    Response::json([
        'success'  => false,
        'message'  => 'جلسه کاربر فعال نیست. لطفاً وارد شوید.',
        'userType' => null,
    ]);
}
```
و `LoginAttemptLimiter` را روی این مسیر هم اعمال کن.

**Priority:**
MEDIUM

**Estimated Effort:**
Low

---

#### [MEDIUM] هر کاربر احرازشده می‌تواند فهرست کامل ادمین‌ها را بگیرد

**File:**
`PHP/src/Controllers/UserController.php` · `PHP/src/Services/UserService.php`

**Location:**
`UserController.php:96-99` · `UserService.php:59-61`

**Problem:**
`getAdminUsers` در فهرست `ADMIN_ONLY_ACTIONS` نیست، پس هر کاربر با نشست معتبر (از جمله `operator` و `verifier`) می‌تواند `id`، `username`، `fullName` و `userType` همه‌ی ادمین‌ها را دریافت کند. route هم `'permission' => null` دارد (`api_v2.php:454`).

**Why it matters:**
کامنت کد توضیح می‌دهد این برای «پرکردن مقصدهای چت با مدیر» است — اما `ChatController::getMessages` صراحتاً غیرادمین‌ها را رد می‌کند (خط ۱۰۰–۱۰۲)، پس کاربران غیرادمین اصلاً به چت دسترسی ندارند و این داده برایشان کاربردی ندارد.

**Impact:**
افشای غیرضروری هویت حساب‌های ممتاز به همه‌ی کاربران. یک حساب `operator` به خطر افتاده، مستقیماً فهرست اهداف بعدی را می‌دهد.

**Recommended Fix:**
یا `getAdminUsers` را پشت مجوز `admin_chat` قفل کن، یا فقط `fullName` را برگردان و `username`/`id` را حذف کن.

**Priority:**
MEDIUM

**Estimated Effort:**
Low

---

#### [MEDIUM] پیام‌های حذف‌شده‌ی چت همچنان با متن کامل به کلاینت ارسال می‌شوند

**File:**
`PHP/src/Controllers/ChatController.php`

**Location:**
خط ۱۱۱–۱۴۴ (کوئری `$baseFields`)

**Problem:**
`deleteMessage` فقط `is_deleted = 1` می‌گذارد (soft delete، خط ۲۳۲)، اما `getMessages` هیچ فیلتری روی `is_deleted` ندارد و `c.message` را همیشه کامل برمی‌گرداند. پنهان‌سازی فقط سمت کلاینت انجام می‌شود.

**Why it matters:**
هرکسی که پاسخ خام API را ببیند (Charles/Frida/لاگ debug/کش HTTP) متن پیام‌های «حذف‌شده» را می‌بیند. برای کاربر، «حذف» یعنی حذف.

**Impact:**
نقض انتظار کاربر از حذف پیام؛ نشت داده‌ای که قرار بوده پاک شود.

**Recommended Fix:**
```sql
-- در $baseFields
CASE WHEN c.is_deleted = 1 THEN NULL ELSE c.message END AS message
```
همچنین `editMessage`/`deleteMessage` باید پیش از عمل، `is_deleted = 0` را هم بررسی کنند تا پیام حذف‌شده دوباره ویرایش نشود.

**Priority:**
MEDIUM

**Estimated Effort:**
Low

---

#### [MEDIUM] دستورهای `<Directory>` و `<DirectoryMatch>` در `.htaccess` نامعتبرند

**File:**
`PHP/config/.htaccess`

**Location:**
خط ۱۷–۲۳، ۵۵–۶۳

**Problem:**
```apache
<Directory "uploads">
    <FilesMatch "\.php$">
        Order allow,deny
        Deny from all
    </FilesMatch>
</Directory>
...
<DirectoryMatch "(^|/)(logs|backups|private|secret)/">
    Order allow,deny
    Deny from all
</DirectoryMatch>
```
`<Directory>` و `<DirectoryMatch>` فقط در `httpd.conf`/vhost مجازند و در `.htaccess` باعث `500 Internal Server Error` می‌شوند. جالب اینکه فایل `PHP/logs/.htaccess` خودش این نکته را در کامنت توضیح داده («`<DirectoryMatch>` در .htaccess ریشه‌ی PHP/ نامعتبر است») و بلوک متناظر از `.htaccess` ریشه حذف شده (خط ۴۸–۵۴ خالی است) — اما `config/.htaccess` هنوز آن را دارد.

**Why it matters:**
یا سرور روی هر درخواست به `PHP/config/` خطای ۵۰۰ می‌دهد (که تصادفاً محافظت می‌کند، ولی به‌شکل غیرقابل‌اتکا)، یا `AllowOverride` این دستورها را نادیده می‌گیرد و حفاظت اصلاً وجود ندارد. هیچ‌کدام قابل استناد نیست.

**Impact:**
محافظت پوشه‌ی config در وضعیت نامعلوم است. اتکا به رفتار تعریف‌نشده‌ی وب‌سرور برای امنیت.

**Recommended Fix:**
بلوک‌های `<Directory>`/`<DirectoryMatch>` را حذف کن و به جای آن‌ها همان الگویی را به کار ببر که `PHP/logs/.htaccess` استفاده می‌کند — یک `.htaccess` مستقیم داخل هر پوشه:
```apache
<IfModule mod_authz_core.c>
    Require all denied
</IfModule>
<IfModule !mod_authz_core.c>
    Order deny,allow
    Deny from all
</IfModule>
```

**Priority:**
MEDIUM

**Estimated Effort:**
Low

---

#### [MEDIUM] پنل‌های مدیریتی وب بدون محدودیت شبکه در دسترس عمومی‌اند

**File:**
`PHP/PermissionManager.php` · `PHP/file_manager.php` · `PHP/Lic/index.php`

**Location:**
سطح ریشه‌ی وب

**Problem:**
هر سه پنل با یک رمز عبور تک‌عاملی از `.env` محافظت می‌شوند (`ADMIN_PASSWORD_HASH`, `LIC_ADMIN_PASSWORD_HASH`) و هیچ محدودیت IP، VPN یا احراز هویت دومرحله‌ای ندارند. `file_manager.php` فهرست تمام فایل‌های PHP ریشه را با نام، اندازه و زمان تغییر برمی‌گرداند.

> کیفیت خودِ پیاده‌سازی خوب است: CSRF با `hash_equals`، `session_regenerate_id(true)` هنگام ورود، کوکی `httponly`/`samesite`/`secure`، انقضای بی‌کاری ۳۰ دقیقه، و `LoginAttemptLimiter` روی رمز پنل. مسئله سطح در دسترس بودن است، نه کیفیت کد.

**Why it matters:**
`PermissionManager.php` می‌تواند مجوز `manage_users` را به هر کاربری بدهد. یک رمز عبور به‌تنهایی تنها چیزی است که بین اینترنت و کنترل کامل مجوزهای سیستم ایستاده.

**Impact:**
یک رمز لو رفته = تصاحب کامل سیستم مجوزها و در نتیجه‌ی آن، داده‌ها.

**Recommended Fix:**
allowlist سطح وب‌سرور روی این سه مسیر:
```apache
<FilesMatch "^(PermissionManager|file_manager)\.php$">
    Require ip 203.0.113.0/24
</FilesMatch>
```
و ترجیحاً انتقال کل پنل‌ها پشت VPN یا HTTP Basic اضافه. `file_manager.php` اگر کاربردی ندارد حذف شود.

**Priority:**
MEDIUM

**Estimated Effort:**
Low

---

#### [MEDIUM] تزریق به لاگ از طریق ورودی‌های کنترل‌شده توسط کاربر

**File:**
`PHP/src/Services/SessionService.php` · `PHP/file_manager.php`

**Location:**
`SessionService.php:357-366` · `file_manager.php:71-79`

**Problem:**
```php
// SessionService.php
$logEntry = "[$timestamp] $action | کاربر: $username";
if ($deviceId) { $logEntry .= " | دستگاه: $deviceId"; }
$logEntry .= " | IP: $ip\n";
file_put_contents($logFile, $logEntry, FILE_APPEND | LOCK_EX);

// file_manager.php
$userAgent = $_SERVER['HTTP_USER_AGENT'] ?? 'unknown';
$logEntry .= " | User-Agent: $userAgent\n";
```
`$username`، `$deviceId` و `User-Agent` بدون حذف newline در فایل لاگ درج می‌شوند. `deviceId` مستقیماً از هدر `X-Device-Id` می‌آید.

**Why it matters:**
مهاجم با فرستادن `deviceId` حاوی `\n[2026-01-01 00:00:00] LOGIN | کاربر: admin` می‌تواند رکوردهای جعلی در `session_activity.log` بسازد و ردپای واقعی خودش را در میان ورودی‌های ساختگی گم کند. `Logger::sanitizeMessage()` فقط سه الگوی JSON (`password`، `csrf_token`، `session_token`) را می‌پوشاند و اصلاً روی این مسیر اجرا نمی‌شود، چون `logActivity` مستقیم `file_put_contents` می‌کند.

**Impact:**
از بین رفتن قابلیت اتکای audit trail — دقیقاً چیزی که در بررسی یک حادثه‌ی امنیتی به آن نیاز داری.

**Recommended Fix:**
همه‌ی مقادیر متغیر را پیش از درج پاک‌سازی کن، و بهتر: از `Logger` مرکزی استفاده کن به‌جای `file_put_contents` مستقیم:
```php
$safe = static fn(string $v): string => str_replace(["\r", "\n"], ' ', mb_substr($v, 0, 200));
$logEntry = sprintf('[%s] %s | کاربر: %s | دستگاه: %s | IP: %s', $timestamp, $action, $safe($username), $safe($deviceId ?? ''), $safe($ip));
```

**Priority:**
MEDIUM

**Estimated Effort:**
Low

---

#### [MEDIUM] دوره‌ی مهلت آفلاین امنیتی روی SharedPreferences قابل دستکاری است

**File:**
`app/src/main/java/com/atk/atk_cargo/security/SecurityVerifier.kt`

**Location:**
خط ۴۹، ۱۰۸–۱۲۴

**Problem:**
```kotlin
private val securityPrefs = context.getSharedPreferences("x1y2z3", Context.MODE_PRIVATE)
...
private fun resolveNetworkFailure(errorType: SecurityErrorType): Pair<Boolean, SecurityErrorType?> {
    val lastSuccess = securityPrefs.getLong(LAST_SUCCESS_TIMESTAMP_KEY, 0L)
    val withinGracePeriod = lastSuccess > 0L &&
            (System.currentTimeMillis() - lastSuccess) < OFFLINE_GRACE_PERIOD_MS   // ۳ روز
    return if (withinGracePeriod) Pair(true, null) else Pair(false, errorType)
}
```
timestamp در XML ساده و بدون رمزگذاری ذخیره می‌شود — درحالی‌که همین پروژه `CryptoManager` با Android Keystore دارد و آن را برای توکن‌ها استفاده می‌کند.

نکته‌ی دوم: `fetchLicenseInfo`/`fetchLicenseValidation` روی پاسخ‌های غیر ۲xx از `connection.inputStream` می‌خوانند که `IOException` پرتاب می‌کند؛ این استثنا تا `verifySecurityStatus` بالا می‌رود و به `resolveNetworkFailure` می‌رسد. یعنی **یک پاسخ ۴۰۳ صریح از سرور لایسنس هم به «مشکل شبکه» ترجمه می‌شود** و کاربر از طریق دوره‌ی مهلت وارد می‌شود.

**Why it matters:**
روی دستگاه روت‌شده، تنظیم دستی timestamp به «الان» + قطع شبکه = عبور نامحدود از بررسی لایسنس. و حتی بدون روت، ابطال لایسنس از سمت سرور تا ۳ روز اثری ندارد اگر سرور خطای HTTP بدهد.

**Impact:**
ابطال لایسنس قابل اجرا نیست؛ دوره‌ی مهلت به یک دور زدن دائمی تبدیل می‌شود.

**Recommended Fix:**
۱. timestamp را با `CryptoManager` رمز کن (زیرساخت موجود است).
۲. کدهای وضعیت HTTP را از خطاهای شبکه تفکیک کن:
```kotlin
val code = connection.responseCode
val body = (if (code in 200..299) connection.inputStream else connection.errorStream)
    ?.bufferedReader()?.use { it.readText() }
if (code == 403 || code == 404) return null   // رد صریح — نه «مشکل شبکه»
```
۳. دوره‌ی مهلت را به سقف تعداد دفعات محدود کن، نه فقط زمان.

**Priority:**
MEDIUM

**Estimated Effort:**
Medium

---

#### [MEDIUM] تشخیص دیباگر/Frida بدون تفکیک نوع build

**File:**
`app/src/main/java/com/atk/atk_cargo/security/SecurityVerifier.kt`

**Location:**
خط ۳۰۰–۳۳۶

**Problem:**
```kotlin
private fun isEnvironmentCompromised(): Boolean {
    if (Debug.isDebuggerConnected()) { ... return true }
    if (hasInjectedLibraries()) { ... return true }
    if (isFridaServerPortOpen()) { ... return true }
    return false
}
```
هیچ گارد `BuildConfig.DEBUG` وجود ندارد. علاوه بر آن `verifyLocalAppSignature()` هش امضا را با `EXPECTED_SIGNATURE_HASH` مقایسه می‌کند، و بیلد debug با کلید debug امضا می‌شود.

**Why it matters:**
اتصال دیباگر به بیلد debug باعث `SecurityErrorType.TAMPERED` و مسدود شدن کل اپ می‌شود. این توسعه و عیب‌یابی روی دستگاه واقعی را عملاً غیرممکن می‌کند — و توسعه‌دهنده را وسوسه می‌کند بررسی امنیتی را موقتاً کامنت کند و یادش برود برگرداند.

نکته‌ی جانبی: `isFridaServerPortOpen()` در هر startup یک اتصال TCP مسدودکننده با timeout ۲۰۰ms به `127.0.0.1:27042` می‌زند که به زمان راه‌اندازی اضافه می‌کند.

**Impact:**
تجربه‌ی توسعه‌دهنده به‌شدت آسیب می‌بیند؛ ریسک غیرفعال شدن دائمی بررسی امنیتی به‌خاطر یک تغییر موقت.

**Recommended Fix:**
بررسی‌ها را فقط در release فعال کن. چون `core:network` به `BuildConfig` ماژول `app` دسترسی ندارد، همان الگویی را به کار ببر که `RetrofitClient.init(..., debugLogging = BuildConfig.DEBUG)` استفاده می‌کند و یک پرچم به `SecurityVerifier` تزریق کن:
```kotlin
single { SecurityVerifier(androidContext(), enforceEnvironmentChecks = !BuildConfig.DEBUG) }
```

**Priority:**
MEDIUM

**Estimated Effort:**
Low

---

### Low Issues

#### [LOW] `json_encode` مستقیم داخل تگ `<script>` بدون `JSON_HEX_TAG`

**File:**
`PHP/PermissionManager.php`

**Location:**
خط ۴۶۸

**Problem:**
```php
const allData = <?php echo json_encode($all_data); ?>;
```
`$all_data['users']` کلیدهایش نام‌های کاربری از دیتابیس‌اند. اگر نام کاربری شامل `</script>` باشد، از context جاوااسکریپت خارج می‌شود.

در عمل `InputValidator::sanitize()` هنگام ساخت کاربر `htmlspecialchars` می‌زند، پس `<` به `&lt;` تبدیل می‌شود و بردار بسته است — اما این محافظت **غیرمستقیم** و شکننده است: هر مسیری که کاربر را بدون عبور از `createUser` بسازد (مثلاً درج مستقیم SQL یا یک ابزار مدیریتی)، آن را می‌شکند.

**Impact:**
XSS بالقوه در پنل مدیریت مجوزها. `Potential Issue` — برای تأیید باید بررسی شود آیا مسیری برای ساخت کاربر بدون `InputValidator` وجود دارد یا نه.

**Recommended Fix:**
```php
const allData = <?php echo json_encode($all_data, JSON_HEX_TAG | JSON_HEX_AMP | JSON_HEX_APOS | JSON_HEX_QUOT); ?>;
```
همچنین خطوط ۴۳۸ و ۴۴۱ از `addslashes()` برای درج رشته در JS استفاده می‌کنند که escaper معتبری برای جاوااسکریپت نیست؛ `json_encode` جایگزین درست است.

**Priority:**
LOW

**Estimated Effort:**
Low

---

#### [LOW] هدر HSTS روی اتصالات غیر HTTPS هم ارسال می‌شود

**File:**
`PHP/src/Core/Response.php`

**Location:**
خط ۱۹

**Problem:**
`Strict-Transport-Security` بدون بررسی `$_SERVER['HTTPS']` ارسال می‌شود. طبق RFC 6797 مرورگرها این هدر را روی اتصال ناامن نادیده می‌گیرند، پس ضرری ندارد اما بی‌اثر است و این توهم را ایجاد می‌کند که HSTS در همه‌ی حالات فعال است.

**Recommended Fix:**
ارسال مشروط، هم‌راستا با کاری که `Lic/_guard.php` برای کوکی `secure` انجام می‌دهد.

**Priority:**
LOW

**Estimated Effort:**
Low

---

#### [LOW] `bind_param` روی نتیجه‌ی `prepare` بدون بررسی null

**File:**
`PHP/src/Controllers/UserController.php`

**Location:**
خط ۲۵۴–۲۶۰

**Problem:**
```php
$stmt = $conn->prepare("UPDATE Users SET fcm_token = ? WHERE id = ?");
if (!$stmt) {
    $stmt = $conn->prepare("UPDATE users SET fcm_token = ? WHERE id = ?");   // fallback حروف کوچک
}
$stmt->bind_param("si", $token, $userId);     // ← اگر هر دو prepare شکست بخورد، fatal error
```
اگر هر دو `prepare` شکست بخورند (مثلاً ستون `fcm_token` وجود نداشته باشد)، `$stmt` مقدار `false` است و `bind_param` روی `false` یک `Error` پرتاب می‌کند.

**Impact:**
خطای ۵۰۰ به‌جای پیام خطای معنادار. کم‌اهمیت چون این endpoint اصلاً توسط کلاینت صدا زده نمی‌شود (به بخش Technical Debt مراجعه کن).

**Recommended Fix:**
بعد از fallback هم `if (!$stmt)` را بررسی کن و `Response::error(...)` بده. یا کل این متد را حذف کن.

**Priority:**
LOW

**Estimated Effort:**
Low
---

## Android Audit

### نقاط قوت (تأیید‌شده در کد)

اینها را دست‌نخورده نگه دار — از استاندارد اکثر پروژه‌های مشابه بالاترند:

- **`network_security_config.xml`** — `cleartextTrafficPermitted="false"` سراسری، به‌علاوه‌ی certificate pinning روی CA میانی/ریشه‌ی Certum با `expiration="2028-08-17"`. انتخاب پین روی CA به‌جای leaf با دلیل درست مستند شده (اپ خارج از Play Store توزیع می‌شود و مکانیزم به‌روزرسانی خودش به همین اتصال وابسته است).
- **`file_path.xml`** — از `path="."` به دو زیرپوشه‌ی مشخص محدود شده.
- **`allowBackup="false"`** + `backup_rules.xml` + `data_extraction_rules.xml` که همگی `sharedpref`/`database`/`file`/`root` را exclude می‌کنند.
- **`BootReceiver`** با `android:permission="android.permission.RECEIVE_BOOT_COMPLETED"` محافظت شده؛ `NotificationActionReceiver` و `LoadingNotificationService` هر دو `exported="false"`.
- **معماری Single Activity** — تنها یک activity export شده و آن هم فقط `MAIN`/`LAUNCHER` است. هیچ deep link، هیچ `<data>` scheme، و هیچ WebView در کل پروژه وجود ندارد؛ کل دسته‌ی حملات مبتنی بر Intent/URI/WebView حذف شده است.
- **ذخیره‌سازی توکن** — `UserPreferencesManager` نام کاربری، access token، refresh token و مجوزها را با AES-GCM از Android Keystore رمز می‌کند (`CryptoManager`)، سپس در DataStore ذخیره می‌کند.

---

#### [HIGH] `targetSdk = 34` عقب‌تر از `compileSdk = 36`

> ⏭️ **فعلاً رد شد (فاز ۲، مورد ۱۲):** طبق تصمیم کاربر. این تغییر رفتارهای سطح سیستم (edge-to-edge اجباری، محدودیت‌های FGS) را فعال می‌کند که بدون دستگاه/امولاتور واقعی قابل تست بصری نیست — کامپایل به‌تنهایی کافی نبود. باقی می‌ماند برای زمانی که تست دستی ممکن باشد.

**File:**
`app/build.gradle.kts`

**Location:**
خط ۳۴–۳۹

**Problem:**
```kotlin
compileSdk = 36
...
targetSdk = 34
```
دو سطح API عقب‌ماندگی. این باعث می‌شود اپ در حالت سازگاری اجرا شود و تغییرات رفتاری Android 15/16 (edge-to-edge اجباری، محدودیت‌های جدید FGS، تغییرات JobScheduler، محدودیت‌های ثبت گیرنده) اعمال نشوند.

**Why it matters:**
حالت سازگاری موقتی است؛ سازنده‌ها و نسخه‌های بعدی آن را حذف می‌کنند. علاوه بر این `android:screenOrientation="portrait"` روی `MainActivity` در Android 16 برای دستگاه‌های بزرگ نادیده گرفته می‌شود — و پروژه از قبل `values-land`، `values-w600dp` و `values-w1240dp` دارد که با قفل portrait در تناقض‌اند.

**Impact:**
شکست‌های ناگهانی در نسخه‌های بعدی اندروید؛ منابع layout مخصوص افقی/تبلت که هرگز اجرا نمی‌شوند (کد مرده).

**Recommended Fix:**
`targetSdk = 36` و اجرای یک پاس تست روی Android 15/16، به‌ویژه برای edge-to-edge و رفتار FGS. قفل portrait را یا حذف کن یا منابع `values-land`/`values-w*` را پاک کن.

**Priority:**
HIGH

**Estimated Effort:**
Medium

---

#### [MEDIUM] race در مقداردهی `AuthSession` هنگام cold start

> ✅ **رفع شد (فاز ۲، مورد ۱۵):** یک `CompletableDeferred` به `AuthSession` اضافه شد؛ `headersInterceptor` قبل از ساخت هدرها `AuthSession.awaitReady()` را صدا می‌زند (مسدودسازی کوتاه روی thread دیسپچر OkHttp، نه Main — هم‌راستا با الگوی موجود `runBlocking` در `TokenAuthenticator`). `AtkCargoApplication.onCreate` پس از پر کردن `AuthSession` از DataStore، `markReady()` را در یک بلوک `finally` صدا می‌زند تا حتی با خطای غیرمنتظره هم headersInterceptor برای همیشه بلاک نماند.


**File:**
`app/src/main/java/com/atk/atk_cargo/api/AtkCargoApplication.kt`

**Location:**
خط ۴۲–۴۹

**Problem:**
```kotlin
RetrofitClient.init(this, userPreferencesManager, debugLogging = BuildConfig.DEBUG)
applicationScope.launch {                       // ← ناهمگام
    AuthSession.username     = userPreferencesManager.username.first()
    AuthSession.deviceId     = userPreferencesManager.deviceId.first()
    AuthSession.sessionToken = userPreferencesManager.sessionToken.first()
    ...
}
```
`headersInterceptor` در `RetrofitClient.kt:117-119` هدرهای `X-Username`/`X-Device-Id`/`X-Session-Token` را از `AuthSession` می‌خواند و **اگر خالی باشند اصلاً اضافه‌شان نمی‌کند**. مقداردهی `AuthSession` در یک coroutine جدا انجام می‌شود که شامل خواندن DataStore از دیسک و سه عملیات رمزگشایی Keystore است.

**Why it matters:**
`MainActivity.onCreate` بلافاصله `startupViewModel.runStartupSequenceOnce()` را صدا می‌زند که سه کار شبکه‌ی موازی اجرا می‌کند. هر درخواستی که پیش از پرشدن `AuthSession` برود، بدون هدر احراز هویت ارسال می‌شود و `401` با `code = session_invalid` می‌گیرد — نه `access_token_expired` — پس `TokenAuthenticator` هم آن را refresh نمی‌کند (`TokenAuthenticator.kt:27`).

`checkSession` تصادفاً مصون است چون اعتبارنامه را در **بدنه** می‌فرستد، نه هدر. اما `chatRepository.refreshMessages()` و `PermissionPoller` این‌طور نیستند.

**Impact:**
خروج غیرمنتظره‌ی کاربر یا خطاهای گذرا هنگام راه‌اندازی سرد، به‌ویژه روی دستگاه‌های کند که خواندن دیسک بیشتر طول می‌کشد. بازتولیدش سخت است — دقیقاً همان نوع باگی که در تولید آزاردهنده می‌شود.

**Recommended Fix:**
مقداردهی را به یک `Deferred` تبدیل کن و interceptor را وادار کن منتظر بماند، یا ساده‌تر: `headersInterceptor` را طوری بنویس که در صورت خالی بودن `AuthSession` مستقیماً از `tokenStore` بخواند:
```kotlin
private val authReady: Deferred<Unit> = applicationScope.async {
    AuthSession.username = userPreferencesManager.username.first()
    ...
}
// در interceptor: runBlocking { authReady.await() }  ← روی ترد OkHttp، نه Main
```

**Priority:**
MEDIUM

**Estimated Effort:**
Low

---

#### [MEDIUM] `System.loadLibrary("secrets")` در `init` — نقطه‌ی شکست تک‌نقطه‌ای

> ✅ **رفع شد (فاز ۲، مورد ۱۶، بدون افزودن `armeabi-v7a`):** `Secrets.isAvailable` اضافه شد (بارگذاری در `try/catch`، هرگز پرتاب نمی‌کند). `AtkCargoApplication.onCreate` قبل از لمس `RetrofitClient` (که خودش `Secrets.getBaseUrl()` را بی‌قید‌وشرط در initializer صدا می‌زد) این پرچم را چک می‌کند. `StartupViewModel` یک state جدید `NativeLibraryUnavailable` دارد که بدون هیچ فراخوانی شبکه‌ای (UpdateManager/SecurityVerifier/RetrofitClient) مستقیماً یک صفحه‌ی خطای صریح (بدون دکمه‌ی «تلاش مجدد»، چون ناسازگاری ABI با retry حل نمی‌شود) نشان می‌دهد. تصمیم افزودن `armeabi-v7a` به release به بعد موکول شد (نیازمند تصمیم محصولی).


**File:**
`core/network/src/main/java/com/atk/atk_cargo/api/Secrets.kt`

**Location:**
خط ۴–۶

**Problem:**
```kotlin
object Secrets {
    init { System.loadLibrary("secrets") }
    external fun getBaseUrl(): String
    ...
}
```
`RetrofitClient.kt:29` این را در سطح property می‌خواند: `private val BASE_URL = Secrets.getBaseUrl()`. اگر `libsecrets.so` بارگذاری نشود، `UnsatisfiedLinkError` در initializer شیء `Secrets` رخ می‌دهد و به `ExceptionInInitializerError` تبدیل می‌شود که غیرقابل بازیابی است.

نسخه‌ی release فقط `arm64-v8a` می‌سازد (`build.gradle.kts:97`) درحالی‌که `minSdk = 28` است و دستگاه‌های ۳۲ بیتی در آن بازه هنوز وجود دارند.

**Why it matters:**
`try/catch` در `MainActivity.onCreate` (خط ۱۵۶) این را نمی‌گیرد، چون خطا هنگام بارگذاری کلاس در Koin/RetrofitClient رخ می‌دهد. `CrashReporter` نصب شده و آن را ثبت می‌کند، ولی اپ غیرقابل استفاده است.

**Impact:**
کرش کامل و بدون بازیابی روی هر ABI پشتیبانی‌نشده.

**Recommended Fix:**
بارگذاری را ایمن کن و یک شکست معنادار بده:
```kotlin
object Secrets {
    val isAvailable: Boolean = runCatching { System.loadLibrary("secrets") }.isSuccess
    ...
}
```
سپس در `StartupViewModel` اگر `!Secrets.isAvailable` بود یک صفحه‌ی خطای صریح («این نسخه با دستگاه شما سازگار نیست») نمایش بده. اگر پشتیبانی از ۳۲ بیت لازم است، `armeabi-v7a` را به abiFilters نسخه‌ی release اضافه کن (در نسخه‌ی debug از قبل هست).

**Priority:**
MEDIUM

**Estimated Effort:**
Low

---

## Kotlin Audit

### نقاط قوت

کیفیت Kotlin این پروژه بالای میانگین است و آمار آن را تأیید می‌کند:

- **۹ مورد `!!` در ۵۱٬۷۳۹ خط** — و بیشترشان پس از یک بررسی null هستند.
- **صفر مورد `GlobalScope`**، صفر `println`، فقط یک `runBlocking` (در `TokenAuthenticator` که طبق قرارداد `okhttp3.Authenticator` همگام است — استفاده‌ی درست).
- **`lateinit` فقط ۳ بار در کد production**.
- استفاده‌ی درست از `sealed interface` برای state و رویداد (`StartupState`, `CargoDialog`, `StartupEvent`, `DownloadState`).
- `Channel(BUFFERED)` + `receiveAsFlow()` برای رویدادهای یک‌باره — الگوی درست به‌جای `SharedFlow` برای side effect.
- `@Volatile` روی فیلدهای `AuthSession` و `AppDatabase.INSTANCE`.
- `collectAsStateWithLifecycle` در **۱۰۲ نقطه** و `collectAsState()` خام در **صفر نقطه**.

---

#### [MEDIUM] بلوک `catch` غیرقابل دسترس، لغو coroutine را بلعیده می‌کند

**File:**
`feature/update/src/main/java/com/atk/atk_cargo/api/UpdateManager.kt`

**Location:**
خط ۳۷۵–۳۸۳

**Problem:**
```kotlin
} catch (_: CancellationException) {
    _downloadState.value = DownloadState.Paused(...)      // ← اولین catch همه را می‌گیرد
} catch (e: CancellationException) {
    throw e                                               // ← هرگز اجرا نمی‌شود
} catch (e: Exception) {
    handleDownloadError(e)
}
```
دو بلوک `catch` برای یک نوع استثنا. دومی مرده است — کامپایلر هشدار می‌دهد ولی خطا نمی‌دهد.

**Why it matters:**
`CancellationException` باید همیشه دوباره پرتاب شود. اینجا وقتی `onCleared()` یا `cancelDownload()` scope را لغو می‌کند، لغو به coroutine والد منتشر نمی‌شود و state به `Paused` می‌رود — انگار کاربر دانلود را متوقف کرده است. این structured concurrency را می‌شکند.

**Impact:**
`DisposableEffect`/`onCleared` تصور می‌کنند لغو انجام شده درحالی‌که وضعیت `Paused` باقی مانده؛ ممکن است دیالوگ به‌روزرسانی در حالت متناقض ظاهر شود.

**Recommended Fix:**
لغوی که کاربر آغاز کرده را از لغوی که scope آغاز کرده تفکیک کن. `cancelDownload()` باید صریحاً `_downloadState.value = Paused(...)` را قبل از `cancel()` بگذارد، و بلوک catch فقط باید rethrow کند:
```kotlin
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    handleDownloadError(e)
}
```

**Priority:**
MEDIUM

**Estimated Effort:**
Low

---

#### [MEDIUM] `FloatTypeAdapter` خطاهای پارس را به `0f` خاموش تبدیل می‌کند

**File:**
`core/network/src/main/java/com/atk/atk_cargo/api/RetrofitClient.kt`

**Location:**
خط ۵۶–۸۹

**Problem:**
```kotlin
override fun read(reader: JsonReader): Float {
    return try {
        when (reader.peek()) {
            JsonToken.NULL   -> { reader.nextNull(); 0f }
            JsonToken.NUMBER -> reader.nextDouble().toFloat()
            JsonToken.STRING -> reader.nextString().toFloatOrNull() ?: 0f
            else             -> { reader.skipValue(); 0f }
        }
    } catch (e: Exception) { 0f }
}
```
هر مقدار غیرقابل پارس به `0f` تبدیل می‌شود. در اپلیکیشنی که کارش **توزین بار** است، وزن صفر یک مقدار پیش‌فرض بی‌ضرر نیست — یک داده‌ی غلط است که در محاسبات تناژ و باقی‌مانده‌ی سهمیه پخش می‌شود.

**نکته‌ی دوم و مهم‌تر:** `registerTypeAdapter(Float::class.java, ...)` در Kotlin به `float` بدوی نگاشت می‌شود. فیلدهای nullable مثل `val loadableTonnage: Float?` به `java.lang.Float` نگاشت می‌شوند و **این آداپتور را استفاده نمی‌کنند** — یعنی رفتار پارس بین فیلدهای nullable و non-null متفاوت است.

**Impact:**
خرابی خاموش داده در محاسبات وزن؛ رفتار ناسازگار بین فیلدهای Float و Float?. `Log.w` هم در release توسط `-assumenosideeffects` حذف می‌شود، پس حتی ردی هم باقی نمی‌ماند.

**Recommended Fix:**
هر دو نوع را ثبت کن و برای فیلدهای وزن، شکست را صریح کن:
```kotlin
.registerTypeAdapter(Float::class.java,     FloatTypeAdapter())
.registerTypeAdapter(Float::class.javaObjectType, FloatTypeAdapter())
```
و به‌جای `0f` خاموش، `null` برگردان تا لایه‌ی بالاتر بتواند «داده‌ی نامعتبر» را از «صفر واقعی» تشخیص دهد. همچنین `Strictness.LENIENT` (خط ۹۳) را به `STRICT` تغییر بده تا پاسخ‌های بدشکل سرور پنهان نشوند.

**Priority:**
MEDIUM

**Estimated Effort:**
Medium (نیازمند بازبینی مدل‌های مصرف‌کننده)

---

#### [MEDIUM] `AnimationManager` حالت سراسری غیر-snapshot و غیر‌ایمن نسبت به thread است

> ✅ **رفع شد (فاز ۲، مورد ۱۴):** `AnimationManager` به `mutableStateOf` تبدیل شد و `areAnimationsEnabled` اکنون یک property است (۲۰ نقطه‌ی فراخوانی در ۱۲ فایل به‌روزرسانی شدند). خواندن تنظیم سیستم از `LaunchedEffect` در `MainActivity` به `AtkCargoApplication.onCreate` منتقل شد. طبق تصمیم کاربر، `setPerformanceScore`/`performanceScore` (هرگز صدا زده نمی‌شد) به‌جای وصل‌کردن حذف شدند؛ نسخه‌ی تکراری و مرده‌ی `AnimationManager` در `feature/startup` هم حذف شد. در همین راستا `hardwareScore`/`saveHardwareScore` (کد مرده‌ی مرتبط، `UserPreferencesManager`/`UserSettingsStore`/`ProfileMenu`) هم حذف شدند.


**File:**
`core/domain/src/main/java/com/atk/atk_cargo/core/domain/AnimationManager.kt`

**Location:**
کل فایل

**Problem:**
```kotlin
object AnimationManager {
    private var performanceScore: Int = 50
    private var performanceAllowsAnimations: Boolean = true
    private var systemAllowsAnimations: Boolean = true
    fun areAnimationsEnabled(): Boolean = performanceAllowsAnimations && systemAllowsAnimations
}
```
سه مشکل هم‌زمان:

۱. **`var` ساده است، نه `mutableStateOf`.** `areAnimationsEnabled()` در ۹ نقطه‌ی داخل Composable خوانده می‌شود (`SecurityScreen.kt:144,707,784,904,986`، `UserManagementScreen.kt:941`، `CargoCounterListContent.kt:615-616`، `SelectInfoHeaderSection.kt:59-60`، `RegisterCargoScreen.kt:310-311`). Compose هیچ خواندنی را رصد نمی‌کند، پس **وقتی مقدار تغییر کند recomposition اتفاق نمی‌افتد**.

۲. **زمان‌بندی مقداردهی.** `MainActivity.kt:94-100` مقدار واقعی سیستم را داخل یک `LaunchedEffect(Unit)` تنظیم می‌کند — یعنی *بعد* از اولین composition. هر Composableی که پیش از آن render شده، مقدار پیش‌فرض `true` را دیده و هرگز به‌روز نمی‌شود.

۳. **`setPerformanceScore()` هرگز صدا زده نمی‌شود** (تأییدشده با grep روی کل ریپازیتوری). یعنی `performanceAllowsAnimations` برای همیشه `true` است و کل منطق گیت‌کردن انیمیشن بر اساس قدرت دستگاه، کد مرده است.

**Impact:**
تنظیم «کاهش حرکت» (Reduce Motion) سیستم در عمل رعایت نمی‌شود مگر تصادفاً — یک مسئله‌ی **دسترس‌پذیری** واقعی برای کاربران حساس به حرکت. گیت‌کردن بر اساس عملکرد دستگاه اصلاً کار نمی‌کند.

**Recommended Fix:**
به snapshot state تبدیل کن تا Compose تغییرات را ببیند:
```kotlin
object AnimationManager {
    var systemAllowsAnimations by mutableStateOf(true)
        private set
    var performanceAllowsAnimations by mutableStateOf(true)
        private set

    fun setSystemAnimationsEnabled(enabled: Boolean) { systemAllowsAnimations = enabled }
    fun setPerformanceScore(score: Int) { performanceAllowsAnimations = score >= 70 }

    val areAnimationsEnabled: Boolean
        get() = performanceAllowsAnimations && systemAllowsAnimations
}
```
مقدار سیستم را در `Application.onCreate` بخوان، نه در `LaunchedEffect`. و یا `setPerformanceScore` را واقعاً صدا بزن یا آن را به‌همراه `saveHardwareScore()`/`hardwareScore` حذف کن.

**Priority:**
MEDIUM

**Estimated Effort:**
Low

---

## Jetpack Compose Audit

### نقاط قوت

- **۱۰۲ مورد `collectAsStateWithLifecycle`، صفر مورد `collectAsState()` خام.** این یعنی هیچ Composableای در پس‌زمینه هم به جمع‌آوری ادامه نمی‌دهد — بهتر از اکثر کدبیس‌های production.
- **کلید روی همه‌ی LazyListها.** بررسی شد و هر `items(...)` کلید صریح دارد، با کامنت‌هایی که دلیل انتخاب کلید ترکیبی را توضیح می‌دهند (`ShipsListScreen.kt:275`: «کلید باید ترکیبی از نام و نوع محموله باشد وگرنه LazyColumn با کلید تکراری کرش می‌کند»).
- **۳۴ حاشیه‌نویسی `@Stable`/`@Immutable`** روی مدل‌های state.
- `contentType` روی برخی LazyListها برای بازاستفاده‌ی بهتر آیتم‌ها.

---

#### [HIGH] محاسبات سنگین مجموعه‌ها بدون `remember` داخل composition

> ✅ **رفع شد (فاز ۲، مورد ۱۰):** خطوط لوله‌ی filter/sortedWith در `ActiveQuotasContent.kt` (هر دو Composable)، `CargoCounterScreen.kt` و `ActiveQuotasGroupedComponents.kt` (`ShipCard`, `WarehouseSection`) در `remember` قرار گرفتند. در `WarehouseSection` یک نکته‌ی ظریف هم رفع شد: قرار دادن `remember` قبل و بعد یک `return` شرطی می‌توانست slot table کامپوز را به‌هم بریزد — هر دو `remember` به یکی ادغام شدند تا قبل از `return` اجرا شوند.

**File:**
`feature/cargo-workflow/src/main/java/com/atk/atk_cargo/feature/cargo_entry/presentation/ActiveQuotasContent.kt`

**Location:**
خط ۵۷، ۱۰۰–۱۱۰، ۱۷۵، ۲۱۵–۲۲۳

**Problem:**
```kotlin
// خط ۵۷ — در بدنه‌ی Composable، بدون remember
val filteredShips = groupedShips.entries.filter { (shipName, ships) ->
    val hasActivity = ships.any { it.entryVouchers + it.exitVouchers > 0 }
    ...
}

// خط ۱۰۰ — داخل خودِ items()، مرتب‌سازی با sumOf در comparator
items(
    items = filteredShips.sortedWith(
        compareByDescending<Map.Entry<String, List<ActiveShipInfo>>> { (_, ships) ->
            val total     = ships.sumOf { it.entryVouchers + it.exitVouchers }
            val completed = ships.sumOf { it.exitVouchers }
            total - completed
        }.thenByDescending { (_, ships) -> ships.sumOf { it.entryVouchers + it.exitVouchers } }
    ).toList(),
    key = { it.key }
)
```
comparator در هر مقایسه دو تا سه بار روی لیست داخلی `sumOf` می‌زند. با n کشتی و m سهمیه در هر کشتی، هزینه‌ی هر recomposition تقریباً `O(n log n × m)` است — و این در هر تایپ در نوار جستجو تکرار می‌شود.

الگوی مشابه در:
- `CargoCounterScreen.kt:270-271` — `filteredShips` و `groupedShips` بدون `remember`
- `ActiveQuotasGroupedComponents.kt:78` — `ships.groupBy { it.loadingWarehouse }` بدون `remember`
- `ActiveQuotasGroupedComponents.kt:142-150` — `filter` + `sortedWith` با `sumOf`، داخل یک کارت قابل باز/بسته شدن که خودش داخل LazyColumn است

**Why it matters:**
تیم این الگو را می‌شناسد — `CargoDetailsComponents.kt:738` دقیقاً درست انجامش داده، با کامنت «مرتب‌سازی در remember نگه داشته می‌شود تا در هر recomposition دوباره اجرا نشود». مسئله ناآگاهی نیست، ناسازگاری در اعمال است.

**Impact:**
jank و فریم‌های ازدست‌رفته در صفحه‌ی «سهمیه‌های فعال» و «شمارنده‌ی بار» — دقیقاً پرکاربردترین صفحات اپ، هنگام تایپ در جستجو. اثر روی دستگاه‌های ضعیف انبار (که این اپ برایشان `minSdk 28` دارد) به‌مراتب بدتر است.

**Recommended Fix:**
هر خط لوله را با کلیدهای ورودی درست در `remember` بپیچ:
```kotlin
val visibleShips = remember(groupedShips, searchQuery, filterState) {
    groupedShips.entries
        .filter { /* ... */ }
        .sortedWith(/* ... */)
        .toList()
}
items(items = visibleShips, key = { it.key }) { ... }
```
اگر ورودی خودش از یک `State` می‌آید و محاسبه پرهزینه است، `derivedStateOf` را در نظر بگیر. در حال حاضر فقط ۵ استفاده از `derivedStateOf` در کل پروژه وجود دارد که برای این حجم UI کم است.

**Priority:**
HIGH

**Estimated Effort:**
Low (تغییر مکانیکی، هر مورد چند دقیقه)

---

#### [MEDIUM] ساخت Flow جدید در هر recomposition

**File:**
`app/src/main/java/com/atk/atk_cargo/MainActivity.kt`

**Location:**
خط ۷۸، ۱۱۴–۱۱۶

**Problem:**
```kotlin
@SuppressLint("FlowOperatorInvokedInComposition")
...
val themeColorLong by startupViewModel.themeColor
    .onEach { isThemeColorLoaded = true }        // ← Flow جدید در هر recomposition
    .collectAsStateWithLifecycle(initialValue = UserPreferencesManager.DEFAULT_THEME_COLOR)
```
`.onEach {}` در هر بار اجرای composition یک نمونه‌ی Flow جدید می‌سازد. `collectAsStateWithLifecycle` هر بار که مرجع Flow تغییر کند، جمع‌آوری را از نو شروع می‌کند. لینت این را می‌گیرد و کد آن را `@SuppressLint` کرده است.

مسئله‌ی دوم: `isThemeColorLoaded` یک `var` ساده در Activity است که به‌عنوان **side effect داخل composition** نوشته می‌شود و `splashScreen.setKeepOnScreenCondition` آن را می‌خواند.

**Impact:**
راه‌اندازی مجدد بی‌مورد جمع‌آوری DataStore در هر recomposition ریشه (شامل رمزگشایی Keystore)؛ همچنین منطق نگه‌داشتن اسپلش به ترتیب اجرای composition وابسته می‌شود.

**Recommended Fix:**
side effect را از composition خارج کن:
```kotlin
val themeColorLong by startupViewModel.themeColor
    .collectAsStateWithLifecycle(initialValue = UserPreferencesManager.DEFAULT_THEME_COLOR)

LaunchedEffect(themeColorLong) { isThemeColorLoaded = true }
```
یا بهتر: یک `StateFlow<Boolean>` به نام `isThemeReady` در `StartupViewModel` بگذار و `setKeepOnScreenCondition` را مستقیم به آن وصل کن — بدون ورود به composition.

**Priority:**
MEDIUM

**Estimated Effort:**
Low

---

#### [MEDIUM] Composableهای بسیار بزرگ و فایل‌های چندمسئولیتی

**File:**
چند فایل

**Location:**
- `feature/reports/.../dialogs/CargoEditSearchDialogsSection.kt` — ۱٬۲۳۵ خط
- `app/src/main/java/com/atk/atk_cargo/security/SecurityScreen.kt` — ۱٬۱۵۰ خط
- `app/src/main/java/com/atk/atk_cargo/ui/screens/ManageReportsScreen.kt` — ۱٬۰۳۴ خط
- `feature/home/.../HomeScreen.kt` — ۹۸۴ خط
- `feature/reports/.../quota_details/QuotaCardComponents.kt` — ۹۴۹ خط

**Problem:**
۵۲۵ تابع `@Composable` در پروژه وجود دارد، اما در فایل‌هایی با میانگین بسیار بالا توزیع شده‌اند (`:feature:reports` = ۱۶٬۰۳۰ خط در ۳۱ فایل ≈ ۵۱۷ خط به‌ازای هر فایل). فایل‌هایی با نام `...DialogsSection.kt` چندین دیالوگ نامرتبط را در یک واحد جمع کرده‌اند.

**Why it matters:**
هرچه Composable بزرگ‌تر باشد، دامنه‌ی recomposition درشت‌تر می‌شود: تغییر یک state باعث اجرای مجدد بدنه‌ی بزرگ‌تری می‌شود. همچنین بازبینی کد، تست و بازاستفاده را عملاً غیرممکن می‌کند.

**Impact:**
recomposition درشت‌دانه، سرعت پایین بازبینی، و کاهش امکان استخراج کامپوننت‌های مشترک.

**Recommended Fix:**
معیار عملی: هر فایل Composable زیر ۳۰۰ خط و هر تابع Composable زیر ۱۰۰ خط. با فایل‌های `*DialogsSection.kt` شروع کن — هر دیالوگ یک فایل. سپس `ManageReportsScreen.kt` را به `:feature:reports` منتقل و تجزیه کن.

**Priority:**
MEDIUM

**Estimated Effort:**
High (بازسازی تدریجی، نه یکباره)

---

#### [MEDIUM] `LocalLayoutDirection` به‌صورت سراسری روی RTL قفل شده

**File:**
`app/src/main/java/com/atk/atk_cargo/core/navigation/MainScreen.kt`

**Location:**
خط ۱۱۸

**Problem:**
```kotlin
CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
```
جهت چیدمان به‌جای مشتق شدن از locale دستگاه، به‌صورت ثابت RTL شده. منابع `values-rtl` هم در پروژه وجود دارند که با این قفل بی‌معنا می‌شوند.

**Impact:**
اپ برای کاربران با locale انگلیسی هم RTL می‌ماند؛ اضافه‌کردن هر زبان LTR در آینده نیازمند لمس هر صفحه است.

**Recommended Fix:**
اگر اپ عمداً فقط فارسی است، `android:supportsRtl="true"` + `resources.configuration` را منبع حقیقت قرار بده و این override را حذف کن، یا `android:localeConfig` را با تنها locale فارسی اعلام کن. اگر override لازم است، دلیلش را در همان خط مستند کن.

**Priority:**
MEDIUM

**Estimated Effort:**
Low

---

## Animation Audit

### وضعیت کلی

انیمیشن‌ها **سبک و GPU-friendly** هستند: عمدتاً `fadeIn`/`fadeOut`، `expandVertically`/`shrinkVertically`، `slideIn`/`slideOut` و `AnimatedVisibility` — همگی روی لایه‌ی گرافیکی اجرا می‌شوند و کار سنگینی روی Main Thread ندارند. `standardTransitions(initialScale, targetScale)` یک انتزاع مشترک برای گذارهای ناوبری است که سازگاری بصری را تضمین می‌کند. هیچ انیمیشن سفارشی مبتنی بر `Canvas` یا `infiniteTransition` پرهزینه‌ای پیدا نشد.

**مسئله‌ی اصلی انیمیشن‌ها عملکردی نیست، بلکه دسترس‌پذیری است** — و به‌طور کامل در یافته‌ی `[MEDIUM] AnimationManager` در بخش Kotlin پوشش داده شد:

- گیت `Reduce Motion` به‌درستی در ۹ نقطه فراخوانی می‌شود، اما چون `AnimationManager` snapshot state نیست، Composableها تغییر آن را نمی‌بینند.
- مقدار واقعی سیستم *بعد* از اولین composition خوانده می‌شود.
- گیت مبتنی بر قدرت دستگاه (`setPerformanceScore`) کد مرده است.

#### [LOW] اسپلش با تأخیر ثابت به‌جای انیمیشن مبتنی بر پیشرفت

**File:**
`app/src/main/java/com/atk/atk_cargo/core/startup/StartupViewModel.kt`

**Location:**
خط ۴۰۹–۴۱۰

جزئیات در بخش Performance Audit (یافته‌ی `[HIGH] اسپلش اجباری ۳٫۸ ثانیه‌ای`). از دید انیمیشن، `SPLASH_MIN_DURATION` یک کف زمانی سخت است نه یک انیمیشن مبتنی بر پیشرفت واقعی؛ کاربر منتظر یک تایمر می‌ماند نه کار واقعی.

**Priority:**
LOW (به‌عنوان مسئله‌ی انیمیشن؛ به‌عنوان مسئله‌ی performance، HIGH)
---

## Performance Audit

#### [HIGH] اسپلش اجباری ۳٫۸ ثانیه‌ای در هر cold start

> ⚠️ **تا حدی رفع شد:** `SPLASH_MIN_DURATION` به ۱۵۰۰ میلی‌ثانیه کاهش یافت (نه ۶۰۰ میلی‌ثانیه‌ی پیشنهادی) — طبق تصمیم صریح کاربر، چون این مقدار در سه کامیت اخیر عمداً برای برندینگ/نمایش لوگو تنظیم شده بود.

**File:**
`app/src/main/java/com/atk/atk_cargo/core/startup/StartupViewModel.kt`

**Location:**
خط ۱۴۴–۱۵۴، ۴۰۹–۴۱۰

**Problem:**
```kotlin
private const val SPLASH_MIN_DURATION = 3800L
private const val SPLASH_MAX_DURATION = 5000L
...
val splashTimer = launch {
    delay(SPLASH_MIN_DURATION.milliseconds)        // ← همیشه، بدون قید و شرط
    withTimeoutOrNull((SPLASH_MAX_DURATION - SPLASH_MIN_DURATION).milliseconds) {
        networkJob.join()
    }
    _isSplashVisible.value = false
}
```
`delay(3800)` بدون شرط اجرا می‌شود. حتی اگر بررسی نسخه، بررسی امنیتی و اعتبارسنجی نشست در ۲۰۰ میلی‌ثانیه تمام شوند، کاربر ۳٫۸ ثانیه به اسپلش خیره می‌ماند. تنها راه فرار، دکمه‌ی `onSkip` است.

**Why it matters:**
این پرمصرف‌ترین بخش زمان راه‌اندازی است و کاملاً مصنوعی است. برای اپراتور انباری که روزی ده‌ها بار اپ را باز می‌کند، این یعنی دقایق تجمعی انتظار. همچنین معیارهای startup را به‌شکل مصنوعی خراب می‌کند و اثر `baselineprofile` (که پروژه زحمت راه‌اندازی‌اش را کشیده) را عملاً نامرئی می‌کند.

**Impact:**
تجربه‌ی کاربری ضعیف؛ خنثی شدن سرمایه‌گذاری روی Baseline Profile.

**Recommended Fix:**
کف را به یک بازه‌ی ضدپرش کوتاه کاهش بده و بگذار کار واقعی زمان‌بندی را تعیین کند:
```kotlin
private const val SPLASH_MIN_DURATION = 600L   // فقط برای جلوگیری از پرش بصری
private const val SPLASH_MAX_DURATION = 5000L
```
منطق فعلی از قبل درست است — کافی است `networkJob.join()` زودتر برگردد. اگر برندینگ اسپلش طولانی لازم است، آن را از مسیر بحرانی خارج کن (نمایش اسپلش هم‌زمان با پیش‌بارگذاری صفحه‌ی اصلی).

**Priority:**
HIGH

**Estimated Effort:**
Low (تغییر یک ثابت + تست)

---

#### [HIGH] معماری مبتنی بر polling در سراسر کلاینت

**File:**
چند فایل

**Location:**
- `CargoCounterScreen.kt:367` — `delay(30_000)`
- `CargoDetailsScreen.kt:193` — `delay(30_000)`
- `SelectInfoScreen.kt:362` — `delay(30_000)`
- `PermissionPoller.kt:22` — `POLL_INTERVAL_MS = 3 * 60 * 1000`
- `LoadingNotificationService.kt:39` — `UPDATE_INTERVAL_MINUTES = 5`
- `ChatNotificationWorker` — `PeriodicWorkRequest(15, MINUTES)`

**Problem:**
پنج مکانیزم polling مستقل به‌صورت هم‌زمان اجرا می‌شوند. برای یک کاربر admin با صفحه‌ی شمارنده‌ی بار باز:

| منبع | دوره | درخواست/ساعت |
|------|------|--------------|
| صفحه‌ی فعال | ۳۰ ثانیه | ۱۲۰ |
| PermissionPoller | ۳ دقیقه | ۲۰ |
| LoadingNotificationService | ۵ دقیقه | ۱۲ |
| ChatNotificationWorker | ۱۵ دقیقه | ۴ |
| **مجموع** | | **≈۱۵۶** |

با ۲۰ کاربر همزمان، این تقریباً **۳٬۱۰۰ درخواست در ساعت** به یک سرور PHP اشتراکی است — و هر درخواست حداقل دو کوئری دیتابیس دارد (اعتبارسنجی نشست + کوئری اصلی).

**Why it matters:**
`MicroCache` سمت سرور (TTL ۴–۸ ثانیه) بار دیتابیس را کم می‌کند اما بار PHP و شبکه را نه. هیچ backoff‌ای وقتی اپ در پس‌زمینه است یا شبکه ضعیف است وجود ندارد. حلقه‌های ۳۰ ثانیه‌ای داخل `LaunchedEffect` هستند، پس با `STOPPED` شدن lifecycle متوقف می‌شوند — این خوب است — اما `PermissionPoller` و FGS این‌طور نیستند.

**Impact:**
مصرف باتری، مصرف داده‌ی موبایل، و بار سرور که به‌صورت خطی با تعداد کاربر رشد می‌کند — سقف مقیاس‌پذیری این معماری پایین است.

**Recommended Fix:**
۱. **کوتاه‌مدت:** `PermissionPoller` را از ۳ دقیقه به ۱۵ دقیقه ببر و آن را با lifecycle گره بزن (فقط وقتی اپ در foreground است). برای هر سه حلقه‌ی ۳۰ ثانیه‌ای backoff تصاعدی هنگام خطا اضافه کن.
۲. **میان‌مدت:** ETag/`If-None-Match` را روی endpointهای پرتکرار فعال کن — `AppApiController::sendCacheableJsonResponse` (خط ۳۴۴) زیرساخت آن را دارد اما فقط روی بخشی از مسیرها به کار رفته. پاسخ `304` هزینه‌ی سریال‌سازی و پهنای باند را حذف می‌کند.
۳. **بلندمدت:** برای داده‌ی بارگیری لحظه‌ای، polling را با push (SSE یا FCM) جایگزین کن. زیرساخت FCM از قبل نیمه‌ساخته است (ستون `fcm_token` و endpoint `users/fcm-token`) اما هیچ SDKای نصب نیست — رجوع به بخش Technical Debt.

**Priority:**
HIGH

**Estimated Effort:**
Medium (کوتاه‌مدت) تا High (push)

---

#### [HIGH] هیچ صفحه‌بندی در هیچ endpoint گزارش‌گیری وجود ندارد

> ⚠️ **دامنه کاهش یافت و به‌صورت سقف محافظتی رفع شد (فاز ۲، مورد ۱۱):** بررسی دقیق‌تر نشان داد مکان‌های ذکرشده‌ی این یافته یا کد مرده بودند (`analytics/kotazh` هیچ‌جا از کلاینت اندروید صدا زده نمی‌شود) یا از قبل توسط `GROUP BY` به تعداد کوتاژ/کشتی محدود بودند (نه به تعداد حواله). `LIMIT 2000` روی سه محل واقعاً بدون سقف اضافه شد: `CargoController::getInitialInfo` (که در عمل هم به ورودهای معلق + خروج ۲۴ ساعت اخیر محدود است، نه کل تاریخچه)، `AnalyticsController::handleKotazhRequest`، و `CargoRepository::searchByTracking`. صفحه‌بندی cursor-based واقعی و تغییر قرارداد API انجام نشد — طبق تصمیم کاربر، چون ریسک/فایده‌اش برای این endpointهای مشخص توجیه نداشت.


**File:**
`PHP/src/Controllers/AnalyticsController.php` · `PHP/src/Services/QuotaService.php` · `PHP/src/Services/ShipService.php`

**Location:**
`AnalyticsController.php:167`, `:277`, `:331`, `:405`, `:509` — و همه‌ی کوئری‌های `QuotaService`

**Problem:**
جستجوی `LIMIT` در `AnalyticsController.php` هیچ نتیجه‌ای ندارد. نمونه:
```php
// AnalyticsController.php:167
$stmt2 = $this->conn->prepare(
    "SELECT trackingNumber, entryTime, netWeight, scaleReceiptNumber, shortageWeight,
            excessWeight, exitTime, exitDate, status
     FROM CargoInfo WHERE loadingQuotaNumber = ?"
);
```
تمام حواله‌های یک کوتاژ بدون سقف بازگردانده می‌شوند. تنها سقف‌های موجود در کل بک‌اند `LIMIT 5000` هستند در `UserRepository::getAll()` و `SessionRepository::getOnlineUsers()` که کامنتشان صریحاً می‌گوید «سقف سخت‌گیرانه است نه صفحه‌بندی».

**Why it matters:**
`CargoInfo` جدول رشدی سیستم است — هر حواله‌ی بارگیری یک ردیف. بعد از یک فصل عملیاتی، یک کوتاژ پرتردد می‌تواند هزاران ردیف داشته باشد. کل نتیجه در حافظه‌ی PHP جمع می‌شود، به JSON سریال می‌شود و از شبکه عبور می‌کند و سپس Gson روی موبایل آن را پارس می‌کند.

`CargoController::saveOrUpdate` صراحتاً `ini_set('memory_limit', '64M')` می‌گذارد (خط ۴۷) — نشانه‌ی اینکه فشار حافظه از قبل موضوع بوده است.

**Impact:**
با رشد داده: خطای `memory_limit`، timeout، پاسخ‌های چندمگابایتی، و OOM سمت کلاینت. این مشکل امروز نمایان نیست ولی قطعی است.

**Recommended Fix:**
صفحه‌بندی مبتنی بر cursor روی `id` اضافه کن (سریع‌تر از `OFFSET` روی جدول‌های بزرگ):
```php
$limit  = min(max((int)$request->get('limit', 100), 1), 500);
$cursor = (int)$request->get('cursor', 0);
$sql = "SELECT ... FROM CargoInfo
        WHERE loadingQuotaNumber = ? AND id > ?
        ORDER BY id ASC LIMIT ?";
```
و در پاسخ `nextCursor` برگردان. سمت کلاینت، `ChatRepository.loadOlderMessages` از قبل الگوی `olderThanId` را پیاده کرده — همان را برای گزارش‌ها تکرار کن.

**Priority:**
HIGH

**Estimated Effort:**
Medium

---

#### [MEDIUM] سه استک HTTP موازی با سه connection pool مجزا

**File:**
چند فایل

**Location:**
- `core/network/.../RetrofitClient.kt` — OkHttp با `ConnectionPool(10, 5, MINUTES)`، کش دیسک ۱۰MB، interceptor هدر، `TokenAuthenticator`
- `core/network/.../TokenRefresher.kt:13-18` — نمونه‌ی **دوم** OkHttp، بدون pool مشترک، بدون کش
- `feature/update/.../UpdateManager.kt` — OkHttp مستقل برای دانلود
- `app/.../security/SecurityVerifier.kt:152,249,272` — `HttpURLConnection` خام

**Problem:**
چهار مسیر شبکه با پیکربندی‌های ناسازگار. `SecurityVerifier` از `HttpURLConnection` با `BUFFER_DURATION = 8000` استفاده می‌کند، `TokenRefresher` از OkHttp با timeout ۱۰/۱۵ ثانیه، و `RetrofitClient` از ۱۰/۳۰/۳۰. هیچ‌کدام interceptor، retry، یا connection pool مشترک ندارند.

**Why it matters:**
certificate pinning از طریق Network Security Config روی **همه‌ی** آن‌ها اعمال می‌شود (چون سطح پلتفرم است) — پس این یک شکاف امنیتی نیست. اما:
- هر استک TLS handshake جداگانه انجام می‌دهد؛ اتصال بازاستفاده نمی‌شود.
- هر تغییری در سیاست شبکه (timeout، retry، هدر تشخیصی، لاگ) باید در چهار جا تکرار شود.
- در راه‌اندازی، `SecurityVerifier` سه درخواست موازی `HttpURLConnection` می‌زند در حالی که `UpdateManager` و `checkSession` هم‌زمان از OkHttp استفاده می‌کنند — چند handshake هم‌زمان به یک هاست.

**Impact:**
زمان راه‌اندازی طولانی‌تر به‌خاطر handshakeهای تکراری؛ نگهداری پرهزینه‌تر؛ ریسک واگرایی پیکربندی.

**Recommended Fix:**
`OkHttpClient` را به یک singleton مشترک در `:core:network` تبدیل کن و همه‌ی مصرف‌کنندگان از `.newBuilder()` روی آن مشتق شوند تا connection pool و کش مشترک بماند:
```kotlin
object HttpStack {
    val shared: OkHttpClient by lazy { /* pool، کش، timeoutهای پایه */ }
    fun forDownload(): OkHttpClient = shared.newBuilder().readTimeout(5, MINUTES).build()
    fun forAuth(): OkHttpClient     = shared.newBuilder().authenticator(null).build()
}
```
`SecurityVerifier` را از `HttpURLConnection` به همین استک منتقل کن.

**Priority:**
MEDIUM

**Estimated Effort:**
Medium

---

#### [MEDIUM] کوئری‌های `DATE()` و زیرپرس‌وجوهای همبسته که از ایندکس استفاده نمی‌کنند

**File:**
`PHP/src/Repositories/SessionRepository.php` · `PHP/src/Controllers/ChatController.php`

**Location:**
`SessionRepository.php:305-311` · `SessionRepository.php:265-281` · `ChatController.php:104-121`

**Problem:**
۱. **تابع روی ستون ایندکس‌شده:**
```sql
SELECT COUNT(*) as today_logins, COUNT(DISTINCT username) as unique_users_today
FROM user_sessions WHERE DATE(login_time) = CURDATE()
```
`DATE(login_time)` ایندکس `idx_login_time` را غیرقابل استفاده می‌کند → full table scan.

۲. **زیرپرس‌وجوی مشتق روی کل جدول:**
```sql
FROM user_sessions us
INNER JOIN (SELECT username, MAX(id) as max_id FROM user_sessions GROUP BY username) latest
    ON us.id = latest.max_id
```
`getLatestSessionsForAllUsers` کل جدول `user_sessions` را group می‌کند. این جدول با هر ورود رشد می‌کند و هرگز pruning نمی‌شود.

۳. **زیرپرس‌وجوی همبسته به‌ازای هر ردیف:**
```sql
-- ChatController::getMessages، برای هر یک از ۱۰۰ پیام
(SELECT GROUP_CONCAT(u2.fullName SEPARATOR ', ')
 FROM admin_chat_reads r JOIN Users u2 ON r.username = u2.username
 WHERE r.message_id = c.id AND r.username != c.username) as read_by_names,
EXISTS(SELECT 1 FROM admin_chat_reads r2 WHERE r2.message_id = c.id AND r2.username = ?) as is_read_by_me
```
دو زیرپرس‌وجوی همبسته × ۱۰۰ ردیف = تا ۲۰۰ اجرای زیرپرس‌وجو در هر بار بارگذاری چت. کامنت کد نشان می‌دهد این شناخته شده و limit به همین دلیل clamp شده است (`Phase1.10`)، اما خودِ الگو باقی مانده.

**Impact:**
با رشد `user_sessions` و `admin_chat_reads`، این کوئری‌ها به‌صورت خطی کند می‌شوند. امروز قابل تحمل، در مقیاس نه.

**Recommended Fix:**
۱. بازه‌ی sargable: `WHERE login_time >= CURDATE() AND login_time < CURDATE() + INTERVAL 1 DAY`.
۲. یک job نگهداری (`scripts/` از قبل `rotate_logs.php` و `health_monitor.php` دارد) که نشست‌های غیرفعال قدیمی‌تر از ۹۰ روز را آرشیو کند.
۳. `read_by_names` را به یک `LEFT JOIN ... GROUP BY` تبدیل کن یا آن را در یک درخواست دوم مجزا بگیر.

**Priority:**
MEDIUM

**Estimated Effort:**
Medium

---

## Memory Audit

### وضعیت کلی: خوب

بررسی هدفمند نشتی‌های رایج، نتایج تمیزی داد:

| بردار نشت | وضعیت |
|-----------|--------|
| نگه‌داشتن `Context` در singleton | ✅ `RetrofitClient.appContext = context.applicationContext` |
| `CoroutineScope` بدون لغو | ✅ `PermissionPoller.destroy()` از طریق `DisposableEffect`؛ `LoadingNotificationService.onDestroy()` scope را لغو می‌کند |
| `GlobalScope` | ✅ صفر مورد |
| جمع‌آوری Flow در پس‌زمینه | ✅ ۱۰۲ مورد `collectAsStateWithLifecycle`، صفر مورد خام |
| listener بدون unregister | ✅ موردی یافت نشد |
| نشت bitmap/تصویر | ✅ هیچ کتابخانه‌ی بارگذاری تصویر و هیچ bitmap دستی وجود ندارد |
| `ViewModel` که `View` نگه دارد | ✅ موردی یافت نشد |

#### [MEDIUM] نتایج بدون سقف در سمت سرور، نه در کلاینت

مسئله‌ی اصلی حافظه در این سیستم سمت **بک‌اند** است نه اپ — به یافته‌ی `[HIGH] هیچ صفحه‌بندی...` مراجعه کن. `ini_set('memory_limit', '64M')` در `CargoController.php:47` یک وصله‌ی موضعی روی همین مسئله است.

#### [LOW] `applicationScope` بدون لغو در `AtkCargoApplication`

**File:**
`app/src/main/java/com/atk/atk_cargo/api/AtkCargoApplication.kt`

**Location:**
خط ۱۷

```kotlin
private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
```
این scope هرگز لغو نمی‌شود. برای یک `Application` قابل قبول است (طول عمرش برابر پروسه است)، اما `CrashReporter.sendPendingReportIfAny(..., applicationScope)` یک درخواست شبکه در آن اجرا می‌کند که در صورت کند بودن شبکه می‌تواند تا کشته‌شدن پروسه ادامه یابد. اثر عملی ناچیز است.

**Priority:**
LOW
---

## PHP Backend Audit

### نقاط قوت

- **صفر مورد SQL Injection.** هر کوئری بررسی شد: تمام مقادیر کاربر از طریق `prepare` + `bind_param`/`execute([...])` عبور می‌کنند. تنها موارد interpolation در SQL، ثابت‌های کلاس (`CargoStatus::EXITED->value`، `self::WORKDAY_BOUNDARY_TIME`، `SESSION_TIMEOUT_SECONDS`) هستند که کنترل‌شده‌اند.
- `UserRepository::update()` یک allow-list صریح ستون دارد (`UPDATABLE_COLUMNS`) که تزریق نام ستون را می‌بندد.
- `declare(strict_types=1)` در همه‌ی فایل‌های `src/`.
- `PDO::ATTR_EMULATE_PREPARES => false` — prepared statement واقعی سمت سرور.
- بدنه‌های `catch` استثناهای داخلی را لاگ می‌کنند و پیام عمومی برمی‌گردانند — ساختار دیتابیس افشا نمی‌شود.
- CI با `php -l` روی همه‌ی فایل‌ها + PHPStan level 5 + PHPUnit.

---

#### [MEDIUM] دو مسیر دسترسی به دیتابیس (PDO و mysqli) به‌صورت موازی

> ⚠️ **بخشی رفع شد (فاز ۲، مورد ۱۸):** فقط بخش «۱» (وصله‌ی `sql_mode`) اعمال شد — `Database::getMysqliConnection` اکنون `STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION` می‌گذارد. **این تغییر فقط با اتصال به دیتابیس واقعی قابل تأیید کامل است** (این محیط به MySQL/MariaDB زنده دسترسی نداشت)؛ اگر جایی در کدبیس به کوتاه‌شدن بی‌صدای مقدار متکی بوده (که بررسی دستی نشانه‌ای از آن پیدا نکرد)، ممکن است حالا با خطای SQL صریح مواجه شود — قابل ردیابی و اصلاح، نه خرابی خاموش. یکسان‌سازی کامل PDO/mysqli (بخش «۲»، مهاجرت معماری) انجام نشد.


**File:**
`PHP/src/Core/Database.php`

**Location:**
خط ۳۱–۷۷

**Problem:**
```php
public function getPdoConnection(): PDO { ... }      // Repositoryها
public function getMysqliConnection(): mysqli { ... } // Controllerها و DatabaseManager
```
هر درخواست ممکن است هر دو اتصال را باز کند. تفکیک بر اساس لایه نیست بلکه تاریخی است: `SessionRepository`, `UserRepository`, `LicenseRepository` از PDO استفاده می‌کنند؛ `CargoController`, `ChatController`, `UtilityController`, `AnalyticsController`, `PermissionRepository`, `AuditLogger` از mysqli.

**Why it matters:**
۱. **تراکنش‌ها فقط روی یک اتصال کار می‌کنند.** `DatabaseManager::beginTransaction()` روی mysqli است. اگر عملیاتی هم `CargoRepository` (mysqli) و هم `SessionRepository` (PDO) را لمس کند، در یک تراکنش نیستند — اتمی بودن یک توهم است. `UserService::updateUser()` دقیقاً این کار را می‌کند: از طریق PDO کاربر را به‌روز می‌کند و بعد `SessionRepository` (PDO) را صدا می‌زند — این یکی اتفاقاً هم‌اتصال است، ولی هیچ چیزی این را تضمین نمی‌کند.
۲. دو اتصال TCP به MySQL در هر درخواست.

**نکته‌ی مهم:** `getMysqliConnection()` در خط ۷۳ اجرا می‌کند:
```php
$this->mysqli->query("SET SESSION sql_mode = 'NO_ENGINE_SUBSTITUTION'");
```
این `STRICT_TRANS_TABLES` را **حذف می‌کند**. یعنی روی اتصال mysqli، درج داده‌ی خیلی بلند به‌جای خطا، بی‌صدا truncate می‌شود. `CargoService.php:37` این را می‌شناسد و برای مسیر خودش دوباره `SET SESSION sql_mode = 'STRICT_TRANS_TABLES'` می‌زند — اما فقط برای همان مسیر. سایر مسیرهای نوشتن mysqli (چت، مجوزها، audit log، FCM) در حالت غیر strict می‌مانند. وجود `PHP/STRICT_MODE_CHECKLIST.md` نشان می‌دهد این موضوع در حال پیگیری است.

**Impact:**
مرزهای تراکنشی غیرقابل اتکا؛ ریسک truncate بی‌صدای داده روی مسیرهای mysqli.

**Recommended Fix:**
۱. **فوری:** `sql_mode` را در `getMysqliConnection()` به `STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION` تغییر بده تا پیش‌فرض امن باشد و `CargoService` نیازی به override نداشته باشد.
۲. **میان‌مدت:** روی PDO یکسان‌سازی کن. Controllerها را یکی‌یکی به Repositoryهای مبتنی بر PDO منتقل کن و `getMysqliConnection()` را deprecate کن.

**Priority:**
MEDIUM

**Estimated Effort:**
High (مهاجرت تدریجی)

---

#### [MEDIUM] گیت احراز هویت دوبار پیاده‌سازی شده است

> ✅ **رفع شد (فاز۳، مورد ۲۵):** `AuthenticatesRequests.php` حذف شد؛ هر ۵ کنترلر مصرف‌کننده‌اش هویت را از `Router::dispatch` می‌گیرند. جزئیات در یادداشت‌های اجرای فاز ۳.

**File:**
`PHP/src/Core/AuthenticatesRequests.php` و `PHP/src/Core/ApiAuthGate.php`

**Location:**
`AuthenticatesRequests.php:17-36, 51-71` · `ApiAuthGate.php:14-44` · `MinVersionGate.php:10-30`

**Problem:**
دو پیاده‌سازی تقریباً یکسان از یک منطق:

| منطق | trait | کلاس static |
|------|-------|-------------|
| اعتبارسنجی نشست | `AuthenticatesRequests::requireAuthenticatedSession()` | `ApiAuthGate::requireAuthenticated()` |
| بررسی مجوز | `AuthenticatesRequests::requirePermission()` | `ApiAuthGate::requirePermission()` |
| گیت نسخه | `AuthenticatesRequests::enforceMinAppVersion()` | `MinVersionGate::enforce()` |

هر سه جفت، کد یکسان با تفاوت‌های جزئی در شکل پاسخ دارند. Router برای مسیرهای `auth => true` از `ApiAuthGate` استفاده می‌کند، و کنترلرهای «Shim» علاوه بر آن `requireAuthenticatedSession()` را هم داخل خودشان صدا می‌زنند.

**Why it matters:**
هر مسیر «Shim» **دوبار** نشست را اعتبارسنجی می‌کند — یعنی دو `SELECT` روی `user_sessions` به‌ازای هر درخواست. با ۳٬۱۰۰ درخواست در ساعت (رجوع به یافته‌ی polling)، این ۳٬۱۰۰ کوئری اضافی است.

خطر بزرگ‌تر نگهداری است: هر اصلاح امنیتی در منطق نشست باید در دو جا اعمال شود. یافته‌ی `[HIGH] گیت نسخه‌ی حداقلی` هر دو نسخه را تحت تأثیر قرار می‌دهد — دقیقاً نمونه‌ی همین ریسک.

**Impact:**
دو برابر شدن کوئری اعتبارسنجی نشست؛ ریسک واگرایی سیاست امنیتی بین دو مسیر.

**Recommended Fix:**
`AuthenticatesRequests` را حذف کن و کنترلرها را وادار کن هویت را از Router دریافت کنند. امضای handler در `Router::dispatch` (خط ۵۹) از قبل آن را پاس می‌دهد:
```php
($route['handler'])($params, $request, $username, $userType);
```
پس کنترلرها فقط باید `$username`/`$userType` را به‌عنوان پارامتر بپذیرند به‌جای اینکه دوباره اعتبارسنجی کنند.

**Priority:**
MEDIUM

**Estimated Effort:**
Medium

---

#### [MEDIUM] `AppApiController` عمدتاً یک Middle Man است

**File:**
`PHP/src/Controllers/AppApiController.php`

**Location:**
خط ۳۶۰–۴۳۴

**Problem:**
۲۲ متد که هیچ کاری جز فراخوانی سرویس نمی‌کنند:
```php
public function checkQuotaStatus(array $params): array { return $this->quotaService->checkQuotaStatus($params); }
public function getShipsList(): array                  { return $this->shipService->getShipsList(); }
public function getQuotaDetails(string $q): ?array     { return $this->quotaService->getQuotaDetails($q); }
// ... ۱۹ مورد دیگر
```
این «Middle Man» کلاسیک است (Fowler). `src/routes/api_v2.php` یک نمونه‌ی مشترک از این کنترلر می‌سازد و از طریق آن به سرویس‌ها می‌رسد — درحالی‌که می‌توانست مستقیم سرویس را صدا بزند.

**Impact:**
یک لایه‌ی اضافه بدون رفتار؛ هر متد جدید سرویس باید در سه جا (سرویس، کنترلر، route table) ثبت شود.

**Recommended Fix:**
در `api_v2.php` سرویس‌ها را مستقیم instantiate کن و متدهای pass-through را حذف کن. متدهایی که واقعاً منطق HTTP دارند (`handle()`, `sendCacheableJsonResponse()`) در کنترلر بمانند.

**Priority:**
MEDIUM

**Estimated Effort:**
Medium

---

#### [MEDIUM] رمزگذاری خروجی ناسازگار بین دو مسیر نوشتن

> ✅ **رفع شد (فاز ۲، مورد ۱۳):** `htmlspecialchars` از `validateStringField` و از `searchByScaleReceipt`/`searchByTracking` حذف شد؛ هر دو مسیر نوشتن حالا فقط `trim` می‌کنند، هم‌راستا با `sanitizeString`. برای داده‌ی موجودی که قبلاً از مسیر `updateCargoInfo` دوبار escape شده، اسکریپت `PHP/scripts/fix_double_escaped_cargo_fields.php` اضافه شد (پیش‌فرض dry-run، نیازمند `--apply` صریح برای اعمال واقعی) — چون دسترسی به دیتابیس تولید برای تست/اجرای مستقیم وجود نداشت، این اسکریپت برای بررسی و اجرای دستی شماست.


**File:**
`PHP/src/Controllers/CargoController.php`

**Location:**
خط ۶۴۳–۶۴۵ در برابر خط ۶۶۱–۶۷۰، و خط ۳۰۷–۳۲۹

**Problem:**
دو متد پاک‌سازی متفاوت روی همان ستون‌های دیتابیس:
```php
// saveOrUpdate() از این استفاده می‌کند — فقط trim
private function sanitizeString(string $input): string {
    return trim($input);
}

// updateCargoInfo() از این استفاده می‌کند — htmlspecialchars
private function validateStringField($value, string $fieldName, bool $required = true, ?int $maxLength = 100): string {
    $sanitized = htmlspecialchars(trim($strValue), ENT_QUOTES, 'UTF-8');
    ...
}
```
سپس هنگام خواندن، `searchByScaleReceipt` و `searchByTracking` **دوباره** `htmlspecialchars` می‌زنند (خط ۳۰۷–۳۲۹, ۳۸۵–۴۰۸).

**Why it matters:**
۱. یک `trackingNumber` که از طریق `saveOrUpdate` ثبت شده خام ذخیره می‌شود؛ همان مقدار اگر از طریق `updateCargoInfo` ویرایش شود، escape‌شده ذخیره می‌شود. **مقایسه‌ی رشته‌ای بین این دو شکست می‌خورد** — و کد در `CargoViewModel` دقیقاً روی `trackingNumber` مقایسه می‌کند (`cargoInfoList.none { it.trackingNumber == trackingNumber }`).
۲. مصرف‌کننده یک کلاینت اندروید است، نه مرورگر. `htmlspecialchars` روی JSON بی‌فایده است و باعث می‌شود کاربر `&amp;` به‌جای `&` ببیند — و در مسیر update، `&amp;amp;` (دوبار escape).

کامنت خط ۶۴۲ خودش این تنش را ثبت کرده: «فقط trim، نه htmlspecialchars، چون escape کردن باعث عدم تطابق با InitialInfo escape‌نشده می‌شد» — یعنی مشکل در یک مسیر حل شده ولی در مسیر دیگر باقی مانده.

**Impact:**
داده‌ی ناسازگار بین دو مسیر نوشتن؛ نمایش entityهای HTML در UI اندروید؛ شکست تطبیق شماره‌ی حواله.

**Recommended Fix:**
یک قاعده: **در لایه‌ی داده هرگز encode نکن؛ در مرز خروجی encode کن.** چون خروجی JSON است و مصرف‌کننده اندروید:
```php
// هر دو مسیر نوشتن
private function sanitizeString(string $input): string { return trim($input); }
// validateStringField هم فقط trim + بررسی طول کند، بدون htmlspecialchars
```
و `htmlspecialchars` را از `searchByScaleReceipt`/`searchByTracking` حذف کن. `json_encode` از قبل خروجی JSON را ایمن می‌کند. برای داده‌ی موجودِ آلوده، یک migration پاک‌سازی لازم است.

**Priority:**
MEDIUM

**Estimated Effort:**
Medium (شامل migration داده)

---

#### [LOW] PHPStan فایل‌های ریشه و پنل مدیریتی را پوشش نمی‌دهد

**File:**
`PHP/phpstan.neon`

**Location:**
بخش `paths`

**Problem:**
```yaml
paths:
    - src
    - api
    - Lic
```
`PermissionManager.php` (۶۵۳ خط، پنل مدیریت مجوزها)، `file_manager.php`، `SessionManager.php`, `User/`, `update_config.php` و `scripts/` هیچ‌کدام تحلیل نمی‌شوند. `php -l` در CI فقط خطای نحوی می‌گیرد، نه خطای نوع.

**Impact:**
حساس‌ترین پنل مدیریتی سیستم کمترین پوشش تحلیل استاتیک را دارد.

**Recommended Fix:**
`PermissionManager.php`, `file_manager.php`, `User/`, `scripts/` را به `paths` اضافه کن. اگر خطاهای موجود زیادند، از `baseline` استفاده کن تا رگرسیون جدید مسدود شود بدون اینکه CI فوراً قرمز شود:
```bash
vendor/bin/phpstan analyse --generate-baseline
```
سپس به‌تدریج level را از ۵ به ۷ ببر.

**Priority:**
LOW

**Estimated Effort:**
Low

---

## API Audit

### جدول endpointها

مسیرها از `PHP/src/routes/api_v2.php` استخراج شدند. ستون «کلاینت» نشان می‌دهد آیا `ApiServiceV2.kt` یا کد اندروید دیگری واقعاً آن را صدا می‌زند.

| Method | Path | Auth | Permission | کلاینت |
|--------|------|:----:|-----------|:------:|
| GET | `ships` | ✅ | `view_reports` | ✅ |
| GET | `ships/{shipName}` | ✅ | `view_reports` | ✅ |
| GET | `ships/{shipName}/warehouses/{warehouseName}` | ✅ | `view_reports` | ✅ |
| GET | `ships/{shipName}/quotas` | ✅ | — | ✅ |
| GET | `ships/active` | ✅ | — | ✅ |
| GET | `quotas/filtered` · `filtered-summary` · `grouped` | ✅ | `view_reports` | ✅ |
| GET | `quotas/existence` · `{n}/status` · `{n}/loadable-tonnage` | ✅ | — | ✅ |
| GET | `quotas/{quotaNumber}` | ✅ | `view_reports` | ✅ |
| POST | `quotas/edit` · `delete` · `{id}/percentage` · `{id}/toggle-status` · `{id}/percentage-restriction` · `{n}/temporary-tonnage` | ✅ | `manage_quotas` | ✅ |
| GET | `realtime/ships` | ✅ | — | ✅ |
| POST | `auth/login` | ❌ | — | ✅ |
| POST | `auth/refresh` | ❌ | — | ✅ (OkHttp خام) |
| POST | `auth/session` | ❌ | — | ✅ |
| POST | `auth/logout` | ❌ | — | ✅ |
| POST | `cargo` | ✅ | — | ✅ |
| PATCH | `cargo/update` | ✅ | `edit_cargo` | ✅ |
| POST | `cargo/confirm` | ✅ | `cargo_counter` | ✅ |
| DELETE | `cargo/delete` | ✅ | `delete_cargo` | ✅ |
| GET | `cargo/search/scale-receipt` · `search/tracking` · `initial-info` · `scale-receipt/check` | ✅ | — | ✅ |
| POST | `cargo/initial-info` | ✅ | `initial_info` | ✅ |
| POST | `utility/check-password` | ✅ | — | ❌ **مرده** |
| POST | `utility/check-existence` | ✅ | — | ✅ |
| POST | `utility/sync-permissions` | ✅ | — | ✅ |
| GET/POST/PATCH/DELETE | `users*` | ✅ | `manage_users` (اکثراً) | ✅ |
| POST | `users/fcm-token` | ✅ | — | ❌ **مرده** |
| GET | `chat/messages` | ✅ | — | ✅ |
| GET | `chat/unread-count` | ✅ | — | ❌ **مرده** |
| POST/PATCH/DELETE | `chat/messages*` | ✅ | — | ✅ |
| POST | `chat/messages/{id}/read` | ✅ | — | ❌ **مرده** |
| GET | `analytics/kotazh` · `realtime` · `comprehensive` | ✅ | `view_reports` | ✅ |
| POST | `analytics/export-log` | ✅ | `view_reports` | ✅ |
| GET | `analytics/quota-remaining` | ✅ | `active_quotas` | ❌ **مرده** |
| GET | `health` | ❌ | — | ❌ (مانیتورینگ) |
| POST | `diagnostics/crash` | ❌ | — | ✅ (HTTP خام) |
| POST | `utility/check-signature` | ❌ | — | ✅ (HttpURLConnection) |
| GET | `utility/check-update` | ❌ | — | ✅ (OkHttp خام) |
| POST | `license/validate` · GET `license/info` | ❌ | — | ✅ (HttpURLConnection) |

---

#### [MEDIUM] API از قراردادهای REST پیروی نمی‌کند

**File:**
`PHP/api/v2/index.php` · `core/network/.../ApiServiceV2.kt`

**Location:**
`index.php:11-14` · کل `ApiServiceV2.kt`

**Problem:**
همه‌ی درخواست‌ها به `api/v2/index.php?route=...` می‌روند:
```kotlin
@GET("api/v2/index.php")
suspend fun getShipDetails(@Query("route") route: String, ...): Response<Ship>
```
مسیر منابع در query string است، نه در URL path. کلاینت رشته‌های route را دستی می‌سازد (`"ships/$shipName"`, `"quotas/$id/percentage"`).

**Why it matters:**
دلیل مستند شده و معتبر است — «`mod_rewrite` روی هاست production فعال نیست». اما پیامدها واقعی‌اند:
- **ایمنی نوع صفر.** یک اشتباه تایپی در `"quotas/$id/toggle-satus"` در زمان کامپایل گرفته نمی‌شود و در زمان اجرا ۴۰۴ می‌دهد.
- **ریسک انکد.** نام کشتی مستقیماً در route درج می‌شود (`ships/$shipName`). اگر نام کشتی شامل `/` باشد، `Router::matchPath` مسیر را به قطعات بیشتری می‌شکند و تطبیق شکست می‌خورد.
- کش HTTP، لاگ سرور و ابزارهای مانیتورینگ همه‌ی درخواست‌ها را یک endpoint می‌بینند.

**Impact:**
شکنندگی در برابر اشتباه تایپی؛ ریسک شکست برای نام‌های حاوی کاراکترهای خاص؛ کاهش قابلیت مشاهده.

**Recommended Fix:**
۱. **فوری و کم‌هزینه:** ساخت route را در یک شیء متمرکز کن. پروژه از قبل `ApiV2Routes` را برای چت دارد (`ApiV2Routes.chatMessageEdit(messageId)`) — آن را به همه‌ی مسیرها گسترش بده و مقادیر را encode کن:
```kotlin
object ApiV2Routes {
    fun shipDetails(name: String) = "ships/${Uri.encode(name)}"
    fun shipQuotas(name: String)  = "ships/${Uri.encode(name)}/quotas"
    fun quotaToggle(id: Int)      = "quotas/$id/toggle-status"
}
```
۲. **میان‌مدت:** اگر روزی `mod_rewrite` در دسترس شد، `.htaccess` از قبل قانونش را دارد (خط ۴۴–۴۶) و فقط `@GET("api/v2/{route}")` لازم است.

**Priority:**
MEDIUM

**Estimated Effort:**
Low (متمرکزسازی route) تا Medium

---

#### [MEDIUM] معنای کدهای وضعیت HTTP ناسازگار است

**File:**
`PHP/src/Controllers/AuthController.php` · `PHP/src/Controllers/ChatController.php`

**Location:**
`AuthController.php:65-69, 82-87, 100-106` · `ChatController.php:147-159, 194-200`

**Problem:**
شکست احراز هویت `200 OK` برمی‌گرداند:
```php
Response::json([
    'success' => false,
    'message' => 'نام کاربری یا رمز عبور اشتباه است!',
    'userType' => null
], 200); // بازگرداندن 200 برای پایداری با کلاینت اندروید
```
اما قفل شدن حساب در `refresh()` کد `429` و ورود همزمان کد `409` می‌دهد. `ChatController::sendMessage` برای خطای مجوز `success => false` با کد `200` برمی‌گرداند، درحالی‌که `getMessages` برای همان شرایط `ApiException(..., 403)` پرتاب می‌کند.

**Why it matters:**
سه سبک سیگنال‌دهی خطا در یک API: کد HTTP، فیلد `success`، و فیلد `error`. کلاینت باید هر سه را بررسی کند. `ChatRepository.kt:116` این را نشان می‌دهد:
```kotlin
if (response.isSuccessful && response.body()?.success == true) { ... }
```
مهم‌تر: چون شکست auth کد `200` است، `TokenAuthenticator` (که فقط روی `401` فعال می‌شود) هرگز برای این مسیرها اجرا نمی‌شود. `SessionValidator.kt:24` این را صراحتاً ثبت کرده: «`checkSession` همیشه HTTP ۲۰۰ برمی‌گرداند پس هرگز ۴۰۱ نمی‌شود و `TokenAuthenticator` صدا زده نمی‌شود» — و برای دور زدنش یک فراخوانی refresh دستی اضافه شده.

**Impact:**
مدیریت خطا در کلاینت پیچیده و مستعد خطا می‌شود؛ رفتار خودکار refresh توکن نیازمند وصله‌های دستی است.

**Recommended Fix:**
یک قرارداد واحد در `Response` تعریف کن و همه‌ی کنترلرها را به آن ملزم کن:
- `401` نشست نامعتبر (با فیلد `code`) · `403` مجوز ناکافی · `409` تعارض · `422` خطای اعتبارسنجی · `429` محدودیت نرخ
- بدنه‌ی خطا همیشه `{ success: false, message, code? }`

چون این تغییر شکننده است، آن را به گیت `X-App-Version` گره بزن: نسخه‌های جدید کد صحیح می‌گیرند.

**Priority:**
MEDIUM

**Estimated Effort:**
Medium

---

#### [MEDIUM] هیچ محدودیت نرخی روی endpointهای غیر ورود وجود ندارد

**File:**
`PHP/src/routes/api_v2.php`

**Location:**
کل جدول route

**Problem:**
`LoginAttemptLimiter` فقط روی `auth/login`، `auth/refresh`، `diagnostics/crash` و `PasswordGateService` اعمال شده. بقیه‌ی ۴۰+ endpoint هیچ محدودیتی ندارند — از جمله `analytics/comprehensive` که سنگین‌ترین کوئری‌های سیستم را اجرا می‌کند.

**Impact:**
یک کلاینت احرازشده (یا یک اسکریپت با توکن دزدیده‌شده) می‌تواند با فراخوانی مکرر endpointهای تحلیلی، دیتابیس را اشباع کند.

**Recommended Fix:**
یک محدودکننده‌ی سراسری غیرمسدودکننده در `ApiAuthGate::requireAuthenticated` اضافه کن (مثلاً ۳۰۰ درخواست در دقیقه به‌ازای هر نشست) با سقف سخت‌گیرانه‌تر برای مسیرهای `analytics/*`.

**Priority:**
MEDIUM

**Estimated Effort:**
Medium
---

## Database Audit

### نقاط قوت

ایندکس‌گذاری در مجموع **متفکرانه** است — ایندکس‌های ترکیبی با ستون‌های covering که با الگوهای کوئری واقعی هم‌راستا هستند:
- `idx_cargo_status_group (loadingQuotaNumber, shipName, loadingWarehouse, shippingCompany, cargoType, status, netWeight)` — دقیقاً ستون‌هایی که `QuotaService` گروه‌بندی و جمع می‌زند.
- کلیدهای خارجی روی `admin_chat_messages`, `admin_chat_reads`, `user_permissions`, `user_sessions` با `ON DELETE CASCADE`/`RESTRICT` مناسب.
- `UNIQUE KEY uk_cargo_scale_receipt_number` — یکتایی شماره‌ی قبض باسکول در سطح دیتابیس اجرا می‌شود، نه فقط در کد.
- `PRIMARY KEY (role, feature)` و `PRIMARY KEY (username, feature)` — مدل‌سازی درست جدول‌های مجوز.

---

#### [HIGH] کلیدهای JOIN ناسازگار بین کوئری‌های تحلیلی و سرویس‌ها

> ✅ **رفع شد:** کلید طبیعی پنج‌ستونی (`loadingQuotaNumber`, `shipName`, `loadingWarehouse`, `shippingCompany`, `cargoType`) در هر سه محل (`AnalyticsController::getRealTimeData`, `handleComprehensiveAnalysisRequest`, `ShipService::getFilteredSummary`) اعمال شد. بقیه‌ی کوئری‌های join‌دار پروژه بررسی و تأیید شدند که از قبل کلید کامل را استفاده می‌کنند.

**File:**
`PHP/src/Controllers/AnalyticsController.php` · `PHP/src/Services/ShipService.php`

**Location:**
`AnalyticsController.php:261-265` · `AnalyticsController.php:323-328` · `ShipService.php:351-352`

**Problem:**
کلید طبیعی یک ردیف کوتاژ **پنج ستونی** است. `QuotaService` این را درست پیاده کرده:
```sql
-- QuotaService.php:45-48 — کامل و درست
LEFT JOIN CargoInfo c ON
    c.loadingQuotaNumber = i.loadingQuotaNumber AND c.shipName = i.shipName
    AND c.cargoType = i.cargoType AND c.shippingCompany = i.shippingCompany
    AND c.loadingWarehouse = i.loadingWarehouse
```
اما سه جای دیگر با کلید ناقص JOIN می‌کنند:
```sql
-- AnalyticsController.php:261 — بدون shipName و بدون cargoType
INNER JOIN CargoInfo c ON i.loadingQuotaNumber = c.loadingQuotaNumber
    AND i.loadingWarehouse = c.loadingWarehouse
    AND i.shippingCompany  = c.shippingCompany
GROUP BY i.loadingQuotaNumber, i.shipName, i.loadingWarehouse, i.shippingCompany, i.cargoType
--                             ^^^^^^^^^^ در GROUP BY هست ولی در JOIN نیست

-- AnalyticsController.php:323 — بدون shipName
JOIN InitialInfo i ON c.loadingQuotaNumber = i.loadingQuotaNumber
    AND c.loadingWarehouse = i.loadingWarehouse
    AND c.shippingCompany  = i.shippingCompany
    AND c.cargoType        = i.cargoType

-- ShipService.php:351 — فقط دو ستون
JOIN InitialInfo i ON c.loadingQuotaNumber = i.loadingQuotaNumber AND c.shipName = i.shipName
```

**Why it matters:**
وقتی ستونی در `GROUP BY` هست ولی در `ON` نیست، هر ردیف `CargoInfo` با **همه‌ی** ردیف‌های `InitialInfo` که در ستون‌های باقی‌مانده مشترک‌اند JOIN می‌شود. نتیجه: `SUM(netWeight)` و `COUNT` در چند گروه تکرار می‌شوند.

این یک حدس نیست — کامنت خط ۳۱۹ همین کلاس تصریح می‌کند: «افزودن `cargoType` به شرط JOIN برای جلوگیری از دوبرابر شدن `SUM(netWeight)` در تطبیق نادرست (B-6)». یعنی این دقیقاً همان دسته باگی است که قبلاً یک‌بار پیدا و برای `cargoType` رفع شده — اما `shipName` هنوز جا افتاده و کوئری خط ۲۶۱ اصلاً اصلاح نشده است.

**Impact:**
اعداد تناژ و تعداد حواله در داشبورد بارگیری لحظه‌ای و تحلیل جامع می‌توانند **بیش‌شماری** شوند — در سیستمی که کارش ردیابی تناژ بارگیری است، این مستقیماً به تصمیم‌های عملیاتی غلط منجر می‌شود. شرط بروز: وجود چند ردیف `InitialInfo` با `loadingQuotaNumber`/`loadingWarehouse`/`shippingCompany` یکسان اما `shipName` یا `cargoType` متفاوت.

**Recommended Fix:**
کلید طبیعی پنج‌ستونی را در هر سه محل اعمال کن:
```sql
ON  c.loadingQuotaNumber = i.loadingQuotaNumber
AND c.shipName           = i.shipName
AND c.loadingWarehouse   = i.loadingWarehouse
AND c.shippingCompany    = i.shippingCompany
AND c.cargoType          = i.cargoType
```
پیش از اعمال، با یک کوئری تشخیصی بررسی کن که آیا داده‌ی فعلی این حالت را دارد یا نه:
```sql
SELECT loadingQuotaNumber, loadingWarehouse, shippingCompany,
       COUNT(DISTINCT shipName) AS ships, COUNT(DISTINCT cargoType) AS types
FROM InitialInfo GROUP BY 1,2,3 HAVING ships > 1 OR types > 1;
```
اگر این کوئری ردیفی برگرداند، بیش‌شماری **در حال حاضر اتفاق می‌افتد**.

**Priority:**
HIGH

**Estimated Effort:**
Low (اصلاح) · Medium (اعتبارسنجی داده‌ی تاریخی)

---

#### [MEDIUM] تاریخ و زمان به‌صورت `varchar(100)` ذخیره می‌شوند

**File:**
`PHP/schema.sql`

**Location:**
خط ۲۶، ۳۱–۳۲ (جدول `CargoInfo`)

**Problem:**
```sql
`entryTime` varchar(100) ... DEFAULT NULL,
`exitTime`  varchar(100) ... DEFAULT NULL,
`exitDate`  varchar(100) ... DEFAULT NULL,
```
تاریخ‌ها به‌صورت رشته‌ی شمسی (`jdate('Y/m/d')` → `"1405/05/24"`) ذخیره می‌شوند و مقایسه‌های بازه‌ای به‌صورت لغوی انجام می‌شوند:
```sql
-- CargoController.php، getInitialInfo
AND (status = 'ENTERED' OR (status = 'EXITED' AND exitDate >= ? AND exitDate <= ?))
```

**Why it matters:**
این **تصادفاً** کار می‌کند چون `Y/m/d` با صفر پیشین به‌صورت لغوی هم‌ترتیب با زمانی است. اما:
- هیچ اعتبارسنجی‌ای وجود ندارد — `"abcd"` بی‌صدا پذیرفته می‌شود (و روی اتصال mysqli که `STRICT_TRANS_TABLES` ندارد، حتی truncate هم می‌شود).
- محاسبه‌ی بازه در SQL ممکن نیست؛ منطق تاریخ باید در PHP انجام شود و به `jdf.php` وابسته است.
- `varchar(100)` برای یک تاریخ ۱۰ کاراکتری، ایندکس را بی‌جهت بزرگ می‌کند (`idx_cargo_exit_window` با `exitDate` به‌عنوان ستون اول).
- منطقه‌ی زمانی هیچ‌جا در داده کدگذاری نشده.

مقایسه کن با `user_sessions` که همه‌ی ستون‌های زمانی‌اش `datetime`/`timestamp` واقعی‌اند — یعنی الگوی درست در همین schema موجود است.

**Impact:**
بدهی مدل‌سازی داده که هر گزارش‌گیری آینده را محدود می‌کند؛ ریسک پذیرش داده‌ی نامعتبر.

**Recommended Fix:**
ستون‌های `datetime` میلادی اضافه کن و رشته‌های شمسی را فقط برای نمایش نگه دار:
```sql
ALTER TABLE CargoInfo
  ADD COLUMN entry_at DATETIME NULL AFTER entryTime,
  ADD COLUMN exit_at  DATETIME NULL AFTER exitTime,
  ADD INDEX idx_cargo_exit_at (exit_at, status);
```
مسیرهای نوشتن هر دو را پر کنند، کوئری‌ها به‌تدریج به ستون جدید مهاجرت کنند، و در پایان ستون‌های `varchar` حذف شوند. تبدیل شمسی←میلادی در `jdf.php` موجود است.

**Priority:**
MEDIUM

**Estimated Effort:**
High

---

#### [MEDIUM] نوع‌دهی ناسازگار ستون‌های وزن

**File:**
`PHP/schema.sql`

**Location:**
خط ۲۷، ۲۹–۳۰

**Problem:**
```sql
`netWeight`      int unsigned  DEFAULT NULL,
`shortageWeight` varchar(100)  DEFAULT NULL,
`excessWeight`   varchar(100)  DEFAULT NULL,
```
سه ستون وزن، دو نوع مختلف. `InitialInfo.averageNetWeight` هم `int` است درحالی‌که PHP آن را با `round($averageNetWeight, 2)` محاسبه می‌کند (`CargoController.php`) — یعنی دقت اعشاری در زمان ذخیره از بین می‌رود.

**Why it matters:**
هر محاسبه‌ای روی کسری/اضافی نیازمند cast ضمنی است که ایندکس را بی‌اثر می‌کند. سمت کلاینت هم این ناسازگاری بازتاب دارد: `CargoModels` برخی وزن‌ها را `Float` و برخی را `String` می‌گیرد، و `FloatTypeAdapter` باید حالت `JsonToken.STRING` را مدیریت کند — که خودش منشأ یافته‌ی `[MEDIUM] FloatTypeAdapter` است.

**Impact:**
ریسک خطای محاسباتی و گرد کردن در داده‌ی تجاری اصلی سیستم.

**Recommended Fix:**
همه‌ی ستون‌های وزن را به یک نوع دقیق واحد ببر:
```sql
ALTER TABLE CargoInfo
  MODIFY shortageWeight DECIMAL(12,2) NULL,
  MODIFY excessWeight   DECIMAL(12,2) NULL,
  MODIFY netWeight      DECIMAL(12,2) NULL;
```
`DECIMAL` به‌جای `FLOAT` چون این مقادیر مالی/عملیاتی‌اند و خطای ممیز شناور در آن‌ها پذیرفتنی نیست.

**Priority:**
MEDIUM

**Estimated Effort:**
Medium

---

#### [MEDIUM] `CargoInfo` هیچ کلید خارجی به `InitialInfo` ندارد

**File:**
`PHP/schema.sql`

**Location:**
خط ۲۰–۵۱

**Problem:**
`CargoInfo` پنج ستون دارد که منطقاً به `InitialInfo` اشاره می‌کنند (`loadingQuotaNumber`, `shipName`, `loadingWarehouse`, `shippingCompany`, `cargoType`) اما هیچ محدودیت ارجاعی وجود ندارد. `InitialInfo` حتی کلید یکتایی روی این ترکیب ندارد — فقط `idx_initial_info_composite` که غیریکتا است.

**Why it matters:**
- `QuotaService::deleteQuota` می‌تواند یک ردیف `InitialInfo` را حذف کند و ردیف‌های `CargoInfo` یتیم شوند.
- هیچ چیز مانع از این نیست که دو ردیف `InitialInfo` با کلید طبیعی یکسان ثبت شوند — که دقیقاً پیش‌شرط باگ بیش‌شماری JOIN است.
- درستی داده کاملاً به کد اپلیکیشن وابسته است.

**Impact:**
ردیف‌های یتیم؛ کوتاژهای تکراری؛ نتایج تجمعی نادرست.

**Recommended Fix:**
اول یکتایی را اعمال کن (که به‌تنهایی ارزش زیادی دارد):
```sql
ALTER TABLE InitialInfo
  ADD UNIQUE KEY uk_initial_natural
    (loadingQuotaNumber, shipName, loadingWarehouse, shippingCompany, cargoType);
```
سپس یک `initial_info_id` به `CargoInfo` اضافه کن و به‌تدریج پر کن؛ کلید خارجی را پس از پاک‌سازی داده اضافه کن.

**Priority:**
MEDIUM

**Estimated Effort:**
High

---

#### [LOW] ایندکس‌های زائد و کم‌ارزش روی `admin_chat_messages`

**File:**
`PHP/schema.sql`

**Location:**
خط ۱۳۲–۱۳۶

**Problem:**
```sql
KEY `idx_created_at` (`created_at`),
KEY `idx_username` (`username`),
KEY `idx_is_read` (`is_read`),
KEY `idx_username_created_at` (`username`,`created_at`),
KEY `idx_is_deleted` (`is_deleted`)
```
- `idx_username` زائد است — `idx_username_created_at` پیشوند آن را پوشش می‌دهد.
- `idx_is_read` و `idx_is_deleted` روی ستون‌های بولی با کاردینالیتی ۲ هستند؛ بهینه‌ساز تقریباً هرگز از آن‌ها استفاده نمی‌کند.

**Impact:**
تقویت هزینه‌ی نوشتن و مصرف فضا بدون سود خواندن. اثر کم است چون جدول چت کوچک است.

**Recommended Fix:**
```sql
ALTER TABLE admin_chat_messages
  DROP INDEX idx_username,
  DROP INDEX idx_is_read,
  DROP INDEX idx_is_deleted;
```

**Priority:**
LOW

**Estimated Effort:**
Low

---

#### [LOW] collation ناسازگار در جدول `Passwords`

**File:**
`PHP/schema.sql`

**Location:**
خط ۸۸

`Passwords` از `utf8mb4_0900_ai_ci` استفاده می‌کند درحالی‌که همه‌ی جدول‌های دیگر `utf8mb4_unicode_ci` هستند. امروز بی‌ضرر است چون این جدول با هیچ جدولی JOIN نمی‌شود، اما اگر روزی بشود، MySQL خطای «Illegal mix of collations» می‌دهد یا مقایسه را غیرقابل‌ایندکس می‌کند.

**Recommended Fix:**
`ALTER TABLE Passwords CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`

**Priority:**
LOW

**Estimated Effort:**
Low

---

## Authentication & Authorization

### جریان کامل (استخراج‌شده از کد)

```text
۱. ورود
   LoginScreen → AuthViewModel → LoginUseCase → AuthRepositoryImpl
     → POST auth/login { username, password, deviceId, deviceModel, androidVersion, appVersion }
   سرور: LoginAttemptLimiter.isLocked → UserService.verifyCredentials (bcrypt cost 12)
        → PermissionService.getUserPermissions → SessionService.createMobileSession
        → SessionRepository.createSession (توکن‌ها با SHA-256 هش می‌شوند)
   پاسخ: { session_token (۳۰ دقیقه), refresh_token (۲۴ ساعت), permissions }
   کلاینت: UserPreferencesManager رمز می‌کند (AES-GCM/Keystore) → DataStore + AuthSession

۲. درخواست احرازشده
   headersInterceptor → X-Username / X-Device-Id / X-Session-Token / X-App-Version
   سرور: Router → ApiAuthGate.requireAuthenticated → SessionService.validateAndGetUserType
        → یک SELECT + یک UPDATE با throttle ۶۰ ثانیه‌ای روی last_activity
        → ApiAuthGate.requirePermission → PermissionService (کش APCu، TTL ۳۰ ثانیه)

۳. انقضای access token
   سرور: 401 { code: "access_token_expired" }
   کلاینت: TokenAuthenticator → Mutex → TokenRefresher.refresh → POST auth/refresh
        → هر دو توکن rotate می‌شوند → درخواست اصلی retry می‌شود

۴. تشخیص سرقت توکن
   refresh token نامعتبر → deactivateAllSessions + SecurityAlerter + 401
```

### ارزیابی

این طراحی **قوی** است و چند تصمیم درست دارد که ارزش تأکید دارند:

- توکن‌ها با SHA-256 هش و ذخیره می‌شوند، نه plaintext (`SessionRepository::hashToken`). دلیل انتخاب SHA-256 به‌جای bcrypt هم درست مستند شده: توکن خودش ۲۵۶ بیت آنتروپی تصادفی دارد.
- **rotation هر دو توکن** در هر refresh، به‌علاوه‌ی **تشخیص reuse** که کل نشست‌ها را باطل می‌کند (`SessionService.php:126-135`) — این الگوی توصیه‌شده‌ی OAuth 2.1 برای کلاینت‌های عمومی است.
- مقایسه‌ی زمان‌ثابت با `hash_equals` روی refresh token و CSRF.
- `refreshToken` عمداً از `InputValidator::sanitize` عبور داده نمی‌شود تا مقایسه نشکند (`AuthController.php:157`) — جزئیات درستی که معمولاً از قلم می‌افتد.
- هویت (`username`/`userType`) همیشه از **نشست احرازشده** گرفته می‌شود، نه از پارامتر ورودی — در `CargoController.php:55-56`, `ChatController.php:42`, `UserController::updateFcmToken` صریحاً پیاده شده.
- IDOR در `UserController::updateUser` (خط ۱۵۹–۱۸۶) به‌درستی بسته شده: کاربر بدون `manage_users` فقط رکورد خودش را می‌تواند ویرایش کند، نمی‌تواند `username`/`userType` را تغییر دهد، و برای تغییر رمز باید رمز فعلی را تأیید کند.
- مالکیت پیام در `ChatController::isMessageOwner` بررسی می‌شود.

### مسائل باقی‌مانده

مسائل احراز هویت/مجوز در بخش Security مستند شده‌اند:
- `[HIGH]` دور زدن گیت نسخه با حذف هدر
- `[MEDIUM]` شمارش نام کاربری در `checkSession`
- `[MEDIUM]` افشای فهرست ادمین‌ها به همه‌ی کاربران
- `[MEDIUM]` race در rate-limiter فایل‌محور

#### [MEDIUM] TOCTOU در ایجاد نشست

**File:**
`PHP/src/Services/SessionService.php`

**Location:**
خط ۳۳–۸۷

**Problem:**
```php
$existingSession = $this->sessionRepository->getActiveSession($username);   // بررسی
if ($existingSession) { ... }
...
$sessionId = $this->sessionRepository->createSession([...]);                 // استفاده
```
بین `getActiveSession` و `createSession` هیچ قفلی نیست. دو درخواست ورود همزمان از دو دستگاه هر دو `null` می‌بینند و هر دو نشست می‌سازند — که قید «ورود تک‌دستگاهی» را نقض می‌کند.

**Impact:**
دور زدن محدودیت ورود همزمان با درخواست‌های موازی. اثر عملی محدود است (نیازمند اعتبارنامه‌ی معتبر) اما یک قید تجاری اعلام‌شده را می‌شکند.

**Recommended Fix:**
یک قید یکتایی جزئی در دیتابیس یا `SELECT ... FOR UPDATE` داخل تراکنش:
```php
$this->db->beginTransaction();
$existing = /* SELECT ... WHERE username = ? AND is_active = 1 FOR UPDATE */;
...
$this->db->commit();
```
`CargoRepository::findCargoByKeys` از قبل دقیقاً همین الگوی `FOR UPDATE` را استفاده می‌کند (خط ۵۴) — پس الگوی درست در کدبیس موجود است.

**Priority:**
MEDIUM

**Estimated Effort:**
Low

---

#### [MEDIUM] ویرایش پروفایل خود کاربر، او را از سیستم خارج می‌کند

> ✅ **رفع شد (فاز ۲، مورد ۱۷):** `UserService::updateUser` اکنون فقط وقتی یکی از فیلدهای امنیتی (`password`، `userType`، `username`) واقعاً تغییر کرده باشد `deactivateAllSessions` را صدا می‌زند؛ ویرایش صرفاً `fullName` دیگر کاربر را بی‌دلیل بیرون نمی‌اندازد.


**File:**
`PHP/src/Services/UserService.php`

**Location:**
خط ۱۸۹–۱۹۱

**Problem:**
```php
$this->userRepository->update($id, $updates);
// پس از ویرایش موفق، جلسات قبلی کاربر را غیرفعال می‌کنیم
$sessionRepo = new \App\Repositories\SessionRepository();
$sessionRepo->deactivateAllSessions($user['username']);
```
این بدون قید و شرط اجرا می‌شود — حتی وقتی کاربر فقط `fullName` خودش را عوض کرده است.

**Why it matters:**
ابطال نشست پس از تغییر رمز یا تغییر `userType` **درست** است. اما ابطال پس از تغییر نام نمایشی، کاربر را بی‌دلیل بیرون می‌اندازد. `UserController` مسیر ویرایش پروفایل شخصی را مجاز می‌کند (خط ۱۶۳–۱۸۶)، پس این سناریو واقعی است.

**Impact:**
اپراتور در میانه‌ی کار انبار به صفحه‌ی ورود پرتاب می‌شود.

**Recommended Fix:**
فقط برای تغییرات امنیتی ابطال کن:
```php
$securitySensitive = array_intersect(array_keys($updates), ['password', 'userType', 'username']);
if (!empty($securitySensitive)) {
    (new SessionRepository())->deactivateAllSessions($user['username']);
}
```

**Priority:**
MEDIUM

**Estimated Effort:**
Low
---

## Error Handling

### وضعیت کلی

مدیریت خطا در بک‌اند **منسجم** است: هر کنترلر یک `try/catch` دولایه دارد که `ApiException` را به کلاینت می‌فرستد و `Throwable` را فقط لاگ می‌کند و پیام عمومی برمی‌گرداند. این دقیقاً کار درست است و از افشای ساختار دیتابیس جلوگیری می‌کند.

سمت کلاینت، الگوی `Result<T>` در Repositoryها و `sealed interface` برای stateها استفاده شده. `AppError` در `:app` تعریف شده است.

---

#### [MEDIUM] `catch (Exception)` سراسری به «دستکاری‌شده» ترجمه می‌شود

**File:**
`app/src/main/java/com/atk/atk_cargo/core/startup/StartupViewModel.kt`

**Location:**
خط ۲۲۷–۲۳۳

**Problem:**
```kotlin
} catch (_: Exception) {
    _securityCheck.value = SecurityCheckState(
        isPassed = false,
        isLoading = false,
        errorType = SecurityErrorType.TAMPERED     // ← هر استثنایی = «دستکاری‌شده»
    )
}
```
`SecurityVerifier.verifySecurityStatus()` از قبل خودش خطاهای شبکه را مدیریت و به `NETWORK_ERROR` نگاشت می‌کند. اما هر استثنای پیش‌بینی‌نشده‌ای که از آن عبور کند — مثلاً `UnsatisfiedLinkError` هنگام خواندن `Secrets`، یا یک `NullPointerException` — به کاربر پیام «برنامه دستکاری شده است» نشان می‌دهد.

**Why it matters:**
این بدترین پیام ممکن برای یک خطای گذراست: کاربر فکر می‌کند دستگاهش آلوده است، پشتیبانی تماس می‌گیرد، و تیم زمان زیادی صرف تعقیب یک تهدید امنیتی موهوم می‌کند. همچنین هیچ لاگی از استثنای اصلی گرفته نمی‌شود (`_` به‌جای `e`).

**Impact:**
تشخیص غلط؛ بار پشتیبانی؛ از دست رفتن اطلاعات عیب‌یابی.

**Recommended Fix:**
```kotlin
} catch (e: Exception) {
    Log.e("StartupViewModel", "بررسی امنیتی با خطای غیرمنتظره شکست خورد", e)
    _securityCheck.value = SecurityCheckState(
        isPassed = false, isLoading = false,
        errorType = SecurityErrorType.UNKNOWN_ERROR
    )
}
```
`UNKNOWN_ERROR` از قبل در enum وجود دارد و در `SecurityBlockScreen` پیام ملایم‌تری دارد.

**Priority:**
MEDIUM

**Estimated Effort:**
Low

---

#### [MEDIUM] `sendMessage` در صورت پاسخ ناقص، موفقیت کاذب گزارش می‌کند

**File:**
`feature/chat/src/main/java/com/atk/atk_cargo/feature/chat/data/ChatRepository.kt`

**Location:**
خط ۱۱۶–۱۲۱

**Problem:**
```kotlin
if (response.isSuccessful && response.body()?.success == true) {
    response.body()?.messageData?.let { newMessage ->
        chatDao.insertMessage(newMessage.toEntity(username))
    }
    Result.success(Unit)      // ← موفق، حتی اگر messageData نال باشد
}
```
اگر سرور `success: true` ولی `messageData: null` بفرستد، هیچ چیزی در دیتابیس محلی درج نمی‌شود ولی `Result.success` برمی‌گردد. کاربر می‌بیند که پیام «ارسال شد» اما پیام در فهرست ظاهر نمی‌شود تا refresh بعدی.

**Impact:**
پیام‌های «گم‌شده» از دید کاربر — یک باگ گزارش‌شونده و سخت برای بازتولید.

**Recommended Fix:**
یا `messageData` را الزامی کن، یا پس از درج ناموفق یک refresh فوری راه بینداز:
```kotlin
val newMessage = response.body()?.messageData
if (newMessage != null) {
    chatDao.insertMessage(newMessage.toEntity(username))
} else {
    refreshMessages()   // fallback
}
Result.success(Unit)
```

**Priority:**
MEDIUM

**Estimated Effort:**
Low

---

#### [MEDIUM] `refreshMessages` کش محلی را روی پاسخ خالی پاک می‌کند

**File:**
`feature/chat/src/main/java/com/atk/atk_cargo/feature/chat/data/ChatRepository.kt`

**Location:**
خط ۶۰–۶۴

**Problem:**
```kotlin
} else {
    // اگر سرور هیچ پیامی برنگرداند، یعنی چت کلاً خالی شده است
    Log.d("ATK_CHAT_DEBUG", "Refresh: Server returned empty list. Clearing local cache.")
    chatDao.clearAll()
}
```
یک باگ گذرای سرور، یک پاسخ ناقص، یا یک تغییر در سطح دسترسی که باعث می‌شود `getMessages` آرایه‌ی خالی برگرداند — همگی کل تاریخچه‌ی محلی چت را پاک می‌کنند.

**Why it matters:**
`ChatController::getMessages` برای کاربر غیرادمین `ApiException(403)` پرتاب می‌کند نه آرایه‌ی خالی، پس آن سناریو پوشش داده شده. اما هر تغییر آینده در سمت سرور که به «آرایه‌ی خالی» منجر شود، بی‌صدا داده‌ی کلاینت را نابود می‌کند.

**Impact:**
از دست رفتن غیرقابل بازیابی کش محلی چت (البته قابل بازیابی از سرور، مگر اینکه همان مشکل سرور ادامه داشته باشد).

**Recommended Fix:**
حذف را فقط بر اساس یک سیگنال صریح انجام بده، نه استنتاج از خالی بودن:
```kotlin
val messages = response.body()?.messages ?: emptyList()
if (messages.isEmpty() && response.body()?.totalCount == 0) {
    chatDao.clearAll()
}
```
یا ساده‌تر: هرگز روی پاسخ خالی پاک نکن و به `deleteOrphanedMessages` تکیه کن که از قبل همگام‌سازی حذف‌ها را در بازه‌ی شناخته‌شده انجام می‌دهد.

**Priority:**
MEDIUM

**Estimated Effort:**
Low

---

#### [LOW] `CryptoManager` شکست رمزگشایی را به رشته‌ی خالی تبدیل می‌کند

**File:**
`app/src/main/java/com/atk/atk_cargo/security/CryptoManager.kt`

**Location:**
خط ۸۳–۸۷

اگر کلید Keystore باطل شود (بازیابی دستگاه، تغییر قفل صفحه)، `decrypt` رشته‌ی خالی برمی‌گرداند و کاربر بی‌هیچ توضیحی به صفحه‌ی ورود می‌رود. این رفتار **عمدی و امن** است (کامنت کد آن را توضیح می‌دهد) اما تجربه‌ی کاربری‌اش گیج‌کننده است.

**Recommended Fix:**
یک `Result` یا نوع بازگشتی nullable برگردان تا لایه‌ی بالاتر بتواند پیام «لطفاً دوباره وارد شوید — تنظیمات امنیتی دستگاه تغییر کرده» را نشان دهد.

**Priority:**
LOW

**Estimated Effort:**
Low

---

## Logging & Observability

### وضعیت

- **صفر مورد `println`** در کد production.
- ProGuard `Log.v/d/i/w` را در release حذف می‌کند (`proguard-rules.pro:179-185`). `Log.e` عمداً باقی می‌ماند.
- `Logger::sanitizeMessage()` سه الگوی حساس (`password`, `csrf_token`, `session_token`) را در JSON ماسک می‌کند.
- `Logger` پیام‌ها را بافر و در `register_shutdown_function` یک‌جا می‌نویسد — کاهش I/O.
- `AuditLogger` عملیات حساس (`createUser`, `updateUser`, `deleteUser`, تغییرات کوتاژ) را در جدول `audit_log` ثبت می‌کند و **عمداً مقدار رمز را ثبت نمی‌کند** (فقط `changedFields`).
- `CrashReporter` بدون سرویس ثالث پیاده شده و به `diagnostics/crash` گزارش می‌فرستد.
- `logs/.htaccess` و `log/.htaccess` دسترسی HTTP مستقیم را می‌بندند.
- `scripts/rotate_logs.php`, `health_monitor.php`, `crash_report_summary.php` برای نگهداری موجودند.

---

#### [MEDIUM] لاگ‌های debug محتوای پیام چت را ثبت می‌کنند

**File:**
`feature/chat/src/main/java/com/atk/atk_cargo/feature/chat/data/ChatRepository.kt`

**Location:**
خط ۱۱۰، ۱۱۴، ۱۲۴

**Problem:**
```kotlin
Log.d("ATK_CHAT_DEBUG", "Send Message: Requesting - user: $username, msg: $message")
```
نام کاربری و **متن کامل پیام** در logcat ثبت می‌شوند. مجموعاً ۲۹ فراخوانی `Log.d/v/i` در کد production وجود دارد که چند مورد آن‌ها داده‌ی کاربر را شامل می‌شوند.

**Why it matters:**
`-assumenosideeffects` این‌ها را در release حذف می‌کند، پس در تولید نشتی وجود ندارد. اما در بیلد debug — که روی دستگاه‌های واقعی برای عیب‌یابی نصب می‌شود — هر اپلیکیشنی با مجوز `READ_LOGS` (یا هر کسی با `adb`) پیام‌های چت مدیران را می‌بیند.

**Impact:**
نشت داده در محیط debug؛ ریسک اینکه یک بیلد debug تصادفاً به دست کاربر برسد.

**Recommended Fix:**
هرگز محتوای پیام را لاگ نکن، حتی در debug. شناسه کافی است:
```kotlin
Log.d("ATK_CHAT", "Send Message: user=$username, len=${message.length}")
```
و `HttpLoggingInterceptor.Level.BODY` را (که در debug فعال است و توکن نشست و رمز عبور را در logcat چاپ می‌کند) به `HEADERS` یا `BASIC` تغییر بده.

**Priority:**
MEDIUM

**Estimated Effort:**
Low

---

#### [MEDIUM] هیچ مانیتورینگ یا هشدار خودکاری در تولید وجود ندارد

**File:**
سطح سیستم

**Problem:**
تنها مکانیزم هشدار `SecurityAlerter` است که فقط برای دو رویداد (`REFRESH_TOKEN_REUSE_DETECTED`, `ACCOUNT_LOCKED`/`IP_LOCKED`) فعال می‌شود و اگر `SECURITY_ALERT_TELEGRAM_BOT_TOKEN` تنظیم نشده باشد کاملاً no-op است. هیچ چیزی نرخ خطای ۵۰۰، زمان پاسخ، یا حجم کرش را رصد نمی‌کند.

`endpoint health` وجود دارد (`GET health`، بدون auth، عمدی برای مانیتورینگ خارجی) و `scripts/health_monitor.php` هم هست — اما هیچ نشانه‌ای از اینکه به یک سرویس مانیتورینگ واقعی وصل باشد وجود ندارد.

**Impact:**
خرابی‌ها ابتدا توسط کاربران کشف می‌شوند، نه تیم. برای سیستمی که عملیات انبار به آن وابسته است، این ریسک بالایی است.

**Recommended Fix:**
۱. `scripts/health_monitor.php` را در cron بگذار و در صورت `unhealthy` از همان `SecurityAlerter` برای هشدار تلگرام استفاده کن (زیرساخت موجود است).
۲. `SecurityAlerter` را به یک `Alerter` عمومی گسترش بده و نرخ خطای ۵۰۰ و حجم کرش را هم به آن وصل کن (`crash_report_summary.php` از قبل تجمیع را انجام می‌دهد).
۳. `SECURITY_ALERT_TELEGRAM_*` را در `.env` تولید حتماً تنظیم کن — بدون آن، حتی تشخیص سرقت توکن هم بی‌صدا است.

**Priority:**
MEDIUM

**Estimated Effort:**
Low (با زیرساخت موجود)

---

## Dependency Audit

### وضعیت: **بسیار خوب** — این نقطه‌ی قوت پروژه است

| وابستگی | نسخه | ارزیابی |
|---------|------|---------|
| AGP | 8.13.0 | به‌روز |
| Kotlin | 2.2.20 | به‌روز |
| Compose BOM | 2025.09.00 | به‌روز |
| Retrofit | 3.0.0 | به‌روز |
| OkHttp | 5.1.0 | به‌روز |
| Koin | 4.1.1 | به‌روز |
| Room | 2.7.0 | به‌روز |
| Navigation Compose | 2.9.4 | به‌روز |
| WorkManager | 2.10.4 | به‌روز |
| CameraX | 1.5.0 | به‌روز |
| PHP | ≥8.1 | پشتیبانی‌شده (اما 8.3 توصیه می‌شود) |
| PHPStan | ^1.10 | نسخه‌ی ۲.x موجود است |
| PHPUnit | ^9.6 | نسخه‌ی ۱۱.x موجود است |

هیچ وابستگی منسوخ یا با آسیب‌پذیری شناخته‌شده‌ای پیدا نشد.

---

#### [LOW] نام مستعارهای version catalog با نسخه‌های واقعی نمی‌خوانند

**File:**
`gradle/libs.versions.toml`

**Problem:**
```toml
androidx-core-ktx-v1160        = { ... version.ref = "core-ktx" }        # واقعی: 1.17.0
androidx-activity-compose-v1101 = { ... version.ref = "activity-compose" } # واقعی: 1.11.0
androidx-navigation-compose-v290 = { ... version.ref = "navigation-compose" } # واقعی: 2.9.4
desugar_jdk_libs-v215          = { ... version.ref = "desugar_jdk_libs" }  # واقعی: 2.1.5
```
پسوند نسخه در نام مستعار، نسخه‌ی قدیمی را نشان می‌دهد و با ارتقا هم‌گام نشده.

**Impact:**
گمراه‌کننده هنگام بازبینی کد؛ کسی که `build.gradle.kts` را می‌خواند نسخه‌ی اشتباه را تصور می‌کند.

**Recommended Fix:**
پسوندهای نسخه را از نام مستعارها حذف کن: `androidx-core-ktx`, `androidx-activity-compose`, `androidx-navigation-compose`, `desugar-jdk-libs`.

**Priority:**
LOW

**Estimated Effort:**
Low

---

#### [LOW] دو استک سریال‌سازی JSON هم‌زمان

**File:**
`app/build.gradle.kts`

**Location:**
خط ۳۱۴، ۳۲۰

`retrofit.gson` (برای شبکه) و `kotlinx.serialization.json` (برای مسیرهای type-safe Navigation) هر دو حضور دارند. این قابل توجیه است — `navigation-compose` نسخه‌ی type-safe به `kotlinx.serialization` نیاز دارد — اما دو کتابخانه، دو مجموعه قانون ProGuard و دو مدل ذهنی برای توسعه‌دهنده به‌همراه دارد.

**Recommended Fix:**
اگر روزی فرصت شد، لایه‌ی شبکه را به `kotlinx.serialization` با `Json.asConverterFactory` منتقل کن. مزیت جانبی: `FloatTypeAdapter` و مسائل مربوط به آن حذف می‌شوند چون `kotlinx.serialization` نوع‌ها را در زمان کامپایل تولید می‌کند و بازتاب ندارد — یعنی کل دسته‌ی باگ‌های release-only مربوط به obfuscation هم از بین می‌رود.

**Priority:**
LOW

**Estimated Effort:**
High

---

## Testing Audit

### وضعیت: **ضعیف‌ترین بخش پروژه**

| معیار | مقدار |
|-------|-------|
| خطوط کد production (Kotlin) | ۵۱٬۷۳۹ |
| خطوط کد تست (Kotlin) | ۸۵۲ |
| **نسبت تست** | **۱٫۶٪** |
| فایل تست Kotlin | ۱۰ (که ۲ تای آن قالب `Example*Test` است) |
| فایل تست PHP | ۶ |

### آنچه تست شده

| فایل | پوشش |
|------|-------|
| `TokenAuthenticatorTest.kt` · `TokenRefresherTest.kt` | ✅ منطق rotation توکن (بحرانی — انتخاب درست) |
| `CargoViewModelTest.kt` | ✅ بخشی از ViewModel اصلی |
| `ReportsDomainTest.kt` · `ReportsDomainCalculationsTest.kt` | ✅ محاسبات دامنه |
| `JalaliDateUtilsTest.kt` | ✅ تبدیل تاریخ |
| `SessionServiceTest.php` | ✅ منطق نشست با mock |
| `LoginAttemptLimiterTest.php` | ✅ (با `sleeper`/`alerter` تزریق‌شده — طراحی خوب) |
| `PermissionServiceTest.php` · `InputValidatorTest.php` · `QuotaCalculatorTest.php` · `LicenseAdminServiceTest.php` | ✅ |
| `CompactStatChipTest.kt` · `ActionButtonTest.kt` | ⚠️ دو کامپوننت جزئی |

### آنچه تست نشده (بحرانی)

| ناحیه | ریسک |
|-------|------|
| `CryptoManager` | رمزنگاری/رمزگشایی توکن — شکست = خروج همه‌ی کاربران |
| `SecurityVerifier` | منطق لایسنس و دوره‌ی مهلت — شکست = اپ باز نمی‌شود |
| `UpdateManager` | تأیید SHA-256 و allowlist دامنه — شکست = نصب APK نامعتبر |
| `AuthRepositoryImpl` / `LoginUseCase` / `LogoutUseCase` | کل جریان ورود |
| `ChatRepository` | همگام‌سازی، `deleteOrphanedMessages` |
| `ReportsRepository` | ۲۴ فراخوانی API |
| `AuthController` / `CargoController` / `ChatController` (PHP) | هیچ تست کنترلری وجود ندارد |
| `PermissionManager.php` | حساس‌ترین پنل، صفر تست، صفر تحلیل استاتیک |
| صفحه‌های Compose | صفر تست UI معنادار |

### تست‌های پیشنهادی (به ترتیب اولویت)

۱. **`UpdateManagerTest`** — تأیید کن `isTrustedDownloadUrl` دامنه‌های غیرمجاز، `http://`، و subdomain‌های جعلی را رد می‌کند؛ و اینکه با هش نامطابق فایل حذف می‌شود. ✅ اعمال شد و سبز (مورد ۲۰)
۲. **`CryptoManagerTest`** (androidTest، چون به Keystore واقعی نیاز دارد) — چرخه‌ی رمز/رمزگشایی، رفتار در برابر ورودی خراب. ✅ نوشته و compile-verified (مورد ۲۰) — اجرای واقعی روی دستگاه، در این محیط ممکن نبود
۳. **`AuthControllerTest`** (PHP) — شکست ورود، قفل شدن، ورود همزمان از دستگاه دوم. ✅ اعمال شد و سبز (مورد ۲۰)
۴. **`SecurityVerifierTest`** — منطق دوره‌ی مهلت آفلاین، تمایز خطای شبکه از رد صریح سرور.
۵. **`ChatRepositoryTest`** — همگام‌سازی حذف، رفتار روی پاسخ خالی.
۶. **تست ادغام JOIN** (PHP) — یک fixture با دو ردیف `InitialInfo` که در چهار ستون مشترک و در `shipName` متفاوت‌اند، و تأیید کن مجموع تناژ دوبرابر نمی‌شود. این تست یافته‌ی `[HIGH]` بخش Database را قفل می‌کند.

**Priority:**
HIGH

**Estimated Effort:**
High
---

## Code Quality

### Code Smells شناسایی‌شده

#### [HIGH] God ViewModel — `CargoViewModel`

**File:**
`feature/cargo/src/main/java/com/atk/atk_cargo/ui/viewmodel/CargoViewModel.kt`

**Location:**
کل فایل (۹۹۸ خط)

**Problem:**
- ۹۹۸ خط، بیش از ۳۵ متد عمومی.
- **۱۰ `MutableStateFlow` مجزا** (`_loadedWeight`, `_cargoCount`, `_cargoWeight`, `_remainingWeight`, `_averageNetWeight`, `_remainingServices`, `_totalServices`, `_isQuotaActive`, `_pendingCargoInfo`) **در کنار** یک `CargoUiState` واحد. دو الگوی state management متناقض در یک کلاس.
- کش دستی با فیلدهای `lastQuotaStatusCheck`, `lastLoadableTonnageUpdate`, `cachedQuotaStatus`, `cachedLoadableTonnage`, `loadableTonnageCacheTimeout` (خط ۱۷۶–۱۸۰) — منطق کش که باید در Repository باشد.
- **مسئولیت‌های درهم:** اعتبارسنجی ورودی، فراخوانی API، مدیریت دیالوگ، صف پیام، بررسی تکراری بودن، پارس خطا، کش، و به‌روزرسانی خوش‌بینانه‌ی لیست محلی.
- `response.body()!!` در خط ۶۷۵ و ۹۴۶.

**Impact:**
غیرقابل تست بودن (تنها یک فایل تست ناقص برایش وجود دارد)، recomposition غیرضروری به‌خاطر state پراکنده، و ریسک بالای رگرسیون در هر تغییر.

**Recommended Fix:**
۱. ده `MutableStateFlow` را در `CargoUiState` ادغام کن — یک منبع حقیقت.
۲. کش (`lastQuotaStatusCheck` و همراهانش) را به `QuotaRepository` منتقل کن؛ آنجا جای درستش است.
۳. `submitCargoInfo` و زیرروال‌هایش را به یک `SubmitCargoUseCase` استخراج کن. `QuotaValidationUseCase` از قبل وجود دارد — همان الگو را ادامه بده.
۴. همه‌ی فراخوانی‌های `apiServiceV2` را به `QuotaRepository` منتقل کن (رجوع به یافته‌ی نشت لایه).

**Priority:**
HIGH

**Estimated Effort:**
High

---

#### [HIGH] نشت لایه — ViewModelها مستقیم `ApiServiceV2` را صدا می‌زنند

**File:**
چند فایل

**Location:**
| فایل | تعداد فراخوانی مستقیم |
|------|:---------------------:|
| `feature/cargo/.../CargoViewModel.kt` | ۱۱ |
| `feature/admin/.../UserManagementViewModel.kt` | ۷ |
| `feature/home/.../ProfileViewModel.kt` | ۲ |
| `feature/cargo-workflow/.../InitialInfoViewModel.kt` | ۲ |
| `feature/cargo-workflow/.../CargoCounterScreen.kt` (ViewModel داخل فایل صفحه) | ۲ |
| `feature/chat/.../ChatViewModel.kt` | ۱ |
| `app/.../StartupViewModel.kt` | ۱ |
| `app/.../PermissionPoller.kt` | ۱ |

**Problem:**
پروژه فقط ۵ Repository دارد (`QuotaRepository`, `AuthRepository`, `ChatRepository`, `ReportsRepository`, `PermissionRepository`) برای ۱۴ ماژول فیچر. بقیه‌ی ViewModelها لایه‌ی داده را دور می‌زنند.

**Why it matters:**
این فقط یک ایراد سلیقه‌ای نیست — پیامدهای عملی دارد:
- **تست‌ناپذیری:** برای تست `UserManagementViewModel` باید Retrofit را mock کنی، نه یک اینترفیس ساده.
- **بدون کش:** هر ViewModel باید کش خودش را بسازد (که `CargoViewModel` دقیقاً همین کار را کرده).
- **بدون منبع حقیقت واحد:** دو ViewModel که همان داده را می‌خواهند، دو بار fetch می‌کنند.
- **جابه‌جایی مدل API به UI:** مدل‌های `data.model` مستقیماً در Composableها استفاده می‌شوند، بدون تفکیک DTO/Domain.

**Impact:**
بدهی معماری که با هر فیچر جدید مرکب می‌شود.

**Recommended Fix:**
برای هر فیچر یک Repository بساز — حتی اگر ابتدا فقط یک wrapper نازک باشد:
```kotlin
class UserRepository(private val api: ApiServiceV2) {
    suspend fun getAllUsers(): Result<List<User>> = runCatching {
        api.getAllUsers().takeIf { it.isSuccessful }?.body().orEmpty()
    }
}
```
سپس `UserManagementViewModel` را به آن وابسته کن. این کار را تدریجی و فیچر‌به‌فیچر انجام بده، نه یکباره.

**Priority:**
HIGH

**Estimated Effort:**
High

---

#### [MEDIUM] `ChatRepository` مسئولیت‌های نامرتبط دارد (Feature Envy)

**File:**
`feature/chat/src/main/java/com/atk/atk_cargo/feature/chat/data/ChatRepository.kt`

**Location:**
خط ۱۹۲–۲۲۷

**Problem:**
```kotlin
suspend fun getAdminUsers(): List<User>
suspend fun getShipsList(): ShipsData
suspend fun getShipQuotas(shipName: String): List<Quota>
```
یک Repository چت که کشتی‌ها و کوتاژها را برمی‌گرداند. این متدها احتمالاً برای قابلیت «اشاره به کشتی/کوتاژ در پیام چت» اضافه شده‌اند، اما مرز ماژول را می‌شکنند: `:feature:chat` حالا به مدل‌های دامنه‌ی گزارش‌ها وابسته است (با نام کامل `com.atk.atk_cargo.data.model.ShipsData` نوشته شده‌اند — نشانه‌ی اینکه import هم غیرطبیعی بوده).

**Impact:**
نقض SRP؛ وابستگی متقاطع بین فیچرها؛ سردرگمی درباره‌ی مالکیت داده.

**Recommended Fix:**
این سه متد را به یک `ShipLookupRepository` در `:core:domain` یا `:feature:reports` منتقل کن و `ChatViewModel` را مستقیماً به آن وابسته کن.

**Priority:**
MEDIUM

**Estimated Effort:**
Low

---

#### [MEDIUM] فایل‌های بسیار بزرگ در سراسر پروژه

| فایل | خط |
|------|-----|
| `feature/reports/.../dialogs/CargoEditSearchDialogsSection.kt` | ۱٬۲۳۵ |
| `app/.../security/SecurityScreen.kt` | ۱٬۱۵۰ |
| `app/.../ui/screens/ManageReportsScreen.kt` | ۱٬۰۳۴ |
| `feature/cargo/.../CargoViewModel.kt` | ۹۹۸ |
| `feature/home/.../HomeScreen.kt` | ۹۸۴ |
| `feature/reports/.../quota_details/QuotaCardComponents.kt` | ۹۴۹ |
| `feature/reports/.../ui/viewmodel/ReportsViewModel.kt` | ۸۳۵ |
| `PHP/src/Services/QuotaService.php` | ۷۰۰ |
| `PHP/src/Controllers/CargoController.php` | ۶۸۰ |
| `PHP/PermissionManager.php` | ۶۵۳ |

میانگین `:feature:reports` = ۵۱۷ خط به‌ازای هر فایل. `PermissionManager.php` علاوه بر PHP، شامل HTML، CSS و ~۲۵۰ خط JavaScript inline هم هست.

**Recommended Fix:**
سقف نرم ۳۰۰ خط برای فایل‌های UI و ۴۰۰ خط برای سرویس‌ها بگذار و در بازبینی کد اعمالش کن. `PermissionManager.php` را به template + دارایی‌های استاتیک تفکیک کن (پوشه‌ی `PHP/assets/` از قبل وجود دارد و `permission-manager.css` در آن است — کار نیمه‌تمام).

**Priority:**
MEDIUM

**Estimated Effort:**
High

---

#### [MEDIUM] ViewModel داخل فایل Composable

**File:**
`feature/cargo-workflow/src/main/java/com/atk/atk_cargo/feature/cargo_counter/presentation/CargoCounterScreen.kt`

**Location:**
خط ۱۰۹–۱۵۰

**Problem:**
`CargoCounterViewModel` داخل همان فایلی تعریف شده که صفحه‌ی Compose در آن است. علاوه بر آن، API آن مبتنی بر callback است:
```kotlin
fun loadActiveShips(onSuccess: (List<ActiveShipInfo>) -> Unit, onError: (String) -> Unit)
fun refreshRealTimeLoadingData(onSuccess: ..., onEmpty: ..., onError: ..., onFinally: ...)
```
این با بقیه‌ی پروژه که از `StateFlow` استفاده می‌کند ناسازگار است، و callbackها در composition گرفته می‌شوند.

**Impact:**
سبک ناسازگار؛ سخت شدن تست؛ ریسک نشت اگر callback به state محدود به composition اشاره کند.

**Recommended Fix:**
ViewModel را به فایل جداگانه منتقل کن و به `StateFlow<UiState>` تبدیلش کن، هم‌راستا با `CargoViewModel` و `ReportsViewModel`.

**Priority:**
MEDIUM

**Estimated Effort:**
Medium

---

#### [LOW] اعداد و رشته‌های جادویی

نمونه‌ها:
- `StartupViewModel.kt:409` — `SPLASH_MIN_DURATION = 3800L` (چرا ۳۸۰۰؟)
- `SecurityVerifier.kt:36` — `BUFFER_DURATION = 8000`، نام گمراه‌کننده (این timeout است نه buffer)
- `SecurityVerifier.kt:49` — `getSharedPreferences("x1y2z3", ...)` — نام مبهم بدون ثابت
- `PermissionPoller.kt:22` — `POLL_INTERVAL_MS = 3 * 60 * 1000L`
- رشته‌ی `"admin"` به‌صورت literal در ۶+ محل مقایسه می‌شود (`StartupViewModel.kt:299`, `LoadingNotificationService.kt:160`, `ChatController.php:299`, `AuthController.php:97`, ...). یک `enum class UserType` وجود ندارد.
- کلیدهای WorkManager به‌صورت literal: `"ChatNotificationWorker"` در دو جا (`StartupViewModel.kt:316, 335`).

**Recommended Fix:**
یک `enum class UserType { ADMIN, OPERATOR, VERIFIER }` با `wireValue` بساز — دقیقاً همان الگویی که `CargoStatus` و `CargoConfirmStatus` در PHP دارند و در Kotlin هم `CargoConfirmStatus.CONFIRMED.wireValue` استفاده می‌شود. الگو موجود است، فقط برای `userType` اعمال نشده.

**Priority:**
LOW

**Estimated Effort:**
Low

---

## Project Structure

### چه چیزی خوب است

- تفکیک `:core:*` / `:feature:*` — جهت وابستگی درست است (فیچرها به core وابسته‌اند، نه برعکس).
- استفاده از اینترفیس‌های مرزی برای شکستن وابستگی: `TokenStore` (در `:core:network`), `UserPreferencesStore`, `ChatPreferencesStore`, `UserSettingsStore` (در `:core:domain`) — همگی توسط `UserPreferencesManager` در `:app` پیاده‌سازی می‌شوند. این الگوی Dependency Inversion درست است و **نقطه‌ی قوت واقعی** معماری این پروژه است.
- توابع الحاقی ناوبری در فیچرها (`homeScreen(...)`, `cargoRegistrationScreen(...)`).
- `core:designsystem` با تم و کامپوننت‌های مشترک.

### چه چیزی بد است

#### [HIGH] هفت پکیج بین ماژول‌ها split شده‌اند

**File:**
سطح پروژه

**Problem:**

| پکیج | ماژول‌ها | وضعیت |
|------|----------|:-----:|
| `com.atk.atk_cargo.api` | `:app` · `:core:network` · `:feature:update` | ⏭️ باقی ماند (فاز۳ #۲۳) — سه‌طرفه، ۲۴ فایل مرکزی + ۲۲ importکننده؛ طبق تصمیم کاربر برای یک تغییر جدا کنار گذاشته شد |
| ~~`com.atk.atk_cargo.core.domain`~~ | `:app` · `:core:domain` | ✅ رفع شد — `AppError.kt` (بدون استفاده) به `com.atk.atk_cargo.error` در `:app` تغییر نام گرفت |
| ~~`com.atk.atk_cargo.core.startup`~~ | `:app` · `:core:domain` | ✅ رفع شد — `StartupViewModel.kt` در `:app` به `com.atk.atk_cargo.startup` تغییر نام گرفت (سمت `:core:domain`/`StartupComposition.kt` دست‌نخورده ماند) |
| ~~`com.atk.atk_cargo.core.ui.components`~~ | `:app` · `:core:designsystem` | ✅ رفع شد (فاز۳ #۲۴) |
| ~~`com.atk.atk_cargo.data.model`~~ | `:core:designsystem` · `:core:network` | ✅ رفع شد — `ColorSelector.kt` به `com.atk.atk_cargo.core.ui.components` در `:core:designsystem` منتقل شد؛ این پکیج حالا فقط در `:core:network` است |
| ~~`com.atk.atk_cargo.ui.viewmodel`~~ | `:feature:cargo` · `:feature:reports` | ✅ رفع شد — `CargoViewModel` به `feature.cargo.viewmodel`، `ReportsViewModel` به `feature.reports.viewmodel` |
| ~~`com.atk.atk_cargo.utils`~~ | `:app` (test) · `:core:common` | ✅ رفع شد — `JalaliDateUtilsTest.kt` از `app/src/test` به `core/common/src/test` منتقل شد (تست کنار کلاسی که تست می‌کند) |

**Why it matters:**
- **نام پکیج، مالکیت ماژول را نشان نمی‌دهد.** وقتی `import com.atk.atk_cargo.api.UpdateManager` را می‌بینی، نمی‌دانی از کدام ماژول می‌آید.
- ابزارها (R8، IDE، تحلیل‌گرهای وابستگی) split package را به‌درستی مدل نمی‌کنند.
- مرزهای ماژول قابل اجرا نیستند — هیچ چیزی مانع از این نمی‌شود که کسی کلاسی را در پکیج «متعلق به» ماژول دیگر بگذارد.
- علاوه بر این، تست‌های `:feature:reports` در `app/src/androidTest/` زندگی می‌کنند (`CompactStatChipTest.kt`, `ActionButtonTest.kt`) — تست در ماژول اشتباه.

**Impact:**
سردرگمی در ناوبری کد؛ مرزهای ماژول تزئینی به‌جای واقعی.

**Recommended Fix:**
نام پکیج را با ماژول هم‌راستا کن:
```text
:core:network        → com.atk.atk_cargo.core.network
:core:designsystem   → com.atk.atk_cargo.core.designsystem
:core:common         → com.atk.atk_cargo.core.common
:feature:update      → com.atk.atk_cargo.feature.update
:feature:cargo       → com.atk.atk_cargo.feature.cargo
:app                 → com.atk.atk_cargo  (فقط Application، MainActivity، DI)
```
این یک refactor مکانیکی است که IDE می‌تواند اکثرش را انجام دهد. تست‌ها را هم به ماژول صاحبشان منتقل کن.

**Priority:**
HIGH (به‌عنوان بدهی معماری، نه ریسک تولید)

**Estimated Effort:**
Medium

---

#### [MEDIUM] کد فیچر در ماژول `:app` باقی مانده

**File:**
`app/src/main/java/com/atk/atk_cargo/`

**Problem:**
`:app` باید فقط نقطه‌ی ورود و اتصال DI باشد، اما ۶٬۲۷۲ خط دارد که شامل:

| مسیر | باید کجا باشد |
|------|---------------|
| `ui/screens/ManageReportsScreen.kt` (۱٬۰۳۴ خط) | `:feature:reports` |
| `core/ui/components/*` (۶ فایل) | `:core:designsystem` |
| `security/*` (`CryptoManager`, `SecurityVerifier`, `SecurityScreen` — ۱٬۴۰۰+ خط) | `:core:security` (ماژول جدید) |
| `core/startup/StartupViewModel.kt` | `:feature:startup` |
| `api/UserPreferencesManager.kt` | `:core:datastore` (ماژول جدید) |
| `api/AppNotificationManager.kt`, `LoadingNotificationService.kt`, `NotificationActionReceiver.kt`, `BootReceiver.kt` | `:core:notification` (ماژول جدید) |
| `workers/ChatNotificationWorker.kt` | `:feature:chat` |
| `core/domain/AppError.kt` | `:core:domain` |

**Impact:**
هر تغییر در این فایل‌ها کل ماژول `:app` را دوباره کامپایل می‌کند — و `:app` به همه‌ی ۱۴ ماژول دیگر وابسته است، پس کندترین واحد کامپایل پروژه است.

**Recommended Fix:**
به ترتیب سود/هزینه:
۱. `ManageReportsScreen.kt` → `:feature:reports` (بزرگ‌ترین برد، ساده‌ترین کار)
۲. `core/ui/components/*` → `:core:designsystem`
۳. ماژول `:core:security` بساز
۴. بقیه به‌تدریج

**Priority:**
MEDIUM

**Estimated Effort:**
Medium

---

#### [LOW] ماژول‌های بیش‌ازحد ریز و DI متمرکز

- `:core:common` فقط ۱ فایل و ۱۳۳ خط دارد (`JalaliDateUtils`). یک ماژول Gradle کامل با سربار پیکربندی برای یک کلاس ابزار.
- `:core:database` فقط ۹۸ خط دارد و صرفاً چت را پوشش می‌دهد.
- **همه‌ی ماژول‌های Koin در یک `appModule` واحد در `:app`** جمع شده‌اند (`di/AppModule.kt`, ۸۰ خط) که ViewModelهای همه‌ی فیچرها را ثبت می‌کند. یعنی `:app` باید به همه‌ی فیچرها وابسته باشد و هر فیچر جدید این فایل را لمس می‌کند.

**Recommended Fix:**
هر فیچر ماژول Koin خودش را داشته باشد و `:app` فقط آن‌ها را جمع کند:
```kotlin
// در :feature:chat
val chatModule = module {
    single { ChatRepository(get(), get(), get()) }
    viewModel { ChatViewModel(get()) }
}
// در :app
modules(coreModule, authModule, chatModule, cargoModule, reportsModule, ...)
```

**Priority:**
LOW

**Estimated Effort:**
Medium
---

## Technical Debt

### کد مرده (تأییدشده با جستجوی کل ریپازیتوری)

| مورد | فایل | شواهد |
|------|------|--------|
| ~~`AnimationManager.setPerformanceScore()`~~ | `core/domain/.../AnimationManager.kt:11` | ✅ حذف شد (فاز ۲، مورد ۱۴) — به‌جای وصل‌کردن، چون هرگز صدا زده نمی‌شد |
| ~~`UserPreferencesManager.saveHardwareScore()`~~ | `app/.../UserPreferencesManager.kt:191` | ✅ حذف شد (فاز ۲، مورد ۱۴) |
| ~~نمایش امتیاز سخت‌افزار~~ | `feature/home/.../ProfileMenu.kt:191` | ✅ حذف شد (فاز ۲، مورد ۱۴) |
| ~~`AnimationManager` تکراری~~ | `feature/startup/.../domain/AnimationManager.kt` | ✅ حذف شد (فاز ۲، مورد ۱۴) |
| ~~`ChatDao.markAsRead(messageId)`~~ | `core/database/.../ChatDao.kt:24` | ✅ حذف شد (فاز ۳، مورد ۲۹) |
| ~~endpoint `chat/messages/{id}/read`~~ | `PHP/src/routes/api_v2.php:524` | ✅ حذف شد (فاز ۳، مورد ۲۹) — route + `ChatController::markAsRead` |
| ~~endpoint `chat/unread-count`~~ | `api_v2.php:498` | ✅ حذف شد (فاز ۳، مورد ۲۹) — route + `ChatController::getUnreadCount` |
| ~~endpoint `utility/check-password`~~ | `api_v2.php:430` | ✅ حذف شد (فاز ۳، مورد ۲۹) — route + `UtilityController::checkPassword` |
| ~~endpoint `analytics/quota-remaining`~~ | `api_v2.php:579` + `quota_remaining_api.php` | ✅ حذف شد (فاز ۳، مورد ۲۹) — route، `AnalyticsController::handleQuotaRemaining`/`getActiveQuotasRemaining`/`getShipQuotasRemaining`، و فایل `quota_remaining_api.php` |
| ~~کل زنجیره‌ی FCM~~ | `api_v2.php:584` + `update_fcm_token.php` + `UserController::updateFcmToken` + ستون `Users.fcm_token` | ✅ حذف شد (فاز ۳، مورد ۲۹) — طبق تصمیم کاربر (بدون Firebase واقعی، حذف کامل به‌جای نگه‌داشتن). ستون `fcm_token` از قبل در `schema.sql` تعریف نشده بود، پس نیازی به migration نبود |
| ~~قوانین ProGuard مرده~~ | `app/proguard-rules.pro:63-64, 159-167` | ✅ حذف شد (فاز ۳، مورد ۲۹) — `ThirdPartyApiService`, `com.itextpdf.**`, `coil.**`, `io.coil.**`, `vico.**` |
| ~~مسیر FileProvider برای PDF~~ | `res/xml/file_path.xml` | ✅ حذف شد (فاز ۳، مورد ۲۹) — ورودی `documents` از `file_path.xml` حذف شد؛ `updates/` (زنده، برای `UpdateManager`) دست‌نخورده ماند |
| ~~منابع layout افقی/تبلت~~ | `res/values-land`, `values-w600dp`, `values-w1240dp` | ✅ حذف شد (فاز ۳، مورد ۲۹) — طبق تصمیم کاربر (`MainActivity` قفل portrait دارد) |

### بدهی ریپازیتوری

> ✅ **رفع شد (فاز ۳، مورد ۳۰):** جزئیات اجرا زیر جدول.

| مورد | اندازه | وضعیت |
|------|--------|--------|
| ~~`graphify-out/`~~ | ۹۱ مگابایت، ۴۷ فایل tracked | ✅ فقط از ردیابی گیت خارج شد (`git rm --cached` + `/graphify-out/` در `.gitignore`)؛ طبق تصمیم کاربر تاریخچه‌ی گیت پاک‌سازی **نشد** تا نیازی به force-push/rewrite نباشد |
| ~~`java_pid23540.hprof`~~ | ۱٫۶۹ گیگابایت | ✅ از قبل روی دیسک وجود نداشت (پاک شده بود) |
| ~~`php_server_info_*.txt`~~ | ۳۵ کیلوبایت | ✅ حذف شد از دیسک (از قبل gitignore بود) |
| ~~`whimsical-beaming-starfish.md`~~ | ۷ کیلوبایت | ✅ حذف شد — بررسی محتوا نشان داد پلن یک تسک نامرتبط قدیمی (استانداردسازی کامنت‌ها) است، نه بخشی از پروژه |
| `.claude/worktrees/eager-curran-a89d37/` | یک checkout کامل | از قبل روی دیسک وجود نداشت |
| ~~`DEEP_CODE_REVIEW.md`~~ | حذف‌شده در working tree | ✅ طبق تصمیم کاربر: فایل بازگردانده **نشد**؛ همه‌ی ۱۴ ارجاع کد/مستندات (۶ کامنت کد + ۸ فایل build/CI/مستندات دیگر) به `DEEP_CODE_AUDIT.md` به‌روزرسانی شدند |

**یادداشت‌های اجرا:**

- تصمیم کاربر: پاک‌سازی `graphify-out/` فقط از این به بعد (بدون بازنویسی تاریخچه‌ی گیت — ریسک force-push روی `main` پذیرفته نشد).
- تصمیم کاربر: ارجاع‌های `DEEP_CODE_REVIEW.md` به‌جای بازگرداندن فایل، به `DEEP_CODE_AUDIT.md` به‌روزرسانی شدند. چون شماره‌گذاری فاز/مورد بین دو گزارش یکی نیست، اعداد قدیمی (`Phase1.3`, `Top20 #8`, ...) حذف و فقط ارجاع به نام فایل جدید نگه داشته شد تا گمراه‌کننده نباشد.
- تأیید شد: `php -l` روی همه‌ی فایل‌های PHP/`.htaccess` تغییریافته بدون خطا، و `./gradlew :app:compileDebugKotlin :feature:cargo:compileDebugKotlin` موفق (exit 0).

---

## Production Readiness

| معیار | وضعیت | توضیح |
|-------|:-----:|-------|
| **Security** | ⚠️ | پایه‌ی قوی (pinning، هش توکن، rotation، bcrypt، CSRF، prepared statements) اما یک DoS بحرانی و رازهای کلاینت قابل استخراج |
| **Stability** | ⚠️ | `Secrets.loadLibrary` بدون محافظ؛ race در `AuthSession`؛ `catch` سراسری که «دستکاری‌شده» نشان می‌دهد |
| **Crash handling** | ✅ | `CrashReporter` سفارشی با ارسال best-effort و سقف حجم لاگ |
| **Logging** | ⚠️ | `Logger` مرکزی با ماسک‌گذاری وجود دارد، اما مسیرهای دور زننده (`SessionService::logActivity`) و لاگ محتوای پیام در debug |
| **Performance** | ❌ | اسپلش ۳٫۸ ثانیه‌ای، معماری polling، بدون صفحه‌بندی، محاسبات بدون remember |
| **Configuration** | ✅ | `.env` با `.env.example` مستند؛ `Config::env()` متمرکز؛ fail-closed در نبود مقدار |
| **Environment separation** | ⚠️ | debug/release در Gradle تفکیک شده، اما بررسی‌های امنیتی گارد `BuildConfig.DEBUG` ندارند |
| **Debug/Release** | ✅ | `applicationIdSuffix = ".debug"`، `versionNameSuffix = "-debug"`، لاگ فقط در debug |
| **ProGuard/R8** | ⚠️ | پیکربندی عالی، اما **هرگز در CI اجرا نمی‌شود** |
| **Obfuscation** | ✅ | full mode + repackage + دیکشنری + آرشیو mapping خودکار (`archiveReleaseMapping`) |
| **API security** | ⚠️ | auth/permission در Router متمرکز، اما گیت نسخه قابل دور زدن و بدون rate limit عمومی |
| **Database security** | ✅ | prepared statement در ۱۰۰٪ کوئری‌ها، allow-list ستون، FK با CASCADE مناسب |
| **Backup** | ❌ | هیچ راهبرد پشتیبان‌گیری از دیتابیس در ریپازیتوری مستند نشده |
| **Monitoring** | ❌ | `health` endpoint و `health_monitor.php` وجود دارند اما به هیچ سرویسی وصل نیستند |
| **Testing** | ❌ | ۱٫۶٪ پوشش؛ مسیر release در CI تست نمی‌شود |

### حکم

**آماده‌ی تولید نیست، اما نزدیک است.** موانع واقعی انتشار فقط چهار موردند (Phase 1 زیر). بقیه‌ی یافته‌ها بدهی هستند که باید برنامه‌ریزی شوند، نه بلاکر.

---

## Prioritized Action Plan

### Phase 1 — Immediate (قبل از انتشار بعدی · تخمین ۲–۳ روز)

> **وضعیت (۲۰۲۶-۰۸-۲۱):** ۶ مورد از ۸ مورد اعمال شد. جزئیات و دو استثنا زیر جدول.

| # | اقدام | فایل | تلاش | وضعیت |
|---|-------|------|------|:-----:|
| ۱ | حذف `sleep()` از `LoginAttemptLimiter` و خارج کردن `SecurityAlerter` از مسیر درخواست | `LoginAttemptLimiter.php` · `SecurityAlerter.php` | Low | ✅ اعمال شد |
| ۲ | rate-limiter اختصاصی و غیرمسدودکننده برای `diagnostics/crash` | `DiagnosticsController.php` · `CrashReportRateLimiter.php` (جدید) | Low | ✅ اعمال شد |
| ۳ | رد کردن درخواست بدون هدر `X-App-Version` | `MinVersionGate.php` · `AuthenticatesRequests.php` | Low | ✅ اعمال شد |
| ۴ | افزودن گیت R8/ProGuard به CI | `.github/workflows/ci.yml` | Low | ✅ اعمال شد — رجوع به تصحیح زیر |
| ۵ | کاهش `SPLASH_MIN_DURATION` | `StartupViewModel.kt` | Low | ✅ اعمال شد (به ۱۵۰۰، نه ۶۰۰ — رجوع به یادداشت زیر) |
| ۶ | چرخش `API_KEY` و `LICENSE_KEY` طبق `scripts/ROTATE_SECRETS.md` | `secrets.cpp` + `.env` سرور | Low | ⛔ انجام نشد — نیازمند دسترسی سرور تولید |
| ۷ | اصلاح کلیدهای JOIN ناقص در `AnalyticsController` و `ShipService` | `AnalyticsController.php` · `ShipService.php` | Low | ✅ اعمال شد |
| ۸ | قفل کردن IP روی `PermissionManager.php` و `file_manager.php` | `PHP/.htaccess` | Low | ⚠️ فقط قالب TODO — رجوع به یادداشت زیر |

**یادداشت‌های اجرا:**

- **تصحیح مورد ۴:** بررسی دقیق‌تر `.github/workflows/ci.yml` نشان داد `lintDebug`/`lintRelease` از قبل در CI وجود داشتند (خواندن اولیه‌ی من با `head -80` این بخش از فایل را قطع کرده بود — ادعای «lint هرگز اجرا نمی‌شود» در گزارش اصلی نادرست بود). چیزی که واقعاً غایب بود فقط `assembleRelease` (اجرای واقعی R8/shrinking) بود؛ همان اضافه شد.
- **مورد ۵:** به‌جای ۶۰۰ میلی‌ثانیه‌ی پیشنهادی گزارش، به ۱۵۰۰ میلی‌ثانیه کاهش یافت — طبق تصمیم کاربر، چون این مقدار در سه کامیت اخیر عمداً برای برندینگ/نمایش لوگو تنظیم شده بود (۴۵۰۰→۴۲۰۰→۳۸۰۰).
- **مورد ۶:** چرخش راز نیازمند تولید مقدار جدید + استقرار هم‌زمان سمت کلاینت (`secrets.cpp`) و سمت سرور (`.env` تولید) است. بدون دسترسی به سرور تولید و بدون هماهنگی زمان انتشار، انجام یک‌طرفه‌ی این کار می‌تواند auth را بشکند. اقدام دستی طبق `scripts/ROTATE_SECRETS.md` لازم است.
- **مورد ۸:** به‌جای حدس زدن IP مدیریتی (که در صورت اشتباه می‌توانست خود ادمین را هم قفل کند)، یک بلوک `<FilesMatch>` کامنت‌شده با `TODO` در `PHP/.htaccess` اضافه شد — طبق انتخاب کاربر. فعال‌سازی واقعی نیازمند جایگزینی `<ADMIN_IP_OR_CIDR>` و تست دستی است.
- **رگرسیون کشف‌شده حین پیاده‌سازی مورد ۱:** طرح اولیه (جایگزینی `sleep()` با یک پنجره‌ی backoff که در `isLocked()` هم بررسی شود) با تست موجود `testUsernameIsNotLockedBeforeReachingMaxAttempts` تناقض داشت — آن تست صراحتاً تضمین می‌کند زیر سقف `MAX_USER_ATTEMPTS`، کاربر قفل نشود. راه‌حل نهایی: `sleep()` کاملاً حذف شد بدون معرفی مکانیزم backoff جدید؛ `isLocked()` دقیقاً همان قرارداد قبلی (فقط بر پایه‌ی شمارنده) را حفظ می‌کند. هر ۹۳ تست PHPUnit پروژه (نه فقط این فایل) پس از تغییر سبز هستند.

### Phase 2 — High Priority (۲–۳ هفته)

> **وضعیت:** مورد به مورد و با تأیید کاربر پیش می‌رود.

| # | اقدام | تلاش | وضعیت |
|---|-------|------|:-----:|
| ۹ | جایگزینی `LoadingNotificationService` با `PeriodicWorkRequest` | Medium | ✅ اعمال شد |
| ۱۰ | `remember`/`derivedStateOf` روی خطوط لوله‌ی مجموعه در `ActiveQuotasContent`، `CargoCounterScreen`، `ActiveQuotasGroupedComponents` | Low | ✅ اعمال شد |
| ۱۱ | صفحه‌بندی cursor-based روی endpointهای تحلیلی و گزارش | Medium | ⚠️ دامنه کاهش یافت — فقط LIMIT محافظتی |
| ۱۲ | `targetSdk = 36` + تست روی Android 15/16 | Medium | ⏭️ فعلاً رد شد (نیازمند دستگاه واقعی) |
| ۱۳ | یکسان‌سازی رمزگذاری خروجی در `CargoController` (حذف `htmlspecialchars` دوگانه) + migration داده | Medium | ✅ اعمال شد (اسکریپت migration برای اجرای دستی) |
| ۱۴ | تبدیل `AnimationManager` به snapshot state + خواندن تنظیم سیستم در `Application.onCreate` | Low | ✅ اعمال شد |
| ۱۵ | رفع race در `AuthSession` هنگام cold start | Low | ✅ اعمال شد |
| ۱۶ | ایمن‌سازی `System.loadLibrary` + افزودن `armeabi-v7a` یا پیام خطای صریح | Low | ✅ اعمال شد (پیام خطا، بدون `armeabi-v7a`) |
| ۱۷ | ابطال نشست فقط برای تغییرات امنیتی در `UserService::updateUser` | Low | ✅ اعمال شد |
| ۱۸ | یکسان کردن `sql_mode` به `STRICT_TRANS_TABLES` در `Database::getMysqliConnection` | Low | ✅ اعمال شد |
| ۱۹ | حذف افشای `userType` و پیام متمایز در `checkSession` | Low | ✅ اعمال شد |
| ۲۰ | تست‌های Phase 1 بخش Testing: `UpdateManagerTest`, `CryptoManagerTest`, `AuthControllerTest` | High | ✅ اعمال شد (`CryptoManagerTest` فقط compile-verified) |

**یادداشت‌های اجرای مورد ۲۰:**

- **`UpdateManagerTest`** (`feature/update/src/test/.../UpdateManagerTest.kt`, JVM unit test) — ۱۸ تست روی `isTrustedDownloadUrl` (تطبیق دقیق دامنه، subdomain، ترفندهای suffix/prefix، رد `http://`، ورودی نامعتبر)، `verifyFileSha256` (تطبیق/عدم‌تطبیق هش، بی‌حساسیت به بزرگ/کوچک، trim فاصله، فایل مفقود) و `compareVersions` (برابر/جدیدتر/قدیمی‌تر/کوتاه‌تر/کاراکتر غیرعددی). چون `isTrustedDownloadUrl` داخلاً به `Constants.BASE_URL` (که از طریق JNI به کتابخانه‌ی نیتیو `Secrets` می‌رسد و در JVM ساده در دسترس نیست) وابسته بود، پارامتر `trustedBaseUrl` با مقدار پیش‌فرض همان `Constants.BASE_URL` اضافه شد (بدون تغییر رفتار در production). تأیید شد: هر ۱۸ تست سبز (`gradlew :feature:update:testDebugUnitTest`).
- **`CryptoManagerTest`** (`app/src/androidTest/.../CryptoManagerTest.kt`, instrumented test) — ۹ تست روی چرخه‌ی رمز/رمزگشایی، تصادفی‌بودن IV، رشته‌ی خالی، ورودی خراب/دستکاری‌شده (تگ GCM)، اشتراک کلید بین نمونه‌ها، فرمت Base64. چون به AndroidKeyStore واقعی نیاز دارد، به‌صورت `androidTest` نوشته شد نه unit test. **محدودیت مهم:** فقط با `gradlew :app:compileDebugAndroidTestKotlin` کامپایل‌شده تأیید شد؛ در این محیط دستگاه/امولاتور اندروید در دسترس نیست، پس این ۹ تست هرگز واقعاً اجرا نشده‌اند — اجرای آن‌ها روی دستگاه واقعی برعهده‌ی کاربر است.
- **`AuthControllerTest`** (`PHP/tests/Unit/Controllers/AuthControllerTest.php`) — ۶ تست روی رد متد غیر-POST، ورودی ناقص، قفل‌شدن پس از تلاش‌های ناموفق، رد اعتبارنامه‌ی نادرست، تعارض ورود همزمان از دستگاه دوم (۴۰۹)، و ورود موفق با توکن‌ها. برای این تست، `AuthController` یک constructor با پارامترهای اختیاری تزریق‌پذیر گرفت (هم‌راستا با الگوی موجود در `SessionService`/`CargoController`؛ فراخوانی‌های production بدون آرگومان همچنان کار می‌کنند). چون `Response::json()`/`Response::error()` با `exit` پاسخ می‌دهند (که غیرقابل catch است و حتی زیر `@runInSeparateProcess` نتیجه‌ی تست را از بین می‌برد)، یک seam تستی کوچک اضافه شد: `PHP/src/Core/Response.php` اکنون زیر ثابت `TESTING_MODE` (فقط از `tests/bootstrap.php` تعریف می‌شود) به‌جای `exit`، یک `ResponseSentException` قابل‌catch پرتاب می‌کند؛ رفتار production بدون تغییر می‌ماند. `LoginAttemptLimiter`/`PermissionService` (هر دو `final`، غیرقابل mock) به‌صورت نمونه‌ی واقعی استفاده شدند — هیچ‌کدام به DB واقعی نیاز ندارند. تأیید شد: `php -l`، PHPStan level 5 بدون خطا، و کل مجموعه‌ی PHPUnit (۹۹ تست، شامل ۶ تست جدید) سبز.

### Phase 3 — Medium Priority (۱–۲ ماه)

> **وضعیت:** مورد به مورد و با تأیید کاربر پیش می‌رود.

| # | اقدام | تلاش | وضعیت |
|---|-------|------|:-----:|
| ۲۱ | Repository برای هر فیچر؛ حذف فراخوانی مستقیم `ApiServiceV2` از ViewModelها | High | ⏳ در انتظار |
| ۲۲ | تجزیه‌ی `CargoViewModel` — ادغام ۱۰ StateFlow، استخراج UseCase، انتقال کش به Repository | High | ⏳ در انتظار |
| ۲۳ | هم‌راستا کردن نام پکیج‌ها با ماژول‌ها (رفع ۷ split package) | Medium | ⚠️ دامنه کاهش یافت — فقط ۶ پکیج کوچک؛ `com.atk.atk_cargo.api` (بزرگ‌ترین، سه‌طرفه) طبق تصمیم کاربر باقی ماند |
| ۲۴ | انتقال `ManageReportsScreen` به `:feature:reports` و `core/ui/components` به `:core:designsystem` | Medium | ✅ اعمال شد |
| ۲۵ | حذف `AuthenticatesRequests` و اتکا به هویت پاس‌شده از Router | Medium | ✅ اعمال شد |
| ۲۶ | حذف متدهای pass-through `AppApiController` | Medium | ✅ اعمال شد |
| ۲۷ | یکسان‌سازی استک HTTP روی یک `OkHttpClient` مشترک | Medium | ✅ اعمال شد |
| ۲۸ | یکسان‌سازی معنای کدهای وضعیت HTTP (پشت گیت نسخه) | Medium | ✅ اعمال شد |
| ۲۹ | حذف کل کد مرده‌ی فهرست‌شده در بخش Technical Debt | Low | ✅ اعمال شد |
| ۳۰ | پاک‌سازی ریپازیتوری: `graphify-out/` از گیت، `.hprof` از دیسک | Low | ✅ اعمال شد |
| ۳۱ | گسترش PHPStan به فایل‌های ریشه + baseline + رفتن به level 7 | Low | ✅ اعمال شد |
| ۳۲ | وصل کردن `health_monitor.php` به cron + هشدار تلگرام | Low | ⏭️ فعلاً کنار گذاشته شد — طبق کاربر، بعداً با رویکرد متفاوت اجرا می‌شود |
| ۳۳ | ماژول Koin به‌ازای هر فیچر | Medium | ✅ اعمال شد |

**یادداشت‌های اجرای مورد ۲۹:**

- حذف کامل شامل: متد `ChatDao.markAsRead` (Kotlin، بدون فراخوان)؛ روت‌ها و متدهای PHP برای `chat/messages/{id}/read` (`ChatController::markAsRead`)، `chat/unread-count` (`ChatController::getUnreadCount`)، `utility/check-password` (`UtilityController::checkPassword` + import بی‌استفاده‌ی `PasswordGateService`) و `analytics/quota-remaining` (`AnalyticsController::handleQuotaRemaining` + دو متد کمکی `getActiveQuotasRemaining`/`getShipQuotasRemaining` که فقط از همان‌جا صدا زده می‌شدند)؛ فایل‌های shim مستقل `PHP/quota_remaining_api.php` و `PHP/update_fcm_token.php`؛ زنجیره‌ی کامل FCM (`UserController::updateFcmToken` + روت `users/fcm-token`) طبق تصمیم کاربر؛ چهار قانون ProGuard مرده (`ThirdPartyApiService`, `com.itextpdf.**`, `coil.**`/`io.coil.**`, `vico.**`)؛ ورودی `documents` در `file_path.xml` (بدون هیچ کد تولید PDF در پروژه)؛ و سه پوشه‌ی منبع `values-land`/`values-w600dp`/`values-w1240dp` طبق تصمیم کاربر.
- **بررسی جانبی:** قبل از حذف `markAsRead`/`getUnreadCount`، تأیید شد که جدول `admin_chat_reads` و منطق `read_by_names`/`is_read_by_me` در `getMessages`/`sendMessage` هنوز زنده‌اند و حذف نشدند — فقط مسیر توقفی endpoint صریح «علامت‌گذاری خوانده‌شده» مرده بود، نه کل مکانیزم ردیابی خواندن.
- ستون `Users.fcm_token` از قبل در `schema.sql` تعریف نشده بود (drift ردیابی‌نشده با production)، پس migration لازم نبود.
- تأیید شد: `php -l` روی همه‌ی فایل‌های PHP تغییریافته بدون خطا، و `./gradlew :core:database:compileDebugKotlin :app:compileDebugKotlin` موفق (exit 0). به دلیل نبود `vendor/bin/phpstan`/`phpunit` نصب‌شده در این محیط، PHPStan/PHPUnit اجرا نشدند.

**یادداشت‌های اجرای مورد ۳۱:**

- `composer install` برای اولین‌بار در این محیط اجرا شد (تا امروز `vendor/bin/phpstan`/`phpunit` هرگز نصب نشده بودند، برخلاف تصور اولیه‌ی «هیچ‌کدام نصب نیستند» در یادداشت مورد ۲۹).
- `paths` در `phpstan.neon` گسترش یافت تا `scripts/`, `User/`, `config/` و ۱۰ فایل ریشه‌ی `PHP/` (`PermissionManager.php`, `SessionManager.php`, `check_signature.php`, `check_update.php`, `export_schema.php`, `file_manager.php`, `get_csrf_token.php`, `get_license_info.php`, `jdf.php`, `update_config.php`, `validate_license.php`) را هم پوشش دهد — قبلاً فقط `src`/`api`/`Lic` آنالیز می‌شدند.
- **رگرسیون کشف‌شده:** با نصب واقعی وابستگی‌ها، قانون `ignoreErrors` قبلی برای `UtilityController.php` (`Result of || is always true`) دیگر با خروجی واقعی PHPStan match نمی‌شد (به‌احتمال زیاد چون نسخه‌ی نصب‌شده‌ی phpstan/nikic-php-parser دیگر نوع `UPDATE_CHECK_API_KEY` را به رشته‌ی لفظی `''` باریک نمی‌کند) و باعث خطای «unmatched ignore» می‌شد. طبق همان اصلی که در نکته‌ی فاز۳ #۲۱ مستند شده بود، قانون حذف و توضیح در کامنت به‌روزرسانی شد.
- در گسترش دامنه، دو خطای واقعی (نه third-party) پیدا شد و مستقیماً رفع شد: `update_config.php` تگ پایانی `?>` + فاصله‌ی خالی بعدش داشت (`Unreachable statement` + هشدار whitespace) — هر دو با حذف `?>` رفع شدند.
- ۴۶ خطای باقی‌مانده در سطح ۵، همگی در `jdf.php` (کتابخانه‌ی تاریخ جلالی شخص‌ثالث با هدر لایسنس GNU/LGPL که طبق یادداشت‌های قبلی گزارش نباید محتوایش دست بخورد) — این‌ها به baseline رفتند، نه اصلاح در کد.
- سطح از ۵ به ۷ افزایش یافت و بلافاصله `--generate-baseline` اجرا شد: **۴۷۴ خطا در ۴۷ فایل** (`phpstan-baseline.neon`، شامل همان ۴۶ خطای `jdf.php` + خطاهای جدید سطح ۶/۷ در کد پروژه مثل `Lic/_guard.php`/`Lic/export.php`) baseline شدند تا هیچ‌کدام بلاک نشوند اما کد جدید از این پس زیر سطح ۷ واقعی چک شود. `phpstan.neon` با `includes: [phpstan-baseline.neon]` آن را بارگذاری می‌کند.
- CI (`ci.yml`) به‌روزرسانی شد: نام مرحله از «PHPStan (level 5)» به «PHPStan (level 7)» تغییر کرد و کامنت بالای آن با وضعیت واقعی (baseline به‌جای ignoreErrors) هم‌راستا شد.
- تأیید شد: `vendor/bin/phpstan analyse` با پیکربندی نهایی «No errors» می‌دهد؛ کل مجموعه‌ی PHPUnit (۹۹ تست، ۱۹۵ assertion) سبز است.

**یادداشت‌های اجرای مورد ۲۶:**

- `AppApiController` دو بخش کاملاً جدا داشت: یک dispatcher قدیمی مرده (`handle()`، از هیچ route صدا زده نمی‌شد — طبق کامنت خودِ فایل) و ۲۲ متد pass-through که واقعاً از `routes/api_v2.php` صدا زده می‌شدند (delegate خالص به `ShipService`/`QuotaService`). هر دو حذف شدند و **کل فایل `AppApiController.php` پاک شد** — بعد از حذف delegateها چیزی برای نگه‌داشتن کلاس باقی نمی‌ماند.
- ۱۷ محل فراخوانی در `routes/api_v2.php` مستقیماً به `(new ShipService())->...`/`(new QuotaService())->...` تغییر کردند، هم‌راستا با الگوی «Direct passthrough» که بقیه‌ی این فایل (مثل `CargoController`) از قبل استفاده می‌کند.
- برای ۶ اکشن نوشتنی (`editQuota`, `updateQuotaPercentage`, `toggleQuotaStatus`, `updateQuotaPercentageRestriction`, `deleteQuota`, `updateTemporaryTonnage`) که برای `AuditLogger` به نام کاربر نیاز دارند: به‌جای اینکه closureهای جدید دوباره یک بار دیگر نشست را (مثل متد حذف‌شده‌ی `AppApiController`) validate کنند، از پارامتر سوم closure که خودِ `Router::dispatch` از قبل با `ApiAuthGate::requireAuthenticated` پر کرده استفاده شد — این **حذف یک اعتبارسنجی نشست تکراری** در هر درخواست نوشتنی است، نه فقط جابه‌جایی کد؛ رفتار نهایی (کاربر تأییدشده‌ی یکسان در لاگ) دقیقاً همان است.
- `AppApiController::sendCacheableJsonResponse` (کمکی ETag/304 برای ۳ route: `ships`, `ships/{shipName}`, `ships/{shipName}/quotas`) به `Response::cacheableJson()` منتقل شد تا در دسترس route handlerهای مستقل هم باشد.
- کامنت‌های حالا نادرست در `QuotaService.php` («اکنون آن کنترلر فقط delegate می‌کند») و `MicroCache.php` («مشترک بین AppApiController و CargoController») به‌روزرسانی شدند.
- تأیید شد: `php -l` روی همه‌ی فایل‌های تغییریافته بدون خطا؛ `php -r` بارگذاری کامل `routes/api_v2.php` + ساخت `Router` بدون خطای fatal (۵۷ route)؛ `vendor/bin/phpstan analyse` بعد از regenerate کردن baseline (برای حذف ۱۹ رکورد یتیم مربوط به `AppApiController.php`، از ۴۷۴ به ۴۶۱ خطا) «No errors» می‌دهد؛ کل مجموعه‌ی PHPUnit (۹۹ تست) سبز. تست end-to-end زنده روی دیتابیس واقعی در این محیط ممکن نبود (بدون MySQL محلی، مثل بقیه‌ی این ممیزی).

**یادداشت‌های اجرای مورد ۲۸:**

- طبق تصمیم کاربر: آستانه‌ی نسخه `4.1.0` انتخاب شد (نسخه‌ی فعلی هنگام شروع کار ۴.۰.۱ بود) و کلاینت اندروید هم‌زمان در همین تغییر پچ شد، نه در یک تغییر جدا.
- `Response::versionGatedJson($body, $legacyHttpCode, $newHttpCode)` به `PHP/src/Core/Response.php` اضافه شد — هدر `X-App-Version` را با ثابت `Response::HTTP_CODES_MIN_APP_VERSION = '4.1.0'` مقایسه می‌کند؛ کلاینت‌های بدون این هدر یا با نسخه‌ی پایین‌تر همچنان کد قدیمی (۲۰۰) می‌گیرند.
- `AuthController::login()` (۳ شاخه: قفل‌شدن حساب → ۴۲۹، رمز/نام‌کاربری اشتباه → ۴۰۱ با `code: invalid_credentials`، دسترسی به بخش درخواستی رد شد → ۴۰۳) و `AuthController::checkSession()` (نشست نامعتبر → ۴۰۱ با `code: session_invalid`) به این گیت وصل شدند. `login()`ی conflict-session (۴۰۹) و `refresh()` از قبل کد صحیح داشتند و دست‌نخورده ماندند.
- `ChatController`: پنج بررسی مجوز/مالکیت در `sendMessage`/`editMessage`/`deleteMessage` (که قبلاً همیشه ۲۰۰ برمی‌گرداندند) به همین گیت وصل شدند (→ ۴۰۳)، هم‌راستا با ۴۰۳ صریح و از قبل بدون‌گیت `getMessages` برای همان شرط — **`getMessages` عمداً دست‌نخورده ماند** چون رفتارش از قبل همان هدف نهایی بود و گیت کردنش یک رگرسیون رفتاری برای کلاینت‌های قدیمی می‌ساخت که وجود نداشت.
- سمت اندروید (هم‌زمان در همین تغییر): `versionName`/`versionCode` در `app/build.gradle.kts` به `4.1.0`/`12` افزایش یافت (منبع مقدار هدر `X-App-Version` که از `PackageInfo.versionName` خوانده می‌شود). بررسی مسیرهای مصرف‌کننده نشان داد بیشتر کد از قبل آماده بود:
  - `SessionValidator.kt`/`StartupViewModel.kt::checkUserSessionAsync` هر دو فقط شرط `isSuccessful && success==true` را چک می‌کنند و در غیر این صورت بدون توجه به کد دقیق به تلاش رفرش دستی می‌روند — رفتارشان برای ۴۰۱ و ۲۰۰-با-success:false یکسان است، نیازی به تغییر نداشتند (فقط کامنت قدیمی `SessionValidator.kt` که می‌گفت «هرگز ۴۰۱ نمی‌شود» به‌روزرسانی شد).
  - `AuthRepositoryImpl.kt` از قبل یک `when(response.code())` آماده برای ۴۰۱/۴۰۳/۵۰۰ داشت (احتمالاً پیش‌بینی‌شده برای همین تغییر) — فقط شاخه‌ی ۴۲۹ (که تا امروز هرگز از سرور نمی‌رسید) اضافه شد.
  - `ChatRepository.kt` تنها جای واقعاً ناقص بود: `sendMessage`/`editMessage`/`deleteMessage` روی خطا فقط `response.body()?.message` را می‌خواندند که برای کد غیر-۲xx همیشه `null` است (Retrofit پیام را در `errorBody()` می‌گذارد نه `body()`) — بدون اصلاح، پیام واقعی سرور («دسترسی غیرمجاز») با یک پیام عمومی جایگزین می‌شد. متد کمکی `extractErrorMessage` (با `org.json.JSONObject`، بدون افزودن وابستگی جدید چون این ماژول Gson ندارد) اضافه شد.
- تست‌های جدید در `AuthControllerTest.php` (۶ تست: قفل‌شدن/رمز اشتباه/نشست نامعتبر هرکدام هم با نسخه‌ی جدید هم بدون هدر، + یک تست نشست معتبر) اضافه شدند. `ChatController` قابل unit-test نبود چون سازنده‌اش مستقیم به mysqli واقعی وصل می‌شود (بدون DB در این محیط)، هم‌راستا با محدودیت‌های تست قبلی این پروژه.
- **رگرسیون کشف‌شده حین تست:** پس از چند بار اجرای کامل PHPUnit در همین نشست، شمارنده‌ی fallback فایلی `LoginAttemptLimiter` برای IP `127.0.0.1` در sandbox موقت تست (`sys_get_temp_dir()/atk_cargo_phpunit`) از سقف ۵۰ عبور کرد و باعث شکست کاذب ۴ تست غیرمرتبط شد. این یک ضعف شناخته‌شده‌ی همان rate-limiter فایل‌محور است (نه چیزی که این تغییر ایجاد کرده)؛ پاک کردن پوشه‌ی sandbox موقت رفعش کرد.
- تأیید شد: `php -l`/`vendor/bin/phpstan analyse` (بعد از regenerate باسلاین برای ۱ خطای جدید در `Response::versionGatedJson`، از ۴۶۱ به ۴۶۲) هر دو تمیز؛ کل PHPUnit (۱۰۵ تست، شامل ۶ تست جدید) سبز؛ `./gradlew :feature:chat:compileDebugKotlin :feature:auth:compileDebugKotlin :core:network:compileDebugKotlin :app:compileDebugKotlin` موفق. تست end-to-end زنده (کلاینت واقعی ↔ سرور واقعی) در این محیط ممکن نبود.

**یادداشت‌های اجرای مورد ۲۷:**

- `core/network/.../HttpStack.kt` اضافه شد: یک `OkHttpClient` پایه با `ConnectionPool(10, 5, MINUTES)` و timeoutهای معقول عمومی (۱۰/۳۰/۳۰ ثانیه) + `retryOnConnectionFailure`. هر مصرف‌کننده با `HttpStack.shared.newBuilder()` فقط تفاوت خودش را روی همین یک pool مشترک اعمال می‌کند — دقیقاً الگوی پیشنهادی گزارش.
- چهار مصرف‌کننده به این pool وصل شدند:
  - `RetrofitClient.kt` — بدون تغییر رفتار (timeoutهای پایه‌ی `HttpStack.shared` از قبل با ثابت‌های همین کلاینت یکسان بودند)؛ interceptorها/authenticator/کش دیسک دست‌نخورده روی `newBuilder()` اعمال شدند.
  - `TokenRefresher.kt` — عمداً **بدون** `TokenAuthenticator`/`headersInterceptor` مشترک، چون این کلاینتِ خودِ منطق رفرش است؛ گرفتن authenticator مشترک می‌توانست حلقه‌ی رفرش-روی-رفرش بسازد.
  - `UpdateManager.kt` (ماژول `:feature:update`) — `Dispatcher` اختصاصی (`maxRequestsPerHost=10`) حفظ شد چون ۴ chunk هم‌زمان به همان هاست دانلود می‌شوند؛ `ConnectionPool` جداگانه‌ی قبلی حذف و به pool مشترک واگذار شد.
  - `SecurityVerifier.kt` (ماژول `:app`) — سه فراخوانی `HttpURLConnection` خام (`authenticateSignatureWithServer`, `fetchLicenseInfo`, `fetchLicenseValidation`) به OkHttp مهاجرت کردند؛ همان `BUFFER_DURATION=8000ms` برای connect/read حفظ شد و منطق «فقط پاسخ موفق پردازش شود» عیناً با `response.isSuccessful` بازتولید شد (بدون دست‌بردن در باگ نامرتبط «۴۰۳ لایسنس به خطای شبکه ترجمه می‌شود» که در بخش دیگری از گزارش، نه این مورد، مستند است).
- certificate pinning به Network Security Config (سطح پلتفرم) متکی است، نه به کد کلاینت خاص — طبق تحلیل خود گزارش، مهاجرت `SecurityVerifier` از `HttpURLConnection` به OkHttp تأثیری روی pinning ندارد.
- تأیید شد: `./gradlew :core:network:compileDebugKotlin :feature:update:compileDebugKotlin :app:compileDebugKotlin` و کل پروژه (`compileDebugKotlin`) موفق؛ `:feature:update:testDebugUnitTest` (۱۸ تست `UpdateManagerTest`) سبز. تست end-to-end زنده (handshake واقعی) در این محیط ممکن نبود.

**یادداشت‌های اجرای مورد ۲۴:**

- شش فایل `core/ui/components/*.kt` (`ConfirmationDialog`, `DateRangePicker`, `EmptyState`, `LoadingOverlay`, `SearchBar`, `SnackbarMessage`) با `git mv` به `:core:designsystem` منتقل شدند — **بدون تغییر package** (`com.atk.atk_cargo.core.ui.components` از قبل هم آنجا وجود داشت؛ `StatisticsCard`/`ErrorState` قبلاً منتقل شده بودند)، پس هیچ import‌ای در بقیه‌ی پروژه نیاز به تغییر نداشت — این پکیج بین `:app` و `:core:designsystem` split شده بود و حالا یکپارچه است.
- به `core/designsystem/build.gradle.kts` دو وابستگی اضافه شد: `core:network` (چون `SnackbarMessage.kt` به `MessageType` نیاز دارد) و `kotlinx-coroutines-android` (چون همان فایل از `delay` استفاده می‌کند) — قبلاً هیچ‌کدام را نداشت چون این فایل‌ها هرگز در این ماژول کامپایل نشده بودند.
- `ManageReportsScreen.kt` (۱۰۳۴ خط) با `git mv` به `:feature:reports/.../presentation/` منتقل و package به `com.atk.atk_cargo.feature.reports.presentation` تغییر کرد. سه وابستگی به کد مخصوص `:app` که مانع انتقال بودند رفع شدند:
  - `navController.navigateToHome()` (از `feature:home`) — چون `feature:home` از قبل به `feature:reports` وابسته است، افزودن وابستگی معکوس یک چرخه‌ی ماژول می‌ساخت. پارامتر ورودی از `navController: NavController?` به `onSessionInvalid: (() -> Unit)?` تغییر کرد (همان الگوی `onSessionInvalid` که `CargoCounterOperationScreen` در `MainScreen.kt` از قبل استفاده می‌کند)؛ فراخوان در `:app` حالا `onSessionInvalid = { navController.navigateToHome() }` پاس می‌دهد.
  - `Intent(context, MainActivity::class.java)` (کلاس مخصوص `:app`) — با `context.packageManager.getLaunchIntentForPackage(context.packageName)` جایگزین شد که بدون وابستگی به کلاس فعالیت اصلی، همان اثر (راه‌اندازی مجدد اپ) را دارد؛ فقط وقتی `onSessionInvalid` هم `null` باشد اجرا می‌شود (fallback نهایی).
  - `koinInject<UserPreferencesManager>()` (کلاس concrete مخصوص `:app`) — با دو تزریق جدا از اینترفیس‌های مرزی موجود جایگزین شد: `koinInject<TokenStore>()` (برای `validateServerSession`) و `koinInject<UserPreferencesStore>()` (برای `.permissions`)، هر دو از قبل در `AppModule.kt` به همان سینگلتون `UserPreferencesManager` وصل بودند — تزریق مستقیم اینترفیس به‌جای کلاس concrete، نه یک workaround.
  - در همین مسیر یک import اشتباه/میراثی هم برطرف شد: `com.atk.atk_cargo.api.CargoInfo` در واقع یک `typealias` محلی `:app` بود (در `DataModel.kt`) به سمت `com.atk.atk_cargo.data.model.CargoInfo` واقعی در `core:network`؛ حالا مستقیم از مسیر canonical import می‌شود.
- پوشه‌های خالی‌شده‌ی `app/.../ui/screens`، `app/.../core/ui/components` و `app/.../ui` (که بعد از این دو انتقال هیچ فایلی نداشتند) حذف شدند.
- تأیید شد: `./gradlew :core:designsystem:compileDebugKotlin :feature:reports:compileDebugKotlin :app:compileDebugKotlin` و کل پروژه (`compileDebugKotlin`) موفق؛ `:feature:reports:testDebugUnitTest`/`:core:designsystem:testDebugUnitTest`/`:app:testDebugUnitTest` سبز (بدون تست UI موجود برای این صفحه‌ی خاص، مطابق وضعیت شناخته‌شده‌ی پوشش تست پروژه).

**یادداشت‌های اجرای مورد ۳۳:**

- `appModule` (تک‌فایل ۸۰ خطی در `:app`) به هشت ماژول Koin مجزا تقسیم شد، هرکدام در پکیج `feature.<name>.di` همان فیچر: `authModule` (`AuthRepository`, `LoginUseCase`, `LogoutUseCase`, `AuthViewModel`)، `chatModule` (`ChatRepository`)، `reportsModule` (`ReportsRepository`↔`QuotaRepository`, `ReportsViewModel`)، `cargoModule` (`CargoViewModel`)، `cargoWorkflowModule` (`InitialInfoViewModel`, `CargoCounterViewModel`)، `homeModule` (`ProfileViewModel`)، `adminModule` (`UserManagementViewModel`)، `updateModule` (`UpdateManager`).
- `appModule` فقط زیرساخت واقعاً مشترک بین فیچرها ماند: `ApiServiceV2`، `CryptoManager`/`SecurityVerifier`، `AppDatabase`، و بایندینگ‌های چهارگانه‌ی اینترفیس مرزی `UserPreferencesManager` (`TokenStore`/`UserPreferencesStore`/`ChatPreferencesStore`/`UserSettingsStore`) — این‌ها واقعاً cross-cutting هستند، نه مخصوص یک فیچر. `StartupViewModel` هم چون خودش هنوز فیزیکی در `app/.../core/startup` است (جابه‌جایی آن خارج از دامنه‌ی این مورد است) در `appModule` ماند.
- سه ماژول (`feature:cargo`, `feature:chat`, `feature:update`) قبلاً هیچ وابستگی Koin نداشتند (چون فقط از appModule متمرکز استفاده می‌شد)؛ `libs.koin.android` به `build.gradle.kts` هرکدام اضافه شد.
- `AtkCargoApplication.kt` حالا هشت ماژول جدید را در کنار `appModule` به `startKoin { modules(listOf(...)) }` پاس می‌دهد. Koin بدون توجه به این‌که کدام ماژول Gradle چه چیزی ثبت کرده یک گراف DI واحد می‌سازد، پس مثلاً `StartupViewModel` در `appModule` هنوز می‌تواند `get<ChatRepository>()`/`get<UpdateManager>()` را از `chatModule`/`updateModule` resolve کند — فقط لازم است هر دو در لیست `modules()` باشند (هستند).
- **تأیید صحت گراف:** چون در این محیط دستگاه/امولاتور برای اجرای واقعی `startKoin` و گرفتن خطای احتمالی «no definition found» در دسترس نبود (و افزودن `koin-test`/`verify()` به‌عنوان یک وابستگی تست جدید خارج از دامنه‌ی این مورد بود)، تک‌تک فراخوانی‌های `get()` در هر ۸ ماژول جدید دستی ردیابی و با بایندینگ متناظرش (در همان ماژول یا `appModule`) تطبیق داده شد — همه resolve می‌شوند.
- تأیید شد: کل پروژه (`compileDebugKotlin` و `testDebugUnitTest`) موفق.

**یادداشت‌های اجرای مورد ۲۳:**

- طبق تصمیم کاربر، دامنه به ۶ پکیج کوچک محدود شد؛ `com.atk.atk_cargo.api` (سه‌طرفه بین `:app`/`:core:network`/`:feature:update`، ۲۴ فایل تعریف‌کننده + ۲۲ فایل import‌کننده در سراسر پروژه، شامل کلاس‌های مرکزی مثل `ApiServiceV2`/`RetrofitClient`/`Constants`/`TokenStore`) عمداً دست‌نخورده ماند و برای یک تلاش جدا کنار گذاشته شد.
- برای هرکدام از ۶ پکیج، به‌جای صرفاً تغییر نام، ترجیح با **یکپارچه‌سازی فیزیکی** بود (انتقال فایل «غریبه» به ماژول درست) هرجا بی‌خطر بود:
  - `AppError.kt` (در `:app`، بدون هیچ فراخوانی‌ای در کل پروژه) — چون افزودن `retrofit2` به `:core:domain` فقط برای میزبانی یک فایل مرده معماری تمیزی نبود، به‌جای انتقال فقط تغییر نام گرفت: `com.atk.atk_cargo.core.domain` → `com.atk.atk_cargo.error` (هنوز در `:app`).
  - `StartupViewModel.kt` (در `:app`، دو importکننده: `AppModule.kt`, `MainActivity.kt`) از `com.atk.atk_cargo.core.startup` به `com.atk.atk_cargo.startup` تغییر نام گرفت — انتقال فیزیکی به `:core:domain` (کنار `StartupComposition.kt`) یعنی بازسازی کامل ViewModel که همان دامنه‌ی مورد ۲۱/۲۲ (خارج از این نشست) است.
  - `ColorSelector.kt`/`adjustColorForTheme`/`cardColors` (در `:core:designsystem` ولی زیر پکیج `data.model`) با `git mv` به `com.atk.atk_cargo.core.ui.components` (پکیج designsystem که همین‌جا زندگی می‌کند) منتقل شدند — ۴ importکننده (`DataModel.kt`، `CargoCounterScreen.kt`، `SelectInfoScreen.kt`، `QuotaDetailsScreen.kt`، `ReportsViewModel.kt`) به‌روزرسانی شدند.
  - `CargoViewModel.kt`/`CargoViewModelTest.kt` (در `:feature:cargo`) و `ReportsViewModel.kt` (در `:feature:reports`) هر دو از `com.atk.atk_cargo.ui.viewmodel` مشترک به پکیج‌های اختصاصی خودشان (`feature.cargo.viewmodel`/`feature.reports.viewmodel`) منتقل شدند — ۱۲ + ۱۷ فایل importکننده (شامل `MainScreen.kt`، `DataModel.kt` و همه‌ی dialogها/صفحات مصرف‌کننده) با `sed` روی الگوی دقیق fully-qualified بازنویسی شدند.
  - `JalaliDateUtilsTest.kt` از `app/src/test` به `core/common/src/test` منتقل شد (تست کنار کلاسی که تست می‌کند)؛ چون `core:common` تا امروز هیچ `testImplementation` نداشت، `libs.junit` اضافه شد.
- **بررسی جانبی مهم:** قبل از حذف/انتقال هرکدام، بررسی شد که آیا واقعاً بدون استفاده است یا خیر — `AppError.kt` تنها موردی بود که صفر فراخوان داشت؛ بقیه (`StartupViewModel`, `ColorSelector`, `CargoViewModel`, `ReportsViewModel`) همگی فعال و پرکاربرد بودند، پس هر importکننده‌ای تک‌تک ردیابی و اصلاح شد نه صرفاً حدس زده شد.
- تأیید شد: `./gradlew compileDebugKotlin` (کل پروژه) و `./gradlew testDebugUnitTest` (کل پروژه، شامل ۱۵ تست `JalaliDateUtilsTest` در خانه‌ی جدیدش) هر دو بدون خطا/شکست.

**یادداشت‌های اجرای مورد ۲۵:**

- ۵ کنترلر (`AnalyticsController`, `CargoController`, `ChatController`, `UserController`, `UtilityController`) از `use AuthenticatesRequests;` استفاده می‌کردند؛ همه به هویت پاس‌شده از `Router::dispatch` (پارامتر سوم/چهارم closure) مهاجرت کردند و trait حذف شد.
- **مهم‌ترین یافته‌ی امنیتی حین بررسی:** برخلاف تصور اولیه («بررسی مجوز داخلی همیشه با Router تکراری است»)، در `UserController::handle()` عضویت در `ADMIN_ONLY_ACTIONS` برای اکشن `getAllUsers` **تنها** لایه‌ی enforcement مجوز `manage_users` بود — route آن در سطح Router عمداً `permission => null` دارد (کامنت خودِ فایل: «getAllUsers قفل شد تا افشای اطلاعات همه‌ی کاربران رخ ندهد»). این بررسی داخلی **حذف نشد**، فقط از `$this->requirePermission()` به `ApiAuthGate::requirePermission($username, $userType, 'manage_users')` (همان کلاس static که Router خودش استفاده می‌کند) منتقل شد. برای ۵ اکشن دیگر (`getAllUsersWithStatus`, `getActiveDeviceId`, `createUser`, `deleteUser`, `forceLogout`) که route‌شان از قبل `permission => 'manage_users'` سطح Router دارد، این بررسی داخلی از قبل غیرقابل‌دسترس بود (Router زودتر رد می‌کند)؛ منتقل‌کردنش به‌جای حذف، یک احتیاط بدون هزینه بود، نه یک تصمیم لازم.
- **عارضه‌ی جانبی بی‌خطر:** شکل بدنه‌ی پاسخ ۴۰۳ برای `getAllUsers` توسط کاربر غیرمدیر از `{"error": true, "message": ...}` (فرمت پیش‌فرض trait) به `{"success": false, "message": ...}` (فرمت `ApiAuthGate`/`Response::error`) تغییر کرد. بررسی شد که این باعث ناسازگاری نمی‌شود: ۵ اکشن دیگر همین کنترلر از قبل (چون Router زودتر رد می‌کند) دقیقاً همین فرمت `success:false` را برمی‌گرداندند؛ این تغییر فقط `getAllUsers` را با آن‌ها هم‌شکل کرد.
- برای `AnalyticsController`/`CargoController`/`ChatController`/`UtilityController`، تمام بررسی‌های `requirePermission()` داخلی با مجوز سطح Router یکسان بودند (مثلاً `updateCargoInfo` ↔ route با `permission=>'edit_cargo'`) پس مستقیماً حذف شدند، نه منتقل — کدام‌یک حذف و کدام‌یک منتقل شود را برای هر ۹+۴+۶ فراخوانی جداگانه بررسی کردم، نه یک قاعده‌ی یکسان برای کل فایل.
- متدهایی که فقط `requireAuthenticatedSession()` داشتند (بدون استفاده‌ی بعدی از نام‌کاربری) امضایشان دست‌نخورده ماند (مثل `CargoController::searchByScaleReceipt`) — فقط خط اعتبارسنجی حذف شد، چون Router با `auth=>true` همان تضمین را می‌دهد.
- متدهایی که به‌جای پارامتر از `$this->authenticatedUsername`/`authenticatedUserType` استفاده می‌کردند (مثل `UserController`/`AnalyticsController`) به‌جای بازنویسی همه‌ی ارجاع‌های داخلی، همان نام property حفظ شد و فقط منبع مقداردهی از فراخوانی trait به پارامتر ورودی تغییر کرد — کمترین دیف ممکن با کمترین ریسک از‌قلم‌افتادگی.
- ۲۸ closure در `routes/api_v2.php` امضایشان به `function (array $params, Request $request, ?string $username[, ?string $userType])` تغییر کرد تا با ترتیب واقعی آرگومان‌های `Router::dispatch` (`$params, $request, $username, $userType`) یکی باشد؛ این ترتیب برای تک‌تک closureها دستی بازبینی شد چون جابه‌جایی تصادفی نوع (مثلاً گرفتن `Request` به‌جای `?string`) فقط در زمان اجرا TypeError می‌داد، نه در PHPStan/php-l.
- فایل `PHP/src/Core/AuthenticatesRequests.php` پس از تأیید صفر ارجاع باقی‌مانده (فقط دو کامنت توضیحی در `MinVersionGate.php`/یک تست، بدون وابستگی کد) حذف شد.
- **بدون تست خودکار برای این ۵ کنترلر** (نه AuthController-style قابل mock، همه مستقیم به mysqli واقعی وصل‌اند) — تنها سپر ایمنی، بازخوانی دستی تک‌تک ۲۸ نقطه‌ی تغییر بود؛ توصیه می‌شود قبل از انتشار این تغییر روی یک نسخه‌ی staging با دیتابیس واقعی دستی تست شود (سناریوهای حیاتی: `getAllUsers` توسط کاربر غیرمدیر باید ۴۰۳ بگیرد؛ `updateUser` روی حساب دیگران بدون `manage_users` باید ۴۰۳ بگیرد؛ حذف/تأیید حواله باید نام‌کاربری واقعی را در audit log ثبت کند).
- تأیید شد: `php -l` روی ۶ فایل تغییریافته، `vendor/bin/phpstan analyse` (بدون baseline جدید)، `vendor/bin/phpunit` (۱۰۵ تست) و بارگذاری کامل `routes/api_v2.php` (۵۷ route، بدون خطای fatal) — همه سبز.

### Phase 4 — Optimization (بلندمدت)

| # | اقدام | تلاش |
|---|-------|------|
| ۳۴ | جایگزینی راز کلاینت با challenge–response مبتنی بر Keystore | High |
| ۳۵ | مهاجرت `entryTime`/`exitTime`/`exitDate` به `DATETIME` | High |
| ۳۶ | یکسان‌سازی نوع ستون‌های وزن روی `DECIMAL(12,2)` | Medium |
| ۳۷ | افزودن `UNIQUE KEY` کلید طبیعی به `InitialInfo` و FK از `CargoInfo` | High |
| ۳۸ | یکسان‌سازی دسترسی دیتابیس روی PDO و حذف `getMysqliConnection` | High |
| ۳۹ | جایگزینی polling با push (SSE یا FCM واقعی) | High |
| ۴۰ | تجزیه‌ی فایل‌های بزرگ Compose (سقف ۳۰۰ خط) | High |
| ۴۱ | مهاجرت شبکه از Gson به `kotlinx.serialization` | High |
| ۴۲ | رسیدن به پوشش تست ۴۰٪+ روی لایه‌های دامنه و داده | High |

---

## Recommended Architecture

```text
┌─────────────────────────────────────────────────────────────┐
│  :app          Application · MainActivity · DI aggregator   │
│                (فقط اتصال — بدون کد فیچر)                    │
└───────────────────────────┬─────────────────────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        ▼                   ▼                   ▼
┌───────────────┐  ┌────────────────┐  ┌────────────────┐
│ :feature:*    │  │ :feature:*     │  │ :feature:*     │
│ ┌───────────┐ │  │  (هر فیچر:)    │  │                │
│ │presentation│ │  │  Screen        │  │  ماژول Koin    │
│ │ (Compose) │ │  │  ViewModel     │  │  خودش را دارد  │
│ ├───────────┤ │  │  Repository    │  │                │
│ │  domain   │ │  │  UseCase       │  │                │
│ │ (UseCase) │ │  └────────────────┘  └────────────────┘
│ ├───────────┤ │
│ │   data    │ │   ← Repository اجباری است، نه اختیاری
│ │(Repository)│ │      ViewModel هرگز ApiService را نمی‌بیند
│ └───────────┘ │
└───────┬───────┘
        │
        ▼
┌──────────────────────────────────────────────────────────────┐
│  :core:network   OkHttpClient مشترک · ApiServiceV2 · Token   │
│  :core:database  Room                                        │
│  :core:datastore UserPreferencesManager  (جدید)              │
│  :core:security  CryptoManager · SecurityVerifier  (جدید)    │
│  :core:notification  AppNotificationManager · Worker (جدید)  │
│  :core:designsystem  تم · کامپوننت‌های مشترک                  │
│  :core:domain    اینترفیس‌های مرزی · مدل‌های دامنه             │
│  :core:common    ابزارها                                     │
└──────────────────────────────────────────────────────────────┘
```

### قواعد قابل اجرا

۱. **ViewModel هرگز `ApiServiceV2` را import نمی‌کند.** با یک قانون lint سفارشی یا بررسی در CI اعمالش کن.
۲. **نام پکیج = مسیر ماژول.** بدون استثنا.
۳. **مدل API از مدل دامنه جداست.** DTOها در `:core:network` می‌مانند؛ Repository آن‌ها را به مدل دامنه نگاشت می‌کند (`toDomain()` از قبل در بخشی از کد وجود دارد — تعمیمش بده).
۴. **هر فیچر ماژول Koin خودش را صادر می‌کند.**

### سمت بک‌اند

```text
api/v2/index.php
   └─ Router (جدول route صریح — تنها نقطه‌ی auth/permission)
        └─ Controller  (فقط HTTP: پارس ورودی، شکل خروجی)
             └─ Service    (منطق تجاری، تراکنش)
                  └─ Repository (فقط PDO — mysqli حذف شود)
                       └─ MySQL
```

`AuthenticatesRequests` حذف شود؛ هویت از Router پاس داده شود. `AppApiController` به یک کنترلر واقعی تبدیل شود یا حذف گردد.
---

## Recommended Project Structure

```text
ATK-Cargo/
├── app/                                  ← فقط اتصال (~۵۰۰ خط، نه ۶٬۲۷۲)
│   └── src/main/java/com/atk/atk_cargo/
│       ├── AtkCargoApplication.kt
│       ├── MainActivity.kt
│       └── di/AppModule.kt               ← فقط جمع‌کننده‌ی ماژول‌های فیچر
│
├── core/
│   ├── common/          com.atk.atk_cargo.core.common
│   ├── designsystem/    com.atk.atk_cargo.core.designsystem
│   │                    + core/ui/components/* منتقل‌شده از :app
│   ├── domain/          com.atk.atk_cargo.core.domain
│   │                    + AppError.kt منتقل‌شده از :app
│   ├── database/        com.atk.atk_cargo.core.database
│   ├── datastore/       ★ جدید — UserPreferencesManager
│   ├── network/         com.atk.atk_cargo.core.network
│   │                    + HttpStack مشترک
│   ├── security/        ★ جدید — CryptoManager · SecurityVerifier · SecurityScreen
│   └── notification/    ★ جدید — AppNotificationManager · BootReceiver · Workers
│
├── feature/
│   ├── auth/            presentation · domain · data
│   ├── startup/         + StartupViewModel منتقل‌شده از :app
│   ├── home/
│   ├── chat/            + ChatNotificationWorker منتقل‌شده از :app
│   ├── cargo/
│   ├── cargo-workflow/  ← کاندید تقسیم: entry · registration · counter · details
│   ├── reports/         + ManageReportsScreen منتقل‌شده از :app
│   ├── admin/
│   └── update/
│
├── PHP/
│   ├── api/v2/index.php
│   ├── src/{Core,Controllers,Services,Repositories,Validators,Enums,Exceptions,routes}
│   ├── admin/           ★ جدید — PermissionManager + قالب/دارایی جدا (به‌جای ریشه)
│   ├── Lic/
│   ├── migrations/
│   ├── scripts/
│   └── tests/           ← گسترش به Controllerها
│
└── baselineprofile/
```

**تغییرات کلیدی:** سه ماژول `core` جدید، خالی شدن `:app`، تقسیم احتمالی `cargo-workflow` (۱۵٬۱۲۶ خط، بزرگ‌ترین ماژول فیچر)، و خارج شدن پنل‌های مدیریتی PHP از web root ریشه.

---

## Final Score

| Category | Score | مبنای امتیاز |
|----------|------:|--------------|
| Security | **7/10** | pinning، هش+rotation توکن، تشخیص reuse، bcrypt cost 12، CSRF، صفر SQLi، allow-list ستون — منهای DoS بحرانی، رازهای XOR، دور زدن گیت نسخه |
| Architecture | **5/10** | ماژول‌بندی و اینترفیس‌های مرزی خوب — منهای ۷ split package، Repository نیمه‌کاره، کد فیچر در `:app` |
| Kotlin | **7/10** | ۹ مورد `!!` در ۵۲ هزار خط، صفر `GlobalScope`، sealed types، structured concurrency — منهای catch غیرقابل دسترس و state سراسری غیر-snapshot |
| Jetpack Compose | **6/10** | ۱۰۲ مورد lifecycle-aware، کلید روی همه‌ی LazyListها، ۳۴ حاشیه‌نویسی پایداری — منهای محاسبات بدون `remember` و فایل‌های ۱٬۲۰۰ خطی |
| Android | **6/10** | NSC + pinning، FileProvider محدود، backup غیرفعال، ProGuard حرفه‌ای — منهای targetSdk 34، FGS ناسازگار با A14، loadLibrary محافظت‌نشده |
| PHP Backend | **7/10** | لایه‌بندی تمیز، strict_types، PHPStan+CI، صفر SQLi — منهای PDO/mysqli دوگانه، گیت تکراری، Middle Man |
| API Design | **5/10** | Router صریح با auth/permission متمرکز — منهای غیر RESTful، کد وضعیت ناسازگار، بدون pagination/rate-limit، ۵ endpoint مرده |
| Database | **6/10** | ایندکس‌های covering متفکرانه، FK درست، UNIQUE روی قبض باسکول — منهای varchar برای تاریخ/وزن، بدون FK روی CargoInfo، JOIN ناقص |
| Performance | **5/10** | MicroCache، ETag، throttle روی last_activity، Baseline Profile — منهای اسپلش ۳٫۸ ثانیه، polling، بدون pagination |
| Memory Management | **7/10** | بدون نشت context/scope/listener، همه‌ی جمع‌آوری‌ها lifecycle-aware — منهای نتایج بدون سقف سمت سرور |
| Error Handling | **6/10** | try/catch دولایه‌ی منسجم، بدون افشای DB، الگوی Result — منهای catch سراسری «TAMPERED»، موفقیت کاذب، پاک شدن کش |
| Testing | **2/10** | ۱٫۶٪ پوشش؛ انتخاب درست تست‌های موجود (rotation توکن) اما مسیر release در CI اجرا نمی‌شود |
| Code Quality | **5/10** | کامنت‌های فارسی باکیفیت که «چرا» را توضیح می‌دهند — منهای God ViewModel، فایل‌های ۱٬۲۰۰ خطی، کد مرده |
| Maintainability | **5/10** | مستندسازی تصمیمات عالی — منهای split package، لایه‌بندی ناسازگار، بدهی کد مرده |
| Scalability | **5/10** | MicroCache و ایندکس‌ها کمک می‌کنند — منهای polling خطی، بدون pagination، بدون rate limit |
| Production Readiness | **6/10** | جداسازی debug/release، آرشیو mapping، crash reporter، health endpoint — منهای بدون مانیتورینگ، بدون backup، R8 بدون گیت |

### **Overall Score: 5.8 / 10**

---

## Final Recommendations

### ۱. اول جلوی خون‌ریزی را بگیر، بعد بازسازی کن

Phase 1 هشت مورد دارد که **همگی Low effort** هستند و در مجموع دو تا سه روز کار می‌برند. این هشت مورد بزرگ‌ترین ریسک‌های واقعی سیستم را می‌بندند:
- یک بردار DoS که با `curl` قابل بهره‌برداری است
- یک گیت امنیتی که با حذف یک هدر دور زده می‌شود
- یک باگ محاسباتی که ممکن است همین حالا اعداد تناژ را اشتباه نشان دهد
- یک مسیر build که هیچ‌وقت تست نشده

**این کار را قبل از هر refactor معماری انجام بده.**

### ۲. بدهی معماری را با فیچرهای جدید بپرداز، نه با یک پروژه‌ی جداگانه

بازنویسی بزرگ معماری در پروژه‌ای که فعال است و ۱٫۶٪ پوشش تست دارد، ریسک بالایی دارد. به‌جای آن یک قاعده بگذار:

> **هر فیچر جدید یا هر تغییر بزرگ در فیچر موجود، باید Repository داشته باشد و نام پکیجش با ماژولش بخواند.**

بعد از شش ماه، بخش زیادی از یافته‌های Phase 3 خودبه‌خود حل شده‌اند.

### ۳. تست را از جایی شروع کن که شکستش بی‌صدا و پرهزینه است

پوشش ۴۰٪ در یک کدبیس ۵۲ هزار خطی هدف واقع‌بینانه‌ای برای این فصل نیست. اما شش تست هدفمند هست:

| تست | چرا اول این |
|-----|-------------|
| `UpdateManagerTest` | شکستش = نصب APK نامعتبر روی همه‌ی دستگاه‌ها |
| `CryptoManagerTest` | شکستش = خروج همه‌ی کاربران، بدون هیچ خطایی |
| تست ادغام JOIN (PHP) | یافته‌ی `[HIGH]` بخش Database را قفل می‌کند |
| `AuthControllerTest` | جریان ورود، قفل شدن، ورود همزمان |
| `SecurityVerifierTest` | منطق دوره‌ی مهلت که الان قابل دور زدن است |
| `ChatRepositoryTest` | منطق همگام‌سازی حذف |

اینها روی هم کمتر از یک هفته کار دارند و ریسک‌پذیرترین بخش‌های سیستم را پوشش می‌دهند.

### ۴. مسیر release را در CI گیت کن — همین امروز

این کمترین تلاش با بیشترین بازده در کل گزارش است. دو خط YAML. پیکربندی R8 این پروژه تهاجمی است (`fullMode`، `repackageclasses`، دیکشنری obfuscation) و مدل‌های Gson در پکیجی هستند که قانون keep صریح ندارد. هر روزی که این گیت نباشد، یک شکست release-only در راه است.

### ۵. آنچه نباید تغییر کند

این پروژه چند تصمیم دارد که از استاندارد میانگین بالاترند. در هر بازسازی، اینها را حفظ کن:

- **certificate pinning روی CA میانی به‌جای leaf** — با استدلال درست و مستند برای اپی که خارج از Play Store توزیع می‌شود.
- **rotation هر دو توکن + تشخیص reuse** که کل نشست‌ها را باطل می‌کند — الگوی OAuth 2.1.
- **هویت همیشه از نشست، هرگز از ورودی** — در `CargoController`، `ChatController` و `updateFcmToken` صریحاً پیاده شده.
- **بررسی IDOR در `UserController::updateUser`** — کامل و درست، شامل تأیید رمز فعلی برای تغییر رمز.
- **اینترفیس‌های مرزی** (`TokenStore`, `UserPreferencesStore`, ...) — بهترین بخش معماری این پروژه.
- **`FOR UPDATE` در `CargoRepository::findCargoByKeys`** — الگوی درست قفل‌گذاری که فقط باید به `SessionService` هم تعمیم یابد.
- **کامنت‌های فارسی که «چرا» را توضیح می‌دهند، نه «چه»** — این کیفیت مستندسازی کمیاب است. حفظش کن.

### ۶. تصمیم‌های محصولی که باید گرفته شوند

سه مورد نیازمند تصمیم است، نه کد:

۱. **زنجیره‌ی FCM مرده** — یا Firebase را واقعاً اضافه کن و push را جایگزین polling کن، یا ستون `fcm_token`، endpoint و کنترلر را حذف کن. الان هیچ‌کدام نیست.
۲. **پشتیبانی از تبلت/افقی** — یا قفل `portrait` را بردار و منابع `values-land`/`values-w*` را زنده کن، یا آن منابع را حذف کن.
۳. **مدل لایسنس** — راز کلاینت قابل استخراج است و این ذاتی است، نه یک باگ. اگر لایسنس یک قید تجاری جدی است، به challenge–response برو. اگر صرفاً یک مانع نرم است، این را صریحاً بپذیر و انرژی را جای دیگر بگذار.

---

## Top 20 Priority List

| # | Issue | Category | Severity | File | Effort |
|---|-------|----------|----------|------|--------|
| ۱ | DoS از طریق `sleep()` مسدودکننده در مسیر لاگین + HTTP همگام تلگرام | Security | **CRITICAL** | `PHP/src/Services/LoginAttemptLimiter.php:57` · `SecurityAlerter.php:81` | Low |
| ۲ | کلیدهای JOIN ناقص → ریسک بیش‌شماری تناژ | Database / Correctness | **HIGH** | `PHP/src/Controllers/AnalyticsController.php:261,323` · `ShipService.php:351` | Low |
| ۳ | گیت `min_allowed_version` با حذف هدر دور زده می‌شود | Security | **HIGH** | `PHP/src/Core/MinVersionGate.php:12` | Low |
| ۴ | `diagnostics/crash` بدون auth با rate-limiter `sleep(8)` | Security / Performance | **HIGH** | `PHP/src/Controllers/DiagnosticsController.php:83` | Low |
| ۵ | مسیر release هرگز در CI اجرا نمی‌شود | Build / Production | **HIGH** | `.github/workflows/ci.yml` | Low |
| ۶ | رازهای کلاینت با XOR تک‌بایتی | Security | **HIGH** | `app/src/main/cpp/secrets.cpp:6` | Medium |
| ۷ | اسپلش اجباری ۳٫۸ ثانیه‌ای در هر cold start | Performance / UX | **HIGH** | `StartupViewModel.kt:409` | Low |
| ۸ | FGS با poll دائمی، ناسازگار با سقف `dataSync` در Android 14+ | Android | **HIGH** | `LoadingNotificationService.kt:137` | Medium |
| ۹ | محاسبات مجموعه بدون `remember` داخل composition | Compose / Performance | **HIGH** | `ActiveQuotasContent.kt:57,100,175,215` | Low |
| ۱۰ | هیچ صفحه‌بندی در endpointهای گزارش‌گیری | Performance / Scalability | **HIGH** | `AnalyticsController.php` · `QuotaService.php` | Medium |
| ۱۱ | نشت لایه — ۶ ViewModel مستقیم `ApiServiceV2` را صدا می‌زنند | Architecture | **HIGH** | `CargoViewModel.kt` · `UserManagementViewModel.kt` و ۴ فایل دیگر | High |
| ۱۲ | `targetSdk = 34` در برابر `compileSdk = 36` | Android / Production | **HIGH** | `app/build.gradle.kts:39` | Medium |
| ۱۳ | پوشش تست ۱٫۶٪ — بدون تست برای رمزنگاری، به‌روزرسانی، احراز هویت | Testing | **HIGH** | سطح پروژه | High |
| ۱۴ | ۷ پکیج split‌شده بین ماژول‌ها | Architecture | **HIGH** | سطح پروژه | Medium |
| ۱۵ | race در rate-limiter فایل‌محور (read-modify-write غیراتمیک) | Security | MEDIUM | `LoginAttemptLimiter.php:75` · `PasswordGateService.php:77` | Low |
| ۱۶ | شمارش نام کاربری + افشای `userType` در `checkSession` | Security | MEDIUM | `AuthController.php:206` | Low |
| ۱۷ | رمزگذاری خروجی ناسازگار بین دو مسیر نوشتن Cargo | Data Integrity | MEDIUM | `CargoController.php:643,661,307` | Medium |
| ۱۸ | `AnimationManager` غیر-snapshot → Reduce Motion کار نمی‌کند | Compose / a11y | MEDIUM | `core/domain/.../AnimationManager.kt` | Low |
| ۱۹ | دو مسیر دسترسی DB (PDO/mysqli) + `sql_mode` غیر strict | PHP Backend | MEDIUM | `PHP/src/Core/Database.php:73` | Low (وصله) / High (یکسان‌سازی) |
| ۲۰ | `catch (Exception)` سراسری که «برنامه دستکاری شده» نشان می‌دهد | Error Handling / UX | MEDIUM | `StartupViewModel.kt:227` | Low |

---

## پیوست — روش‌شناسی ممیزی

**آنچه بررسی شد:**
- تمام ۱۹۰ فایل Kotlin در ۱۵ ماژول Gradle (خواندن کامل فایل‌های اصلی، اسکن الگویی بقیه)
- تمام فایل‌های PHP خارج از `vendor/` و `build/` (۱۰۰+ فایل)
- `schema.sql` کامل، migrationها، `phpstan.neon`، `composer.json`، `phpunit.xml`
- `AndroidManifest.xml`، هر چهار فایل `res/xml/`، `proguard-rules.pro`، `secrets.cpp`، `CMakeLists.txt`
- تمام `build.gradle.kts`، `libs.versions.toml`، `gradle.properties`، `settings.gradle.kts`
- `.github/workflows/ci.yml`، `.gitignore`، همه‌ی فایل‌های `.htaccess`
- ردیابی جریان انتها‌به‌انتها: Compose → ViewModel → Repository → Retrofit → Router → Controller → Service → Repository → MySQL

**آنچه بررسی نشد (و برای تأیید نیاز به دسترسی دارد):**
- محیط اجرای production (نسخه‌ی PHP، فعال بودن APCu، پیکربندی PHP-FPM، `AllowOverride` در Apache)
- محتوای واقعی `.env` تولید (آیا `SECURITY_ALERT_TELEGRAM_*` تنظیم شده؟)
- داده‌ی واقعی دیتابیس — یافته‌ی `[HIGH]` کلید JOIN نیازمند اجرای کوئری تشخیصی روی داده‌ی تولید است تا مشخص شود بیش‌شماری **در حال حاضر** رخ می‌دهد یا فقط ممکن است
- رفتار زمان اجرا: هیچ build، تست یا پروفایلی در جریان این ممیزی اجرا نشد
- تاریخچه‌ی گیت برای رازهای لو رفته (فقط وجود `ROTATE_SECRETS.md` تأیید می‌کند که این موضوع شناخته‌شده است)

**سطح اطمینان:** یافته‌های علامت‌گذاری‌شده با فایل و شماره‌ی خط مستقیماً از خواندن کد استخراج شده‌اند. هرجا استنتاج لازم بوده (مثلاً اثر واقعی JOIN ناقص روی داده‌ی موجود)، صراحتاً به‌عنوان نیازمند تأیید علامت‌گذاری شده است.
