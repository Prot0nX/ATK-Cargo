# =======================================================================
# تنظیمات ProGuard برای برنامه ATK-Cargo
# =======================================================================

# =======================================================================
# 1. تنظیمات پایه و بهینه‌سازی (اولویت بالا - تنظیمات اصلی)
# =======================================================================
-optimizationpasses 7                  # تعداد دفعات بهینه‌سازی
-dontusemixedcaseclassnames            # نام کلاس‌ها را با حروف مختلط نسازد
-dontskipnonpubliclibraryclasses       # کلاس‌های غیرعمومی کتابخانه‌ها را نادیده نگیرد
-dontpreverify                         # تأیید قبلی را انجام ندهد (سرعت بیشتر)
-verbose                               # گزارش‌های مفصل
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*  # بهینه‌سازی‌های خاص

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
# حفظ کلاس امضاپژیر با سازنده
-keep class com.atk.atk_cargo.security.SignatureVerifier {
    <init>(android.content.Context);
}

# حفظ متغیرهای حساس در Companion Object
-keepclassmembers,allowobfuscation class com.atk.atk_cargo.security.SignatureVerifier$Companion {
    private static final <fields>;
}

# حفظ enum SecurityErrorType
-keep enum com.atk.atk_cargo.security.SecurityErrorType

# محافظت از کلاس‌های مربوط به امنیت
-keep class com.atk.atk_cargo.security.** { *; }

# محافظت از MainActivity با حفظ ساختار اصلی
-keep class com.atk.atk_cargo.MainActivity {
    public <init>();  # سازنده عمومی
    protected void onCreate(android.os.Bundle);  # متد اصلی چرخه حیات
}

# محافظت از توابع و فیلدهای امنیتی MainActivity
-keepclassmembers,allowobfuscation class com.atk.atk_cargo.MainActivity {
    private void performSecurityCheck();
    private void HandleSecurityCheck(...);
    private *** isSecurityCheck*;
    private *** signatureVerifier;
}

# =======================================================================
# 4. تنظیمات API و ارتباطات شبکه (اولویت متوسط)
# =======================================================================
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
# 9. تنظیمات پیشرفته جهت جلوگیری از مهندسی معکوس (اولویت پایین)
# =======================================================================
-renamesourcefileattribute SourceFile  # تغییر نام فایل منبع
-repackageclasses 'o'                  # بسته‌بندی مجدد کلاس‌ها
-allowaccessmodification               # اجازه تغییر سطح دسترسی
-overloadaggressively                  # بازنویسی انبوه
-flattenpackagehierarchy               # مسطح‌سازی سلسله مراتب بسته‌ها

# =======================================================================
# 10. حذف لاگ‌ها (اولویت پایین)
# =======================================================================
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# =======================================================================
# 11. تنظیمات Compose (اولویت بالا)
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
# 12. تنظیمات Navigation Compose (اولویت متوسط)
# =======================================================================
-keep class androidx.navigation.** { *; }
-keepclassmembers class * {
    @androidx.navigation.** <methods>;
}

# =======================================================================
# 13. تنظیمات Camera & ML Kit (اولویت متوسط)
# =======================================================================
-keep class androidx.camera.** { *; }
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# =======================================================================
# 14. تنظیمات TensorFlow Lite (اولویت متوسط)
# =======================================================================
-keep class org.tensorflow.lite.** { *; }
-keep class org.tensorflow.lite.gpu.** { *; }
-keep class org.tensorflow.lite.support.** { *; }
-dontwarn org.tensorflow.lite.**

# =======================================================================
# 15. تنظیمات MediaPipe (اولویت متوسط)
# =======================================================================
-keep class com.google.mediapipe.** { *; }
-dontwarn com.google.mediapipe.**

# =======================================================================
# 16. تنظیمات Serialization (اولویت متوسط)
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
# 17. تنظیمات Ktor (اولویت متوسط)
# =======================================================================
-keep class io.ktor.** { *; }
-keepclassmembers class io.ktor.** {
    <fields>;
    <methods>;
}
-dontwarn io.ktor.**

# =======================================================================
# 18. تنظیمات Coil (اولویت پایین)
# =======================================================================
-keep class coil.** { *; }
-keep class io.coil.** { *; }
-dontwarn coil.**
-dontwarn io.coil.**

# =======================================================================
# 19. تنظیمات Lottie (اولویت پایین)
# =======================================================================
-keep class com.airbnb.lottie.** { *; }
-dontwarn com.airbnb.lottie.**

# =======================================================================
# 20. تنظیمات Apache POI (اولویت پایین)
# =======================================================================
-keep class org.apache.poi.** { *; }
-dontwarn org.apache.poi.**
-dontwarn org.apache.xmlbeans.**
-dontwarn org.apache.commons.**

# =======================================================================
# 21. تنظیمات iText PDF (اولویت پایین)
# =======================================================================
-keep class com.itextpdf.** { *; }
-dontwarn com.itextpdf.**

# =======================================================================
# 22. تنظیمات Charts (Vico) (اولویت پایین)
# =======================================================================
-keep class com.patrykandpatrick.vico.** { *; }
-dontwarn com.patrykandpatrick.vico.**

# =======================================================================
# 23. تنظیمات Work Manager (اولویت متوسط)
# =======================================================================
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker
-keepclassmembers class * extends androidx.work.Worker {
    public <init>(android.content.Context,androidx.work.WorkerParameters);
}

# =======================================================================
# 24. سرکوب هشدارها (اولویت پایین)
# =======================================================================
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
-dontwarn org.slf4j.**
-dontwarn aQute.bnd.**
-dontwarn edu.umd.cs.findbugs.**
-dontwarn org.apache.batik.**
-dontwarn org.osgi.framework.**
-dontwarn javax.lang.model.**
-dontwarn org.tensorflow.lite.gpu.GpuDelegateFactory$Options**
-dontwarn com.sun.jna.**
-dontwarn java.awt.**
-dontwarn javax.swing.**
-dontwarn sun.misc.**
-dontwarn org.apache.log4j.**
-dontwarn org.apache.commons.logging.**
