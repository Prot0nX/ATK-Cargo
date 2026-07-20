package com.atk.atk_cargo.feature.home.presentation

import android.annotation.SuppressLint
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.automirrored.rounded.ManageSearch
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.AddTask
import androidx.compose.material.icons.rounded.DirectionsBoat
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.ManageAccounts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.atk.atk_cargo.MainActivity
import com.atk.atk_cargo.R
import com.atk.atk_cargo.api.AppNotificationManager
import com.atk.atk_cargo.api.LogoutRequest
import com.atk.atk_cargo.api.QuotaTonnageWarning
import com.atk.atk_cargo.api.RetrofitClient
import com.atk.atk_cargo.api.UpdateUserRequest
import com.atk.atk_cargo.api.User
import com.atk.atk_cargo.api.UserPreferencesManager
import com.atk.atk_cargo.data.model.MenuItem
import com.atk.atk_cargo.feature.admin.presentation.getUserTypeDisplay
import com.atk.atk_cargo.feature.cargo_counter.navigation.navigateToCargoCounter
import com.atk.atk_cargo.feature.cargo_entry.navigation.navigateToInitialInfo
import com.atk.atk_cargo.feature.cargo_entry.navigation.navigateToSelectInfo
import com.atk.atk_cargo.feature.chat.navigation.navigateToAdminChat
import com.atk.atk_cargo.feature.reports.navigation.navigateToManageShips
import com.atk.atk_cargo.ui.theme.SurfaceDark
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
import com.atk.atk_cargo.utils.hashPassword
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

@SuppressLint("HardwareIds")
@Composable
fun HomeScreen(
    navController: NavController,
    username: String,
    userType: String,
    userPermissions: Map<String, Boolean>,
    isSessionValid: Boolean,
    onLogoutClick: () -> Unit,
    onManageUsersClick: () -> Unit,
    warningsCount: Int = 0
) {
    var selectedMenuItem by remember { mutableStateOf<MenuItem?>(null) }
    var showGridAnimation by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val mainActivity = context as MainActivity
    val userPreferencesManager = remember { UserPreferencesManager(context) }
    val coroutineScope = rememberCoroutineScope()

    val unreadCountByMe by mainActivity.getChatRepository().unreadCount.collectAsState(initial = 0)

    AnimatedContent(
        targetState = isSessionValid && username.isNotEmpty(),
        transitionSpec = {
            fadeIn(animationSpec = tween(600)) + slideInVertically(
                animationSpec = tween(600),
                initialOffsetY = { fullHeight -> -fullHeight }
            ) togetherWith fadeOut(animationSpec = tween(600)) + slideOutVertically(
                animationSpec = tween(600),
                targetOffsetY = { fullHeight -> fullHeight }
            )
        },
        label = ""
    ) { isLoggedIn ->
        val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
        val primaryColor = MaterialTheme.colorScheme.primary
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = if (isDark) 0.14f else 0.08f),
                            primaryColor.copy(alpha = 0f)
                        ),
                        center = androidx.compose.ui.geometry.Offset(width * 0.9f, height * 0.1f),
                        radius = width * 0.8f
                    ),
                    center = androidx.compose.ui.geometry.Offset(width * 0.9f, height * 0.1f),
                    radius = width * 0.8f
                )
                
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = if (isDark) 0.10f else 0.06f),
                            primaryColor.copy(alpha = 0f)
                        ),
                        center = androidx.compose.ui.geometry.Offset(width * 0.1f, height * 0.9f),
                        radius = width * 0.8f
                    ),
                    center = androidx.compose.ui.geometry.Offset(width * 0.1f, height * 0.9f),
                    radius = width * 0.8f
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                Header(
                    username = username,
                    userType = userType,
                    onLogoutClick = {
                        coroutineScope.launch {
                            try {
                                showGridAnimation = false
                                delay(300)

                                val deviceId = Build.DISPLAY ?: UUID.randomUUID().toString()

                                val sessionToken = userPreferencesManager.sessionToken.first()
                                val logoutRequest = LogoutRequest(
                                    username = username,
                                    deviceId = deviceId,
                                    sessionToken = sessionToken.takeIf { it.isNotEmpty() }
                                )

                                val response = RetrofitClient.apiService.logout(logoutRequest)
                                if (response.isSuccessful && response.body()?.success == true) {
                                    userPreferencesManager.clearUserCredentials()
                                    mainActivity.updateSessionValidity(false)
                                    onLogoutClick()
                                } else {
                                    val errorMessage = when (response.code()) {
                                        400 -> "❌ درخواست نامعتبر"
                                        401 -> "🔐 جلسه منقضی شده است"
                                        404 -> "⚠️ جلسه فعالی یافت نشد"
                                        500 -> "🔧 خطای داخلی سرور"
                                        else -> "خطا در خروج (کد: ${response.code()})"
                                    }

                                    Toast.makeText(mainActivity, errorMessage, Toast.LENGTH_SHORT).show()

                                    userPreferencesManager.clearUserCredentials()
                                    mainActivity.updateSessionValidity(false)
                                    onLogoutClick()
                                }
                            } catch (_: Exception) {
                                userPreferencesManager.clearUserCredentials()
                                mainActivity.updateSessionValidity(false)
                                onLogoutClick()
                            }
                        }
                    },
                    userPreferencesManager = userPreferencesManager,
                    coroutineScope = coroutineScope,
                    mainActivity = mainActivity,
                    warningsCount = warningsCount
                )

                if (isLoggedIn) {
                    LaunchedEffect(Unit) {
                        delay(200)
                        showGridAnimation = true
                    }
                    CategorizedMenuGrid(
                        menuItems = getMenuItemsForUserType(userPermissions),
                        showAnimation = showGridAnimation,
                        badgeCounts = mapOf("admin_chat" to unreadCountByMe),
                        onItemClick = { item ->
                            when (item.route) {
                                "manage_users" -> {
                                    onManageUsersClick()
                                }
                                else -> {
                                    selectedMenuItem = item
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    LaunchedEffect(selectedMenuItem) {
        selectedMenuItem?.let { menuItem ->
            when (menuItem.route) {
                "initial_info", "select_info", "cargo_counter", "manage_ships", "manage_users", "admin_chat" -> {
                    showGridAnimation = false
                    delay(300)
                    when (menuItem.route) {
                        "initial_info" -> navController.navigateToInitialInfo()
                        "select_info" -> navController.navigateToSelectInfo()
                        "cargo_counter" -> navController.navigateToCargoCounter()
                        "manage_ships" -> navController.navigateToManageShips()
                        "admin_chat" -> navController.navigateToAdminChat()
                    }
                }
            }
            selectedMenuItem = null
        }
    }
}

@SuppressLint("HardwareIds")
@Composable
private fun Header(
    username: String,
    userType: String,
    onLogoutClick: () -> Unit,
    userPreferencesManager: UserPreferencesManager,
    coroutineScope: CoroutineScope,
    mainActivity: MainActivity,
    warningsCount: Int = 0
) {
    val headerScale = remember { Animatable(0.97f) }
    val headerOpacity = remember { Animatable(0f) }
    var showQuotaTonnage by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        launch {
            headerScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
        launch {
            headerOpacity.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 400)
            )
        }
    }

    LaunchedEffect(mainActivity.shouldOpenWarningsDialog) {
        if (mainActivity.shouldOpenWarningsDialog) {
            showQuotaTonnage = true
            mainActivity.shouldOpenWarningsDialog = false

            val appNotificationManager = AppNotificationManager(mainActivity)
            appNotificationManager.clearAll()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(headerScale.value)
            .alpha(headerOpacity.value)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (username.isNotEmpty()) {
            Box(modifier = Modifier.weight(1f)) {
                ProfileMenu(
                    username = username,
                    userType = userType,
                    onLogoutClick = {
                        coroutineScope.launch {
                            try {
                                val deviceId = Build.DISPLAY ?: UUID.randomUUID().toString()
                                val sessionToken = userPreferencesManager.sessionToken.first()
                                val logoutRequest = LogoutRequest(
                                    username = username,
                                    deviceId = deviceId,
                                    sessionToken = sessionToken.takeIf { it.isNotEmpty() }
                                )

                                val response = RetrofitClient.apiService.logout(logoutRequest)
                                if (response.isSuccessful && response.body()?.success == true) {
                                    userPreferencesManager.clearUserCredentials()
                                    mainActivity.updateSessionValidity(false)
                                    onLogoutClick()
                                } else {
                                    val errorMessage = when (response.code()) {
                                        400 -> "❌ درخواست نامعتبر"
                                        401 -> "🔐 جلسه منقضی شده است"
                                        404 -> "⚠️ جلسه فعالی یافت نشد"
                                        500 -> "🔧 خطای داخلی سرور"
                                        else -> "خطا در خروج (کد: ${response.code()})"
                                    }
                                    Toast.makeText(mainActivity, errorMessage, Toast.LENGTH_SHORT).show()
                                    userPreferencesManager.clearUserCredentials()
                                    mainActivity.updateSessionValidity(false)
                                    onLogoutClick()
                                }
                            } catch (_: Exception) {
                                userPreferencesManager.clearUserCredentials()
                                mainActivity.updateSessionValidity(false)
                                onLogoutClick()
                            }
                        }
                    }
                )
            }
        }

        if (username.isNotEmpty() && userType == "admin") {
            SummaryStatsButton(
                onClick = { showQuotaTonnage = true },
                warningsCount = warningsCount
            )
        }
    }

    if (showQuotaTonnage) {
        QuotaTonnageDialog(onDismiss = { showQuotaTonnage = false })
    }
}

@Composable
private fun SummaryStatsButton(onClick: () -> Unit, warningsCount: Int = 0) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        label = ""
    )

    val infiniteTransition = rememberInfiniteTransition(label = "badge_pulse")
    val badgeScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badge_scale"
    )

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val glassBgColor = if (isDark) MaterialTheme.colorScheme.surface.copy(alpha = 0.55f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
    val glassBorderColor = if (isPressed) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
    } else {
        if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
    }

    Card(
        modifier = Modifier
            .size(height = 64.dp, width = 56.dp)
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = glassBgColor),
        border = BorderStroke(1.dp, glassBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                contentDescription = "گزارشات",
                modifier = Modifier.size(26.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            if (warningsCount > 0) {
                Badge(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .scale(badgeScale),
                    containerColor = MaterialTheme.colorScheme.error
                ) {
                    Text(
                        text = if (warningsCount > 9) "9+" else warningsCount.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }
        }
    }
}

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

@Composable
fun NotificationSettingRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isDark) Color.White.copy(alpha = 0.03f) else Color.Black.copy(alpha = 0.02f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (enabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Switch(
            checked = enabled,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

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
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isDark) Color.White.copy(alpha = 0.03f) else Color.Black.copy(alpha = 0.02f))
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
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Column {
                Text(
                    text = "رنگ اصلی برنامه",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "تم رنگی رابط کاربری",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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

@Composable
fun ProfileSettingsDialog(
    user: User,
    onDismiss: () -> Unit,
    onLogout: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var showConfirmation by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
                Text(
                    "تغییر رمز عبور",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.size(48.dp))
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "نام کاربری:",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                user.username,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "نام و نام خانوادگی:",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                user.fullName ?: "",
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "نقش کاربری:",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                getUserTypeDisplay(user.userType),
                                fontWeight = FontWeight.Bold,
                                color = when (user.userType) {
                                    "admin" -> MaterialTheme.colorScheme.primary
                                    "operator" -> MaterialTheme.colorScheme.secondary
                                    "verifier" -> MaterialTheme.colorScheme.tertiary
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it.filter { char -> char.isDigit() }
                        errorMessage = ""
                    },
                    label = { Text("رمز عبور جدید") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right),
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Next
                    ),
                    isError = errorMessage.isNotEmpty()
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it.filter { char -> char.isDigit() }
                        errorMessage = ""
                    },
                    label = { Text("تکرار رمز عبور جدید") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right),
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Done
                    ),
                    isError = errorMessage.isNotEmpty()
                )
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Right
                    )
                }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "برای حفظ امنیت، رمز عبور باید فقط شامل اعداد و حداقل 4 رقم باشد",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        password.isEmpty() -> {
                            errorMessage = "لطفاً رمز عبور جدید را وارد کنید"
                            return@Button
                        }
                        password.length < 4 -> {
                            errorMessage = "رمز عبور باید حداقل 4 رقم باشد"
                            return@Button
                        }
                        confirmPassword.isEmpty() -> {
                            errorMessage = "لطفاً تکرار رمز عبور را وارد کنید"
                            return@Button
                        }
                        password != confirmPassword -> {
                            errorMessage = "رمز عبور و تکرار آن مطابقت ندارند"
                            return@Button
                        }
                        else -> {
                            showConfirmation = true
                        }
                    }
                },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تغییر رمز عبور")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )

    if (showConfirmation) {
        AlertDialog(
            onDismissRequest = { showConfirmation = false },
            title = {
                Text(
                    "تأیید تغییر رمز عبور",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("آیا از تغییر رمز عبور خود اطمینان دارید؟")
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                "پس از تغییر رمز عبور، نیاز به ورود مجدد خواهید داشت",
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            try {
                                val updateRequest = UpdateUserRequest(
                                    id = user.id,
                                    username = user.username,
                                    fullName = null,
                                    password = hashPassword(password),
                                    userType = user.userType
                                )
                                val response = RetrofitClient.apiService.updateUser(updateRequest)
                                if (response.success) {
                                    Toast.makeText(context, "رمز عبور با موفقیت تغییر کرد", Toast.LENGTH_SHORT).show()
                                    delay(800)
                                    onDismiss()
                                    onLogout()
                                } else {
                                    errorMessage = response.message
                                    showConfirmation = false
                                }
                            } catch (e: Exception) {
                                errorMessage = "خطا در تغییر رمز عبور: ${e.message}"
                                showConfirmation = false
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("تأیید و تغییر رمز عبور")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmation = false },
                    enabled = !isLoading
                ) {
                    Text("انصراف")
                }
            }
        )
    }
}

@Composable
private fun CategorizedMenuGrid(
    menuItems: List<MenuItem>,
    showAnimation: Boolean,
    badgeCounts: Map<String, Int> = emptyMap(),
    onItemClick: (MenuItem) -> Unit
) {
    val groupedItems = menuItems.groupBy { it.category }
    val categoryOrder = listOf("عملیات پایه", "نظارت", "مدیریت", "ارتباطات")
    val activeCategories = categoryOrder.filter { groupedItems.containsKey(it) }
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        activeCategories.forEach { category ->
            val items = groupedItems[category] ?: return@forEach
            val itemCount = items.size
            val isAlwaysWideCategory = category == "نظارت" || category == "ارتباطات"
            
            item(span = { GridItemSpan(2) }) {
                CategoryHeader(title = category, showAnimation = showAnimation)
            }
            
            when {
                isAlwaysWideCategory -> {
                    items(
                        count = itemCount,
                        span = { GridItemSpan(2) }
                    ) { index ->
                        WideMenuCard(
                            item = items[index],
                            showAnimation = showAnimation,
                            badgeCount = badgeCounts[items[index].route] ?: 0,
                            onItemClick = onItemClick
                        )
                    }
                }
                itemCount == 1 -> {
                    item(span = { GridItemSpan(2) }) {
                        WideMenuCard(
                            item = items[0],
                            showAnimation = showAnimation,
                            badgeCount = badgeCounts[items[0].route] ?: 0,
                            onItemClick = onItemClick
                        )
                    }
                }
                itemCount % 2 == 0 -> {
                    items(
                        count = itemCount,
                        span = { GridItemSpan(1) }
                    ) { index ->
                        CompactMenuCard(
                            item = items[index],
                            showAnimation = showAnimation,
                            badgeCount = badgeCounts[items[index].route] ?: 0,
                            onItemClick = onItemClick
                        )
                    }
                }
                else -> {
                    items(
                        count = itemCount - 1,
                        span = { GridItemSpan(1) }
                    ) { index ->
                        CompactMenuCard(
                            item = items[index],
                            showAnimation = showAnimation,
                            badgeCount = badgeCounts[items[index].route] ?: 0,
                            onItemClick = onItemClick
                        )
                    }
                    item(span = { GridItemSpan(2) }) {
                        WideMenuCard(
                            item = items.last(),
                            showAnimation = showAnimation,
                            badgeCount = badgeCounts[items.last().route] ?: 0,
                            onItemClick = onItemClick
                        )
                    }
                }
            }
            
            item(span = { GridItemSpan(2) }) {
                Spacer(modifier = Modifier.height(2.dp))
            }
        }
    }
}

@Composable
private fun CategoryHeader(title: String, showAnimation: Boolean) {
    AnimatedVisibility(
        visible = showAnimation,
        enter = fadeIn(animationSpec = tween(500)) + slideInVertically(initialOffsetY = { -15 })
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp, bottom = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.3.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.25f)
                    .height(2.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0f)
                            )
                        )
                    )
            )
        }
    }
}

@Composable
private fun CompactMenuCard(
    item: MenuItem,
    showAnimation: Boolean,
    badgeCount: Int = 0,
    onItemClick: (MenuItem) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    val pressOffset by animateFloatAsState(
        targetValue = if (isPressed) 3f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "offset"
    )

    val (icon, iconColor, _) = when (item.title) {
        "ثبت حواله" -> Triple(Icons.AutoMirrored.Rounded.Assignment, Color(0xFF3B82F6), listOf(Color(0xFFF0F7FF), Color.White))
        "تعریف کشتی" -> Triple(Icons.Rounded.AddTask, Color(0xFF06B6D4), listOf(Color(0xFFECFEFF), Color.White))
        "مدیریت کاربران" -> Triple(Icons.Rounded.ManageAccounts, Color(0xFF6366F1), listOf(Color(0xFFEEF2FF), Color.White))
        "مدیریت کشتی ها" -> Triple(Icons.Rounded.DirectionsBoat, Color(0xFF14B8A6), listOf(Color(0xFFF0FDFA), Color.White))
        else -> Triple(Icons.AutoMirrored.Rounded.Assignment, MaterialTheme.colorScheme.primary, listOf(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f), MaterialTheme.colorScheme.surface))
    }

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val glassBg = if (isDark) {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
    } else {
        Color.White.copy(alpha = 0.75f)
    }
    val glassBorder = if (isPressed) {
        iconColor.copy(alpha = 0.4f)
    } else {
        if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
    }

    AnimatedVisibility(
        visible = showAnimation,
        enter = fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 0.9f)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale)
                .offset(y = pressOffset.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        },
                        onTap = { onItemClick(item) }
                    )
                },
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, glassBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(containerColor = glassBg)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier.size(54.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .rotate(15f)
                                    .background(
                                        color = iconColor.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                            )
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        color = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.03f),
                                        RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = iconColor
                                )
                            }
                        }
                        if (badgeCount > 0) {
                            Badge(
                                modifier = Modifier.align(Alignment.TopEnd).offset(x = 6.dp, y = (-6).dp),
                                containerColor = Color(0xFFF97316)
                            ) {
                                Text(text = badgeCount.toString(), color = Color.White, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = item.description,
                            style = TextStyle(fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WideMenuCard(
    item: MenuItem,
    showAnimation: Boolean,
    badgeCount: Int = 0,
    onItemClick: (MenuItem) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    val pressOffset by animateFloatAsState(
        targetValue = if (isPressed) 2f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "offset"
    )

    val (icon, iconColor, _) = when (item.title) {
        "ثبت حواله" -> Triple(Icons.AutoMirrored.Rounded.Assignment, Color(0xFF3B82F6), listOf(Color(0xFFF0F7FF), Color.White))
        "تعریف کشتی" -> Triple(Icons.Rounded.AddTask, Color(0xFF06B6D4), listOf(Color(0xFFECFEFF), Color.White))
        "مدیریت کاربران" -> Triple(Icons.Rounded.ManageAccounts, Color(0xFF6366F1), listOf(Color(0xFFEEF2FF), Color.White))
        "مدیریت کشتی ها" -> Triple(Icons.Rounded.DirectionsBoat, Color(0xFF14B8A6), listOf(Color(0xFFF0FDFA), Color.White))
        else -> when (item.category) {
            "نظارت" -> Triple(Icons.AutoMirrored.Rounded.ManageSearch, Color(0xFF3B82F6), listOf(Color(0xFFF0F7FF), Color.White))
            "ارتباطات" -> Triple(Icons.Rounded.Forum, Color(0xFFF43F5E), listOf(Color(0xFFFFF1F2), Color(0xFFFFF7ED)))
            else -> Triple(Icons.AutoMirrored.Rounded.Assignment, MaterialTheme.colorScheme.primary, listOf(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f), MaterialTheme.colorScheme.surface))
        }
    }

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val glassBg = if (isDark) {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
    } else {
        Color.White.copy(alpha = 0.75f)
    }
    val glassBorder = if (isPressed) {
        iconColor.copy(alpha = 0.4f)
    } else {
        if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
    }

    AnimatedVisibility(
        visible = showAnimation,
        enter = fadeIn(animationSpec = tween(500)) + slideInVertically(initialOffsetY = { 20 })
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale)
                .offset(y = pressOffset.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        },
                        onTap = { onItemClick(item) }
                    )
                },
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, glassBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(containerColor = glassBg)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier.size(54.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .rotate(15f)
                                        .background(
                                            color = iconColor.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                )
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(
                                            color = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.03f),
                                            RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                        tint = iconColor
                                    )
                                }
                            }
                            if (badgeCount > 0) {
                                Badge(
                                    modifier = Modifier.align(Alignment.TopEnd).offset(x = 4.dp, y = (-4).dp),
                                    containerColor = Color(0xFFF97316)
                                ) {
                                    Text(text = badgeCount.toString(), color = Color.White, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(180f)
                            .alpha(0.3f),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun QuotaTonnageDialog(onDismiss: () -> Unit) {
    var warnings by remember { mutableStateOf<List<QuotaTonnageWarning>>(emptyList()) }
    var isAllClear by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var refreshTrigger by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    val loadData: () -> Unit = {
        scope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                isLoading = true
                errorMessage = null
                warnings = emptyList()
                isAllClear = false
            }

            try {
                val response = RetrofitClient.apiService.getActiveQuotaReport()
                if (response.isSuccessful) {
                    response.body()?.use { responseBody ->
                        val body = responseBody.string()
                        if (body.isNotEmpty()) {
                            val parsedWarnings = parseQuotaTonnageData(body)

                            withContext(Dispatchers.Main) {
                                if (parsedWarnings.isEmpty() && body.contains("در حد مجاز")) {
                                    isAllClear = true
                                } else {
                                    warnings = parsedWarnings
                                }
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                errorMessage = "داده‌ای دریافت نشد"
                            }
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        errorMessage = "خطا در دریافت داده (کد: ${response.code()})"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = "خطا در ارتباط با سرور: ${e.message}"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
    }

    LaunchedEffect(refreshTrigger) {
        loadData()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.90f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Checklist,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            "گزارش هشدار تناژ کوتاژ",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { refreshTrigger++ },
                            enabled = !isLoading,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "بروزرسانی",
                                modifier = Modifier.size(20.dp),
                                tint = if (isLoading)
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                else
                                    MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "بستن",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    contentAlignment = if (isLoading || errorMessage != null) Alignment.Center else Alignment.TopStart
                ) {
                    when {
                        isLoading -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(40.dp),
                                    strokeWidth = 3.dp
                                )
                                Text(
                                    "در حال دریافت داده...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        errorMessage != null -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(50.dp)
                                )
                                Text(
                                    errorMessage!!,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        isAllClear -> {
                            AllClearMessage()
                        }

                        warnings.isNotEmpty() -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(warnings) { warning ->
                                    QuotaTonnageWarningCard(warning)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuotaTonnageWarningCard(warning: QuotaTonnageWarning) {
    val scope = rememberCoroutineScope()
    var isToggling by remember { mutableStateOf(false) }
    var isActive by remember { mutableStateOf(warning.isActive) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (isActive)
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
        else
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.05f),
        border = BorderStroke(
            1.5.dp,
            if (isActive)
                MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
        ),
        tonalElevation = if (isActive) 2.dp else 0.5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        "هشدار تناژ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                    ) {
                        Text(
                            "کوتاژ ${warning.quotaNumber}",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (!isActive) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        ) {
                            Text(
                                "فعال",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoItem(
                    icon = "🛳",
                    label = "کشتی",
                    value = warning.shipName,
                    modifier = Modifier.weight(1f),
                    valueColor = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                InfoItem(
                    icon = "👤",
                    label = "صاحب کالا",
                    value = warning.cargoOwner,
                    modifier = Modifier.weight(1f),
                    valueColor = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoItem(
                    icon = "📦",
                    label = "مانده فعلی",
                    value = "${warning.currentRemaining} کیلوگرم",
                    modifier = Modifier.weight(1f),
                    valueColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
                InfoItem(
                    icon = "📝",
                    label = "حواله‌های ورود",
                    value = "${warning.voucherCount} عدد",
                    modifier = Modifier.weight(1f),
                    valueColor = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = if (warning.isNegative)
                    MaterialTheme.colorScheme.error.copy(alpha = if (isActive) 0.15f else 0.05f)
                else
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isActive) 0.3f else 0.1f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "⚠️",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "مانده بعد از خروج:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        "${warning.remainingAfterExit} کیلوگرم",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (warning.isNegative)
                            MaterialTheme.colorScheme.error.copy(alpha = if (isActive) 1f else 0.5f)
                        else
                            MaterialTheme.colorScheme.primary.copy(alpha = if (isActive) 1f else 0.5f)
                    )
                }
            }

            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            if (isToggling) {
                LoadingActionButton(
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                )
            } else {
                Button(
                    onClick = {
                        isToggling = true
                        scope.launch(Dispatchers.IO) {
                            try {
                                val response = RetrofitClient.apiService.toggleQuotaStatus(
                                    action = "toggleQuotaStatus",
                                    id = 0,
                                    quotaNumber = warning.quotaNumber
                                )
                                if (response.isSuccessful) {
                                    withContext(Dispatchers.Main) {
                                        isActive = !isActive
                                    }
                                }
                            } catch (_: Exception) {
                            } finally {
                                isToggling = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isActive)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else
                            MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isActive) Icons.Default.ToggleOn else Icons.Default.ToggleOff,
                            contentDescription = null,
                            tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            if (isActive) "فعال کردن" else "غیرفعال کردن",
                            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AllClearMessage() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(50.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "همه چیز در حد مجاز است!",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "تناژ کوتاژها در حد مجاز هستند و مشکلی برای بارگیری وجود ندارد",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

fun parseQuotaTonnageData(rawData: String): List<QuotaTonnageWarning> {
    val warnings = mutableListOf<QuotaTonnageWarning>()
    val blocks = rawData.split("━━━━━━━━━━━━━━━━")

    for (block in blocks) {
        if (block.contains("کشتی") && block.contains("کوتاژ")) {
            try {
                val shipName = block.substringAfter("کشتی *").substringBefore("*").trim()
                val cargoOwner = block.substringAfter("👤 ").substringBefore("\n").trim()
                val quotaNumber = block.substringAfter("کوتاژ: ").substringBefore("\n").trim()
                val currentRemaining = block.substringAfter("مانده فعلی: ").substringBefore(" کیلوگرم").trim()
                val voucherCount = block.substringAfter("حواله‌های ورود شده: ").substringBefore(" عدد").trim()
                val remainingAfterExit = block.substringAfter("مانده بعداز خروج: ").substringBefore(" کیلوگرم").trim()

                val isNegative = remainingAfterExit.contains("−") || remainingAfterExit.contains("-")

                warnings.add(
                    QuotaTonnageWarning(
                        shipName = shipName,
                        cargoOwner = cargoOwner,
                        quotaNumber = quotaNumber,
                        currentRemaining = currentRemaining,
                        voucherCount = voucherCount,
                        remainingAfterExit = remainingAfterExit,
                        isNegative = isNegative
                    )
                )
            } catch (_: Exception) {
            }
        }
    }

    return warnings
}

@Composable
private fun LoadingActionButton(
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = color.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = color,
                strokeWidth = 1.5.dp
            )
        }
    }
}

@Composable
private fun InfoItem(
    icon: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                icon,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

fun getMenuItemsForUserType(userPermissions: Map<String, Boolean>): List<MenuItem> {
    val items = mutableListOf<MenuItem>()
    
    if (userPermissions["select_info"] == true) {
        items.add(MenuItem("ثبت حواله", R.drawable.ic_boosters, "select_info", "عملیات پایه", "ثبت و مدیریت حواله‌های جدید"))
    }
    if (userPermissions["initial_info"] == true) {
        items.add(MenuItem("تعریف کشتی", R.drawable.ic_journal, "initial_info", "عملیات پایه", "ثبت اطلاعات اولیه کشتی"))
    }
    if (userPermissions["cargo_counter"] == true) {
        items.add(MenuItem("نظارت بارشمار", R.drawable.ic_cargo_counter, "cargo_counter", "نظارت", "کنترل و نظارت بر روند بارگیری"))
    }
    if (userPermissions["manage_users"] == true) {
        items.add(MenuItem("مدیریت کاربران", R.drawable.profile_admin, "manage_users", "مدیریت", "افزودن و مدیریت سطح دسترسی"))
    }
    if (userPermissions["manage_ships"] == true) {
        items.add(MenuItem("مدیریت کشتی ها", R.drawable.ic_reports, "manage_ships", "مدیریت", "لیست کشتی‌ها و وضعیت آن‌ها"))
    }
    
    return items
}
