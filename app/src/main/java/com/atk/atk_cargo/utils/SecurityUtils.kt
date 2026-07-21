package com.atk.atk_cargo.utils

import java.security.MessageDigest

// ===== SECURITY =====
// هشینگ SHA-256 بدون Salt برای سازگاری با پروتکل سرور فعلی.
// توجه: در صورت خطا، Exception پرتاب می‌شود — هرگز پسورد خام برگردانده نمی‌شود.
// این تابع باید روی Dispatchers.IO فراخوانی شود.
@Throws(SecurityException::class)
fun hashPassword(password: String): String {
    return try {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        bytes.fold(StringBuilder()) { sb, it -> sb.append("%02x".format(it)) }.toString()
    } catch (e: Exception) {
        // هرگز پسورد خام را برنگردان — برای امنیت Exception پرتاب می‌شود
        throw SecurityException("خطای امنیتی: عملیات هشینگ رمز عبور با شکست مواجه شد.", e)
    }
}
