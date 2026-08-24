# قوانین بهینه‌سازی، امنیت و مبهم‌سازی R8 و ProGuard برای نسخه نهایی.

# تنظیم دیکشنری‌های مبهم‌سازی نام کلاس‌ها و پکیج‌ها.
-obfuscationdictionary proguard-dictionary.txt
-classobfuscationdictionary proguard-dictionary.txt
-packageobfuscationdictionary proguard-dictionary.txt

-repackageclasses 'obfuscated'
-allowaccessmodification
-renamesourcefileattribute SourceFile

# حفظ متادیتا و انوتیشن‌های ضروری در زمان اجرا.
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,Exceptions
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,AnnotationDefault
-keepattributes SourceFile,LineNumberTable

# حفظ کامپوننت‌های اصلی فریم‌ورک اندروید.
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgent

# حفظ کلاس‌ها و متدهای متصل به کدهای محلی C++ و JNI.
-keep class com.atk.atk_cargo.api.Secrets { *; }

# حفظ کلاس‌ها و انوم‌های ماژول‌های امنیتی.
-keepclassmembers enum com.atk.atk_cargo.security.SecurityErrorType {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# حفظ اکتیویتی اصلی برنامه.
-keep class com.atk.atk_cargo.MainActivity {
    public <init>();
    protected void onCreate(android.os.Bundle);
}

# قوانین شبکه، Retrofit، OkHttp و سریال‌سازی Gson.
-keep interface com.atk.atk_cargo.api.ApiServiceV2 { *; }

-keepclassmembers interface * {
    @retrofit2.http.* <methods>;
}

# حفظ ساختار مدل‌های داده و DTOها جهت عملکرد صحیح Gson.
-keep class com.atk.atk_cargo.data.model.** {
    <fields>;
    <init>(...);
}

# حفظ مبدل‌های سفارشی Gson و فیلدهای دارای SerializedName.
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# حفظ ساب‌کلاس‌های TypeToken برای پردازش ساختارهای جنریک در Gson.
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# قوانین کاتلین، کوروتین‌ها و سریال‌سازی kotlinx.serialization.
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}

# حفظ کارخانه‌های مدیریت کوروتین‌های کاتلین.
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# حفظ مسیرها و اشیای سریال‌پذیر در ناوبری کامپوز.
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

# قوانین پایگاه داده Room و رابط Parcelable.
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

# قوانین تسک‌های پس‌زمینه و WorkManager.
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-dontwarn androidx.work.**

# قوانین مربوط به Jetpack Compose و ناوبری رابط کاربری.
-dontwarn androidx.compose.**
-keep class androidx.compose.ui.platform.NestedScrollInteropConnection { *; }

-keepclassmembers class * {
    @androidx.navigation.** <methods>;
}

# قوانین کتابخانه‌های شخص ثالث، Lottie، ZXing و CameraX.
-dontwarn com.airbnb.lottie.**
-dontwarn com.google.zxing.**
-dontwarn com.google.mlkit.**
-dontwarn androidx.camera.**
-dontwarn androidx.media3.**

# حذف لاگ‌های تشخیصی android.util.Log در نسخه نهایی (Release).
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
}

# نادیده گرفتن هشدارهای عمومی وابستگی‌های بدون مصرف در زمان اجرا.
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
-dontwarn org.slf4j.**
-dontwarn com.sun.jna.**
-dontwarn sun.misc.**
-dontwarn javax.lang.model.**
