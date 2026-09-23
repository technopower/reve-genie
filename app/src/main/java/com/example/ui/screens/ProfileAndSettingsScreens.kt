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
import com.example.ui.components.GenieTopBar
import com.example.ui.theme.*

@Composable
fun ProfileAndSettingsScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val profile = MockEducationalRepository.defaultProfile
    var darkModeEnabled by remember { mutableStateOf(false) }
    var notificationEnabled by remember { mutableStateOf(true) }
    var selectedLanguage by remember { mutableStateOf(profile.language) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            GenieTopBar(
                title = "My Profile & Settings",
                onBackClick = onNavigateBack
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
            // Profile Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(PrimaryContainerLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👨‍🎓", fontSize = 38.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(profile.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(profile.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PrimaryContainerLight
                    ) {
                        Text(
                            text = "${profile.educationLevel} • ${profile.curriculum}",
                            style = MaterialTheme.typography.labelMedium,
                            color = PrimaryIndigo,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔥 ${profile.streakDays} Days", fontWeight = FontWeight.Bold)
                            Text("Current Streak", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("⭐ ${profile.xp} XP", fontWeight = FontWeight.Bold)
                            Text("Total Score", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🏆 Lvl ${profile.level}", fontWeight = FontWeight.Bold)
                            Text("Scholar Rank", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Settings options
            Text("App Preferences", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text("App Language / ভাষা") },
                        supportingContent = { Text(selectedLanguage) },
                        leadingContent = { Icon(Icons.Default.Language, contentDescription = null, tint = PrimaryIndigo) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                        modifier = Modifier.clickable { showLanguageDialog = true }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    ListItem(
                        headlineContent = { Text("Study Notifications & Reminders") },
                        supportingContent = { Text("Daily 07:30 PM reminder") },
                        leadingContent = { Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = PrimaryIndigo) },
                        trailingContent = {
                            Switch(
                                checked = notificationEnabled,
                                onCheckedChange = { notificationEnabled = it }
                            )
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    ListItem(
                        headlineContent = { Text("Daily Goal Target") },
                        supportingContent = { Text("${profile.dailyGoalMinutes} Minutes / Day") },
                        leadingContent = { Icon(Icons.Default.Timer, contentDescription = null, tint = PrimaryIndigo) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // About & Help
            Text("About REVE GENIE", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text("Universal Translator") },
                        supportingContent = { Text("Guaranteed Permanent 100% Free") },
                        leadingContent = { Icon(Icons.Default.Translate, contentDescription = null, tint = SuccessEmerald) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    ListItem(
                        headlineContent = { Text("Version") },
                        supportingContent = { Text("1.0.0 (Production Build)") },
                        leadingContent = { Icon(Icons.Default.Info, contentDescription = null, tint = PrimaryIndigo) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = onLogout,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRose),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (showLanguageDialog) {
            AlertDialog(
                onDismissRequest = { showLanguageDialog = false },
                title = { Text("Select Primary Language") },
                text = {
                    Column {
                        listOf("বাংলা", "English").forEach { lang ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedLanguage = lang
                                        showLanguageDialog = false
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = selectedLanguage == lang, onClick = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(lang, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showLanguageDialog = false }) { Text("Close") }
                }
            )
        }
    }
}
