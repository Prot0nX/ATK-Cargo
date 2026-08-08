package com.atk.atk_cargo.api

import com.atk.atk_cargo.BuildConfig
import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // اتصال باید سریع برقرار شود؛ خواندن/نوشتن ممکن است بار سنگین‌تری داشته باشد
    private const val CONNECT_TIMEOUT_SECONDS = 10L
    private const val READ_TIMEOUT_SECONDS = 30L
    private const val WRITE_TIMEOUT_SECONDS = 30L

    // Base URL from Secrets
    private val BASE_URL = Secrets.getBaseUrl()

    // Float Type Adapter for better handling of float values
    private class FloatTypeAdapter : TypeAdapter<Float>() {
        override fun write(out: JsonWriter, value: Float) {
            out.value(value)
        }

        override fun read(reader: JsonReader): Float {
            return try {
                when (reader.peek()) {
                    JsonToken.NULL -> {
                        reader.nextNull()
                        0f
                    }
                    JsonToken.NUMBER -> reader.nextDouble().toFloat()
                    JsonToken.STRING -> reader.nextString().toFloatOrNull() ?: 0f
                    else -> {
                        reader.skipValue()
                        0f
                    }
                }
            } catch (_: Exception) {
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
        val request = original.newBuilder()
            .addHeader("Accept", "application/json")
            .addHeader("Content-Type", "application/json")
            .method(original.method, original.body)
            .build()
        chain.proceed(request)
    }

    // چون چند صفحه هم‌زمان به همین هاست poll می‌کنند، pool بزرگ‌تر از پیش‌فرض OkHttp
    // (۵ اتصال) باعث می‌شود اتصالات idle بین pollها دوباره استفاده شوند نه بسته/باز.
    private val connectionPool = ConnectionPool(10, 5, TimeUnit.MINUTES)

    // Configure OkHttpClient
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(headersInterceptor)
        .connectionPool(connectionPool)
        .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    // Configure and create Retrofit instance
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    // Create API Service instance
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}

// Retrofit client for Third Party API
object ThirdPartyRetrofitClient {
    private const val CONNECT_TIMEOUT_SECONDS = 10L
    private const val READ_TIMEOUT_SECONDS = 30L
    private const val WRITE_TIMEOUT_SECONDS = 30L
    private const val BASE_URL = "https://pishrodarya.ir/WorknetWebSite/service/api/"

    // Headers interceptor for Third Party API
    private val headersInterceptor = Interceptor { chain ->
        val original = chain.request()
        val request = original.newBuilder()
            .addHeader("webUserName", Secrets.getWebUserName())
            .addHeader("webUserPass", Secrets.getWebUserPass())
            .addHeader("AuthUser", Secrets.getAuthUser())
            .addHeader("AuthenticationX365", Secrets.getAuthenticationX365())
            .addHeader("Content-Type", "application/json")
            .method(original.method, original.body)
            .build()
        chain.proceed(request)
    }

    // Configure OkHttpClient
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(headersInterceptor)
        .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    // Configure Gson
    private val gson = GsonBuilder()
        .setStrictness(Strictness.LENIENT)
        .serializeNulls()
        .create()

    // Configure and create Retrofit instance
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    // Create Third Party API Service instance
    val thirdPartyApiService: ThirdPartyApiService by lazy {
        retrofit.create(ThirdPartyApiService::class.java)
    }
}