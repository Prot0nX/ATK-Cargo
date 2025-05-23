# =======================================================================
# تنظیمات ProGuard برای برنامه ATK-Cargo - نسخه بهبود یافته امنیتی
# =======================================================================

# =======================================================================
# 1. تنظیمات پایه و بهینه‌سازی پیشرفته (اولویت بالا)
# =======================================================================
-optimizationpasses 5                  # کاهش تعداد دفعات بهینه‌سازی برای کاهش خطا
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
# 3. تنظیمات امنیتی پیشرفته برای محافظت از کدهای حساس (اولویت بالا)
# =======================================================================

# محافظت شدید از کلاس SignatureVerifier
-keep class com.atk.atk_cargo.security.SignatureVerifier {
    <init>(android.content.Context);
}

# مبهم‌سازی شدید متدهای امنیتی SignatureVerifier
-keepclassmembers,allowobfuscation,allowshrinking class com.atk.atk_cargo.security.SignatureVerifier {
    private *** i();
    private *** k();
    private *** m();
    private *** q();
    private *** s(***);
    private *** v(***);
    private *** encryptData(***);
    private *** decryptData(***);
    private *** checkLicenseValidity();
    private *** o(***);
}

# مبهم‌سازی شدید ثوابت امنیتی در Companion Object
-keepclassmembers,allowobfuscation,allowshrinking class com.atk.atk_cargo.security.SignatureVerifier$Companion {
    private static final java.lang.String C;
    private static final java.lang.String D;
    private static final java.lang.String E;
    private static final java.lang.String LICENSE_ENDPOINT;
    private static final java.lang.String LICENSE_INFO_ENDPOINT;
    private static final java.lang.String KEY_LICENSE;
    private static final int TIMEOUT_MILLIS;
    private static final int MAX_RETRIES;
}

# حفظ enum SecurityErrorType با مبهم‌سازی
-keep,allowobfuscation enum com.atk.atk_cargo.security.SecurityErrorType

# محافظت کامل از پکیج امنیتی
-keep,allowobfuscation class com.atk.atk_cargo.security.** { *; }

# محافظت از MainActivity با حداقل نمایش
-keep class com.atk.atk_cargo.MainActivity {
    public <init>();
    protected void onCreate(android.os.Bundle);
}

# مبهم‌سازی شدید متدهای امنیتی MainActivity
-keepclassmembers,allowobfuscation,allowshrinking class com.atk.atk_cargo.MainActivity {
    private void performSecurityCheck();
    @androidx.compose.runtime.Composable private void HandleSecurityCheck(kotlin.jvm.functions.Function0);
    private void checkUserSession();
    private void handleIntent(android.content.Intent);
    private void observeApplicationStates();
    private void handleDownloadState(***);
}

# مبهم‌سازی فیلدهای امنیتی MainActivity
-keepclassmembers,allowobfuscation,allowshrinking class com.atk.atk_cargo.MainActivity {
    private *** isSecurityCheck*;
    private *** signatureVerifier;
    private *** securityErrorType;
    private *** _isSessionValid;
    private *** userPreferencesManager;
}

# =======================================================================
# 4. تنظیمات مبهم‌سازی پیشرفته (اولویت بالا)
# =======================================================================
-renamesourcefileattribute ""          # حذف کامل نام فایل منبع
-repackageclasses 'a'                  # بسته‌بندی مجدد با نام کوتاه
-allowaccessmodification               # اجازه تغییر سطح دسترسی
-overloadaggressively                  # بازنویسی انبوه

# مبهم‌سازی نام کلاس‌ها و متدها
-obfuscationdictionary dictionary.txt
-classobfuscationdictionary dictionary.txt
-packageobfuscationdictionary dictionary.txt

# =======================================================================
# 5. تنظیمات ضد دیباگ و ضد تحلیل (اولویت بالا)
# =======================================================================
# حذف کامل اطلاعات دیباگ
-keepattributes !SourceFile,!LineNumberTable

# حذف کامل لاگ‌ها و اطلاعات حساس
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
    public static *** wtf(...);
    public static *** println(...);
}

# حذف کامل System.out و System.err
-assumenosideeffects class java.lang.System {
    public static *** out;
    public static *** err;
}

# حذف printStackTrace
-assumenosideeffects class java.lang.Throwable {
    public void printStackTrace();
    public void printStackTrace(java.io.PrintStream);
    public void printStackTrace(java.io.PrintWriter);
}

# =======================================================================
# 6. تنظیمات API و ارتباطات شبکه (اولویت متوسط)
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
# 7. مدل‌های برنامه (اولویت متوسط)
# =======================================================================
# حفظ مدل‌های داده برای سریالیزیشن/دیسریالیزیشن
-keep,allowobfuscation class com.atk.atk_cargo.api.** { *; }
-keep,allowobfuscation class com.atk.atk_cargo.models.** { *; }
-keep,allowobfuscation class com.atk.atk_cargo.network.** { *; }

# =======================================================================
# 8. تنظیمات Kotlin (اولویت متوسط)
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
# 9. تنظیمات Coroutines (اولویت متوسط)
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
# 10. تنظیمات Android (اولویت متوسط)
# =======================================================================
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# =======================================================================
# 11. تنظیمات ضد مهندسی معکوس پیشرفته (اولویت بالا)
# =======================================================================
# جلوگیری از reflection روی کلاس‌های امنیتی
-keepclassmembers,allowobfuscation class com.atk.atk_cargo.security.** {
    !public <fields>;
    !public <methods>;
}

# جلوگیری از reflection روی MainActivity
-keepclassmembers,allowobfuscation class com.atk.atk_cargo.MainActivity {
    !public <fields>;
    !public <methods>;
}

# مبهم‌سازی نام متغیرها و ثوابت
-keepclassmembers,allowobfuscation class * {
    private static final <fields>;
    private final <fields>;
    private <fields>;
}

# =======================================================================
# 12. سرکوب هشدارها و Missing Classes (اولویت بالا)
# =======================================================================
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
-dontwarn org.slf4j.**
-dontwarn aQute.bnd.**
-dontwarn edu.umd.cs.findbugs.**
-dontwarn org.apache.batik.**
-dontwarn org.osgi.framework.**
-dontwarn javax.crypto.**
-dontwarn java.security.**

# Apache POI و OpenXML
-dontwarn org.openxmlformats.**
-dontwarn org.apache.poi.**
-dontwarn org.apache.commons.compress.**
-dontwarn org.apache.xmlbeans.**

# TensorFlow Lite
-dontwarn org.tensorflow.lite.**

# XZ Utils
-dontwarn org.tukaani.xz.**

# W3C DOM
-dontwarn org.w3c.dom.**
-dontwarn org.w3.**

# ASM
-dontwarn org.objectweb.asm.**

# OSGi
-dontwarn org.osgi.**

# Microsoft Office schemas
-dontwarn com.microsoft.schemas.**

# ETSI schemas
-dontwarn org.etsi.uri.**

# Java AWT و Swing (برای کتابخانه‌های PDF)
-dontwarn java.awt.**
-dontwarn javax.swing.**
-dontwarn java.beans.**

# Java Mail
-dontwarn javax.mail.**

# JMS
-dontwarn javax.jms.**

# JMX
-dontwarn javax.management.**

# JNDI
-dontwarn javax.naming.**

# Java ImageIO
-dontwarn javax.imageio.**

# Java Tools
-dontwarn javax.tools.**

# XML Crypto
-dontwarn javax.xml.crypto.**
-dontwarn org.apache.jcp.xml.dsig.**
-dontwarn org.apache.xml.security.**

# Google MediaPipe
-dontwarn com.google.mediapipe.**

# Sun JMX
-dontwarn com.sun.jdmk.**

# Java Management
-dontwarn java.lang.management.**

# Java Invoke
-dontwarn java.lang.invoke.**

# Apache Log4j
-dontwarn org.apache.log4j.**

# iText PDF
-dontwarn com.itextpdf.**

# GraphBuilder
-dontwarn com.graphbuilder.**

# AutoValue
-dontwarn autovalue.shaded.**
-dontwarn com.google.auto.value.**

# =======================================================================
# 13. تنظیمات اضافی برای امنیت بیشتر
# =======================================================================
# حذف metadata های اضافی
-keepattributes !LocalVariableTable,!LocalVariableTypeTable

# مبهم‌سازی نام پارامترها
-keepparameternames

# تنظیمات بهینه‌سازی
-dontshrink
-dontoptimize

# =======================================================================
# 14. قوانین اضافی برای رفع خطاهای Missing Class
# =======================================================================
-dontwarn javax.lang.model.SourceVersion
-dontwarn javax.lang.model.element.Element
-dontwarn javax.lang.model.element.ElementKind
-dontwarn javax.lang.model.element.Modifier
-dontwarn javax.lang.model.element.TypeElement
-dontwarn javax.lang.model.type.TypeMirror
-dontwarn javax.lang.model.type.TypeVisitor
-dontwarn javax.lang.model.util.Elements
-dontwarn javax.lang.model.util.SimpleTypeVisitor8
-dontwarn javax.lang.model.util.Types
