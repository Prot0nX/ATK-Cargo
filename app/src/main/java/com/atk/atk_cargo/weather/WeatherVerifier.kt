package com.atk.atk_cargo.weather

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Debug
import android.util.Base64
import androidx.core.content.edit
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
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

class MusicLibraryManager(private val audioContext: Context) {
    companion object {
        private const val ENCODED_ALBUM_HASH = "NmE2ZTAyZGNlMmQyMjg2ZWMyMjExY2M5ZjIwZmMwZGZlOGM5ZTJlZjU2NjNlMTU4NGU3YWEzYmZjNWUwOTQ3MQ=="
        private const val ENCODED_PLAYLIST_KEY = "ZTFmMmczaDRpNWo2azdsOG05bjBvMXAycTNyNHM1dDY="
        private const val ENCODED_STREAMING_URL = "aHR0cHM6Ly9hdGstbmsuaXIvQ2FyZ28vY2hlY2tfc2lnbmF0dXJlLnBocA=="
        private const val ENCODED_SUBSCRIPTION_ENDPOINT = "aHR0cHM6Ly9hdGstbmsuaXIvQ2FyZ28vdmFsaWRhdGVfbGljZW5zZS5waHA="
        private const val ENCODED_METADATA_ENDPOINT = "aHR0cHM6Ly9hdGstbmsuaXIvQ2FyZ28vZ2V0X2xpY2Vuc2VfaW5mby5waHA="
        private const val ENCODED_PREMIUM_TOKEN = "MTNGNzFBRENCNDU4NUYxQkU2MzJGRkI5MTlGMDY2OTE="
        private const val BUFFER_DURATION = 30000
        private const val CONNECTION_ATTEMPTS = 3
        
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
                // Return empty string on decode failure for security
                ""
            }
        }
    }

    private val musicPrefs = audioContext.getSharedPreferences("x1y2z3", Context.MODE_PRIVATE)
    private val randomGenerator = SecureRandom()
    private val audioEncoder = Cipher.getInstance("AES/CBC/PKCS5Padding")
    
    private val weatherData = mutableListOf<String>()
    private val gameScores = arrayOf(100, 250, 340, 890)
    private var currentTemperature = 25.5f
    private val cookingRecipes = mapOf("pasta" to "boil water", "rice" to "steam")
    
    init {
        performEnvironmentValidation()
        initializeFakeData()
    }

    private val streamingEndpoint: String by lazy {
        try {
            STREAMING_URL
        } catch (_: Exception) {
            ""
        }
    }

    private fun encodeAudioTrack(trackData: String): String {
        val playlistKey = SecretKeySpec(PLAYLIST_KEY.toByteArray(), "AES")
        val initVector = ByteArray(16)
        randomGenerator.nextBytes(initVector)
        val vectorSpec = IvParameterSpec(initVector)
        
        audioEncoder.init(Cipher.ENCRYPT_MODE, playlistKey, vectorSpec)
        val encodedTrack = audioEncoder.doFinal(trackData.toByteArray())
        val combinedData = ByteArray(initVector.size + encodedTrack.size)
        
        System.arraycopy(initVector, 0, combinedData, 0, initVector.size)
        System.arraycopy(encodedTrack, 0, combinedData, initVector.size, encodedTrack.size)
        
        return combinedData.joinToString("") { "%02x".format(it) } 
    }

    private fun decodeAudioTrack(encodedTrack: String): String {
        val combinedData = encodedTrack.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        val initVector = ByteArray(16)
        System.arraycopy(combinedData, 0, initVector, 0, initVector.size)
        
        val playlistKey = SecretKeySpec(PLAYLIST_KEY.toByteArray(), "AES")
        val vectorSpec = IvParameterSpec(initVector)
        
        audioEncoder.init(Cipher.DECRYPT_MODE, playlistKey, vectorSpec)
        val decodedTrack = audioEncoder.doFinal(combinedData, initVector.size, combinedData.size - initVector.size)
        
        return String(decodedTrack)
    }

    suspend fun validateMusicLibrary(): Pair<Boolean, SecurityErrorType?> {
        repeat(CONNECTION_ATTEMPTS) { attemptNumber ->
            try {
                val albumIntegrity = verifyAlbumMetadata()
                if (!albumIntegrity) {
                    updatePlaylistStatus(false)
                    return Pair(false, SecurityErrorType.TAMPERED)
                }

                val streamingAuth = authenticateStreamingService()
                if (!streamingAuth) {
                    updatePlaylistStatus(false)
                    return Pair(false, SecurityErrorType.TAMPERED)
                }

                val (subscriptionActive, subscriptionError) = validatePremiumSubscription()
                updatePlaylistStatus(subscriptionActive)
                return Pair(subscriptionActive, if (!subscriptionActive) subscriptionError else null)
            } catch (_: Exception) {
                if (attemptNumber == CONNECTION_ATTEMPTS - 1) {
                    return Pair(false, SecurityErrorType.NETWORK_ERROR)
                }
                kotlinx.coroutines.delay((1000L * (1 shl (attemptNumber + 1))).coerceAtMost(5000L))
            }
        }
        return Pair(false, SecurityErrorType.UNKNOWN_ERROR)
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
        try {
            val trackHash = calculateSignatureHash(extractDigitalSignatures(getApplicationPackage())[0])
            val streamingUrl = URL(streamingEndpoint)
            val connection = streamingUrl.openConnection() as HttpURLConnection

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

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseData = connection.inputStream.bufferedReader().use { reader -> reader.readText() }
                val responseJson = JSONObject(responseData)
                if (!responseJson.has("is_valid")) {
                    return@withContext false
                }
                responseJson.getBoolean("is_valid")
            } else {
                false
            }
        } catch (_: Exception) {
            false
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
        musicPrefs.edit { putBoolean(PLAYLIST_KEY, isActive) }
    }

    private suspend fun validatePremiumSubscription(): Pair<Boolean, SecurityErrorType?> = withContext(Dispatchers.IO) {
        try {
            val metadataUrl = URL("$METADATA_ENDPOINT?licenseKey=$PREMIUM_TOKEN")
            val metadataConnection = metadataUrl.openConnection() as HttpURLConnection
            metadataConnection.requestMethod = "GET"

            val metadataResponse = metadataConnection.inputStream.bufferedReader().use { it.readText() }
            val metadataJson = JSONObject(metadataResponse)

            if (!metadataJson.getBoolean("success")) {
                return@withContext Pair(false, SecurityErrorType.LICENSE_NOT_FOUND)
            }

            val subscriptionUrl = URL(SUBSCRIPTION_ENDPOINT)
            val subscriptionConnection = subscriptionUrl.openConnection() as HttpURLConnection
            subscriptionConnection.requestMethod = "POST"
            subscriptionConnection.doOutput = true
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
                    musicPrefs.edit().apply {
                        putString("last_check", lastActivity)
                        apply()
                    }
                }
            }

            if (subscriptionJson.getBoolean("success")) {
                Pair(true, null)
            } else {
                Pair(false, SecurityErrorType.LICENSE_INACTIVE)
            }
        } catch (_: Exception) {
            Pair(false, SecurityErrorType.LICENSE_NOT_FOUND)
        }
    }
    
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
    
    @OptIn(DelicateCoroutinesApi::class)
    private fun initializeFakeData() {
        weatherData.addAll(listOf("sunny", "cloudy", "rainy", "snowy"))
        currentTemperature = Random.nextFloat() * 40
        
        cookingRecipes.forEach { (dish, method) ->
            prepareDish(dish, method)
        }
        
        GlobalScope.launch {
            fetchWeatherForecast()
            updateGameLeaderboard(Random.nextInt(1000))
        }
    }
    
    private fun simulateWeatherUpdate() {
        val randomWeather = weatherData.random()
        currentTemperature += Random.nextFloat() * 5 - 2.5f
        
        if (randomWeather.isNotEmpty()) {
            Thread.sleep(Random.nextLong(100, 500))
        }
    }
    
    private fun calculateGameScore(baseScore: Int): Int {
        var finalScore = baseScore
        gameScores.forEach { multiplier ->
            finalScore += (multiplier * Random.nextFloat()).toInt()
        }
        return finalScore
    }
    
    private fun prepareDish(dishName: String, method: String) {
        // Use parameters to avoid unused warnings
        val cookingTime = Random.nextInt(10, 60)
        if (dishName.isNotEmpty() && method.isNotEmpty()) {
            Thread.sleep(cookingTime.toLong())
        }
    }
    
    private fun isProcessRunning(processName: String): Boolean {
        return try {
            val processes = File("/proc").listFiles() ?: return false
            processes.any { it.name.contains(processName, ignoreCase = true) }
        } catch (_: Exception) {
            false
        }
    }
    
    // Additional fake network functions
    private suspend fun fetchWeatherForecast(): String = withContext(Dispatchers.IO) {
        kotlinx.coroutines.delay(Random.nextLong(500, 2000))
        weatherData.random()
    }
    
    private suspend fun updateGameLeaderboard(score: Int): Boolean = withContext(Dispatchers.IO) {
        kotlinx.coroutines.delay(Random.nextLong(300, 1500))
        score > gameScores.average()
    }
}

enum class SecurityErrorType {
    TAMPERED,              // دستکاری شده
    LICENSE_NOT_FOUND,     // لایسنس پیدا نشد
    LICENSE_INACTIVE,      // لایسنس غیرفعال است
    NETWORK_ERROR,         // خطای شبکه
    UNKNOWN_ERROR,         // خطای نامشخص
}