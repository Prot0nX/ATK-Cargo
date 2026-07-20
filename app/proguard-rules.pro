# =======================================================================
# 1. تنظیمات پایه و بهینه‌سازی (اولویت بالا - تنظیمات اصلی)
# =======================================================================
-optimizationpasses 10                 # تعداد دفعات بهینه‌سازی (افزایش یافته)
-dontusemixedcaseclassnames            # نام کلاس‌ها را با حروف مختلط نسازد
-dontskipnonpubliclibraryclasses       # کلاس‌های غیرعمومی کتابخانه‌ها را نادیده نگیرد
-dontpreverify                         # تأیید قبلی را انجام ندهد (سرعت بیشتر)
-verbose                               # گزارش‌های مفصل
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*  # بهینه‌سازی‌های خاص

# تنظیمات اضافی برای کاهش حجم APK
-repackageclasses 'obfuscated'         # کلاس‌ها را با هم بسته‌بندی می‌کند (بهینه‌سازی حجم)
-allowaccessmodification               # اجازه تغییر سطح دسترسی کلاس‌ها برای بهینه‌سازی
-mergeinterfacesaggressively           # ادغام تهاجمی رابط‌های مشابه
-overloadaggressively                  # استفاده مجدد از نام متدها با پارامترهای متفاوت
-renamesourcefileattribute SourceFile # تغییر نام فایل منبع در استک‌تریس
-adaptresourcefilenames **.properties  # تطبیق نام فایل‌های منابع با کلاس‌های مبهم شده
-adaptresourcefilecontents **.properties,META-INF/MANIFEST.MF  # بروزرسانی محتوای فایل‌ها

# =======================================================================
# 2. تنظیمات حفظ ویژگی‌ها (اولویت بالا - برای عملکرد صحیح)
# =======================================================================
-keepattributes *Annotation*           # حفظ آنوتیشن‌ها (برای رتروفیت و گسون ضروری است)
-keepattributes Signature              # حفظ اطلاعات Generic Signature
-keepattributes Exceptions             # حفظ اطلاعات استثناها
-keepattributes InnerClasses,EnclosingMethod  # حفظ ساختار کلاس‌های داخلی
-keepattributes !SourceFile,!LineNumberTable    # حذف اطلاعات خط در تولید نهایی برای امنیت
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault  # مورد نیاز برای Jetpack Compose

# =======================================================================
# 3. تنظیمات امنیتی برای محافظت از توابع امنیتی (اولویت بالا)
# =======================================================================
# حفظ کلاس امضاپژیر با سازنده (نام واقعی کلاس: MusicLibraryManager)
-keep class com.atk.atk_cargo.weather.MusicLibraryManager {
    <init>(android.content.Context);
    public Pair validateMusicLibrary();
}

# حفظ متغیرهای حساس در Companion Object (فقط فیلدها)
-keepclassmembers class com.atk.atk_cargo.weather.MusicLibraryManager$Companion {
    private static final java.lang.String ENCODED_*;
}

# حفظ enum SecurityErrorType (رعایت استانداردهای پروگارد برای انوم)
-keepclassmembers enum com.atk.atk_cargo.weather.SecurityErrorType {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# محافظت از MainActivity با حفظ ساختار اصلی (فقط موارد ضروری)
-keep class com.atk.atk_cargo.MainActivity {
    public <init>();
    protected void onCreate(android.os.Bundle);
}

# مبهم‌سازی توابع و فیلدهای امنیتی MainActivity (اجازه تغییر نام)
-keepclassmembernames class com.atk.atk_cargo.MainActivity {
    private void calculateWeatherForecast();
    private boolean isSecurityCheckPassed;
    private *** signatureVerifier;
}

# =======================================================================
# 4. تنظیمات API و ارتباطات شبکه (اولویت متوسط)
# =======================================================================
# رتروفیت و سرویس‌های API
-keepattributes Signature, RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

-keepclassmembers interface com.atk.atk_cargo.api.ApiService {
    <methods>;
}

# =======================================================================
# 4.1. مبهم‌سازی و محافظت از API Endpoints (امنیت بالا)
# =======================================================================

# حفظ ساختار ApiService اما مبهم‌سازی مقادیر رشته‌ای
-keepclassmembers interface com.atk.atk_cargo.api.ApiService {
    @retrofit2.http.POST <methods>;
    @retrofit2.http.GET <methods>;
}

# مبهم‌سازی تمام رشته‌های ثابت در ApiService
-adaptresourcefilecontents com/atk/atk_cargo/api/ApiService.class

# حذف اطلاعات دیباگ از ApiService در release
-assumenosideeffects class com.atk.atk_cargo.api.ApiService {
    # این باعث حذف لاگ‌های مربوط به API می‌شود
}

# مبهم‌سازی پارامترهای Query و Field
-keepclassmembers class * {
    @retrofit2.http.Query *;
    @retrofit2.http.Field *;
    @retrofit2.http.Body *;
}

# رمزنگاری و مبهم‌سازی نام متدها و پارامترها
-obfuscationdictionary proguard-dictionary.txt
-classobfuscationdictionary proguard-dictionary.txt
-packageobfuscationdictionary proguard-dictionary.txt

# مبهم‌سازی تهاجمی برای کلاس‌های API
-repackageclasses 'obfuscated.api'
-allowaccessmodification

# حذف اطلاعات منبع و شماره خط برای ApiService در release
-keepattributes !SourceFile,!LineNumberTable

# مبهم‌سازی رشته‌های ثابت در کلاس‌های API
# این باعث می‌شود نام فایل‌های PHP مبهم شوند
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static void checkNotNullParameter(java.lang.Object, java.lang.String);
}

# Retrofit
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# OkHttp و Okio (قوانین در خود کتابخانه وجود دارد)
-dontwarn okhttp3.**
-dontwarn okio.**

# Gson (قوانین در خود کتابخانه وجود دارد)
-dontwarn com.google.gson.**
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# حفظ همه data class ها برای Gson
-keep class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# حفظ data class های Kotlin
-keep class kotlin.Metadata { *; }
-keepclassmembers class * {
    @kotlin.Metadata <fields>;
}

-dontwarn sun.misc.**

# =======================================================================
# 5. مدل‌های برنامه (اولویت متوسط)
# =======================================================================
# حفظ مدل‌های داده برای سریالیزیشن/دیسریالیزیشن

-keep class com.atk.atk_cargo.api.** { *; }
-keep class com.atk.atk_cargo.network.** { *; }
# حفظ data class هایSummary برای Gson (فقط کلاس‌های مورد نیاز)
-keep class com.atk.atk_cargo.models.** { *; }

# Chat specific rules
-keep class com.atk.atk_cargo.data.db.ChatMessageEntity { *; }
-keep class com.atk.atk_cargo.api.ChatMessage { *; }
-keep class com.atk.atk_cargo.api.ChatMessagesResponse { *; }
-keep class com.atk.atk_cargo.api.SendMessageResponse { *; }
-keep class com.atk.atk_cargo.api.SendMessageRequest { *; }
-keep class com.atk.atk_cargo.api.UnreadCountResponse { *; }

# تنظیمات پایه Kotlin (بسیاری از این موارد خودکار هستند)
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# =======================================================================
# 7. تنظیمات Coroutines (اولویت متوسط)
# =======================================================================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# =======================================================================
# 8. تنظیمات Android (اولویت متوسط)
# =======================================================================
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# =======================================================================
# 9. حذف لاگ‌ها و بهینه‌سازی کد (اولویت پایین)
# =======================================================================
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
    public static int wtf(...);
}

# حذف printStackTrace در release
-assumenosideeffects class java.lang.Throwable {
    public void printStackTrace();
}

# =======================================================================
# 10. تنظیمات Compose (اولویت بالا)
# =======================================================================
# بخش Jetpack Compose به طور خودکار توسط کتابخانه مدیریت می‌شود
# در اینجا فقط موارد ضروری یا تداخلی اضافه شود
-dontwarn androidx.compose.**

# Navigation Compose (در کتابخانه موجود است)
-keepclassmembers class * {
    @androidx.navigation.** <methods>;
}

# Coil, Lottie, iText, Charts (عموماً خودکار هستند)
-dontwarn coil.**
-dontwarn io.coil.**
-dontwarn com.airbnb.lottie.**
-dontwarn com.itextpdf.**
-dontwarn com.patrykandpatrick.vico.**

# =======================================================================
# 18. تنظیمات Work Manager (اولویت متوسط)
# =======================================================================
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker
-keepclassmembers class * extends androidx.work.Worker {
    public <init>(android.content.Context,androidx.work.WorkerParameters);
}

# =======================================================================
# 19. سرکوب هشدارها (اولویت پایین)
# =======================================================================
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
-dontwarn org.slf4j.**
-dontwarn aQute.bnd.**
-dontwarn edu.umd.cs.findbugs.**
-dontwarn org.osgi.framework.**
-dontwarn javax.lang.model.**
-dontwarn com.sun.jna.**
-dontwarn java.awt.**
-dontwarn javax.swing.**
-dontwarn sun.misc.**
-dontwarn org.apache.log4j.**
-dontwarn org.apache.commons.logging.**