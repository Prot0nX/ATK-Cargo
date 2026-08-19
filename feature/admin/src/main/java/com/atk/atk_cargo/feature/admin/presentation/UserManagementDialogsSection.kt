package com.atk.atk_cargo.feature.admin.presentation

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PersonAddAlt
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.atk.atk_cargo.data.model.CreateUserRequest
import com.atk.atk_cargo.api.RetrofitClient
import com.atk.atk_cargo.data.model.UpdateUserRequest
import com.atk.atk_cargo.data.model.User
import com.atk.atk_cargo.feature.admin.presentation.UserTypeInfo
import com.atk.atk_cargo.ui.theme.ATKCargoTheme
import kotlinx.coroutines.launch

// این فایل دیالوگ‌های افزودن/ویرایش/حذف/خروج‌اجباری کاربر را از UserManagementScreen.kt
// جدا نگه می‌دارد (A1-6، بازسازی ساختاری). وابسته به قطعات internal تعریف‌شده در
// UserManagementScreen.kt (پالت رنگ‌های Dialog*/UserManagement*/Modal*، و توابع
// getUserTypeDisplay/sortUsersByType که در همان فایل باقی می‌مانند) که چون هم‌پکیج
// هستند نیازی به import ندارند.

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
                        Brush.verticalGradient(listOf(ModalGradientTop, ModalGradientBottom)),
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
                            color = DialogTitleColor
                        )
                        Text(
                            "افزودن کاربر به سیستم",
                            style = MaterialTheme.typography.labelSmall,
                            color = DialogMutedText
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
                                    char == '‌' ||
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
                    label = "رمز عبور (حداقل ۸ کاراکتر)",
                    value = password,
                    onValueChange = { password = it },
                    ltr = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
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
                            .border(1.5.dp, DialogFieldBorder, RoundedCornerShape(13.dp))
                            .clickable(enabled = !isLoading, onClick = onDismiss)
                            .padding(vertical = 13.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("انصراف", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DialogMutedText)
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
                                    password.length < 8 -> errorMessage = "رمز عبور باید حداقل ۸ کاراکتر باشد"
                                    else -> {
                                        scope.launch {
                                            isLoading = true
                                            try {
                                                val request = CreateUserRequest(
                                                    username = username,
                                                    fullName = fullName,
                                                    password = password,
                                                    userType = selectedUserType
                                                )
                                                val response = RetrofitClient.apiServiceV2.createUser(request)
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
                                color = UserManagementOnAccent
                            )
                        } else {
                            Text("تایید و ثبت", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = UserManagementOnAccent)
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
            color = DialogMutedText
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
                unfocusedBorderColor = DialogFieldBorder,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
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
                    onValueChange = { password = it },
                    label = { Text("رمز عبور جدید (حداقل ۸ کاراکتر، در صورت عدم تغییر خالی بگذارید)") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
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
                                password.isNotEmpty() && password.length < 8 -> errorMessage = "رمز عبور باید حداقل ۸ کاراکتر باشد"
                                else -> {
                                    val updateRequest = UpdateUserRequest(
                                        id = user.id,
                                        username = username.takeIf { it != user.username },
                                        fullName = fullName.takeIf { it != user.fullName },
                                        password = password.takeIf { it.isNotEmpty() },
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
