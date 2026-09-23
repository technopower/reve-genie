package com.example.model

sealed interface StorageResult<out T> {
    data class Success<out T>(val data: T) : StorageResult<T>
    data class Error(val message: String, val cause: Throwable? = null) : StorageResult<Nothing>
    data object Loading : StorageResult<Nothing>
}

sealed interface PdfUploadState {
    data object Idle : PdfUploadState
    data class InProgress(
        val bytesTransferred: Long,
        val totalBytes: Long,
        val progressFraction: Float
    ) : PdfUploadState
    data class Success(
        val item: FirebasePdfItem,
        val downloadUrl: String
    ) : PdfUploadState
    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : PdfUploadState
}

data class FirebasePdfItem(
    val id: String,
    val name: String,
    val storagePath: String,
    val downloadUrl: String? = null,
    val sizeBytes: Long = 0L,
    val contentType: String? = "application/pdf",
    val timeCreatedMillis: Long = System.currentTimeMillis(),
    val updatedMillis: Long = System.currentTimeMillis(),
    val subject: String = "General",
    val educationLevel: String = "",
    val customMetadata: Map<String, String> = emptyMap()
) {
    fun toPdfDocument(pageCount: Int = 1): PdfDocument {
        val dateFormat = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
        val dateStr = try {
            dateFormat.format(java.util.Date(timeCreatedMillis))
        } catch (_: Exception) {
            "Recent"
        }
        return PdfDocument(
            id = id,
            fileName = name,
            pages = pageCount,
            subject = subject,
            uploadDate = dateStr
        )
    }
}
