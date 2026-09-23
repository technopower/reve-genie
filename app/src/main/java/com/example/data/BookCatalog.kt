package com.example.data

import com.example.model.AcademicGroup
import com.example.model.Book
import com.example.model.SubjectPaper

interface BookRepository {
    fun getBooksForContext(
        className: String,
        curriculumId: String = "",
        groupId: String = "",
        subjectId: String = "",
        paperId: String = "",
        chapterId: String = ""
    ): List<Book>

    fun getBookById(id: String): Book?
    fun getBooksBySubject(subjectId: String, className: String = ""): List<Book>
    fun getAllBooks(): List<Book>
    fun searchBooks(query: String): List<Book>
}

object BookCatalog : BookRepository {

    // Official 2026 NCTB Textbook Edition
    const val CURRENT_CATALOG_EDITION = "2026 Edition"

    /**
     * Builds the Cloudflare R2 HTTPS URL for any textbook PDF in a scalable folder hierarchy:
     *   https://pub-e0fcb7b94d124c9001acd9b29e3855.r2.dev/{classFolder}/{mediumFolder}/{fileName}
     */
    fun makePdfUrl(fileName: String, mediumFolder: String = "bangla"): String {
        val classFolder = when {
            fileName.startsWith("Class6", ignoreCase = true) -> "class6"
            fileName.startsWith("Class7", ignoreCase = true) -> "class7"
            fileName.startsWith("Class8", ignoreCase = true) -> "class8"
            fileName.startsWith("SSC", ignoreCase = true) -> "ssc"
            fileName.startsWith("HSC", ignoreCase = true) -> "hsc"
            else -> "general"
        }
        val cleanMedium = when {
            mediumFolder.contains("eng", ignoreCase = true) || 
            mediumFolder.contains("ev", ignoreCase = true) || 
            fileName.contains("_EV_", ignoreCase = true) -> "english"
            else -> "bangla"
        }
        
        // Unify to .pdf.pdf pattern which is the most common successful pattern on R2
        var finalFileName = fileName
        if (!finalFileName.endsWith(".pdf", ignoreCase = true)) {
            finalFileName = "$finalFileName.pdf"
        }
        if (!finalFileName.endsWith(".pdf.pdf", ignoreCase = true)) {
            finalFileName = "$finalFileName.pdf"
        }
        
        return R2StorageConfig.buildBookUrl(classFolder, cleanMedium, finalFileName)
    }

    val books: List<Book> = listOf(
        // ==========================================
        // CLASS 6 - BANGLA VERSION (BV)
        // ==========================================
        Book(
            id = "c6_charupath_bv",
            className = "Class 6",
            curriculumId = "nctb_bn",
            groupId = "general_junior",
            subjectId = "bangla",
            paperId = "none",
            title = "Charupath (Class 6 - BV)",
            titleBn = "চারুপাঠ (৬ষ্ঠ শ্রেণি - বাংলা ভার্সন)",
            pdfUrl = R2StorageConfig.buildBookUrl("class6", "bangla", "Class6_Charupath_BV_NCTB_2026.pdf.pdf"),
            edition = "2026 Edition",
            pages = 110,
            fileSizeBytes = 11534336L,
            medium = "bangla",
            version = "BV"
        ),
        Book(
            id = "c6_anandapath_bv",
            className = "Class 6",
            curriculumId = "nctb_bn",
            groupId = "general_junior",
            subjectId = "anandapath",
            paperId = "none",
            title = "Anandapath (Class 6 - BV)",
            titleBn = "আনন্দপাঠ (৬ষ্ঠ শ্রেণি - বাংলা ভার্সন)",
            pdfUrl = R2StorageConfig.buildBookUrl("class6", "bangla", "Class6_Anandapath_BV_NCTB_2026.pdf.pdf"),
            edition = "2026 Edition",
            pages = 98,
            fileSizeBytes = 9437184L,
            medium = "bangla",
            version = "BV"
        ),
        Book(
            id = "c6_bangla_grammar_bv",
            className = "Class 6",
            curriculumId = "nctb_bn",
            groupId = "general_junior",
            subjectId = "bangla_grammar",
            paperId = "none",
            title = "Bangla Byakoron o Nirmiti (Class 6 - BV)",
            titleBn = "বাংলা ব্যাকরণ ও নির্মিতি (৬ষ্ঠ শ্রেণি - বাংলা ভার্সন)",
            pdfUrl = R2StorageConfig.buildBookUrl("class6", "bangla", "Class6_Bangla_Byakoron_Nitimala_BV_NCTB_2026.pdf.pdf"),
            edition = "2026 Edition",
            pages = 132,
            fileSizeBytes = 12582912L,
            medium = "bangla",
            version = "BV"
        ),
        Book(
            id = "c6_english_bv",
            className = "Class 6",
            curriculumId = "nctb_bn",
            groupId = "general_junior",
            subjectId = "english",
            paperId = "none",
            title = "English for Today (Class 6 - BV)",
            titleBn = "ইংলিশ ফর টুডে (৬ষ্ঠ শ্রেণি - বাংলা ভার্সন)",
            pdfUrl = R2StorageConfig.buildBookUrl("class6", "bangla", "Class6_English_For_Today_BV_NCTB_2026.pdf.pdf"),
            edition = "2026 Edition",
            pages = 172,
            fileSizeBytes = 14680064L,
            medium = "bangla",
            version = "BV"
        ),
        Book(
            id = "c6_english_grammar_bv",
            className = "Class 6",
            curriculumId = "nctb_bn",
            groupId = "general_junior",
            subjectId = "english_grammar",
            paperId = "none",
            title = "English Grammar and Composition (Class 6 - BV)",
            titleBn = "ইংলিশ গ্রামার অ্যান্ড কম্পোজিশন (৬ষ্ঠ শ্রেণি)",
            pdfUrl = R2StorageConfig.buildBookUrl("class6", "bangla", "Class6_English_Grammar_Composition_BV_NCTB_2026.pdf.pdf"),
            edition = "2026 Edition",
            pages = 156,
            fileSizeBytes = 13631488L,
            medium = "bangla",
            version = "BV"
        ),
        Book(
            id = "c6_math_bv",
            className = "Class 6",
            curriculumId = "nctb_bn",
            groupId = "general_junior",
            subjectId = "math",
            paperId = "none",
            title = "Mathematics (Class 6 - BV)",
            titleBn = "গণিত (৬ষ্ঠ শ্রেণি - বাংলা ভার্সন)",
            pdfUrl = R2StorageConfig.buildBookUrl("class6", "bangla", "Class6_Math_BV_NCTB_2026.pdf.pdf"),
            edition = "2026 Edition",
            pages = 240,
            fileSizeBytes = 18874368L,
            medium = "bangla",
            version = "BV"
        ),
        Book(
            id = "c6_science_bv",
            className = "Class 6",
            curriculumId = "nctb_bn",
            groupId = "general_junior",
            subjectId = "science",
            paperId = "none",
            title = "General Science (Class 6 - BV)",
            titleBn = "বিজ্ঞান (৬ষ্ঠ শ্রেণি - বাংলা ভার্সন)",
            pdfUrl = R2StorageConfig.buildBookUrl("class6", "bangla", "Class6_Science_BV_NCTB_2026.pdf.pdf"),
            edition = "2026 Edition",
            pages = 188,
            fileSizeBytes = 15728640L,
            medium = "bangla",
            version = "BV"
        ),
        Book(
            id = "c6_bgs_bv",
            className = "Class 6",
            curriculumId = "nctb_bn",
            groupId = "general_junior",
            subjectId = "bgs",
            paperId = "none",
            title = "Bangladesh and Global Studies (Class 6 - BV)",
            titleBn = "বাংলাদেশ ও বিশ্বপরিচয় (৬ষ্ঠ শ্রেণি - বাংলা ভার্সন)",
            pdfUrl = R2StorageConfig.buildBookUrl("class6", "bangla", "Class6_Bangladesh_Global_Studies_BV_NCTB_2026.pdf.pdf"),
            edition = "2026 Edition",
            pages = 136,
            fileSizeBytes = 11534336L,
            medium = "bangla",
            version = "BV"
        ),
        Book(
            id = "c6_ict_bv",
            className = "Class 6",
            curriculumId = "nctb_bn",
            groupId = "general_junior",
            subjectId = "ict",
            paperId = "none",
            title = "Information & Communication Technology (Class 6 - BV)",
            titleBn = "তথ্য ও যোগাযোগ প্রযুক্তি (৬ষ্ঠ শ্রেণি - বাংলা ভার্সন)",
            pdfUrl = R2StorageConfig.buildBookUrl("class6", "bangla", "Class6_ICT_BV_NCTB_2026.pdf.pdf"),
            edition = "2026 Edition",
            pages = 96,
            fileSizeBytes = 8388608L,
            medium = "bangla",
            version = "BV"
        ),

        // ==========================================
        // CLASS 6 - ENGLISH VERSION (EV)
        // ==========================================
        Book(
            id = "c6_charupath_ev",
            className = "Class 6",
            curriculumId = "nctb_en",
            groupId = "general_junior",
            subjectId = "bangla",
            paperId = "none",
            title = "Charupath (Class 6 - EV)",
            titleBn = "চারুপাঠ (৬ষ্ঠ শ্রেণি - ইংলিশ ভার্সন)",
            pdfUrl = R2StorageConfig.buildBookUrl("class6", "english", "Class6_Charupath_EV_NCTB_2026.pdf.pdf"),
            edition = "2026 Edition",
            pages = 110,
            fileSizeBytes = 11534336L,
            medium = "english",
            version = "EV"
        ),
        Book(
            id = "c6_anandapath_ev",
            className = "Class 6",
            curriculumId = "nctb_en",
            groupId = "general_junior",
            subjectId = "anandapath",
            paperId = "none",
            title = "Anandapath (Class 6 - EV)",
            titleBn = "আনন্দপাঠ (৬ষ্ঠ শ্রেণি - ইংলিশ ভার্সন)",
            pdfUrl = R2StorageConfig.buildBookUrl("class6", "english", "Class6_Anandapath_EV_NCTB_2026.pdf.pdf"),
            edition = "2026 Edition",
            pages = 98,
            fileSizeBytes = 9437184L,
            medium = "english",
            version = "EV"
        ),
        Book(
            id = "c6_bangla_grammar_ev",
            className = "Class 6",
            curriculumId = "nctb_en",
            groupId = "general_junior",
            subjectId = "bangla_grammar",
            paperId = "none",
            title = "Bangla Byakoron o Nirmiti (Class 6 - EV)",
            titleBn = "বাংলা ব্যাকরণ ও নির্মিতি (৬ষ্ঠ শ্রেণি - ইংলিশ ভার্সন)",
            pdfUrl = R2StorageConfig.buildBookUrl("class6", "english", "Class6_Bangla_Byakoron_Nitimala_EV_NCTB_2026.pdf.pdf"),
            edition = "2026 Edition",
            pages = 132,
            fileSizeBytes = 12582912L,
            medium = "english",
            version = "EV"
        ),
        Book(
            id = "c6_english_ev",
            className = "Class 6",
            curriculumId = "nctb_en",
            groupId = "general_junior",
            subjectId = "english",
            paperId = "none",
            title = "English for Today (Class 6 - EV)",
            titleBn = "English for Today (Class 6 - EV)",
            pdfUrl = R2StorageConfig.buildBookUrl("class6", "english", "Class6_English_For_Today_EV_NCTB_2026.pdf.pdf"),
            edition = "2026 Edition",
            pages = 172,
            fileSizeBytes = 14680064L,
            medium = "english",
            version = "EV"
        ),
        Book(
            id = "c6_math_ev",
            className = "Class 6",
            curriculumId = "nctb_en",
            groupId = "general_junior",
            subjectId = "math",
            paperId = "none",
            title = "Mathematics (Class 6 - EV)",
            titleBn = "Mathematics (Class 6 - EV)",
            pdfUrl = R2StorageConfig.buildBookUrl("class6", "english", "Class6_Math_EV_NCTB_2026.pdf.pdf"),
            edition = "2026 Edition",
            pages = 240,
            fileSizeBytes = 18874368L,
            medium = "english",
            version = "EV"
        ),
        Book(
            id = "c6_science_ev",
            className = "Class 6",
            curriculumId = "nctb_en",
            groupId = "general_junior",
            subjectId = "science",
            paperId = "none",
            title = "General Science (Class 6 - EV)",
            titleBn = "General Science (Class 6 - EV)",
            pdfUrl = R2StorageConfig.buildBookUrl("class6", "english", "Class6_Science_EV_NCTB_2026.pdf.pdf"),
            edition = "2026 Edition",
            pages = 188,
            fileSizeBytes = 15728640L,
            medium = "english",
            version = "EV"
        ),
        Book(
            id = "c6_bgs_ev",
            className = "Class 6",
            curriculumId = "nctb_en",
            groupId = "general_junior",
            subjectId = "bgs",
            paperId = "none",
            title = "Bangladesh and Global Studies (Class 6 - EV)",
            titleBn = "Bangladesh and Global Studies (Class 6 - EV)",
            pdfUrl = R2StorageConfig.buildBookUrl("class6", "english", "Class6_Bangladesh_Global_Studies_EV_NCTB_2026.pdf.pdf"),
            edition = "2026 Edition",
            pages = 136,
            fileSizeBytes = 11534336L,
            medium = "english",
            version = "EV"
        ),
        Book(
            id = "c6_ict_ev",
            className = "Class 6",
            curriculumId = "nctb_en",
            groupId = "general_junior",
            subjectId = "ict",
            paperId = "none",
            title = "Information & Communication Technology (Class 6 - EV)",
            titleBn = "ICT (Class 6 - EV)",
            pdfUrl = R2StorageConfig.buildBookUrl("class6", "english", "Class6_ICT_EV_NCTB_2026.pdf.pdf"),
            edition = "2026 Edition",
            pages = 96,
            fileSizeBytes = 8388608L,
            medium = "english",
            version = "EV"
        ),

        // ==========================================
        // CLASS 7 (Junior - GENERAL_JUNIOR)
        // ==========================================
        Book(
            id = "c7_bangla_bv",
            className = "Class 7",
            curriculumId = "nctb_bn",
            groupId = "general_junior",
            subjectId = "bangla",
            paperId = "none",
            title = "Saptavarna Bangla (Class 7 - BV)",
            titleBn = "সপ্তবর্ণা বাংলা সাহিত্য (৭ম শ্রেণি)",
            pdfUrl = R2StorageConfig.buildBookUrl("class7", "bangla", "Class7_Saptabarna_BV_NCTB_2026.pdf.pdf"),
            pages = 156,
            fileSizeBytes = 13631488L,
            medium = "bangla",
            version = "BV"
        ),
        Book(
            id = "c7_bangla_ev",
            className = "Class 7",
            curriculumId = "nctb_en",
            groupId = "general_junior",
            subjectId = "bangla",
            paperId = "none",
            title = "Saptavarna Bangla (Class 7 - EV)",
            titleBn = "সপ্তবর্ণা বাংলা সাহিত্য (৭ম শ্রেণি - EV)",
            pdfUrl = R2StorageConfig.buildBookUrl("class7", "english", "Class7_Saptabarna_EV_NCTB_2026.pdf.pdf"),
            pages = 156,
            fileSizeBytes = 13631488L,
            medium = "english",
            version = "EV"
        ),
        Book(
            id = "c7_english_bv",
            className = "Class 7",
            curriculumId = "nctb_bn",
            groupId = "general_junior",
            subjectId = "english",
            paperId = "none",
            title = "English for Today (Class 7 - BV)",
            titleBn = "ইংলিশ ফর টুডে (৭ম শ্রেণি)",
            pdfUrl = R2StorageConfig.buildBookUrl("class7", "bangla", "Class7_English_For_Today_BV_NCTB_2026.pdf.pdf"),
            pages = 122,
            fileSizeBytes = 12582912L,
            medium = "bangla",
            version = "BV"
        ),
        Book(
            id = "c7_english_ev",
            className = "Class 7",
            curriculumId = "nctb_en",
            groupId = "general_junior",
            subjectId = "english",
            paperId = "none",
            title = "English for Today (Class 7 - EV)",
            titleBn = "ইংলিশ ফর টুডে (৭ম শ্রেণি - EV)",
            pdfUrl = R2StorageConfig.buildBookUrl("class7", "english", "Class7_English_For_Today_EV_NCTB_2026.pdf.pdf"),
            pages = 122,
            fileSizeBytes = 12582912L,
            medium = "english",
            version = "EV"
        ),
        Book(
            id = "c7_math_bv",
            className = "Class 7",
            curriculumId = "nctb_bn",
            groupId = "general_junior",
            subjectId = "math",
            paperId = "none",
            title = "Mathematics (Class 7 - BV)",
            titleBn = "গণিত (৭ম শ্রেণি)",
            pdfUrl = R2StorageConfig.buildBookUrl("class7", "bangla", "Class7_Math_BV_NCTB_2026.pdf.pdf"),
            pages = 252,
            fileSizeBytes = 19922944L,
            medium = "bangla",
            version = "BV"
        ),
        Book(
            id = "c7_math_ev",
            className = "Class 7",
            curriculumId = "nctb_en",
            groupId = "general_junior",
            subjectId = "math",
            paperId = "none",
            title = "Mathematics (Class 7 - EV)",
            titleBn = "গণিত (৭ম শ্রেণি - EV)",
            pdfUrl = R2StorageConfig.buildBookUrl("class7", "english", "Class7_Math_EV_NCTB_2026.pdf.pdf"),
            pages = 252,
            fileSizeBytes = 19922944L,
            medium = "english",
            version = "EV"
        ),
        Book(
            id = "c7_science_bv",
            className = "Class 7",
            curriculumId = "nctb_bn",
            groupId = "general_junior",
            subjectId = "science",
            paperId = "none",
            title = "General Science (Class 7 - BV)",
            titleBn = "বিজ্ঞান (৭ম শ্রেণি)",
            pdfUrl = R2StorageConfig.buildBookUrl("class7", "bangla", "Class7_Science_BV_NCTB_2026.pdf.pdf"),
            pages = 196,
            fileSizeBytes = 16777216L,
            medium = "bangla",
            version = "BV"
        ),
        Book(
            id = "c7_science_ev",
            className = "Class 7",
            curriculumId = "nctb_en",
            groupId = "general_junior",
            subjectId = "science",
            paperId = "none",
            title = "General Science (Class 7 - EV)",
            titleBn = "বিজ্ঞান (৭ম শ্রেণি - EV)",
            pdfUrl = R2StorageConfig.buildBookUrl("class7", "english", "Class7_Science_EV_NCTB_2026.pdf.pdf"),
            pages = 196,
            fileSizeBytes = 16777216L,
            medium = "english",
            version = "EV"
        ),
        Book(
            id = "c7_bgs_bv",
            className = "Class 7",
            curriculumId = "nctb_bn",
            groupId = "general_junior",
            subjectId = "bgs",
            paperId = "none",
            title = "Bangladesh and Global Studies (Class 7 - BV)",
            titleBn = "বাংলাদেশ ও বিশ্বপরিচয় (৭ম শ্রেণি)",
            pdfUrl = R2StorageConfig.buildBookUrl("class7", "bangla", "Class7_Bangladesh_Global_Studies_BV_NCTB_2026.pdf.pdf"),
            pages = 144,
            fileSizeBytes = 12058624L,
            medium = "bangla",
            version = "BV"
        ),
        Book(
            id = "c7_bgs_ev",
            className = "Class 7",
            curriculumId = "nctb_en",
            groupId = "general_junior",
            subjectId = "bgs",
            paperId = "none",
            title = "Bangladesh and Global Studies (Class 7 - EV)",
            titleBn = "বাংলাদেশ ও বিশ্বপরিচয় (৭ম শ্রেণি - EV)",
            pdfUrl = R2StorageConfig.buildBookUrl("class7", "english", "Class7_Bangladesh_Global_Studies_EV_NCTB_2026.pdf.pdf"),
            pages = 144,
            fileSizeBytes = 12058624L,
            medium = "english",
            version = "EV"
        ),
        Book(
            id = "c7_ict_bv",
            className = "Class 7",
            curriculumId = "nctb_bn",
            groupId = "general_junior",
            subjectId = "ict",
            paperId = "none",
            title = "Information & Communication Technology (Class 7 - BV)",
            titleBn = "তথ্য ও যোগাযোগ প্রযুক্তি (৭ম শ্রেণি)",
            pdfUrl = R2StorageConfig.buildBookUrl("class7", "bangla", "Class7_ICT_BV_NCTB_2026.pdf.pdf"),
            pages = 104,
            fileSizeBytes = 9437184L,
            medium = "bangla",
            version = "BV"
        ),
        Book(
            id = "c7_ict_ev",
            className = "Class 7",
            curriculumId = "nctb_en",
            groupId = "general_junior",
            subjectId = "ict",
            paperId = "none",
            title = "Information & Communication Technology (Class 7 - EV)",
            titleBn = "তথ্য ও যোগাযোগ প্রযুক্তি (৭ম শ্রেণি - EV)",
            pdfUrl = R2StorageConfig.buildBookUrl("class7", "english", "Class7_ICT_EV_NCTB_2026.pdf.pdf"),
            pages = 104,
            fileSizeBytes = 9437184L,
            medium = "english",
            version = "EV"
        ),

        // ==========================================
        // CLASS 8 (Junior - GENERAL_JUNIOR)
        // ==========================================
        Book(
            id = "c8_bangla_bv",
            className = "Class 8",
            curriculumId = "nctb_bn",
            groupId = "general_junior",
            subjectId = "bangla",
            paperId = "none",
            title = "Sahitya Kanika (Class 8 - BV)",
            titleBn = "সাহিত্য কণিকা (৮ম শ্রেণি)",
            pdfUrl = R2StorageConfig.buildBookUrl("class8", "bangla", "Class8_Sahitya_Kanika_BV_NCTB_2026.pdf.pdf"),
            pages = 168,
            fileSizeBytes = 14680064L,
            medium = "bangla",
            version = "BV"
        ),
        Book(
            id = "c8_bangla_ev",
            className = "Class 8",
            curriculumId = "nctb_en",
            groupId = "general_junior",
            subjectId = "bangla",
            paperId = "none",
            title = "Sahitya Kanika (Class 8 - EV)",
            titleBn = "সাহিত্য কণিকা (৮ম শ্রেণি - EV)",
            pdfUrl = R2StorageConfig.buildBookUrl("class8", "english", "Class8_Sahitya_Kanika_EV_NCTB_2026.pdf.pdf"),
            pages = 168,
            fileSizeBytes = 14680064L,
            medium = "english",
            version = "EV"
        ),
        Book(
            id = "c8_english_bv",
            className = "Class 8",
            curriculumId = "nctb_bn",
            groupId = "general_junior",
            subjectId = "english",
            paperId = "none",
            title = "English for Today (Class 8 - BV)",
            titleBn = "ইংলিশ ফর টুডে (৮ম শ্রেণি)",
            pdfUrl = R2StorageConfig.buildBookUrl("class8", "bangla", "Class8_English_For_Today_BV_NCTB_2026.pdf.pdf"),
            pages = 180,
            fileSizeBytes = 15728640L,
            medium = "bangla",
            version = "BV"
        ),
        Book(
            id = "c8_english_ev",
            className = "Class 8",
            curriculumId = "nctb_en",
            groupId = "general_junior",
            subjectId = "english",
            paperId = "none",
            title = "English for Today (Class 8 - EV)",
            titleBn = "ইংলিশ ফর টুডে (৮ম শ্রেণি - EV)",
            pdfUrl = R2StorageConfig.buildBookUrl("class8", "english", "Class8_English_For_Today_EV_NCTB_2026.pdf.pdf"),
            pages = 180,
            fileSizeBytes = 15728640L,
            medium = "english",
            version = "EV"
        ),
        Book(
            id = "c8_math_bv",
            className = "Class 8",
            curriculumId = "nctb_bn",
            groupId = "general_junior",
            subjectId = "math",
            paperId = "none",
            title = "Mathematics (Class 8 - BV)",
            titleBn = "গণিত (৮ম শ্রেণি)",
            pdfUrl = R2StorageConfig.buildBookUrl("class8", "bangla", "Class8_Math_BV_NCTB_2026.pdf.pdf"),
            pages = 264,
            fileSizeBytes = 20971520L,
            medium = "bangla",
            version = "BV"
        ),
        Book(
            id = "c8_math_ev",
            className = "Class 8",
            curriculumId = "nctb_en",
            groupId = "general_junior",
            subjectId = "math",
            paperId = "none",
            title = "Mathematics (Class 8 - EV)",
            titleBn = "গণিত (৮ম শ্রেণি - EV)",
            pdfUrl = R2StorageConfig.buildBookUrl("class8", "english", "Class8_Math_EV_NCTB_2026.pdf.pdf"),
            pages = 264,
            fileSizeBytes = 20971520L,
            medium = "english",
            version = "EV"
        ),
        Book(
            id = "c8_science_bv",
            className = "Class 8",
            curriculumId = "nctb_bn",
            groupId = "general_junior",
            subjectId = "science",
            paperId = "none",
            title = "General Science (Class 8 - BV)",
            titleBn = "বিজ্ঞান (৮ম শ্রেণি)",
            pdfUrl = R2StorageConfig.buildBookUrl("class8", "bangla", "Class8_Science_BV_NCTB_2026.pdf.pdf"),
            pages = 212,
            fileSizeBytes = 17825792L,
            medium = "bangla",
            version = "BV"
        ),
        Book(
            id = "c8_science_ev",
            className = "Class 8",
            curriculumId = "nctb_en",
            groupId = "general_junior",
            subjectId = "science",
            paperId = "none",
            title = "General Science (Class 8 - EV)",
            titleBn = "বিজ্ঞান (৮ম শ্রেণি - EV)",
            pdfUrl = R2StorageConfig.buildBookUrl("class8", "english", "Class8_Science_EV_NCTB_2026.pdf.pdf"),
            pages = 212,
            fileSizeBytes = 17825792L,
            medium = "english",
            version = "EV"
        ),
        Book(
            id = "c8_bgs_bv",
            className = "Class 8",
            curriculumId = "nctb_bn",
            groupId = "general_junior",
            subjectId = "bgs",
            paperId = "none",
            title = "Bangladesh and Global Studies (Class 8 - BV)",
            titleBn = "বাংলাদেশ ও বিশ্বপরিচয় (৮ম শ্রেণি)",
            pdfUrl = R2StorageConfig.buildBookUrl("class8", "bangla", "Class8_Bangladesh_Global_Studies_BV_NCTB_2026.pdf.pdf"),
            pages = 152,
            fileSizeBytes = 13107200L,
            medium = "bangla",
            version = "BV"
        ),
        Book(
            id = "c8_bgs_ev",
            className = "Class 8",
            curriculumId = "nctb_en",
            groupId = "general_junior",
            subjectId = "bgs",
            paperId = "none",
            title = "Bangladesh and Global Studies (Class 8 - EV)",
            titleBn = "বাংলাদেশ ও বিশ্বপরিচয় (৮ম শ্রেণি - EV)",
            pdfUrl = R2StorageConfig.buildBookUrl("class8", "english", "Class8_Bangladesh_Global_Studies_EV_NCTB_2026.pdf.pdf"),
            pages = 152,
            fileSizeBytes = 13107200L,
            medium = "english",
            version = "EV"
        ),
        Book(
            id = "c8_ict_bv",
            className = "Class 8",
            curriculumId = "nctb_bn",
            groupId = "general_junior",
            subjectId = "ict",
            paperId = "none",
            title = "Information & Communication Technology (Class 8 - BV)",
            titleBn = "তথ্য ও যোগাযোগ প্রযুক্তি (৮ম শ্রেণি)",
            pdfUrl = R2StorageConfig.buildBookUrl("class8", "bangla", "Class8_ICT_BV_NCTB_2026.pdf.pdf"),
            pages = 112,
            fileSizeBytes = 10485760L,
            medium = "bangla",
            version = "BV"
        ),
        Book(
            id = "c8_ict_ev",
            className = "Class 8",
            curriculumId = "nctb_en",
            groupId = "general_junior",
            subjectId = "ict",
            paperId = "none",
            title = "Information & Communication Technology (Class 8 - EV)",
            titleBn = "তথ্য ও যোগাযোগ প্রযুক্তি (৮ম শ্রেণি - EV)",
            pdfUrl = R2StorageConfig.buildBookUrl("class8", "english", "Class8_ICT_EV_NCTB_2026.pdf.pdf"),
            pages = 112,
            fileSizeBytes = 10485760L,
            medium = "english",
            version = "EV"
        ),

        // ==========================================
        // CLASS 9 & 10 (Secondary - SSC SCIENCE)
        // ==========================================
        Book(
            id = "ssc_physics_bv",
            className = "Class 10 (SSC)",
            curriculumId = "nctb_bn",
            groupId = "science",
            subjectId = "physics",
            paperId = "none",
            title = "Physics (SSC - BV)",
            titleBn = "পদার্থবিজ্ঞান (৯ম-১০ম শ্রেণি)",
            pdfUrl = R2StorageConfig.buildBookUrl("ssc", "bangla", "SSC_Physics_BV_NCTB_2026.pdf.pdf"),
            pages = 320,
            fileSizeBytes = 26214400L,
            medium = "bangla",
            version = "BV"
        ),
        Book(
            id = "ssc_physics_ev",
            className = "Class 10 (SSC)",
            curriculumId = "nctb_en",
            groupId = "science",
            subjectId = "physics",
            paperId = "none",
            title = "Physics (SSC - EV)",
            titleBn = "Physics (SSC - EV)",
            pdfUrl = R2StorageConfig.buildBookUrl("ssc", "english", "SSC_Physics_EV_NCTB_2026.pdf.pdf"),
            pages = 320,
            fileSizeBytes = 26214400L,
            medium = "english",
            version = "EV"
        ),
        Book(
            id = "ssc_chemistry_bv",
            className = "Class 10 (SSC)",
            curriculumId = "nctb_bn",
            groupId = "science",
            subjectId = "chemistry",
            paperId = "none",
            title = "Chemistry (SSC - BV)",
            titleBn = "রসায়ন (৯ম-১০ম শ্রেণি)",
            pdfUrl = R2StorageConfig.buildBookUrl("ssc", "bangla", "SSC_Chemistry_BV_NCTB_2026.pdf.pdf"),
            pages = 296,
            fileSizeBytes = 24117248L,
            medium = "bangla",
            version = "BV"
        ),
        Book(
            id = "ssc_chemistry_ev",
            className = "Class 10 (SSC)",
            curriculumId = "nctb_en",
            groupId = "science",
            subjectId = "chemistry",
            paperId = "none",
            title = "Chemistry (SSC - EV)",
            titleBn = "Chemistry (SSC - EV)",
            pdfUrl = R2StorageConfig.buildBookUrl("ssc", "english", "SSC_Chemistry_EV_NCTB_2026.pdf.pdf"),
            pages = 296,
            fileSizeBytes = 24117248L,
            medium = "english",
            version = "EV"
        ),
        Book(
            id = "ssc_biology_bv",
            className = "Class 10 (SSC)",
            curriculumId = "nctb_bn",
            groupId = "science",
            subjectId = "biology",
            paperId = "none",
            title = "Biology (SSC - BV)",
            titleBn = "জীববিজ্ঞান (৯ম-১০ম শ্রেণি)",
            pdfUrl = R2StorageConfig.buildBookUrl("ssc", "bangla", "SSC_Biology_BV_NCTB_2026.pdf.pdf"),
            pages = 310,
            fileSizeBytes = 25165824L,
            medium = "bangla",
            version = "BV"
        ),
        Book(
            id = "ssc_biology_ev",
            className = "Class 10 (SSC)",
            curriculumId = "nctb_en",
            groupId = "science",
            subjectId = "biology",
            paperId = "none",
            title = "Biology (SSC - EV)",
            titleBn = "Biology (SSC - EV)",
            pdfUrl = R2StorageConfig.buildBookUrl("ssc", "english", "SSC_Biology_EV_NCTB_2026.pdf.pdf"),
            pages = 310,
            fileSizeBytes = 25165824L,
            medium = "english",
            version = "EV"
        ),
        Book(
            id = "ssc_higher_math_bv",
            className = "Class 10 (SSC)",
            curriculumId = "nctb_bn",
            groupId = "science",
            subjectId = "higher_math",
            paperId = "none",
            title = "Higher Mathematics (SSC - BV)",
            titleBn = "উচ্চতর গণিত (৯ম-১০ম শ্রেণি)",
            pdfUrl = R2StorageConfig.buildBookUrl("ssc", "bangla", "SSC_Higher_Mathematics_BV_NCTB_2026.pdf.pdf"),
            pages = 340,
            fileSizeBytes = 27262976L,
            medium = "bangla",
            version = "BV"
        ),
        Book(
            id = "ssc_higher_math_ev",
            className = "Class 10 (SSC)",
            curriculumId = "nctb_en",
            groupId = "science",
            subjectId = "higher_math",
            paperId = "none",
            title = "Higher Mathematics (SSC - EV)",
            titleBn = "Higher Mathematics (SSC - EV)",
            pdfUrl = R2StorageConfig.buildBookUrl("ssc", "english", "SSC_Higher_Mathematics_EV_NCTB_2026.pdf.pdf"),
            pages = 340,
            fileSizeBytes = 27262976L,
            medium = "english",
            version = "EV"
        ),
        Book(
            id = "ssc_math_bv",
            className = "Class 10 (SSC)",
            curriculumId = "nctb_bn",
            groupId = "science",
            subjectId = "math",
            paperId = "none",
            title = "General Mathematics (SSC - BV)",
            titleBn = "সাধারণ গণিত (৯ম-১০ম শ্রেণি)",
            pdfUrl = R2StorageConfig.buildBookUrl("ssc", "bangla", "SSC_General_Mathematics_BV_NCTB_2026.pdf.pdf"),
            pages = 360,
            fileSizeBytes = 29360128L,
            medium = "bangla",
            version = "BV"
        ),
        Book(
            id = "ssc_math_ev",
            className = "Class 10 (SSC)",
            curriculumId = "nctb_en",
            groupId = "science",
            subjectId = "math",
            paperId = "none",
            title = "General Mathematics (SSC - EV)",
            titleBn = "General Mathematics (SSC - EV)",
            pdfUrl = R2StorageConfig.buildBookUrl("ssc", "english", "SSC_General_Mathematics_EV_NCTB_2026.pdf.pdf"),
            pages = 360,
            fileSizeBytes = 29360128L,
            medium = "english",
            version = "EV"
        ),
        Book(
            id = "ssc_bangla_1_bv",
            className = "Class 10 (SSC)",
            curriculumId = "nctb_bn",
            groupId = "science",
            subjectId = "bangla",
            paperId = "paper_1",
            title = "Bangla Sahitya 1st Paper (SSC - BV)",
            titleBn = "বাংলা সাহিত্য ১ম পত্র (এসএসসি)",
            pdfUrl = R2StorageConfig.buildBookUrl("ssc", "bangla", "SSC_Bangla_1st_BV_NCTB_2026.pdf.pdf"),
            pages = 280,
            fileSizeBytes = 22020096L,
            medium = "bangla",
            version = "BV"
        ),
        Book(
            id = "ssc_english_1_bv",
            className = "Class 10 (SSC)",
            curriculumId = "nctb_bn",
            groupId = "science",
            subjectId = "english",
            paperId = "paper_1",
            title = "English for Today 1st Paper (SSC - BV)",
            titleBn = "ইংলিশ ফর টুডে ১ম পত্র (এসএসসি)",
            pdfUrl = R2StorageConfig.buildBookUrl("ssc", "bangla", "SSC_English_1st_BV_NCTB_2026.pdf.pdf"),
            pages = 210,
            fileSizeBytes = 17825792L,
            medium = "bangla",
            version = "BV"
        ),
        Book(
            id = "ssc_ict_bv",
            className = "Class 10 (SSC)",
            curriculumId = "nctb_bn",
            groupId = "science",
            subjectId = "ict",
            paperId = "none",
            title = "Information & Communication Technology (SSC - BV)",
            titleBn = "তথ্য ও যোগাযোগ প্রযুক্তি (এসএসসি)",
            pdfUrl = R2StorageConfig.buildBookUrl("ssc", "bangla", "SSC_ICT_BV_NCTB_2026.pdf.pdf"),
            pages = 180,
            fileSizeBytes = 15728640L,
            medium = "bangla",
            version = "BV"
        ),
        Book(
            id = "ssc_ict_ev",
            className = "Class 10 (SSC)",
            curriculumId = "nctb_en",
            groupId = "science",
            subjectId = "ict",
            paperId = "none",
            title = "Information & Communication Technology (SSC - EV)",
            titleBn = "Information & Communication Technology (SSC - EV)",
            pdfUrl = R2StorageConfig.buildBookUrl("ssc", "english", "SSC_ICT_EV_NCTB_2026.pdf.pdf"),
            pages = 180,
            fileSizeBytes = 15728640L,
            medium = "english",
            version = "EV"
        ),
        Book(
            id = "ssc_bgs_bv",
            className = "Class 10 (SSC)",
            curriculumId = "nctb_bn",
            groupId = "science",
            subjectId = "bgs",
            paperId = "none",
            title = "Bangladesh and Global Studies (SSC - BV)",
            titleBn = "বাংলাদেশ ও বিশ্বপরিচয় (এসএসসি)",
            pdfUrl = R2StorageConfig.buildBookUrl("ssc", "bangla", "SSC_BGS_BV_NCTB_2026.pdf.pdf"),
            pages = 210,
            fileSizeBytes = 18874368L,
            medium = "bangla",
            version = "BV"
        ),
        Book(
            id = "ssc_bgs_ev",
            className = "Class 10 (SSC)",
            curriculumId = "nctb_en",
            groupId = "science",
            subjectId = "bgs",
            paperId = "none",
            title = "Bangladesh and Global Studies (SSC - EV)",
            titleBn = "Bangladesh & Global Studies (SSC - EV)",
            pdfUrl = R2StorageConfig.buildBookUrl("ssc", "english", "SSC_BGS_EV_NCTB_2026.pdf.pdf"),
            pages = 210,
            fileSizeBytes = 18874368L,
            medium = "english",
            version = "EV"
        ),

        Book(
            id = "ssc_bangla_2_bv",
            className = "Class 10 (SSC)",
            curriculumId = "nctb_bn",
            groupId = "science",
            subjectId = "bangla",
            paperId = "paper_2",
            title = "Bangla Second Paper (SSC - BV)",
            titleBn = "বাংলা ২য় পত্র: ব্যাকরণ ও নির্মিতি (এসএসসি)",
            pdfUrl = R2StorageConfig.buildBookUrl("ssc", "bangla", "SSC_Bangla_2nd_BV_NCTB_2026.pdf.pdf"),
            pages = 230,
            fileSizeBytes = 18874368L,
            medium = "bangla",
            version = "BV"
        ),
        Book(
            id = "ssc_english_2_bv",
            className = "Class 10 (SSC)",
            curriculumId = "nctb_bn",
            groupId = "science",
            subjectId = "english",
            paperId = "paper_2",
            title = "English Grammar and Composition 2nd Paper (SSC - BV)",
            titleBn = "ইংলিশ ২য় পত্র: গ্রামার ও কম্পোজিশন (এসএসসি)",
            pdfUrl = R2StorageConfig.buildBookUrl("ssc", "bangla", "SSC_English_2nd_BV_NCTB_2026.pdf.pdf"),
            pages = 240,
            fileSizeBytes = 19922944L,
            medium = "bangla",
            version = "BV"
        ),
        Book(
            id = "ssc_english_2_ev",
            className = "Class 10 (SSC)",
            curriculumId = "nctb_en",
            groupId = "science",
            subjectId = "english",
            paperId = "paper_2",
            title = "English Grammar and Composition 2nd Paper (SSC - EV)",
            titleBn = "English Grammar & Comp (SSC - EV)",
            pdfUrl = R2StorageConfig.buildBookUrl("ssc", "english", "SSC_English_2nd_EV_NCTB_2026.pdf.pdf"),
            pages = 240,
            fileSizeBytes = 19922944L,
            medium = "english",
            version = "EV"
        ),

        // ==========================================
        // CLASS 9 & 10 (Secondary - SSC BUSINESS STUDIES)
        // ==========================================
        Book(
            id = "ssc_accounting",
            className = "Class 10 (SSC)",
            curriculumId = "nctb_bn",
            groupId = "business_studies",
            subjectId = "accounting",
            paperId = "none",
            title = "Accounting (Secondary SSC)",
            titleBn = "হিসাববিজ্ঞান (৯ম-১০ম শ্রেণি)",
            pdfUrl = makePdfUrl("SSC_Accounting_NCTB_2026.pdf"),
            pages = 270,
            fileSizeBytes = 23068672L
        ),
        Book(
            id = "ssc_business_ent",
            className = "Class 10 (SSC)",
            curriculumId = "nctb_bn",
            groupId = "business_studies",
            subjectId = "business_entrepreneurship",
            paperId = "none",
            title = "Business Entrepreneurship (SSC)",
            titleBn = "ব্যবসায় উদ্যোগ (৯ম-১০ম শ্রেণি)",
            pdfUrl = makePdfUrl("SSC_Business_Entrepreneurship_NCTB_2026.pdf"),
            pages = 210,
            fileSizeBytes = 17825792L
        ),
        Book(
            id = "ssc_finance_banking",
            className = "Class 10 (SSC)",
            curriculumId = "nctb_bn",
            groupId = "business_studies",
            subjectId = "finance_banking",
            paperId = "none",
            title = "Finance and Banking (SSC)",
            titleBn = "ফিন্যান্স ও ব্যাংকিং (৯ম-১০ম শ্রেণি)",
            pdfUrl = makePdfUrl("SSC_Finance_Banking_NCTB_2026.pdf"),
            pages = 230,
            fileSizeBytes = 19922944L
        ),
        Book(
            id = "ssc_general_science_bus",
            className = "Class 10 (SSC)",
            curriculumId = "nctb_bn",
            groupId = "business_studies",
            subjectId = "general_science",
            paperId = "none",
            title = "General Science (SSC Commerce & Arts)",
            titleBn = "সাধারণ বিজ্ঞান (ব্যবসায় ও মানবিক শাখা)",
            pdfUrl = makePdfUrl("SSC_General_Science_NCTB_2026.pdf"),
            pages = 250,
            fileSizeBytes = 20971520L
        ),

        // ==========================================
        // CLASS 9 & 10 (Secondary - SSC HUMANITIES)
        // ==========================================
        Book(
            id = "ssc_history",
            className = "Class 10 (SSC)",
            curriculumId = "nctb_bn",
            groupId = "humanities",
            subjectId = "history",
            paperId = "none",
            title = "History of Bangladesh and World Civilization (SSC)",
            titleBn = "বাংলাদেশের ইতিহাস ও বিশ্বসভ্যতা (এসএসসি)",
            pdfUrl = makePdfUrl("SSC_History_NCTB_2026.pdf"),
            pages = 260,
            fileSizeBytes = 22020096L
        ),
        Book(
            id = "ssc_geography",
            className = "Class 10 (SSC)",
            curriculumId = "nctb_bn",
            groupId = "humanities",
            subjectId = "geography",
            paperId = "none",
            title = "Geography and Environment (SSC)",
            titleBn = "ভূগোল ও পরিবেশ (এসএসসি)",
            pdfUrl = makePdfUrl("SSC_Geography_NCTB_2026.pdf"),
            pages = 240,
            fileSizeBytes = 19922944L
        ),
        Book(
            id = "ssc_economics",
            className = "Class 10 (SSC)",
            curriculumId = "nctb_bn",
            groupId = "humanities",
            subjectId = "economics",
            paperId = "none",
            title = "Economics (Secondary SSC)",
            titleBn = "অর্থনীতি (৯ম-১০ম শ্রেণি)",
            pdfUrl = makePdfUrl("SSC_Economics_NCTB_2026.pdf"),
            pages = 220,
            fileSizeBytes = 18874368L
        ),
        Book(
            id = "ssc_civics",
            className = "Class 10 (SSC)",
            curriculumId = "nctb_bn",
            groupId = "humanities",
            subjectId = "civics",
            paperId = "none",
            title = "Civics and Citizenship (SSC)",
            titleBn = "পৌরনীতি ও নাগরিকতা (এসএসসি)",
            pdfUrl = makePdfUrl("SSC_Civics_NCTB_2026.pdf"),
            pages = 210,
            fileSizeBytes = 17825792L
        ),

        // ==========================================
        // CLASS 11 & 12 (Higher Secondary - HSC SCIENCE)
        // ==========================================
        Book(
            id = "hsc_physics_1_bv",
            className = "Class 12 (HSC)",
            curriculumId = "nctb_bn",
            groupId = "science",
            subjectId = "physics_1",
            paperId = "paper_1",
            title = "Physics 1st Paper (HSC - BV)",
            titleBn = "পদার্থবিজ্ঞান ১ম পত্র (একাদশ-দ্বাদশ শ্রেণি)",
            pdfUrl = R2StorageConfig.buildBookUrl("hsc", "bangla", "HSC_Physics_1st_Paper_BV_NCTB_2026.pdf.pdf"),
            pages = 420,
            fileSizeBytes = 35651584L,
            medium = "bangla",
            version = "BV"
        ),
        Book(
            id = "hsc_physics_1_ev",
            className = "Class 12 (HSC)",
            curriculumId = "nctb_en",
            groupId = "science",
            subjectId = "physics_1",
            paperId = "paper_1",
            title = "Physics 1st Paper (HSC - EV)",
            titleBn = "Physics 1st Paper (HSC - EV)",
            pdfUrl = R2StorageConfig.buildBookUrl("hsc", "english", "HSC_Physics_1st_Paper_EV_NCTB_2026.pdf.pdf"),
            pages = 420,
            fileSizeBytes = 35651584L,
            medium = "english",
            version = "EV"
        ),
        Book(
            id = "hsc_physics_2_bv",
            className = "Class 12 (HSC)",
            curriculumId = "nctb_bn",
            groupId = "science",
            subjectId = "physics_2",
            paperId = "paper_2",
            title = "Physics 2nd Paper (HSC - BV)",
            titleBn = "পদার্থবিজ্ঞান ২য় পত্র (একাদশ-দ্বাদশ শ্রেণি)",
            pdfUrl = R2StorageConfig.buildBookUrl("hsc", "bangla", "HSC_Physics_2nd_Paper_BV_NCTB_2026.pdf.pdf"),
            pages = 450,
            fileSizeBytes = 38797312L,
            medium = "bangla",
            version = "BV"
        ),
        Book(
            id = "hsc_physics_2_ev",
            className = "Class 12 (HSC)",
            curriculumId = "nctb_en",
            groupId = "science",
            subjectId = "physics_2",
            paperId = "paper_2",
            title = "Physics 2nd Paper (HSC - EV)",
            titleBn = "Physics 2nd Paper (HSC - EV)",
            pdfUrl = R2StorageConfig.buildBookUrl("hsc", "english", "HSC_Physics_2nd_Paper_EV_NCTB_2026.pdf.pdf"),
            pages = 450,
            fileSizeBytes = 38797312L,
            medium = "english",
            version = "EV"
        ),
        Book(
            id = "hsc_chemistry_1_bv",
            className = "Class 12 (HSC)",
            curriculumId = "nctb_bn",
            groupId = "science",
            subjectId = "chemistry_1",
            paperId = "paper_1",
            title = "Chemistry 1st Paper (HSC - BV)",
            titleBn = "রসায়ন ১ম পত্র (একাদশ-দ্বাদশ শ্রেণি)",
            pdfUrl = R2StorageConfig.buildBookUrl("hsc", "bangla", "HSC_Chemistry_1st_Paper_BV_NCTB_2026.pdf.pdf"),
            pages = 410,
            fileSizeBytes = 34603008L,
            medium = "bangla",
            version = "BV"
        ),
        Book(
            id = "hsc_chemistry_1_ev",
            className = "Class 12 (HSC)",
            curriculumId = "nctb_en",
            groupId = "science",
            subjectId = "chemistry_1",
            paperId = "paper_1",
            title = "Chemistry 1st Paper (HSC - EV)",
            titleBn = "Chemistry 1st Paper (HSC - EV)",
            pdfUrl = R2StorageConfig.buildBookUrl("hsc", "english", "HSC_Chemistry_1st_Paper_EV_NCTB_2026.pdf.pdf"),
            pages = 410,
            fileSizeBytes = 34603008L,
            medium = "english",
            version = "EV"
        ),
        Book(
            id = "hsc_chemistry_2_bv",
            className = "Class 12 (HSC)",
            curriculumId = "nctb_bn",
            groupId = "science",
            subjectId = "chemistry_2",
            paperId = "paper_2",
            title = "Chemistry 2nd Paper (HSC - BV)",
            titleBn = "রসায়ন ২য় পত্র (একাদশ-দ্বাদশ শ্রেণি)",
            pdfUrl = R2StorageConfig.buildBookUrl("hsc", "bangla", "HSC_Chemistry_2nd_Paper_BV_NCTB_2026.pdf.pdf"),
            pages = 430,
            fileSizeBytes = 36700160L,
            medium = "bangla",
            version = "BV"
        ),
        Book(
            id = "hsc_higher_math_1_bv",
            className = "Class 12 (HSC)",
            curriculumId = "nctb_bn",
            groupId = "science",
            subjectId = "higher_math_1",
            paperId = "paper_1",
            title = "Higher Mathematics 1st Paper (HSC - BV)",
            titleBn = "উচ্চতর গণিত ১ম পত্র (একাদশ-দ্বাদশ শ্রেণি)",
            pdfUrl = R2StorageConfig.buildBookUrl("hsc", "bangla", "HSC_Higher_Math_1st_Paper_BV_NCTB_2026.pdf.pdf"),
            pages = 480,
            fileSizeBytes = 41943040L,
            medium = "bangla",
            version = "BV"
        ),
        Book(
            id = "hsc_higher_math_1_ev",
            className = "Class 12 (HSC)",
            curriculumId = "nctb_en",
            groupId = "science",
            subjectId = "higher_math_1",
            paperId = "paper_1",
            title = "Higher Mathematics 1st Paper (HSC - EV)",
            titleBn = "Higher Mathematics 1st Paper (HSC - EV)",
            pdfUrl = R2StorageConfig.buildBookUrl("hsc", "english", "HSC_Higher_Math_1st_Paper_EV_NCTB_2026.pdf.pdf"),
            pages = 480,
            fileSizeBytes = 41943040L,
            medium = "english",
            version = "EV"
        ),
        Book(
            id = "hsc_higher_math_2_bv",
            className = "Class 12 (HSC)",
            curriculumId = "nctb_bn",
            groupId = "science",
            subjectId = "higher_math_2",
            paperId = "paper_2",
            title = "Higher Mathematics 2nd Paper (HSC - BV)",
            titleBn = "উচ্চতর গণিত ২য় পত্র (একাদশ-দ্বাদশ শ্রেণি)",
            pdfUrl = R2StorageConfig.buildBookUrl("hsc", "bangla", "HSC_Higher_Math_2nd_Paper_BV_NCTB_2026.pdf.pdf"),
            pages = 490,
            fileSizeBytes = 42991616L,
            medium = "bangla",
            version = "BV"
        ),
        Book(
            id = "hsc_biology_1_bv",
            className = "Class 12 (HSC)",
            curriculumId = "nctb_bn",
            groupId = "science",
            subjectId = "biology_1",
            paperId = "paper_1",
            title = "Biology 1st Paper (HSC - BV)",
            titleBn = "জীববিজ্ঞান ১ম পত্র: উদ্ভিদবিজ্ঞান (একাদশ-দ্বাদশ)",
            pdfUrl = R2StorageConfig.buildBookUrl("hsc", "bangla", "HSC_Biology_1st_Paper_BV_NCTB_2026.pdf.pdf"),
            pages = 390,
            fileSizeBytes = 33554432L,
            medium = "bangla",
            version = "BV"
        ),
        Book(
            id = "hsc_biology_2_bv",
            className = "Class 12 (HSC)",
            curriculumId = "nctb_bn",
            groupId = "science",
            subjectId = "biology_2",
            paperId = "paper_2",
            title = "Biology 2nd Paper (HSC - BV)",
            titleBn = "জীববিজ্ঞান ২য় পত্র: প্রাণিবিজ্ঞান (একাদশ-দ্বাদশ)",
            pdfUrl = R2StorageConfig.buildBookUrl("hsc", "bangla", "HSC_Biology_2nd_Paper_BV_NCTB_2026.pdf.pdf"),
            pages = 410,
            fileSizeBytes = 35651584L,
            medium = "bangla",
            version = "BV"
        ),
        Book(
            id = "hsc_bangla_1_bv",
            className = "Class 12 (HSC)",
            curriculumId = "nctb_bn",
            groupId = "science",
            subjectId = "bangla_1",
            paperId = "paper_1",
            title = "Sahitya Path Bangla 1st Paper (HSC - BV)",
            titleBn = "সাহিত্য পাঠ বাংলা ১ম পত্র (এইচএসসি)",
            pdfUrl = R2StorageConfig.buildBookUrl("hsc", "bangla", "HSC_Bangla_1st_Paper_BV_NCTB_2026.pdf.pdf"),
            pages = 310,
            fileSizeBytes = 26214400L,
            medium = "bangla",
            version = "BV"
        ),
        Book(
            id = "hsc_bangla_2_bv",
            className = "Class 12 (HSC)",
            curriculumId = "nctb_bn",
            groupId = "science",
            subjectId = "bangla_2",
            paperId = "paper_2",
            title = "Bangla 2nd Paper (HSC - BV)",
            titleBn = "বাংলা ২য় পত্র: ব্যাকরণ ও নির্মিতি (এইচএসসি)",
            pdfUrl = R2StorageConfig.buildBookUrl("hsc", "bangla", "HSC_Bangla_2nd_Paper_BV_NCTB_2026.pdf.pdf"),
            pages = 260,
            fileSizeBytes = 22020096L,
            medium = "bangla",
            version = "BV"
        ),
        Book(
            id = "hsc_english_1_bv",
            className = "Class 12 (HSC)",
            curriculumId = "nctb_bn",
            groupId = "science",
            subjectId = "english_1",
            paperId = "paper_1",
            title = "English for Today 1st Paper (HSC - BV)",
            titleBn = "ইংলিশ ফর টুডে ১ম পত্র (এইচএসসি)",
            pdfUrl = R2StorageConfig.buildBookUrl("hsc", "bangla", "HSC_English_1st_Paper_BV_NCTB_2026.pdf.pdf"),
            pages = 280,
            fileSizeBytes = 24117248L,
            medium = "bangla",
            version = "BV"
        ),
        Book(
            id = "hsc_english_2_bv",
            className = "Class 12 (HSC)",
            curriculumId = "nctb_bn",
            groupId = "science",
            subjectId = "english_2",
            paperId = "paper_2",
            title = "English Grammar & Writing 2nd Paper (HSC - BV)",
            titleBn = "ইংলিশ ২য় পত্র: গ্রামার ও রাইটিং (এইচএসসি)",
            pdfUrl = R2StorageConfig.buildBookUrl("hsc", "bangla", "HSC_English_2nd_Paper_BV_NCTB_2026.pdf.pdf"),
            pages = 290,
            fileSizeBytes = 25165824L,
            medium = "bangla",
            version = "BV"
        ),
        Book(
            id = "hsc_ict_bv",
            className = "Class 12 (HSC)",
            curriculumId = "nctb_bn",
            groupId = "science",
            subjectId = "ict",
            paperId = "none",
            title = "Information & Communication Technology (HSC - BV)",
            titleBn = "তথ্য ও যোগাযোগ প্রযুক্তি (একাদশ-দ্বাদশ শ্রেণি)",
            pdfUrl = R2StorageConfig.buildBookUrl("hsc", "bangla", "HSC_ICT_BV_NCTB_2026.pdf.pdf"),
            pages = 190,
            fileSizeBytes = 16777216L,
            medium = "bangla",
            version = "BV"
        ),
        Book(
            id = "hsc_ict_ev",
            className = "Class 12 (HSC)",
            curriculumId = "nctb_en",
            groupId = "science",
            subjectId = "ict",
            paperId = "none",
            title = "Information & Communication Technology (HSC - EV)",
            titleBn = "ICT (HSC - EV)",
            pdfUrl = R2StorageConfig.buildBookUrl("hsc", "english", "HSC_ICT_EV_NCTB_2026.pdf.pdf"),
            pages = 190,
            fileSizeBytes = 16777216L,
            medium = "english",
            version = "EV"
        ),

        // ==========================================
        // CLASS 11 & 12 (Higher Secondary - HSC BUSINESS STUDIES)
        // ==========================================
        Book(
            id = "hsc_accounting_1",
            className = "Class 12 (HSC)",
            curriculumId = "nctb_bn",
            groupId = "business_studies",
            subjectId = "accounting_1",
            paperId = "paper_1",
            title = "Accounting 1st Paper (HSC)",
            titleBn = "হিসাববিজ্ঞান ১ম পত্র (একাদশ-দ্বাদশ শ্রেণি)",
            pdfUrl = makePdfUrl("HSC_Accounting_1st_Paper_NCTB_2026.pdf"),
            pages = 380,
            fileSizeBytes = 32505856L
        ),
        Book(
            id = "hsc_accounting_2",
            className = "Class 12 (HSC)",
            curriculumId = "nctb_bn",
            groupId = "business_studies",
            subjectId = "accounting_2",
            paperId = "paper_2",
            title = "Accounting 2nd Paper (HSC)",
            titleBn = "হিসাববিজ্ঞান ২য় পত্র (একাদশ-দ্বাদশ শ্রেণি)",
            pdfUrl = makePdfUrl("HSC_Accounting_2nd_Paper_NCTB_2026.pdf"),
            pages = 390,
            fileSizeBytes = 33554432L
        ),
        Book(
            id = "hsc_bom_1",
            className = "Class 12 (HSC)",
            curriculumId = "nctb_bn",
            groupId = "business_studies",
            subjectId = "bom_1",
            paperId = "paper_1",
            title = "Business Organization & Management 1st Paper (HSC)",
            titleBn = "ব্যবসায় সংগঠন ও ব্যবস্থাপনা ১ম পত্র",
            pdfUrl = makePdfUrl("HSC_BOM_1st_Paper_NCTB_2026.pdf"),
            pages = 310,
            fileSizeBytes = 26214400L
        ),
        Book(
            id = "hsc_bom_2",
            className = "Class 12 (HSC)",
            curriculumId = "nctb_bn",
            groupId = "business_studies",
            subjectId = "bom_2",
            paperId = "paper_2",
            title = "Business Organization & Management 2nd Paper (HSC)",
            titleBn = "ব্যবসায় সংগঠন ও ব্যবস্থাপনা ২য় পত্র",
            pdfUrl = makePdfUrl("HSC_BOM_2nd_Paper_NCTB_2026.pdf"),
            pages = 320,
            fileSizeBytes = 27262976L
        ),
        Book(
            id = "hsc_fbi_1",
            className = "Class 12 (HSC)",
            curriculumId = "nctb_bn",
            groupId = "business_studies",
            subjectId = "fbi_1",
            paperId = "paper_1",
            title = "Finance, Banking & Insurance 1st Paper (HSC)",
            titleBn = "ফিন্যান্স, ব্যাংকিং ও বিমা ১ম পত্র",
            pdfUrl = makePdfUrl("HSC_FBI_1st_Paper_NCTB_2026.pdf"),
            pages = 330,
            fileSizeBytes = 28311552L
        ),
        Book(
            id = "hsc_fbi_2",
            className = "Class 12 (HSC)",
            curriculumId = "nctb_bn",
            groupId = "business_studies",
            subjectId = "fbi_2",
            paperId = "paper_2",
            title = "Finance, Banking & Insurance 2nd Paper (HSC)",
            titleBn = "ফিন্যান্স, ব্যাংকিং ও বিমা ২য় পত্র",
            pdfUrl = makePdfUrl("HSC_FBI_2nd_Paper_NCTB_2026.pdf"),
            pages = 340,
            fileSizeBytes = 29360128L
        ),

        // ==========================================
        // CLASS 11 & 12 (Higher Secondary - HSC HUMANITIES)
        // ==========================================
        Book(
            id = "hsc_economics_1",
            className = "Class 12 (HSC)",
            curriculumId = "nctb_bn",
            groupId = "humanities",
            subjectId = "economics_1",
            paperId = "paper_1",
            title = "Economics 1st Paper (HSC)",
            titleBn = "অর্থনীতি ১ম পত্র (একাদশ-দ্বাদশ শ্রেণি)",
            pdfUrl = makePdfUrl("HSC_Economics_1st_Paper_NCTB_2026.pdf"),
            pages = 360,
            fileSizeBytes = 30408704L
        ),
        Book(
            id = "hsc_economics_2",
            className = "Class 12 (HSC)",
            curriculumId = "nctb_bn",
            groupId = "humanities",
            subjectId = "economics_2",
            paperId = "paper_2",
            title = "Economics 2nd Paper (HSC)",
            titleBn = "অর্থনীতি ২য় পত্র (একাদশ-দ্বাদশ শ্রেণি)",
            pdfUrl = makePdfUrl("HSC_Economics_2nd_Paper_NCTB_2026.pdf"),
            pages = 370,
            fileSizeBytes = 31457280L
        ),
        Book(
            id = "hsc_history_1",
            className = "Class 12 (HSC)",
            curriculumId = "nctb_bn",
            groupId = "humanities",
            subjectId = "history_1",
            paperId = "paper_1",
            title = "History 1st Paper (HSC)",
            titleBn = "ইতিহাস ১ম পত্র (একাদশ-দ্বাদশ শ্রেণি)",
            pdfUrl = makePdfUrl("HSC_History_1st_Paper_NCTB_2026.pdf"),
            pages = 340,
            fileSizeBytes = 29360128L
        ),
        Book(
            id = "hsc_history_2",
            className = "Class 12 (HSC)",
            curriculumId = "nctb_bn",
            groupId = "humanities",
            subjectId = "history_2",
            paperId = "paper_2",
            title = "History 2nd Paper (HSC)",
            titleBn = "ইতিহাস ২য় পত্র (একাদশ-দ্বাদশ শ্রেণি)",
            pdfUrl = makePdfUrl("HSC_History_2nd_Paper_NCTB_2026.pdf"),
            pages = 350,
            fileSizeBytes = 30408704L
        ),
        Book(
            id = "hsc_civics_1",
            className = "Class 12 (HSC)",
            curriculumId = "nctb_bn",
            groupId = "humanities",
            subjectId = "civics_1",
            paperId = "paper_1",
            title = "Civics and Good Governance 1st Paper (HSC)",
            titleBn = "পৌরনীতি ও সুশাসন ১ম পত্র (এইচএসসি)",
            pdfUrl = makePdfUrl("HSC_Civics_1st_Paper_NCTB_2026.pdf"),
            pages = 330,
            fileSizeBytes = 28311552L
        ),
        Book(
            id = "hsc_civics_2",
            className = "Class 12 (HSC)",
            curriculumId = "nctb_bn",
            groupId = "humanities",
            subjectId = "civics_2",
            paperId = "paper_2",
            title = "Civics and Good Governance 2nd Paper (HSC)",
            titleBn = "পৌরনীতি ও সুশাসন ২য় পত্র (এইচএসসি)",
            pdfUrl = makePdfUrl("HSC_Civics_2nd_Paper_NCTB_2026.pdf"),
            pages = 340,
            fileSizeBytes = 29360128L
        ),
        Book(
            id = "hsc_logic",
            className = "Class 12 (HSC)",
            curriculumId = "nctb_bn",
            groupId = "humanities",
            subjectId = "logic",
            paperId = "none",
            title = "Logic (HSC Humanities)",
            titleBn = "যুক্তিবিদ্যা (একাদশ-দ্বাদশ শ্রেণি)",
            pdfUrl = makePdfUrl("HSC_Logic_NCTB_2026.pdf"),
            pages = 310,
            fileSizeBytes = 26214400L
        )
    )

    override fun getBooksForContext(
        className: String,
        curriculumId: String,
        groupId: String,
        subjectId: String,
        paperId: String,
        chapterId: String
    ): List<Book> {
        val normClass = normalizeClassName(className)
        val normGroup = groupId.lowercase().trim()
        val normSubject = subjectId.lowercase().trim()
        val normPaper = paperId.lowercase().trim()
        val normCurriculum = curriculumId.lowercase().trim()

        val isJunior = AcademicCatalog.isJuniorClass(className)
        val isSecondary = AcademicCatalog.isSecondaryClass(className)
        val isHigherSecondary = AcademicCatalog.isHigherSecondaryClass(className)

        return books.filter { book ->
            // 1. Match education level
            val bookMatchesClass = when {
                isJunior -> AcademicCatalog.isJuniorClass(book.className) &&
                        (normClass.isEmpty() || book.className.equals(normClass, ignoreCase = true))
                isSecondary -> AcademicCatalog.isSecondaryClass(book.className)
                isHigherSecondary -> AcademicCatalog.isHigherSecondaryClass(book.className)
                else -> book.className.contains(normClass, ignoreCase = true)
            }
            if (!bookMatchesClass) return@filter false

            // 2. Match curriculum (if specified)
            if (normCurriculum.isNotEmpty()) {
                val matchesCurriculum = book.curriculumId.contains(normCurriculum, ignoreCase = true) ||
                        normCurriculum.contains(book.curriculumId, ignoreCase = true) ||
                        (normCurriculum.contains("bangla") && book.curriculumId.contains("bn")) ||
                        (normCurriculum.contains("english") && book.curriculumId.contains("en"))
                if (!matchesCurriculum) return@filter false
            }

            // 3. Match group (Junior ignores stream group and strictly matches general_junior)
            if (!isJunior && normGroup.isNotEmpty()) {
                val groupMatches = book.groupId.equals(normGroup, ignoreCase = true) ||
                        book.group.id.equals(normGroup, ignoreCase = true) ||
                        isCommonSubjectAcrossGroups(book.subjectId)
                if (!groupMatches) return@filter false
            }

            // 4. Match subject ID (canonical matching)
            if (normSubject.isNotEmpty()) {
                val subjectMatches = book.subjectId.equals(normSubject, ignoreCase = true) ||
                        (normSubject == "physics" && (book.subjectId == "physics_1" || book.subjectId == "physics_2" || book.subjectId == "physics")) ||
                        (normSubject == "chemistry" && (book.subjectId == "chemistry_1" || book.subjectId == "chemistry_2" || book.subjectId == "chemistry")) ||
                        (normSubject == "higher_math" && (book.subjectId == "higher_math_1" || book.subjectId == "higher_math_2" || book.subjectId == "higher_math")) ||
                        (normSubject == "biology" && (book.subjectId == "biology_1" || book.subjectId == "biology_2" || book.subjectId == "biology")) ||
                        (normSubject == "accounting" && (book.subjectId == "accounting_1" || book.subjectId == "accounting_2" || book.subjectId == "accounting")) ||
                        (normSubject == "economics" && (book.subjectId == "economics_1" || book.subjectId == "economics_2" || book.subjectId == "economics")) ||
                        (normSubject == "history" && (book.subjectId == "history_1" || book.subjectId == "history_2" || book.subjectId == "history")) ||
                        (normSubject == "civics" && (book.subjectId == "civics_1" || book.subjectId == "civics_2" || book.subjectId == "civics"))
                if (!subjectMatches) return@filter false
            }

            // 5. Match Paper ID (Paper 1 vs Paper 2 separation)
            if (normPaper.isNotEmpty() && normPaper != "none") {
                val paperMatches = book.paperId.equals(normPaper, ignoreCase = true) ||
                        (normPaper == "paper_1" && book.subjectId.endsWith("_1")) ||
                        (normPaper == "paper_2" && book.subjectId.endsWith("_2"))
                if (!paperMatches) return@filter false
            }

            // 6. Match Chapter ID (if specified)
            if (chapterId.isNotEmpty() && book.chapterId.isNotEmpty()) {
                if (!book.chapterId.equals(chapterId, ignoreCase = true)) return@filter false
            }

            true
        }
    }

    override fun getBookById(id: String): Book? {
        val cleanId = id.trim()
        val exact = books.find { it.id.equals(cleanId, ignoreCase = true) }
        if (exact != null) return exact

        // Legacy ID alias resolution
        val aliasId = when (cleanId) {
            "c6_bangla" -> "c6_charupath_bv"
            "c6_english" -> "c6_english_bv"
            "c6_math" -> "c6_math_bv"
            "c6_science" -> "c6_science_bv"
            "c6_bgs" -> "c6_bgs_bv"
            "c6_ict" -> "c6_ict_bv"
            "c7_bangla" -> "c7_saptavarna_bv"
            "c7_english" -> "c7_english_bv"
            "c7_math" -> "c7_math_bv"
            "c7_science" -> "c7_science_bv"
            "c7_bgs" -> "c7_bgs_bv"
            "c7_ict" -> "c7_ict_bv"
            "c8_bangla" -> "c8_bangla_bv"
            "c8_english" -> "c8_english_bv"
            "c8_math" -> "c8_math_bv"
            "c8_science" -> "c8_science_bv"
            "c8_bgs" -> "c8_bgs_bv"
            "c8_ict" -> "c8_ict_bv"
            "ssc_physics" -> "ssc_physics_bv"
            "ssc_chemistry" -> "ssc_chemistry_bv"
            "ssc_biology" -> "ssc_biology_bv"
            "ssc_higher_math" -> "ssc_higher_math_bv"
            "ssc_math" -> "ssc_math_bv"
            "ssc_bangla_1" -> "ssc_bangla_1_bv"
            "ssc_bangla" -> "ssc_bangla_1_bv"
            "ssc_english_1" -> "ssc_english_1_bv"
            "ssc_english" -> "ssc_english_1_bv"
            "ssc_ict" -> "ssc_ict_bv"
            "ssc_bgs" -> "ssc_bgs_bv"
            "hsc_physics_1" -> "hsc_physics_1_bv"
            "hsc_physics_2" -> "hsc_physics_2_bv"
            "hsc_physics" -> "hsc_physics_1_bv"
            "hsc_chemistry_1" -> "hsc_chemistry_1_bv"
            "hsc_chemistry_2" -> "hsc_chemistry_2_bv"
            "hsc_chemistry" -> "hsc_chemistry_1_bv"
            "hsc_higher_math_1" -> "hsc_higher_math_1_bv"
            "hsc_higher_math_2" -> "hsc_higher_math_2_bv"
            "hsc_higher_math" -> "hsc_higher_math_1_bv"
            "hsc_biology_1" -> "hsc_biology_1_bv"
            "hsc_biology_2" -> "hsc_biology_2_bv"
            "hsc_biology" -> "hsc_biology_1_bv"
            "hsc_bangla_1" -> "hsc_bangla_1_bv"
            "hsc_bangla_2" -> "hsc_bangla_2_bv"
            "hsc_english_1" -> "hsc_english_1_bv"
            "hsc_english_2" -> "hsc_english_2_bv"
            "hsc_ict" -> "hsc_ict_bv"
            else -> cleanId
        }
        return books.find { it.id.equals(aliasId, ignoreCase = true) }
    }

    override fun getBooksBySubject(subjectId: String, className: String): List<Book> {
        val cleanSubj = subjectId.lowercase().trim()
        val isJunior = className.isNotEmpty() && AcademicCatalog.isJuniorClass(className)
        val isSecondary = className.isNotEmpty() && AcademicCatalog.isSecondaryClass(className)
        val isHigherSecondary = className.isNotEmpty() && AcademicCatalog.isHigherSecondaryClass(className)

        return books.filter { book ->
            val matchClass = when {
                className.isEmpty() -> true
                isJunior -> AcademicCatalog.isJuniorClass(book.className)
                isSecondary -> AcademicCatalog.isSecondaryClass(book.className)
                isHigherSecondary -> AcademicCatalog.isHigherSecondaryClass(book.className)
                else -> book.className.contains(className, ignoreCase = true)
            }
            matchClass && (book.subjectId.equals(cleanSubj, ignoreCase = true) ||
                    book.id.contains(cleanSubj, ignoreCase = true))
        }
    }

    override fun getAllBooks(): List<Book> = books

    override fun searchBooks(query: String): List<Book> {
        val q = query.lowercase().trim()
        if (q.isEmpty()) return books
        return books.filter {
            it.title.lowercase().contains(q) ||
                    it.titleBn.lowercase().contains(q) ||
                    it.subjectId.lowercase().contains(q) ||
                    it.className.lowercase().contains(q) ||
                    it.author.lowercase().contains(q)
        }
    }

    private fun normalizeClassName(rawClass: String): String {
        val lower = rawClass.lowercase().trim()
        return when {
            lower.contains("class 6") -> "Class 6"
            lower.contains("class 7") -> "Class 7"
            lower.contains("class 8") -> "Class 8"
            lower.contains("class 9") -> "Class 9"
            lower.contains("class 10") || lower.contains("ssc") -> "Class 10 (SSC)"
            lower.contains("class 11") -> "Class 11"
            lower.contains("class 12") || lower.contains("hsc") -> "Class 12 (HSC)"
            else -> rawClass
        }
    }

    private fun isCommonSubjectAcrossGroups(subjectId: String): Boolean {
        val s = subjectId.lowercase()
        return s == "bangla" || s == "bangla_1" || s == "bangla_2" ||
                s == "english" || s == "english_1" || s == "english_2" ||
                s == "ict" || s == "math" || s == "bgs"
    }
}
