# =======================================================================
# ENTERPRISE PROGUARD & R8 CONFIGURATION - ATK-CARGO
# =======================================================================

# -----------------------------------------------------------------------
# 1. OPTIMIZATION & HARDENING CONFIGURATION
# -----------------------------------------------------------------------
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose

# Aggressive Obfuscation & Package Repackaging
-repackageclasses 'obfuscated'
-allowaccessmodification
-mergeinterfacesaggressively
-overloadaggressively
-renamesourcefileattribute SourceFile
-adaptresourcefilenames **.properties
-adaptresourcefilecontents **.properties,META-INF/MANIFEST.MF

# Obfuscation Dictionaries
-obfuscationdictionary proguard-dictionary.txt
-classobfuscationdictionary proguard-dictionary.txt
-packageobfuscationdictionary proguard-dictionary.txt

# R8 Optimizations
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

# -----------------------------------------------------------------------
# 2. ATTRIBUTE PRESERVATION
# -----------------------------------------------------------------------
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,Exceptions
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,AnnotationDefault
-keepattributes !SourceFile,!LineNumberTable

# -----------------------------------------------------------------------
# 3. ANDROID COMPONENT ENTRY POINTS
# -----------------------------------------------------------------------
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgent

# -----------------------------------------------------------------------
# 4. JNI & NATIVE BRIDGE PROTECTION
# -----------------------------------------------------------------------
# Critical: Keep JNI class name and native method signatures matching C++ libsecrets.so
-keep,allowoptimization class com.atk.atk_cargo.api.Secrets {
    native <methods>;
}
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

# -----------------------------------------------------------------------
# 5. SECURITY & CORE APPLICATION HARDENING
# -----------------------------------------------------------------------
# Keep Security Error Enum value methods
-keepclassmembers enum com.atk.atk_cargo.security.SecurityErrorType {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Preserve MainActivity lifecycle entry point while allowing member obfuscation
-keep class com.atk.atk_cargo.MainActivity {
    public <init>();
    protected void onCreate(android.os.Bundle);
}

# -----------------------------------------------------------------------
# 6. NETWORKING: RETROFIT, OKHTTP & GSON
# -----------------------------------------------------------------------
# Retrofit Interface & Annotations
-keepattributes Signature, RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

-keepclassmembers interface * {
    @retrofit2.http.* <methods>;
}

# Gson Serialization & Annotations (Scoped to classes implementing TypeAdapters or SerializedName fields)
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Networking Library Warning Suppress
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-dontwarn com.google.gson.**

# -----------------------------------------------------------------------
# 7. KOTLINX SERIALIZATION & JETPACK COMPOSE NAVIGATION (SCOPED)
# -----------------------------------------------------------------------
# Type-safe Navigation routes using Kotlinx Serialization - Scoped specifically to @Serializable targets
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}

-keepclassmembers class **$Companion {
    *** serializer(...);
}

-keepclassmembers class * implements kotlinx.serialization.KSerializer {
    public static *** INSTANCE;
}

-keepclassmembers class * {
    @kotlinx.serialization.Serializer *** serializer(...);
}

# -----------------------------------------------------------------------
# 8. KOTLIN COROUTINES & METADATA
# -----------------------------------------------------------------------
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}

-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# -----------------------------------------------------------------------
# 9. ROOM DATABASE & PARCELABLE
# -----------------------------------------------------------------------
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.**

-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# -----------------------------------------------------------------------
# 10. DEPENDENCY INJECTION (KOIN) & WORKMANAGER (SCOPED - NO OVERLY BROAD RULES)
# -----------------------------------------------------------------------
# Koin DI reflection safety (Scoped to annotated members and core entry point)
-keepclassmembers class * {
    @org.koin.core.annotation.* <fields>;
    @org.koin.core.annotation.* <methods>;
}
-keepnames class org.koin.core.Koin { *; }
-dontwarn org.koin.**

# WorkManager Worker reflection instantiation (Scoped strictly to Worker constructors)
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-dontwarn androidx.work.**

# -----------------------------------------------------------------------
# 11. JETPACK COMPOSE & UI LIBRARIES
# -----------------------------------------------------------------------
-dontwarn androidx.compose.**
-keep class androidx.compose.ui.platform.NestedScrollInteropConnection { *; }

# Third-party UI & Media Libraries
-dontwarn coil.**
-dontwarn io.coil.**
-dontwarn com.airbnb.lottie.**
-dontwarn com.itextpdf.**
-dontwarn com.patrykandpatrick.vico.**
-dontwarn com.google.zxing.**
-dontwarn androidx.media3.**

# -----------------------------------------------------------------------
# 12. RELEASE LOGGING STRIPPING & OBFUSCATION HARDENING
# -----------------------------------------------------------------------
# Strip all Android Log methods from release binary
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
    public static int wtf(...);
}

# Strip stack traces in production
-assumenosideeffects class java.lang.Throwable {
    public void printStackTrace();
}

# Strip Kotlin Intrinsics null checks parameter strings for obfuscation
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static void checkNotNullParameter(java.lang.Object, java.lang.String);
    public static void checkNotNull(java.lang.Object, java.lang.String);
}

# -----------------------------------------------------------------------
# 13. GLOBAL WARNING SUPPRESSIONS
# -----------------------------------------------------------------------
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
-dontwarn org.slf4j.**
-dontwarn com.sun.jna.**
-dontwarn sun.misc.**
-dontwarn javax.lang.model.**