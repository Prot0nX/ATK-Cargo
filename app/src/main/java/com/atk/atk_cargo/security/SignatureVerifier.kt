package com.atk.atk_cargo.security

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import java.security.MessageDigest

class SignatureVerifier(private val context: Context) {
    companion object {
        private const val VALID_APP_SIGNATURE = "8ca349c0fb572e9d10c62eb5ec6a83c9733eb3b15c362916f6a6efbbd8c2090b"
        private const val SIGNATURE_VERIFIED_KEY = "signature_verified"
    }

    @SuppressLint("PackageManagerGetSignatures")
    fun verifyAppSignature(): Boolean {
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

    private val preferences = context.getSharedPreferences("app_security", Context.MODE_PRIVATE)

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