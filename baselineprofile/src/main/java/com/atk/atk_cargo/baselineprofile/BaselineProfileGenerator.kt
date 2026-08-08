package com.atk.atk_cargo.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import org.junit.Rule
import org.junit.Test

/**
 * تولید Baseline Profile برای بهبود زمان راه‌اندازی سرد اپلیکیشن.
 * این تست مسیر بحرانی راه‌اندازی (Application -> Splash -> اولین فریم) را پروفایل می‌کند.
 */
class BaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() = baselineProfileRule.collect(
        packageName = "com.atk.atk_cargo"
    ) {
        pressHome()
        startActivityAndWait()
    }
}
