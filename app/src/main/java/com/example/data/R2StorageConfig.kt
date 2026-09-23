package com.example.data

import com.example.model.Book
import com.example.model.EducationLevel
import com.example.model.Medium
import com.example.model.R2PdfBook
import java.net.URLEncoder

/**
 * Cloudflare R2 Object Storage configuration and safe public PDF URL builder.
 *
 * All textbook PDFs are hosted on Cloudflare R2:
 * Bucket: reve-genie-books
 * Base URL: https://pub-e0fbc7b94d124c9091aacdb9b29e3855.r2.dev/
 *
 * Folder Structure:
 *   /{classPath}/{mediumPath}/{exactFileName}
 * e.g.,
 *   https://pub-e0fbc7b94d124c9091aacdb9b29e3855.r2.dev/class6/bangla/Class6_Math_BV_NCTB_2026.pdf.pdf
 *   https://pub-e0fbc7b94d124c9091aacdb9b29e3855.r2.dev/class6/english/Class6_Math_EV_NCTB_2026.pdf.pdf
 */
object R2StorageConfig {

    const val BUCKET_NAME = "reve-genie-books"
    
    // Cloudflare R2 Public Development URL endpoint
    const val BASE_URL = "https://pub-e0fbc7b94d124c9091aacdb9b29e3855.r2.dev/"
    const val R2_BASE_URL = "https://pub-e0fbc7b94d124c9091aacdb9b29e3855.r2.dev/"

    /**
     * Extracts canonical R2 object key from any URL or key string.
     */
    fun getCanonicalR2Key(urlOrKey: String): String {
        return if (urlOrKey.contains("r2.dev/")) {
            urlOrKey.substringAfter("r2.dev/").trimStart('/')
        } else {
            urlOrKey.trimStart('/')
        }
    }

    /**
     * Returns the canonical, safely encoded public HTTPS URL for a given R2 key or URL.
     * Encodes individual path segments safely (e.g. spaces, special chars) while preserving the '/' separator.
     */
    fun getCanonicalUrl(urlOrKey: String): String {
        val rawKey = getCanonicalR2Key(urlOrKey)
        val encodedKey = rawKey.split('/').joinToString("/") { segment ->
            try {
                URLEncoder.encode(segment, "UTF-8").replace("+", "%20")
            } catch (_: Exception) {
                segment
            }
        }
        return "$BASE_URL$encodedKey"
    }

    /**
     * Returns the canonical public HTTPS URL for a given Book model.
     */
    fun getCanonicalUrl(book: Book): String {
        return getCanonicalUrl(book.pdfUrl)
    }

    /**
     * Builds the public HTTPS URL from an exact object key (e.g. "class6/bangla/Class6_Math_BV_NCTB_2026.pdf.pdf").
     */
    fun buildPdfUrl(objectKey: String): String {
        return getCanonicalUrl(objectKey)
    }

    /**
     * Builds the public HTTPS URL from classPath, medium, and exact fileName.
     */
    fun buildPdfUrl(
        classPath: String,
        medium: Medium,
        fileName: String
    ): String {
        val cleanClass = classPath.trim().trim('/')
        val mediumFolder = medium.folderName
        val cleanFile = fileName.trim().trimStart('/')
        return getCanonicalUrl("$cleanClass/$mediumFolder/$cleanFile")
    }

    /**
     * Builds the public HTTPS URL from folderPath and fileName.
     */
    fun buildPdfUrl(folderPath: String, fileName: String): String {
        val cleanFolder = folderPath.trim().trim('/')
        val cleanFileName = fileName.trim().trimStart('/')
        return getCanonicalUrl("$cleanFolder/$cleanFileName")
    }

    /**
     * Type-safe builder using EducationLevel and Medium.
     */
    fun buildBookUrl(educationLevel: EducationLevel, medium: Medium, fileName: String): String {
        return buildPdfUrl(educationLevel.folderName, medium, fileName)
    }

    /**
     * Flexible builder supporting class folder and medium folder strings.
     */
    fun buildBookUrl(classFolder: String, mediumFolder: String, fileName: String): String {
        val normalizedClass = when {
            classFolder.contains("6", ignoreCase = true) -> "class6"
            classFolder.contains("7", ignoreCase = true) -> "class7"
            classFolder.contains("8", ignoreCase = true) -> "class8"
            classFolder.contains("9", ignoreCase = true) || classFolder.contains("10", ignoreCase = true) || classFolder.contains("ssc", ignoreCase = true) -> "ssc"
            classFolder.contains("11", ignoreCase = true) || classFolder.contains("12", ignoreCase = true) || classFolder.contains("hsc", ignoreCase = true) -> "hsc"
            else -> classFolder.trim().trim('/').lowercase()
        }
        val med = if (mediumFolder.contains("eng", ignoreCase = true) || mediumFolder.contains("ev", ignoreCase = true)) {
            Medium.ENGLISH
        } else {
            Medium.BANGLA
        }
        return buildPdfUrl(normalizedClass, med, fileName)
    }

    /**
     * Resolves the R2 HTTPS URL directly from folderPath and fileName.
     */
    fun getBookUrl(folderPath: String, fileName: String): String {
        return buildPdfUrl(folderPath, fileName)
    }
}
