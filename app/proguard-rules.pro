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
-repackageclasses 'obfuscated'         # بسته‌بندی مجدد کلاس‌ها
-allowaccessmodification               # اجازه تغییر سطح دسترسی
-mergeinterfacesaggressively           # ادغام تهاجمی رابط‌ها
-overloadaggressively                  # بارگذاری مجدد تهاجمی
-renamesourcefileattribute SourceFile # تغییر نام فایل منبع
-adaptresourcefilenames **.properties  # تطبیق نام فایل‌های منابع
-adaptresourcefilecontents **.properties,META-INF/MANIFEST.MF  # تطبیق محتوای فایل‌های منابع

# =======================================================================
# 2. تنظیمات حفظ ویژگی‌ها (اولویت بالا - برای عملکرد صحیح)
# =======================================================================
-keepattributes *Annotation*           # حفظ همه آنوتیشن‌ها
-keepattributes Signature              # حفظ اطلاعات امضا
-keepattributes Exceptions             # حفظ اطلاعات استثناها
-keepattributes InnerClasses,EnclosingMethod  # حفظ کلاس‌های داخلی
-keepattributes SourceFile,LineNumberTable    # حفظ اطلاعات خط برای دیباگ
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault  # برای کامپوز

# =======================================================================
# 3. تنظیمات امنیتی برای محافظت از توابع امنیتی (اولویت بالا)
# =======================================================================
# حفظ کلاس امضاپژیر با سازنده (نام واقعی کلاس: MusicLibraryManager)
-keep class com.atk.atk_cargo.weather.MusicLibraryManager {
    <init>(android.content.Context);
}

# حفظ متغیرهای حساس در Companion Object
-keepclassmembers,allowobfuscation class com.atk.atk_cargo.weather.MusicLibraryManager$Companion {
    private static final <fields>;
}

# حفظ enum SecurityErrorType
-keep enum com.atk.atk_cargo.weather.SecurityErrorType

# محافظت از کلاس‌های مربوط به امنیت
-keep class com.atk.atk_cargo.weather.** { *; }

# محافظت از MainActivity با حفظ ساختار اصلی
-keep class com.atk.atk_cargo.MainActivity {
    public <init>();  # سازنده عمومی
    protected void onCreate(android.os.Bundle);  # متد اصلی چرخه حیات
}

# محافظت از توابع و فیلدهای امنیتی MainActivity
-keepclassmembers,allowobfuscation class com.atk.atk_cargo.MainActivity {
    private void calculateWeatherForecast();
    private *** isSecurityCheck*;
    private *** signatureVerifier;
}

# =======================================================================
# 4. تنظیمات API و ارتباطات شبکه (اولویت متوسط)
# =======================================================================
# محافظت خاص از ApiService و کلاس‌های مرتبط
-keep class com.atk.atk_cargo.api.ApiService { *; }
-keepnames class com.atk.atk_cargo.api.** { *; }
-keepattributes Signature, RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# جلوگیری از بهینه‌سازی مضر برای ApiService
-keepclassmembers class com.atk.atk_cargo.api.ApiService {
    <methods>;
}
-keepclassmembers interface com.atk.atk_cargo.api.ApiService {
    <methods>;
}

# Retrofit
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# OkHttp
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-keep class okio.** { *; }
-dontwarn okio.**

# Gson
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-dontwarn sun.misc.**

# =======================================================================
# 5. مدل‌های برنامه (اولویت متوسط)
# =======================================================================
# حفظ مدل‌های داده برای سریالیزیشن/دیسریالیزیشن
-keep class com.atk.atk_cargo.api.** { *; }
-keep class com.atk.atk_cargo.models.** { *; }
-keep class com.atk.atk_cargo.network.** { *; }

# =======================================================================
# 6. تنظیمات Kotlin (اولویت متوسط)
# =======================================================================
-keep class kotlin.** { *; }
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
-keep class androidx.compose.** { *; }
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.material3.** { *; }
-keep class androidx.compose.foundation.** { *; }
-keepclassmembers class androidx.compose.** {
    <fields>;
    <methods>;
}

# =======================================================================
# 11. تنظیمات Navigation Compose (اولویت متوسط)
# =======================================================================
-keep class androidx.navigation.** { *; }
-keepclassmembers class * {
    @androidx.navigation.** <methods>;
}

# =======================================================================
# 12. تنظیمات Camera & ML Kit (اولویت متوسط)
# =======================================================================
-keep class androidx.camera.** { *; }
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# =======================================================================
# 13. تنظیمات Serialization (اولویت متوسط)
# =======================================================================
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class * {
    @kotlinx.serialization.** <fields>;
}
-keepclassmembers @kotlinx.serialization.Serializable class * {
    <fields>;
    <methods>;
}

# =======================================================================
# 14. تنظیمات Coil (اولویت پایین)
# =======================================================================
-keep class coil.** { *; }
-keep class io.coil.** { *; }
-dontwarn coil.**
-dontwarn io.coil.**

# =======================================================================
# 15. تنظیمات Lottie (اولویت پایین)
# =======================================================================
-keep class com.airbnb.lottie.** { *; }
-dontwarn com.airbnb.lottie.**

# =======================================================================
# 16. تنظیمات iText PDF (اولویت پایین)
# =======================================================================
-keep class com.itextpdf.** { *; }
-dontwarn com.itextpdf.**

# =======================================================================
# 17. تنظیمات Charts (Vico) (اولویت پایین)
# =======================================================================
-keep class com.patrykandpatrick.vico.** { *; }
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
