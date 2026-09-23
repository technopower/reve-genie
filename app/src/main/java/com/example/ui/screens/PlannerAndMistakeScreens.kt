package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MockEducationalRepository
import com.example.model.MistakeItem
import com.example.model.StudyPlanItem
import com.example.ui.components.GenieTopBar
import com.example.ui.theme.*

@Composable
fun MistakeNotebookScreen(
    onNavigateBack: () -> Unit,
    onPracticeMistakes: () -> Unit
) {
    val mistakes = MockEducationalRepository.mistakeNotebook
    var selectedCategory by remember { mutableStateOf("All") }

    Scaffold(
        topBar = {
            GenieTopBar(
                title = "Mistake Notebook",
                subtitle = "${mistakes.size} Saved Weak Questions to Review",
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = onPracticeMistakes) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Practice Weak Questions", tint = PrimaryIndigo)
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 4.dp) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Button(
                        onClick = onPracticeMistakes,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                    ) {
                        Icon(Icons.Default.Repeat, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Practice All Weak Questions Now")
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                // Category Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All", "Concept", "Calculation", "Grammar").forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            items(mistakes.filter { selectedCategory == "All" || it.category == selectedCategory }) { mistake ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val headerText = buildString {
                                if (mistake.className.isNotBlank()) append("${mistake.className} • ")
                                append(mistake.subject)
                                if (mistake.chapter.isNotBlank()) append(" • ${mistake.chapter}")
                                if (mistake.topic.isNotBlank()) append(" • ${mistake.topic}")
                            }
                            Text(
                                headerText,
                                style = MaterialTheme.typography.labelSmall,
                                color = PrimaryIndigo,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = ErrorRose.copy(alpha = 0.12f)
                            ) {
                                Text(mistake.category, color = ErrorRose, fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(mistake.question, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = ErrorRose, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Your Answer: ${mistake.wrongAnswer}", color = ErrorRose, style = MaterialTheme.typography.bodySmall)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = SuccessEmerald, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Correct Answer: ${mistake.correctAnswer}", color = SuccessEmerald, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "💡 Why: ${mistake.explanation}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudyPlannerScreen(
    onNavigateBack: () -> Unit
) {
    val syllabusState by com.example.data.SyllabusRepository.syllabusFlow.collectAsState()
    val activeSubjects = syllabusState.subjects

    val initialItems = remember(activeSubjects, syllabusState) {
        if (activeSubjects.isNotEmpty()) {
            activeSubjects.take(5).mapIndexed { idx, subj ->
                val times = listOf("07:00 PM", "07:45 PM", "08:30 PM", "09:15 PM", "10:00 PM")
                val chapter = subj.chapters.firstOrNull()
                val topic = chapter?.topics?.firstOrNull()
                StudyPlanItem(
                    time = times.getOrElse(idx) { "08:00 PM" },
                    subject = subj.name,
                    topic = topic?.title ?: "${chapter?.title ?: subj.name} Review",
                    durationMinutes = 35,
                    isCompleted = false,
                    className = syllabusState.educationLevel,
                    curriculumId = subj.curriculumId.ifEmpty { syllabusState.curriculumId },
                    groupId = subj.groupId.ifEmpty { syllabusState.groupId },
                    group = if (subj.groupId.isNotEmpty()) subj.group else syllabusState.group,
                    paperId = subj.paperId,
                    paper = subj.paper,
                    chapter = chapter?.title ?: ""
                )
            }
        } else {
            emptyList()
        }
    }

    val items = remember(initialItems) { mutableStateListOf(*initialItems.toTypedArray()) }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            GenieTopBar(
                title = "AI Study Routine",
                subtitle = "${syllabusState.educationLevel} • ${items.size} Active Tasks",
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Task")
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
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryIndigo.copy(alpha = 0.12f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryIndigo.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📅 Today's Intelligent Routine", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.weight(1f))
                            Text("${items.size} Sessions", fontSize = 12.sp, color = PrimaryIndigo, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "REVE GENIE personalized routine based on your selected subjects and daily study target.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text("Schedule Timeline", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
            }

            items(items) { item ->
                val index = items.indexOf(item)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable {
                            items[index] = item.copy(isCompleted = !item.isCompleted)
                        },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = item.isCompleted,
                            onCheckedChange = { isChecked ->
                                items[index] = item.copy(isCompleted = isChecked)
                            }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${item.time} • ${item.subject}",
                                style = MaterialTheme.typography.labelSmall,
                                color = PrimaryIndigo,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = item.topic,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "${item.durationMinutes} min",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
