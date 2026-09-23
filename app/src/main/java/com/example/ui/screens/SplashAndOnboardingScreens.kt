package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AcademicCatalog
import com.example.data.AcademicCatalogSubject
import com.example.data.SyllabusRepository
import com.example.model.AcademicGroup
import com.example.model.SubjectPaper
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        delay(1800)
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0D1B2A), Color(0xFF1B263B), Color(0xFF3D5AFE))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🧞‍♂️",
                    fontSize = (48 * scale).sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "REVE GENIE",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 2.sp
            )

            Text(
                text = "AI TEACHER",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00E5FF),
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "\"Your Personal AI Teacher\" • \"তোমার সার্বক্ষণিক এআই শিক্ষক\"",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            LinearProgressIndicator(
                modifier = Modifier
                    .width(140.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = Color(0xFF00E5FF),
                trackColor = Color.White.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
fun OnboardingScreen(onFinishOnboarding: () -> Unit) {
    var step by remember { mutableIntStateOf(1) }
    val totalSteps = 8

    var selectedLang by remember { mutableStateOf("বাংলা") }
    var selectedLevel by remember { mutableStateOf("Class 10 (SSC)") }
    var selectedCurriculum by remember { mutableStateOf("NCTB (Bangla Version)") }
    var selectedGroup by remember { mutableStateOf(AcademicGroup.SCIENCE) }
    val selectedSubjects = remember { mutableStateListOf("math", "physics", "chemistry", "english") }
    val selectedGoals = remember { mutableStateListOf("Exam Preparation", "Concept Learning") }
    var selectedDailyGoal by remember { mutableIntStateOf(30) }

    LaunchedEffect(selectedLevel) {
        if (AcademicCatalog.isJuniorClass(selectedLevel)) {
            selectedGroup = AcademicGroup.GENERAL_JUNIOR
        } else if (selectedGroup == AcademicGroup.GENERAL_JUNIOR) {
            selectedGroup = AcademicGroup.SCIENCE
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Step $step of $totalSteps",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onFinishOnboarding) {
                        Text("Skip")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { step.toFloat() / totalSteps },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        bottomBar = {
            Surface(
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (step > 1) {
                        OutlinedButton(
                            onClick = { step-- },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Back")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    val canContinue = if (step == 5) selectedSubjects.isNotEmpty() else true

                    Button(
                        onClick = {
                            if (step < totalSteps) {
                                step++
                            } else {
                                SyllabusRepository.updateOnboardingConfiguration(
                                    language = selectedLang,
                                    educationLevel = selectedLevel,
                                    curriculum = selectedCurriculum,
                                    selectedSubjects = selectedSubjects.toList(),
                                    dailyGoalMinutes = selectedDailyGoal,
                                    group = selectedGroup
                                )
                                onFinishOnboarding()
                            }
                        },
                        enabled = canContinue,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryIndigo,
                            disabledContainerColor = PrimaryIndigo.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier.testTag("onboarding_next_button")
                    ) {
                        Text(if (step == totalSteps) "Start Learning 🚀" else "Continue")
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            when (step) {
                1 -> OnboardingStep1Welcome()
                2 -> OnboardingStep2Language(selectedLang) { selectedLang = it }
                3 -> OnboardingStep3EducationLevel(selectedLevel) { selectedLevel = it }
                4 -> OnboardingStep4Curriculum(selectedCurriculum) { selectedCurriculum = it }
                5 -> OnboardingStep5Subjects(
                    selectedSubjects = selectedSubjects,
                    educationLevel = selectedLevel,
                    curriculum = selectedCurriculum,
                    selectedGroup = selectedGroup,
                    onGroupSelected = { selectedGroup = it },
                    language = selectedLang
                )
                6 -> OnboardingStep6Goals(selectedGoals)
                7 -> OnboardingStep7DailyGoal(selectedDailyGoal) { selectedDailyGoal = it }
                8 -> OnboardingStep8Complete(selectedLang, selectedLevel, selectedDailyGoal)
            }
        }
    }
}

@Composable
fun OnboardingStep1Welcome() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(130.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(PrimaryContainerLight),
            contentAlignment = Alignment.Center
        ) {
            Text("🎓", fontSize = 64.sp)
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Meet Your AI Teacher",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Instant 24/7 AI tutor customized for Bangladesh students. Master NCTB, SSC, HSC and University Admission exams with intelligent problem solving.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun OnboardingStep2Language(current: String, onSelect: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text(
            text = "Choose Your Language / ভাষা নির্বাচন করো",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "You can ask questions and study in either language anytime.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        listOf(
            "বাংলা" to "সহজ বাংলায় বিজ্ঞান, গণিত ও সব বিষয়ের সমাধান",
            "English" to "Comprehensive explanations and grammar coach in English"
        ).forEach { (lang, desc) ->
            val isSelected = current == lang
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { onSelect(lang) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) PrimaryIndigo.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) PrimaryIndigo else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (lang == "বাংলা") "🇧🇩" else "🌐", fontSize = 32.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = lang,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (isSelected) {
                        Surface(
                            shape = CircleShape,
                            color = PrimaryIndigo.copy(alpha = 0.2f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = PrimaryIndigo,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingStep3EducationLevel(current: String, onSelect: (String) -> Unit) {
    val levels = listOf(
        "Class 6", "Class 7", "Class 8", "Class 9", "Class 10 (SSC)",
        "Class 11", "Class 12 (HSC)", "University", "Admission Candidate"
    )
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("What is your education level?", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        levels.forEach { level ->
            val isSelected = current == level
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onSelect(level) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) PrimaryIndigo.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) PrimaryIndigo else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = level,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isSelected) {
                        Surface(
                            shape = CircleShape,
                            color = PrimaryIndigo.copy(alpha = 0.2f),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = PrimaryIndigo,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingStep4Curriculum(current: String, onSelect: (String) -> Unit) {
    val curriculums = listOf("NCTB (Bangla Version)", "NCTB (English Version)", "English Medium (Cambridge / Edexcel)", "Admission (Engineering / Medical / Varsity)")
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("Select Curriculum", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        curriculums.forEach { cur ->
            val isSelected = current == cur
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { onSelect(cur) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) PrimaryIndigo.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) PrimaryIndigo else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                )
            ) {
                Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = cur,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    if (isSelected) {
                        Surface(
                            shape = CircleShape,
                            color = PrimaryIndigo.copy(alpha = 0.2f),
                            modifier = Modifier.size(30.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = PrimaryIndigo,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingStep5Subjects(
    selectedSubjects: MutableList<String>,
    educationLevel: String = "Class 10 (SSC)",
    curriculum: String = "NCTB (Bangla Version)",
    selectedGroup: AcademicGroup = AcademicGroup.SCIENCE,
    onGroupSelected: (AcademicGroup) -> Unit = {},
    language: String = "বাংলা"
) {
    val availableGroups = remember(educationLevel, curriculum) {
        AcademicCatalog.getAvailableGroups(educationLevel, curriculum)
    }

    val availableSubjects = remember(educationLevel, curriculum, selectedGroup) {
        AcademicCatalog.getSubjects(educationLevel, curriculum, selectedGroup)
    }

    // Ensure state consistency: prune invalid selections and retain valid ones
    LaunchedEffect(availableSubjects) {
        val validIds = AcademicCatalog.filterValidSelectedSubjects(selectedSubjects, availableSubjects)
        selectedSubjects.clear()
        if (validIds.isNotEmpty()) {
            selectedSubjects.addAll(validIds)
        } else {
            val coreList = availableSubjects.filter { it.isCore }
            val defaults = if (coreList.isNotEmpty()) coreList.take(4) else availableSubjects.take(3)
            defaults.forEach { selectedSubjects.add(it.id) }
        }
    }

    val isJuniorSchool = AcademicCatalog.isJuniorClass(educationLevel)
    val isBanglaLang = curriculum.contains("Bangla", ignoreCase = true) || language == "বাংলা"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Choose Your Subjects",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (isJuniorSchool) {
                "Junior National Curriculum (Class 6–8) • $educationLevel"
            } else {
                "Select your Academic Group and priority subjects for $educationLevel."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Group Selector Tabs (for Class 9-12 and Admission)
        if (!isJuniorSchool && availableGroups.size > 1) {
            Text(
                text = "Academic Group",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableGroups.forEach { grp ->
                    val isGroupActive = selectedGroup == grp
                    val groupTitle = when (grp) {
                        AcademicGroup.SCIENCE -> if (isBanglaLang) "বিজ্ঞান" else "Science"
                        AcademicGroup.BUSINESS_STUDIES -> if (isBanglaLang) "ব্যবসায় শিক্ষা" else "Business"
                        AcademicGroup.HUMANITIES -> if (isBanglaLang) "মানবিক" else "Humanities"
                        AcademicGroup.GENERAL_JUNIOR -> if (isBanglaLang) "সাধারণ" else "General"
                    }
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onGroupSelected(grp) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isGroupActive) PrimaryIndigo else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isGroupActive) 1.5.dp else 1.dp,
                            color = if (isGroupActive) PrimaryIndigo else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = groupTitle,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isGroupActive) FontWeight.Bold else FontWeight.Medium,
                                color = if (isGroupActive) Color.White else MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        } else if (isJuniorSchool) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = PrimaryIndigo.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryIndigo.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🏫", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBanglaLang) "সাধারণ পাঠ্যক্রম (General Stream)" else "General Junior Stream",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryIndigo
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Available Subjects List
        availableSubjects.forEach { subject ->
            val isSelected = selectedSubjects.contains(subject.id)
            val displayName = subject.getDisplayName(isBangla = isBanglaLang)
            val secondaryName = if (isBanglaLang && subject.nameEn != displayName) subject.nameEn else ""

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clickable {
                        if (isSelected) {
                            selectedSubjects.remove(subject.id)
                        } else {
                            selectedSubjects.add(subject.id)
                        }
                    },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) PrimaryIndigo.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) PrimaryIndigo else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(subject.icon, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (subject.paper != SubjectPaper.NONE) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                ) {
                                    Text(
                                        text = if (isBanglaLang) subject.paper.titleBn else subject.paper.titleEn,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        if (secondaryName.isNotBlank()) {
                            Text(
                                text = secondaryName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    if (isSelected) {
                        Surface(
                            shape = CircleShape,
                            color = PrimaryIndigo.copy(alpha = 0.2f),
                            modifier = Modifier.size(30.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = PrimaryIndigo,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .border(
                                    width = 1.5.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun OnboardingStep6Goals(selectedGoals: MutableList<String>) {
    val goals = listOf(
        "Exam Preparation (Board / Finals)",
        "Daily Homework & Equation Solving",
        "Deep Concept Clarity",
        "English Fluency & Speaking",
        "Weak Subject Recovery",
        "Routine Study Discipline"
    )
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("Your Learning Goals", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        goals.forEach { goal ->
            val isSelected = selectedGoals.contains(goal)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable {
                        if (isSelected) selectedGoals.remove(goal) else selectedGoals.add(goal)
                    },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) PrimaryIndigo.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) PrimaryIndigo else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                )
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = goal,
                        modifier = Modifier.weight(1f),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isSelected) {
                        Surface(
                            shape = CircleShape,
                            color = PrimaryIndigo.copy(alpha = 0.2f),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = PrimaryIndigo,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .border(
                                    width = 1.5.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingStep7DailyGoal(current: Int, onSelect: (Int) -> Unit) {
    val times = listOf(10, 20, 30, 45, 60, 90)
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("Daily Study Target", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Commit a small time daily to maintain your study streak.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(24.dp))
        times.forEach { mins ->
            val isSelected = current == mins
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { onSelect(mins) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) PrimaryIndigo.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) PrimaryIndigo else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                )
            ) {
                Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("⏱️", fontSize = 26.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "$mins Minutes / Day",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (mins == 30) "Recommended for SSC / HSC candidates" else "Consistent daily progress",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (isSelected) {
                        Surface(
                            shape = CircleShape,
                            color = PrimaryIndigo.copy(alpha = 0.2f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = PrimaryIndigo,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingStep8Complete(lang: String, level: String, dailyGoal: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(SuccessEmerald.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text("✨", fontSize = 52.sp)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Your AI Teacher is Ready!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Configured for $level with $dailyGoal mins daily target. Your personal AI Teacher is ready to guide you step-by-step.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
