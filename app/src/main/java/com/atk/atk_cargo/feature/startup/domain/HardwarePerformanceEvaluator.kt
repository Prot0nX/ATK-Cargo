package com.atk.atk_cargo.feature.startup.domain

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.hardware.display.DisplayManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.view.Display
import androidx.core.graphics.createBitmap
import android.graphics.Canvas
import com.atk.atk_cargo.api.UserPreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HardwarePerformanceEvaluator(
    private val context: Context,
    private val userPreferencesManager: UserPreferencesManager
) {
    companion object {
        private const val EVALUATION_VALIDITY_HOURS = 24 // ارزیابی مجدد هر 24 ساعت
    }

    suspend fun evaluatePerformance(): Int {
        val lastEvaluation = userPreferencesManager.getScoreTimestamp()
        val currentTime = System.currentTimeMillis()
        val validityDuration = EVALUATION_VALIDITY_HOURS * 60 * 60 * 1000L

        val currentDeviceSpecs = generateAdvancedDeviceSpecs()
        val cachedDeviceSpecs = userPreferencesManager.getDeviceSpecs()

        if (currentTime - lastEvaluation < validityDuration &&
            cachedDeviceSpecs == currentDeviceSpecs) {
            val cachedScore = userPreferencesManager.getHardwareScore()
            if (cachedScore != -1) {
                return cachedScore
            }
        }

        val performanceMetrics = withContext(Dispatchers.Default) {
            val metrics = mutableMapOf<String, Float>()

            metrics["cpu_benchmark"] = runCPUBenchmark()
            metrics["gpu_benchmark"] = runGPUBenchmark()
            metrics["memory_benchmark"] = runMemoryBenchmark()

            metrics["ram_performance"] = evaluateAdvancedRAM()
            metrics["cpu_performance"] = evaluateAdvancedCPU()
            metrics["gpu_performance"] = evaluateGPU()
            metrics["display_performance"] = evaluateDisplay()
            metrics["storage_performance"] = evaluateAdvancedStorage()
            metrics["thermal_performance"] = evaluateThermal()
            metrics["android_performance"] = evaluateAndroidVersion()

            metrics
        }

        val finalScore = calculateWeightedScore(performanceMetrics)
        userPreferencesManager.saveHardwareScore(finalScore, currentDeviceSpecs)

        return finalScore
    }

    private fun generateAdvancedDeviceSpecs(): String {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)

            val totalRAM = memoryInfo.totalMem / (1024 * 1024 * 1024)
            val coreCount = Runtime.getRuntime().availableProcessors()
            val androidVersion = Build.VERSION.SDK_INT
            val architecture = Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown"
            val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
            val refreshRate = displayManager.getDisplay(Display.DEFAULT_DISPLAY)?.refreshRate ?: 60f

            val statFs = StatFs(Environment.getDataDirectory().path)
            val totalStorage = statFs.totalBytes / (1024 * 1024 * 1024)

            "RAM:${totalRAM}GB|CPU:${coreCount}cores|Android:${androidVersion}|Arch:${architecture}|Refresh:${refreshRate}Hz|Storage:${totalStorage}GB"
        } catch (_: Exception) {
            "UNKNOWN_SPECS"
        }
    }

    private fun runCPUBenchmark(): Float {
        return try {
            val startTime = System.nanoTime()
            var result = 0.0

            repeat(100000) {
                result += kotlin.math.sin(it.toDouble()) * kotlin.math.cos(it.toDouble())
            }

            val duration = (System.nanoTime() - startTime) / 1_000_000f

            when {
                duration < 50f -> 100f
                duration < 100f -> 80f
                duration < 200f -> 60f
                duration < 400f -> 40f
                else -> 20f
            }
        } catch (_: Exception) {
            50f
        }
    }

    private fun runGPUBenchmark(): Float {
        return try {
            val startTime = System.nanoTime()
            val bitmap = createBitmap(100, 100)
            val canvas = Canvas(bitmap)

            repeat(1000) {
                canvas.drawColor(android.graphics.Color.rgb(it % 255, (it * 2) % 255, (it * 3) % 255))
            }

            val duration = (System.nanoTime() - startTime) / 1_000_000f

            when {
                duration < 30f -> 100f
                duration < 60f -> 80f
                duration < 120f -> 60f
                duration < 250f -> 40f
                else -> 20f
            }
        } catch (_: Exception) {
            50f
        }
    }

    private fun runMemoryBenchmark(): Float {
        return try {
            val startTime = System.nanoTime()

            val arrays = mutableListOf<IntArray>()
            repeat(100) {
                arrays.add(IntArray(1000) { it })
            }
            arrays.clear()

            System.gc()

            val duration = (System.nanoTime() - startTime) / 1_000_000f

            when {
                duration < 20f -> 100f
                duration < 40f -> 80f
                duration < 80f -> 60f
                duration < 160f -> 40f
                else -> 20f
            }
        } catch (_: Exception) {
            50f
        }
    }

    private fun evaluateAdvancedRAM(): Float {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)

            val totalRAM = memoryInfo.totalMem / (1024 * 1024 * 1024)
            val availableRAM = memoryInfo.availMem / (1024 * 1024 * 1024)
            val ramUsagePercent = 1f - (availableRAM.toFloat() / totalRAM.toFloat())

            when {
                totalRAM >= 12 -> 100f
                totalRAM >= 8 -> 85f
                totalRAM >= 6 -> 70f
                totalRAM >= 4 -> 55f
                totalRAM >= 3 -> 40f
                totalRAM >= 2 -> 25f
                else -> 10f
            } * (1f - ramUsagePercent * 0.3f)
        } catch (_: Exception) {
            50f
        }
    }

    private fun evaluateAdvancedCPU(): Float {
        return try {
            val coreCount = Runtime.getRuntime().availableProcessors()
            val architecture = Build.SUPPORTED_ABIS.firstOrNull() ?: ""

            val coreScore = when {
                coreCount >= 8 -> 100f
                coreCount >= 6 -> 85f
                coreCount >= 4 -> 70f
                coreCount >= 2 -> 50f
                else -> 25f
            }

            val archScore = when {
                architecture.contains("arm64-v8a") -> 100f
                architecture.contains("armeabi-v7a") -> 80f
                architecture.contains("x86_64") -> 70f
                architecture.contains("x86") -> 50f
                else -> 30f
            }

            (coreScore * 0.7f + archScore * 0.3f)
        } catch (_: Exception) {
            50f
        }
    }

    private fun evaluateGPU(): Float {
        return try {
            val packageManager = context.packageManager

            val hasOpenGLES3 = packageManager.hasSystemFeature("android.hardware.opengles.es_version_3_0")
            val hasOpenGLES31 = packageManager.hasSystemFeature("android.hardware.opengles.es_version_3_1")
            val hasOpenGLES32 = packageManager.hasSystemFeature("android.hardware.opengles.es_version_3_2")

            when {
                hasOpenGLES32 -> 100f
                hasOpenGLES31 -> 85f
                hasOpenGLES3 -> 70f
                else -> 40f
            }
        } catch (_: Exception) {
            50f
        }
    }

    private fun evaluateDisplay(): Float {
        return try {
            val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
            val display = displayManager.getDisplay(Display.DEFAULT_DISPLAY)
            val refreshRate = display?.refreshRate ?: 60f

            when {
                refreshRate >= 120f -> 100f
                refreshRate >= 90f -> 85f
                refreshRate >= 60f -> 70f
                else -> 40f
            }
        } catch (_: Exception) {
            50f
        }
    }

    private fun evaluateAdvancedStorage(): Float {
        return try {
            val statFs = StatFs(Environment.getDataDirectory().path)
            val availableBytes = statFs.availableBytes
            val availableGB = availableBytes / (1024 * 1024 * 1024)

            when {
                availableGB >= 64 -> 100f
                availableGB >= 32 -> 85f
                availableGB >= 16 -> 70f
                availableGB >= 8 -> 55f
                availableGB >= 4 -> 40f
                availableGB >= 2 -> 25f
                else -> 10f
            }
        } catch (_: Exception) {
            50f
        }
    }

    private fun evaluateThermal(): Float {
        return try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

            when {
                batteryLevel >= 80 -> 100f
                batteryLevel >= 60 -> 85f
                batteryLevel >= 40 -> 70f
                batteryLevel >= 20 -> 50f
                else -> 30f
            }
        } catch (_: Exception) {
            70f
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun evaluateAndroidVersion(): Float {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> 100f
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> 90f
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> 80f
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> 70f
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> 60f
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> 50f
            else -> 30f
        }
    }

    private fun calculateWeightedScore(metrics: Map<String, Float>): Int {
        val weights = mapOf(
            "cpu_benchmark" to 0.15f,
            "gpu_benchmark" to 0.20f,
            "memory_benchmark" to 0.15f,
            "ram_performance" to 0.15f,
            "cpu_performance" to 0.10f,
            "gpu_performance" to 0.10f,
            "display_performance" to 0.08f,
            "storage_performance" to 0.03f,
            "thermal_performance" to 0.02f,
            "android_performance" to 0.02f
        )

        var weightedSum = 0f
        var totalWeight = 0f

        metrics.forEach { (metric, value) ->
            val weight = weights[metric] ?: 0f
            weightedSum += value * weight
            totalWeight += weight
        }

        val finalScore = if (totalWeight > 0) (weightedSum / totalWeight).coerceIn(0f, 100f) else 50f

        return finalScore.toInt()
    }
}
