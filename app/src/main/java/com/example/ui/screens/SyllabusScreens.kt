package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.GenieTopBar
import com.example.ui.components.ProgressRing
import com.example.ui.theme.*

// ==========================================
// 1. SYLLABUS HOME SCREEN
// ==========================================
@Composable
fun SyllabusHomeScreen(
    syllabus: PersonalizedSyllabus,
    selectedSubjectId: String,
    onSelectSubject: (String) -> Unit,
    onOpenChapter: (SyllabusChapter) -> Unit,
    onOpenTopic: (SyllabusTopic, SyllabusChapter, SyllabusSubject) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToChatWithContext: (TopicAiContext) -> Unit
) {
    val isBangla = syllabus.language == "বাংলা"
    val activeSubject = syllabus.subjects.find { it.id == selectedSubjectId } ?: syllabus.subjects.firstOrNull()

    Scaffold(
        topBar = {
            GenieTopBar(
                title = if (isBangla) "সিলেবাস ও লার্নিং পাথ" else "Syllabus",
                subtitle = if (isBangla) "তোমার ব্যক্তিগত পাঠ্যসূচি" else "Your Personalized Learning Path",
                onBackClick = onNavigateBack
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Header: Personalized Progress Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("syllabus_overview_card"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = PrimaryIndigo.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "${syllabus.educationLevel} • ${syllabus.curriculum}",
                                        color = PrimaryIndigo,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = if (isBangla) "সার্বিক পাঠ্যক্রম অগ্রগতি" else "Overall Syllabus Completion",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${syllabus.completedChapters} of ${syllabus.totalChapters} Chapters Mastered",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            ProgressRing(
                                progress = syllabus.overallProgressPercent / 100f,
                                modifier = Modifier.size(76.dp),
                                strokeWidth = 8.dp,
                                color = PrimaryIndigo,
                                trackColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🔥", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${syllabus.streakDays} Days Study Streak",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StreakFire
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SuccessEmerald.copy(alpha = 0.14f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = SuccessEmerald, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Verified Curriculum",
                                        color = SuccessEmerald,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Smart Recommendation Section: Continue Learning & Recommended Revision
            item {
                Text(
                    text = if (isBangla) "স্মার্ট সুপারিশ ও চলমান পড়া" else "Smart Recommendations",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Continue Learning Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (activeSubject != null && activeSubject.chapters.isNotEmpty()) {
                                onOpenChapter(activeSubject.chapters.first())
                            }
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PrimaryIndigo.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("▶️", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Continue Learning • ${syllabus.continueSubjectName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = PrimaryIndigo,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = syllabus.continueChapterTitle,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { syllabus.continueProgressPercent / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = PrimaryIndigo
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "${syllabus.continueProgressPercent}%",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = PrimaryIndigo
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Recommended Revision Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryIndigo.copy(alpha = 0.12f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryIndigo.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("💡", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Recommended: ${syllabus.recommendedSubjectName}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp
                            )
                            Text(
                                text = syllabus.recommendedReason,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Subject Tabs (Filtered by Onboarding selections)
            item {
                Text(
                    text = if (isBangla) "বিষয়সমূহ (নির্বাচিত)" else "Subjects (From Onboarding)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(syllabus.subjects) { subject ->
                        val isSelected = subject.id == selectedSubjectId
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectSubject(subject.id) },
                            leadingIcon = {
                                Text(subject.icon, fontSize = 16.sp)
                            },
                            label = {
                                Text(
                                    text = if (isBangla) subject.nameBn else subject.name,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                selectedBorderColor = PrimaryIndigo,
                                borderWidth = 1.dp,
                                selectedBorderWidth = 2.dp
                            ),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                selectedContainerColor = PrimaryIndigo.copy(alpha = 0.15f),
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Active Subject Detail Card
            if (activeSubject != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(activeSubject.colorHex).copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(activeSubject.icon, fontSize = 24.sp)
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${activeSubject.name} (${activeSubject.nameBn})",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Current: ${activeSubject.currentChapterTitle}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "${activeSubject.progressPercent}%",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = Color(activeSubject.colorHex)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { activeSubject.progressPercent / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color(activeSubject.colorHex)
                            )

                            if (activeSubject.weakTopics.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("⚠️ Weak Topic: ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WarningAmber)
                                    Text(
                                        text = activeSubject.weakTopics.joinToString(", "),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = if (isBangla) "অধ্যায়ভিত্তিক পাঠ্যক্রম তালিকা" else "Chapter Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Chapters List
                items(activeSubject.chapters) { chapter ->
                    ChapterCardItem(
                        chapter = chapter,
                        subjectColorHex = activeSubject.colorHex,
                        isBangla = isBangla,
                        onClick = { onOpenChapter(chapter) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun ChapterCardItem(
    chapter: SyllabusChapter,
    subjectColorHex: Long,
    isBangla: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = chapter.status != ChapterStatus.LOCKED, onClick = onClick)
            .testTag("chapter_card_${chapter.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (chapter.status == ChapterStatus.LOCKED)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (chapter.status == ChapterStatus.LOCKED) 0.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(subjectColorHex).copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "Chapter ${chapter.chapterNumber} (অধ্যায় ${chapter.chapterNumber})",
                        color = Color(subjectColorHex),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                ChapterStatusBadge(status = chapter.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "${chapter.title} • ${chapter.titleBn}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "⏱️ ${chapter.estimatedHours} study time  •  ${chapter.topics.size} Topics",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { chapter.progressPercent / 100f },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(subjectColorHex)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "${chapter.progressPercent}%",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(subjectColorHex)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Verified, contentDescription = null, tint = SuccessEmerald, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = chapter.curriculumFrameworkTag,
                    fontSize = 10.sp,
                    color = SuccessEmerald,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ChapterStatusBadge(status: ChapterStatus) {
    val (label, bg, fg) = when (status) {
        ChapterStatus.COMPLETED -> Triple("COMPLETED", SuccessEmerald.copy(alpha = 0.15f), SuccessEmerald)
        ChapterStatus.IN_PROGRESS -> Triple("IN PROGRESS", PrimaryIndigo.copy(alpha = 0.15f), PrimaryIndigo)
        ChapterStatus.NOT_STARTED -> Triple("NOT STARTED", Color(0xFF64748B).copy(alpha = 0.15f), Color(0xFF64748B))
        ChapterStatus.LOCKED -> Triple("LOCKED", Color(0xFF94A3B8).copy(alpha = 0.15f), Color(0xFF94A3B8))
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bg
    ) {
        Text(
            text = label,
            color = fg,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

// ==========================================
// 2. CHAPTER OVERVIEW SCREEN
// ==========================================
@Composable
fun ChapterOverviewScreen(
    chapter: SyllabusChapter,
    subject: SyllabusSubject,
    onNavigateBack: () -> Unit,
    onOpenTopic: (SyllabusTopic) -> Unit,
    onStartChapterQuiz: () -> Unit,
    onAskAiChapter: () -> Unit
) {
    Scaffold(
        topBar = {
            GenieTopBar(
                title = "Chapter ${chapter.chapterNumber}: ${chapter.title}",
                subtitle = "${subject.name} • ${chapter.curriculumFrameworkTag}",
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = onAskAiChapter) {
                        Icon(Icons.Default.SmartToy, contentDescription = "Ask AI", tint = Color(subject.colorHex))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Chapter Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = chapter.titleBn,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Estimated Time: ${chapter.estimatedHours} • ${chapter.topics.size} Key Topics",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            ChapterStatusBadge(status = chapter.status)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        LinearProgressIndicator(
                            progress = { chapter.progressPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(subject.colorHex)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Learning Objectives
                        Text(
                            text = "🎯 Learning Objectives (শিখনফল)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        chapter.learningObjectives.forEach { objective ->
                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                Text("• ", color = Color(subject.colorHex), fontWeight = FontWeight.Bold)
                                Text(
                                    text = objective,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Chapter Quiz Action Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryContainerLight)
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(PrimaryIndigo),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Quiz, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Chapter Assessment Quiz", fontWeight = FontWeight.Bold, color = OnPrimaryContainerLight)
                            Text("Test concept mastery and update your syllabus %", style = MaterialTheme.typography.bodySmall, color = OnPrimaryContainerLight.copy(alpha = 0.8f))
                        }
                        Button(
                            onClick = onStartChapterQuiz,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                        ) {
                            Text("Take Quiz")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("Chapter Topics & Lessons", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Topics List
            items(chapter.topics) { topic ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenTopic(topic) }
                        .testTag("topic_card_${topic.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (topic.isCompleted) SuccessEmerald.copy(alpha = 0.15f)
                                    else Color(subject.colorHex).copy(alpha = 0.12f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (topic.isCompleted) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = SuccessEmerald, modifier = Modifier.size(20.dp))
                            } else {
                                Text(
                                    "${chapter.topics.indexOf(topic) + 1}",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(subject.colorHex)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = topic.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = topic.titleBn,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (topic.isWeak) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = WarningAmber.copy(alpha = 0.14f)
                                ) {
                                    Text(
                                        text = "⚠️ Weak Topic - Needs Practice",
                                        color = WarningAmber,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Open Topic",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Revision Summary
            if (chapter.revisionSummary.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📝 Quick Revision Summary", fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = chapter.revisionSummary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. TOPIC DETAIL SCREEN (Concept, Practice, AI Teacher)
// ==========================================
@Composable
fun TopicDetailScreen(
    topic: SyllabusTopic,
    chapter: SyllabusChapter,
    subject: SyllabusSubject,
    onNavigateBack: () -> Unit,
    onAskAiTeacher: (TopicAiContext) -> Unit,
    onToggleBookmark: (String) -> Unit,
    onAddToMistakes: (String) -> Unit,
    onMarkCompleted: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedDifficulty by remember { mutableStateOf<QuestionDifficulty?>(null) }
    val tabTitles = listOf("Concept (মূলভাব)", "Example (উদাহরণ)", "Practice (অনুশীলনী)")

    val filteredQuestions = if (selectedDifficulty == null) {
        topic.practiceQuestions
    } else {
        topic.practiceQuestions.filter { it.difficulty == selectedDifficulty }
    }

    val currentSyllabus = com.example.data.SyllabusRepository.getSyllabus()
    val aiContext = TopicAiContext(
        educationLevel = currentSyllabus.educationLevel,
        curriculum = currentSyllabus.curriculum,
        subjectId = subject.id,
        subjectName = subject.name,
        chapterId = chapter.id,
        chapterTitle = chapter.title,
        topicId = topic.id,
        topicTitle = topic.title,
        topicTitleBn = topic.titleBn,
        language = currentSyllabus.language,
        groupId = subject.groupId.ifEmpty { currentSyllabus.groupId },
        group = if (subject.groupId.isNotEmpty()) subject.group else currentSyllabus.group,
        paperId = subject.paperId,
        paper = subject.paper,
        curriculumId = subject.curriculumId.ifEmpty { currentSyllabus.curriculumId }
    )

    Scaffold(
        topBar = {
            GenieTopBar(
                title = topic.title,
                subtitle = "${subject.name} • ${chapter.title}",
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = { onAskAiTeacher(aiContext) }) {
                        Icon(Icons.Default.SmartToy, contentDescription = "Ask AI", tint = PrimaryIndigo)
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { onAskAiTeacher(aiContext) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.SmartToy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ask AI Teacher")
                    }

                    Button(
                        onClick = onMarkCompleted,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (topic.isCompleted) SuccessEmerald else PrimaryIndigo
                        )
                    ) {
                        Icon(
                            if (topic.isCompleted) Icons.Default.CheckCircle else Icons.Default.Done,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (topic.isCompleted) "Completed 🎉" else "Mark Complete")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // AI Teacher Context Header Bar
            Surface(
                color = PrimaryContainerLight,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🤖", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "Context: ${subject.name} > ${topic.title}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = OnPrimaryContainerLight
                            )
                            Text(
                                "AI knows your grade level and curriculum",
                                fontSize = 11.sp,
                                color = OnPrimaryContainerLight.copy(alpha = 0.8f)
                            )
                        }
                    }
                    TextButton(
                        onClick = { onAskAiTeacher(aiContext) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Chat AI", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PrimaryIndigo)
                    }
                }
            }

            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> ConceptTab(topic = topic, onAskAi = { onAskAiTeacher(aiContext) })
                1 -> ExampleTab(topic = topic, onAskAi = { onAskAiTeacher(aiContext) })
                2 -> PracticeTab(
                    questions = filteredQuestions,
                    selectedDifficulty = selectedDifficulty,
                    onSelectDifficulty = { selectedDifficulty = it },
                    onToggleBookmark = onToggleBookmark,
                    onAddToMistakes = onAddToMistakes,
                    onAskAiForQuestion = { onAskAiTeacher(aiContext) }
                )
            }
        }
    }
}

@Composable
fun ConceptTab(topic: SyllabusTopic, onAskAi: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "📖 Concept Explanation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = topic.conceptExplanation,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "বাংলা সারসংক্ষেপ:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = PrimaryIndigo
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = topic.conceptExplanationBn,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // AI Assistant Callout
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("💡", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Need this in simpler terms?", fontWeight = FontWeight.Bold)
                    Text("REVE GENIE AI can break this concept down with real-life analogies.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(
                    onClick = onAskAi,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                ) {
                    Text("Ask AI")
                }
            }
        }
    }
}

@Composable
fun ExampleTab(topic: SyllabusTopic, onAskAi: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "📝 Standard Worked Example",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = topic.exampleProblem,
                        modifier = Modifier.padding(14.dp),
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Step-by-Step Mathematical Solution:",
                    fontWeight = FontWeight.Bold,
                    color = SuccessEmerald
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = topic.exampleSolution,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 22.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // AI Problem Generator
        FilledTonalButton(
            onClick = onAskAi,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Generate Similar AI Example with Solution")
        }
    }
}

@Composable
fun PracticeTab(
    questions: List<PracticeQuestionItem>,
    selectedDifficulty: QuestionDifficulty?,
    onSelectDifficulty: (QuestionDifficulty?) -> Unit,
    onToggleBookmark: (String) -> Unit,
    onAddToMistakes: (String) -> Unit,
    onAskAiForQuestion: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // Difficulty filter chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedDifficulty == null,
                onClick = { onSelectDifficulty(null) },
                label = { Text("All (${questions.size})") }
            )
            FilterChip(
                selected = selectedDifficulty == QuestionDifficulty.EASY,
                onClick = { onSelectDifficulty(QuestionDifficulty.EASY) },
                label = { Text("Easy") }
            )
            FilterChip(
                selected = selectedDifficulty == QuestionDifficulty.MEDIUM,
                onClick = { onSelectDifficulty(QuestionDifficulty.MEDIUM) },
                label = { Text("Medium") }
            )
            FilterChip(
                selected = selectedDifficulty == QuestionDifficulty.HARD,
                onClick = { onSelectDifficulty(QuestionDifficulty.HARD) },
                label = { Text("Hard") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (questions.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                Text("No questions in this difficulty level.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            questions.forEach { question ->
                PracticeQuestionCard(
                    question = question,
                    onToggleBookmark = { onToggleBookmark(question.id) },
                    onAddToMistakes = { onAddToMistakes(question.id) },
                    onAskAi = onAskAiForQuestion
                )
                Spacer(modifier = Modifier.height(14.dp))
            }
        }
    }
}

@Composable
fun PracticeQuestionCard(
    question: PracticeQuestionItem,
    onToggleBookmark: () -> Unit,
    onAddToMistakes: () -> Unit,
    onAskAi: () -> Unit
) {
    var showHint by remember { mutableStateOf(false) }
    var showSolution by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (question.difficulty) {
                        QuestionDifficulty.EASY -> SuccessEmerald.copy(alpha = 0.15f)
                        QuestionDifficulty.MEDIUM -> WarningAmber.copy(alpha = 0.15f)
                        QuestionDifficulty.HARD -> ErrorRose.copy(alpha = 0.15f)
                    }
                ) {
                    Text(
                        text = question.difficulty.name,
                        color = when (question.difficulty) {
                            QuestionDifficulty.EASY -> SuccessEmerald
                            QuestionDifficulty.MEDIUM -> WarningAmber
                            QuestionDifficulty.HARD -> ErrorRose
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Row {
                    IconButton(onClick = onToggleBookmark) {
                        Icon(
                            imageVector = if (question.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (question.isBookmarked) PrimaryIndigo else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onAddToMistakes) {
                        Icon(
                            imageVector = Icons.Default.NoteAdd,
                            contentDescription = "Add to Mistake Notebook",
                            tint = if (question.isAddedToMistakes) ErrorRose else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = question.questionText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )

            // Hint Drawer
            AnimatedVisibility(visible = showHint) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = WarningAmber.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "💡 Hint: ${question.hint}",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            // Step-by-Step Solution Drawer
            AnimatedVisibility(visible = showSolution) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = PrimaryIndigo.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryIndigo.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Step-by-Step Solution:", fontWeight = FontWeight.Bold, color = PrimaryIndigo)
                            Spacer(modifier = Modifier.height(6.dp))
                            question.steps.forEach { step ->
                                Text("• $step", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Answer: ${question.correctAnswer}", fontWeight = FontWeight.Bold, color = SuccessEmerald)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showHint = !showHint },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(if (showHint) "Hide Hint" else "Hint", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { showSolution = !showSolution },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(if (showSolution) "Hide Steps" else "Step-by-Step", fontSize = 12.sp)
                }

                Button(
                    onClick = onAskAi,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.SmartToy, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ask AI", fontSize = 12.sp)
                }
            }
        }
    }
}
