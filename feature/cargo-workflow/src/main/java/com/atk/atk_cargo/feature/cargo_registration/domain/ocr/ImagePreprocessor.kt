package com.atk.atk_cargo.feature.cargo_registration.domain.ocr

import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import kotlin.math.sqrt

// روی هر فریم دوربین اجرا می‌شود؛ بافرهای موقت برای کارایی بیشتر به حداقل رسیده‌اند
fun preprocessImage(imageProxy: ImageProxy): InputImage {
    val bitmap = imageProxy.toBitmap()
    val width = bitmap.width
    val height = bitmap.height

    val pixels = IntArray(width * height)
    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

 // مرحله 1: افزایش کنتراست مستقیم روی آرایه‌ی پیکسل (معادل ColorMatrix قبلی)
    applyContrastEnhancement(pixels)

 // مرحله 2: تبدیل به تصویر باینری با آستانه‌گذاری محلی
    adaptiveThresholding(pixels, width, height)

    val scratch = IntArray(width * height)

 // مرحله 3: حذف نویز با فیلتر میانه
    medianFilter(pixels, scratch, width, height)

 // مرحله 4: تقویت لبه‌ها برای بهبود تشخیص اعداد
    enhanceEdges(pixels, scratch, width, height)

    bitmap.setPixels(pixels, 0, width, 0, 0, width, height)

    return InputImage.fromBitmap(bitmap, imageProxy.imageInfo.rotationDegrees)
}

private fun applyContrastEnhancement(pixels: IntArray) {
    for (i in pixels.indices) {
        val pixel = pixels[i]
        val a = ((pixel ushr 24) and 0xFF) * 1.2f
        val r = ((pixel shr 16) and 0xFF) * 2.5f - 50f
        val g = ((pixel shr 8) and 0xFF) * 2.5f - 50f
        val b = (pixel and 0xFF) * 2.5f - 50f

        val ac = a.toInt().coerceIn(0, 255)
        val rc = r.toInt().coerceIn(0, 255)
        val gc = g.toInt().coerceIn(0, 255)
        val bc = b.toInt().coerceIn(0, 255)

        pixels[i] = (ac shl 24) or (rc shl 16) or (gc shl 8) or bc
    }
}

private fun adaptiveThresholding(pixels: IntArray, width: Int, height: Int) {
    val windowSize = 15  // اندازه پنجره برای محاسبه آستانه محلی
    val c = 10          // ثابت کاهش از میانگین محلی
    
    for (y in 0 until height) {
        for (x in 0 until width) {
            val pos = y * width + x
            
 // محاسبه میانگین در پنجره محلی
            var sum = 0
            var count = 0
            
            for (wy in maxOf(0, y - windowSize / 2) until minOf(height, y + windowSize / 2 + 1)) {
                for (wx in maxOf(0, x - windowSize / 2) until minOf(width, x + windowSize / 2 + 1)) {
                    val pixel = pixels[wy * width + wx]
                    val gray = (pixel and 0xFF) + ((pixel shr 8) and 0xFF) + ((pixel shr 16) and 0xFF)
                    sum += gray / 3
                    count++
                }
            }
            
            val threshold = if (count > 0) sum / count - c else 128
            
 // اعمال آستانه محلی
            val pixel = pixels[pos]
            val gray = ((pixel and 0xFF) + ((pixel shr 8) and 0xFF) + ((pixel shr 16) and 0xFF)) / 3
            
            pixels[pos] = if (gray > threshold) 0xFFFFFFFF.toInt() else 0xFF000000.toInt()
        }
    }
}

private fun medianFilter(pixels: IntArray, output: IntArray, width: Int, height: Int) {
    System.arraycopy(pixels, 0, output, 0, pixels.size)
    val windowSize = 3
    val window = IntArray(windowSize * windowSize)
    
    for (y in 1 until height - 1) {
        for (x in 1 until width - 1) {
            var idx = 0
            
 // جمع‌آوری مقادیر پیکسل‌های همسایه
            for (wy in -1..1) {
                for (wx in -1..1) {
                    window[idx++] = pixels[(y + wy) * width + (x + wx)]
                }
            }
            
 // مرتب‌سازی و انتخاب مقدار میانه
            window.sort()
            output[y * width + x] = window[windowSize * windowSize / 2]
        }
    }
    
 // کپی نتایج به آرایه اصلی
    for (i in pixels.indices) {
        pixels[i] = output[i]
    }
}

private fun enhanceEdges(pixels: IntArray, output: IntArray, width: Int, height: Int) {
    System.arraycopy(pixels, 0, output, 0, pixels.size)
    val sobelX = arrayOf(
        intArrayOf(-1, 0, 1),
        intArrayOf(-2, 0, 2),
        intArrayOf(-1, 0, 1)
    )
    
    val sobelY = arrayOf(
        intArrayOf(1, 2, 1),
        intArrayOf(0, 0, 0),
        intArrayOf(-1, -2, -1)
    )
    
    for (y in 1 until height - 1) {
        for (x in 1 until width - 1) {
            var sumX = 0
            var sumY = 0
            
            for (wy in -1..1) {
                for (wx in -1..1) {
                    val pixel = pixels[(y + wy) * width + (x + wx)]
                    val gray = if (pixel == 0xFFFFFFFF.toInt()) 255 else 0
                    
                    sumX += gray * sobelX[wy + 1][wx + 1]
                    sumY += gray * sobelY[wy + 1][wx + 1]
                }
            }
            
            val magnitude = minOf(255, sqrt((sumX * sumX + sumY * sumY).toDouble()).toInt())
            
 // تقویت لبه‌ها اگر مقدار بیش از آستانه باشد
            if (magnitude > 30) {
                output[y * width + x] = 0xFF000000.toInt()  // لبه‌ها سیاه می‌شوند
            }
        }
    }
    
 // ادغام لبه‌های تقویت شده با تصویر اصلی
    for (i in pixels.indices) {
 // اگر پیکسل در تصویر اصلی سیاه است یا در خروجی لبه تشخیص داده شده، آن را سیاه نگه دار
        if (pixels[i] == 0xFF000000.toInt() || output[i] == 0xFF000000.toInt()) {
            pixels[i] = 0xFF000000.toInt()
        }
    }
}
