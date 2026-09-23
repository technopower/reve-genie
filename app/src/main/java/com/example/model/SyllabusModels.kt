package com.example.model

enum class ChapterStatus {
    COMPLETED,
    IN_PROGRESS,
    NOT_STARTED,
    LOCKED
}

enum class QuestionDifficulty {
    EASY,
    MEDIUM,
    HARD
}

data class PracticeQuestionItem(
    val id: String,
    val topicId: String,
    val questionText: String,
    val difficulty: QuestionDifficulty,
    val hint: String,
    val steps: List<String>,
    val correctAnswer: String,
    val isBookmarked: Boolean = false,
    val isAddedToMistakes: Boolean = false
)

data class SyllabusTopic(
    val id: String,
    val chapterId: String,
    val title: String,
    val titleBn: String,
    val conceptExplanation: String,
    val conceptExplanationBn: String,
    val exampleProblem: String,
    val exampleSolution: String,
    val isCompleted: Boolean = false,
    val progressPercent: Int = 0,
    val isWeak: Boolean = false,
    val practiceQuestions: List<PracticeQuestionItem> = emptyList(),
    val quickQuizQuestions: List<QuizQuestion> = emptyList()
)

data class SyllabusChapter(
    val id: String,
    val subjectId: String,
    val chapterNumber: Int,
    val title: String,
    val titleBn: String,
    val progressPercent: Int = 0,
    val status: ChapterStatus = ChapterStatus.NOT_STARTED,
    val estimatedHours: String,
    val isVerifiedCurriculum: Boolean = true,
    val curriculumFrameworkTag: String = "Verified Curriculum Framework",
    val learningObjectives: List<String> = emptyList(),
    val topics: List<SyllabusTopic> = emptyList(),
    val revisionSummary: String = "",
    val chapterQuiz: List<QuizQuestion> = emptyList()
)

data class SyllabusSubject(
    val id: String,
    val name: String,
    val nameBn: String,
    val icon: String,
    val colorHex: Long,
    val progressPercent: Int = 0,
    val completedChapters: Int = 0,
    val totalChapters: Int = 0,
    val currentChapterTitle: String = "",
    val weakTopics: List<String> = emptyList(),
    val nextRecommendedLesson: String = "",
    val chapters: List<SyllabusChapter> = emptyList(),
    val groupId: String = AcademicGroup.SCIENCE.id,
    val group: AcademicGroup = AcademicGroup.SCIENCE,
    val paperId: String = SubjectPaper.NONE.id,
    val paper: SubjectPaper = SubjectPaper.NONE,
    val curriculumId: String = "nctb_bn"
)

data class PersonalizedSyllabus(
    val educationLevel: String,
    val curriculum: String,
    val language: String,
    val overallProgressPercent: Int,
    val completedChapters: Int,
    val totalChapters: Int,
    val streakDays: Int,
    val subjects: List<SyllabusSubject>,
    // Smart Recommendations
    val continueSubjectId: String,
    val continueSubjectName: String,
    val continueChapterTitle: String,
    val continueTopicTitle: String,
    val continueProgressPercent: Int,
    val recommendedSubjectName: String,
    val recommendedTopicTitle: String,
    val recommendedReason: String,
    val groupId: String = AcademicGroup.SCIENCE.id,
    val group: AcademicGroup = AcademicGroup.SCIENCE,
    val curriculumId: String = "nctb_bn"
)

data class TopicAiContext(
    val educationLevel: String,
    val curriculum: String,
    val subjectId: String,
    val subjectName: String,
    val chapterId: String,
    val chapterTitle: String,
    val topicId: String,
    val topicTitle: String,
    val topicTitleBn: String,
    val language: String,
    val groupId: String = AcademicGroup.SCIENCE.id,
    val group: AcademicGroup = AcademicGroup.SCIENCE,
    val paperId: String = SubjectPaper.NONE.id,
    val paper: SubjectPaper = SubjectPaper.NONE,
    val curriculumId: String = "nctb_bn",
    val bookId: String? = null,
    val bookTitle: String? = null,
    val bookTitleBn: String? = null,
    val bookEdition: String? = null
)
