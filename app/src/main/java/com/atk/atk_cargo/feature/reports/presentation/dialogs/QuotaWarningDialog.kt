package com.atk.atk_cargo.feature.reports.presentation.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.atk.atk_cargo.api.WarningStatus
import com.atk.atk_cargo.feature.reports.domain.format
import com.atk.atk_cargo.feature.reports.domain.formatWeightWithDetail
import com.atk.atk_cargo.feature.reports.presentation.quota_details.ActionButton
import com.atk.atk_cargo.ui.viewmodel.ReportsViewModel
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun QuotaWarningDialog(
    warnings: List<WarningStatus>,
    onDismiss: () -> Unit,
    viewModel: ReportsViewModel
) {
    val warningsPerPage = 3
    val groupedWarnings = warnings.chunked(warningsPerPage)
    val totalPages = groupedWarnings.size
    val pagerState = rememberPagerState(pageCount = { totalPages })
    var expandedCardId by remember { mutableStateOf<String?>(null) }
    var showBulkDeactivateConfirm by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = 200,
            easing = FastOutSlowInEasing
        ),
        label = "dialog scale"
    )
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = 150,
            easing = FastOutSlowInEasing
        ),
        label = "dialog alpha"
    )

    if (warnings.isEmpty()) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.70f)
                .padding(12.dp)
                .scale(scale)
                .alpha(alpha)
                .animateContentSize(
                    animationSpec = tween(
                        durationMillis = 250,
                        easing = FastOutSlowInEasing
                    )
                ),
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(16.dp)
            ) {
                DialogHeader(
                    currentPage = pagerState.currentPage,
                    totalPages = totalPages,
                    onClose = onDismiss
                )

                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                    ) { page ->
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(groupedWarnings[page]) { warning ->
                                QuotaCard(
                                    warning = warning,
                                    viewModel = viewModel,
                                    isExpanded = expandedCardId == warning.quotaNumber,
                                    onExpandChange = { shouldExpand ->
                                        expandedCardId = if (shouldExpand) warning.quotaNumber else null
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (totalPages > 1) {
                        PageNavigation(
                            pagerState = pagerState,
                            pageCount = totalPages
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Text("بستن")
                        }
                    }

                    Button(
                        onClick = { showBulkDeactivateConfirm = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Text("غیرفعال کردن")
                        }
                    }
                }
            }
        }
    }

    if (showBulkDeactivateConfirm) {
        AlertDialog(
            onDismissRequest = { showBulkDeactivateConfirm = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("غیرفعال کردن ${warnings.size} کوتاژ") },
            text = { Text("آیا از غیرفعال کردن تمام ${warnings.size} کوتاژ هشداردار اطمینان دارید؟ این عملیات قابل بازگشت نیست.") },
            confirmButton = {
                Button(
                    onClick = {
                        showBulkDeactivateConfirm = false
                        viewModel.deactivateQuotasInBulk(warnings.map { it.quotaId ?: 0 })
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("غیرفعال کردن")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showBulkDeactivateConfirm = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}

@Composable
private fun DialogHeader(
    currentPage: Int,
    totalPages: Int,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "هشدار درصد کوتاژ",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "بستن",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (totalPages > 1) {
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier.wrapContentWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = buildString {
                            append(currentPage + 1)
                            append(" از ")
                            append(totalPages)
                            append(" کوتاژ")
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
            )
        }
    }
}

@Composable
private fun PageNavigation(
    pagerState: PagerState,
    pageCount: Int
) {
    if (pageCount <= 1) return

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            repeat(pageCount) { page ->
                PageIndicatorDot(
                    isSelected = page == pagerState.currentPage,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(page)
                        }
                    }
                )
                if (page < pageCount - 1) {
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            NavigationButton(
                text = "قبلی",
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                enabled = pagerState.currentPage > 0,
                onClick = {
                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                }
            )

            NavigationButton(
                text = "بعدی",
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                enabled = pagerState.currentPage < pageCount - 1,
                onClick = {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
            )
        }
    }
}

@Composable
private fun PageIndicatorDot(
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
        },
        animationSpec = tween(
            durationMillis = 150,
            easing = FastOutSlowInEasing
        ),
        label = "dot color"
    )

    val size by animateFloatAsState(
        targetValue = if (isSelected) 10f else 8f,
        animationSpec = tween(
            durationMillis = 150,
            easing = FastOutSlowInEasing
        ),
        label = "dot size"
    )

    Box(
        modifier = Modifier
            .size(size.dp)
            .background(color, CircleShape)
            .clickable(onClick = onClick)
    )
}

@Composable
private fun NavigationButton(
    text: String,
    icon: ImageVector,
    enabled: Boolean,
    onClick: suspend () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    OutlinedButton(
        onClick = {
            coroutineScope.launch {
                onClick()
            }
        },
        enabled = enabled,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (enabled) {
                MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            }
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text)
    }
}

@Composable
private fun QuotaCard(
    warning: WarningStatus,
    viewModel: ReportsViewModel,
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit
) {
    var isPercentageRestrictionLoading by remember { mutableStateOf(false) }
    var isStatusToggleLoading by remember { mutableStateOf(false) }
    val mainColor = MaterialTheme.colorScheme.error
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 200,
                    easing = FastOutSlowInEasing
                )
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.05f)
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
        )
    ) {
        Column {
            QuotaCardHeader(
                shortQuotaNumber = warning.quotaNumber.takeLast(4),
                warning = warning,
                isExpanded = isExpanded,
                onExpandClick = { onExpandChange(!isExpanded) }
            )

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = tween(
                        durationMillis = 200,
                        easing = FastOutSlowInEasing
                    )
                ) + fadeIn(
                    animationSpec = tween(
                        durationMillis = 150,
                        easing = FastOutSlowInEasing
                    )
                ),
                exit = shrinkVertically(
                    animationSpec = tween(
                        durationMillis = 150,
                        easing = FastOutSlowInEasing
                    )
                ) + fadeOut(
                    animationSpec = tween(
                        durationMillis = 100,
                        easing = FastOutSlowInEasing
                    )
                )
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = 8.dp,
                        end = 8.dp,
                        bottom = 8.dp
                    )
                ) {
                    QuotaCardContent(warning = warning)

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = mainColor.copy(alpha = 0.1f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isPercentageRestrictionLoading) {
                            LoadingActionButton(
                                label = if (warning.isPercentageRestricted) "آزاد کردن درصد" else "محدود کردن درصد",
                                color = mainColor,
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            ActionButton(
                                icon = if (warning.isPercentageRestricted) {
                                    Icons.Default.ToggleOff
                                } else {
                                    Icons.Default.ToggleOn
                                },
                                label = if (warning.isPercentageRestricted) "آزاد کردن درصد" else "محدود کردن درصد",
                                color = mainColor,
                                onClick = {
                                    isPercentageRestrictionLoading = true
                                    viewModel.toggleQuotaPercentageRestriction(
                                        id = warning.quotaId ?: 0,
                                        isRestricted = !warning.isPercentageRestricted
                                    ) {
                                        isPercentageRestrictionLoading = false
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (isStatusToggleLoading) {
                            LoadingActionButton(
                                label = if (warning.isActive) "غیرفعال‌سازی کوتاژ" else "فعال‌سازی کوتاژ",
                                color = mainColor,
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            ActionButton(
                                icon = if (warning.isActive) {
                                    Icons.Default.Close
                                } else {
                                    Icons.Default.Check
                                },
                                label = if (warning.isActive) " غیرفعال‌سازی کوتاژ" else "فعال‌سازی کوتاژ",
                                color = mainColor,
                                onClick = {
                                    isStatusToggleLoading = true
                                    scope.launch {
                                        viewModel.toggleQuotaStatus(warning.quotaId ?: 0, warning.quotaNumber)
                                        isStatusToggleLoading = false
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuotaCardHeader(
    shortQuotaNumber: String,
    warning: WarningStatus,
    isExpanded: Boolean,
    onExpandClick: () -> Unit
) {
    val rotationState by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "expand icon rotation"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onExpandClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            QuotaNumberBadge(number = shortQuotaNumber)

            val diff = abs(warning.remainingTonnage - warning.percentageAmount)
            Column {
                Text(
                    text = formatWeightWithDetail(diff.toFloat()),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "اختلاف با درصد",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }

        Icon(
            imageVector = Icons.Default.ExpandMore,
            contentDescription = if (isExpanded) "بستن" else "بازکردن",
            modifier = Modifier.rotate(rotationState),
            tint = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun QuotaNumberBadge(number: String) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
        modifier = Modifier.size(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = number,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun QuotaCardContent(warning: WarningStatus) {
    Column(
        modifier = Modifier.padding(
            start = 8.dp,
            end = 8.dp,
            bottom = 8.dp
        )
    ) {
        HorizontalDivider(
            modifier = Modifier.padding(bottom = 8.dp),
            color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CompactInfoSection(
                title = "اطلاعات اصلی",
                items = listOf(
                    "شماره کوتاژ" to warning.quotaNumber,
                    "درصد تنظیم شده" to "${warning.percentage.format(2)}%"
                ),
                modifier = Modifier.weight(1f)
            )

            CompactInfoSection(
                title = "جزئیات تناژ",
                items = listOf(
                    "تناژ درصد" to formatWeightWithDetail(warning.percentageAmount.toFloat()),
                    "تناژ مانده" to formatWeightWithDetail(warning.remainingTonnage)
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CompactInfoSection(
    title: String,
    items: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.error.copy(alpha = 0.05f),
                RoundedCornerShape(8.dp)
            )
            .padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 2.dp)
        )

        items.forEach { (label, value) ->
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Normal
                    ),
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.error,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun LoadingActionButton(
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = color.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = color,
                strokeWidth = 2.dp
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
