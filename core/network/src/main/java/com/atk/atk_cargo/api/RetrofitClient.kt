package com.atk.atk_cargo.api

import android.content.Context
import android.util.Log
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

    // باید پیش از اولین دسترسی به apiService (در AtkCargoApplication.onCreate) فراخوانی شود تا کش HTTP دیسک فعال شود
    private var appContext: Context? = null

    // core:network به BuildConfig ماژول app دسترسی ندارد، پس این پرچم و tokenStore از بیرون (app) در init تزریق می‌شوند
    private var debugLogging: Boolean = false
    private var tokenStore: TokenStore? = null

    fun init(context: Context, tokenStore: TokenStore, debugLogging: Boolean) {
        appContext = context.applicationContext
        this.tokenStore = tokenStore
        this.debugLogging = debugLogging
    }

    // نسخه‌ی برنامه فقط یک‌بار خوانده و کش می‌شود؛ برای گیت min_allowed_version سمت سرور در هر درخواست فرستاده می‌شود
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
                            // مقدار رشته‌ای غیرقابل‌پارس بی‌صدا ۰f می‌شد؛ حالا برای عیب‌یابی در Logcat هم ثبت می‌شود
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

    // interceptor لاگ برای بیلدهای دیباگ؛ عمداً lazy است چون property initializerها قبل از init() اجرا می‌شوند و eager بودن همیشه مقدار پیش‌فرض debugLogging را می‌گرفت
    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = if (debugLogging) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    // Headers interceptor
    private val headersInterceptor = Interceptor { chain ->
        val original = chain.request()
        val builder = original.newBuilder()
            .addHeader("Accept", "application/json")
            .addHeader("Content-Type", "application/json")

        // هویت نشست فعلی در صورت وجود به هر درخواست افزوده می‌شود؛ قبل از ورود کاربر این مقادیر خالی هستند و هدرها اضافه نمی‌شوند
        AuthSession.username.takeIf { it.isNotEmpty() }?.let { builder.addHeader("X-Username", it) }
        AuthSession.deviceId.takeIf { it.isNotEmpty() }?.let { builder.addHeader("X-Device-Id", it) }
        AuthSession.sessionToken.takeIf { it.isNotEmpty() }?.let { builder.addHeader("X-Session-Token", it) }
        // قفل نسخه‌ی منقضی قبلاً فقط سمت کلاینت بود؛ یک کلاینت دستکاری‌شده می‌توانست با دور زدن دیالوگ همچنان به همه‌ی APIها دسترسی داشته باشد
        appVersionName?.let { builder.addHeader("X-App-Version", it) }

        val request = builder
            .method(original.method, original.body)
            .build()
        chain.proceed(request)
    }

    // چون چند صفحه هم‌زمان poll می‌کنند، pool بزرگ‌تر از پیش‌فرض OkHttp باعث می‌شود اتصالات idle دوباره استفاده شوند نه بسته/باز
    private val connectionPool = ConnectionPool(10, 5, TimeUnit.MINUTES)

    // تمدید خودکار access token با refresh token روی ۴۰۱ با code=access_token_expired؛ tokenStore باید قبل از اولین دسترسی به apiService مقداردهی شده باشد
    private val tokenAuthenticator: TokenAuthenticator by lazy {
        TokenAuthenticator(
            baseUrl = BASE_URL,
            tokenStore = tokenStore!!
        )
    }

    // ساخت OkHttpClient؛ lazy تا appContext قبل از ساخته‌شدن این کلاینت (در RetrofitClient.init) فرصت مقداردهی داشته باشد
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

        // فقط برای GETهایی که سرور صریحاً Cache-Control/ETag می‌فرستد اثر دارد؛ endpointهای نوشتن/حذف بدون این هدرها کش نمی‌شوند
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

    // Router v2 تنها API stack کلاینت است؛ v1/protected_proxy.php کاملاً از سرور و کلاینت حذف شده
    val apiServiceV2: ApiServiceV2 by lazy {
        retrofit.create(ApiServiceV2::class.java)
    }
}