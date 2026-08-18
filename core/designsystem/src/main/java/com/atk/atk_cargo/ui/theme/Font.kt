package com.atk.atk_cargo.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

object VazirmatnFontFamily {
    @Composable
    fun create(): FontFamily {
        val context = LocalContext.current
        return remember {
            FontFamily(
                // Thin (100)
                Font(
                    path = "fonts/main/Vazirmatn-Thin.ttf",
                    assetManager = context.assets,
                    weight = FontWeight(100),
                    style = FontStyle.Normal
                ),
                // ExtraLight (200)
                Font(
                    path = "fonts/main/Vazirmatn-ExtraLight.ttf",
                    assetManager = context.assets,
                    weight = FontWeight(200),
                    style = FontStyle.Normal
                ),
                // Light (300)
                Font(
                    path = "fonts/main/Vazirmatn-Light.ttf",
                    assetManager = context.assets,
                    weight = FontWeight(300),
                    style = FontStyle.Normal
                ),
                // Regular (400) - پیش‌فرض
                Font(
                    path = "fonts/main/Vazirmatn-Regular.ttf",
                    assetManager = context.assets,
                    weight = FontWeight(400),
                    style = FontStyle.Normal
                ),
                // Medium (500)
                Font(
                    path = "fonts/main/Vazirmatn-Medium.ttf",
                    assetManager = context.assets,
                    weight = FontWeight(500),
                    style = FontStyle.Normal
                ),
                // SemiBold (600)
                Font(
                    path = "fonts/main/Vazirmatn-SemiBold.ttf",
                    assetManager = context.assets,
                    weight = FontWeight(600),
                    style = FontStyle.Normal
                ),
                // Bold (700)
                Font(
                    path = "fonts/main/Vazirmatn-Bold.ttf",
                    assetManager = context.assets,
                    weight = FontWeight(700),
                    style = FontStyle.Normal
                ),
                // ExtraBold (800)
                Font(
                    path = "fonts/main/Vazirmatn-ExtraBold.ttf",
                    assetManager = context.assets,
                    weight = FontWeight(800),
                    style = FontStyle.Normal
                ),
                // Black (900)
                Font(
                    path = "fonts/main/Vazirmatn-Black.ttf",
                    assetManager = context.assets,
                    weight = FontWeight(900),
                    style = FontStyle.Normal
                )
            )
        }
    }
}

