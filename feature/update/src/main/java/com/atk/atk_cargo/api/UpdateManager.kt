package com.atk.atk_cargo.api

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import kotlin.math.pow
import kotlin.time.Duration.Companion.milliseconds

data class UpdateInfo(
    val latestVersion: String,
    val downloadUrl: String,
    val minRequiredVersion: String = "1.0",
    val updatePriority: String = "normal",
    val updateMessage: String = "",
    val forceUpdate: Boolean = false,
    val updateSize: String = "0",
    val releaseDate: String = "",
    val minAndroidVersion: Int = 21,
    val minAppVersion: String = "1.0",
    val excludedVersions: List<String> = emptyList(),
    val sha256: String = ""
)

class UpdateManager(
    context: Context
) {
    private val appContext: Context = context.applicationContext
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .protocols(listOf(Protocol.HTTP_1_1))
        .retryOnConnectionFailure(true)
        .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
        .dispatcher(Dispatcher().apply {
            maxRequestsPerHost = 10
            maxRequests = 20
        })
        .build()

    private val _downloadProgress = MutableStateFlow(DownloadProgress.Initial)
    val downloadProgress: StateFlow<DownloadProgress> = _downloadProgress

    private val _updateInfo = MutableStateFlow<UpdateInfo?>(null)
    val updateInfo: StateFlow<UpdateInfo?> = _updateInfo

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState

    private val _minAllowedVersion = MutableStateFlow<String?>(null)

    private var downloadTimestamp: Long = 0
    private var downloadJob: Job? = null
    private val downloadedBytes = java.util.concurrent.atomic.AtomicLong(0)
    private var totalBytes: Long = 0
    private var lastProgress: Float = 0f
    private lateinit var currentDownloadFile: File

    // تعداد بخش‌های دانلود همزمان
    private val concurrentChunks = 4
    // سایز هر بخش (8 مگابایت)
    private val chunkSize = 8 * 1024 * 1024L

    sealed class DownloadState {
        data object Idle : DownloadState()
        data object Downloading : DownloadState()
        data class Paused(
            val downloadedBytes: Long,
            val totalBytes: Long,
            val progress: Float
        ) : DownloadState()
        data object Completed : DownloadState()
        data class Error(val message: String) : DownloadState()
    }

    data class DownloadProgress(
        val progress: Float = 0f,
        val downloadedSize: Pair<String, String> = "0" to "MB",
        val totalSize: Pair<String, String> = "0" to "MB",
        val speed: Pair<String, String> = "0" to "KB/s"
    ) {
        companion object {
            val Initial = DownloadProgress()
        }
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
            ?: return true
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    data class VersionCheckResult(
        val isVersionAllowed: Boolean,
        val hasUpdate: Boolean
    )

    suspend fun checkVersionAndUpdate(): VersionCheckResult {
        if (!isInternetAvailable()) {
            return VersionCheckResult(isVersionAllowed = true, hasUpdate = false)
        }

        return withContext(Dispatchers.IO) {
            try {
                val currentAppVersion = getCurrentAppVersion()
                val encodedVersion = URLEncoder.encode(currentAppVersion, "UTF-8")

                val request = Request.Builder()
                    .url("${Constants.BASE_URL}api/v2/index.php?route=utility/check-update&current_version=$encodedVersion")
                    .addHeader("X-Api-Key", Constants.API_KEY)
                    .build()

                client.newCall(request).execute().use { response ->
                    val responseBody = response.body.string()

                    when {
                        response.code == 426 -> {
                            val error = JSONObject(responseBody).optString("error", "نسخه برنامه منسوخ شده است")
                            _downloadState.value = DownloadState.Error(error)
                            VersionCheckResult(isVersionAllowed = false, hasUpdate = false)
                        }
                        response.isSuccessful -> {
                            val jsonResponse = JSONObject(responseBody)

                            // ===== حداقل نسخه‌ی مجاز =====
                            val minAllowed = jsonResponse.optString("min_allowed_version", "").ifEmpty {
                                jsonResponse.optString("minAllowedVersion", "")
                            }
                            _minAllowedVersion.value = minAllowed.ifEmpty { null }
                            val isVersionAllowed = if (minAllowed.isNotEmpty()) {
                                // compare > 0 => current newer; ==0 => equal; <0 => current older
                                compareVersions(currentAppVersion, minAllowed) >= 0
                            } else {
                                true
                            }

                            // ===== بررسی وجود نسخه جدید - پشتیبانی از هر دو فرمت (snake_case و camelCase) =====
                            val latestVersion = jsonResponse.optString("latest_version", "").ifEmpty {
                                jsonResponse.optString("latestVersion", "")
                            }

                            val locallyComputedHasUpdate = latestVersion.isNotEmpty() &&
                                    compareVersions(latestVersion, currentAppVersion) > 0
                            val hasUpdate = jsonResponse.optBoolean(
                                "has_update",
                                jsonResponse.optBoolean("hasUpdate", locallyComputedHasUpdate)
                            )

                            if (hasUpdate) {
                                _downloadState.value = DownloadState.Idle
                                // پارس کردن version_constraints
                                val versionConstraints = jsonResponse.optJSONObject("version_constraints")
                                val excludedVersionsList = versionConstraints?.optJSONArray("excluded_versions")?.let { array ->
                                    List(array.length()) { array.getString(it) }
                                } ?: emptyList()

                                // پشتیبانی از هر دو فرمت برای تمام فیلدها
                                val downloadUrl = jsonResponse.optString("download_url", "").ifEmpty {
                                    jsonResponse.optString("downloadUrl", "")
                                }
                                val minRequiredVersion = jsonResponse.optString("min_required_version", "").ifEmpty {
                                    jsonResponse.optString("minRequiredVersion", "1.0")
                                }
                                val updatePriority = jsonResponse.optString("update_priority", "").ifEmpty {
                                    jsonResponse.optString("updatePriority", "normal")
                                }
                                val updateMessage = jsonResponse.optString("update_message", "").ifEmpty {
                                    jsonResponse.optString("updateMessage", "")
                                }
                                val forceUpdate = jsonResponse.optBoolean("force_update",
                                    jsonResponse.optBoolean("forceUpdate", false))

                                val updateSize = jsonResponse.optString("update_size", "").ifEmpty {
                                    jsonResponse.optString("updateSize", "0")
                                }
                                val releaseDate = jsonResponse.optString("release_date", "").ifEmpty {
                                    jsonResponse.optString("releaseDate", "")
                                }
                                val sha256 = jsonResponse.optString("sha256", "")

                                _updateInfo.value = UpdateInfo(
                                    latestVersion = latestVersion,
                                    downloadUrl = downloadUrl,
                                    minRequiredVersion = minRequiredVersion,
                                    updatePriority = updatePriority,
                                    updateMessage = updateMessage,
                                    forceUpdate = forceUpdate,
                                    updateSize = updateSize,
                                    releaseDate = releaseDate,
                                    minAndroidVersion = versionConstraints?.optInt("min_android_version", 21) ?: 21,
                                    minAppVersion = versionConstraints?.optString("min_app_version", "1.0") ?: "1.0",
                                    excludedVersions = excludedVersionsList,
                                    sha256 = sha256
                                )
                            }
                            VersionCheckResult(isVersionAllowed, hasUpdate)
                        }
                        else -> {
                            val errorMsg = "خطا در بررسی بروزرسانی: ${response.code}"
                            _downloadState.value = DownloadState.Error(errorMsg)
                            VersionCheckResult(isVersionAllowed = true, hasUpdate = false)
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val errorMsg = "خطا در بررسی بروزرسانی: ${e.localizedMessage}"
                _downloadState.value = DownloadState.Error(errorMsg)
                VersionCheckResult(isVersionAllowed = true, hasUpdate = false)
            }
        }
    }

    private fun compareVersions(version1: String, version2: String): Int {
        // پاک‌سازی و نرمال‌سازی ورودی‌ها
        val v1Clean = version1.trim().replace(Regex("[^0-9.]"), "")
        val v2Clean = version2.trim().replace(Regex("[^0-9.]"), "")
        
        // تبدیل به لیست اعداد صحیح
        val v1Parts = v1Clean.split(".").mapNotNull { 
            it.toIntOrNull()?.takeIf { num -> num >= 0 }
        }
        val v2Parts = v2Clean.split(".").mapNotNull { 
            it.toIntOrNull()?.takeIf { num -> num >= 0 }
        }
        
        // اگر هر دو خالی باشند، برابرند
        if (v1Parts.isEmpty() && v2Parts.isEmpty()) return 0
        // اگر یکی خالی باشد، دیگری بزرگتر است
        if (v1Parts.isEmpty()) return -1
        if (v2Parts.isEmpty()) return 1
        
        val maxLength = maxOf(v1Parts.size, v2Parts.size)
        
        for (i in 0 until maxLength) {
            val v1 = v1Parts.getOrNull(i) ?: 0
            val v2 = v2Parts.getOrNull(i) ?: 0
            
            if (v1 != v2) {
                return v1 - v2
            }
        }
        
        return 0
    }
    
    // دریافت نسخه فعلی برنامه
    private fun getCurrentAppVersion(): String {
        return try {
            val packageInfo = appContext.packageManager.getPackageInfo(appContext.packageName, 0)
            packageInfo.versionName ?: "1.0"
        } catch (_: Exception) {
            "1.0"
        }
    }

    // بررسی می‌کند که download_url متعلق به همان دامنه معتبر سرور باشد تا از هدایت دانلود به میزبان جعلی جلوگیری شود
    private fun isTrustedDownloadUrl(url: String): Boolean = runCatching {
        val requestHost = java.net.URI(url).takeIf { it.scheme == "https" }?.host ?: return false
        val trustedHost = java.net.URI(Constants.BASE_URL).host ?: return false
        requestHost == trustedHost || requestHost.endsWith(".$trustedHost")
    }.getOrDefault(false)

    @SuppressLint("DefaultLocale")
    fun startDownload(downloadUrl: String, startPosition: Long = 0) {
        if (downloadJob?.isActive == true) return

        // بدون این بررسی، نفوذ به سرور می‌تواند کلاینت را به دانلود APK دلخواه هدایت کند
        if (!isTrustedDownloadUrl(downloadUrl)) {
            _downloadState.value = DownloadState.Error("آدرس به‌روزرسانی نامعتبر است.")
            return
        }

        downloadJob = CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                if (!isInternetAvailable()) {
                    _downloadState.value = DownloadState.Error("لطفاً اتصال اینترنت خود را بررسی کنید")
                    return@launch
                }

                _downloadState.value = DownloadState.Downloading
                downloadTimestamp = System.currentTimeMillis()
                currentDownloadFile = File(appContext.cacheDir, "updates/update_${downloadTimestamp}.apk")
                currentDownloadFile.parentFile?.mkdirs()

                if (startPosition == 0L) {
                    downloadedBytes.set(0)
                    totalBytes = getFileSize(downloadUrl).also { size ->
                        if (size <= 0) throw IOException("Invalid content length: $size")
                    }
                }

                val chunks = calculateChunks(totalBytes, startPosition)

                RandomAccessFile(currentDownloadFile, "rw").use {
                    it.setLength(totalBytes)
                }

                val downloadJobs = chunks.map { chunk ->
                    async {
                        downloadChunk(downloadUrl, chunk)
                    }
                }

                var lastUpdateTime = System.currentTimeMillis()
                var lastDownloadedBytes = downloadedBytes.get()

                while (isActive && downloadJobs.any { it.isActive }) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastUpdateTime >= 100) {
                        val currentDownloadedBytes = downloadedBytes.get()
                        val timeSpent = (currentTime - lastUpdateTime) / 1000f
                        val speed = ((currentDownloadedBytes - lastDownloadedBytes) / timeSpent) / 1024
                        val progress = (currentDownloadedBytes.toFloat() / totalBytes.toFloat()) * 100
                        lastProgress = progress

                        _downloadProgress.value = DownloadProgress(
                            progress = progress,
                            downloadedSize = formatFileSize(currentDownloadedBytes),
                            totalSize = formatFileSize(totalBytes),
                            speed = String.format("%.1f", speed) to "KB/s"
                        )

                        lastUpdateTime = currentTime
                        lastDownloadedBytes = currentDownloadedBytes
                    }
                    delay(100.milliseconds)
                }

                downloadJobs.awaitAll()

                if (downloadedBytes.get() >= totalBytes) {
                    val expectedSha256 = _updateInfo.value?.sha256.orEmpty()
                    if (expectedSha256.isEmpty()) {
                        // fail-closed: بدون هش مرجع از سرور، فایل نامعتبر تلقی می‌شود
                        currentDownloadFile.delete()
                        _downloadState.value = DownloadState.Error("امکان تأیید یکپارچگی فایل وجود ندارد؛ به‌روزرسانی لغو شد.")
                    } else if (!verifyFileSha256(currentDownloadFile, expectedSha256)) {
                        currentDownloadFile.delete()
                        _downloadState.value = DownloadState.Error("فایل دانلودشده معتبر نیست (عدم تطابق هش)")
                    } else {
                        _downloadState.value = DownloadState.Completed
                    }
                }
            } catch (_: CancellationException) {
                _downloadState.value = DownloadState.Paused(
                    downloadedBytes = downloadedBytes.get(),
                    totalBytes = totalBytes,
                    progress = lastProgress
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleDownloadError(e)
            }
        }
    }

    private fun verifyFileSha256(file: File, expectedHash: String): Boolean {
        return try {
            val digest = java.security.MessageDigest.getInstance("SHA-256")
            file.inputStream().use { input ->
                val buffer = ByteArray(256 * 1024)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
            }
            val actualHash = digest.digest().joinToString("") { "%02x".format(it) }
            actualHash.equals(expectedHash.trim(), ignoreCase = true)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("UpdateManager_Log", "خطا در محاسبه SHA-256: ${e.message}", e)
            false
        }
    }

    private fun getFileSize(downloadUrl: String): Long {
        return try {
            val request = Request.Builder()
                .url(downloadUrl)
                .head()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Failed to get file size: ${response.code}")
                }
                response.headers["content-length"]?.toLongOrNull()
                    ?: throw IOException("Content length not found")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw IOException("Error getting file size: ${e.message}", e)
        }
    }

    private fun calculateChunks(contentLength: Long, startPosition: Long): List<Pair<Long, Long>> {
        val chunks = mutableListOf<Pair<Long, Long>>()
        val remainingLength = contentLength - startPosition
        val optimalChunkSize = maxOf(remainingLength / concurrentChunks, chunkSize)
            .coerceAtMost(remainingLength)

        var start = startPosition
        while (start < contentLength) {
            val end = minOf(start + optimalChunkSize - 1, contentLength - 1)
            chunks.add(start to end)
            start = end + 1
        }
        return chunks
    }

    private suspend fun downloadChunk(
        downloadUrl: String,
        chunk: Pair<Long, Long>
    ) {
        val (start, end) = chunk
        var attempt = 0
        val maxAttempts = 3

        fun executeDownload() {
            val request = Request.Builder()
                .url(downloadUrl)
                .addHeader("Range", "bytes=$start-$end")
                .addHeader("Connection", "keep-alive")
                .addHeader("Accept-Encoding", "identity")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Unexpected response: ${response.code}")
                }

                val body = response.body
                val buffer = ByteArray(calculateOptimalBufferSize(totalBytes))

                RandomAccessFile(currentDownloadFile, "rw").use { file ->
                    file.seek(start)
                    body.byteStream().buffered(buffer.size).use { input ->
                        var bytesRead: Int
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            file.write(buffer, 0, bytesRead)
                            downloadedBytes.addAndGet(bytesRead.toLong())
                        }
                    }
                }
            }
        }

        while (true) {
            try {
                executeDownload()
                break
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                attempt++
                if (attempt >= maxAttempts) throw e

                val delayTime = (1000L * 2.0.pow(attempt.toDouble())).toLong()
                delay(delayTime.coerceAtMost(10_000).milliseconds)
            }
        }
    }

    private fun calculateOptimalBufferSize(contentLength: Long): Int {
        return when {
            contentLength > 100 * 1024 * 1024 -> 1024 * 1024  // 1 MB buffer
            contentLength > 50 * 1024 * 1024 -> 512 * 1024    // 512 KB buffer
            else -> 256 * 1024                                // 256 KB buffer
        }
    }

    @SuppressLint("DefaultLocale")
    private fun formatFileSize(size: Long): Pair<String, String> {
        val kb = size / 1024.0
        val mb = kb / 1024.0
        return when {
            mb >= 1 -> String.format("%.1f", mb) to "MB"
            kb >= 1 -> String.format("%.1f", kb) to "KB"
            else -> String.format("%d", size) to "B"
        }
    }

    fun pauseDownload() {
        downloadJob?.cancel()
        _downloadState.value = DownloadState.Paused(
            downloadedBytes = downloadedBytes.get(),
            totalBytes = totalBytes,
            progress = lastProgress
        )
    }

    fun resumeDownload() {
        when (val currentState = _downloadState.value) {
            is DownloadState.Paused -> {
                // بازیابی وضعیت قبلی دانلود
                downloadedBytes.set(currentState.downloadedBytes)
                totalBytes = currentState.totalBytes
                lastProgress = currentState.progress

                _updateInfo.value?.downloadUrl?.let { url ->
                    startDownload(url, downloadedBytes.get())
                }
            }
            else -> {
                _downloadState.value = DownloadState.Error("وضعیت نامعتبر برای ادامه دانلود")
            }
        }
    }

    fun cancelDownload() {
        downloadJob?.cancel()
        downloadedBytes.set(0)
        totalBytes = 0
        lastProgress = 0f
        _downloadState.value = DownloadState.Idle
        _downloadProgress.value = DownloadProgress.Initial
        if (::currentDownloadFile.isInitialized) {
            currentDownloadFile.delete()
        }
    }

    fun getDownloadedFile(): File {
        return if (::currentDownloadFile.isInitialized && currentDownloadFile.exists()) {
            currentDownloadFile
        } else {
            File(appContext.cacheDir, "updates/update_${downloadTimestamp}.apk")
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    fun installUpdate(apkFile: File) {
        try {
            // بررسی وجود فایل
            if (!apkFile.exists()) {
                throw IOException("فایل نصب یافت نشد")
            }

            // بررسی حجم فایل
            if (apkFile.length() == 0L) {
                throw IOException("فایل نصب خالی است")
            }

            // بررسی پسوند فایل
            if (!apkFile.name.endsWith(".apk", ignoreCase = true)) {
                throw IOException("فرمت فایل نصب نامعتبر است")
            }

            val uri = FileProvider.getUriForFile(
                appContext,
                "${appContext.packageName}.fileprovider",
                apkFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                addCategory(Intent.CATEGORY_DEFAULT)
            }

            if (intent.resolveActivity(appContext.packageManager) != null) {
                appContext.startActivity(intent)
                _downloadState.value = DownloadState.Idle
            } else {
                throw Exception("برنامه‌ای برای نصب فایل APK یافت نشد")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val errorMessage = when {
                e is IOException && e.message?.contains("ENOSPC") == true -> "فضای کافی در دستگاه موجود نیست"
                e.message?.contains("Permission denied") == true -> "خطای دسترسی به حافظه"
                else -> "خطا در نصب بروزرسانی: ${e.message}"
            }
            _downloadState.value = DownloadState.Error(errorMessage)
            Log.e("UpdateManager_Log", "Install error", e)
        }
    }

    private fun handleDownloadError(e: Exception) {
        val message = when (e) {
            is CancellationException -> "دانلود متوقف شد"
            is IOException -> when {
                e.message?.contains("ENOSPC") == true -> "فضای کافی در دستگاه موجود نیست"
                e.message?.contains("timeout") == true -> "زمان دانلود به پایان رسید"
                else -> "خطا در اتصال به اینترنت"
            }
            is SecurityException -> "خطا در دسترسی به حافظه دستگاه"
            else -> "خطا در دانلود: ${e.message}"
        }
        _downloadState.value = DownloadState.Error(message)
        if (::currentDownloadFile.isInitialized && downloadedBytes.get() == 0L) {
            currentDownloadFile.delete()
        }
    }

    private fun cleanupDownloadFiles() {
        try {
            val updatesDir = File(appContext.cacheDir, "updates")
            if (updatesDir.exists()) {
                updatesDir.listFiles()?.forEach { file ->
                    if (file.name != "update_${downloadTimestamp}.apk") {
                        file.delete()
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("UpdateManager_Log", "Error cleaning up download files", e)
        }
    }

    fun onCleared() {
        downloadJob?.cancel()
        cleanupDownloadFiles()
    }
}