package com.atk.atk_cargo.feature.home.presentation.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.atk.atk_cargo.MainActivity
import com.atk.atk_cargo.api.RetrofitClient
import com.atk.atk_cargo.api.User
import com.atk.atk_cargo.api.UserPreferencesManager
import com.atk.atk_cargo.feature.home.presentation.ProfileSettingsDialog
import com.atk.atk_cargo.ui.theme.SurfaceDark
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ProfileMenu(
    username: String,
    userType: String,
    onLogoutClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var currentUser by remember { mutableStateOf<User?>(null) }
    val context = LocalContext.current
    val userPreferencesManager = remember { UserPreferencesManager(context) }
    val hardwareScore by userPreferencesManager.hardwareScore.collectAsState(initial = -1)
    val loadingEnabled by userPreferencesManager.loadingNotificationsEnabled.collectAsState(initial = true)
    val chatEnabled by userPreferencesManager.chatNotificationsEnabled.collectAsState(initial = true)
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = ""
    )

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val glassBgColor = if (isDark) MaterialTheme.colorScheme.surface.copy(alpha = 0.55f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
    val glassBorderColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)

    val greeting = remember {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "صبح بخیر"
            in 12..15 -> "ظهر بخیر"
            in 16..18 -> "عصر بخیر"
            in 19..23 -> "شب بخیر"
            else -> "بامداد بخیر"
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = glassBgColor),
        border = BorderStroke(1.dp, glassBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                    )
                                ),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .padding(1.5.dp)
                            .background(
                                color = if (isDark) SurfaceDark else Color.White,
                                shape = RoundedCornerShape(13.dp)
                            )
                            .padding(2.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(11.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Column {
                        Text(
                            text = "$greeting، $username",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = getUserTypeDisplay(userType),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (hardwareScore > 0) {
                                Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(MaterialTheme.colorScheme.outlineVariant))
                                Text(
                                    text = "امتیاز: $hardwareScore",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.rotate(rotationState),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    
                    NotificationSettingRow(
                        title = "اعلان‌های بارگیری",
                        subtitle = "بررسی خودکار و هشدار تناژ",
                        icon = Icons.Default.Inventory,
                        enabled = loadingEnabled,
                        onCheckedChange = { isEnabled ->
                            val activity = context as? MainActivity
                            CoroutineScope(Dispatchers.Main).launch {
                                userPreferencesManager.setLoadingNotificationsEnabled(isEnabled)
                                if (isEnabled) {
                                    activity?.startLoadingNotificationService()
                                    activity?.checkTonnageWarnings()
                                } else {
                                    activity?.stopLoadingNotificationService()
                                }
                            }
                        }
                    )

                    NotificationSettingRow(
                        title = "اعلان‌های گفتگو",
                        subtitle = "پیام‌های جدید و منشن‌ها",
                        icon = Icons.Rounded.Forum,
                        enabled = chatEnabled,
                        onCheckedChange = { isEnabled ->
                            val activity = context as? MainActivity
                            CoroutineScope(Dispatchers.Main).launch {
                                userPreferencesManager.setChatNotificationsEnabled(isEnabled)
                                if (isEnabled) {
                                    activity?.startChatNotificationWorker()
                                } else {
                                    activity?.stopChatNotificationService()
                                }
                            }
                        }
                    )

                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    ThemeColorPickerRow(
                        userPreferencesManager = userPreferencesManager
                    )

                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    ActionButtons(
                        onSettingsClick = {
                            showSettings = true
                            expanded = false
                        },
                        onLogoutClick = {
                            expanded = false
                            onLogoutClick()
                        }
                    )
                }
            }
        }
    }

    if (showSettings) {
        LaunchedEffect(Unit) {
            if (currentUser == null) {
                try {
                    val response = RetrofitClient.apiService.getAllUsers()
                    currentUser = response.find { it.username == username }
                } catch (_: Exception) {
                    Toast.makeText(context, "خطا در دریافت اطلاعات", Toast.LENGTH_SHORT).show()
                    showSettings = false
                    currentUser = null
                }
            }
        }
        
        currentUser?.let { user ->
            ProfileSettingsDialog(
                user = user,
                onDismiss = {
                    showSettings = false
                    currentUser = null
                },
                onLogout = onLogoutClick
            )
        }
    }
}

private fun getUserTypeDisplay(userType: String): String {
    return when (userType.lowercase()) {
        "admin" -> "مدیر سیستم"
        "operator" -> "اپراتور"
        "counter" -> "بارشمار"
        else -> userType
    }
}

@Composable
private fun ActionButtons(
    onSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                .clickable { onSettingsClick() }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Security,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "رمز عبور",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                .clickable { onLogoutClick() }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "خروج",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
