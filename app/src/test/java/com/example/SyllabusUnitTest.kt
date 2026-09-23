package com.example

import com.example.data.SyllabusRepository
import com.example.model.AcademicGroup
import com.example.model.ChapterStatus
import com.example.model.SubjectPaper
import com.example.ui.viewmodel.SyllabusViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SyllabusUnitTest {

    @Before
    fun setup() {
        SyllabusRepository.resetState()
        SyllabusRepository.updateOnboardingConfiguration(
            language = "বাংলা",
            educationLevel = "Class 10 (SSC)",
            curriculum = "NCTB Bangla Version",
            selectedSubjects = listOf("Mathematics", "Physics", "Chemistry", "English"),
            dailyGoalMinutes = 30
        )
    }

    @Test
    fun testSyllabusInitialization() {
        val syllabus = SyllabusRepository.getSyllabus()
        assertNotNull(syllabus)
        assertEquals("Class 10 (SSC)", syllabus.educationLevel)
        assertEquals("NCTB Bangla Version", syllabus.curriculum)
        assertTrue(syllabus.subjects.isNotEmpty())
    }

    @Test
    fun testTopicCompletionUpdatesOverallProgress() {
        val initialSyllabus = SyllabusRepository.getSyllabus()
        val initialProgress = initialSyllabus.overallProgressPercent

        val subject = initialSyllabus.subjects.first()
        val chapter = subject.chapters.first()
        val topic = chapter.topics.first()

        SyllabusRepository.markTopicCompleted(subject.id, chapter.id, topic.id)

        val updatedSyllabus = SyllabusRepository.getSyllabus()
        val updatedSubject = updatedSyllabus.subjects.find { it.id == subject.id }!!
        val updatedChapter = updatedSubject.chapters.find { it.id == chapter.id }!!
        val updatedTopic = updatedChapter.topics.find { it.id == topic.id }!!

        assertTrue(updatedTopic.isCompleted)
        assertEquals(100, updatedTopic.progressPercent)
        assertTrue(updatedChapter.progressPercent >= 0)
    }

    @Test
    fun testOnboardingConfigurationUpdatesSyllabus() {
        SyllabusRepository.updateOnboardingConfiguration(
            language = "English",
            educationLevel = "Class 10 (SSC)",
            curriculum = "NCTB English Version",
            group = AcademicGroup.SCIENCE,
            selectedSubjects = listOf("Physics", "Chemistry"),
            dailyGoalMinutes = 45
        )

        val syllabus = SyllabusRepository.getSyllabus()
        assertEquals("Class 10 (SSC)", syllabus.educationLevel)
        assertEquals("NCTB English Version", syllabus.curriculum)
        assertEquals("English", syllabus.language)
        assertEquals(2, syllabus.subjects.size)
        assertTrue(syllabus.subjects.any { it.name == "Physics" })
        assertTrue(syllabus.subjects.any { it.name == "Chemistry" })
    }

    @Test
    fun testClassIsolation_Class8MathDoesNotReturnClass10Chapters() {
        SyllabusRepository.updateOnboardingConfiguration(
            language = "English",
            educationLevel = "Class 8",
            curriculum = "NCTB English Version",
            group = AcademicGroup.GENERAL_JUNIOR,
            selectedSubjects = listOf("math"),
            dailyGoalMinutes = 30
        )

        val syllabus = SyllabusRepository.getSyllabus()
        val mathSubject = syllabus.subjects.first { it.id == "math" }
        val chapterTitles = mathSubject.chapters.map { it.title }

        // Must contain junior math
        assertTrue(chapterTitles.any { it.contains("Numbers", ignoreCase = true) || it.contains("Fractions", ignoreCase = true) })
        // Must NOT contain secondary (Class 10) topics like Quadratic Equations
        assertFalse(chapterTitles.any { it.contains("Quadratic", ignoreCase = true) })
    }

    @Test
    fun testSubjectIsolation_Class10PhysicsDoesNotReturnChemistryChapters() {
        SyllabusRepository.updateOnboardingConfiguration(
            language = "English",
            educationLevel = "Class 10 (SSC)",
            curriculum = "NCTB English Version",
            group = AcademicGroup.SCIENCE,
            selectedSubjects = listOf("physics"),
            dailyGoalMinutes = 30
        )

        val syllabus = SyllabusRepository.getSyllabus()
        val physicsSubject = syllabus.subjects.first { it.id == "physics" }
        val chapterTitles = physicsSubject.chapters.map { it.title }

        // Must contain Physics chapters
        assertTrue(chapterTitles.any { it.contains("Motion", ignoreCase = true) || it.contains("Force", ignoreCase = true) })
        // Must NOT contain Chemistry chapters
        assertFalse(chapterTitles.any { it.contains("Chemical Bonds", ignoreCase = true) || it.contains("Structure of Matter", ignoreCase = true) })
    }

    @Test
    fun testGroupIsolation_Class10BusinessAccountingDoesNotReturnPhysicsChapters() {
        SyllabusRepository.updateOnboardingConfiguration(
            language = "English",
            educationLevel = "Class 10 (SSC)",
            curriculum = "NCTB English Version",
            group = AcademicGroup.BUSINESS_STUDIES,
            selectedSubjects = listOf("accounting"),
            dailyGoalMinutes = 30
        )

        val syllabus = SyllabusRepository.getSyllabus()
        val accSubject = syllabus.subjects.first { it.id == "accounting" }
        val chapterTitles = accSubject.chapters.map { it.title }

        // Must contain Accounting chapters
        assertTrue(chapterTitles.any { it.contains("Double Entry", ignoreCase = true) || it.contains("Financial Statements", ignoreCase = true) })
        // Must NOT contain Physics chapters
        assertFalse(chapterTitles.any { it.contains("Motion", ignoreCase = true) || it.contains("Energy", ignoreCase = true) })
    }

    @Test
    fun testPaperIsolation_Class11PhysicsPaper1DoesNotReturnPaper2Chapters() {
        SyllabusRepository.updateOnboardingConfiguration(
            language = "English",
            educationLevel = "Class 11 (HSC)",
            curriculum = "NCTB English Version",
            group = AcademicGroup.SCIENCE,
            selectedSubjects = listOf("physics_1"),
            dailyGoalMinutes = 30
        )

        val syllabus = SyllabusRepository.getSyllabus()
        val p1Subject = syllabus.subjects.first { it.id == "physics_1" }
        val chapterTitles = p1Subject.chapters.map { it.title }

        // Must contain Paper 1 chapters (Vectors, Mechanics)
        assertTrue(chapterTitles.any { it.contains("Vectors", ignoreCase = true) })
        // Must NOT contain Paper 2 chapters (Thermodynamics, Current Electricity)
        assertFalse(chapterTitles.any { it.contains("Thermodynamics", ignoreCase = true) })
    }

    @Test
    fun testTopicAiContextCorrectness() {
        SyllabusRepository.updateOnboardingConfiguration(
            language = "English",
            educationLevel = "Class 10 (SSC)",
            curriculum = "NCTB English Version",
            group = AcademicGroup.SCIENCE,
            selectedSubjects = listOf("physics"),
            dailyGoalMinutes = 30
        )

        val syllabus = SyllabusRepository.getSyllabus()
        val subject = syllabus.subjects.first { it.id == "physics" }
        val chapter = subject.chapters.first()
        val topic = chapter.topics.first()

        val aiContext = SyllabusRepository.getTopicAiContext(subject.id, chapter.id, topic.id)
        assertEquals("Class 10 (SSC)", aiContext.educationLevel)
        assertEquals("physics", aiContext.subjectId)
        assertEquals(chapter.id, aiContext.chapterId)
        assertEquals(topic.id, aiContext.topicId)
        assertNotNull(aiContext.topicTitle)
        assertTrue(aiContext.topicTitle.isNotEmpty())
    }

    @Test
    fun testBookmarkAndMistakeNotebook() {
        val syllabus = SyllabusRepository.getSyllabus()
        val subject = syllabus.subjects.first()
        val chapter = subject.chapters.first()
        val topic = chapter.topics.first()
        val question = topic.practiceQuestions.first()

        SyllabusRepository.toggleBookmark(topic.id, question.id)
        SyllabusRepository.addQuestionToMistakes(topic.id, question.id)

        val updatedSyllabus = SyllabusRepository.getSyllabus()
        val updatedQuestion = updatedSyllabus.subjects.first().chapters.first().topics.first().practiceQuestions.first()
        assertTrue(updatedQuestion.isBookmarked)
        assertTrue(updatedQuestion.isAddedToMistakes)
    }

    @Test
    fun testCaseA_Class8_NctbBanglaVersion_GeneralJunior() {
        val availableGroups = com.example.data.AcademicCatalog.getAvailableGroups("Class 8", "NCTB (Bangla Version)")
        assertEquals(listOf(AcademicGroup.GENERAL_JUNIOR), availableGroups)

        val subjects = com.example.data.AcademicCatalog.getSubjects(
            educationLevel = "Class 8",
            curriculum = "NCTB (Bangla Version)",
            group = AcademicGroup.GENERAL_JUNIOR
        )
        val ids = subjects.map { it.id }.toSet()

        // Must contain junior subjects
        assertTrue(ids.contains("bangla"))
        assertTrue(ids.contains("english_for_today"))
        assertTrue(ids.contains("english_grammar"))
        assertTrue(ids.contains("math"))
        assertTrue(ids.contains("science"))
        assertTrue(ids.contains("bgs"))
        assertTrue(ids.contains("ict"))
        assertTrue(ids.contains("religion_islam"))
        assertTrue(ids.contains("religion_hindu"))
        assertTrue(ids.contains("religion_buddhist"))
        assertTrue(ids.contains("religion_christian"))

        // Must NOT contain senior group subjects
        assertFalse(ids.contains("physics"))
        assertFalse(ids.contains("chemistry"))
        assertFalse(ids.contains("accounting"))
        assertFalse(ids.contains("economics"))
    }

    @Test
    fun testCaseB_Class10_Science() {
        val subjects = com.example.data.AcademicCatalog.getSubjects(
            educationLevel = "Class 10 (SSC)",
            curriculum = "NCTB (Bangla Version)",
            group = AcademicGroup.SCIENCE
        )
        val ids = subjects.map { it.id }.toSet()

        // Common subjects
        assertTrue(ids.contains("bangla"))
        assertTrue(ids.contains("english"))
        assertTrue(ids.contains("math"))
        assertTrue(ids.contains("ict"))
        assertTrue(ids.contains("bgs"))

        // Science subjects
        assertTrue(ids.contains("physics"))
        assertTrue(ids.contains("chemistry"))
        assertTrue(ids.contains("biology"))
        assertTrue(ids.contains("higher_math"))

        // Must NOT contain other groups
        assertFalse(ids.contains("accounting"))
        assertFalse(ids.contains("business_entrepreneurship"))
        assertFalse(ids.contains("history"))
    }

    @Test
    fun testCaseC_Class10_BusinessStudies() {
        val subjects = com.example.data.AcademicCatalog.getSubjects(
            educationLevel = "Class 10 (SSC)",
            curriculum = "NCTB (Bangla Version)",
            group = AcademicGroup.BUSINESS_STUDIES
        )
        val ids = subjects.map { it.id }.toSet()

        // Common subjects
        assertTrue(ids.contains("bangla"))
        assertTrue(ids.contains("english"))
        assertTrue(ids.contains("math"))

        // Business Studies subjects
        assertTrue(ids.contains("accounting"))
        assertTrue(ids.contains("business_entrepreneurship"))
        assertTrue(ids.contains("finance_banking"))

        // Must NOT contain science subjects
        assertFalse(ids.contains("physics"))
        assertFalse(ids.contains("chemistry"))
        assertFalse(ids.contains("biology"))
    }

    @Test
    fun testCaseD_Class10_Humanities() {
        val subjects = com.example.data.AcademicCatalog.getSubjects(
            educationLevel = "Class 10 (SSC)",
            curriculum = "NCTB (Bangla Version)",
            group = AcademicGroup.HUMANITIES
        )
        val ids = subjects.map { it.id }.toSet()

        // Common subjects
        assertTrue(ids.contains("bangla"))
        assertTrue(ids.contains("english"))
        assertTrue(ids.contains("math"))

        // Humanities subjects
        assertTrue(ids.contains("history"))
        assertTrue(ids.contains("geography"))
        assertTrue(ids.contains("economics"))
        assertTrue(ids.contains("civics"))

        // Must NOT contain science or business subjects
        assertFalse(ids.contains("physics"))
        assertFalse(ids.contains("chemistry"))
        assertFalse(ids.contains("accounting"))
    }

    @Test
    fun testCaseE_Class12_Science_PaperIsolation() {
        val subjects = com.example.data.AcademicCatalog.getSubjects(
            educationLevel = "Class 12 (HSC)",
            curriculum = "NCTB (Bangla Version)",
            group = AcademicGroup.SCIENCE
        )
        val ids = subjects.map { it.id }.toSet()

        assertTrue(ids.contains("physics_1"))
        assertTrue(ids.contains("physics_2"))
        assertTrue(ids.contains("chemistry_1"))
        assertTrue(ids.contains("chemistry_2"))
        assertTrue(ids.contains("higher_math_1"))
        assertTrue(ids.contains("higher_math_2"))
        assertTrue(ids.contains("biology_1"))
        assertTrue(ids.contains("biology_2"))

        // Verify Paper 1 vs Paper 2 Chapter Resolution
        val physics1Chapters = com.example.data.AcademicContentCatalog.getChapters(
            educationLevel = "Class 12 (HSC)",
            curriculum = "NCTB (Bangla Version)",
            group = AcademicGroup.SCIENCE,
            subjectId = "physics_1",
            paper = SubjectPaper.PAPER_1
        )
        val physics2Chapters = com.example.data.AcademicContentCatalog.getChapters(
            educationLevel = "Class 12 (HSC)",
            curriculum = "NCTB (Bangla Version)",
            group = AcademicGroup.SCIENCE,
            subjectId = "physics_2",
            paper = SubjectPaper.PAPER_2
        )

        val p1Titles = physics1Chapters.map { it.title }.toSet()
        val p2Titles = physics2Chapters.map { it.title }.toSet()

        assertTrue(p1Titles.contains("Vectors") || p1Titles.contains("Newtonian Mechanics"))
        assertTrue(p2Titles.contains("Thermodynamics") || p2Titles.contains("Current Electricity"))

        // Paper 1 chapters must not exist in Paper 2 and vice versa
        assertFalse(p1Titles.contains("Thermodynamics"))
        assertFalse(p2Titles.contains("Vectors"))
    }

    @Test
    fun testCaseF_Class12_BusinessStudies_PaperIsolation() {
        val subjects = com.example.data.AcademicCatalog.getSubjects(
            educationLevel = "Class 12 (HSC)",
            curriculum = "NCTB (Bangla Version)",
            group = AcademicGroup.BUSINESS_STUDIES
        )
        val ids = subjects.map { it.id }.toSet()

        assertTrue(ids.contains("accounting_1"))
        assertTrue(ids.contains("accounting_2"))
        assertTrue(ids.contains("bom_1"))
        assertTrue(ids.contains("bom_2"))
        assertTrue(ids.contains("fbi_1"))
        assertTrue(ids.contains("fbi_2"))

        val acc1Chapters = com.example.data.AcademicContentCatalog.getChapters(
            educationLevel = "Class 12 (HSC)",
            curriculum = "NCTB (Bangla Version)",
            group = AcademicGroup.BUSINESS_STUDIES,
            subjectId = "accounting_1",
            paper = SubjectPaper.PAPER_1
        )
        val acc2Chapters = com.example.data.AcademicContentCatalog.getChapters(
            educationLevel = "Class 12 (HSC)",
            curriculum = "NCTB (Bangla Version)",
            group = AcademicGroup.BUSINESS_STUDIES,
            subjectId = "accounting_2",
            paper = SubjectPaper.PAPER_2
        )

        val acc1Titles = acc1Chapters.map { it.title }.toSet()
        val acc2Titles = acc2Chapters.map { it.title }.toSet()

        assertTrue(acc1Titles.any { it.contains("Accounts", ignoreCase = true) || it.contains("Worksheet", ignoreCase = true) || it.contains("Accounting", ignoreCase = true) })
        assertTrue(acc2Titles.any { it.contains("Partnership", ignoreCase = true) || it.contains("Cost", ignoreCase = true) || it.contains("Company", ignoreCase = true) })
    }

    @Test
    fun testCaseG_Class12_Humanities_PaperIsolation() {
        val subjects = com.example.data.AcademicCatalog.getSubjects(
            educationLevel = "Class 12 (HSC)",
            curriculum = "NCTB (Bangla Version)",
            group = AcademicGroup.HUMANITIES
        )
        val ids = subjects.map { it.id }.toSet()

        assertTrue(ids.contains("economics_1"))
        assertTrue(ids.contains("economics_2"))
        assertTrue(ids.contains("history_1"))
        assertTrue(ids.contains("history_2"))
        assertTrue(ids.contains("civics_1"))
        assertTrue(ids.contains("civics_2"))
        assertTrue(ids.contains("logic"))

        val econ1Chapters = com.example.data.AcademicContentCatalog.getChapters(
            educationLevel = "Class 12 (HSC)",
            curriculum = "NCTB (Bangla Version)",
            group = AcademicGroup.HUMANITIES,
            subjectId = "economics_1",
            paper = SubjectPaper.PAPER_1
        )
        val econ2Chapters = com.example.data.AcademicContentCatalog.getChapters(
            educationLevel = "Class 12 (HSC)",
            curriculum = "NCTB (Bangla Version)",
            group = AcademicGroup.HUMANITIES,
            subjectId = "economics_2",
            paper = SubjectPaper.PAPER_2
        )

        val econ1Titles = econ1Chapters.map { it.title }.toSet()
        val econ2Titles = econ2Chapters.map { it.title }.toSet()

        assertTrue(econ1Titles.any { it.contains("Micro", ignoreCase = true) || it.contains("Demand", ignoreCase = true) || it.contains("Consumer", ignoreCase = true) })
        assertTrue(econ2Titles.any { it.contains("Macro", ignoreCase = true) || it.contains("Bangladesh", ignoreCase = true) || it.contains("Development", ignoreCase = true) })
    }

    @Test
    fun testSelectionStateTransition_ScienceToBusiness_PrunesInvalidAndPreservesValid() {
        // Given Science selections: Math, Physics, Chemistry
        val scienceSubjects = listOf("math", "physics", "chemistry")

        // When switching to Business Studies:
        val businessAvailable = com.example.data.AcademicCatalog.getSubjects(
            "Class 10 (SSC)", "NCTB (Bangla Version)", AcademicGroup.BUSINESS_STUDIES
        )
        val validForBusiness = com.example.data.AcademicCatalog.filterValidSelectedSubjects(scienceSubjects, businessAvailable)

        // Math remains selected because it is common
        assertTrue(validForBusiness.contains("math"))
        // Physics and Chemistry are pruned
        assertFalse(validForBusiness.contains("physics"))
        assertFalse(validForBusiness.contains("chemistry"))

        // When switching back to Science with the current state (only math remaining):
        val scienceAvailable = com.example.data.AcademicCatalog.getSubjects(
            "Class 10 (SSC)", "NCTB (Bangla Version)", AcademicGroup.SCIENCE
        )
        val validBackToScience = com.example.data.AcademicCatalog.filterValidSelectedSubjects(validForBusiness, scienceAvailable)
        assertEquals(listOf("math"), validBackToScience)
    }

    @Test
    fun testSelectionStateTransition_BusinessToScience() {
        // Given Business selections: Math, Accounting, Finance
        val businessSubjects = listOf("math", "accounting", "finance_banking")

        val scienceAvailable = com.example.data.AcademicCatalog.getSubjects(
            "Class 10 (SSC)", "NCTB (Bangla Version)", AcademicGroup.SCIENCE
        )
        val validForScience = com.example.data.AcademicCatalog.filterValidSelectedSubjects(businessSubjects, scienceAvailable)

        assertTrue(validForScience.contains("math"))
        assertFalse(validForScience.contains("accounting"))
        assertFalse(validForScience.contains("finance_banking"))
    }

    @Test
    fun testCrossContextIsolation_Class8vsClass10() {
        val class8Chapters = com.example.data.AcademicContentCatalog.getChapters(
            educationLevel = "Class 8",
            curriculum = "NCTB (Bangla Version)",
            group = AcademicGroup.GENERAL_JUNIOR,
            subjectId = "math",
            paper = SubjectPaper.NONE
        )
        val class10Chapters = com.example.data.AcademicContentCatalog.getChapters(
            educationLevel = "Class 10 (SSC)",
            curriculum = "NCTB (Bangla Version)",
            group = AcademicGroup.SCIENCE,
            subjectId = "math",
            paper = SubjectPaper.NONE
        )

        val c8Titles = class8Chapters.map { it.title }.toSet()
        val c10Titles = class10Chapters.map { it.title }.toSet()

        assertTrue(c8Titles.any { it.contains("Numbers", ignoreCase = true) || it.contains("Fractions", ignoreCase = true) })
        assertTrue(c10Titles.any { it.contains("Quadratic", ignoreCase = true) || it.contains("Coordinate", ignoreCase = true) })

        // No cross leakage
        assertFalse(c8Titles.contains("Quadratic Equations (দ্বিঘাত সমীকরণ)"))
        assertFalse(c10Titles.contains("Numbers, Factors & Fractions"))
    }

    @Test
    fun testQuizQuestionIsolation() {
        // Class 8 Quiz cannot return Physics or Chemistry questions
        val class8Questions = com.example.data.CurriculumQuizRepository.resolveQuizQuestions(
            selectedClass = "Class 8",
            selectedCurriculum = "NCTB (Bangla Version)",
            targetSubjectId = "science",
            selectedGroup = AcademicGroup.GENERAL_JUNIOR
        )
        assertTrue(class8Questions.isNotEmpty())
        class8Questions.forEach { q ->
            assertFalse(q.className.contains("Class 10"))
            assertFalse(q.className.contains("Class 12"))
        }

        // Science group cannot return business questions
        val scienceQuestions = com.example.data.CurriculumQuizRepository.resolveQuizQuestions(
            selectedClass = "Class 10 (SSC)",
            selectedCurriculum = "NCTB (Bangla Version)",
            targetSubjectId = "physics",
            selectedGroup = AcademicGroup.SCIENCE
        )
        assertTrue(scienceQuestions.isNotEmpty())
        scienceQuestions.forEach { q ->
            assertEquals("physics", q.subjectId.lowercase())
        }
    }
}
