package com.atk.atk_cargo.feature.home.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.automirrored.rounded.ManageSearch
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SignalCellular4Bar
import androidx.compose.material.icons.filled.SignalCellularOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.AddTask
import androidx.compose.material.icons.rounded.DirectionsBoat
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.ManageAccounts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.atk.atk_cargo.MainActivity
import com.atk.atk_cargo.api.LogoutRequest
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
import com.atk.atk_cargo.feature.home.domain.getMenuItemsForUserType
import com.atk.atk_cargo.feature.home.presentation.components.ProfileMenu
import com.atk.atk_cargo.feature.reports.navigation.navigateToManageShips
import com.atk.atk_cargo.utils.hashPassword
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
            fadeIn(animationSpec = tween(500)) + slideInVertically(
                animationSpec = tween(500),
                initialOffsetY = { fullHeight -> -fullHeight }
            ) togetherWith fadeOut(animationSpec = tween(500)) + slideOutVertically(
                animationSpec = tween(500),
                targetOffsetY = { fullHeight -> fullHeight }
            )
        },
        label = "home_screen_session_animation"
    ) { isLoggedIn ->
        val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
        val primaryColor = MaterialTheme.colorScheme.primary

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // پس‌زمینه زنده صنعتی با گرادیان ملایم و عدم ضربه به کنتراست
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = if (isDark) 0.12f else 0.06f),
                            primaryColor.copy(alpha = 0f)
                        ),
                        center = androidx.compose.ui.geometry.Offset(width * 0.85f, height * 0.08f),
                        radius = width * 0.75f
                    ),
                    center = androidx.compose.ui.geometry.Offset(width * 0.85f, height * 0.08f),
                    radius = width * 0.75f
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = if (isDark) 0.08f else 0.04f),
                            primaryColor.copy(alpha = 0f)
                        ),
                        center = androidx.compose.ui.geometry.Offset(width * 0.15f, height * 0.85f),
                        radius = width * 0.75f
                    ),
                    center = androidx.compose.ui.geometry.Offset(width * 0.15f, height * 0.85f),
                    radius = width * 0.75f
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // هدر مدیریت کاربری و خلاصه هشدارهای عملیاتی
                Header(
                    username = username,
                    userType = userType,
                    onLogoutClick = {
                        coroutineScope.launch {
                            try {
                                showGridAnimation = false
                                delay(200)

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
                        delay(150)
                        showGridAnimation = true
                    }

                    val menuItems = remember(userPermissions) {
                        getMenuItemsForUserType(userPermissions)
                    }

                    // شبکه منوی دسته‌بندی‌شده و تطبیقی (Responsive Adaptive Operational Grid)
                    CategorizedMenuGrid(
                        menuItems = menuItems,
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
                    delay(250)
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
                animationSpec = tween(durationMillis = 350)
            )
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(headerScale.value)
            .alpha(headerOpacity.value)
            .padding(horizontal = 16.dp, vertical = 8.dp),
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
    }
}

@Composable
private fun SummaryStatsButton(onClick: () -> Unit, warningsCount: Int = 0) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        label = "button_scale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "badge_pulse")
    val badgeScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badge_scale"
    )

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val containerColor = if (isDark) {
        MaterialTheme.colorScheme.surfaceContainerHigh
    } else {
        MaterialTheme.colorScheme.surface
    }
    val borderColor = if (warningsCount > 0) {
        MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    }

    Card(
        modifier = Modifier
            .size(height = 64.dp, width = 60.dp)
            .scale(scale)
            .semantics { contentDescription = "گزارشات و هشدارهای تناژ کوتاژ، $warningsCount هشدار" }
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
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.5.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = if (warningsCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )

            if (warningsCount > 0) {
                Badge(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .scale(badgeScale),
                    containerColor = MaterialTheme.colorScheme.error
                ) {
                    Text(
                        text = if (warningsCount > 9) "9+" else warningsCount.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// ===== نوار آگاهی صنعتی و پویا از وضعیت سیستم =====
@Composable
private fun SystemAwarenessBanner() {
    val context = LocalContext.current
    var isOnline by remember { mutableStateOf(true) }

    DisposableEffect(context) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isOnline = true
            }

            override fun onLost(network: Network) {
                isOnline = false
            }
        }

        try {
            val activeNetwork = connectivityManager?.activeNetwork
            val capabilities = connectivityManager?.getNetworkCapabilities(activeNetwork)
            isOnline = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager?.registerNetworkCallback(request, networkCallback)
        } catch (_: Exception) {
            isOnline = true
        }

        onDispose {
            try {
                connectivityManager?.unregisterNetworkCallback(networkCallback)
            } catch (_: Exception) {
            }
        }
    }

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val bannerBg = if (isDark) {
        MaterialTheme.colorScheme.surfaceContainerHigh
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }

    val statusColor = if (isOnline) Color(0xFF10B981) else Color(0xFFF59E0B)
    val statusText = if (isOnline) "سیستم آمـاده بارگیری" else "حالت آفلاین (ذخیره محلی)"
    val networkText = if (isOnline) "آنلاین" else "آفلاین"
    val syncText = if (isOnline) "همگام" else "در انتظار شبکه"

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        color = bannerBg,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isOnline) Icons.Default.SignalCellular4Bar else Icons.Default.SignalCellularOff,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = networkText,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOnline) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
                        fontWeight = if (!isOnline) FontWeight.Bold else FontWeight.Normal
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (isOnline) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = syncText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ===== شبکه عملیاتی دسته‌بندی شده و تطبیقی =====
@Composable
private fun CategorizedMenuGrid(
    menuItems: List<MenuItem>,
    showAnimation: Boolean,
    badgeCounts: Map<String, Int> = emptyMap(),
    onItemClick: (MenuItem) -> Unit
) {
    val groupedItems = remember(menuItems) { menuItems.groupBy { it.category } }
    val categoryOrder = listOf("عملیات پایه", "نظارت", "مدیریت", "ارتباطات")
    val activeCategories = remember(groupedItems) { categoryOrder.filter { groupedItems.containsKey(it) } }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val availableWidth = maxWidth
        val columnsCount = if (availableWidth >= 600.dp) 3 else 2

        LazyVerticalGrid(
            columns = GridCells.Fixed(columnsCount),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item(span = { GridItemSpan(columnsCount) }) {
                SystemAwarenessBanner()
            }

            activeCategories.forEach { category ->
                val items = groupedItems[category] ?: return@forEach
                val itemCount = items.size
                val isAlwaysWideCategory = category == "نظارت" || category == "ارتباطات"

                item(span = { GridItemSpan(columnsCount) }) {
                    CategoryHeader(title = category, showAnimation = showAnimation)
                }

                when {
                    isAlwaysWideCategory -> {
                        items(
                            count = itemCount,
                            span = { GridItemSpan(columnsCount) }
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
                        item(span = { GridItemSpan(columnsCount) }) {
                            WideMenuCard(
                                item = items[0],
                                showAnimation = showAnimation,
                                badgeCount = badgeCounts[items[0].route] ?: 0,
                                onItemClick = onItemClick
                            )
                        }
                    }
                    itemCount % columnsCount == 0 -> {
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
                        item(span = { GridItemSpan(columnsCount) }) {
                            WideMenuCard(
                                item = items.last(),
                                showAnimation = showAnimation,
                                badgeCount = badgeCounts[items.last().route] ?: 0,
                                onItemClick = onItemClick
                            )
                        }
                    }
                }

                item(span = { GridItemSpan(columnsCount) }) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun CategoryHeader(title: String, showAnimation: Boolean) {
    AnimatedVisibility(
        visible = showAnimation,
        enter = fadeIn(animationSpec = tween(400)) + slideInVertically(initialOffsetY = { -10 })
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.2.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.3f)
                    .height(3.dp)
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
        label = "compact_card_scale"
    )

    val (icon, iconColor) = when (item.title) {
        "ثبت حواله" -> Pair(Icons.AutoMirrored.Rounded.Assignment, Color(0xFF2563EB))
        "تعریف کشتی" -> Pair(Icons.Rounded.AddTask, Color(0xFF0891B2))
        "مدیریت کاربران" -> Pair(Icons.Rounded.ManageAccounts, Color(0xFF4F46E5))
        "مدیریت کشتی ها" -> Pair(Icons.Rounded.DirectionsBoat, Color(0xFF0D9488))
        else -> Pair(Icons.AutoMirrored.Rounded.Assignment, MaterialTheme.colorScheme.primary)
    }

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val containerColor = if (isDark) {
        MaterialTheme.colorScheme.surfaceContainerHigh
    } else {
        MaterialTheme.colorScheme.surface
    }
    val borderColor = if (isDark) {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
    }

    AnimatedVisibility(
        visible = showAnimation,
        enter = fadeIn(animationSpec = tween(350)) + scaleIn(initialScale = 0.92f)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(115.dp)
                .scale(scale)
                .semantics { contentDescription = "${item.title}: ${item.description}" }
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
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.5.dp, if (isPressed) iconColor else borderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = containerColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(iconColor.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(26.dp),
                                tint = iconColor
                            )
                        }

                        if (badgeCount > 0) {
                            Badge(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 6.dp, y = (-6).dp),
                                containerColor = MaterialTheme.colorScheme.error
                            ) {
                                Text(
                                    text = badgeCount.toString(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
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
        label = "wide_card_scale"
    )

    val (icon, iconColor) = when (item.title) {
        "ثبت حواله" -> Pair(Icons.AutoMirrored.Rounded.Assignment, Color(0xFF2563EB))
        "تعریف کشتی" -> Pair(Icons.Rounded.AddTask, Color(0xFF0891B2))
        "مدیریت کاربران" -> Pair(Icons.Rounded.ManageAccounts, Color(0xFF4F46E5))
        "مدیریت کشتی ها" -> Pair(Icons.Rounded.DirectionsBoat, Color(0xFF0D9488))
        else -> when (item.category) {
            "نظارت" -> Pair(Icons.AutoMirrored.Rounded.ManageSearch, Color(0xFF2563EB))
            "ارتباطات" -> Pair(Icons.Rounded.Forum, Color(0xFFE11D48))
            else -> Pair(Icons.AutoMirrored.Rounded.Assignment, MaterialTheme.colorScheme.primary)
        }
    }

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val containerColor = if (isDark) {
        MaterialTheme.colorScheme.surfaceContainerHigh
    } else {
        MaterialTheme.colorScheme.surface
    }
    val borderColor = if (isDark) {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
    }

    AnimatedVisibility(
        visible = showAnimation,
        enter = fadeIn(animationSpec = tween(400)) + slideInVertically(initialOffsetY = { 15 })
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .scale(scale)
                .semantics { contentDescription = "${item.title}: ${item.description}" }
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
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.5.dp, if (isPressed) iconColor else borderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = containerColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(iconColor.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(26.dp),
                                    tint = iconColor
                                )
                            }
                            if (badgeCount > 0) {
                                Badge(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 4.dp, y = (-4).dp),
                                    containerColor = MaterialTheme.colorScheme.error
                                ) {
                                    Text(
                                        text = badgeCount.toString(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
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
                            .rotate(180f),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ===== دیالوگ‌های بازطراحی‌شده صنعتی =====
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
                IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "بستن")
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
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(14.dp)
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
                            Text("نام کاربری:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(user.username, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("نام و نام خانوادگی:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(user.fullName ?: "-", fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("نقش کاربری:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                getUserTypeDisplay(user.userType),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it.filter { char -> char.isDigit() }
                        errorMessage = ""
                    },
                    label = { Text("رمز عبور جدید (عددی)") },
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
                    shape = RoundedCornerShape(10.dp)
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
                            "رمز عبور باید فقط شامل اعداد و حداقل ۴ رقم باشد.",
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
                        password.isEmpty() -> errorMessage = "لطفاً رمز عبور جدید را وارد کنید"
                        password.length < 4 -> errorMessage = "رمز عبور باید حداقل ۴ رقم باشد"
                        confirmPassword.isEmpty() -> errorMessage = "لطفاً تکرار رمز عبور را وارد کنید"
                        password != confirmPassword -> errorMessage = "رمز عبور و تکرار آن مطابقت ندارند"
                        else -> showConfirmation = true
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.height(48.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
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
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.height(48.dp)
            ) {
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("آیا از تغییر رمز عبور خود اطمینان دارید؟")
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(10.dp)
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
                                "پس از تغییر رمز عبور، نیاز به ورود مجدد خواهید داشت.",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
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
                                    delay(600)
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
                    enabled = !isLoading,
                    modifier = Modifier.height(48.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
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
                    enabled = !isLoading,
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("انصراف")
                }
            }
        )
    }
}


