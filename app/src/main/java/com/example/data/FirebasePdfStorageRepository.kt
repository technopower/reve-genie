package com.example.data

import android.net.Uri
import com.example.model.FirebasePdfItem
import com.example.model.PdfUploadState
import com.example.model.StorageResult
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

interface PdfStorageService {
    fun uploadPdf(
        fileUri: Uri,
        fileName: String,
        folderPath: String = "textbooks",
        subject: String = "General",
        educationLevel: String = "",
        customMetadata: Map<String, String> = emptyMap()
    ): Flow<PdfUploadState>

    fun uploadPdfBytes(
        bytes: ByteArray,
        fileName: String,
        folderPath: String = "textbooks",
        subject: String = "General",
        educationLevel: String = "",
        customMetadata: Map<String, String> = emptyMap()
    ): Flow<PdfUploadState>

    suspend fun getDownloadUrl(storagePath: String): StorageResult<String>

    suspend fun listPdfFiles(folderPath: String = "textbooks"): StorageResult<List<FirebasePdfItem>>

    suspend fun deletePdf(storagePath: String): StorageResult<Unit>

    suspend fun getPdfMetadata(storagePath: String): StorageResult<FirebasePdfItem>
}

class FirebasePdfStorageRepository(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) : PdfStorageService {

    companion object {
        const val DEFAULT_FOLDER = "textbooks"
        private const val MIME_TYPE_PDF = "application/pdf"

        @Volatile
        private var instance: FirebasePdfStorageRepository? = null

        fun getInstance(): FirebasePdfStorageRepository {
            return instance ?: synchronized(this) {
                instance ?: FirebasePdfStorageRepository().also { instance = it }
            }
        }
    }

    override fun uploadPdf(
        fileUri: Uri,
        fileName: String,
        folderPath: String,
        subject: String,
        educationLevel: String,
        customMetadata: Map<String, String>
    ): Flow<PdfUploadState> = callbackFlow {
        trySend(PdfUploadState.InProgress(0L, 0L, 0f))

        val sanitizedFileName = sanitizeFileName(fileName)
        val path = buildStoragePath(folderPath, sanitizedFileName)
        val fileRef = storage.reference.child(path)

        val metadataBuilder = StorageMetadata.Builder()
            .setContentType(MIME_TYPE_PDF)
            .setCustomMetadata("subject", subject)
            .setCustomMetadata("educationLevel", educationLevel)
            .setCustomMetadata("originalName", fileName)

        customMetadata.forEach { (key, value) ->
            metadataBuilder.setCustomMetadata(key, value)
        }

        val uploadTask = fileRef.putFile(fileUri, metadataBuilder.build())

        uploadTask.addOnProgressListener { taskSnapshot ->
            val total = taskSnapshot.totalByteCount
            val transferred = taskSnapshot.bytesTransferred
            val fraction = if (total > 0) transferred.toFloat() / total.toFloat() else 0f
            trySend(PdfUploadState.InProgress(transferred, total, fraction))
        }

        uploadTask.addOnSuccessListener { snapshot ->
            // Retrieve download URL
            fileRef.downloadUrl.addOnSuccessListener { uri ->
                val meta = snapshot.metadata
                val pdfItem = FirebasePdfItem(
                    id = fileRef.name,
                    name = meta?.getCustomMetadata("originalName") ?: fileRef.name,
                    storagePath = fileRef.path,
                    downloadUrl = uri.toString(),
                    sizeBytes = meta?.sizeBytes ?: snapshot.totalByteCount,
                    contentType = meta?.contentType ?: MIME_TYPE_PDF,
                    timeCreatedMillis = meta?.creationTimeMillis ?: System.currentTimeMillis(),
                    updatedMillis = meta?.updatedTimeMillis ?: System.currentTimeMillis(),
                    subject = meta?.getCustomMetadata("subject") ?: subject,
                    educationLevel = meta?.getCustomMetadata("educationLevel") ?: educationLevel,
                    customMetadata = meta?.customMetadataKeys?.associateWith { k -> meta.getCustomMetadata(k).orEmpty() } ?: emptyMap()
                )
                trySend(PdfUploadState.Success(pdfItem, uri.toString()))
                close()
            }.addOnFailureListener { error ->
                trySend(PdfUploadState.Error("Upload finished but failed to retrieve download URL: ${error.message}", error))
                close(error)
            }
        }

        uploadTask.addOnFailureListener { exception ->
            trySend(PdfUploadState.Error(exception.localizedMessage ?: "PDF Upload failed", exception))
            close(exception)
        }

        awaitClose {
            if (uploadTask.isInProgress) {
                uploadTask.cancel()
            }
        }
    }

    override fun uploadPdfBytes(
        bytes: ByteArray,
        fileName: String,
        folderPath: String,
        subject: String,
        educationLevel: String,
        customMetadata: Map<String, String>
    ): Flow<PdfUploadState> = callbackFlow {
        trySend(PdfUploadState.InProgress(0L, bytes.size.toLong(), 0f))

        val sanitizedFileName = sanitizeFileName(fileName)
        val path = buildStoragePath(folderPath, sanitizedFileName)
        val fileRef = storage.reference.child(path)

        val metadataBuilder = StorageMetadata.Builder()
            .setContentType(MIME_TYPE_PDF)
            .setCustomMetadata("subject", subject)
            .setCustomMetadata("educationLevel", educationLevel)
            .setCustomMetadata("originalName", fileName)

        customMetadata.forEach { (key, value) ->
            metadataBuilder.setCustomMetadata(key, value)
        }

        val uploadTask = fileRef.putBytes(bytes, metadataBuilder.build())

        uploadTask.addOnProgressListener { taskSnapshot ->
            val total = taskSnapshot.totalByteCount
            val transferred = taskSnapshot.bytesTransferred
            val fraction = if (total > 0) transferred.toFloat() / total.toFloat() else 0f
            trySend(PdfUploadState.InProgress(transferred, total, fraction))
        }

        uploadTask.addOnSuccessListener { snapshot ->
            fileRef.downloadUrl.addOnSuccessListener { uri ->
                val meta = snapshot.metadata
                val pdfItem = FirebasePdfItem(
                    id = fileRef.name,
                    name = meta?.getCustomMetadata("originalName") ?: fileRef.name,
                    storagePath = fileRef.path,
                    downloadUrl = uri.toString(),
                    sizeBytes = meta?.sizeBytes ?: bytes.size.toLong(),
                    contentType = meta?.contentType ?: MIME_TYPE_PDF,
                    timeCreatedMillis = meta?.creationTimeMillis ?: System.currentTimeMillis(),
                    updatedMillis = meta?.updatedTimeMillis ?: System.currentTimeMillis(),
                    subject = meta?.getCustomMetadata("subject") ?: subject,
                    educationLevel = meta?.getCustomMetadata("educationLevel") ?: educationLevel,
                    customMetadata = meta?.customMetadataKeys?.associateWith { k -> meta.getCustomMetadata(k).orEmpty() } ?: emptyMap()
                )
                trySend(PdfUploadState.Success(pdfItem, uri.toString()))
                close()
            }.addOnFailureListener { error ->
                trySend(PdfUploadState.Error("Upload succeeded but failed to retrieve URL: ${error.message}", error))
                close(error)
            }
        }

        uploadTask.addOnFailureListener { exception ->
            trySend(PdfUploadState.Error(exception.localizedMessage ?: "Byte upload failed", exception))
            close(exception)
        }

        awaitClose {
            if (uploadTask.isInProgress) {
                uploadTask.cancel()
            }
        }
    }

    override suspend fun getDownloadUrl(storagePath: String): StorageResult<String> {
        return try {
            val ref = resolveReference(storagePath)
            val uri = ref.downloadUrl.awaitTask()
            StorageResult.Success(uri.toString())
        } catch (e: Exception) {
            StorageResult.Error("Failed to get download URL for '$storagePath': ${e.message}", e)
        }
    }

    override suspend fun listPdfFiles(folderPath: String): StorageResult<List<FirebasePdfItem>> {
        return try {
            val folderRef = storage.reference.child(folderPath.trim('/'))
            val listResult = folderRef.listAll().awaitTask()
            val pdfList = mutableListOf<FirebasePdfItem>()

            for (itemRef in listResult.items) {
                val item = try {
                    val metadata = itemRef.metadata.awaitTask()
                    val downloadUri = try {
                        itemRef.downloadUrl.awaitTask().toString()
                    } catch (_: Exception) {
                        null
                    }

                    FirebasePdfItem(
                        id = itemRef.name,
                        name = metadata.getCustomMetadata("originalName") ?: itemRef.name,
                        storagePath = itemRef.path,
                        downloadUrl = downloadUri,
                        sizeBytes = metadata.sizeBytes,
                        contentType = metadata.contentType ?: MIME_TYPE_PDF,
                        timeCreatedMillis = metadata.creationTimeMillis,
                        updatedMillis = metadata.updatedTimeMillis,
                        subject = metadata.getCustomMetadata("subject") ?: "General",
                        educationLevel = metadata.getCustomMetadata("educationLevel") ?: "",
                        customMetadata = metadata.customMetadataKeys.associateWith { k ->
                            metadata.getCustomMetadata(k).orEmpty()
                        }
                    )
                } catch (_: Exception) {
                    FirebasePdfItem(
                        id = itemRef.name,
                        name = itemRef.name,
                        storagePath = itemRef.path,
                        downloadUrl = null
                    )
                }
                pdfList.add(item)
            }

            // Sort by updated/created time descending (newest first)
            val sortedList = pdfList.sortedByDescending { it.updatedMillis }
            StorageResult.Success(sortedList)
        } catch (e: Exception) {
            StorageResult.Error("Failed to list PDF files in '$folderPath': ${e.message}", e)
        }
    }

    override suspend fun deletePdf(storagePath: String): StorageResult<Unit> {
        return try {
            val ref = resolveReference(storagePath)
            ref.delete().awaitTask()
            StorageResult.Success(Unit)
        } catch (e: Exception) {
            StorageResult.Error("Failed to delete PDF at '$storagePath': ${e.message}", e)
        }
    }

    override suspend fun getPdfMetadata(storagePath: String): StorageResult<FirebasePdfItem> {
        return try {
            val ref = resolveReference(storagePath)
            val meta = ref.metadata.awaitTask()
            val downloadUri = try {
                ref.downloadUrl.awaitTask().toString()
            } catch (_: Exception) {
                null
            }

            val item = FirebasePdfItem(
                id = ref.name,
                name = meta.getCustomMetadata("originalName") ?: ref.name,
                storagePath = ref.path,
                downloadUrl = downloadUri,
                sizeBytes = meta.sizeBytes,
                contentType = meta.contentType ?: MIME_TYPE_PDF,
                timeCreatedMillis = meta.creationTimeMillis,
                updatedMillis = meta.updatedTimeMillis,
                subject = meta.getCustomMetadata("subject") ?: "General",
                educationLevel = meta.getCustomMetadata("educationLevel") ?: "",
                customMetadata = meta.customMetadataKeys.associateWith { k ->
                    meta.getCustomMetadata(k).orEmpty()
                }
            )
            StorageResult.Success(item)
        } catch (e: Exception) {
            StorageResult.Error("Failed to fetch metadata for '$storagePath': ${e.message}", e)
        }
    }

    private fun resolveReference(pathOrUrl: String): StorageReference {
        return if (pathOrUrl.startsWith("gs://") || pathOrUrl.startsWith("https://")) {
            storage.getReferenceFromUrl(pathOrUrl)
        } else {
            storage.reference.child(pathOrUrl.trim('/'))
        }
    }

    private fun buildStoragePath(folder: String, fileName: String): String {
        val cleanFolder = folder.trim('/')
        return if (cleanFolder.isEmpty()) fileName else "$cleanFolder/$fileName"
    }

    private fun sanitizeFileName(rawName: String): String {
        val base = rawName.trim().replace("\\s+".toRegex(), "_")
        return if (base.endsWith(".pdf", ignoreCase = true)) base else "$base.pdf"
    }
}

private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { result ->
        if (cont.isActive) cont.resume(result)
    }
    addOnFailureListener { exception ->
        if (cont.isActive) cont.resumeWithException(exception)
    }
    addOnCanceledListener {
        if (cont.isActive) cont.cancel()
    }
}
