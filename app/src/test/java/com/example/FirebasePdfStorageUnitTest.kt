package com.example

import com.example.model.*
import org.junit.Assert.*
import org.junit.Test

class FirebasePdfStorageUnitTest {

    @Test
    fun testFirebasePdfItem_toPdfDocument_mapping() {
        val item = FirebasePdfItem(
            id = "test_physics_ch1.pdf",
            name = "Physics Chapter 1 Vectors.pdf",
            storagePath = "textbooks/test_physics_ch1.pdf",
            downloadUrl = "https://firebasestorage.googleapis.com/v0/b/test/o/textbooks%2Ftest_physics_ch1.pdf",
            sizeBytes = 2048000L,
            contentType = "application/pdf",
            timeCreatedMillis = 1700000000000L,
            updatedMillis = 1700000000000L,
            subject = "Physics",
            educationLevel = "Class 10 (SSC)",
            customMetadata = mapOf("chapter" to "Vectors", "pages" to "42")
        )

        val doc = item.toPdfDocument(pageCount = 42)
        assertEquals("test_physics_ch1.pdf", doc.id)
        assertEquals("Physics Chapter 1 Vectors.pdf", doc.fileName)
        assertEquals(42, doc.pages)
        assertEquals("Physics", doc.subject)
        assertNotNull(doc.uploadDate)
    }

    @Test
    fun testStorageResult_stateHandling() {
        val success: StorageResult<String> = StorageResult.Success("https://example.com/pdf/sample.pdf")
        assertTrue(success is StorageResult.Success)
        assertEquals("https://example.com/pdf/sample.pdf", (success as StorageResult.Success).data)

        val error: StorageResult<String> = StorageResult.Error("Network error", RuntimeException("No connection"))
        assertTrue(error is StorageResult.Error)
        assertEquals("Network error", (error as StorageResult.Error).message)

        val loading: StorageResult<String> = StorageResult.Loading
        assertTrue(loading is StorageResult.Loading)
    }

    @Test
    fun testPdfUploadState_transitions() {
        val idle: PdfUploadState = PdfUploadState.Idle
        assertEquals(PdfUploadState.Idle, idle)

        val inProgress: PdfUploadState = PdfUploadState.InProgress(
            bytesTransferred = 512L,
            totalBytes = 1024L,
            progressFraction = 0.5f
        )
        assertTrue(inProgress is PdfUploadState.InProgress)
        assertEquals(0.5f, (inProgress as PdfUploadState.InProgress).progressFraction)

        val item = FirebasePdfItem(
            id = "doc_1.pdf",
            name = "Chemistry_Paper_1.pdf",
            storagePath = "textbooks/doc_1.pdf",
            downloadUrl = "https://storage.url/doc_1.pdf"
        )
        val success: PdfUploadState = PdfUploadState.Success(item, "https://storage.url/doc_1.pdf")
        assertTrue(success is PdfUploadState.Success)
        assertEquals("https://storage.url/doc_1.pdf", (success as PdfUploadState.Success).downloadUrl)

        val failure: PdfUploadState = PdfUploadState.Error("Upload timed out")
        assertTrue(failure is PdfUploadState.Error)
        assertEquals("Upload timed out", (failure as PdfUploadState.Error).message)
    }
}
