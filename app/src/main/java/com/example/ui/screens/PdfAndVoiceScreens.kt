package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AcademicCatalog
import com.example.data.BookCatalog
import com.example.data.BookDownloadState
import com.example.data.BookStorageManager
import com.example.data.MockEducationalRepository
import com.example.model.AcademicGroup
import com.example.model.Book
import com.example.model.PdfDocument
import com.example.model.SubjectPaper
import com.example.ui.components.GenieTopBar
import com.example.ui.components.PdfReaderModal
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import com.google.accompanist.permissions.*
import com.example.util.SpeechToTextManager
import com.example.data.GeminiRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfTeacherScreen(
    onNavigateBack: () -> Unit,
    onOpenPdfChat: (PdfDocument) -> Unit,
    onOpenBookChat: (Book) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Cloud Textbooks, 1: Custom Uploads
    val customPdfs = remember { mutableStateListOf(*MockEducationalRepository.samplePdfs.toTypedArray()) }

    // Academic filters
    var selectedClass by remember { mutableStateOf("Class 6") }
    var selectedVersion by remember { mutableStateOf(com.example.model.BookVersion.BV) }
    var selectedGroup by remember { mutableStateOf(AcademicGroup.SCIENCE) }
    var selectedPaper by remember { mutableStateOf(SubjectPaper.NONE) }
    var searchQuery by remember { mutableStateOf("") }

    // Active Reader and Download states
    var activeViewingBook by remember { mutableStateOf<Book?>(null) }
    val downloadProgressMap = remember { mutableStateMapOf<String, Float>() }
    val downloadingBooks = remember { mutableStateMapOf<String, Boolean>() }
    val downloadErrorMap = remember { mutableStateMapOf<String, String>() }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    // Filter books according to academic context
    val currentBooks = remember(selectedClass, selectedVersion, selectedGroup, selectedPaper, searchQuery, refreshTrigger) {
        val isJunior = AcademicCatalog.isJuniorClass(selectedClass)
        val isSec = AcademicCatalog.isSecondaryClass(selectedClass)
        val isHsc = AcademicCatalog.isHigherSecondaryClass(selectedClass)

        val baseList = BookCatalog.getBooksForContext(
            className = selectedClass,
            curriculumId = selectedVersion.curriculumId,
            groupId = if (isJunior) "general_junior" else selectedGroup.id,
            paperId = if (isHsc && selectedPaper != SubjectPaper.NONE) selectedPaper.id else ""
        )

        if (searchQuery.isBlank()) {
            baseList
        } else {
            baseList.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.titleBn.contains(searchQuery, ignoreCase = true) ||
                        it.subjectId.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Modal PDF Viewer
    activeViewingBook?.let { book ->
        PdfReaderModal(
            book = book,
            onDismiss = { activeViewingBook = null },
            onAskAiAboutBook = { b ->
                activeViewingBook = null
                onOpenBookChat(b)
                onOpenPdfChat(b.toPdfDocument())
            }
        )
    }

    Scaffold(
        topBar = {
            GenieTopBar(
                title = "Textbook & PDF Library",
                subtitle = "Official NCTB Cloud Books & Offline Reader",
                onBackClick = onNavigateBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = PrimaryIndigo
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("NCTB Books (${currentBooks.size})", fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("My Uploads (${customPdfs.size})", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            if (selectedTab == 0) {
                // NCTB Cloud Books Tab
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    item {
                        // Class & Group Filter Card
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    "🎓 Academic Context Filter",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryIndigo
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                // Version / Medium Selector Row
                                Text("Curriculum Version:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(
                                        com.example.model.BookVersion.BV to "🇧🇩 Bangla Version (BV)",
                                        com.example.model.BookVersion.EV to "🇬🇧 English Version (EV)"
                                    ).forEach { (ver, label) ->
                                        FilterChip(
                                            selected = selectedVersion == ver,
                                            onClick = { selectedVersion = ver },
                                            label = { Text(label, fontSize = 12.sp) },
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Class Selector Row
                                Text("Class Level:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("Class 6", "Class 7", "Class 8", "Class 10 (SSC)", "Class 12 (HSC)").forEach { cls ->
                                        FilterChip(
                                            selected = selectedClass == cls,
                                            onClick = { selectedClass = cls },
                                            label = { Text(cls, fontSize = 12.sp) },
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }
                                }

                                val isJunior = AcademicCatalog.isJuniorClass(selectedClass)
                                val isHsc = AcademicCatalog.isHigherSecondaryClass(selectedClass)

                                if (!isJunior) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Academic Group:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        listOf(
                                            AcademicGroup.SCIENCE to "Science",
                                            AcademicGroup.BUSINESS_STUDIES to "Commerce",
                                            AcademicGroup.HUMANITIES to "Humanities"
                                        ).forEach { (grp, label) ->
                                            FilterChip(
                                                selected = selectedGroup == grp,
                                                onClick = { selectedGroup = grp },
                                                label = { Text(label, fontSize = 12.sp) },
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                        }
                                    }
                                }

                                if (isHsc) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Paper Filter:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        listOf(
                                            SubjectPaper.NONE to "All Papers",
                                            SubjectPaper.PAPER_1 to "1st Paper",
                                            SubjectPaper.PAPER_2 to "2nd Paper"
                                        ).forEach { (paper, label) ->
                                            FilterChip(
                                                selected = selectedPaper == paper,
                                                onClick = { selectedPaper = paper },
                                                label = { Text(label, fontSize = 12.sp) },
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Search Field
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search textbook by name or subject...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Available Textbooks (${currentBooks.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = PrimaryContainerLight
                            ) {
                                Text(
                                    "⚡ Zero APK Bloat",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryIndigo,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (currentBooks.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("������", fontSize = 40.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("No textbooks match this filter", fontWeight = FontWeight.Bold)
                                    Text("Try changing the class or group selection above.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    items(currentBooks, key = { it.id }) { book ->
                        val isDownloaded = remember(refreshTrigger, book.id) {
                            BookStorageManager.isBookDownloaded(context, book)
                        }
                        val isDownloading = downloadingBooks[book.id] == true
                        val progress = downloadProgressMap[book.id] ?: 0f
                        val downloadError = downloadErrorMap[book.id]

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .testTag("book_card_${book.id}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                when {
                                                    book.subjectId.contains("physics") -> Color(0xFF3B82F6).copy(alpha = 0.12f)
                                                    book.subjectId.contains("chem") -> Color(0xFF10B981).copy(alpha = 0.12f)
                                                    book.subjectId.contains("math") -> Color(0xFFF59E0B).copy(alpha = 0.12f)
                                                    book.subjectId.contains("bio") -> Color(0xFF8B5CF6).copy(alpha = 0.12f)
                                                    book.subjectId.contains("acc") -> Color(0xFFEC4899).copy(alpha = 0.12f)
                                                    else -> PrimaryIndigo.copy(alpha = 0.12f)
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            when {
                                                book.subjectId.contains("physics") -> "⚛️"
                                                book.subjectId.contains("chem") -> "🧪"
                                                book.subjectId.contains("math") -> "📐"
                                                book.subjectId.contains("bio") -> "🧬"
                                                book.subjectId.contains("acc") -> "📊"
                                                book.subjectId.contains("bangla") -> "📖"
                                                book.subjectId.contains("english") -> "🔤"
                                                book.subjectId.contains("ict") -> "💻"
                                                else -> "📘"
                                            },
                                            fontSize = 24.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            book.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 2
                                        )
                                        if (book.titleBn.isNotBlank()) {
                                            Text(
                                                book.titleBn,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = if (book.version == "EV") Color(0xFF3B82F6).copy(alpha = 0.15f) else Color(0xFF10B981).copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    book.version,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (book.version == "EV") Color(0xFF2563EB) else Color(0xFF059669),
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                )
                                            }
                                            Text(
                                                "${book.edition} • ${book.pages} Pages • ${(book.fileSizeBytes / (1024 * 1024)).coerceAtLeast(8)} MB",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            if (isDownloaded) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color(0xFF10B981).copy(alpha = 0.15f)
                                                ) {
                                                    Text(
                                                        "⚡ Offline Ready",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF10B981),
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                if (isDownloading) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Downloading textbook...", style = MaterialTheme.typography.labelSmall, color = PrimaryIndigo)
                                            Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { progress },
                                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                            color = PrimaryIndigo,
                                        )
                                    }
                                }

                                if (downloadError != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "Error: $downloadError",
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Action Buttons: View PDF, Download PDF, Ask AI
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // View PDF button
                                    Button(
                                        onClick = { activeViewingBook = book },
                                        modifier = Modifier.weight(1f).testTag("view_pdf_btn_${book.id}"),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                                    ) {
                                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (isDownloaded) "Read Offline" else "View PDF", fontSize = 13.sp)
                                    }

                                    // Download / Delete button
                                    if (isDownloaded) {
                                        OutlinedButton(
                                            onClick = {
                                                BookStorageManager.deleteDownloadedBook(context, book)
                                                refreshTrigger++
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                                        ) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Offline Copy", modifier = Modifier.size(16.dp))
                                        }
                                    } else {
                                        OutlinedButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    downloadingBooks[book.id] = true
                                                    downloadErrorMap.remove(book.id)
                                                    BookStorageManager.downloadBookFlow(context, book).collect { state ->
                                                        when (state) {
                                                            is BookDownloadState.Progress -> {
                                                                downloadProgressMap[book.id] = state.progressFraction
                                                            }
                                                            is BookDownloadState.Completed -> {
                                                                downloadingBooks[book.id] = false
                                                                refreshTrigger++
                                                            }
                                                            is BookDownloadState.Error -> {
                                                                downloadingBooks[book.id] = false
                                                                downloadErrorMap[book.id] = state.message
                                                            }
                                                            BookDownloadState.Idle -> {}
                                                        }
                                                    }
                                                }
                                            },
                                            enabled = !isDownloading,
                                            modifier = Modifier.weight(1f).testTag("download_pdf_btn_${book.id}"),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(if (isDownloading) "Saving..." else "Download", fontSize = 13.sp)
                                        }
                                    }

                                    // Ask AI
                                    IconButton(
                                        onClick = {
                                            onOpenBookChat(book)
                                            onOpenPdfChat(book.toPdfDocument())
                                        },
                                        modifier = Modifier
                                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                                            .testTag("ask_ai_btn_${book.id}")
                                    ) {
                                        Icon(Icons.Default.SmartToy, contentDescription = "Ask AI", tint = PrimaryIndigo)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Custom Uploads Tab (Original Feature preserved)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    item {
                        // Upload Card
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = PrimaryContainerLight),
                            modifier = Modifier.fillMaxWidth().clickable {
                                val newPdf = PdfDocument("pdf_${System.currentTimeMillis()}", "NCTB_Class_10_Physics_Guide.pdf", 56, "Physics", "Today")
                                customPdfs.add(0, newPdf)
                            }
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryIndigo.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(32.dp))
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text("Upload Custom PDF / Notes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OnPrimaryContainerLight)
                                Text("Ask questions, generate quizzes, or summarize custom lecture slides.", style = MaterialTheme.typography.bodySmall, color = OnPrimaryContainerLight.copy(alpha = 0.8f), textAlign = TextAlign.Center)
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = {
                                        val newPdf = PdfDocument("pdf_${System.currentTimeMillis()}", "Class_10_Lecture_Notes_${customPdfs.size + 1}.pdf", 48, "Science", "Today")
                                        customPdfs.add(0, newPdf)
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                                ) {
                                    Text("Choose File / ক্যামেরা দিয়ে ছবি নাও")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Your Custom PDFs (${customPdfs.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    items(customPdfs) { pdf ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable { onOpenPdfChat(pdf) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFD97706).copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFD97706))
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(pdf.fileName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1)
                                    Text("${pdf.pages} Pages • ${pdf.subject} • ${pdf.uploadDate}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(onClick = { onOpenPdfChat(pdf) }) {
                                    Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Chat with PDF", tint = PrimaryIndigo)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VoiceTeacherScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val speechManager = remember { SpeechToTextManager(context) }
    
    val micPermissionState = rememberPermissionState(android.Manifest.permission.RECORD_AUDIO)
    
    val recognizedText by speechManager.text.collectAsState()
    val isListeningFlow by speechManager.isListening.collectAsState()
    val voiceError by speechManager.error.collectAsState()
    
    var aiResponse by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }
    var selectedSpeed by remember { mutableStateOf("1.0x") }

    val infiniteWave = rememberInfiniteTransition(label = "wave")
    val waveHeight by infiniteWave.animateFloat(
        initialValue = 10f,
        targetValue = 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave_h"
    )

    DisposableEffect(Unit) {
        onDispose {
            speechManager.destroy()
        }
    }

    Scaffold(
        topBar = {
            GenieTopBar(
                title = "Voice AI Teacher",
                subtitle = "Speak in বাংলা or English",
                onBackClick = onNavigateBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isListeningFlow) "I am listening to your voice..." else if (isThinking) "AI Teacher is thinking..." else "Tap the microphone to speak",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isListeningFlow) PrimaryIndigo else MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                if (recognizedText.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PrimaryIndigo.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = recognizedText,
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (aiResponse.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SuccessEmerald.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = aiResponse,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                if (voiceError != null) {
                    Text(
                        text = voiceError ?: "",
                        color = Color.Red,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // Animated Voice Waveform Canvas
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(CircleShape)
                    .background(PrimaryContainerLight.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(160.dp)) {
                    val barWidth = 6.dp.toPx()
                    val spacing = 8.dp.toPx()
                    val numBars = 12
                    val startX = (size.width - (numBars * (barWidth + spacing))) / 2

                    for (i in 0 until numBars) {
                        val h = if (isListeningFlow) (waveHeight * ((i % 4) + 1) * 0.4f).coerceAtLeast(15f) else 15f
                        drawRoundRect(
                            color = PrimaryIndigo,
                            topLeft = Offset(startX + i * (barWidth + spacing), (size.height - h) / 2),
                            size = androidx.compose.ui.geometry.Size(barWidth, h),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                        )
                    }
                }
            }

            // Controls
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Speed chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    listOf("0.75x", "1.0x", "1.25x", "1.5x").forEach { speed ->
                        FilterChip(
                            selected = selectedSpeed == speed,
                            onClick = { selectedSpeed = speed },
                            label = { Text(speed) },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                // Big Mic Button
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(if (isListeningFlow) PrimaryIndigo else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { 
                            if (micPermissionState.status.isGranted) {
                                if (isListeningFlow) {
                                    speechManager.stopListening()
                                    // When stopped, send text to AI
                                    if (recognizedText.isNotBlank()) {
                                        scope.launch {
                                            isThinking = true
                                            aiResponse = GeminiRepository.getAiTeacherResponse(recognizedText)
                                            isThinking = false
                                        }
                                    }
                                } else {
                                    aiResponse = ""
                                    speechManager.startListening()
                                }
                            } else {
                                micPermissionState.launchPermissionRequest()
                            }
                        }
                        .testTag("voice_mic_toggle_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isListeningFlow) Icons.Default.Mic else Icons.Default.MicOff,
                        contentDescription = "Mic",
                        tint = if (isListeningFlow) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (isListeningFlow) "Tap to pause & ask" else if (micPermissionState.status.isGranted) "Tap to start talking" else "Grant mic permission to start",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
