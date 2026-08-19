package com.atk.atk_cargo.feature.auth.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.atk.atk_cargo.feature.auth.R
import com.atk.atk_cargo.feature.auth.viewmodel.AuthViewModel
import com.atk.atk_cargo.feature.auth.viewmodel.LoginFormState
import com.atk.atk_cargo.feature.auth.viewmodel.LoginUiState
import com.atk.atk_cargo.ui.theme.ATKCargoTheme
import com.atk.atk_cargo.ui.theme.WindowSizeClass
import com.atk.atk_cargo.ui.theme.rememberAdaptiveLayoutConfig
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = koinViewModel()
) {
    val loginState by viewModel.loginState.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()

    // ===== SIDE EFFECTS =====
    LaunchedEffect(loginState) {
        if (loginState is LoginUiState.Success) {
            onLoginSuccess()
        }
    }

    // ===== ADAPTIVE LAYOUT & THEME TOKENS =====
    val adaptiveConfig = rememberAdaptiveLayoutConfig()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (adaptiveConfig.windowSizeClass == WindowSizeClass.COMPACT) {
            // حالت عمودی گوشی (Portrait Layout)
            CompactLoginLayout(
                formState = formState,
                loginState = loginState,
                onUsernameChanged = viewModel::onUsernameChanged,
                onPasswordChanged = viewModel::onPasswordChanged,
                onPasswordToggle = viewModel::togglePasswordVisibility,
                onLoginClick = viewModel::login
            )
        } else {
            // حالت افقی گوشی یا تبلت (Dual-Column Expanded Layout)
            ExpandedLoginLayout(
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

@Composable
private fun CompactLoginLayout(
    formState: LoginFormState,
    loginState: LoginUiState,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onPasswordToggle: () -> Unit,
    onLoginClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .imePadding()
            .padding(horizontal = ATKCargoTheme.spacing.xl, vertical = ATKCargoTheme.spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ATKCargoTheme.spacing.l)
        ) {
            // کارت برندینگ (لوگو، نام سامانه و توضیح کوتاه)
            LoginBrandingCard()

            // کارت فرم ورود
            IndustrialLoginFormCard(
                formState = formState,
                loginState = loginState,
                onUsernameChanged = onUsernameChanged,
                onPasswordChanged = onPasswordChanged,
                onPasswordToggle = onPasswordToggle,
                onLoginClick = onLoginClick
            )
        }

        Spacer(modifier = Modifier.height(ATKCargoTheme.spacing.xl))
        IndustrialFooterInfo()
    }
}

@Composable
private fun ExpandedLoginLayout(
    formState: LoginFormState,
    loginState: LoginUiState,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onPasswordToggle: () -> Unit,
    onLoginClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(ATKCargoTheme.spacing.xl)
            .imePadding(),
        horizontalArrangement = Arrangement.spacedBy(ATKCargoTheme.spacing.xxl),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ستون سمت چپ: هدر تصویر پس‌زمینه + برندینگ و ویژگی‌های سامانه
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(ATKCargoTheme.dimensions.cardCornerRadius))
        ) {
            // تصویر پس‌زمینه صنعتی
            Image(
                painter = painterResource(R.drawable.login_bg),
                contentDescription = "تصویر پس‌زمینه هدر سامانه ATK Cargo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // گرادینت پوششی برای بالا بردن کنتراست متن
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
                            )
                        )
                    )
            )

            // محتوای هدر
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(ATKCargoTheme.spacing.xxl),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                IndustrialBrandingHeaderContent(isCompact = false)

                Spacer(modifier = Modifier.height(ATKCargoTheme.spacing.xl))

                IndustrialFeatureBadge(
                    icon = Icons.Default.Security,
                    title = "احراز هویت امن پرسنل",
                    subtitle = "کنترل دقیق سطوح دسترسی عملیاتی"
                )
                Spacer(modifier = Modifier.height(ATKCargoTheme.spacing.m))
                IndustrialFeatureBadge(
                    icon = Icons.Default.SignalCellularAlt,
                    title = "عملکرد پایدار آفلاین",
                    subtitle = "ثبت اطلاعات حتی هنگام قطع ارتباط شبکه"
                )
                Spacer(modifier = Modifier.height(ATKCargoTheme.spacing.m))
                IndustrialFeatureBadge(
                    icon = Icons.Default.CheckCircle,
                    title = "سازگار با تجهیزات صنعتی",
                    subtitle = "پشتیبانی کامل از بارکدخوان و اسکنر نوری"
                )
            }
        }

        // ستون سمت راست: فرم ورود صنعتی
        Column(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxHeight()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IndustrialLoginFormCard(
                formState = formState,
                loginState = loginState,
                onUsernameChanged = onUsernameChanged,
                onPasswordChanged = onPasswordChanged,
                onPasswordToggle = onPasswordToggle,
                onLoginClick = onLoginClick
            )

            Spacer(modifier = Modifier.height(ATKCargoTheme.spacing.l))
            IndustrialFooterInfo()
        }
    }
}

/**
 * کارت برندینگ سبک با آیکون لوگو، نام سامانه و توضیح کوتاه (مطابق طراحی جدید).
 */
@Composable
private fun LoginBrandingCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = ATKCargoTheme.semanticColors.cardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = ATKCargoTheme.dimensions.borderWidthThin,
            color = ATKCargoTheme.semanticColors.borderSubtle
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ATKCargoTheme.spacing.xl, vertical = ATKCargoTheme.spacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ATKCargoTheme.spacing.s)
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.LocalShipping,
                        contentDescription = "لوگوی سامانه ATK Cargo",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Text(
                text = "ATK Cargo",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "ENTERPRISE LOGISTICS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.8.sp
            )
            Text(
                text = "سامانه جامع مدیریت عملیات بارگیری",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun IndustrialBrandingHeaderContent(isCompact: Boolean) {
    Column(
        horizontalAlignment = if (isCompact) Alignment.CenterHorizontally else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(ATKCargoTheme.spacing.xs)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ATKCargoTheme.spacing.m)
        ) {
            Surface(
                modifier = Modifier.size(if (isCompact) 48.dp else 60.dp),
                shape = RoundedCornerShape(ATKCargoTheme.dimensions.buttonCornerRadius),
                color = MaterialTheme.colorScheme.primary,
                tonalElevation = ATKCargoTheme.elevation.level3
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.LocalShipping,
                        contentDescription = "لوگوی سامانه ATK Cargo",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(if (isCompact) 28.dp else 34.dp)
                    )
                }
            }

            Column {
                Text(
                    text = "ATK Cargo",
                    style = if (isCompact) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "ENTERPRISE LOGISTICS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = MaterialTheme.typography.labelMedium.letterSpacing
                )
            }
        }

        Spacer(modifier = Modifier.height(ATKCargoTheme.spacing.xxs))

        Text(
            text = "سامانه جامع مدیریت عملیات بارگیری",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = if (isCompact) TextAlign.Center else TextAlign.Start
        )
    }
}

@Composable
private fun IndustrialFeatureBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ATKCargoTheme.spacing.m),
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
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
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun IndustrialLoginFormCard(
    formState: LoginFormState,
    loginState: LoginUiState,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onPasswordToggle: () -> Unit,
    onLoginClick: () -> Unit
) {
    val passwordFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val haptic = LocalHapticFeedback.current

    val isLoginEnabled = formState.username.isNotBlank() &&
            formState.password.isNotBlank() &&
            loginState !is LoginUiState.Loading

    val isLoading = loginState is LoginUiState.Loading
    val errorMessage = (loginState as? LoginUiState.Error)?.message

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = ATKCargoTheme.semanticColors.cardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = ATKCargoTheme.dimensions.borderWidthThin,
            color = ATKCargoTheme.semanticColors.borderSubtle
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ATKCargoTheme.spacing.xl),
            verticalArrangement = Arrangement.spacedBy(ATKCargoTheme.spacing.l)
        ) {

            // Title & Helper
            Column(verticalArrangement = Arrangement.spacedBy(ATKCargoTheme.spacing.xxs)) {
                Text(
                    text = "ورود پرسنل",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "نام کاربری و رمز عبور عددی خود را وارد کنید",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Username Field
            IndustrialInputField(
                value = formState.username,
                onValueChange = onUsernameChanged,
                label = "نام کاربری",
                placeholder = "نام کاربری",
                fieldDescription = "فیلد نام کاربری",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "آیکون پرسنل",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(ATKCargoTheme.dimensions.iconSmall)
                    )
                },
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
                keyboardActions = KeyboardActions(
                    onNext = { passwordFocusRequester.requestFocus() }
                )
            )

            // Password Field (Numeric PIN)
            IndustrialInputField(
                value = formState.password,
                onValueChange = onPasswordChanged,
                label = "رمز عبور (فقط عدد)",
                placeholder = "••••",
                fieldDescription = "فیلد رمز عبور عددی",
                trailingIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(ATKCargoTheme.spacing.xs)
                    ) {
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onPasswordToggle()
                            },
                            modifier = Modifier.size(ATKCargoTheme.dimensions.touchTargetMin)
                        ) {
                            Icon(
                                imageVector = if (formState.isPasswordVisible)
                                    Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (formState.isPasswordVisible)
                                    "پنهان‌سازی رمز عبور" else "نمایش رمز عبور",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "آیکون قفل امنیتی",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(ATKCargoTheme.dimensions.iconSmall)
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
                        if (isLoginEnabled) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onLoginClick()
                        }
                    }
                ),
                focusRequester = passwordFocusRequester
            )

            // Error Banner (Animated Visibility & Accessibility LiveRegion)
            AnimatedVisibility(
                visible = errorMessage != null,
                enter = expandVertically(animationSpec = tween(ATKCargoTheme.motion.durationMedium2)) + fadeIn(),
                exit = shrinkVertically(animationSpec = tween(ATKCargoTheme.motion.durationShort2)) + fadeOut()
            ) {
                errorMessage?.let { msg ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                liveRegion = LiveRegionMode.Assertive
                                contentDescription = "خطای ورود: $msg"
                            },
                        shape = RoundedCornerShape(ATKCargoTheme.dimensions.buttonCornerRadius),
                        color = MaterialTheme.colorScheme.errorContainer,
                        border = androidx.compose.foundation.BorderStroke(
                            width = ATKCargoTheme.dimensions.borderWidthThin,
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(ATKCargoTheme.spacing.m),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(ATKCargoTheme.spacing.m)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(ATKCargoTheme.dimensions.iconDefault)
                            )
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Action Button (پرشده، تخت و بدون سایه، مطابق طراحی جدید)
            Button(
                onClick = {
                    keyboardController?.hide()
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLoginClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ATKCargoTheme.dimensions.buttonLargeHeight)
                    .semantics {
                        contentDescription = if (isLoading) "در حال تایید و ورود به سامانه..." else "دکمه ورود به سامانه"
                    },
                shape = RoundedCornerShape(13.dp),
                enabled = isLoginEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = ATKCargoTheme.elevation.level0,
                    pressedElevation = ATKCargoTheme.elevation.level0
                )
            ) {
                if (isLoading) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(ATKCargoTheme.spacing.m),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.5.dp
                        )
                        Text(
                            text = "در حال ارتباط با سرور...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = "ورود به سامانه عملیات",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun IndustrialInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    fieldDescription: String,
    leadingIcon: @Composable (() -> Unit)? = null,
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
        verticalArrangement = Arrangement.spacedBy(ATKCargoTheme.spacing.xs)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = ATKCargoTheme.spacing.xxs)
        )

        // فیلد پرشده و بدون حاشیه (Pill Input) مطابق طراحی جدید
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = ATKCargoTheme.dimensions.inputMinHeight)
                .then(
                    if (focusRequester != null) Modifier.focusRequester(focusRequester)
                    else Modifier
                )
                .semantics { contentDescription = fieldDescription },
            shape = RoundedCornerShape(13.dp),
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
                focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                focusedContainerColor = ATKCargoTheme.semanticColors.inputBackground,
                unfocusedContainerColor = ATKCargoTheme.semanticColors.inputBackground,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun IndustrialFooterInfo() {
    Text(
        text = "شرکت امین تجار خوزستان • کلیه حقوق محفوظ است",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.outline,
        textAlign = TextAlign.Center
    )
}

@Preview(name = "Compact - Industrial Light Header Image", showBackground = true, locale = "fa")
@Composable
private fun LoginScreenLightPreview() {
    ATKCargoTheme(darkTheme = false) {
        Surface(modifier = Modifier.fillMaxSize()) {
            CompactLoginLayout(
                formState = LoginFormState(username = "admin"),
                loginState = LoginUiState.Idle,
                onUsernameChanged = {},
                onPasswordChanged = {},
                onPasswordToggle = {},
                onLoginClick = {}
            )
        }
    }
}

@Preview(name = "Compact - Industrial Dark Header Image", showBackground = true, locale = "fa")
@Composable
private fun LoginScreenDarkPreview() {
    ATKCargoTheme(darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize()) {
            CompactLoginLayout(
                formState = LoginFormState(username = "operator_1"),
                loginState = LoginUiState.Idle,
                onUsernameChanged = {},
                onPasswordChanged = {},
                onPasswordToggle = {},
                onLoginClick = {}
            )
        }
    }
}

@Preview(name = "Compact - Error State", showBackground = true, locale = "fa")
@Composable
private fun LoginScreenErrorPreview() {
    ATKCargoTheme(darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize()) {
            CompactLoginLayout(
                formState = LoginFormState(username = "981042", password = "123"),
                loginState = LoginUiState.Error("شناسه پرسنلی یا رمز عبور اشتباه است."),
                onUsernameChanged = {},
                onPasswordChanged = {},
                onPasswordToggle = {},
                onLoginClick = {}
            )
        }
    }
}

@Preview(name = "Expanded - Tablet View Header Image", showBackground = true, device = "spec:width=1280dp,height=800dp,dpi=240", locale = "fa")
@Composable
private fun LoginScreenTabletPreview() {
    ATKCargoTheme(darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize()) {
            ExpandedLoginLayout(
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
