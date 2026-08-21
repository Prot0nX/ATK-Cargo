package com.atk.atk_cargo.api

import android.content.Context
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.security.MessageDigest

// تست‌های UpdateManager: تأیید allowlist دامنه‌ی دانلود، تأیید یکپارچگی SHA-256، و مقایسه‌ی نسخه (DEEP_CODE_AUDIT.md #۲۰).
// trustedBaseUrl به‌عنوان پارامتر پاس داده می‌شود تا این تست‌ها به کتابخانه‌ی نیتیو Secrets (که در JVM ساده در دسترس نیست) وابسته نباشند.
class UpdateManagerTest {
    private lateinit var updateManager: UpdateManager

    private val trustedBaseUrl = "https://atk-nk.ir/"

    @Before
    fun setUp() {
        // این دو تابع تحت تست به Context دسترسی ندارند؛ فقط برای ساخت شیء لازم است
        updateManager = UpdateManager(mockk<Context>(relaxed = true))
    }

    // ===== isTrustedDownloadUrl =====

    @Test
    fun `exact matching https host is trusted`() {
        assertTrue(updateManager.isTrustedDownloadUrl("https://atk-nk.ir/downloads/app.apk", trustedBaseUrl))
    }

    @Test
    fun `subdomain of trusted host is trusted`() {
        assertTrue(updateManager.isTrustedDownloadUrl("https://cdn.atk-nk.ir/downloads/app.apk", trustedBaseUrl))
    }

    @Test
    fun `different host is rejected`() {
        assertFalse(updateManager.isTrustedDownloadUrl("https://evil.com/downloads/app.apk", trustedBaseUrl))
    }

    @Test
    fun `host that merely contains trusted domain as a suffix trick is rejected`() {
        // "evil-atk-nk.ir" به‌اشتباه ممکن است با یک بررسی سطحی contains() قبول شود؛ endsWith(".$trustedHost") این را می‌بندد
        assertFalse(updateManager.isTrustedDownloadUrl("https://evil-atk-nk.ir/app.apk", trustedBaseUrl))
    }

    @Test
    fun `host with trusted domain as prefix trick is rejected`() {
        assertFalse(updateManager.isTrustedDownloadUrl("https://atk-nk.ir.evil.com/app.apk", trustedBaseUrl))
    }

    @Test
    fun `http scheme is rejected even for the trusted host`() {
        // cleartext هرگز مجاز نیست؛ تابع فقط https را می‌پذیرد
        assertFalse(updateManager.isTrustedDownloadUrl("http://atk-nk.ir/downloads/app.apk", trustedBaseUrl))
    }

    @Test
    fun `malformed url is rejected without throwing`() {
        assertFalse(updateManager.isTrustedDownloadUrl("not a valid url ///", trustedBaseUrl))
    }

    @Test
    fun `empty url is rejected without throwing`() {
        assertFalse(updateManager.isTrustedDownloadUrl("", trustedBaseUrl))
    }

    // ===== verifyFileSha256 =====

    @Test
    fun `verifyFileSha256 returns true when hash matches`() {
        val file = writeTempFile("hello world")
        val expectedHash = sha256Hex("hello world")

        assertTrue(updateManager.verifyFileSha256(file, expectedHash))
    }

    @Test
    fun `verifyFileSha256 returns false when hash does not match`() {
        val file = writeTempFile("hello world")
        val wrongHash = sha256Hex("something else entirely")

        assertFalse(updateManager.verifyFileSha256(file, wrongHash))
    }

    @Test
    fun `verifyFileSha256 comparison is case-insensitive`() {
        val file = writeTempFile("hello world")
        val expectedHash = sha256Hex("hello world").uppercase()

        assertTrue(updateManager.verifyFileSha256(file, expectedHash))
    }

    @Test
    fun `verifyFileSha256 trims surrounding whitespace from expected hash`() {
        val file = writeTempFile("hello world")
        val expectedHash = "  " + sha256Hex("hello world") + "  "

        assertTrue(updateManager.verifyFileSha256(file, expectedHash))
    }

    @Test
    fun `verifyFileSha256 returns false for missing file without throwing`() {
        val missingFile = File("this/path/does/not/exist.apk")

        assertFalse(updateManager.verifyFileSha256(missingFile, sha256Hex("anything")))
    }

    // ===== compareVersions =====

    @Test
    fun `compareVersions returns zero for equal versions`() {
        assertEquals(0, updateManager.compareVersions("4.0.1", "4.0.1").signOf())
    }

    @Test
    fun `compareVersions returns positive when first version is newer`() {
        assertTrue(updateManager.compareVersions("4.1.0", "4.0.1") > 0)
    }

    @Test
    fun `compareVersions returns negative when first version is older`() {
        assertTrue(updateManager.compareVersions("3.9.9", "4.0.1") < 0)
    }

    @Test
    fun `compareVersions treats missing trailing segments as zero`() {
        // "1.2" باید معادل "1.2.0" در نظر گرفته شود
        assertEquals(0, updateManager.compareVersions("1.2", "1.2.0").signOf())
    }

    @Test
    fun `compareVersions ignores non-numeric characters`() {
        assertEquals(0, updateManager.compareVersions("v4.0.1", "4.0.1-debug").signOf())
    }

    private fun Int.signOf(): Int = when {
        this > 0 -> 1
        this < 0 -> -1
        else -> 0
    }

    private fun writeTempFile(content: String): File {
        val file = File.createTempFile("update_manager_test", ".tmp")
        file.deleteOnExit()
        file.writeText(content)
        return file
    }

    private fun sha256Hex(content: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(content.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
