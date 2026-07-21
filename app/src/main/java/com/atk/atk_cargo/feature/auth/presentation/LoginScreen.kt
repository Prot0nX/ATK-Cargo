package com.atk.atk_cargo.feature.auth.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.atk.atk_cargo.R
import com.atk.atk_cargo.feature.auth.viewmodel.AuthViewModel
import com.atk.atk_cargo.feature.auth.viewmodel.LoginFormState
import com.atk.atk_cargo.feature.auth.viewmodel.LoginUiState
import com.atk.atk_cargo.ui.theme.ATKCargoTheme
import org.koin.androidx.compose.koinViewModel

// ===== CONSTANTS =====
private val HeaderHeight = 260.dp
private val LogoSize = 64.dp
private val LogoIconSize = 40.dp
private val LogoCorner = 16.dp
private val FormPadding = 24.dp
private val ButtonHeight = 56.dp
private val ButtonCorner = 16.dp
private val FieldCorner = 16.dp
private val FieldSpacing = 16.dp
private val SpacingSmall = 8.dp
private val SpacingMedium = 12.dp
private val ProgressIndicatorSize = 24.dp
private val ProgressIndicatorStrokeWidth = 2.dp

// ===== SCREEN =====

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = koinViewModel()
) {
    val loginState by viewModel.loginState.collectAsState()
    val formState by viewModel.formState.collectAsState()

    // ===== SIDE EFFECTS =====
    LaunchedEffect(loginState) {
        if (loginState is LoginUiState.Success) {
            onLoginSuccess()
        }
    }

    // ===== UI =====
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .imePadding()
        ) {
            LoginHeader()
            LoginForm(
                formState = formState,
                loginState = loginState,
                onUsernameChanged = viewModel::onUsernameChanged,
                onPasswordChanged = viewModel::onPasswordChanged,
                onPasswordToggle = viewModel::togglePasswordVisibility,
                onLoginClick = viewModel::login
            )
        }
    }
}

// ===== COMPONENTS =====

@Composable
private fun LoginHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(HeaderHeight)
    ) {
        // تصویر پس‌زمینه
        Image(
            painter = painterResource(R.drawable.login_bg),
            contentDescription = "تصویر پس‌زمینه صفحه ورود",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // گرادینت انتقال به پس‌زمینه
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )

        // لوگو و نام برند
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            Box(
                modifier = Modifier
                    .size(LogoSize)
                    .clip(RoundedCornerShape(LogoCorner))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(SpacingSmall)
                    .semantics { contentDescription = "لوگوی ATK Cargo" },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = null, // توضیح روی کانتینر تعریف شده
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(LogoIconSize)
                )
            }

            Spacer(modifier = Modifier.height(SpacingMedium))

            Text(
                text = "ATK Cargo",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "مدیریت هوشمند فرآیند بارگیری",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
                    .padding(horizontal = 12.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun LoginForm(
    formState: LoginFormState,
    loginState: LoginUiState,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onPasswordToggle: () -> Unit,
    onLoginClick: () -> Unit
) {
    // ===== KEYBOARD / FOCUS =====
    val passwordFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // ===== DERIVED STATE =====
    val isLoginEnabled = formState.username.isNotBlank() &&
            formState.password.isNotBlank() &&
            loginState !is LoginUiState.Loading
    val isLoading = loginState is LoginUiState.Loading
    val errorMessage = (loginState as? LoginUiState.Error)?.message

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(FormPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(FieldSpacing)
    ) {
        // عنوان فرم
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "ورود به حساب کاربری",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "خوش آمدید! لطفاً اطلاعات خود را وارد کنید.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(SpacingSmall))

        // فیلد نام کاربری
        LoginInputField(
            value = formState.username,
            onValueChange = onUsernameChanged,
            label = "نام کاربری",
            placeholder = "نام کاربری خود را وارد کنید",
            fieldDescription = "فیلد نام کاربری",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "آیکون کاربر",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            },
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next,
            keyboardActions = KeyboardActions(
                onNext = { passwordFocusRequester.requestFocus() }
            )
        )

        // فیلد رمز عبور
        LoginInputField(
            value = formState.password,
            onValueChange = onPasswordChanged,
            label = "رمز عبور (فقط عدد)",
            placeholder = "رمز عبور عددی خود را وارد کنید",
            fieldDescription = "فیلد رمز عبور — فقط ارقام مجاز است",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "آیکون قفل رمز عبور",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            },
            trailingIcon = {
                IconButton(onClick = onPasswordToggle) {
                    Icon(
                        imageVector = if (formState.isPasswordVisible)
                            Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (formState.isPasswordVisible)
                            "پنهان کردن رمز عبور" else "نمایش رمز عبور",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            },
            isPassword = true,
            passwordVisible = formState.isPasswordVisible,
            keyboardType = KeyboardType.NumberPassword,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    if (isLoginEnabled) onLoginClick()
                }
            ),
            focusRequester = passwordFocusRequester
        )

        // پیام خطا با انیمیشن
        AnimatedVisibility(
            visible = errorMessage != null,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            errorMessage?.let { msg ->
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
                            RoundedCornerShape(SpacingSmall)
                        )
                        .padding(12.dp)
                        .semantics { contentDescription = "خطای ورود: $msg" },
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(SpacingSmall))

        // دکمه ورود
        Button(
            onClick = {
                keyboardController?.hide()
                onLoginClick()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(ButtonHeight)
                .semantics { contentDescription = if (isLoading) "در حال ورود..." else "ورود به برنامه" },
            shape = RoundedCornerShape(ButtonCorner),
            enabled = isLoginEnabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(ProgressIndicatorSize),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = ProgressIndicatorStrokeWidth
                )
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(SpacingSmall),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ورود به برنامه",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

// ===== REUSABLE WIDGETS =====

@Composable
fun LoginInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    fieldDescription: String,
    leadingIcon: @Composable () -> Unit,
    trailingIcon: @Composable (() -> Unit)? = null,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    focusRequester: FocusRequester? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SpacingSmall)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (focusRequester != null) Modifier.focusRequester(focusRequester)
                    else Modifier
                )
                .semantics { contentDescription = fieldDescription },
            shape = RoundedCornerShape(FieldCorner),
            visualTransformation = if (isPassword && !passwordVisible)
                PasswordVisualTransformation()
            else
                VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = keyboardActions,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )
    }
}

// ===== PREVIEWS =====

@Preview(name = "Login - Light Theme", showBackground = true, locale = "fa")
@Composable
private fun LoginScreenLightPreview() {
    ATKCargoTheme(darkTheme = false) {
        LoginFormPreviewContent()
    }
}

@Preview(name = "Login - Dark Theme", showBackground = true, locale = "fa")
@Composable
private fun LoginScreenDarkPreview() {
    ATKCargoTheme(darkTheme = true) {
        LoginFormPreviewContent()
    }
}

@Preview(name = "Login - Dark Error State", showBackground = true, locale = "fa")
@Composable
private fun LoginScreenErrorPreview() {
    ATKCargoTheme(darkTheme = true) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
            ) {
                LoginHeader()
                LoginForm(
                    formState = LoginFormState(username = "admin", password = "1234"),
                    loginState = LoginUiState.Error("نام کاربری یا رمز عبور اشتباه است"),
                    onUsernameChanged = {},
                    onPasswordChanged = {},
                    onPasswordToggle = {},
                    onLoginClick = {}
                )
            }
        }
    }
}

@Composable
private fun LoginFormPreviewContent() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
        ) {
            LoginHeader()
            LoginForm(
                formState = LoginFormState(),
                loginState = LoginUiState.Idle,
                onUsernameChanged = {},
                onPasswordChanged = {},
                onPasswordToggle = {},
                onLoginClick = {}
            )
        }
    }
}
