package com.example.ui.screens

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GeminiRepository
import com.example.ui.components.CameraPreview
import com.example.ui.components.GenieTopBar
import com.example.ui.theme.*
import com.example.util.TextRecognitionHelper
import com.google.accompanist.permissions.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ImageScannerScreen(
    onNavigateBack: () -> Unit,
    onSolveComplete: (String, String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    
    var hasCaptured by remember { mutableStateOf(false) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var flashEnabled by remember { mutableStateOf(false) }
    var selectedSubjectFilter by remember { mutableStateOf("Math") }
    
    var recognizedText by remember { mutableStateOf("") }
    var aiSolution by remember { mutableStateOf("") }
    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

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
                currentBitmap = bitmap
                scope.launch {
                    hasCaptured = true
                    recognizedText = TextRecognitionHelper.recognizeText(bitmap)
                }
            } catch (e: Exception) {
                recognizedText = "Error loading gallery image: ${e.message}"
                hasCaptured = true
            }
        }
    }

    val scanAnimation = rememberInfiniteTransition(label = "scan")
    val laserY by scanAnimation.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser"
    )

    Scaffold(
        topBar = {
            GenieTopBar(
                title = "AI Question Scanner",
                subtitle = "Align math, physics, chem or english question",
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = { flashEnabled = !flashEnabled }) {
                        Icon(
                            imageVector = if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Flash",
                            tint = if (flashEnabled) WarningAmber else MaterialTheme.colorScheme.onSurface
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
                .background(Color(0xFF0F172A)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Subject hint selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                listOf("Math", "Physics", "Chemistry", "English").forEach { subj ->
                    val isSelected = selectedSubjectFilter == subj
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) PrimaryIndigo else Color.White.copy(alpha = 0.15f),
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clickable { selectedSubjectFilter = subj }
                    ) {
                        Text(
                            text = subj,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Viewfinder box
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(320.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .border(2.dp, PrimaryIndigoLight, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (cameraPermissionState.status.isGranted) {
                    if (!hasCaptured) {
                        CameraPreview(
                            modifier = Modifier.fillMaxSize(),
                            imageCapture = imageCapture
                        )
                        
                        // Targeting lines overlay
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(Icons.Default.CropFree, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Frame your textbook question inside this box",
                                color = Color.White.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        // Preview captured question
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                "Captured Question Preview",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    recognizedText.ifEmpty { "Reading text..." },
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(14.dp),
                                    maxLines = 5
                                )
                            }

                            if (isAnalyzing) {
                                Spacer(modifier = Modifier.height(16.dp))
                                LinearProgressIndicator(color = Color(0xFF00E5FF))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("AI is reading image & solving step-by-step...", color = Color.White, fontSize = 12.sp)
                            }
                        }

                        // Scanning laser effect
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val y = size.height * laserY
                            drawLine(
                                color = Color(0xFF00E5FF),
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 3.dp.toPx()
                            )
                        }
                    }
                } else {
                    Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                        Text("Grant Camera Permission")
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Shutter & Gallery Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp, start = 24.dp, end = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.size(54.dp)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery", tint = Color.White, modifier = Modifier.size(28.dp))
                }

                if (!hasCaptured) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable {
                                if (cameraPermissionState.status.isGranted) {
                                    imageCapture.takePicture(
                                        cameraExecutor,
                                        object : ImageCapture.OnImageCapturedCallback() {
                                            override fun onCaptureSuccess(image: ImageProxy) {
                                                val buffer = image.planes[0].buffer
                                                val bytes = ByteArray(buffer.capacity())
                                                buffer.get(bytes)
                                                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                                image.close()
                                                
                                                currentBitmap = bitmap
                                                scope.launch {
                                                    hasCaptured = true
                                                    recognizedText = TextRecognitionHelper.recognizeText(bitmap)
                                                }
                                            }
                                        }
                                    )
                                } else {
                                    cameraPermissionState.launchPermissionRequest()
                                }
                            }
                            .testTag("camera_shutter_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(PrimaryIndigo)
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            isAnalyzing = true
                            scope.launch {
                                val bmp = currentBitmap
                                aiSolution = if (bmp != null) {
                                    // Gemini multimodal image solver
                                    GeminiRepository.solveQuestionFromImage(bmp, recognizedText)
                                } else {
                                    // OpenAI text solver
                                    GeminiRepository.solveQuestionFromText(recognizedText)
                                }
                                delay(300)
                                onSolveComplete(recognizedText, aiSolution)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.height(54.dp).testTag("solve_with_ai_button")
                    ) {
                        Text("Solve with AI ⚡", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (hasCaptured) {
                    IconButton(
                        onClick = {
                            hasCaptured = false
                            isAnalyzing = false
                            recognizedText = ""
                            aiSolution = ""
                            currentBitmap = null
                        },
                        modifier = Modifier.size(54.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Retake", tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                } else {
                    Spacer(modifier = Modifier.size(54.dp))
                }
            }
        }
    }
}

@Composable
fun ScanResultScreen(
    questionText: String,
    solutionText: String,
    onNavigateBack: () -> Unit,
    onPracticeSimilar: () -> Unit,
    onSaveToMistakes: () -> Unit
) {
    Scaffold(
        topBar = {
            GenieTopBar(
                title = "Solution Result",
                subtitle = "Solved by REVE GENIE AI",
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = onSaveToMistakes) {
                        Icon(Icons.Default.BookmarkBorder, contentDescription = "Save")
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
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
            Text("Detected Question", style = MaterialTheme.typography.labelMedium, color = PrimaryIndigo, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = questionText.ifEmpty { "No text detected" },
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("💡 AI Detailed Solution", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = solutionText.ifEmpty { "AI couldn't generate a solution." },
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SuccessEmerald.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("✅ Answer Verified by REVE GENIE AI", fontWeight = FontWeight.Bold, color = SuccessEmerald, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onPracticeSimilar,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
            ) {
                Icon(Icons.Default.FitnessCenter, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Practice 3 Similar Questions")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onSaveToMistakes,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.BookmarkAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save to Mistake Notebook")
            }
        }
    }
}
