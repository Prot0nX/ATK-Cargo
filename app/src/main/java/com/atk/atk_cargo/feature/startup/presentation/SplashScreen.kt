package com.atk.atk_cargo.feature.startup.presentation

// ===== IMPORTS =====
import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.atk.atk_cargo.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// ===== CONFIGURATION =====
private object SplashScreenConstants {
    const val APP_TITLE = "سیستم مدیریت هوشمند بارگیری"
    const val VERSION_PREFIX = "نسخه"
    const val BRAND_NAME = "ATK Smart Cargo Management"

    /** تأخیر قبل از شروع انیمیشن حروف (ms) */
    const val BRAND_ANIMATION_START_DELAY = 600L

    /** تأخیر بین ظاهر شدن هر حرف (ms) */
    const val CHAR_STAGGER_DELAY = 42L

    /** مدت انیمیشن fadeIn/slideUp هر حرف (ms) */
    const val CHAR_REVEAL_DURATION = 280

    /** تأخیر قبل از شروع shimmer پس از اتمام حروف (ms) */
    const val SHIMMER_START_DELAY = 120L

    /** مدت انیمیشن shimmer sweep (ms) */
    const val SHIMMER_DURATION = 650

    /** تأخیر قبل از نمایش subtitle فارسی (ms) */
    const val SUBTITLE_FADE_DELAY = 200L

    /** مدت fadeIn عنوان فارسی و نسخه (ms) */
    const val SUBTITLE_FADE_DURATION = 500
}

// ===== COLORS =====
private val SplashBrandColor = Color(0xFF137FEC)
private val SplashBrandColorDark = Color(0xFF0B4F94)
private val ShimmerColor = Color.White.copy(alpha = 0.75f)

// ===== CORE SCREEN =====

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

    // انیمیشن fade-in برای subtitle فارسی و نسخه
    LaunchedEffect(Unit) {
        delay(SplashScreenConstants.BRAND_ANIMATION_START_DELAY +
                SplashScreenConstants.CHAR_STAGGER_DELAY * SplashScreenConstants.BRAND_NAME.length +
                SplashScreenConstants.SHIMMER_START_DELAY +
                SplashScreenConstants.SHIMMER_DURATION +
                SplashScreenConstants.SUBTITLE_FADE_DELAY)
        textAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = SplashScreenConstants.SUBTITLE_FADE_DURATION)
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

// ===== COMPONENTS =====

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
        // نام برند انگلیسی با انیمیشن حرف‌به‌حرف
        AnimatedBrandTitle(brandName = SplashScreenConstants.BRAND_NAME)

        Spacer(modifier = Modifier.height(14.dp))

        // زیرعنوان فارسی و نسخه با fade-in پس از اتمام انیمیشن برند
        Column(
            modifier = Modifier.alpha(textAlpha),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SplashSubtitleBadge()
            Spacer(modifier = Modifier.height(4.dp))
            SplashVersionBadge(appVersion)
        }
    }
}

/**
 * نمایش حرف‌به‌حرف نام برند با افکت slideUp + fadeIn برای هر کاراکتر،
 * و سپس یک shimmer sweep روی کل متن پس از ظاهر شدن همه حروف.
 */
@Composable
private fun AnimatedBrandTitle(brandName: String) {
    val characters = remember(brandName) { brandName.toList() }
    val totalChars = characters.size

    // هر حرف دو مقدار انیمیشن مستقل دارد: alpha و offsetY (به پیکسل)
    val alphaAnimatables = remember(totalChars) {
        List(totalChars) { Animatable(0f) }
    }
    val offsetAnimatables = remember(totalChars) {
        List(totalChars) { Animatable(24f) }
    }

    // shimmer: از ۰ (خارج از چپ) تا ۱ (خارج از راست) حرکت می‌کند
    val shimmerProgress = remember { Animatable(0f) }
    var shimmerActive by remember { mutableStateOf(false) }

    // اجرای انیمیشن staggered reveal
    LaunchedEffect(Unit) {
        delay(SplashScreenConstants.BRAND_ANIMATION_START_DELAY)

        characters.forEachIndexed { index, char ->
            // فاصله‌ها بدون انیمیشن ظاهر می‌شوند
            if (char == ' ') {
                alphaAnimatables[index].snapTo(1f)
                offsetAnimatables[index].snapTo(0f)
                delay(SplashScreenConstants.CHAR_STAGGER_DELAY)
                return@forEachIndexed
            }

            launch {
                alphaAnimatables[index].animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = SplashScreenConstants.CHAR_REVEAL_DURATION,
                        easing = FastOutSlowInEasing
                    )
                )
            }
            launch {
                offsetAnimatables[index].animateTo(
                    targetValue = 0f,
                    animationSpec = tween(
                        durationMillis = SplashScreenConstants.CHAR_REVEAL_DURATION,
                        easing = FastOutSlowInEasing
                    )
                )
            }
            delay(SplashScreenConstants.CHAR_STAGGER_DELAY)
        }

        // پس از نمایش همه حروف، shimmer را فعال کن
        delay(SplashScreenConstants.SHIMMER_START_DELAY)
        shimmerActive = true
        shimmerProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = SplashScreenConstants.SHIMMER_DURATION,
                easing = LinearEasing
            )
        )
    }

    // Row خارجی برای مرکز کردن افقی
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .drawWithContent {
                    drawContent()
                    // رسم shimmer sweep روی محتوا
                    if (shimmerActive) {
                        val sweepWidth = size.width * 0.35f
                        val centerX = shimmerProgress.value * (size.width + sweepWidth) - sweepWidth / 2f
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    ShimmerColor,
                                    Color.Transparent
                                ),
                                start = Offset(centerX - sweepWidth / 2f, 0f),
                                end = Offset(centerX + sweepWidth / 2f, 0f),
                                tileMode = TileMode.Clamp
                            ),
                            size = size
                        )
                    }
                }
        ) {
            characters.forEachIndexed { index, char ->
                Text(
                    text = char.toString(),
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Default,
                    color = Color.White,
                    letterSpacing = 1.8.sp,
                    modifier = Modifier
                        .alpha(alphaAnimatables[index].value)
                        .offset {
                            IntOffset(
                                x = 0,
                                y = offsetAnimatables[index].value.roundToInt()
                            )
                        }
                )
            }
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
