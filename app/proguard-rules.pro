# =======================================================================
# 1. تنظیمات API و ارتباطات شبکه
# =======================================================================

# Retrofit
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# OkHttp
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okio.** { *; }

# مدل‌های API (نگهداری کلاس‌های مدل برای سریالیزیشن/دیسریالیزیشن صحیح)
-keep class com.atk.atk_cargo.api.** { *; }
-keep class com.atk.atk_cargo.models.** { *; }
-keep class com.atk.atk_cargo.network.** { *; }

# Gson
-keep class com.google.gson.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# =======================================================================
# 2. تنظیمات پایه و بهینه‌سازی
# =======================================================================
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

# =======================================================================
# 3. تنظیمات امنیتی و SignatureVerifier
# =======================================================================
# مبهم‌سازی کلاس اصلی امضاپژیر
-keep class com.atk.atk_cargo.security.SignatureVerifier {
    <init>(android.content.Context);
}

# حفظ متغیرهای حساس در Companion Object
-keepclassmembers,allowobfuscation class com.atk.atk_cargo.security.SignatureVerifier$Companion {
    private static final <fields>;
}

# =======================================================================
# 4. تنظیمات Kotlin
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
# 5. تنظیمات Android
# =======================================================================
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable,Signature
-keepattributes InnerClasses,EnclosingMethod
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# =======================================================================
# 6. تنظیمات Coroutines
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
# 7. حذف لاگ‌ها
# =======================================================================
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# =======================================================================
# 8. تنظیمات پیشرفته جهت جلوگیری از مهندسی معکوس
# =======================================================================
-keepattributes SourceFile,LineNumberTable,*Annotation*
-renamesourcefileattribute SourceFile
-repackageclasses 'o'
-allowaccessmodification
-overloadaggressively
-flattenpackagehierarchy

# =======================================================================
# 9. سرکوب هشدارها
# =======================================================================
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
-dontwarn org.slf4j.**
-dontwarn aQute.bnd.**
-dontwarn edu.umd.cs.findbugs.**
-dontwarn org.apache.batik.**
-dontwarn org.osgi.framework.**
