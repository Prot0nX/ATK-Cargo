# گزارش بررسی عمیق پروژه ATK-Cargo

**تاریخ:** ۱۴۰۵/۰۵/۲۵ (2026-08-16) · **کامیت:** `3f9ce3c` · **دامنه:** کلاینت اندروید (۱۶۵ فایل Kotlin) + سرور PHP (۷۰+ فایل، ~۷۰۰۰ خط در `src/`)

---

## ۱. خلاصه اجرایی

معماری کلی پروژه سالم است: لایه‌بندی Controller/Service/Repository سمت سرور، Clean-ish Architecture با Koin سمت کلاینت، Prepared Statement در همه‌جا، bcrypt، Android Keystore، Network Security Config، و R8/ProGuard فعال. کامنت‌های فارسی داخل کد نشان می‌دهد چند دور بازبینی امنیتی قبلاً انجام شده و شکاف‌های مشخصی (S-1 تا S-4، C-3، C-5) بسته شده‌اند.

**اما** گیت احراز هویت به‌صورت ناقص اعمال شده است. تِرِیت `AuthenticatesRequests` روی `CargoController`، `AppApiController`، `AnalyticsController` و بخشی از `UtilityController` نشسته، ولی **چهار کنترلر باقی‌مانده کاملاً بدون احراز هویت رها شده‌اند** — و یکی از آن‌ها مدیریت کاربران است. نتیجه‌ی عملی: هر کسی که آدرس سرور را بداند می‌تواند بدون هیچ اعتبارنامه‌ای یک کاربر `admin` بسازد و کنترل کامل سیستم را در دست بگیرد.

| دسته | تعداد یافته | بحرانی | بالا | متوسط | پایین |
|---|---|---|---|---|---|
| امنیت | ۲۱ | ۳ | ۴ | ۷ | ۷ |
| باگ واقعی | ۴ | ۰ | ۲ | ۲ | ۰ |
| کیفیت کد | ۹ | — | — | — | — |
| پرفورمنس | ۸ | — | — | — | — |

> **توصیه:** موارد `S-01`، `S-02`، `S-03` قبل از هر کار دیگری باید بسته شوند. مجموع تلاش تخمینی برای هر سه: کمتر از یک روز کاری.

---

## ۲. امنیت

### 🔴 بحرانی

---

#### S-01 — `users_api.php` هیچ احراز هویتی ندارد؛ ساخت ادمین توسط مهاجم ناشناس

**فایل:** [`PHP/src/Controllers/UserController.php:26`](PHP/src/Controllers/UserController.php:26) · [`PHP/users_api.php`](PHP/users_api.php)

`UserController::handle()` مستقیماً `action` را می‌خواند و dispatch می‌کند. برخلاف `CargoController` که با `use AuthenticatesRequests` و `$this->requireAuthenticatedSession()` محافظت شده، این کنترلر **هیچ گیتی ندارد** — نه نشست، نه مجوز، نه بررسی `userType`:

```php
public function handle(): void {
    $action = $this->request->get('action');   // ← بدون requireAuthenticatedSession()
    if ($this->request->isGet())      $this->handleGet((string)$action);
    elseif ($this->request->isPost()) $this->handlePost((string)$action);
}
```

و `protected_proxy.php` هم فقط rate-limit و whitelist فایل را چک می‌کند، نه هویت را.

**اثر عملی (به ترتیب شدت):**

| action | نتیجه برای مهاجم ناشناس |
|---|---|
| `createUser` | **ساخت کاربر با `userType=admin` → تصاحب کامل سیستم** |
| `updateUser` | تغییر رمز هر کاربر (از جمله ادمین‌های موجود) |
| `deleteUser` | حذف کاربران |
| `getAllUsersWithStatus` | افشای کامل لیست کاربران، نوع دسترسی، مدل دستگاه، آخرین فعالیت |
| `forceLogout` | خروج اجباری هر کاربر (اختلال عملیاتی) |

**سناریوی حمله:** یک درخواست `POST` به `protected_proxy.php?target=users_api.php&action=createUser` با بدنه‌ی `{"username":"x","fullName":"xxx","password":"y","userType":"admin"}` کافی است. `UserService::createUser` هیچ بررسی‌ای روی مقدار `userType` ندارد و آن را مستقیم در دیتابیس می‌نویسد.

**اصلاح:**
```php
class UserController {
    use AuthenticatesRequests;   // + use App\Core\AuthenticatesRequests;

    public function handle(): void {
        $this->requireAuthenticatedSession();
        $this->requirePermission('manage_users');
        // ...
    }
}
```
علاوه بر آن، `userType` باید در برابر یک allow-list (`['admin','operator','verifier']`) اعتبارسنجی شود و ارتقای کاربر به `admin` نیاز به گیت جداگانه داشته باشد.

---

#### S-02 — `proxy_generator.php` بدون احراز هویت، `protected_proxy.php` را بازنویسی می‌کند

**فایل:** [`PHP/proxy_generator.php`](PHP/proxy_generator.php)

این فایل (کد obfuscate‌شده‌ی تک‌خطی) در انتهای اجرا این کار را می‌کند:

```php
$result = file_put_contents('protected_proxy.php', _o6());
```

**بدون هیچ بررسی هویت، توکن یا IP.** هر درخواست HTTP به این فایل، تنها دروازه‌ی ورودی کل API را با نسخه‌ی تولیدشده بازنویسی می‌کند.

اگرچه `proxy_generator.php` در `PROXY_EXCLUDED_FILES` قرار دارد (پس از طریق خود پروکسی قابل فراخوانی نیست)، اما **مستقیماً از طریق URL کاملاً در دسترس است** و `.htaccess` هم آن را مسدود نمی‌کند.

ضمناً نسخه‌ی تولیدشده کیفیت پایین‌تری از فایل فعلی دارد: `Access-Control-Allow-Origin: *` می‌گذارد، لیست کامل فایل‌های موجود سرور را در پاسخ خطا افشا می‌کند (`"available_files"=>$this->A`)، و rate-limit فقط فایلی است (بدون APCu).

**اصلاح:** حذف کامل فایل از سرور production. اگر قابلیت بازتولید لازم است، به اسکریپت CLI با `php_sapi_name() === 'cli'` منتقل شود.

---

#### S-03 — چت: هویت فقط از پارامتر `username` خوانده می‌شود (جعل هویت)

**فایل:** [`PHP/src/Controllers/ChatController.php:29`](PHP/src/Controllers/ChatController.php:29)

کل کنترل دسترسی چت روی این تابع بنا شده:

```php
private function isAdmin(string $username): bool {
    $stmt = $this->prepareAndExecute("SELECT userType FROM Users WHERE username = ? LIMIT 1", 's', $username);
    // ...
    return $result && $result['userType'] === 'admin';
}
```

و `$username` مستقیماً از ورودی درخواست می‌آید:
```php
$username = $input['username'] ?? '';          // POST
$username = (string)$this->request->get('username', '');   // GET
```

**نام کاربری یک راز نیست** — از طریق `users_api.php?action=getAllUsers` (که آن هم بدون احراز هویت است، S-01) به‌راحتی قابل استخراج است. هر کسی با دانستن نام کاربری یک ادمین می‌تواند:
- تمام تاریخچه‌ی چت مدیران را بخواند (`getMessages`)
- به نام آن ادمین پیام بفرستد (`sendMessage`)
- پیام‌های همان ادمین را ویرایش/حذف کند (`isMessageOwner` هم فقط روی همین `$username` جعل‌شده کار می‌کند)

**اصلاح:** `ChatController` باید `use AuthenticatesRequests` بگیرد و `$username` را **صرفاً** از `$this->authenticatedUsername` (که از هدر `X-Session-Token` اعتبارسنجی شده) بخواند، نه از بدنه/کوئری. پارامتر `username` ورودی باید کاملاً نادیده گرفته شود.

---

### 🟠 بالا

---

#### S-04 — `OnlineUsersController` بدون احراز هویت: افشای اطلاعات + خروج اجباری

**فایل:** [`PHP/src/Controllers/OnlineUsersController.php:22`](PHP/src/Controllers/OnlineUsersController.php:22)

هیچ گیتی ندارد و علاوه بر آن `Access-Control-Allow-Origin: *` می‌فرستد (خط ۲۶) — یعنی هر صفحه‌ی وبی در اینترنت می‌تواند از مرورگر قربانی این API را صدا بزند.

- `get_online_users` → **آدرس IP، `device_id`، مدل دستگاه، نوع کاربر و زمان فعالیت تمام کاربران آنلاین**
- `force_logout` → خروج اجباری هر کاربر دلخواه (اختلال عملیاتی مستمر)
- `heartbeat` → زنده نگه داشتن نشست دلخواه

**اصلاح:** افزودن `AuthenticatesRequests` + `requirePermission('manage_users')`، و جایگزینی `ACAO: *` با دامنه‌ی صریح پنل وب (یا حذف کامل، چون کلاینت اندروید هدر `Origin` نمی‌فرستد).

---

#### S-05 — نشست‌ها هرگز منقضی نمی‌شوند

**فایل:** [`PHP/src/Repositories/SessionRepository.php:40`](PHP/src/Repositories/SessionRepository.php:40)

```sql
SELECT id FROM user_sessions
WHERE username = :username AND device_id = :device_id
  AND session_token = :token AND is_active = 1   -- ← هیچ شرطی روی last_activity
```

`last_activity` در هر درخواست به‌روزرسانی می‌شود اما **هیچ‌جا برای ابطال استفاده نمی‌شود**. `SessionManager::$sessionTimeout = 86400` تعریف شده ولی هیچ کوئری‌ای آن را نمی‌خواند. تنها راه ابطال، logout صریح یا ویرایش کاربر است.

نتیجه: یک `session_token` سرقت‌شده (از دستگاه گم‌شده، بکاپ، یا لاگ) **تا ابد معتبر است**.

**اصلاح:**
```sql
AND is_active = 1
AND last_activity > (NOW() - INTERVAL 24 HOUR)
```
به‌همراه یک cron روزانه که نشست‌های کهنه را `is_active = 0` کند. (تابع `cleanupExpiredSessions` که برای همین منظور صدا زده می‌شود اصلاً وجود ندارد — بند B-02.)

---

#### S-06 — بدون rate-limit یا قفل روی ورود (`check_Auth.php`)

**فایل:** [`PHP/src/Controllers/AuthController.php:35`](PHP/src/Controllers/AuthController.php:35)

`login()` تلاش‌های ناموفق را فقط **لاگ** می‌کند و هیچ شمارنده/قفلی ندارد. تنها سد موجود rate-limit عمومی پروکسی است: **۶۰ درخواست در دقیقه به‌ازای هر IP** — یعنی ۸۶٬۴۰۰ تلاش رمز در روز از یک IP، و بدون محدودیت با چرخش IP.

جالب اینکه زیرساخت لازم از قبل ساخته شده: `PasswordGateService` دقیقاً همین کار را (۵ تلاش / ۱۵ دقیقه با APCu) برای رمز عملیاتی انجام می‌دهد.

**اصلاح:** استفاده از همان الگوی `PasswordGateService` در `AuthController::login()` با کلید `username + IP`.

---

#### S-07 — مسیر legacy رمز عبور: عملاً «Pass-the-Hash»

**فایل:** [`PHP/src/Services/UserService.php:59`](PHP/src/Services/UserService.php:59)

```php
// ===== حالت ۲: SHA-256 یا متن خام (سیستم قدیم) =====
if (!hash_equals($storedPassword, $password)) {
    return null;
}
$upgradedHash = password_hash($storedPassword, PASSWORD_BCRYPT, ['cost' => 12]);
```

کلاینت SHA-256 رمز را می‌فرستد و سرور آن را با هش ذخیره‌شده مقایسه می‌کند. یعنی **خودِ هش، رمز عبور است**. اگر جدول `Users` نشت کند (SQLi در آینده، بکاپ، دسترسی DBA)، مهاجم بدون شکستن هیچ هشی مستقیماً وارد می‌شود.

بدتر: خط بعد `password_hash($storedPassword, ...)` — یعنی bcrypt روی **هش ذخیره‌شده** اعمال می‌شود، نه رمز واقعی کاربر. پس بعد از مهاجرت هم، مقداری که کاربر باید بفرستد همچنان همان SHA-256 است. مهاجرت، ضعف را حفظ کرده و فقط پنهانش کرده.

ضمناً SHA-256 سمت کلاینت **بدون salt** است → قابل حمله با rainbow table.

**اصلاح (نیاز به هماهنگی کلاینت):** کلاینت باید رمز خام را روی TLS بفرستد و سرور `password_hash` واقعی بزند. مسیر مهاجرت: یک فلگ `password_scheme` در جدول `Users`؛ در اولین ورود موفق با اسکیم قدیمی، رمز خام دریافتی از کلاینت جدید را bcrypt کن و اسکیم را ارتقا بده. تا آن زمان دست‌کم مسیر legacy را زمان‌دار (deadline) کن.

---

### 🟡 متوسط

| کد | یافته | فایل | توضیح و اصلاح |
|---|---|---|---|
| **S-08** | `update_fcm_token` بدون احراز هویت | [`UserController.php:176`](PHP/src/Controllers/UserController.php:176) | `user_id` از ورودی می‌آید و بدون بررسی مالکیت `UPDATE Users SET fcm_token`. مهاجم می‌تواند توکن push هر کاربر را با توکن خودش عوض کند (دریافت نوتیفیکیشن‌های او) یا خراب کند. باید `user_id` از نشست احرازشده استخراج شود، نه از ورودی. |
| **S-09** | `export_schema.php` روی HTTP اجرا می‌شود | [`export_schema.php:206`](PHP/export_schema.php:206) | شرط `php_sapi_name() === 'cli' \|\| isset($_SERVER['HTTP_HOST'])` باعث می‌شود هر GET، کل schema دیتابیس را استخراج و در `schema.sql` بنویسد و پیام موفقیت با مسیر مطلق فایل‌سیستم برگرداند. شرط باید فقط `=== 'cli'` باشد. |
| **S-10** | `getClientIp()` به هدرهای قابل‌جعل اعتماد می‌کند | [`Request.php:103`](PHP/src/Core/Request.php:103) | `HTTP_CLIENT_IP` و `HTTP_X_FORWARDED_FOR` بالاتر از `REMOTE_ADDR` اولویت دارند. این IP در `user_sessions.ip_address` و لاگ‌ها ذخیره می‌شود → ممیزی غیرقابل‌اتکا. (توجه: `protected_proxy.php` درست عمل می‌کند و فقط `REMOTE_ADDR` را می‌خواند.) اگر پشت CDN/LB نیستید، فقط `REMOTE_ADDR`. |
| **S-11** | `checkExistence` بدون احراز هویت | [`UtilityController.php:131`](PHP/src/Controllers/UtilityController.php:131) | یک oracle برای شمارش کوتاژها: مهاجم با پیمایش `loadingQuotaNumber` می‌فهمد کدام کوتاژها ثبت شده‌اند. افزودن `requireAuthenticatedSession()`. |
| **S-12** | اعتبارنامه‌ی API شرکت ثالث داخل APK | [`app/src/main/cpp/secrets.cpp`](app/src/main/cpp/secrets.cpp) | `webUserName`/`webUserPass`/`AuthUser` برای `pishrodarya.ir` با XOR تک‌بایتی `0x5A` مبهم شده‌اند — با `strings` + یک XOR ساده در چند دقیقه بازیابی می‌شوند. بدتر: **مقادیر خام به‌صورت کامنت `// Original: "..."` کنار هرکدام در سورس کنترل‌شده‌ی git هستند.** obfuscation در APK ≠ محرمانگی؛ راه‌حل درست: پروکسی‌کردن این فراخوانی‌ها از سرور خودتان. حداقل اقدام فوری: حذف کامنت‌های `Original` و چرخش اعتبارنامه‌ها. |
| **S-13** | کلید API پیش‌فرض hardcode در سورس | [`config/config.php:52`](PHP/config/config.php:52) | `UPDATE_CHECK_API_KEY` مقدار fallback واقعی `atk_nk_9290VV42-...` دارد و در git کامیت شده. اگر `.env` روی سرور تنظیم نشده باشد (که پیش‌فرض همین است)، این کلید فعال است. باید در نبودِ متغیر محیطی، خطا بدهد نه fallback. |
| **S-14** | هش رمز ادمین در `.env.example` | [`PHP/.env.example`](PHP/.env.example) | `ADMIN_PASSWORD_HASH=$2y$10$bUbu4...` با توضیح «هش پیش‌فرض مربوط به کلمه عبور ادمین». هرکس فایل نمونه را کپی کند، رمز پنل `PermissionManager` عمومی است. مقدار باید خالی و با راهنمای تولید (`php -r "echo password_hash(...)"`) جایگزین شود. |

### 🟢 پایین

| کد | یافته | جزئیات |
|---|---|---|
| **S-15** | بدون Certificate Pinning | [`RetrofitClient.kt`](app/src/main/java/com/atk/atk_cargo/api/RetrofitClient.kt) — `network_security_config.xml` گواهی Certum را bundle کرده اما `<pin-set>` ندارد و `src="system"` هم مجاز است. برای اپ عملیاتی داخلی، pinning روی SPKI توصیه می‌شود (به‌همراه backup pin). |
| **S-16** | پوشه‌ی `PHP/log/` توسط `.htaccess` محافظت نشده | `.htaccess` فقط `(logs\|backups\|private\|secret)/` را مسدود می‌کند — پوشه‌ی مفرد `log/` (که `protected_proxy.php` و `PasswordGateService` در آن می‌نویسند) پوشش داده نشده. `*.log` با FilesMatch مسدود است، اما `blocked_ips.txt`، `rate_limit_*.txt` و `pwdgate_*.json` قابل خواندن‌اند. افزودن `log` به الگو. |
| **S-17** | `display_errors = 1` در پنل مدیریت | [`PermissionManager.php:8`](PHP/PermissionManager.php:8) — `error_reporting(E_ALL); ini_set('display_errors', 1);` در فایلی که روی production سرو می‌شود، مسیر فایل‌ها و جزئیات داخلی را در صفحه‌ی خطا نمایش می‌دهد. |
| **S-18** | تزریق نام ستون در `UserRepository::update()` | [`UserRepository.php:89`](PHP/src/Repositories/UserRepository.php:89) — کلیدهای آرایه مستقیم در SQL درج می‌شوند (`"{$key} = :{$key}"`). فعلاً همه‌ی فراخوان‌ها کلید ثابت می‌دهند، پس **قابل بهره‌برداری نیست**، اما یک تله برای توسعه‌ی آینده است. افزودن allow-list ستون‌ها. |
| **S-19** | fallback فایلیِ rate-limit در برابر رقابت (race) آسیب‌پذیر | [`protected_proxy.php:125`](PHP/protected_proxy.php:125) — `file_get_contents` + `file_put_contents` بدون قفل؛ درخواست‌های هم‌زمان می‌توانند شمارنده را بازنشانی کنند. ضمناً هر IP یک فایل دائمی می‌سازد (رشد بی‌حد پوشه). با APCu مشکلی نیست، ولی fallback باید `flock` بگیرد و فایل‌های کهنه پاک شوند. |
| **S-20** | `syncPermissions` نشست را بدون توکن اعتبارسنجی می‌کند | [`UtilityController.php:285`](PHP/src/Controllers/UtilityController.php:285) — از `SessionManager::isSessionActive($username, $deviceId)` استفاده می‌کند که **توکن را چک نمی‌کند**؛ `username` و `deviceId` هیچ‌کدام سرّی نیستند. باید به `requireAuthenticatedSession()` مهاجرت کند (همان اصلاحی که برای `checkSession` با کد C-5 انجام شده). |
| **S-21** | فیلتر User-Agent در `.htaccess` بی‌اثر/مضر | `.htaccess:89-95` — `Deny from env=bots` با `Order Allow,Deny` ترکیب شده؛ در Apache 2.4 دستورات `Order/Allow/Deny` منسوخ‌اند و بدون `mod_access_compat` نادیده گرفته می‌شوند. مسدودکردن `curl`/`wget` هم امنیتی نیست (تغییر UA یک فلگ است) و صرفاً دیباگ را سخت می‌کند. |

---

## ۳. باگ‌های واقعی (نه سبک کدنویسی)

#### B-01 🟠 — فراخوانی متد ناموجود `Request::post()` → خطای ۵۰۰

**فایل:** [`OnlineUsersController.php:37,131,132`](PHP/src/Controllers/OnlineUsersController.php:37)

```php
$action = (string)($this->request->get('action') ?? $this->request->post('action') ?? 'get_online_users');
```

کلاس `App\Core\Request` متدی به نام `post()` **ندارد** (فقط `get`، `all`، `getHeader`، `isPost`، `isGet`، `getClientIp`، `sanitize`).

چون `??` کوتاه‌مدار است، این کد فقط وقتی خراب می‌شود که `action` در query string نباشد:
- **درخواست بدون `action`** → به‌جای پیش‌فرض `get_online_users`، خطای Fatal
- **`action=heartbeat`** → خط ۱۳۱ بی‌قید `post()` را صدا می‌زند → **همیشه Fatal**

و چون `Error` زیرکلاس `Exception` نیست، `catch (Exception $e)` آن را نمی‌گیرد؛ پاسخ یک ۵۰۰ خام است. **قابلیت heartbeat عملاً به‌طور کامل از کار افتاده.**

**اصلاح:** حذف `->post(...)`؛ متد `get()` از قبل هر سه منبع JSON/POST/GET را پوشش می‌دهد.

---

#### B-02 🟠 — فراخوانی متد ناموجود `cleanupExpiredSessions()`

**فایل:** [`OnlineUsersController.php:146`](PHP/src/Controllers/OnlineUsersController.php:146)

```php
$cleanedCount = $sessionManager->cleanupExpiredSessions();
```

این متد **در هیچ‌جای پروژه تعریف نشده** — نه در `SessionManager`، نه در `SessionService`، نه در `SessionRepository`. اکشن `cleanup_inactive` همیشه Fatal می‌دهد.

این مستقیماً به S-05 وصل است: مکانیزم پاک‌سازی نشست‌های منقضی طراحی شده، صدا زده شده، اما هرگز پیاده‌سازی نشده — به همین دلیل هیچ نشستی منقضی نمی‌شود.

---

#### B-03 🟡 — `generateDailyStats.php` شکسته است

**فایل:** [`PHP/generateDailyStats.php`](PHP/generateDailyStats.php)

```php
require_once __DIR__ . '/realTimeLoadingData.php';
$cargoAPI = new CargoAPI($dbConnection);
```

`realTimeLoadingData.php` بعد از بازآرایی به یک wrapper چهارخطی تبدیل شده که `AnalyticsController` را می‌سازد و **بلافاصله اجرا و `exit` می‌کند**. کلاس `CargoAPI` دیگر وجود ندارد. اگر این اسکریپت در cron باشد، هر اجرا شکست می‌خورد (احتمالاً بی‌صدا).

---

#### B-04 🟡 — `check_table_structure.php` مسیر اشتباه دارد

**فایل:** [`PHP/check_table_structure.php:2`](PHP/check_table_structure.php:2)

`require_once __DIR__ . '/PHP/config/config.php'` — خودِ فایل داخل `PHP/` است، پس مسیر درست `/config/config.php` است. فایل همیشه Fatal می‌دهد. (خوشبختانه در `PROXY_EXCLUDED_FILES` است.) این فایل باید حذف شود، نه اصلاح — یک ابزار تشخیصی است که ساختار جداول را روی خروجی چاپ می‌کند.

---

## ۴. کیفیت کد و معماری

### C-01 — سه پیاده‌سازی موازی از یک گیت احراز هویت

`AuthenticatesRequests` (trait مشترک) نوشته شده، اما `AppApiController` و `AnalyticsController` **کپی خصوصی خودشان** را از `requireAuthenticatedSession` و `requirePermission` نگه داشته‌اند (`AppApiController.php:41,94` و `AnalyticsController.php:53,75`). سه نسخه از منطق امنیتی یعنی سه جای فراموش‌شدنی هنگام اصلاح. مثلاً گیت `enforceMinAppVersion` فقط در trait هست — دو کنترلر دیگر آن را ندارند.

**اقدام:** حذف نسخه‌های خصوصی و `use AuthenticatesRequests` در هر دو.

### C-02 — تکرار گسترده‌ی توابع کمکی

- `sendJsonResponse()` در **۶ کنترلر** با بدنه‌ی تقریباً یکسان تکرار شده (`Analytics`, `AppApi`, `Cargo`, `Chat`, `License`, `OnlineUsers`, `Utility`) — درحالی‌که `Core\Response::json()` دقیقاً همین کار را می‌کند.
- `sanitizeInput()`/`sanitizeString()` در ۴ جا کپی شده، همگی معادل `InputValidator::sanitize()`.

**اقدام:** یک `BaseController` یا استفاده‌ی مستقیم از `Response`/`InputValidator`. تخمین: ~۲۰۰ خط کد تکراری حذف می‌شود.

### C-03 — سه مسیر مستقل اتصال به دیتابیس

۱. `Database::getPdoConnection()` (Repository‌ها) ۲. `Database::getMysqliConnection()` (کنترلرها) ۳. `getDbConnection()` سراسری در `config/config.php` (کد قدیمی/`PermissionManager`).

هر درخواست ممکن است دو کانکشن جدا به همان دیتابیس باز کند. تنظیمات هم ناهمگون است: PDO با `ERRMODE_EXCEPTION` و `EMULATE_PREPARES=false`، mysqli بدون هیچ‌کدام (خطاها بی‌صدا رد می‌شوند).

### C-04 — `htmlspecialchars` به‌عنوان «sanitize» ورودی

`InputValidator::sanitize()` روی **همه‌ی** ورودی‌ها اعمال می‌شود، از جمله `username` و `deviceId` که فقط به عنوان کلید دیتابیس استفاده می‌شوند. این کار:
- امنیت SQL اضافه نمی‌کند (Prepared Statement از قبل هست)
- داده را **خراب می‌کند**: کاربری با `'` یا `&` در نام، به‌صورت `&#039;` ذخیره می‌شود و دیگر با رکورد اصلی match نمی‌شود
- کدگذاری خروجی را با اعتبارسنجی ورودی خلط می‌کند

**اصل درست:** اعتبارسنجی در ورودی (فرمت/طول/allow-list)، escape در خروجی (بر اساس context).

### C-05 — فایل‌های بیش‌ازحد بزرگ

| فایل | حجم | مسئله |
|---|---|---|
| `SelectInfoScreen.kt` | ۹۵ KB | ۱۷ Composable در یک فایل؛ منطق UI + state + شبکه در هم |
| `AppApiController.php` | ۷۹ KB / ۱۵۰۰+ خط | routing + business logic + SQL + کش، همه در یک کلاس |
| `QuotasListScreen.kt` | ۶۸ KB | — |
| `QuotaManagementDialog.kt` | ۶۷ KB | یک «دیالوگ» |
| `CargoController.php` | ۳۹ KB | — |

`AppApiController` مخصوصاً باید به `QuotaService` / `ShipService` / `WarehouseService` شکسته شود؛ الگوی Service Layer در پروژه از قبل وجود دارد (`CargoService`) و فقط اعمال نشده.

### C-06 — فایل‌ها و کد مرده

- `check_table_structure.php` (شکسته، B-04)
- `generateDailyStats.php` (شکسته، B-03)
- `export_schema.php` (ابزار توسعه روی production، S-09)
- `proxy_generator.php` (خطر امنیتی، S-02)
- `protected_proxy.html` / `PHP/index.html` — بررسی شوند
- دو `SessionManager.php` (ریشه + `User/`) و دو `jdf.php`
- `file_path.xml` و `file_paths.xml` — هر دو موجود، فقط اولی در Manifest ارجاع دارد

### C-07 — پوشش تست تقریباً صفر

۴ فایل تست برای ۱۶۵ فایل Kotlin، که دو تای آن‌ها `ExampleUnitTest` و `ExampleInstrumentedTest` پیش‌فرض Android Studio هستند. سمت PHP هیچ تستی نیست. با توجه به منطق مالی/تناژ (`calculateLoadableTonnage`، `getQuotasRemaining`، محاسبات درصد)، این پرریسک‌ترین بخش پروژه است که هیچ شبکه‌ی ایمنی ندارد.

### C-08 — Lint در حالت خاموش

```kotlin
lint {
    abortOnError = false
    checkReleaseBuilds = false
}
```
یعنی هشدارهای lint (از جمله دسته‌ی Security) هرگز build را متوقف نمی‌کنند و روی release اصلاً اجرا نمی‌شوند.

### C-09 — دو کتابخانه‌ی سریال‌سازی هم‌زمان

هم `kotlinx-serialization` (+ پلاگین) و هم `Gson` (از طریق `retrofit-gson`) در dependency‌ها هستند. کد از Gson استفاده می‌کند و kotlinx فقط وزن اضافه است. ضمناً `Strictness.LENIENT` در Gson باعث می‌شود پاسخ‌های ناقص/خراب سرور بی‌صدا به مقدار پیش‌فرض تبدیل شوند (مثل `FloatTypeAdapter` که در هر خطا `0f` برمی‌گرداند) — منشأ بالقوه‌ی نمایش عدد اشتباه به‌جای خطا.

---

## ۵. پرفورمنس و کارایی

### P-01 🟠 — هر درخواست احرازشده = ۳ کوئری دیتابیس

`requireAuthenticatedSession()` در هر فراخوانی API انجام می‌دهد:

1. `SELECT id FROM user_sessions WHERE ...` (اعتبارسنجی توکن)
2. `UPDATE user_sessions SET last_activity = NOW() WHERE ...` ← **نوشتن**
3. `SELECT * FROM Users WHERE username = ?` (برای `userType`)

مورد ۳ حتی وقتی `requirePermission` صدا زده نمی‌شود هم اجرا می‌شود، و `SELECT *` هش رمز را هم می‌آورد. مورد ۲ روی جدولی با **۱۲ ایندکس** می‌نویسد — هر UPDATE باید همه‌ی ایندکس‌های مرتبط را به‌روز کند.

**اصلاح:**
- `userType` را به کوئری اعتبارسنجی نشست JOIN کنید (۳ کوئری → ۱)
- `last_activity` را throttle کنید: `... AND last_activity < NOW() - INTERVAL 60 SECOND` (کاهش ~۹۵٪ نوشتن)
- `SELECT *` → `SELECT userType`

### P-02 🟠 — بار polling ضرب‌شونده

هم‌زمان چند حلقه‌ی مستقل در حال poll هستند:

| منبع | بازه |
|---|---|
| `SelectInfoScreen` | ۳۰ ثانیه (۲ فراخوانی: `refreshData` + `fetchRealTimeData`) |
| `CargoDetailsScreen` | ۳۰ ثانیه |
| `CargoCounterScreen` | ۳۰ ثانیه |
| `RegisterCargoScreen` | ۱ ثانیه (!) |
| `ChatViewModel` | `POLLING_INTERVAL` |
| `PermissionPoller` | `POLL_INTERVAL_MS` |
| `LoadingNotificationService` | ۱ دقیقه (foreground service) |

هر کدام timer و کش خودش را دارد، بدون هماهنگی. با ۲۰ کاربر فعال، این یعنی ده‌ها درخواست بر ثانیه به سرور برای داده‌ای که اغلب تغییر نکرده. حلقه‌ی ۱ ثانیه‌ای در `RegisterCargoScreen:542` مخصوصاً باید بررسی شود.

**اصلاح:** یک `PollingCoordinator` مشترک با backoff (وقتی داده تغییر نکرده، بازه را دو برابر کن تا سقف مشخص)، و استفاده‌ی جدی‌تر از `ETag`/`If-None-Match` — زیرساختش سمت سرور (`sendCacheableJsonResponse`) از قبل هست ولی فقط روی چند endpoint اعمال شده.

### P-03 🟡 — پروکسی، هزینه‌ی هر درخواست را دوبرابر می‌کند

`protected_proxy.php` هر درخواست را با `include $target` اجرا می‌کند که خودش دوباره `bootstrap.php` را بار می‌کند. ضمناً کل خروجی در `ob_start()` بافر می‌شود (تأخیر TTFB) و سپس یکجا echo می‌شود.

**نکات مثبت موجود:** کش APCu روی whitelist، نوشتن لاگ بعد از `fastcgi_finish_request()`. هر دو خوب انجام شده‌اند.

**پیشنهاد:** جایگزینی پروکسی با یک router واقعی (`index.php` + جدول مسیرها) — یک bootstrap، بدون `include` پویا، بدون بافر کامل خروجی، و ضمناً حذف کل کلاس آسیب‌پذیری «هر `*.php` در پوشه = یک endpoint» (که S-02 و S-09 از آن ناشی می‌شوند).

### P-04 🟡 — ۱۲ ایندکس روی `user_sessions`

```
idx_username, idx_device_id, idx_is_active, idx_login_time, idx_last_activity,
idx_session_token, idx_userType, idx_username_active, idx_device_active,
idx_active_activity, idx_username_device_active, idx_userType_active
```

بسیاری از این‌ها زیرمجموعه‌ی دیگری هستند (`idx_username` ⊂ `idx_username_active` ⊂ `idx_username_device_active`). MySQL برای هر INSERT/UPDATE باید همه را نگه‌داری کند — و با توجه به P-01، این جدول پرنویس‌ترین جدول سیستم است.

**اصلاح:** نگه‌داشتن `idx_username_device_active`، `idx_active_activity`، `idx_session_token`؛ حذف بقیه. (اول با `sys.schema_unused_indexes` تأیید شود.)

### P-05 🟡 — `SELECT *` روی مسیر داغ

`UserRepository::getByUsername()` — `SELECT * FROM Users` که در هر درخواست احرازشده اجرا می‌شود و هش رمز، `fcm_token` و بقیه‌ی ستون‌ها را غیرضروری منتقل می‌کند. همین‌طور `LicenseController` دو بار `SELECT * FROM licenses`.

### P-06 🟡 — بافر خروجی و gzip تکراری

الگوی زیر در **۶ کنترلر** تکرار شده:
```php
if (extension_loaded('zlib') && !ini_get('zlib.output_compression') && !in_array('ob_gzhandler', ob_list_handlers(), true)) {
    ob_start('ob_gzhandler');
}
```
درحالی‌که `.htaccess` از قبل `mod_deflate` را برای `application/json` فعال کرده. فشرده‌سازی دوباره در PHP هزینه‌ی CPU اضافه است. بهتر است فقط به سطح وب‌سرور تکیه شود.

### P-07 🟢 — نکات مثبت پرفورمنس (حفظ شوند)

- `ConnectionPool(10, 5, MINUTES)` در OkHttp با توجه به الگوی polling، انتخاب درستی است
- کش دیسک ۱۰MB HTTP + پشتیبانی ETag سمت سرور
- `MicroCache` با APCu و `forget()` صریح بعد از نوشتن — الگوی درست invalidation
- `Logger` با بافر و flush در `register_shutdown_function`
- Baseline Profile فعال + آرشیو خودکار `mapping.txt`
- ایندکس‌های ترکیبی `CargoInfo` (`idx_cargo_status_group`, `idx_cargo_ship_lookup`) دقیقاً با کوئری‌های `getInitialInfo` هم‌راستا هستند — طراحی خوبی است

### P-08 🟢 — `Response::json()` و بافر

`ob_start('ob_gzhandler')` **بعد از** `http_response_code()` و ارسال هدرها فراخوانی می‌شود؛ در برخی پیکربندی‌های SAPI این ترتیب باعث می‌شود gzip اعمال نشود و صرفاً هزینه‌ی بافر بماند. بررسی و ساده‌سازی شود.

---

## ۶. پیشنهادات و ایده‌های قابل پیاده‌سازی

### 💡 I-01 — Router واقعی به‌جای پروکسی فایل‌محور (بیشترین بازده)

مدل فعلی «هر فایل `.php` در پوشه = یک endpoint عمومی» ریشه‌ی S-01، S-02، S-04، S-09 و S-11 است: **فراموش کردن گیت احراز هویت روی یک فایل جدید، به‌طور خودکار یک endpoint باز می‌سازد.**

یک `index.php` با جدول مسیرهای صریح این کلاس مشکل را از بین می‌برد:

```php
return [
    'users.create'  => [UserController::class, 'create',  'auth' => true, 'perm' => 'manage_users'],
    'auth.login'    => [AuthController::class, 'login',   'auth' => false, 'throttle' => '5/15m'],
    'chat.messages' => [ChatController::class, 'index',   'auth' => true, 'perm' => 'admin_chat'],
];
```

مزایا: احراز هویت پیش‌فرض روشن (opt-out به‌جای opt-in)، یک bootstrap به‌جای دو، حذف `include` پویا، و امکان تعریف متمرکز rate-limit.

### 💡 I-02 — تست خودکار برای منطق محاسباتی

منطق تناژ و درصد (`calculateLoadableTonnage`، `getActiveQuotasRemaining`، `getShipQuotasRemaining`) مستقیماً روی اعداد عملیاتی اثر می‌گذارد و هیچ تستی ندارد. شروع با ~۲۰ تست PHPUnit روی همین توابع، بیشترین کاهش ریسک را به‌ازای کمترین تلاش می‌دهد.

### 💡 I-03 — ممیزی تغییرات (Audit Trail)

الان `session_activity.log` فقط ورود/خروج را ثبت می‌کند. یک جدول `audit_log` (کاربر، action، جدول، رکورد، مقدار قبل/بعد، زمان، IP) برای عملیات حساس — حذف حواله، ویرایش کوتاژ، تغییر تناژ موقت، ساخت/حذف کاربر — هم نیاز انطباقی را پوشش می‌دهد و هم عیب‌یابی اختلافات عملیاتی را ممکن می‌کند.

### 💡 I-04 — جایگزینی polling با WebSocket/SSE

با توجه به P-02، حتی یک SSE ساده (`text/event-stream`) روی همین PHP، بار سرور را چند برابر کم می‌کند و هم‌زمان تأخیر به‌روزرسانی را از ۳۰ ثانیه به ~۱ ثانیه می‌رساند — یعنی هم سریع‌تر، هم سبک‌تر. برای چت و «بارگیری لحظه‌ای» بیشترین ارزش را دارد.

### 💡 I-05 — تازه‌سازی توکن (Refresh Token)

با اصلاح S-05 (انقضای نشست)، کاربران هر ۲۴ ساعت مجبور به ورود مجدد می‌شوند. الگوی access token کوتاه‌مدت (۱۵ دقیقه) + refresh token بلندمدت با چرخش، هم امنیت را حفظ می‌کند و هم تجربه‌ی کاربری را. زیرساخت `session_token` موجود قابل توسعه به این مدل است.

### 💡 I-06 — CI ساده روی GitHub Actions

- `./gradlew lintRelease assembleRelease` با `abortOnError = true`
- `php -l` روی همه‌ی فایل‌های PHP (سه باگ B-01 تا B-04 با تحلیل استاتیک قابل تشخیص بودند)
- افزودن **PHPStan سطح ۵** — دقیقاً متدهای ناموجود `Request::post()` و `cleanupExpiredSessions()` را می‌گیرد

### 💡 I-07 — حالت آفلاین برای ثبت حواله

اپ در محیط بندری استفاده می‌شود که پوشش شبکه ناپایدار است. Room از قبل در پروژه هست (فعلاً فقط برای چت). صف ثبت آفلاین با همگام‌سازی خودکار هنگام بازگشت شبکه، ارزش عملیاتی مستقیم دارد.

### 💡 I-08 — یکپارچه‌سازی مدیریت خطا در کلاینت

`FloatTypeAdapter` در هر خطا `0f` برمی‌گرداند و `CryptoManager` در خطا رشته‌ی خالی. هر دو fail-safe هستند اما **بی‌صدا** — کاربر عدد `۰` می‌بیند بدون اینکه بداند داده نامعتبر بوده. یک `Result<T>` یا لاگ‌کردن این حالت‌ها در Crashlytics، عیب‌یابی میدانی را بسیار ساده‌تر می‌کند.

---

## ۷. نقشه‌ی راه پیشنهادی

### گام ۰ — فوری (امروز، < ۲ ساعت)
| # | اقدام | فایل |
|---|---|---|
| ۱ | حذف `proxy_generator.php` از سرور | S-02 |
| ۲ | افزودن `AuthenticatesRequests` + `requirePermission('manage_users')` به `UserController` | S-01 |
| ۳ | افزودن `AuthenticatesRequests` به `OnlineUsersController` و حذف `ACAO: *` | S-04 |
| ۴ | حذف `check_table_structure.php` و محدودکردن `export_schema.php` به CLI | B-04, S-09 |

### گام ۱ — این هفته
| # | اقدام | مرجع |
|---|---|---|
| ۵ | هویت چت از `authenticatedUsername` (نه از بدنه‌ی درخواست) | S-03 |
| ۶ | انقضای نشست بر اساس `last_activity` + پیاده‌سازی `cleanupExpiredSessions` | S-05, B-02 |
| ۷ | rate-limit ورود با الگوی `PasswordGateService` | S-06 |
| ۸ | احراز هویت `update_fcm_token` و `checkExistence` | S-08, S-11 |
| ۹ | رفع `Request::post()` و اصلاح `generateDailyStats.php` | B-01, B-03 |
| ۱۰ | چرخش اعتبارنامه‌های شرکت ثالث + حذف کامنت‌های `// Original:` | S-12 |
| ۱۱ | خالی‌کردن `ADMIN_PASSWORD_HASH` در `.env.example` و حذف fallback کلید API | S-13, S-14 |

### گام ۲ — این ماه
- بازطراحی مسیریابی به router صریح (I-01) — همزمان S-10، S-16، S-19، S-21 و P-03 را می‌بندد
- یکپارچه‌سازی گیت‌های احراز هویت و حذف `sendJsonResponse`/`sanitizeInput` تکراری (C-01، C-02)
- بهینه‌سازی مسیر احراز هویت: ۳ کوئری → ۱، throttle روی `last_activity`، هرس ایندکس‌ها (P-01، P-04، P-05)
- راه‌اندازی CI با PHPStan و lint سخت‌گیرانه (I-06، C-08)

### گام ۳ — میان‌مدت
- شکستن `AppApiController` به Service‌های مجزا (C-05)
- تست‌های واحد برای منطق تناژ (I-02)
- `PollingCoordinator` مشترک یا مهاجرت به SSE (P-02، I-04)
- مهاجرت رمز عبور به bcrypt واقعی با رمز خام روی TLS (S-07)
- جدول audit trail (I-03)

---

## پیوست — فهرست کامل یافته‌ها

**امنیت:** S-01 users_api بدون auth · S-02 proxy_generator باز · S-03 جعل هویت چت · S-04 OnlineUsers بدون auth · S-05 نشست بدون انقضا · S-06 بدون قفل ورود · S-07 pass-the-hash · S-08 fcm_token بدون auth · S-09 export_schema روی HTTP · S-10 IP قابل جعل · S-11 oracle وجود کوتاژ · S-12 اعتبارنامه در APK · S-13 کلید API hardcode · S-14 هش ادمین در env.example · S-15 بدون pinning · S-16 پوشه‌ی log باز · S-17 display_errors · S-18 تزریق نام ستون · S-19 race در rate-limit · S-20 syncPermissions بدون توکن · S-21 htaccess بی‌اثر

**باگ:** B-01 `Request::post()` · B-02 `cleanupExpiredSessions()` · B-03 generateDailyStats · B-04 مسیر check_table_structure

**کیفیت:** C-01 گیت سه‌گانه · C-02 توابع تکراری · C-03 سه مسیر DB · C-04 sanitize نادرست · C-05 فایل‌های بزرگ · C-06 کد مرده · C-07 بدون تست · C-08 lint خاموش · C-09 دو کتابخانه‌ی سریال‌سازی

**پرفورمنس:** P-01 ۳ کوئری در هر درخواست · P-02 polling ضرب‌شونده · P-03 هزینه‌ی پروکسی · P-04 ایندکس اضافی · P-05 SELECT * · P-06 gzip تکراری · P-07 نکات مثبت · P-08 ترتیب بافر

**ایده:** I-01 router · I-02 تست محاسبات · I-03 audit trail · I-04 SSE · I-05 refresh token · I-06 CI · I-07 حالت آفلاین · I-08 خطاهای بی‌صدا
