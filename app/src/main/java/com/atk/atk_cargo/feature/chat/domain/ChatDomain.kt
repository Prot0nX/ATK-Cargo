package com.atk.atk_cargo.feature.chat.domain

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import com.atk.atk_cargo.data.db.ChatMessageEntity
import com.atk.atk_cargo.ui.theme.BackgroundDark
import com.atk.atk_cargo.ui.theme.BackgroundLight

sealed class ChatUiItem {
    abstract val id: String
    data class Header(val date: String, override val id: String) : ChatUiItem()
    data class Message(val entity: ChatMessageEntity) : ChatUiItem() {
        override val id: String = entity.id.toString()
    }
}

data class ShipInfoModel(
    val shipName: String,
    val kotazh: String,
    val warehouse: String,
    val owner: String,
    val shippingCompany: String
)

@Composable
fun getChatBackgroundColor(backgroundId: Int): Color {
    val isDark = isSystemInDarkTheme()
    return when (backgroundId) {
        0 -> if (isDark) BackgroundDark else BackgroundLight
        1 -> Color(0xFFECE5DD) // WhatsApp Light
        2 -> Color(0xFF202C33) // Dark Gray/Blue
        3 -> Color(0xFF000000) // Pure Black
        else -> if (isDark) BackgroundDark else BackgroundLight
    }
}

@Composable
fun getAdaptiveBubbleColor(baseColor: Color, isMe: Boolean): Color {
    val isDark = isSystemInDarkTheme()
    if (!isDark) return baseColor

    val luminance = ColorUtils.calculateLuminance(baseColor.toArgb())
    
    return if (isMe) {
        if (luminance > 0.7) {
            baseColor.copy(alpha = 0.85f)
        } else {
            baseColor
        }
    } else {
        if (luminance > 0.85) {
            Color(0xFF202C33)
        } else {
            baseColor
        }
    }
}

fun getDateHeaderColor(isDarkTheme: Boolean): Color {
    return if (isDarkTheme) Color(0xFF1F1F1F) else Color(0xFFE1F5FE)
}

fun Modifier.rotateIcon(degrees: Float) = this.then(
    Modifier.graphicsLayer(rotationZ = degrees)
)

fun extractShipInfoAndText(message: String): Pair<String, ShipInfoModel?> {
    val lines = message.lines()
    val shipInfoLines = mutableListOf<String>()
    val textLines = mutableListOf<String>()

    lines.forEach { line ->
        if (line.contains("کشتی:") || 
            line.contains("کوتاژ:") || 
            line.contains("انبار:") || 
            line.contains("صاحب کالا:") || 
            line.contains("باربری:")
        ) {
            shipInfoLines.add(line)
        } else {
            textLines.add(line)
        }
    }

    var shipInfo: ShipInfoModel? = null
    val shipLine = shipInfoLines.find { it.contains("کشتی:") }
    val kotazhLine = shipInfoLines.find { it.contains("کوتاژ:") }
    val warehouseLine = shipInfoLines.find { it.contains("انبار:") }
    val ownerLine = shipInfoLines.find { it.contains("صاحب کالا:") }
    val companyLine = shipInfoLines.find { it.contains("باربری:") }

    if (shipLine != null && kotazhLine != null) {
        shipInfo = ShipInfoModel(
            shipName = shipLine.substringAfter("کشتی:").trim(),
            kotazh = kotazhLine.substringAfter("کوتاژ:").trim(),
            warehouse = warehouseLine?.substringAfter("انبار:")?.trim() ?: "-",
            owner = ownerLine?.substringAfter("صاحب کالا:")?.trim() ?: "-",
            shippingCompany = companyLine?.substringAfter("باربری:")?.trim() ?: "-"
        )
    }

    val cleanText = textLines.joinToString("\n").trim()
    
    if (shipInfo == null) {
        return Pair(message, null)
    }

    return Pair(cleanText, shipInfo)
}

fun replaceUsernamesWithFullNames(text: String, users: List<com.atk.atk_cargo.api.User>): String {
    var result = text
    users.forEach { user ->
        if (result.contains("@${user.username}")) {
            val fullName = user.fullName ?: user.username
            result = result.replace("@${user.username}", "@$fullName")
        }
    }
    return result
}
