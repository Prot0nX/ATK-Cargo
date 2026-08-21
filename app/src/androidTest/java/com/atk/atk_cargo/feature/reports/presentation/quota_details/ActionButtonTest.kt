package com.atk.atk_cargo.feature.reports.presentation.quota_details

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

// تست Compose برای ActionButton که فقط از پارامترهای ورودی رندر می‌شود و هیچ state داخلی ندارد.
class ActionButtonTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun actionButton_displaysLabel() {
        composeTestRule.setContent {
            MaterialTheme {
                ActionButton(
                    icon = Icons.Default.Delete,
                    label = "حذف",
                    color = Color.Red,
                    onClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("حذف").assertExists()
    }

    @Test
    fun actionButton_click_invokesCallback() {
        var clicked = false

        composeTestRule.setContent {
            MaterialTheme {
                ActionButton(
                    icon = Icons.Default.Delete,
                    label = "حذف",
                    color = Color.Red,
                    onClick = { clicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText("حذف").performClick()

        assert(clicked) { "onClick باید بعد از کلیک روی ActionButton صدا زده شود" }
    }
}
