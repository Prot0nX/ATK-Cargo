package com.atk.atk_cargo.security

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.util.Base64
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class SignatureVerifier(private val context: Context) {
    companion object {
        private const val C = "0b0838afbe74afd97e781c9533015294ce86b70627017cccb807ca2af208ba21"
        private const val D = "e1f2g3h4i5j6k7l8m9n0o1p2q3r4s5t6"
        private const val E = "aHR0cHM6Ly9hdGstbmsuY2xpY2svQ2FyZ28vY2hlY2tfc2lnbmF0dXJlLnBocA=="
        private const val LICENSE_ENDPOINT = "https://atk-nk.click/Cargo/validate_license.php"
        private const val LICENSE_INFO_ENDPOINT = "https://atk-nk.click/Cargo/get_license_info.php"
        private const val KEY_LICENSE = "13F71ADCB4585F1BE632FFB919F06691"
        private const val TIMEOUT_MILLIS = 30000
        private const val MAX_RETRIES = 3
    }

    private val sharedPreferences = context.getSharedPreferences("x1y2z3", Context.MODE_PRIVATE)
    private val secureRandom = SecureRandom()
    private val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")

    private val g: String by lazy {
        try {
            String(Base64.decode(E, Base64.NO_WRAP), Charsets.UTF_8)
        } catch (h: Exception) {
            ""
        }
    }

    private fun encryptData(data: String): String {
        val key = SecretKeySpec(D.toByteArray(), "AES")
        val iv = ByteArray(16)
        secureRandom.nextBytes(iv)
        val ivSpec = IvParameterSpec(iv)
        
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec)
        val encrypted = cipher.doFinal(data.toByteArray())
        val combined = ByteArray(iv.size + encrypted.size)
        
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encrypted, 0, combined, iv.size, encrypted.size)
        
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    private fun decryptData(encryptedData: String): String {
        val combined = Base64.decode(encryptedData, Base64.NO_WRAP)
        val iv = ByteArray(16)
        System.arraycopy(combined, 0, iv, 0, iv.size)
        
        val key = SecretKeySpec(D.toByteArray(), "AES")
        val ivSpec = IvParameterSpec(iv)
        
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)
        val decrypted = cipher.doFinal(combined, iv.size, combined.size - iv.size)
        
        return String(decrypted)
    }

    suspend fun i(): Pair<Boolean, SecurityErrorType?> {
        var retryCount = 0
        while (retryCount < MAX_RETRIES) {
            try {
                val signatureValid = k()
                if (!signatureValid) {
                    o(false)
                    return Pair(false, SecurityErrorType.TAMPERED)
                }

                val serverValid = m()
                if (!serverValid) {
                    o(false)
                    return Pair(false, SecurityErrorType.TAMPERED)
                }

                val (licenseValid, licenseError) = checkLicenseValidity()
                o(licenseValid)
                return Pair(licenseValid, if (!licenseValid) licenseError else null)
            } catch (e: Exception) {
                retryCount++
                if (retryCount >= MAX_RETRIES) {
                    return Pair(false, SecurityErrorType.NETWORK_ERROR)
                }
                kotlinx.coroutines.delay((1000L * (1 shl retryCount)).coerceAtMost(5000L))
            }
        }
        return Pair(false, SecurityErrorType.UNKNOWN_ERROR)
    }

    private fun k(): Boolean {
        return try {
            val p = q()
            val r = s(p)

            if (r.isNotEmpty()) {
                val t = r[0]
                val u = v(t)
                val encryptedSignature = encryptData(u)
                val decryptedSignature = decryptData(encryptedSignature)
                decryptedSignature == C
            } else {
                false
            }
        } catch (w: Exception) {
            false
        }
    }

    private suspend fun m(): Boolean = withContext(Dispatchers.IO) {
        try {
            val x = v(s(q())[0])
            val y = URL(g)
            val z = y.openConnection() as HttpURLConnection

            z.requestMethod = "POST"
            z.doOutput = true
            z.connectTimeout = TIMEOUT_MILLIS
            z.readTimeout = TIMEOUT_MILLIS
            z.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            z.setRequestProperty("User-Agent", "ATK-Cargo-App")

            val aa = JSONObject().apply {
                put("app_signature", x)
                put("app_package", context.packageName)
                put("app_version", context.packageManager.getPackageInfo(context.packageName, 0).versionName)
            }

            z.outputStream.use { ab ->
                ab.write(aa.toString().toByteArray(Charsets.UTF_8))
                ab.flush()
            }

            val ac = z.responseCode
            if (ac == HttpURLConnection.HTTP_OK) {
                val ad = z.inputStream.bufferedReader().use { ae -> ae.readText() }
                val af = JSONObject(ad)
                if (!af.has("is_valid")) {
                    return@withContext false
                }
                af.getBoolean("is_valid")
            } else {
                false
            }
        } catch (ag: Exception) {
            false
        }
    }

    @SuppressLint("PackageManagerGetSignatures")
    private fun q(): PackageInfo {
        return if (VERSION.SDK_INT >= VERSION_CODES.P) {
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNING_CERTIFICATES
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNATURES
            )
        }
    }

    private fun s(ah: PackageInfo): Array<Signature> {
        return if (VERSION.SDK_INT >= VERSION_CODES.P) {
            ah.signingInfo.apkContentsSigners
        } else {
            @Suppress("DEPRECATION")
            ah.signatures
        }
    }

    private fun v(ai: Signature): String {
        return try {
            val aj = MessageDigest.getInstance("SHA-256")
            val ak = aj.digest(ai.toByteArray())
            ak.joinToString("") { "%02x".format(it) }
        } catch (al: Exception) {
            ""
        }
    }

    private fun o(am: Boolean) {
        sharedPreferences.edit { putBoolean(D, am) }
    }

    private suspend fun checkLicenseValidity(): Pair<Boolean, SecurityErrorType?> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$LICENSE_INFO_ENDPOINT?licenseKey=$KEY_LICENSE")
            val infoConnection = url.openConnection() as HttpURLConnection
            infoConnection.requestMethod = "GET"

            val infoResponse = infoConnection.inputStream.bufferedReader().use { it.readText() }
            val infoJson = JSONObject(infoResponse)

            if (!infoJson.getBoolean("success")) {
                return@withContext Pair(false, SecurityErrorType.LICENSE_NOT_FOUND)
            }

            val validateUrl = URL(LICENSE_ENDPOINT)
            val validateConnection = validateUrl.openConnection() as HttpURLConnection
            validateConnection.requestMethod = "POST"
            validateConnection.doOutput = true
            validateConnection.setRequestProperty("Content-Type", "application/json")

            val requestBody = JSONObject().apply {
                put("licenseKey", KEY_LICENSE)
                put("update_last_check", true)
            }

            validateConnection.outputStream.use { os ->
                os.write(requestBody.toString().toByteArray())
                os.flush()
            }

            val validateResponse = validateConnection.inputStream.bufferedReader().use { it.readText() }
            val validateJson = JSONObject(validateResponse)

            validateJson.optJSONObject("license")?.let { licenseData ->
                val lastCheck = licenseData.optString("lastCheck")
                if (lastCheck.isNotEmpty()) {
                    sharedPreferences.edit().apply {
                        putString("last_check", lastCheck)
                        apply()
                    }
                }
            }

            if (validateJson.getBoolean("success")) {
                Pair(true, null)
            } else {
                Pair(false, SecurityErrorType.LICENSE_INACTIVE)
            }
        } catch (e: Exception) {
            Pair(false, SecurityErrorType.LICENSE_NOT_FOUND)
        }
    }
}

enum class SecurityErrorType {
    TAMPERED,              // دستکاری شده
    LICENSE_NOT_FOUND,     // لایسنس پیدا نشد
    LICENSE_INACTIVE,      // لایسنس غیرفعال است
    NETWORK_ERROR,         // خطای شبکه
    UNKNOWN_ERROR,         // خطای نامشخص
}