package com.example.data

import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface ISyllabusRepository {
    val syllabusFlow: StateFlow<PersonalizedSyllabus>
    fun getSyllabus(): PersonalizedSyllabus
    fun updateTopicProgress(subjectId: String, chapterId: String, topicId: String, percent: Int)
    fun markTopicCompleted(subjectId: String, chapterId: String, topicId: String)
    fun recordQuizResult(subjectId: String, chapterId: String, scorePercent: Int, wrongQuestions: List<QuizQuestion>)
    fun toggleBookmark(topicId: String, questionId: String)
    fun addQuestionToMistakes(topicId: String, questionId: String)
    fun updateOnboardingConfiguration(
        language: String,
        educationLevel: String,
        curriculum: String,
        selectedSubjects: List<String>,
        dailyGoalMinutes: Int,
        group: AcademicGroup = AcademicGroup.SCIENCE
    )
}

object SyllabusRepository : ISyllabusRepository {

    private var currentLanguage = "বাংলা"
    private var currentEducationLevel = "Class 10 (SSC)"
    private var currentCurriculum = "NCTB (Bangla Version)"
    private var currentGroup: AcademicGroup = AcademicGroup.SCIENCE
    private var currentDailyGoalMinutes = 30
    private var userSelectedSubjectNames = listOf("Mathematics", "Physics", "Chemistry", "English")

    // Dynamic progress stores (topicId -> %, chapterId -> quizScore)
    private val topicProgressMap = mutableMapOf<String, Int>(
        "m_quad_1" to 100,
        "m_quad_2" to 72,
        "p_motion_1" to 100,
        "p_motion_2" to 60,
        "c_matter_1" to 100,
        "e_verbs_1" to 85
    )

    private val completedTopicIds = mutableSetOf<String>("m_quad_1", "p_motion_1", "c_matter_1")
    private val bookmarkedQuestionIds = mutableSetOf<String>("pq_m_1")
    private val mistakeQuestionIds = mutableSetOf<String>("pq_p_1")

    private val _syllabusFlow = MutableStateFlow(buildSyllabus())
    override val syllabusFlow: StateFlow<PersonalizedSyllabus> = _syllabusFlow.asStateFlow()

    override fun getSyllabus(): PersonalizedSyllabus = _syllabusFlow.value

    fun resetState() {
        completedTopicIds.clear()
        topicProgressMap.clear()
        bookmarkedQuestionIds.clear()
        mistakeQuestionIds.clear()
        currentEducationLevel = "Class 10 (SSC)"
        currentCurriculum = "NCTB (Bangla Version)"
        currentGroup = AcademicGroup.SCIENCE
        userSelectedSubjectNames = listOf("Mathematics", "Physics", "Chemistry", "English")
        _syllabusFlow.value = buildSyllabus()
    }

    override fun updateOnboardingConfiguration(
        language: String,
        educationLevel: String,
        curriculum: String,
        selectedSubjects: List<String>,
        dailyGoalMinutes: Int,
        group: AcademicGroup
    ) {
        currentLanguage = language
        currentEducationLevel = educationLevel
        currentCurriculum = curriculum
        currentGroup = if (AcademicCatalog.isJuniorClass(educationLevel)) {
            AcademicGroup.GENERAL_JUNIOR
        } else {
            group
        }
        val available = AcademicCatalog.getSubjects(educationLevel, curriculum, currentGroup)
        val validSubjects = AcademicCatalog.filterValidSelectedSubjects(selectedSubjects, available)
        userSelectedSubjectNames = if (validSubjects.isNotEmpty()) {
            validSubjects
        } else {
            AcademicCatalog.getDefaultCoreSubjectIds(educationLevel, curriculum, currentGroup)
        }
        currentDailyGoalMinutes = dailyGoalMinutes
        _syllabusFlow.value = buildSyllabus()
    }

    fun getActiveAcademicContext(
        subjectId: String? = null,
        chapterId: String? = null,
        topicId: String? = null
    ): TopicAiContext {
        val s = _syllabusFlow.value
        val subject = if (!subjectId.isNullOrEmpty()) {
            s.subjects.find { it.id.equals(subjectId, ignoreCase = true) } ?: s.subjects.firstOrNull()
        } else {
            s.subjects.find { it.id.equals(s.continueSubjectId, ignoreCase = true) } ?: s.subjects.firstOrNull()
        }
        val chapter = if (!chapterId.isNullOrEmpty() && subject != null) {
            subject.chapters.find { it.id == chapterId } ?: subject.chapters.firstOrNull()
        } else {
            subject?.chapters?.firstOrNull()
        }
        val topic = if (!topicId.isNullOrEmpty() && chapter != null) {
            chapter.topics.find { it.id == topicId } ?: chapter.topics.firstOrNull()
        } else {
            chapter?.topics?.firstOrNull()
        }

        return TopicAiContext(
            educationLevel = s.educationLevel,
            curriculum = s.curriculum,
            subjectId = subject?.id ?: "math",
            subjectName = subject?.name ?: "Mathematics",
            chapterId = chapter?.id ?: "",
            chapterTitle = chapter?.title ?: "",
            topicId = topic?.id ?: "",
            topicTitle = topic?.title ?: (chapter?.title ?: (subject?.name ?: "Academic Topic")),
            topicTitleBn = topic?.titleBn ?: (chapter?.titleBn ?: (subject?.nameBn ?: "পাঠ")),
            language = s.language,
            groupId = subject?.groupId?.ifEmpty { s.groupId } ?: s.groupId,
            group = subject?.group ?: s.group,
            paperId = subject?.paperId ?: SubjectPaper.NONE.id,
            paper = subject?.paper ?: SubjectPaper.NONE,
            curriculumId = subject?.curriculumId?.ifEmpty { s.curriculumId } ?: s.curriculumId
        )
    }

    fun getTopicAiContext(
        subjectId: String? = null,
        chapterId: String? = null,
        topicId: String? = null
    ): TopicAiContext = getActiveAcademicContext(subjectId, chapterId, topicId)

    override fun updateTopicProgress(subjectId: String, chapterId: String, topicId: String, percent: Int) {
        topicProgressMap[topicId] = percent.coerceIn(0, 100)
        if (percent >= 100) {
            completedTopicIds.add(topicId)
        }
        _syllabusFlow.value = buildSyllabus()
    }

    override fun markTopicCompleted(subjectId: String, chapterId: String, topicId: String) {
        topicProgressMap[topicId] = 100
        completedTopicIds.add(topicId)
        _syllabusFlow.value = buildSyllabus()
    }

    override fun recordQuizResult(
        subjectId: String,
        chapterId: String,
        scorePercent: Int,
        wrongQuestions: List<QuizQuestion>
    ) {
        // Update all topics in this chapter if quiz passed
        val currentSyllabus = _syllabusFlow.value
        val subject = currentSyllabus.subjects.find { it.id == subjectId }
        val chapter = subject?.chapters?.find { it.id == chapterId }
        chapter?.topics?.forEach { topic ->
            val current = topicProgressMap[topic.id] ?: 0
            val updated = maxOf(current, scorePercent)
            topicProgressMap[topic.id] = updated
            if (updated >= 80) completedTopicIds.add(topic.id)
        }
        _syllabusFlow.value = buildSyllabus()
    }

    override fun toggleBookmark(topicId: String, questionId: String) {
        if (bookmarkedQuestionIds.contains(questionId)) {
            bookmarkedQuestionIds.remove(questionId)
        } else {
            bookmarkedQuestionIds.add(questionId)
        }
        _syllabusFlow.value = buildSyllabus()
    }

    override fun addQuestionToMistakes(topicId: String, questionId: String) {
        mistakeQuestionIds.add(questionId)
        _syllabusFlow.value = buildSyllabus()
    }

    private fun buildSyllabus(): PersonalizedSyllabus {
        val frameworkTag = if (currentCurriculum.contains("Cambridge", ignoreCase = true) ||
            currentCurriculum.contains("Edexcel", ignoreCase = true)
        ) {
            "Verified Cambridge / Edexcel Framework"
        } else {
            "Verified NCTB National Curriculum Framework"
        }

        val curriculumId = CurriculumQuizRepository.mapToCurriculumId(currentCurriculum)
        val catalogSubjects = AcademicCatalog.getSubjects(currentEducationLevel, currentCurriculum, currentGroup)
        val catalogIds = catalogSubjects.map { it.id.lowercase() }.toSet()

        val allSubjects = catalogSubjects.map { cat ->
            buildSubjectForCatalog(cat, frameworkTag, curriculumId)
        }

        // Filter subjects based on user selection in Onboarding, strictly constrained to active catalog
        val filteredSubjects = allSubjects.filter { subj ->
            subj.id.lowercase() in catalogIds &&
            userSelectedSubjectNames.any { selected ->
                val s = selected.lowercase().trim()
                s == subj.id.lowercase() ||
                selected.equals(subj.name, ignoreCase = true) ||
                selected.equals(subj.nameBn, ignoreCase = true) ||
                (s != "math" && (subj.name.contains(selected, ignoreCase = true) || selected.contains(subj.name, ignoreCase = true)))
            }
        }.ifEmpty {
            val coreIds = catalogSubjects.filter { it.isCore }.map { it.id.lowercase() }
            val coreSubjects = allSubjects.filter { it.id.lowercase() in coreIds }
            if (coreSubjects.isNotEmpty()) coreSubjects else allSubjects.take(4)
        }

        var totalChaptersAcross = 0
        var completedChaptersAcross = 0
        var totalProgressSum = 0

        filteredSubjects.forEach { subj ->
            totalChaptersAcross += subj.totalChapters
            completedChaptersAcross += subj.completedChapters
            totalProgressSum += subj.progressPercent
        }

        val overallProgress = if (filteredSubjects.isNotEmpty()) {
            totalProgressSum / filteredSubjects.size
        } else {
            0
        }

        val continueSubj = filteredSubjects.firstOrNull()
        val recommendedSubj = filteredSubjects.getOrNull(1) ?: continueSubj
        val continueCh = continueSubj?.chapters?.firstOrNull()
        val continueTopic = continueCh?.topics?.firstOrNull()

        return PersonalizedSyllabus(
            educationLevel = currentEducationLevel,
            curriculum = currentCurriculum,
            language = currentLanguage,
            overallProgressPercent = overallProgress,
            completedChapters = completedChaptersAcross,
            totalChapters = totalChaptersAcross,
            streakDays = MockEducationalRepository.defaultProfile.streakDays,
            subjects = filteredSubjects,
            continueSubjectId = continueSubj?.id ?: "math",
            continueSubjectName = continueSubj?.name ?: "Mathematics",
            continueChapterTitle = continueCh?.title ?: "Quadratic Equations (দ্বিঘাত সমীকরণ)",
            continueTopicTitle = continueTopic?.title ?: "Quadratic Formula & Discriminant Analysis",
            continueProgressPercent = continueTopic?.let { topicProgressMap[it.id] } ?: 72,
            recommendedSubjectName = recommendedSubj?.name ?: "Physics",
            recommendedTopicTitle = recommendedSubj?.chapters?.firstOrNull()?.topics?.firstOrNull()?.title ?: "Newton's Second Law & Momentum",
            recommendedReason = "Practice comprehensive problem solving and chapter revision for ${recommendedSubj?.name ?: "Physics"}.",
            groupId = currentGroup.id,
            group = currentGroup,
            curriculumId = curriculumId
        )
    }

    private fun buildSubjectForCatalog(
        cat: AcademicCatalogSubject,
        frameworkTag: String,
        curriculumId: String
    ): SyllabusSubject {
        val chapters = AcademicContentCatalog.getChapters(
            educationLevel = currentEducationLevel,
            curriculum = currentCurriculum,
            group = cat.group,
            subjectId = cat.id,
            paper = cat.paper,
            frameworkTag = frameworkTag,
            completedTopicIds = completedTopicIds,
            topicProgressMap = topicProgressMap,
            bookmarkedQuestionIds = bookmarkedQuestionIds,
            mistakeQuestionIds = mistakeQuestionIds
        ).ifEmpty {
            return buildGenericCatalogSubject(cat, frameworkTag, curriculumId)
        }

        val completedCount = chapters.count { it.status == ChapterStatus.COMPLETED }
        val avgProgress = if (chapters.isNotEmpty()) chapters.map { it.progressPercent }.average().toInt() else 0
        val currentCh = chapters.firstOrNull { it.status == ChapterStatus.IN_PROGRESS } ?: chapters.firstOrNull()

        return SyllabusSubject(
            id = cat.id,
            name = cat.nameEn,
            nameBn = cat.nameBn,
            icon = cat.icon,
            colorHex = cat.colorHex,
            progressPercent = avgProgress,
            completedChapters = completedCount,
            totalChapters = chapters.size,
            currentChapterTitle = currentCh?.let { "${it.title} (${it.titleBn})" } ?: "",
            weakTopics = currentCh?.topics?.filter { it.isWeak }?.map { it.title } ?: emptyList(),
            nextRecommendedLesson = currentCh?.topics?.firstOrNull { !it.isCompleted }?.title ?: "",
            chapters = chapters,
            groupId = cat.group.id,
            group = cat.group,
            paperId = cat.paper.id,
            paper = cat.paper,
            curriculumId = curriculumId
        )
    }

    private fun buildGenericCatalogSubject(
        cat: AcademicCatalogSubject,
        frameworkTag: String,
        curriculumId: String
    ): SyllabusSubject {
        val ch1Id = "ch_${cat.id}_1"
        val ch2Id = "ch_${cat.id}_2"
        val top1Id = "top_${cat.id}_1"
        val top2Id = "top_${cat.id}_2"

        val ch1Topics = listOf(
            SyllabusTopic(
                id = top1Id,
                chapterId = ch1Id,
                title = "Fundamentals of ${cat.nameEn}",
                titleBn = "${cat.nameBn}-এর মৌলিক ধারণা",
                conceptExplanation = "Core concepts, definitions, and foundational principles for ${cat.nameEn}.",
                conceptExplanationBn = "${cat.nameBn}-এর মূল নীতিমালা, সংজ্ঞা এবং পাঠ্যক্রমিক বিষয়বস্তু।",
                exampleProblem = "Explain key application of ${cat.nameEn}.",
                exampleSolution = "Analyze step-by-step applying standard curriculum principles.",
                isCompleted = completedTopicIds.contains(top1Id),
                progressPercent = topicProgressMap[top1Id] ?: 0,
                practiceQuestions = listOf(
                    PracticeQuestionItem(
                        id = "pq_${cat.id}_1",
                        topicId = top1Id,
                        questionText = "State the fundamental principle of ${cat.nameEn}.",
                        difficulty = QuestionDifficulty.EASY,
                        hint = "Review introductory chapter definitions.",
                        steps = listOf("Identify standard definition", "Apply to curriculum problem"),
                        correctAnswer = "Standard curriculum definition",
                        isBookmarked = bookmarkedQuestionIds.contains("pq_${cat.id}_1"),
                        isAddedToMistakes = mistakeQuestionIds.contains("pq_${cat.id}_1")
                    )
                ),
                quickQuizQuestions = listOf(
                    QuizQuestion(
                        id = "qq_${cat.id}_1",
                        subjectId = cat.id,
                        topic = cat.nameEn,
                        questionText = "Which principle is primary in ${cat.nameEn}?",
                        options = listOf("Standard Rule A", "Fundamental Law B", "General Concept C", "Applied Rule D"),
                        correctIndex = 1,
                        explanation = "Fundamental Law B represents the primary guideline.",
                        groupId = cat.group.id,
                        group = cat.group,
                        paperId = cat.paper.id,
                        paper = cat.paper,
                        curriculumId = curriculumId,
                        className = currentEducationLevel
                    )
                )
            )
        )

        val ch2Topics = listOf(
            SyllabusTopic(
                id = top2Id,
                chapterId = ch2Id,
                title = "Analytical Application in ${cat.nameEn}",
                titleBn = "${cat.nameBn}-এ প্রায়োগিক বিশ্লেষণ",
                conceptExplanation = "Detailed examination and analytical problem-solving in ${cat.nameEn}.",
                conceptExplanationBn = "${cat.nameBn}-এ বাস্তব সমস্যার সমাধান ও বিশদ মূল্যায়ন।",
                exampleProblem = "Solve application problem in ${cat.nameEn}.",
                exampleSolution = "Execute calculation or conceptual derivation.",
                isCompleted = completedTopicIds.contains(top2Id),
                progressPercent = topicProgressMap[top2Id] ?: 0
            )
        )

        val chapters = listOf(
            SyllabusChapter(
                id = ch1Id,
                subjectId = cat.id,
                chapterNumber = 1,
                title = "Foundations of ${cat.nameEn}",
                titleBn = "${cat.nameBn}-এর পরিচিতি ও ভিত্তি",
                progressPercent = calculateChapterProgress(ch1Topics),
                status = if (calculateChapterProgress(ch1Topics) == 100) ChapterStatus.COMPLETED else ChapterStatus.IN_PROGRESS,
                estimatedHours = "3.5 hrs",
                curriculumFrameworkTag = frameworkTag,
                learningObjectives = listOf("Understand core theories", "Solve basic problems"),
                topics = ch1Topics
            ),
            SyllabusChapter(
                id = ch2Id,
                subjectId = cat.id,
                chapterNumber = 2,
                title = "Advanced Principles & Practice",
                titleBn = "উন্নত প্রয়োগ ও অনুশীলন",
                progressPercent = calculateChapterProgress(ch2Topics),
                status = ChapterStatus.NOT_STARTED,
                estimatedHours = "4.0 hrs",
                curriculumFrameworkTag = frameworkTag,
                learningObjectives = listOf("Analyze exam problems", "Synthesize findings"),
                topics = ch2Topics
            )
        )

        val completedCount = chapters.count { it.status == ChapterStatus.COMPLETED }
        val avgProgress = chapters.sumOf { it.progressPercent } / chapters.size

        return SyllabusSubject(
            id = cat.id,
            name = cat.nameEn,
            nameBn = cat.nameBn,
            icon = cat.icon,
            colorHex = cat.colorHex,
            progressPercent = avgProgress,
            completedChapters = completedCount,
            totalChapters = chapters.size,
            currentChapterTitle = chapters.first().title,
            chapters = chapters,
            groupId = cat.group.id,
            group = cat.group,
            paperId = cat.paper.id,
            paper = cat.paper,
            curriculumId = curriculumId
        )
    }

    // --- Mathematics Syllabus Builder ---
    private fun buildMathSubject(frameworkTag: String): SyllabusSubject {
        val ch1Topics = listOf(
            SyllabusTopic(
                id = "m_quad_1",
                chapterId = "ch_m_1",
                title = "Standard Form & Factorization Method",
                titleBn = "দ্বিঘাত সমীকরণের আদর্শ রূপ ও উৎপাদকে বিশ্লেষণ",
                conceptExplanation = "A quadratic equation has the general form ax² + bx + c = 0 where a ≠ 0. The roots can be solved by factoring into two linear factors (x - p)(x - q) = 0.",
                conceptExplanationBn = "যেকোনো দ্বিঘাত সমীকরণের সাধারণ রূপ হলো ax² + bx + c = 0 (যেখানে a ≠ 0)। একে উৎপাদকে বিশ্লেষণ করে দুটি রৈখিক সমীকরণ গঠন করে সমাধান বের করা যায়।",
                exampleProblem = "Solve by factoring: x² - 5x + 6 = 0",
                exampleSolution = "x² - 3x - 2x + 6 = 0  ⇒  x(x - 3) - 2(x - 3) = 0  ⇒  (x - 3)(x - 2) = 0. Therefore, roots are x = 2, 3.",
                isCompleted = completedTopicIds.contains("m_quad_1"),
                progressPercent = topicProgressMap["m_quad_1"] ?: 0,
                practiceQuestions = listOf(
                    PracticeQuestionItem(
                        id = "pq_m_1",
                        topicId = "m_quad_1",
                        questionText = "Find the roots of x² - 7x + 12 = 0 by factorization.",
                        difficulty = QuestionDifficulty.EASY,
                        hint = "Look for two numbers whose product is 12 and sum is -7 (-3 and -4).",
                        steps = listOf(
                            "Step 1: Write x² - 4x - 3x + 12 = 0",
                            "Step 2: Group terms: x(x - 4) - 3(x - 4) = 0",
                            "Step 3: Factor out (x - 4): (x - 4)(x - 3) = 0",
                            "Step 4: Solve roots: x = 3, 4"
                        ),
                        correctAnswer = "x = 3, 4",
                        isBookmarked = bookmarkedQuestionIds.contains("pq_m_1"),
                        isAddedToMistakes = mistakeQuestionIds.contains("pq_m_1")
                    ),
                    PracticeQuestionItem(
                        id = "pq_m_2",
                        topicId = "m_quad_1",
                        questionText = "Solve 2x² - 5x + 2 = 0 using splitting the middle term.",
                        difficulty = QuestionDifficulty.MEDIUM,
                        hint = "Product a·c = 2 × 2 = 4. Factors summing to -5 are -4 and -1.",
                        steps = listOf(
                            "Step 1: 2x² - 4x - x + 2 = 0",
                            "Step 2: 2x(x - 2) - 1(x - 2) = 0",
                            "Step 3: (2x - 1)(x - 2) = 0",
                            "Step 4: x = 1/2 or x = 2"
                        ),
                        correctAnswer = "x = 1/2, 2",
                        isBookmarked = bookmarkedQuestionIds.contains("pq_m_2"),
                        isAddedToMistakes = mistakeQuestionIds.contains("pq_m_2")
                    )
                ),
                quickQuizQuestions = listOf(
                    QuizQuestion(
                        id = "qq_m_1",
                        subjectId = "math",
                        topic = "Factorization",
                        questionText = "What are the solutions to x² - 9 = 0?",
                        options = listOf("x = 3 only", "x = -3 only", "x = ±3", "x = 9"),
                        correctIndex = 2,
                        explanation = "x² - 9 = (x - 3)(x + 3) = 0, giving x = ±3."
                    )
                )
            ),
            SyllabusTopic(
                id = "m_quad_2",
                chapterId = "ch_m_1",
                title = "Quadratic Formula & Discriminant Analysis",
                titleBn = "দ্বিঘাত সূত্র ও নিশ্চয়ক (Discriminant) বিশ্লেষণ",
                conceptExplanation = "For ax² + bx + c = 0, the roots are x = (-b ± √(b² - 4ac)) / (2a). The discriminant D = b² - 4ac determines the nature of the roots.",
                conceptExplanationBn = "দ্বিঘাত সূত্র x = (-b ± √(b² - 4ac)) / (2a)। নিশ্চয়ক D = b² - 4ac এর মানের উপর ভিত্তি করে মূলের প্রকৃতি (বাস্তব, সমান বা অবাস্তব) নির্ধারিত হয়।",
                exampleProblem = "Determine the nature of roots for 2x² - 4x + 2 = 0.",
                exampleSolution = "D = b² - 4ac = (-4)² - 4(2)(2) = 16 - 16 = 0. Since D = 0, the roots are real and equal (x = 1).",
                isCompleted = completedTopicIds.contains("m_quad_2"),
                progressPercent = topicProgressMap["m_quad_2"] ?: 72,
                isWeak = true,
                practiceQuestions = listOf(
                    PracticeQuestionItem(
                        id = "pq_m_3",
                        topicId = "m_quad_2",
                        questionText = "Calculate the discriminant of 3x² - 5x + 2 = 0 and specify root nature.",
                        difficulty = QuestionDifficulty.HARD,
                        hint = "Compute D = (-5)² - 4(3)(2). If D > 0 and a perfect square, roots are real and rational.",
                        steps = listOf(
                            "Step 1: Identify a = 3, b = -5, c = 2",
                            "Step 2: D = (-5)² - 4(3)(2) = 25 - 24 = 1",
                            "Step 3: D > 0 and 1² = 1 (perfect square)",
                            "Step 4: Roots are real, rational, and unequal"
                        ),
                        correctAnswer = "D = 1, Real, Rational & Unequal",
                        isBookmarked = bookmarkedQuestionIds.contains("pq_m_3"),
                        isAddedToMistakes = mistakeQuestionIds.contains("pq_m_3")
                    )
                )
            )
        )

        val ch2Topics = listOf(
            SyllabusTopic(
                id = "m_coord_1",
                chapterId = "ch_m_2",
                title = "Distance Formula & Collinear Points",
                titleBn = "দূরত্ব নির্ণয়ের সূত্র ও সমরেখ বিন্দু",
                conceptExplanation = "Distance between (x₁, y₁) and (x₂, y₂) is d = √((x₂ - x₁)² + (y₂ - y₁)²).",
                conceptExplanationBn = "স্থানাঙ্ক জ্যামিতিতে দুটি বিন্দুর মধ্যবর্তী দূরত্ব নির্ণয়ের সূত্র d = √((x₂ - x₁)² + (y₂ - y₁)²)।",
                exampleProblem = "Find distance between P(1, 2) and Q(4, 6).",
                exampleSolution = "d = √((4 - 1)² + (6 - 2)²) = √(3² + 4²) = √(9 + 16) = √25 = 5 units.",
                isCompleted = completedTopicIds.contains("m_coord_1"),
                progressPercent = topicProgressMap["m_coord_1"] ?: 65
            )
        )

        val ch3Topics = listOf(
            SyllabusTopic(
                id = "m_trig_1",
                chapterId = "ch_m_3",
                title = "Trigonometric Ratios & Identities",
                titleBn = "ত্রিকোণমিতিক অনুপাত ও অভেদাবলী",
                conceptExplanation = "Fundamental identities: sin²θ + cos²θ = 1, sec²θ - tan²θ = 1, cosec²θ - cot²θ = 1.",
                conceptExplanationBn = "মৌলিক ত্রিকোণমিতিক অভেদ: sin²θ + cos²θ = 1, sec²θ - tan²θ = 1, cosec²θ - cot²θ = 1।",
                exampleProblem = "Prove that (sinθ / cosθ) + (cosθ / sinθ) = secθ·cosecθ.",
                exampleSolution = "(sin²θ + cos²θ) / (sinθ·cosθ) = 1 / (sinθ·cosθ) = secθ·cosecθ.",
                isCompleted = completedTopicIds.contains("m_trig_1"),
                progressPercent = topicProgressMap["m_trig_1"] ?: 72
            )
        )

        val ch4Topics = listOf(
            SyllabusTopic(
                id = "m_stat_1",
                chapterId = "ch_m_4",
                title = "Grouped Data Mean, Median & Mode",
                titleBn = "শ্রেণিবিন্যাসকৃত উপাত্তের গড়, মধ্যক ও প্রচুরক",
                conceptExplanation = "Mean = ∑(fi·xi) / N. Median = L + [ (N/2 - Fc) / fm ] × h.",
                conceptExplanationBn = "উপাত্তের গাণিতিক গড়, মধ্যক ও প্রচুরক নির্ণয়ের সাধারণ সূত্রাবলি।",
                exampleProblem = "Calculate the class mark of interval 31 - 40.",
                exampleSolution = "Class mark xi = (31 + 40) / 2 = 71 / 2 = 35.5.",
                isCompleted = completedTopicIds.contains("m_stat_1"),
                progressPercent = topicProgressMap["m_stat_1"] ?: 92
            )
        )

        val chapters = listOf(
            SyllabusChapter(
                id = "ch_m_1",
                subjectId = "math",
                chapterNumber = 4,
                title = "Quadratic Equations",
                titleBn = "দ্বিঘাত সমীকরণ",
                progressPercent = calculateChapterProgress(ch1Topics),
                status = ChapterStatus.IN_PROGRESS,
                estimatedHours = "4.5 hrs",
                curriculumFrameworkTag = frameworkTag,
                learningObjectives = listOf(
                    "Understand standard form ax² + bx + c = 0",
                    "Solve equations via middle-term splitting and completing the square",
                    "Apply the quadratic formula with discriminant analysis",
                    "Solve real-world geometric and rate problems using quadratics"
                ),
                topics = ch1Topics,
                revisionSummary = "Remember: Discriminant D = b² - 4ac. When D > 0 roots are real; D = 0 roots are equal; D < 0 roots are imaginary.",
                chapterQuiz = MockEducationalRepository.sampleQuizzes.filter { it.subjectId == "math" }
            ),
            SyllabusChapter(
                id = "ch_m_2",
                subjectId = "math",
                chapterNumber = 11,
                title = "Coordinate Geometry",
                titleBn = "স্থানাঙ্ক জ্যামিতি",
                progressPercent = calculateChapterProgress(ch2Topics),
                status = ChapterStatus.IN_PROGRESS,
                estimatedHours = "3.8 hrs",
                curriculumFrameworkTag = frameworkTag,
                learningObjectives = listOf(
                    "Plot points on Cartesian coordinate plane",
                    "Calculate distances between two coordinate points",
                    "Compute slopes and gradients of straight lines",
                    "Find the area of triangles using coordinates"
                ),
                topics = ch2Topics,
                revisionSummary = "Slope m = (y₂ - y₁) / (x₂ - x₁). Parallel lines have equal slopes (m₁ = m₂).",
                chapterQuiz = emptyList()
            ),
            SyllabusChapter(
                id = "ch_m_3",
                subjectId = "math",
                chapterNumber = 9,
                title = "Trigonometric Ratios",
                titleBn = "ত্রিকোণমিতিক অনুপাত",
                progressPercent = calculateChapterProgress(ch3Topics),
                status = ChapterStatus.NOT_STARTED,
                estimatedHours = "5.0 hrs",
                curriculumFrameworkTag = frameworkTag,
                learningObjectives = listOf(
                    "Master sin, cos, tan, cot, sec, cosec definitions",
                    "Memorize standard values for 0°, 30°, 45°, 60°, 90°",
                    "Prove trigonometric algebraic identities",
                    "Apply angle of elevation and depression to height problems"
                ),
                topics = ch3Topics,
                revisionSummary = "Remember sin²θ + cos²θ = 1 and tanθ = sinθ/cosθ.",
                chapterQuiz = emptyList()
            ),
            SyllabusChapter(
                id = "ch_m_4",
                subjectId = "math",
                chapterNumber = 17,
                title = "Statistics",
                titleBn = "পরিসংখ্যান",
                progressPercent = calculateChapterProgress(ch4Topics),
                status = ChapterStatus.COMPLETED,
                estimatedHours = "3.2 hrs",
                curriculumFrameworkTag = frameworkTag,
                learningObjectives = listOf(
                    "Construct frequency distribution tables",
                    "Calculate direct and shortcut mean",
                    "Determine median and mode of grouped data",
                    "Draw histogram, frequency polygon and ogive curves"
                ),
                topics = ch4Topics,
                revisionSummary = "Mode = L + [ f₁ / (f₁ + f₂) ] × h.",
                chapterQuiz = emptyList()
            )
        )

        val completedCount = chapters.count { it.status == ChapterStatus.COMPLETED }
        val avgProgress = chapters.map { it.progressPercent }.average().toInt()

        return SyllabusSubject(
            id = "math",
            name = "Mathematics",
            nameBn = "গণিত",
            icon = "🧮",
            colorHex = 0xFF3D5AFE,
            progressPercent = avgProgress,
            completedChapters = completedCount,
            totalChapters = chapters.size,
            currentChapterTitle = "Quadratic Equations (দ্বিঘাত সমীকরণ)",
            weakTopics = listOf("Discriminant Analysis", "Calculus Limits"),
            nextRecommendedLesson = "Quadratic Formula & Discriminant Analysis",
            chapters = chapters
        )
    }

    // --- Physics Syllabus Builder ---
    private fun buildPhysicsSubject(frameworkTag: String): SyllabusSubject {
        val ch1Topics = listOf(
            SyllabusTopic(
                id = "p_motion_1",
                chapterId = "ch_p_1",
                title = "Distance, Displacement & Velocity",
                titleBn = "দূরত্ব, সরণ, দ্রুতি ও বেগ",
                conceptExplanation = "Scalar vs Vector: Distance is total ground covered; displacement is shortest straight line from origin to final point with direction.",
                conceptExplanationBn = "দূরত্ব হলো মোট অতিক্রান্ত পথ (স্কেলার রাশি), আর সরণ হলো নির্দিষ্ট দিকে আদি ও শেষ বিন্দুর সরলরৈখিক ব্যবধান (ভেক্টর রাশি)।",
                exampleProblem = "An athlete completes a circular track of radius 7m in 20s. Find displacement.",
                exampleSolution = "Since starting and ending positions coincide, net displacement = 0 m. Distance = 2πr = 2 × (22/7) × 7 = 44 m.",
                isCompleted = completedTopicIds.contains("p_motion_1"),
                progressPercent = topicProgressMap["p_motion_1"] ?: 0
            ),
            SyllabusTopic(
                id = "p_motion_2",
                chapterId = "ch_p_1",
                title = "Newton's Laws & Equations of Motion",
                titleBn = "গতির সমীকরণ ও নিউটনের সূত্রাবলী",
                conceptExplanation = "Four motion equations under uniform acceleration: v = u + at, s = ((u+v)/2)t, s = ut + 0.5at², v² = u² + 2as. Force F = ma.",
                conceptExplanationBn = "সমত্বরণে গতিশীল বস্তুর চারটি মৌলিক সমীকরণ: v = u + at, s = ut + 0.5at², v² = u² + 2as। বল ও ত্বরণের সম্পর্ক F = ma।",
                exampleProblem = "Calculate force needed to accelerate a 5 kg mass from rest to 10 m/s in 2 seconds.",
                exampleSolution = "a = (v - u) / t = (10 - 0) / 2 = 5 m/s². Force F = m × a = 5 kg × 5 m/s² = 25 N.",
                isCompleted = completedTopicIds.contains("p_motion_2"),
                progressPercent = topicProgressMap["p_motion_2"] ?: 60,
                isWeak = true,
                practiceQuestions = listOf(
                    PracticeQuestionItem(
                        id = "pq_p_1",
                        topicId = "p_motion_2",
                        questionText = "A car starts from rest with acceleration 2 m/s². How far will it travel in 5 seconds?",
                        difficulty = QuestionDifficulty.EASY,
                        hint = "Use s = ut + 0.5at² with u = 0.",
                        steps = listOf(
                            "Step 1: Given u = 0, a = 2 m/s², t = 5 s",
                            "Step 2: Formula s = 0 + 0.5 × 2 × (5)²",
                            "Step 3: s = 1 × 25 = 25 meters"
                        ),
                        correctAnswer = "25 meters",
                        isBookmarked = bookmarkedQuestionIds.contains("pq_p_1"),
                        isAddedToMistakes = mistakeQuestionIds.contains("pq_p_1")
                    )
                )
            )
        )

        val ch2Topics = listOf(
            SyllabusTopic(
                id = "p_energy_1",
                chapterId = "ch_p_2",
                title = "Work, Kinetic Energy & Potential Energy",
                titleBn = "কাজ, গতিশক্তি ও বিভব শক্তি",
                conceptExplanation = "Work W = F·s·cosθ. Kinetic energy Ek = 0.5mv², Potential energy Ep = mgh.",
                conceptExplanationBn = "কাজ W = F·s·cosθ (জুল)। গতিশক্তি Ek = 0.5mv² এবং বিভব শক্তি Ep = mgh।",
                exampleProblem = "Find kinetic energy of a 2 kg object moving at 4 m/s.",
                exampleSolution = "Ek = 0.5 × 2 kg × (4 m/s)² = 0.5 × 2 × 16 = 16 Joules.",
                isCompleted = completedTopicIds.contains("p_energy_1"),
                progressPercent = topicProgressMap["p_energy_1"] ?: 76
            )
        )

        val chapters = listOf(
            SyllabusChapter(
                id = "ch_p_1",
                subjectId = "physics",
                chapterNumber = 2,
                title = "Motion & Force",
                titleBn = "গতি ও বল",
                progressPercent = calculateChapterProgress(ch1Topics),
                status = ChapterStatus.IN_PROGRESS,
                estimatedHours = "4.0 hrs",
                curriculumFrameworkTag = frameworkTag,
                learningObjectives = listOf(
                    "Differentiate between scalars and vectors",
                    "Apply equations of motion under uniform acceleration",
                    "Interpret velocity-time and distance-time graphs",
                    "Calculate momentum and force from Newton's Second Law"
                ),
                topics = ch1Topics,
                revisionSummary = "Acceleration due to gravity g = 9.8 m/s². In free fall, use v = u + gt.",
                chapterQuiz = MockEducationalRepository.sampleQuizzes.filter { it.subjectId == "physics" }
            ),
            SyllabusChapter(
                id = "ch_p_2",
                subjectId = "physics",
                chapterNumber = 4,
                title = "Work, Power & Energy",
                titleBn = "কাজ, ক্ষমতা ও শক্তি",
                progressPercent = calculateChapterProgress(ch2Topics),
                status = ChapterStatus.NOT_STARTED,
                estimatedHours = "3.5 hrs",
                curriculumFrameworkTag = frameworkTag,
                learningObjectives = listOf(
                    "Define work in scientific terms and its unit (Joule)",
                    "Derive formulas for kinetic and potential energy",
                    "Apply law of conservation of mechanical energy",
                    "Calculate power (P = W/t) and efficiency"
                ),
                topics = ch2Topics,
                revisionSummary = "Total mechanical energy E = Ek + Ep remains constant in a closed conservative system.",
                chapterQuiz = emptyList()
            )
        )

        val completedCount = chapters.count { it.status == ChapterStatus.COMPLETED }
        val avgProgress = chapters.map { it.progressPercent }.average().toInt()

        return SyllabusSubject(
            id = "physics",
            name = "Physics",
            nameBn = "পদার্থবিজ্ঞান",
            icon = "⚛️",
            colorHex = 0xFF0891B2,
            progressPercent = avgProgress,
            completedChapters = completedCount,
            totalChapters = chapters.size,
            currentChapterTitle = "Motion & Force (গতি ও বল)",
            weakTopics = listOf("Newton's 2nd Law Numerical Problems"),
            nextRecommendedLesson = "Newton's Laws & Equations of Motion",
            chapters = chapters
        )
    }

    // --- Chemistry Syllabus Builder ---
    private fun buildChemistrySubject(frameworkTag: String): SyllabusSubject {
        val ch1Topics = listOf(
            SyllabusTopic(
                id = "c_matter_1",
                chapterId = "ch_c_1",
                title = "Subatomic Particles & Electronic Configuration",
                titleBn = "পরমাণুর কণিকাসমূহ ও ইলেকট্রন বিন্যাস",
                conceptExplanation = "Protons and neutrons reside in the nucleus; electrons orbit in 2n² shells (K, L, M, N) and subshells (s, p, d, f) according to Aufbau principle.",
                conceptExplanationBn = "পরমাণুর কেন্দ্রে প্রোটন ও নিউট্রন থাকে এবং চারপাশের শক্তিস্তরে ইলেকট্রন আউফবাউ নীতি অনুযায়ী বিন্যস্ত থাকে।",
                exampleProblem = "Write electronic configuration for Sodium (Na, Z = 11).",
                exampleSolution = "Na (11): 1s² 2s² 2p⁶ 3s¹.",
                isCompleted = completedTopicIds.contains("c_matter_1"),
                progressPercent = topicProgressMap["c_matter_1"] ?: 100
            )
        )

        val ch2Topics = listOf(
            SyllabusTopic(
                id = "c_bond_1",
                chapterId = "ch_c_2",
                title = "Ionic, Covalent & Metallic Bonding",
                titleBn = "আয়নিক, সমযোজী ও ধাতব বন্ধন",
                conceptExplanation = "Ionic bonds form by complete electron transfer between metal and non-metal; covalent bonds form by mutual sharing of electron pairs between non-metals.",
                conceptExplanationBn = "ধাতু ও অধাতুর মধ্যে ইলেকট্রন আদান-প্রদানে আয়নিক বন্ধন এবং অধাতুদ্বয়ের মধ্যে ইলেকট্রন শেয়ারের মাধ্যমে সমযোজী বন্ধন তৈরি হয়।",
                exampleProblem = "Explain bonding in Water (H₂O).",
                exampleSolution = "Oxygen shares one electron with each of two Hydrogen atoms, creating two single covalent bonds (polar covalent).",
                isCompleted = completedTopicIds.contains("c_bond_1"),
                progressPercent = topicProgressMap["c_bond_1"] ?: 62,
                isWeak = true
            )
        )

        val chapters = listOf(
            SyllabusChapter(
                id = "ch_c_1",
                subjectId = "chemistry",
                chapterNumber = 3,
                title = "Structure of Matter",
                titleBn = "পদার্থের গঠন",
                progressPercent = calculateChapterProgress(ch1Topics),
                status = ChapterStatus.COMPLETED,
                estimatedHours = "3.5 hrs",
                curriculumFrameworkTag = frameworkTag,
                learningObjectives = listOf(
                    "Understand atomic mass, isotopes and atomic number",
                    "Apply Bohr's atomic model and energy levels",
                    "Write electronic configuration using 2n² and s,p,d,f orbitals"
                ),
                topics = ch1Topics,
                revisionSummary = "Valence electrons in outermost shell dictate chemical reactivity and group placement in periodic table.",
                chapterQuiz = MockEducationalRepository.sampleQuizzes.filter { it.subjectId == "chemistry" }
            ),
            SyllabusChapter(
                id = "ch_c_2",
                subjectId = "chemistry",
                chapterNumber = 5,
                title = "Chemical Bonds",
                titleBn = "রাসায়নিক বন্ধন",
                progressPercent = calculateChapterProgress(ch2Topics),
                status = ChapterStatus.IN_PROGRESS,
                estimatedHours = "4.2 hrs",
                curriculumFrameworkTag = frameworkTag,
                learningObjectives = listOf(
                    "Understand octet and duplet rules",
                    "Explain formation of ionic compounds and their properties (high melting point, conductivity in solution)",
                    "Explain formation and properties of covalent compounds",
                    "Understand metallic bond and electron sea model"
                ),
                topics = ch2Topics,
                revisionSummary = "Ionic compounds conduct electricity only in molten or aqueous state, not in solid crystal state.",
                chapterQuiz = emptyList()
            )
        )

        val completedCount = chapters.count { it.status == ChapterStatus.COMPLETED }
        val avgProgress = chapters.map { it.progressPercent }.average().toInt()

        return SyllabusSubject(
            id = "chemistry",
            name = "Chemistry",
            nameBn = "রসায়ন",
            icon = "🧪",
            colorHex = 0xFF7C3AED,
            progressPercent = avgProgress,
            completedChapters = completedCount,
            totalChapters = chapters.size,
            currentChapterTitle = "Chemical Bonds (রাসায়নিক বন্ধন)",
            weakTopics = listOf("Covalent vs Ionic Bonding"),
            nextRecommendedLesson = "Ionic, Covalent & Metallic Bonding",
            chapters = chapters
        )
    }

    // --- English Syllabus Builder ---
    private fun buildEnglishSubject(frameworkTag: String): SyllabusSubject {
        val ch1Topics = listOf(
            SyllabusTopic(
                id = "e_verbs_1",
                chapterId = "ch_e_1",
                title = "Right Form of Verbs & Subject-Verb Agreement",
                titleBn = "ক্রিয়ার সঠিক রূপ ও Subject-Verb Agreement",
                conceptExplanation = "Singular subjects take singular verbs; plural subjects take plural verbs. Modal auxiliaries are followed by base form.",
                conceptExplanationBn = "Subject singular হলে verb singular হবে। Modal auxiliary এর পর সর্বদা verb এর base form বসে।",
                exampleProblem = "Correct: One of the boys (be) absent yesterday.",
                exampleSolution = "Answer: One of the boys was absent yesterday. (Subject is 'One', not 'boys').",
                isCompleted = completedTopicIds.contains("e_verbs_1"),
                progressPercent = topicProgressMap["e_verbs_1"] ?: 85
            )
        )

        val chapters = listOf(
            SyllabusChapter(
                id = "ch_e_1",
                subjectId = "english",
                chapterNumber = 1,
                title = "Grammar & Structure",
                titleBn = "ইংরেজি ব্যাকরণ ও বাক্য গঠন",
                progressPercent = calculateChapterProgress(ch1Topics),
                status = ChapterStatus.IN_PROGRESS,
                estimatedHours = "3.0 hrs",
                curriculumFrameworkTag = frameworkTag,
                learningObjectives = listOf(
                    "Master Subject-Verb Agreement rules",
                    "Apply correct tense forms in contextual passage editing",
                    "Understand active and passive voice conversions",
                    "Use appropriate prepositions and idioms in composition"
                ),
                topics = ch1Topics,
                revisionSummary = "Watch out for phrases like 'as well as', 'along with', 'in addition to' which follow the first subject.",
                chapterQuiz = MockEducationalRepository.sampleQuizzes.filter { it.subjectId == "english" }
            )
        )

        val completedCount = chapters.count { it.status == ChapterStatus.COMPLETED }
        val avgProgress = chapters.map { it.progressPercent }.average().toInt()

        return SyllabusSubject(
            id = "english",
            name = "English",
            nameBn = "ইংরেজি",
            icon = "🇬🇧",
            colorHex = 0xFFD97706,
            progressPercent = avgProgress,
            completedChapters = completedCount,
            totalChapters = chapters.size,
            currentChapterTitle = "Grammar & Structure (ইংরেজি ব্যাকরণ)",
            weakTopics = listOf("Appropriate Prepositions"),
            nextRecommendedLesson = "Right Form of Verbs & Subject-Verb Agreement",
            chapters = chapters
        )
    }

    // --- Biology Syllabus Builder ---
    private fun buildBiologySubject(frameworkTag: String): SyllabusSubject {
        val ch1Topics = listOf(
            SyllabusTopic(
                id = "b_cell_1",
                chapterId = "ch_b_1",
                title = "Plant & Animal Cell Organelles",
                titleBn = "উদ্ভিদ ও প্রাণী কোষের অঙ্গাণুসমূহ",
                conceptExplanation = "Plant cells have chloroplasts and cellulose cell walls; animal cells contain centrosomes and lack plastids.",
                conceptExplanationBn = "উদ্ভিদ কোষে ক্লোরোপ্লাস্ট ও সেলুলোজ প্রাচীর থাকে; প্রাণী কোষে সেন্ট্রোজোম উপস্থিত থাকে।",
                exampleProblem = "Identify the powerhouse of the cell.",
                exampleSolution = "Mitochondria is the powerhouse of the cell as it produces cellular ATP.",
                isCompleted = false,
                progressPercent = 40
            )
        )
        val chapters = listOf(
            SyllabusChapter(
                id = "ch_b_1",
                subjectId = "biology",
                chapterNumber = 2,
                title = "Cells and Tissues",
                titleBn = "জীবকোষ ও টিস্যু",
                progressPercent = 40,
                status = ChapterStatus.IN_PROGRESS,
                estimatedHours = "3.2 hrs",
                curriculumFrameworkTag = frameworkTag,
                learningObjectives = listOf("Identify plant and animal cell components", "Explain functions of mitochondria and chloroplasts"),
                topics = ch1Topics
            )
        )
        return SyllabusSubject(
            id = "biology",
            name = "Biology",
            nameBn = "জীববিজ্ঞান",
            icon = "🧬",
            colorHex = 0xFF059669,
            progressPercent = 40,
            completedChapters = 0,
            totalChapters = 1,
            currentChapterTitle = "Cells and Tissues (জীবকোষ ও টিস্যু)",
            chapters = chapters
        )
    }

    // --- ICT Syllabus Builder ---
    private fun buildIctSubject(frameworkTag: String): SyllabusSubject {
        val ch1Topics = listOf(
            SyllabusTopic(
                id = "ict_sec_1",
                chapterId = "ch_ict_1",
                title = "Computer Security, Passwords & Cyber Ethics",
                titleBn = "কম্পিউটার ও ব্যবহারকারীর নিরাপত্তা এবং সাইবার নীতি",
                conceptExplanation = "Two-factor authentication (2FA), strong passwords, and firewall protect against malware and unauthorized access.",
                conceptExplanationBn = "দ্বিমুখী যাচাইকরণ (2FA), শক্তিশালী পাসওয়ার্ড এবং ফায়ারওয়াল সাইবার নিরাপত্তা নিশ্চিত করে।",
                exampleProblem = "What constitutes a strong password?",
                exampleSolution = "At least 12 characters including uppercase, lowercase, numbers, and special symbols.",
                isCompleted = true,
                progressPercent = 100
            )
        )
        val chapters = listOf(
            SyllabusChapter(
                id = "ch_ict_1",
                subjectId = "ict",
                chapterNumber = 3,
                title = "Digital Security & World",
                titleBn = "আমার শিক্ষায় ইন্টারনেট ও নিরাপত্তা",
                progressPercent = 100,
                status = ChapterStatus.COMPLETED,
                estimatedHours = "2.5 hrs",
                curriculumFrameworkTag = frameworkTag,
                learningObjectives = listOf("Implement safe online practices", "Recognize malware threats"),
                topics = ch1Topics
            )
        )
        return SyllabusSubject(
            id = "ict",
            name = "ICT",
            nameBn = "তথ্য ও যোগাযোগ প্রযুক্তি",
            icon = "💻",
            colorHex = 0xFF0284C7,
            progressPercent = 100,
            completedChapters = 1,
            totalChapters = 1,
            currentChapterTitle = "Digital Security & World",
            chapters = chapters
        )
    }

    private fun calculateChapterProgress(topics: List<SyllabusTopic>): Int {
        if (topics.isEmpty()) return 0
        return topics.map { it.progressPercent }.average().toInt()
    }
}
