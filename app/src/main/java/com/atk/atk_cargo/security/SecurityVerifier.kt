package com.atk.atk_cargo.security

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Debug
import android.util.Log
import androidx.core.content.edit
import com.atk.atk_cargo.api.Secrets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import javax.net.ssl.SSLException
import javax.net.ssl.SSLHandshakeException
import kotlin.time.Duration.Companion.milliseconds

enum class SecurityErrorType {
    TAMPERED,              // دستکاری شده
    LICENSE_NOT_FOUND,     // لایسنس پیدا نشد
    LICENSE_INACTIVE,      // لایسنس غیرفعال است
    NETWORK_ERROR,         // خطای شبکه
    UNKNOWN_ERROR,         // خطای نامشخص
}

class SecurityVerifier(private val context: Context) {
    companion object {
        private const val BUFFER_DURATION = 8000
        private const val CONNECTION_ATTEMPTS = 2

        // اگر آخرین بررسی امنیتی موفق در این بازه‌ی زمانی رخ داده باشد، خطای شبکه
        // (نه TAMPERED/LICENSE_INACTIVE) به‌جای قفل کامل، اجازه‌ی ورود موقت می‌دهد
        private const val OFFLINE_GRACE_PERIOD_MS = 3 * 24 * 60 * 60 * 1000L // ۳ روز
        private const val LAST_SUCCESS_TIMESTAMP_KEY = "last_verified_success_timestamp"

        private val EXPECTED_SIGNATURE_HASH: String by lazy { Secrets.getExpectedSignatureHash() }
        private val SIGNATURE_CHECK_URL: String by lazy { Secrets.getSignatureCheckUrl() }
        private val LICENSE_CHECK_URL: String by lazy { Secrets.getLicenseCheckUrl() }
        private val LICENSE_INFO_URL: String by lazy { Secrets.getLicenseInfoUrl() }
        private val LICENSE_KEY: String by lazy { Secrets.getLicenseKey() }
    }

    private val securityPrefs = context.getSharedPreferences("x1y2z3", Context.MODE_PRIVATE)

    suspend fun verifySecurityStatus(): Pair<Boolean, SecurityErrorType?> = withContext(Dispatchers.IO) {
        repeat(CONNECTION_ATTEMPTS) { attemptNumber ->
            try {
                // بررسی امضای برنامه به صورت محلی
                val isSignatureValid = verifyLocalAppSignature()
                if (!isSignatureValid) {
                    return@withContext Pair(false, SecurityErrorType.TAMPERED)
                }

                // بررسی محیط اجرا (دیباگر/Frida/Xposed) — با اقدام واقعی، نه فقط لاگ
                if (isEnvironmentCompromised()) {
                    return@withContext Pair(false, SecurityErrorType.TAMPERED)
                }

                // اجرای درخواست‌های شبکه به صورت موازی
                val signatureAuthDeferred = async { authenticateSignatureWithServer() }
                val licenseDeferred = async { validateLicenseWithServer() }

                val isSignatureAuthPassed = signatureAuthDeferred.await()
                if (!isSignatureAuthPassed) {
                    return@withContext Pair(false, SecurityErrorType.TAMPERED)
                }

                val (isLicenseActive, licenseError) = licenseDeferred.await()
                if (isLicenseActive) {
                    recordSuccessfulVerification()
                }
                return@withContext Pair(isLicenseActive, if (!isLicenseActive) licenseError else null)
            } catch (e: SSLHandshakeException) {
                Log.e("SecurityVerifier", "SSL Handshake failed (attempt ${attemptNumber + 1}): ${e.message}", e)
                if (attemptNumber == CONNECTION_ATTEMPTS - 1) {
                    return@withContext resolveNetworkFailure(SecurityErrorType.NETWORK_ERROR)
                }
                delay((500L * (1 shl attemptNumber)).coerceAtMost(2000L).milliseconds)
            } catch (e: SSLException) {
                Log.e("SecurityVerifier", "SSL error (attempt ${attemptNumber + 1}): ${e.message}", e)
                if (attemptNumber == CONNECTION_ATTEMPTS - 1) {
                    return@withContext resolveNetworkFailure(SecurityErrorType.NETWORK_ERROR)
                }
                delay((500L * (1 shl attemptNumber)).coerceAtMost(2000L).milliseconds)
            } catch (e: IOException) {
                Log.e("SecurityVerifier", "Network I/O error (attempt ${attemptNumber + 1}): ${e.message}", e)
                if (attemptNumber == CONNECTION_ATTEMPTS - 1) {
                    return@withContext resolveNetworkFailure(SecurityErrorType.NETWORK_ERROR)
                }
                delay((500L * (1 shl attemptNumber)).coerceAtMost(2000L).milliseconds)
            } catch (e: Exception) {
                Log.e("SecurityVerifier", "General security verification error (attempt ${attemptNumber + 1}): ${e.message}", e)
                if (attemptNumber == CONNECTION_ATTEMPTS - 1) {
                    return@withContext resolveNetworkFailure(SecurityErrorType.UNKNOWN_ERROR)
                }
                delay((500L * (1 shl attemptNumber)).coerceAtMost(2000L).milliseconds)
            }
        }
        resolveNetworkFailure(SecurityErrorType.UNKNOWN_ERROR)
    }

    /**
     * تصمیم نهایی وقتی سرور اصلاً قابل دسترس نبوده (نه اینکه صراحتاً پاسخ نامعتبر داده باشد):
     * اگر آخرین تأیید موفق کمتر از OFFLINE_GRACE_PERIOD_MS پیش بوده، اجازه‌ی ورود موقت داده می‌شود
     * تا کاربر در محیط‌های بدون اینترنت (انبار/بندر) کاملاً از کار نیفتد.
     */
    private fun resolveNetworkFailure(errorType: SecurityErrorType): Pair<Boolean, SecurityErrorType?> {
        val lastSuccess = securityPrefs.getLong(LAST_SUCCESS_TIMESTAMP_KEY, 0L)
        val withinGracePeriod = lastSuccess > 0L &&
                (System.currentTimeMillis() - lastSuccess) < OFFLINE_GRACE_PERIOD_MS
        return if (withinGracePeriod) {
            Log.w("SecurityVerifier", "Network unreachable — allowing entry via offline grace period")
            Pair(true, null)
        } else {
            Pair(false, errorType)
        }
    }

    private fun recordSuccessfulVerification() {
        securityPrefs.edit(commit = false) {
            putLong(LAST_SUCCESS_TIMESTAMP_KEY, System.currentTimeMillis())
        }
    }

    private fun verifyLocalAppSignature(): Boolean {
        return try {
            val packageInfo = getApplicationPackage()
            val signatures = extractDigitalSignatures(packageInfo)

            if (signatures.isNotEmpty()) {
                val primarySignature = signatures[0]
                val signatureHash = calculateSignatureHash(primarySignature)
                signatureHash == EXPECTED_SIGNATURE_HASH
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    private suspend fun authenticateSignatureWithServer(): Boolean = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val packageInfo = getApplicationPackage()
            val signatures = extractDigitalSignatures(packageInfo)
            if (signatures.isEmpty()) return@withContext false

            val signatureHash = calculateSignatureHash(signatures[0])
            val streamingUrl = URL(SIGNATURE_CHECK_URL)
            connection = streamingUrl.openConnection() as HttpURLConnection

            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.connectTimeout = BUFFER_DURATION
            connection.readTimeout = BUFFER_DURATION
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            connection.setRequestProperty("User-Agent", "ATK-Cargo-App")

            val requestPayload = JSONObject().apply {
                put("app_signature", signatureHash)
                put("app_package", context.packageName)
                put("app_version", context.packageManager.getPackageInfo(context.packageName, 0).versionName)
            }

            connection.outputStream.use { outputStream ->
                outputStream.write(requestPayload.toString().toByteArray(Charsets.UTF_8))
                outputStream.flush()
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val responseData = connection.inputStream.bufferedReader().use { it.readText() }
                val responseJson = JSONObject(responseData)
                responseJson.optBoolean("is_valid", false)
            } else {
                false
            }
        } catch (e: IOException) {
            // خطای شبکه (نه پاسخ صریح نامعتبر) — باید به بیرون منتقل شود تا verifySecurityStatus
            // بتواند بین «سرور صراحتاً رد کرد» و «اصلاً قابل دسترس نبود» تفاوت بگذارد
            throw e
        } catch (e: Exception) {
            Log.e("SecurityVerifier", "Error in server signature authentication: ${e.message}", e)
            false
        } finally {
            connection?.disconnect()
        }
    }

    private fun getApplicationPackage(): PackageInfo {
        return context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_SIGNING_CERTIFICATES
        )
    }

    private fun extractDigitalSignatures(packageData: PackageInfo): Array<Signature> {
        return packageData.signingInfo?.apkContentsSigners ?: emptyArray()
    }

    private fun calculateSignatureHash(signature: Signature): String {
        return try {
            val hashGenerator = MessageDigest.getInstance("SHA-256")
            val hashBytes = hashGenerator.digest(signature.toByteArray())
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (_: Exception) {
            ""
        }
    }

    /**
     * license_info و license_check قبلاً به‌صورت سریال فراخوانی می‌شدند (تا ۳۰s+ در بدترین حالت)
     * ولی هیچ وابستگی داده‌ای بین payload هایشان وجود ندارد؛ تنها وابستگی منطقی این است که
     * اگر لایسنس اصلاً پیدا نشود، نتیجه‌ی validation بی‌اثر می‌شود. با فراخوانی موازی این دو،
     * زمان کل به بزرگترین یکی از دو درخواست کاهش می‌یابد.
     */
    private suspend fun validateLicenseWithServer(): Pair<Boolean, SecurityErrorType?> = coroutineScope {
        try {
            val infoDeferred = async(Dispatchers.IO) { fetchLicenseInfo() }
            val validationDeferred = async(Dispatchers.IO) { fetchLicenseValidation() }

            val infoJson = infoDeferred.await()
            if (infoJson == null || !infoJson.optBoolean("success", false)) {
                return@coroutineScope Pair(false, SecurityErrorType.LICENSE_NOT_FOUND)
            }

            val validationJson = validationDeferred.await()
                ?: return@coroutineScope Pair(false, SecurityErrorType.LICENSE_NOT_FOUND)

            validationJson.optJSONObject("license")?.let { licenseData ->
                val lastActivity = licenseData.optString("lastCheck")
                if (lastActivity.isNotEmpty()) {
                    securityPrefs.edit(commit = false) {
                        putString("last_check", lastActivity)
                    }
                }
            }

            if (validationJson.optBoolean("success", false)) {
                Pair(true, null)
            } else {
                Pair(false, SecurityErrorType.LICENSE_INACTIVE)
            }
        } catch (e: IOException) {
            // خطای شبکه — نباید به‌عنوان LICENSE_NOT_FOUND برچسب بخورد؛ باید به بیرون
            // منتقل شود تا verifySecurityStatus بتواند grace period آفلاین را اعمال کند
            throw e
        } catch (e: Exception) {
            Log.e("SecurityVerifier", "Error in server license validation: ${e.message}", e)
            Pair(false, SecurityErrorType.LICENSE_NOT_FOUND)
        }
    }

    private fun fetchLicenseInfo(): JSONObject? {
        var connection: HttpURLConnection? = null
        return try {
            val infoUrl = URL("$LICENSE_INFO_URL?licenseKey=$LICENSE_KEY")
            connection = infoUrl.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = BUFFER_DURATION
            connection.readTimeout = BUFFER_DURATION

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            JSONObject(response)
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            Log.e("SecurityVerifier", "Error fetching license info: ${e.message}", e)
            null
        } finally {
            connection?.disconnect()
        }
    }

    private fun fetchLicenseValidation(): JSONObject? {
        var connection: HttpURLConnection? = null
        return try {
            val validationUrl = URL(LICENSE_CHECK_URL)
            connection = validationUrl.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.connectTimeout = BUFFER_DURATION
            connection.readTimeout = BUFFER_DURATION
            connection.setRequestProperty("Content-Type", "application/json")

            val validationRequest = JSONObject().apply {
                put("licenseKey", LICENSE_KEY)
                put("update_last_check", true)
            }

            connection.outputStream.use { outputStream ->
                outputStream.write(validationRequest.toString().toByteArray())
                outputStream.flush()
            }

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            JSONObject(response)
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            Log.e("SecurityVerifier", "Error fetching license validation: ${e.message}", e)
            null
        } finally {
            connection?.disconnect()
        }
    }
    
    /**
     * برخلاف پیاده‌سازی قبلی (که فقط اسامی PID داخل /proc را می‌گشت — رشته‌ای مثل
     * "frida" هرگز در نام یک PID عددی ظاهر نمی‌شود، پس همیشه false بود)، این نسخه
     * دو سیگنال واقعی را بررسی می‌کند:
     *   ۱. کتابخانه‌های تزریق‌شده در نگاشت حافظه‌ی پروسه‌ی جاری (/proc/self/maps)
     *   ۲. باز بودن پورت پیش‌فرض frida-server (۲۷۰۴۲) روی localhost
     * و نتیجه واقعاً مسیر امنیتی را مسدود می‌کند، نه فقط لاگ.
     */
    private fun isEnvironmentCompromised(): Boolean {
        if (Debug.isDebuggerConnected()) {
            Log.w("SecurityVerifier", "Debugger detected")
            return true
        }
        if (hasInjectedLibraries()) {
            Log.w("SecurityVerifier", "Injected library detected in process maps")
            return true
        }
        if (isFridaServerPortOpen()) {
            Log.w("SecurityVerifier", "Frida server port is open")
            return true
        }
        return false
    }

    private fun hasInjectedLibraries(): Boolean {
        val suspiciousMarkers = listOf("frida", "xposed", "substrate", "gum-js-loop", "linjector")
        return try {
            File("/proc/self/maps").useLines { lines ->
                lines.any { line -> suspiciousMarkers.any { line.contains(it, ignoreCase = true) } }
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun isFridaServerPortOpen(): Boolean {
        return try {
            java.net.Socket().use { socket ->
                socket.connect(java.net.InetSocketAddress("127.0.0.1", 27042), 200)
                true
            }
        } catch (_: Exception) {
            false
        }
    }
}
