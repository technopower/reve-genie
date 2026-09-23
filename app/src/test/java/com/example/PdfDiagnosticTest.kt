package com.example

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.test.core.app.ApplicationProvider
import com.example.data.BookCatalog
import com.example.data.BookDownloadState
import com.example.data.BookStorageManager
import com.example.data.R2StorageConfig
import com.example.model.Book
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PdfDiagnosticTest {

    private val context: Context by lazy {
        ApplicationProvider.getApplicationContext<Context>()
    }

    private fun runDiagnosticForBook(label: String, bookId: String) {
        println("==================================================")
        println("DIAGNOSTIC TEST FOR: $label (bookId=$bookId)")
        println("==================================================")

        val book: Book? = BookCatalog.getBookById(bookId)
        if (book == null) {
            println("ERROR: Book not found in catalog for id: $bookId")
            return
        }

        val canonicalUrl = R2StorageConfig.getCanonicalUrl(book)
        val r2Key = book.r2Key
        val fileName = book.fileName

        println("bookId: ${book.id}")
        println("title: ${book.title}")
        println("class: ${book.className}")
        println("medium: ${book.medium}")
        println("fileName: $fileName")
        println("r2Key: $r2Key")
        println("finalUrl: $canonicalUrl")

        // 1. Direct HTTP connection check
        var httpStatus = -1
        var contentType = "none"
        var contentLength = -1L
        var headerBytesHex = "none"
        var headerAscii = "none"

        try {
            val url = URL(canonicalUrl)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15000
                readTimeout = 30000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "ReveGenieAiTeacher/1.0 (Android; Linux)")
                setRequestProperty("Accept", "application/pdf,*/*")
            }
            conn.connect()
            httpStatus = conn.responseCode
            contentType = conn.contentType ?: "none"
            contentLength = conn.contentLengthLong

            if (httpStatus in 200..299) {
                conn.inputStream.use { stream ->
                    val header = ByteArray(8)
                    val read = stream.read(header)
                    if (read > 0) {
                        headerBytesHex = header.take(read).joinToString(" ") { String.format("%02X", it) }
                        headerAscii = String(header, 0, read)
                    }
                }
            }
            conn.disconnect()
        } catch (e: Exception) {
            println("HTTP Connection Exception: ${e.javaClass.name}: ${e.message}")
        }

        println("HTTP status: $httpStatus")
        println("Content-Type: $contentType")
        println("Content-Length: $contentLength")
        println("PDF first 4/8 bytes hex: $headerBytesHex")
        println("PDF first 4/8 bytes ascii: $headerAscii")

        // 2. View PDF flow (ensureBookAvailable + PdfRenderer)
        var viewResult = "FAILED"
        var downloadedFilePath = "none"
        var downloadedFileSize = -1L
        try {
            runBlocking {
                val file = BookStorageManager.ensureBookAvailable(context, book, forceDownload = true)
                downloadedFilePath = file.absolutePath
                downloadedFileSize = file.length()
                println("FILE_CHECK: exists=${file.exists()} size=${file.length()} readable=${file.canRead()} path=${file.absolutePath}")
                
                // Test ParcelFileDescriptor + PdfRenderer
                var pfd: ParcelFileDescriptor? = null
                var renderer: PdfRenderer? = null
                try {
                    pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                    renderer = PdfRenderer(pfd)
                    val pageCount = renderer.pageCount
                    println("PdfRenderer SUCCESS: pageCount=$pageCount")
                    viewResult = "SUCCESS (pages=$pageCount)"
                } finally {
                    try { renderer?.close() } catch (_: Exception) {}
                    try { pfd?.close() } catch (_: Exception) {}
                }
            }
        } catch (e: Exception) {
            println("VIEW PDF EXCEPTION: ${e.javaClass.name}: ${e.message}")
            e.printStackTrace()
            viewResult = "FAILED: ${e.javaClass.name} - ${e.message}"
        }

        println("downloaded file path: $downloadedFilePath")
        println("downloaded file size: $downloadedFileSize")
        println("View result: $viewResult")

        // 3. Download flow
        var downloadResult = "FAILED"
        try {
            runBlocking {
                val states = BookStorageManager.downloadBookFlow(context, book).toList()
                val lastState = states.lastOrNull()
                println("DOWNLOAD STATES: ${states.map { it.javaClass.simpleName }}")
                when (lastState) {
                    is BookDownloadState.Completed -> {
                        println("DOWNLOAD COMPLETED: file=${lastState.file.absolutePath} size=${lastState.file.length()}")
                        downloadResult = "SUCCESS (size=${lastState.file.length()})"
                    }
                    is BookDownloadState.Error -> {
                        println("DOWNLOAD ERROR: ${lastState.message}")
                        downloadResult = "FAILED: ${lastState.message}"
                    }
                    else -> {
                        downloadResult = "FAILED: unexpected last state $lastState"
                    }
                }
            }
        } catch (e: Exception) {
            println("DOWNLOAD EXCEPTION: ${e.javaClass.name}: ${e.message}")
            e.printStackTrace()
            downloadResult = "FAILED: ${e.javaClass.name} - ${e.message}"
        }

        println("Download result: $downloadResult")
        println("==================================================\n")
    }

    @Test
    fun testClass7_Working() {
        runDiagnosticForBook("WORKING CLASS 7", "c7_english_bv")
    }

    @Test
    fun testClass8_Failing() {
        runDiagnosticForBook("FAILING CLASS 8", "c8_math_bv")
    }

    @Test
    fun testSsc_Failing() {
        runDiagnosticForBook("FAILING SSC", "ssc_physics_bv")
    }

    @Test
    fun testHsc_Failing() {
        runDiagnosticForBook("FAILING HSC", "hsc_physics_1_bv")
    }
}
