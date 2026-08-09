package com.atk.atk_cargo.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test

/**
 * تولید Baseline Profile برای بهبود زمان راه‌اندازی سرد اپلیکیشن.
 *
 * این تست کل مسیر بحرانی راه‌اندازی را پروفایل می‌کند: Application -> Splash ->
 * صفحه‌ی لاگین (تایپ + submit) -> HomeScreen. قبلاً فقط startActivityAndWait()
 * فراخوانی می‌شد که تنها اولین فریم را پوشش می‌داد.
 *
 * برای پروفایل کردن مسیر موفق تا HomeScreen، یک نام‌کاربری/رمز معتبر باید از طریق
 * آرگومان instrumentation فراهم شود (چون این ماژول دسترسی به سرور واقعی ندارد):
 *   -Pandroid.testInstrumentationRunnerArguments.baselineProfileUsername=...
 *   -Pandroid.testInstrumentationRunnerArguments.baselineProfilePassword=...
 * در نبود این آرگومان‌ها، تست فقط تا صفحه‌ی لاگین (تایپ و submit) پیش می‌رود؛
 * صفحه‌ی خطای اعتبارسنجی هم بخشی از مسیر بحرانی است و همچنان پروفایل می‌شود.
 */
class BaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() = baselineProfileRule.collect(
        packageName = "com.atk.atk_cargo"
    ) {
        val args = InstrumentationRegistry.getArguments()
        val username = args.getString("baselineProfileUsername")
        val password = args.getString("baselineProfilePassword")

        pressHome()
        startActivityAndWait()

        // منتظر عبور از Splash/ServerSyncing/SecurityBlock و رسیدن به صفحه‌ی لاگین می‌مانیم.
        // بین click() و text=... حتماً باید object را دوباره fetch کرد چون کلیک باعث
        // recomposition می‌شود و reference قدیمی StaleObjectException می‌دهد.
        device.wait(Until.findObject(By.desc("فیلد نام کاربری")), 15_000)?.click() ?: return@collect
        device.waitForIdle()
        device.wait(Until.findObject(By.desc("فیلد نام کاربری")), 3_000)
            ?.text = username ?: "baseline_profile_user"
        device.waitForIdle()

        device.wait(Until.findObject(By.desc("فیلد رمز عبور عددی")), 5_000)?.click()
        device.waitForIdle()
        device.wait(Until.findObject(By.desc("فیلد رمز عبور عددی")), 3_000)
            ?.text = password ?: "000000"
        device.waitForIdle()

        device.wait(Until.findObject(By.desc("دکمه ورود به سامانه")), 5_000)?.click()

        // منتظر می‌مانیم تا یا وارد HomeScreen شویم (دکمه‌ی ورود دیگر وجود ندارد) یا
        // خطای اعتبارسنجی نمایش داده شود — هر دو بخشی از مسیر بحرانی login هستند
        device.wait(Until.gone(By.desc("دکمه ورود به سامانه")), 12_000)
        device.waitForIdle(3_000)

        // اگر لاگین موفق بود، دیالوگ سیستمی مجوز POST_NOTIFICATIONS ممکن است ظاهر شود؛
        // برای رسیدن واقعی به HomeScreen (نه ماندن پشت دیالوگ سیستمی) باید رد شود
        device.findObject(By.textContains("Allow"))?.click()
        device.waitForIdle(2_000)
    }
}
