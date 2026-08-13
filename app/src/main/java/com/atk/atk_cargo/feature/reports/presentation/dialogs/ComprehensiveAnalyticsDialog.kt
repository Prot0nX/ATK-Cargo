package com.atk.atk_cargo.feature.reports.presentation.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.atk.atk_cargo.ui.viewmodel.ReportsViewModel

// internal (نه private) چون QuotaAnalysisSection.kt هم به این‌ها نیاز دارد
internal val AnalyticsAccent: Color
    @Composable get() = MaterialTheme.colorScheme.primary

internal val AnalyticsAccentBg: Color
    @Composable get() = MaterialTheme.colorScheme.primaryContainer

internal val AnalyticsAccentBorder: Color
    @Composable get() = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)

internal val AnalyticsCardBorder: Color
    @Composable get() = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

internal val AnalyticsMutedBg: Color
    @Composable get() = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

internal val AnalyticsMutedText: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

internal val AnalyticsTitleColor: Color
    @Composable get() = MaterialTheme.colorScheme.onSurface

internal val AnalyticsScreenBg: Color
    @Composable get() = MaterialTheme.colorScheme.surface

@Composable
fun ComprehensiveAnalyticsDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    viewModel: ReportsViewModel
) {
    val analyticsData by viewModel.comprehensiveAnalytics.collectAsState()
    val loadingState by viewModel.analyticsLoadingState.collectAsState()

    LaunchedEffect(isVisible) {
        if (isVisible) {
            viewModel.loadComprehensiveAnalytics()
        }
    }

    if (isVisible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = AnalyticsScreenBg
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(bottom = 12.dp)
                    ) {
                        AnalyticsHeaderCard(onClose = onDismiss)
                        AnalyticsDateNavigation(viewModel = viewModel)
                    }
                    HorizontalDivider(color = AnalyticsCardBorder.copy(alpha = 0.7f))

                    Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                        when (loadingState) {
                            // C-2: Idle فقط حالت اولیه‌ی قبل از اولین بارگذاری است؛ خودِ
                            // بارگذاری توسط LaunchedEffect(isVisible) بالا انجام می‌شود.
                            // این شاخه قبلاً هم یک LaunchedEffect(Unit) داشت که در اولین
                            // نمایش، همزمان با آن یکی اجرا می‌شد و دو درخواست شبکه‌ی
                            // همسان به سنگین‌ترین کوئری سرور می‌زد (B-2).
                            ReportsViewModel.LoadingState.Idle,
                            is ReportsViewModel.LoadingState.Loading -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        CircularProgressIndicator(color = AnalyticsAccent)
                                        Text(
                                            text = "در حال بارگذاری تحلیل‌ها...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                            is ReportsViewModel.LoadingState.Error -> {
                                val error = (loadingState as ReportsViewModel.LoadingState.Error).message
                                ErrorStateCard(
                                    errorMessage = error,
                                    onRetry = { viewModel.loadComprehensiveAnalytics() }
                                )
                            }
                            ReportsViewModel.LoadingState.Success -> {
                                // C-1: QuotaAnalysis دیگر لیست را از اینجا نمی‌گیرد؛ خودش
                                // مستقیماً از ReportsViewModel.analyticsGroups (که از همان
                                // comprehensiveAnalytics مشتق شده) می‌خواند.
                                analyticsData?.quotaCompletionAnalysis?.let {
                                    QuotaAnalysis(viewModel = viewModel)
                                } ?: EmptyStateCard("داده‌ای برای کوتاژها یافت نشد")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalyticsDateNavigation(
    viewModel: ReportsViewModel
) {
    val offset by viewModel.analyticsDateOffset.collectAsState()
    val analytics by viewModel.comprehensiveAnalytics.collectAsState()
    val dateInfo = analytics?.dateInfo

    val dayName = dateInfo?.dayName ?: ""

    // B-1/B-8: پنجره واقعی «روز کاری» است (دیروز ۰۷:۰۰ تا امروز ۰۷:۰۰)، نه لحظه
    // حاضر؛ به‌جای نمایش فقط تاریخ پایان، بازه کامل نشان داده می‌شود تا کاربر
    // متوجه شود چه بخشی از امروز هنوز در این گزارش نیست.
    val windowStart = dateInfo?.windowStartDate
    val windowEnd = dateInfo?.windowEndDate
    val windowLabel = if (windowStart != null && windowEnd != null) {
        "${dateInfo.windowStartTime ?: "07:00"} $windowStart  ←  ${dateInfo.windowEndTime ?: "07:00"} $windowEnd"
    } else {
        dateInfo?.jalaliDate ?: ""
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .padding(top = 12.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(AnalyticsMutedBg)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { viewModel.setAnalyticsDateOffset(offset + 1) },
            enabled = offset < 0,
            modifier = Modifier.size(26.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "روز بعد",
                tint = if (offset < 0) AnalyticsAccent else AnalyticsMutedText.copy(alpha = 0.4f),
                modifier = Modifier.size(16.dp)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = if (offset == 0) "روز کاری اخیر" else if (offset == -1) "دیروز" else dayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AnalyticsTitleColor
                )
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = AnalyticsMutedText,
                    modifier = Modifier.size(12.dp)
                )
            }
            if (windowLabel.isNotEmpty()) {
                Text(
                    text = windowLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = AnalyticsMutedText
                )
            }
        }

        IconButton(
            onClick = { viewModel.setAnalyticsDateOffset(offset - 1) },
            enabled = offset > -ReportsViewModel.ANALYTICS_MAX_DAYS_BACK,
            modifier = Modifier.size(26.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "روز قبل",
                tint = if (offset > -ReportsViewModel.ANALYTICS_MAX_DAYS_BACK) AnalyticsAccent else AnalyticsMutedText.copy(alpha = 0.4f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun AnalyticsHeaderCard(
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(AnalyticsMutedBg)
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "بستن",
                tint = AnalyticsTitleColor,
                modifier = Modifier.size(16.dp)
            )
        }

        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(AnalyticsAccentBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Analytics,
                contentDescription = null,
                tint = AnalyticsAccent,
                modifier = Modifier.size(16.dp)
            )
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = "تحلیل جامع عملیات",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AnalyticsTitleColor
            )
            Text(
                text = "گزارشات روز کاری (۰۷:۰۰ تا ۰۷:۰۰)",
                style = MaterialTheme.typography.labelSmall,
                color = AnalyticsMutedText
            )
        }
    }
}

@Composable
private fun ErrorStateCard(
    errorMessage: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "خطا در بارگذاری داده‌ها",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
            // B-9: تنها راه قبلی برای تلاش مجدد، بستن و باز کردن دوباره دیالوگ
            // یا تغییر تاریخ بود؛ در محیط بندری با شبکه ناپایدار این یک شکاف
            // UX واقعی است.
            if (onRetry != null) {
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onErrorContainer,
                        contentColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "تلاش مجدد",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
internal fun EmptyStateCard(
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Inbox,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "داده‌ای موجود نیست",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
