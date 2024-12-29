# بهینه‌سازی عمومی
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

# حفظ کلاس‌های اصلی پکیج (با امکان مبهم‌سازی)
-keep class com.atk.atk_cargo.** { *; }
-keepclassmembers class com.atk.atk_cargo.** { *; }
-keepnames class com.atk.atk_cargo.** { *; }

# حفظ و مبهم‌سازی ApiService
-keep interface com.atk.atk_cargo.api.ApiService {
    <methods>;
}
-keepnames interface com.atk.atk_cargo.api.ApiService

# حفظ RetrofitClient و Secrets
-keep class com.atk.atk_cargo.api.RetrofitClient { *; }
-keep class com.atk.atk_cargo.api.Secrets { *; }
-keepnames class com.atk.atk_cargo.api.RetrofitClient
-keepnames class com.atk.atk_cargo.api.Secrets

# حفظ کلاس‌های مدل داده (با امکان مبهم‌سازی فیلدها)
-keepclassmembers class com.atk.atk_cargo.api.* {
    <fields>;
    <init>(...);
    <methods>;
}

# Retrofit
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

# OkHttp
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

# Gson
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

# AndroidX
-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**
-dontnote com.google.android.material.**

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

-keep class **.R$* {
    <fields>;
}
-dontwarn **.R$*

# missing_rules.txt
-dontwarn java.awt.Shape
-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn aQute.bnd.annotation.**
-dontwarn edu.umd.cs.findbugs.annotations.**
-dontwarn org.apache.batik.**
-dontwarn org.osgi.framework.**

-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile