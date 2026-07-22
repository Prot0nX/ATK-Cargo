package com.atk.atk_cargo.security

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Debug
import android.util.Log
import androidx.core.content.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.net.ssl.SSLException
import javax.net.ssl.SSLHandshakeException
import kotlin.time.Duration.Companion.milliseconds

import com.atk.atk_cargo.api.Secrets

enum class SecurityErrorType {
    TAMPERED,              // دستکاری شده
    LICENSE_NOT_FOUND,     // لایسنس پیدا نشد
    LICENSE_INACTIVE,      // لایسنس غیرفعال است
    NETWORK_ERROR,         // خطای شبکه
    UNKNOWN_ERROR,         // خطای نامشخص
}

class SecurityVerifier(private val context: Context) {
    companion object {
        private const val BUFFER_DURATION = 15000
        private const val CONNECTION_ATTEMPTS = 2
        
        private val EXPECTED_SIGNATURE_HASH: String by lazy { Secrets.getExpectedSignatureHash() }
        private val LICENSE_STATUS_PREF_KEY: String by lazy { Secrets.getLicenseStatusPrefKey() }
        private val SIGNATURE_CHECK_URL: String by lazy { Secrets.getSignatureCheckUrl() }
        private val LICENSE_CHECK_URL: String by lazy { Secrets.getLicenseCheckUrl() }
        private val LICENSE_INFO_URL: String by lazy { Secrets.getLicenseInfoUrl() }
        private val LICENSE_KEY: String by lazy { Secrets.getLicenseKey() }
    }

    private val securityPrefs = context.getSharedPreferences("x1y2z3", Context.MODE_PRIVATE)
    private val randomGenerator = SecureRandom()
    private val cryptoCipher = object : ThreadLocal<Cipher>() {
        override fun initialValue(): Cipher {
            return Cipher.getInstance("AES/CBC/PKCS5Padding")
        }
    }
    
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    init {
        managerScope.launch {
            performEnvironmentValidation()
        }
    }

    private fun encryptSignatureLocal(signatureData: String): String {
        return try {
            val secretKey = SecretKeySpec(LICENSE_STATUS_PREF_KEY.toByteArray(), "AES")
            val initVector = ByteArray(16)
            randomGenerator.nextBytes(initVector)
            val vectorSpec = IvParameterSpec(initVector)
            
            val cipher = cryptoCipher.get()!!
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, vectorSpec)
            val encryptedBytes = cipher.doFinal(signatureData.toByteArray())
            val combinedData = ByteArray(initVector.size + encryptedBytes.size)
            
            System.arraycopy(initVector, 0, combinedData, 0, initVector.size)
            System.arraycopy(encryptedBytes, 0, combinedData, initVector.size, encryptedBytes.size)
            
            combinedData.joinToString("") { "%02x".format(it) }
        } catch (_: Exception) { "" }
    }

    private fun decryptSignatureLocal(encryptedData: String): String {
        return try {
            val combinedData = encryptedData.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
            if (combinedData.size <= 16) return ""
            
            val initVector = ByteArray(16)
            System.arraycopy(combinedData, 0, initVector, 0, initVector.size)
            
            val secretKey = SecretKeySpec(LICENSE_STATUS_PREF_KEY.toByteArray(), "AES")
            val vectorSpec = IvParameterSpec(initVector)
            
            val cipher = cryptoCipher.get()!!
            cipher.init(Cipher.DECRYPT_MODE, secretKey, vectorSpec)
            val decryptedBytes = cipher.doFinal(combinedData, initVector.size, combinedData.size - initVector.size)
            
            String(decryptedBytes)
        } catch (_: Exception) { "" }
    }

    suspend fun verifySecurityStatus(): Pair<Boolean, SecurityErrorType?> = withContext(Dispatchers.IO) {
        repeat(CONNECTION_ATTEMPTS) { attemptNumber ->
            try {
                // بررسی امضای برنامه به صورت محلی
                val isSignatureValid = verifyLocalAppSignature()
                if (!isSignatureValid) {
                    updateLicenseStatusLocally(false)
                    return@withContext Pair(false, SecurityErrorType.TAMPERED)
                }

                // اجرای درخواست‌های شبکه به صورت موازی
                val signatureAuthDeferred = async { authenticateSignatureWithServer() }
                val licenseDeferred = async { validateLicenseWithServer() }

                val isSignatureAuthPassed = signatureAuthDeferred.await()
                if (!isSignatureAuthPassed) {
                    updateLicenseStatusLocally(false)
                    return@withContext Pair(false, SecurityErrorType.TAMPERED)
                }

                val (isLicenseActive, licenseError) = licenseDeferred.await()
                updateLicenseStatusLocally(isLicenseActive)
                return@withContext Pair(isLicenseActive, if (!isLicenseActive) licenseError else null)
            } catch (e: SSLHandshakeException) {
                Log.e("SecurityVerifier", "SSL Handshake failed (attempt ${attemptNumber + 1}): ${e.message}", e)
                if (attemptNumber == CONNECTION_ATTEMPTS - 1) {
                    return@withContext Pair(false, SecurityErrorType.NETWORK_ERROR)
                }
                delay((500L * (1 shl attemptNumber)).coerceAtMost(2000L).milliseconds)
            } catch (e: SSLException) {
                Log.e("SecurityVerifier", "SSL error (attempt ${attemptNumber + 1}): ${e.message}", e)
                if (attemptNumber == CONNECTION_ATTEMPTS - 1) {
                    return@withContext Pair(false, SecurityErrorType.NETWORK_ERROR)
                }
                delay((500L * (1 shl attemptNumber)).coerceAtMost(2000L).milliseconds)
            } catch (e: Exception) {
                Log.e("SecurityVerifier", "General security verification error (attempt ${attemptNumber + 1}): ${e.message}", e)
                if (attemptNumber == CONNECTION_ATTEMPTS - 1) {
                    return@withContext Pair(false, SecurityErrorType.NETWORK_ERROR)
                }
                delay((500L * (1 shl attemptNumber)).coerceAtMost(2000L).milliseconds)
            }
        }
        Pair(false, SecurityErrorType.UNKNOWN_ERROR)
    }

    private fun verifyLocalAppSignature(): Boolean {
        return try {
            val packageInfo = getApplicationPackage()
            val signatures = extractDigitalSignatures(packageInfo)

            if (signatures.isNotEmpty()) {
                val primarySignature = signatures[0]
                val signatureHash = calculateSignatureHash(primarySignature)
                val encryptedHash = encryptSignatureLocal(signatureHash)
                val decryptedHash = decryptSignatureLocal(encryptedHash)
                decryptedHash == EXPECTED_SIGNATURE_HASH
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

    private fun updateLicenseStatusLocally(isActive: Boolean) {
        securityPrefs.edit(commit = false) { putBoolean(LICENSE_STATUS_PREF_KEY, isActive) }
    }

    private suspend fun validateLicenseWithServer(): Pair<Boolean, SecurityErrorType?> = withContext(Dispatchers.IO) {
        var infoConnection: HttpURLConnection? = null
        var validationConnection: HttpURLConnection? = null
        try {
            val infoUrl = URL("$LICENSE_INFO_URL?licenseKey=$LICENSE_KEY")
            infoConnection = infoUrl.openConnection() as HttpURLConnection
            infoConnection.requestMethod = "GET"
            infoConnection.connectTimeout = BUFFER_DURATION
            infoConnection.readTimeout = BUFFER_DURATION

            val infoResponse = infoConnection.inputStream.bufferedReader().use { it.readText() }
            val infoJson = JSONObject(infoResponse)

            if (!infoJson.optBoolean("success", false)) {
                return@withContext Pair(false, SecurityErrorType.LICENSE_NOT_FOUND)
            }

            val validationUrl = URL(LICENSE_CHECK_URL)
            validationConnection = validationUrl.openConnection() as HttpURLConnection
            validationConnection.requestMethod = "POST"
            validationConnection.doOutput = true
            validationConnection.connectTimeout = BUFFER_DURATION
            validationConnection.readTimeout = BUFFER_DURATION
            validationConnection.setRequestProperty("Content-Type", "application/json")

            val validationRequest = JSONObject().apply {
                put("licenseKey", LICENSE_KEY)
                put("update_last_check", true)
            }

            validationConnection.outputStream.use { outputStream ->
                outputStream.write(validationRequest.toString().toByteArray())
                outputStream.flush()
            }

            val validationResponse = validationConnection.inputStream.bufferedReader().use { it.readText() }
            val validationJson = JSONObject(validationResponse)

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
        } catch (e: Exception) {
            Log.e("SecurityVerifier", "Error in server license validation: ${e.message}", e)
            Pair(false, SecurityErrorType.LICENSE_NOT_FOUND)
        } finally {
            infoConnection?.disconnect()
            validationConnection?.disconnect()
        }
    }
    
    private fun performEnvironmentValidation() {
        if (Debug.isDebuggerConnected()) {
            Log.w("SecurityVerifier", "Debugger detected!")
        }
        
        val suspiciousProcesses = listOf("frida", "xposed", "substrate")
        suspiciousProcesses.forEach { process ->
            if (isProcessRunning(process)) {
                Log.w("SecurityVerifier", "Suspicious framework detected: $process")
            }
        }
    }
    
    private fun isProcessRunning(processName: String): Boolean {
        return try {
            val procDir = File("/proc")
            if (!procDir.exists()) return false
            procDir.list()?.take(20)?.any { it.contains(processName, ignoreCase = true) } == true
        } catch (_: Exception) {
            false
        }
    }
}
