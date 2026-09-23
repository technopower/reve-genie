package com.example.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.PdfDocument
import com.example.model.TopicAiContext
import com.example.ui.navigation.Screen
import com.example.ui.screens.*
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.viewmodel.SyllabusViewModel

@Composable
fun ReveGenieApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }
    var selectedSubjectId by remember { mutableStateOf("math") }
    var selectedPdfDocument by remember { mutableStateOf<PdfDocument?>(null) }
    var selectedBookForAi by remember { mutableStateOf<com.example.model.Book?>(null) }
    var activeTopicContext by remember { mutableStateOf<TopicAiContext?>(null) }
    
    var lastScannedQuestion by remember { mutableStateOf("") }
    var lastScannedSolution by remember { mutableStateOf("") }

    val syllabusViewModel: SyllabusViewModel = viewModel()
    val syllabus by syllabusViewModel.syllabus.collectAsState()
    val selectedSyllabusSubjectId by syllabusViewModel.selectedSubjectId.collectAsState()
    val activeChapter by syllabusViewModel.activeChapter.collectAsState()
    val activeTopic by syllabusViewModel.activeTopic.collectAsState()

    // Navigation bar should appear on main tab screens
    val isMainTab = currentScreen is Screen.Home ||
            currentScreen is Screen.Syllabus ||
            currentScreen is Screen.Chat ||
            currentScreen is Screen.StudyPlanner ||
            currentScreen is Screen.Profile

    Scaffold(
        bottomBar = {
            if (isMainTab) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("main_bottom_nav_bar")
                ) {
                    NavigationBarItem(
                        selected = currentScreen is Screen.Home,
                        onClick = { currentScreen = Screen.Home },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = currentScreen is Screen.Syllabus,
                        onClick = { currentScreen = Screen.Syllabus },
                        icon = { Icon(Icons.Default.MenuBook, contentDescription = "Syllabus") },
                        label = { Text("Syllabus") }
                    )
                    NavigationBarItem(
                        selected = currentScreen is Screen.Chat && activeTopicContext == null,
                        onClick = {
                            activeTopicContext = null
                            currentScreen = Screen.Chat
                        },
                        icon = { Icon(Icons.Default.SmartToy, contentDescription = "AI Teacher") },
                        label = { Text("AI Teacher") }
                    )
                    NavigationBarItem(
                        selected = currentScreen is Screen.StudyPlanner,
                        onClick = { currentScreen = Screen.StudyPlanner },
                        icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Planner") },
                        label = { Text("Planner") }
                    )
                    NavigationBarItem(
                        selected = currentScreen is Screen.Profile,
                        onClick = { currentScreen = Screen.Profile },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile") }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            Crossfade(targetState = currentScreen, label = "screen_crossfade") { screen ->
                when (screen) {
                    is Screen.Splash -> {
                        SplashScreen(
                            onSplashComplete = { currentScreen = Screen.Onboarding }
                        )
                    }
                    is Screen.Onboarding -> {
                        OnboardingScreen(
                            onFinishOnboarding = { currentScreen = Screen.Auth }
                        )
                    }
                    is Screen.Auth -> {
                        AuthScreen(
                            onLoginSuccess = { currentScreen = Screen.Home }
                        )
                    }
                    is Screen.Home -> {
                        HomeDashboard(
                            onNavigateToChat = {
                                activeTopicContext = null
                                currentScreen = Screen.Chat
                            },
                            onNavigateToScan = { currentScreen = Screen.Scan },
                            onNavigateToVoice = { currentScreen = Screen.VoiceTeacher },
                            onNavigateToPdf = { currentScreen = Screen.PdfTeacher },
                            onNavigateToQuiz = { currentScreen = Screen.Quiz },
                            onNavigateToTranslate = { currentScreen = Screen.Translate },
                            onNavigateToSubject = { subjectId ->
                                selectedSubjectId = subjectId
                                currentScreen = Screen.SubjectDetail
                            },
                            onNavigateToStudyPlan = { currentScreen = Screen.StudyPlanner },
                            onNavigateToMistakes = { currentScreen = Screen.MistakeNotebook },
                            onNavigateToNotifications = { currentScreen = Screen.Home },
                            onNavigateToSyllabus = { currentScreen = Screen.Syllabus }
                        )
                    }
                    is Screen.Syllabus -> {
                        SyllabusHomeScreen(
                            syllabus = syllabus,
                            selectedSubjectId = selectedSyllabusSubjectId,
                            onSelectSubject = { syllabusViewModel.selectSubject(it) },
                            onOpenChapter = { chapter ->
                                syllabusViewModel.openChapter(chapter)
                                currentScreen = Screen.ChapterOverview
                            },
                            onOpenTopic = { topic, chapter, subject ->
                                syllabusViewModel.openChapter(chapter)
                                syllabusViewModel.openTopic(topic)
                                currentScreen = Screen.TopicDetail
                            },
                            onNavigateBack = { currentScreen = Screen.Home },
                            onNavigateToChatWithContext = { ctx ->
                                activeTopicContext = ctx
                                currentScreen = Screen.Chat
                            }
                        )
                    }
                    is Screen.ChapterOverview -> {
                        val activeSubj = syllabus.subjects.find { it.id == selectedSyllabusSubjectId }
                            ?: syllabus.subjects.first()
                        val ch = activeChapter ?: activeSubj.chapters.first()
                        ChapterOverviewScreen(
                            chapter = ch,
                            subject = activeSubj,
                            onNavigateBack = { currentScreen = Screen.Syllabus },
                            onOpenTopic = { topic ->
                                syllabusViewModel.openTopic(topic)
                                currentScreen = Screen.TopicDetail
                            },
                            onStartChapterQuiz = { currentScreen = Screen.Quiz },
                            onAskAiChapter = {
                                activeTopicContext = TopicAiContext(
                                    educationLevel = syllabus.educationLevel,
                                    curriculum = syllabus.curriculum,
                                    subjectId = activeSubj.id,
                                    subjectName = activeSubj.name,
                                    chapterId = ch.id,
                                    chapterTitle = ch.title,
                                    topicId = ch.topics.firstOrNull()?.id ?: "",
                                    topicTitle = ch.topics.firstOrNull()?.title ?: ch.title,
                                    topicTitleBn = ch.topics.firstOrNull()?.titleBn ?: ch.titleBn,
                                    language = syllabus.language
                                )
                                currentScreen = Screen.Chat
                            }
                        )
                    }
                    is Screen.TopicDetail -> {
                        val activeSubj = syllabus.subjects.find { it.id == selectedSyllabusSubjectId }
                            ?: syllabus.subjects.first()
                        val ch = activeChapter ?: activeSubj.chapters.first()
                        val tp = activeTopic ?: ch.topics.first()
                        TopicDetailScreen(
                            topic = tp,
                            chapter = ch,
                            subject = activeSubj,
                            onNavigateBack = { currentScreen = Screen.ChapterOverview },
                            onAskAiTeacher = { ctx ->
                                activeTopicContext = ctx
                                currentScreen = Screen.Chat
                            },
                            onToggleBookmark = { qId -> syllabusViewModel.toggleBookmark(tp.id, qId) },
                            onAddToMistakes = { qId -> syllabusViewModel.addQuestionToMistakes(tp.id, qId) },
                            onMarkCompleted = {
                                syllabusViewModel.completeTopic(activeSubj.id, ch.id, tp.id)
                            }
                        )
                    }
                    is Screen.Chat -> {
                        AiTeacherChatScreen(
                            onNavigateBack = { currentScreen = Screen.Home },
                            onNavigateToScan = { currentScreen = Screen.Scan },
                            onNavigateToVoice = { currentScreen = Screen.VoiceTeacher },
                            onNavigateToPdf = { currentScreen = Screen.PdfTeacher },
                            topicContext = activeTopicContext,
                            initialBook = selectedBookForAi,
                            initialPdf = selectedPdfDocument
                        )
                    }
                    is Screen.Scan -> {
                        ImageScannerScreen(
                            onNavigateBack = { currentScreen = Screen.Home },
                            onSolveComplete = { question, solution -> 
                                lastScannedQuestion = question
                                lastScannedSolution = solution
                                currentScreen = Screen.ScanResult 
                            }
                        )
                    }
                    is Screen.ScanResult -> {
                        ScanResultScreen(
                            questionText = lastScannedQuestion,
                            solutionText = lastScannedSolution,
                            onNavigateBack = { currentScreen = Screen.Scan },
                            onPracticeSimilar = { currentScreen = Screen.Quiz },
                            onSaveToMistakes = { currentScreen = Screen.MistakeNotebook }
                        )
                    }
                    is Screen.SubjectDetail -> {
                        SubjectDetailScreen(
                            subjectId = selectedSubjectId,
                            onNavigateBack = { currentScreen = Screen.Home },
                            onNavigateToQuiz = { currentScreen = Screen.Quiz },
                            onNavigateToChat = { currentScreen = Screen.Chat }
                        )
                    }
                    is Screen.Quiz -> {
                        QuizScreen(
                            initialClass = syllabus.educationLevel,
                            initialCurriculum = syllabus.curriculum,
                            initialSubjectId = selectedSyllabusSubjectId.ifEmpty { selectedSubjectId },
                            initialChapterId = activeChapter?.id,
                            initialTopicId = activeTopic?.id,
                            onNavigateBack = { currentScreen = Screen.Home },
                            onNavigateToMistakes = { currentScreen = Screen.MistakeNotebook },
                            initialGroup = syllabus.group,
                            initialPaper = syllabus.subjects.firstOrNull()?.paper ?: com.example.model.SubjectPaper.NONE
                        )
                    }
                    is Screen.Translate -> {
                        TranslationScreen(
                            onNavigateBack = { currentScreen = Screen.Home }
                        )
                    }
                    is Screen.MistakeNotebook -> {
                        MistakeNotebookScreen(
                            onNavigateBack = { currentScreen = Screen.Home },
                            onPracticeMistakes = { currentScreen = Screen.Quiz }
                        )
                    }
                    is Screen.StudyPlanner -> {
                        StudyPlannerScreen(
                            onNavigateBack = { currentScreen = Screen.Home }
                        )
                    }
                    is Screen.PdfTeacher -> {
                        PdfTeacherScreen(
                            onNavigateBack = { currentScreen = Screen.Home },
                            onOpenPdfChat = { pdf ->
                                selectedPdfDocument = pdf
                                currentScreen = Screen.Chat
                            },
                            onOpenBookChat = { book ->
                                selectedPdfDocument = book.toPdfDocument()
                                selectedBookForAi = book
                                activeTopicContext = book.toTopicAiContext(syllabus.language)
                                currentScreen = Screen.Chat
                            }
                        )
                    }
                    is Screen.VoiceTeacher -> {
                        VoiceTeacherScreen(
                            onNavigateBack = { currentScreen = Screen.Home }
                        )
                    }
                    is Screen.Profile -> {
                        ProfileAndSettingsScreen(
                            onNavigateBack = { currentScreen = Screen.Home },
                            onLogout = { currentScreen = Screen.Auth }
                        )
                    }
                }
            }
        }
    }
}
