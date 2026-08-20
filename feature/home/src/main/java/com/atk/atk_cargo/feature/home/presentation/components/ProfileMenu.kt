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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.atk.atk_cargo.core.startup.LocalNotificationPermissionRequester
import com.atk.atk_cargo.core.startup.LocalStartupViewModel
import com.atk.atk_cargo.data.model.User
import com.atk.atk_cargo.domain.session.UserSettingsStore
import com.atk.atk_cargo.feature.home.presentation.ProfileSettingsDialog
import com.atk.atk_cargo.feature.home.presentation.ProfileViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

/** رنگ‌های تیل سازگار با تم روشن/تاریک برای منوی پروفایل. */
private class ProfilePalette(
    val accent: Color,
    val accentBg: Color,
    val cardBg: Color,
    val cardBorder: Color,
    val mutedBg: Color,
    val mutedText: Color,
    val titleColor: Color
)

@Composable
private fun rememberProfilePalette(): ProfilePalette {
    val isDark = isSystemInDarkTheme()
    val accent = MaterialTheme.colorScheme.primary
    return ProfilePalette(
        accent = accent,
        accentBg = accent.copy(alpha = if (isDark) 0.18f else 0.16f),
        cardBg = MaterialTheme.colorScheme.surface,
        cardBorder = MaterialTheme.colorScheme.outlineVariant,
        mutedBg = MaterialTheme.colorScheme.surfaceVariant,
        mutedText = MaterialTheme.colorScheme.onSurfaceVariant,
        titleColor = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun ProfileMenu(
    username: String,
    userType: String,
    onLogoutClick: () -> Unit
) {
    val palette = rememberProfilePalette()
    var expanded by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var currentUser by remember { mutableStateOf<User?>(null) }
    val context = LocalContext.current
    val startupViewModel = LocalStartupViewModel.current
    val requestNotificationPermission = LocalNotificationPermissionRequester.current
    val userPreferencesManager = koinInject<UserSettingsStore>()
    val profileViewModel: ProfileViewModel = koinViewModel()
    val hardwareScore by userPreferencesManager.hardwareScore.collectAsStateWithLifecycle(initialValue = -1)
    val loadingEnabled by userPreferencesManager.loadingNotificationsEnabled.collectAsStateWithLifecycle(initialValue = true)
    val chatEnabled by userPreferencesManager.chatNotificationsEnabled.collectAsStateWithLifecycle(initialValue = true)
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "expand_rotation"
    )

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

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .semantics { contentDescription = "منوی کاربر $username" },
        shape = RoundedCornerShape(18.dp),
        color = palette.cardBg,
        border = BorderStroke(1.dp, palette.cardBorder)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(palette.accentBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = palette.accent
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "$greeting، $username",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.titleColor
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = palette.accentBg
                            ) {
                                Text(
                                    text = getUserTypeDisplay(userType),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.accent,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            if (hardwareScore > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(palette.mutedText.copy(alpha = 0.5f))
                                )
                                Text(
                                    text = "امتیاز سخت‌افزار: $hardwareScore",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = palette.mutedText
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (expanded) palette.accentBg else palette.mutedBg)
                        .clickable { expanded = !expanded },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "بستن منو" else "باز کردن منو",
                        modifier = Modifier
                            .size(18.dp)
                            .rotate(rotationState),
                        tint = if (expanded) palette.accent else palette.mutedText
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = palette.cardBorder
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        NotificationSettingRow(
                            title = "اعلان‌های بارگیری",
                            subtitle = "بررسی خودکار و هشدار تناژ",
                            icon = Icons.Default.Inventory,
                            enabled = loadingEnabled,
                            modifier = Modifier.weight(1f),
                            onCheckedChange = { isEnabled ->
                                CoroutineScope(Dispatchers.Main).launch {
                                    userPreferencesManager.setLoadingNotificationsEnabled(isEnabled)
                                    if (isEnabled) {
                                        requestNotificationPermission()
                                        startupViewModel.startLoadingNotificationService()
                                    } else {
                                        startupViewModel.stopLoadingNotificationService()
                                    }
                                }
                            }
                        )

                        NotificationSettingRow(
                            title = "اعلان‌های گفتگو",
                            subtitle = "پیام‌های جدید و منشن‌ها",
                            icon = Icons.Rounded.Forum,
                            enabled = chatEnabled,
                            modifier = Modifier.weight(1f),
                            onCheckedChange = { isEnabled ->
                                CoroutineScope(Dispatchers.Main).launch {
                                    userPreferencesManager.setChatNotificationsEnabled(isEnabled)
                                    if (isEnabled) {
                                        startupViewModel.startChatNotificationWorker()
                                    } else {
                                        startupViewModel.stopChatNotificationService()
                                    }
                                }
                            }
                        )
                    }

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = palette.cardBorder
                    )

                    ThemeColorPickerRow(
                        userPreferencesManager = userPreferencesManager
                    )

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = palette.cardBorder
                    )

                    ActionButtons(
                        accent = palette.accent,
                        accentBg = palette.accentBg,
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
                profileViewModel.loadSelfProfile(
                    onSuccess = { currentUser = it },
                    onError = {
                        Toast.makeText(context, "خطا در دریافت اطلاعات کاربر", Toast.LENGTH_SHORT).show()
                        showSettings = false
                        currentUser = null
                    }
                )
            }
        }

        currentUser?.let { user ->
            ProfileSettingsDialog(
                user = user,
                viewModel = profileViewModel,
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
    accent: Color,
    accentBg: Color,
    onSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .clickable { onSettingsClick() },
            color = accentBg,
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = accent
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "رمز عبور",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = accent
                )
            }
        }

        Surface(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .clickable { onLogoutClick() },
            color = MaterialTheme.colorScheme.errorContainer,
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "خروج",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}
