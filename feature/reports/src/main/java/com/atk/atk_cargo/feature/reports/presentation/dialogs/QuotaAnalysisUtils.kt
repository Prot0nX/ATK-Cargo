package com.atk.atk_cargo.feature.reports.presentation.dialogs

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

// اشتراک‌گذاری متن و فیلد جستجوی مشترک بخش تحلیل کوتاژها — از QuotaAnalysisSection.kt جدا شد (فاز۴ #۴۰)
// تابع مشترک برای گرفتن خطای ActivityNotFoundException در هر دو نقطه اشتراک‌گذاری، هم‌راستا با الگوی PDF در ReportsViewModel.kt
internal fun shareAnalyticsText(context: Context, text: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "ارسال اطلاعات")
    try {
        context.startActivity(shareIntent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "برنامه‌ای برای اشتراک‌گذاری یافت نشد.", Toast.LENGTH_SHORT).show()
    }
}

@Composable
internal fun SearchField(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "جستجو بر اساس نام کشتی، شماره...",
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Surface(
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(11.dp),
        color = AnalyticsMutedBg
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = AnalyticsMutedText,
                modifier = Modifier.size(18.dp)
            )

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (searchQuery.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodySmall,
                        color = AnalyticsMutedText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        color = AnalyticsTitleColor
                    ),
                    cursorBrush = SolidColor(AnalyticsAccent)
                )
            }

            if (searchQuery.isNotEmpty()) {
                IconButton(
                    onClick = { onSearchQueryChange("") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "پاک کردن",
                        tint = AnalyticsMutedText,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
