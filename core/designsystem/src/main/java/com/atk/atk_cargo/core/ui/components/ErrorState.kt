package com.atk.atk_cargo.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.atk.atk_cargo.ui.theme.ATKCargoTheme

@Composable
fun ErrorState(
    modifier: Modifier = Modifier,
    message: String = "خطایی در دریافت اطلاعات رخ داده است.",
    errorDetails: String? = null,
    icon: ImageVector = Icons.Default.ErrorOutline,
    actionText: String = "تلاش مجدد",
    onRetryClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(ATKCargoTheme.spacing.huge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(ATKCargoTheme.dimensions.iconHuge),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(ATKCargoTheme.spacing.l))
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        if (errorDetails != null) {
            Spacer(modifier = Modifier.height(ATKCargoTheme.spacing.s))
            Text(
                text = errorDetails,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        if (onRetryClick != null) {
            Spacer(modifier = Modifier.height(ATKCargoTheme.spacing.xxl))
            ElevatedButton(
                onClick = onRetryClick,
                shape = ATKCargoTheme.appShapes.button,
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text(text = actionText)
            }
        }
    }
}

