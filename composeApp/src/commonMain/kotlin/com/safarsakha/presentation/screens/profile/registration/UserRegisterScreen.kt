package com.safarsakha.presentation.screens.profile.registration

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Premium Design Tokens ────────────────────────────────────────────────────
private val NavyColor = Color(0xFF0F172A)
private val SkyColor = Color(0xFF0EA5E9)
private val SlateColor = Color(0xFF64748B)
private val BorderColor = Color(0xFFE2E8F0)
private val BgColor = Color(0xFFFFFFFF)
private val LightBgColor = Color(0xFFF8FAFC)
private val ErrorColor = Color(0xFFDC2626)
private val ErrorBgColor = Color(0xFFFEF2F2)

@Composable
fun UserRegisterScreen(
    modifier: Modifier = Modifier,
    viewModel: UserRegisterViewModel,
    onBackToLogin: () -> Unit,
    onRegistrationSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle successful registration
    LaunchedEffect(uiState.isRegistrationSuccess) {
        if (uiState.isRegistrationSuccess) {
            onRegistrationSuccess()
            viewModel.onEvent(UserRegisterEvent.OnResetSuccess)
        }
    }

    // Handle error display
    if (uiState.error != null) {
        LaunchedEffect(uiState.error) {
            // Error is displayed in the UI, can add snackbar logic here
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = LightBgColor
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 32.dp),
                verticalArrangement = Arrangement.Center
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                }

                Text(
                    text = "Create Account",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyColor,
                    letterSpacing = (-0.5f).sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Join us and start your journey",
                    fontSize = 15.sp,
                    color = SlateColor,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(32.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 2.dp,
                            shape = RoundedCornerShape(24.dp),
                            ambientColor = NavyColor.copy(alpha = 0.04f),
                            spotColor = NavyColor.copy(alpha = 0.08f)
                        )
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(24.dp),
                            ambientColor = NavyColor.copy(alpha = 0.02f),
                            spotColor = NavyColor.copy(alpha = 0.04f)
                        ),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = BgColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = BorderColor.copy(alpha = 0.60f),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = "Register",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyColor,
                            letterSpacing = (-0.2f).sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Custom styling map shared across premium text inputs
                        val textFieldColors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SkyColor,
                            unfocusedBorderColor = BorderColor,
                            disabledBorderColor = BorderColor.copy(alpha = 0.5f),
                            focusedLabelColor = SkyColor,
                            unfocusedLabelColor = SlateColor,
                            focusedContainerColor = LightBgColor.copy(alpha = 0.5f),
                            unfocusedContainerColor = LightBgColor.copy(alpha = 0.5f)
                        )

                        OutlinedTextField(
                            value = uiState.name,
                            onValueChange = { viewModel.onEvent(UserRegisterEvent.OnNameChanged(it)) },
                            label = { Text("Full Name", fontWeight = FontWeight.Medium) },
                            placeholder = { Text("John Doe") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !uiState.isLoading,
                            shape = RoundedCornerShape(12.dp),
                            colors = textFieldColors,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = uiState.email,
                            onValueChange = { viewModel.onEvent(UserRegisterEvent.OnEmailChanged(it)) },
                            label = { Text("Email Address", fontWeight = FontWeight.Medium) },
                            placeholder = { Text("user@example.com") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !uiState.isLoading,
                            shape = RoundedCornerShape(12.dp),
                            colors = textFieldColors,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = uiState.phoneNumber,
                            onValueChange = { viewModel.onEvent(UserRegisterEvent.OnPhoneNumberChanged(it)) },
                            label = { Text("Phone Number", fontWeight = FontWeight.Medium) },
                            placeholder = { Text("+1234567890") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !uiState.isLoading,
                            shape = RoundedCornerShape(12.dp),
                            colors = textFieldColors,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = uiState.password,
                            onValueChange = { viewModel.onEvent(UserRegisterEvent.OnPasswordChanged(it)) },
                            label = { Text("Password", fontWeight = FontWeight.Medium) },
                            placeholder = { Text("Minimum 6 characters") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !uiState.isLoading,
                            shape = RoundedCornerShape(12.dp),
                            colors = textFieldColors,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = uiState.confirmPassword,
                            onValueChange = { viewModel.onEvent(UserRegisterEvent.OnConfirmPasswordChanged(it)) },
                            label = { Text("Confirm Password", fontWeight = FontWeight.Medium) },
                            placeholder = { Text("Re-enter your password") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !uiState.isLoading,
                            shape = RoundedCornerShape(12.dp),
                            colors = textFieldColors,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            )
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { viewModel.onEvent(UserRegisterEvent.OnRegisterClick) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            enabled = !uiState.isLoading,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyColor),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.5.dp,
                                    color = Color.White
                                )
                            } else {
                                Text(
                                    text = "Register",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Premium UI error alert block design
                        if (uiState.error != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ErrorColor.copy(alpha = 0.06f))
                                    .border(1.dp, ErrorColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("⚠️ ", fontSize = 12.sp)
                                    Text(
                                        text = uiState.error!!,
                                        color = ErrorColor,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Already have an account?",
                            fontSize = 13.sp,
                            color = SlateColor,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        TextButton(
                            onClick = onBackToLogin,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isLoading
                        ) {
                            Text(
                                text = "Back to Login",
                                color = SkyColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}