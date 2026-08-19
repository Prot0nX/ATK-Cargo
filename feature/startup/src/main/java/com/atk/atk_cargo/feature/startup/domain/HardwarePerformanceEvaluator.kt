package com.atk.atk_cargo.feature.startup.domain

import android.app.ActivityManager
import android.content.Context
import android.hardware.display.DisplayManager
import android.view.Display

/**
 * تخمین ارزان و همزمان (بدون I/O، بدون بنچمارک) از توان دستگاه.
 * فقط برای تصمیم دوتایی «انیمیشن ساده یا کامل» در AnimationManager استفاده می‌شود،
 * پس دقت بالا لازم نیست — سه سیگنال ارزان سیستم‌عامل کافی است.
 */
class HardwarePerformanceEvaluator(private val context: Context) {

    fun evaluatePerformance(): Int {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val coreCount = Runtime.getRuntime().availableProcessors()
            val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
            val refreshRate = displayManager.getDisplay(Display.DEFAULT_DISPLAY)?.refreshRate ?: 60f

            val coreScore = when {
                coreCount >= 8 -> 100
                coreCount >= 6 -> 80
                coreCount >= 4 -> 60
                coreCount >= 2 -> 40
                else -> 20
            }

            val refreshScore = when {
                refreshRate >= 120f -> 100
                refreshRate >= 90f -> 85
                refreshRate >= 60f -> 65
                else -> 40
            }

            val lowRamPenalty = if (activityManager.isLowRamDevice) 30 else 0

            (coreScore * 0.6f + refreshScore * 0.4f - lowRamPenalty).toInt().coerceIn(0, 100)
        } catch (_: Exception) {
            50
        }
    }
}
