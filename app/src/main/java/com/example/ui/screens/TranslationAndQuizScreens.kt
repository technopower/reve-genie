package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AcademicCatalog
import com.example.data.AcademicContentCatalog
import com.example.data.AcademicQuizSubject
import com.example.data.CurriculumQuizRepository
import com.example.data.MockEducationalRepository
import com.example.model.*
import com.example.ui.components.GenieTopBar
import com.example.ui.theme.*
import com.example.data.GeminiRepository
import com.example.data.SyllabusRepository
import com.example.data.TranslationLanguage
import com.example.data.TranslationLanguages
import com.example.util.AndroidTtsManager
import kotlinx.coroutines.launch

@Composable
fun TranslationScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        AndroidTtsManager.init(context)
    }
    DisposableEffect(Unit) {
        onDispose {
            AndroidTtsManager.stop()
        }
    }

    val isSpeaking by AndroidTtsManager.isSpeaking.collectAsState()

    var isTranslating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Default REQUIRED Language Pair: Source = Bangla, Target = English
    var sourceLang by remember { mutableStateOf(TranslationLanguages.BANGLA) }
    var targetLang by remember { mutableStateOf(TranslationLanguages.ENGLISH) }

    var inputText by remember { mutableStateOf("আমি ঢাকা যাই") }
    var correctedText by remember { mutableStateOf("আমি ঢাকায় যাই।") }
    var translatedText by remember { mutableStateOf("I go to Dhaka.") }

    // Picker state
    var pickingSource by remember { mutableStateOf<Boolean?>(null) } // true for source, false for target, null for closed

    Scaffold(
        topBar = {
            GenieTopBar(
                title = "Universal Translation",
                subtitle = "Smart Dual-Stage Sentence Correction & Translation",
                onBackClick = onNavigateBack,
                actions = {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SuccessEmerald.copy(alpha = 0.15f),
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Text(
                            text = "100% FREE",
                            color = SuccessEmerald,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Language Swap & Picker Bar
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Source Language Button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PrimaryIndigo.copy(alpha = 0.12f),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { pickingSource = true }
                            .testTag("source_lang_btn")
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(sourceLang.name, fontWeight = FontWeight.Bold, color = PrimaryIndigo, fontSize = 15.sp)
                            Text(sourceLang.nativeName, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Swap Button
                    IconButton(
                        onClick = {
                            val tempLang = sourceLang
                            sourceLang = targetLang
                            targetLang = tempLang

                            val tempText = inputText
                            inputText = translatedText
                            translatedText = tempText
                            correctedText = tempText
                            errorMessage = null
                        },
                        modifier = Modifier.testTag("swap_languages_btn")
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Swap Languages", tint = PrimaryIndigo, modifier = Modifier.size(28.dp))
                    }

                    // Target Language Button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PrimaryIndigo.copy(alpha = 0.12f),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { pickingSource = false }
                            .testTag("target_lang_btn")
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(targetLang.name, fontWeight = FontWeight.Bold, color = PrimaryIndigo, fontSize = 15.sp)
                            Text(targetLang.nativeName, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Input Card
            Text("Source Text (${sourceLang.name})", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = inputText,
                onValueChange = {
                    inputText = it
                    errorMessage = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .testTag("translation_input_field"),
                shape = RoundedCornerShape(14.dp),
                maxLines = 5,
                placeholder = { Text("Enter sentence to correct & translate...") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action Button
            Button(
                onClick = {
                    if (inputText.isBlank()) {
                        errorMessage = "Please enter some text to translate."
                        return@Button
                    }
                    errorMessage = null
                    scope.launch {
                        isTranslating = true
                        val result = GeminiRepository.translateWithCorrection(
                            text = inputText,
                            sourceLang = sourceLang.name,
                            targetLang = targetLang.name
                        )
                        correctedText = result.correctedText
                        translatedText = result.translatedText
                        if (!result.success && result.error != null) {
                            errorMessage = result.error
                        }
                        isTranslating = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("translate_now_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                enabled = !isTranslating
            ) {
                if (isTranslating) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Correcting & Translating...", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.Translate, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Correct & Translate Now", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚠️", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Corrected Sentence Card
            if (correctedText.isNotBlank()) {
                Text("Corrected Sentence (${sourceLang.name})", style = MaterialTheme.typography.labelMedium, color = PrimaryIndigo, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryContainerLight.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, PrimaryIndigo.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth().testTag("corrected_sentence_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✍️", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = correctedText,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = OnPrimaryContainerLight
                            )
                        }
                        if (!correctedText.trim().equals(inputText.trim(), ignoreCase = true)) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "✨ Automatically corrected grammar & structure before translation",
                                fontSize = 11.sp,
                                color = PrimaryIndigo,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Final Translation Result Card
            Text("Translated Text (${targetLang.name})", style = MaterialTheme.typography.labelMedium, color = PrimaryIndigo, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("translated_result_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = translatedText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp,
                        lineHeight = 24.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(translatedText))
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy")
                        }
                        OutlinedButton(
                            onClick = {
                                AndroidTtsManager.toggle(translatedText, "translation_tts")
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = if (isSpeaking) ButtonDefaults.outlinedButtonColors(
                                containerColor = PrimaryIndigo.copy(alpha = 0.12f),
                                contentColor = PrimaryIndigo
                            ) else ButtonDefaults.outlinedButtonColors()
                        ) {
                            Icon(
                                if (isSpeaking) Icons.Default.VolumeUp else Icons.Default.VolumeUp,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isSpeaking) PrimaryIndigo else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isSpeaking) "Stop" else "Listen")
                        }
                    }
                }
            }
        }
    }

    // 131-Language Selector Dialog
    if (pickingSource != null) {
        val isSourcePicker = pickingSource == true
        var searchQuery by remember { mutableStateOf("") }

        val filteredLanguages = remember(searchQuery) {
            if (searchQuery.isBlank()) {
                TranslationLanguages.allLanguages
            } else {
                TranslationLanguages.allLanguages.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                            it.nativeName.contains(searchQuery, ignoreCase = true) ||
                            it.code.contains(searchQuery, ignoreCase = true)
                }
            }
        }

        AlertDialog(
            onDismissRequest = { pickingSource = null },
            title = {
                Text(
                    text = if (isSourcePicker) "Select Source Language" else "Select Target Language",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search 131 languages...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(filteredLanguages, key = { it.code }) { lang ->
                            val isSelected = if (isSourcePicker) sourceLang.code == lang.code else targetLang.code == lang.code
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable {
                                        if (isSourcePicker) {
                                            sourceLang = lang
                                            if (targetLang.code == lang.code) {
                                                targetLang = if (lang.code == "bn") TranslationLanguages.ENGLISH else TranslationLanguages.BANGLA
                                            }
                                        } else {
                                            targetLang = lang
                                            if (sourceLang.code == lang.code) {
                                                sourceLang = if (lang.code == "bn") TranslationLanguages.ENGLISH else TranslationLanguages.BANGLA
                                            }
                                        }
                                        pickingSource = null
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) PrimaryContainerLight else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(lang.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(lang.nativeName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = PrimaryIndigo, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { pickingSource = null }) {
                    Text("Close")
                }
            }
        )
    }
}

private fun resolveQuizQuestions(
    selectedClass: String,
    selectedCurriculum: String,
    targetSubjectId: String,
    chapterId: String = "",
    topicId: String? = null,
    difficulty: QuestionDifficulty? = null,
    maxCount: Int = 10,
    selectedChapter: SyllabusChapter? = null,
    selectedGroup: AcademicGroup = AcademicGroup.SCIENCE,
    selectedPaper: SubjectPaper = SubjectPaper.NONE
): List<QuizQuestion> {
    return CurriculumQuizRepository.resolveQuizQuestions(
        selectedClass = selectedClass,
        selectedCurriculum = selectedCurriculum,
        targetSubjectId = targetSubjectId,
        chapterId = chapterId,
        topicId = topicId,
        difficulty = difficulty,
        maxCount = maxCount,
        selectedChapter = selectedChapter,
        selectedGroup = selectedGroup,
        selectedPaper = selectedPaper
    )
}

@Composable
fun QuizScreen(
    onNavigateBack: () -> Unit,
    initialClass: String? = null,
    initialCurriculum: String? = null,
    initialSubjectId: String? = null,
    initialChapterId: String? = null,
    initialTopicId: String? = null,
    onNavigateToMistakes: () -> Unit = {},
    initialGroup: AcademicGroup? = null,
    initialPaper: SubjectPaper? = null
) {
    val activeSyllabus = SyllabusRepository.getSyllabus()

    val classes = listOf("Class 6", "Class 7", "Class 8", "Class 9", "Class 10 (SSC)", "Class 11", "Class 12 (HSC)")
    var selectedClass by remember {
        mutableStateOf(
            classes.find { initialClass != null && (it.equals(initialClass, ignoreCase = true) || initialClass.contains(it, ignoreCase = true)) }
                ?: classes.find { it.equals(activeSyllabus.educationLevel, ignoreCase = true) || activeSyllabus.educationLevel.contains(it, ignoreCase = true) }
                ?: classes[4]
        )
    }

    val allCurricula = listOf(
        "NCTB (Bangla Version)",
        "NCTB (English Version)",
        "English Medium (Cambridge / Edexcel)",
        "Admission (Engineering / Medical / Varsity)"
    )

    val availableCurricula = remember(selectedClass) {
        if (AcademicCatalog.isHigherSecondaryClass(selectedClass) || selectedClass.contains("12") || selectedClass.contains("Admission")) {
            allCurricula
        } else {
            allCurricula.filter { !it.contains("Admission") }
        }
    }

    var selectedCurriculum by remember {
        val initialMatch = availableCurricula.find {
            initialCurriculum != null &&
            (it.equals(initialCurriculum, ignoreCase = true) || it.contains(initialCurriculum, ignoreCase = true) || initialCurriculum.contains(it, ignoreCase = true))
        } ?: availableCurricula.find { it.equals(activeSyllabus.curriculum, ignoreCase = true) || it.contains(activeSyllabus.curriculum, ignoreCase = true) }
          ?: availableCurricula[0]
        mutableStateOf(initialMatch)
    }

    val isClass6to8 = AcademicCatalog.isJuniorClass(selectedClass)

    var selectedGroup by remember {
        mutableStateOf(
            if (isClass6to8) AcademicGroup.GENERAL_JUNIOR
            else initialGroup ?: (if (activeSyllabus.group == AcademicGroup.GENERAL_JUNIOR) AcademicGroup.SCIENCE else activeSyllabus.group)
        )
    }

    val availableSubjects = remember(selectedClass, selectedCurriculum, selectedGroup) {
        AcademicCatalog.getQuizSubjects(selectedClass, selectedCurriculum, selectedGroup)
    }

    var selectedSubject by remember {
        mutableStateOf(
            availableSubjects.find { it.id.equals(initialSubjectId, ignoreCase = true) }
                ?: availableSubjects.firstOrNull()
        )
    }

    val supportedPapers = selectedSubject?.availablePapers ?: listOf(SubjectPaper.NONE)
    val hasPapers = supportedPapers.size > 1 && supportedPapers.any { it != SubjectPaper.NONE }

    var selectedPaper by remember {
        mutableStateOf(
            initialPaper?.takeIf { it in supportedPapers }
                ?: supportedPapers.firstOrNull()
                ?: SubjectPaper.NONE
        )
    }

    val availableChapters = remember(selectedClass, selectedCurriculum, selectedGroup, selectedSubject, selectedPaper) {
        if (selectedSubject == null) emptyList()
        else AcademicContentCatalog.getChapters(
            educationLevel = selectedClass,
            curriculum = selectedCurriculum,
            group = selectedGroup,
            subjectId = selectedSubject!!.id,
            paper = selectedPaper
        )
    }

    var selectedChapter by remember {
        mutableStateOf(
            availableChapters.find { it.id == initialChapterId }
                ?: availableChapters.firstOrNull()
        )
    }

    val availableTopics = remember(selectedChapter) {
        selectedChapter?.topics ?: emptyList()
    }

    var selectedTopic by remember {
        mutableStateOf(
            availableTopics.find { it.id == initialTopicId }
        )
    }

    var selectedDifficulty by remember { mutableStateOf<QuestionDifficulty?>(null) }
    var selectedQuizSize by remember { mutableIntStateOf(10) }

    var quizStarted by remember { mutableStateOf(false) }
    var activeQuestions by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }

    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var isSubmitted by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    var quizFinished by remember { mutableStateOf(false) }

    // Synchronize subject selection if available subjects changed
    LaunchedEffect(availableSubjects) {
        if (selectedSubject == null || availableSubjects.none { it.id == selectedSubject?.id }) {
            selectedSubject = availableSubjects.find { it.id.equals(initialSubjectId, ignoreCase = true) } ?: availableSubjects.firstOrNull()
            val papers = selectedSubject?.availablePapers ?: listOf(SubjectPaper.NONE)
            selectedPaper = papers.firstOrNull() ?: SubjectPaper.NONE
        }
    }

    // Synchronize chapter selection if available chapters changed
    LaunchedEffect(availableChapters) {
        if (selectedChapter == null || availableChapters.none { it.id == selectedChapter?.id }) {
            selectedChapter = availableChapters.find { it.id == initialChapterId } ?: availableChapters.firstOrNull()
            selectedTopic = null
        }
    }

    // Downstream state reset handlers as mandated in Section H
    fun handleClassSelect(cls: String) {
        if (selectedClass == cls) return
        selectedClass = cls
        val isJunior = AcademicCatalog.isJuniorClass(cls)
        val validCurricula = if (AcademicCatalog.isHigherSecondaryClass(cls) || cls.contains("12") || cls.contains("Admission")) {
            allCurricula
        } else {
            allCurricula.filter { !it.contains("Admission") }
        }
        if (selectedCurriculum !in validCurricula) {
            selectedCurriculum = validCurricula.first()
        }
        selectedGroup = if (isJunior) AcademicGroup.GENERAL_JUNIOR
        else if (selectedGroup == AcademicGroup.GENERAL_JUNIOR) AcademicGroup.SCIENCE
        else selectedGroup

        val newSubjects = AcademicCatalog.getQuizSubjects(cls, selectedCurriculum, selectedGroup)
        selectedSubject = newSubjects.firstOrNull()
        selectedPaper = selectedSubject?.availablePapers?.firstOrNull() ?: SubjectPaper.NONE
        val newChapters = if (selectedSubject != null) {
            AcademicContentCatalog.getChapters(cls, selectedCurriculum, selectedGroup, selectedSubject!!.id, selectedPaper)
        } else emptyList()
        selectedChapter = newChapters.firstOrNull()
        selectedTopic = null
    }

    fun handleCurriculumSelect(cur: String) {
        if (selectedCurriculum == cur) return
        selectedCurriculum = cur
        val newSubjects = AcademicCatalog.getQuizSubjects(selectedClass, cur, selectedGroup)
        selectedSubject = newSubjects.find { it.id == selectedSubject?.id } ?: newSubjects.firstOrNull()
        val papers = selectedSubject?.availablePapers ?: listOf(SubjectPaper.NONE)
        selectedPaper = if (selectedPaper in papers) selectedPaper else papers.firstOrNull() ?: SubjectPaper.NONE
        val newChapters = if (selectedSubject != null) {
            AcademicContentCatalog.getChapters(selectedClass, cur, selectedGroup, selectedSubject!!.id, selectedPaper)
        } else emptyList()
        selectedChapter = newChapters.firstOrNull()
        selectedTopic = null
    }

    fun handleGroupSelect(grp: AcademicGroup) {
        if (selectedGroup == grp) return
        selectedGroup = grp
        val newSubjects = AcademicCatalog.getQuizSubjects(selectedClass, selectedCurriculum, grp)
        selectedSubject = newSubjects.find { it.id == selectedSubject?.id } ?: newSubjects.firstOrNull()
        val papers = selectedSubject?.availablePapers ?: listOf(SubjectPaper.NONE)
        selectedPaper = if (selectedPaper in papers) selectedPaper else papers.firstOrNull() ?: SubjectPaper.NONE
        val newChapters = if (selectedSubject != null) {
            AcademicContentCatalog.getChapters(selectedClass, selectedCurriculum, grp, selectedSubject!!.id, selectedPaper)
        } else emptyList()
        selectedChapter = newChapters.firstOrNull()
        selectedTopic = null
    }

    fun handleSubjectSelect(subj: AcademicQuizSubject) {
        if (selectedSubject?.id == subj.id) return
        selectedSubject = subj
        val papers = subj.availablePapers
        selectedPaper = papers.firstOrNull() ?: SubjectPaper.NONE
        val newChapters = AcademicContentCatalog.getChapters(selectedClass, selectedCurriculum, selectedGroup, subj.id, selectedPaper)
        selectedChapter = newChapters.firstOrNull()
        selectedTopic = null
    }

    fun handlePaperSelect(paper: SubjectPaper) {
        if (selectedPaper == paper) return
        selectedPaper = paper
        val newChapters = if (selectedSubject != null) {
            AcademicContentCatalog.getChapters(selectedClass, selectedCurriculum, selectedGroup, selectedSubject!!.id, paper)
        } else emptyList()
        selectedChapter = newChapters.firstOrNull()
        selectedTopic = null
    }

    fun handleChapterSelect(ch: SyllabusChapter) {
        if (selectedChapter?.id == ch.id) return
        selectedChapter = ch
        selectedTopic = null
    }

    val targetSubjectId = selectedSubject?.id?.lowercase()?.trim() ?: "bangla"
    val chId = selectedChapter?.id?.trim() ?: ""
    val tId = selectedTopic?.id?.trim()
    val availableQuestionsForSetup = remember(
        selectedClass, selectedCurriculum, selectedGroup, selectedSubject, selectedPaper, selectedChapter, selectedTopic, selectedDifficulty, selectedQuizSize
    ) {
        if (selectedSubject == null) emptyList()
        else CurriculumQuizRepository.resolveQuizQuestions(
            selectedClass = selectedClass,
            selectedCurriculum = selectedCurriculum,
            targetSubjectId = targetSubjectId,
            chapterId = chId,
            topicId = tId,
            difficulty = selectedDifficulty,
            maxCount = selectedQuizSize,
            selectedChapter = selectedChapter,
            selectedGroup = selectedGroup,
            selectedPaper = selectedPaper
        )
    }

    if (!quizStarted) {
        Scaffold(
            topBar = {
                GenieTopBar(
                    title = "Quiz Setup",
                    subtitle = "$selectedCurriculum • $selectedClass",
                    onBackClick = onNavigateBack
                )
            },
            bottomBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    shadowElevation = 8.dp,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Button(
                            onClick = {
                                activeQuestions = availableQuestionsForSetup
                                currentQuestionIndex = 0
                                selectedOption = null
                                isSubmitted = false
                                score = 0
                                quizFinished = false
                                quizStarted = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("quiz_setup_next_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                        ) {
                            Text(
                                text = if (availableQuestionsForSetup.isNotEmpty()) "Next (${availableQuestionsForSetup.size} Questions)" else "Next",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // 1. SELECT CLASS
                Text("Select Class", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    classes.forEach { cls ->
                        val isSelected = selectedClass == cls
                        Surface(
                            modifier = Modifier.clickable { handleClassSelect(cls) },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) PrimaryIndigo else MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) PrimaryIndigo else Color.Transparent)
                        ) {
                            Text(
                                text = cls, 
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), 
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                // 2. SELECT CURRICULUM
                Text("Select Curriculum", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))
                availableCurricula.forEach { cur ->
                    val isSelected = selectedCurriculum == cur
                    val curId = CurriculumQuizRepository.mapToCurriculumId(cur)
                    val badge = when (curId) {
                        CurriculumQuizRepository.CURRICULUM_NCTB_BN -> "বাংলা সংস্করণ"
                        CurriculumQuizRepository.CURRICULUM_NCTB_EN -> "English Version"
                        CurriculumQuizRepository.CURRICULUM_CAMBRIDGE_EDEXCEL -> "Cambridge / Edexcel"
                        CurriculumQuizRepository.CURRICULUM_ADMISSION -> "BUET / DU / Medical"
                        else -> ""
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { handleCurriculumSelect(cur) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) PrimaryIndigo.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            2.dp,
                            if (isSelected) PrimaryIndigo else Color.Transparent
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = cur,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp
                                )
                                if (badge.isNotEmpty()) {
                                    Text(
                                        text = badge,
                                        fontSize = 11.sp,
                                        color = if (isSelected) PrimaryIndigo else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (isSelected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = PrimaryIndigo)
                            }
                        }
                    }
                }
                
                // 3. SELECT ACADEMIC GROUP (Class 9-12 only)
                if (!isClass6to8) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Select Academic Group", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            AcademicGroup.SCIENCE,
                            AcademicGroup.BUSINESS_STUDIES,
                            AcademicGroup.HUMANITIES
                        ).forEach { grp ->
                            val isGrpSelected = selectedGroup == grp
                            Surface(
                                modifier = Modifier.clickable { handleGroupSelect(grp) },
                                shape = RoundedCornerShape(16.dp),
                                color = if (isGrpSelected) PrimaryIndigo else MaterialTheme.colorScheme.surfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isGrpSelected) PrimaryIndigo else Color.Transparent)
                            ) {
                                Text(
                                    text = grp.titleEn,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    fontWeight = FontWeight.Bold,
                                    color = if (isGrpSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 4. SELECT SUBJECT
                Text("Select Subject", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))
                availableSubjects.forEach { subj ->
                    val isSelected = selectedSubject?.id == subj.id
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { handleSubjectSelect(subj) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isSelected) PrimaryIndigo.copy(alpha=0.15f) else MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(2.dp, if (isSelected) PrimaryIndigo else Color.Transparent)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(subj.icon, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(subj.nameEn, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                if (subj.nameBn.isNotBlank() && subj.nameBn != subj.nameEn) {
                                    Text(subj.nameBn, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            if (isSelected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = PrimaryIndigo)
                            }
                        }
                    }
                }
                
                // 5. SELECT PAPER (when subject has multiple papers)
                if (hasPapers) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Select Paper", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        supportedPapers.filter { it != SubjectPaper.NONE }.forEach { paper ->
                            val isPaperSelected = selectedPaper == paper
                            val paperLabel = when (paper) {
                                SubjectPaper.PAPER_1 -> "1st Paper (১ম পত্র)"
                                SubjectPaper.PAPER_2 -> "2nd Paper (২য় পত্র)"
                                else -> paper.titleEn
                            }
                            Surface(
                                modifier = Modifier.weight(1f).clickable { handlePaperSelect(paper) },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isPaperSelected) PrimaryIndigo else MaterialTheme.colorScheme.surfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isPaperSelected) PrimaryIndigo else Color.Transparent)
                            ) {
                                Text(
                                    text = paperLabel,
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                    color = if (isPaperSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 6. SELECT CHAPTER
                if (availableChapters.isNotEmpty()) {
                    Text("Select Chapter", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    availableChapters.forEach { chapter ->
                        val isChapterSelected = selectedChapter?.id == chapter.id
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { handleChapterSelect(chapter) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isChapterSelected) PrimaryIndigo.copy(alpha=0.15f) else MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(2.dp, if (isChapterSelected) PrimaryIndigo else Color.Transparent)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(chapter.title, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                    if (chapter.titleBn.isNotBlank() && chapter.titleBn != chapter.title) {
                                        Text(chapter.titleBn, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                if (isChapterSelected) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = PrimaryIndigo)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                // 7. SELECT TOPIC (Optional)
                if (availableTopics.isNotEmpty()) {
                    Text("Select Topic (Optional)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // "All Topics in Chapter" option
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { selectedTopic = null },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = if (selectedTopic == null) PrimaryIndigo.copy(alpha=0.15f) else MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(2.dp, if (selectedTopic == null) PrimaryIndigo else Color.Transparent)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("All Topics in Chapter", fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                            if (selectedTopic == null) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = PrimaryIndigo)
                            }
                        }
                    }
                    
                    availableTopics.forEach { topic ->
                        val isTopicSelected = selectedTopic?.id == topic.id
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { selectedTopic = topic },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isTopicSelected) PrimaryIndigo.copy(alpha=0.15f) else MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(2.dp, if (isTopicSelected) PrimaryIndigo else Color.Transparent)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(topic.title, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                    if (topic.titleBn.isNotBlank() && topic.titleBn != topic.title) {
                                        Text(topic.titleBn, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                if (isTopicSelected) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = PrimaryIndigo)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                // 8. SELECT DIFFICULTY
                Text("Select Difficulty", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    val difficulties = listOf(null, QuestionDifficulty.EASY, QuestionDifficulty.MEDIUM, QuestionDifficulty.HARD)
                    val diffLabels = listOf("Mix", "Easy", "Med", "Hard")
                    
                    difficulties.forEachIndexed { index, diff ->
                        val isSelected = selectedDifficulty == diff
                        Surface(
                            modifier = Modifier.weight(1f).padding(horizontal = 4.dp).clickable { selectedDifficulty = diff },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) PrimaryIndigo else MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) PrimaryIndigo else Color.Transparent)
                        ) {
                            Text(
                                text = diffLabels[index], 
                                modifier = Modifier.padding(vertical = 12.dp), 
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                // 9. SELECT QUIZ SIZE (5, 10, 15, 20)
                Text("Select Quiz Size", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val supportedSizes = listOf(5, 10, 15, 20)
                    supportedSizes.forEach { size ->
                        val isSelected = selectedQuizSize == size
                        Surface(
                            modifier = Modifier.weight(1f).clickable { selectedQuizSize = size },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) PrimaryIndigo else MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) PrimaryIndigo else Color.Transparent)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "$size",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Questions",
                                    fontSize = 10.sp,
                                    color = if (isSelected) Color.White.copy(alpha=0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                if (availableQuestionsForSetup.isEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⚠️", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "No questions currently available for ${selectedSubject?.nameEn ?: "this subject"}${selectedChapter?.let { " - " + it.title } ?: ""}. Please select another chapter or reset difficulty.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                } else if (availableQuestionsForSetup.size < selectedQuizSize) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = PrimaryIndigo.copy(alpha = 0.12f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ℹ️", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Only ${availableQuestionsForSetup.size} questions are available for this selection (requested $selectedQuizSize).",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        return
    }

    if (activeQuestions.isEmpty()) {
        Scaffold(
            topBar = {
                GenieTopBar(
                    title = "Quiz",
                    subtitle = "$selectedCurriculum • ${selectedSubject?.name ?: "Assessment"}",
                    onBackClick = { quizStarted = false }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("📋", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No Questions Available",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "There are no quiz questions currently available for ${selectedSubject?.name ?: "the selected subject"}${selectedChapter?.let { " • " + it.title } ?: ""}.\n\nPlease return to Quiz Setup and select a different chapter or difficulty.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { quizStarted = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                ) {
                    Text("Return to Quiz Setup", fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    val currentQ = activeQuestions.getOrNull(currentQuestionIndex) ?: return

    Scaffold(
        topBar = {
            GenieTopBar(
                title = if (quizFinished) "Quiz Results" else "Question ${currentQuestionIndex + 1}/${activeQuestions.size}",
                subtitle = if (quizFinished) "Comprehensive Performance Analysis" else "$selectedCurriculum • ${selectedSubject?.name}",
                onBackClick = { if (quizFinished) onNavigateBack() else quizStarted = false }
            )
        }
    ) { padding ->
        if (quizFinished) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(PrimaryIndigo.copy(alpha = 0.15f))
                        .border(2.dp, PrimaryIndigo, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$score / ${activeQuestions.size}", fontWeight = FontWeight.Black, fontSize = 28.sp, color = PrimaryIndigo)
                        Text("${(score.toFloat() / activeQuestions.size * 100).toInt()}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PrimaryIndigo)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text("Excellent Effort, Alex! 🎉", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Your concept understanding has been logged to your Study Analytics.", color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("Performance Breakdown", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Curriculum:")
                            Text(selectedCurriculum, fontWeight = FontWeight.SemiBold, color = PrimaryIndigo)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Correct Answers:")
                            Text("$score", fontWeight = FontWeight.Bold, color = SuccessEmerald)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Wrong Answers:")
                            Text("${activeQuestions.size - score}", fontWeight = FontWeight.Bold, color = ErrorRose)
                        }
                        if (activeQuestions.size - score > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "📝 Saved to Mistake Notebook for review",
                                style = MaterialTheme.typography.bodySmall,
                                color = ErrorRose,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Earned XP:")
                            Text("+${score * 25} XP", fontWeight = FontWeight.Bold, color = GoldXp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        quizStarted = false
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                ) {
                    Text("Try Another Quiz")
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Return to Dashboard")
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // SCROLLABLE AREA
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { (currentQuestionIndex + 1).toFloat() / activeQuestions.size },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = PrimaryIndigo
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (activeQuestions.size < selectedQuizSize) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PrimaryIndigo.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("ℹ️", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Only ${activeQuestions.size} questions are available for this selection.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PrimaryIndigo,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Text(
                        text = currentQ.questionText,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    currentQ.options.forEachIndexed { index, option ->
                        val isSelected = selectedOption == index
                        val isCorrect = index == currentQ.correctIndex

                        val containerColor = when {
                            isSubmitted && isCorrect -> SuccessEmerald.copy(alpha = 0.15f)
                            isSubmitted && isSelected && !isCorrect -> ErrorRose.copy(alpha = 0.15f)
                            isSelected -> Color.Transparent
                            else -> MaterialTheme.colorScheme.surface
                        }

                        val borderColor = when {
                            isSubmitted && isCorrect -> SuccessEmerald
                            isSubmitted && isSelected && !isCorrect -> ErrorRose
                            isSelected -> PrimaryIndigo
                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable(enabled = !isSubmitted) { selectedOption = index },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = containerColor),
                            border = androidx.compose.foundation.BorderStroke(2.dp, borderColor)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) PrimaryIndigo else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (isSelected) {
                                            Icon(Icons.Default.Check, contentDescription = "Selected", tint = Color.White, modifier = Modifier.size(18.dp))
                                        } else {
                                            Text(
                                                text = ('A' + index).toString(),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(option, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    if (isSubmitted) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("💡 Explanation:", fontWeight = FontWeight.Bold, color = PrimaryIndigo)
                                Text(currentQ.explanation, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                
                // FIXED BOTTOM ACTION BAR
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .navigationBarsPadding()
                        .imePadding()
                ) {
                    Button(
                        onClick = {
                            if (!isSubmitted) {
                                isSubmitted = true
                                if (selectedOption == currentQ.correctIndex) {
                                    score++
                                } else {
                                    // Save mistake to Mistake Notebook
                                    val userChoice = if (selectedOption != null && selectedOption!! in currentQ.options.indices) {
                                        currentQ.options[selectedOption!!]
                                    } else {
                                        "Option ${((selectedOption ?: 0) + 'A'.code).toChar()}"
                                    }
                                    val correctChoice = if (currentQ.correctIndex in currentQ.options.indices) {
                                        currentQ.options[currentQ.correctIndex]
                                    } else {
                                        "Option ${('A'.code + currentQ.correctIndex).toChar()}"
                                    }
                                    val subjectName = selectedSubject?.name ?: currentQ.subjectId.replaceFirstChar { it.uppercase() }
                                    val chapterName = selectedChapter?.title ?: currentQ.chapterId.ifEmpty { "General" }
                                    val topicName = selectedTopic?.title ?: currentQ.topic.ifEmpty { chapterName }

                                    MockEducationalRepository.addMistake(
                                        questionText = currentQ.questionText,
                                        className = selectedClass,
                                        subject = subjectName,
                                        chapter = chapterName,
                                        topic = topicName,
                                        wrongAnswer = userChoice,
                                        correctAnswer = correctChoice,
                                        explanation = currentQ.explanation,
                                        category = when (currentQ.difficulty) {
                                            QuestionDifficulty.HARD -> "Concept"
                                            QuestionDifficulty.MEDIUM -> "Calculation"
                                            else -> "Formula"
                                        }
                                    )
                                }
                            } else {
                                if (currentQuestionIndex < activeQuestions.size - 1) {
                                    currentQuestionIndex++
                                    selectedOption = null
                                    isSubmitted = false
                                } else {
                                    quizFinished = true
                                }
                            }
                        },
                        enabled = selectedOption != null,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                    ) {
                        Text(
                            if (!isSubmitted) "Submit Answer" else if (currentQuestionIndex < activeQuestions.size - 1) "Next Question →" else "Finish & View Results",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
