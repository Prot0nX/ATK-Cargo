package com.atk.atk_cargo.security

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Debug
import android.util.Log
import androidx.core.content.edit
import com.atk.atk_cargo.api.HttpStack
import com.atk.atk_cargo.api.Secrets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLException
import javax.net.ssl.SSLHandshakeException
import kotlin.time.Duration.Companion.milliseconds

enum class SecurityErrorType {
    TAMPERED,                  // امضای دیجیتال معتبر نیست (نصب از منبع غیررسمی)
    ENVIRONMENT_COMPROMISED,   // دیباگر/Frida/Xposed روی دستگاه فعال است
    LICENSE_NOT_FOUND,         // لایسنس پیدا نشد
    LICENSE_INACTIVE,          // لایسنس غیرفعال است
    NETWORK_ERROR,             // خطای شبکه
    UNKNOWN_ERROR,             // خطای نامشخص
}

class SecurityVerifier(private val context: Context) {
    companion object {
        private const val BUFFER_DURATION = 8000
        private const val CONNECTION_ATTEMPTS = 2

        private const val OFFLINE_GRACE_PERIOD_MS = 3 * 24 * 60 * 60 * 1000L // ۳ روز
        private const val LAST_SUCCESS_TIMESTAMP_KEY = "last_verified_success_timestamp"

        private val EXPECTED_SIGNATURE_HASH: String by lazy { Secrets.getExpectedSignatureHash() }
        private val SIGNATURE_CHECK_URL: String by lazy { Secrets.getSignatureCheckUrl() }
        private val LICENSE_CHECK_URL: String by lazy { Secrets.getLicenseCheckUrl() }
        private val LICENSE_INFO_URL: String by lazy { Secrets.getLicenseInfoUrl() }
        private val LICENSE_KEY: String by lazy { Secrets.getLicenseKey() }
    }

    private val securityPrefs = context.getSharedPreferences("x1y2z3", Context.MODE_PRIVATE)

 // مشتق از HttpStack.shared (connection pool مشترک با API/دانلود آپدیت، ) جایگزین HttpURLConnection خام قبلی، با همان BUFFER_DURATION برای connect و read.
    private val securityHttpClient: OkHttpClient by lazy {
        HttpStack.shared.newBuilder()
            .connectTimeout(BUFFER_DURATION.toLong(), TimeUnit.MILLISECONDS)
            .readTimeout(BUFFER_DURATION.toLong(), TimeUnit.MILLISECONDS)
            .build()
    }

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
                    return@withContext Pair(false, SecurityErrorType.ENVIRONMENT_COMPROMISED)
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
        try {
            val packageInfo = getApplicationPackage()
            val signatures = extractDigitalSignatures(packageInfo)
            if (signatures.isEmpty()) return@withContext false

            val signatureHash = calculateSignatureHash(signatures[0])
            val requestPayload = JSONObject().apply {
                put("app_signature", signatureHash)
                put("app_package", context.packageName)
                put("app_version", context.packageManager.getPackageInfo(context.packageName, 0).versionName)
            }

            val request = Request.Builder()
                .url(SIGNATURE_CHECK_URL)
                .post(requestPayload.toString().toRequestBody("application/json; charset=UTF-8".toMediaType()))
                .header("User-Agent", "ATK-Cargo-App")
                .build()

            securityHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val responseJson = JSONObject(response.body?.string().orEmpty())
                    responseJson.optBoolean("is_valid", false)
                } else {
                    false
                }
            }
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            Log.e("SecurityVerifier", "Error in server signature authentication: ${e.message}", e)
            false
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
            throw e
        } catch (e: Exception) {
            Log.e("SecurityVerifier", "Error in server license validation: ${e.message}", e)
            Pair(false, SecurityErrorType.UNKNOWN_ERROR)
        }
    }

    private fun fetchLicenseInfo(): JSONObject? {
        return try {
            val request = Request.Builder()
                .url(LICENSE_INFO_URL)
                .header("X-License-Key", LICENSE_KEY)
                .build()

            securityHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                JSONObject(response.body?.string().orEmpty())
            }
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            Log.e("SecurityVerifier", "Error fetching license info: ${e.message}", e)
            null
        }
    }

    private fun fetchLicenseValidation(): JSONObject? {
        return try {
            val validationRequest = JSONObject().apply {
                put("licenseKey", LICENSE_KEY)
                put("update_last_check", true)
            }

            val request = Request.Builder()
                .url(LICENSE_CHECK_URL)
                .post(validationRequest.toString().toRequestBody("application/json".toMediaType()))
                .build()

            securityHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                JSONObject(response.body?.string().orEmpty())
            }
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            Log.e("SecurityVerifier", "Error fetching license validation: ${e.message}", e)
            null
        }
    }

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