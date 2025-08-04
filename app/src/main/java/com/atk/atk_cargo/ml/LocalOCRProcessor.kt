package com.atk.atk_cargo.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.math.max

/**
 * کلاس پردازش پیشرفته OCR برای تشخیص وزن خالص
 * این کلاس از ML Kit و الگوریتم‌های بهینه‌شده برای تشخیص دقیق وزن استفاده می‌کند
 */
class LocalOCRProcessor(private val context: Context) {
    
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    // الگوهای مختلف برای تشخیص وزن خالص
    private val weightPatterns = listOf(
        // الگوی اصلی: "وزن خالص: 12345" یا "خالص: 12,345"
        Regex("(?:وزن\\s*خالص|خالص)\\s*[:\\s=]\\s*(\\d{1,2}[,.]\\d{3}|\\d{4,6})"),
        // الگوی عددی ساده: اعداد 4 تا 6 رقمی
        Regex("\\b(\\d{4,6})\\b"),
        // الگوی با کاما یا نقطه: 12,345 یا 12.345
        Regex("\\b(\\d{1,2}[,.]\\d{3})\\b"),
        // الگوی فارسی: "خالص ۱۲۳۴۵"
        Regex("(?:خالص|وزن)\\s*[:\\s=]\\s*([۰-۹]{4,6})")
    )
    
    /**
     * پردازش تصویر و استخراج وزن خالص
     */
    suspend fun processImage(bitmap: Bitmap): String? = withContext(Dispatchers.IO) {
        try {
            // پیش‌پردازش تصویر برای بهبود دقت
            val processedBitmap = preprocessImage(bitmap)
            
            // استخراج متن با ML Kit
            val extractedText = extractTextWithMLKit(processedBitmap)
            
            // تحلیل هوشمند متن برای یافتن وزن
            val result = analyzeTextForWeight(extractedText)
            
            // اگر نتیجه‌ای پیدا نشد، تصویر اصلی را هم امتحان کن
            if (result == null) {
                val originalText = extractTextWithMLKit(bitmap)
                return@withContext analyzeTextForWeight(originalText)
            }
            
            return@withContext result
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * پیش‌پردازش تصویر برای بهبود دقت OCR
     */
    private fun preprocessImage(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        // ایجاد bitmap جدید برای پردازش
        val processedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(processedBitmap)
        
        // تنظیم paint برای بهبود کنتراست و تبدیل به grayscale
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(
                ColorMatrix().apply {
                    setSaturation(0f) // تبدیل به grayscale
                    
                    // افزایش کنتراست
                    val contrast = 1.8f
                    val brightness = 20f
                    val matrix = floatArrayOf(
                        contrast, 0f, 0f, 0f, brightness,
                        0f, contrast, 0f, 0f, brightness,
                        0f, 0f, contrast, 0f, brightness,
                        0f, 0f, 0f, 1f, 0f
                    )
                    set(matrix)
                }
            )
        }
        
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return processedBitmap
    }
    
    /**
     * استخراج متن با ML Kit
     */
    private suspend fun extractTextWithMLKit(bitmap: Bitmap): String = suspendCancellableCoroutine { continuation ->
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        
        textRecognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                continuation.resume(visionText.text)
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
                continuation.resume("")
            }
    }
    
    /**
     * تحلیل هوشمند متن برای یافتن وزن خالص
     */
    private fun analyzeTextForWeight(text: String): String? {
        if (text.isBlank()) return null
        
        val candidates = mutableListOf<WeightCandidate>()
        val lines = text.split("\n")
        
        // جستجو در هر خط
        lines.forEachIndexed { lineIndex, line ->
            // جستجو با الگوهای مختلف
            weightPatterns.forEachIndexed { patternIndex, pattern ->
                val matches = pattern.findAll(line)
                matches.forEach { matchResult ->
                    val numberStr = matchResult.groupValues[1]
                        .replace(Regex("[,.]"), "") // حذف کاما و نقطه
                        .replace(Regex("[۰-۹]")) { persianToEnglish(it.value) } // تبدیل اعداد فارسی
                    
                    val number = numberStr.toIntOrNull()
                    if (number != null && isValidWeight(number)) {
                        val score = calculateScore(line, number, patternIndex, lineIndex)
                        candidates.add(WeightCandidate(number.toString(), score, line))
                    }
                }
            }
        }
        
        // انتخاب بهترین کاندید
        return candidates.maxByOrNull { it.score }?.weight
    }
    
    /**
     * محاسبه امتیاز برای هر کاندید
     */
    private fun calculateScore(line: String, weight: Int, patternIndex: Int, lineIndex: Int): Double {
        var score = 0.0
        
        // امتیاز بر اساس وجود کلمات کلیدی
        score += when {
            line.contains("وزن خالص", ignoreCase = true) -> 2.0
            line.contains("خالص", ignoreCase = true) -> 1.5
            line.contains("وزن", ignoreCase = true) -> 1.0
            line.contains("net", ignoreCase = true) -> 1.2
            line.contains("weight", ignoreCase = true) -> 1.0
            else -> 0.0
        }
        
        // امتیاز بر اساس محدوده وزن
        score += when (weight) {
            in 10000..25000 -> 1.5 // محدوده متداول
            in 8000..30000 -> 1.2
            in 5000..35000 -> 1.0
            in 35000..45000 -> 0.8
            else -> 0.0
        }
        
        // امتیاز بر اساس نوع الگو (الگوهای دقیق‌تر امتیاز بیشتر)
        score += when (patternIndex) {
            0 -> 1.0 // الگوی کامل با کلمه کلیدی
            1 -> 0.6 // عدد ساده
            2 -> 0.8 // عدد با کاما/نقطه
            3 -> 0.9 // الگوی فارسی
            else -> 0.5
        }
        
        // امتیاز بر اساس موقعیت در متن (خطوط اول مهم‌تر)
        score += max(0.0, 0.5 - (lineIndex * 0.1))
        
        return score
    }
    
    /**
     * بررسی معتبر بودن وزن
     */
    private fun isValidWeight(weight: Int): Boolean {
        return weight in 5000..45000
    }
    
    /**
     * تبدیل اعداد فارسی به انگلیسی
     */
    private fun persianToEnglish(persianDigit: String): String {
        val persianDigits = "۰۱۲۳۴۵۶۷۸۹"
        val englishDigits = "0123456789"
        
        return persianDigit.map { char ->
            val index = persianDigits.indexOf(char)
            if (index >= 0) englishDigits[index] else char
        }.joinToString("")
    }
    
    /**
     * کلاس داده برای نگهداری اطلاعات کاندیدهای وزن
     */
    private data class WeightCandidate(
        val weight: String,
        val score: Double,
        val sourceLine: String
    )
    
    /**
     * آزادسازی منابع
     */
    fun cleanup() {
        textRecognizer.close()
    }
}