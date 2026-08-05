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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAddAlt
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.atk.atk_cargo.api.CreateUserRequest
import com.atk.atk_cargo.api.DeleteUserRequest
import com.atk.atk_cargo.api.ForceLogoutRequest
import com.atk.atk_cargo.api.RetrofitClient
import com.atk.atk_cargo.api.UpdateUserRequest
import com.atk.atk_cargo.api.User
import com.atk.atk_cargo.api.UserPreferencesManager
import com.atk.atk_cargo.api.UserTypeInfo
import com.atk.atk_cargo.ui.theme.ATKCargoTheme
import com.atk.atk_cargo.utils.hashPassword
import kotlinx.coroutines.launch

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

    // Single expanded role state (accordion behavior - only one role open at a time)
    var expandedRole by remember { mutableStateOf<String?>("admin") }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val userPreferencesManager = remember { UserPreferencesManager(context) }

    val currentUsername by userPreferencesManager.username.collectAsState(initial = "")
    val currentUserType by userPreferencesManager.userType.collectAsState(initial = "")
    val userPermissions by userPreferencesManager.permissions.collectAsState(initial = emptyMap())
    val isMainAdmin = currentUsername == "Prot0nX"

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    fun fetchUsersWithStatus() {
        isLoading = true
        scope.launch {
            try {
                // Try getAllUsersWithStatus first, fallback to getAllUsers if needed
                val response = try {
                    RetrofitClient.apiService.getAllUsersWithStatus()
                } catch (_: Exception) {
                    RetrofitClient.apiService.getAllUsers()
                }
                users = sortUsersByType(response)
                isLoading = false
            } catch (e: Exception) {
                Toast.makeText(context, "خطا در دریافت لیست کاربران: ${e.message}", Toast.LENGTH_LONG).show()
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchUsersWithStatus()
    }

    // Filtered users logic
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

    // Grouping by role
    val groupedUsers = remember(filteredUsers) {
        val groups = LinkedHashMap<String, List<User>>()
        val roleOrder = listOf("admin", "operator", "verifier")

        roleOrder.forEach { role ->
            val list = filteredUsers.filter { it.userType == role }
            if (list.isNotEmpty()) {
                groups[role] = list
            }
        }

        // Other roles if any
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
                    .fillMaxHeight(0.75f)
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

                // Dashboard Summary Cards
                DashboardSummaryRow(users = users)

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                UserSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Filter Chips
                RoleFilterChipRow(
                    selectedFilter = selectedRoleFilter,
                    totalCount = users.size,
                    adminCount = users.count { it.userType == "admin" },
                    operatorCount = users.count { it.userType == "operator" },
                    verifierCount = users.count { it.userType == "verifier" },
                    onFilterSelected = { selectedRoleFilter = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Content Area
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

                                    // Sticky Header for each role group
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
                scope.launch {
                    try {
                        val response = RetrofitClient.apiService.updateUser(updateRequest)
                        if (response.success) {
                            fetchUsersWithStatus()
                            showEditDialog = null
                            Toast.makeText(context, "اطلاعات کاربر با موفقیت بروزرسانی شد", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, response.message, Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "خطا در ویرایش کاربر: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }

    showDeleteConfirmation?.let { user ->
        EnhancedDeleteConfirmationDialog(
            user = user,
            onConfirm = {
                scope.launch {
                    try {
                        val request = DeleteUserRequest(userId = user.id)
                        val response = RetrofitClient.apiService.deleteUser(request)
                        if (response.success) {
                            fetchUsersWithStatus()
                            showDeleteConfirmation = null
                            Toast.makeText(context, "کاربر با موفقیت حذف شد", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, response.message, Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "خطا در حذف کاربر: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            },
            onDismiss = { showDeleteConfirmation = null }
        )
    }

    showForceLogoutConfirmation?.let { user ->
        EnhancedForceLogoutDialog(
            user = user,
            onConfirm = {
                scope.launch {
                    try {
                        val activeDeviceId: String = try {
                            val sessionResponse = RetrofitClient.apiService.getActiveDeviceId(username = user.username)
                            if (sessionResponse.isSuccessful && sessionResponse.body()?.success == true) {
                                sessionResponse.body()?.deviceId ?: ""
                            } else ""
                        } catch (_: Exception) { "" }

                        val request = ForceLogoutRequest(
                            username = user.username,
                            deviceId = activeDeviceId
                        )
                        val response = RetrofitClient.apiService.forceLogoutUser(request)
                        if (response.isSuccessful && response.body()?.success == true) {
                            Toast.makeText(context, "کاربر ${user.username} با موفقیت از سیستم خارج شد", Toast.LENGTH_SHORT).show()
                            fetchUsersWithStatus()
                        } else {
                            val errorMsg = response.body()?.message ?: "خطا در خروج اجباری کاربر"
                            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "خطا در ارتباط با سرور: ${e.message}", Toast.LENGTH_LONG).show()
                    } finally {
                        showForceLogoutConfirmation = null
                    }
                }
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

private val UserManagementAccent = Color(0xFF0D9488)
private val UserCardBg = Color(0xFFFAFAFB)
private val UserCardBorder = Color(0xFFE4E6E9)
private val UserCardBorderHover = Color(0xFFA9DCD3)
private val UserAvatarBg = Color(0xFFDCEFEA)
private val UserRoleBadgeBg = Color(0xFFD9EFE9)
private val ForceLogoutTint = Color(0xFFC2760A)
private val ModalGradientBottom = Color(0xFFF2FAF8)

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
                        color = Color.White,
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

            // Actions dropdown menu
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
                        containerColor = Color.White
                    ) {
                        Column(modifier = Modifier.padding(6.dp).width(IntrinsicSize.Max)) {
                            UserMenuRow(
                                label = "ویرایش اطلاعات",
                                labelColor = Color(0xFF1F2937),
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
                                labelColor = Color(0xFF1F2937),
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
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alphaAnim by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )

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

@Composable
fun EnhancedAddUserDialog(
    onDismiss: () -> Unit,
    onUserAdded: () -> Unit,
    isMainAdmin: Boolean
) {
    var username by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedUserType by remember { mutableStateOf("operator") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun isValidFullName(name: String): Boolean {
        val trimmedName = name.trim()
        return trimmedName.split(" ").filter { it.isNotEmpty() }.size >= 2
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.Transparent,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .heightIn(max = 680.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(listOf(Color.White, ModalGradientBottom)),
                        RoundedCornerShape(20.dp)
                    )
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header: title + subtitle, icon badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            "ایجاد کاربر جدید",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1F2937)
                        )
                        Text(
                            "افزودن کاربر به سیستم",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF8A8F98)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(UserAvatarBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PersonAddAlt,
                            contentDescription = null,
                            tint = UserManagementAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                DesignTextField(
                    label = "نام و نام خانوادگی",
                    value = fullName,
                    onValueChange = { input ->
                        val newValue = input.filter { char ->
                            char == ' ' ||
                                    char == '\u200C' ||
                                    (char.code in 0x0600..0x06FF) ||
                                    (char.code in 0xFB50..0xFDFF) ||
                                    (char.code in 0xFE70..0xFEFF)
                        }
                        fullName = newValue
                        errorMessage = ""
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                DesignTextField(
                    label = "نام کاربری (حروف انگلیسی)",
                    value = username,
                    onValueChange = { input ->
                        val newValue = input.filter { char ->
                            char.isLetterOrDigit() && char.code < 128
                        }
                        username = newValue.lowercase()
                        errorMessage = ""
                    },
                    ltr = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        imeAction = ImeAction.Next
                    )
                )

                DesignTextField(
                    label = "رمز عبور (حداقل ۴ رقم عددی)",
                    value = password,
                    onValueChange = { password = it.filter { char -> char.isDigit() } },
                    ltr = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Done
                    )
                )

                // Role selection section
                UserTypeSelection(
                    selectedUserType = selectedUserType,
                    onUserTypeSelected = { selectedUserType = it },
                    isMainAdmin = isMainAdmin
                )

                if (errorMessage.isNotEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(13.dp))
                            .border(1.5.dp, Color(0xFFE0E2E6), RoundedCornerShape(13.dp))
                            .clickable(enabled = !isLoading, onClick = onDismiss)
                            .padding(vertical = 13.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("انصراف", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF737780))
                    }

                    Box(
                        modifier = Modifier
                            .weight(1.4f)
                            .clip(RoundedCornerShape(13.dp))
                            .background(UserManagementAccent)
                            .clickable(enabled = !isLoading) {
                                when {
                                    username.isEmpty() -> errorMessage = "لطفاً نام کاربری را وارد کنید"
                                    username.length < 4 -> errorMessage = "نام کاربری باید حداقل ۴ کاراکتر باشد"
                                    fullName.isEmpty() -> errorMessage = "لطفاً نام و نام خانوادگی را وارد کنید"
                                    !isValidFullName(fullName) -> errorMessage = "نام و نام خانوادگی باید حداقل شامل نام و نام خانوادگی باشد"
                                    password.isEmpty() -> errorMessage = "لطفاً رمز عبور را وارد کنید"
                                    password.length < 4 -> errorMessage = "رمز عبور باید حداقل ۴ رقم باشد"
                                    else -> {
                                        scope.launch {
                                            isLoading = true
                                            try {
                                                val hashedPassword = hashPassword(password)
                                                val request = CreateUserRequest(
                                                    username = username,
                                                    fullName = fullName,
                                                    password = hashedPassword,
                                                    userType = selectedUserType
                                                )
                                                val response = RetrofitClient.apiService.createUser(request)
                                                if (response.isSuccessful && response.body()?.success == true) {
                                                    Toast.makeText(context, "کاربر با موفقیت ایجاد شد", Toast.LENGTH_SHORT).show()
                                                    onUserAdded()
                                                    onDismiss()
                                                } else {
                                                    errorMessage = "خطا در ایجاد کاربر: ${response.errorBody()?.string()}"
                                                }
                                            } catch (e: Exception) {
                                                errorMessage = "خطا در ارتباط: ${e.message}"
                                            } finally {
                                                isLoading = false
                                            }
                                        }
                                    }
                                }
                            }
                            .padding(vertical = 13.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text("تایید و ثبت", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DesignTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    ltr: Boolean = false,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF8A8F98)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            textStyle = androidx.compose.ui.text.TextStyle(
                textAlign = if (ltr) TextAlign.Left else TextAlign.Right,
                fontSize = 14.sp
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = UserManagementAccent,
                unfocusedBorderColor = Color(0xFFE5E7EA),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
    }
}

@Composable
fun EnhancedEditUserDialog(
    user: User,
    isMainAdmin: Boolean,
    onDismiss: () -> Unit,
    onSave: (UpdateUserRequest) -> Unit
) {
    var username by remember { mutableStateOf(user.username) }
    var fullName by remember { mutableStateOf(user.fullName ?: "") }
    var password by remember { mutableStateOf("") }
    var selectedUserType by remember { mutableStateOf(user.userType) }
    var errorMessage by remember { mutableStateOf("") }

    fun isValidFullName(name: String): Boolean {
        val trimmedName = name.trim()
        return trimmedName.split(" ").filter { it.isNotEmpty() }.size >= 2
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .heightIn(max = 680.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "ویرایش اطلاعات کاربر",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "بستن",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it.trim()
                        errorMessage = ""
                    },
                    label = { Text("نام کاربری") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { input ->
                        fullName = input
                        errorMessage = ""
                    },
                    label = { Text("نام و نام خانوادگی") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it.filter { char -> char.isDigit() } },
                    label = { Text("رمز عبور جدید (در صورت عدم تغییر خالی بگذارید)") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                )

                UserTypeSelection(
                    selectedUserType = selectedUserType,
                    onUserTypeSelected = { selectedUserType = it },
                    isMainAdmin = isMainAdmin
                )

                if (errorMessage.isNotEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("انصراف")
                    }

                    Button(
                        onClick = {
                            when {
                                username.isEmpty() -> errorMessage = "نام کاربری نمی‌تواند خالی باشد"
                                fullName.isEmpty() -> errorMessage = "نام و نام خانوادگی نمی‌تواند خالی باشد"
                                !isValidFullName(fullName) -> errorMessage = "نام و نام خانوادگی باید معتبر باشد"
                                else -> {
                                    val updateRequest = UpdateUserRequest(
                                        id = user.id,
                                        username = username.takeIf { it != user.username },
                                        fullName = fullName.takeIf { it != user.fullName },
                                        password = password.takeIf { it.isNotEmpty() }?.let { hashPassword(it) },
                                        userType = selectedUserType
                                    )
                                    onSave(updateRequest)
                                }
                            }
                        },
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("ذخیره تغییرات")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedDeleteConfirmationDialog(
    user: User,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        title = {
            Text(
                "تأیید حذف کاربر",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "آیا از حذف کاربر زیر اطمینان کامل دارید؟",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("نام کاربری:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(user.username, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("نقش:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(getUserTypeDisplay(user.userType), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Text(
                    text = "⚠️ این عملیات قابل بازگشت نیست!",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isLoading = true
                    onConfirm()
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onError)
                } else {
                    Text("حذف کاربر")
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("انصراف")
            }
        }
    )
}

@Composable
fun EnhancedForceLogoutDialog(
    user: User,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(ATKCargoTheme.semanticColors.warning.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null,
                    tint = ATKCargoTheme.semanticColors.warning,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        title = {
            Text(
                "خروج اجباری کاربر",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "آیا می‌خواهید کاربر زیر را از تمامی دستگاه‌های فعال خارج کنید؟",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isLoading = true
                    onConfirm()
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = ATKCargoTheme.semanticColors.warning),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = ATKCargoTheme.semanticColors.onWarning)
                } else {
                    Text("خروج اجباری")
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("انصراف")
            }
        }
    )
}

@Composable
private fun UserTypeSelection(
    selectedUserType: String,
    onUserTypeSelected: (String) -> Unit,
    isMainAdmin: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "نقش کاربر:",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )

        val userTypesList = getUserTypes()
        val availableUserTypes = if (isMainAdmin) userTypesList else userTypesList.filter { it.value != "admin" }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            availableUserTypes.forEach { userType ->
                UserTypeOptionHorizontal(
                    userType = userType,
                    isSelected = selectedUserType == userType.value,
                    onSelect = { onUserTypeSelected(userType.value) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun UserTypeOptionHorizontal(
    userType: UserTypeInfo,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onSelect() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = userType.icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )

            Text(
                text = userType.label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun getUserTypes() = listOf(
    UserTypeInfo(
        label = "مدیر سیستم",
        description = "مدیریت سیستم",
        value = "admin",
        icon = Icons.Default.AdminPanelSettings,
        color = Color(0xFF1E88E5)
    ),
    UserTypeInfo(
        label = "باسکول‌چی",
        description = "ثبت وزن بار",
        value = "operator",
        icon = Icons.Default.Engineering,
        color = Color(0xFF43A047)
    ),
    UserTypeInfo(
        label = "بارشمار",
        description = "شمارش بار",
        value = "verifier",
        icon = Icons.Default.PersonSearch,
        color = Color(0xFFFB8C00)
    )
)

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