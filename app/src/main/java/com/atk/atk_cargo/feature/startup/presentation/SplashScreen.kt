package com.atk.atk_cargo.feature.startup.presentation

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.atk.atk_cargo.R
import kotlinx.coroutines.delay

private object SplashScreenConstants {
    const val APP_TITLE = "سیستم مدیریت هوشمند بارگیری"
    const val VERSION_PREFIX = "نسخه"
}

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun SplashScreen(onSkip: () -> Unit) {
    val textAlpha = remember { Animatable(0f) }
    val screenAlpha = remember { Animatable(1f) }
    val context = LocalContext.current
    val appVersion = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (_: Exception) {
            "نامشخص"
        }
    }

    var lastClickTime by remember { mutableLongStateOf(0L) }
    var isSkipped by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(800)
        textAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
    }

    LaunchedEffect(isSkipped) {
        if (isSkipped) {
            screenAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = 400,
                    easing = FastOutSlowInEasing
                )
            )
            delay(50)
            onSkip()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(screenAlpha.value)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(SplashBrandColor, SplashBrandColorDark)
                )
            )
            .pointerInput(Unit) {
                detectTapGestures { _ ->
                    if (!isSkipped) {
                        val currentTime = System.currentTimeMillis()
                        if (lastClickTime > 0 && currentTime - lastClickTime <= 500) {
                            isSkipped = true
                        }
                        lastClickTime = currentTime
                    }
                }
            }
    ) {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_logo))

        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 32.dp)
        )

        SplashScreenContent(
            textAlpha = textAlpha.value,
            appVersion = appVersion ?: "نامشخص"
        )
    }
}

private val SplashBrandColor = Color(0xFF137FEC)
private val SplashBrandColorDark = Color(0xFF0B4F94)

@Composable
private fun SplashScreenContent(
    textAlpha: Float,
    appVersion: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 48.dp, start = 24.dp, end = 24.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .alpha(textAlpha),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SplashSubtitleBadge()
            Spacer(modifier = Modifier.height(8.dp))
            SplashVersionBadge(appVersion)
        }
    }
}

@Composable
private fun SplashSubtitleBadge() {
    Text(
        text = SplashScreenConstants.APP_TITLE,
        style = MaterialTheme.typography.bodyMedium,
        color = Color.White.copy(alpha = 0.9f),
        modifier = Modifier
            .background(
                color = Color.White.copy(alpha = 0.15f),
                shape = CircleShape
            )
            .padding(horizontal = 16.dp, vertical = 4.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun SplashVersionBadge(appVersion: String) {
    Text(
        text = "${SplashScreenConstants.VERSION_PREFIX} $appVersion",
        style = MaterialTheme.typography.labelMedium,
        color = Color.White.copy(alpha = 0.5f),
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.5.sp
    )
}
