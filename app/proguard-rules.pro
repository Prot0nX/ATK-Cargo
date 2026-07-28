# =======================================================================
# ENTERPRISE PROGUARD & R8 CONFIGURATION - ATK-CARGO
# Production-Grade Security Hardening, Optimization & Shrinking Rules
# =======================================================================

# -----------------------------------------------------------------------
# SECTION 1: GENERAL OPTIMIZATION & R8 HARDENING
# -----------------------------------------------------------------------
-optimizationpasses 10
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose

# Obfuscation Dictionaries & Repackaging
-obfuscationdictionary proguard-dictionary.txt
-classobfuscationdictionary proguard-dictionary.txt
-packageobfuscationdictionary proguard-dictionary.txt

-repackageclasses 'obfuscated'
-allowaccessmodification
-mergeinterfacesaggressively
-overloadaggressively
-renamesourcefileattribute SourceFile
-adaptresourcefilenames **.properties
-adaptresourcefilecontents **.properties,META-INF/MANIFEST.MF

# R8 Optimization Rules & Fine Tuning
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

# -----------------------------------------------------------------------
# SECTION 2: ATTRIBUTE PRESERVATION & METADATA
# -----------------------------------------------------------------------
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,Exceptions
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,AnnotationDefault
-keepattributes !SourceFile,!LineNumberTable

# -----------------------------------------------------------------------
# SECTION 3: ANDROID CORE ENTRY POINTS
# -----------------------------------------------------------------------
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgent

# -----------------------------------------------------------------------
# SECTION 4: JNI & NATIVE BRIDGE PROTECTION
# -----------------------------------------------------------------------
# Critical: Keep JNI class name, package name, and method signatures matching C++ libsecrets.so
-keep class com.atk.atk_cargo.api.Secrets { *; }
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

# -----------------------------------------------------------------------
# SECTION 5: SECURITY & CRITICAL APPLICATION HARDENING
# -----------------------------------------------------------------------
# Security Verifier, Anti-Tamper & Cryptographic Engine
-keep class com.atk.atk_cargo.security.SecurityVerifier { *; }
-keepclassmembers class com.atk.atk_cargo.security.SecurityVerifier { *; }

-keep class com.atk.atk_cargo.security.CryptoManager { *; }
-keepclassmembers class com.atk.atk_cargo.security.CryptoManager { *; }

-keepclassmembers enum com.atk.atk_cargo.security.SecurityErrorType {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class com.atk.atk_cargo.api.UserPreferencesManager { *; }

# Preserve MainActivity lifecycle entry point
-keep class com.atk.atk_cargo.MainActivity {
    public <init>();
    protected void onCreate(android.os.Bundle);
}

# -----------------------------------------------------------------------
# SECTION 6: NETWORKING (RETROFIT, OKHTTP, GSON)
# -----------------------------------------------------------------------
# Retrofit Interfaces & Annotations
-keep interface com.atk.atk_cargo.api.ApiService { *; }
-keep interface com.atk.atk_cargo.api.ThirdPartyApiService { *; }

-keepclassmembers interface * {
    @retrofit2.http.* <methods>;
}

# Data Models & DTO Preservation (Gson Reflection Safety)
-keep class com.atk.atk_cargo.data.model.** { *; }
-keepclassmembers class com.atk.atk_cargo.data.model.** { *; }
-keep class com.atk.atk_cargo.api.DataModel** { *; }

# Gson TypeAdapters & SerializedName Annotations
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Networking Library Types & Warnings
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-dontwarn com.google.gson.**

# -----------------------------------------------------------------------
# SECTION 7: KOTLIN, COROUTINES & SERIALIZATION
# -----------------------------------------------------------------------
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Kotlinx Serialization & Navigation Routes
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}

-keepclassmembers class **$Companion {
    *** serializer(...);
}

-keep class * implements kotlinx.serialization.KSerializer
-keepclassmembers class * implements kotlinx.serialization.KSerializer {
    public static *** INSTANCE;
}

-keepclassmembers class * {
    @kotlinx.serialization.Serializer *** serializer(...);
}

# -----------------------------------------------------------------------
# SECTION 8: ROOM DATABASE & PARCELABLE
# -----------------------------------------------------------------------
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keepclassmembers class * {
    @androidx.room.TypeConverter <methods>;
}
-dontwarn androidx.room.**

-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# -----------------------------------------------------------------------
# SECTION 9: DEPENDENCY INJECTION (KOIN)
# -----------------------------------------------------------------------
-keepclassmembers class * {
    @org.koin.core.annotation.* <fields>;
    @org.koin.core.annotation.* <methods>;
}
-keepnames class org.koin.core.Koin { *; }
-dontwarn org.koin.**

# -----------------------------------------------------------------------
# SECTION 10: WORKMANAGER & BACKGROUND TASKS
# -----------------------------------------------------------------------
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-dontwarn androidx.work.**

# -----------------------------------------------------------------------
# SECTION 11: JETPACK COMPOSE & UI LIBRARIES
# -----------------------------------------------------------------------
-dontwarn androidx.compose.**
-keep class androidx.compose.ui.platform.NestedScrollInteropConnection { *; }

-keepclassmembers class * {
    @androidx.navigation.** <methods>;
}

# -----------------------------------------------------------------------
# SECTION 12: MEDIA, DOCUMENT & THIRD-PARTY SDKs
# -----------------------------------------------------------------------
# Image Loading & Animations
-dontwarn coil.**
-dontwarn io.coil.**
-dontwarn com.airbnb.lottie.**

# Document & Charts Processing
-dontwarn com.itextpdf.**
-dontwarn com.patrykandpatrick.vico.**

# Barcode & CameraX ML Kit
-dontwarn com.google.zxing.**
-dontwarn com.google.mlkit.**
-dontwarn androidx.camera.**
-dontwarn androidx.media3.**

# -----------------------------------------------------------------------
# SECTION 13: RELEASE LOGGING STRIPPING & WARNING SUPPRESSIONS
# -----------------------------------------------------------------------
# Strip Android Log methods from Release Binary
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
    public static int wtf(...);
}

# Strip Throwable printStackTrace from Release Binary
-assumenosideeffects class java.lang.Throwable {
    public void printStackTrace();
}

# Strip Kotlin Intrinsics null checks parameter strings
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static void checkNotNullParameter(java.lang.Object, java.lang.String);
    public static void checkNotNull(java.lang.Object, java.lang.String);
}

# Global Warning Suppressions
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
-dontwarn org.slf4j.**
-dontwarn com.sun.jna.**
-dontwarn sun.misc.**
-dontwarn javax.lang.model.**