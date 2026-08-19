package com.atk.atk_cargo.api

import android.content.Context
import android.util.Log
import com.atk.atk_cargo.BuildConfig
import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // اتصال باید سریع برقرار شود؛ خواندن/نوشتن ممکن است بار سنگین‌تری داشته باشد
    private const val CONNECT_TIMEOUT_SECONDS = 10L
    private const val READ_TIMEOUT_SECONDS = 30L
    private const val WRITE_TIMEOUT_SECONDS = 30L
    private const val HTTP_CACHE_SIZE_BYTES = 10L * 1024 * 1024

    // Base URL from Secrets
    private val BASE_URL = Secrets.getBaseUrl()

    // باید پیش از اولین دسترسی به apiService فراخوانی شود (در AtkCargoApplication.onCreate)
    // تا کش HTTP دیسک فعال شود؛ okHttpClient با lazy مقداردهی می‌شود، پس این مقدار
    // به‌موقع در دسترس okHttpClient قرار می‌گیرد.
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    // نسخه‌ی برنامه فقط یک‌بار خوانده و کش می‌شود؛ برای گیت min_allowed_version
    // سمت سرور (S-4) در هر درخواست احرازهویت‌شده فرستاده می‌شود
    private val appVersionName: String? by lazy {
        try {
            appContext?.let { ctx ->
                ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName
            }
        } catch (_: Exception) {
            null
        }
    }

    // Float Type Adapter for better handling of float values
    private class FloatTypeAdapter : TypeAdapter<Float>() {
        override fun write(out: JsonWriter, value: Float) {
            out.value(value)
        }

        override fun read(reader: JsonReader): Float {
            return try {
                when (reader.peek()) {
                    // null یک مقدار مجاز/مورد انتظار از سرور است، نه خطا — بدون لاگ
                    JsonToken.NULL -> {
                        reader.nextNull()
                        0f
                    }
                    JsonToken.NUMBER -> reader.nextDouble().toFloat()
                    JsonToken.STRING -> {
                        val raw = reader.nextString()
                        raw.toFloatOrNull() ?: run {
                            // قبلاً این حالت بی‌صدا 0f برمی‌گرداند — یعنی یک عدد
                            // واقعی (مثلاً تناژ/درصد) که سرور رشته‌ی غیرقابل‌پارس
                            // فرستاده، در UI به‌شکل «۰» دیده می‌شد بدون هیچ نشانه‌ای
                            // که داده نامعتبر بوده (I-08). حداقل در Logcat ثبت می‌شود
                            // تا در عیب‌یابی میدانی گم نشود.
                            Log.w("RetrofitClient", "FloatTypeAdapter: مقدار رشته‌ای غیرقابل‌تبدیل به float دریافت شد: \"$raw\" — 0f جایگزین شد")
                            0f
                        }
                    }
                    else -> {
                        Log.w("RetrofitClient", "FloatTypeAdapter: نوع JSON غیرمنتظره برای فیلد float: ${reader.peek()} — 0f جایگزین شد")
                        reader.skipValue()
                        0f
                    }
                }
            } catch (e: Exception) {
                Log.w("RetrofitClient", "FloatTypeAdapter: خطا هنگام پارس مقدار float — 0f جایگزین شد", e)
                0f
            }
        }
    }

    // Configure Gson with custom type adapter
    private val gson = GsonBuilder()
        .setStrictness(Strictness.LENIENT)
        .serializeNulls()
        .registerTypeAdapter(Float::class.java, FloatTypeAdapter())
        .create()

    // Logging interceptor for debug builds
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    // Headers interceptor
    private val headersInterceptor = Interceptor { chain ->
        val original = chain.request()
        val builder = original.newBuilder()
            .addHeader("Accept", "application/json")
            .addHeader("Content-Type", "application/json")

        // هویت نشست فعلی برای احراز هویت endpointهای محافظت‌شده (مثل app_api.php)
        // در صورت وجود به هر درخواست افزوده می‌شود؛ قبل از ورود کاربر این مقادیر
        // خالی هستند و هدرها اضافه نمی‌شوند.
        AuthSession.username.takeIf { it.isNotEmpty() }?.let { builder.addHeader("X-Username", it) }
        AuthSession.deviceId.takeIf { it.isNotEmpty() }?.let { builder.addHeader("X-Device-Id", it) }
        AuthSession.sessionToken.takeIf { it.isNotEmpty() }?.let { builder.addHeader("X-Session-Token", it) }
        // قفل نسخه‌ی منقضی (min_allowed_version) قبلاً فقط سمت کلاینت اعمال می‌شد؛
        // یک کلاینت قدیمی/دستکاری‌شده که دیالوگ VersionExpired را دور بزند همچنان
        // به همه‌ی APIهای تجاری دسترسی کامل داشت (S-4)
        appVersionName?.let { builder.addHeader("X-App-Version", it) }

        val request = builder
            .method(original.method, original.body)
            .build()
        chain.proceed(request)
    }

    // چون چند صفحه هم‌زمان به همین هاست poll می‌کنند، pool بزرگ‌تر از پیش‌فرض OkHttp
    // (۵ اتصال) باعث می‌شود اتصالات idle بین pollها دوباره استفاده شوند نه بسته/باز.
    private val connectionPool = ConnectionPool(10, 5, TimeUnit.MINUTES)

    // I-05: تمدید خودکار access token با refresh token روی ۴۰۱ با
    // code=access_token_expired. appContext!! چون همین invariant از قبل روی
    // appContext در این فایل وجود دارد (باید قبل از اولین دسترسی به apiService
    // مقداردهی شود).
    private val tokenAuthenticator: TokenAuthenticator by lazy {
        TokenAuthenticator(
            baseUrl = BASE_URL,
            userPreferencesManager = UserPreferencesManager(appContext!!)
        )
    }

    // Configure OkHttpClient — lazy تا appContext قبل از ساخته‌شدن این کلاینت
    // (توسط RetrofitClient.init در AtkCargoApplication.onCreate) فرصت مقداردهی داشته باشد
    private val okHttpClient: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(headersInterceptor)
            .authenticator(tokenAuthenticator)
            .connectionPool(connectionPool)
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)

        // فقط برای GETهایی که سرور صریحاً Cache-Control/ETag می‌فرستد (مثل
        // getShipsList) اثر دارد؛ endpointهای نوشتن/حذف بدون این هدرها کش نمی‌شوند.
        appContext?.let { ctx ->
            builder.cache(Cache(File(ctx.cacheDir, "http_cache"), HTTP_CACHE_SIZE_BYTES))
        }

        builder.build()
    }

    // Configure and create Retrofit instance
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    // Router v2 — تنها API stack کلاینت (v1/protected_proxy.php کاملاً حذف
    // شده، هم سمت سرور هم سمت کلاینت — DEEP_CODE_AUDIT.md #Phase3.1/3.2).
    val apiServiceV2: ApiServiceV2 by lazy {
        retrofit.create(ApiServiceV2::class.java)
    }
}