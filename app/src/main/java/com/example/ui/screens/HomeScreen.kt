package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MockEducationalRepository
import com.example.data.SyllabusRepository
import com.example.model.SubjectType
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun HomeDashboard(
    onNavigateToChat: () -> Unit,
    onNavigateToScan: () -> Unit,
    onNavigateToVoice: () -> Unit,
    onNavigateToPdf: () -> Unit,
    onNavigateToQuiz: () -> Unit,
    onNavigateToTranslate: () -> Unit,
    onNavigateToSubject: (String) -> Unit,
    onNavigateToStudyPlan: () -> Unit,
    onNavigateToMistakes: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToSyllabus: () -> Unit = {}
) {
    val profile = MockEducationalRepository.defaultProfile
    val syllabus by SyllabusRepository.syllabusFlow.collectAsState()
    val syllabusSubjects = syllabus.subjects

    // Dynamic continue learning topic based on syllabus
    val firstSubject = syllabusSubjects.firstOrNull()
    val firstChapter = firstSubject?.chapters?.firstOrNull()
    val currentTopic = firstChapter?.topics?.firstOrNull()

    // Determine weak topics preview text from available subjects
    val mistakeTopicPreview = if (syllabusSubjects.any { it.id == "math" }) {
        if (syllabusSubjects.any { it.id == "physics" }) {
            "Algebra, Newton's Laws, Prepositions"
        } else {
            "Algebra, Prepositions & Vocabulary"
        }
    } else {
        "Grammar, Reading Comprehension, Prepositions"
    }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(PrimaryContainerLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👨‍🎓", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Good morning, ${profile.name.split(" ")[0]} 👋",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${syllabus.educationLevel} • ${syllabus.curriculum.split(" ")[0]}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = StreakFire.copy(alpha = 0.12f),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🔥", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "${profile.streakDays}d",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = StreakFire
                                )
                            }
                        }

                        IconButton(
                            onClick = onNavigateToNotifications,
                            modifier = Modifier.testTag("notification_icon_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    Badge(containerColor = PrimaryIndigo) { Text("3") }
                                }
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                            }
                        }
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
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Daily Goal Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("daily_goal_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⏱️ Today's Goal", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PrimaryContainerLight
                            ) {
                                Text(
                                    "✨ 1,240 XP",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryIndigo
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${profile.completedMinutesToday} of ${profile.dailyGoalMinutes} min completed",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = PrimaryIndigo
                        )
                        Text(
                            text = "Only 12 minutes left to keep your streak!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    ProgressRing(
                        progress = profile.completedMinutesToday.toFloat() / profile.dailyGoalMinutes,
                        modifier = Modifier.size(68.dp),
                        strokeWidth = 7.dp,
                        color = PrimaryIndigo
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // AI Study Tools Section — EXACT ORDER REQUIRED:
            // 1. Translator, 2. Ask AI, 3. Scan, 4. Voice, 5. Quiz, 6. PDF
            Text(
                text = "AI Study Tools",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionChip(icon = Icons.Default.Translate, label = "Translator", badge = "FREE", color = Color(0xFF00897B), onClick = onNavigateToTranslate)
                QuickActionChip(icon = Icons.Default.SmartToy, label = "Ask AI", color = PrimaryIndigo, onClick = onNavigateToChat)
                QuickActionChip(icon = Icons.Default.CameraAlt, label = "Scan", color = Color(0xFF0891B2), onClick = onNavigateToScan)
                QuickActionChip(icon = Icons.Default.Mic, label = "Voice", color = Color(0xFF7C3AED), onClick = onNavigateToVoice)
                QuickActionChip(icon = Icons.Default.Quiz, label = "Quiz", color = SuccessEmerald, onClick = onNavigateToQuiz)
                QuickActionChip(icon = Icons.Default.PictureAsPdf, label = "PDF", color = Color(0xFFD97706), onClick = onNavigateToPdf)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Continue Learning Banner with Syllabus Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Continue Learning",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onNavigateToSyllabus) {
                    Text("View Full Syllabus")
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            // Continue Learning Card (Dynamic from Syllabus)
            if (firstSubject != null && currentTopic != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToSyllabus() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(firstSubject.colorHex).copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(firstSubject.icon, fontSize = 26.sp)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(firstSubject.name, style = MaterialTheme.typography.labelMedium, color = Color(firstSubject.colorHex), fontWeight = FontWeight.Bold)
                            Text("${currentTopic.title} (${currentTopic.titleBn})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { (firstSubject.progressPercent / 100f).coerceIn(0.1f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = Color(firstSubject.colorHex)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        FilledTonalButton(
                            onClick = onNavigateToSyllabus,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Resume")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // AI Recommendation Card (Dynamic from available subjects)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryIndigo.copy(alpha = 0.12f)),
                border = BorderStroke(1.dp, PrimaryIndigo.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("💡", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("AI Recommendation", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        val recText = if (firstSubject != null && currentTopic != null) {
                            "Practice ${currentTopic.title} for 10 minutes to strengthen core concepts in ${firstSubject.name}."
                        } else {
                            "Practice daily questions to boost your mastery."
                        }
                        Text(
                            recText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = onNavigateToQuiz,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Start", fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Core Subjects Section (Dynamic from Syllabus)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Core Subjects",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { onNavigateToSubject("all") }) {
                    Text("View All")
                }
            }

            syllabusSubjects.forEach { subj ->
                val mastery = when (subj.id) {
                    "math" -> 85
                    "physics" -> 68
                    "chemistry" -> 74
                    "english" -> 88
                    "biology" -> 78
                    "ict" -> 82
                    else -> 75
                }
                val totalTopics = subj.chapters.sumOf { it.topics.size }
                SubjectCard(
                    subject = subj,
                    masteryPercent = mastery,
                    topicCount = if (totalTopics > 0) totalTopics else 12,
                    onClick = { onNavigateToSubject(subj.id) },
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Weak Topics & Mistake Notebook shortcut (Dynamic)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToMistakes),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(ErrorRose.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = ErrorRose)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Mistake Notebook (3 Pending)", fontWeight = FontWeight.Bold)
                        Text(mistakeTopicPreview, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
