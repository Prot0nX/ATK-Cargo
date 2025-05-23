package com.atk.atk_cargo.api

import android.graphics.Bitmap
import android.util.Base64
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

// Data classes for OpenRouter API
data class OpenRouterRequest(
    @SerializedName("model") val model: String,
    @SerializedName("messages") val messages: List<OpenRouterMessage>,
    @SerializedName("max_tokens") val maxTokens: Int = 300,
    @SerializedName("temperature") val temperature: Double = 0.1
)

data class OpenRouterMessage(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: List<OpenRouterContent>
)

data class OpenRouterContent(
    @SerializedName("type") val type: String,
    @SerializedName("text") val text: String? = null,
    @SerializedName("image_url") val imageUrl: OpenRouterImageUrl? = null
)

data class OpenRouterImageUrl(
    @SerializedName("url") val url: String
)

data class OpenRouterResponse(
    @SerializedName("choices") val choices: List<OpenRouterChoice>
)

data class OpenRouterChoice(
    @SerializedName("message") val message: OpenRouterResponseMessage
)

data class OpenRouterResponseMessage(
    @SerializedName("content") val content: String
)

interface OpenRouterService {
    @POST("api/v1/chat/completions")
    suspend fun analyzeImage(
        @Header("Authorization") authorization: String,
        @Header("HTTP-Referer") referer: String,
        @Header("X-Title") title: String,
        @Body request: OpenRouterRequest
    ): Response<OpenRouterResponse>
}

object OpenRouterClient {
    private const val BASE_URL = "https://openrouter.ai/"
    private const val MODEL = "meta-llama/llama-3.2-11b-vision-instruct:free"
    
    // API Key - از OpenRouterConfig استفاده می‌شود
    private val service: OpenRouterService by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
        
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(OpenRouterService::class.java)
    }
    
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
    
    suspend fun analyzeWeightFromImage(bitmap: Bitmap): String? = withContext(Dispatchers.IO) {
        try {
            val base64Image = bitmapToBase64(bitmap)
            val imageUrl = "data:image/jpeg;base64,$base64Image"
            
            val request = OpenRouterRequest(
                model = MODEL,
                messages = listOf(
                    OpenRouterMessage(
                        role = "user",
                        content = listOf(
                            OpenRouterContent(
                                type = "text",
                                text = """
                                    لطفاً این تصویر قبض باسکول را تحلیل کنید و مقدار وزن خالص را استخراج کنید.
                                    
                                    دستورالعمل‌ها:
                                    1. به دنبال عبارات "وزن خالص"، "خالص" یا "وزن" باشید
                                    2. عدد مربوط به وزن خالص را پیدا کنید (معمولاً بین 5,000 تا 45,000 کیلوگرم)
                                    3. فقط عدد را بدون واحد و بدون کاما برگردانید
                                    4. اگر چندین عدد پیدا کردید، آن را که بیشترین احتمال وزن خالص کامیون است انتخاب کنید
                                    5. اگر هیچ وزن خالصی پیدا نکردید، "0" برگردانید
                                    
                                    مثال پاسخ صحیح: 25000
                                    فقط عدد را برگردانید، هیچ توضیح اضافی نیاز نیست.
                                """.trimIndent()
                            ),
                            OpenRouterContent(
                                type = "image_url",
                                imageUrl = OpenRouterImageUrl(url = imageUrl)
                            )
                        )
                    )
                ),
                maxTokens = 300,
                temperature = 0.1
            )
            
            val response = service.analyzeImage(
                authorization = "Bearer ${OpenRouterConfig.API_KEY}",
                referer = "https://atk-cargo.com",
                title = "ATK Cargo Weight Scanner",
                request = request
            )
            
            if (response.isSuccessful) {
                val result = response.body()?.choices?.firstOrNull()?.message?.content
                return@withContext extractNumberFromResponse(result)
            } else {
                println("OpenRouter API Error: ${response.code()} - ${response.message()}")
                return@withContext null
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }
    
    private fun extractNumberFromResponse(response: String?): String? {
        if (response.isNullOrBlank()) return null
        
        // استخراج عدد از پاسخ
        val cleanResponse = response.trim()
        
        // ابتدا به دنبال اعداد 4 تا 6 رقمی بگردیم
        val numberRegex = Regex("\\b(\\d{4,6})\\b")
        val matches = numberRegex.findAll(cleanResponse).toList()
        
        // بررسی هر عدد پیدا شده
        for (match in matches) {
            val number = match.value.toIntOrNull()
            if (number != null && number in 5000..45000) {
                return number.toString()
            }
        }
        
        // اگر عدد مستقیم پیدا نشد، سعی کنیم اعداد با کاما را پیدا کنیم
        val commaNumberRegex = Regex("\\b(\\d{1,2}[,.]\\d{3}[,.]?\\d{0,3})\\b")
        val commaMatches = commaNumberRegex.findAll(cleanResponse).toList()
        
        for (commaMatch in commaMatches) {
            val commaNumber = commaMatch.value.replace(Regex("[,.]"), "").toIntOrNull()
            if (commaNumber != null && commaNumber in 5000..45000) {
                return commaNumber.toString()
            }
        }
        
        // اگر هیچ عدد معتبری پیدا نشد، null برگردان (نه صفر)
        return null
    }
} 