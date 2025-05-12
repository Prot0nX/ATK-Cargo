package com.atk.atk_cargo.utils

import android.util.Log
import com.atk.atk_cargo.BuildConfig

/**
 * کلاس کمکی برای لاگ کردن که در حالت انتشار لاگ‌ها را نمایش نمی‌دهد
 */
object LogUtils {
    private const val TAG = "ATK_Cargo"
    
    /**
     * لاگ کردن پیام‌های دیباگ
     * @param message پیام لاگ
     * @param tag تگ لاگ (اختیاری)
     */
    fun d(message: String, tag: String = TAG) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message)
        }
    }
    
    /**
     * لاگ کردن پیام‌های خطا
     * @param message پیام خطا
     * @param throwable خطای رخ داده (اختیاری)
     * @param tag تگ لاگ (اختیاری)
     */
    fun e(message: String, throwable: Throwable? = null, tag: String = TAG) {
        if (BuildConfig.DEBUG) {
            if (throwable != null) {
                Log.e(tag, message, throwable)
            } else {
                Log.e(tag, message)
            }
        }
    }
    
    /**
     * لاگ کردن پیام‌های اطلاعاتی
     * @param message پیام اطلاعاتی
     * @param tag تگ لاگ (اختیاری)
     */
    fun i(message: String, tag: String = TAG) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, message)
        }
    }
    
    /**
     * لاگ کردن پیام‌های هشدار
     * @param message پیام هشدار
     * @param tag تگ لاگ (اختیاری)
     */
    fun w(message: String, tag: String = TAG) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, message)
        }
    }
}