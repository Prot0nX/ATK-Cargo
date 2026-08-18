package com.atk.atk_cargo.feature.reports.presentation.dialogs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

/**
 * تست Compose برای یک composable بی‌حالت دیگر (DEEP_CODE_AUDIT.md #Phase4.9)
 * — CompactStatChip فقط یک مقدار متنی را با یک آیکون نمایش می‌دهد، بدون هیچ
 * state یا تعامل داخلی.
 */
class CompactStatChipTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun compactStatChip_displaysValue() {
        composeTestRule.setContent {
            MaterialTheme {
                CompactStatChip(
                    icon = Icons.Default.Scale,
                    value = "۱۲۰۰ تن",
                    color = Color.Blue
                )
            }
        }

        composeTestRule.onNodeWithText("۱۲۰۰ تن").assertExists()
    }
}
