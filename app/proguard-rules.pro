# ========================================
# بهینه‌سازی عمومی
# ========================================
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

# ========================================
# حفظ اطلاعات خطایابی
# ========================================
-keepattributes SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile

# ========================================
# اجازه مبهم‌سازی برای MainActivity و SignatureVerifier
# ========================================
# اجازه مبهم‌سازی برای MainActivity
-keep,allowobfuscation class com.atk.atk_cargo.MainActivity { *; }

# اجازه مبهم‌سازی برای SignatureVerifier
-keep,allowobfuscation class com.atk.atk_cargo.security.SignatureVerifier { *; }

# ========================================
# حفظ RetrofitClient و Secrets
# ========================================
-keep class com.atk.atk_cargo.api.RetrofitClient { *; }
-keep class com.atk.atk_cargo.api.Secrets { *; }
-keepnames class com.atk.atk_cargo.api.RetrofitClient
-keepnames class com.atk.atk_cargo.api.Secrets

# ========================================
# حفظ کلاس‌های مدل داده
# ========================================
-keepclassmembers class com.atk.atk_cargo.api.* {
    <fields>;
    <init>(...);
    <methods>;
}

# ========================================
# حفظ سایر کلاس‌های پکیج com.atk.atk_cargo به جز MainActivity و SignatureVerifier
# ========================================
-keep class com.atk.atk_cargo.api.** { *; }
-keep class com.atk.atk_cargo.model.** { *; }

# ========================================
# حفاظت از اعضا در SignatureVerifier
# ========================================
-keepclassmembers class com.atk.atk_cargo.security.SignatureVerifier {
    <fields>;
    <methods>;
}

# ========================================
# مبهم‌سازی ثابت‌های رشته‌ای در SignatureVerifier
# ========================================
-keepclassmembers class com.atk.atk_cargo.security.SignatureVerifier {
    private static final java.lang.String VALID_APP_SIGNATURE;
    private static final java.lang.String SIGNATURE_CHECK;
}
-assumenosideeffects class com.atk.atk_cargo.security.SignatureVerifier {
    private static final java.lang.String VALID_APP_SIGNATURE;
    private static final java.lang.String SIGNATURE_CHECK;
}

# ========================================
# تنظیمات Retrofit
# ========================================
-keepattributes Signature, InnerClasses
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

# ========================================
# تنظیمات OkHttp
# ========================================
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ========================================
# تنظیمات Gson
# ========================================
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ========================================
# تنظیمات AndroidX
# ========================================
-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**
-dontnote com.google.android.material.**

# ========================================
# تنظیمات Coroutines
# ========================================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# ========================================
# حذف لاگ‌ها در نسخه نهایی
# ========================================
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# ========================================
# حفظ کلاس‌های R
# ========================================
-keep class **.R$* {
    <fields>;
}
-dontwarn **.R$*

# ========================================
# قوانین اضافی برای رفع هشدارها
# ========================================
-dontwarn java.awt.Shape
-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn aQute.bnd.annotation.**
-dontwarn edu.umd.cs.findbugs.annotations.**
-dontwarn org.apache.batik.**
-dontwarn org.osgi.framework.**

# ========================================
# پایان فایل ProGuard
# ========================================
