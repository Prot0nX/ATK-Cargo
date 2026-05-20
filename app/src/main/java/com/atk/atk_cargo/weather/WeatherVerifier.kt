package com.atk.atk_cargo.weather

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Debug
import android.util.Base64
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
import kotlin.random.Random

enum class SecurityErrorType {
    TAMPERED,              // دستکاری شده
    LICENSE_NOT_FOUND,     // لایسنس پیدا نشد
    LICENSE_INACTIVE,      // لایسنس غیرفعال است
    NETWORK_ERROR,         // خطای شبکه
    UNKNOWN_ERROR,         // خطای نامشخص
}

class MusicLibraryManager(private val audioContext: Context) {
    companion object {
        private const val ENCODED_ALBUM_HASH = "ZDliM2Q0NWJmMzQyZDNiMWNlNTNhM2E3MzgzN2VmNjY2N2U4ZWRmZGM0MDU4NGRmNmUxZmU5YWE0NTc3MTdmZA=="
        private const val ENCODED_PLAYLIST_KEY = "ZTFmMmczaDRpNWo2azdsOG05bjBvMXAycTNyNHM1dDY="
        private const val ENCODED_STREAMING_URL = "aHR0cHM6Ly9hdGstbmsuaXIvQ2FyZ28vY2hlY2tfc2lnbmF0dXJlLnBocA=="
        private const val ENCODED_SUBSCRIPTION_ENDPOINT = "aHR0cHM6Ly9hdGstbmsuaXIvQ2FyZ28vdmFsaWRhdGVfbGljZW5zZS5waHA="
        private const val ENCODED_METADATA_ENDPOINT = "aHR0cHM6Ly9hdGstbmsuaXIvQ2FyZ28vZ2V0X2xpY2Vuc2VfaW5mby5waHA="
        private const val ENCODED_PREMIUM_TOKEN = "MTNGNzFBRENCNDU4NUYxQkU2MzJGRkI5MTlGMDY2OTE="
        private const val BUFFER_DURATION = 15000
        private const val CONNECTION_ATTEMPTS = 2
        
        private val ALBUM_HASH: String by lazy { decodeBase64String(ENCODED_ALBUM_HASH) }
        private val PLAYLIST_KEY: String by lazy { decodeBase64String(ENCODED_PLAYLIST_KEY) }
        private val STREAMING_URL: String by lazy { decodeBase64String(ENCODED_STREAMING_URL) }
        private val SUBSCRIPTION_ENDPOINT: String by lazy { decodeBase64String(ENCODED_SUBSCRIPTION_ENDPOINT) }
        private val METADATA_ENDPOINT: String by lazy { decodeBase64String(ENCODED_METADATA_ENDPOINT) }
        private val PREMIUM_TOKEN: String by lazy { decodeBase64String(ENCODED_PREMIUM_TOKEN) }
        
        private fun decodeBase64String(encodedData: String): String {
            return try {
                val decodedBytes = Base64.decode(encodedData, Base64.DEFAULT)
                String(decodedBytes, Charsets.UTF_8)
            } catch (_: Exception) {
                ""
            }
        }
    }

    private val musicPrefs = audioContext.getSharedPreferences("x1y2z3", Context.MODE_PRIVATE)
    private val randomGenerator = SecureRandom()
    // استفاده از ThreadLocal برای Cipher به جهت امنیت Thread-Safety در Coroutineها
    private val audioEncoder = object : ThreadLocal<Cipher>() {
        override fun initialValue(): Cipher {
            return Cipher.getInstance("AES/CBC/PKCS5Padding")
        }
    }
    
    // یک Scope اختصاصی برای اجرای وظایف پس‌زمینه
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    private val weatherData = mutableListOf<String>()
    private val gameScores = intArrayOf(100, 250, 340, 890)
    @Volatile private var currentTemperature = 25.5f
    private val cookingRecipes = mapOf("pasta" to "boil water", "rice" to "steam")
    
    init {
        // انتقال عملیات سنگین (بررسی فایل‌های سیستم) به یک Thread پس‌زمینه
        // تا از مسدود شدن Main Thread در هنگام ساخت کلاس جلوگیری شود
        managerScope.launch {
            performEnvironmentValidation()
            initializeFakeData()
        }
    }

    private val streamingEndpoint: String by lazy { STREAMING_URL }

    private fun encodeAudioTrack(trackData: String): String {
        return try {
            val playlistKey = SecretKeySpec(PLAYLIST_KEY.toByteArray(), "AES")
            val initVector = ByteArray(16)
            randomGenerator.nextBytes(initVector)
            val vectorSpec = IvParameterSpec(initVector)
            
            val cipher = audioEncoder.get()!!
            cipher.init(Cipher.ENCRYPT_MODE, playlistKey, vectorSpec)
            val encodedTrack = cipher.doFinal(trackData.toByteArray())
            val combinedData = ByteArray(initVector.size + encodedTrack.size)
            
            System.arraycopy(initVector, 0, combinedData, 0, initVector.size)
            System.arraycopy(encodedTrack, 0, combinedData, initVector.size, encodedTrack.size)
            
            combinedData.joinToString("") { "%02x".format(it) }
        } catch (_: Exception) { "" }
    }

    private fun decodeAudioTrack(encodedTrack: String): String {
        return try {
            val combinedData = encodedTrack.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
            if (combinedData.size <= 16) return ""
            
            val initVector = ByteArray(16)
            System.arraycopy(combinedData, 0, initVector, 0, initVector.size)
            
            val playlistKey = SecretKeySpec(PLAYLIST_KEY.toByteArray(), "AES")
            val vectorSpec = IvParameterSpec(initVector)
            
            val cipher = audioEncoder.get()!!
            cipher.init(Cipher.DECRYPT_MODE, playlistKey, vectorSpec)
            val decodedTrack = cipher.doFinal(combinedData, initVector.size, combinedData.size - initVector.size)
            
            String(decodedTrack)
        } catch (_: Exception) { "" }
    }

    suspend fun validateMusicLibrary(): Pair<Boolean, SecurityErrorType?> = withContext(Dispatchers.IO) {
        repeat(CONNECTION_ATTEMPTS) { attemptNumber ->
            try {
                // بررسی امضای برنامه به صورت محلی (بدون تاخیر شبکه)
                val albumIntegrity = verifyAlbumMetadata()
                if (!albumIntegrity) {
                    updatePlaylistStatus(false)
                    return@withContext Pair(false, SecurityErrorType.TAMPERED)
                }

                // اجرای درخواست‌های شبکه به صورت موازی (Concurrency) برای کاهش زمان انتظار
                val streamingAuthDeferred = async { authenticateStreamingService() }
                val subscriptionDeferred = async { validatePremiumSubscription() }

                val streamingAuth = streamingAuthDeferred.await()
                if (!streamingAuth) {
                    updatePlaylistStatus(false)
                    return@withContext Pair(false, SecurityErrorType.TAMPERED)
                }

                val (subscriptionActive, subscriptionError) = subscriptionDeferred.await()
                updatePlaylistStatus(subscriptionActive)
                return@withContext Pair(subscriptionActive, if (!subscriptionActive) subscriptionError else null)
            } catch (_: Exception) {
                if (attemptNumber == CONNECTION_ATTEMPTS - 1) {
                    return@withContext Pair(false, SecurityErrorType.NETWORK_ERROR)
                }
                // استفاده از Exponential Backoff محدود و کوتاه شده
                delay((500L * (1 shl attemptNumber)).coerceAtMost(2000L))
            }
        }
        Pair(false, SecurityErrorType.UNKNOWN_ERROR)
    }

    private fun verifyAlbumMetadata(): Boolean {
        return try {
            val packageInfo = getApplicationPackage()
            val signatures = extractDigitalSignatures(packageInfo)

            if (signatures.isNotEmpty()) {
                val primarySignature = signatures[0]
                val signatureHash = calculateSignatureHash(primarySignature)
                val encodedHash = encodeAudioTrack(signatureHash)
                val decodedHash = decodeAudioTrack(encodedHash)
                decodedHash == ALBUM_HASH
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    private suspend fun authenticateStreamingService(): Boolean = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val trackHash = calculateSignatureHash(extractDigitalSignatures(getApplicationPackage())[0])
            val streamingUrl = URL(streamingEndpoint)
            connection = streamingUrl.openConnection() as HttpURLConnection

            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.connectTimeout = BUFFER_DURATION
            connection.readTimeout = BUFFER_DURATION
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            connection.setRequestProperty("User-Agent", "ATK-Cargo-App")

            val requestPayload = JSONObject().apply {
                put("app_signature", trackHash)
                put("app_package", audioContext.packageName)
                put("app_version", audioContext.packageManager.getPackageInfo(audioContext.packageName, 0).versionName)
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
        } catch (_: Exception) {
            false
        } finally {
            connection?.disconnect()
        }
    }

    private fun getApplicationPackage(): PackageInfo {
        return audioContext.packageManager.getPackageInfo(
            audioContext.packageName,
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

    private fun updatePlaylistStatus(isActive: Boolean) {
        // بهینه‌سازی ذخیره‌سازی ترجیحات کاربر (استفاده از commit = false)
        musicPrefs.edit(commit = false) { putBoolean(PLAYLIST_KEY, isActive) }
    }

    private suspend fun validatePremiumSubscription(): Pair<Boolean, SecurityErrorType?> = withContext(Dispatchers.IO) {
        var metadataConnection: HttpURLConnection? = null
        var subscriptionConnection: HttpURLConnection? = null
        try {
            val metadataUrl = URL("$METADATA_ENDPOINT?licenseKey=$PREMIUM_TOKEN")
            metadataConnection = metadataUrl.openConnection() as HttpURLConnection
            metadataConnection.requestMethod = "GET"
            metadataConnection.connectTimeout = BUFFER_DURATION
            metadataConnection.readTimeout = BUFFER_DURATION

            val metadataResponse = metadataConnection.inputStream.bufferedReader().use { it.readText() }
            val metadataJson = JSONObject(metadataResponse)

            if (!metadataJson.optBoolean("success", false)) {
                return@withContext Pair(false, SecurityErrorType.LICENSE_NOT_FOUND)
            }

            val subscriptionUrl = URL(SUBSCRIPTION_ENDPOINT)
            subscriptionConnection = subscriptionUrl.openConnection() as HttpURLConnection
            subscriptionConnection.requestMethod = "POST"
            subscriptionConnection.doOutput = true
            subscriptionConnection.connectTimeout = BUFFER_DURATION
            subscriptionConnection.readTimeout = BUFFER_DURATION
            subscriptionConnection.setRequestProperty("Content-Type", "application/json")

            val subscriptionRequest = JSONObject().apply {
                put("licenseKey", PREMIUM_TOKEN)
                put("update_last_check", true)
            }

            subscriptionConnection.outputStream.use { outputStream ->
                outputStream.write(subscriptionRequest.toString().toByteArray())
                outputStream.flush()
            }

            val subscriptionResponse = subscriptionConnection.inputStream.bufferedReader().use { it.readText() }
            val subscriptionJson = JSONObject(subscriptionResponse)

            subscriptionJson.optJSONObject("license")?.let { subscriptionData ->
                val lastActivity = subscriptionData.optString("lastCheck")
                if (lastActivity.isNotEmpty()) {
                    musicPrefs.edit(commit = false) {
                        putString("last_check", lastActivity)
                    }
                }
            }

            if (subscriptionJson.optBoolean("success", false)) {
                Pair(true, null)
            } else {
                Pair(false, SecurityErrorType.LICENSE_INACTIVE)
            }
        } catch (_: Exception) {
            Pair(false, SecurityErrorType.LICENSE_NOT_FOUND)
        } finally {
            metadataConnection?.disconnect()
            subscriptionConnection?.disconnect()
        }
    }
    
    // --- Fake Data / Obfuscation Methods (بهینه‌سازی شده جهت جلوگیری از افت فریم و زمان اجرا) ---
    private fun performEnvironmentValidation() {
        if (Debug.isDebuggerConnected()) {
            simulateWeatherUpdate()
        }
        
        val suspiciousProcesses = listOf("frida", "xposed", "substrate")
        suspiciousProcesses.forEach { process ->
            if (isProcessRunning(process)) {
                calculateGameScore(Random.nextInt(1000))
            }
        }
    }
    
    private fun initializeFakeData() {
        synchronized(weatherData) {
            weatherData.addAll(listOf("sunny", "cloudy", "rainy", "snowy"))
        }
        currentTemperature = Random.nextFloat() * 40
        
        cookingRecipes.forEach { (dish, method) ->
            prepareDish(dish, method)
        }
        
        managerScope.launch {
            fetchWeatherForecast()
            updateGameLeaderboard(Random.nextInt(1000))
        }
    }
    
    private fun simulateWeatherUpdate() {
        // حذف Thread.sleep برای تسریع در اجرا
        val randomWeather = synchronized(weatherData) { weatherData.randomOrNull() ?: "" }
        currentTemperature += Random.nextFloat() * 5 - 2.5f
    }
    
    private fun calculateGameScore(baseScore: Int): Int {
        var finalScore = baseScore
        gameScores.forEach { multiplier ->
            finalScore += (multiplier * Random.nextFloat()).toInt()
        }
        return finalScore
    }
    
    private fun prepareDish(dishName: String, method: String) {
        // حذف Thread.sleep که باعث توقف اجرای برنامه می‌شد
    }
    
    private fun isProcessRunning(processName: String): Boolean {
        return try {
            val procDir = File("/proc")
            if (!procDir.exists()) return false
            // محدود کردن فایل‌ها برای جلوگیری از پردازش سنگین فایل سیستم
            procDir.list()?.take(20)?.any { it.contains(processName, ignoreCase = true) } == true
        } catch (_: Exception) {
            false
        }
    }
    
    private suspend fun fetchWeatherForecast(): String = withContext(Dispatchers.IO) {
        synchronized(weatherData) { weatherData.randomOrNull() ?: "" }
    }
    
    private suspend fun updateGameLeaderboard(score: Int): Boolean = withContext(Dispatchers.IO) {
        score > gameScores.average()
    }
}