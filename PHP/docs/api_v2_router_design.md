# طراحی API نسخه‌ی ۲ (Router بازطراحی‌شده)

**هدف:** جایگزینی مدل فعلی «هر فایل `.php` در پوشه = یک endpoint عمومی که پروکسی آن را `include` می‌کند» با یک router صریح، مبتنی بر جدول route، که هم مسیر URL تمیز داشته باشد و هم گیت احراز هویت/مجوز هر route را در یک نقطه‌ی قابل‌ممیزی اعلام کند.

**وضعیت:** سند طراحی — قبل از هرگونه پیاده‌سازی، نقاط تصمیمی که در بخش ۶ آمده باید مشخص شوند.

---

## ۱. معماری فعلی (سه لایه)

| لایه | نمونه | مسیر دسترسی | گیت فعلی |
|---|---|---|---|
| **۱. پروکسی‌شده** | `app_api.php`, `users_api.php`, `check_Auth.php`, ۲۰ فایل دیگر | `protected_proxy.php?target=X.php&action=Y` | rate-limit عمومی IP + whitelist فایلی (`glob('*.php')` منهای لیست استثنا) + هرکنترلر گیت احراز هویت خودش را دارد |
| **۲. URL مستقیم** | `check_signature.php`, `validate_license.php`, `get_license_info.php` | URL کامل hardcode در `secrets.cpp` (`Secrets.getSignatureCheckUrl()` و...) | امضای برنامه / کلید لایسنس، بدون عبور از پروکسی |
| **۳. پنل مستقل** | `PermissionManager.php`, `online_users_api.php`, `file_manager.php` | مستقیم، نشست PHP سراسری (`$_SESSION['perm_manager_auth']`) | لاگین جدا، خارج از مدل اپ اندروید |

این سند فقط **لایه‌ی ۱** را بازطراحی می‌کند — ۴۰ متد Retrofit در `ApiService.kt` که هرکدام روی ۲۰ فایل `target=` نگاشت می‌شوند. لایه‌های ۲ و ۳ خارج از دامنه‌ی این تغییرند.

---

## ۲. مشکل دقیق مدل فعلی

`protected_proxy.php` یک whitelist **opt-out** دارد: هر فایل `.php` جدید در پوشه‌ی `PHP/` به‌طور خودکار از طریق پروکسی قابل دسترسی است، مگر اینکه صراحتاً به `PROXY_EXCLUDED_FILES` اضافه شود. این دقیقاً ریشه‌ی سه یافته‌ی امنیتی بود که در همین جلسه بسته شد:

- `proxy_generator.php` — ابزار توسعه که فراموش شد از پروکسی مستثنا نشود
- `check_table_structure.php` — همین‌طور
- `export_schema.php` — در whitelist بود تا وقتی صریحاً اضافه نشد

و مهم‌تر: حتی وقتی یک فایل **قصداً** یک endpoint است (مثل `users_api.php`)، هیچ مکانیزمی در سطح پروکسی تضمین نمی‌کند که کنترلر پشت آن واقعاً `requireAuthenticatedSession()` را صدا زده باشد — این دقیقاً همان چیزی بود که در `UserController` فراموش شده بود (S-01). پروکسی از وجود/نبود احراز هویت داخل فایلی که `include` می‌کند **هیچ اطلاعی ندارد**.

---

## ۳. معماری پیشنهادی

### ۳.۱ الگوی مسیر (Route Pattern)

**تصمیم طراحی:** نه REST خالص (resource+HTTP-verb برای هر عملیات)، نه ادامه‌ی الگوی فعلی `action=` در query string — بلکه **مسیرهای اکشن‌محور تمیز** (`/api/v2/{resource}/{action}`):

```
GET  /api/v2/ships                          (قبلاً: app_api.php?action=getShipsList)
GET  /api/v2/ships/{shipName}                (قبلاً: app_api.php?action=getShipDetails)
GET  /api/v2/quotas/{quotaNumber}            (قبلاً: app_api.php?action=getQuotaDetails)
POST /api/v2/quotas/{id}/percentage          (قبلاً: app_api.php?action=updateQuotaPercentage)
POST /api/v2/cargo                           (قبلاً: saveOrUpdateCargoInfo.php)
POST /api/v2/auth/login                      (قبلاً: check_Auth.php)
```

**چرا نه REST خالص:** حدود ۱۵ از ۴۰ endpoint فعلی ماهیتاً «اکشن» هستند نه CRUD روی یک resource (`toggleQuotaStatus`, `updateTemporaryTonnage`, `logAnalyticsExport`, `checkQuotaExistenceCargo`). تبدیل این‌ها به REST خالص (مثلاً `PATCH /quotas/{id}` با بدنه‌ی partial) نیاز به بازنویسی منطق داخلی هر کنترلر دارد (تشخیص این‌که کدام فیلد از بدنه تغییر کرده)، بدون فایده‌ی واقعی برای این پروژه (یک API داخلی با یک مصرف‌کننده، نه یک API عمومی چندمصرف‌کننده). مسیرهای اکشن‌محور همان منطق فعلی کنترلرها را دست‌نخورده نگه می‌دارند و فقط لایه‌ی مسیریابی را عوض می‌کنند.

**چرا نه ادامه‌ی `action=` در query:** چون همان مشکل فعلی (هیچ چیز مسیر را از فایل فیزیکی جدا نمی‌کند) را حل نمی‌کند مگر مسیر از یک جدول صریح عبور کند، نه از `include` پویا.

### ۳.۲ مکانیزم سرور (Front Controller)

```
PHP/
  public/                    ← تنها پوشه‌ی expose‌شده به وب (DocumentRoot جدید)
    index.php                ← تنها نقطه‌ی ورود؛ .htaccess همه‌چیز را به این می‌فرستد
    .htaccess                ← RewriteRule ^(.*)$ index.php [QSA,L]
  src/
    Core/
      Router.php             ← کلاس dispatch: تطبیق path+method با routes.php
    routes/
      api_v2.php              ← آرایه‌ی route table (بخش ۳.۳)
    Controllers/               ← دست‌نخورده (همان کنترلرهای فعلی، فقط متدهایشان از router صدا زده می‌شود نه از فایل wrapper)
```

`index.php` (شبه‌کد):

```php
require_once __DIR__ . '/../src/bootstrap.php';

$routes = require __DIR__ . '/../src/routes/api_v2.php';
$router = new \App\Core\Router($routes);
$router->dispatch(
    $_SERVER['REQUEST_METHOD'],
    parse_url($_SERVER['REQUEST_URI'], PHP_URL_PATH)
);
```

`Router::dispatch()` مسیر را با جدول تطبیق می‌دهد، پارامترهای مسیر (`{id}`, `{shipName}`) را استخراج می‌کند، **قبل از فراخوانی کنترلر** اگر route با `'auth' => true` علامت خورده باشد خودش هدرهای `X-Session-Token` را با `SessionService::validateAndGetUserType()` بررسی می‌کند (همان چیزی که الان `AuthenticatesRequests` انجام می‌دهد)، و اگر `'permission' => 'feature'` داشته باشد آن را هم چک می‌کند — یعنی **حتی اگر کنترلر خودش هم گیت را صدا نزند، router جلوی درخواست غیرمجاز را می‌گیرد** (دفاع دوگانه، نه جایگزین گیت داخل کنترلر).

### ۳.۳ نمونه‌ی جدول Route (کامل برای بخش Quotas، الگو برای بقیه)

```php
// PHP/src/routes/api_v2.php
return [
    // path                              method  controller           action                  auth   permission
    'ships'                          => ['GET',  ShipController::class,  'list',                true,  null],
    'ships/{shipName}'               => ['GET',  ShipController::class,  'details',             true,  null],
    'ships/{shipName}/warehouses/{warehouseName}' => ['GET', WarehouseController::class, 'details', true, null],

    'quotas/{quotaNumber}'           => ['GET',  QuotaController::class, 'details',             true,  null],
    'ships/{shipName}/quotas'        => ['GET',  QuotaController::class, 'listForShip',         true,  null],
    'quotas/filtered'                => ['GET',  QuotaController::class, 'filtered',            true,  null],
    'quotas/{id}/edit'               => ['POST', QuotaController::class, 'edit',                true,  'manage_quotas'],
    'quotas/{id}/percentage'         => ['POST', QuotaController::class, 'updatePercentage',    true,  'manage_quotas'],
    'quotas/{id}/toggle-status'      => ['POST', QuotaController::class, 'toggleStatus',        true,  'manage_quotas'],
    'quotas/{id}/percentage-restriction' => ['POST', QuotaController::class, 'updatePercentageRestriction', true, 'manage_quotas'],
    'quotas/delete'                  => ['POST', QuotaController::class, 'delete',              true,  'manage_quotas'],
    'quotas/{quotaNumber}/status'    => ['GET',  QuotaController::class, 'checkStatus',         true,  null],
    'quotas/{quotaNumber}/loadable-tonnage' => ['GET', QuotaController::class, 'loadableTonnage', true, null],
    'quotas/{quotaNumber}/temporary-tonnage' => ['POST', QuotaController::class, 'updateTemporaryTonnage', true, 'manage_quotas'],

    'auth/login'                     => ['POST', AuthController::class,  'login',               false, null],
    'auth/session'                   => ['POST', AuthController::class,  'checkSession',        false, null],
    'auth/logout'                    => ['POST', AuthController::class,  'logout',              false, null],

    'users'                          => ['GET',  UserController::class,  'list',                true,  'manage_users'],
    'users'                          => ['POST', UserController::class,  'create',              true,  'manage_users'],
    'users/{id}'                     => ['PATCH', UserController::class, 'update',              true,  null], // خودِ کنترلر تشخیص self/admin می‌دهد
    'users/{id}'                     => ['DELETE', UserController::class, 'delete',             true,  'manage_users'],
    // ... (۱۶ ردیف باقی‌مانده برای cargo/chat/search طبق همین الگو)
];
```

> این جدول تمام گیت‌های احراز هویت/مجوز پروژه را در **یک فایل** قابل مرور می‌کند — چیزی که الان در ۹ کنترلر پراکنده است.

---

## ۴. تغییرات سمت کلاینت

هر ۴۰ متد `ApiService.kt` باید از `target=`/`action=` query param به Retrofit annotations استاندارد (`@Path`, HTTP-verb واقعی) مهاجرت کنند. نمونه:

```kotlin
// قبل
@POST("protected_proxy.php")
suspend fun updateQuotaPercentage(
    @Query("target") target: String = "app_api.php",
    @Query("action") action: String = "updateQuotaPercentage",
    @Query("id") id: Int,
    @Query("percentage") percentage: Double,
    @Query("isEnabled") isEnabled: Int
): Response<SuccessResponse>

// بعد
@POST("api/v2/quotas/{id}/percentage")
suspend fun updateQuotaPercentage(
    @Path("id") id: Int,
    @Body request: UpdatePercentageRequest
): Response<SuccessResponse>
```

`RetrofitClient`'s `headersInterceptor` (که `X-Username`/`X-Device-Id`/`X-Session-Token`/`X-App-Version` را اضافه می‌کند) **بدون تغییر** باقی می‌ماند — این بخش به شکل URL کاری ندارد.

تخمین حجم: ۴۰ متد در `ApiService.kt` + دیتاکلاس‌های Request جدید برای اکشن‌هایی که الان پارامترشان در query string پخش است (باید در یک body object جمع شوند) + هر call-site که این متدها را صدا می‌زند (حدود ۱۵ فایل ViewModel/Repository) باید verify شود که امضای جدید را درست صدا می‌زند.

---

## ۵. برنامه‌ی Rollout (فازبندی)

| فاز | اقدام | شرط عبور به فاز بعد |
|---|---|---|
| **۱. ساخت موازی** | `PHP/public/` + Router + `routes/api_v2.php` ساخته می‌شود؛ `protected_proxy.php` و همه‌ی فایل‌های `target=` فعلی **بدون هیچ تغییری** به کار خود ادامه می‌دهند | v2 روی همان سرور، پشت `/api/v2/`، قابل تست مستقل از اپ فعلی |
| **۲. کلاینت جدید** | نسخه‌ی جدید اپ با `ApiService.kt` مهاجرت‌یافته ساخته و در محیط داخلی/بتا تست می‌شود | تست دستی کامل هر ۴۰ endpoint در محیط staging |
| **۳. انتشار تدریجی** | نسخه‌ی جدید منتشر می‌شود؛ `v1` (پروکسی فعلی) و `v2` **هم‌زمان** روی سرور فعال می‌مانند | لاگ‌های سرور نشان دهند سهم ترافیک v1 به‌مرور افت می‌کند |
| **۴. جمع‌آوری v1** | وقتی سهم ترافیک v1 به یک آستانه‌ی مشخص (مثلاً <۱٪) رسید **یا** یک ددلاین از پیش اعلام‌شده فرارسید، `min_allowed_version` برای اجبار به‌روزرسانی باقی‌مانده‌ها بالا برده می‌شود | بعد از یک پنجره‌ی امن (مثلاً ۲ هفته پس از جمع‌آوری)، `protected_proxy.php` و تمام فایل‌های `target=` قدیمی حذف می‌شوند |

**نکته‌ی مهم:** در تمام فاز ۱ تا ۳، `protected_proxy.php` با همان مدل whitelist فعلی زنده می‌ماند. اگر می‌خواهید این پنجره (که می‌تواند هفته‌ها/ماه‌ها طول بکشد) هم از همان کلاس آسیب‌پذیری در امان باشد، سخت‌کردن whitelist فعلی (گزینه‌ی A که قبلاً مطرح شد) یک اقدام مستقل و کم‌هزینه است که در هر زمان — حتی موازی با اجرای این طراحی — قابل انجام است.

---

## ۶. نقاط تصمیمی که نیاز به پاسخ شما دارند

قبل از شروع پیاده‌سازی، این موارد باید مشخص شوند چون مستقیم روی شکل کد اثر می‌گذارند:

1. **سیاست جمع‌آوری v1:** آیا مجازید در نهایت با `min_allowed_version` نصب‌های قدیمی را مجبور به آپدیت کنید؟ اگر نه (مثلاً به‌خاطر کاربرانی با دستگاه‌های قدیمی/بدون دسترسی به فروشگاه)، `protected_proxy.php` باید **برای همیشه** نگه‌داشته و نگهداری شود (یعنی هیچ‌وقت واقعاً به یک router واحد نمی‌رسید، بلکه دو سیستم موازی برای همیشه).
2. **کانال توزیع اپ:** آیا نصب از طریق دامنه‌ی اختصاصی (`atk-nk.ir`) است یا Play Store؟ این مستقیماً روی سرعت رسیدن کاربران به نسخه‌ی جدید (و در نتیجه طول فاز ۳) اثر می‌گذارد.
3. **دامنه‌ی نام‌گذاری مسیرها:** آیا `/api/v2/...` روی همان دامنه (`atk-nk.ir/Cargo/test_api/`) قابل قبول است، یا مسیر دیگری مد نظر دارید؟
4. **زمان‌بندی:** آیا این کار در یک بازه‌ی مشخص (مثلاً «قبل از نسخه‌ی بعدی») باید تمام شود، یا فرصت کافی برای فازبندی کامل (رول‌اوت تدریجی + دوره‌ی هم‌پوشانی) هست؟

---

## ۷. برآورد حجم کار

| بخش | برآورد |
|---|---|
| `Router` + `routes/api_v2.php` + `public/index.php` | یک جلسه‌ی کاری |
| بازنویسی ۹ کنترلر برای کار با پارامترهای route (به‌جای `$this->request->get()`) | چند جلسه — اکثر منطق داخلی دست‌نخورده می‌ماند |
| مهاجرت ۴۰ متد `ApiService.kt` + دیتاکلاس‌های Request جدید | یک جلسه‌ی کاری متمرکز |
| تست دستی کامل هر ۴۰ endpoint (چون تغییر مسیر یعنی هر مسیر شبکه در اپ باید دوباره تست شود) | این بخش زمان‌بر‌ترین قسمت است، خارج از کاری که من می‌توانم بدون build/run واقعی اپ روی دستگاه انجام دهم |
| هماهنگی انتشار (build امضاشده، توزیع، مانیتور رول‌اوت) | خارج از دسترسی من — نیاز به کلید امضا و کانال توزیع شما دارد |
