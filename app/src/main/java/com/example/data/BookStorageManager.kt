package com.example.data

import android.content.Context
import android.util.Log
import com.example.model.Book
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

sealed interface BookDownloadState {
    data object Idle : BookDownloadState
    data class Progress(
        val progressFraction: Float,
        val downloadedBytes: Long,
        val totalBytes: Long
    ) : BookDownloadState
    data class Completed(val file: File) : BookDownloadState
    data class Error(val message: String, val throwable: Throwable? = null) : BookDownloadState
}

object BookStorageManager {

    private const val TAG = "BookStorageManager"
    private const val TEXTBOOK_FOLDER = "downloaded_textbooks"

    private object SafeLog {
        fun i(tag: String, msg: String) {
            try {
                android.util.Log.i(tag, msg)
            } catch (_: Throwable) {
                println("[$tag] $msg")
            }
        }

        fun d(tag: String, msg: String) {
            try {
                android.util.Log.d(tag, msg)
            } catch (_: Throwable) {
                println("[$tag] $msg")
            }
        }

        fun w(tag: String, msg: String, tr: Throwable? = null) {
            try {
                android.util.Log.w(tag, msg, tr)
            } catch (_: Throwable) {
                println("[$tag] WARN: $msg ${tr?.message ?: ""}")
            }
        }

        fun e(tag: String, msg: String, tr: Throwable? = null) {
            try {
                android.util.Log.e(tag, msg, tr)
            } catch (_: Throwable) {
                println("[$tag] ERROR: $msg ${tr?.message ?: ""}")
            }
        }
    }

    fun getCacheKey(book: Book): String {
        val safeId = book.id.replace("[^a-zA-Z0-9_]".toRegex(), "_")
        val safeEdition = book.edition.replace("[^a-zA-Z0-9]".toRegex(), "_").trim('_').lowercase()
        return if (safeEdition.isNotBlank()) "${safeId}_$safeEdition" else safeId
    }

    fun getLocalFile(context: Context, book: Book): File {
        val folder = File(context.filesDir, TEXTBOOK_FOLDER)
        if (!folder.exists()) {
            folder.mkdirs()
        }
        val safeKey = getCacheKey(book)
        val editionFile = File(folder, "$safeKey.pdf")
        if (editionFile.exists()) {
            return editionFile
        }

        // Backward compatibility: If an existing valid file was stored under book.id only
        val legacyId = book.id.replace("[^a-zA-Z0-9_]".toRegex(), "_")
        val legacyFile = File(folder, "$legacyId.pdf")
        if (legacyFile.exists() && isValidPdfFile(legacyFile)) {
            return legacyFile
        }

        return editionFile
    }

    /**
     * Verifies that a file exists and begins with the valid PDF magic header (%PDF).
     * Prevents marking 404 HTML responses or truncated bytes as offline-ready.
     */
    fun isValidPdfFile(file: File): Boolean {
        if (!file.exists() || file.length() < 32) return false
        return try {
            file.inputStream().use { input ->
                val header = ByteArray(4)
                val bytesRead = input.read(header)
                bytesRead == 4 &&
                        header[0] == 0x25.toByte() && // %
                        header[1] == 0x50.toByte() && // P
                        header[2] == 0x44.toByte() && // D
                        header[3] == 0x46.toByte()    // F
            }
        } catch (e: Exception) {
            SafeLog.w(TAG, "Failed to read header of file: ${file.absolutePath}", e)
            false
        }
    }

    fun isBookDownloaded(context: Context, book: Book): Boolean {
        val file = getLocalFile(context, book)
        if (!file.exists()) return false
        if (!isValidPdfFile(file)) {
            SafeLog.w(TAG, "File for ${book.id} (${book.edition}) failed PDF validation (%PDF- missing or corrupt). Deleting invalid file.")
            try { file.delete() } catch (_: Exception) {}
            return false
        }
        return true
    }

    fun getCachedFileOrNull(context: Context, book: Book): File? {
        val file = getLocalFile(context, book)
        return if (isValidPdfFile(file)) file else null
    }

    fun getDownloadedBookSizeBytes(context: Context, book: Book): Long {
        val file = getLocalFile(context, book)
        return if (isValidPdfFile(file)) file.length() else 0L
    }

    fun deleteDownloadedBook(context: Context, book: Book): Boolean {
        val file = getLocalFile(context, book)
        var deleted = if (file.exists()) file.delete() else false
        val folder = File(context.filesDir, TEXTBOOK_FOLDER)
        val legacyId = book.id.replace("[^a-zA-Z0-9_]".toRegex(), "_")
        val legacyFile = File(folder, "$legacyId.pdf")
        if (legacyFile.exists()) {
            deleted = legacyFile.delete() || deleted
        }
        return deleted
    }

    fun downloadBookFlow(context: Context, book: Book): Flow<BookDownloadState> = flow {
        if (book.pdfUrl.isBlank() || book.r2Key.isBlank()) {
            SafeLog.w(TAG, "PDF URL is missing for book ${book.id}")
            emit(BookDownloadState.Error("PDF unavailable for ${book.title} (${book.edition})."))
            return@flow
        }

        val finalUrl = R2StorageConfig.getCanonicalUrl(book)
        val r2Key = book.r2Key
        val fileName = book.fileName
        val academicLevel = book.className
        val classLevel = book.className
        val medium = book.medium
        val subject = book.subjectId

        SafeLog.i(
            "PDF_FLOW",
            "bookId=${book.id}\n" +
            "title=${book.title}\n" +
            "academicLevel=$academicLevel\n" +
            "classLevel=$classLevel\n" +
            "medium=$medium\n" +
            "subject=$subject\n" +
            "fileName=$fileName\n" +
            "r2Key=$r2Key\n" +
            "finalUrl=$finalUrl"
        )
        SafeLog.i(TAG, "Trace: Starting download flow for Book id='${book.id}', title='${book.title}', class='$classLevel', subject='$subject', finalUrl='$finalUrl'")
        emit(BookDownloadState.Progress(0f, 0L, book.fileSizeBytes))

        val targetFile = getLocalFile(context, book)
        if (isValidPdfFile(targetFile)) {
            SafeLog.i(TAG, "Book ${book.id} is already cached and verified (%PDF valid) at ${targetFile.absolutePath}")
            emit(BookDownloadState.Completed(targetFile))
            return@flow
        } else if (targetFile.exists()) {
            // Delete invalid or corrupted cached file
            try { targetFile.delete() } catch (_: Exception) {}
        }

        val tempFile = File(targetFile.parentFile, "${targetFile.name}.tmp")
        if (tempFile.exists()) {
            try { tempFile.delete() } catch (_: Exception) {}
        }

        var downloadSucceeded = false
        var lastErrorMessage = "PDF unavailable for ${book.title} (${book.edition})."

        SafeLog.i(TAG, "Attempting connection to book PDF URL for ${book.id}: $finalUrl")
        var connection: HttpURLConnection? = null
        var inputStream: InputStream? = null
        var responseCode = -1

        try {
            val url = URL(finalUrl)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15000
                readTimeout = 60000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "ReveGenieAiTeacher/1.0 (Android; Linux)")
                setRequestProperty("Accept", "application/pdf,*/*")
            }
            connection.connect()

            responseCode = connection.responseCode
            val contentType = connection.contentType ?: "unknown"
            val contentLength = connection.contentLengthLong.let { if (it > 0) it else book.fileSizeBytes }
            val locationHeader = connection.getHeaderField("Location") ?: "none"

            SafeLog.i(
                "PDF_HTTP",
                "url=$finalUrl\n" +
                "responseCode=$responseCode\n" +
                "contentType=$contentType\n" +
                "contentLength=$contentLength\n" +
                "location=$locationHeader"
            )
            SafeLog.i(TAG, "HTTP Response code $responseCode received from $finalUrl for ${book.id}")

            if (responseCode in 200..299) {
                inputStream = connection.inputStream

                FileOutputStream(tempFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalRead = 0L

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalRead += bytesRead
                        val fraction = if (contentLength > 0) (totalRead.toFloat() / contentLength.toFloat()).coerceIn(0f, 1f) else 0.5f
                        emit(BookDownloadState.Progress(fraction, totalRead, contentLength))
                    }
                }

                // Strict verification: check for %PDF magic header
                if (isValidPdfFile(tempFile)) {
                    if (tempFile.renameTo(targetFile)) {
                        downloadSucceeded = true
                        SafeLog.i(TAG, "Download and verification SUCCESSFUL for ${book.id}. Saved to ${targetFile.absolutePath} (${targetFile.length()} bytes)")
                        emit(BookDownloadState.Completed(targetFile))
                    } else {
                        lastErrorMessage = "Storage Error: Failed to save ${book.title} locally."
                    }
                } else {
                    SafeLog.w(TAG, "Downloaded file from $finalUrl is not a valid PDF (%PDF header missing). Response was likely HTML error.")
                    lastErrorMessage = "Resource Error: ${book.title} is currently unavailable in the library (Invalid Format)."
                    if (tempFile.exists()) tempFile.delete()
                }
            } else {
                lastErrorMessage = when (responseCode) {
                    404 -> "Not Found: ${book.title} is not yet available in the 2026 digital library."
                    401, 403 -> "Access Denied: Authentication required for ${book.title}."
                    500, 502, 503, 504 -> "Server Error: The library server is currently overloaded. Please try again later."
                    else -> "Network Error: Received HTTP $responseCode while accessing ${book.title}."
                }
                SafeLog.w(TAG, "Server returned HTTP $responseCode for $finalUrl")
            }
        } catch (e: Exception) {
            SafeLog.e(TAG, "Exception downloading from $finalUrl for ${book.id}: ${e.message}", e)
            SafeLog.e("PDF_DOWNLOAD", "exception class=${e.javaClass.name}\nexception message=${e.message}\nHTTP response code=$responseCode\nfinal URL=$finalUrl", e)
            lastErrorMessage = "PDF unavailable for ${book.title} (${book.edition}): ${e.localizedMessage ?: e.message}"
        } finally {
            try { inputStream?.close() } catch (_: Exception) {}
            try { connection?.disconnect() } catch (_: Exception) {}
            if (tempFile.exists() && !targetFile.exists()) {
                tempFile.delete()
            }
        }

        if (!downloadSucceeded) {
            emit(BookDownloadState.Error(lastErrorMessage))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun ensureBookAvailable(context: Context, book: Book, forceDownload: Boolean = false): File = withContext(Dispatchers.IO) {
        if (book.pdfUrl.isBlank() || book.r2Key.isBlank()) {
            throw IllegalStateException("PDF unavailable for ${book.title} (${book.edition}).")
        }

        val finalUrl = R2StorageConfig.getCanonicalUrl(book)
        val r2Key = book.r2Key
        val fileName = book.fileName
        val academicLevel = book.className
        val classLevel = book.className
        val medium = book.medium
        val subject = book.subjectId

        SafeLog.i(
            "PDF_FLOW",
            "bookId=${book.id}\n" +
            "title=${book.title}\n" +
            "academicLevel=$academicLevel\n" +
            "classLevel=$classLevel\n" +
            "medium=$medium\n" +
            "subject=$subject\n" +
            "fileName=$fileName\n" +
            "r2Key=$r2Key\n" +
            "finalUrl=$finalUrl"
        )
        SafeLog.i(TAG, "Trace: ensureBookAvailable for Book id='${book.id}', title='${book.title}', class='$classLevel', subject='$subject', finalUrl='$finalUrl', forceDownload=$forceDownload")
        
        val targetFile = getLocalFile(context, book)
        if (!forceDownload && isValidPdfFile(targetFile)) {
            SafeLog.i(TAG, "Using existing validated cached PDF for ${book.id} (${targetFile.length()} bytes)")
            return@withContext targetFile
        }

        if (targetFile.exists()) {
            targetFile.delete()
        }

        val tempFile = File(targetFile.parentFile, "${targetFile.name}.tmp")
        if (tempFile.exists()) {
            try { tempFile.delete() } catch (_: Exception) {}
        }

        var responseCode = -1
        var connection: HttpURLConnection? = null

        try {
            SafeLog.i(TAG, "ensureBookAvailable connecting to $finalUrl for ${book.id}")
            val url = URL(finalUrl)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15000
                readTimeout = 60000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "ReveGenieAiTeacher/1.0 (Android; Linux)")
                setRequestProperty("Accept", "application/pdf,*/*")
            }
            connection.connect()

            responseCode = connection.responseCode
            val contentType = connection.contentType ?: "unknown"
            val contentLength = connection.contentLengthLong.let { if (it > 0) it else book.fileSizeBytes }
            val locationHeader = connection.getHeaderField("Location") ?: "none"

            SafeLog.i(
                "PDF_HTTP",
                "url=$finalUrl\n" +
                "responseCode=$responseCode\n" +
                "contentType=$contentType\n" +
                "contentLength=$contentLength\n" +
                "location=$locationHeader"
            )
            SafeLog.i(TAG, "ensureBookAvailable HTTP $responseCode from $finalUrl")

            if (responseCode in 200..299) {
                connection.inputStream.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }

                if (isValidPdfFile(tempFile) && tempFile.renameTo(targetFile)) {
                    SafeLog.i(TAG, "ensureBookAvailable SUCCESS: downloaded and verified ${book.id} (${targetFile.length()} bytes)")
                    return@withContext targetFile
                } else {
                    SafeLog.w(TAG, "ensureBookAvailable: Downloaded file from $finalUrl failed PDF magic bytes check.")
                    if (tempFile.exists()) tempFile.delete()
                    throw IllegalStateException("Resource Error: ${book.title} is currently unavailable in the library (Invalid Format).")
                }
            } else {
                SafeLog.w(TAG, "ensureBookAvailable: Received HTTP $responseCode from $finalUrl")
                val msg = when (responseCode) {
                    404 -> "Not Found: ${book.title} is not yet available in the 2026 digital library."
                    401, 403 -> "Access Denied: Authentication required for ${book.title}."
                    500, 502, 503, 504 -> "Server Error: The library server is currently overloaded. Please try again later."
                    else -> "Network Error: Received HTTP $responseCode while accessing ${book.title}."
                }
                throw IllegalStateException(msg)
            }
        } catch (e: Exception) {
            SafeLog.e(TAG, "ensureBookAvailable error connecting to $finalUrl: ${e.message}", e)
            SafeLog.e("PDF_VIEW", "exception class=${e.javaClass.name}\nexception message=${e.message}\nHTTP response code=$responseCode\nfinal URL=$finalUrl", e)
            if (tempFile.exists()) {
                tempFile.delete()
            }
            throw IllegalStateException("PDF unavailable for ${book.title} (${book.edition}): ${e.localizedMessage ?: e.message}", e)
        } finally {
            try { connection?.disconnect() } catch (_: Exception) {}
        }
    }
}
