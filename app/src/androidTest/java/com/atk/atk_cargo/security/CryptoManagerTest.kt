package com.atk.atk_cargo.security

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

// تست instrumented (نه unit test) چون AndroidKeyStore فقط روی دستگاه/امولاتور واقعی در دسترس است (DEEP_CODE_AUDIT.md #۲۰)
@RunWith(AndroidJUnit4::class)
class CryptoManagerTest {

    @Test
    fun decryptOfEncryptedValueReturnsOriginalString() {
        val cryptoManager = CryptoManager()
        val original = "session-token-1234567890"

        val encrypted = cryptoManager.encrypt(original)
        val decrypted = cryptoManager.decrypt(encrypted)

        assertEquals(original, decrypted)
    }

    @Test
    fun encryptOutputDiffersFromInput() {
        val cryptoManager = CryptoManager()
        val original = "plain-text-value"

        val encrypted = cryptoManager.encrypt(original)

        assertNotEquals(original, encrypted)
    }

    @Test
    fun encryptIsRandomizedAcrossCalls() {
        // setRandomizedEncryptionRequired(true) یعنی IV هر بار جدید تولید می‌شود؛ همان متن دو خروجی متفاوت باید بدهد
        val cryptoManager = CryptoManager()
        val original = "same-input-both-times"

        val first = cryptoManager.encrypt(original)
        val second = cryptoManager.encrypt(original)

        assertNotEquals(first, second)
        // ولی هر دو باید به همان مقدار اصلی رمزگشایی شوند
        assertEquals(original, cryptoManager.decrypt(first))
        assertEquals(original, cryptoManager.decrypt(second))
    }

    @Test
    fun encryptOfEmptyStringReturnsEmptyString() {
        val cryptoManager = CryptoManager()

        assertEquals("", cryptoManager.encrypt(""))
    }

    @Test
    fun decryptOfEmptyStringReturnsEmptyString() {
        val cryptoManager = CryptoManager()

        assertEquals("", cryptoManager.decrypt(""))
    }

    @Test
    fun decryptOfGarbageInputReturnsEmptyStringWithoutThrowing() {
        // ورودی نامعتبر (نه Base64 قابل رمزگشایی) نباید کرش کند؛ باید بی‌صدا رشته‌ی خالی بدهد
        val cryptoManager = CryptoManager()

        val result = cryptoManager.decrypt("this-is-not-valid-base64-ciphertext!!!")

        assertEquals("", result)
    }

    @Test
    fun decryptOfTamperedCiphertextReturnsEmptyStringWithoutThrowing() {
        // تغییر یک بایت در ciphertext معتبر باید تگ احراز هویت GCM را بشکند، نه اینکه متن نادرست رمزگشایی‌شده برگرداند
        val cryptoManager = CryptoManager()
        val encrypted = cryptoManager.encrypt("some sensitive value")

        val tampered = StringBuilder(encrypted)
        val tamperIndex = tampered.length / 2
        tampered.setCharAt(tamperIndex, if (tampered[tamperIndex] == 'A') 'B' else 'A')

        val result = cryptoManager.decrypt(tampered.toString())

        assertEquals("", result)
    }

    @Test
    fun multipleInstancesShareTheSameKeystoreKeyAndCanDecryptEachOthersOutput() {
        // کلید در AndroidKeyStore ذخیره می‌شود نه در حافظه‌ی نمونه؛ دو نمونه‌ی جدا باید بتوانند خروجی هم را رمزگشایی کنند
        val first = CryptoManager()
        val second = CryptoManager()
        val original = "cross-instance-value"

        val encrypted = first.encrypt(original)
        val decrypted = second.decrypt(encrypted)

        assertEquals(original, decrypted)
    }

    @Test
    fun encryptedOutputIsValidBase64() {
        val cryptoManager = CryptoManager()
        val encrypted = cryptoManager.encrypt("check-base64-format")

        // نباید پرتاب کند؛ اگر فرمت نامعتبر باشد این خط با IllegalArgumentException شکست می‌خورد
        val decoded = android.util.Base64.decode(encrypted, android.util.Base64.DEFAULT)

        assertTrue(decoded.isNotEmpty())
    }
}
