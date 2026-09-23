package com.example

import com.example.data.BookCatalog
import com.example.model.AcademicGroup
import com.example.model.SubjectPaper
import org.junit.Assert.*
import org.junit.Test

class BookCatalogUnitTest {

    @Test
    fun testJuniorClassFiltering_Class8() {
        val books = BookCatalog.getBooksForContext(
            className = "Class 8",
            curriculumId = "nctb_bn",
            groupId = "general_junior"
        )
        assertTrue(books.isNotEmpty())

        val subjectIds = books.map { it.subjectId }.toSet()
        assertTrue(subjectIds.contains("bangla"))
        assertTrue(subjectIds.contains("english"))
        assertTrue(subjectIds.contains("math"))
        assertTrue(subjectIds.contains("science"))
        assertTrue(subjectIds.contains("bgs"))
        assertTrue(subjectIds.contains("ict"))

        // Senior subjects must NOT exist in Class 8
        assertFalse(subjectIds.contains("physics"))
        assertFalse(subjectIds.contains("chemistry"))
        assertFalse(subjectIds.contains("higher_math"))
        assertFalse(subjectIds.contains("accounting"))
    }

    @Test
    fun testSecondaryClass10_ScienceGroup() {
        val scienceBooks = BookCatalog.getBooksForContext(
            className = "Class 10 (SSC)",
            curriculumId = "nctb_bn",
            groupId = "science"
        )
        assertTrue(scienceBooks.isNotEmpty())

        val subjectIds = scienceBooks.map { it.subjectId }.toSet()
        assertTrue(subjectIds.contains("physics"))
        assertTrue(subjectIds.contains("chemistry"))
        assertTrue(subjectIds.contains("biology"))
        assertTrue(subjectIds.contains("higher_math"))
        assertTrue(subjectIds.contains("math"))

        // Commerce/Arts specific subjects must NOT exist in Science
        assertFalse(subjectIds.contains("accounting"))
        assertFalse(subjectIds.contains("business_entrepreneurship"))
        assertFalse(subjectIds.contains("finance_banking"))
        assertFalse(subjectIds.contains("economics"))
        assertFalse(subjectIds.contains("history"))
        assertFalse(subjectIds.contains("civics"))
    }

    @Test
    fun testSecondaryClass10_BusinessStudiesGroup() {
        val businessBooks = BookCatalog.getBooksForContext(
            className = "Class 10 (SSC)",
            curriculumId = "nctb_bn",
            groupId = "business_studies"
        )
        assertTrue(businessBooks.isNotEmpty())

        val subjectIds = businessBooks.map { it.subjectId }.toSet()
        assertTrue(subjectIds.contains("accounting"))
        assertTrue(subjectIds.contains("business_entrepreneurship"))
        assertTrue(subjectIds.contains("finance_banking"))

        // Science subjects must NOT exist in Business
        assertFalse(subjectIds.contains("physics"))
        assertFalse(subjectIds.contains("chemistry"))
        assertFalse(subjectIds.contains("higher_math"))
        assertFalse(subjectIds.contains("biology"))
    }

    @Test
    fun testSecondaryClass10_HumanitiesGroup() {
        val humanitiesBooks = BookCatalog.getBooksForContext(
            className = "Class 10 (SSC)",
            curriculumId = "nctb_bn",
            groupId = "humanities"
        )
        assertTrue(humanitiesBooks.isNotEmpty())

        val subjectIds = humanitiesBooks.map { it.subjectId }.toSet()
        assertTrue(subjectIds.contains("history"))
        assertTrue(subjectIds.contains("geography"))
        assertTrue(subjectIds.contains("economics"))
        assertTrue(subjectIds.contains("civics"))

        // Science & Business must not leak
        assertFalse(subjectIds.contains("physics"))
        assertFalse(subjectIds.contains("accounting"))
    }

    @Test
    fun testHigherSecondaryClass12_PaperSeparation_Physics() {
        val paper1Books = BookCatalog.getBooksForContext(
            className = "Class 12 (HSC)",
            curriculumId = "nctb_bn",
            groupId = "science",
            subjectId = "physics",
            paperId = "paper_1"
        )
        assertEquals(1, paper1Books.size)
        val p1 = paper1Books.first()
        assertEquals("physics_1", p1.subjectId)
        assertEquals("paper_1", p1.paperId)
        assertTrue(p1.title.contains("1st Paper"))

        val paper2Books = BookCatalog.getBooksForContext(
            className = "Class 12 (HSC)",
            curriculumId = "nctb_bn",
            groupId = "science",
            subjectId = "physics",
            paperId = "paper_2"
        )
        assertEquals(1, paper2Books.size)
        val p2 = paper2Books.first()
        assertEquals("physics_2", p2.subjectId)
        assertEquals("paper_2", p2.paperId)
        assertTrue(p2.title.contains("2nd Paper"))

        // IDs and URLs must be distinct
        assertNotEquals(p1.id, p2.id)
        assertNotEquals(p1.pdfUrl, p2.pdfUrl)
    }

    @Test
    fun testHigherSecondaryClass12_PaperSeparation_Accounting() {
        val acc1Books = BookCatalog.getBooksForContext(
            className = "Class 12 (HSC)",
            curriculumId = "nctb_bn",
            groupId = "business_studies",
            subjectId = "accounting",
            paperId = "paper_1"
        )
        assertEquals(1, acc1Books.size)
        assertEquals("accounting_1", acc1Books.first().subjectId)

        val acc2Books = BookCatalog.getBooksForContext(
            className = "Class 12 (HSC)",
            curriculumId = "nctb_bn",
            groupId = "business_studies",
            subjectId = "accounting",
            paperId = "paper_2"
        )
        assertEquals(1, acc2Books.size)
        assertEquals("accounting_2", acc2Books.first().subjectId)
    }

    @Test
    fun testHigherSecondaryClass12_PaperSeparation_Economics() {
        val econ1Books = BookCatalog.getBooksForContext(
            className = "Class 12 (HSC)",
            curriculumId = "nctb_bn",
            groupId = "humanities",
            subjectId = "economics",
            paperId = "paper_1"
        )
        assertEquals(1, econ1Books.size)
        assertEquals("economics_1", econ1Books.first().subjectId)

        val econ2Books = BookCatalog.getBooksForContext(
            className = "Class 12 (HSC)",
            curriculumId = "nctb_bn",
            groupId = "humanities",
            subjectId = "economics",
            paperId = "paper_2"
        )
        assertEquals(1, econ2Books.size)
        assertEquals("economics_2", econ2Books.first().subjectId)
    }

    @Test
    fun testBookPdfUrlsAreCloudReady() {
        val allBooks = BookCatalog.getAllBooks()
        assertTrue(allBooks.isNotEmpty())
        for (book in allBooks) {
            assertTrue("Book ${book.id} must have remote HTTP/HTTPS or GS URL",
                book.pdfUrl.startsWith("http://") || book.pdfUrl.startsWith("https://") || book.pdfUrl.startsWith("gs://"))
            assertFalse("Book ${book.id} must not reference file:// or assets/",
                book.pdfUrl.startsWith("file://") || book.pdfUrl.contains("assets/"))
            assertTrue("Book ${book.id} must have non-empty title and class",
                book.title.isNotBlank() && book.className.isNotBlank())
        }
    }

    @Test
    fun testSearchBooks() {
        val searchResults = BookCatalog.searchBooks("Physics")
        assertTrue(searchResults.isNotEmpty())
        assertTrue(searchResults.all {
            it.title.contains("Physics", ignoreCase = true) ||
            it.subjectId.contains("physics", ignoreCase = true)
        })

        val banglaResults = BookCatalog.searchBooks("হিসাববিজ্ঞান")
        assertTrue(banglaResults.isNotEmpty())
        assertTrue(banglaResults.any { it.titleBn.contains("হিসাববিজ্ঞান") })
    }

    @Test
    fun testGetBookById() {
        val book = BookCatalog.getBookById("ssc_physics")
        assertNotNull(book)
        assertEquals("Class 10 (SSC)", book?.className)
        assertEquals("physics", book?.subjectId)
        assertEquals("science", book?.groupId)
    }

    @Test
    fun testBookStorageManager_LocalFileGenerationAndOfflineCheck() {
        val book = BookCatalog.getBookById("ssc_physics")!!
        assertNotNull(book.pdfUrl)
        assertTrue(book.pdfUrl.startsWith("https://"))
        
        // Ensure toPdfDocument conversion preserves metadata
        val pdfDoc = book.toPdfDocument()
        assertEquals(book.id, pdfDoc.id)
        assertEquals("${book.title}.pdf", pdfDoc.fileName)
        assertEquals(book.pages, pdfDoc.pages)
    }

    @Test
    fun testBookAiTeacherEngine_AnswersQuestionFromTextbook() {
        val context = com.example.model.TopicAiContext(
            educationLevel = "Class 10 (SSC)",
            curriculum = "NCTB (Bangla Version)",
            subjectId = "physics",
            subjectName = "Physics",
            chapterId = "ssc_phy_ch2",
            chapterTitle = "Motion",
            topicId = "ssc_phy_ch2_t1",
            topicTitle = "Scalar and Vector Quantities",
            topicTitleBn = "স্কেলার ও ভেক্টর রাশি",
            language = "বাংলা",
            groupId = "science",
            group = AcademicGroup.SCIENCE
        )

        val book = BookCatalog.getBookById("ssc_physics")
        val answer = com.example.data.BookAiTeacherEngine.generateBookAnswer(
            query = "স্কেলার ও ভেক্টর রাশি বলতে কী বোঝায়?",
            academicContext = context,
            activeBook = book
        )

        assertNotNull(answer)
        assertFalse(answer.isOutOfScope)
        assertNotNull(answer.sourceBookContext)
        assertEquals("Physics (SSC - BV)", answer.sourceBookContext?.bookTitle)
        assertTrue(answer.stepByStepExplanation.isNotEmpty())
        assertTrue(answer.text.contains("স্কেলার") || answer.text.contains("ভেক্টর") || answer.text.contains("Physics"))
    }

    @Test
    fun testBookAiTeacherEngine_DetectsOutOfScopeQuestionTruthfully() {
        val context = com.example.model.TopicAiContext(
            educationLevel = "Class 8",
            curriculum = "NCTB (Bangla Version)",
            subjectId = "math",
            subjectName = "Mathematics",
            chapterId = "c8_math_ch1",
            chapterTitle = "Pattern",
            topicId = "c8_math_ch1_t1",
            topicTitle = "Number Patterns",
            topicTitleBn = "সংখ্যার প্যাটার্ন",
            language = "বাংলা",
            groupId = "general_junior",
            group = AcademicGroup.GENERAL_JUNIOR
        )

        val book = BookCatalog.getBookById("c8_math")
        val answer = com.example.data.BookAiTeacherEngine.generateBookAnswer(
            query = "weather in London tomorrow cricket score",
            academicContext = context,
            activeBook = book
        )

        assertNotNull(answer)
        assertTrue(answer.isOutOfScope)
        assertTrue(answer.text.contains("পাঠ্যবইয়ে তথ্য পাওয়া যায়নি") || answer.text.contains("Not Found in Textbook"))
    }

    @Test
    fun testAndroidTtsSanitizer() {
        val markdown = "**Bold Title** with *italic* and `code` • bullet point ⇒ সুতরাং"
        val clean = com.example.util.AndroidTtsManager.sanitizeTextForSpeech(markdown)
        assertFalse(clean.contains("**"))
        assertFalse(clean.contains("`"))
        assertFalse(clean.contains("•"))
        assertTrue(clean.contains("Bold Title"))
    }

    @Test
    fun testAllCatalogBooksHaveValidDirectPdfUrls() {
        val allBooks = BookCatalog.getAllBooks()
        assertTrue("Book catalog must not be empty", allBooks.isNotEmpty())

        for (book in allBooks) {
            assertTrue("Book ID must not be blank: ${book.id}", book.id.isNotBlank())
            assertTrue("Class name must not be blank: ${book.id}", book.className.isNotBlank())
            assertTrue("Title must not be blank: ${book.id}", book.title.isNotBlank())
            assertTrue("SubjectId must not be blank: ${book.id}", book.subjectId.isNotBlank())
            assertTrue("PDF URL must start with http/https: ${book.id} -> ${book.pdfUrl}", book.pdfUrl.startsWith("http"))
            assertTrue("PDF URL must contain .pdf: ${book.id} -> ${book.pdfUrl}", book.pdfUrl.contains(".pdf", ignoreCase = true))
        }
    }

    @Test
    fun testBookToTopicAiContextMapping() {
        val book = BookCatalog.getBookById("ssc_physics")
        assertNotNull(book)
        val context = book!!.toTopicAiContext("বাংলা")

        assertEquals("Class 10 (SSC)", context.educationLevel)
        assertEquals("physics", context.subjectId)
        assertEquals(book.title, context.subjectName)
        assertEquals(AcademicGroup.SCIENCE, context.group)
        assertEquals(SubjectPaper.NONE, context.paper)
        assertEquals("ssc_physics_bv", context.bookId)
        assertEquals("বাংলা", context.language)
    }

    @Test
    fun testBookStorageManager_LocalFilePathResolution() {
        val tempDir = java.nio.file.Files.createTempDirectory("reve_test").toFile()
        val mockContext = object : android.content.ContextWrapper(null) {
            override fun getFilesDir(): java.io.File = tempDir
        }

        val book = BookCatalog.getBookById("ssc_physics")
        assertNotNull(book)

        val localFile = com.example.data.BookStorageManager.getLocalFile(mockContext, book!!)
        assertTrue("Local path must contain downloaded_textbooks folder", localFile.absolutePath.contains("downloaded_textbooks"))
        assertEquals("ssc_physics_bv_2026_edition.pdf", localFile.name)
        assertFalse("File must not exist initially", com.example.data.BookStorageManager.isBookDownloaded(mockContext, book))
        assertNull("getCachedFileOrNull must be null when not downloaded", com.example.data.BookStorageManager.getCachedFileOrNull(mockContext, book))

        // Create a simulated cached PDF file
        localFile.parentFile?.mkdirs()
        val validPdfContent = "%PDF-1.7 Valid test PDF content for local storage cache validation".toByteArray()
        localFile.writeBytes(validPdfContent)

        assertTrue("isBookDownloaded must be true when cached file exists", com.example.data.BookStorageManager.isBookDownloaded(mockContext, book))
        val cached = com.example.data.BookStorageManager.getCachedFileOrNull(mockContext, book)
        assertNotNull("getCachedFileOrNull must return the cached file", cached)
        assertEquals(localFile.absolutePath, cached!!.absolutePath)
        assertEquals(validPdfContent.size.toLong(), com.example.data.BookStorageManager.getDownloadedBookSizeBytes(mockContext, book))

        // Test delete
        val deleted = com.example.data.BookStorageManager.deleteDownloadedBook(mockContext, book)
        assertTrue("deleteDownloadedBook should return true", deleted)
        assertFalse("isBookDownloaded should be false after delete", com.example.data.BookStorageManager.isBookDownloaded(mockContext, book))

        tempDir.deleteRecursively()
    }

    @Test
    fun testBookStorageManager_RejectsCorruptOrHtml404File() {
        val tempDir = java.nio.file.Files.createTempDirectory("reve_test_corrupt").toFile()
        val mockContext = object : android.content.ContextWrapper(null) {
            override fun getFilesDir(): java.io.File = tempDir
        }

        val book = BookCatalog.getBookById("ssc_chemistry")!!
        val localFile = com.example.data.BookStorageManager.getLocalFile(mockContext, book)
        localFile.parentFile?.mkdirs()

        // Write HTML 404 response content (not a PDF)
        val html404Content = "<html><body><h1>404 Not Found</h1></body></html>".toByteArray()
        localFile.writeBytes(html404Content)

        assertFalse("isValidPdfFile must return false for HTML error content", com.example.data.BookStorageManager.isValidPdfFile(localFile))
        assertFalse("isBookDownloaded must return false and clean up corrupt file", com.example.data.BookStorageManager.isBookDownloaded(mockContext, book))
        assertNull("getCachedFileOrNull must be null for non-PDF file", com.example.data.BookStorageManager.getCachedFileOrNull(mockContext, book))
        assertFalse("Invalid file must be deleted upon failed validation", localFile.exists())

        tempDir.deleteRecursively()
    }

    @Test
    fun testPhysicsChemistryBiologyMathematicsUrlsLoggedAndValid() {
        val physicsBooks = BookCatalog.getAllBooks().filter { it.subjectId.contains("physic", ignoreCase = true) }
        val chemistryBooks = BookCatalog.getAllBooks().filter { it.subjectId.contains("chem", ignoreCase = true) }
        val biologyBooks = BookCatalog.getAllBooks().filter { it.subjectId.contains("bio", ignoreCase = true) }
        val mathBooks = BookCatalog.getAllBooks().filter { it.subjectId.contains("math", ignoreCase = true) }

        assertTrue("Physics books must exist in catalog", physicsBooks.isNotEmpty())
        assertTrue("Chemistry books must exist in catalog", chemistryBooks.isNotEmpty())
        assertTrue("Biology books must exist in catalog", biologyBooks.isNotEmpty())
        assertTrue("Mathematics books must exist in catalog", mathBooks.isNotEmpty())

        val checkedSubjects = listOf(
            "Physics" to physicsBooks,
            "Chemistry" to chemistryBooks,
            "Biology" to biologyBooks,
            "Mathematics" to mathBooks
        )

        for ((subjectName, books) in checkedSubjects) {
            for (b in books) {
                println("Logged PDF URL for $subjectName [${b.id}] (${b.className}): ${b.pdfUrl}")
                assertTrue("URL for ${b.id} must start with http", b.pdfUrl.startsWith("http"))
                assertTrue("URL for ${b.id} must be valid", b.pdfUrl.contains(".pdf"))
            }
        }
    }

    @Test
    fun testBookStorageManager_EnsureBookAvailable_UsesCacheFirst() {
        kotlinx.coroutines.runBlocking {
            val tempDir = java.nio.file.Files.createTempDirectory("reve_test_cached").toFile()
            val mockContext = object : android.content.ContextWrapper(null) {
                override fun getFilesDir(): java.io.File = tempDir
            }

            val book = BookCatalog.getBookById("ssc_physics")!!
            val localFile = com.example.data.BookStorageManager.getLocalFile(mockContext, book)
            localFile.parentFile?.mkdirs()
            val dummyPdfHeader = "%PDF-1.7 cached local content with valid length".toByteArray()
            localFile.writeBytes(dummyPdfHeader)

            // When cached file exists, ensureBookAvailable must return the local file without network request
            val resultFile = com.example.data.BookStorageManager.ensureBookAvailable(mockContext, book, forceDownload = false)
            assertEquals(localFile.absolutePath, resultFile.absolutePath)
            assertTrue(resultFile.exists())
            assertEquals(dummyPdfHeader.size.toLong(), resultFile.length())

            tempDir.deleteRecursively()
        }
    }

    @Test
    fun testClass6_CharupathBookEntryAndMetadata() {
        val c6Charupath = BookCatalog.getBookById("c6_charupath_bv")
        assertNotNull("Class 6 Charupath BV must exist", c6Charupath)
        c6Charupath!!
        assertTrue("Title must be Charupath", c6Charupath.title.contains("Charupath"))
        assertTrue("TitleBn must contain চারুপাঠ", c6Charupath.titleBn.contains("চারুপাঠ"))
        assertEquals("Class 6", c6Charupath.className)
        assertEquals("2026 Edition", c6Charupath.edition)
        assertEquals(110, c6Charupath.pages)
        assertTrue("PDF URL must point to Cloudflare R2 bucket", c6Charupath.pdfUrl.startsWith("https://pub-e0fbc7b94d124c9091aacdb9b29e3855.r2.dev/"))
        assertTrue("PDF URL must contain Class6_Charupath_BV_NCTB_2026.pdf.pdf", c6Charupath.pdfUrl.contains("Class6_Charupath_BV_NCTB_2026.pdf.pdf"))

        val c7English = BookCatalog.getBookById("c7_english")
        assertNotNull("Class 7 English book must exist", c7English)
        c7English!!
        assertEquals("English for Today (Class 7 - BV)", c7English.title)
        assertEquals("2026 Edition", c7English.edition)
        assertEquals(122, c7English.pages)

        val aiContext = c6Charupath.toTopicAiContext()
        assertEquals("c6_charupath_bv", aiContext.bookId)
        assertEquals(c6Charupath.title, aiContext.bookTitle)
        assertEquals("2026 Edition", aiContext.bookEdition)
    }

    @Test
    fun testR2UrlBuilderExactFormatForClass6BvAndEv() {
        val bvMathUrl = com.example.data.R2StorageConfig.buildBookUrl("class6", "bangla", "Class6_Math_BV_NCTB_2026.pdf.pdf")
        val evMathUrl = com.example.data.R2StorageConfig.buildBookUrl("class6", "english", "Class6_Math_EV_NCTB_2026.pdf.pdf")

        assertEquals(
            "https://pub-e0fbc7b94d124c9091aacdb9b29e3855.r2.dev/class6/bangla/Class6_Math_BV_NCTB_2026.pdf.pdf",
            bvMathUrl
        )
        assertEquals(
            "https://pub-e0fbc7b94d124c9091aacdb9b29e3855.r2.dev/class6/english/Class6_Math_EV_NCTB_2026.pdf.pdf",
            evMathUrl
        )
    }

    @Test
    fun testClass6BvBookListExactFilenames() {
        val class6BvBooks = BookCatalog.getBooksForContext(className = "Class 6", curriculumId = "nctb_bn")
        assertEquals(9, class6BvBooks.size)

        val expectedBvFilenames = listOf(
            "Class6_Anandapath_BV_NCTB_2026.pdf.pdf",
            "Class6_Bangla_Byakoron_Nitimala_BV_NCTB_2026.pdf.pdf",
            "Class6_Bangladesh_Global_Studies_BV_NCTB_2026.pdf.pdf",
            "Class6_Charupath_BV_NCTB_2026.pdf.pdf",
            "Class6_English_For_Today_BV_NCTB_2026.pdf.pdf",
            "Class6_English_Grammar_Composition_BV_NCTB_2026.pdf.pdf",
            "Class6_ICT_BV_NCTB_2026.pdf.pdf",
            "Class6_Math_BV_NCTB_2026.pdf.pdf",
            "Class6_Science_BV_NCTB_2026.pdf.pdf"
        )

        for (expected in expectedBvFilenames) {
            val found = class6BvBooks.any { it.pdfUrl.endsWith("/class6/bangla/$expected") }
            assertTrue("Class 6 BV book list must contain R2 URL ending with /class6/bangla/$expected", found)
        }
    }

    @Test
    fun testClass6EvBookListExactFilenames() {
        val class6EvBooks = BookCatalog.getBooksForContext(className = "Class 6", curriculumId = "nctb_en")
        assertEquals(8, class6EvBooks.size)

        val expectedEvFilenames = listOf(
            "Class6_Anandapath_EV_NCTB_2026.pdf.pdf",
            "Class6_Bangla_Byakoron_Nitimala_EV_NCTB_2026.pdf.pdf",
            "Class6_Bangladesh_Global_Studies_EV_NCTB_2026.pdf.pdf",
            "Class6_Charupath_EV_NCTB_2026.pdf.pdf",
            "Class6_English_For_Today_EV_NCTB_2026.pdf.pdf",
            "Class6_ICT_EV_NCTB_2026.pdf.pdf",
            "Class6_Math_EV_NCTB_2026.pdf.pdf",
            "Class6_Science_EV_NCTB_2026.pdf.pdf"
        )

        for (expected in expectedEvFilenames) {
            val found = class6EvBooks.any { it.pdfUrl.endsWith("/class6/english/$expected") }
            assertTrue("Class 6 EV book list must contain R2 URL ending with /class6/english/$expected", found)
        }
    }

    @Test
    fun testScalableR2UrlBuilderForFutureLevels() {
        val class7Url = com.example.data.R2StorageConfig.buildBookUrl("class7", "bangla", "Class7_Math_BV_NCTB_2026.pdf")
        val class8Url = com.example.data.R2StorageConfig.buildBookUrl("class8", "english", "Class8_Science_EV_NCTB_2026.pdf")
        val sscUrl = com.example.data.R2StorageConfig.buildBookUrl("ssc", "bangla", "SSC_Physics_NCTB_2026.pdf")
        val hscUrl = com.example.data.R2StorageConfig.buildBookUrl("hsc", "bangla", "HSC_Physics_1st_Paper_NCTB_2026.pdf")

        assertEquals("https://pub-e0fbc7b94d124c9091aacdb9b29e3855.r2.dev/class7/bangla/Class7_Math_BV_NCTB_2026.pdf", class7Url)
        assertEquals("https://pub-e0fbc7b94d124c9091aacdb9b29e3855.r2.dev/class8/english/Class8_Science_EV_NCTB_2026.pdf", class8Url)
        assertEquals("https://pub-e0fbc7b94d124c9091aacdb9b29e3855.r2.dev/ssc/bangla/SSC_Physics_NCTB_2026.pdf", sscUrl)
        assertEquals("https://pub-e0fbc7b94d124c9091aacdb9b29e3855.r2.dev/hsc/bangla/HSC_Physics_1st_Paper_NCTB_2026.pdf", hscUrl)
    }

    @Test
    fun testDistinctPdfUrlMappingForSahityaKanikaPhysicsSscAndPhysicsHsc() {
        val sahityaKanika = BookCatalog.getBookById("c8_bangla")
        val physicsSsc = BookCatalog.getBookById("ssc_physics")
        val physicsHsc1 = BookCatalog.getBookById("hsc_physics_1")

        assertNotNull("Sahitya Kanika Class 8 must exist", sahityaKanika)
        assertNotNull("Physics SSC must exist", physicsSsc)
        assertNotNull("Physics HSC 1st Paper must exist", physicsHsc1)

        sahityaKanika!!
        physicsSsc!!
        physicsHsc1!!

        // Assert distinct URLs
        assertNotEquals(sahityaKanika.pdfUrl, physicsSsc.pdfUrl)
        assertNotEquals(sahityaKanika.pdfUrl, physicsHsc1.pdfUrl)
        assertNotEquals(physicsSsc.pdfUrl, physicsHsc1.pdfUrl)

        // Assert no shared demo/tracemonkey links
        assertFalse("Must not contain tracemonkey", sahityaKanika.pdfUrl.contains("tracemonkey"))
        assertFalse("Must not contain tracemonkey", physicsSsc.pdfUrl.contains("tracemonkey"))
        assertFalse("Must not contain tracemonkey", physicsHsc1.pdfUrl.contains("tracemonkey"))

        // Assert each points to its own specific NCTB 2026 PDF
        assertTrue(sahityaKanika.pdfUrl.contains("Class8_Sahitya_Kanika_BV_NCTB_2026.pdf"))
        assertTrue(physicsSsc.pdfUrl.contains("SSC_Physics_BV_NCTB_2026.pdf"))
        assertTrue(physicsHsc1.pdfUrl.contains("HSC_Physics_1st_Paper_BV_NCTB_2026.pdf"))
    }

    @Test
    fun testAllBooksHaveUniquePdfUrlsAndNoSharedDemoFallback() {
        val allBooks = BookCatalog.getAllBooks()
        val allUrls = allBooks.map { it.pdfUrl }

        // Assert all URLs are unique
        assertEquals("Every book must have a unique PDF URL", allUrls.size, allUrls.toSet().size)

        for (book in allBooks) {
            assertFalse("Book ${book.id} must not reference tracemonkey demo PDF", book.pdfUrl.contains("tracemonkey"))
            assertTrue("Book ${book.id} URL must end with .pdf", book.pdfUrl.endsWith(".pdf"))
            assertTrue("Book ${book.id} URL must contain 2026", book.pdfUrl.contains("2026"))
        }
    }

    @Test
    fun testPdfUnavailableHandlingForInvalidBookUrl() {
        kotlinx.coroutines.runBlocking {
            val tempDir = java.nio.file.Files.createTempDirectory("reve_test_unavailable").toFile()
            val mockContext = object : android.content.ContextWrapper(null) {
                override fun getFilesDir(): java.io.File = tempDir
            }

            val invalidBook = com.example.model.Book(
                id = "invalid_book_test",
                className = "Class 10 (SSC)",
                curriculumId = "nctb_bn",
                groupId = "science",
                subjectId = "physics",
                paperId = "none",
                title = "Unknown Subject",
                titleBn = "অজানা বিষয়",
                pdfUrl = "",
                edition = "2026 Edition",
                pages = 100,
                fileSizeBytes = 1000L
            )

            try {
                com.example.data.BookStorageManager.ensureBookAvailable(mockContext, invalidBook)
                fail("ensureBookAvailable should throw for invalid / missing PDF URL")
            } catch (e: Exception) {
                assertTrue("Error message must indicate PDF unavailable: ${e.message}", e.message?.contains("PDF unavailable") == true)
            }

            tempDir.deleteRecursively()
        }
    }

    @Test
    fun testNoHardcodedYearMismatchAcrossAllBooks() {
        val allBooks = BookCatalog.getAllBooks()
        assertTrue(allBooks.isNotEmpty())

        for (book in allBooks) {
            assertNotEquals(
                "Book ${book.id} (${book.title}) must not have hardcoded 2024-2025 mismatch",
                "2024-2025",
                book.edition
            )
            assertNotEquals(
                "Book ${book.id} (${book.title}) must not have hardcoded 2024 Edition",
                "2024 Edition",
                book.edition
            )
            assertEquals(
                "Book ${book.id} must be 2026 Edition",
                "2026 Edition",
                book.edition
            )
            assertTrue(
                "PDF URL for ${book.id} must target 2026",
                book.pdfUrl.contains("2026")
            )
        }
    }

    @Test
    fun testBookIdAndEditionCacheKeyConsistency() {
        val charupath = BookCatalog.getBookById("c6_charupath_bv")!!
        val cacheKey = com.example.data.BookStorageManager.getCacheKey(charupath)
        assertEquals("c6_charupath_bv_2026_edition", cacheKey)

        val physics = BookCatalog.getBookById("ssc_physics")!!
        val physicsKey = com.example.data.BookStorageManager.getCacheKey(physics)
        assertEquals("ssc_physics_bv_2026_edition", physicsKey)

        val context = charupath.toTopicAiContext()
        val chatMessage = com.example.data.BookAiTeacherEngine.generateBookAnswer(
            query = "Explain poem",
            academicContext = context,
            activeBook = charupath
        )
        assertNotNull(chatMessage.sourceBookContext)
        assertEquals(charupath.id, chatMessage.sourceBookContext?.bookId)
        assertEquals(charupath.edition, chatMessage.sourceBookContext?.edition)
    }

    @Test
    fun testCanonicalUrlConstruction_Class8_SSC_HSC() {
        val c8MathBv = BookCatalog.getBookById("c8_math_bv")!!
        val c8MathEv = BookCatalog.getBookById("c8_math_ev")!!
        val sscPhysicsBv = BookCatalog.getBookById("ssc_physics_bv")!!
        val sscPhysicsEv = BookCatalog.getBookById("ssc_physics_ev")!!
        val hscPhysics1Bv = BookCatalog.getBookById("hsc_physics_1_bv")!!
        val hscPhysics1Ev = BookCatalog.getBookById("hsc_physics_1_ev")!!

        assertEquals("class8/bangla/Class8_Math_BV_NCTB_2026.pdf.pdf", c8MathBv.r2Key)
        assertEquals("class8/english/Class8_Math_EV_NCTB_2026.pdf.pdf", c8MathEv.r2Key)
        assertEquals("ssc/bangla/SSC_Physics_BV_NCTB_2026.pdf.pdf", sscPhysicsBv.r2Key)
        assertEquals("ssc/english/SSC_Physics_EV_NCTB_2026.pdf.pdf", sscPhysicsEv.r2Key)
        assertEquals("hsc/bangla/HSC_Physics_1st_Paper_BV_NCTB_2026.pdf.pdf", hscPhysics1Bv.r2Key)
        assertEquals("hsc/english/HSC_Physics_1st_Paper_EV_NCTB_2026.pdf.pdf", hscPhysics1Ev.r2Key)

        val c8Url = com.example.data.R2StorageConfig.getCanonicalUrl(c8MathBv)
        val sscUrl = com.example.data.R2StorageConfig.getCanonicalUrl(sscPhysicsBv)
        val hscUrl = com.example.data.R2StorageConfig.getCanonicalUrl(hscPhysics1Bv)

        assertTrue(c8Url.startsWith("https://pub-e0fbc7b94d124c9091aacdb9b29e3855.r2.dev/class8/bangla/"))
        assertTrue(sscUrl.startsWith("https://pub-e0fbc7b94d124c9091aacdb9b29e3855.r2.dev/ssc/bangla/"))
        assertTrue(hscUrl.startsWith("https://pub-e0fbc7b94d124c9091aacdb9b29e3855.r2.dev/hsc/bangla/"))
    }
}
