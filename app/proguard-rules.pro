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
# 11. سرکوب هشدارها (اولویت پایین)
# =======================================================================
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
-dontwarn org.slf4j.**
-dontwarn aQute.bnd.**
-dontwarn edu.umd.cs.findbugs.**
-dontwarn org.apache.batik.**
-dontwarn org.osgi.framework.**

# قوانین اضافی برای کلاس‌های گمشده (تولید شده توسط R8)
-dontwarn javax.lang.model.SourceVersion
-dontwarn javax.lang.model.element.Element
-dontwarn javax.lang.model.element.ElementKind
-dontwarn javax.lang.model.element.Modifier
-dontwarn javax.lang.model.type.TypeMirror
-dontwarn javax.lang.model.type.TypeVisitor
-dontwarn javax.lang.model.util.SimpleTypeVisitor8
-dontwarn org.tensorflow.lite.gpu.GpuDelegateFactory$Options$GpuBackend
-dontwarn org.tensorflow.lite.gpu.GpuDelegateFactory$Options
