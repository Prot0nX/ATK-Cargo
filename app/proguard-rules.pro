# =======================================================================
# ENTERPRISE PROGUARD & R8 CONFIGURATION - ATK-CARGO
# Production-Grade Security Hardening, Optimization & Shrinking Rules
# =======================================================================

# -----------------------------------------------------------------------
# SECTION 1: GENERAL OPTIMIZATION & R8 HARDENING
# -----------------------------------------------------------------------
# Obfuscation Dictionaries & Repackaging
-obfuscationdictionary proguard-dictionary.txt
-classobfuscationdictionary proguard-dictionary.txt
-packageobfuscationdictionary proguard-dictionary.txt

-repackageclasses 'obfuscated'
-allowaccessmodification
-renamesourcefileattribute SourceFile

# -----------------------------------------------------------------------
# SECTION 2: ATTRIBUTE PRESERVATION & METADATA
# -----------------------------------------------------------------------
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,Exceptions
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,AnnotationDefault
-keepattributes SourceFile,LineNumberTable

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
# libsecrets.so اکنون از JNI_OnLoad + RegisterNatives استفاده می‌کند (نه قرارداد نام‌گذاری
# استاندارد Java_pkg_Class_method) — بنابراین نام‌های واقعی متد در جدول سیمبل .so دیگر
# فاش نمی‌شوند. اما چون RegisterNatives با رشتهٔ ثابت نام متد در زمان کامپایل C++ بایند
# می‌شود، نام کلاس/متدهای Secrets باید دقیقاً حفظ شوند وگرنه JNI_OnLoad شکست می‌خورد.
-keep class com.atk.atk_cargo.api.Secrets { *; }

# -----------------------------------------------------------------------
# SECTION 5: SECURITY & CRITICAL APPLICATION HARDENING
# -----------------------------------------------------------------------
# SecurityVerifier / CryptoManager / UserPreferencesManager:
-keepclassmembers enum com.atk.atk_cargo.security.SecurityErrorType {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Preserve MainActivity lifecycle entry point
-keep class com.atk.atk_cargo.MainActivity {
    public <init>();
    protected void onCreate(android.os.Bundle);
}

# -----------------------------------------------------------------------
# SECTION 6: NETWORKING (RETROFIT, OKHTTP, GSON)
# -----------------------------------------------------------------------
# Retrofit Interfaces & Annotations
-keep interface com.atk.atk_cargo.api.ApiServiceV2 { *; }

-keepclassmembers interface * {
    @retrofit2.http.* <methods>;
}

# Data Models & DTO Preservation (Gson Reflection Safety)
-keep class com.atk.atk_cargo.data.model.** {
    <fields>;
    <init>(...);
}

# Gson TypeAdapters & SerializedName Annotations
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Gson TypeToken anonymous subclasses (LoadingNotificationService, UserPreferencesManager)
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

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
-keep @kotlinx.serialization.Serializable class * { *; }
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
-dontwarn com.airbnb.lottie.**

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
}

# Global Warning Suppressions
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
-dontwarn org.slf4j.**
-dontwarn com.sun.jna.**
-dontwarn sun.misc.**
-dontwarn javax.lang.model.**