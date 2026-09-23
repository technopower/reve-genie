package com.example.model

/**
 * Cloud-ready Book model for academic textbooks and chapter PDFs.
 * Does not embed PDFs in the APK/assets; points to cloud storage URLs.
 */
data class Book(
    val id: String,
    val className: String,
    val curriculumId: String = "nctb_bn",
    val groupId: String = "general_junior",
    val subjectId: String,
    val paperId: String = "none",
    val title: String,
    val pdfUrl: String,
    val titleBn: String = "",
    val chapterId: String = "",
    val pages: Int = 0,
    val fileSizeBytes: Long = 0L,
    val coverUrl: String = "",
    val edition: String = "2026 Edition",
    val author: String = "National Curriculum and Textbook Board (NCTB)",
    val group: AcademicGroup = AcademicGroup.fromId(groupId),
    val paper: SubjectPaper = SubjectPaper.fromId(paperId),
    val medium: String = if (curriculumId == "nctb_en") "english" else "bangla",
    val version: String = if (curriculumId == "nctb_en") "EV" else "BV"
) {
    val r2Key: String
        get() {
            return if (pdfUrl.contains("r2.dev/")) {
                pdfUrl.substringAfter("r2.dev/").trimStart('/')
            } else {
                pdfUrl.trimStart('/')
            }
        }

    val fileName: String
        get() {
            return r2Key.substringAfterLast('/')
        }

    fun toPdfDocument(): PdfDocument {
        return PdfDocument(
            id = id,
            fileName = if (title.endsWith(".pdf", ignoreCase = true)) title else "$title.pdf",
            pages = if (pages > 0) pages else 1,
            subject = subjectId,
            uploadDate = "NCTB Cloud"
        )
    }

    fun toTopicAiContext(language: String = "বাংলা"): TopicAiContext {
        return TopicAiContext(
            educationLevel = className,
            curriculum = if (curriculumId == "nctb_en") "NCTB (English Version)" else "NCTB (Bangla Version)",
            subjectId = subjectId,
            subjectName = title,
            chapterId = chapterId,
            chapterTitle = "",
            topicId = "",
            topicTitle = title,
            topicTitleBn = titleBn,
            language = language,
            groupId = groupId,
            group = group,
            paperId = paperId,
            paper = paper,
            curriculumId = curriculumId,
            bookId = id,
            bookTitle = title,
            bookTitleBn = titleBn,
            bookEdition = edition
        )
    }
}
