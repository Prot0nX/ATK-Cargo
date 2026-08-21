package com.atk.atk_cargo.feature.home.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.automirrored.rounded.ManageSearch
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.filled.SignalCellular4Bar
import androidx.compose.material.icons.filled.SignalCellularOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.rounded.AddTask
import androidx.compose.material.icons.rounded.DirectionsBoat
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.ManageAccounts
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.atk.atk_cargo.core.domain.AnimationManager
import com.atk.atk_cargo.core.startup.LocalStartupViewModel
import com.atk.atk_cargo.data.model.MenuItem
import com.atk.atk_cargo.feature.auth.domain.LogoutUseCase
import com.atk.atk_cargo.feature.cargo_counter.navigation.navigateToCargoCounter
import com.atk.atk_cargo.feature.cargo_entry.navigation.navigateToInitialInfo
import com.atk.atk_cargo.feature.cargo_entry.navigation.navigateToSelectInfo
import com.atk.atk_cargo.feature.chat.data.ChatRepository
import com.atk.atk_cargo.feature.chat.navigation.navigateToAdminChat
import com.atk.atk_cargo.feature.home.domain.getMenuItemsForUserType
import com.atk.atk_cargo.feature.home.presentation.components.ProfileMenu
import com.atk.atk_cargo.feature.reports.navigation.navigateToManageShips
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.time.Duration.Companion.milliseconds

@SuppressLint("HardwareIds")
@Composable
fun HomeScreen(
    navController: NavController,
    username: String,
    userType: String,
    userPermissions: Map<String, Boolean>,
    isSessionValid: Boolean,
    onManageUsersClick: () -> Unit
) {
    var selectedMenuItem by remember { mutableStateOf<MenuItem?>(null) }
    var showGridAnimation by remember { mutableStateOf(false) }
    val chatRepository = koinInject<ChatRepository>()

    val unreadCountByMe by chatRepository.unreadCount.collectAsStateWithLifecycle(initialValue = 0)

    AnimatedContent(
        targetState = isSessionValid && username.isNotEmpty(),
        transitionSpec = {
            fadeIn(animationSpec = tween(200)) + slideInVertically(
                animationSpec = tween(200),
                initialOffsetY = { fullHeight -> -fullHeight }
            ) togetherWith fadeOut(animationSpec = tween(200)) + slideOutVertically(
                animationSpec = tween(200),
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
                    onBeforeLogout = {
                        showGridAnimation = false
                        delay(200.milliseconds)
                    }
                )

                if (isLoggedIn) {
                    LaunchedEffect(Unit) {
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
                    delay(250.milliseconds)
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
    onBeforeLogout: suspend () -> Unit
) {
    val headerScale = remember { Animatable(0.97f) }
    val headerOpacity = remember { Animatable(0f) }
    val startupViewModel = LocalStartupViewModel.current
    val logoutUseCase = koinInject<LogoutUseCase>()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

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
                            onBeforeLogout()
                            val result = logoutUseCase(username)
                            startupViewModel.updateSessionValidity(false)
                            result.getOrNull()?.let { message ->
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
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

    // انیمیشن پالس فقط وقتی هشدار وجود دارد و انیمیشن‌ها فعال‌اند اجرا می‌شود
    val badgeScale: Float = if (warningsCount > 0 && AnimationManager.areAnimationsEnabled()) {
        val infiniteTransition = rememberInfiniteTransition(label = "badge_pulse")
        val scale by infiniteTransition.animateFloat(
            initialValue = 0.92f,
            targetValue = 1.12f,
            animationSpec = infiniteRepeatable(
                animation = tween(900, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "badge_scale"
        )
        scale
    } else {
        1f
    }

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
        MaterialTheme.colorScheme.surface
    }
    val borderColor = if (isDark) {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
    }

    val statusColor = if (isOnline) Color(0xFF10B981) else Color(0xFFF59E0B)
    val statusIconBg = statusColor.copy(alpha = 0.14f)
    val statusText = if (isOnline) "سیستم آمـاده بارگیری" else "حالت آفلاین (ذخیره محلی)"
    val networkText = if (isOnline) "آنلاین" else "آفلاین"
    val syncText = if (isOnline) "همگام" else "در انتظار شبکه"

    // پالس تزئینی وضعیت آنلاین/آفلاین، فقط تابع تنظیم سیستمی «حذف انیمیشن‌ها»
    val pulseAlpha: Float
    val pulseScale: Float
    if (AnimationManager.areAnimationsEnabled()) {
        val pulseTransition = rememberInfiniteTransition(label = "status_pulse")
        pulseAlpha = pulseTransition.animateFloat(
            initialValue = 0.45f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1600, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "status_pulse_alpha"
        ).value
        pulseScale = pulseTransition.animateFloat(
            initialValue = 1f,
            targetValue = 2.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(1600, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "status_pulse_scale"
        ).value
    } else {
        // حلقه پالس مخفی می‌شود و فقط نقطه توپر ثابت وضعیت را نشان می‌دهد
        pulseAlpha = 0f
        pulseScale = 1f
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bannerBg),
        border = BorderStroke(0.5.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(34.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(10.dp))
                            .background(statusIconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isOnline) Icons.Default.SignalCellular4Bar else Icons.Default.SignalCellularOff,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = statusColor
                        )
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = (-2).dp, y = (-2).dp)
                            .size(9.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(statusColor.copy(alpha = pulseAlpha))
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = (-2).dp, y = (-2).dp)
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "به‌روزرسانی خودکار فعال",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(12.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                )

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

@Composable
private fun CategorizedMenuGrid(
    menuItems: List<MenuItem>,
    showAnimation: Boolean,
    badgeCounts: Map<String, Int> = emptyMap(),
    onItemClick: (MenuItem) -> Unit
) {
    val groupedItems = remember(menuItems) { menuItems.groupBy { it.category } }
    val categoryOrder = listOf("عملیات پایه", "مدیریت", "ارتباطات")
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
                val (wideItems, compactItems) = items.partition { it.isWideCard() }

                item(span = { GridItemSpan(columnsCount) }) {
                    CategoryHeader(title = category, showAnimation = showAnimation)
                }

                items(
                    count = wideItems.size,
                    key = { index -> wideItems[index].route },
                    span = { GridItemSpan(columnsCount) }
                ) { index ->
                    WideMenuCard(
                        item = wideItems[index],
                        showAnimation = showAnimation,
                        badgeCount = badgeCounts[wideItems[index].route] ?: 0,
                        onItemClick = onItemClick
                    )
                }

                if (wideItems.isNotEmpty() && compactItems.isNotEmpty()) {
                    item(span = { GridItemSpan(columnsCount) }) {
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }

                items(
                    count = compactItems.size,
                    key = { index -> compactItems[index].route },
                    span = { GridItemSpan(1) }
                ) { index ->
                    CompactMenuCard(
                        item = compactItems[index],
                        showAnimation = showAnimation,
                        badgeCount = badgeCounts[compactItems[index].route] ?: 0,
                        onItemClick = onItemClick
                    )
                }

                item(span = { GridItemSpan(columnsCount) }) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

private fun MenuItem.isWideCard(): Boolean = route == "manage_ships" || category == "ارتباطات"

@Composable
private fun CategoryHeader(title: String, showAnimation: Boolean) {
    AnimatedVisibility(
        visible = showAnimation,
        enter = fadeIn(animationSpec = tween(400)) + slideInVertically(initialOffsetY = { -10 })
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
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

    // کارت‌های عملیات فعال (ثبت/تعریف/مدیریت) با پس‌زمینه رنگی برجسته می‌شوند تا از کارت‌های نظارتی/فهرستی متمایز شوند
    val isCreationAction = item.route == "select_info" || item.route == "initial_info" || item.route == "manage_users"

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val containerColor = when {
        isCreationAction -> iconColor.copy(alpha = if (isDark) 0.16f else 0.1f)
        isDark -> MaterialTheme.colorScheme.surfaceContainerHigh
        else -> MaterialTheme.colorScheme.surface
    }
    val borderColor = when {
        isCreationAction -> iconColor.copy(alpha = 0.18f)
        isDark -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
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
            border = BorderStroke(1.dp, if (isPressed) iconColor else borderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
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
                                .background(if (isCreationAction) iconColor else iconColor.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(26.dp),
                                tint = if (isCreationAction) Color.White else iconColor
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
            border = BorderStroke(1.dp, if (isPressed) iconColor else borderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
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

