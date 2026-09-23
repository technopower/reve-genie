package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Auth : Screen("auth")
    object Home : Screen("home")
    object Chat : Screen("chat")
    object Scan : Screen("scan")
    object ScanResult : Screen("scan_result")
    object SubjectDetail : Screen("subject_detail")
    object Quiz : Screen("quiz")
    object Translate : Screen("translate")
    object MistakeNotebook : Screen("mistake_notebook")
    object StudyPlanner : Screen("study_planner")
    object PdfTeacher : Screen("pdf_teacher")
    object VoiceTeacher : Screen("voice_teacher")
    object Profile : Screen("profile")
    object Syllabus : Screen("syllabus")
    object ChapterOverview : Screen("chapter_overview")
    object TopicDetail : Screen("topic_detail")
}
