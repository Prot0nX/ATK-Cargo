package com.atk.atk_cargo.feature.cargo_registration.domain.ocr

import android.content.Context
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class EnhancedNumberAnalyzer(
    private val context: Context,
    private val onNumbersDetected: (List<String>, String) -> Unit,
    private val onAnalysisStateChanged: ((Boolean) -> Unit)? = null
) : ImageAnalysis.Analyzer {
    
    private val textRecognizer = TextRecognition.getClient(com.google.mlkit.vision.text.latin.TextRecognizerOptions.DEFAULT_OPTIONS)
    private var lastDetectionTime = 0L
    private val detectionCooldown = 1500L // کاهش زمان انتظار برای سرعت بیشتر
    private var isProcessingWithAI = false
    
    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val currentTime = System.currentTimeMillis()
        
        if (isProcessingWithAI || currentTime - lastDetectionTime < detectionCooldown) {
            imageProxy.close()
            return
        }
        
        lastDetectionTime = currentTime
        isProcessingWithAI = true
        onAnalysisStateChanged?.invoke(true)
        
        processImage(imageProxy)
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImage(imageProxy: ImageProxy) {
        val inputImage = InputImage.fromMediaImage(
            imageProxy.image!!,
            imageProxy.imageInfo.rotationDegrees
        )
        
        textRecognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                val detectedText = visionText.text
                val numbers = extractNetWeights(detectedText)
                
                isProcessingWithAI = false
                onAnalysisStateChanged?.invoke(false)
                
                if (numbers.isNotEmpty()) {
                    val bestNumber = numbers.first()
                    onNumbersDetected(numbers, bestNumber)
                } else {
                    onNumbersDetected(emptyList(), "")
                }
            }
            .addOnFailureListener { e ->
                isProcessingWithAI = false
                onAnalysisStateChanged?.invoke(false)
                onNumbersDetected(emptyList(), "")
                e.printStackTrace()
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun extractNetWeights(text: String): List<String> {
        val result = mutableListOf<String>()
        
        val patterns = listOf(
            Regex("(?:وزن\\s*خالص|خالص)[\\s:=]*(\\d{1,2}[,.]\\d{3})"),
            Regex("(?:وزن\\s*خالص|خالص)[\\s:=]*(\\d{5,6})"),
            Regex("\\b(\\d{5,6})\\b"),
            Regex("\\b(\\d{1,2}[,.]\\d{3})\\b")
        )
        
        for (pattern in patterns) {
            val matches = pattern.findAll(text)
            matches.forEach { matchResult ->
                val numberStr = matchResult.groupValues[1].replace(Regex("[,.]"), "")
                val number = numberStr.toIntOrNull()
                if (number != null && number in 1000..60000) {
                    result.add(number.toString())
                }
            }
            
            if (result.isNotEmpty()) break
        }
        
        return result
    }
}

suspend fun recognizeTextFromImage(image: InputImage): String = suspendCancellableCoroutine { continuation ->
    val recognizer = TextRecognition.getClient(com.google.mlkit.vision.text.latin.TextRecognizerOptions.DEFAULT_OPTIONS)
    
    recognizer.process(image)
        .addOnSuccessListener { visionText ->
            // بازگرداندن متن استخراج شده
            continuation.resume(visionText.text)
        }
        .addOnFailureListener { e ->
            // در صورت خطا، رشته خالی برگردان
            continuation.resume("")
            e.printStackTrace()
        }
}

fun extractNumber(text: String): String {
    val pattern = Regex("\\b(\\d{5,6}|\\d{1,2}[,.]\\d{3})\\b")
    val matchResult = pattern.find(text)
    return if (matchResult != null) {
        matchResult.groupValues[1].replace(Regex("[,.]"), "")
    } else {
        ""
    }
}

