package com.atk.atk_cargo.security

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

class SignatureVerifier(private val context: Context) {
    companion object {
        private const val VALID_APP_SIGNATURE = "0b0838afbe74afd97e781c9533015294ce86b70627017cccb807ca2af208ba21"
        private const val SIGNATURE_VERIFIED_KEY = "signature_verified"
        private const val SIGNATURE_CHECK_URL = "https://atk-nk.site/check_signature.php"
    }

    private val preferences = context.getSharedPreferences("app_security", Context.MODE_PRIVATE)

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
            val url = URL(SIGNATURE_CHECK_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val jsonResponse = JSONObject(response)

            val validSignature = jsonResponse.getString("valid_signature")
            val currentSignature = calculateSignatureHash(getSignatures(getPackageInfo())[0])

            currentSignature == validSignature
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
            hash.fold("") { str, it -> str + "%02x".format(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun setSignatureVerified(verified: Boolean) {
        preferences.edit().putBoolean(SIGNATURE_VERIFIED_KEY, verified).apply()
    }
}