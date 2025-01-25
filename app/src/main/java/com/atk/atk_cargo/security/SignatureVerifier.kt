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
        private const val C = "8ca349c0fb572e9d10c62eb5ec6a83c9733eb3b15c362916f6a6efbbd8c2090b"
        private const val D = "e1f2g3h4i5j6k7l8m9n0o1p2q3r4s5t6"
        private const val E = "aHR0cHM6Ly9hdGstbmsuc2l0ZS9jaGVja19zaWduYXR1cmUucGhw"
    }

    private val f = context.getSharedPreferences("x1y2z3", Context.MODE_PRIVATE)

    private val g: String by lazy {
        try {
            String(Base64.decode(E, Base64.NO_WRAP), Charsets.UTF_8)
        } catch (h: Exception) {
            h.printStackTrace()
            ""
        }
    }

    suspend fun i(): Boolean {
        val j = k()
        val l = m()
        val n = j && l
        o(n)
        return n
    }

    private fun k(): Boolean {
        return try {
            val p = q()
            val r = s(p)

            if (r.isNotEmpty()) {
                val t = r[0]
                val u = v(t)
                u == C
            } else {
                false
            }
        } catch (w: Exception) {
            w.printStackTrace()
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
            z.setRequestProperty("Content-Type", "application/json; charset=UTF-8")

            val aa = JSONObject()
            aa.put("app_signature", x)

            z.outputStream.use { ab ->
                ab.write(aa.toString().toByteArray(Charsets.UTF_8))
                ab.flush()
            }

            val ac = z.responseCode
            if (ac == HttpURLConnection.HTTP_OK) {
                val ad = z.inputStream.bufferedReader().use { ae -> ae.readText() }
                val af = JSONObject(ad)

                if (af.has("is_valid")) {
                    af.getBoolean("is_valid")
                } else {
                    false
                }
            } else {
                false
            }
        } catch (ag: Exception) {
            ag.printStackTrace()
            false
        }
    }

    @SuppressLint("PackageManagerGetSignatures")
    private fun q(): PackageInfo {
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

    private fun s(ah: PackageInfo): Array<Signature> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
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
            al.printStackTrace()
            ""
        }
    }

    private fun o(am: Boolean) {
        f.edit().putBoolean(D, am).apply()
    }
}
