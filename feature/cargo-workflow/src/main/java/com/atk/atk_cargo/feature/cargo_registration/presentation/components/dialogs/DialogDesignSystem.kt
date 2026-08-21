package com.atk.atk_cargo.feature.cargo_registration.presentation.components.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInBack
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

// زبان طراحی مشترک همه‌ی دیالوگ‌های صفحه ثبت/خروج حواله، برگرفته از الگوی دیالوگ «حواله تکراری»
internal val DialogShellCornerRadius = 24.dp
internal val DialogContentCornerRadius = 12.dp
internal val DialogButtonCornerRadius = 12.dp
internal val DialogButtonHeight = 48.dp
internal val DialogBadgeSize = 100.dp
internal val DialogBadgeIconSize = 48.dp

@Composable
internal fun rememberDialogEnterTransition() = remember {
    expandIn(
        expandFrom = Alignment.Center,
        animationSpec = tween(300, easing = EaseOutBack)
    ) + fadeIn(animationSpec = tween(300))
}

@Composable
internal fun rememberDialogExitTransition() = remember {
    shrinkOut(
        shrinkTowards = Alignment.Center,
        animationSpec = tween(300, easing = EaseInBack)
    ) + fadeOut(animationSpec = tween(300))
}

// پوسته‌ی مشترک دیالوگ: Surface گرد با انیمیشن ورود/خروج فنری و Column داخلی استاندارد
@Composable
internal fun StandardDialogShell(
    onDismissRequest: () -> Unit,
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = dismissOnBackPress,
            dismissOnClickOutside = dismissOnClickOutside,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(DialogShellCornerRadius)),
            shape = RoundedCornerShape(DialogShellCornerRadius),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            AnimatedVisibility(
                visible = true,
                enter = rememberDialogEnterTransition(),
                exit = rememberDialogExitTransition()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    content = content
                )
            }
        }
    }
}

/** نشان دایره‌ای رنگی بالای هر دیالوگ (همان الگوی آیکن Lottie دیالوگ «حواله تکراری»، برای آیکن ساده). */
@Composable
internal fun DialogBadge(icon: ImageVector, tint: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(DialogBadgeSize)
            .background(tint.copy(alpha = 0.1f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(DialogBadgeIconSize)
        )
    }
}

@Composable
internal fun DialogTitle(text: String, color: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = color
    )
}

/** کارت ثانویه‌ی محتوای اصلی (متن هشدار، اطلاعات کمکی، فرم و ...)، هم‌شکل با کارت پیام دیالوگ «حواله تکراری». */
@Composable
internal fun DialogContentCard(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceVariant,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    contentPadding: Modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(DialogContentCornerRadius),
        color = color,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = contentPadding, content = content)
    }
}

@Composable
internal fun DialogMessageText(
    message: String,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    textAlign: TextAlign = if (message.contains("\n")) TextAlign.Start else TextAlign.Justify
) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.Medium,
            textAlign = textAlign
        ),
        color = color,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
internal fun DialogButtonRow(
    primaryText: String,
    onPrimaryClick: () -> Unit,
    modifier: Modifier = Modifier,
    primaryEnabled: Boolean = true,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    primaryIcon: ImageVector? = null,
    primaryLoading: Boolean = false,
    secondaryText: String? = null,
    onSecondaryClick: (() -> Unit)? = null,
    secondaryEnabled: Boolean = true
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (secondaryText != null && onSecondaryClick != null) {
            OutlinedButton(
                onClick = onSecondaryClick,
                enabled = secondaryEnabled,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = RoundedCornerShape(DialogButtonCornerRadius),
                modifier = Modifier
                    .weight(1f)
                    .height(DialogButtonHeight)
            ) {
                Text(secondaryText, fontWeight = FontWeight.Bold)
            }
        }

        Button(
            onClick = onPrimaryClick,
            enabled = primaryEnabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryColor,
                contentColor = Color.White,
                disabledContainerColor = primaryColor.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(DialogButtonCornerRadius),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp,
                pressedElevation = 12.dp
            ),
            modifier = Modifier
                .weight(1f)
                .height(DialogButtonHeight)
        ) {
            if (primaryLoading) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    primaryIcon?.let {
                        Icon(imageVector = it, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    Text(primaryText, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
