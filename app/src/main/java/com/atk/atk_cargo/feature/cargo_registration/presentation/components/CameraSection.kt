package com.atk.atk_cargo.feature.cargo_registration.presentation.components

import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.atk.atk_cargo.feature.cargo_registration.domain.ocr.EnhancedNumberAnalyzer
import com.atk.atk_cargo.ui.theme.ATKCargoTheme
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ScannerGuideOverlay(
    scanAreaSize: Float = 0.7f,
    guideColor: Color = Color.Green.copy(alpha = 0.7f),
    guideThickness: Float = 2f,
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val scanAreaWidth = width * scanAreaSize
        val scanAreaHeight = height * scanAreaSize
        val left = (width - scanAreaWidth) / 2
        val top = (height - scanAreaHeight) / 2

        // Main rectangle frame
        drawRect(
            color = guideColor,
            topLeft = Offset(left, top),
            size = Size(scanAreaWidth, scanAreaHeight),
            style = Stroke(width = guideThickness)
        )

        // Corner indicators
        val cornerSize = 20f

        // Top-left corner
        drawLine(
            color = guideColor,
            start = Offset(left, top),
            end = Offset(left + cornerSize, top),
            strokeWidth = guideThickness
        )
        drawLine(
            color = guideColor,
            start = Offset(left, top),
            end = Offset(left, top + cornerSize),
            strokeWidth = guideThickness
        )

        // Top-right corner
        drawLine(
            color = guideColor,
            start = Offset(left + scanAreaWidth, top),
            end = Offset(left + scanAreaWidth - cornerSize, top),
            strokeWidth = guideThickness
        )
        drawLine(
            color = guideColor,
            start = Offset(left + scanAreaWidth, top),
            end = Offset(left + scanAreaWidth, top + cornerSize),
            strokeWidth = guideThickness
        )

        // Bottom-left corner
        drawLine(
            color = guideColor,
            start = Offset(left, top + scanAreaHeight),
            end = Offset(left + cornerSize, top + scanAreaHeight),
            strokeWidth = guideThickness
        )
        drawLine(
            color = guideColor,
            start = Offset(left, top + scanAreaHeight),
            end = Offset(left, top + scanAreaHeight - cornerSize),
            strokeWidth = guideThickness
        )

        // Bottom-right corner
        drawLine(
            color = guideColor,
            start = Offset(left + scanAreaWidth, top + scanAreaHeight),
            end = Offset(left + scanAreaWidth - cornerSize, top + scanAreaHeight),
            strokeWidth = guideThickness
        )
        drawLine(
            color = guideColor,
            start = Offset(left + scanAreaWidth, top + scanAreaHeight),
            end = Offset(left + scanAreaWidth, top + scanAreaHeight - cornerSize),
            strokeWidth = guideThickness
        )

        // Horizontal guide line
        drawLine(
            color = guideColor.copy(alpha = 0.4f),
            start = Offset(left, top + scanAreaHeight / 2),
            end = Offset(left + scanAreaWidth, top + scanAreaHeight / 2),
            strokeWidth = guideThickness / 2,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )
    }
}

@Composable
fun EnhancedCameraPreview(
    onImageCaptured: (ImageProxy, String?) -> Unit,
    onError: (ImageCaptureException) -> Unit,
) {
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var preview: Preview? by remember { mutableStateOf(null) }
    var camera: Camera? by remember { mutableStateOf(null) }
    val lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    val executor = ContextCompat.getMainExecutor(context)

    // Weight detection state
    var detectedNumber by remember { mutableStateOf<String?>(null) }
    var isValidWeight by remember { mutableStateOf(false) }
    var processingActive by remember { mutableStateOf(true) }

    // AI Analysis state
    var isAIAnalyzing by remember { mutableStateOf(false) }
    var analysisSource by remember { mutableStateOf<String?>(null) }

    // Camera status
    var hasTorch by remember { mutableStateOf(false) }
    var isTorchOn by remember { mutableStateOf(false) }

    // Scanner guide parameters
    val guideColor = Color.Green.copy(alpha = 0.7f)
    val guideThickness = 2.dp
    val scanAreaSize = 0.7f // 70% of screen width

    Box(modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(1f)
    ) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    this.scaleType = PreviewView.ScaleType.FILL_CENTER
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    preview = Preview.Builder()
                        .setTargetRotation(previewView.display.rotation)
                        .setTargetResolution(android.util.Size(1440, 1080)) // Using explicit resolution instead of aspect ratio
                        .build()
                        .also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }

                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(lensFacing)
                        .build()

                    imageCapture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setTargetResolution(android.util.Size(1440, 1080)) // Using 4:3 ratio with explicit resolution
                        .build()

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setTargetResolution(android.util.Size(1280, 960)) // 4:3 aspect ratio using explicit resolution
                        .build()
                        .apply {
                            setAnalyzer(executor, EnhancedNumberAnalyzer(
                                context = context,
                                onNumbersDetected = { extractedNumbers, bestEstimate ->
                                    if (processingActive) {
                                        if (bestEstimate.isNotEmpty()) {
                                            detectedNumber = bestEstimate
                                            val weight = bestEstimate.toDoubleOrNull()
                                            isValidWeight = weight != null && weight in 1000.0..60000.0
                                            analysisSource = "ML_KIT"
                                        } else {
                                            detectedNumber = null
                                            analysisSource = null
                                        }
                                    }
                                },
                                onAnalysisStateChanged = { isAnalyzing ->
                                    isAIAnalyzing = isAnalyzing
                                }
                            ))
                        }

                    try {
                        cameraProvider.unbindAll()
                        camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture,
                            imageAnalysis
                        )
                        hasTorch = camera?.cameraInfo?.hasFlashUnit() == true
                    } catch (exc: Exception) {
                        exc.printStackTrace()
                    }
                }, executor)
                previewView
            },
            modifier = Modifier.matchParentSize()
        )

        // Scanner guide overlay
        ScannerGuideOverlay(
            scanAreaSize = scanAreaSize,
            guideColor = guideColor,
            guideThickness = guideThickness.value
        )

        // Header با اطلاعات حالت اسکن
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.Black.copy(alpha = 0.8f),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val successColor = ATKCargoTheme.semanticColors.success
                val errorColor = MaterialTheme.colorScheme.error
                val infoColor = ATKCargoTheme.semanticColors.info

                // نمایش حالت اسکن فعال
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(ATKCargoTheme.spacing.s)
                ) {
                    val (icon, title, color) = Triple("📱", "اسکن داخلی", successColor)

                    Text(
                        text = icon,
                        fontSize = 20.sp
                    )
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (isAIAnalyzing) {
                        androidx.compose.material3.CircularProgressIndicator(
                            color = color,
                            modifier = Modifier.size(ATKCargoTheme.dimensions.iconSmall),
                            strokeWidth = ATKCargoTheme.dimensions.borderWidthMedium
                        )
                    }
                }

                Text(
                    text = "قبض باسکول را در کادر قرار دهید - پردازش سریع",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = ATKCargoTheme.spacing.xs)
                )
            }
        }

        // Display detected weight با انیمیشن نرم
        AnimatedVisibility(
            visible = detectedNumber != null,
            enter = fadeIn(animationSpec = tween(400)) + slideInVertically(
                initialOffsetY = { -it },
                animationSpec = tween(400)
            ),
            exit = fadeOut(animationSpec = tween(300)) + slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = tween(300)
            )
        ) {
            detectedNumber?.let { number ->
                val successColor = ATKCargoTheme.semanticColors.success
                val errorColor = MaterialTheme.colorScheme.error
                Surface(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(ATKCargoTheme.spacing.l),
                    shape = ATKCargoTheme.appShapes.large,
                    color = if (isValidWeight)
                        successColor.copy(alpha = 0.95f)
                    else
                        errorColor.copy(alpha = 0.95f),
                ) {
                    Column(
                        modifier = Modifier.padding(ATKCargoTheme.spacing.xl),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(ATKCargoTheme.spacing.s)
                        ) {
                            Icon(
                                imageVector = if (isValidWeight) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${NumberFormat.getNumberInstance(Locale("en", "US")).format(number.toDoubleOrNull() ?: 0.0)} کیلوگرم",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        if (!isValidWeight && number.isNotEmpty()) {
                            Text(
                                text = "وزن باید بین 1,000 تا 60,000 کیلوگرم باشد",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // نمایش وضعیت تحلیل AI
        AnimatedVisibility(
            visible = isAIAnalyzing,
            enter = fadeIn(animationSpec = tween(300)) + expandIn(
                expandFrom = Alignment.Center,
                animationSpec = tween(300)
            ),
            exit = fadeOut(animationSpec = tween(300)) + shrinkOut(
                shrinkTowards = Alignment.Center,
                animationSpec = tween(300)
            )
        ) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp)
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.8f),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        color = Color(0xFF2196F3),
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "تحلیل هوشمند در حال انجام...",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Capture button
        var isCapturing by remember { mutableStateOf(false) }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .size(72.dp)
                .clickable(enabled = !isCapturing) {
                    if (detectedNumber == null) {
                        Toast.makeText(context, "هنوز عددی تشخیص داده نشده است!", Toast.LENGTH_SHORT).show()
                        return@clickable
                    }

                    processingActive = false
                    isCapturing = true

                    imageCapture?.takePicture(
                        executor,
                        object : ImageCapture.OnImageCapturedCallback() {
                            override fun onCaptureSuccess(image: ImageProxy) {
                                onImageCaptured(image, detectedNumber)
                                isCapturing = false
                            }

                            override fun onError(exception: ImageCaptureException) {
                                onError(exception)
                                isCapturing = false
                            }
                        }
                    )
                },
            shape = CircleShape,
            color = if (detectedNumber != null) Color.White.copy(alpha = 0.9f) else Color.Gray.copy(alpha = 0.5f),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                if (isCapturing) {
                    androidx.compose.material3.CircularProgressIndicator(
                        color = Color.Black,
                        modifier = Modifier.size(36.dp),
                        strokeWidth = 3.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Camera,
                        contentDescription = "گرفتن عکس",
                        tint = Color.Black,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        // Flashlight button
        if (hasTorch) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 24.dp)
                    .size(56.dp)
                    .clickable {
                        isTorchOn = !isTorchOn
                        camera?.cameraControl?.enableTorch(isTorchOn)
                    },
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.6f),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = if (isTorchOn) Icons.Default.FlashOff else Icons.Default.FlashOn,
                        contentDescription = "چراغ قوه",
                        tint = if (isTorchOn) Color.Yellow else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        onDispose {
            processingActive = false
        }
    }
}
