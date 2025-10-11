package com.atk.atk_cargo.api

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
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
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import kotlin.math.pow

class UpdateManager(
    context: Context
) : ViewModel() {
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

    private var downloadTimestamp: Long = 0
    private var downloadJob: Job? = null
    private var downloadedBytes: Long = 0
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

    private suspend fun isInternetAvailable(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // First attempt: Check Google DNS
                try {
                    Socket().use { socket ->
                        socket.connect(InetSocketAddress("8.8.8.8", 53), 5000)
                        return@withContext true
                    }
                } catch (_: IOException) {
                    // If Google DNS fails, try soft98.ir
                    val url = URL("https://soft98.ir")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.connectTimeout = 5000
                    connection.connect()
                    connection.disconnect()
                    return@withContext true
                }
            } catch (_: Exception) {
                return@withContext false
            }
        }
    }

    suspend fun checkForUpdate(): Boolean {
        if (!isInternetAvailable()) {
            _downloadState.value = DownloadState.Error("لطفاً اتصال اینترنت خود را بررسی کنید")
            return false
        }

        return withContext(Dispatchers.IO) {
            try {
                val currentVersion = appContext.packageManager.getPackageInfo(appContext.packageName, 0).versionName
                val encodedVersion = URLEncoder.encode(currentVersion, "UTF-8")
                val encodedApiKey = URLEncoder.encode(Constants.API_KEY, "UTF-8")

                val request = Request.Builder()
                    .url("${Constants.BASE_URL}/check_update.php?current_version=$encodedVersion&api_key=$encodedApiKey")
                    .build()

                client.newCall(request).execute().use { response ->
                    val responseBody = response.body.string()
                    when {
                        response.code == 426 -> {
                            val error = JSONObject(responseBody).optString("error", "نسخه برنامه منسوخ شده است")
                            _downloadState.value = DownloadState.Error(error)
                            false
                        }
                        response.isSuccessful -> {
                            val jsonResponse = JSONObject(responseBody)
                            val hasUpdate = jsonResponse.getBoolean("hasUpdate")
                            if (hasUpdate) {
                                val updateInfo = jsonResponse.optJSONObject("updateInfo")
                                _updateInfo.value = UpdateInfo(
                                    latestVersion = jsonResponse.getString("latestVersion"),
                                    downloadUrl = jsonResponse.getString("downloadUrl"),
                                    changeLog = parseChangeLog(jsonResponse.getJSONObject("changeLog")
                                        .toString()),
                                    updatePriority = updateInfo?.optString("priority", "normal") ?: "normal",
                                    updateMessage = updateInfo?.optString("message", "") ?: "",
                                    forceUpdate = updateInfo?.optBoolean("forceUpdate", false) ?: false,
                                    updateSize = updateInfo?.optString("size", "0") ?: "0",
                                    releaseDate = updateInfo?.optString("releaseDate", "") ?: "",
                                    minAndroidVersion = updateInfo?.optInt("minAndroidVersion", 21) ?: 21
                                )
                            }
                            hasUpdate
                        }
                        else -> {
                            _downloadState.value = DownloadState.Error("خطا در بررسی بروزرسانی: ${response.code}")
                            false
                        }
                    }
                }
            } catch (e: Exception) {
                _downloadState.value = DownloadState.Error("خطا در بررسی بروزرسانی: ${e.localizedMessage}")
                false
            }
        }
    }

    private suspend fun parseChangeLog(jsonString: String): ChangeLogInfo {
        return withContext(Dispatchers.Default) {
            try {
                val jsonObject = JSONObject(jsonString)
                ChangeLogInfo(
                    newFeatures = jsonObject.optJSONArray("newFeatures")?.let { array ->
                        List(array.length()) { array.getString(it) }
                    } ?: emptyList(),
                    improvements = jsonObject.optJSONArray("improvements")?.let { array ->
                        List(array.length()) { array.getString(it) }
                    } ?: emptyList(),
                    fixes = jsonObject.optJSONArray("fixes")?.let { array ->
                        List(array.length()) { array.getString(it) }
                    } ?: emptyList(),
                    others = jsonObject.optJSONArray("others")?.let { array ->
                        List(array.length()) { array.getString(it) }
                    } ?: emptyList()
                )
            } catch (_: Exception) {
                ChangeLogInfo()
            }
        }
    }

    @SuppressLint("DefaultLocale")
    internal fun startDownload(downloadUrl: String, startPosition: Long = 0) {
        if (downloadJob?.isActive == true) return

        downloadJob = CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                if (!isInternetAvailable()) {
                    _downloadState.value = DownloadState.Error("لطفاً اتصال اینترنت خود را بررسی کنید")
                    return@launch
                }

                _downloadState.value = DownloadState.Downloading
                downloadTimestamp = System.currentTimeMillis()
                currentDownloadFile = File(appContext.externalCacheDir, "updates/update_${downloadTimestamp}.apk")
                currentDownloadFile.parentFile?.mkdirs()

                if (startPosition == 0L) {
                    downloadedBytes = 0
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
                var lastDownloadedBytes = downloadedBytes

                while (isActive && downloadJobs.any { it.isActive }) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastUpdateTime >= 100) {
                        val timeSpent = (currentTime - lastUpdateTime) / 1000f
                        val speed = ((downloadedBytes - lastDownloadedBytes) / timeSpent) / 1024
                        val progress = (downloadedBytes.toFloat() / totalBytes.toFloat()) * 100
                        lastProgress = progress

                        _downloadProgress.value = DownloadProgress(
                            progress = progress,
                            downloadedSize = formatFileSize(downloadedBytes),
                            totalSize = formatFileSize(totalBytes),
                            speed = String.format("%.1f", speed) to "KB/s"
                        )

                        lastUpdateTime = currentTime
                        lastDownloadedBytes = downloadedBytes
                    }
                    delay(100)
                }

                downloadJobs.awaitAll()

                if (downloadedBytes >= totalBytes) {
                    _downloadState.value = DownloadState.Completed
                }
            } catch (_: CancellationException) {
                _downloadState.value = DownloadState.Paused(
                    downloadedBytes = downloadedBytes,
                    totalBytes = totalBytes,
                    progress = lastProgress
                )
            } catch (e: Exception) {
                handleDownloadError(e)
            }
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
                            synchronized(this) {
                                downloadedBytes += bytesRead
                            }
                        }
                    }
                }
            }
        }

        while (true) {
            try {
                executeDownload()
                break
            } catch (e: Exception) {
                attempt++
                if (attempt >= maxAttempts) throw e

                val delayTime = (1000L * 2.0.pow(attempt.toDouble())).toLong()
                delay(delayTime.coerceAtMost(10_000))
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
            downloadedBytes = downloadedBytes,
            totalBytes = totalBytes,
            progress = lastProgress
        )
    }

    fun resumeDownload() {
        when (val currentState = _downloadState.value) {
            is DownloadState.Paused -> {
                // بازیابی وضعیت قبلی دانلود
                downloadedBytes = currentState.downloadedBytes
                totalBytes = currentState.totalBytes
                lastProgress = currentState.progress

                _updateInfo.value?.downloadUrl?.let { url ->
                    startDownload(url, downloadedBytes)
                }
            }
            else -> {
                _downloadState.value = DownloadState.Error("وضعیت نامعتبر برای ادامه دانلود")
            }
        }
    }

    fun cancelDownload() {
        downloadJob?.cancel()
        downloadedBytes = 0
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
            File(appContext.externalCacheDir, "updates/update_${downloadTimestamp}.apk")
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
            } else {
                throw Exception("برنامه‌ای برای نصب فایل APK یافت نشد")
            }
        } catch (e: Exception) {
            val errorMessage = when {
                e is IOException && e.message?.contains("ENOSPC") == true -> "فضای کافی در دستگاه موجود نیست"
                e.message?.contains("Permission denied") == true -> "خطای دسترسی به حافظه"
                else -> "خطا در نصب بروزرسانی: ${e.message}"
            }
            _downloadState.value = DownloadState.Error(errorMessage)
            Log.e("UpdateManager", "Install error", e)
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
        if (::currentDownloadFile.isInitialized && downloadedBytes == 0L) {
            currentDownloadFile.delete()
        }
    }

    private fun cleanupDownloadFiles() {
        try {
            val updatesDir = File(appContext.externalCacheDir, "updates")
            if (updatesDir.exists()) {
                updatesDir.listFiles()?.forEach { file ->
                    if (file.name != "update_${downloadTimestamp}.apk") {
                        file.delete()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("UpdateManager", "Error cleaning up download files", e)
        }
    }

    public override fun onCleared() {
        super.onCleared()
        downloadJob?.cancel()
        cleanupDownloadFiles()
    }
}