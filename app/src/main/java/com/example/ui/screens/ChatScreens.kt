package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BookAiTeacherEngine
import com.example.data.BookCatalog
import com.example.data.MockEducationalRepository
import com.example.data.PythonApiClient
import com.example.data.SyllabusRepository
import com.example.model.*
import com.example.ui.components.GenieTopBar
import com.example.ui.theme.*
import com.example.util.AndroidTtsManager
import kotlinx.coroutines.launch

@Composable
fun AiTeacherChatScreen(
    onNavigateBack: () -> Unit,
    onNavigateToScan: () -> Unit,
    onNavigateToVoice: () -> Unit,
    onNavigateToPdf: () -> Unit,
    topicContext: TopicAiContext? = null,
    initialBook: Book? = null,
    initialPdf: PdfDocument? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        AndroidTtsManager.init(context)
    }
    DisposableEffect(Unit) {
        onDispose {
            AndroidTtsManager.stop()
        }
    }

    val messages = remember { mutableStateListOf(*MockEducationalRepository.initialChatMessages.toTypedArray()) }
    var inputText by remember { mutableStateOf("") }
    val syllabusState by SyllabusRepository.syllabusFlow.collectAsState()
    val effectiveContext = topicContext ?: remember(syllabusState) {
        SyllabusRepository.getActiveAcademicContext()
    }

    var activeBook by remember(effectiveContext, initialBook, initialPdf) {
        mutableStateOf(
            BookAiTeacherEngine.resolveActiveBook(
                context = effectiveContext,
                selectedBook = initialBook,
                pdfDocument = initialPdf
            )
        )
    }

    var isAiTyping by remember { mutableStateOf(false) }
    var showBookSelectorDialog by remember { mutableStateOf(false) }

    val isSpeaking by AndroidTtsManager.isSpeaking.collectAsState()
    val speakingMessageId by AndroidTtsManager.speakingMessageId.collectAsState()

    // Gallery launcher for Multimodal image questions (routed to Gemini via Python backend)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                messages.add(
                    ChatMessage(
                        id = System.currentTimeMillis().toString(),
                        sender = SenderType.USER,
                        text = "📷 [Attached Image Question]"
                    )
                )
                isAiTyping = true
                scope.launch {
                    val currentBookTitle = activeBook?.title ?: "${effectiveContext.subjectName} Textbook"
                    val contextStr = "Textbook: $currentBookTitle | Class: ${effectiveContext.educationLevel} | Curriculum: ${effectiveContext.curriculum}"
                    val imgResponse = PythonApiClient.analyzeImageWithGemini(
                        bitmap = bitmap,
                        question = "Analyze this image and solve the textbook problem or diagram step-by-step.",
                        context = contextStr,
                        language = effectiveContext.language
                    )
                    if (imgResponse.success && imgResponse.reply.isNotBlank()) {
                        messages.add(
                            ChatMessage(
                                id = System.currentTimeMillis().toString(),
                                sender = SenderType.AI,
                                text = imgResponse.reply,
                                sourceBookContext = BookSourceContext(
                                    bookId = activeBook?.id ?: "nctb_book",
                                    bookTitle = currentBookTitle,
                                    bookTitleBn = activeBook?.titleBn ?: "",
                                    chapterTitle = effectiveContext.chapterTitle,
                                    topicTitle = effectiveContext.topicTitle,
                                    className = effectiveContext.educationLevel
                                )
                            )
                        )
                    } else {
                        val err = imgResponse.error ?: "Unable to analyze image with Gemini."
                        messages.add(
                            ChatMessage(
                                id = System.currentTimeMillis().toString(),
                                sender = SenderType.AI,
                                text = "⚠️ Gemini Multimodal: $err"
                            )
                        )
                    }
                    isAiTyping = false
                }
            } catch (e: Exception) {
                Log.e("CHAT_SCREEN", "Error processing image attachment", e)
            }
        }
    }

    // Textbook-aware greeting
    LaunchedEffect(effectiveContext, activeBook) {
        val currentBookTitle = activeBook?.title ?: "${effectiveContext.subjectName} Textbook"
        val hasContextGreeting = messages.any { it.text.contains(effectiveContext.topicTitle) || it.text.contains(currentBookTitle) }
        if (!hasContextGreeting) {
            val groupInfo = if (effectiveContext.group != AcademicGroup.GENERAL_JUNIOR) " • Group: ${effectiveContext.group.titleEn}" else ""
            val paperInfo = if (effectiveContext.paper != SubjectPaper.NONE) " • ${effectiveContext.paper.titleEn}" else ""
            messages.add(
                ChatMessage(
                    id = System.currentTimeMillis().toString(),
                    sender = SenderType.AI,
                    text = "Hello! I am your REVE GENIE AI Teacher connected with **$currentBookTitle** (${activeBook?.titleBn ?: effectiveContext.topicTitleBn}).\n\n" +
                            "📖 **Textbook**: $currentBookTitle\n" +
                            "🎯 **Class**: ${effectiveContext.educationLevel} • **Curriculum**: ${effectiveContext.curriculum}$groupInfo$paperInfo\n" +
                            "📚 **Chapter**: ${effectiveContext.chapterTitle} > **Topic**: ${effectiveContext.topicTitle}\n\n" +
                            "Ask me any question from this textbook in বাংলা or English for step-by-step explanations, formulas, or worked examples!",
                    sourceBookContext = BookSourceContext(
                        bookId = activeBook?.id ?: "nctb_book",
                        bookTitle = currentBookTitle,
                        bookTitleBn = activeBook?.titleBn ?: "",
                        chapterTitle = effectiveContext.chapterTitle,
                        topicTitle = effectiveContext.topicTitle,
                        className = effectiveContext.educationLevel,
                        group = if (effectiveContext.group != AcademicGroup.GENERAL_JUNIOR) effectiveContext.group.titleEn else "",
                        paper = if (effectiveContext.paper != SubjectPaper.NONE) effectiveContext.paper.titleEn else ""
                    )
                )
            )
        }
    }

    val promptSuggestions = listOf(
        "Explain ${effectiveContext.topicTitle} step-by-step from textbook",
        "Give me a worked example for ${effectiveContext.topicTitleBn}",
        "What key formulas are in ${effectiveContext.chapterTitle}?",
        "Explain in simpler terms with real-world application in বাংলা"
    )

    Scaffold(
        topBar = {
            GenieTopBar(
                title = "AI Teacher",
                subtitle = "${effectiveContext.educationLevel} • ${activeBook?.title ?: effectiveContext.subjectName}",
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(
                        onClick = { showBookSelectorDialog = true },
                        modifier = Modifier.testTag("chat_switch_book_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Change Book", tint = PrimaryIndigo)
                    }
                    IconButton(onClick = onNavigateToVoice) {
                        Icon(Icons.Default.Mic, contentDescription = "Voice", tint = PrimaryIndigo)
                    }
                    IconButton(onClick = onNavigateToScan) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Scan", tint = PrimaryIndigo)
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.navigationBarsPadding().imePadding()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    // Attachment shortcuts
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AssistChip(
                            onClick = onNavigateToScan,
                            leadingIcon = { Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            label = { Text("Scan") },
                            shape = RoundedCornerShape(12.dp)
                        )
                        AssistChip(
                            onClick = {
                                galleryLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            leadingIcon = { Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            label = { Text("Image") },
                            shape = RoundedCornerShape(12.dp)
                        )
                        AssistChip(
                            onClick = onNavigateToPdf,
                            leadingIcon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            label = { Text("PDF Library") },
                            shape = RoundedCornerShape(12.dp)
                        )
                        AssistChip(
                            onClick = onNavigateToVoice,
                            leadingIcon = { Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            label = { Text("Voice") },
                            shape = RoundedCornerShape(12.dp)
                        )
                        if (activeBook != null) {
                            AssistChip(
                                onClick = { showBookSelectorDialog = true },
                                leadingIcon = { Text("📖", fontSize = 12.sp) },
                                label = { Text(activeBook?.title?.take(16) ?: "Book") },
                                shape = RoundedCornerShape(12.dp),
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = PrimaryContainerLight.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("Ask anything from textbook in বাংলা/English...") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_text_input"),
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryIndigo,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                if (inputText.isNotBlank()) {
                                    val query = inputText.trim()
                                    messages.add(
                                        ChatMessage(
                                            id = System.currentTimeMillis().toString(),
                                            sender = SenderType.USER,
                                            text = query
                                        )
                                    )
                                    inputText = ""
                                    isAiTyping = true

                                    scope.launch {
                                        val currentBookTitle = activeBook?.title ?: "${effectiveContext.subjectName} Textbook"
                                        val contextPrompt = "Textbook: $currentBookTitle | Class: ${effectiveContext.educationLevel} | Curriculum: ${effectiveContext.curriculum} | Chapter: ${effectiveContext.chapterTitle} | Topic: ${effectiveContext.topicTitle}"

                                        val pyResponse = PythonApiClient.chatWithOpenAI(
                                            message = query,
                                            context = contextPrompt,
                                            language = effectiveContext.language
                                        )

                                        if (pyResponse.success && pyResponse.reply.isNotBlank()) {
                                            messages.add(
                                                ChatMessage(
                                                    id = System.currentTimeMillis().toString(),
                                                    sender = SenderType.AI,
                                                    text = pyResponse.reply,
                                                    sourceBookContext = BookSourceContext(
                                                        bookId = activeBook?.id ?: "nctb_book",
                                                        bookTitle = currentBookTitle,
                                                        bookTitleBn = activeBook?.titleBn ?: "",
                                                        chapterTitle = effectiveContext.chapterTitle,
                                                        topicTitle = effectiveContext.topicTitle,
                                                        className = effectiveContext.educationLevel,
                                                        group = if (effectiveContext.group != AcademicGroup.GENERAL_JUNIOR) effectiveContext.group.titleEn else "",
                                                        paper = if (effectiveContext.paper != SubjectPaper.NONE) effectiveContext.paper.titleEn else ""
                                                    )
                                                )
                                            )
                                        } else {
                                            // Fallback to local textbook engine if Python backend server is unpowered/offline
                                            val fallbackAnswer = BookAiTeacherEngine.generateBookAnswer(
                                                query = query,
                                                academicContext = effectiveContext,
                                                activeBook = activeBook
                                            )
                                            messages.add(fallbackAnswer)
                                        }
                                        isAiTyping = false
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(PrimaryIndigo)
                                .testTag("chat_send_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
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
        ) {
            // Active Textbook & Topic Context Banner
            Surface(
                color = PrimaryContainerLight,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📖", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        val currentBookName = activeBook?.title ?: "${effectiveContext.subjectName} Textbook"
                        val groupText = if (effectiveContext.group != AcademicGroup.GENERAL_JUNIOR) " • ${effectiveContext.group.titleEn}" else ""
                        val paperText = if (effectiveContext.paper != SubjectPaper.NONE) " • ${effectiveContext.paper.titleEn}" else ""
                        Text(
                            text = "$currentBookName ($effectiveContext.educationLevel$groupText$paperText)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryIndigo,
                            maxLines = 1
                        )
                        Text(
                            text = "${effectiveContext.subjectName} > ${effectiveContext.chapterTitle} > ${effectiveContext.topicTitle}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnPrimaryContainerLight,
                            maxLines = 1
                        )
                    }
                    TextButton(
                        onClick = { showBookSelectorDialog = true },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                    ) {
                        Text("Switch", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryIndigo)
                    }
                }
            }

            // Prompt Starters Carousel
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(promptSuggestions) { suggestion ->
                    SuggestionChip(
                        onClick = { inputText = suggestion },
                        label = { Text(suggestion, fontSize = 12.sp) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Chat Messages List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                reverseLayout = false
            ) {
                items(messages, key = { it.id }) { msg ->
                    ChatBubbleItem(
                        message = msg,
                        isSpeaking = isSpeaking,
                        speakingMessageId = speakingMessageId,
                        onToggleSpeech = { text, id ->
                            AndroidTtsManager.toggle(text, id)
                        }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                if (isAiTyping) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = PrimaryIndigo)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI Teacher is consulting OpenAI & textbook...", style = MaterialTheme.typography.bodySmall, color = PrimaryIndigo)
                        }
                    }
                }
            }
        }
    }

    // Switch Textbook Dialog
    if (showBookSelectorDialog) {
        val availableBooks = remember(effectiveContext) {
            BookCatalog.getBooksForContext(
                className = effectiveContext.educationLevel,
                curriculumId = effectiveContext.curriculumId,
                groupId = effectiveContext.groupId
            ).ifEmpty { BookCatalog.getAllBooks().take(8) }
        }

        AlertDialog(
            onDismissRequest = { showBookSelectorDialog = false },
            title = {
                Text("Select Connected Textbook", fontWeight = FontWeight.Bold)
            },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 350.dp)) {
                    items(availableBooks) { bk ->
                        val isSelected = activeBook?.id == bk.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    activeBook = bk
                                    showBookSelectorDialog = false
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) PrimaryContainerLight else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("📘", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(bk.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    if (bk.titleBn.isNotBlank()) {
                                        Text(bk.titleBn, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text("${bk.className} • ${bk.pages} Pages", fontSize = 10.sp, color = PrimaryIndigo)
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = PrimaryIndigo, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBookSelectorDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun ChatBubbleItem(
    message: ChatMessage,
    isSpeaking: Boolean = false,
    speakingMessageId: String? = null,
    onToggleSpeech: (String, String) -> Unit = { _, _ -> }
) {
    val isUser = message.sender == SenderType.USER
    val clipboardManager = LocalClipboardManager.current
    val isCurrentlySpeaking = isSpeaking && speakingMessageId == message.id

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(PrimaryContainerLight),
                contentAlignment = Alignment.Center
            ) {
                Text("🧞‍♂️", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) PrimaryIndigo else MaterialTheme.colorScheme.surface,
            tonalElevation = if (isUser) 0.dp else 2.dp,
            border = if (isUser) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            modifier = Modifier.widthIn(max = 330.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Source Book Attribution Badge
                if (!isUser && message.sourceBookContext != null) {
                    val ctx = message.sourceBookContext
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (message.isOutOfScope) Color(0xFFFEF2F2) else PrimaryContainerLight.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(if (message.isOutOfScope) "⚠️" else "📖", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "Source: ${ctx.bookTitle}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (message.isOutOfScope) Color(0xFFDC2626) else PrimaryIndigo,
                                    maxLines = 1
                                )
                                Text(
                                    text = "${ctx.chapterTitle} > ${ctx.topicTitle}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Main Answer Content
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
                )

                // Step-by-Step Breakdown Card
                if (message.stepByStepExplanation.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🪜", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "ধাপে ধাপে ব্যাখ্যা (Step-by-Step Explanation):",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = PrimaryIndigo
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            message.stepByStepExplanation.forEachIndexed { idx, step ->
                                Text(
                                    text = step,
                                    fontSize = 12.sp,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Worked Example Problem Card
                if (message.exampleCase != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF0FDF4)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = message.exampleCase,
                                fontSize = 12.sp,
                                color = Color(0xFF166534),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // Math block
                if (message.mathFormula != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PrimaryContainerLight),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = message.mathFormula,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = OnPrimaryContainerLight,
                            modifier = Modifier.padding(10.dp),
                            fontSize = 13.sp
                        )
                    }
                }

                // Physics problem solver breakdown
                if (message.physicsBreakdown != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(10.dp)
                    ) {
                        Text("• Given:", fontWeight = FontWeight.Bold, color = Color(0xFF0891B2), fontSize = 12.sp)
                        Text(message.physicsBreakdown.given, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("• Formula:", fontWeight = FontWeight.Bold, color = Color(0xFF0891B2), fontSize = 12.sp)
                        Text(message.physicsBreakdown.formula, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("• Solution:", fontWeight = FontWeight.Bold, color = Color(0xFF0891B2), fontSize = 12.sp)
                        Text(message.physicsBreakdown.solution, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("• Final Answer: ${message.physicsBreakdown.answer}", fontWeight = FontWeight.ExtraBold, color = SuccessEmerald, fontSize = 12.sp)
                    }
                }

                // English Correction block
                if (message.englishCorrection != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(10.dp)
                    ) {
                        Text("Correction:", fontWeight = FontWeight.Bold, color = SuccessEmerald, fontSize = 12.sp)
                        Text(message.englishCorrection.corrected, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Rule Explanation:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        Text(message.englishCorrection.ruleExplanation, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Better Alternative: ${message.englishCorrection.betterSentence}", color = PrimaryIndigo, fontSize = 12.sp)
                    }
                }

                // AI Response Actions: Speak Answer, Copy, Share
                if (!isUser) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // "Speak Answer" Button (Android TTS)
                        OutlinedButton(
                            onClick = {
                                val fullSpeechText = buildString {
                                    append(message.text)
                                    if (message.stepByStepExplanation.isNotEmpty()) {
                                        append(". ")
                                        append(message.stepByStepExplanation.joinToString(". "))
                                    }
                                    if (message.exampleCase != null) {
                                        append(". ")
                                        append(message.exampleCase)
                                    }
                                }
                                onToggleSpeech(fullSpeechText, message.id)
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = if (isCurrentlySpeaking) ButtonDefaults.outlinedButtonColors(
                                containerColor = PrimaryIndigo.copy(alpha = 0.12f),
                                contentColor = PrimaryIndigo
                            ) else ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("tts_speak_btn_${message.id}")
                        ) {
                            Icon(
                                if (isCurrentlySpeaking) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Speak Answer",
                                modifier = Modifier.size(16.dp),
                                tint = if (isCurrentlySpeaking) PrimaryIndigo else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (isCurrentlySpeaking) "Stop Speaking" else "Speak Answer",
                                fontSize = 11.sp,
                                fontWeight = if (isCurrentlySpeaking) FontWeight.Bold else FontWeight.Normal
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = { clipboardManager.setText(AnnotatedString(message.text)) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
