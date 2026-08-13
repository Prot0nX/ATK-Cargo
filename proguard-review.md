# گزارش جامع بازبینی `app/proguard-rules.pro` — ATK-Cargo

**تاریخ:** ۱۴۰۵/۰۵/۲۲ (2026-08-13) · **نسخه اپ:** 3.0.34 (versionCode 10)
**محیط:** AGP 8.13.0 · Kotlin 2.2.20 · `android.enableR8.fullMode=true` · minSdk 28 / targetSdk 34

---

## ۰. خلاصه اجرایی

فایل از نظر **ساختار و دسته‌بندی** بسیار خوب نوشته شده (۱۳ سکشن، کامنت‌گذاری منظم). اما از نظر **اثر واقعی روی خروجی release**، فایل دچار یک تناقض بنیادی است:

> سکشن ۱ تلاش می‌کند obfuscation را حداکثر کند (`-repackageclasses`, دیکشنری، `-allowaccessmodification`)، اما سکشن‌های ۵ و ۶ با قوانین `-keep ... { *; }` روی کل پکیج‌ها، **همان obfuscation را برای مهم‌ترین بخش‌های اپ خنثی می‌کنند**.

### شواهد عینی از بیلد release موجود

| سنجه | مقدار | منبع |
|---|---|---|
| کل کلاس‌های اپ در mapping | ۹۱۶ | `mapping.txt` |
| کلاس‌های اپ که **اصلاً obfuscate نشده‌اند** | **۲۵۵ (٪۲۷.۸)** | `mapping.txt` |
| کل seedها (ورودی‌های keep‌شده) | ۱۵٬۹۹۵ | `seeds.txt` |
| seed از `okhttp3` + `retrofit2` | **۵٬۵۶۹ (٪۳۴.۸)** | `seeds.txt` |
| seed از `com.atk.atk_cargo.api` + `.data` | **۳٬۸۸۶ (٪۲۴.۳)** | `seeds.txt` |
| کلاس حذف‌شده از okhttp3/retrofit2 | **۲** (از ۱۲۰٬۸۵۹ حذف کل) | `usage.txt` |

یعنی **٪۵۹ از کل keepها** فقط از دو گروه قانون بیش‌ازحد باز می‌آید، و shrinking روی کل لایه شبکه عملاً **صفر** است.

### تفکیک کلاس‌های obfuscate‌نشده اپ

```
107  com.atk.atk_cargo.api            ← Secrets, UpdateManager, SessionValidator,
                                          AuthSession, RetrofitClient, UserPreferencesManager,
                                          AtkCargoApplication, ChatViewModel, Constants ...
 79  com.atk.atk_cargo.data.model
 39  com.atk.atk_cargo.data.repository
  9  com.atk.atk_cargo.data.db
  5  com.atk.atk_cargo.data
 12  feature.*.navigation             ← قابل توجیه (Navigation type-safe)
  2  com.atk.atk_cargo.security       ← SecurityVerifier, CryptoManager
  1  com.atk.atk_cargo.workers
```

### نمره‌دهی

| حوزه | نمره | وضعیت |
|---|---|---|
| ساختار و خوانایی | ۹/۱۰ | عالی |
| امنیت واقعی خروجی | ۴/۱۰ | ضعیف — obfuscation روی نقاط حساس خنثی شده |
| پرفورمنس / حجم | ۴/۱۰ | ضعیف — shrinking شبکه صفر |
| صحت و پایداری runtime | ۵/۱۰ | ریسک‌دار — iText و حذف null-checkها |
| بهداشت پیکربندی | ۵/۱۰ | ~۲۵٪ قوانین مرده یا بی‌اثر |
| **کل** | **۵.۴/۱۰** | نیازمند بازنویسی هدفمند |

---

## ۱. مشکلات بحرانی (Critical)

### C1 — قوانین `-keep` بیش‌ازحد باز، obfuscation را روی حساس‌ترین کد خنثی می‌کنند

**محل:** خطوط ۹۲–۹۷ و ۵۲، ۶۱–۶۵، ۷۲

```proguard
-keep class com.atk.atk_cargo.data.model.** { *; }
-keep class com.atk.atk_cargo.api.**  { *; }   # ← فاجعه‌بار
-keep class com.atk.atk_cargo.data.** { *; }
```

پکیج `com.atk.atk_cargo.api` فقط DTO نیست؛ **۱۸ فایل منطق تجاری** دارد:

```
ApiService.kt          AtkCargoApplication.kt   AuthSession.kt
BootReceiver.kt        ChatViewModel.kt         Constants.kt
RetrofitClient.kt      Secrets.kt               SessionValidator.kt
UpdateManager.kt       UserPreferencesManager.kt  PermissionPoller.kt
LoadingNotificationService.kt  NotificationActionReceiver.kt ...
```

همه با نام کامل و امضای متدها در APK باقی می‌مانند. یعنی مهاجم با یک `jadx` سادهٔ بدون هیچ تلاشی:
- کل قرارداد API و endpointها (`ApiService`)
- منطق نشست و اعتبارسنجی توکن (`AuthSession`, `SessionValidator`)
- زنجیرهٔ آپدیت (`UpdateManager`) — نقطهٔ ورود بالقوه برای تزریق APK جعلی
- کلید و مدیریت ترجیحات کاربر (`UserPreferencesManager`, `Constants`)

را با نام‌های خوانا می‌بیند.

**بدتر — تناقض ضدتمپر (خط ۶۱–۶۵):**

```proguard
-keep class com.atk.atk_cargo.security.SecurityVerifier { *; }
-keep class com.atk.atk_cargo.security.CryptoManager   { *; }
```

`SecurityVerifier` دقیقاً شامل متدهای زیر است (تأیید‌شده از سورس):
`verifySecurityStatus()`, `verifyLocalAppSignature()`, `calculateSignatureHash()`,
`isEnvironmentCompromised()`, `hasInjectedLibraries()`, `isFridaServerPortOpen()`,
`validateLicenseWithServer()`, `authenticateSignatureWithServer()`

اپ تشخیص Frida دارد — ولی **دقیقاً همان نام متدهایی که Frida برای hook کردن نیاز دارد را در APK دست‌نخورده نگه می‌دارد.** یک اسکریپت Frida سه‌خطی کافی است:

```js
Java.use("com.atk.atk_cargo.security.SecurityVerifier")
    .isEnvironmentCompromised.implementation = function () { return false; };
```

این نه یک ضعف تئوریک، بلکه **باطل‌کنندهٔ کل سرمایه‌گذاری روی لایهٔ امنیتی** است. هیچ دلیل فنی‌ای برای keep کردن `SecurityVerifier` وجود ندارد: نه Gson روی آن reflection می‌زند، نه JNI به آن نام‌محور دسترسی دارد.

**اصلاح:** فقط DTOهای واقعی را keep کنید، آن هم با `allowobfuscation` روی نام کلاس:

```proguard
# فقط مدل‌های داده که Gson با reflection می‌سازد
-keep class com.atk.atk_cargo.data.model.** { <fields>; <init>(...); }

# SecurityVerifier / CryptoManager: کاملاً حذف شود — نیازی به keep ندارند
# UserPreferencesManager: فقط اگر تست شد نیاز دارد
```

---

### C2 — keep کامل روی `retrofit2` و `okhttp3` — ٪۳۵ کل seedها، shrinking صفر

**محل:** خطوط ۱۰۹–۱۱۰

```proguard
-keep class retrofit2.** { *; }
-keep class okhttp3.**   { *; }
```

**داده:** ۵٬۵۶۹ seed از این دو پکیج. از ۱۲۰٬۸۵۹ ورودی حذف‌شده، فقط **۲ مورد** از okhttp3/retrofit2 بوده. یعنی R8 اجازهٔ حذف حتی یک کلاس بلااستفاده از لایهٔ شبکه را ندارد.

هر دو کتابخانه (Retrofit 3.0.0، OkHttp 5.1.0) **consumer ProGuard rules صحیح و رسمی خودشان را در AAR/JAR دارند** و AGP آن‌ها را خودکار merge می‌کند (در `configuration.txt` قابل مشاهده است). این دو خط نه‌تنها لازم نیستند، بلکه:

- حجم DEX و method count را بی‌دلیل بالا می‌برند
- کل استک شبکه را با نام خوانا در APK نگه می‌دارند (کمک مستقیم به مهندسی معکوس ترافیک)
- بارگذاری کلاس‌ها در startup را کندتر می‌کنند

**اصلاح:** هر دو خط کاملاً حذف شوند. `-dontwarn okhttp3.**` و `-dontwarn retrofit2.**` هم حذف شوند (پوشش خطاهای واقعی در full mode).

---

### C3 — `-keepattributes !SourceFile,!LineNumberTable` معنایی معکوس دارد

**محل:** خط ۳۶

در سمانتیک فیلتر ProGuard/R8، **اگر لیست فیلتر با نفی‌کننده تمام شود، یک `**` ضمنی در انتها فرض می‌شود.** بنابراین این خط معنایش این است:

> «همهٔ attributeها را نگه دار، **به‌جز** `SourceFile` و `LineNumberTable`.»

یعنی به‌طور ناخواسته این‌ها هم نگه داشته می‌شوند:
`LocalVariableTable`، `LocalVariableTypeTable`، `MethodParameters`، `SourceDebugExtension`، `Synthetic`، `Deprecated`

`LocalVariableTable` و `MethodParameters` **نام پارامترها و متغیرهای محلی اصلی** را برمی‌گردانند. یعنی حتی کلاس‌هایی که obfuscate شده‌اند، در decompiler با نام متغیرهای واقعی خوانده می‌شوند — دقیقاً برعکس هدف سکشن ۱.

هم‌زمان، خط ۲۴ (`-renamesourcefileattribute SourceFile`) بی‌اثر می‌شود چون `SourceFile` اصلاً keep نشده. یعنی بدترین حالت ممکن: **obfuscation ضعیف‌تر + قابلیت تحلیل کرش کمتر**.

خطوط ۳۴–۳۵ هم به‌خاطر همین `**` ضمنی، کاملاً زائد شده‌اند.

**اصلاح استاندارد اندروید:**

```proguard
-keepattributes Signature,InnerClasses,EnclosingMethod,Exceptions
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,AnnotationDefault
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
```

با این ترکیب: نام فایل به `SourceFile` بی‌معنا تبدیل می‌شود، شمارهٔ خط برای deobfuscate کردن کرش با `mapping.txt` باقی می‌ماند، و `LocalVariableTable` حذف می‌شود.

> ⚠️ هرگز از `-keepattributes` با لیست فقط-نفی استفاده نکنید.

---

## ۲. مشکلات مهم (High)

### H1 — حذف null-checkهای Kotlin: تبدیل fail-fast به رفتار تعریف‌نشده

**محل:** خطوط ۲۳۱–۲۳۴

```proguard
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static void checkNotNullParameter(java.lang.Object, java.lang.String);
    public static void checkNotNull(java.lang.Object, java.lang.String);
}
```

کامنت بالای آن می‌گوید «Strip … parameter strings»، اما این قانون **رشته را حذف نمی‌کند؛ خودِ بررسی null را حذف می‌کند.**

پیامد در این پروژه مشخص است:
- `Secrets` از JNI مقدار برمی‌گرداند — اگر `libsecrets.so` بارگذاری نشود یا XOR decrypt شکست بخورد، `null` وارد پارامترهای non-null Kotlin می‌شود
- Gson می‌تواند برای فیلدهای non-null کاتلین `null` تولید کند (Gson از nullability کاتلین خبر ندارد)
- به‌جای `NullPointerException` واضح در نقطهٔ ورود، خطا چند لایه بعد و در جای بی‌ربط ظاهر می‌شود — یا اصلاً دادهٔ نادرست ذخیره می‌شود

R8 **خودش** رشته‌های نام پارامتر را در حالت optimize حذف می‌کند؛ این قانون سود اضافه‌ای ندارد و فقط ریسک ایجاد می‌کند.

**اصلاح:** کل بلاک حذف شود.

---

### H2 — حذف `Log.e` و `printStackTrace` = نابینایی کامل در production

**محل:** خطوط ۲۱۵–۲۲۸ · **دامنه:** ۹۶ فراخوانی `Log.*` و ۴ مورد `printStackTrace` در سورس

حذف `Log.d/v/i` منطقی است. اما حذف `Log.w`، `Log.e` و `Log.wtf` یعنی وقتی `validateLicenseWithServer()` یا `authenticateSignatureWithServer()` در دست کاربر واقعی شکست می‌خورد، **هیچ ردی باقی نمی‌ماند**. برای اپی که کل جریان راه‌اندازی‌اش به اعتبارسنجی لایسنس سرور وابسته است، این یعنی تشخیص علت شکست عملاً غیرممکن می‌شود.

هم‌زمان `-assumenosideeffects` روی `Throwable.printStackTrace()` بلاک‌های `catch { e.printStackTrace() }` را به بلاک خالی تبدیل می‌کند — یعنی **خطای بلعیده‌شدهٔ کاملاً خاموش**.

**اصلاح:**

```proguard
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
# w / e / wtf عمداً نگه داشته می‌شوند تا خطاهای production قابل تشخیص بمانند
```

و بلاک `Throwable.printStackTrace` حذف شود — به‌جایش در سورس `printStackTrace` را با `Log.e` جایگزین کنید.

---

### H3 — سکشن ۹ (Koin) کاملاً کد مرده است

**محل:** خطوط ۱۶۸–۱۷۳

```proguard
-keepclassmembers class * {
    @org.koin.core.annotation.* <fields>;
    @org.koin.core.annotation.* <methods>;
}
```

پروژه از `io.insert-koin:koin-android:3.5.6` با **DSL modules** استفاده می‌کند (`di/AppModule.kt`). وابستگی `koin-annotations` / `koin-ksp-compiler` در `build.gradle.kts` وجود ندارد، پس annotationهای `org.koin.core.annotation.*` هرگز در classpath نیستند. این قانون هیچ‌وقت match نمی‌کند.

Koin با DSL هیچ نیاز به keep rule ندارد (از lambda استفاده می‌کند، نه reflection).

**اصلاح:** کل سکشن ۹ حذف شود، شامل `-dontwarn org.koin.**`.

---

### H4 — iText: هم ریسک runtime، هم مشکل لایسنس

**محل:** خط ۲۰۲ (`-dontwarn com.itextpdf.**`) · **مصرف‌کننده:** `feature/reports/domain/ExportPdfUseCase.kt`

**الف) ریسک runtime:** `com.itextpdf:itextpdf:5.5.13.4` فقط `-dontwarn` دارد و **هیچ `-keep`**. iText 5 در زمان اجرا با `Class.forName` سراغ provider های BouncyCastle و منابع فونت/encoding می‌رود. تحت **R8 full mode**، کلاس‌هایی که ارجاع ایستا ندارند حذف می‌شوند → خروجی PDF **فقط در بیلد release** می‌شکند (در debug که minify خاموش است سالم کار می‌کند؛ همین باعث می‌شود باگ تا انتشار پنهان بماند).

**ب) هشدار مضاعف — `packaging.excludes`:** در `build.gradle.kts:118-191` این الگوها فعال‌اند:
`**/*.properties`، `**/*.bin`، `**/*.dat`، `**/*.txt`
iText 5 فایل‌های متریک فونت و پیام‌های خطا را دقیقاً با همین پسوندها حمل می‌کند. باید تأیید شود که PDF export در بیلد release واقعاً کار می‌کند.

**ج) مشکل لایسنس (⚠️ حقوقی):**

> **iText نسخهٔ 5.5.13.x تحت AGPLv3 منتشر می‌شود.**

AGPL ایجاب می‌کند که هر اپلیکیشنی که این کتابخانه را توزیع می‌کند، **کل سورس خود را تحت همان لایسنس منتشر کند** — مگر اینکه لایسنس تجاری از iText Group خریداری شده باشد. برای یک اپ باری/تجاری بسته‌منبع، این یک ریسک حقوقی واقعی است، نه یک نکتهٔ تشریفاتی.

**گزینه‌ها:**
1. خرید لایسنس تجاری iText
2. مهاجرت به `android.graphics.pdf.PdfDocument` (بومی اندروید، بدون هزینه، بدون وابستگی)
3. مهاجرت به کتابخانهٔ Apache-2.0 مثل `PdfBox-Android`

**اصلاح ProGuard (تا زمان تصمیم‌گیری):**

```proguard
-keep class com.itextpdf.** { *; }
-dontwarn com.itextpdf.**
-dontwarn org.bouncycastle.**
```

---

### H5 — `proguardFiles` دوبار اعلام شده

**محل:** `app/build.gradle.kts:29-32` (در `defaultConfig`) و `:59-62` (در `release`)

`defaultConfig.proguardFiles` روی **همهٔ** build typeها اعمال می‌شود. اعلام مجدد در `release` باعث می‌شود کل مجموعه قوانین و فایل پیش‌فرض **دوبار** وارد پیکربندی merge‌شده شوند. عملاً بی‌خطر است (R8 قوانین تکراری را یکی می‌کند) اما:
- `configuration.txt` را دو برابر و دیباگ آن را سخت می‌کند
- قوانین به `debug` و `benchmark` هم تسری پیدا می‌کند که خواستهٔ شما نیست

**اصلاح:** بلاک `proguardFiles` را از `defaultConfig` حذف کنید و فقط در `release` نگه دارید.

---

## ۳. مشکلات متوسط (Medium)

### M1 — گزینه‌هایی که R8 کاملاً نادیده می‌گیرد (حس امنیت کاذب)

| خط | گزینه | وضعیت در R8 |
|---|---|---|
| ۹ | `-optimizationpasses 10` | نادیده گرفته می‌شود (R8 خودش pass ها را مدیریت می‌کند) |
| ۱۰ | `-dontusemixedcaseclassnames` | نادیده گرفته می‌شود |
| ۱۱ | `-dontskipnonpubliclibraryclasses` | نادیده گرفته می‌شود |
| ۱۲ | `-dontpreverify` | نادیده گرفته می‌شود (مخصوص JVM) |
| ۱۳ | `-verbose` | نادیده گرفته می‌شود |
| ۲۲ | `-mergeinterfacesaggressively` | نادیده گرفته می‌شود |
| ۲۳ | `-overloadaggressively` | نادیده گرفته می‌شود |
| ۲۹ | `-optimizations !...` | نادیده گرفته می‌شود |

نکتهٔ مهم دربارهٔ خط ۲۹: اگر R8 آن را **رعایت می‌کرد**، نتیجه بدتر بود — چون `!field/*` و `!class/merging/*` دقیقاً بهینه‌سازی‌های ارزشمند را **خاموش** می‌کنند. این خط از پیکربندی‌های قدیمی ProGuard کپی شده و با هدف «بهینه‌سازی حداکثری» در تضاد است.

**اصلاح:** همه حذف شوند (فقط نویز و گمراهی هستند). R8 پیام `Ignoring option:` برای این‌ها در لاگ بیلد چاپ می‌کند.

---

### M2 — دیکشنری obfuscation عملاً بی‌فایده است

`app/proguard-dictionary.txt` شامل: `a, b, c, ..., z, aa, ab, ..., cz` (حدود ۱۳۰ ورودی)

این **دقیقاً همان الگوی نام‌گذاری پیش‌فرض R8** است. یعنی هر سه گزینهٔ خط ۱۶–۱۸ صفر ارزش افزوده دارند.

دیکشنری مؤثر از کلمات کلیدی رزروشده یا نام‌های گمراه‌کننده استفاده می‌کند تا decompilerها را دچار مشکل کند:

```
do
if
int
for
new
null
true
class
while
return
```

**فایل‌های یتیم:**
- `app/dictionary.txt` — به هیچ‌جا ارجاع نشده
- `app/security-dictionary.txt` — **فایل خراب**: با انکودینگ UTF-16 و حاوی متن `-adaptclassstrings com.atk.atk_cargo.MainActivity` — این یک قطعه پیکربندی ProGuard است که به‌اشتباه به‌عنوان فایل دیکشنری ذخیره شده. اگر به‌عنوان دیکشنری استفاده شود، نتیجه نام‌های نامعتبر خواهد بود.

**اصلاح:** هر دو فایل یتیم حذف شوند و `proguard-dictionary.txt` با کلمات رزروشده بازنویسی شود.

---

### M3 — قوانین Gson TypeToken غایب‌اند (فعلاً توسط C1 پنهان شده‌اند)

دو محل استفاده از زیرکلاس ناشناس `TypeToken` وجود دارد:

- `api/LoadingNotificationService.kt:265` → `object : TypeToken<List<RealTimeLoadingData>>() {}`
- `api/UserPreferencesManager.kt:51` → `object : TypeToken<Map<String, Boolean>>() {}`

این‌ها فقط به این دلیل کار می‌کنند که `-keep class com.atk.atk_cargo.api.** { *; }` (خط ۹۴) کلاس‌های ناشناس داخل پکیج را هم نگه می‌دارد. **به‌محض اصلاح C1، این دو مورد در release می‌شکنند** (`RuntimeException: Missing type parameter`).

**اصلاح — همراه با اصلاح C1 اضافه شود:**

```proguard
-keepattributes Signature
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
```

---

### M4 — `-dontwarn` های بیش‌ازحد باز، خطاهای واقعی full mode را پنهان می‌کنند

**محل:** خطوط ۱۱۱–۱۱۴، ۱۱۹، ۱۵۹، ۱۷۳، ۱۸۶

```proguard
-dontwarn okhttp3.**      -dontwarn retrofit2.**
-dontwarn com.google.gson.**   -dontwarn kotlin.**
-dontwarn androidx.room.**     -dontwarn androidx.compose.**
```

این‌ها کتابخانه‌هایی هستند که **در اپ حضور کامل دارند**. `-dontwarn` باید فقط برای وابستگی‌های اختیاری غایب استفاده شود (مثل `bouncycastle`, `conscrypt`, `openjsse`, `slf4j`, `jna` که در خطوط ۲۳۷–۲۴۳ درست به‌کار رفته‌اند).

با `android.enableR8.fullMode=true`، خطای «missing class» یک سیگنال جدی است که این قوانین آن را خاموش می‌کنند — و نتیجه‌اش `NoClassDefFoundError` در دست کاربر است، نه خطای بیلد.

**اصلاح:** هر شش مورد حذف شوند. اگر بیلد شکست، خطای واقعی را ببینید و قانون **هدفمند** بنویسید.

---

### M5 — `-dontwarn` برای کتابخانه‌هایی که در پروژه نیستند

| خط | قانون | واقعیت |
|---|---|---|
| ۲۰۹ | `-dontwarn androidx.media3.**` | media3 اصلاً dependency نیست |
| ۱۹۸ | `-dontwarn io.coil.**` | پکیج اشتباه — Coil 2.x پکیجش `coil.` است (خط ۱۹۷ درست است) |
| ۲۰۳ | `-dontwarn com.patrykandpatrick.vico.**` | vico در `build.gradle.kts:287` اعلام شده ولی **صفر استفاده در سورس** |

مورد vico یک یافتهٔ جانبی مهم است: `implementation(libs.core)` یک وابستگی مرده است که بی‌دلیل به حجم APK اضافه می‌کند. باید حذف شود.

مشابهاً `ktor-client-core = "3.3.0"` در `libs.versions.toml` تعریف شده اما در `dependencies` استفاده نشده.

---

### M6 — قوانین زائد و هم‌پوشان

| خط | قانون | پوشیده‌شده توسط |
|---|---|---|
| ۵۲ | `-keep class ...api.Secrets { *; }` | خط ۹۴ (`api.**`) |
| ۷۲ | `-keep class ...api.UserPreferencesManager { *; }` | خط ۹۴ |
| ۸۴، ۸۵ | `-keep interface ...api.ApiService / ThirdPartyApiService` | خط ۹۴ |
| ۹۲، ۹۳ | `data.model.**` | خطوط ۹۶، ۹۷ (`data.**`) |
| ۷۵–۷۸ | `MainActivity` | خط ۴۱ (`* extends android.app.Activity`) |

**نکتهٔ سمانتیک مهم:** `-keep class X { *; }` **به‌طور ضمنی شامل اعضا هم هست**. بنابراین همهٔ `-keepclassmembers` های جفت‌شده در خطوط ۶۲، ۶۵، ۹۳، ۹۵، ۹۷ **کاملاً بی‌اثر** هستند — یک الگوی رایج ناشی از کپی‌برداری.

---

### M7 — قوانین resource بی‌اثر

**محل:** خطوط ۲۵–۲۶

```proguard
-adaptresourcefilenames **.properties
-adaptresourcefilecontents **.properties,META-INF/MANIFEST.MF
```

- `**/*.properties` در `build.gradle.kts:134` از packaging **حذف** شده → هیچ فایل properties در APK نیست
- APK اندروید اصلاً `META-INF/MANIFEST.MF` ندارد (مفهوم JAR است)

هر دو خط no-op هستند.

---

### M8 — `@Serializable` بیش‌ازحد باز

**محل:** خط ۱۳۲

```proguard
-keep @kotlinx.serialization.Serializable class * { *; }
```

استفادهٔ واقعی `@Serializable` در پروژه:
- `feature/*/navigation/*.kt` (۶ فایل) — Navigation Compose type-safe routes → **نام کلاس واقعاً لازم است** (در route pattern استفاده می‌شود)
- `data/model/CargoModels.kt` — نیازی به نگه‌داشتن نام کلاس ندارد

**اصلاح — محدود کردن به پکیج navigation:**

```proguard
-keep @kotlinx.serialization.Serializable class com.atk.atk_cargo.feature.**.navigation.** { *; }
-keepclassmembers @kotlinx.serialization.Serializable class * {
    static **$* *;
    static <fields>;
    *** Companion;
    *** serializer(...);
}
```

kotlinx-serialization 1.9.0 خودش consumer rules کامل دارد؛ بیشتر سکشن ۷ (خطوط ۱۳۷–۱۴۸) زائد است.

---

## ۴. مشکلات کم‌اهمیت (Low)

### L1 — `Secrets` بالاترین ارزش برای مهاجم، کاملاً در معرض دید

خط ۵۲ **از نظر فنی درست است** — JNI با `Java_com_atk_atk_1cargo_api_Secrets_*` نام‌محور کار می‌کند و بدون keep کرش می‌کند.

اما نتیجه‌اش این است که مهاجم یک **نقشهٔ برچسب‌خوردهٔ کامل** دریافت می‌کند:

```
getApiKey()          getWebUserPass()      getLicenseKey()
getBaseUrl()         getAuthUser()         getExpectedSignatureHash()
getSignatureCheckUrl()  getLicenseCheckUrl()  getAuthenticationX365()
```

با `hook` کردن این متدها یا `strings` روی `libsecrets.so`، همهٔ اسرار قابل استخراج‌اند (به‌ویژه چون `secrets.cpp:8` از `decryptXor()` استفاده می‌کند که در برابر تحلیل ایستا مقاومت ناچیزی دارد).

**بهبود پیشنهادی (خارج از دامنهٔ این فایل، اما مرتبط):** استفاده از `JNI_OnLoad` + `RegisterNatives` برای ثبت پویا. با این روش نام کلاس و متدها قابل obfuscate می‌شوند و خط ۵۲ به‌کلی حذف می‌شود.

### L2 — build type `benchmark` با امضای debug

`build.gradle.kts:72-78`: `benchmark` از release ارث می‌برد (پس minify روشن است) اما `signingConfig = debug`. `SecurityVerifier.verifyLocalAppSignature()` هش امضا را با مقدار مورد انتظار مقایسه می‌کند → **در بیلد benchmark همیشه شکست می‌خورد**. برای پروفایلینگ باید مسیر عبور مشخصی در نظر گرفته شود.

### L3 — استراتژی نگه‌داری `mapping.txt` مستند نشده

با اصلاح C3، `mapping.txt` تنها راه خواندن کرش‌های production می‌شود. باید برای هر انتشار آرشیو شود (فعلاً در `app/build/outputs/mapping/release/` است که با `clean` پاک می‌شود).

---

## ۵. جمع‌بندی آماری فایل

| دسته | تعداد خط | درصد |
|---|---|---|
| مؤثر و صحیح | ~۹۵ | ٪۳۹ |
| زائد / هم‌پوشان | ~۳۵ | ٪۱۴ |
| نادیده‌گرفته‌شده توسط R8 | ~۱۵ | ٪۶ |
| مرده (کتابخانهٔ غایب) | ~۱۲ | ٪۵ |
| **مضر (امنیت یا پرفورمنس)** | **~۲۵** | **٪۱۰** |
| کامنت و فاصله | ~۶۱ | ٪۲۵ |

---

## ۶. پیکربندی پیشنهادی بازنویسی‌شده

```proguard
# =======================================================================
# PROGUARD / R8 CONFIGURATION - ATK-CARGO
# AGP 8.13 · R8 full mode · minSdk 28
# اصل راهنما: هیچ قانونی بدون دلیل مستند اضافه نشود.
# =======================================================================

# ----------------------------------------------------------------------
# 1. OBFUSCATION & REPACKAGING
# ----------------------------------------------------------------------
-repackageclasses 'o'
-allowaccessmodification

# دیکشنری باید کلمات رزروشدهٔ جاوا باشد، نه a/b/c (که پیش‌فرض R8 است)
-obfuscationdictionary      proguard-dictionary.txt
-classobfuscationdictionary proguard-dictionary.txt
-packageobfuscationdictionary proguard-dictionary.txt

# ----------------------------------------------------------------------
# 2. ATTRIBUTES
# هرگز لیست فقط-نفی ننویسید — تیل ضمنی «**» فعال می‌شود.
# ----------------------------------------------------------------------
-keepattributes Signature,InnerClasses,EnclosingMethod,Exceptions
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,AnnotationDefault
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ----------------------------------------------------------------------
# 3. JNI BRIDGE (اجباری — نام‌محور با libsecrets.so)
# TODO: مهاجرت به RegisterNatives تا این keep حذف شود.
# ----------------------------------------------------------------------
-keep class com.atk.atk_cargo.api.Secrets { native <methods>; }
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

# ----------------------------------------------------------------------
# 4. DATA MODELS (Gson reflection)
# فقط DTOها — نه repository، نه manager، نه ViewModel.
# ----------------------------------------------------------------------
-keep class com.atk.atk_cargo.data.model.** {
    <fields>;
    <init>(...);
}

-keepattributes Signature
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ----------------------------------------------------------------------
# 5. RETROFIT INTERFACE
# ----------------------------------------------------------------------
-keep,allowobfuscation interface com.atk.atk_cargo.api.ApiService
-keep,allowobfuscation interface com.atk.atk_cargo.api.ThirdPartyApiService
-keepclassmembers,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
# نکته: Retrofit 3.0 و OkHttp 5.1 قوانین consumer خود را دارند —
# هیچ «-keep class retrofit2.**» یا «-dontwarn» اضافه نکنید.

# ----------------------------------------------------------------------
# 6. NAVIGATION TYPE-SAFE ROUTES (نام کلاس در route pattern استفاده می‌شود)
# ----------------------------------------------------------------------
-keep @kotlinx.serialization.Serializable class com.atk.atk_cargo.feature.**.navigation.** { *; }
-keepclassmembers @kotlinx.serialization.Serializable class * {
    static **$* *;
    static <fields>;
    *** Companion;
    *** serializer(...);
}

# ----------------------------------------------------------------------
# 7. ANDROID ENTRY POINTS & PARCELABLE
# (Room / WorkManager / Compose / Koin قوانین consumer خود را دارند)
# ----------------------------------------------------------------------
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# ----------------------------------------------------------------------
# 8. iTEXT (reflection روی provider و منابع فونت)
# ⚠️ لایسنس iText 5.5.x = AGPLv3 — نیازمند بررسی حقوقی
# ----------------------------------------------------------------------
-keep class com.itextpdf.** { *; }
-dontwarn com.itextpdf.**

# ----------------------------------------------------------------------
# 9. LOG STRIPPING
# w/e/wtf عمداً نگه داشته می‌شوند تا خطاهای production قابل تشخیص بمانند.
# ----------------------------------------------------------------------
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# ----------------------------------------------------------------------
# 10. WARNINGS — فقط برای وابستگی‌های اختیاری غایب
# ----------------------------------------------------------------------
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
-dontwarn org.slf4j.**
-dontwarn com.sun.jna.**
-dontwarn sun.misc.**
-dontwarn javax.lang.model.**
```

**حذف‌شده‌ها نسبت به نسخهٔ فعلی:**
`SecurityVerifier` / `CryptoManager` / `UserPreferencesManager` keep · `api.**` و `data.**` keep · `retrofit2.**` و `okhttp3.**` keep · کل سکشن Koin · بلاک `Intrinsics` · بلاک `printStackTrace` · گزینه‌های نادیده‌گرفته‌شدهٔ R8 · `-dontwarn` های بیش‌ازحد باز · قوانین media3/vico/coil-اشتباه · قوانین resource بی‌اثر.

---

## ۷. برنامهٔ اجرا (به ترتیب اولویت)

### گام ۱ — اصلاحات بی‌ریسک (فوری)
- [ ] حذف `-keep class retrofit2.**` و `-keep class okhttp3.**` (خطوط ۱۰۹–۱۱۰) — **C2**
- [ ] اصلاح `-keepattributes` طبق بخش C3 — **C3**
- [ ] حذف بلاک `Intrinsics` (خطوط ۲۳۱–۲۳۴) — **H1**
- [ ] حذف `Log.w/e/wtf` از `assumenosideeffects` و حذف بلاک `printStackTrace` — **H2**
- [ ] حذف کل سکشن ۹ (Koin) — **H3**
- [ ] حذف گزینه‌های نادیده‌گرفته‌شده (M1) و قوانین resource (M7)
- [ ] حذف بلاک `proguardFiles` از `defaultConfig` — **H5**

### گام ۲ — کاهش سطح حمله (نیازمند تست دقیق release)
- [ ] افزودن `-keep class com.itextpdf.** { *; }` و **تست export PDF در بیلد release** — **H4-الف**
- [ ] افزودن قوانین Gson TypeToken — **M3**
- [ ] حذف `-keep` برای `SecurityVerifier`, `CryptoManager`, `UserPreferencesManager`
- [ ] محدود کردن `api.**` / `data.**` به `data.model.**` — **C1**
- [ ] تست کامل: ورود → نشست → اسکن بارکد → گزارش → PDF → چت → آپدیت → تأیید لایسنس

### گام ۳ — پاکسازی و بهبود
- [ ] بازنویسی `proguard-dictionary.txt` با کلمات رزروشدهٔ جاوا — **M2**
- [ ] حذف `app/dictionary.txt` و `app/security-dictionary.txt` — **M2**
- [ ] حذف وابستگی‌های مرده: vico (`libs.core`)، `ktor-client-core` — **M5**
- [ ] بررسی packaging excludes در برابر منابع iText — **H4-ب**

### گام ۴ — تصمیمات راهبردی
- [ ] **تعیین تکلیف لایسنس iText (AGPL)** — خرید لایسنس یا مهاجرت — **H4-ج**
- [ ] مهاجرت JNI به `RegisterNatives` — **L1**
- [ ] استراتژی آرشیو `mapping.txt` برای هر انتشار — **L3**
- [ ] مسیر عبور امضا برای بیلد `benchmark` — **L2**

### روش اعتبارسنجی هر گام

```bash
./gradlew clean assembleRelease && grep -cE "^com\.atk\.atk_cargo\..* -> com\.atk\.atk_cargo\." app/build/outputs/mapping/release/mapping.txt
```

عدد ۲۵۵ فعلی باید به کمتر از ۱۰۰ برسد (فقط `data.model` + route های navigation). هم‌زمان `wc -l app/build/outputs/mapping/release/seeds.txt` باید از ۱۵٬۹۹۵ به حدود ۵٬۰۰۰ کاهش یابد.
