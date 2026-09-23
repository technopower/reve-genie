package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.BookStorageManager
import com.example.model.Book
import com.example.ui.theme.PrimaryIndigo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun PdfReaderModal(
    book: Book,
    onDismiss: () -> Unit,
    onAskAiAboutBook: (Book) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var totalPages by remember { mutableIntStateOf(0) }
    var currentPageIndex by remember { mutableIntStateOf(0) }
    var currentPageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isOfflineMode by remember { mutableStateOf(false) }

    var zoomScale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    var pdfRenderer by remember { mutableStateOf<PdfRenderer?>(null) }
    var fileDescriptor by remember { mutableStateOf<ParcelFileDescriptor?>(null) }

    fun safelyCloseRenderer() {
        try {
            pdfRenderer?.close()
        } catch (_: Exception) {}
        pdfRenderer = null

        try {
            fileDescriptor?.close()
        } catch (_: Exception) {}
        fileDescriptor = null
    }

    fun renderPage(renderer: PdfRenderer, pageIndex: Int) {
        if (pageIndex < 0 || pageIndex >= renderer.pageCount) return
        scope.launch(Dispatchers.IO) {
            var page: PdfRenderer.Page? = null
            try {
                page = renderer.openPage(pageIndex)
                val width = (page.width * 2).coerceAtMost(2048)
                val height = (page.height * 2).coerceAtMost(2048)
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(android.graphics.Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                withContext(Dispatchers.Main) {
                    currentPageBitmap = bitmap
                    currentPageIndex = pageIndex
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = "Error rendering page ${pageIndex + 1}: ${e.message}"
                }
            } finally {
                try {
                    page?.close()
                } catch (_: Exception) {}
            }
        }
    }

    fun loadPdf(forceRemoteDownload: Boolean = false) {
        isLoading = true
        errorMessage = null
        scope.launch(Dispatchers.IO) {
            safelyCloseRenderer()

            try {
                val cachedFile = if (!forceRemoteDownload) {
                    BookStorageManager.getCachedFileOrNull(context, book)
                } else null

                val isUsingCache = cachedFile != null && cachedFile.exists() && cachedFile.length() > 0
                android.util.Log.i("PdfReaderModal", "Opening PDF for Book id='${book.id}', title='${book.title}', class='${book.className}', subject='${book.subjectId}', pdfUrl='${book.pdfUrl}', isUsingCache=$isUsingCache, forceRemoteDownload=$forceRemoteDownload")

                withContext(Dispatchers.Main) {
                    isOfflineMode = isUsingCache
                }

                val targetFile: File = if (cachedFile != null && cachedFile.length() > 0) {
                    // Priority 1: ALWAYS open the local cached PDF from downloaded_textbooks/ first
                    cachedFile
                } else {
                    // Priority 2: Fall back to remote download
                    BookStorageManager.ensureBookAvailable(context, book, forceDownload = forceRemoteDownload)
                }

                if (!targetFile.exists() || targetFile.length() == 0L) {
                    throw IllegalStateException("PDF file is empty or could not be found in storage.")
                }

                val pfd = try {
                    ParcelFileDescriptor.open(targetFile, ParcelFileDescriptor.MODE_READ_ONLY)
                } catch (e: Exception) {
                    throw IllegalStateException("Unable to open file descriptor for PDF: ${e.message}", e)
                }

                val renderer = try {
                    PdfRenderer(pfd)
                } catch (e: Exception) {
                    try {
                        pfd.close()
                    } catch (_: Exception) {}
                    if (isUsingCache) {
                        // Cached file was corrupted; delete it to allow clean re-download
                        BookStorageManager.deleteDownloadedBook(context, book)
                    }
                    throw IllegalStateException("The PDF file appears corrupted or invalid: ${e.localizedMessage ?: e.message}")
                }

                withContext(Dispatchers.Main) {
                    fileDescriptor = pfd
                    pdfRenderer = renderer
                    totalPages = renderer.pageCount
                    isLoading = false
                    renderPage(renderer, 0)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    safelyCloseRenderer()
                    isLoading = false
                    val msg = e.localizedMessage ?: e.message ?: "Unknown error"
                    errorMessage = if (msg.startsWith("PDF unavailable", ignoreCase = true)) msg else "PDF unavailable: $msg"
                }
            }
        }
    }

    LaunchedEffect(book.id) {
        loadPdf()
    }

    DisposableEffect(Unit) {
        onDispose {
            safelyCloseRenderer()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("pdf_reader_dialog"),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                topBar = {
                    Surface(
                        tonalElevation = 4.dp,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    IconButton(onClick = onDismiss) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = book.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "${book.className} • ${book.edition}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = if (isOfflineMode) Color(0xFF10B981).copy(alpha = 0.15f) else PrimaryIndigo.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = if (isOfflineMode) "⚡ OFFLINE" else "☁️ CLOUD",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isOfflineMode) Color(0xFF10B981) else PrimaryIndigo,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            zoomScale = 1f
                                            offsetX = 0f
                                            offsetY = 0f
                                        }
                                    ) {
                                        Icon(Icons.Default.ZoomOutMap, contentDescription = "Reset Zoom")
                                    }
                                    IconButton(onClick = { onAskAiAboutBook(book) }) {
                                        Icon(Icons.Default.SmartToy, contentDescription = "Ask AI", tint = PrimaryIndigo)
                                    }
                                }
                            }
                        }
                    }
                },
                bottomBar = {
                    Surface(
                        tonalElevation = 8.dp,
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.navigationBarsPadding()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    val renderer = pdfRenderer ?: return@Button
                                    if (currentPageIndex > 0) {
                                        renderPage(renderer, currentPageIndex - 1)
                                    }
                                },
                                enabled = currentPageIndex > 0 && !isLoading,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Page")
                                Text("Prev")
                            }

                            Text(
                                text = if (totalPages > 0) "Page ${currentPageIndex + 1} of $totalPages" else "Page --",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Button(
                                onClick = {
                                    val renderer = pdfRenderer ?: return@Button
                                    if (currentPageIndex < totalPages - 1) {
                                        renderPage(renderer, currentPageIndex + 1)
                                    }
                                },
                                enabled = currentPageIndex < totalPages - 1 && !isLoading,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Next")
                                Icon(Icons.Default.ChevronRight, contentDescription = "Next Page")
                            }
                        }
                    }
                }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(Color(0xFF1E293B)),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isLoading -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(32.dp)
                            ) {
                                CircularProgressIndicator(color = PrimaryIndigo, strokeWidth = 4.dp)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    if (isOfflineMode) "Opening cached offline textbook..." else "Loading textbook from cloud...",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    if (isOfflineMode) "Loading stored pages from device storage" else "Preparing pages for reading and AI queries",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }

                        errorMessage != null -> {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                modifier = Modifier.padding(24.dp).fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.ErrorOutline,
                                        contentDescription = "Error",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    val isUnavailable = errorMessage?.contains("unavailable", ignoreCase = true) == true
                                    val isNetworkError = errorMessage?.contains("network", ignoreCase = true) == true ||
                                            errorMessage?.contains("connection", ignoreCase = true) == true ||
                                            errorMessage?.contains("timeout", ignoreCase = true) == true ||
                                            errorMessage?.contains("unreachable", ignoreCase = true) == true

                                    val errorHeading = when {
                                        isUnavailable -> "PDF Unavailable"
                                        isNetworkError -> "Network Connection Error"
                                        else -> "Could Not Load PDF"
                                    }

                                    Text(
                                        errorHeading,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        errorMessage ?: "An error occurred while loading the PDF.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedButton(
                                            onClick = onDismiss,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Close")
                                        }
                                        Button(
                                            onClick = { loadPdf(forceRemoteDownload = true) },
                                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                                            modifier = Modifier.weight(1.5f)
                                        ) {
                                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Retry / পুনরায় চেষ্টা")
                                        }
                                    }
                                }
                            }
                        }

                        currentPageBitmap != null -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(Unit) {
                                        detectTransformGestures { _, pan, zoom, _ ->
                                            zoomScale = (zoomScale * zoom).coerceIn(1f, 4f)
                                            if (zoomScale > 1f) {
                                                offsetX += pan.x
                                                offsetY += pan.y
                                            } else {
                                                offsetX = 0f
                                                offsetY = 0f
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    bitmap = currentPageBitmap!!.asImageBitmap(),
                                    contentDescription = "PDF Page ${currentPageIndex + 1}",
                                    modifier = Modifier
                                        .fillMaxWidth(0.95f)
                                        .aspectRatio(0.707f) // Standard A4 ratio
                                        .graphicsLayer(
                                            scaleX = zoomScale,
                                            scaleY = zoomScale,
                                            translationX = offsetX,
                                            translationY = offsetY
                                        )
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
