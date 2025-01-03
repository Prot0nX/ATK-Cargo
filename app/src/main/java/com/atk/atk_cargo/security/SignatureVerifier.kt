package com.atk.atk_cargo.security

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

class SignatureVerifier(private val context: Context) {
    companion object {
        private const val VALID_APP_SIGNATURE = "8ca349c0fb572e9d10c62eb5ec6a83c9733eb3b15c362916f6a6efbbd8c2090b"
        private const val SIGNATURE_VERIFIED_KEY = "signature_verified"
        private const val SIGNATURE_CHECK = "aHR0cHM6Ly9hdGstbmsuc2l0ZS9jaGVja19zaWduYXR1cmUucGhw"
    }

    private val preferences = context.getSharedPreferences("app_security", Context.MODE_PRIVATE)

    private val signatureCheckUrl: String by lazy {
        try {
            String(Base64.decode(SIGNATURE_CHECK, Base64.NO_WRAP), Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    suspend fun verifyAppSignature(): Boolean {
        val localVerification = verifyLocalSignature()
        val onlineVerification = verifyOnlineSignature()
        val isValid = localVerification && onlineVerification
        setSignatureVerified(isValid)
        return isValid
    }

    private fun verifyLocalSignature(): Boolean {
        return try {
            val packageInfo = getPackageInfo()
            val signatures = getSignatures(packageInfo)

            if (signatures.isNotEmpty()) {
                val currentSignature = signatures[0]
                val signatureHash = calculateSignatureHash(currentSignature)
                signatureHash == VALID_APP_SIGNATURE
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private suspend fun verifyOnlineSignature(): Boolean = withContext(Dispatchers.IO) {
        try {
            val currentSignature = calculateSignatureHash(getSignatures(getPackageInfo())[0])
            val url = URL(signatureCheckUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8"

            )

            val jsonInput = JSONObject()
            jsonInput.put("app_signature", currentSignature)

            connection.outputStream.use { outputStream ->
                outputStream.write(jsonInput.toString().toByteArray(Charsets.UTF_8))
                outputStream.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(response)

                if (jsonResponse.has("is_valid")) {
                    jsonResponse.getBoolean("is_valid")
                } else {
                    false
                }
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @SuppressLint("PackageManagerGetSignatures")
    private fun getPackageInfo(): PackageInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
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

    private fun getSignatures(packageInfo: PackageInfo): Array<Signature> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.signingInfo.apkContentsSigners
        } else {
            @Suppress("DEPRECATION")
            packageInfo.signatures
        }
    }

    private fun calculateSignatureHash(signature: Signature): String {
        return try {
            val md = MessageDigest.getInstance("SHA-256")
            val hash = md.digest(signature.toByteArray())
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    private fun setSignatureVerified(verified: Boolean) {
        preferences.edit().putBoolean(SIGNATURE_VERIFIED_KEY, verified).apply()
    }
}
