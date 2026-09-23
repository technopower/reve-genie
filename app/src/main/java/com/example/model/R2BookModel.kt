package com.example.model

/**
 * Educational Medium / Language division.
 */
enum class Medium(val id: String, val displayName: String, val folderName: String) {
    BANGLA("bangla", "Bangla Version", "bangla"),
    ENGLISH("english", "English Version", "english");

    companion object {
        fun fromId(id: String): Medium {
            return entries.find { it.id.equals(id, ignoreCase = true) || it.folderName.equals(id, ignoreCase = true) }
                ?: BANGLA
        }

        fun fromVersion(version: BookVersion): Medium {
            return when (version) {
                BookVersion.BV -> BANGLA
                BookVersion.EV -> ENGLISH
            }
        }
    }
}

/**
 * Book Version indicator (Bangla Version vs English Version).
 */
enum class BookVersion(val code: String, val displayName: String, val curriculumId: String) {
    BV("BV", "Bangla Version (বাংলা)", "nctb_bn"),
    EV("EV", "English Version (English)", "nctb_en");

    companion object {
        fun fromCode(code: String): BookVersion {
            return entries.find { it.code.equals(code, ignoreCase = true) }
                ?: if (code.contains("EV", ignoreCase = true) || code.contains("en", ignoreCase = true)) EV else BV
        }
    }
}

/**
 * Education Level folder mapping for scalable R2 storage hierarchy.
 */
enum class EducationLevel(val id: String, val displayName: String, val folderName: String) {
    CLASS_6("class6", "Class 6", "class6"),
    CLASS_7("class7", "Class 7", "class7"),
    CLASS_8("class8", "Class 8", "class8"),
    SSC("ssc", "Class 9-10 (SSC)", "ssc"),
    HSC("hsc", "Class 11-12 (HSC)", "hsc");

    companion object {
        fun fromClassName(className: String): EducationLevel {
            val lower = className.lowercase().trim()
            return when {
                lower.contains("class 6") -> CLASS_6
                lower.contains("class 7") -> CLASS_7
                lower.contains("class 8") -> CLASS_8
                lower.contains("class 9") || lower.contains("class 10") || lower.contains("ssc") -> SSC
                lower.contains("class 11") || lower.contains("class 12") || lower.contains("hsc") -> HSC
                else -> CLASS_6
            }
        }
    }
}

/**
 * Reusable PDF Book model for Cloudflare R2 Object Storage integration.
 */
data class R2PdfBook(
    val id: String,
    val title: String,
    val titleBn: String = "",
    val className: String,
    val educationLevel: EducationLevel = EducationLevel.fromClassName(className),
    val medium: Medium = Medium.BANGLA,
    val version: BookVersion = if (medium == Medium.ENGLISH) BookVersion.EV else BookVersion.BV,
    val subjectId: String,
    val paperId: String = "none",
    val groupId: String = "general_junior",
    val fileName: String,
    val folderPath: String = "${educationLevel.folderName}/${medium.folderName}",
    val edition: String = "2026 Edition",
    val pages: Int = 0,
    val fileSizeBytes: Long = 0L,
    val author: String = "National Curriculum and Textbook Board (NCTB)"
) {
    /**
     * Converts to the unified application Book model.
     */
    fun toBook(pdfUrl: String): Book {
        return Book(
            id = id,
            className = className,
            curriculumId = version.curriculumId,
            groupId = groupId,
            subjectId = subjectId,
            paperId = paperId,
            title = title,
            titleBn = titleBn,
            pdfUrl = pdfUrl,
            edition = edition,
            pages = pages,
            fileSizeBytes = fileSizeBytes,
            author = author,
            medium = medium.id,
            version = version.code
        )
    }
}
