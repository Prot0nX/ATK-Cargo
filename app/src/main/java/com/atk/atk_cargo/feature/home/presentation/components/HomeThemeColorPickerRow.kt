package com.atk.atk_cargo.feature.home.presentation.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atk.atk_cargo.api.UserPreferencesManager
import com.atk.atk_cargo.ui.theme.ThemeBlue
import com.atk.atk_cargo.ui.theme.ThemeBlueDark
import com.atk.atk_cargo.ui.theme.ThemeBlueOcean
import com.atk.atk_cargo.ui.theme.ThemeGold
import com.atk.atk_cargo.ui.theme.ThemeGreen
import com.atk.atk_cargo.ui.theme.ThemeGreenDark
import com.atk.atk_cargo.ui.theme.ThemeGreenTeal
import com.atk.atk_cargo.ui.theme.ThemeOlive
import com.atk.atk_cargo.ui.theme.ThemeOrange
import com.atk.atk_cargo.ui.theme.ThemePink
import com.atk.atk_cargo.ui.theme.ThemePurple
import com.atk.atk_cargo.ui.theme.ThemePurpleDark
import com.atk.atk_cargo.ui.theme.ThemeRed
import com.atk.atk_cargo.ui.theme.ThemeRedDark
import com.atk.atk_cargo.ui.theme.ThemeSlateBlue
import com.atk.atk_cargo.ui.theme.ThemeTeal
import kotlinx.coroutines.launch

private val PickerAccent = Color(0xFF0D9488)
private val PickerAccentBg = Color(0xFFDCEFEA)
private val PickerCardBorder = Color(0xFFE4E6E9)
private val PickerMutedBg = Color(0xFFF3F4F5)
private val PickerMutedText = Color(0xFF8A8F98)
private val PickerTitleColor = Color(0xFF1F2937)

private data class ThemeColorOption(
    val color: Color,
    val colorLong: Long,
    val label: String
)

private val themeColorOptions = listOf(
    ThemeColorOption(ThemeBlue,       0xFF137fecL, "آبی"),
    ThemeColorOption(ThemeBlueDark,   0xFF1976D2L, "آبی تیره"),
    ThemeColorOption(ThemeBlueOcean,  0xFF0288D1L, "آبی اقیانوس"),
    ThemeColorOption(ThemeTeal,       0xFF0097A7L, "فیروزه"),
    ThemeColorOption(ThemeGreen,      0xFF10b981L, "سبز"),
    ThemeColorOption(ThemeGreenDark,  0xFF388E3CL, "سبز تیره"),
    ThemeColorOption(ThemeGreenTeal,  0xFF00796BL, "سبز آبی"),
    ThemeColorOption(ThemeOlive,      0xFF689F38L, "زیتونی"),
    ThemeColorOption(ThemePurple,     0xFF8B5CF6L, "بنفش"),
    ThemeColorOption(ThemePurpleDark, 0xFF7B1FA2L, "بنفش تیره"),
    ThemeColorOption(ThemePink,       0xFFE91E63L, "صورتی"),
    ThemeColorOption(ThemeOrange,     0xFFE64A19L, "نارنجی"),
    ThemeColorOption(ThemeRed,        0xFFEF4444L, "قرمز"),
    ThemeColorOption(ThemeRedDark,    0xFFC62828L, "قرمز تیره"),
    ThemeColorOption(ThemeGold,       0xFFFFA000L, "طلایی"),
    ThemeColorOption(ThemeSlateBlue,  0xFF455A64L, "خاکستری آبی")
)

@Composable
fun ThemeColorPickerRow(
    userPreferencesManager: UserPreferencesManager
) {
    val currentColorLong by userPreferencesManager.themeColor.collectAsState(initial = 0xFF137fecL)
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(PickerMutedBg)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PickerAccentBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = PickerAccent
                )
            }
            Column {
                Text(
                    text = "رنگ اصلی برنامه",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = PickerTitleColor
                )
                Text(
                    text = "تم رنگی رابط کاربری",
                    style = MaterialTheme.typography.labelSmall,
                    color = PickerMutedText
                )
            }
        }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(themeColorOptions) { option ->
                val isSelected = currentColorLong == option.colorLong
                val sizeAnim by animateFloatAsState(
                    targetValue = if (isSelected) 40f else 34f,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label = "size_${option.label}"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(sizeAnim.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected)
                                    option.color
                                else
                                    option.color.copy(alpha = 0.75f)
                            )
                            .clickable {
                                coroutineScope.launch {
                                    userPreferencesManager.saveThemeColor(option.colorLong)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = option.label,
                                modifier = Modifier.size(18.dp),
                                tint = Color.White
                            )
                        }
                    }

                    Text(
                        text = option.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = if (isSelected)
                            option.color
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
