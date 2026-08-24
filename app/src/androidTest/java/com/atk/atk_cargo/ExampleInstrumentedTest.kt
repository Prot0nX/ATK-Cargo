package com.atk.atk_cargo

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

// تست instrumented که روی دستگاه اندروید اجرا میشود.
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
 // دریافت بافت (Context) برنامه تحت تست.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.atk.atk_cargo", appContext.packageName)
    }
}