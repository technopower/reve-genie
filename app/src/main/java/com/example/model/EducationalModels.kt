package com.example.model

enum class AcademicGroup(
    val id: String,
    val titleEn: String,
    val titleBn: String
) {
    GENERAL_JUNIOR("general_junior", "General (Junior)", "সাধারণ (জুনিয়র)"),
    SCIENCE("science", "Science", "বিজ্ঞান"),
    BUSINESS_STUDIES("business_studies", "Business Studies", "ব্যবসায় শিক্ষা"),
    HUMANITIES("humanities", "Humanities", "মানবিক");

    companion object {
        fun fromId(id: String?): AcademicGroup {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: SCIENCE
        }
    }
}

enum class SubjectPaper(
    val id: String,
    val titleEn: String,
    val titleBn: String
) {
    NONE("none", "Single Paper", "একক পত্র"),
    PAPER_1("paper_1", "1st Paper", "১ম পত্র"),
    PAPER_2("paper_2", "2nd Paper", "২য় পত্র");

    companion object {
        fun fromId(id: String?): SubjectPaper {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: NONE
        }
    }
}

enum class SubjectType(val id: String, val titleEn: String, val titleBn: String, val icon: String, val colorHex: Long) {
    MATH("math", "Mathematics", "গণিত", "🧮", 0xFF3D5AFE),
    PHYSICS("physics", "Physics", "পদার্থবিজ্ঞান", "⚛️", 0xFF0891B2),
    CHEMISTRY("chemistry", "Chemistry", "রসায়ন", "🧪", 0xFF7C3AED),
    ENGLISH("english", "English", "ইংরেজি", "🇬🇧", 0xFFD97706)
}

data class UserProfile(
    val name: String = "Alex Rahman",
    val email: String = "alex.rahman@example.com",
    val educationLevel: String = "Class 10 (SSC)",
    val curriculum: String = "NCTB (Bangla Version)",
    val language: String = "বাংলা",
    val dailyGoalMinutes: Int = 30,
    val completedMinutesToday: Int = 18,
    val streakDays: Int = 7,
    val xp: Int = 1240,
    val level: Int = 4
)

data class TopicItem(
    val id: String,
    val subjectId: String,
    val title: String,
    val titleBn: String,
    val masteryPercent: Int,
    val isWeakTopic: Boolean = false
)

data class BookSourceContext(
    val bookId: String,
    val bookTitle: String,
    val bookTitleBn: String = "",
    val chapterTitle: String,
    val topicTitle: String,
    val className: String,
    val group: String = "",
    val paper: String = "",
    val edition: String = ""
)

data class ChatMessage(
    val id: String,
    val sender: SenderType,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val mathFormula: String? = null,
    val physicsBreakdown: PhysicsBreakdown? = null,
    val chemistryEquation: String? = null,
    val englishCorrection: EnglishCorrection? = null,
    val sourceBookContext: BookSourceContext? = null,
    val stepByStepExplanation: List<String> = emptyList(),
    val exampleCase: String? = null,
    val isOutOfScope: Boolean = false
)

enum class SenderType {
    USER, AI
}

data class PhysicsBreakdown(
    val given: String,
    val formula: String,
    val solution: String,
    val answer: String
)

data class EnglishCorrection(
    val original: String,
    val corrected: String,
    val ruleExplanation: String,
    val betterSentence: String
)

data class QuizQuestion(
    val id: String,
    val subjectId: String,
    val topic: String,
    val questionText: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val difficulty: QuestionDifficulty = QuestionDifficulty.MEDIUM,
    val chapterId: String = "",
    val topicId: String = "",
    val curriculumId: String = "nctb_bn",
    val className: String = "",
    val groupId: String = AcademicGroup.SCIENCE.id,
    val group: AcademicGroup = AcademicGroup.SCIENCE,
    val paperId: String = SubjectPaper.NONE.id,
    val paper: SubjectPaper = SubjectPaper.NONE
)

data class MistakeItem(
    val id: String,
    val subject: String,
    val topic: String,
    val question: String,
    val wrongAnswer: String,
    val correctAnswer: String,
    val explanation: String,
    val category: String = "Concept", // Concept, Calculation, Formula, Grammar, Careless, Memory
    val className: String = "",
    val chapter: String = "",
    val groupId: String = AcademicGroup.SCIENCE.id,
    val group: AcademicGroup = AcademicGroup.SCIENCE,
    val paperId: String = SubjectPaper.NONE.id,
    val paper: SubjectPaper = SubjectPaper.NONE,
    val curriculumId: String = "nctb_bn"
)

data class StudyPlanItem(
    val time: String,
    val subject: String,
    val topic: String,
    val durationMinutes: Int,
    val isCompleted: Boolean = false,
    val className: String = "",
    val curriculumId: String = "nctb_bn",
    val groupId: String = AcademicGroup.SCIENCE.id,
    val group: AcademicGroup = AcademicGroup.SCIENCE,
    val paperId: String = SubjectPaper.NONE.id,
    val paper: SubjectPaper = SubjectPaper.NONE,
    val chapter: String = ""
)

data class AchievementItem(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val isUnlocked: Boolean,
    val progress: String
)

data class PdfDocument(
    val id: String,
    val fileName: String,
    val pages: Int,
    val subject: String,
    val uploadDate: String
)
