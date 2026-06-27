package com.safarsakha.presentation.screens.admin.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Design tokens (matching UserProfileScreen) ──────────────────────────────
private val NavyColor = Color(0xFF0F172A)
private val SkyColor = Color(0xFF0EA5E9)
private val CreamColor = Color(0xFFF4E7D3)
private val ErrorColor = Color(0xFFDC2626)
private val SlateColor = Color(0xFF64748B)
private val BorderColor = Color(0xFFE2E8F0)
private val BgColor = Color(0xFFFFFFFF)

@Composable
fun AdminLoginScreen(
    viewModel: AdminLoginViewModel,
    onLoginSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Password visibility toggle state (UI only — no business logic)
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoginSuccess) {
        if (uiState.isLoginSuccess) {
            onLoginSuccess()
            viewModel.onEvent(AdminLoginEvent.NavigationDone)
        }
    }

    // ── ROOT ────────────────────────────────────────────────────────────
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BgColor
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // ── BRAND HEADER ──────────────────────────────────────────
                BrandHeader()

                Spacer(modifier = Modifier.height(36.dp))

                // ── LOGIN CARD ────────────────────────────────────────────
                LoginCard(
                    email = uiState.email,
                    password = uiState.password,
                    isLoading = uiState.isLoading,
                    error = uiState.errorMessage,
                    passwordVisible = passwordVisible,
                    onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
                    onEmailChange = { viewModel.onEvent(AdminLoginEvent.EmailChanged(it)) },
                    onPasswordChange = { viewModel.onEvent(AdminLoginEvent.PasswordChanged(it)) },
                    onLoginClick = { viewModel.onEvent(AdminLoginEvent.LoginClicked) }
                )
            }
        }
    }
}

// =============================================================================
// BRAND HEADER
// =============================================================================

@Composable
private fun BrandHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "SafarSakha Admin",
            fontSize = 38.sp,
            fontWeight = FontWeight.ExtraBold,
            color = NavyColor,
            letterSpacing = (-1.2f).sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .size(width = 32.dp, height = 3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(SkyColor.copy(alpha = 0.9f), SkyColor.copy(alpha = 0.25f))
                    )
                )
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Manage your platform with ease",
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            color = SlateColor,
            letterSpacing = 0.15f.sp,
            textAlign = TextAlign.Center
        )
    }
}

// =============================================================================
// LOGIN CARD
// =============================================================================

@Composable
private fun LoginCard(
    email: String,
    password: String,
    isLoading: Boolean,
    error: String?,
    passwordVisible: Boolean,
    onPasswordVisibilityToggle: () -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = NavyColor.copy(alpha = 0.04f),
                spotColor = NavyColor.copy(alpha = 0.08f)
            )
            .shadow(
                elevation = 14.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = NavyColor.copy(alpha = 0.02f),
                spotColor = NavyColor.copy(alpha = 0.05f)
            )
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White.copy(alpha = 0.94f))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        CreamColor.copy(alpha = 0.85f),
                        BorderColor.copy(alpha = 0.60f),
                        BorderColor.copy(alpha = 0.25f)
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(
                start = 28.dp, end = 28.dp, top = 32.dp, bottom = 28.dp
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Admin Access",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = NavyColor,
                letterSpacing = (-0.5f).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Sign in to manage your platform",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = SlateColor
            )
            Spacer(modifier = Modifier.height(28.dp))

            // Email
            PremiumTextField(
                value = email,
                onValueChange = onEmailChange,
                label = "Admin Email",
                placeholder = "admin@safarsakha.com",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password
            PremiumTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = "Password",
                placeholder = "Enter your password",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = onPasswordVisibilityToggle,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (passwordVisible)
                                Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = if (passwordVisible) "Hide" else "Show",
                            modifier = Modifier.size(18.dp),
                            tint = SlateColor
                        )
                    }
                },
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            // Error
            if (error != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ErrorColor.copy(alpha = 0.06f))
                        .border(1.dp, ErrorColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = error,
                        fontSize = 13.sp,
                        color = ErrorColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Login Button
            Button(
                onClick = onLoginClick,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NavyColor,
                    contentColor = Color.White,
                    disabledContainerColor = NavyColor.copy(alpha = 0.45f),
                    disabledContentColor = Color.White.copy(alpha = 0.60f)
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    hoveredElevation = 2.dp
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "Login as Admin",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.1f.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(Modifier.weight(1f).height(1.dp).background(BorderColor))
                Text(
                    text = "Admin Portal",
                    fontSize = 12.sp,
                    color = SlateColor.copy(alpha = 0.65f)
                )
                Box(Modifier.weight(1f).height(1.dp).background(BorderColor))
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Admin note
            Text(
                text = "Secure access for platform administrators",
                fontSize = 12.sp,
                color = SlateColor.copy(alpha = 0.55f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// =============================================================================
// PREMIUM OUTLINED TEXT FIELD
// =============================================================================

@Composable
private fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label, fontSize = 14.sp) },
        placeholder = {
            Text(text = placeholder, fontSize = 14.sp, color = SlateColor.copy(alpha = 0.45f))
        },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        enabled = enabled,
        singleLine = true,
        interactionSource = remember { MutableInteractionSource() },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SkyColor,
            focusedLabelColor = SkyColor,
            focusedLeadingIconColor = SkyColor,
            focusedTextColor = NavyColor,
            focusedContainerColor = Color.Transparent,
            unfocusedBorderColor = BorderColor,
            unfocusedLabelColor = SlateColor,
            unfocusedLeadingIconColor = SlateColor,
            unfocusedTextColor = NavyColor,
            unfocusedContainerColor = Color.Transparent,
            disabledBorderColor = BorderColor.copy(alpha = 0.45f),
            disabledLabelColor = SlateColor.copy(alpha = 0.35f),
            disabledTextColor = NavyColor.copy(alpha = 0.35f),
            disabledContainerColor = BorderColor.copy(alpha = 0.12f),
            errorBorderColor = ErrorColor,
            errorLabelColor = ErrorColor,
            errorTextColor = NavyColor,
            errorContainerColor = Color.Transparent
        ),
        modifier = modifier
    )
}