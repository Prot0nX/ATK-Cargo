package com.atk.atk_cargo.feature.chat.presentation.components

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColorInt
import com.atk.atk_cargo.data.ColorWheel
import com.atk.atk_cargo.feature.chat.domain.getAdaptiveBubbleColor
import com.atk.atk_cargo.feature.chat.domain.getChatBackgroundColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(
    onBackClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onSettingsClick: () -> Unit,
    isLoading: Boolean
) {
    Surface(
        shadowElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "اطلاع‌رسانی و گفتگو",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isLoading) {
                        Text(
                            text = "در حال بروزرسانی...",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, "بازگشت", tint = MaterialTheme.colorScheme.onSurface)
                }
            },
            actions = {
                IconButton(onClick = onRefreshClick) {
                    Icon(Icons.Default.Refresh, "بروزرسانی", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Default.Settings, "تنظیمات", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ChatSettingsDialogEnhanced(
    currentFontSize: Int,
    currentMyColor: Long,
    currentOtherColor: Long,
    currentBackgroundId: Int,
    currentBubbleShape: Int,
    onDismiss: () -> Unit,
    onSave: (Int, Long, Long, Int, Int) -> Unit
) {
    var fontSize by remember { mutableFloatStateOf(currentFontSize.toFloat()) }
    var myColor by remember { mutableLongStateOf(currentMyColor) }
    var otherColor by remember { mutableLongStateOf(currentOtherColor) }
    var backgroundId by remember { mutableIntStateOf(currentBackgroundId) }
    var bubbleShape by remember { mutableIntStateOf(currentBubbleShape) }
    
    var showAdvancedColorPicker by remember { mutableStateOf(false) }
    var advancedColorTarget by remember { mutableStateOf("MY") }

    val colorPalette = listOf(
        0xFF2196F3, 0xFF4CAF50, 0xFFE91E63, 0xFF9C27B0,
        0xFFFF9800, 0xFF607D8B, 0xFF212121, 0xFFFFFFFF,
        0xFF075E54, 0xFF128C7E, 0xFF25D366, 0xFF34B7F1
    )

    if (showAdvancedColorPicker) {
        AdvancedColorPickerDialog(
            initialColor = if (advancedColorTarget == "MY") myColor else otherColor,
            onColorSelected = { selectedColor ->
                if (advancedColorTarget == "MY") {
                    myColor = selectedColor
                } else {
                    otherColor = selectedColor
                }
                showAdvancedColorPicker = false
            },
            onDismiss = { showAdvancedColorPicker = false }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "تنظیمات ظاهر گفتگو",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                Column(
                    modifier = Modifier
                        .heightIn(max = 500.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "پیش‌نمایش زنده",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    ChatSettingsPreviewRefined(
                        fontSize = fontSize.toInt(),
                        myColor = Color(myColor),
                        otherColor = Color(otherColor),
                        backgroundId = backgroundId,
                        bubbleShapeId = bubbleShape
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    SettingSectionRefined(title = "اندازه متن") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("-A", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
                            Slider(
                                value = fontSize,
                                onValueChange = { fontSize = it },
                                valueRange = 12f..26f,
                                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                            )
                            Text("+A", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    SettingSectionRefined(title = "رنگ پیام‌های من", icon = Icons.Default.Person) {
                        LazyRowColorsRefined(
                            colors = colorPalette,
                            selectedColor = myColor,
                            onSelect = { myColor = it },
                            onCustomClick = {
                                advancedColorTarget = "MY"
                                showAdvancedColorPicker = true
                            }
                        )
                    }

                    SettingSectionRefined(title = "رنگ پیام‌های دیگران", icon = Icons.Default.Person) {
                        LazyRowColorsRefined(
                            colors = colorPalette.reversed(),
                            selectedColor = otherColor,
                            onSelect = { otherColor = it },
                            onCustomClick = {
                                advancedColorTarget = "OTHER"
                                showAdvancedColorPicker = true
                            }
                        )
                    }

                    SettingSectionRefined(title = "پس‌زمینه گفتگو") {
                        val backgroundOptions = listOf(
                            0 to Color(0xFFF8FAFC),
                            1 to Color(0xFFECE5DD),
                            2 to Color(0xFF202C33),
                            3 to Color(0xFF000000)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            reverseLayout = true
                        ) {
                            items(backgroundOptions) { (id, color) ->
                                Box(
                                    modifier = Modifier
                                        .size(60.dp, 40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(color)
                                        .border(
                                            width = if (backgroundId == id) 2.dp else 1.dp,
                                            color = if (backgroundId == id) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { backgroundId = id },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (backgroundId == id) {
                                        Icon(Icons.Default.Check, null, tint = if (ColorUtils.calculateLuminance(color.toArgb()) > 0.5) Color.Black else Color.White, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    SettingSectionRefined(title = "حالت نمایش پیام‌ها") {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val shapes = listOf("کلاسیک" to 0, "مربعی" to 1, "مدرن" to 2)
                            shapes.forEach { (label, id) ->
                                ShapeCardRefined(
                                    selected = bubbleShape == id,
                                    label = label,
                                    shapeId = id,
                                    onClick = { bubbleShape = id },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("انصراف", style = MaterialTheme.typography.labelLarge)
                    }

                    Button(
                        onClick = { onSave(fontSize.toInt(), myColor, otherColor, backgroundId, bubbleShape) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            "ذخیره تنظیمات",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingSectionRefined(
    title: String,
    icon: ImageVector? = null,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (icon != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        content()
    }
}

@Composable
fun ChatSettingsPreviewRefined(
    fontSize: Int,
    myColor: Color,
    otherColor: Color,
    backgroundId: Int,
    bubbleShapeId: Int
) {
    val backgroundColor by animateColorAsState(
        targetValue = getChatBackgroundColor(backgroundId),
        label = "backgroundColor"
    )

    val animatedFontSize by animateFloatAsState(targetValue = fontSize.toFloat(), label = "fontSize")
    val animatedMyColor by animateColorAsState(targetValue = myColor, label = "myColor")
    val animatedOtherColor by animateColorAsState(targetValue = otherColor, label = "otherColor")

    val myBubbleShape = when (bubbleShapeId) {
        0 -> RoundedCornerShape(topStart = 16.dp, topEnd = 0.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
        1 -> RoundedCornerShape(4.dp)
        2 -> RoundedCornerShape(topStart = 20.dp, topEnd = 2.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
        else -> RoundedCornerShape(16.dp)
    }
    
    val otherBubbleShape = when (bubbleShapeId) {
        0 -> RoundedCornerShape(topStart = 0.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
        1 -> RoundedCornerShape(4.dp)
        2 -> RoundedCornerShape(topStart = 2.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
        else -> RoundedCornerShape(16.dp)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            val adaptiveOtherColor = getAdaptiveBubbleColor(animatedOtherColor, false)
            val otherTextColor = if (ColorUtils.calculateLuminance(adaptiveOtherColor.toArgb()) > 0.5) Color.Black else Color.White
            Surface(
                color = adaptiveOtherColor,
                shape = otherBubbleShape,
                modifier = Modifier.align(Alignment.Start)
            ) {
                Text(
                    "چطوری؟ طرح جدید رو دیدی؟",
                    fontSize = animatedFontSize.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = otherTextColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val adaptiveMyColor = getAdaptiveBubbleColor(animatedMyColor, true)
            val myTextColor = if (ColorUtils.calculateLuminance(adaptiveMyColor.toArgb()) > 0.5) Color.Black else Color.White
            Surface(
                color = adaptiveMyColor,
                shape = myBubbleShape,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    "آره، عالی و مینیمال شده! 😍",
                    fontSize = animatedFontSize.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = myTextColor
                )
            }
        }
    }
}

@Composable
fun LazyRowColorsRefined(
    colors: List<Long>, 
    selectedColor: Long, 
    onSelect: (Long) -> Unit, 
    onCustomClick: () -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth(),
        reverseLayout = true
    ) {
        item {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, Color.LightGray.copy(alpha = 0.5f), CircleShape)
                    .clickable(onClick = onCustomClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = "Custom", modifier = Modifier.size(20.dp))
            }
        }

        items(colors) { colorLong ->
            val color = Color(colorLong)
            val isSelected = selectedColor == colorLong
            
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable { onSelect(colorLong) },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(if (ColorUtils.calculateLuminance(color.toArgb()) > 0.5) Color.Black else Color.White, CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
fun ShapeCardRefined(
    selected: Boolean, 
    label: String, 
    shapeId: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val previewShape = when (shapeId) {
        0 -> RoundedCornerShape(topStart = 8.dp, topEnd = 1.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
        1 -> RoundedCornerShape(2.dp)
        2 -> RoundedCornerShape(topStart = 10.dp, topEnd = 2.dp, bottomStart = 10.dp, bottomEnd = 10.dp)
        else -> RoundedCornerShape(8.dp)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(
                    if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else Color.Transparent,
                    RoundedCornerShape(12.dp)
                )
                .border(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp, 18.dp)
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        previewShape
                    )
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label, 
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ErrorView(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("تلاش مجدد")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedColorPickerDialog(
    hexStringInitial: String = "#FFFFFF",
    initialColor: Long,
    onColorSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var hexString by remember { mutableStateOf(String.format("#%06X", (0xFFFFFF and initialColor.toInt()))) }
    val initialC = Color(initialColor)
    var currentColor by remember { mutableStateOf(initialC) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("انتخاب پیشرفته رنگ", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .padding(16.dp)
                ) {
                    ColorWheel(
                        modifier = Modifier.fillMaxSize(),
                        initialColor = currentColor,
                        onColorChanged = {
                            currentColor = it
                            hexString = String.format("#%06X", (0xFFFFFF and it.toArgb()))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(currentColor, CircleShape)
                            .border(1.dp, Color.Gray, CircleShape)
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    OutlinedTextField(
                        value = hexString,
                        onValueChange = { 
                            hexString = it
                            try {
                                val colorInt = it.toColorInt()
                                currentColor = Color(colorInt)
                            } catch (_: Exception) {
                            }
                        },
                        label = { Text("کد HEX") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onColorSelected(currentColor.toArgb().toLong()) },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("تایید")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("لغو")
            }
        }
    )
}
