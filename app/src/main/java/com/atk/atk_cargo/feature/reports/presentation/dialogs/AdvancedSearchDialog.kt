package com.atk.atk_cargo.feature.reports.presentation.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

private val SearchAccent: Color
    @Composable get() = MaterialTheme.colorScheme.primary

private val SearchOnAccent: Color
    @Composable get() = MaterialTheme.colorScheme.onPrimary

private val SearchAccentBg: Color
    @Composable get() = MaterialTheme.colorScheme.primaryContainer

private val SearchMutedBg: Color
    @Composable get() = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

private val SearchMutedText: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

private val SearchTitleColor: Color
    @Composable get() = MaterialTheme.colorScheme.onSurface

private val SearchModalGradientTop: Color
    @Composable get() = MaterialTheme.colorScheme.surface

private val SearchModalGradientBottom: Color
    @Composable get() = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else Color(0xFFF2FAF8)

@Composable
fun AdvancedSearchDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onSearchReceipt: (String) -> Unit,
    onSearchTracking: (String) -> Unit
) {
    var searchNumber by remember { mutableStateOf("") }
    var selectedSearchType by remember { mutableStateOf(SearchType.RECEIPT_NUMBER) }

    if (isOpen) {
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
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.46f),
                shape = RoundedCornerShape(20.dp),
                color = Color.Transparent
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(listOf(SearchModalGradientTop, SearchModalGradientBottom)),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                text = "جستجوی پیشرفته",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = SearchTitleColor
                            )
                            Text(
                                text = when (selectedSearchType) {
                                    SearchType.RECEIPT_NUMBER -> "جستجو بر اساس شماره قبض"
                                    SearchType.TRACKING_NUMBER -> "جستجو بر اساس شماره حواله"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = SearchMutedText
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(SearchAccentBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = SearchAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    SearchTypeTabRow(
                        selectedSearchType = selectedSearchType,
                        onSearchTypeSelected = {
                            selectedSearchType = it
                            searchNumber = ""
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(
                            text = when (selectedSearchType) {
                                SearchType.RECEIPT_NUMBER -> "شماره قبض باسکول"
                                SearchType.TRACKING_NUMBER -> "شماره حواله"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = SearchMutedText
                        )
                        OutlinedTextField(
                            value = searchNumber,
                            onValueChange = {
                                if (it.all { char -> char.isDigit() }) {
                                    searchNumber = it
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SearchAccent,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            ),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Right),
                            leadingIcon = {
                                Icon(
                                    imageVector = when (selectedSearchType) {
                                        SearchType.RECEIPT_NUMBER -> Icons.Default.Receipt
                                        SearchType.TRACKING_NUMBER -> Icons.Default.Numbers
                                    },
                                    contentDescription = null,
                                    tint = SearchAccent
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Search
                            ),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    if (searchNumber.isNotBlank()) {
                                        when (selectedSearchType) {
                                            SearchType.RECEIPT_NUMBER -> onSearchReceipt(searchNumber)
                                            SearchType.TRACKING_NUMBER -> onSearchTracking(searchNumber)
                                        }
                                    }
                                }
                            ),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(13.dp))
                                .background(SearchMutedBg)
                                .clickable(onClick = onDismiss)
                                .padding(vertical = 13.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("انصراف", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = SearchMutedText)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1.3f)
                                .clip(RoundedCornerShape(13.dp))
                                .background(if (searchNumber.isNotBlank()) SearchAccent else SearchAccent.copy(alpha = 0.4f))
                                .clickable(enabled = searchNumber.isNotBlank()) {
                                    when (selectedSearchType) {
                                        SearchType.RECEIPT_NUMBER -> onSearchReceipt(searchNumber)
                                        SearchType.TRACKING_NUMBER -> onSearchTracking(searchNumber)
                                    }
                                }
                                .padding(vertical = 13.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("جستجو", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SearchOnAccent)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchTypeTabRow(
    selectedSearchType: SearchType,
    onSearchTypeSelected: (SearchType) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = remember {
        listOf(
            SearchTabInfo(SearchType.RECEIPT_NUMBER, "شماره قبض", Icons.Default.Receipt),
            SearchTabInfo(SearchType.TRACKING_NUMBER, "شماره حواله", Icons.Default.Numbers)
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SearchMutedBg)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tabs.forEach { tab ->
            val isSelected = selectedSearchType == tab.type

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                    .clickable { onSearchTypeSelected(tab.type) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = null,
                        tint = if (isSelected) SearchAccent else SearchMutedText,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) SearchAccent else SearchMutedText,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

private data class SearchTabInfo(
    val type: SearchType,
    val label: String,
    val icon: ImageVector
)
