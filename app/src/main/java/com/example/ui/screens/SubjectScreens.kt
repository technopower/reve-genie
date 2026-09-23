package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.SyllabusRepository
import com.example.model.SubjectType
import com.example.model.SyllabusSubject
import com.example.ui.components.GenieTopBar
import com.example.ui.components.SubjectCard
import com.example.ui.theme.*

@Composable
fun SubjectDetailScreen(
    subjectId: String,
    onNavigateBack: () -> Unit,
    onNavigateToQuiz: () -> Unit,
    onNavigateToChat: () -> Unit
) {
    val syllabusState by SyllabusRepository.syllabusFlow.collectAsState()
    
    // Find matching subject from syllabus repository or fallback to first available subject
    val matchedSyllabusSubject = syllabusState.subjects.find { 
        it.id.equals(subjectId, ignoreCase = true) || it.name.equals(subjectId, ignoreCase = true)
    } ?: syllabusState.subjects.firstOrNull()

    val subjectTitleEn = matchedSyllabusSubject?.name ?: "Mathematics"
    val subjectTitleBn = matchedSyllabusSubject?.nameBn ?: "গণিত"
    val subjectIcon = matchedSyllabusSubject?.icon ?: "🧮"
    val subjectColorHex = matchedSyllabusSubject?.colorHex ?: 0xFF3D5AFE
    val progressPercent = matchedSyllabusSubject?.progressPercent ?: 75

    // Flatten topics from the subject's chapters in the syllabus repository
    val syllabusTopics = matchedSyllabusSubject?.chapters?.flatMap { it.topics } ?: emptyList()

    Scaffold(
        topBar = {
            GenieTopBar(
                title = subjectTitleEn,
                subtitle = "$subjectTitleBn • ${syllabusState.educationLevel} ${syllabusState.curriculum}",
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = onNavigateToChat) {
                        Icon(Icons.Default.SmartToy, contentDescription = "Ask Subject AI", tint = Color(subjectColorHex))
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
                // Header Mastery Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(subjectColorHex).copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(subjectIcon, fontSize = 28.sp)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "$subjectTitleEn Mastery",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Based on your quizzes & questions",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        LinearProgressIndicator(
                            progress = { progressPercent / 100f },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = Color(subjectColorHex)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = onNavigateToChat,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(subjectColorHex)),
                                modifier = Modifier.weight(1f).padding(end = 6.dp)
                            ) {
                                Icon(Icons.Default.SmartToy, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Ask AI")
                            }
                            OutlinedButton(
                                onClick = onNavigateToQuiz,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).padding(start = 6.dp)
                            ) {
                                Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Take Quiz")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Chapters & Topics (অধ্যায়সমূহ)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (syllabusTopics.isNotEmpty()) {
                items(syllabusTopics) { topic ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable { onNavigateToChat() },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = if (topic.isWeak) androidx.compose.foundation.BorderStroke(1.5.dp, ErrorRose.copy(alpha = 0.5f)) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = topic.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (topic.isWeak) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = ErrorRose.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "Weak Topic",
                                                color = ErrorRose,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = topic.titleBn,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { topic.progressPercent / 100f },
                                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                    color = if (topic.isWeak) ErrorRose else Color(subjectColorHex)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Text(
                                text = "${topic.progressPercent}%",
                                fontWeight = FontWeight.Bold,
                                color = if (topic.isWeak) ErrorRose else Color(subjectColorHex)
                            )
                        }
                    }
                }
            } else {
                // Fallback for demo
                item {
                    Text(
                        "No topics found for this subject.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }
        }
    }
}
