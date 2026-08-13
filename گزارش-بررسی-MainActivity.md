# گزارش جامع بررسی `MainActivity` (نقطه ورود برنامه)

تاریخ بررسی: ۱۴۰۵/۰۵/۲۲ (۲۰۲۶-۰۸-۱۳) · شاخه: `main` · کامیت پایه: `f4d81ba`

---

## ۱. دامنه بررسی و نقشه جریان

`MainActivity` تنها Activity برنامه است (Single Activity Architecture) و همزمان سه نقش دارد:
میزبان Compose، ماشین حالت راه‌اندازی (splash/sync/security/update/ready)، و پل بین Composeها و APIهای سطح Activity (مجوز، startActivity، Toast).

**کلاینت (Android/Compose)**

| لایه | فایل |
|---|---|
| نقطه ورود، ماشین حالت UI، دیالوگ آپدیت | `app/src/main/java/com/atk/atk_cargo/MainActivity.kt` |
| ماشین حالت راه‌اندازی، توالی شبکه | `.../core/startup/StartupViewModel.kt` |
| CompositionLocalها | `.../core/startup/StartupComposition.kt` |
| بررسی امضا/لایسنس/محیط | `.../security/SecurityVerifier.kt` |
| بررسی نسخه + دانلود/نصب APK | `.../api/UpdateManager.kt` |
| ترجیحات (DataStore) | `.../api/UserPreferencesManager.kt` |
| دیالوگ آپدیت | `.../feature/update/presentation/UpdateDialog.kt` |
| DI | `.../di/AppModule.kt:30, 33, 55, 56` |
| مانیفست | `app/src/main/AndroidManifest.xml` |
| FileProvider paths | `app/src/main/res/xml/file_path.xml` |
| Network Security Config | `app/src/main/res/xml/network_security_config.xml` |

**سرور (PHP)**

| لایه | فایل |
|---|---|
| بررسی نسخه/آپدیت | `PHP/check_update.php` → `PHP/src/Controllers/UtilityController.php:182-228` |
| پیکربندی آپدیت | `PHP/update_config.php` |
| بررسی امضای اپ | `PHP/check_signature.php` → `UtilityController.php:35-89` |
| بررسی نشست | `PHP/check_session.php` → `PHP/src/Controllers/AuthController.php:131-173` |
| منطق نشست | `PHP/src/Services/SessionService.php:165-180` |
| لایسنس | `PHP/validate_license.php` · `PHP/get_license_info.php` |

**جریان راه‌اندازی:**

```
installSplashScreen() (تم سیستمی)
  → runBlocking { themeColor.first() }        ← مسدودسازی Main Thread
  → setContent
      ├─ LaunchedEffect: handleIntent(intent) + runStartupSequenceOnce()
      ├─ LaunchedEffect: collect(events) → Toast / تنظیمات باتری
      └─ when(startupState):
           Splash → SplashScreen (کامپوزبل دوم)
           Syncing → ServerSyncingScreen
           VersionExpired → VersionExpiredDialog → killProcess()
           SecurityBlocked → SecurityBlockScreen
           Ready → HandleMainContent()
                     ├─ isUpdateAvailable → UpdateDialog  (MainScreen اصلاً ساخته نمی‌شود)
                     └─ else → MainScreen()

توالی شبکه (موازی، Dispatchers.IO):
  checkVersionAndUpdate() ─── GET  check_update.php?current_version&api_key
  performAppSecurityCheck() ─┬ POST check_signature.php
                             ├ GET  get_license_info.php?licenseKey=...
                             └ POST validate_license.php
  checkUserSessionAsync() ─── POST check_session.php
```

---

## ۲. خلاصه مدیریتی

| حوزه | وضعیت | مهم‌ترین مورد |
|---|---|---|
| باگ عملکردی | 🔴 بحرانی | با وجود هر آپدیت (حتی `force_update=false`)، `MainScreen` **هرگز ساخته نمی‌شود** و `onDismiss` خالی است ⇒ کاربر تا نصب آپدیت به‌کلی از برنامه قفل می‌شود |
| پرفورمنس | 🔴 بحرانی | `runBlocking` روی Main Thread در `onCreate:65` برای خواندن DataStore ⇒ ریسک ANR و تأخیر اولین فریم در هر cold start |
| امنیت (کلاینت) | 🔴 بحرانی | APK آپدیت **بدون هیچ بررسی صحت** (SHA-256/امضا) در `externalCacheDir` دانلود و نصب می‌شود؛ `file_path.xml` کل حافظه خارجی را در معرض FileProvider گذاشته است |
| امنیت (سرور) | 🔴 بحرانی | `sessionToken` از کلاینت ارسال می‌شود ولی سرور **هرگز آن را اعتبارسنجی نمی‌کند**؛ هویت نشست فقط `username` + `deviceId` است که هیچ‌کدام سرّی نیستند |
| قرارداد کلاینت/سرور | 🟠 جدی | سرور فیلدهای آپدیت را زیر `updateInfo` می‌فرستد، کلاینت آن‌ها را در **سطح ریشه** می‌خواند ⇒ پیام، حجم، اولویت و `forceUpdate` همیشه مقدار پیش‌فرض می‌گیرند |
| باگ | 🟠 جدی | `try/catch` روی کل `onCreate` ⇒ در صورت خطا کاربر فقط **صفحه سفید** می‌بیند؛ `CancellationException` هم بلعیده می‌شود |
| باگ | 🟠 جدی | خطای موقت ۵xx سرور در `check_session` به‌شکل `200 + success:false` برمی‌گردد ⇒ **خروج اجباری همه کاربران** |
| کیفیت کد | 🟠 متوسط | Activity سه مسئولیت دارد؛ `@SuppressLint` غیرلازم، `as UpdateInfo` ناامن، `killProcess` برای خروج، `rememberCoroutineScope` زائد |

---

## ۳. یافته‌های بحرانی (🔴)

### C-1 — قفل کامل برنامه با هر آپدیت موجود، حتی غیراجباری

`MainActivity.kt:137-165`

```kotlin
if (isUpdateAvailable && updateInfo != null) {
    UpdateDialog(..., onDismiss = { /* Handle dismiss */ })
} else {
    MainScreen()
}
```

سه مشکل روی هم انباشته شده:

1. `MainScreen()` در شاخه `else` است؛ یعنی وقتی آپدیتی وجود دارد، **کل برنامه ساخته نمی‌شود** — نه صفحه‌ای پشت دیالوگ، نه ناوبری، هیچ.
2. `onDismiss` خالی است. `UpdateDialog.kt:80-86` صراحتاً اجازه بستن با Back و کلیک بیرون را می‌دهد (`dismissOnBackPress = downloadState !is Downloading`)، ولی چون callback کاری نمی‌کند، دیالوگ بلافاصله دوباره رندر می‌شود.
3. `updateInfo.forceUpdate` در `UpdateManager.kt:194,225` پارس و ذخیره می‌شود اما **در هیچ نقطه‌ای از کد خوانده نمی‌شود** (grep روی کل ماژول اندروید تأیید شد). همین حکم برای `updatePriority`، `minAndroidVersion`، `minAppVersion`، `excludedVersions` و `releaseDate` هم برقرار است — داده مرده.

**اثر:** به‌محض اینکه `update_config.php` نسخه‌ای بالاتر از نسخه کاربر اعلام کند، تمام کاربران تا نصب موفق APK از برنامه بیرون می‌مانند. اگر لینک دانلود خراب باشد یا دستگاه فضای کافی نداشته باشد، برنامه عملاً از کار افتاده است.

**اصلاح پیشنهادی:** `MainScreen()` را همیشه بساز و دیالوگ را روی آن overlay کن؛ حالت نمایش دیالوگ را در یک `remember { mutableStateOf(true) }` نگه دار و `onDismiss` آن را false کند؛ و بستن را فقط وقتی `forceUpdate == true` است ممنوع کن.

```kotlin
var showUpdateDialog by rememberSaveable { mutableStateOf(true) }
MainScreen()
val info = updateInfo
if (isUpdateAvailable && info != null && showUpdateDialog) {
    UpdateDialog(
        updateInfo = info,
        onDismiss = { if (!info.forceUpdate) showUpdateDialog = false },
        ...
    )
}
```

---

### C-2 — `runBlocking` روی Main Thread در `onCreate`

`MainActivity.kt:65-67`

```kotlin
val initialThemeColor = kotlinx.coroutines.runBlocking {
    userPreferencesManager.themeColor.first()
}
```

کامنت بالای آن هدف را درست توضیح می‌دهد (جلوگیری از پرش رنگ در اولین فریم)، ولی راه‌حل اشتباه است: این خط اولین خواندن `DataStore` را انجام می‌دهد که شامل باز کردن فایل، رمزگشایی Proto و ساخت شیء `Preferences` روی دیسک است — همه روی Main Thread و **قبل از** اولین فریم. روی دستگاه‌های کند یا وقتی I/O دیسک تحت فشار است، این می‌تواند صدها میلی‌ثانیه طول بکشد و در بدترین حالت ANR بدهد.

نکته مهم: `installSplashScreen()` (خط ۵۸) دقیقاً برای همین سناریو `setKeepOnScreenCondition` دارد. راه درست:

```kotlin
val splash = installSplashScreen()
super.onCreate(savedInstanceState)

var initialThemeColor: Long? = null
splash.setKeepOnScreenCondition { initialThemeColor == null }
lifecycleScope.launch {
    initialThemeColor = userPreferencesManager.themeColor.first()
}
```
یا ساده‌تر: خواندن رنگ تم را به `StartupViewModel` منتقل کن و تا زمانی که `StartupState.Splash` است اصلاً تم واقعی لازم نیست.

---

### C-3 — زنجیره آپدیت APK بدون هیچ تضمین صحت

سه ضعف که با هم یک مسیر نصب کد دلخواه می‌سازند:

1. **بدون بررسی هش/امضا.** `UpdateManager.installUpdate()` (`UpdateManager.kt:525-540`) فقط سه چیز را چک می‌کند: وجود فایل، غیرصفر بودن حجم، و پسوند `.apk`. هیچ SHA-256 مورد انتظاری از سرور گرفته نمی‌شود و امضای APK دانلودشده با امضای برنامه فعلی مقایسه نمی‌شود. سرور هم در `UtilityController.php:207-224` هیچ فیلد checksum نمی‌فرستد.
2. **دانلود در حافظه مشترک.** فایل در `appContext.externalCacheDir/updates/` نوشته می‌شود (`UpdateManager.kt:308`). این مسیر روی حافظه خارجی است و بین لحظه اتمام دانلود و لحظه `ACTION_VIEW`، پنجره TOCTOU برای جایگزینی فایل وجود دارد — به‌ویژه روی دستگاه‌هایی که برنامه‌های دیگر دسترسی گسترده‌تری دارند.
3. **`file_path.xml` بیش از حد باز است.**

```xml
<external-path name="external" path="." />
```
این یعنی FileProvider حاضر است برای **هر مسیری در کل حافظه خارجی** URI بسازد، نه فقط پوشه `updates`. اگر هر جای دیگری از برنامه مسیری از ورودی غیرقابل‌اعتماد بگیرد و به `getUriForFile` بدهد، تبدیل به نشت فایل دلخواه می‌شود.

**اصلاح:**
- `internalCacheDir`/`filesDir` به‌جای `externalCacheDir` برای APK.
- محدود کردن `file_path.xml` به دقیقاً همان زیرپوشه‌های لازم (`<cache-path name="updates" path="updates/" />`).
- افزودن `sha256` به پاسخ `check_update.php` و اعتبارسنجی آن در `installUpdate` قبل از ساخت Intent.
- علاوه بر آن، مقایسه امضای APK دانلودشده با `EXPECTED_SIGNATURE_HASH` که همین حالا در `SecurityVerifier` موجود است (`PackageManager.getPackageArchiveInfo` با `GET_SIGNING_CERTIFICATES`).

---

### C-4 — `try/catch` روی کل `onCreate` ⇒ صفحه سفید بی‌صدا

`MainActivity.kt:61-128`

کل بدنه — شامل `setContent` — در یک `try` قرار دارد و `catch (e: Exception)` فقط `Log.e` می‌زند. اگر خطایی رخ دهد (مثلاً `LocalStartupViewModel` تزریق نشود، یا `runBlocking` خطا بدهد چون `safePreferences` فقط `IOException` را می‌گیرد و بقیه را دوباره پرتاب می‌کند — `UserPreferencesManager.kt:33-40`)، هیچ `setContent` ای اجرا نمی‌شود و کاربر یک Activity کاملاً خالی می‌بیند: نه پیام، نه دکمه، نه راه خروج.

ضمناً `Exception` عام، `CancellationException` را هم می‌گیرد که در کد کوروتینی هرگز نباید بلعیده شود.

**اصلاح:** در بلوک `catch` یک `setContent` جایگزین با پیام خطا و دکمه «تلاش مجدد»/«خروج» بگذار، و `CancellationException` را دوباره پرتاب کن.

---

### C-5 — (سرور) `sessionToken` ارسال می‌شود ولی هرگز اعتبارسنجی نمی‌شود

`StartupViewModel.kt:256-259` توکن نشست را در بدنه درخواست می‌گذارد:

```kotlin
val sessionRequest = SessionCheckRequest(username, deviceId, sessionToken.takeIf { it.isNotEmpty() })
```

اما `AuthController::checkSession` (`AuthController.php:131-173`) فقط `username` و `deviceId` را می‌خواند و مستقیم به `SessionService::isSessionActive($username, $deviceId)` می‌دهد (`SessionService.php:165-180`). فیلد `sessionToken` در سمت سرور **اصلاً خوانده نمی‌شود**.

**اثر امنیتی:** هویت نشست عملاً روی دو مقدار غیرسرّی بنا شده است. هر کسی که `username` و `deviceId` یک کاربر را بداند (هر دو در بدنه درخواست‌های عادی حرکت می‌کنند و روی دستگاه ذخیره می‌شوند)، می‌تواند نشست فعال را «معتبر» اعلام کند. بدتر اینکه `isSessionActive` هربار `updateLastActivity` را صدا می‌زند — یعنی همین فراخوانی، نشست را برای همیشه زنده نگه می‌دارد.

**اصلاح سمت سرور:** ستون توکن نشست را در جدول نشست‌ها ذخیره کن و در `isSessionActive` با `hash_equals()` مقایسه کن؛ نشست بدون توکن معتبر باید نامعتبر شمرده شود.

---

## ۴. یافته‌های جدی (🟠)

### S-1 — عدم تطابق قرارداد پاسخ `check_update.php`

سرور (`UtilityController.php:209-224`) این ساختار را می‌فرستد:

```json
{
  "hasUpdate": true, "latestVersion": "...", "downloadUrl": "...",
  "minAllowedVersion": "...", "changeLog": [],
  "updateInfo": { "priority": "...", "message": "...", "forceUpdate": false,
                  "size": "...", "releaseDate": "...", "minAndroidVersion": 21 }
}
```

اما کلاینت (`UpdateManager.kt:176-231`) همه این فیلدها را در **سطح ریشه** جستجو می‌کند (`update_priority`/`updatePriority`, `update_message`/`updateMessage`, `force_update`/`forceUpdate`, `update_size`/`updateSize`, `release_date`/`releaseDate`) و `version_constraints` را هم در سطح ریشه می‌خواند — چیزی که سرور اصلاً نمی‌فرستد.

**نتیجه عملی:** `updateMessage` همیشه `""` است ⇒ `ModernUpdateContent(message = "")` یک بدنه خالی نشان می‌دهد. `updatePriority` همیشه `"normal"`، `forceUpdate` همیشه `false`، `excludedVersions` همیشه خالی. تنها دلیل اینکه حجم فایل درست نمایش داده می‌شود، درخواست HEAD جبرانی در `UpdateManager.kt:203-217` است — یعنی یک RTT اضافه برای جبران باگی که با تصحیح مسیر JSON حل می‌شود.

همچنین کلاینت فیلد `hasUpdate` سرور را کاملاً نادیده می‌گیرد و خودش با `compareVersions` تصمیم می‌گیرد (`UpdateManager.kt:166-172`) — منطق تکراری در دو طرف با دو الگوریتم متفاوت (`version_compare` در PHP در برابر پارس دستی در Kotlin).

**اصلاح:** یکی از دو طرف را با دیگری هم‌راستا کن. ترجیح: سرور ساختار مسطح `snake_case` بفرستد و کلاینت فقط همان را بخواند و به `hasUpdate` سرور اعتماد کند.

---

### S-2 — خروج اجباری همه کاربران با یک خطای موقت سرور

`PHP/check_session.php:12-21`

```php
} catch (Exception $e) {
    Response::json([...'success' => false...], 200); // 200 جهت پایداری با کلاینت اندروید
}
```

کلاینت (`StartupViewModel.kt:260-266`) به‌درستی بین «خطای شبکه» و «رد صریح سرور» تفاوت می‌گذارد و کامنت خوبی هم دارد: پاسخ صریح سرور همیشه fail-closed است و grace period نمی‌گیرد. اما سرور با برگرداندن `200 + success:false` روی خطای داخلی، یک خطای گذرا (قطع دیتابیس، timeout) را به‌عنوان **رد صریح** جا می‌زند.

**اثر:** یک دقیقه قطعی MySQL ⇒ `isValid = false` ⇒ `clearUserCredentials()` و پیام «لطفاً دوباره وارد شوید!» برای همه کاربرانی که در آن بازه برنامه را باز کرده‌اند (`StartupViewModel.kt:175-178`).

همین مشکل در `AuthController.php:150-156` هم هست: «کاربر وجود ندارد» با کد ۲۰۰ برمی‌گردد.

**اصلاح:** خطای داخلی سرور باید ۵۰۳/۵۰۰ برگردد و کلاینت باید ۵xx را هم‌ارز خطای شبکه (واجد grace period) تلقی کند، نه رد صریح.

---

### S-3 — کلید API در query string و به‌صورت رشته ثابت در سورس سرور

`UtilityController.php:185-190`

```php
$apiKey = (string)$this->request->get('api_key', '');
if ($apiKey !== 'atk_nk_9290VV42-38XQ02DI-F2WY4L2K-EJA7V682') {
```

سه ایراد:
1. کلید به‌صورت **پارامتر GET** ارسال می‌شود (`UpdateManager.kt:134`) ⇒ در access log وب‌سرور، لاگ پروکسی و هر واسط میانی ثبت می‌شود.
2. کلید به‌صورت literal داخل کد سرور hardcode است — چرخاندن آن نیازمند deploy است و در هر backup/repo قابل مشاهده.
3. مقایسه با `!==` است، نه `hash_equals()`؛ در این مورد خاص بردار حمله زمان‌سنجی ضعیف است ولی برای یک secret مقایسه ثابت‌زمان استاندارد است.

ضمناً `checkUpdate()` برخلاف `checkSignature()` **هیچ هدر امنیتی** (`X-Content-Type-Options`, `Cache-Control: no-store`) و هیچ محدودیت نرخی ندارد.

**اصلاح:** انتقال کلید به هدر (`X-Api-Key`)، خواندن آن از متغیر محیطی/`config.php`، مقایسه با `hash_equals()`، افزودن هدرهای امنیتی و rate limit.

---

### S-4 — قفل نسخه منقضی فقط سمت کلاینت اعمال می‌شود

`min_allowed_version` از سرور می‌آید ولی تصمیم‌گیری کاملاً در کلاینت است (`UpdateManager.kt:154-159`) و اقدام آن هم فقط نمایش `VersionExpiredDialog` است. هیچ endpoint دیگری در سرور نسخه کلاینت را بررسی نمی‌کند — نه `check_session`، نه APIهای عملیاتی. یعنی یک کلاینت قدیمی یا دستکاری‌شده که این دیالوگ را دور بزند، همچنان به تمام APIها دسترسی کامل دارد.

**اصلاح:** بررسی نسخه کلاینت را به لایه احراز هویت سرور منتقل کن (مثلاً هدر `X-App-Version` در `AuthenticatesRequests`) و در صورت قدیمی بودن، ۴۲۶ برگردان.

---

### S-5 — خروج با `killProcess` به‌جای بستن درست Activity

`MainActivity.kt:104`

```kotlin
onExit = { android.os.Process.killProcess(android.os.Process.myPid()) }
```

`killProcess` پروسه را می‌کشد ولی **task را از Recents حذف نمی‌کند** و هیچ چرخه حیاتی را به‌درستی خاتمه نمی‌دهد: `onDestroy` اجرا نمی‌شود، `onCleared` ViewModelها صدا زده نمی‌شود (پس `updateManager.onCleared()` و پاکسازی فایل‌های موقت انجام نمی‌شود)، و نوشتن‌های DataStore که در پرواز هستند از دست می‌روند. کاربر با یک تپ روی Recents برنامه را دوباره باز می‌کند — پس به‌عنوان «سد امنیتی» هم مؤثر نیست.

**اصلاح:** `finishAndRemoveTask()`.

---

### S-6 — رخدادهای یک‌باره روی `SharedFlow` با `replay = 0` گم می‌شوند

`StartupViewModel.kt:107` — `MutableSharedFlow<StartupEvent>(extraBufferCapacity = 4)` با `replay = 0`.

نکته مهم `SharedFlow`: `extraBufferCapacity` فقط برای مشترکین کند بافر می‌سازد؛ اگر **هیچ مشترکی نباشد**، `emit` بی‌صدا داده را دور می‌ریزد. مشترک در `MainActivity.kt:75-82` داخل composition است، پس در بازه بازسازی Activity (تغییر زبان، حالت تیره، اندازه فونت، multi-window) مشترکی وجود ندارد و پیام «لطفاً دوباره وارد شوید!» یا درخواست بهینه‌سازی باتری می‌تواند بی‌صدا حذف شود.

علاوه بر آن، `collect` بدون `repeatOnLifecycle` است؛ یعنی وقتی برنامه در پس‌زمینه است هم فعال می‌ماند و ممکن است `startActivity` تنظیمات باتری در پس‌زمینه اجرا شود (که در اندروید ۱۰+ به‌هرحال بلاک می‌شود و کاربر هیچ بازخوردی نمی‌گیرد).

**اصلاح:** استفاده از `Channel(Channel.BUFFERED).receiveAsFlow()` و مصرف آن با `repeatOnLifecycle(Lifecycle.State.STARTED)`.

---

### S-7 — `handleIntent` در هر بازسازی Activity دوباره اجرا می‌شود

`MainActivity.kt:70-73`

```kotlin
LaunchedEffect(Unit) { startupViewModel.handleIntent(intent) ... }
```

`LaunchedEffect(Unit)` در برابر recomposition مقاوم است ولی در برابر **بازسازی Activity** نه: با هر config change، `setContent` از نو اجرا می‌شود و `handleIntent` دوباره با همان Intent فراخوانی می‌شود.

`handleIntent` (`StartupViewModel.kt:219-229`) برای `navigate_to` idempotent شده (extra را حذف می‌کند)، اما برای `action == "com.atk.atk_cargo.OPEN_WARNINGS"` نه — چون `action` روی Intent باقی می‌ماند. نتیجه: کاربر که با نوتیفیکیشن هشدارها وارد شده، بعد از هر چرخش/تغییر تنظیمات سیستم دوباره دیالوگ هشدارها را می‌بیند.

**اصلاح:** پس از مصرف، `intent.action = null` بگذار یا یک `handledIntentHash` در ViewModel نگه دار.

---

### S-8 — Cast ناامن `updateInfo as UpdateInfo`

`MainActivity.kt:154` — بین `if (updateInfo != null)` در خط ۱۳۷ و استفاده در خط ۱۵۴، مقدار `updateInfo` (که از یک `StateFlow` سراسری singleton می‌آید) می‌تواند تغییر کند. اگر در آن فاصله `null` شود، `ClassCastException`/`NullPointerException` می‌دهد. الگوی درست، snapshot گرفتن در یک متغیر محلی است:

```kotlin
val info = updateInfo ?: return@… // یا: val info = updateInfo; if (isUpdateAvailable && info != null) { ... }
```

---

### S-9 — نصب مجدد خودکار و خطای کهنه به‌خاطر singleton بودن `UpdateManager`

`AppModule.kt:55` — `single { UpdateManager(androidContext()) }` (طول عمر پروسه) در حالی که کلاس از `ViewModel` ارث می‌برد و `onCleared` آن دستی از `StartupViewModel.onCleared()` صدا زده می‌شود (`StartupViewModel.kt:404-407`). سه پیامد:

1. **نصب تکراری:** `LaunchedEffect(downloadState)` در `MainActivity.kt:141-151` روی `DownloadState.Completed` نصب را شروع می‌کند. چون `downloadState` یک StateFlow ماندگار است، بعد از هر بازسازی Activity (یا برگشت از صفحه نصب) دوباره `Completed` دیده می‌شود و Intent نصب **مجدداً** باز می‌شود.
2. **خطای کهنه:** `checkVersionAndUpdate` در شکست، `_downloadState` را روی `Error` می‌گذارد (`UpdateManager.kt:143, 237, 244`) در حالی که `isUpdateAvailable` هنوز `false` است و دیالوگ نمایش داده نمی‌شود؛ آن خطا در singleton باقی می‌ماند و اگر بعداً آپدیتی موجود شود، دیالوگ مستقیماً در حالت Error باز شده و یک Toast خطای بی‌ربط نشان می‌دهد.
3. **شیء «cleared» ولی زنده:** بعد از `onCleared`، همان نمونه singleton برای Activityهای بعدی دوباره استفاده می‌شود. فعلاً کار می‌کند چون `UpdateManager` از `viewModelScope` استفاده نمی‌کند (اسکوپ خودش را می‌سازد)، اما این یک بمب ساعتی طراحی است.

**اصلاح:** `UpdateManager` نباید `ViewModel` باشد. یک کلاس ساده با `CoroutineScope` مدیریت‌شده کافی است. حالت `Completed` باید بعد از شروع نصب مصرف و به `Idle` بازگردد.

---

## ۵. یافته‌های متوسط (🟡)

| # | مورد | محل | توضیح |
|---|---|---|---|
| M-1 | دو اسپلش پشت سر هم | `MainActivity.kt:58` + `:96-98` | `installSplashScreen()` اسپلش سیستمی را نشان می‌دهد و بلافاصله `SplashScreen` کامپوزبل روی آن می‌آید. `SPLASH_MIN_DURATION = 1200ms` (`StartupViewModel.kt:410`) **در هر cold start** حداقل ۱.۲ ثانیه به زمان تا محتوا اضافه می‌کند، حتی اگر شبکه در ۲۰۰ms پاسخ داده باشد |
| M-2 | `resolveActivity` منسوخ + فیلتر دید بسته | `MainActivity.kt:175` | از API 30 به بعد `resolveActivity` تحت Package Visibility فیلتر می‌شود و مانیفست هیچ `<intent>` برای اکشن‌های تنظیمات ندارد ⇒ ممکن است بی‌دلیل `null` برگرداند و به شاخه fallback برود. الگوی درست: مستقیم `startActivity` داخل `try/catch (ActivityNotFoundException)` |
| M-3 | نتیجه مجوز نوتیفیکیشن فقط لاگ می‌شود | `MainActivity.kt:48-52` | نه `shouldShowRequestPermissionRationale` بررسی می‌شود، نه رد دائمی مدیریت می‌شود. کاربری که یک‌بار رد کند، در دفعات بعد هیچ بازخوردی نمی‌گیرد و دیالوگ هم دیگر باز نمی‌شود (`checkNotificationPermission:207-218` بی‌صدا هیچ‌کاری نمی‌کند) |
| M-4 | `rememberCoroutineScope` زائد | `MainActivity.kt:92, 111-113` | `retrySecurityCheck()` خودش داخل `viewModelScope.launch` است (`StartupViewModel.kt:197-201`)؛ پیچیدن آن در یک scope دیگر بی‌فایده است |
| M-5 | `@SuppressLint` غیرلازم | `MainActivity.kt:54` | `"CoroutineCreationDuringComposition"` هیچ هشدار واقعی‌ای در این فایل ندارد (تنها launch داخل یک callback است، نه بدنه composition). `@SuppressLint`های بی‌مصرف هشدارهای واقعی آینده را پنهان می‌کنند |
| M-6 | Activity مسئولیت اضافه دارد | `MainActivity.kt:45` | تزریق `UserPreferencesManager` فقط برای یک رنگ تم. این باید یک `StateFlow<Color>` در `StartupViewModel` باشد؛ Activity نباید مستقیم به لایه داده وصل شود |
| M-7 | نبود `enableEdgeToEdge` | `MainActivity.kt` | `targetSdk = 34` است ولی `compileSdk = 36`؛ به‌محض ارتقای targetSdk به ۳۵+، edge-to-edge اجباری می‌شود و بدون مدیریت inset، UI زیر نوار وضعیت/ناوبری می‌رود |
| M-8 | لاگ‌ها بدون گارد release | سراسر فایل | `Log.d`/`Log.e` بدون `if (BuildConfig.DEBUG)`. با اینکه `isMinifyEnabled = true` است، قواعد ProGuard فعلی لزوماً `Log.d` را حذف نمی‌کنند. لاگ خط ۵۱ وضعیت مجوز و خط ۱۲۷ پیام خطای داخلی را افشا می‌کند |
| M-9 | `savedInstanceState` استفاده نمی‌شود | `MainActivity.kt:55` | هیچ بازیابی حالتی وجود ندارد؛ با توجه به اینکه کل UI روی `StartupState` سوار است و ViewModel از config change جان سالم به در می‌برد، فعلاً مشکل‌ساز نیست ولی process death پوشش داده نشده |
| M-10 | grace period آفلاین قابل دستکاری | `SecurityVerifier.kt:41,116-118` · `StartupViewModel.kt:414` | مهلت ۳ روزه روی `System.currentTimeMillis()` و `SharedPreferences("x1y2z3")` ساده تکیه دارد. عقب بردن ساعت دستگاه یا ویرایش prefs روی دستگاه root، مهلت را نامحدود می‌کند. نام مبهم فایل prefs، امنیت از طریق ابهام است نه امنیت واقعی |
| M-11 | خواندن ناهمگام `downloadedBytes` | `UpdateManager.kt:333-352` در برابر `:435-437` | نوشتن داخل `synchronized(this)` است ولی خواندن در حلقه پیشرفت بدون همگام‌سازی و روی یک `Long` غیر `@Volatile` است ⇒ ریسک torn read و عدم مشاهده مقدار به‌روز بین threadها. `AtomicLong` گزینه درست است |
| M-12 | حلقه polling پیشرفت دانلود | `UpdateManager.kt:333-352` | `while(...) { ...; delay(100) }` یک coroutine را در تمام مدت دانلود بیدار نگه می‌دارد. جریان‌محور کردن پیشرفت (emit از داخل `downloadChunk`) بهینه‌تر است |

---

## ۶. پرفورمنس — جمع‌بندی مسیر cold start

| مرحله | هزینه | ارزیابی |
|---|---|---|
| `installSplashScreen()` | ~0 | ✅ درست، قبل از `super.onCreate` |
| `runBlocking { themeColor.first() }` | **۵۰–۳۰۰ms روی Main Thread** | 🔴 C-2 |
| ۵ درخواست شبکه موازی (نسخه، امضا، لایسنس×۲، نشست) | max(تک‌درخواست) | ✅ معماری موازی درست است؛ کامنت‌های `StartupViewModel.kt:127-146` و `SecurityVerifier.kt:219-224` دلیل را خوب مستند کرده‌اند |
| `SPLASH_MIN_DURATION` | **+۱۲۰۰ms اجباری** | 🟡 M-1 — روی شبکه سریع، تمام این زمان تأخیر خالص است |
| درخواست HEAD اضافه برای حجم آپدیت | +۱ RTT | 🟠 نتیجه مستقیم S-1؛ با اصلاح قرارداد JSON حذف می‌شود |
| `combine` چهار StateFlow با `SharingStarted.Eagerly` | ناچیز | ✅ ماشین حالت تمیز و بدون حالت نامعتبر |
| `collectAsState(themeColor)` | یک collector DataStore به‌ازای هر Activity | 🟡 با انتقال به ViewModel حل می‌شود (M-6) |
| بازسازی کل درخت UI در گذار `SecurityBlocked → Ready` | یک recomposition کامل | ✅ اجتناب‌ناپذیر و کم‌هزینه |

**نکته مثبت مهم:** `isInternetAvailable()` که قبلاً پروب سوکت به `8.8.8.8` و fallback به یک دامنه شخص ثالث می‌زد، حالا فقط از `ConnectivityManager` استفاده می‌کند (`UpdateManager.kt:104-110`) — این اصلاح قبلی هم از نظر سرعت و هم از نظر حریم خصوصی درست بوده است.

---

## ۷. کیفیت کد و معماری

**نقاط قوت (باید حفظ شوند):**

- گذار از ۵ متغیر boolean مستقل به `sealed interface StartupState` (`StartupViewModel.kt:42-54`) — حالت‌های نامعتبر دیگر قابل بیان نیستند. این بازسازی درست انجام شده است.
- `LocalStartupViewModel` / `LocalNotificationPermissionRequester` به‌جای `LocalContext.current as MainActivity` — سازگار با `@Preview` و تست UI (`StartupComposition.kt`).
- تفکیک صریح «خطای شبکه» از «رد صریح سرور» در `checkUserSessionAsync` و `SecurityVerifier.resolveNetworkFailure` با کامنت‌های توضیحی دقیق.
- `runStartupSequenceOnce` با گارد `startupSequenceStarted` در سطح ViewModel — مقاوم در برابر config change.
- کامنت‌های فارسی «چرا»محور (نه «چه»محور) در نقاط غیربدیهی — نمونه خوب: `MainActivity.kt:56-57, 62-64, 125-126` و `StartupViewModel.kt:134-135, 264-265`.

**ضعف‌های ساختاری:**

1. **سه مسئولیت در یک Activity.** میزبانی Compose + روتینگ ماشین حالت + میزبانی دیالوگ آپدیت. منطق `HandleMainContent` (خطوط ۱۳۱-۱۶۶) هیچ ربطی به Activity ندارد و باید یک کامپوزبل مستقل در `feature/update` باشد که Activity فقط صدایش می‌زند.
2. **`when(startupState)` بدون شاخه مشترک.** هیچ حالت خطای عمومی وجود ندارد؛ اگر ViewModel به حالت غیرمنتظره برسد (که فعلاً `sealed` جلویش را گرفته)، UX جایگزینی نیست.
3. **نام‌گذاری.** `HandleMainContent` با `@Composable` بودن، باید `MainContent` یا `UpdateGate` باشد — پیشوند `Handle` معمولاً برای توابع رخداد به کار می‌رود.
4. **ارجاع‌های fully-qualified داخل بدنه** به‌جای import: `kotlinx.coroutines.runBlocking` (خط ۶۵)، `android.os.Process` (خط ۱۰۴)، `android.provider.Settings` (خطوط ۱۷۱، ۱۷۹، ۱۸۷)، `androidx.activity.result.contract.ActivityResultContracts` (خط ۴۹)، `@androidx.compose.runtime.Composable` (خط ۱۳۱). با بقیه فایل که import منظم دارد ناسازگار است.
5. **`else -> { /* Other states don't require specific handling */ }`** (خط ۱۴۹) و **`onDismiss = { /* Handle dismiss */ }`** (خط ۱۶۱) — کامنت‌های TODO لباس مبدل پوشیده؛ دومی یک باگ بحرانی است (C-1).

---

## ۸. اولویت‌بندی اقدامات

**فوری (قبل از انتشار بعدی):**

1. **C-1** — `MainScreen()` را از شاخه `else` بیرون بیاور و `onDismiss` را پیاده کن؛ `forceUpdate` را واقعاً اعمال کن.
2. **C-2** — حذف `runBlocking` از Main Thread با `setKeepOnScreenCondition`.
3. **C-4** — UI جایگزین در بلوک `catch` به‌جای صفحه سفید.
4. **S-2** — سرور روی خطای داخلی ۵۰۳ برگرداند و کلاینت ۵xx را واجد grace period بداند (جلوگیری از خروج دسته‌جمعی کاربران).

**کوتاه‌مدت:**

5. **C-3** — انتقال دانلود APK به حافظه داخلی + بررسی SHA-256 + محدود کردن `file_path.xml`.
6. **C-5** — اعتبارسنجی واقعی `sessionToken` در `SessionService::isSessionActive`.
7. **S-1** — یکسان‌سازی قرارداد JSON آپدیت و حذف درخواست HEAD جبرانی.
8. **S-3** — انتقال `api_key` به هدر و متغیر محیطی + `hash_equals` + هدرهای امنیتی.
9. **S-8، S-9** — رفع cast ناامن و ماندگاری حالت `Completed`/`Error` در singleton.

**میان‌مدت:**

10. **S-4** — انتقال اعمال `min_allowed_version` به لایه احراز هویت سرور.
11. **S-6، S-7** — `Channel` + `repeatOnLifecycle` برای رخدادها؛ idempotent کردن `handleIntent`.
12. **M-1** — کاهش/حذف `SPLASH_MIN_DURATION` وقتی شبکه زودتر آماده است.
13. **M-6** — انتقال رنگ تم به `StartupViewModel` و حذف تزریق مستقیم `UserPreferencesManager` در Activity.
14. **M-11** — `AtomicLong` برای `downloadedBytes`.
