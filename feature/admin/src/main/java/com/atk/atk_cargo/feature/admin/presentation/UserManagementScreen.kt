package com.atk.atk_cargo.feature.admin.presentation

import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.atk.atk_cargo.core.domain.AnimationManager
import com.atk.atk_cargo.data.model.User
import com.atk.atk_cargo.domain.session.UserPreferencesStore
import com.atk.atk_cargo.ui.theme.ATKCargoTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun UserManagementDialog(
    onDismiss: () -> Unit
) {
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedRoleFilter by remember { mutableStateOf("all") }
    var isLoading by remember { mutableStateOf(true) }

    var showEditDialog by remember { mutableStateOf<User?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf<User?>(null) }
    var showForceLogoutConfirmation by remember { mutableStateOf<User?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

 // وضعیت باز بودن تکی نقش در ساختار آکاردئونی.
    var expandedRole by remember { mutableStateOf<String?>("admin") }

    val context = LocalContext.current
    val userPreferencesManager = koinInject<UserPreferencesStore>()
    val viewModel: UserManagementViewModel = koinViewModel()

    val currentUsername by userPreferencesManager.username.collectAsStateWithLifecycle(initialValue = "")
    val currentUserType by userPreferencesManager.userType.collectAsStateWithLifecycle(initialValue = "")
    val userPermissions by userPreferencesManager.permissions.collectAsStateWithLifecycle(initialValue = emptyMap())
    val isMainAdmin = currentUsername == "Prot0nX"

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    fun fetchUsersWithStatus() {
        isLoading = true
        viewModel.fetchUsersWithStatus(
            onSuccess = { response ->
                users = sortUsersByType(response)
                isLoading = false
            },
            onError = { message ->
                Toast.makeText(context, "خطا در دریافت لیست کاربران: $message", Toast.LENGTH_LONG).show()
                isLoading = false
            }
        )
    }

    LaunchedEffect(Unit) {
        fetchUsersWithStatus()
    }

 // منطق فیلتر کردن کاربران.
    val filteredUsers = remember(users, searchQuery, selectedRoleFilter) {
        users.filter { user ->
            val matchesQuery = searchQuery.isEmpty() ||
                    user.username.contains(searchQuery, ignoreCase = true) ||
                    (user.fullName?.contains(searchQuery, ignoreCase = true) == true)

            val matchesRole = when (selectedRoleFilter) {
                "admin" -> user.userType == "admin"
                "operator" -> user.userType == "operator"
                "verifier" -> user.userType == "verifier"
                else -> true
            }

            matchesQuery && matchesRole
        }
    }

 // گروه‌بندی کاربران بر اساس نقش.
    val groupedUsers = remember(filteredUsers) {
        val groups = LinkedHashMap<String, List<User>>()
        val roleOrder = listOf("admin", "operator", "verifier")

        roleOrder.forEach { role ->
            val list = filteredUsers.filter { it.userType == role }
            if (list.isNotEmpty()) {
                groups[role] = list
            }
        }

 // سایر نقش‌های کاربری.
        val others = filteredUsers.filter { it.userType !in roleOrder }
        if (others.isNotEmpty()) {
            groups["other"] = others
        }

        groups
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .width(34.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            )
        }
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.90f)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
            ) {
 // Header Bar
                TopHeaderSection(
                    canAddUser = currentUserType == "admin" || userPermissions["manage_users"] == true,
                    onAddUserClick = { showAddDialog = true },
                    onRefreshClick = { fetchUsersWithStatus() },
                    onCloseClick = onDismiss
                )

                Spacer(modifier = Modifier.height(12.dp))

 // کارت‌های خلاصه وضعیت داشبورد.
                DashboardSummaryRow(users = users)

                Spacer(modifier = Modifier.height(12.dp))

 // نوار جستجو.
                UserSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

 // چیپ‌های فیلتر.
                RoleFilterChipRow(
                    selectedFilter = selectedRoleFilter,
                    totalCount = users.size,
                    adminCount = users.count { it.userType == "admin" },
                    operatorCount = users.count { it.userType == "operator" },
                    verifierCount = users.count { it.userType == "verifier" },
                    onFilterSelected = { selectedRoleFilter = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

 // بخش محتوای اصلی.
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    when {
                        isLoading -> {
                            ShimmerUserLoadingList()
                        }
                        filteredUsers.isEmpty() -> {
                            EmptyStateView(
                                searchQuery = searchQuery,
                                selectedFilter = selectedRoleFilter,
                                onClearFilter = {
                                    searchQuery = ""
                                    selectedRoleFilter = "all"
                                }
                            )
                        }
                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                groupedUsers.forEach { (role, roleUserList) ->
                                    val isExpanded = (expandedRole == role)

 // سرستون چسبان برای هر گروه نقشی.
                                    stickyHeader(key = "header_$role") {
                                        RoleGroupHeader(
                                            role = role,
                                            userCount = roleUserList.size,
                                            isExpanded = isExpanded,
                                            onToggleExpand = {
                                                expandedRole = if (expandedRole == role) null else role
                                            }
                                        )
                                    }

                                    if (isExpanded) {
                                        items(roleUserList, key = { it.id }) { user ->
                                            EnterpriseUserCard(
                                                user = user,
                                                isMainAdmin = isMainAdmin,
                                                currentUserType = currentUserType,
                                                currentUsername = currentUsername,
                                                userPermissions = userPermissions,
                                                onEditClick = { showEditDialog = user },
                                                onDeleteClick = { showDeleteConfirmation = user },
                                                onForceLogoutClick = { showForceLogoutConfirmation = user }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

 // ==================== DIALOGS ====================

    if (showAddDialog) {
        EnhancedAddUserDialog(
            viewModel = viewModel,
            onDismiss = { showAddDialog = false },
            onUserAdded = { fetchUsersWithStatus() },
            isMainAdmin = isMainAdmin
        )
    }

    showEditDialog?.let { user ->
        EnhancedEditUserDialog(
            user = user,
            isMainAdmin = isMainAdmin,
            onDismiss = { showEditDialog = null },
            onSave = { updateRequest ->
                viewModel.updateUser(
                    request = updateRequest,
                    onSuccess = {
                        fetchUsersWithStatus()
                        showEditDialog = null
                        Toast.makeText(context, "اطلاعات کاربر با موفقیت بروزرسانی شد", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { message ->
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    },
                    onError = { message ->
                        Toast.makeText(context, "خطا در ویرایش کاربر: $message", Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }

    showDeleteConfirmation?.let { user ->
        EnhancedDeleteConfirmationDialog(
            user = user,
            onConfirm = {
                viewModel.deleteUser(
                    userId = user.id,
                    onSuccess = {
                        fetchUsersWithStatus()
                        showDeleteConfirmation = null
                        Toast.makeText(context, "کاربر با موفقیت حذف شد", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { message ->
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    },
                    onError = { message ->
                        Toast.makeText(context, "خطا در حذف کاربر: $message", Toast.LENGTH_LONG).show()
                    }
                )
            },
            onDismiss = { showDeleteConfirmation = null }
        )
    }

    showForceLogoutConfirmation?.let { user ->
        EnhancedForceLogoutDialog(
            user = user,
            onConfirm = {
                viewModel.forceLogoutUser(
                    user = user,
                    onSuccess = { message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        fetchUsersWithStatus()
                    },
                    onFailure = { message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    },
                    onError = { message ->
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    },
                    onFinally = {
                        showForceLogoutConfirmation = null
                    }
                )
            },
            onDismiss = { showForceLogoutConfirmation = null }
        )
    }
}

@Composable
private fun TopHeaderSection(
    title: String = "مدیریت کاربران",
    subtitle: String = "کنترل کاربران و نقش‌ها",
    canAddUser: Boolean,
    onAddUserClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onCloseClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconButton(
                onClick = onCloseClick,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "بستن",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (canAddUser) {
                Button(
                    onClick = onAddUserClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.inverseSurface,
                        contentColor = MaterialTheme.colorScheme.inverseOnSurface
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "کاربر جدید",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            IconButton(
                onClick = onRefreshClick,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "بروزرسانی",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun DashboardSummaryRow(users: List<User>) {
    val stats = listOf(
        "کل کاربران" to users.size,
        "مدیران" to users.count { it.userType == "admin" },
        "باسکول‌چی" to users.count { it.userType == "operator" },
        "بارشمار" to users.count { it.userType == "verifier" }
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            stats.forEachIndexed { index, (label, count) ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (index != stats.lastIndex) {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(30.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    )
                }
            }
        }
    }
}

@Composable
private fun UserSearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                "جستجو بر اساس نام یا نام کاربری",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "پاک کردن",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(50),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    )
}

@Composable
private fun RoleFilterChipRow(
    selectedFilter: String,
    totalCount: Int,
    adminCount: Int,
    operatorCount: Int,
    verifierCount: Int,
    onFilterSelected: (String) -> Unit
) {
    val tabs = listOf(
        Triple("all", "همه", totalCount),
        Triple("admin", "مدیران", adminCount),
        Triple("operator", "باسکول‌چی", operatorCount),
        Triple("verifier", "بارشمار", verifierCount)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        tabs.forEach { (value, label, _) ->
            val isSelected = selectedFilter == value
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(11.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                    .clickable { onFilterSelected(value) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// internal (نه private) چون UserManagementDialogsSection.kt هم به این‌ها نیاز دارد
internal val UserManagementAccent: Color
    @Composable get() = MaterialTheme.colorScheme.primary

internal val UserManagementOnAccent: Color
    @Composable get() = MaterialTheme.colorScheme.onPrimary

private val UserCardBg: Color
    @Composable get() = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)

private val UserCardBorder: Color
    @Composable get() = MaterialTheme.colorScheme.outlineVariant

private val UserCardBorderHover: Color
    @Composable get() = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)

internal val UserAvatarBg: Color
    @Composable get() = MaterialTheme.colorScheme.primaryContainer

private val UserRoleBadgeBg: Color
    @Composable get() = MaterialTheme.colorScheme.primaryContainer

private val ForceLogoutTint: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFFE8A34D) else Color(0xFFC2760A)

internal val ModalGradientBottom: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFF0F2E2A) else Color(0xFFF2FAF8)

internal val ModalGradientTop: Color
    @Composable get() = MaterialTheme.colorScheme.surface

internal val DialogMutedText: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

internal val DialogTitleColor: Color
    @Composable get() = MaterialTheme.colorScheme.onSurface

internal val DialogFieldBorder: Color
    @Composable get() = MaterialTheme.colorScheme.outlineVariant

@Composable
private fun RoleGroupHeader(
    role: String,
    userCount: Int,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    val roleTitle = when (role) {
        "admin" -> "مدیران سیستم"
        "operator" -> "باسکول‌چی‌ها"
        "verifier" -> "بارشمارها"
        else -> "سایر نقش‌ها"
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                .clickable { onToggleExpand() }
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = roleTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = UserManagementAccent,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "$userCount نفر",
                        style = MaterialTheme.typography.labelSmall,
                        color = UserManagementOnAccent,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "بستن" else "باز کردن",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun EnterpriseUserCard(
    user: User,
    isMainAdmin: Boolean,
    currentUserType: String,
    currentUsername: String,
    userPermissions: Map<String, Boolean>,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onForceLogoutClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val canManage = isMainAdmin || currentUserType == "admin" || userPermissions["manage_users"] == true
    val isProtectedUser = user.username == "Prot0nX"
    val isCurrentUser = user.username == currentUsername
    val displayName = user.fullName?.takeIf { it.isNotBlank() } ?: user.username

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(13.dp),
        color = UserCardBg,
        border = BorderStroke(1.dp, if (menuExpanded) UserCardBorderHover else UserCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Right
                    )

                    if (user.isOnline) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(ATKCargoTheme.semanticColors.success)
                        )
                    }

                    if (isProtectedUser) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "مدیر اصلی",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (isCurrentUser) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "شما",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Text(
                    text = user.username,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Right
                )
            }

            Surface(
                color = UserRoleBadgeBg,
                shape = RoundedCornerShape(100)
            ) {
                Text(
                    text = getUserTypeDisplay(user.userType),
                    style = MaterialTheme.typography.labelSmall,
                    color = UserManagementAccent,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontWeight = FontWeight.SemiBold
                )
            }

 // منوی کشویی عملیات.
            if (canManage) {
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(9.dp))
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "عملیات",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        shape = RoundedCornerShape(14.dp),
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        Column(modifier = Modifier.padding(6.dp).width(IntrinsicSize.Max)) {
                            UserMenuRow(
                                label = "ویرایش اطلاعات",
                                labelColor = DialogTitleColor,
                                icon = Icons.Default.Edit,
                                iconTint = UserManagementAccent,
                                enabled = !isProtectedUser,
                                onClick = {
                                    menuExpanded = false
                                    onEditClick()
                                }
                            )
                            UserMenuRow(
                                label = "خروج اجباری",
                                labelColor = DialogTitleColor,
                                icon = Icons.AutoMirrored.Filled.ExitToApp,
                                iconTint = ForceLogoutTint,
                                enabled = !isProtectedUser && !isCurrentUser,
                                onClick = {
                                    menuExpanded = false
                                    onForceLogoutClick()
                                }
                            )
                            UserMenuRow(
                                label = "حذف کاربر",
                                labelColor = MaterialTheme.colorScheme.error,
                                icon = Icons.Default.Delete,
                                iconTint = MaterialTheme.colorScheme.error,
                                enabled = !isProtectedUser && (isMainAdmin || currentUserType == "admin"),
                                onClick = {
                                    menuExpanded = false
                                    onDeleteClick()
                                }
                            )
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Composable
private fun UserMenuRow(
    label: String,
    labelColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(9.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp)
            .alpha(if (enabled) 1f else 0.4f),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = labelColor
        )
        Spacer(modifier = Modifier.width(24.dp))
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun ShimmerUserLoadingList() {
    val alphaAnim: Float = if (AnimationManager.areAnimationsEnabled) {
        val transition = rememberInfiniteTransition(label = "shimmer")
        transition.animateFloat(
            initialValue = 0.2f,
            targetValue = 0.6f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "shimmer_alpha"
        ).value
    } else {
        0.4f
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(5) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alphaAnim)
                )
            ) {}
        }
    }
}

@Composable
private fun EmptyStateView(
    searchQuery: String,
    selectedFilter: String,
    onClearFilter: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.PersonSearch,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (searchQuery.isNotEmpty() || selectedFilter != "all")
                "هیچ کاربری با فیلترهای مشخص شده یافت نشد"
            else
                "لیست کاربران خالی است",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )

        if (searchQuery.isNotEmpty() || selectedFilter != "all") {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onClearFilter) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text("پاک کردن فیلترها")
                }
            }
        }
    }
}


fun sortUsersByType(users: List<User>): List<User> {
    val typeOrder = mapOf("admin" to 0, "operator" to 1, "verifier" to 2)
    return users.sortedWith(compareBy(
        { typeOrder[it.userType] ?: 3 },
        { it.username }
    ))
}

fun getUserTypeDisplay(userType: String): String {
    return when (userType) {
        "admin" -> "مدیر سیستم"
        "operator" -> "باسکول‌چی"
        "verifier" -> "بارشمار"
        else -> userType
    }
}